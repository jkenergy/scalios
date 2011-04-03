/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-01 03:54:13 +0100 (Tue, 01 Apr 2008) $
 * $LastChangedRevision: 699 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/etaskasm.s $
 *  
 * Target CPU:		ARM7TDMI
 * Target compiler:	arm-elf-gcc
 * Visibility:		Internal
 *
 * All the extended task switching assembly; in a separate file for demand-linking (only brought in when there are extended tasks)
 */


/* Function to switch from kernel stack across to extended task stack and do restore/create as requested (1 = restore context, 0 = create new context)
 * Returns 1 if extended task is being preempted by a higher priority task
 * or 0 if extended task is terminating or blocking
 *
 * "from" is a pointer (stored in w0) indicating where to save the old stack pointer.
 * "to" is the new stack pointer (to the extended task stack, which has stored on it the registers to recover if restoring)
 * Flag "restore" is a unat, which is a 16-bit register (w2) on this platform.
 * initsp is the inital value of the stack pointer (used if restore is 0)
 * 
 *                              r0              r1              r2                 r3 
 * uint16 switch2ext(os_stackp *from, os_stackp restoresp, unat restore, os_stackp initsp)
 */
	.text
	.arm
	.global os_switch2ext
os_switch2ext:
	/* Save the non-volatile context (callee saves). Executing in Supervisor mode, and
	 * remaining in Supervisor mode after the stack switch.
	 *
	 * Non-volatile context to save is r4-r11, r14 (lr).
	 */
	stmfd sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}
	
	/* Write the current (kernel) stack pointer to 'from' (i.e. indirect through r0) */
	str sp, [r0, #0]
	
	/* If 'restore' flag is non-zero then restoring a context, else creating a new one */
	cmp r2, #0
	bne .L_restore
	
	/* Creating a new context. Initialise SP to 'init' value and jump to os_runcreatecx() 
	 *
	 * $Req: artf1095 $
	 */
	mov sp, r3
	
	/* Now on extended task's (empty) stack; start a new context */
	b os_runcreatecx
	/* NOTREACHED */
	
.L_restore:
	/* Restore SP to 'restoresp' value (r2) */
	mov sp, r2
	
	/* Now on extended task's stack where we left off when the task blocked */

	/* Restore non-volatile context from stack saved when the task blocked */

	ldmfd sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}

	/* Now able to return to blocking point, jumping to Thumb mode if appropriate */
	bx lr

	
/* Function to return to the kernel stack, but also pass a reason for the
 * return to the kernel so that the appropriate action can be taken. 
 *
 * r0 is the return register; this function returns first param (i.e. val, r0) to the stub caller on the kernel stack
 *
 *                    r0            r1
 * void jump2ks(unat val, os_stackp to)
 */
.L_SPsave:
	.word os_SPsave

	.text
	.arm
	.global os_jump2ks
os_jump2ks:
	/* Save the stack pointer into a global variable; used to calibrate stack overheads of kernel */
	ldr r2, .L_SPsave
	str sp, [r2, #0]
	
	/* Restore SP to given stackp value, moving to the kernel stack */
	mov sp, r1
	
	/* Restore the registers originally saved on move from the kernel stack to extended task's stack */
	ldmfd sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}

	/* Return to place in kernel where call to os_switch2ext was made */
	bx lr


/* Function to save extended context and switch back to kernel stack. Called when an extended task blocks.
 *
 * r0 is the return register; this function returns first param (i.e. val) to the stub caller on the kernel stack.
 *
 *                       r0             r1              r2
 * void os_save2ks(unat val, os_stackp *from, os_stackp to)
 */
	.text
	.arm
	.global os_save2ks
os_save2ks:
	/* Save non-volatile context before returning to kernel */
	stmfd sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}
	
	/* Store current SP in place directed by 'from' */
	str sp, [r1, #0]

	/* Restore SP to 'to' value */
	mov sp, r2
	
	/* Now on kernel stack; recover saved non-volatile context */
	ldmfd sp!, {r4, r5, r6, r7, r8, r9, r10, r11, lr}

	/* Return to executing kernel */
	bx lr
