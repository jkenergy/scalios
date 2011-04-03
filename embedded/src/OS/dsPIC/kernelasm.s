; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2008-01-09 07:22:11 +0000 (Wed, 09 Jan 2008) $
; $LastChangedRevision: 519 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/kernelasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			internal

.include "kernelasm.inc"

; Called when the kernel decides to shutdown due to catastrophic internal failure.
; Switch to the kernel stack in order to give stack space for a possible call to ShutdownHook()
; Then calls shutdown(error) which never returns.
;
; W0 should be set to the error value (never E_OK) that is passed to shutdown(error)
;
; void panic_shutdown(StatusType error)
.text
.global _os_panic_shutdown
_os_panic_shutdown:
	; Disable hardware stack checking
	disable_stackcheck
	
	; Reset to kernel stack ready for call to shutdown
	mov #__SP_init, w15

	; Re-enable stack checking to the top of the kernel stack
	enable_stackcheck w1

	; W0 is still same as on entry, which should be the error code to be passed to shutdown
	goto _os_shutdown			; call shutdown(error)
	
