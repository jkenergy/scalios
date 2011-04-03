/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/getal.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for GetAlarm().
 */

#include <osint.h>

StatusType os_GetAlarm(AlarmType AlarmID, TickRefType Tick)
{
	StatusType rc;
	
	ENTER_KERNEL();
	OS_API_TRACE_GET_ALARM(AlarmID);


#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_ALARM(AlarmID)) {
		rc = E_OS_ID;				/* $Req: artf1189 $ */
	}
	else
#endif
	if(AlarmID->dyn.c->running) {
		int32 delta;
		const CounterType c = AlarmID->counter;
		const TickType short_now = c->driver->now(c->device);
		const os_longtick long_now = os_now(AlarmID->counter, short_now);
		
		OS_API_TRACE_GET_ALARM_RUNNING(now.short_tick);
		
		delta = AlarmID->dyn.c->due - long_now;				/* Modulo 32-bit arithmetic */
		assert(delta > -((int32)(c->alarmbase.maxallowedvalue)));
		assert(delta <= c->alarmbase.maxallowedvalue);

		if(delta < 0) {
			/* Alarm already due but not yet processed, so return zero */
			delta = 0;
		}
		*Tick = (TickType)delta;	/* $Req: artf1187 $ */

		rc = E_OK;					/* $Req: artf1188 $ */
	}
	else {
		rc = E_OS_NOFUNC;			/* $Req: artf1188 $ */
	}
	
	if (rc != E_OK && os_flags.errorhook) {
		/* call the error hook handler */
		OS_ERRORHOOK_2(GetAlarm, rc, AlarmID, Tick);
	}
	
	OS_API_TRACE_GET_ALARM_FINISH(rc);
	LEAVE_KERNEL();	
	
	return rc;
}
