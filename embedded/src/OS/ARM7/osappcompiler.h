/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/osappcompiler.h $
 * 
 * Target CPU:		ARM7
 * Target compiler:	arm-elf-gcc
 * Visibility:		User
 */

#ifndef _COMPILER_H_
#define _COMPILER_H_

/* Type to represent tick time with a longer range than TickType; must be signed */
typedef int64 os_longtick;

#define FASTROM(x)				const x 					/* All the same speed on ARM */

/* The top of the kernel stack */
#define OS_KS_TOP				(os_stackp)(0x40004000U)	/* Top of kernel stack; see default startup.s @todo needs a better system than this */

#endif /* _COMPILER_H_ */
