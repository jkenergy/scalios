/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-06-16 00:46:48 +0100 (Sat, 16 Jun 2007) $
 * $LastChangedRevision: 433 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/alarmact.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Function for handling the action of an alarm expiry (activating a task)
 * 
 * This function is pointed to by the os_alarmcb member "process"; set statically in OIL file
 * 
 */

#include <osint.h>

void os_expiry_activatetask(const struct os_expirycb *exp)
{
	const TaskType TaskID = ((const struct os_expiry_taskcb *)exp)->task;

	assert(KERNEL_LOCKED());
	
	/* Alarm expiry behaves as if via an API call $Req: artf1107 artf1113 $ */
	if(os_activate_task(TaskID) && os_flags.errorhook) {
		OS_ERRORHOOK_1(ActivateTask, E_OS_LIMIT, TaskID);	/* @TODO artf1305: should we create a new code instead of using ActivateTask? */
	}
}
