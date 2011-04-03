; Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
; 
; $LastChangedDate: 2008-01-09 23:57:18 +0000 (Wed, 09 Jan 2008) $
; $LastChangedRevision: 538 $
; $LastChangedBy: kentindell $
; $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/common/dsPIC/targetasm.s $

; Target CPU:			dsPIC30F
; Target compiler:		MPLAB ASM30
; Visibility:			testing

; Code to capture the SP on entry to a basic or extended task function. Used in calibrating stack
; overheads.

	.global _os_SPentry					; Place where SP is stored

	.section	.text,code
	.def	_os_taskentry_TaskSPSave1
	.val	_os_taskentry_TaskSPSave1
	.scl	2
	.type	041
	.endef
	.global	_os_taskentry_TaskSPSave1	; export
_os_taskentry_TaskSPSave1:
	mov w15, _os_SPentry	; Store current SP
	mov _os_SPoffstore, w0
	mov w0, _os_SPoffstore2
	rcall _os_TerminateTask
	reset					; NOTREACHED

	.section	.text,code
	.def	_os_taskentry_TaskSPSave2
	.val	_os_taskentry_TaskSPSave2
	.scl	2
	.type	041
	.endef
	.global	_os_taskentry_TaskSPSave2	; export
_os_taskentry_TaskSPSave2:
	mov w15, _os_SPentry	; Store current SP
	rcall _os_TerminateTask
	reset					; NOTREACHED
