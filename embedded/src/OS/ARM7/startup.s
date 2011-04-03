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
 * Default startup code to initialise the C run-time environment.
 *
 * 1. Set up stacks.
 * 2. Set up interrupt vectors.
 * 2. Initialize data section.
 * 3. Clear BSS section.
 * 4. Call main().
 */
 
/* os_ksp_init is the start of the kernel stack. All basic tasks and the kernel itself run on the kernel
 * stack. The kernel and tasks run in Privileged mode.
 *
 * The abort, Undefined, System and User modes all share the same one-word scratch stack. When the trap occurs
 * the stack is switched to the base of the kernel stack and shutdown is called. The execution mode
 * remains in the mode of the trap.
 *
 * The FIQ and IRQ stacks are separately allocated.
 */
 	.section b
	.balign 4
.L_scratch_stack_init:
	.word os_scratch_stack
.L_k_stack:
	.word os_k_stack
.L_irq_stack:
	.word os_irq_stack
.L_fiq_stack:
	.word os_irq_stack
