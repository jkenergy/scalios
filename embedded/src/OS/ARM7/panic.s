/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-01 02:54:13 +0000 (Tue, 01 Apr 2008) $
 * $LastChangedRevision: 699 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.1.240/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/target.c $
 * 
 * Target CPU:			ARM7TDMI
 * Target compiler:		arm-elf-gcc
 * Visibility:			Internal
 * 
 * Default trap handler to shut down the OS. In a separate file in order to be demand-linked: can be
 * replaced by alternative code if not needed.
 * 
 * Note the error code values:
 * 
 * E_OS_SYS_UNDEFINED_INSTR				40
 * E_OS_SYS_SOFTWARE_INT				41
 * E_OS_SYS_PREFETCH_ABORT				42
 * E_OS_SYS_DATA_ABORT					43
 * E_OS_SYS_RESERVED					44
 * 
 * See oslibtarget.h: must be kept consistent.
 */

/* os_ksp_init is the start of the kernel stack.
 */
.L_ksp_init:
	.word os_ksp_init
	
	.text
	.arm
	.global os_trap_undefined_inst
os_trap_undefined_inst:
	ldr sp, .L_ksp_init
	mov r0, #40
	bl os_shutdown

	.global os_trap_software_int
os_trap_software_int:
	ldr sp, .L_ksp_init
	mov r0, #41
	bl os_shutdown

	.global os_trap_prefetch_abort
os_trap_prefetch_abort:
	ldr sp, .L_ksp_init
	mov r0, #42
	bl os_shutdown

	.global os_trap_data_abort
os_trap_data_abort:
	ldr sp, .L_ksp_init
	mov r0, #43
	bl os_shutdown

	.global os_trap_reserved
os_trap_reserved:
	ldr sp, .L_ksp_init
	mov r0, #44
	bl os_shutdown
