/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-17 05:05:51 +0000 (Thu, 17 Jan 2008) $
 * $LastChangedRevision: 560 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/drivers/timer23.h $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	MPLAB C30
 * Visibility:		Application (compiled against at configuration time)
 *
 * Definitions for the dsPIC/PIC24 timers. See:
 * 
 * "dsPIC30F Family Reference Manual" (Section 12)
 * 
 * See also:
 * 
 * "Writing A Timer Device Driver"
 * 
 * for details on the generic OS timer driver model.
 * 
 * The OS model require a 16-bit free-running counter with an output-compare interrupt.
 * This is only possible for timers 2 and 3 on the dsPIC/PIC24 family. For performance
 * reasons there are two hardwired drivers: one for timer2 and one for timer3. In addition,
 * the mapping of the output compare module to the timer is fixed at compile time by code
 * generation from the offline tool.
 * 
 * The mapping is fixed because the choice of OC must be made at configuration time: the
 * number of OC channels is not known until the specific device is chosen, and some OC
 * channels have hardware constraints (they control physical pins on the specific chip).
 * Further, interrupts from the OC must be dismissed with a BCLR instruction, and this
 * needs to inline the exact parameters (known only at configuration time).
 * 
 * This file contains the necessary definitions to allow the code generator to select the
 * appropriate code.
 */

#ifndef _TIMER23_H_
#define _TIMER23_H_

/* Note that the dsPIC33F is the same as the PIC24H */

#if !defined(__PIC24__) && !defined(__dsPIC30__)
#error Either __PIC24__ or __dsPIC30__ must be defined
#else
#if defined(__PIC24__) && defined(__dsPIC30__)
#error Only one of __PIC24__ and __dsPIC30__ can be defined
#endif
#endif

/* Device struct definition: empty since everything is inlined at application build time */
struct os_devicecb_timerA {
};

/* Definitions for timer and output-compare registers for dsPIC30F/dsPIC33/PIC24 */

#define OC1CON							(*(volatile uint16 *)(0x0184u))
#define OC2CON							(*(volatile uint16 *)(0x018au))
#define OC3CON							(*(volatile uint16 *)(0x0190u))
#define OC4CON							(*(volatile uint16 *)(0x0196u))
#define OC5CON							(*(volatile uint16 *)(0x019cu))
#define OC6CON							(*(volatile uint16 *)(0x01a2u))
#define OC7CON							(*(volatile uint16 *)(0x01a8u))
#define OC8CON							(*(volatile uint16 *)(0x01aeu))
#define OC1R							(*(volatile uint16 *)(0x0182u))
#define OC2R							(*(volatile uint16 *)(0x0188u))
#define OC3R							(*(volatile uint16 *)(0x018eu))
#define OC4R							(*(volatile uint16 *)(0x0194u))
#define OC5R							(*(volatile uint16 *)(0x019au))
#define OC6R							(*(volatile uint16 *)(0x01a0u))
#define OC7R							(*(volatile uint16 *)(0x01a6u))
#define OC8R							(*(volatile uint16 *)(0x01acu))
#define T2CON							(*(volatile uint16 *)(0x0110u))
#define T3CON							(*(volatile uint16 *)(0x0112u))
#define TMR2							(*(volatile uint16 *)(0x0106u))
#define TMR3							(*(volatile uint16 *)(0x010au))
#define PR2								(*(volatile uint16 *)(0x010cu))
#define PR3								(*(volatile uint16 *)(0x010eu))

/* Must use atomic instruction to set or clear bits in the IFS0 register
 * because read-modify-write across more than one instruction could lead
 * to accidental clearing of other interrupts in the same register.
 */
