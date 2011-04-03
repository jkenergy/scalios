/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-01 01:57:59 +0000 (Sat, 01 Mar 2008) $
 * $LastChangedRevision: 634 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/disp.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: update comments for the extended task dispatch to reflect restorecx being a flag not a function pointer $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

/* This file is compiled three ways:
 * 
 * 1) BASIC_TASKS_ONLY defined: the system has only basic tasks, and all references to extended tasks can be compiled-out.
 * 2) EXTENDED_TASKS_ONLY defined: the system has only extended tasks, and all references to basic tasks can be compiled-out.
 * 3) MIXED_TASKS defined: both basic and extended tasks must be supported.
 *
 * Exactly one of the above must be defined. 
 */
 
#include <osint.h>

/* 
 * Generic hook wrapper function.
 * Hooks are called with all tasks and category 2 interrupts locked out, and with stack checking on within
 * a special hook context on the kernel stack.
 * 
 * On entry:
 * 
 * Kernel is locked/stack checking off
 * SP is on the kernel stack
 * os_curtask is the task for which hook is been called.
 *
 * On exit:
 * Kernel is locked/stack checking off
 * SP is on the kernel stack
 * os_curtask is the task for which hook is been called.
 * curtos is undefined (and thus needs initialising after calls to call_hook)
 */

#if !defined(BASIC_TASKS_ONLY) && !defined(EXTENDED_TASKS_ONLY) && !defined(MIXED_TASKS)
#error One of BASIC_TASKS_ONLY, EXTENDED_TASKS_ONLY or MIXED_TASKS must be defined
#endif

#ifdef BASIC_TASKS_ONLY
#define KS_DISPATCH os_ks_dispatch_bt
#endif
#ifdef EXTENDED_TASKS_ONLY
#define KS_DISPATCH os_ks_dispatch_et
#endif
#ifdef MIXED_TASKS
#define KS_DISPATCH os_ks_dispatch_mix
#endif

#ifdef STACK_CHECKING
static void call_prepost_hook(os_hookf hook, nat stackoffset)
#else
static void call_prepost_hook(os_hookf hook)
#endif
{
	assert(os_curpri == os_kernelpri);
	/* @todo could use this function to implement exception handling functions (at same point as pre-task hooks,
	 * i.e. on task entry and resumption */
	assert(KERNEL_LOCKED());
	assert(ON_KERNEL_STACK());

#ifdef STACK_CHECKING
	OFFSET_CURTOS_HOOK(stackoffset);	/* offset curtos from the current SP by the specified number of bytes */
#endif

	/* @todo setup global flag to indicate hook is executing */

	ENABLE_STACKCHECK();

	MARK_OUT_KERNEL();					/* drop out of the kernel, keeping IPL at kernel level  */
	hook();								/* call the hook routine with IPL set to kernel level and stack checking on */
	MARK_IN_KERNEL();					/* go back into the kernel, disabling stack checking */
	DISABLE_STACKCHECK();				/* disable stack checking (if enabled) */
}

#if defined(EXTENDED_TASKS_ONLY) || defined(MIXED_TASKS)
/* Handler to create a new context from scratch (extended task has never run or
 * has terminated).
 * 
 * The corresponding function restores a context from an extended task that was
 * blocked or preempted (the code is target-specific and typically written in
 * assembly language; see os_switch2ext() function).
 */
