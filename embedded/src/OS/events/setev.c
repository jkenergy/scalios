/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/setev.c $
 *  
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType SetEvent(TaskType <TaskID>, EventMaskType <Mask>)
 * 
 * The call sets a specified event.
 */
#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_SetEvent(TaskType TaskID, EventMaskType Mask)
{
	StatusType rc;
	
	/* Code can be called from a task or category 2 ISR */
	/* $Req: artf1162 $ */
	ENTER_KERNEL();
	OS_API_TRACE_SET_EVENT(TaskID, Mask);
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;						/* $Req: artf1045 $ */
	}
	else if(!VALID_TASK(TaskID)) {
		rc = E_OS_ID;								/* $Req: artf1165 $ */
	}
	else if (!EXTENDEDTASK(TaskID)) {
		rc = E_OS_ACCESS;
	}
	else if (os_set_event(TaskID, Mask)) {			/* Call worker function to do the event setting */
		rc = E_OS_STATE;							/* task t is suspended (i.e. not running/ready, not waiting) $Req: artf1166 $ */
	}
	else {
		DISPATCH_IF_TASK_SWITCH_PENDING();			/* $Req: artf1097 $ */
		
		rc = E_OK;									/* $Req: artf1164 $ */
	}
	
	if (rc != E_OK && os_flags.errorhook) {		/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_2(SetEvent, rc, TaskID, Mask);
	}

	OS_API_TRACE_SET_EVENT_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}

#else

/* $Req: artf1164 $ */
StatusType os_SetEvent(TaskType TaskID, EventMaskType Mask)
{
	/* Code can be called from a task or category 2 ISR */
	/* $Req: artf1162 $ */
	ENTER_KERNEL();
	OS_API_TRACE_SET_EVENT(TaskID, Mask);
	
	os_set_event(TaskID, Mask);
	DISPATCH_IF_TASK_SWITCH_PENDING();	/* $Req: artf1097 $ */

	OS_API_TRACE_SET_EVENT_FINISH(E_OK);
	LEAVE_KERNEL();
	
	return E_OK;
}

#endif /* OS_EXTENDED_STATUS */
