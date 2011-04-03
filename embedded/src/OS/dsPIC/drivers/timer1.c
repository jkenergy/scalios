/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-17 05:05:51 +0000 (Thu, 17 Jan 2008) $
 * $LastChangedRevision: 560 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/dsPIC/drivers/timer1.c $
 * 
 * Target CPU:		dsPIC
 * Target compiler:	MPLAB C30
 * Visibility:		Internal
 *
 * This is a simulation of a hardware counter that uses a tick interrupt from timer1
 * to advance a static variable (timer1_count). This is compared against an output compare
 * simulation register, and the counter expires in the normal way.
 */

#include <osint.h>
#include <drivers/timer1.h>

static TickType timer1_count;
static TickType timer1_compare;
static uint16 timer1_intsenabled;
static CounterType this_counter;	/* Cache of timer1->this_counter; ISR code (see below) uses this to determine the counter */

/* Called when Scalios shuts down */
void os_cdevicedrv_timer1_stop(DeviceId dev)
{
	T1_INTERRUPT_DISABLE();			/* No further interrupts from OC since stopping Scalios */
}

/* Called when Scalios is starting up. Set timer and OC into a known state ready for events. */
void os_cdevicedrv_timer1_start(DeviceId dev)
{
	struct os_devicecb_timer1 *timer1 = (struct os_devicecb_timer1 *)dev;
	PR1 = timer1->PR1_init;
	T1_INTERRUPT_DISABLE();			/* Don't ever want wrap interrupts from timer */
	T1CON = timer1->T1CON_init;		/* Set timer counting (and other config settings) */
	TMR1 = 0;						/* Start time at zero */
	timer1_count = 0;
	T1_INTERRUPT_DISMISS();
	T1_INTERRUPT_ENABLE();
	this_counter = timer1->this_counter;
}

void os_cdevicedrv_timer1_disable_ints(DeviceId dev)
{
	timer1_intsenabled = 0;
}

void os_cdevicedrv_timer1_enable_ints(DeviceId dev, TickType now, TickType rel)
{
	/* No race to resolve because timer count is advanced by software */
	timer1_compare = now + rel;
	timer1_intsenabled = 1U;
}

TickType os_cdevicedrv_timer1_now(DeviceId dev)
{
	assert(KERNEL_LOCKED());
	
	return timer1_count;
}

/* The generic timer build process generates a single ExpireCounter() call; this function
 * replaces the generic because it includes the count increment and compare functionality.
 */
ISR(os_timer1)
{
	T1_INTERRUPT_DISMISS();
	timer1_count++;
	if(timer1_intsenabled && timer1_count == timer1_compare) {
		os_ExpireCounter(this_counter);
	}
}
