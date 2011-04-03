/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/getalbase.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for GetAlarm().
 */

#include <osint.h>

StatusType os_GetAlarmBase(AlarmType AlarmID, AlarmBaseRefType Info)
{
	StatusType rc = E_OK;	/* GetAlarmBase() shall in standard status always return E_OK $Req: artf1185 $ */

	/* GetAlarmBase() shall be callable from tasks, category 2 ISRs and some hooks (see Figure 12 doc1002) $Req: artf1184 $ */
	/* No need to lock the kernel except/until calling error hook since dealing only with constant (ROM-based) data */
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_ALARM(AlarmID)) {
		rc = E_OS_ID;				/* $Req: artf1186 $ */
	}
	else
#endif	
	*Info = AlarmID->counter->alarmbase;
	
#ifdef OS_EXTENDED_STATUS
	ENTER_KERNEL();
	OS_API_TRACE_GET_ALARM_BASE(AlarmID);
	
	if(rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_2(GetAlarmBase, rc, AlarmID, Info);
	}
	/* Note: no _FINISH trace because the error hooks are already traced, and
	 * no other actions occur after the starting trace point
	 */
	LEAVE_KERNEL();
#endif

	return rc;
}

