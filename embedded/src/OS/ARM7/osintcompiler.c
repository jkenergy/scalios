/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/osintcompiler.c $
 * 
 * Target CPU:		ARM7
 * Target compiler:	arm-elf-gcc
 * Visibility:		Internal
 */

#include <osint.h>

/* @todo placeholder for kernel tracing word; should be an SPI register or an I/O port */
/* Allows external hardware to trace what is happening and when in the kernel
 */
volatile uint32 NEAR(os_kerneltraceword);

#ifndef NDEBUG
/* return 1 if the current SR IPL is at kernel level */
unat os_at_kernel_ipl(void)
{
	register unat tmp;
	
	OS_SAVE_IPL(tmp);	/* side effect sets tmp */
	
	return (tmp & 0x00000008U) == os_kernelipl;
}
#endif /* NDEBUG */
