/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-11 02:31:35 +0000 (Fri, 11 Jan 2008) $
 * $LastChangedRevision: 556 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/shutdown.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#include <osint.h>

/* This function would normally be better implemented as a macro, but it exposes ENTER_KERNEL() and NOT_REACHED() into
 * the API (since the user code gets compiled against it). This exposes too much kernel internals for this to be desirable.
 */
void os_ShutdownOS(StatusType Error)
{
	ENTER_KERNEL();
#ifdef OS_EXTENDED_STATUS
	if(INTERRUPTS_LOCKED() && os_flags.errorhook) {
		/* call the error hook handler */
		OS_ERRORHOOK_1(ShutdownOS, E_OS_DISABLEDINT, Error);
		/* Prepare to panic shutdown with E_OS_DISABLEDINTS error code */
		Error = E_OS_DISABLEDINT;
	}
#endif
	OS_API_TRACE_SHUTDOWN_OS();
	os_shutdown(Error);
	NOT_REACHED();
}
