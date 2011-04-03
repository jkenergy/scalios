/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 *
 * $LastChangedDate: 2008-01-17 05:05:51 +0000 (Thu, 17 Jan 2008) $
 * $LastChangedRevision: 560 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/devices/osappdevices.h $
 *
 * Target CPU:          Generic
 * Target compiler:     Standard ANSI C
 * Visibility:          User
 */
 
#ifndef DEVICES_H_
#define DEVICES_H_

struct os_devicecb;             /* Anonymous struct; can only assign pointers. This is cast to a concrete type by driver functions */
struct os_drivercb;             /* Anonymous struct; can only assign pointers. This is cast to a concrete type by driver functions */

StatusType os_driver_default_ctl(DeviceId, DeviceControlCodeType, DeviceControlDataType);

/* Structure that represents all common functions provided by drivers */
struct os_driver_handle_cb {
	StatusType (*ctl_device)(DeviceId, DeviceControlCodeType, DeviceControlDataType);	/* ctl function common to all device drivers; Points to os_driver_default_ctl() if not implemented by driver */
};
 
/* Structure used to tie device to driver for the an API call to access ctl_device call */
struct os_device_handlecb {
	const struct os_devicecb *device;					/* ptr to the device specific control block */
	const struct os_driver_handle_cb *driver;			/* ptr to the driver for the device */
}; 

extern const struct os_device_handlecb const os_devices[];       	/* Stored in ROM; Device control block instances */
extern const struct os_device_handlecb * const os_last_device;

#endif /*DEVICES_H_*/
