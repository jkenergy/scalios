/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/stopschedtab.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 *
 * StopScheduleTable() call. $Req: artf1034 $
 */

#include <osint.h>

StatusType os_StopScheduleTable(ScheduleTableType ScheduleTableID)
{
	StatusType rc;
	AlarmType a = ScheduleTableID->alarm;
	
	ENTER_KERNEL();
	OS_API_TRACE_STOP_SCHED_TAB(ScheduleTableID);
	
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_SCHEDTAB(ScheduleTableID)) {
		rc = E_OS_ID;				/* $Req: artf1057 $ */
	}
	else
#endif
	if(a->dyn.c->running) {
		rc = E_OS_NOFUNC;			/* $Req: artf1058 $ */
	}
	else {
		const CounterType c = a->counter;
		rc = E_OK;					/* $Req: artf1059 $ */
		
		/* Call appropriate handler to cancel the alarm */
		CANCEL_ALARM(c, a);
		
		/* If there is a "next table" set then de-next it */
		if(ScheduleTableID->dyn->next_tab) {
			assert(ScheduleTableID->dyn->next_tab->dyn->nexted);
			ScheduleTableID->dyn->next_tab->dyn->nexted = 0;
		}
	}
	
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_1(StopScheduleTable, rc, ScheduleTableID);
	}
				
	OS_API_TRACE_STOP_SCHED_TAB_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}
