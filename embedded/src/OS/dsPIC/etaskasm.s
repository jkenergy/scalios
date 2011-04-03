; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2007-03-09 06:48:38 +0000 (Fri, 09 Mar 2007) $
; $LastChangedRevision: 366 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/etaskasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			internal

; All the extended task switching assembly; in a separate file for demand-linking (only brought in when there are extended tasks)

.include "kernelasm.inc"

; Switch from kernel stack across to extended task stack and do restore/create as requested (1 = restore context, 0 = create new context)
; Returns 1 if extended task is being preempted by a higher priority task
; or 0 if extended task is terminating or blocking
;
; "from" is a pointer (stored in w0) indicating where to save the old stack pointer.
; "to" is the new stack pointer (to the extended task stack, which has stored on it the registers to recover if restoring)
; Flag "restore" is a unat, which is a 16-bit register (w2) on this platform.
; initsp is the inital value of the stack pointer (used if restore is 0)
; 
; uint16 switch2ext(os_stackp *from, os_stackp restoresp, unat restore, os_stackp initsp)
.text
.global _os_switch2ext
_os_switch2ext:

	push.d w8		; Push W8-W14 to kernel stack
	push.d w10
	push.d w12
	push w14

	mov w15, [w0]	; Store current SP

	mov w3, w15		; Initialize SP to init value

	btss w2,#0
	goto _os_runcreatecx ; $Req: artf1095 $
	mov w1, w15		; Restore SP to given stackp value therefore moving to an extended stack
	; Restore context and continue running the user entry function
	pop w14			; Pop W14-W8 from extended stack
	pop.d w12
	pop.d w10
	pop.d w8

	return
	
; Return to the kernel stack, but also pass a reason for the
; return to the kernel
;
; W0 is the return register; this function returns first param (i.e. val, W0) to the stub caller on the kernel stack
; void jump2ks(uint16 val, os_stackp to)
.text
.global _os_jump2ks
_os_jump2ks:
	mov w15, _os_SPsave	; Only saved in order for calibration program to work out stack usage of kernel
	mov w1, w15			; Restore SP to given stackp value, moving to the kernel stack

	pop w14				; Pop W14-W8 from kernel stack
	pop.d w12
	pop.d w10
	pop.d w8

	return

; Save extended context and switch back to kernel stack
;
; W0 is the return register; this function returns first param (i.e. val, W0) to the stub caller on the kernel stack
; 
; void os_save2ks(uint16 val, os_stackp *from, os_stackp to)
.text
.global _os_save2ks
_os_save2ks:
	push.d w8		; Push W8-W14 to extended stack
	push.d w10
	push.d w12
	push w14

	mov w15, [w1]	; Store current SP

	mov w2, w15		; Restore SP to given os_stackp value, moving to the kernel stack

	pop w14			; Pop W14-W8 from kernel stack
	pop.d w12
	pop.d w10
	pop.d w8

	return




