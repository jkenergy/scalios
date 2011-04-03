/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcsendqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* @TODO Route sending messages directly to receive message objects $Req: artf1237 artf1283 $ */
/* @TODO artf1310 need to define target-specific assembler for flag test, set, clear to ensure atomicity and avoid need to lock interrupts */

/* Driver "send" function. The 'device type' is local IPC. Only works for an queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_send_queued(com_receiverh dest, ApplicationDataRef src)
{
	/* cast the anonymous dest type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_queued_receivercb *internal_recv = (const struct com_internal_queued_receivercb *)(dest->drv_receiver);
	struct com_queuecb_dyn *queue_dyn = internal_recv->dyn;

	StatusType rc;
	
	IPC_LOCK_BUFFER(internal_recv->guard);
		
	/* First step is to check there is space for the message; if no space then discard */
	if(queue_dyn->num_messages < internal_recv->num_slots) {
		/* Queue in FIFO ring buffer $Req: artf1242 $ */
		OS_BLOCK_COPY(internal_recv->message_size, src, queue_dyn->tail);
		if(queue_dyn->tail == internal_recv->last) {
			queue_dyn->tail = internal_recv->first;
		}
		else {
			queue_dyn->tail += internal_recv->message_size;
		}
		queue_dyn->num_messages++;
		
		/* Still needs to be atomic, even though COM is locked because the flag accessors are unknown
		 * at build time and could be any task or ISR, including category 1 ISRs.
		 */
		*internal_recv->flag = COM_TRUE;				/* $Req: artf1248 artf1258 $ */

		/* Note that notification may include a callback, which may call an API call, which
		 * may be SendMessage(), which may call this function. To ensure no re-entrancy issues,
		 * the notification is done last after all message data structures have been updated.
		 */
		 IPC_UNLOCK_BUFFER(internal_recv->guard);	/* @todo unlock after notify or before? */
		 
		rc = IPC_NOTIFY(internal_recv);
	}
	else {
		queue_dyn->overflow = 1U;						/* $Req: artf1243 $ */
		
		IPC_UNLOCK_BUFFER(internal_recv->guard);
		
		rc = E_OK;
	}
	return rc;
}

