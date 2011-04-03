/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcsendoverwrite.c $
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

/* Driver "send" function. The 'device type' is local IPC. Only works for an unqueued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_send_unqueued(com_receiverh dest, ApplicationDataRef src)
{
	/* cast the anonymous dest type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_unqueued_receivercb *internal_recv = (const struct com_internal_unqueued_receivercb *)(dest->drv_receiver);
	
	IPC_LOCK_BUFFER(internal_recv->guard);
	
	OS_BLOCK_COPY(internal_recv->message_size, src, internal_recv->msg_data);		/* $Req: artf1244 $ */
	
	*internal_recv->flag = COM_TRUE;												/* $Req: artf1248 artf1258 $ */
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);

	/* Note that notification may include a callback, which may call an API call, which
	 * may be SendMessage(), which may call this function. To ensure no re-entrancy issues,
	 * the notification is done last after all message data structures have been updated.
	 */
	return IPC_NOTIFY(internal_recv);	
}
