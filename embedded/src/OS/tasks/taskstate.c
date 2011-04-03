/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-10 08:53:23 +0000 (Thu, 10 Jan 2008) $
 * $LastChangedRevision: 549 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/taskstate.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 *
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType GetTaskState(TaskType <TaskID>, TaskStateRefType <State>)
 * 
 * This call returns the current state of the task.
 *
 */
 
#include <osint.h>
 
StatusType os_GetTaskState(TaskType TaskID, TaskStateRefType State) 
{
	StatusType rc = E_OK;

	/* Can be called from tasks, ISRs or hooks */
	/* $Req: artf1141 $ */
	ENTER_KERNEL();
	OS_API_TRACE_GET_TASK_STATE(TaskID);
	
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;				/* $Req: artf1045 $ */
	}
	else if(!VALID_TASK(TaskID)) {
		rc = E_OS_ID;						/* $Req: artf1142 $ */
		*State = INVALID_TASK;				/* $Req: artf1142 $ */
	}
	else
#endif
	/* $Req: artf1141 $ */
	if (os_curtask == TaskID) {
		/* If requested task is current task then task must be running; note that there is no handle
		 * for the idle task so if the idle task is running then this call cannot return RUNNING.
		 * 
		 * Note also that a task can be RUNNING even if an ISR is running.
		 */
		/* $Req: artf1118 $ */
		/* $Req: artf1119 $ */
		*State = RUNNING;
	}
	else if (EXTENDEDTASK(TaskID) && TaskID->dyn->wait != 0) {
		/* Wait for no events means wait == DUMMY_EVENT */
		*State = WAITING;
	}
	else if (*TaskID->count == 0) {
		/* If not running and the activation count is zero then the task is suspended */
		*State = SUSPENDED;
	}
	else {	/* Not waiting or suspended, and not the current task, therefore task must be READY */
		*State = READY;
	}

#ifdef OS_EXTENDED_STATUS
	if (rc != E_OK && os_flags.errorhook) {	/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_2(GetTaskState, rc, TaskID, State);
	}
#endif

	OS_API_TRACE_GET_TASK_STATE_FINISH(rc);
	LEAVE_KERNEL();

	return rc;	
}
