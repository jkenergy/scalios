/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-12-12 20:48:03 +0000 (Wed, 12 Dec 2007) $
 * $LastChangedRevision: 503 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/chain.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType ChainTask(TaskType <TaskID>)
 * 
 * This call terminates the currently running task and activates the given task.
 * Never returns to caller on success.
*/

#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_ChainTask(TaskType TaskID)
{
	StatusType rc;

	ENTER_KERNEL();
	OS_API_TRACE_CHAIN_TASK(TaskID);

	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;						/* $Req: artf1045 $ */
	}
	else if(!VALID_TASK(TaskID)) {
		rc = E_OS_ID;								/* $Req: artf1134 artf1128 $ */
	}
	else if (os_curlastlocked) {					/* check if holding a resource */
		rc = E_OS_RESOURCE;							/* $Req: artf1134 artf1132 $ */
	}
	else if (IN_CAT2_ISR()) {
		rc = E_OS_CALLEVEL;							/* $Req: artf1134 artf1133 $ */
	}
	else if (*TaskID->count >= TaskID->countlimit && TaskID != os_curtask) { /* $Req: artf1086 artf1135 $ */
		rc = E_OS_LIMIT;							/* Hit the counter limit and not activiting self $Req: artf1134 $ */
	}
	else {
		/* setup global var that is used to activate (re-queue) a task just after termination */
		os_chaintask = TaskID;
		os_terminate();								/* Non preemptive task also releases processor $Req: artf1099 $ */
		NOT_REACHED();
	}

	/* error condition occurred, so call error hook and return to caller */
	
	if (os_flags.errorhook) {					/* $Req: artf1223 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(ChainTask, rc, TaskID);
	}

	OS_API_TRACE_CHAIN_TASK_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}

#else

StatusType os_ChainTask(TaskType TaskID)
{
	ENTER_KERNEL();
	OS_API_TRACE_CHAIN_TASK(TaskID);
	
	if (*TaskID->count < TaskID->countlimit || TaskID == os_curtask) {	/* $Req: artf1086 $ */
		/* Not hit the counter limit or activiting self */
		
		/* setup global var that is used to activate (re-queue) a task just after termination */
		os_chaintask = TaskID;
		os_terminate();									/* $Req: artf1097 $ */
		NOT_REACHED();
	}

	/* error condition occurred, so call error hook and return to caller */
	
	if (os_flags.errorhook) {		/* $Req: artf1223 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(ChainTask, E_OS_LIMIT, TaskID);
	}

	OS_API_TRACE_CHAIN_TASK_FINISH(E_OS_LIMIT);
	LEAVE_KERNEL();

	return E_OS_LIMIT;
}

#endif /* OS_EXTENDED_STATUS */
