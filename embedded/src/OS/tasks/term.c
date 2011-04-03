/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-12-12 20:48:03 +0000 (Wed, 12 Dec 2007) $
 * $LastChangedRevision: 503 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/term.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: need to add a check for being in a hook, returning E_OS_CALLEVEL $
 * $CodeReviewItem: need to add code to implement requirement artf1041, artf1042 $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType TerminateTask(void)
 * 
 * This call terminates the currently running task.
 * Does not return on success.
 * 
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS
StatusType os_TerminateTask(void)
{
	StatusType rc;
	
	ENTER_KERNEL();
	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;							/* $Req: artf1045 $ */
	}
	else if (os_curlastlocked) {						/* check if holding a resource */
		rc = E_OS_RESOURCE;								/* $Req: artf1132 $ */
	}
	else if (IN_CAT2_ISR()) {
		rc = E_OS_CALLEVEL;								/* $Req: artf1133 $ */
	}
	else {
		/* Implicit release of internal resources $Req: artf1131 artf1099 $ */
		os_terminate();									/* $Req: artf1097 artf1130 $ */
		NOT_REACHED();
	}

	/* error condition occurred */
	if (os_flags.errorhook) {	/* $Req: artf1223 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_0(TerminateTask, rc);
	}

	LEAVE_KERNEL();
	
	return rc;
}

#else

StatusType os_TerminateTask(void)
{
	ENTER_KERNEL();
	
	/* Implicit release of internal resources $Req: artf1131 artf1099 $ */
	os_terminate();										/* $Req: artf1097 artf1130 $ */
	NOT_REACHED();
}

#endif /* OS_EXTENDED_STATUS */
