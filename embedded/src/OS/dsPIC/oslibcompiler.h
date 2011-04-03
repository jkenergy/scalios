/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-11 06:05:19 +0000 (Mon, 11 Feb 2008) $
 * $LastChangedRevision: 609 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/oslibcompiler.h $
 * 
 * Target CPU: 		dsPIC
 * Target compiler:	Microchip C30
 * Visibility:		User
 * 
 * This file defines the compiler-specific OS API datatypes and calls.
 *
 */
 
#ifndef OSLIBCOMPILER_H_
#define OSLIBCOMPILER_H_

#ifdef __C30__

/* Control interrupt priority of CPU */
#define OS_SET_IPL(x)						asm("mov.b wreg,_SRbits" : : "a"(x) : "cc" )			/* Set the low byte of the SR which stores the IPL bits */
#define OS_SET_IPL_FROM_PRI(x)				asm("mov.b wreg,_SRbits" : : "a"(x) : "cc" )			/* Set the low byte of the SR which stores the IPL bits */
#define OS_SET_IPL_KERNEL()					asm("mov.b wreg,_SRbits" : : "a"(os_kernelipl) : "cc" )	/* Set IPL to kernel level, locking out Category 2 interrupts */
#define OS_SET_IPL_MAX()					OS_SET_IPL(0xe0)										/* Set IPL to max level, locking out all maskable interrupts */

/*
 * Get the IPL of the target by reading the ls-byte of the status register (SRbits).
 * "=a" to claim usage of WREG for output, i.e. get the SR ls-byte into W0
 */
#define OS_SAVE_IPL(tmp)					asm("mov.b _SRbits, wreg" : "=a"(tmp))					/* Side-effect: assigns back to tmp */
#define OS_SET_IPL_0()						asm("clr.b _SRbits") 									/* Clear the low byte of the SR which stores the IPL bits */

/* Used for timer device drivers */
/* @todo consider prefixing these with OS_ to protect namespace */

#define BCLR_IFS0(bit)		asm("bclr 0x0084, #%0" : : "i"(bit))
#define BCLR_IFS1(bit)		asm("bclr 0x0086, #%0" : : "i"(bit))
#define BCLR_IFS2(bit)		asm("bclr 0x0088, #%0" : : "i"(bit))
#define BSET_IFS0(bit)		asm("bset 0x0084, #%0" : : "i"(bit))
#define BSET_IFS1(bit)		asm("bset 0x0086, #%0" : : "i"(bit))
#define BSET_IFS2(bit)		asm("bset 0x0088, #%0" : : "i"(bit))

#ifdef __PIC24__ /* And dsPIC33 series */
#define BCLR_IEC0(bit)		asm("bclr 0x0094, #%0" : : "i"(bit))
#define BCLR_IEC1(bit)		asm("bclr 0x0096, #%0" : : "i"(bit))
#define BCLR_IEC2(bit)		asm("bclr 0x0098, #%0" : : "i"(bit))
#define BSET_IEC0(bit)		asm("bset 0x0094, #%0" : : "i"(bit))
#define BSET_IEC1(bit)		asm("bset 0x0096, #%0" : : "i"(bit))
#define BSET_IEC2(bit)		asm("bset 0x0098, #%0" : : "i"(bit))
#define IPC0				(*(volatile uint16 *)(0x00a4u))
#define IPC4				(*(volatile uint16 *)(0x00acu))
#else /* dsPIC30 series */
#define BCLR_IEC0(bit)		asm("bclr 0x008c, #%0" : : "i"(bit))
#define BCLR_IEC1(bit)		asm("bclr 0x008e, #%0" : : "i"(bit))
#define BCLR_IEC2(bit)		asm("bclr 0x0090, #%0" : : "i"(bit))
#define BSET_IEC0(bit)		asm("bset 0x008c, #%0" : : "i"(bit))
#define BSET_IEC1(bit)		asm("bset 0x008e, #%0" : : "i"(bit))
#define BSET_IEC2(bit)		asm("bset 0x0090, #%0" : : "i"(bit))
#define IPC0				(*(volatile uint16 *)(0x0094u))
#define IPC4				(*(volatile uint16 *)(0x009cu))
#endif /* __PIC24__ */




#define NEAR(x)				x __attribute__((near))
#define FAR(x)				x __attribute__((far))

typedef unsigned char uint8;
typedef unsigned int uint16;
typedef unsigned long uint32;
typedef signed long int32;
typedef int int16;
typedef uint16 unat;			/* Natural unsigned word size for the machine */
typedef int16  nat;				/* Natural signed word size for the machine */

typedef void (*os_entryf) (void);
typedef void (*os_callbackf) (void);

typedef uint16 os_block_size;	/* Size indication for block copy */
typedef uint8 os_block_type;

extern inline void OS_BLOCK_COPY(os_block_size count, os_block_type *source, os_block_type *dest)
{
	assert(count > 0 && count < 0x4000U);	/* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	asm volatile ("dec %0,%0\n\trepeat %0\n\tmov.b [%1++],[%2++]" : "+r"(count), "+r"(source), "+r"(dest) : );	
}

extern inline void OS_BLOCK_ZERO(os_block_size count, os_block_type *dest)
{
	assert(count > 0 && count <= 0x4000U); /* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	asm volatile ("dec %0,%0\n\trepeat %0\n\tclr.b [%1++]" : "+r"(count), "+r"(dest) : );	
}

#else	/* __C30__ */

/* @todo this is largely out-of-date and needs to be brought into line with above */

#error "Running C30 compiler"

#define OS_SET_IPL(x)					((x)--, assert(0))											/* @TODO task1028 Something to suppress warning about statement without effect */
#define OS_SET_IPL_FROM_PRI(x)			((x)--, assert(0))											/* @TODO task1028 Something to suppress warning about statement without effect */
#define OS_SET_IPL_KERNEL()				{assert(0);}
#define OS_SET_IPL_MAX()				{assert(0);}
#define OS_SAVE_IPL(tmp)				((tmp)--, assert(0))										/* @TODO task1028 Something to suppress warning about statement without effect */
#define OS_SET_IPL_0()					{assert(0);}

/* Standard gcc 32-bit host installation */
#define NEAR(x)				x
#define FAR(x)				x

typedef unsigned char uint8;
typedef unsigned short uint16;
typedef unsigned int uint32;
typedef signed int int32;
typedef short int16;
typedef uint16 unat;
typedef int16  nat;

typedef void (*os_entryf) (void);
typedef void (*os_callbackf) (void);

void OS_BLOCK_COPY(os_block_size count, os_block_type *source, os_block_type *dest);
void OS_BLOCK_ZERO(os_block_size count, os_block_type *dest);


#endif /* __C30__ */

#endif /*OSLIBCOMPILER_H_*/
