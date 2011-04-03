/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcreinitunqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "reinit receiver" function. The 'device type' is local IPC. Only works for an unqueued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_reinit_receiver_unqueued(com_receiverh receiver)
{
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_unqueued_receivercb *internal_recv = (const struct com_internal_unqueued_receivercb *)(receiver->drv_receiver);
	
	/* Do not lock the resource of the receiver, since this function may be called by any task within the system, i.e.
	 * OSEK does not require callers of StartCOM() to be declared, thus resource locking is not possible.
	 */
	
	/* $Req: artf1261 $ */
	if ( internal_recv->init ) {
		/* Initial value available from config. so copy to received message buffer */
		OS_BLOCK_COPY(internal_recv->message_size, internal_recv->init, internal_recv->msg_data);
	}
	else {
		/* No initial value available, so need to zero the value in the buffer */ 
		OS_BLOCK_ZERO(internal_recv->message_size, internal_recv->msg_data);
	}
	
	*internal_recv->flag = COM_FALSE;		/* $Req: artf1248 artf1258 $ clear associated flag */
	
	return E_OK;	
}
