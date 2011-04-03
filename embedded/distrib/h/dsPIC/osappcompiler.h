/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-04-14 01:20:49 +0100 (Sat, 14 Apr 2007) $
 * $LastChangedRevision: 415 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/osappcompiler.h $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	MPLAB C30
 * Visibility:		User
 */

#ifndef _COMPILER_H_
#define _COMPILER_H_

#define FASTROM(x)				NEAR(x)				/* NEAR(x); If RAM overheads are too high then change to const x */

extern uint16  					_SPLIM_init;		/* _SPLIM_init is provided by LINK30 linker, rather than a C source instance */

#define OS_KS_TOP				(&_SPLIM_init)		/* Top of kernel stack, assumes C runtime startup has initialised SPLIM */

/* Macro that defines interrupt handler for the specified trap while allowing compiler to
 * automatically load handler address into trap vector table.
 * 
 * Handler calls: os_panic_shutdown(error);
 * 
 * This call is done in assembly since if done in C then RCOUNT and R0-R7 get pushed
 * onto the stack (which may have already overflowed).
 * 
 * All traps handled in this way cause a device RESET and thus never return; see shutdown()
 */
#define OS_TRAP_HANDLER(trap,error)	void __attribute__((__interrupt__)) _##trap(void)	\
									{													\
										/* os_panic_shutdown(error);	*/				\
										asm("mov #%0, W0" : : "g"(error));				\
										asm("goto _panic_shutdown");					\
									}


#endif /* _COMPILER_H_ */
