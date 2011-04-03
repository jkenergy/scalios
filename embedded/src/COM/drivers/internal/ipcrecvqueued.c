/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcrecvqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "receive" function. The 'device type' is local IPC. Only works for a queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_recv_queued(com_receiverh receiver, ApplicationDataRef dest)
{
	StatusType rc;
	
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_queued_receivercb *internal_recv = (const struct com_internal_queued_receivercb *)(receiver->drv_receiver);
	
	struct com_queuecb_dyn * const queue_dyn = internal_recv->dyn;
	
	assert(internal_recv->num_slots > 0);						/* Constrained by OIL; see OIL 2.5 spec 3.2.10.10 p26 */

	IPC_LOCK_BUFFER(internal_recv->guard);

	if(queue_dyn->num_messages > 0) {
		if(queue_dyn->overflow) {
			queue_dyn->overflow = 0;					/* $Req: artf1290 $ */
			rc = E_COM_LIMIT;							/* $Req: artf1289 $ */
		}
		else {
			rc = E_OK;
		}
		
		OS_BLOCK_COPY(internal_recv->message_size, queue_dyn->head, dest);
		
		if(queue_dyn->head == internal_recv->last) {
			queue_dyn->head = internal_recv->first;
		}
		else {
			queue_dyn->head += internal_recv->message_size;
		}
		queue_dyn->num_messages--;
	}
	else {
		assert(queue_dyn->overflow == 0);
		rc = E_COM_NOMSG;								/* $Req: artf1288 $ */
	}
	
	*internal_recv->flag = COM_FALSE;					/* $Req: artf1324 artf1248 artf1258 $ */

	IPC_UNLOCK_BUFFER(internal_recv->guard);

	return rc;
}

