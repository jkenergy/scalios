/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-21 19:58:43 +0000 (Fri, 21 Mar 2008) $
 * $LastChangedRevision: 684 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/nextschedtab.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Sets the schedule table to be kicked off next after the current one reaches the end of its cycle
 * 
 * $Req artf1046 $
 */
#include <osint.h>

StatusType os_NextScheduleTable(ScheduleTableType ScheduleTableID_current, ScheduleTableType ScheduleTableID_next)
{
	StatusType rc;
	
	ENTER_KERNEL();
	OS_API_TRACE_NEXT_SCHED_TAB(ScheduleTableID_current, ScheduleTableID_next);

	/* Need to check for a set of conditions for this being a valid call.
	 * 
	 * Firstly, ID checks on the two identifiers; Extended Status only.
	 * Then need to check that 'current' has to be started (i.e. running)
	 * Then need to check that 'next' is not started (i.e. not running) and also not "nexted" (i.e. not the next of
	 * anything else or this); Extended Status only.
	 * 
	 * @TODO: Add requirements to requirements doc to clear up some of the open issues here.
	 */
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_SCHEDTAB(ScheduleTableID_current) || !VALID_SCHEDTAB(ScheduleTableID_next)) {
		rc = E_OS_ID;				/* $Req: artf1060 $ */
	}
	else
#endif
	if(!ScheduleTableID_current->alarm->dyn.c->running) {
		/* $Req: artf1061 $
		 * $Req: artf1383 $
		 */
		rc = E_OS_NOFUNC;
	}
#ifdef OS_EXTENDED_STATUS
	else if(ScheduleTableID_current->alarm->counter != ScheduleTableID_next->alarm->counter) {
		rc = E_OS_ID;				/* $Req: artf1075 $ */
	}
	else if(ScheduleTableID_next->alarm->dyn.c->running || ScheduleTableID_next->dyn->nexted) {
		/* Two error conditions giving the same error code. Firstly, it is an error if
		 * the next schedule table is already running. Secondly, it is an error if the
		 * 'next' schedule table is already going to become next at the end of another
		 * (or this) schedule table cycle (i.e. if it is "nexted").
		 */
		rc = E_OS_STATE;			/* $Req: artf1072 $ */
	}
#endif
	else {
		rc = E_OK;
		
		/* If 'current' already had a 'next' set then we need to mark the previous one as no longer "nexted"
		 * 
		 * $Req: artf1074 $
		 */
		if(ScheduleTableID_current->dyn->next_tab) {
			ScheduleTableID_current->dyn->next_tab->dyn->nexted = 0;
		}
		/* Set up the next schedule table (and mark it as being "nexted" for status reporting)
		 * 
		 * $Req: artf1062 $
		 * $Req: artf1074 $
		 */
		ScheduleTableID_next->dyn->nexted = 1U;
		ScheduleTableID_current->dyn->next_tab = ScheduleTableID_next;
	}
	
	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_2(NextScheduleTable, rc, ScheduleTableID_current, ScheduleTableID_next);
	}
		
	OS_API_TRACE_NEXT_SCHED_TAB_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}
