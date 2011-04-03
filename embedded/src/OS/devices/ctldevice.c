/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-24 01:00:12 +0100 (Fri, 24 Aug 2007) $
 * $LastChangedRevision: 471 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/devices/ctldevice.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

StatusType os_ControlDevice(DeviceType Device, DeviceControlCodeType Code, DeviceControlDataType Data)
{
	StatusType rc;
	
	ENTER_KERNEL();
	OS_API_TRACE_CONTROL_DEVICE(Device, Code, Data);

	
#ifdef OS_EXTENDED_STATUS
	if(!VALID_DEVICE(Device)) {
		rc = E_OS_ID;						/* Not a valid device */
	}
	else {
		assert(Device->driver->ctl_device);
		rc = Device->driver->ctl_device(Device->device, Code, Data);
	}
#else
	rc = Device->driver->ctl_device(Device->device, Code, Data);
#endif

	if (rc != E_OK && os_flags.errorhook) {
		/* call the error hook handler */
		OS_ERRORHOOK_2(ControlDevice, rc, Code, Data);
	}

	OS_API_TRACE_CONTROL_DEVICE_FINISH(rc);
	LEAVE_KERNEL();
	
	return rc;
}
