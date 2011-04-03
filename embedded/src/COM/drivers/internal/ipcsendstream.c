/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcsendstream.c $
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

/* Driver "send" function. The 'device type' is local IPC. Only works for a stream receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 *
 * 'size' is a pointer to the number of bytes to send. The number of bytes actually sent is written back over.
 */
StatusType com_driver_internal_send_stream(com_receiverh dest, ApplicationDataRef src, LengthRef size)
{
	/* cast the anonymous dest type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_stream_receivercb *internal_recv = (const struct com_internal_stream_receivercb *)(dest->drv_receiver);
	COMLengthType to_end;	/* Space spare between tail and end of allocated buffer RAM */
	COMLengthType n;		/* Amount of data to be copied */
	struct com_stream_queuecb_dyn *dyn = internal_recv->dyn;	/* Cache */
	unat do_notify;
	
	IPC_LOCK_BUFFER(internal_recv->guard);
	
	n = internal_recv->size - dyn->used;	/* Find the number of free bytes in the ring buffer */
	if(n > *size) {
		n = *size;							/* Clip to the number of requested bytes */
	}
	/* n now contains the number of bytes to be copied */
	
	/* The space in the ring buffer might not be contiguous so need to deal with wrapping */
	to_end = internal_recv->last - dyn->tail;		/* number of bytes until end (i.e. 'last') of buffer space */
	
	if(n > to_end) {
		/* Not enough space; two block copies required */
		OS_BLOCK_COPY(to_end, src, dyn->tail);
		OS_BLOCK_COPY(n - to_end, src + to_end, internal_recv->first);
		
		dyn->tail = internal_recv->first + n - to_end;
	}
	else {
		/* Space to copy the data without wrapping */
		OS_BLOCK_COPY(n, src, dyn->tail);
		dyn->tail += n;
	}

	dyn->used += n;
	
	/* Gone over high threshold in this call? */
	if(dyn->used > internal_recv->high_threshold && dyn->used - n <= internal_recv->high_threshold) {
		do_notify = 1U;
	}
	else {
		do_notify = 0;
	}

	*size = n;	/* Write back the number of bytes actually sent */
		
	if(do_notify) {	
		*internal_recv->high_flag = COM_TRUE;				/* $Req: artf1248 artf1258 $ */
	}
	
	IPC_UNLOCK_BUFFER(internal_recv->guard);

	/* Note that notification may include a callback, which may call an API call, which
	 * may be SendMessage(), which may call this function. To ensure no re-entrancy issues,
	 * the notification is done last after all message data structures have been updated.
	 */
	if(do_notify) {
		return IPC_NOTIFY_HIGH(internal_recv);
	}
	else {
		return E_OK;
	}
}
