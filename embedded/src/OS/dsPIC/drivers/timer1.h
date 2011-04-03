/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-17 05:05:51 +0000 (Thu, 17 Jan 2008) $
 * $LastChangedRevision: 560 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/drivers/timer1.h $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	MPLAB C30
 * Visibility:		Internal
 * 
 * This is the main header file for the timer1 driver. This timer is used only for a "tick" driver, set
 * up to interrupt at a regular interval and drive a software counter (which can then be used to drive
 * subsidiary counters).
 * 
 * The driver is pre-compiled and placed in the OS library in the normal way.
 */

#ifndef TIMER1_H_
#define TIMER1_H_

/* Structure created and initialized by configuration process.
 * 
 * T1CON contains the following subfields:
 * 
 * TON			bit 15		1=Timer enabled
 * TSIDL		bit 13		1=Resume counting at end of idle
 * TGATE		bit 6		if TCS=0, then don't care; if TCS=1 then 1=external osc., 0=TCY (instruction clock)
 * TCKPS<0:1>	bit 4:5		00=1, 01=8, 10=64, 11=256 				@todo documentation isn't clear if it's this way round
 * TSYNC		bit 2		1=Synchronous clock source, 0=async.		
 * TCS			bit 1		1=external clock select, 0=internal clock
 * 
 * Driver needs T1CON_init to have:
 * 	TON=1
 * 	TSIDL = user-defined
 *  TGATE = user-defined
 *  TCKPS = user-defined
 *  TSYNC = user-defined
 *  TCS = user-defined
 */
struct os_devicecb_timer1 {
	uint16 PR1_init;
	uint16 T1CON_init;
	CounterType this_counter;
};

/* This variable is initialized by the configuration tool to link to a defined software
 * counter.
 */

/* These register locations are common across dsPIC30, dsPIC33 and PIC24 */
#define T1CON							(*(volatile uint16 *)(0x0104u))
#define TMR1							(*(volatile uint16 *)(0x0100u))
#define PR1								(*(volatile uint16 *)(0x0102u))

/* Must use atomic instruction to set or clear bits in the IFS0 register
 * because read-modify-write across more than one instruction could lead
 * to accidental clearing of other interrupts in the same register.
 * 
 * These bit patterns, for timer 1, are common across dsPIC30, dsPIC33 and PIC24.
 * @todo recheck this.
 */
#define T1_INTERRUPT_DISABLE()			BCLR_IEC0(3)		/* T1IE is IEC0 bit 3 */
#define T1_INTERRUPT_ENABLE()			BSET_IEC0(3)
#define T1_INTERRUPT_DISMISS()			BCLR_IFS0(3)

/* Function prototypes for the two timer drivers */
void os_cdevicedrv_timer1_stop(DeviceId);
void os_cdevicedrv_timer1_start(DeviceId);
void os_cdevicedrv_timer1_disable_ints(DeviceId);
void os_cdevicedrv_timer1_enable_ints(DeviceId, TickType, TickType);
TickType os_cdevicedrv_timer1_now(DeviceId);

#endif /*TIMER1_H_*/

