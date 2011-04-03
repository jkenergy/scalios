/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/camulti.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Service function to cancel alarm. Called via a function pointer.
 * 
 * Supports multiple alarms on a counter and hence multiple schedule tables on a counter ($Req: artf1369 $)
 */

#include <osint.h>

void os_cancel_alarm_multi(AlarmType a, CounterType c)
{
	assert(KERNEL_LOCKED());
	assert(!SINGLETON_ALARM(a));
	assert(!SINGLETON_COUNTER(c));
		
	assert(c->dyn->head);	/* Must be at least one alarm in the queue or else we can't get to make this call */
	assert(a->dyn.c->running); /* Caller must ensure this */
	
	/* Is the alarm at the head of the queue? */
	if(c->dyn->head == a) {
		AlarmType next_alarm = a->dyn.m->next;
		if(next_alarm) {
			const TickType short_now = c->driver->now(c->device);
			
			const os_longtick long_now = os_now(c, short_now);
			const os_longtick next_alarm_due = next_alarm->dyn.c->due;
			const int32 now_to_next_alarm_due = next_alarm_due - long_now;		/* Modulo 32-bit */
			
			OS_API_TRACE_CANCEL_ALARM_MULTI_AT_HEAD(short_now);
	
			assert(now_to_next_alarm_due > -((int32)(c->alarmbase.maxallowedvalue)));
			assert(now_to_next_alarm_due <= c->alarmbase.maxallowedvalue);
			
			if(now_to_next_alarm_due < 0) {
				/* Next alarm is pending already */
				c->driver->enable_ints(c->device, short_now, 0); /* A parameter of 0 is defined to mean 'make pending' */
			}
			else {
				/* Next alarm is in the future (possibly near future) */
				c->driver->enable_ints(c->device, short_now, (TickType)now_to_next_alarm_due);	
			}
			
			/* Strip the head off the queue and point at the subsequent alarm */
			c->dyn->head = next_alarm;
			/* prev is "don't care" when item is head */
		}
		else {
			/* Nothing else in the queue, mark the queue as empty, and stop future interrupts */
			c->dyn->head = 0;
			c->driver->disable_ints(c->device);				
		}
	}
	else {
		/* Simply take the alarm out of the list */
		a->dyn.m->prev->dyn.m->next = a->dyn.m->next;		
		if(a->dyn.m->next) {
			a->dyn.m->next->dyn.m->prev = a->dyn.m->prev;
		}
		/* No need to ask device driver to change anything since hardware will still
		 * interrupt at the right time.
		 */
	}
}
