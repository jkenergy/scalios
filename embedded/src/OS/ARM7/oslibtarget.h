/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-19 01:18:52 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 702 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/oslibtarget.h $
 *
 * $CodeReview: kentindell, 2006-10-16 $
 * 
 * Target CPU: 		ARM7
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 * 
 * This file defines the target-specific OS API datatypes and calls.
 */

#ifndef APITARGET_H_
#define APITARGET_H_

/* Additional non-OSEK error codes */

/* Trap error codes. Must be unique number codes because testing framework includes
 * a function to obtain the error string for a given error code and all cases must be
 * unique.
 * 
 * Must be kept consistent with numbers in panic.s.
 */

/* $Req: artf1124 $ */
#define E_OS_SYS_UNDEFINED_INSTR		(40U)
#define E_OS_SYS_SOFTWARE_INT			(41U)
#define E_OS_SYS_PREFETCH_ABORT			(42U)
#define E_OS_SYS_DATA_ABORT				(43U)
#define E_OS_SYS_RESERVED				(44U)

typedef uint32 os_stackword;	/* Stack alignment is 32 bits for this target */
typedef uint32 os_pri;			/* Priorities are stored as integers. Bits 4:0 (i.e. enough bits to code for sizeof(os_primask))
								 * are "software" priorities and correspond to a machine IPL of 0.
								 * 
								 * Bit 7 encodes the IPL for the priority (0 = IRQ enabled, 1 = IRQ disabled)
								 * 
								 * Bits 31:7 and 6:5 are always zero.
								 */
typedef uint32 os_primask;		/* Bit mask priority; used for ready queue, must be at least 16 bits; bit 0 corresponds to priority 1 $Req: artf1090 $ */
typedef uint32 os_ipl;			/* Priority level on the ARM7; single bit set to correspond with CPSR IRQ (bit 7); declaring as a 32-bit value rather than 16 or 8 bits makes for faster code */
typedef uint32 EventMaskType;	/* Event mask; limit of 15 events per task plus one dummy event (must support at least 8 events; $Req: artf1089 $ */
typedef uint32 TickType;		/* Support 32-bit tick range, which maps on to 32-bit timer/counters on LPC21xx series */
typedef uint32 DeviceControlCodeType;

/* Indicates in integer nanoseconds the duration of the system counter tick $Req: artf1207 $ */
/* Note: will not work on devices that do not support 32-bit integers */
extern const uint32 OSTICKDURATION;

/* Defined here to give visibility to in-line assembler macros */
#define OS_ARM7_FIQ							(0x00000040U)
#define OS_ARM7_IRQ							(0x00000080U)

#endif /*APITARGET_H_*/