void os_runcreatecx(void)
{
	assert(!ON_KERNEL_STACK());										/* should be on extended stack when called */
	
	os_curtask->dyn->restore = 1U;									/* restore is likely to be in assembly language; restores an existing context saved on the stack */
	
	ENABLE_STACKCHECK();											/* Setup top of stack for task to be entered, so now enable stack overflow checking */
	
	LEAVE_KERNEL();
	
	os_curtask->entry();
	
	/* The task should never return to here under OSEK rules (all tasks must terminate with TerminateTask), but just in case
	 * a mistake has been made by the programmer we handle a run off the end of the task function as an implict
	 * TerminateTask call. AUTOSAR requires that this is handled gracefully (and by reporting an error).
	 * 
	 * See also the basic task dispatch function in this file: it performs the same functionality for basic tasks
	 * that run off the end.
	 */

	ENTER_KERNEL_DIRECT();											/* Go straight to kernel level - don't try and preserve previous IPL since never coming back */
	/* Three steps:
	 * 
	 * 1. Auto release all locked resources.
	 * 2. Enable interrupts (i.e. put the "fast" interrupt macro counters back to zero).
	 * 3. Call the error hook.
	 * 
	 * Note that (2) must come before (3) since the error hook may make an API call and this would
	 * otherwise return an error (E_OS_DISABLEDINT).
	 */
#ifdef OS_EXTENDED_STATUS
	/* Autorelease of resources on termination. Note that this is only required for extended status
	 * since it is only the extended status error checking code that breaks if a task terminates
	 * while holding resources (in standard status the only dynamic data stored in a resource is
	 * the previous priority level, and that is overwritten anyway when the resource is next locked).
	 */
	os_tidylockedresources();								/* $Req: artf1042 $ */
#endif
	/* Clear out flags for fast interrupt macros: makes sure that these calls operate correctly going forward.
	 */
	CLEAR_INTERRUPTS();										/* $Req: artf1049 $ */
		
	if (os_flags.errorhook) {
		/* call the error hook handler: E_OS_MISSINGEND on autoterminate */
		OS_ERRORHOOK_0(NoServiceId, E_OS_MISSINGEND);		/* $Req: artf1041 $ */
	}
	
	os_terminate();											/* Autoterminate on return from task entry function $Req: artf1038 $ */
	NOT_REACHED();
}

/* blocks the current (extended) task */
void os_block(void)
{
	os_stackp newsp;
	
	assert(KERNEL_LOCKED());
	assert(!ON_KERNEL_STACK());										/* Cannot block a basic task */
	assert(os_curtask->dyn->restore == 1U);							/* Make sure we restore back to here */
	
	DISABLE_STACKCHECK();
	
	newsp = os_kssp; os_kssp = 0;											/* Pick up the saved kernel stack pointer; set it to zero to mark that we are on the kernel stack */
	os_save2ks(STOP, &os_curtask->dyn->savesp, newsp);					/* Save the current stack, switch to the kernel and block */
	
	/* EXECUTION RESUMES FROM HERE WHEN UNBLOCKED */
	
	/* Priority is equal to the dispatch priority of the task; in effect the internal resources are re-occupied */
	assert(os_curpri == os_curtask->boostpri);							/* $Req: artf1102 $ */

	ENABLE_STACKCHECK();
}

/* (Continue to) run an extended task until terminate/blocked or a higher priority task should preempt and run
 *
 * This function ends up as a 'stub' call; the os_switch2ext functions is in assembly language and performs a stack switch and a jump. Later
 * the os_switch2ext function returns (with a code to indicate why the extended task has stopped running) and the call completes.
 */
static void exttaskdispatch(void)
{
	unat action;

	assert(ON_KERNEL_STACK());		/* should be on kernel stack when called */
	
#ifdef STACK_CHECKING
	os_curtos = os_curtask->tos;		/* @todo Offline tool needs to calibrate an offset to make sure things line up right */
#endif
	

  	
	for(;;) {			
		/* The os_switch2ext call saves the current top of stack into os_kssp, sets the stack to the saved stack pointer to
		 * the extended task's stack, and then calls the entry function of the extended task or os_runcreatecx() to 'resume
		 * the saved context' depending on whether the extended task has preempted/blocked or has terminated/never run.
		 *
		 * $Req: artf1095 $
		 * 
		 * The os_switch2ext call returns a code to indicate whether preemption should occur (RUNDISPATCH),
		 * whether the extended task has blocked or terminated (STOP).
		 */
		assert(KERNEL_LOCKED());
		OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_RESUME(os_curtask); 
		action = os_switch2ext(&os_kssp, os_curtask->dyn->savesp, os_curtask->dyn->restore, os_curtask->initsp);
		OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_FINISH(os_curtask);		
		
		if (action == RUNDISPATCH) {
			/* Got here because of a switch across from the extended task's stack in the dispatch call. We couldn't
			 * call dispatch directly because the overhead would build up on the wrong stack. So we call it here
			 * after we have switched stacks back (and done a context save, of course).
			 */
			assert(KERNEL_LOCKED());
			assert(ON_KERNEL_STACK());			/* should be back on kernel stack by now */	
			
			KS_DISPATCH();
			
			/* Now all the higher priority tasks have completed; will restore the original extended task by
			 * looping around and calling os_switch2ext again. The original dispatch call on that other stack will
			 * return to its caller.
			 */
		}
		else { /* Extended task now wants to stop execution, either by terminating or blocking */
			/* will be back on KS by this point */
			assert(KERNEL_LOCKED());

			break;						/* Return from exttaskdispatch; extended task does not want to run any more */
		}
	}
	/* This function only terminates when the extended task terminates or blocks */
	assert(action == STOP);
		
	assert(ON_KERNEL_STACK());			/* should be back on kernel stack when returning */	
	
	assert(STACKCHECK_OFF());
}
#endif

