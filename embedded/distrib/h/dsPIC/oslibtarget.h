/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2006-12-07 22:25:47 +0000 (Thu, 07 Dec 2006) $
 * $LastChangedRevision: 299 $
 * $LastChangedBy: kentindell $
 * $HeadURL: svn://192.168.2.254/jke/rtos/trunk/embedded/src/OS/dsPIC/apitarget.h $
 *
 * $CodeReview: kentindell, 2006-10-16 $
 * 
 * Target CPU: 		dsPIC
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 * 
 * This file defines the target-specific OS API datatypes and calls.
 */

#ifndef APITARGET_H_
#define APITARGET_H_

/* Additional non-OSEK error codes */

/* Trap error codes */
/* $Req: artf1124 $ */
#define E_OS_SYS_TRAP0					(9U)
#define E_OS_SYS_OSCFAILURE				(10U)
#define E_OS_SYS_ADDRERROR				(11U)
#define E_OS_STACKFAULT					(12U)		/* $Req: artf1040 $ */
#define E_OS_SYS_MATHERROR				(13U)
#define E_OS_SYS_TRAP5					(14U)
#define E_OS_SYS_TRAP6					(15U)
#define E_OS_SYS_TRAP7					(16U)

typedef uint16 os_stackword;	/* Stack alignment is 16 bits for this target */
typedef uint16 os_pri;			/* Priorities are stored as integers. The bottom 4 bits (i.e. enough bits to code for sizeof(os_primask))
								 * are "software" priorities and correspond to a machine IPL of 0.
								 * 
								 * Bits 5:7 encode the IPL for the priority. Thus a priority of:
								 * 
								 * 00000000.01100000 is a priority that is higher than any software
								 * priority and for which the corresponding IPL is 3.
								 * 
								 * Bits 8:15 are always zero.
								 */
typedef uint16 os_primask;		/* Bit mask priority; used for ready queue, must be at least 16 bits $Req: artf1090 $ */
typedef uint16 os_ipl;			/* Priority level on the dsPIC30F; bits matching with the SR IPL (bits 5:7); declaring as a 16-bit value rather than 8-bit makes for faster code */
typedef uint16 EventMaskType;	/* Event mask; limit of 15 events per task plus one dummy event (must support at least 8 events; $Req: artf1089 $ */
typedef uint16 TickType;		/* Support 16-bit tick range */

/* Indicates in integer nanoseconds the duration of the system counter tick $Req: artf1207 $ */
/* Note: will not work on devices that do not support 32-bit integers */
extern uint32 OSTICKDURATION;

#endif /*APITARGET_H_*/
