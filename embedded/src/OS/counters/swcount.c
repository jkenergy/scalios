/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/counters/swcount.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * Function for handling the incrementing of a software counter. Does the work of incrementing the counter, but
 * this function is called from the API and also the "handler" for an alarm.
 */

/* A software counter is a device driver for a standard (hardware) counter */

#include <osint.h>

static void os_increment_counter_single(CounterType c)
{
	const struct os_devicecb_soft *dev = (const struct os_devicecb_soft *)c->device;
	struct os_devicecb_soft_dyn *dyn = dev->dyn;
	
	assert(KERNEL_LOCKED());
	assert(SINGLETON_COUNTER(c));

	/* Increment the count and then check to see if it has matched with the alarm 
	 * expiry time. If so, process the alarm. Wraps at the maxallowedvalue point.
	 * 
	 * (Effectively an emulation of a hardware counter).
	 * 
	 */
	/* $Req: artf1064 $ */
	if(dyn->count == c->alarmbase.maxallowedvalue) {
		dyn->count = 0;
	}
	else {
		dyn->count++;
	}
	
	if(dyn->enabled && dyn->count == dyn->match) {
		/* Note: the following call potentially leads to recursion. The system builder tool
		 * must warn of this and the user must take care.
		 */
		AlarmType a = EXPIRED_ALARM(c);
		a->process(a->action);		/* Call the handler for the alarm that expired (which will activate the task, etc.) $Req: artf1064 $ */	

		/* Set the expired alarm up to expire again (cycle > 0) or stop */
		c->expired(c);
	}
}

/* Worker function to increment counter. Called from API and also alarm expiry handler, etc. */
static void os_increment_counter_multi(CounterType c)
{
	const struct os_devicecb_soft *dev = (const struct os_devicecb_soft *)c->device;
	struct os_devicecb_soft_dyn *dyn = dev->dyn;

	assert(KERNEL_LOCKED());
	assert(!SINGLETON_COUNTER(c));

	/* $Req: artf1064 $ */
	if(dyn->count == c->alarmbase.maxallowedvalue) {
		dyn->count = 0;
	}
	else {
		dyn->count++;
	}
	
	/* A while loop is used because 'rel' passed to 'enableints' can be 0, entailing
	 * either (A) an implementation whereby enableints loops over concurrent events, or
	 * (B) an implementation where it is done here.
	 * 
	 * Implementation (B) is preferred for two reasons: (1) it keeps the drivers at O(1)
	 * complexity which makes real-time predictability easier, and (2) it keeps the maximum
	 * stack depth lower.
	 */
	while(dyn->enabled && dyn->count == dyn->match) {
		AlarmType a = EXPIRED_ALARM(c);

		/* Note: the following call potentially leads to recursion. The system builder tool
		 * must warn of this and the user must take care.
		 */
		a->process(a->action);		/* Call the handler for the alarm that expired (which will activate the task, etc.) $Req: artf1064 $ */	

		/* This call would normally advance 'due' forwards, but might not if there are
		 * concurrent alarm expiry events.
		 */
		c->expired(c);
		
	}
}

void os_increment_counter(CounterType c)
{
	if(SINGLETON_COUNTER(c)) {
		os_increment_counter_single(c);
	}
	else {
		os_increment_counter_multi(c);
	}
}

/* Device driver calls for software counter */
void os_cdevicedrv_soft_stop(DeviceId dev)
{
	assert(KERNEL_LOCKED());
}

void os_cdevicedrv_soft_start(DeviceId dev)
{
	struct os_devicecb_soft_dyn *softcounter = ((const struct os_devicecb_soft *)dev)->dyn;
	
	softcounter->count = 0;
	
	assert(KERNEL_LOCKED());
}

void os_cdevicedrv_soft_disable_ints(DeviceId dev)
{
	struct os_devicecb_soft_dyn *softcounter = ((const struct os_devicecb_soft *)dev)->dyn;	
	softcounter->enabled = 0;
	
	assert(KERNEL_LOCKED());
}

void os_cdevicedrv_soft_enable_ints(DeviceId dev, TickType now, TickType rel)
{
	const struct os_devicecb_soft *softdev = (const struct os_devicecb_soft *)dev;
	const TickType time_to_wrap = softdev->maxallowedvalue - now;
	
	/* Normally driver would have to look at 'now', the relative time from now,
	 * and the timer to see what the due time should be (and whether it has
	 * expired already). But no need for a software counter to be this complex
	 * because time is not advancing until 'count' is incremented (i.e. there is
	 * no race condition).
	 */
	softdev->dyn->enabled = 1U;
	assert(now == softdev->dyn->count);
	
	/* Calculate the value of the counter at the right time */
	if(time_to_wrap < rel) {	/* Yes, a wrap will take place */
		rel -= time_to_wrap;
		softdev->dyn->match = rel;
	}
	else {
		softdev->dyn->match = now + rel;
	}
	
	assert(KERNEL_LOCKED());
}

TickType os_cdevicedrv_soft_now(DeviceId dev)
{
	struct os_devicecb_soft_dyn *softcounter = ((const struct os_devicecb_soft *)dev)->dyn;
	
	/* Kernel needs to be locked to ensure driver isn't multi-threaded: there are concurrent
	 * update problems (in general) if the driver calls are re-entered or interleaved.
	 */
	assert(KERNEL_LOCKED());
	
	return softcounter->count;
}