#if defined(BASIC_TASKS_ONLY) || defined(MIXED_TASKS)
/* run a basic task until terminate */
static void basictaskdispatch(void)
{
	/* Creating (and saving) a jmp_buf so that the newly-run basic task can terminate via a longjmp call inside a TerminateTask call */
	JMP_BUF * save_curenv = os_curenv;				/* save the current environment buffer prior to overwrite for basic about to be entered */
	JMP_BUF env;									/* This buffer is only needed for basic tasks */
	os_curenv = &env;								/* Set the accessor to the new jump buffer */

													/* Set the top of stack (for error checking etc.). The top of stack is the 
													 * stack usage value for basic task (offline tool needs to calibrate an offset so
												 	 * that everything is lined up right) plus the current stack pointer
													 * 
													 * @todo lint reports "suspicious cast", which is right. This is probably
													 * a target-specific function since stack points have all kinds of restrictions.
													 * Move this to target-specific header somewhere.
													 */

#ifdef STACK_CHECKING
	OFFSET_CURTOS_TASK(os_curtask->stackoffset);		/* offset curtos from the current SP by the specified number of bytes */
#endif
													 
	if (SETJMP(*os_curenv) == 0) {					
													/* This half of the 'if' is the half that is the run-on continuation */
		assert(os_kssp == 0);							/* All basic tasks run on the kernel stack */
		
		/* The priority to go to when exiting the kernel is set to the new task's boost priority,
		 * which is the highest ceiling of any of the internal resources used by the task
		 * (note that we don't actually have to implement an actual internal resource because of this)
		 */
		
		ENABLE_STACKCHECK();						/* Setup top of stack for task to be entered, so now enable stack overflow checking */
		
		LEAVE_KERNEL();
		/* Now running as user task with IPL of 0; pending cat 2 ISRs will come in */
		/* AT THIS POINT WE CAN BE PREEMPTED BY OTHER TASKS */
		OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_START(os_curtask);
		os_curtask->entry();							/* Go run user code; $Req: artf1095 $ */
		OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_FINISH(os_curtask);
		ENTER_KERNEL_DIRECT();						/* Go straight to kernel IPL - (don't keep it high if it was) */
		
		/* Task should not come back via this route because it ought to
		 * end with a TerminateTask call, which ought to take the code back to where the setjmp() call was made.
		 * 
		 * There are three things we need to do if we "fall off the end" of a task entry function:
		 * 
		 * 1. Tidy up the locked resources (unlock all the ones we held).
		 * 2. Clear down the interrupts if they were locked (by the "fast" interrupt macros).
		 * 3. Signal the error to the user via the E_OS_MISSINGEND code.
		 */
		
#ifdef OS_EXTENDED_STATUS
		/* Autorelease of resources on termination. Note that this is only required for extended status
		 * since it is only the error checking code that breaks if a task terminates while holding resources.
		 *
		 * Note that there is no need for any code to autorelease resources in standard status because
		 * the only dynamic data stored in a resource is the previous priority level, and that is overwritten
		 * anyway (when next locked).
		 */
		os_tidylockedresources();								/* $Req: artf1042 $ */
#endif
		/* Clear out flags for fast interrupt macros: makes sure that these calls operate correctly going forward.
		 * The IPL will be set appropriately going forward since the code to leave the kernel sets the IPL
		 * to the value implied by os_curpri.
		 * 
		 * Note: must clear out interrupts prior to calling the error hook, since if the error hook made an API
		 * call then the call would fail due to interrupts being locked.
		 */
		CLEAR_INTERRUPTS();										/* $Req: artf1049 $ */

		if (os_flags.errorhook) {
			/* call the error hook handler */
			OS_ERRORHOOK_0(TerminateTask, E_OS_MISSINGEND);		/* $Req: artf1041 $ */
		}
		/* No further code is required for termination of a basic task since the terminate function would simply return to
		 * the 'else' part of this 'if' anyway.
		 * 
		 * $Req: artf1038 $
		 */
	}
	else {
		/* This half of the 'if' is the half that is RETURNING from a longjmp.
		 *
		 * We have come back from a TerminateTask call; we are in the kernel.
		 * For the TerminateTask call to succeed in extended status then there must have been
		 * no resources locked, and the interrupts must have been cleared. In standard
		 * status this might not be true, but is an application error.
		 */		
	}
	
	assert(KERNEL_LOCKED());
	
	os_curenv = save_curenv;							/* restore the previous environment buffer */
	
	/* basic task has now finished (task moving to suspended state) */
	DISABLE_STACKCHECK();
}
#endif

