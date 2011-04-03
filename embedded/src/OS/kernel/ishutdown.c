/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-11 06:03:28 +0000 (Mon, 11 Feb 2008) $
 * $LastChangedRevision: 608 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/ishutdown.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#include <osint.h>

/* Shuts down the OS by calling the ShutdownHook() if required to do so, then either -
 * 
 * 	1) Performs a system reset, if any error code other than E_OK is given, or
 *  2) Returns back to the StartupOS() call that started the OS, if error code was E_OK
 * 
 * This code has to be reentrant in case of a double failure during shutdown hook.
 */
void os_shutdown(StatusType error)
{
 	/* If called to handle OS panic (e.g. stack overflow trap) then will have already been switched to
 	 * kernel stack. If called from ShutdownOS() API call then will be running on stack of calling task.
 	 */

	/* No call to PostTaskHook() when shutting down */
	/* $Req: artf1043 $ */
	
	/* Should do:
	 * 
	 * assert(KERNEL_LOCKED());
	 * 
	 * but this will fail if this function is called from a trap handler (in assembler)
	 * since MARK_IN_KERNEL() is in C and conditionally compiled. Nevertheless, a call
	 * from a trap handler runs with IPL3 = 1 and hence regular ISRs cannot occur
	 */
	assert(STACKCHECK_ON());

	if (os_flags.shutdownhook) {					/* Check if need to call the shutdown handler. */
		os_flags.shutdownhook = 0;					/* Prevent re-entry to the Shutdown hook */
		/* Should wrap the call to ShutdownHook() with MARK_OUT_KERNEL() and MARK_IN_KERNEL() but
		 * since this might be called from a trap (see above) the marking might fail. It is OK not
		 * to call the markers because ShutdownHook() may make no API calls other than
		 * GetActiveApplicationMode() - see OSEK OS spec 2.2.3 page 45 - which is simply a macro-wrapper
		 * to access a variable (see core.h). Similarly there is no need to change os_curpri because
		 * there is never a valid call to LEAVE_KERNEL().
		 */
		/* $Req: artf1218 $ */
		OS_API_TRACE_SHUTDOWN_HOOK(error);
		OS_KERNEL_TRACE_SHUTDOWN_HOOK_START();
		ShutdownHook(error);						/* call the hook routine with IPL set to kernel level or trap level and stack checking on */
		OS_KERNEL_TRACE_SHUTDOWN_HOOK_FINISH();
	}

	/* $Req: artf1217 $ */
	DISABLE_STACKCHECK();
	 
	if (error != E_OK)	{
		SYSTEM_RESET();								/* do system reset if non-E_OK error code specified */
	}
	else {
		/* @TODO task1038 check that this works correctly when called from extended task stack */
		/* Can only do this if E_OK, which is only true if ShutdownOS() called with E_OK;
		 * cannot be from a trap call or internal kernel panic since these never use E_OK.
		 */
		LONGJMP(os_startosenv, 1);						/* jump back to the StartOS() if called with E_OK */	
	}
 
	NOT_REACHED();
}

