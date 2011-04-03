/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/sramulti.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Supports multiple alarms on a counter and hence multiple schedule tables on a counter ($Req: artf1369 $)
 * 
 */

#include <osint.h>


/* queues the alarm at the head of the queue
 */
static void os_queue_alarm_empty(CounterType c, os_longtick long_now, AlarmType new_alarm, TickType rel)
{
	assert(KERNEL_LOCKED());
	assert(!SINGLETON_COUNTER(c));
	assert(!SINGLETON_ALARM(new_alarm));
	assert(c->dyn->head == 0);
	assert(rel <= c->alarmbase.maxallowedvalue);

	/* $Req: artf1190 artf1196 $ */
	new_alarm->dyn.c->due = long_now + rel;							/* Modulo 32-bit arithmeic */
	c->dyn->head = new_alarm;
	new_alarm->dyn.m->next = 0;
	/* prev is "don't care" when item is head */
}

void os_setrelalarm_multi(AlarmType a, CounterType c, os_longtick long_now, TickType short_now, TickType rel)
{
	assert(KERNEL_LOCKED());

	/* Empty queue or not? */
	if(c->dyn->head) {
		os_queue_alarm(c, long_now, a, rel);						/* $Req: artf1190 artf1196 $ */

		/* Did the queuing result in the new alarm being the first one? */
		if(c->dyn->head == a) {
			/* New alarm at the head of the queue so the interrupt is now due at a different time */
			c->driver->enable_ints(c->device, short_now, rel);		/* $Req: artf1190 artf1196 $ */
		}
	}
	else {
		/* Empty queue */
		/* Nothing in the queue, this is the first thing */
		os_queue_alarm_empty(c, long_now, a, rel);
		c->driver->enable_ints(c->device, short_now, rel);			/* $Req: artf1190 artf1196 $ */
	}
}
