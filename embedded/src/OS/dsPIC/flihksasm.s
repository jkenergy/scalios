; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2008-01-09 07:20:57 +0000 (Wed, 09 Jan 2008) $
; $LastChangedRevision: 517 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/flihksasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			internal

.include "kernelasm.inc"

; Function used to handle context save around ISR handlers
; In separate file for demand linking (only brought in when there are ISR handlers)

.text
.global os_flih_on_ks
os_flih_on_ks:
	; Already on the Kernel Stack, so no stack switch required
	; Turn off stack checking (if defined)
	disable_stackcheck
	
	; stack remainder of w0-w7, rcount and psvpag (C calling convention for caller) prior to calling C function
	push.d w2
	push.d w4
	push.d w6
	push RCOUNT
	
	; At this point w1=slih wrapper addr
	; call pre-selected C wrapper for slih(isr_cb)
	call w1

	; to keep assertion checks consistent, need to mark as being out of the kernel
	mark_out_kernel
	
	; restore registers (rcount, w7-w0)
	pop RCOUNT
	pop.d w6
	pop.d w4
	pop.d w2

	enable_stackcheck w1

	pop _PSVPAG										; restore values stored by the corresponding zlih
	pop.d w0										

	; return from the interrupt handler
	clr _os_kerneltraceword							; mark the interrupt as finishing now
	retfie
