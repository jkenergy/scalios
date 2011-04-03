/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-01 03:54:13 +0100 (Tue, 01 Apr 2008) $
 * $LastChangedRevision: 699 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/kernelasm.s $
 *
 * Target CPU:			ARM7
 * Target compiler:		arm-elf-gcc
 * Visibility:			internal
 *
 * Called when the kernel decides to shutdown due to catastrophic internal failure.
 * Switch to the kernel stack in order to give stack space for a possible call to ShutdownHook()
 * Then calls shutdown(error) which never returns.
 *
 * r0 should be set to the error value (never E_OK) that is passed to shutdown(error)
 *
 * void os_panic_shutdown(StatusType error)
 */
.text
.global os_panic_shutdown
os_panic_shutdown:
/* @TODO fix this up for ARM7 */

/*	; Disable hardware stack checking
	disable_stackcheck
	
	; Reset to kernel stack ready for call to shutdown
	mov #__SP_init, w15

	; Re-enable stack checking to the top of the kernel stack
	enable_stackcheck w1

	; W0 is still same as on entry, which should be the error code to be passed to shutdown
	goto _os_shutdown			; call shutdown(error)
*/
