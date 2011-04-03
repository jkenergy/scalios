/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/oslibtarget.h $
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
#define E_OS_SYS_TRAP0					(32U)
#define E_OS_SYS_OSCFAILURE				(33U)
#define E_OS_SYS_ADDRERROR				(34U)
/* E_OS_STACKFAULT symbol defined for all targets and no specific trap error code defined here */
#define E_OS_SYS_MATHERROR				(36U)
#define E_OS_SYS_TRAP5					(37U)
#define E_OS_SYS_TRAP6					(38U)
#define E_OS_SYS_TRAP7					(39U)

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
typedef uint16 os_primask;		/* Bit mask priority; used for ready queue, must be at least 16 bits; bit 0 corresponds to priority 1 $Req: artf1090 $ */
typedef uint16 os_ipl;			/* Priority level on the dsPIC30F; bits matching with the SR IPL (bits 5:7); declaring as a 16-bit value rather than 8-bit makes for faster code */
typedef uint16 EventMaskType;	/* Event mask; limit of 15 events per task plus one dummy event (must support at least 8 events; $Req: artf1089 $ */
typedef uint16 TickType;		/* Support 16-bit tick range */
typedef uint16 DeviceControlCodeType;

/* Indicates in integer nanoseconds the duration of the system counter tick $Req: artf1207 $ */
/* Note: will not work on devices that do not support 32-bit integers */
extern const uint32 OSTICKDURATION;

#endif /*APITARGET_H_*/
