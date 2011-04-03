/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal/ipcreinitzero.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>
#include <drivers/internal.h>
#include "ipcint.h"

/* Driver "reinit receiver" function. The 'device type' is local IPC. Only works for a zero length receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_internal_reinit_receiver_zero(com_receiverh receiver)
{
	/* cast the anonymous receiver type to the concrete driver-specific receivercb used by this driver function */
	const struct com_internal_zero_receivercb *internal_recv = (const struct com_internal_zero_receivercb *)(receiver->drv_receiver);
	
	/* Zero messages have no receive buffer, so to reinit just need to clear the flag */
		
	*internal_recv->flag = COM_FALSE;		/* $Req: artf1248 artf1258 $ clear associated flag */
	
	return E_OK;	
}
