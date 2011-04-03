/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-11-13 02:20:20 +0000 (Tue, 13 Nov 2007) $
 * $LastChangedRevision: 482 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/resources/lock.c $
 *  
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType GetResource(ResourceType <ResID>)
 * 
 * This call locks a resource.
 */

#include <osint.h>

#ifdef OS_EXTENDED_STATUS

StatusType os_GetResource(ResourceType ResID)
{
	StatusType rc;										/* Return code */
	os_pri basepri;

	/* Can be called from a task or ISR */
	/* $Req: artf1152 $ */  
	/* If called by an ISR then we need to get the base priority for error checking */
	if (IN_CAT2_ISR()) {
		basepri = os_curisr->basepri;
	}
	else {
		basepri = os_curtask->basepri;
	}

	OS_KERNEL_TRACE_GET_RESOURCE(ResID);
 	ENTER_KERNEL();
 	OS_API_TRACE_GET_RESOURCE(ResID);
 	
	if(INTERRUPTS_LOCKED()) {
		rc = E_OS_DISABLEDINT;							/* $Req: artf1045 $ */
	}
	else if(!VALID_RESOURCE(ResID)) {
		rc = E_OS_ID;									/* $Req: artf1155 $ */
	}
	else if (ResID->dyn->prevlocked						/* Was the resource already locked by this task or ISR? */
		|| basepri > ResID->ceil) {						/* or has this task or ISR been declared as accessing this resource? */
		rc = E_OS_ACCESS;								/* $Req: artf1154 $ */
	}
	else {
		/* $Req: artf1157 $ */
		if (os_curlastlocked) {
			/* not the first resource to be locked by the task, so refer new resource to one previously locked */
			ResID->dyn->prevlocked = os_curlastlocked;
		}
		else {
			/* first resource to be locked by the task, so refer resource to itself (so that resource is seen as been locked) */
			ResID->dyn->prevlocked = ResID;
		}
		os_curlastlocked = ResID;
		/* $Req: artf1151 $ */
		ResID->dyn->prev = os_curpri;						/* Save the previous priority (will restore back) */
		if (os_curpri < ResID->ceil) {						/* Do we need to boost priority? */
			os_curpri = ResID->ceil;						/* Boost the priority up */
		}
		rc = E_OK;
	}
	
	if (rc != E_OK && os_flags.errorhook ) {		/* $Req: artf1223 artf1111 artf1112 $ */
		/* call the error hook handler */
		OS_ERRORHOOK_1(GetResource, rc, ResID);
	}

	OS_API_TRACE_GET_RESOURCE_FINISH(rc);
	LEAVE_KERNEL();
			 
	return rc;
}

#else

/* Returns only E_OK */
/* $Req: artf1153 $ */
StatusType os_GetResource(ResourceType ResID)
{

	/* Can be called from a task or ISR */
	/* $Req: artf1152 $ */  
	OS_KERNEL_TRACE_GET_RESOURCE(ResID);
	ENTER_KERNEL();
 	OS_API_TRACE_GET_RESOURCE(ResID);
 	
	/* $Req: artf1151 $ */
	ResID->dyn->prev = os_curpri;						/* Save the previous priority (will restore back) */
	if (os_curpri < ResID->ceil) {						/* Do we need to boost priority? */
		os_curpri = ResID->ceil;						/* Boost the priority up */
	}
 	
	OS_API_TRACE_GET_RESOURCE_FINISH(E_OK);
 	LEAVE_KERNEL();										/* Set the IPL to the value corresponding to curpri */
	
	return E_OK;
}

#endif /* OS_EXTENDED_STATUS */


