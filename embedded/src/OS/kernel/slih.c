/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/slih.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

/* This file is compiled two ways: once with ISR_STACK_CHECKING defined, and with NO_ISR_STACK_CHECKING.
 * This creates two functions: os_slih_stackcheck and os_slih_nostackcheck, each put into the library in
 * their own object files. The correct one is linked in based on ZLIH interrupt wrappers created
 * by the configuration tool.
 */
 
#include <osint.h>

#if !defined(ISR_STACK_CHECKING) && !defined(NO_ISR_STACK_CHECKING)
#error One of ISR_STACK_CHECKING and NO_ISR_STACK_CHECKING must be defined
#endif


#if !defined(STACK_CHECKING) && defined (ISR_STACK_CHECKING)
/* Cannot have ISR stack checking when global stack checking is disabled, so no function called os_slih_stackcheck
 * will end up in the library. This will cause a link failure if the wrong library has been selected. */
#else
/* This is the second level interrupt handler that is called from the target specific first level handler.
 * 
 * returns 1 iff a task switch is needed.
 */
#ifdef ISR_STACK_CHECKING 
unat os_slih_stackcheck(ISRType isr)
#else
/* Called if the ISR handler runs without stack checking. Note: could have this function plus os_slih_stackcheck()
 * in the kernel since stack checking is disabled on a per-ISR basis.
 * 
 * returns true iff a task switch is needed.
 */
unat os_slih_no_stackcheck(ISRType isr)
#endif
{
	/* Define and save original context, prior to setup of new context */
#ifdef ISR_STACK_CHECKING
	os_stackp save_curtos = os_curtos; 		/* save top of stack of the task running prior to interrupt */
#endif

	os_pri save_curpri = os_curpri;			/* save priority of task running prior to interrupt handler being executed */
#ifdef OS_EXTENDED_STATUS
	ResourceType save_curlastlocked = os_curlastlocked;
#endif
	/* Any extra target-specific context saving; see compiler.h for more details */	
	/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	DECLARE_SAVE_TARGET_ISR_CONTEXT
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

	ISR_PROLOG();							/* Target-specifc pre-ISR processing */
	
	MARK_IN_KERNEL();						/* ISR didn't occur inside the kernel */
	
	assert(os_at_kernel_ipl());
	assert(ON_KERNEL_STACK());
	assert(STACKCHECK_OFF());				/* stack checking should be off when called */

	/* setup new context ready for call to user ISR handler */
	os_curisr = isr;
	OS_API_TRACE_DISPATCH_ISR(os_curisr);
	
	os_curpri = isr->basepri; 					/* This stops API calls from trying to do a task switch */

#ifdef ISR_STACK_CHECKING	
	OFFSET_CURTOS_ISR(isr->stackoffset);		/* offset curtos from the current SP by the specified number of bytes */
	ENABLE_STACKCHECK();						/* $Req: artf1039 $ */
#endif

#ifdef OS_EXTENDED_STATUS
	os_curlastlocked = 0;
#endif

	LEAVE_KERNEL();								/* This will set interrupt priority down again; could be interrupted here */	
	OS_KERNEL_TRACE_DISPATCH_ISR_START(isr);
	isr->handler();
	OS_KERNEL_TRACE_DISPATCH_ISR_FINISH(isr);
	ENTER_KERNEL_DIRECT();

#ifdef OS_EXTENDED_STATUS
	if(os_curlastlocked) {
		/* Autorelease of resources on termination. Although this is required in both standard
		 * and extended status, there is no need to do it in standard status because the data
		 * structures that need tidying do not even exist.
		 * 
		 * See discussion of OS369 in the requirements analysis wiki page (wiki1016). See
		 * also artf1042 for a similar requirement for tasks.
		 * 
		 * $Req: artf1348 $
		 */
		os_tidylockedresources();								/* $Req: artf1348 $ */
		
		/* Also call the error hook (if configured) to indicate the erroneous behaviour.
		 */
		if(os_flags.errorhook) {
			OS_ERRORHOOK_0(NoServiceId, E_OS_RESOURCE);			/* NoServiceId since not in an API call */
		}
	}
#endif
	
	/* Check that the 'fast' interrupt macro counters have not been left 'on' when finishing.
	 * If so, call the error hook (if configured) with E_OS_DISABLEDINT and then put the
	 * counters back to zero. See also artf1049 for similar with tasks.
	 * 
	 * See discussion of requirement OS368 in the requirements analysis wiki page
	 * (wiki1016).
	 *
	 * This 'check and tidy up' operates in standard as well as extended status (regretfully this does
	 * slow down interrupt handling in the standard status build).
	 * 
	 * $Req: artf1347 $
	 */
	if(INTERRUPTS_LOCKED()) {
		CLEAR_INTERRUPTS();
		/* Error hook is called with kernel locked and stack checking turned on: the stack
		 * is the ISR's allocated stack space (i.e. on the kernel stack).
		 */
		if(os_flags.errorhook) {
			OS_ERRORHOOK_0(NoServiceId, E_OS_DISABLEDINT);		/* NoServiceId since not in an API call */
		}
	}
	
#ifdef ISR_STACK_CHECKING	
	DISABLE_STACKCHECK();
#endif

#ifdef OS_EXTENDED_STATUS
	os_tidylockedresources();
#endif

	/* Restore original context after return from user ISR handler */
#ifdef OS_EXTENDED_STATUS	
	os_curlastlocked = save_curlastlocked;
#endif
#ifdef ISR_STACK_CHECKING
	os_curtos = save_curtos;			/* restore top of stack of the task running prior to interrupt */
#endif
	os_curpri = save_curpri;			/* restore curpri to priority of task running prior to interrupt handler being executed */
	
	/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	RESTORE_TARGET_ISR_CONTEXT
	/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/
	
	
	/* Is a switch to another task needed? */

	assert(STACKCHECK_OFF());		/* stack checking should be off when called */
	
	ISR_EPILOG();					/* Target-specifc post-ISR processing */
	
	/* Note: still marked as in kernel; FLIH will clear down marker (if in debug mode) */
	if (TASK_SWITCH_PENDING(os_curpri)) {
		 /* Yes switch needed */
		return 1U;
	}
	else {
		/* no switch needed, go back to interrupted task */
		return 0;
	}
}
#endif
