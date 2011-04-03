/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-10-20 10:36:51 +0100 (Sat, 20 Oct 2007) $
 * $LastChangedRevision: 479 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/devices/defaultctl.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

/* 
 * Default Driver "ctl" function. Provided for device drivers that do not support the function.
 */
StatusType os_driver_default_ctl(DeviceId device, DeviceControlCodeType code, DeviceControlDataType data)
{
	return E_OS_SYS_UNKNOWN_CODE;	/* No driver ctl function, so code must be unknown */
}
