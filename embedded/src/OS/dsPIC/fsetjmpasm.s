; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2007-01-12 13:50:06 +0000 (Fri, 12 Jan 2007) $
; $LastChangedRevision: 333 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/fsetjmpasm.s $

; Target CPU:			dsPIC
; Target compiler:		MPLAB ASM30
; Visibility:			internal

.text
.global _os_fsetjmp
_os_fsetjmp:
; Called with w0 indicating the environment buffer (see fsetjmp.h)
; Returns 0 in w0 to indicate returning from the initial call to fsetjmp()
	mov.d w8,[w0++] 			; store double word pair
	mov.d w10,[w0++] 			; store double word pair
	mov.d w12,[w0++] 			; store double word pair
	mov w14,[w0++] 				; store single word (not including stack pointer)
	sub.w w15, #4, w1			; store what stack pointer will be after return
	mov w1, [w0++]				
	mov.d [w1], w2				; store return address
	mov.d w2, [w0++]
	retlw.w #0, w0				; return 0 (i.e. return value from fsetjmp() function)	

.global _os_flongjmp
_os_flongjmp:
; Called with w0 indicating the environment buffer (see fsetjmp.h)
; and w1 indicating the return value that is passed to the return of fsetjmp() - i.e. moved to w1
;
; Function does not return to caller; returns to caller of fsetjmp
	mov.d [w0++], w8			; see fsetjmp
	mov.d [w0++], w10
	mov.d [w0++], w12
	mov.d [w0++], w14			; restores stack pointer as well; stack now pointing to where it will be
	mov.d [w0], w2
	mov.d w2,[w15++]			; push the stored return address back on the stack
	mov w1,w0					; pass the return code through
	return						; return to the (original) caller of setjmp()
