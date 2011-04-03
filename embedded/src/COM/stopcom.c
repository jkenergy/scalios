/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/stopcom.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>


/* Performs the "real" formal shutdown of COM.
 * Must be only called when shutdown is pending
 */
void com_final_shutdown()
{
	StatusType rc = E_OK;
	const struct com_device_initcb *device = &com_init_devices[0];
	uint16 cnt;
	
	assert(com_shutdown_pending==1);
	assert(com_calldepth==0);
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SHUTDOWN);
	COM_TRACE_OFF();
	
	com_shutdown_pending = 0;	/* clear ending flag since formal shutdown now being done */
	
	/* Stop each Device */
	for (cnt = com_num_init_devices; cnt > 0; cnt--) {
	
		/* Use driver specific function to stop the device */
		StatusType drv_rc;
		
		assert(device->stop_device);	/* devices that do not provide stop function should not be in com_devices[] */
				
		drv_rc = device->stop_device(device->device);
		
		if ( drv_rc != E_OK ) {
			rc = drv_rc;
		}
		
		device++;
	}
	
	/* 
	 * Call error hook if any stop_device() calls reported error.
	 * Call hook as if called from StopCOM API call with COM_SHUTDOWN_IMMEDIATE Mode value.
	 */
	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {
		
		COMShutdownModeType Mode = COM_SHUTDOWN_IMMEDIATE;	/* necessary to use error hook mechanism correctly */
		
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_1(StopCOM, rc, Mode);
		
		LEAVE_COM_ERROR_HOOK();
	}	
}


/* 
 * StopCOM API call.
 * 
 * com_appmode is set to 0 (flag indicating com no longer available)
 * com_shutdown_pending is set to 1 (indicating shutdown should occur as soon as possible)
 * 
 * If StopCOM() is called when no other COM call is active, then the shutdown will occur immediately.
 * Otherwise it will occur when the last top level COM API call exits.
 * 
 * This mechanism allows for graceful shutdown of COM within a multi-tasking system, since final shutdown
 * does not actually occur until all COM calls have completed.
 * 
 * Any new calls made after StopCOM() will fail in extended status, since COM is seen as been shutdown as soon
 * as the call to StopCOM() is made, even if final shutdown is still pending.
 */

#ifdef COM_EXTENDED_STATUS
StatusType StopCOM(COMShutdownModeType Mode)
{
	StatusType rc;
	
	/* No COM_API_ENTER() since StopCOM() creates an imbalance in nesting count, forcing shutdown by highest level call */
	
	if (!COM_STARTED()) {
		rc = E_COM_SYS_STATE;		/* COM not yet started */
	}	
	else if (Mode != COM_SHUTDOWN_IMMEDIATE) {
		rc = E_COM_ID;				/* Failed with wrong shutdown mode */
	}
	else {
		com_appmode = 0;			/* COM officially stopped from this point on $Req: artf1274 $ */
	
		com_shutdown_pending = 1;	/* Indicate that that shutdown is now pending */
	
									/* Do actual shutdown of COM layer if no other COM API calls active */
		if (--com_calldepth==0) {
			com_final_shutdown();
		}
	}

	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		/* $Req: artf1265 $ */
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_1(StopCOM, rc, Mode);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	return rc;
}
#else
/* Standard status build */
StatusType StopCOM(COMShutdownModeType Mode)
{
	/* No COM_API_ENTER() since StopCOM() creates an imbalance in nesting count, forcing shutdown by highest level call */
	
	com_appmode = 0;			/* COM officially stopped from this point on $Req: artf1274 $ */

	com_shutdown_pending = 1;	/* Indicate that the formal shutdown is now pending */

								/* Do actual shutdown of COM layer if no other COM API calls active */
	if (--com_calldepth==0) {
		com_final_shutdown();
	}
	
	return E_OK;
}
#endif