/* Forces termination of the current task. This function never returns */
#ifdef BASIC_TASKS_ONLY
void os_terminate_bt(void)
#endif
#ifdef EXTENDED_TASKS_ONLY
void os_terminate_et(void)
#endif
#ifdef MIXED_TASKS
void os_terminate_mix(void)
#endif
{
	assert(KERNEL_LOCKED());

#ifdef MIXED_TASKS	
	if (ON_KERNEL_STACK()) {										/* What type of task are we terminating? */
#endif
#if defined(MIXED_TASKS) || defined(BASIC_TASKS_ONLY)		
		assert(BASICTASK(os_curtask));
		
		/* $Req: artf1130 $ */
		LONGJMP(*os_curenv, 1);										/* Basic task termination; simply jump back to the dispatch
														 			 * function where we came from
														 			 */
		NOT_REACHED();
#endif
#ifdef MIXED_TASKS
	}
	else {															/* Extended task termination */
#endif
#if defined(MIXED_TASKS) || defined(EXTENDED_TASKS_ONLY)
		os_stackp newsp;

		assert(EXTENDEDTASK(os_curtask));

		DISABLE_STACKCHECK();										/* Turn off stack checking prior to switching to kernel stack. */

		/* No need to do os_curtask->dyn->savesp = os_curtask->initsp since will be taken care of by os_switch2ext() */
		os_curtask->dyn->restore = 0;									/* When this task is run next the context will be created from scratch */
		newsp = os_kssp; os_kssp = 0;										/* Pick up the saved kernel stack pointer; set it to zero to mark that we are on the kernel stack */
		os_jump2ks(STOP, newsp);
		NOT_REACHED();
#endif
#ifdef MIXED_TASKS
	}
#endif
}


/* MAIN WORKER FUNCTION OF KERNEL
 *
 * Runs all higher priority tasks then returns to resume the current task.
 * 
 * Called when there is at least one task with a base priority higher than the current priority of the
 * currently running task.
 */
