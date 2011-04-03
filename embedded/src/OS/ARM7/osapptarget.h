/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:37:25 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 701 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/ARM7/osapptarget.h $
 * 
 * Target CPU:		ARM7
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

#ifndef _TARGET_H_
#define _TARGET_H_

#define USE_BITFIELD_FLAGS		/* Bitfields are implemented efficiently on this target */

/* By default bring in the definitions of the target environment. If these are not wanted then
 * they can be omitted by adding -DOMIT_LPC21XX_DEFS to the application code compilation.
 */
#ifndef OMIT_LPC21XX_DEFS

/* Definitions for ARMPrimeCell Vectored Interrupt Controller
 */
#define VICIRQStatus		(*((volatile uint32 *)0xFFFFF000))
#define VICFIQStatus		(*((volatile uint32 *)0xFFFFF004))
#define VICRawIntr			(*((volatile uint32 *)0xFFFFF008))
#define VICIntSelect		(*((volatile uint32 *)0xFFFFF00C))
#define VICIntEnable		(*((volatile uint32 *)0xFFFFF010))
#define VICIntEnClr			(*((volatile uint32 *)0xFFFFF014))
#define VICSoftInt			(*((volatile uint32 *)0xFFFFF018))
#define VICSoftIntClr		(*((volatile uint32 *)0xFFFFF01C))
#define VICProtection		(*((volatile uint32 *)0xFFFFF020))
#define VICVectAddr			(*((volatile uint32 *)0xFFFFF030))
#define VICDefVectAddr		(*((volatile uint32 *)0xFFFFF034))
#define VICVectAddr0		(*((volatile uint32 *)0xFFFFF100))
#define VICVectAddr1		(*((volatile uint32 *)0xFFFFF104))
#define VICVectAddr2		(*((volatile uint32 *)0xFFFFF108))
#define VICVectAddr3		(*((volatile uint32 *)0xFFFFF10C))
#define VICVectAddr4		(*((volatile uint32 *)0xFFFFF110))
#define VICVectAddr5		(*((volatile uint32 *)0xFFFFF114))
#define VICVectAddr6		(*((volatile uint32 *)0xFFFFF118))
#define VICVectAddr7		(*((volatile uint32 *)0xFFFFF11C))
#define VICVectAddr8		(*((volatile uint32 *)0xFFFFF120))
#define VICVectAddr9		(*((volatile uint32 *)0xFFFFF124))
#define VICVectAddr10		(*((volatile uint32 *)0xFFFFF128))
#define VICVectAddr11		(*((volatile uint32 *)0xFFFFF12C))
#define VICVectAddr12		(*((volatile uint32 *)0xFFFFF130))
#define VICVectAddr13		(*((volatile uint32 *)0xFFFFF134))
#define VICVectAddr14		(*((volatile uint32 *)0xFFFFF138))
#define VICVectAddr15		(*((volatile uint32 *)0xFFFFF13C))
#define VICVectCntl0		(*((volatile uint32 *)0xFFFFF200))
#define VICVectCntl1		(*((volatile uint32 *)0xFFFFF204))
#define VICVectCntl2		(*((volatile uint32 *)0xFFFFF208))
#define VICVectCntl3		(*((volatile uint32 *)0xFFFFF20C))
#define VICVectCntl4		(*((volatile uint32 *)0xFFFFF210))
#define VICVectCntl5		(*((volatile uint32 *)0xFFFFF214))
#define VICVectCntl6		(*((volatile uint32 *)0xFFFFF218))
#define VICVectCntl7		(*((volatile uint32 *)0xFFFFF21C))
#define VICVectCntl8		(*((volatile uint32 *)0xFFFFF220))
#define VICVectCntl9		(*((volatile uint32 *)0xFFFFF224))
#define VICVectCntl10		(*((volatile uint32 *)0xFFFFF228))
#define VICVectCntl11		(*((volatile uint32 *)0xFFFFF22C))
#define VICVectCntl12		(*((volatile uint32 *)0xFFFFF230))
#define VICVectCntl13		(*((volatile uint32 *)0xFFFFF234))
#define VICVectCntl14		(*((volatile uint32 *)0xFFFFF238))
#define VICVectCntl15		(*((volatile uint32 *)0xFFFFF23C))
#endif

