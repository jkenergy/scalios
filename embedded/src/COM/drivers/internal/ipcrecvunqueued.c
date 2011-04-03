/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcrecvunqueued.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "receive" function. The 'device type' is local IPC. Only works for an unqueued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_recv_unqueued(com_receiverh receiver, ApplicationDataRef dest)
{
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_unqueued_receivercb *internal_recv = (const struct com_internal_unqueued_receivercb *)(receiver->drv_receiver);

	IPC_LOCK_BUFFER(internal_recv->guard);

	OS_BLOCK_COPY(internal_recv->message_size, internal_recv->msg_data, dest);
	
	*internal_recv->flag = COM_FALSE;								/* $Req: artf1324 artf1248 artf1258 $ */
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);
	
	return E_OK;
}
