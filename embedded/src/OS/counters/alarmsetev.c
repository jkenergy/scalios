/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-06-16 00:46:48 +0100 (Sat, 16 Jun 2007) $
 * $LastChangedRevision: 433 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/alarmsetev.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Function for handling the action of an alarm expiry (incrementing a software counter)
 * 
 * This function is pointed to by the os_alarmcb member "process"; set statically in OIL file
 */

#include <osint.h>

void os_expiry_setevent(const struct os_expirycb *exp)
{
	const TaskType TaskID = ((const struct os_expiry_eventcb *)exp)->task;
	const EventMaskType Mask = ((const struct os_expiry_eventcb *)exp)->event;
	
	assert(KERNEL_LOCKED());
	assert(EXTENDEDTASK(TaskID));

#ifdef OS_EXTENDED_STATUS
	/* Alarm expiry behaves as if via an API call $Req: artf1107 artf1113 $ */
	if(os_set_event(TaskID, Mask) && os_flags.errorhook) {
		/* call the error hook handler */
		OS_ERRORHOOK_2(SetEvent, E_OS_STATE, TaskID, Mask);
	}
#else
	os_set_event(TaskID, Mask);
#endif
}
