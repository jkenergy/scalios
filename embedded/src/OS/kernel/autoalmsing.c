/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/autoalmsing.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 */

#include <osint.h>	
		
/* Autostart the singleton alarms identified by the specified app mode */	
void os_autostart_alarms_singleton(AppModeType appmode)
{
	unat i;
	const struct os_auto_alarm *autoalarm = appmode->auto_s_alarms;
	
	assert(appmode->num_auto_s_alarms > 0);

	for(i = appmode->num_auto_s_alarms; i >0; i--) {
		AlarmType a = autoalarm->alarm;
		CounterType c = a->counter;
		TickType short_now = c->driver->now(c->device);
		
		OS_API_TRACE_AUTOSTART_ALARM_SINGLETON(short_now);
		
		assert(SINGLETON_COUNTER(c));			/* Only singleton counters should be in this autostart list */	
		assert(c->dyn->long_tick == 0);			/* Done by C runtime start up and/or reinit() */
		
		a->dyn.c->cycle = autoalarm->cycle;							/* setup the alarm */
		assert(c->dyn->long_tick == 0);

		a->dyn.c->due = short_now + autoalarm->rel;
		a->dyn.c->running = 1U;
		
		c->driver->enable_ints(c->device, short_now, autoalarm->rel);		/* start the counter */

		autoalarm++;
	}
}
