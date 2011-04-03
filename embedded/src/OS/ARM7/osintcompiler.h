/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/osintcompiler.h $
 *  
 * Target CPU:		ARM7
 * Target compiler:	arm-elf-gcc
 * Visibility:		Internal
 */
#ifndef _I_COMPILER_H_
#define _I_COMPILER_H_

#define USE_STANDARD_SETJMP

#ifdef STACK_CHECKING

/* There is no built-in hardware for stack checking so use a software technique:
 * 
 * 1. When stack checking is turned on, write a 'magic' word into the stack above the declared
 *    stack space.
 * 2. When the stack checking is turned off, read the word from the stack and check against the
 *    magic number. If there is no match then the stack has overflowed.
 * 
 * The stack must have an extra word allocated to hold this stack space: this is a configuration
 * tool issue.
 * 
 * On the ARM7, the ABI defines the stack as 'full descending'. This means that the stack grows
 * down and the stack pointer points to the last item pushed on the stack. The stack needs to be
 * allocated with this spare word on top of the requested stack space (see the host tool for
 * more details).
 */

#ifdef DEBUG
extern unat os_stack_checking_on;
#define STACKCHECK_ON()					(os_stack_checking_on == 1U)
#define STACKCHECK_OFF()				(os_stack_checking_on == 0)
#define INIT_STACKCHECK_TO_OFF()		(os_stack_checking_on = 0)
#define STACKCHECK_CLEAR()				(os_stack_checking_on = 0)
#define STACKCHECK_SET()				(os_stack_checking_on = 1)
#else
#define STACKCHECK_ON()					(1U)
#define STACKCHECK_OFF()				(1U)
#define INIT_STACKCHECK_TO_OFF()		{}
#endif

/* Helper functions to verify the stack has not overflowed */
void os_enable_stackcheck(void);
void os_disable_stackcheck(void);

#define DISABLE_STACKCHECK()			{GETSP(os_SPoffstore); os_disable_stackcheck();}
#define ENABLE_STACKCHECK()				{os_enable_stackcheck();}

#else /* Not using stack checking at all */

#define INIT_STACKCHECK_TO_OFF()		{}
#define DISABLE_STACKCHECK()			{}			
#define ENABLE_STACKCHECK()				{}
#define STACKCHECK_OFF()				(1U)
#define STACKCHECK_ON()					(1U)

#endif /* STACK_CHECKING */

/* This sets the hardware priority to lock or unlock the kernel; the hardware priority is changed after the
 * software priority level is changed due to evaluation order
 */

#define DECLARE_SAVE_TARGET_TASK_CONTEXT	/* no extra context */
#define DECLARE_SAVE_TARGET_ISR_CONTEXT		/* no extra context */

#define RESTORE_TARGET_TASK_CONTEXT			/* no extra context */
#define RESTORE_TARGET_ISR_CONTEXT			/* no extra context */

#define GETSP(tmp)							{asm("mov %0, r13" : "=r"(tmp));}

typedef void (*os_hookf) (void);

/* Side-effect: assigns back to r0; FSB() applied only to non-empty priority queue mask.
 * Based on standard count-leading-zeroes algorithm, flipped to give the bit number of
 * the most significant set bit.
 */
#define FSB(r0)								{unsigned int r1, r2; assert(r0);\
											 asm("movs %1,%0,lsr #16\n"\
												 "moveq %1,%0\n"\
												 "moveq %0,#16\n"\
												 "movne %0,#0\n"\
												 "movs %2,%1,lsr #8\n"\
												 "addeq %0,%0,#8\n"\
												 "moveq %2,%1\n"\
												 "movs %1,%2,lsr #4\n"\
												 "addeq %0,%0,#4\n"\
												 "moveq %1,%2\n"\
												 "movs %2,%1,lsr #2\n"\
												 "addeq %0,%0,#2\n"\
												 "moveq %2,%1\n"\
												 "movs %1,%2,lsr #1\n"\
												 "addeq %0,%0,#1\n"\
												 "subne %0,%0,#1\n"\
												 "addcc %0,%0,#1\n"\
												: "=r"(r0), "=r"(r1), "=r"(r2));\
												tmp = 31U - tmp;\
											}

/* Indicate if code is called from a Category 1 ISR. On the ARM7TDMI, Cat1 == FIQ and Cat2 == IRQ,
 * so simply test if we are in FIQ mode (i.e. bits 4:0 of CPSR are 10001).
 */
#define IN_CAT1_ISR()					((CPSR & OS_ARM7_FIQ) == OS_ARM7_FIQ)					

/* There is no specific reset instruction on the ARM7TDMI so spin an empty loop with FIQ and IRQ disabled
 * and wait for the watchdog to trigger (if possible)
 */
#define SYSTEM_RESET()					{OS_SET_IPL_MAX(); for(;;);}

/* Trace hooks. For this target API tracing remains unimplemented. In a future update, place codes into a ring-buffer
 * and push that ring buffer out to a trace tool. Also can enable or disable tracing based on per-object
 * criteria and on extended or standard status.
 */
