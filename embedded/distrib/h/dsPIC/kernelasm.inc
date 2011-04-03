; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2007-04-10 23:11:14 +0100 (Tue, 10 Apr 2007) $
; $LastChangedRevision: 411 $
; $LastChangedBy: markdixon $
; $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/kernelasm.inc $

; Header file to bring in to generated assembler (systemasm.s) to instantiate per-ISR "wrappers".

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			External (generated assembler)
;
;
.global _os_isrcb_h					; Handle used in code resolving nesting race

.ifdef STACK_CHECKING
.macro enable_stackcheck t0
	mov.w	  _os_curtos, \t0
	mov.w     \t0, SPLIM
	nop								; Can't access the stack directly after a write to SPLIM
.endm
.macro disable_stackcheck
	setm      SPLIM
	mov.w     w15, _os_SPstore			; Can't access the stack directly after a write to SPLIM
.endm

.equ ISRCB_SIZE,6					; 6 with this compiler - CHECK WHEN PORTING OR CHANGING THE C code:should be sizeof(isrcb)

.else ; no stack checking

.macro enable_stackcheck t0
.endm

.macro disable_stackcheck
	mov.w		w15, _os_SPstore		; Needed only for kernel stack usage calibration
.endm

.equ ISRCB_SIZE,4
.endif

; Generic Zero Level Interrupt Handler (ZLIH) macro that is expanded for each ISR handler

.macro zlih stackcheck, stacksw, nestrace, isrcb_number
; stchchk=1 for stack checking, 0 for no stack checking (no overflow checking needed)
; stksw=1 for stack switching, 0 for no stack switching required (basic tasks only)
; nestrace=1 if nesting race can occur, 0 if it can't (e.g. only cat 2 ISRs at one IPL, or NSTDIS set)

	push.d w0										; Stack original W0,W1 first, since we are going to trash them now
	
	push	_PSVPAG									; Stack PSVPAG in case it is managed by compiler 
	mov.b	#__const_psvpage,w0
	mov	w0, _PSVPAG	
	
	
	mov _os_kernelipl,w0							; Set IPL to kernel level now to prevent nested race ASAP 
	mov.b wreg,_SRbits		
	mov	#_os_isrs+\isrcb_number*ISRCB_SIZE,w0		; Move isrcb addr to w0
.if \nestrace 
													; Stack contents at this point are as follows -
													;	
													;		15								0
													;	-10	|	PC<15:0>					|
													;	-8	|	SR<7:0> CORCON<3> PC<22:16>	|
													;	-6	|	W0							|
													;	-4	|	W1							|
													;	-2	|	PSVPAG						|
													;	SP	|	<free word>					|
													
	mov w0,_os_isrcb_h								; Temporary store for ISR control block handle used to resolve nesting race only
	mov [w15-8],w0									; Get the previous SR from the stack and store (SR stacked by CPU prior to handler being called)
.endif
.if \nestrace & \stackcheck
	mov #handle(os_inner_flih_nesting_sc),w1			; Select right version of the First Level Interrupt Handler (FLIH) to run
.endif
.if \nestrace & !\stackcheck
	mov #handle(os_inner_flih_nesting_no_sc),w1
.endif
.if !\nestrace & \stackcheck
	mov #handle(os_inner_flih_no_nesting_sc),w1
.endif
.if !\nestrace & !\stackcheck
	mov #handle(os_inner_flih_no_nesting_no_sc),w1
.endif

; At this point w0 contains the previous SR, and w1 contains an address for the right "inner" FLIH code. Optionally _isrcb_h contains a
; handle to the ISR control block. The stack is as drawn above.

.if \stacksw
	cp0 _os_kssp										; Maybe on kernel stack, maybe not
	bra nz, os_flih_not_on_ks
.endif
	goto os_flih_on_ks									; On the kernel stack, so don't need to switch stacks
.endm

;---------------------------------------------------------------------------------------------

.macro inner_flih_nesting stackcheck
	; call C function slih(isr_cb)
	push w0											; store old SR on stack
	mov _os_curisr,w0									; get and store os_curisr in case of nested interrupts
	push w0
	mov _isrcb_h,w0									; get isrcb into W0 to pass to slih(isr_cb)

.if \stackcheck
	CALL _os_slih_stackcheck
.else
	CALL _os_slih_no_stackcheck
.endif

	pop w0
	mov w0,_os_curisr									; restore os_curisr
	pop w0											; restore old SR from stack
		
	; called C function returns value in W0 indicating whether dispatch is required
	btss w0,#0;	w0==0
	return

	; nesting race can occur, so compare IPLs of "old_sr" to "os_curpri"
	lsr	w1,#13,w1									; get bits 15:13 which are the IPL of the SR (see stack content on entry)
	mov	_os_curpri,w0
	lsr	w0,#5,w0									; get bits 7:5 of curpri, which is the IPL of the current priority value
	
	cpseq w0,w1										; if old_sr(IPL)!=curpri(IPL) then nesting race has occurred, so do not dispatch
	return
	mov _ks_dispatch, w0
	goto w0
.endm

.macro inner_flih_no_nesting stackcheck
	; call C function slih(isr_cb), at this point w0=isr_cb
.if \stackcheck
	call _os_slih_stackcheck
.else
	call _os_slih_no_stackcheck
.endif		
	; clear os_curisr to zero since no nesting (i.e. not restore of curisr) and no longer
	; in an ISR (so GetISRID returns INVALID_ISR). os_curisr is a file register in near space.
	; $Req: artf1322 $
	clr _os_curisr

	; called C function returns value in W0 indicating whether dispatch is required
	btss w0,#0
	return
	; W0==1, so dispatch is required
	mov _os_ks_dispatch, w0
	goto w0	
	return
.endm
