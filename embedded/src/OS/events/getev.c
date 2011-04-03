/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/getev.c $
 *  
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType GetEvent(TaskType <TaskID>, EventMaskRefType <Event>)
 * 
 * This call gets the event mask of the specified task.
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_GetEvent(TaskType TaskID, EventMaskRefType Event)
{
	StatusType rc;
	
	/* This code can be called from a task, from a hook, or from a category 2 ISR */
	/* $Req: artf1172 $ */
	ENTER_KERNEL();
	
	OS_API_TRACE_GET_EVENT(TaskID);
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;						/* $Req: artf1045 $ */
	}
	else if(!VALID_TASK(TaskID)) {
		rc = E_OS_ID;								/* $Req: artf1174 $ */
	}
	else if (!EXTENDEDTASK(TaskID)) {
		rc = E_OS_ACCESS;							/* $Req: artf1175 $ */
	}
	else if (*TaskID->count == 0 && TaskID->dyn->wait == 0) {
		/* If task is waiting for no events then wait == DUMMY_EVENT */
													/* $Req: artf1176 $ */
		rc = E_OS_STATE;							/* task t is suspended (i.e. not running/ready, not waiting) */
	}
	else {
		/* $Req: artf1171 $ */
		*Event = TaskID->dyn->set;					/* get current event mask of the task t */
		rc = E_OK;									/* $Req: artf1173 $ */
	}
	
	if (rc != E_OK && os_flags.errorhook) {		/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_2(GetEvent, rc, TaskID, Event);
	}

	OS_API_TRACE_GET_EVENT_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}

#else

/* $Req: artf1173 $ */
StatusType os_GetEvent(TaskType TaskID, EventMaskRefType Event)
{
	/* @TODO task1035 may not need to lock the kernel for this call, so could change to MACRO, e.g.
	 * #define S_GetEvent(t,er) (*er = t->dyn->set). We could use our atomic word read macros used in COM?
	 * Depends whether t->dyn->set can be read atomically, if it can the above macro could live in target.h
	 */

	/* This code can be called from a task, from a hook, or from a category 2 ISR */
	/* $Req: artf1172 $ */
	ENTER_KERNEL();
	OS_API_TRACE_GET_EVENT(TaskID);
	
	/* $Req: artf1171 $ */
	*Event = TaskID->dyn->set;		/* get current event mask of the task t */
	
	OS_API_TRACE_GET_EVENT_FINISH(E_OK);
	LEAVE_KERNEL();
	
	return E_OK;
}

#endif
