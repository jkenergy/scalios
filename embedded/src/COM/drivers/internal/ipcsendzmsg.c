/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcsendzmsg.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * SendZeroMessage() isn't in OSEK COM CCCB but is provided as an extension to CCCB $Req: artf1295 $
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "send zero" function. The 'device type' is local IPC. Only works for an zero length messages.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
/* $Req: artf1256 $ */
StatusType com_driver_internal_send_zero(com_receiverh dest)
{
	/* cast the anonymous dest type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_zero_receivercb *internal_dest = (const struct com_internal_zero_receivercb *)(dest->drv_receiver);

	/* No concurrency control needed since there is no data to read, and no API call
	 * to read it (so there is no actual concurrent update of shared data).
	 * 
	 * The flags are always set atomically by design, and the
	 * notifications can in any case always activate a task that causes an immediate
	 * context switch. Note that the flag and the task activation (if any) will not
	 * be atomic with respect to each other.
	 */
	
	/* Note that notification may include a callback, which may call an API call, which
	 * may be SendMessage(), which may call this function. To ensure no re-entrancy issues,
	 * the notification is done last after all message data structures have been updated.
	 */
	*internal_dest->flag = COM_TRUE;				 		/* $Req: artf1248 artf1258 */
	
	return IPC_NOTIFY(internal_dest);
}