void KS_DISPATCH(void)
{
	/* Target-specific save (if any)
	 * 
	 * This allows local variables (stored on the stack) to be included here as things that
	 * are preserved across calls; it is an easy way to stored bits of context at the C
	 * language level without relying on assembly language, which is more difficult to maintain.
	 */	
	/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	DECLARE_SAVE_TARGET_TASK_CONTEXT
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
	TaskType save_curtask;
#ifdef STACK_CHECKING
	os_stackp save_curtos;
#endif
	os_pri save_curpri;
#ifdef OS_EXTENDED_STATUS
	ResourceType save_curlastlocked;
#endif
	
	OS_API_TRACE_DISPATCH_TOP();

	assert(KERNEL_LOCKED());
	assert(os_nexttask);
	assert(TASK_SWITCH_PENDING(os_curpri));						/* dispatch() should only be called when there is something higher priority to run */
	assert(ON_KERNEL_STACK());
	assert(STACKCHECK_OFF());									/* stack checking should be off when dispatch is called */

	/* Save current context that is going to be changed,
	 * there maybe more depending on task type, see basictaskdispatch() and exttaskdispatch() */
		
	save_curtask = os_curtask;
#ifdef STACK_CHECKING
	save_curtos = os_curtos;
#endif
	save_curpri = os_curpri;
#ifdef OS_EXTENDED_STATUS
	save_curlastlocked = os_curlastlocked;
#endif
	os_curpri = os_kernelpri;			/* When LEAVE_KERNEL() in an API function is called by a pre or post task hook
										 * it will set the IPL to kernel level, keeping cat2 ISRs locked out.
										 */

	/* Call post task hook here, since os_curtask has just been preempted (unless os_idle task, i.e. called from StartOS) */
	if(os_flags.posttaskhook && os_curtask != OS_IDLE_TASK) {
		/* $Req: artf1118 $ */
		OS_API_TRACE_POSTTASK_HOOK();
		
		OS_KERNEL_TRACE_DISPATCH_POSTHOOK_START();
#ifdef STACK_CHECKING
		call_prepost_hook(&PostTaskHook, os_posttask_hook_offset);
#else
		call_prepost_hook(&PostTaskHook);
#endif
		OS_KERNEL_TRACE_DISPATCH_POSTHOOK_FINISH();
	}
	
	do {		/* dispatch os_nexttask while there are higher priority tasks to run */
		assert(os_nexttask != OS_IDLE_TASK);
		os_curtask = os_nexttask;
		OS_API_TRACE_DISPATCH(os_curtask);
		assert(KERNEL_LOCKED());								/* ... we should still be in the kernel */

		/* Call pre task hook here, since os_curtask is about to be entered (or resumed after block) */
		if (os_flags.pretaskhook) {
			/* $Req: artf1119 $ */
			OS_API_TRACE_PRETASK_HOOK();
			OS_KERNEL_TRACE_DISPATCH_PREHOOK_START();
#ifdef STACK_CHECKING
			call_prepost_hook(&PreTaskHook, os_pretask_hook_offset);
#else
			call_prepost_hook(&PreTaskHook);
#endif
			OS_KERNEL_TRACE_DISPATCH_PREHOOK_FINISH();
		}

#ifdef OS_EXTENDED_STATUS			
		os_curlastlocked = 0; 
#endif
		os_curpri = os_curtask->boostpri;					/* $Req: artf1102 $ */
	
		assert(STACKCHECK_OFF());							/* stack checking should be off */

#ifdef EXTENDED_TASKS_ONLY
		exttaskdispatch();
#endif
#ifdef BASIC_TASKS_ONLY
		basictaskdispatch();
#endif
#ifdef MIXED_TASKS
		if (EXTENDEDTASK(os_curtask)) {						/* If the task to run is an extended one ... */
			exttaskdispatch();								/* ... then go and run it (a bit tricky so done in another function) */
			/* extended task has either terminated or blocked when reached here, will be back on KS here */
		} 
		else {			
			basictaskdispatch();							/* run the basic task */				
		}
#endif
		/* both extended and basic tasks reach here after termination (or blocked if extended task) */			
		assert(STACKCHECK_OFF());							/* stack checking should be off */
		
		assert(ON_KERNEL_STACK());							/* should always be on the kernel stack at this point */
		
		/* When we return from running a task the kernel is locked; any interrupt state on terminate/block is discarded.
		 * Returning to the previously running task (via LEAVE_KERNEL() macro) restores the appropriate interrupt level
		 * prior to the task switch. $Req: artf1049 $
		 */
		assert(KERNEL_LOCKED());
		os_curpri = os_kernelpri;			/* Put os_curpri back to os_kernelpri for any remaining hook calls (see
											 * earlier). Ensures that API calls made from hooks do not set the IPL below
											 * OS level (i.e. ensure that cat2 ISRs do not interrupt hooks).
											 */

		/* Call post task hook here, since os_curtask has just terminated/blocked (not been preempted) */
		if (os_flags.posttaskhook) {
			OS_API_TRACE_POSTTASK_HOOK();
			
			OS_KERNEL_TRACE_DISPATCH_POSTHOOK_START();
#ifdef STACK_CHECKING
			call_prepost_hook(&PostTaskHook, os_posttask_hook_offset);
#else
			call_prepost_hook(&PostTaskHook);
#endif
			OS_KERNEL_TRACE_DISPATCH_POSTHOOK_FINISH();
		}

		/* Take the task out of the priority queue and identify the os_nexttask to be run */
		os_dequeuetask();										

		/* check if a chained task needs to be activated (queued) */
		if (os_chaintask) {
			os_queuetask(os_chaintask);
			/* No need to invoke DISPATCH_IF_TASK_SWITCH_PENDING() since already in dispatch loop (i.e. the while loop will dispatch the next task) */
			os_chaintask = 0;
		}

		/* Run a task; loop back around to see if there are more higher priority tasks to run (and run them) */
	}
	while(TASK_SWITCH_PENDING(save_curpri)); 				/* While there are higher priority tasks to run */
		
	/* No more higher priority tasks to run; can fall back to the preempted task and resume it */

	assert(KERNEL_LOCKED());									/* Still in the kernel */
		
	assert(ON_KERNEL_STACK());								/* Should always be on the kernel stack at this point */
		
	/* Restore previous context */
		
	/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	RESTORE_TARGET_TASK_CONTEXT								
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

#ifdef OS_EXTENDED_STATUS
	os_curlastlocked = save_curlastlocked;
#endif
	os_curtask = save_curtask;
		
	/* Call pre task hook here, since os_curtask is about to be resumed (after been preempted) */
	if (os_flags.pretaskhook && os_curtask != OS_IDLE_TASK) {
		OS_API_TRACE_PRETASK_HOOK();
		
		OS_KERNEL_TRACE_DISPATCH_PREHOOK_START();
#ifdef STACK_CHECKING
		call_prepost_hook(&PreTaskHook, os_pretask_hook_offset);
#else
		call_prepost_hook(&PreTaskHook);
#endif
		OS_KERNEL_TRACE_DISPATCH_PREHOOK_FINISH();
	}

	/* Restore curpri after hook has been called since hook must run with curpri set to kernel level (see earlier) */
	os_curpri = save_curpri;
	
#ifdef STACK_CHECKING
	/* restore curtos after hook has been called, since it trashes this value */
	os_curtos = save_curtos;
#endif

	assert(STACKCHECK_OFF());								/* stack checking should be off when dispatch has finished */
	OS_API_TRACE_DISPATCH_BOTTOM();
}

