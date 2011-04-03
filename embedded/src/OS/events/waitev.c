/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-10 08:51:18 +0000 (Thu, 10 Jan 2008) $
 * $LastChangedRevision: 546 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/waitev.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: need to explain DUMMY_EVENT better with more extensive comments $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType WaitEvent(EventMaskType <Mask>)
 * 
 * This call waits for a specified event (blocks).
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_WaitEvent(EventMaskType Mask)
{
	StatusType rc = E_OK;
	ENTER_KERNEL();
	OS_API_TRACE_WAIT_EVENT(Mask);
	
	/* Note: No corresponding assertion in extended status version because caller might be an ISR or a basic task */
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;					/* $Req: artf1045 $ */
	}
	else if (IN_CAT2_ISR()) {					/* Make this check first because checks below are invalid for ISR caller: os_curtask is still set while an ISR is running but is not the caller */
		rc = E_OS_CALLEVEL;						/* $Req: artf1181 $ */
	}
	else if (!EXTENDEDTASK(os_curtask)) {		/* Only extended tasks can wait */
		rc = E_OS_ACCESS;						/* $Req: artf1180 $ */
	}
	else if (os_curlastlocked) {				/* Check if holding a resource */
		rc = E_OS_RESOURCE;						/* $Req: artf1182 $ */
	}
	else if (!(Mask & os_curtask->dyn->set)) {
		/* event(s) to wait for are not already set, so need to block */
		
		/* set the mask of events that the task is waiting for
		 * DUMMY_EVENT ensures that a distinction can be made between a WAITING and SUSPENDED task,
		 * i.e. when a task is waiting on no events if this call made with a e=0
		 */
		os_curtask->dyn->wait = Mask | DUMMY_EVENT;	/* $Req: artf1177 $ */
		
		/* $Req: artf1178 $ */
		/* If task is non preemptive will still release the processor $Req: artf1099 */
		os_block();								/* $Req: artf1097 $ */
		/* Returns here when we have unblocked, with the priority equal to the dispatch priority */
		
		assert(KERNEL_LOCKED());
	}
	
	if (rc != E_OK && os_flags.errorhook) {		/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(WaitEvent, rc, Mask);
	}

	OS_API_TRACE_WAIT_EVENT_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}

#else

/* $Req: artf1179 */
StatusType os_WaitEvent(EventMaskType Mask)
{
	ENTER_KERNEL();
	OS_API_TRACE_WAIT_EVENT(Mask);
	
	assert(os_curtask->dyn->wait == 0);		/* task cannot be waiting on events while running */
	
	if (!(Mask & os_curtask->dyn->set)) {
		/* event(s) to wait for are not already set, so need to block */
		
		/* set the mask of events that the task is waiting for
		 * DUMMY_EVENT ensures that a distinction can be made between a WAITING and SUSPENDED task,
		 * i.e. when a task is waiting on no events if this call made with a e=0
		 */		
		os_curtask->dyn->wait = Mask | DUMMY_EVENT;	/* $Req: artf1177 */
		
		/* $Req: artf1178 $ */
		os_block();								/* $Req: artf1097 $ */
		/* Returns here when we have unblocked */
		
		assert(KERNEL_LOCKED());
	}
	
	OS_API_TRACE_WAIT_EVENT_FINISH(E_OK);
	LEAVE_KERNEL();
	
	return E_OK;
}

#endif
