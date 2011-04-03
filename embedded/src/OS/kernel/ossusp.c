/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-29 02:11:24 +0000 (Sat, 29 Mar 2008) $
 * $LastChangedRevision: 698 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/ossusp.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API calls: 	void SuspendOSInterrupts(void)
 * 				void ResumeOSInterrupts(void)
 * 
 */
 
#include <osint.h>

/* Functions can be called prior to StartOS() and after ShutdownOS() $Req: artf1069 $ */

os_ipl NEAR(os_sus_os_ipl);					/* IPL save/restore for SuspendOSInterrupts/ResumeOSInterrupts API calls */

void os_SuspendOSInterrupts(void)
{
	/* Potentially have a race with category 1 and category 2 ISRs interrupting a caller to this function. But it is
	 * not a problem since os_sus_os_cnt will be restored to the original value on return from the interrupt (unless the ISR uses
	 * the interrupt function incorrectly and terminates without performing a ResumeOSInterrupts(), which is prohibited
	 * (see OSEK OS spec. p26: "It is not allowed to return from an interrupt within such protected critical sections.")
	 */
	if(os_sus_os_cnt++ == 0) {
		OS_SAVE_IPL(os_sus_os_ipl);
		if(IPL_HIGHER_THAN_KERNEL_IPL(os_sus_os_ipl)) {
			/* Set IPL to lock out all Category 2 interrupts, but need to check to ensure IPL not lowered since this
			 * API call can be made from Category 1 interrupts.
			 */
			OS_KERNEL_TRACE_SUSPEND_OS_INTERRUPTS();
			OS_SET_IPL_KERNEL();			/* Lock out Category 2 ISRs $Req: artf1148 $ */
			OS_API_TRACE_SUSPEND_OS_INTERRUPTS();
		}
	}
}

void os_ResumeOSInterrupts(void)
{
	OS_API_TRACE_RESUME_OS_INTERRUPTS();
	if (os_sus_os_cnt && --os_sus_os_cnt == 0) {	/* If counter is zero prior to the call then an error; do nothing $Req: artf1044 $ */
		OS_SET_IPL(os_sus_os_ipl);						/* Restore IPL to level when SuspendOSInterrupts() was first called $Req: artf1149 $ */
		OS_KERNEL_TRACE_RESUME_OS_INTERRUPTS()
	}
}
