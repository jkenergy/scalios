/* This is a framework for the device-specific control system.
 * 
 * The parameters in can be a struct that is cast appropriately (depending on the code).
 * Also the struct can be RAM or ROM. If RAM, data can be passed back to the caller by
 * the function populating the struct appropriately.
 * 
 * The StatusType return code should signal device-specific errors only if a real error
 * condition has occurred, since otherwise too many calls to the error hook in extended
 * status builds will occur. But if a genuine error has occurred the error hook ought to
 * be called because the programmer will probably put a breakpoint on that error hook.
 */

#include <osint.h>

/* This is the include file to define the device:
 * 
 * #include <mydevicetype.h>
 * 
 * These will be in the header file:
 */
#define OS_MYDEVICE_SLEEP_REQUEST		(0U)
#define OS_MYDEVICE_SLEEP_STATUS		(1U)
#define E_OS_SYS_MYDEVICE_SLEEP_FAIL	(234U)

struct os_mydevicecb {
	int athing;
	char anotherthing;
};

StatusType com_device_ctl_fn(DeviceId device, DeviceControlCodeType code, DeviceControlDataType data)
{
	struct os_mydevicecb *mydevice = (struct os_mydevicecb *)device;
	
	StatusType rc;
	
	switch(code) {
		case OS_MYDEVICE_SLEEP_REQUEST:
			/* Do some stuff using 'mydevice' struct */
			mydevice->athing = 1U;
			rc = E_OK;
			break;
		case OS_MYDEVICE_SLEEP_STATUS:
			/* Look at the device using 'mydevice' and see if it worked */
			rc = E_OS_SYS_MYDEVICE_SLEEP_FAIL;
		default:
			rc = E_OS_SYS_UNKNOWN_CODE;
			break;
	}
	
	return rc;
}

