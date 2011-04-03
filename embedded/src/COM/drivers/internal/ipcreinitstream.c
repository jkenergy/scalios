/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcreinitstream.c $
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
StatusType com_driver_internal_reinit_receiver_stream(com_receiverh receiver)
{
	 /* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_stream_receivercb *internal_recv = (const struct com_internal_stream_receivercb *)(receiver->drv_receiver);

	struct com_stream_queuecb_dyn *queue_dyn = internal_recv->dyn;

	
	/* Do not lock the resource of the receiver, since this function may be called by any task within the system, i.e.
	 * OSEK does not require callers of StartCOM() to be declared, thus resource locking is not possible.
	 */
	
	queue_dyn->head = internal_recv->first;
	queue_dyn->tail = internal_recv->first;
	queue_dyn->used = 0;
	
	*internal_recv->low_flag = COM_FALSE;
	*internal_recv->high_flag = COM_FALSE;
		
	return E_OK;
}
