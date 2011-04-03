/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-02 01:03:26 +0100 (Thu, 02 Aug 2007) $
 * $LastChangedRevision: 454 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/default/defaultinit.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

/* Driver "receiver init" function. The 'device type' is local IPC. Only works for a queued receive message.
 * There is only one instance of the IPC pseudo-device, so no "device" is specified in the concrete driver-specific receivercb.
 */
StatusType com_driver_default_init_receiver(com_receiverh receiver, ApplicationDataRef data)
{
#ifdef COM_EXTENDED_STATUS	
	return E_COM_ID;				/* Not a valid message, so return E_COM_ID in extended status */
#else
	return E_OK;					/* Not a valid message, so return E_OK in standard status */
#endif
}