/* Macros to control interrupt registers. NB: these vary between dsPIC30F, dsPIC33F and PIC24 */
#ifdef __PIC24__
#define OC1_INTERRUPT_DISMISS()			BCLR_IFS0(2)		/* OC1IF is IFS0 bit 2 */
#define OC1_INTERRUPT_SET_PENDING()		BSET_IFS0(2)
#define OC1_INTERRUPT_ENABLE()			BCLR_IEC0(2)
#define OC1_INTERRUPT_DISABLE()			BSET_IEC0(2)
#define OC2_INTERRUPT_DISMISS()			BCLR_IFS0(6)		/* OC2IF is IFS0 bit 6 */
#define OC2_INTERRUPT_SET_PENDING()		BSET_IFS0(6)
#define OC2_INTERRUPT_ENABLE()			BCLR_IEC0(6)
#define OC2_INTERRUPT_DISABLE()			BSET_IEC0(6)
#define OC3_INTERRUPT_DISMISS()			BCLR_IFS1(9)		/* OC3IF is IFS1 bit 9 */
#define OC3_INTERRUPT_SET_PENDING()		BSET_IFS1(9)
#define OC3_INTERRUPT_ENABLE()			BCLR_IEC1(9)
#define OC3_INTERRUPT_DISABLE()			BSET_IEC1(9)
#define OC4_INTERRUPT_DISMISS()			BCLR_IFS1(10)		/* OC4IF is IFS1 bit 10 */
#define OC4_INTERRUPT_SET_PENDING()		BSET_IFS1(10)
#define OC4_INTERRUPT_ENABLE()			BCLR_IEC1(10)
#define OC4_INTERRUPT_DISABLE()			BSET_IEC1(10)
#define OC5_INTERRUPT_DISMISS()			BCLR_IFS2(9)		/* OC5IF is IFS2 bit 9 */
#define OC5_INTERRUPT_SET_PENDING()		BSET_IFS2(9)
#define OC5_INTERRUPT_ENABLE()			BCLR_IEC2(9)
#define OC5_INTERRUPT_DISABLE()			BSET_IEC2(9)
#define OC6_INTERRUPT_DISMISS()			BCLR_IFS2(10)		/* OC6IF is IFS2 bit 10 */
#define OC6_INTERRUPT_SET_PENDING()		BSET_IFS2(10)
#define OC6_INTERRUPT_ENABLE()			BCLR_IEC2(10)
#define OC6_INTERRUPT_DISABLE()			BSET_IEC2(10)
#define OC7_INTERRUPT_DISMISS()			BCLR_IFS2(11)		/* OC7IF is IFS2 bit 11 */
#define OC7_INTERRUPT_SET_PENDING()		BSET_IFS2(11)
#define OC7_INTERRUPT_ENABLE()			BCLR_IEC2(11)
#define OC7_INTERRUPT_DISABLE()			BSET_IEC2(11)
#define OC8_INTERRUPT_DISMISS()			BCLR_IFS2(12)		/* OC8IF is IFS2 bit 12 */
#define OC8_INTERRUPT_SET_PENDING()		BSET_IFS2(12)
#define OC8_INTERRUPT_ENABLE()			BCLR_IEC2(12)
#define OC8_INTERRUPT_DISABLE()			BSET_IEC2(12)

#define T2_INTERRUPT_DISABLE()			BCLR_IEC0(7)		/* T2IE is IEC0 bit 7 */
#define T3_INTERRUPT_DISABLE()			BCLR_IEC0(8)		/* T3IE is IEC0 bit 8 */
#endif /* __PIC24__ */

