/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-19 01:18:52 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 702 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/osinttarget.h $
 * 
 * Target CPU:		ARM7
 * Target compiler:	Generic ANSI C
 * Visibility:		Internal
 */

#ifndef _ITARGET_H_
#define _ITARGET_H_

#define DUMMY_EVENT (0x80000000U)									/* Set to be the top event bit so that we can tell between suspended and waiting for no events */

#define IPL_HIGHER_THAN_KERNEL_IPL(ipl)		(ipl & OS_ARM7_FIQ)		/* Only true if the F (FIQ) bit is set in CPSR, since kernel level is always at IRQ level */

#define OS_DESCENDING_STACK

#ifdef STACK_CHECKING
/* Offset the current top of stack by the specified number of bytes
 * System stack is ascending on this target, so add the offset amount to the current SP
 * 
 * Need also to copy the stack pointer to a global variable so that the correct offset can be calculated
 * in a calibration test program.
 */

#define OFFSET_CURTOS(x)			{uint8 *tmp; GETSP(tmp); os_curtos = (os_stackp)(tmp - (x));}
#endif

#define ISR_PROLOG()				/* Nothing on this platform */
#define ISR_EPILOG()				(VICVectAddr = 0)

#endif /* _ITARGET_H_ */
