/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/act.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType ActivateTask(TaskType <TaskID>)
 * 
 * Make a task ready to run (might be blocked, in which case no switch will occur).
 */

#include <osint.h>

StatusType os_ActivateTask(TaskType TaskID)
{
	StatusType rc;

	/* Can be called from tasks and ISRs */
	/* $Req: artf1121 $ */

	ENTER_KERNEL();									/* Lock the kernel if necessary (don't lower interrupt level though) */
	OS_API_TRACE_ACTIVATE_TASK(TaskID);

	
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;						/* $Req: artf1045 $ */
	}
	else if(!VALID_TASK(TaskID)) {
		rc = E_OS_ID;								/* $Req: artf1128 $ */
	}
	else
#endif
	/* First step is to check it's OK to continue. This is a vital test because the FIFO queues are
	 * exactly large enough to hold the number of activations of the tasks at the priority of the queue.
	 */
	if(os_activate_task(TaskID)) {					/* Call worker function to do the activation (returns whether successful or not) */
		rc = E_OS_LIMIT;							/* $Req: artf1127 $ */
	}
	else {
		DISPATCH_IF_TASK_SWITCH_PENDING();			/* $Req: artf1097 artf1129 $ */

		rc = E_OK;
	}
	
	if (rc != E_OK && os_flags.errorhook) {		/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(ActivateTask, rc, TaskID);
	}

	OS_API_TRACE_ACTIVATE_TASK_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}
