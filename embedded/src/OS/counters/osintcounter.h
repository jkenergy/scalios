/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/osintcounter.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#ifndef ICOUNTER_H_
#define ICOUNTER_H_

/* Functions for queueing alarms into a queue associated with a given counter */
void os_queue_alarm(CounterType c, os_longtick now, AlarmType new_alarm, TickType rel);
/* The function os_queue_alarm_empty() is declared as static in the one place it is called
 * from (see os_setrelalarm_multi() function)
 */

#define SINGLETON_COUNTER(c)					(c->singleton_alarm)
#define SINGLETON_ALARM(a)						(a->dyn.m == 0)

#define EXPIRED_ALARM(c)						(SINGLETON_COUNTER(c) ? (c)->singleton_alarm : (c)->dyn->head)

/* os_first_alarm and os_last_alarm might be 0 so need a test of handle for non-zero */
#define VALID_ALARM(a)							((a) && (a) >= os_first_alarm && (a) <= os_last_alarm)

#define VALID_COUNTER(c)						((c) >= os_first_counter && (c) <= os_last_counter)
#define SOFTWARE_COUNTER(c)						((c)->driver->stop == os_cdevicedrv_soft_stop)

#define CANCEL_ALARM(cnt, alm)					{(cnt)->cancel((alm), (cnt)); (alm)->dyn.c->running = 0;}
													
#endif /*ICOUNTER_H_*/
