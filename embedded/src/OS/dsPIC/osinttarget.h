/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/osinttarget.h $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	Generic ANSI C
 * Visibility:		Internal
 */

#ifndef _ITARGET_H_
#define _ITARGET_H_

#define DUMMY_EVENT (0x8000U)		/* Set to be the top event bit so that we can tell between suspended and waiting for no events */

#define IPL_HIGHER_THAN_KERNEL_IPL(ipl)		(ipl > os_kernenipl)			/* Returns 1 if ipl1 is higher than ipl2 on this target */

/* Does the stacked status register containing the IPL match the IPL implied by the current priority? */
#define SSRMATCHPRI(ssr, pri)		((ssr >> 13) == (pri >> 5))		/* Stacked SR (bits 13:15 are IPL) and os_pri (bits 5:7 are IPL) */

#define PRI2IPL(p)					((p) & 0x00E0)					/* Bits 5:7 of os_pri maps directly to IPL in SR */

#define OS_ASCENDING_STACK

#define VICVectAddr    (*((volatile unsigned long *) 0xFFFFF030))

#define ISR_PROLOG()				/* Nothing on this platform */
#define ISR_EPILOG()				/* Nothing on this platform */

#ifdef STACK_CHECKING
/* Offset the current top of stack by the specified number of bytes
 * System stack is ascending on this target, so add the offset amount to the current SP
 * 
 * Need also to copy the stack pointer to a global variable so that the correct offset can be calculated
 * in a calibration test program.
 */

#define OFFSET_CURTOS(x)			(GETSP(os_curtos); os_curtos += (x)))
#endif

#endif /* _ITARGET_H_ */
