/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-10 08:52:49 +0000 (Thu, 10 Jan 2008) $
 * $LastChangedRevision: 548 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/resources/schedule.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: The E_OS_CALLEVEL return test needs to include a test from being called from a hook $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType Schedule(void)
 * 
 * This call offers up a scheduling point.
 * 
 */
#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_Schedule(void)
{
	StatusType rc;

	ENTER_KERNEL();
	OS_API_TRACE_SCHEDULE();

	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;								/* $Req: artf1045 $ */
	}
	else if (os_curlastlocked) {							/* check if holding a resource */
		rc = E_OS_RESOURCE;									/* $Req: artf1138 $ */
	}
	else if (IN_CAT2_ISR()) {
		rc = E_OS_CALLEVEL;									/* $Req: artf1137 $ */
	}
	else {
		assert(os_nexttask);								/* Never 0 */
		assert(os_curtask->boostpri == os_curpri);			/* No resources locked */
		
		OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_TEST();
		if (TASK_SWITCH_PENDING(os_curtask->basepri)) {		/* Is there a higher priority task (or more than one) that should run when we unlock? */
			/* $Req: artf1136 artf1099 $ */
			os_curpri = os_curtask->basepri;				/* drop curpri so dispatch will switch to os_nexttask */
			assert(!IN_CAT2_ISR());							/* Can never switch while we are running an ISR handler because no task should have a high enough priority */
			DISABLE_STACKCHECK();
			os_swst_dispatch();								/* If so, run it (them) */
			ENABLE_STACKCHECK();
			os_curpri = os_curtask->boostpri;				/* restore curpri to boosted priority */
		}
		OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_DONE();
		rc = E_OK;
	}
	
	if (rc != E_OK && os_flags.errorhook) {					/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_0(Schedule, rc);
	}

	OS_API_TRACE_SCHEDULE_FINISH(rc);
	LEAVE_KERNEL();

	return rc;	
}

#else
 
StatusType os_Schedule(void)
{
	ENTER_KERNEL();
	OS_API_TRACE_SCHEDULE();
	
	/* Assume caller is not an ISR */
	
	assert(os_nexttask);								/* Never 0: system invariant */
	
	OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_TEST();	
	if (TASK_SWITCH_PENDING(os_curtask->basepri)) {		/* Is there a higher priority task (or more than one) that should run when we unlock? */
		os_curpri = os_curtask->basepri;
		assert(!IN_CAT2_ISR());							/* Can never switch while we are running an ISR handler because no task should have a high enough priority */
		DISABLE_STACKCHECK();
		os_swst_dispatch();								/* If so, run it (them) */
		ENABLE_STACKCHECK();
		os_curpri = os_curtask->boostpri;
	}
	OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_DONE();
	
	OS_API_TRACE_SCHEDULE_FINISH(E_OK);
 	LEAVE_KERNEL();
	
	return E_OK;
}

#endif /* OS_EXTENDED_STATUS */
