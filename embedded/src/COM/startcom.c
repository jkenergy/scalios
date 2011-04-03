/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/startcom.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

unat com_calldepth;

/* Static helper function to init devices and receiving messages */
static StatusType init_devices()
{
	StatusType rc = E_OK;
	const struct com_device_initcb *device = &com_init_devices[0];
	const struct com_messagecb *msg = &com_rcv_msgs[0];
	uint16 i;
	
	/* Start all COM Devices */
	for (i = com_num_init_devices; i > 0; i--) {
	
		/* Use driver specific function to start the device */
		StatusType drv_rc;
		
		assert(device->start_device);	/* devices that do not provide startup function should not be in com_devices[] */ 
		
		drv_rc = device->start_device(device->device);
		
		if (drv_rc != E_OK && rc == E_OK) {
			rc = drv_rc;	/* only record first error code that is returned */
		}
		
		device++;
	}
	
	/* Initialise all COM receivers */
	for (i = com_num_rcv_msgs; i > 0; i--) {
		
		com_receiverh receiver = msg->cb.receiver;
		
		/* Use driver specific function to re-init the message */
		StatusType dev_rc = receiver->driver->reinit_receiver(receiver);
		
		if (dev_rc != E_OK && rc == E_OK) {
			rc = dev_rc;	/* only record first error code that is returned */
		}		
		
		msg++;
	}
	
	return rc;
}



StatusType StartCOM(COMApplicationModeType Mode)
{
	StatusType rc = E_OK;
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_START);
	COM_TRACE_OFF();
	
#ifdef COM_EXTENDED_STATUS

	/* @TODO artf1315 Check if started from a task, otherwise return implementation specific error code
	 * Specification suggests that the call is not supposed to be called from an ISR. Don't know why not.
	 * No standard OSEK call is provided that allows identification of whether a task is currently running.
	 */
	
	/* Mode numbers are greater than zero; used as a check to see if COM is started */
	
	if(COM_STARTED() || com_shutdown_pending) {
		rc = E_COM_SYS_STATE;		/* COM already started, or pending shutdown has not yet completed */
	}
	else if(Mode < 1 || Mode > com_mode_count) {		/* Range check Mode to make sure it's legal, return E_COM_ID if not */
		rc = E_COM_ID;				/* Invalid startup mode $Req: artf1273 $ */
	}
	else {
#endif
		assert(com_calldepth == 0);			/* must be zero if com was not already started */

		com_appmode = Mode;
		com_calldepth = 1;					/* init nest count for COM API calls, this allows graceful shutdown when nested API call stack active */
		
		/* init COM devices and messages */
		rc = init_devices();
		
		if(com_call_StartCOMExtension) {
			/* call user supplied function, returning status code */
			if ( rc == E_OK ) {
				rc = StartCOMExtension();
			}
			else {
				/* don't overwrite error code returned from starting devices/initialising receivers */
				StartCOMExtension();
			}
		}
#ifdef COM_EXTENDED_STATUS		
	}
#endif
	
	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		/* $Req: artf1265 $ */
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_1(StartCOM, rc, Mode);
		
		LEAVE_COM_ERROR_HOOK();
	}

	/* No need for any specific initialization of message objects since C run-time startup
	 * (and StopCOM) will ensure that they already contain the right values.
	 * $Req: artf1271 artf1262 artf1261 artf1245 $
	 */
	return rc;
}

