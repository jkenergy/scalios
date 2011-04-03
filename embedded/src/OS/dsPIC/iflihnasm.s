; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2007-01-12 13:50:06 +0000 (Fri, 12 Jan 2007) $
; $LastChangedRevision: 333 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/iflihnasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			internal

.include "kernelasm.inc"

.global os_inner_flih_nesting_no_sc
.text
os_inner_flih_nesting_no_sc:
inner_flih_nesting 0
