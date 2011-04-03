/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcreinitqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "reinit receiver" function. The 'device type' is local IPC. Only works for a queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_reinit_receiver_queued(com_receiverh receiver)
{
	 /* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_queued_receivercb *internal_recv = (const struct com_internal_queued_receivercb *)(receiver->drv_receiver);

	struct com_queuecb_dyn *queue_dyn;
	
	assert(internal_recv->num_slots > 0);			/* Constrained by OIL; see OIL 2.5 spec 3.2.10.10 p26 */
	
	queue_dyn = internal_recv->dyn;
	
	/* Do not lock the resource of the receiver, since this function may be called by any task within the system, i.e.
	 * OSEK does not require callers of StartCOM() to be declared, thus resource locking is not possible.
	 */
	
	queue_dyn->head = internal_recv->first;
	queue_dyn->tail = internal_recv->first;
	queue_dyn->num_messages = 0;
	queue_dyn->overflow = 0;	
	*internal_recv->flag = COM_FALSE;
		
	return E_OK;
}
