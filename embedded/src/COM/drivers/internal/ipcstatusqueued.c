/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcstatusqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "receiver status" function. The 'device type' is local IPC. Only works for a queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_status_queued(com_receiverh receiver)
{
	StatusType rc;
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_queued_receivercb *internal_recv = (const struct com_internal_queued_receivercb *)(receiver->drv_receiver);
	struct com_queuecb_dyn *dyn = internal_recv->dyn;
	
	IPC_LOCK_BUFFER(internal_recv->guard);
	
	if(dyn->num_messages > 0) {
		if(dyn->overflow) {
			rc = E_COM_LIMIT;	/* $Req: artf1298 $ */
		}
		else {
			rc = E_OK;			/* $Req: artf1299 $ */
		}
	}
	else {
		rc = E_COM_NOMSG;		/* $Req: artf1297 $ */
	}
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);
	
	return rc;
}
