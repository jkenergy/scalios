/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-11 06:06:01 +0000 (Mon, 11 Feb 2008) $
 * $LastChangedRevision: 610 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/kernel.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#include <osint.h>

/* Functions can be called prior to StartOS() and after ShutdownOS()  */

unat NEAR(os_dis_all_cnt);								/* Should be either 1 or 0; cleared to zero by C run-time startup and reinit() $Req: artf1069 $ */
unat NEAR(os_sus_all_cnt);								/* Nesting count for SuspendAllInterrupts/ResumeAllInterrupts calls; cleared to zero by C run-time startup and reinit() $Req: artf1069 $ */
unat NEAR(os_sus_os_cnt);								/* Nesting count for SuspendOSInterrupts/ResumeOSInterrupts calls; cleared to zero by C run-time startup and reinit() $Req: artf1069 $ */
os_ipl NEAR(os_save_all_ipl);							/* IPL save/restore for DisableAllInterrupts/EnableAllInterrupts & SuspendAllInterrupts/ResumeAllInterrupts API calls */

JMP_BUF NEAR(os_startosenv);							/* The set_jmp buffer to return to StartOS */
os_primask NEAR(os_priqueuestatus);						/* Bit mask for priority queues; a '1' in bit 'n' indicates correponding priority queue is not empty */
AppModeType NEAR(os_appmode);							/* Current application mode of the system */
os_stackp NEAR(os_kssp);								/* Kernel stack pointer save area; 0 indicates running on kernel stack */
TaskType NEAR(os_nexttask);								/* Highest priority runnable task; may not be os_curtask */
TaskType NEAR(os_chaintask);							/* Global var storing task handle used during chaintask */

#ifdef STACK_CHECKING
os_stackp NEAR(os_curtos);								/* Current top of stack for task or ISR handler */
#endif
os_stackp NEAR(os_SPoffstore);							/* Copy of stack pointer used to calibrate stack usage of kernel itself */
os_stackp NEAR(os_SPonstore);							/* Copy of stack pointer used to calibrate stack usage of kernel itself */
os_stackp NEAR(os_SPsave);								/* Saved copy of stack pointer; used by os_jump2ks for calibration tests; always included even if stack checking turned off */

JMP_BUF * NEAR(os_curenv);								/* The set_jmp buffer to return to the kernel */
TaskType NEAR(os_curtask) = OS_IDLE_TASK;				/* Currently running task */
ISRType NEAR(os_curisr);								/* Currently running category 2 ISR */
os_pri NEAR(os_curpri) = IDLEPRI;						/* Current priority of currently running task or ISR */

#ifdef OS_EXTENDED_STATUS
ResourceType NEAR(os_curlastlocked);					/* Currently last locked resource (and still held) by task or ISR; 0 if none */

#endif

#ifndef NDEBUG
unat os_kernelnesting;
#endif

/* Link check symbols: referred to in system.c appropriately to check the right library is used */ 
#ifdef STACK_CHECKING
const unat os_smlnk;	/* link check variable defined when stack monitoring enabled */
#else
const unat os_nosmlnk;	/* link check variable defined when stack monitoring not enabled */
#endif

#ifdef OS_EXTENDED_STATUS
const unat os_eslnk;	/* link check variable defined for extended status build */
#else
const unat os_sslnk;	/* link check variable defined for standard status build */
#endif

/* Call the error hook routine, passing the given error code */
void os_call_error_hook(StatusType rc)
{
	os_pri prev_pri;
	
	/* Stack checking is always on when entered */
	/* on stack of calling task, i.e. KS for basic tasks or extended stack for extended tasks */
	
	assert(STACKCHECK_ON());
	assert(KERNEL_LOCKED());							/* $Req: artf1110 $ */
	assert(os_flags.errorhook);							/* $Req: artf1223 $ */
	
	/* Save os_curpri and set it to os_kernelpri so that LEAVE_KERNEL() calls in the API keep the IPL high
	 * enough to block cat2 ISRs. This allows ErrorHook() to run at OS level but also to make API calls.
	 * 
	 * $Req: artf1110 $
	 */
	prev_pri = os_curpri;
	os_curpri = os_kernelpri;
	
	os_flags.errorhook = 0;								/* prevent recursive calls to the hook $Req: artf1112 $ */
	OS_API_TRACE_ERROR_HOOK(rc);
	MARK_OUT_KERNEL();									/* Leave CPI running at OS level (i.e. cat2 ISRs blocked) */
	OS_KERNEL_TRACE_ERROR_HOOK_START();
	ErrorHook(rc);										/* $Req: artf1222 $ */
	OS_KERNEL_TRACE_ERROR_HOOK_FINISH();
	MARK_IN_KERNEL();
	
	os_flags.errorhook = 1U;							/* $Req: artf1112 */
	
	os_curpri = prev_pri;
}

#ifdef OS_EXTENDED_STATUS
/* run down os_curlastlocked list and zero out the chain of resources locked */ 		 
void os_tidylockedresources(void) {
	if (os_curlastlocked) {
		/* @todo log this somewhere as an error */
		ResourceType pr;
		
		do {
			pr = os_curlastlocked->dyn->prevlocked;
			os_curlastlocked->dyn->prevlocked = 0;
			os_curlastlocked = pr;
		}
		while (os_curlastlocked);
	}
}
#endif
