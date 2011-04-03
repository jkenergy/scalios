/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-11 02:31:35 +0000 (Fri, 11 Jan 2008) $
 * $LastChangedRevision: 556 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/allsusp.c $
 * 
 * $CodeReview: kentindell, 2006-10-06 $
 * $CodeReviewItem: need to ensure that os_sus_all_cnt can only be modified once all interrupts are disabled $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API calls: 	void SuspendAllInterrupts(void)
 * 				void ResumeAllInterrupts(void) 
 */
 
#include <osint.h>

os_ipl NEAR(os_sus_all_ipl);

/* Functions can be called prior to StartOS() and after ShutdownOS() $Req: artf1069 $ */

void os_SuspendAllInterrupts(void)
{	
	os_ipl tmp;
	/* Callable from tasks, ISRs (category 1 and 2), hooks, alarm callbacks $Req: artf1106 artf1212 $ */
	if (os_sus_all_cnt++ == 0) {
		/* Save IPL on entry so can restore in appropriate ResumeAllInterrupts. */		
		OS_SAVE_IPL(tmp);
		OS_KERNEL_TRACE_SUSPEND_ALL_INTERRUPTS();
		OS_SET_IPL_MAX();									/* Lock out all maskable interrupts $Req: artf1144  $ */
		OS_API_TRACE_SUSPEND_ALL_INTERRUPTS();
		os_sus_all_ipl = tmp;
	}
}

void os_ResumeAllInterrupts(void)
{
	OS_API_TRACE_RESUME_ALL_INTERRUPTS();
	/* Callable from tasks, ISRs (category 1 and 2), hooks, alarm callbacks $Req: artf1106 artf1212 $ */
	if (os_sus_all_cnt && --os_sus_all_cnt == 0) {		/* If counter is zero prior to the call then an error; do nothing $Req: artf1044 $ */
		OS_SET_IPL(os_sus_all_ipl);						/* Restore IPL to level when outermost SuspendAllInterrupts() was called $Req: artf1146 $ */
		OS_KERNEL_TRACE_RESUME_ALL_INTERRUPTS();
	}
}
