/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-28 18:17:04 +0000 (Thu, 28 Feb 2008) $
 * $LastChangedRevision: 626 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/getschedtabstatus.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

/* API call to query a schedule table's status.
 * 
 * $Req: artf1345 $
 */
StatusType os_GetScheduleTableStatus(ScheduleTableType ScheduleID, ScheduleTableStatusRefType ScheduleStatus)
{
	StatusType rc = E_OK;

	ENTER_KERNEL();
	OS_API_TRACE_GET_SCHED_TAB_STATUS(ScheduleID);

#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;							/* $Req: artf1045 $ */
	}
	else if(!VALID_SCHEDTAB(ScheduleID)) {
		rc = E_OS_ID;									/* $Req: artf1068 $ */
	}
	else
#endif
	if(ScheduleID->alarm->dyn.c->running) {
		*ScheduleStatus = SCHEDULETABLE_RUNNING;		/* $Req: artf1067 $ */
	}
	else if(ScheduleID->dyn->nexted) {
		*ScheduleStatus = SCHEDULETABLE_NEXT;			/* $Req: artf1083 $ */
	}
	else {
		*ScheduleStatus = SCHEDULETABLE_NOT_STARTED;	/* $Req: artf1065 $ */
	}

#ifdef OS_EXTENDED_STATUS
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_2(GetScheduleTableStatus, rc, ScheduleID, ScheduleStatus);
	}
#endif
	OS_API_TRACE_GET_SCHED_TAB_STATUS_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}
