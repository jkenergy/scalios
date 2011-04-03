/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-13 18:54:41 +0000 (Wed, 13 Feb 2008) $
 * $LastChangedRevision: 619 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/common/dsPIC/isr.c $
 * 
 * Target CPU: 		dsPIC
 * Target compiler:	Microchip C30
 * Visibility:		User
 * 
 * Defines target-specific implementation of standard test driver functions.
 */

#include <osint.h>
#include <drivers/timer1.h>
#include <framework.h>

/* Returns 1 if this cat1 interrupt will interrupt the cat2 ISR defined
 * here.
 */
unat cat1_interrupts_cat2(void)
{
	return 1U;
}

/* Returns 1 if the cat1 won't interrupt the cat2, but will precede it (i.e.
 * run first) when the two are both raised at the same time.
 */
unat cat1_precedes_cat2(void)
{
	assert(!cat1_interrupts_cat2());
	return 1U;
}

/* Pair of functions to trigger and dismiss a category 1 ISR; used to test the
 * DisableAllInterrupts() and SuspendAllInterrupts() 'fast' interrupt functions.
 */
void testing_trigger_cat1_isr(void)
{
	/* Uses INT1 as the triggerable interrupt. Connected to external pin. Important not to trigger
	 * this interrupt via hardware source directly.
	 * 
	 * CHECK: must have a higher priority than INT0.
	 */
	IPC4 = (IPC4 & 0xfff0U) | 0x0002U;		/* Set priority to 2 */
	BCLR_IFS1(0);							/* Dismiss any prior interrupt */
	BSET_IEC1(0);							/* Set interrupts to enabled */
	BSET_IFS1(0);							/* Set interrupt to pending */
}

void testing_dismiss_cat1_isr(void)
{
	/* Clear IFS1 bit 0 to dismiss interrupt */
	BCLR_IFS1(0);
}

/* Actual handler for category 1 ISR; target- and compiler-specific code here */
void __attribute__((interrupt,auto_psv)) _INT1Interrupt(void) 
{
	testing_dismiss_cat1_isr();
	SET_TESTEVENT("Running cat1");
}

/* Sets up an interrupt source and makes it pending; returns when it is pending and an interrupt will
 * occur. ISR is at lowest priority.
 */
void testing_trigger_isr(void)
{
#ifdef USECAT2ISR
	/* Uses INT0 as the triggerable interrupt. Connected to external pin. Important not to trigger this
	 * interrupt via hardware source directly (CHECK: "INT0" must be the vector for ISRX in the test OIL)
	 * Write IPC0 bits 0-2 to priority 1 (CHECK: must be consistent with the priority for ISRX in the test OIL)
	 * Write to IEC0 bit 0 to enable the interrupt.
	 * Write to IFS0 bit 0 to set the interrupt pending.
	 */
	IPC0 = (IPC0 & 0xfff0U) | 0x0001U;		/* Set priority to 1 */
	BCLR_IFS0(0);							/* Dismiss any prior interrupt */
	BSET_IEC0(0);							/* Set interrupts to enabled */
	BSET_IFS0(0);							/* Set interrupt pending */
#endif
}

/* Called by ISR to dismiss the source */
void testing_dismiss_isr(void)
{
#ifdef USECAT2ISR
	/* Clear IFS0 bit 0 to dismiss interrupt */
	BCLR_IFS0(0);
#endif
}

/* Called by test to stop the ISR from interrupting in case
 * it became pending */
void testing_stop_isr(void)
{
	/* Disable future interrupts */
	BCLR_IEC0(0);
	
	/* Dismiss any pending interrupt */
	BCLR_IFS0(0);
}

#ifdef USE_FAKE_COUNTER_DRIVER

/* These functions substitute the ones in the library; they can be compiled out when the real timer
 * driver is wanted in a test.
 * 
 * The functions are patched through to a generic gake timer handler in the test. The 'dev' parameter
 * is omitted since it cannot be decoded by the generic driver.
 */

/* Called when Scalios shuts down */
void os_cdevicedrv_timer1_stop(DeviceId dev)
{
	assert(KERNEL_LOCKED());

	fake_timer_stop();
}

/* Note that the start function passes through the counter: it decodes this from 'dev', since the
 * generic functions require a binding from the counter but do not know how to decode the
 * target- and device-specific device control block.
 */
void os_cdevicedrv_timer1_start(DeviceId dev)
{
	struct os_devicecb_timer1 *timer1 = (struct os_devicecb_timer1 *)dev;
	CounterType counter;
	
	assert(KERNEL_LOCKED());

	counter = timer1->this_counter;
	fake_timer_start(counter);
}

void os_cdevicedrv_timer1_disable_ints(DeviceId dev)
{
	assert(KERNEL_LOCKED());

	fake_timer_disable_ints();
}

void os_cdevicedrv_timer1_enable_ints(DeviceId dev, TickType now, TickType rel)
{
	assert(KERNEL_LOCKED());

	fake_timer_enable_ints(now, rel);
}

TickType os_cdevicedrv_timer1_now(DeviceId dev)
{
	assert(KERNEL_LOCKED());

	fake_timer_now();
}

#endif /* USE_FAKE_COUNTER_DRIVER */