#ifdef __dsPIC30__
#define OC1_INTERRUPT_DISMISS()			BCLR_IFS0(2)		/* OC1IF is IFS0 bit 2 */
#define OC1_INTERRUPT_SET_PENDING()		BSET_IFS0(2)
#define OC1_INTERRUPT_ENABLE()			BCLR_IEC0(2)
#define OC1_INTERRUPT_DISABLE()			BSET_IEC0(2)
#define OC2_INTERRUPT_DISMISS()			BCLR_IFS0(5)		/* OC2IF is IFS0 bit 5 */
#define OC2_INTERRUPT_SET_PENDING()		BSET_IFS0(5)
#define OC2_INTERRUPT_ENABLE()			BCLR_IEC0(5)
#define OC2_INTERRUPT_DISABLE()			BSET_IEC0(5)
#define OC3_INTERRUPT_DISMISS()			BCLR_IFS1(3)		/* OC3IF is IFS1 bit 3 */
#define OC3_INTERRUPT_SET_PENDING()		BSET_IFS1(3)
#define OC3_INTERRUPT_ENABLE()			BCLR_IEC1(3)
#define OC3_INTERRUPT_DISABLE()			BSET_IEC1(3)
#define OC4_INTERRUPT_DISMISS()			BCLR_IFS1(4)		/* OC4IF is IFS1 bit 4 */
#define OC4_INTERRUPT_SET_PENDING()		BSET_IFS1(4)
#define OC4_INTERRUPT_ENABLE()			BCLR_IEC1(4)
#define OC4_INTERRUPT_DISABLE()			BSET_IEC1(4)
#define OC5_INTERRUPT_DISMISS()			BCLR_IFS2(0)		/* OC5IF is IFS2 bit 0 */
#define OC5_INTERRUPT_SET_PENDING()		BSET_IFS2(0)
#define OC5_INTERRUPT_ENABLE()			BCLR_IEC2(0)
#define OC5_INTERRUPT_DISABLE()			BSET_IEC2(0)
#define OC6_INTERRUPT_DISMISS()			BCLR_IFS2(1)		/* OC6IF is IFS2 bit 1 */
#define OC6_INTERRUPT_SET_PENDING()		BSET_IFS2(1)
#define OC6_INTERRUPT_ENABLE()			BCLR_IEC2(1)
#define OC6_INTERRUPT_DISABLE()			BSET_IEC2(1)
#define OC7_INTERRUPT_DISMISS()			BCLR_IFS2(2)		/* OC7IF is IFS2 bit 2 */
#define OC7_INTERRUPT_SET_PENDING()		BSET_IFS2(2)
#define OC7_INTERRUPT_ENABLE()			BCLR_IEC2(2)
#define OC7_INTERRUPT_DISABLE()			BSET_IEC2(2)
#define OC8_INTERRUPT_DISMISS()			BCLR_IFS2(3)		/* OC8IF is IFS2 bit 3 */
#define OC8_INTERRUPT_SET_PENDING()		BSET_IFS2(3)
#define OC8_INTERRUPT_ENABLE()			BCLR_IEC2(3)
#define OC8_INTERRUPT_DISABLE()			BSET_IEC2(3)

#define T2_INTERRUPT_DISABLE()			BCLR_IEC0(6)		/* T2IE is IEC0 bit 6 */
#define T3_INTERRUPT_DISABLE()			BCLR_IEC0(7)		/* T3IE is IEC0 bit 7 */
#endif /* __dsPIC30__ */

/* Function prototypes for the two timer drivers */
void os_cdevicedrv_timer2_stop(DeviceId dev);
void os_cdevicedrv_timer2_start(DeviceId dev);
void os_cdevicedrv_timer2_disable_ints(DeviceId dev);
void os_cdevicedrv_timer2_enable_ints(DeviceId dev, TickType now, TickType rel);
TickType os_cdevicedrv_timer2_now(DeviceId dev);

void os_cdevicedrv_timer3_stop(DeviceId dev);
void os_cdevicedrv_timer3_start(DeviceId dev);
void os_cdevicedrv_timer3_disable_ints(DeviceId dev);
void os_cdevicedrv_timer3_enable_ints(DeviceId dev, TickType now, TickType rel);
TickType os_cdevicedrv_timer3_now(DeviceId dev);

#ifdef TIMER2
/* Called when Scalios is shutting down. Stop future interrupts from OC. Leave timer running in its
 * settings in case post-RTOS application wishes to do something with the timer.
 */
void os_cdevicedrv_timer2_stop(DeviceId dev)
{
	T2_OCx_INTERRUPT_DISABLE();		/* No further interrupts from OC since stopping Scalios */
}

