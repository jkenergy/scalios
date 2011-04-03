/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-19 01:18:52 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 702 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/oslibcompiler.h $
 * 
 * Target CPU: 		ARM7
 * Target compiler:	arm-elf-gcc
 * Visibility:		User
 * 
 * This file defines the compiler-specific OS API datatypes and calls.
 *
 */
 
#ifndef OSLIBCOMPILER_H_
#define OSLIBCOMPILER_H_

/* Pull out the FIQ and IRQ bits from the CPSR; side-effect: assigns to tmp */
#define OS_SAVE_IPL(tmp)					{asm("MRS %0, cpsr" : "=r"(tmp) ); (tmp) &= 0x0000000cU;}

/* Set the I and F bits of CPSR; should never have F bit set while I bit is 0 */
#define OS_SET_IPL(x)						{uint32 tmp; asm("MRS %0, cpsr" : "=r"(tmp) ); tmp &= 0xfffffff3U; tmp |= x; asm("MSR cpsr_c, %0" : : "r"(tmp) );}

/* No fast way to enable IRQ and FIQ */
#define OS_SET_IPL_0()						OS_SET_IPL(0)

/* Pull out the I and F bits from the priority and put them into the CPSR; on this platform we can never have an IPL with the F bit set */
#define OS_SET_IPL_FROM_PRI(x)				{assert(((x) & OS_ARM7_FIQ) == 0); OS_SET_IPL((x) & OS_ARM7_IRQ);}

/* Kernel level is hardwired to IRQ level; set IRQ=1 (bit 7 of CPSR) */
#define OS_SET_IPL_KERNEL()					OS_SET_IPL(OS_ARM7_IRQ)

/* Set both FIQ and IRQ flags to lock out both sources of interrupts
 * Disable IRQ first then FIQ to avoid spurious interrupt problem: see
 * See 7.1.1.2 (page 52) of "LPC21xx and LPC22xx User manual" for
 * details of the workaround.
 */

#define OS_SET_IPL_MAX()					{uint32 tmp; asm("mrs  %0, cpsr\n"\
															 "orr  %0, %0, #128\n"\
															 "msr  cpsr_c, %0\n"\
															 "orr  %0, %0, #64\n"\
															 "msr  cpsr_c, %0" : "=r"(tmp));}

/* NEAR and FAR not used on this platform */
#define NEAR(x)				x
#define FAR(x)				x

/* See Section 7 (ARM C and C++ Language Mappings) of "Procedure Call Standard for the ARM Architecture"
 * (part of ARM ABI definitions)
 */
typedef unsigned char uint8;
typedef unsigned short uint16;
typedef unsigned int uint32;
typedef signed int int32;
typedef short int16;
typedef uint32 unat;			/* Natural unsigned word size for the machine */
typedef int32  nat;				/* Natural signed word size for the machine */
typedef long long int64;		/* Extension to ANSI C89 */

typedef void (*os_entryf) (void);
typedef void (*os_callbackf) (void);

typedef uint32 os_block_size;	/* Size indication for block copy */
typedef uint8 os_block_type;

extern inline void OS_BLOCK_COPY(os_block_size count, os_block_type *source, os_block_type *dest)
{
	assert(count > 0 && count < 0x4000U);	/* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	/* asm volatile ("dec %0,%0\n\trepeat %0\n\tmov.b [%1++],[%2++]" : "+r"(count), "+r"(source), "+r"(dest) : );*/
}

extern inline void OS_BLOCK_ZERO(os_block_size count, os_block_type *dest)
{
	assert(count > 0 && count <= 0x4000U); /* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	/* asm volatile ("dec %0,%0\n\trepeat %0\n\tclr.b [%1++]" : "+r"(count), "+r"(dest) : );*/	
}

#endif /*OSLIBCOMPILER_H_*/
