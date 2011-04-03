/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/autoalmmulti.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 */

#include <osint.h>
	
/* Autostart the non-singleton alarms identified by the specified app mode */
void os_autostart_alarms_multi(AppModeType appmode)
{
	const struct os_auto_alarm *last_autoalarm = 0;
	AlarmType a;
	CounterType c;	
	TickType short_now;
	unat i;
	const struct os_auto_alarm *autoalarm = appmode->auto_m_alarms;
	
	assert(appmode->num_auto_m_alarms > 0);	/* must be at least one autostarted multi alarm for this to be called */
	
	/* Config tool must ensure that Autostarted alarms are -
	 * a) Grouped by counter
	 * b) In descending order of their "due" value (i.e. tail first)
	 */
	for(i = appmode->num_auto_m_alarms; i > 0; i--) {
		a = autoalarm->alarm;
		c = a->counter;

		assert(!SINGLETON_COUNTER(c));						/* Only non-singleton counters should be in this autostart list */
		
		if(c->dyn->head == 0) {								/* Changed to a new counter */
			/* Finish up with last counter and start it running */
			if(last_autoalarm) {
				AlarmType last_alarm = last_autoalarm->alarm;
				CounterType last_counter = last_alarm->counter;
				
				last_counter->dyn->head = last_alarm;		/* Last counter head is last alarm processed */
				/* last_alarm->dyn.m->prev is "don't care" since it's the first alarm in the queue */			
				last_counter->driver->enable_ints(last_counter->device, short_now, last_autoalarm->rel);
				
				last_autoalarm = 0;							/* Break queue of alarms since changed to new counter */
			}
			short_now = c->driver->now(c->device);			/* Get 'now' from the new counter, this is used by all alarms related to the new counter. */
			
			OS_API_TRACE_AUTOSTART_ALARM_MULTI(short_now);
		}
		
		a->dyn.c->cycle = autoalarm->cycle;
		a->dyn.c->due = short_now + autoalarm->rel;
		a->dyn.c->running = 1U;

		if (last_autoalarm) {
			a->dyn.m->next = last_autoalarm->alarm;
			last_autoalarm->alarm->dyn.m->prev = a;
		}
		else {
			a->dyn.m->next = 0;
		}
		
		last_autoalarm = autoalarm;
		autoalarm++;
	}
	
	/* Setup the counter of the last alarm processed (since this will not be done in the above loop) */
	assert(a == last_autoalarm->alarm);		/* assert to ensure loop executed at least once */
	assert(c == a->counter);
	
	c->dyn->head = a;					/* Head of new queue attached to this counter */
	/* a->dyn.m->prev = "don't care" since it's the first alarm in the queue */			
	c->driver->enable_ints(c->device, short_now, last_autoalarm->rel);
}
