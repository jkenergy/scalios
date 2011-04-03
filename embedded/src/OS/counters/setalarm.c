/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/setalarm.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 *
 * 
 * This file is compiled in two versions: one for SETREL and one for SETABS. It is done via a special
 * make command to insert a #define SETREL or #define SETABS at the top of the file in a temporary
 * copy. This is done instead of #including a source file because many debuggers have trouble with
 * stepping through code that came from header files.
 */

#include <osint.h>

#if !defined(SETABS) && !defined(SETREL)
#error Either SETABS or SETREL must be defined
#endif

#ifdef SETABS
StatusType os_SetAbsAlarm(AlarmType AlarmID, TickType start, TickType cycle)
#define TRACE_START OS_API_TRACE_SETABSALARM
#define TRACE_FINISH OS_API_TRACE_SETABSALARM_FINISH
#else
#define start increment
StatusType os_SetRelAlarm(AlarmType AlarmID, TickType increment, TickType cycle)
#define TRACE_START OS_API_TRACE_SETRELALARM
#define TRACE_FINISH OS_API_TRACE_SETRELALARM_FINISH
#endif
{
	StatusType rc;
#ifdef SETABS
	int32 rel;
#endif
	
	/* Callable from tasks and ISRs $Req: artf1192 artf1198 $ */
	ENTER_KERNEL();
	TRACE_START(AlarmID, start, cycle, short_now);

	/* Extended status checks for:
	 *    E_OS_DISABLEDINT
	 *    E_OS_ID
	 *    E_OS_STATE
	 *    E_OS_VALUE
	 * 
	 * Standard status checks for:
	 *    E_OS_STATE
	 */	
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_ALARM(AlarmID)) {
		rc = E_OS_ID;				/* $Req: artf1194 artf1327 $ */
	}
	else if(AlarmID->dyn.c->running) {
	    /* Return E_OS_STATE if the alarm is in use or E_OK if no other errors $Req: artf1199 artf1193 $ */	
		rc = E_OS_STATE;
	}
	else if((start > AlarmID->counter->alarmbase.maxallowedvalue) ||
#ifdef SETREL
			(increment == 0) ||	/* $Req: artf1071 $ */
#endif
			(cycle > AlarmID->counter->alarmbase.maxallowedvalue) ||
			(cycle && (cycle < AlarmID->counter->alarmbase.mincycle))) {
		/* E_OS_VALUE returned from SetRelAlarm() and SetAbsAlarm() $Req: artf1195 artf1200 $ */
		rc = E_OS_VALUE;
	}
#else /* Standard status error checks */
	if(AlarmID->dyn.c->running) {
	    /* Return E_OS_STATE if the alarm is in use or E_OK if no other errors $Req: artf1199 artf1193 $ */	
		rc = E_OS_STATE;
	}
#endif
	
	else {
		/* Main API function; same for both standard and extended status */
		const CounterType c = AlarmID->counter;
		const TickType short_now = c->driver->now(c->device);
		const os_longtick long_now = os_now(c, short_now);
		
		/* $Req: artf1191 artf1197 $ */
		AlarmID->dyn.c->cycle = cycle;
		AlarmID->dyn.c->running = 1;

#ifdef SETABS
		/* $Req: artf1196 $ */
		
		/* Convert the absolute start time into a relative start time. This is done in counter-specific
		 * modulo time. Note that this is the same logic used in os_now() and also GetElapsedCounterValue().
		 */
		if(start < short_now) {										/* The start time will after a wrap of the counter */
			rel = c->alarmbase.maxallowedvalue - short_now;			/* First half; calculation won't wrap */
			rel += start + 1U;										/* Second half; add 1 to correct for maxallowedvalue being one less than the range size */
		}
		else {														/* Not wrapped; just do simple arithmetic to get elapsed time */
			rel = start - short_now;
		}

		assert(rel >= 0);
		assert(rel <= c->alarmbase.maxallowedvalue);
		
		c->setrelalarm(AlarmID, c, long_now, short_now, (TickType)rel);
#else
		/* $Req: artf1190 $ */
		c->setrelalarm(AlarmID, c, long_now, short_now, increment);
#endif
		rc = E_OK;
	}
	
	if (rc != E_OK && os_flags.errorhook) {
#ifdef SETABS
		OS_ERRORHOOK_3(SetAbsAlarm, rc, AlarmID, start, cycle);		/* Note that start and cycle have to match names of parameter unions in order for ERRORHOOK_n macros to work */
#else
		OS_ERRORHOOK_3(SetRelAlarm, rc, AlarmID, increment, cycle);	/* Note that increment and cycle have to match names of parameter unions in order for ERRORHOOK_n macros to work */
#endif
	}

	TRACE_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}
