/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-10 08:51:18 +0000 (Thu, 10 Jan 2008) $
 * $LastChangedRevision: 546 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/clearev.c $
 *  
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType ClearEvent(EventMaskType <Mask>)
 * 
 * This call clears the specified event.
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_ClearEvent(EventMaskType Mask)
{
	StatusType rc;
	ENTER_KERNEL();
	
	OS_API_TRACE_CLEAR_EVENT(Mask);

	/* No corresponding assertion because os_curtask isn't valid yet: caller
	 * might be an ISR and os_curtask refer to an irrelevant task.
	 */
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;			/* $Req: artf1045 $ */
	}
	else if (IN_CAT2_ISR()) {
		rc = E_OS_CALLEVEL;				/* $Req: artf1170 $ */
	}
	else if (!EXTENDEDTASK(os_curtask)) {
		rc = E_OS_ACCESS;				/* $Req: artf1169 $ */
	}
	else {
		/* $Req: artf1167 $ */
		os_curtask->dyn->set &= ~Mask;		/* $Req: artf1167 $ */
		rc = E_OK;						/* $Req: artf1168 $ */
	}
	
	if (rc != E_OK && os_flags.errorhook) {	/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(ClearEvent, rc, Mask);
	}

	OS_API_TRACE_CLEAR_EVENT_FINISH(rc);
	LEAVE_KERNEL();

	return rc;
}

#else

/* $Req: artf1168 $ */
StatusType os_ClearEvent(EventMaskType Mask)
{
	ENTER_KERNEL();
	OS_API_TRACE_CLEAR_EVENT(Mask);
	
	/* Assume caller is not an ISR */
	assert(os_curtask->dyn->wait == 0);				/* task cannot be waiting on events while running */
	
	/* $Req: artf1167 $ */
	os_curtask->dyn->set &= ~Mask;					/* Clear the specified events of the current task */
	
	OS_API_TRACE_CLEAR_EVENT_FINISH(E_OK);
	LEAVE_KERNEL();
	
	return E_OK;
}

#endif
