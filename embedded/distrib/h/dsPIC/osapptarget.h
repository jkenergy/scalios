/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-01-09 21:52:20 +0000 (Tue, 09 Jan 2007) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/osapptarget.h $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

#ifndef _TARGET_H_
#define _TARGET_H_

typedef int32 os_longtick;			/* Must be wide enough to store 2 x range(TickType) and must be signed to allow to allow comparisons to use sign to detect event ordering */

#define USE_BITFIELD_FLAGS		/* Bitfields are implemented efficiently on this target */

#endif /* _ITARGET_H_ */
