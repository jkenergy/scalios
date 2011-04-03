/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/startschedtab.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		External
 */
 
#ifndef STARTSCHEDTAB_H_
#define STARTSCHEDTAB_H_

#include <osint.h>

#if !defined(SETABS) && !defined(SETREL)
#error Either SETABS or SETREL must be defined
#endif

#ifdef SETABS
StatusType os_StartScheduleTableAbs(ScheduleTableType ScheduleTableID, TickType Tickvalue)
#define TRACE_START OS_API_TRACE_START_SCHEDTAB_ABS
#define TRACE_FINISH OS_API_TRACE_START_SCHEDTAB_ABS_FINISH
#define TICK_PARAM Tickvalue
#else
StatusType os_StartScheduleTableRel(ScheduleTableType ScheduleTableID, TickType Offset)
#define TRACE_START OS_API_TRACE_START_SCHEDTAB_REL
#define TRACE_FINISH OS_API_TRACE_START_SCHEDTAB_REL_FINISH
#define TICK_PARAM Offset
#endif
{
	AlarmType const a = ScheduleTableID->alarm;
	CounterType const c = a->counter;
	StatusType rc;

	ENTER_KERNEL();
	TRACE_START(ScheduleTableID, TICK_PARAM);
	
	/* If the alarm is running, the table is running (i.e. started). But we also interpret "started" to include
	 * being "nexted" as well. This is consistent with the definition of started used for the NextScheduleTable()
	 * API call. This is relied upon within the schedule table callback handler (see assertion in os_expiry_schedtab())
	 */
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;				/* $Req: artf1045 $ */
	}
	else if(!VALID_SCHEDTAB(ScheduleTableID)) {
		rc = E_OS_ID;						/* $Req: artf1079 artf1053 $ */
	}
	else
#endif
	if(a->dyn.c->running || ScheduleTableID->dyn->nexted) {
		rc = E_OS_STATE;					/* $Req: artf1081 artf1055 $ */
	}
#ifdef OS_EXTENDED_STATUS
#ifdef SETABS
	else if(Tickvalue > c->alarmbase.maxallowedvalue) {
		rc = E_OS_VALUE;					/* $Req: artf1080 $ */
	}
#else
	else if(Offset > c->alarmbase.maxallowedvalue || Offset == 0) {
		rc = E_OS_VALUE;					/* $Req: artf1054 artf1076 $ */
	}
#endif
#endif
	else {
		TickType short_now = c->driver->now(c->device);
		os_longtick long_now;
		int32 rel;

		rc = E_OK;

		OS_API_TRACE_START_SCHED_TAB_NOW(short_now);
		long_now = os_now(c, short_now);
		
#ifdef SETABS
		/* Convert the absolute start time into a relative start time. This is done in counter-specific
		 * modulo time. Note that this is the same logic used in os_now() and also GetElapsedCounterValue().
		 */
		if(Tickvalue < short_now) {										/* The start time will after a wrap of the counter */
			rel = c->alarmbase.maxallowedvalue - short_now;				/* First half; calculation won't wrap */
			rel += Tickvalue + 1U;										/* Second half; add 1 to correct for maxallowedvalue being one less than the range size */
		}
		else {															/* Not wrapped; just do simple arithmetic to get elapsed time */
			rel = Tickvalue - short_now;
		}
#else
		rel = Offset;						/* $Req: artf1056 $ */
#endif
		assert(rel >= 0);
		assert(rel <= c->alarmbase.maxallowedvalue);

		/* Now start the table. Technically we could avoid initializing current_xpoint if we rely on
		 * resetting current_xpoint to first_xpoint wherever we stop the table running. It is safer to
		 * do it here, however, since there are several places where the table could be stopped (manually,
		 * implicitly on a single-shot table, implicitly on a table where a "next" has been set).
		 * 
		 * $Req: artf1344 $
		 * 
		 * If the table has been cancelled this is ignored by the code and the restart occurs from the
		 * start of the table ($Req: artf1377 $)
		 */
		assert(ScheduleTableID->dyn->nexted == 0);
		ScheduleTableID->dyn->next_tab = 0;
		ScheduleTableID->dyn->first_run = 1U;
		ScheduleTableID->dyn->current_xpoint = ScheduleTableID->first_xpoint;
		assert(ScheduleTableID->dyn->current_xpoint);
		a->dyn.c->cycle = ScheduleTableID->first_xpoint->delta;
		a->dyn.c->running = 1U;
		c->setrelalarm(a, c, long_now, short_now, (TickType)rel);
	}
	if (rc != E_OK && os_flags.errorhook) {
#ifdef SETABS
		OS_ERRORHOOK_2(StartScheduleTableAbs, rc, ScheduleTableID, Tickvalue);		/* Note that tck has to match name of parameter unions in order for ERRORHOOK_n macros to work */
#else
		OS_ERRORHOOK_2(StartScheduleTableRel, rc, ScheduleTableID, Offset);			/* Note that tck has to match name of parameter unions in order for ERRORHOOK_n macros to work */
#endif
	}
	TRACE_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}

#endif /*STARTSCHEDTAB_H_*/
