/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/cexpmulti.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Worker function for CounterExpired() API call. Assumes multiple alarms bound to the counter.
 * 
 * When the counter expires, need to find the first entry in the time-ordered
 * queue of events (alarms, schedule tables) to process next. The time ordering
 * is based on deltas between the events, with concurrent events having a delta of
 * zero between them.
 * 
 * This is the full generic function that allows multiple alarm events to be queued. Returns the alarm that should
 * be processed.
 * 
 * Supports multiple alarms on a counter and hence multiple schedule tables on a counter ($Req: artf1369 $)
 */

#include <osint.h>

void os_counter_expired_multi(CounterType c)
{
	const AlarmType original = c->dyn->head;						/* The alarm to be handled */
	const os_longtick original_due = original->dyn.c->due;
	const AlarmType next = original->dyn.m->next;
	const TickType cycle = original->dyn.c->cycle;
	const TickType short_now = c->driver->now(c->device);
	const os_longtick long_now = os_now(c, short_now);				/* Time taken here to ensure that overflow takes place from lower now to upper now */
	
	assert(KERNEL_LOCKED());
	assert(!SINGLETON_COUNTER(c));

	OS_API_TRACE_COUNTER_EXPIRED_MULTI(short_now);

	if(next == 0) {
		/* Simple case: one alarm, either to be re-queued or stopped */
		if(cycle) {
			/* $Req: artf1191 artf1197 $ */
			/* Just one cyclic alarm; simply move the event on in time: optimization to avoid need to unstitch then requeue */
			int32 now_to_alarm_due; 
			
			original->dyn.c->due += cycle;		/* Modulo 32-bit arithmetic */
			now_to_alarm_due = original->dyn.c->due - long_now;
			assert(now_to_alarm_due > -((int32)(c->alarmbase.maxallowedvalue)));
			assert(now_to_alarm_due <= c->alarmbase.maxallowedvalue);
		
			if(now_to_alarm_due < 0) {
				now_to_alarm_due = 0;
			}
			c->driver->enable_ints(c->device, short_now, (TickType)now_to_alarm_due);
		}
		else {
			/* Just one alarm now finished; de-queue it and stop the underlying counter */
			c->dyn->head = 0;
			original->dyn.c->running = 0;
			c->driver->disable_ints(c->device);
		}
	}
	else {
		int32 now_to_head_due;
		
		/* More than one alarm in the queue */
		
		/* Unstitch the alarm */		
		c->dyn->head = next;
		/* next->dyn.m->prev is "don't care" since it's now the head of the queue */
		
		if(cycle) {
			os_queue_alarm(c, original_due, original, cycle); /* If it is cyclic then re-queue it in the appropriate place */
		}
		else {
			original->dyn.c->running = 0;
		}
		
		now_to_head_due = c->dyn->head->dyn.c->due - long_now;
		
		assert(now_to_head_due > -((int32)(c->alarmbase.maxallowedvalue)));
		assert(now_to_head_due <= c->alarmbase.maxallowedvalue);
		
		/* Is the head already expired or not? */
		if(now_to_head_due < 0) {
			now_to_head_due = 0;
			/* Head alarm is already due */
		}
		c->driver->enable_ints(c->device, short_now, (TickType)now_to_head_due);
	}
}
