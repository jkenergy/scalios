; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2008-01-09 07:21:15 +0000 (Wed, 09 Jan 2008) $
; $LastChangedRevision: 518 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/flihnksasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			internal

.include "kernelasm.inc"

; Functions used to handle context save around ISR handlers
; In separate file for demand linking (only brought in when there are ISR handlers)


;Not already on ks, so need to switch (see also flih_on_ks for parallel code that does the same job, but doesn't switch to the kernel stack and back)
.text
.global os_flih_not_on_ks
os_flih_not_on_ks:
	disable_stackcheck

	; switch to ks
	push w2											; push w2 onto original stack
	mov w15,w2										; old sp=sp
	mov _os_kssp,w15									; sp=os_kssp
	clr _os_kssp	

	; now on the kernel stack
	
	; stack remainder of W0-W7, RCOUNT onto KS (C calling convention for caller) prior to calling C function
	push w3
	push.d w4
	push.d w6
	push RCOUNT

	; push old SP onto new stack (kernel stack)
	push w2

	; At this point W1=pre-selected slih wrapper addr
	; call C function slih(isr_cb)
	call w1

	; to keep assertion checks consistent, need to mark as being out of the kernel
	mark_out_kernel

	;Pop old SP off kernel stack
	pop w2

	; restore registers (RCOUNT, W7-W0) from KS
	pop RCOUNT
	pop.d w6
	pop.d w4
	pop w3
	
	; switch back to original stack
	mov w15,_os_kssp									; restore os_kssp to the value it was before switch to kernel stack
	mov w2,w15										; SP=old SP

	; pop the original context off the original stack and enable stack checking
	pop w2
	
	enable_stackcheck w1
	
	pop _PSVPAG										; restore values stored by the corresponding zlih
	pop.d w0
	
	; return from the interrupt handler
	clr _os_kerneltraceword							; mark the interrupt as finishing now
	retfie
