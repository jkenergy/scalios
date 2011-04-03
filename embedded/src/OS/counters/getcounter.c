/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-21 19:54:38 +0000 (Fri, 21 Mar 2008) $
 * $LastChangedRevision: 679 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/getcounter.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for GetCounterValue().
 * 
 * $Req: artf1355 $
 */

#include <osint.h>

StatusType os_GetCounterValue(CounterType CounterID, TickRefType Value)
{
	StatusType rc;
	
	ENTER_KERNEL();					/* GetCounterValue() shall be callable from tasks and cat2 ISRs (see AUTOSAR OS 3.0 spec p56 Table 1) */

	OS_API_TRACE_GET_COUNTER_VALUE(CounterID);

#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;									/* $Req: artf1045 $ */
	}
	else if(!VALID_COUNTER(CounterID)) {
		rc = E_OS_ID;											/* $Req: artf1351 $ */
	}
	else
#endif
	{
		*Value = CounterID->driver->now(CounterID->device);		/* $Req: artf1384 $ */
		rc = E_OK;
	}

#ifdef OS_EXTENDED_STATUS
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_2(GetCounterValue, rc, CounterID, Value);
	}
#endif
	
	OS_API_TRACE_GET_COUNTER_VALUE_FINISH(rc);
	LEAVE_KERNEL();

#if OS_EXTENDED_STATUS
	return rc;
#else
	return E_OK;
#endif
}
