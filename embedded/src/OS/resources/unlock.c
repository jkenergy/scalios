/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-17 05:05:51 +0000 (Thu, 17 Jan 2008) $
 * $LastChangedRevision: 560 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/resources/unlock.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: The error returns aren't quite right and need to conform $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType ReleaseResource(ResourceType <ResID>)
 * 
 * This call unlocks a resource.
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS
StatusType os_ReleaseResource(ResourceType ResID)
{
	StatusType rc = E_OK;

	/* Can be called from a task or ISR */
	/* $Req: artf1161 $ */
	ENTER_KERNEL();
	OS_API_TRACE_RELEASE_RESOURCE(ResID);

	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;		/* $Req: artf1045 $ */
	}
	else if(!VALID_RESOURCE(ResID)) {
		rc = E_OS_ID;											/* $Req: artf1326 $ */
	}
	else if (os_curlastlocked != ResID) {						/* Can only unlock the resource that was locked last */	
		rc = E_OS_NOFUNC;										/* $Req: artf1159 */ 
	}
	else {
		/* Note that the specification requires E_OS_ACCESS to be returned under the condition
		 * of the following assert (see artf1160). However, another error condition always occurs
		 * at the same time - E_OS_FUNC - because the resource is unlocked out-of-order (the
		 * resource can never be locked in order because the lock would fail with an E_OS_ACCESS
		 * fault instead).
		 */
		assert(os_curtask->basepri <= ResID->ceil);				/* $Req: artf1160 $ */
		assert(os_curpri >= ResID->ceil);						/* If we locked this resource last then priority should be still boosted */
		
		if (ResID->dyn->prevlocked == ResID) {					/* Last one in the chain, since refers to itself */
			os_curlastlocked = 0;
		}
		else {
			os_curlastlocked = ResID->dyn->prevlocked;
		}
		ResID->dyn->prevlocked = 0;

		/* $Req: artf1157 artf1156 $ */
		os_curpri = ResID->dyn->prev;							/* Priority at point corresponding outer lock call was made */

		DISPATCH_IF_TASK_SWITCH_PENDING();						/* $Req: artf1097 $ */
	}

	if (rc != E_OK && os_flags.errorhook) {						/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(ReleaseResource, rc, ResID);
	}

	OS_API_TRACE_RELEASE_RESOURCE_FINISH(rc);
 	LEAVE_KERNEL();
 	OS_KERNEL_TRACE_RELEASE_RESOURCE(ResID);
 	
	return rc;
}

#else

/* $Req: artf1158 $ */
StatusType os_ReleaseResource(ResourceType ResID)
{
	/* Can be called from a task or ISR */
	/* $Req: artf1161 $ */
	
	ENTER_KERNEL();
	OS_API_TRACE_RELEASE_RESOURCE(ResID)
 	
	/* $Req: artf1157 artf1156 $ */
	os_curpri = ResID->dyn->prev;								/* Priority at point corresponding outer lock call was made */

	DISPATCH_IF_TASK_SWITCH_PENDING();							/* $Req: artf1097 $ */
	
	OS_API_TRACE_RELEASE_RESOURCE_FINISH(E_OK);
 	LEAVE_KERNEL();
 	OS_KERNEL_TRACE_RELEASE_RESOURCE(ResID);
 	
	return E_OK;
}

#endif /* OS_EXTENDED_STATUS */