#endif /* _ITARGET_H_ */

/* Control block for LPC21xx VIC */
struct os_lpc21xx_vic_initcb {
	ISRType VICDefVectAddr_init;							/* The ISR to handle all non-vectored IRQ interrupts (written to VICDefVectAddr); equal to PRIORITY = 0 in OIL */
	void (*FIQ_ISR) (void);									/* The function that handles all FIQ interrupts */

	/* A '1' in the bit number corresponding to the interrupt source number (see table below) indicates
	 * that the source will trigger a FIQ (else it will trigger an IRQ).
	 */
	uint32 VICIntSelect_init;
	
	/* The handle of the ISR that will service the interrupt source with the correspoding priority in OIL */
	ISRType VICVectAddr0_init;								/* PRIORITY = 16 */
	ISRType VICVectAddr1_init;
	ISRType VICVectAddr2_init;
	ISRType VICVectAddr3_init;
	ISRType VICVectAddr4_init;
	ISRType VICVectAddr5_init;
	ISRType VICVectAddr6_init;
	ISRType VICVectAddr7_init;
	ISRType VICVectAddr8_init;
	ISRType VICVectAddr9_init;
	ISRType VICVectAddr10_init;
	ISRType VICVectAddr11_init;
	ISRType VICVectAddr12_init;
	ISRType VICVectAddr13_init;
	ISRType VICVectAddr14_init;
	ISRType VICVectAddr15_init;								/* PRIORITY = 1 */
	
	/* Bits 4:0 indicate the number of the interrupt source corresponding to the priority in OIL. 
	 * Bit  5 is '1' if there is an interrupt at this priority, '0' otherwise.
	 * Bits 31:6 are always '0'.
	 */
	uint32 VICVectCntl0_init;								/* PRIORITY = 16 */				
	uint32 VICVectCntl1_init;
	uint32 VICVectCntl2_init;
	uint32 VICVectCntl3_init;
	uint32 VICVectCntl4_init;
	uint32 VICVectCntl5_init;
	uint32 VICVectCntl6_init;
	uint32 VICVectCntl7_init;
	uint32 VICVectCntl8_init;
	uint32 VICVectCntl9_init;
	uint32 VICVectCntl10_init;
	uint32 VICVectCntl11_init;
	uint32 VICVectCntl12_init;
	uint32 VICVectCntl13_init;
	uint32 VICVectCntl14_init;
	uint32 VICVectCntl15_init;								/* PRIORITY = 1 */
};

/* Table giving the interrupt source and the number corresponding to that source.
 * 
 * Interrupt source	Number
 * ---------------- ------
 * "CAN4_RX"			29
 * "CAN3_RX"			28
 * "CAN2_RX"			27
 * "CAN1_RX"			26
 * "CAN4_TX"			23
 * "CAN3_TX"			22
 * "CAN2_TX"			21
 * "CAN1_TX"			20
 * "CAN_common"			19
 * "ADC"				18
 * "EINT3"				17
 * "EINT2"				16
 * "EINT1"				15
 * "EINT0"				14
 * "RTC"				13
 * "PLL"				12
 * "SPI1_SSP"			11
 * "SPI0"				10
 * "I2C"				9
 * "PWM"				8
 * "UART1"				7
 * "UART0"				6
 * "TIMER1"				5
 * "TIMER0"				4
 * "ARMCore1"			3
 * "ARMCore0"			2
 * "WDT"				0
 */

/* Instantiated by the configuration process in osgen.c */
extern struct os_lpc21xx_vic_initcb os_vic_initcb;

