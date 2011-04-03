/* FLIH (First Level Interrupt Handler) for ARM7TDMI IRQ vector
 *
 * This assumes that the standard VIC (Vectored Interrupt Controller) is present
 * on the device (e.g. the LPC21xx family).
 * 
 * The FLIH runs on the IRQ stack because the registers have been switched over.
 * The handler has to take care of switching to the kernel tack to run the
 * dispatcher, which will switch to the appropriate extended stack if necessary.
 * 
 * The FLIH does not have to contend with nesting or (therefore) a nesting race.
 */

/* Define access to external variables */
.L_kssp:
	.word	os_kssp
.L_curisr:
	.word	os_curisr

.ifdef DEBUG
.L_kernelnesting:
	.word	os_kernelnesting
.endif

.ifdef TRACE
.L_kerneltraceword:
	.word	os_kerneltraceword
.endif

.ifdef STACK_CHECKING
.L_curtos:
	.word	os_curtos
.L_SPoffstore:
	.word	os_SPoffstore
.endif

	.text
	.arm
	.global	irqwrapper
	.type	irqwrapper, %function

irqwrapper:
	/* Align the link register to the interrupted instruction
	 * (see Table 2-3 p2-17 of ARM7TDMI Technical Reference Manual
	 */
	sub	lr, lr, #4
	
	/* Stack the entire volatile ARM7TDMI context here because we are going to call out to a C function
	 * and must respect the convention (defined by the ARM ABI) that the caller is responsible for the
	 * following registers:
	 *
	 * r12, r0-r3
	 *
	 * Also note:
	 * r13 is the stack pointer (SP)
	 * r14 is the link register (LR)
	 * r12 is the intra-procedure-call register (IP)
	 *
	 * We need to also save r14 here because we will overwrite it when we call the ISR's entry function.
	 *
	 * We save r4 and r5 because they are going to be overwritten and used to preserve values until the end of
	 * this handler.
	 */
	stmfd sp!, {r0, r1, r2, r3, r4, r5, r12, lr}	
	
	/* We use the VIC to determine not the vector but the handle for the ISR control block.
	 *
	 * This is simply read out of VICVectAddr - 0xFFFFF030 on the LPC21xx. See LPC21xx and LPC22xx User manual
	 * page 47 section 5.12.
	 */
	mvn	r1, #0
	ldr	r0, [r1, #-4047]
		
	/* For interrupt kernel tracing this is the place to write the ISR handle to indicate an interrupt has occurred.
	 */
.ifdef TRACE
	ldr r1, .L_kerneltraceword
	str r0, [r1, #0]
.endif

	/* Turn off stack checking (if defined) */
.ifdef STACK_CHECKING
	/* Stack calibration word */
	ldr r1, .L_SPoffstore
	str sp, [r1, #0]
	bl	os_disable_stackcheck
.endif
	
	/* We can now run the SLIH to run the handler; r0 should contain the handle to the ISR control block
	 * This does the main target-independent work of running the handler
	 */
	bl os_slih
	 
	/* SLIH returns 1 in r0 if dispatch is required; if no switch then return from IRQ */
	cmp r0, #1
	bne .L1

	/* OK, now here we have to (possibly) switch stacks to the kernel stack in order to run the
	 * dispatch() function, which will run the appropriate task(s).
	 *
	 * Check if we interrupted the kernel stack. If we did then we need to get back on to
	 * that stack and go into supervisor mode.
	 *
	 * There are 3 IRQ bank registers: r13 (SP_irq), r14 (LR_irq), and SPSR (SPSR_irq).
	 * LR_irq is already saved, and SP_irq is a global variable that keeps track of this stack.
	 * We need to save SPSR_irq now and restore it when we return here prior to returning
	 * to the originally interrupted task. We use non-volatile r5, which will be preserved
	 * by the callee (i.e. the kernel task dispatcher).
	 */
	mrs r5, spsr
	
	/* Now we can switch mode by writing to CPSR mode bits. This will 'magic' the stack pointer
	 * and link register back to the value for the interrupted task (because the bank registers
	 * will be the original ones).
	 *
	 * Switch to mode 10011 (Supervisor)
	 */
	mrs r0, cpsr
	and	r0, r0, #31
	orr	r0, r0, #17
	msr cpsr_c, r0

	/* Now SP and LR are the ones from Supervisor mode. We are now back on the stack of the interrupted
	 * task. This might be an extended task's stack, in which case we now need to switch to the
	 * kernel stack. If we are already on the kernel stack, os_kssp will be 0.
	 *
	 * Should not push anything on to the stack now because a wrapper that puts data on a task stack is
	 * an overhead that is multiplied across every extended task stack.
	 */ 
	ldr r0, .L_kssp				/* &os_kssp into r0 */
	ldr r1, [r0, #0]			/* load value of kssp into r1 */
	cmp	r1, #0
	beq	.L2
		
	/* Save the stack pointer into a non-volatile register */
	mov r4, sp
	
	/* Move the stack to the kernel stack (set sp from os_kssp) */
	mov sp, r1

	/* Clear os_kssp */
	mov	r1, #0
	str r1, [r0, #0]

.L2:
	/* Now we know we are on the kernel stack and can dispatch the higher priority tasks.
	 */
	bl	dispatch

	/* The higher priority tasks have finished and now we return to the interrupted task.. */
	
	/* Restore the stack pointer */
	mov sp, r4
	
	/* Switch back to mode 10010 (IRQ) to get back to IRQ stack.
	 */
	mrs r0, cpsr
	and	r0, r0, #31
	orr	r0, r0, #18
	msr cpsr_c, r0

	/* Recover SPSR_irq */
	msr spsr, r5
	
	/* Now we are back running on the original IRQ stack, with the IRQ bank registers
	 * as they were, and should be able to return from the interrupt back to the original thread.
	 */
.L1:
	/* Clear os_curisr to zero since no nesting (i.e. not restore of curisr) and no longer
	 * in an ISR (so GetISRID returns INVALID_ISR).
	 *
	 * $Req: artf1322 $
	 */
	ldr r0, .L_curisr
	mov	r1, #0
	str r1, [r0, #0]

.ifdef DEBUG
	/* To keep assertion checks consistent, need to mark as being out of the kernel now. Decrement the kernel nesting count. */
	ldr	r0, .L_kernelnesting
	ldr	r1, [r0, #0]
	sub	r1, r1, #1
	str	r1, [r0, #0]
.endif

.ifdef STACK_CHECKING
	bl os_enable_stackcheck
.endif

	/* Mark the interrupt as finishing for performance tracing */
.ifdef TRACE
	ldr r0, .L_kerneltraceword
	mov	r1, #0
	str r1, [r0, #0]
.endif
	
	/* Return from interrupt at end of wrapper */
	ldmfd	sp!, {r0, r1, r2, r3, r4, r5, r12, pc}^