/* Called when Scalios is starting up. Set timer and OC into a known state ready for events. */
void os_cdevicedrv_timer2_start(DeviceId dev)
{
	T2_INTERRUPT_DISABLE();			/* Don't ever want wrap interrupts from timer */
	T2_OCx_INTERRUPT_DISABLE();		/* Don't (yet) want match interrupts from OC: nothing to match yet */
	T2CON = T2CON_INIT;				/* Set timer counting (and other config settings) */
	PR2 = 0xFFFFU;					/* Make sure full 16-bit counter */
	T2_OCxCON = T2_OCxCON_INIT;		/* Set up matching OC */
	TMR2 = 0;						/* Start time at zero */
}

/* Called by alarm handling code when no more events are due in the future. Timer keeps
 * running.
 */
void os_cdevicedrv_timer2_disable_ints(DeviceId dev)
{
	T2_OCx_INTERRUPT_DISABLE();
}

/* Called to schedule a new event in the future. Called by the alarm subsystem when
 * the next due event in the hardware (if any) is no longer needed and must be cancelled
 * and replaced with the demanded one.
 */
void os_cdevicedrv_timer2_enable_ints(DeviceId dev, TickType now, TickType rel)
{
	TickType ticks_to_event;

	/* Set the comparitor to match against the new event-due time */
	T2_OCxR = now + rel;
	
	/* Cancel any pending interrupt (prior to the OCxR write the OCxR register
	 * could have contained garbage, since this call made be made with no prior
	 * events, and there may therefore have been a spurious match).
	 */
	T2_OCx_INTERRUPT_DISMISS();
	
	/* Make sure interrupts are enabled (if not already) */
	T2_OCx_INTERRUPT_ENABLE();
	
	/* Resolve the race condition: check to see if we missed the time we set the event for. */
	
	/* Step 1: work out how long from recent-past until event expiry */
	ticks_to_event = TMR2 - now; /* Done in modulo arithmetic */

	/* Step 2: check to see if this time has already expired */
	if(rel < ticks_to_event) {
		/* The comparitor might not have been set when the counter rolled around */
		/* to the event-due time. In any case, the event is already due. */
		/* Step 3: manually set the interrupt pending. */
		T2_OCx_INTERRUPT_SET_PENDING();
	}
}

/* Returns the current time in the counter. */
TickType os_cdevicedrv_timer2_now(DeviceId dev)
{
	return TMR2;
}
#endif /* TIMER2 */

#ifdef TIMER3
/* These functions are exactly the same as the timer 2 ones except that a 2 is replaced
 * by a 3. KEEP CONSISTENT (i.e. if a bug fix is applied to one set, apply it to the other
 * set too).
 */
void os_cdevicedrv_timer3_stop(DeviceId dev)
{
	T3_OCx_INTERRUPT_DISABLE();
}

void os_cdevicedrv_timer3_start(DeviceId dev)
{
	T3_INTERRUPT_DISABLE();
	T3_OCx_INTERRUPT_DISABLE();
	T3CON = T3CON_INIT;
	PR2 = 0xFFFFU;
	T3_OCxCON = T3_OCxCON_INIT;
	TMR3 = 0;
}

void os_cdevicedrv_timer3_disable_ints(DeviceId dev)
{
	T3_OCx_INTERRUPT_DISABLE();
}

void os_cdevicedrv_timer3_enable_ints(DeviceId dev, TickType now, TickType rel)
{
	TickType ticks_to_event;

	T3_OCxR = now + rel;
	T3_OCx_INTERRUPT_DISMISS();
	T3_OCx_INTERRUPT_ENABLE();
	ticks_to_event = TMR3 - now;
	if(rel < ticks_to_event) {
		T3_OCx_INTERRUPT_SET_PENDING();
	}
}

TickType os_cdevicedrv_timer3_now(DeviceId dev)
{
	return TMR3;
}
#endif /* TIMER3 */

#endif /* TIMER23_H */
