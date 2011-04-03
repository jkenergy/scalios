/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-28 18:17:04 +0000 (Thu, 28 Feb 2008) $
 * $LastChangedRevision: 626 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/inccounter.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for IncrementCounter()
 * 
 * $Req: artf1361 $
 */

#include <osint.h>

StatusType os_IncrementCounter(CounterType CounterID)
{
	StatusType rc = E_OK;

	ENTER_KERNEL();
	OS_API_TRACE_INCREMENT_COUNTER(CounterID);

#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_COUNTER(CounterID) || !SOFTWARE_COUNTER(CounterID)) {
		rc = E_OS_ID;		/* $Req: artf1063 $ */
	}
	else
#endif
	{
		/* Increment counter returns OK even if the alarm actions cause errors. $Req: artf1073 $ */
		
		/* IncrementCounter() shall increment the counter <CounterID> by one (if any alarm 
		 * connected to this counter expires, the given action, e.g. task activation, is done)
		 * and shall return E_OK $Req: artf1064 $ */
		os_increment_counter(CounterID);
		
		DISPATCH_IF_TASK_SWITCH_PENDING();	/* Might have activated a task (or sent an event that woke an extended task) */
	}
#ifdef OS_EXTENDED_STATUS
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_1(IncrementCounter, rc, CounterID);
	}
#endif
	OS_API_TRACE_INCREMENT_COUNTER_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}
