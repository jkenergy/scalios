/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-01-12 13:50:06 +0000 (Fri, 12 Jan 2007) $
 * $LastChangedRevision: 333 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/idle.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal 
 * 
 */

#include <osint.h>

void os_idle(void)
{
	/* Default implementation of the os_idle task */
	
	assert(!KERNEL_LOCKED());
	assert(STACKCHECK_ON());
	assert(ON_KERNEL_STACK());
		
	
	/* @todo add os_idle task processing here */
}
