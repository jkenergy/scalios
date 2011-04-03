/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-21 19:53:58 +0000 (Fri, 21 Mar 2008) $
 * $LastChangedRevision: 678 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/count.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Functions for adding an alarm into a queue attached to a counter.
 */

#include <osint.h>

/* Synthesize a longer range time from the smaller range of time provided by the counter.
 * This is of os_longtick type, and stores time from zero to 2^((sizeof(os_longnow) * 2) - 1).
 * 
 * This is done to resolve certain race conditions in the alarm queueing operations, where
 * alarms can be due (but unprocessed) in the past, and new alarms added into the future
 * where the delay between the two alarms is more than the range of the counter.
 * 
 * Note that this range extension doesn't provide a time that can be used outside of the
 * counter/alarm queueing and manipulation functions, since it might not catch all "wraps" of the
 * counter 'now' value: the calls to os_now() might not be made often enough. But in
 * the case where wraps are missed there are no events in the queue to be processed so it cannot
 * cause a failure of alarm processing).
 */
os_longtick os_now(CounterType c, TickType short_now)
{
	TickType delta;

	const TickType max = c->alarmbase.maxallowedvalue;
	
	assert(short_now <= max);
	assert(c->dyn != 0);
	assert(SINGLETON_COUNTER(c) ? c->dyn->head == 0 : 1U);
	
	/* Work out how long it was since the last time os_now() was called. If
	 * the new time is numerically lower than the old time, then the counter
	 * has wrapped, and so compute the difference using modulo arithmetic.
	 */
	if(short_now < c->dyn->last_now) {
		/* Wrapped, so the time advanced is evaluated in a counter-specific modulo */
		delta = max - c->dyn->last_now;
		delta += short_now + 1U;	/* Modulo is max + 1; carry over the extra 1 (without overflowing!) from above */
	}
	else {
		delta = short_now - c->dyn->last_now;
	}

	assert(delta <= max);
	
	/* Update the long time by the change */
	c->dyn->long_tick += delta;					/* Modulo 32-bit arithmetic */
	
	/* Now long_tick indicates the long time that can be used: this has moved on. OK.
	 * Now record the short time to which this corresponds.
	 */
	c->dyn->last_now = short_now;
	
	/* Return the long time */
	return c->dyn->long_tick;	
}

/* Go down the (non-empty) queue and insert the new alarm at the right place
 */
void os_queue_alarm(CounterType c, os_longtick long_now, AlarmType new_alarm, TickType rel)
{
	os_longtick new_alarm_due;
	os_longtick counter_due;
	int32 tmp;
	
	assert(KERNEL_LOCKED());
	assert(!SINGLETON_ALARM(new_alarm));
	assert(!SINGLETON_COUNTER(c));
	assert(c->dyn->head);
	
	new_alarm_due = long_now + rel;
	new_alarm->dyn.c->due = new_alarm_due;
	counter_due = c->dyn->head->dyn.c->due;

	/* Find out the time between the new alarm and the counter (i.e. head) becoming due.
	 * If the new alarm is due first then insert it at the head of the queue.
	 *
	 * Uses "clock arithmetic" because the tick values can wrap around. Assume that
	 * the new alarm is due after the counter is due, and so a negative result would
	 * indicate the alarm is due first.
	 */
	tmp = new_alarm_due - counter_due;
	assert(tmp > -((int32)(c->alarmbase.maxallowedvalue)));
	assert(tmp <= c->alarmbase.maxallowedvalue);
	
	if(tmp < 0) {
		/* Insert new alarm at the head of the queue */
		c->dyn->head->dyn.m->prev = new_alarm;
		new_alarm->dyn.m->next = c->dyn->head;
		/* prev is "don't care" when item is head */
		c->dyn->head = new_alarm;
	}
	else {
		/* Search the list to insert at the right place */
		AlarmType prior = c->dyn->head;
		AlarmType current = prior->dyn.m->next;

		while(current && (tmp = new_alarm_due - current->dyn.c->due, tmp >= 0)) {
			assert(tmp > -((int32)(c->alarmbase.maxallowedvalue)));
			assert(tmp <= c->alarmbase.maxallowedvalue);
			prior = current;
			current = current->dyn.m->next;
		}
		/* Loop terminates when:
		 * 
		 * Run off the end of the list (i.e. 'current' == 0, 'prior' == something)
		 * --or--
		 * New alarm due before 'current' (and after 'prior')
		 * 
		 * In either case, the alarm needs to be appended to 'prior'.
		 */
		prior->dyn.m->next = new_alarm;
		new_alarm->dyn.m->next = current;
		new_alarm->dyn.m->prev = prior;
		if(current) {
			current->dyn.m->prev = new_alarm;
		}		
	}
}
