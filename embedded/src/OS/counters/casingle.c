/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-03 02:50:19 +0100 (Fri, 03 Aug 2007) $
 * $LastChangedRevision: 459 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/casingle.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Service function to cancel alarm. Called via a function pointer.
 * This function assumes alarm/counter is a singleton (i.e. exactly one alarm bound to the counter)
 */

#include <osint.h>

void os_cancel_alarm_single(AlarmType a, CounterType c)
{
	assert(KERNEL_LOCKED());
	assert(SINGLETON_ALARM(a));
	assert(SINGLETON_COUNTER(c));
	
	c->driver->disable_ints(c->device);	/* No future events needed;  */
}
