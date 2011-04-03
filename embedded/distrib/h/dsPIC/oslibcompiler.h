/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2006-12-07 22:25:47 +0000 (Thu, 07 Dec 2006) $
 * $LastChangedRevision: 299 $
 * $LastChangedBy: kentindell $
 * $HeadURL: svn://192.168.2.254/jke/rtos/trunk/embedded/src/OS/dsPIC/apicompiler.h $
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

#define NEAR(x)				x __attribute__((near))
#define FAR(x)				x __attribute__((far))

typedef unsigned char uint8;
typedef unsigned int uint16;
typedef unsigned long uint32;
typedef unsigned long int32;
typedef int int16;
typedef uint16 unat;			/* Natural unsigned word size for the machine */
typedef int16  nat;				/* Natural signed word size for the machine */

typedef void (*os_entryf) (void);
typedef void (*os_callbackf) (void);

extern inline void OS_BLOCK_COPY(uint16 count, uint8 *source, uint8 *dest)
{
	assert(count > 0 && count <= 0x4000U);	/* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	asm volatile ("dec %0,%0\n\trepeat %0\n\tmov.b [%1++],[%2++]" : "+r"(count), "+r"(source), "+r"(dest) : );	
}

extern inline void OS_BLOCK_ZERO(uint16 count, uint8 *dest)
{
	assert(count > 0 && count <= 0x4000U); /* asm repeat instruction only uses bottom 14 bits, hence max count is 16383 */
	asm volatile ("dec %0,%0\n\trepeat %0\n\tclr.b [%1++]" : "+r"(count), "+r"(dest) : );	
}

#else	/* __C30__ */

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

void OS_BLOCK_COPY(uint16 count, uint8 *source, uint8 *dest);
void OS_BLOCK_ZERO(uint16 count, uint8 *dest);


#endif /* __C30__ */

#endif /*OSLIBCOMPILER_H_*/
