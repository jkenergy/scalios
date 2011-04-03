/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/target.c $
 * 
 * Target CPU:			ARM7TDMI
 * Target compiler:		arm-elf-gcc
 * Visibility:			Internal
 */

#include <osint.h>

#ifdef STACK_CHECKING
void os_enable_stackcheck(void)
{
	assert(STACKCHECK_ON());
	STACKCHECK_CLEAR();
	*os_curtos = 0xDEADBEEFU;
}

void os_disable_stackcheck(void)
{
	assert(STACKCHECK_OFF());
	STACKCHECK_CLEAR();
	if(*os_curtos != 0xDEADBEEFU) {
		os_panic_shutdown(E_OS_STACKFAULT);
	}
}
#endif

/* Set up the VIC for the configured interrupts */
void os_lpc21xx_init_vic(void)
{
	/* Note that function will finish but leave VICIntEnable unchanged: each device driver should
	 * modify this at the appropriate time.
	 */
	
	/* Indicate the default Cat 2 (i.e. IRQ) ISR */
	VICDefVectAddr = (uint32)(os_vic_initcb.VICDefVectAddr_init);		/* Priority 0 */
	
	/* Indicate the Cat 2 ISR handles */
	VICVectAddr0 = (uint32)(os_vic_initcb.VICVectAddr0_init); 			/* Priority 16 */
	VICVectAddr1 = (uint32)(os_vic_initcb.VICVectAddr1_init); 			/* Priority 15 */
	VICVectAddr2 = (uint32)(os_vic_initcb.VICVectAddr2_init); 			/* Priority 14 */
	VICVectAddr3 = (uint32)(os_vic_initcb.VICVectAddr3_init); 			/* Priority 13 */
	VICVectAddr4 = (uint32)(os_vic_initcb.VICVectAddr4_init); 			/* Priority 12 */
	VICVectAddr5 = (uint32)(os_vic_initcb.VICVectAddr5_init); 			/* Priority 11 */
	VICVectAddr6 = (uint32)(os_vic_initcb.VICVectAddr6_init); 			/* Priority 10 */
	VICVectAddr7 = (uint32)(os_vic_initcb.VICVectAddr7_init); 			/* Priority 9 */
	VICVectAddr8 = (uint32)(os_vic_initcb.VICVectAddr8_init); 			/* Priority 8 */
	VICVectAddr9 = (uint32)(os_vic_initcb.VICVectAddr9_init); 			/* Priority 7 */
	VICVectAddr10 = (uint32)(os_vic_initcb.VICVectAddr10_init); 		/* Priority 6 */
	VICVectAddr11 = (uint32)(os_vic_initcb.VICVectAddr11_init); 		/* Priority 5 */
	VICVectAddr12 = (uint32)(os_vic_initcb.VICVectAddr12_init); 		/* Priority 4 */
	VICVectAddr13 = (uint32)(os_vic_initcb.VICVectAddr13_init); 		/* Priority 3 */
	VICVectAddr14 = (uint32)(os_vic_initcb.VICVectAddr14_init); 		/* Priority 2 */
	VICVectAddr15 = (uint32)(os_vic_initcb.VICVectAddr15_init); 		/* Priority 1 */

	/* Indicate the interrupt source for each Cat 2 ISR */
	VICVectCntl0 = (uint32)(os_vic_initcb.VICVectCntl0_init); 			/* Priority 16 */
	VICVectCntl1 = (uint32)(os_vic_initcb.VICVectCntl1_init); 			/* Priority 15 */
	VICVectCntl2 = (uint32)(os_vic_initcb.VICVectCntl2_init); 			/* Priority 14 */
	VICVectCntl3 = (uint32)(os_vic_initcb.VICVectCntl3_init); 			/* Priority 13 */
	VICVectCntl4 = (uint32)(os_vic_initcb.VICVectCntl4_init); 			/* Priority 12 */
	VICVectCntl5 = (uint32)(os_vic_initcb.VICVectCntl5_init); 			/* Priority 11 */
	VICVectCntl6 = (uint32)(os_vic_initcb.VICVectCntl6_init); 			/* Priority 10 */
	VICVectCntl7 = (uint32)(os_vic_initcb.VICVectCntl7_init); 			/* Priority 9 */
	VICVectCntl8 = (uint32)(os_vic_initcb.VICVectCntl8_init); 			/* Priority 8 */
	VICVectCntl9 = (uint32)(os_vic_initcb.VICVectCntl9_init); 			/* Priority 7 */
	VICVectCntl10 = (uint32)(os_vic_initcb.VICVectCntl10_init); 		/* Priority 6 */
	VICVectCntl11 = (uint32)(os_vic_initcb.VICVectCntl11_init); 		/* Priority 5 */
	VICVectCntl12 = (uint32)(os_vic_initcb.VICVectCntl12_init); 		/* Priority 4 */
	VICVectCntl13 = (uint32)(os_vic_initcb.VICVectCntl13_init); 		/* Priority 3 */
	VICVectCntl14 = (uint32)(os_vic_initcb.VICVectCntl14_init); 		/* Priority 2 */
	VICVectCntl15 = (uint32)(os_vic_initcb.VICVectCntl15_init); 		/* Priority 1 */
	
	/* Set up which interrupt source is a FIQ and which an IRQ */
	VICIntSelect = os_vic_initcb.VICIntSelect_init;
}

