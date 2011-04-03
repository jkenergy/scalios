/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/srasingle.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>
void os_setrelalarm_single(AlarmType a, CounterType c, os_longtick long_now, TickType short_now, TickType rel)
{
	assert(KERNEL_LOCKED());
	assert(rel <= c->alarmbase.maxallowedvalue);
	
	/* $Req: artf1190 artf1196 $ */
	a->dyn.c->due = long_now + rel;			/* 32-bit arithmetic */
	c->driver->enable_ints(c->device, short_now, rel);
}
