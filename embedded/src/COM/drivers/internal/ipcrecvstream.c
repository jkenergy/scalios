/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcrecvstream.c $
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
StatusType com_driver_internal_recv_stream(com_receiverh src, ApplicationDataRef dest, LengthRef size)
{
	const struct com_internal_stream_receivercb *internal_recv = (const struct com_internal_stream_receivercb *)(src->drv_receiver);
	COMLengthType to_end;	/* Space between head and end of allocated buffer RAM */
	COMLengthType n;		/* Amount of data to be copied */
	struct com_stream_queuecb_dyn *dyn = internal_recv->dyn;	/* Cache */
	unat do_notify;
	
	IPC_LOCK_BUFFER(internal_recv->guard);
	
	n = dyn->used;		/* Can read up to the amount of data stored in the buffer */
	if(n > *size) {
		n = *size;		/* Clip to the number of requested bytes */
	}
	/* n now contains the number of bytes to be copied */
	
	/* The data in the ring buffer might not be contiguous so need to deal with wrapping */
	to_end = internal_recv->last - dyn->head;	/* Space from the head of the data to the end of the allocated buffer space (might not all be actual data) */	
	
	if(n > to_end) {
		/* Two block copies required */
		OS_BLOCK_COPY(to_end, dyn->head, dest);
		OS_BLOCK_COPY(n - to_end, internal_recv->first, dest + to_end);
		
		dyn->head = internal_recv->first + n - to_end;
	}
	else {
		/* Space to copy the data without wrapping */
		OS_BLOCK_COPY(n, dyn->head, dest);
		dyn->head += n;
	}

	dyn->used -= n;
	
	/* Gone under low threshold in this call? */
	if(dyn->used < internal_recv->low_threshold && dyn->used + n >= internal_recv->low_threshold) {
		do_notify = 1U;
	}
	else {
		do_notify = 0;
	}

	*size = n;	/* Write back the number of bytes actually received */
	
	if(do_notify) {	
		*internal_recv->low_flag = COM_TRUE;						/* $Req: artf1248 artf1258 $ */
	}
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);

	/* Note that notification may include a callback, which may call an API call, which
	 * may be SendMessage(), which may call this function. To ensure no re-entrancy issues,
	 * the notification is done last after all message data structures have been updated.
	 */
	if(do_notify) {
		return IPC_NOTIFY_LOW(internal_recv);
	}
	else {
		return E_OK;
	}
}