#ifndef BASIC_TASKS_ONLY

#ifdef EXTENDED_TASKS_ONLY
void os_swst_dispatch_et(void)
#endif
#ifdef MIXED_TASKS
void os_swst_dispatch_mix(void)
#endif
{
	if (ON_KERNEL_STACK()) {													
		KS_DISPATCH();				/* This calls either os_ks_dispatch_et() or os_ks_dispatch_mix() depending on which way it is compiled */
	}																			
	else {	/* We are not yet running on the kernel stack; save registers on	
			 * to the current stack, switch to the kernel stack,				
			 * and then run dispatch()											
			 */
		os_stackp newsp;															
																				
		newsp = os_kssp; os_kssp = 0;													
																				
		/* This works by falling back into the 'stub' exttaskdispatch call */	
		os_save2ks(RUNDISPATCH, &os_curtask->dyn->savesp, newsp);						
																				
		/* The os_save2ks call saves a context on the extended stack then			
		 * switches to the kernel stack; this call saves a context				
		 * in the right format that restore will use. Execution will			
		 * resume from here.													
		 */																		
		 																		
		/* EXECUTION REACHES HERE WHEN TASK RESUMES */							
		/* should be back on extended stack by now */							
		assert(!ON_KERNEL_STACK());												
	}																
}
#endif /* only include function if there can be any extended tasks */
