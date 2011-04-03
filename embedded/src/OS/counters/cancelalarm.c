/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/cancelalarm.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call for CancelAlarm().
 */

#include <osint.h>

StatusType os_CancelAlarm(AlarmType AlarmID)
{
	StatusType rc;
	
	ENTER_KERNEL();					/* CancelAlarm() shall be callable from tasks and ISRs $Req: artf1202 $ */

	OS_API_TRACE_CANCEL_ALARM(AlarmID);

#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_ALARM(AlarmID)) {
		rc = E_OS_ID;				/* $Req: artf1204 $ */
	}
	else
#endif
	if(AlarmID->dyn.c->running) {
		const CounterType c = AlarmID->counter;
		
		/* Call appropriate handler to process the alarm */
		CANCEL_ALARM(c, AlarmID);	/* $Req: artf1201 $ */
		rc = E_OK;					/* $Req: artf1203 $ */
	}
	else {
		rc = E_OS_NOFUNC;			/* $Req: artf1203 $ */
	}

	if (rc != E_OK && os_flags.errorhook) {
		OS_ERRORHOOK_1(CancelAlarm, rc, AlarmID);
	}
	
	OS_API_TRACE_CANCEL_ALARM_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}
