/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-29 02:11:24 +0000 (Sat, 29 Mar 2008) $
 * $LastChangedRevision: 698 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/osintkernel.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#ifndef IKERNEL_H_
#define IKERNEL_H_

#include <fsetjmp.h>

/* All Basic Tasks (BTs) execute on one single kernel stack.
 * All Extended Tasks (ETs) execute on their own individual stacks.
 * 
 * See docs/stack.pdf for a diagram of how this works.
 */
unat os_switch2ext(os_stackp *save, os_stackp restoresp, unat restore, os_stackp initsp);		/* Switch to extended task stack; WRITTEN IN ASSEMBLY LANGUAGE */
void os_jump2ks(unat reason, os_stackp to);						/* Discard registers and jump to the kernel stack; WRITTEN IN ASSEMBLY LANGUAGE */
void os_save2ks(unat reason, os_stackp *from, os_stackp to);			/* Save resigers and switch to the kernel stack; WRITTEN IN ASSEMBLY LANGUAGE */
void os_panic_shutdown(StatusType error);						/* Switch to kernel stack then call shutdown(error) */

void os_idle(void);

extern os_stackp NEAR(os_kssp);									/* Kernel saved stack pointer; 0 when running on the kernel, where the kernel stack has got to otherwise */
extern TaskType NEAR(os_chaintask);								/* global var storing task handle used during chaintask */

#ifdef STACK_CHECKING
extern nat FASTROM(os_pretask_hook_offset);					/* Offset applied to the stack pointer (in bytes); includes stack usage, kernel overhead prior to hook entry, and stack limit offsets */
extern nat FASTROM(os_posttask_hook_offset);					/* Offset applied to the stack pointer (in bytes); includes stack usage, kernel overhead prior to hook entry, and stack limit offsets */

extern os_stackp NEAR(os_curtos);									/* Current top of stack for task or ISR handler */

/* Offset os_curtos macros; map to same target supplied offset macro */
#define OFFSET_CURTOS_TASK(x)	OFFSET_CURTOS(x)
#define OFFSET_CURTOS_ISR(x)	OFFSET_CURTOS(x)
#define OFFSET_CURTOS_HOOK(x)	OFFSET_CURTOS(x)
#endif

extern os_stackp NEAR(os_SPoffstore);								/* Stored stack pointer for calibration tests */
extern os_stackp NEAR(os_SPonstore);								/* Stored stack pointer for calibration tests */
extern os_stackp NEAR(os_SPsave);									/* Saved copy of stack pointer; used by os_jump2ks for calibration tests */

#define ON_KERNEL_STACK()		(os_kssp == 0)

/* Reason values for why an extended task stopped running */
#define RUNDISPATCH				(0)
#define STOP					(1U)

/* Define the Priority of the os_idle task; always lower than all other tasks */
#define IDLEPRI					(0)

/* Detailed description of stack switching functions.
 * --------------------------------------------------
 * 
 * os_switch2ext
 * ----------
 * 
 * The function os_switch2ext() Switches to an extended task stack, saving the old context on the kernel stack,
 * putting the stack pointer to where 'save' indicates, sets the stack pointer to the 'to' value and then jumps
 * (not calls!) the specified void/void function 'f'. The function to run ('f') is either a "restore context"
 * function, or else it is a "create context" function. The stack pointer value for 'to' is either the saved ET
 * stack pointer (in the case of restore context) or the initial stack pointer (in the case of create context).
 *
 * The reason for this somewhat complex parameter passing (rather than passing a "do restore" or "do create" flag)
 * is that the function is target-specific and coded in assembly language. It is very tricky to write assembly
 * code that can de-reference pointers to access structures (such as the task control block): the code has to be
 * kept compatible with the compiler and with the structure definition. This would be a maintenance headache and
 * also a source of tricky-to-find bugs.
 *
 * This function returns only when the ET should stop running. A return value of RUNDISPATCH indicates that the ET
 * is being preempted by a higher priority task. A return value of STOP indicates that the ET has voluntarily
 * stopped (either terminating or blocking).
 *
 * This function is called only from the "stub" function running on the kernel stack.
 *
 * Note that the 'save' parameter is only ever &os_kssp so we don't really need to pass this as a parameter: could
 * hardwire it into the assembly language. But this way around is no slower and makes the assembly language
 * much easier to write (no external references because the compiler puts the value in a convenient-to-use
 * register).
 *
 * The function saves the non-volatile (i.e. callee-save) C registers on the kernel stack, saves the top of stack
 * into the specified place (save always points to os_kssp), changes the stack pointer to the top of the new ET stack,
 * recovers the non-volatile registers there, and then makes a jump to the function indicated by 'f'.
 * 
 * os_jump2ks
 * -------
 * 
 * Return to the kernel stack (i.e. cause the os_switch2ext() call to return). Give a reason for the return
 * (1 means blocked and 2 means terminated) which is returned to the caller of the os_switch2ext() function.
 * Set the stack pointer to 'to'.
 *
 * This function is only called when running on an ET stack. It does not save any context on the ET stack
 * (so is only called when the ET is terminating). It jumps to the kernel stack ('to'), recovers the compiler
 * non-volatile registers and returns (to the caller of the os_switch2ext() function passing on the reason code.
 * 
 * os_save2ks
 * -------
 * 
 * Save extended (non-volatile registers) context and switch back to kernel stack. This is like os_jump2ks but
 * is only called when the ET is preempted. Reason is always 0.
 */

