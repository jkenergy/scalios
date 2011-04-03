/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-29 02:11:24 +0000 (Sat, 29 Mar 2008) $
 * $LastChangedRevision: 698 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/osintcompiler.h $
 *  
 * Target CPU:		dsPIC
 * Target compiler:	MPLAB C30
 * Visibility:		Internal
 */
#ifndef _I_COMPILER_H_
#define _I_COMPILER_H_


/* Pull in standard Microchip registers, else define dummy registers if using standard C */
#ifdef __C30__
#define SPLIM				(*(volatile uint16 *)(0x0020u))
#define WREG15				(*(volatile uint16 *)(0x001eu))
#define SR					(*(volatile uint16 *)(0x0042u))

/* If the standard C30 setjmp() is to be used, define USE_STANDARD_SETJMP */
typedef uint16 os_fjmp_buf_word;
#define OS_FJMP_BUF_SIZE					(10U)
/* 16-bit registers to save in the buffer:
 * 	 w8
 * 	 w9
 * 	 w10
 * 	 w11
 * 	 w12
 * 	 w13
 * 	 w14
 * 	 sp  			; stack pointer prior to setjmp call
 *   pc - low word  ; program counter on return from setjmp call
 *   pc - high word ;
 */

#else

#define USE_STANDARD_SETJMP

extern uint16 SR;
extern uint16 WREG15;
extern uint16 SPLIM;

#endif

#ifdef STACK_CHECKING

/* Use the built-in hardware for stack overflow checking */
/* SPLIM points to the last usable word of the stack, hence so does curtos */

#ifdef __C30__
#define INIT_STACKCHECK_TO_OFF()		{asm("setm SPLIM\nnop");}

#define DISABLE_STACKCHECK()			{assert(STACKCHECK_ON()); asm("setm SPLIM\nmov w15,_os_SPoffstore");} /* second instruction must guarantee no access to [w15] after setting SPLIM */
#define ENABLE_STACKCHECK()				{assert(STACKCHECK_OFF()); asm("mov %0, SPLIM\nmov w15,_os_SPonstore" : : "r"(os_curtos));}
#define STACKCHECK_ON()					(SPLIM != 0xFFFEU)
#define STACKCHECK_OFF()				(!STACKCHECK_ON())

#else /* standard C */

#define INIT_STACKCHECK_TO_OFF()		{}

#define DISABLE_STACKCHECK()			{} /* second instruction must guarantee no access to [w15] after setting SPLIM */
#define ENABLE_STACKCHECK()				{}
#define STACKCHECK_ON()					(1U)
#define STACKCHECK_OFF()				(0)

#endif

#else /* Not using stack checking at all */

#define INIT_STACKCHECK_TO_OFF()

#ifdef __C30__
#define DISABLE_STACKCHECK()			asm("mov w15,_os_SPoffstore")			/* Instruction for stack usage calibration of kernel overheads */			
#else
#define DISABLE_STACKCHECK()			{}			
#endif

#define ENABLE_STACKCHECK()				
/* When no stack checking is included these functions (used by assertion checking) always give "true" */
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


#define GETSP(tmp)							((tmp) = (os_stackp)(WREG15))

typedef void (*os_hookf) (void);

/*
 * Set the IPL of the target by assigning to the ls-byte of the status register (SRbits).
 * 
 * "a" to claim usage of WREG, i.e. put the operand into W0
 * "cc" indicates that the condition flags are clobbered
 * 
 */
#ifdef __C30__
#define FSB(tmp)						{assert(tmp); asm("ff1l %0, %0" : "+r"(tmp)); tmp = 16U - tmp;}		/* Side-effect: assigns back to tmp; FSB() applied only to non-empty priority queue mask */

#define IN_CAT1_ISR()					((os_curpri & 0x00E0U) != (SR & 0x00E0U))					/* If the running IPL is different to the IPL implied by curpri
																								 * then a Category 1 ISR must have occurred (or else the user directly
																						 		 * altered IPL, which is illegal).
																								 */


#define SYSTEM_RESET()					asm("RESET")											/* Do a software reset */

#else /* standard C */

#define FSB(tmp)						((tmp)--, assert(0))										/* @TODO task1028 Something to suppress warning about statement without effect */
#define IN_CAT1_ISR()					(assert(0))
#define SYSTEM_RESET()					{assert(0);}

