/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-11 02:31:35 +0000 (Fri, 11 Jan 2008) $
 * $LastChangedRevision: 556 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/osintcompiler.c $
 * 
 * Target CPU:		dsPIC30F
 * Target compiler:	Generic ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

/* @todo placeholder for kernel tracing word; should be an SPI register or an I/O port */
/* Allows external hardware to trace what is happening and when in the kernel
 */
volatile uint16 NEAR(os_kerneltraceword);

#ifndef NDEBUG
/* return 1 if the current SR IPL is at kernel level */
unat os_at_kernel_ipl(void)
{
	register unat tmp;
	
	OS_SAVE_IPL(tmp);	/* side effect sets tmp */
	
	return (tmp & 0x00E0U) == os_kernelipl;
}
#endif /* NDEBUG */