#ifndef NDEBUG
extern unat os_kernelnesting;						/* Number of (re)entries to the kernel; used for assertion checking only
													 * to allow assertion checks in expressions; typically stops via a breakpoint
													 */
													 
unat os_at_kernel_ipl(void);						/* return 1 if the current SR IPL is at kernel level */													 
													 
#define KERNEL_LOCKED()		(os_kernelnesting > 0)
#define NOT_REACHED()		(assert(0))

#else

#define NOT_REACHED()								/* NOTREACHED */

#endif /* NDEBUG */

void os_block(void);												/* Deactivate the current (extended) task and hold its context */
unat os_slih_stackcheck(ISRType isr);									/* Second level interrupt handler (includes stack checking code) */
unat os_slih_no_stackcheck(ISRType isr);								/* Second level interrupt handler (does not include stack checking code) */
void os_tidylockedresources(void);
void os_call_error_hook(StatusType);								/* Error hook handler */


void os_shutdown(StatusType error);

#define MARK_IN_KERNEL()	{assert(os_kernelnesting++ == 0);}
#define MARK_OUT_KERNEL()	{assert(--os_kernelnesting == 0);}

#define ENTER_KERNEL()	 {ENTER_KERNEL_DIRECT();}

/* Go straight to kernel without seeing what current level is */
#define ENTER_KERNEL_DIRECT()			{MARK_IN_KERNEL();  OS_SET_IPL_KERNEL();}

/* Leave kernel; recover interrupt level; priorities can have interrupt levels (we can be leaving
 * the kernel to go back to an ISR, or a task with cat 2 interrupts locked out (via a resource
 * lock)
 *
 * $Req: artf1101 $
 */
#define LEAVE_KERNEL()		{MARK_OUT_KERNEL(); OS_SET_IPL_FROM_PRI(os_curpri);}	/* $Req: artf1049 $ */

/* If any of the counters is non-zero then are running with interrupts locked by one or more of DisableAllInterrupts(),
 * SuspendAllInterrupts() or SuspendOSInterrupts(). Calls to API calls are not legal when true. $Req: artf1045 $
 */
#define INTERRUPTS_LOCKED()			(os_sus_os_cnt | os_sus_all_cnt | os_dis_all_cnt)
#define CLEAR_INTERRUPTS()			(os_sus_os_cnt = os_sus_all_cnt = os_dis_all_cnt = 0)


/* Current context values common for both tasks and Category 2 ISRs */
#ifdef STACK_CHECKING
extern os_stackp NEAR(os_curtos);										/* Top of stack */
#endif

extern JMP_BUF * NEAR(os_curenv);										/* The set_jmp buffer to return to the kernel */
extern os_pri NEAR(os_curpri);											/* Current priority of currently running task or ISR */
#ifdef OS_EXTENDED_STATUS
extern ResourceType NEAR(os_curlastlocked);								/* Currently last locked resource (and still held) by task or ISR; 0 if none */
#endif

extern JMP_BUF NEAR(os_startosenv);										/* The set_jmp buffer to return to StartOS */
extern TaskType NEAR(os_nexttask);										/* Highest priority runnable task - 0 means "don't know - find it out from 'ready' */
extern os_ipl FASTROM(os_kernelipl);									/* IPL of the kernel level (i.e. IPL of highest priority cat 2 ISR) */
extern os_pri FASTROM(os_kernelpri);									/* Priority of the kernel (i.e. priority of highest priority cat 2 ISR) */

#define IN_CAT2_ISR()				(os_curisr)

#define TASK_SWITCH_PENDING(p)		(assert(os_nexttask), (assert(VALID_TASK(os_nexttask) || (os_nexttask == OS_IDLE_TASK))), os_nexttask->basepri > (p))

/*
 * Do a dispatch if a task switch is now needed (i.e. os_nexttask cache has a base priority higher than the current task)
 */
#define DISPATCH_IF_TASK_SWITCH_PENDING()	{											\
												OS_KERNEL_TRACE_TASK_SWITCH_TEST();		\
												if (TASK_SWITCH_PENDING(os_curpri)) {	\
													DISABLE_STACKCHECK();				\
													os_swst_dispatch();					\
													ENABLE_STACKCHECK();				\
												}										\
												OS_KERNEL_TRACE_TASK_SWITCH_DONE();		\
											}

/* Priority Queue Management
 * -------------------------
 * 
 * See docs/priqueue.pdf and docs/os_taskcb.pdf for a diagrams on how the FIFO queues are managed.
 * 
 * os_priqueuestatus is a word (typically 16-bit) that has a bit set if the corresponding priority
 * queue is non-empty.
 */
extern os_primask NEAR(os_priqueuestatus);						/* Bit mask for priority queues; a '1' indicates correponding queue is not empty. Bit 0 corresponds to a priority of 1 (0 is reserved for idle task) */

#endif /*IKERNEL_H_*/
