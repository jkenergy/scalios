/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcinitstream.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "init receiver" function. The 'device type' is local IPC. Only works for a queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_init_receiver_stream(com_receiverh receiver, ApplicationDataRef data)
{
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_stream_receivercb *internal_recv = (const struct com_internal_stream_receivercb *)(receiver->drv_receiver);
	StatusType rc;

	IPC_LOCK_BUFFER(internal_recv->guard);
		
	/* data parameter is unused in this function because the OSEK COM semantics require
	 * merely that the queues are emptied, so simply call reinit function that empties the queues.
	 */
	rc = com_driver_internal_reinit_receiver_stream(receiver);
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);
	
	return rc;
}
