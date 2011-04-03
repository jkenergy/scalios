/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/countexp.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * CounterExpired() API call.
 * 
 * The main body of the function is accessed by a function pointer, with two variants. The
 * first handles a time-ordered queue of alarms to be handled now and in the future. The second
 * is a "singleton" variant where exactly one alarm is bound to the counter.
 *
 */

#include <osint.h>

/* In standard status the function only returns E_OK.
 */
#ifdef OS_EXTENDED_STATUS
StatusType os_ExpireCounter(CounterType CounterID)
{
	StatusType rc = E_OK;
	AlarmType a;

	ENTER_KERNEL();
	OS_API_TRACE_EXPIRE_COUNTER(CounterID);
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_COUNTER(CounterID)) {
		rc = E_OS_ID;				/* $Req: artf1328 $ */
	}
	else {		
		/* Call the handler for the alarm (which will activate the task, etc.)
		 * This is done before the alarm is processed (e.g. stopped or restarted
		 * for another cycle). Any user-written code that executes between the
		 * the following two lines will see the alarm as 'expired' (e.g. if
		 * GetAlarm() is called in the error hook).
		 * 
		 * See also IncrementCounter() for similar code.
		 * 
		 * Code in the handler can change the alarm behaviour by overwriting
		 * the alarm data before returning (e.g. to change the cycle time).
		 */
		
		a = EXPIRED_ALARM(CounterID);		
		a->process(a->action);				

		/* Call appropriate worker function to expire the alarm and re-trigger if necessary */
		CounterID->expired(CounterID);
	
		DISPATCH_IF_TASK_SWITCH_PENDING();	/* Might have activated a task (or sent an event that woke an extended task) */
	}
	/* @TODO task1037 check across all API calls whether the error hook call if and call can be pulled into EXTENDED_STATUS
	 * macro check. For example, in this call rc is always E_OK in standard status. The C compiler ought to
	 * optimize away the call.
	 */
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_1(ExpireCounter, rc, CounterID);
	}
	
	OS_API_TRACE_EXPIRE_COUNTER_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}

#else

StatusType os_ExpireCounter(CounterType CounterID)
{
	AlarmType a;

	ENTER_KERNEL();
	OS_API_TRACE_EXPIRE_COUNTER(CounterID);


	/* Call appropriate worker function and determine the alarm to be processed */
	a = EXPIRED_ALARM(CounterID);
	a->process(a->action);				/* Call the handler for the alarm (which will activate the task, etc.) */
	CounterID->expired(CounterID);		/* Retrigger alarm */
	DISPATCH_IF_TASK_SWITCH_PENDING();	/* Might have activated a task (or sent an event that woke an extended task) */
	
	OS_API_TRACE_EXPIRE_COUNTER_FINISH(E_OK);
	LEAVE_KERNEL();
	
	return E_OK;
}

#endif
