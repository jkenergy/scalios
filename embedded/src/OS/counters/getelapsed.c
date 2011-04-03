/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/getelapsed.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for GetElapsedCounterValue().
 * 
 * $Req: artf1358 $
 */

#include <osint.h>

StatusType os_GetElapsedCounterValue(CounterType CounterID, TickRefType Value, TickRefType ElapsedValue)
{
#ifdef OS_EXTENDED_STATUS
	StatusType rc;
#endif
	
	ENTER_KERNEL();					/* GetElapsedCounterValue() shall be callable from tasks and cat2 ISRs (see AUTOSAR OS 3.0 spec p56 Table 1) */

	OS_API_TRACE_GET_COUNTER_VALUE(CounterID);

#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;									/* $Req: artf1045 $ */
	}
	else if(!VALID_COUNTER(CounterID)) {
		rc = E_OS_ID;											/* $Req: artf1353 $ */
	}
	else if(*Value > CounterID->alarmbase.maxallowedvalue) {
		rc = E_OS_VALUE;										/* $Req: artf1357 $ */
	}
	else
#endif
	{
		/* Function returns the time between two measured counter values, with the
		 * previous value passed in.
		 * 
		 * $Req: artf1354 $
		 */
		TickType old = *Value;
		TickType now = CounterID->driver->now(CounterID->device);

		if(now < old) {					/* Wrapped; add the two ends together to get the elapsed time */
			*ElapsedValue = CounterID->alarmbase.maxallowedvalue - old;	/* First half; calculation won't wrap */
			*ElapsedValue += now + 1U;	/* Second half; add 1 to correct for maxallowedvalue being one less than the range size */
		}
		else {							/* Not wrapped; just do simple arithmetic to get elapsed time */
			*ElapsedValue = now - old;
		}
		
		/* Now update Value with the latest 'now' */
		*Value = now;											/* $Req: artf1384 $ */	
#ifdef OS_EXTENDED_STATUS
		rc = E_OK;
#endif
	}
	
#ifdef OS_EXTENDED_STATUS
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_3(GetElapsedCounterValue, rc, CounterID, Value, ElapsedValue);
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