#define TRACE_CODE(x)					/* Nothing */
#define TRACE_HANDLE(h)					/* Nothing */
#define TRACE_TICK(t)					/* Nothing */
#define TRACE_UINT16(i)					/* Nothing */
#define TRACE_STATUS(e)					/* Nothing */
#define TRACE_MASK(m)					/* Nothing */
#define TRACE_REF(r)					/* Nothing */
#define TRACE_DONE()					/* Nothing */

/* Kernel tracing hooks. There are compiled in this target to write to the SPI buffer and trace
 * significant events in the kernel. Used for calibrating kernel overheads and also determining
 * task execution times, resource holding times and interrupt lockout times.
 */

/* The KERNELTRACE() macro is required to execute atomically: it can occur anywhere, and run at
 * any priority, and thus is open to preemption. Typically will need KERNELTRACE() to be mapped
 * to inline assembly language to ensure atomicity.
 * 
 * @TODO Need to choose a particular memory location for tracing. E.g. I/O port, or a debugger port.
 * For now a placeholder.
 */
extern volatile uint32 NEAR(os_kerneltraceword);

#ifdef KERNELTRACE
#undef KERNELTRACE
#define KERNELTRACE(c)													{os_kerneltraceword = (uint32)(c);}
#else
#define KERNELTRACE(c)													/* Leave out kernel tracing from Scalios */
#endif

/* On this platform the kernel trace codes are:
 * 
 * CODE					MEANING
 * ============================
 * 0x00000000			Return from exception at end of interrupt
 * ISR handle			(1st) Soon after start of interrupt for ISR; (2nd) just before call to ISR handle function; (3rd) just after ISR handle function returns.
 * Basic Task handle	(1st) Just before call to Task entry function; (2nd) just after Task entry function returns.
 * Ext. Task handle		(1st) Just before resumption of extended task code; (2nd) just after Task entry function code suspends/terminates/blocks.
 * Resource handle		(1st) Just before kernel locked on 'get' call; (2nd) just after kernel unlocked on 'release' call
 * 
 * Other codes are as given below.
 */

#define OS_KERNEL_TRACE_DISPATCH_POSTHOOK_START()						{KERNELTRACE(0x00000001U);}
#define OS_KERNEL_TRACE_DISPATCH_POSTHOOK_FINISH()						{KERNELTRACE(0x00000002U);}
#define OS_KERNEL_TRACE_DISPATCH_PREHOOK_START()						{KERNELTRACE(0x00000003U);}
#define OS_KERNEL_TRACE_DISPATCH_PREHOOK_FINISH()						{KERNELTRACE(0x00000004U);}

#define OS_KERNEL_TRACE_SHUTDOWN_HOOK_START()							{KERNELTRACE(0x00000005U);}
#define OS_KERNEL_TRACE_SHUTDOWN_HOOK_FINISH()							{KERNELTRACE(0x00000006U);}
#define OS_KERNEL_TRACE_ERROR_HOOK_START()								{KERNELTRACE(0x00000007U);}
#define OS_KERNEL_TRACE_ERROR_HOOK_FINISH()								{KERNELTRACE(0x00000008U);}

#define OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_TEST()						{KERNELTRACE(0x00000009U);}
#define OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_DONE()						{KERNELTRACE(0x0000000aU);}

#define OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_RESUME(t)				{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_FINISH(t)				{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_START(t)					{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_FINISH(t)					{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_DISPATCH_ISR_START(t)							{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_ISR_FINISH(t)							{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_GET_RESOURCE(r)									{KERNELTRACE(r);}
#define OS_KERNEL_TRACE_RELEASE_RESOURCE(r)								{KERNELTRACE(r);}

#define OS_KERNEL_TRACE_TASK_SWITCH_TEST()								{KERNELTRACE(0x0000000bU);}
#define OS_KERNEL_TRACE_TASK_SWITCH_DONE()								{KERNELTRACE(0x0000000cU);}

#define OS_KERNEL_TRACE_SUSPEND_OS_INTERRUPTS()							{KERNELTRACE(0x0000000dU);}
#define OS_KERNEL_TRACE_RESUME_OS_INTERRUPTS()							{KERNELTRACE(0x0000000eU);}
#define OS_KERNEL_TRACE_SUSPEND_ALL_INTERRUPTS()						{KERNELTRACE(0x0000000fU);}
#define OS_KERNEL_TRACE_RESUME_ALL_INTERRUPTS()							{KERNELTRACE(0x00000010U);}
#define OS_KERNEL_TRACE_DISABLE_ALL_INTERRUPTS()						{KERNELTRACE(0x00000011U);}
#define OS_KERNEL_TRACE_ENABLE_ALL_INTERRUPTS()							{KERNELTRACE(0x00000012U);}

/* Also there are hooks in the First Level Interrupt Handlers to mark that an interrupt
 * has occurred, or is about to return from the interrupt. This is necessary to calculate
 * execution times for tasks etc. These markers are target-specific and in interrupt-specific
 * code.
 */
#endif /* _ICOMPILER_H_ */
