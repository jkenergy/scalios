/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/cexpsingle.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Worker function for CounterExpired() API call. Assumes singleton counter: only one alarm bound to the counter.
 */
 
#include <osint.h>

void os_counter_expired_single(CounterType c)
{
 	const AlarmType a = c->singleton_alarm;				/* The alarm to be handled */
	const TickType cycle = a->dyn.c->cycle;
	const TickType short_now = c->driver->now(c->device);
	const os_longtick long_now = os_now(c, short_now);				/* Time taken here to ensure that overflow takes place from lower now to upper now */
	
	assert(KERNEL_LOCKED());
	assert(SINGLETON_COUNTER(c));
	
	/* If the alarm is periodic, set it up for another go */	
	if(cycle) {								/* Periodic alarm, requeue */
		int32 now_to_alarm_due;
		/* $Req: artf1191 artf1197 $ */
		a->dyn.c->due += cycle;
		now_to_alarm_due = a->dyn.c->due - long_now;
		assert(now_to_alarm_due > -((int32)(c->alarmbase.maxallowedvalue)));
		assert(now_to_alarm_due <= c->alarmbase.maxallowedvalue);
		
		if(now_to_alarm_due < 0) {
			now_to_alarm_due = 0;
		}
		c->driver->enable_ints(c->device, short_now, (TickType)now_to_alarm_due);	/* The driver function resolves the race, where the counter has gone past now + rel */	
	}
	else {
		a->dyn.c->running = 0;				/* Alarm is no longer running; record this for error handling (both Standard and Extended status) */
		c->driver->disable_ints(c->device);
	}
}
