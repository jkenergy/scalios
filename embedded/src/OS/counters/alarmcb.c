/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-06-16 00:46:48 +0100 (Sat, 16 Jun 2007) $
 * $LastChangedRevision: 433 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/alarmcb.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Function for handling the action of an alarm expiry (setting an event)
 * 
 * This function is pointed to by the os_alarmcb member "process"; set statically in OIL file
 */

#include <osint.h>

void os_expiry_callback(const struct os_expirycb *exp)
{
	assert(KERNEL_LOCKED());
	/* Alarm callbacks run at OS level $Req: artf1211 $ */
	((const struct os_expiry_callbackcb *)exp)->callback();
}