#endif /* __C30__ */

//extern volatile unsigned int dummy16;

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
extern volatile uint16 NEAR(os_kerneltraceword);

#ifdef KERNELTRACE
#undef KERNELTRACE
#define KERNELTRACE(c)													{os_kerneltraceword = (uint16)(c);}
#else
#define KERNELTRACE(c)													/* Leave out kernel tracing from Scalios */
#endif

/* On this platform the kernel trace codes are:
 * 
 * CODE					MEANING
 * ============================
 * 0x0000				RETFIE at end of interrupt
 * ISR handle			(1st) Soon after start of interrupt for ISR; (2nd) just before call to ISR handle function; (3rd) just after ISR handle function returns.
 * Basic Task handle	(1st) Just before call to Task entry function; (2nd) just after Task entry function returns.
 * Ext. Task handle		(1st) Just before resumption of extended task code; (2nd) just after Task entry function code suspends/terminates/blocks.
 * Resource handle		(1st) Just before kernel locked on 'get' call; (2nd) just after kernel unlocked on 'release' call
 * 
 * Other codes are as given below.
 */

#define OS_KERNEL_TRACE_DISPATCH_POSTHOOK_START()						{KERNELTRACE(0x0001U);}
#define OS_KERNEL_TRACE_DISPATCH_POSTHOOK_FINISH()						{KERNELTRACE(0x0002U);}
#define OS_KERNEL_TRACE_DISPATCH_PREHOOK_START()						{KERNELTRACE(0x0003U);}
#define OS_KERNEL_TRACE_DISPATCH_PREHOOK_FINISH()						{KERNELTRACE(0x0004U);}

#define OS_KERNEL_TRACE_SHUTDOWN_HOOK_START()							{KERNELTRACE(0x0005U);}
#define OS_KERNEL_TRACE_SHUTDOWN_HOOK_FINISH()							{KERNELTRACE(0x0006U);}
#define OS_KERNEL_TRACE_ERROR_HOOK_START()								{KERNELTRACE(0x0007U);}
#define OS_KERNEL_TRACE_ERROR_HOOK_FINISH()								{KERNELTRACE(0x0008U);}

#define OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_TEST()						{KERNELTRACE(0x0009U);}
#define OS_KERNEL_TRACE_SCHEDULE_TASK_SWITCH_DONE()						{KERNELTRACE(0x000AU);}

#define OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_RESUME(t)				{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_EXTENDED_TASK_FINISH(t)				{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_START(t)					{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_BASIC_TASK_FINISH(t)					{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_DISPATCH_ISR_START(t)							{KERNELTRACE(t);}
#define OS_KERNEL_TRACE_DISPATCH_ISR_FINISH(t)							{KERNELTRACE(t);}

#define OS_KERNEL_TRACE_GET_RESOURCE(r)									{KERNELTRACE(r);}
#define OS_KERNEL_TRACE_RELEASE_RESOURCE(r)								{KERNELTRACE(r);}

#define OS_KERNEL_TRACE_TASK_SWITCH_TEST()								{KERNELTRACE(0x000BU);}
#define OS_KERNEL_TRACE_TASK_SWITCH_DONE()								{KERNELTRACE(0x000CU);}

#define OS_KERNEL_TRACE_SUSPEND_OS_INTERRUPTS()							{KERNELTRACE(0x000DU);}
#define OS_KERNEL_TRACE_RESUME_OS_INTERRUPTS()							{KERNELTRACE(0x000EU);}
#define OS_KERNEL_TRACE_SUSPEND_ALL_INTERRUPTS()						{KERNELTRACE(0x000FU);}
#define OS_KERNEL_TRACE_RESUME_ALL_INTERRUPTS()							{KERNELTRACE(0x0010U);}
#define OS_KERNEL_TRACE_DISABLE_ALL_INTERRUPTS()						{KERNELTRACE(0x0011U);}
#define OS_KERNEL_TRACE_ENABLE_ALL_INTERRUPTS()							{KERNELTRACE(0x0012U);}

/* Also there are hooks in the First Level Interrupt Handlers to mark that an interrupt
 * has occurred, or is about to return from the interrupt. This is necessary to calculate
 * execution times for tasks etc. These markers are target-specific and in interrupt-specific
 * code.
 */
#endif /* _ICOMPILER_H_ */
