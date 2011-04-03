/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-29 02:11:24 +0000 (Sat, 29 Mar 2008) $
 * $LastChangedRevision: 698 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/startos.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal 
 * 
 * API call: void StartOS(AppModeType <Mode>)
 * 
 */

#include <osint.h>

static void start_counters(void)
{
	CounterType c = os_counters;
	unat i;
	
	for(i = os_num_counters; i > 0; i--) {
		c->driver->start(c->device);
		c++;
	}
}

/* Autostart the tasks identified by the specified app mode (if any) */
static void start_tasks(AppModeType appmode)
{
	TaskType t;
	unat n;
	
	os_priqueuestatus = 0;
	os_nexttask = OS_IDLE_TASK;
		
	TaskType const *i = appmode->autotasks;
	
	for (n = appmode->numautotasks; n > 0; n--) {
		t = *(i++);
		os_queuetask(t);	
	}
}

/* Function that checks that the RAM data has been initialized correctly. Defines the assumed
 * startup values of data; all other unchecked data is "don't care"
 */

#ifndef NDEBUG
static void assert_initialized(void)
{
	unat i;
	
	/* assert that the C run-time startup and/or os_reinit() has initialised the kernel variables correctly */
	assert(os_curtask == OS_IDLE_TASK);
	assert(os_curpri == IDLEPRI);
	assert(os_curisr == 0);
	assert(os_curenv == 0);
	assert(os_kssp == 0);
#ifdef OS_EXTENDED_STATUS
	assert(os_curlastlocked == 0);
#endif		
	/* @TODO artf1307 assert any other kernel vars */

	/* @TODO artf1306 strength-reduce this code */
	for(i = 0; i < os_num_alarms; i++) {
		AlarmType a = &os_alarms[i];
		
		assert(a->dyn.c->running == 0);
		/* Other dyn.c and dyn.m values are "don't care" because are set when the alarm is set running */
	}
	
	for(i = 0; i < os_num_schedtables; i++) {
		ScheduleTableType t = &os_schedtables[i];
		
		assert(t->dyn->nexted == 0);
		assert(t->alarm->dyn.c->running == 0);
		/* t->dyn->current_xpoint == t->first_xpoint for autostarted alarms but cannot recognize that
		 * since can't easily tell which schedule tables are autostarted (i.e. which tables are
		 * bound to autostarted alarms).
		 */
		/* Other dyn values are "don't care" */
	}

	for(i = 0; i < os_num_counters; i++) {
		CounterType c = &os_counters[i];

		if(!SINGLETON_COUNTER(c)) {	
			assert(c->dyn->head == 0);			/* Must be zero because used to guard against empty queue */
			assert(c->dyn->long_tick == 0);
			assert(c->dyn->last_now == 0);
		}
		/* Other values are "don't care"; singleton counters have no dynamic values */
	}
}
#endif /* NDEBUG */

/* 
 * StartOS API call.
 * This service is called to start the operating system in a specific mode.
 */
void os_StartOS(AppModeType appmode)
{
	os_ipl save_ipl;
	
	OS_SAVE_IPL(save_ipl);									/* Save IPL on entry so can restore on exit, side-effect: assigns back to save_ipl */
	
	ENTER_KERNEL_DIRECT();								/* Set IPL to kernel level while setting up OS variables etc. */
	OS_API_TRACE_START_OS();
	INIT_STACKCHECK_TO_OFF();							/* Make sure stack checking is turned off so that when/if dispatch() is called there are no stack errors
														 * Note that this requires the kernel stack (i.e. main()'s stack) is big enough to support this call to
														 * StartOS plus a call to dispatch() and the entry into the subsequent task
														 */
#ifndef NDEBUG
	assert_initialized();								/* assert the C runtime startup has setup all vars to startup state */
#endif
	
	/* Saving a jmp_buf (os_startosenv) so that the entire OS can be terminated via a longjmp call inside a ShutdownOS call */
	/* $Req: artf1216 artf1219 $ */
	if (SETJMP(os_startosenv) == 0) {					
														/* This half of the 'if' is the half that is the run-on continuation */
		/* $Req: artf1214 artf1094 $ */
		os_appmode = appmode;							/* Store application mode in which the OS was started */

		/* setup the tasks and alarms that are to be autostarted for the given app mode */													
		start_tasks(appmode);
		
		/* Initialize all counters in the system to start running */
		start_counters();
		
		/* Autostart alarms */
		if(appmode->start_alarms_singleton) {
			appmode->start_alarms_singleton(appmode);
		}

		if(appmode->start_alarms_multi) {
			appmode->start_alarms_multi(appmode);
		}
		
		/* Autostarted schedule tables are autostarted implicitly by the underlying alarms being autostarted */

#ifdef STACK_CHECKING	
		os_curtos = OS_KS_TOP;
#endif

		/* Call startup hook if required to do so */
		/* $Req: artf1215 $ */
		if (os_flags.startuphook) {						/* check if need to call the startuphook handler */
														/* hook needs calling */
			ENABLE_STACKCHECK();						/* enable stack checking */
			/* @todo check that we really need to raise IPL to lock out all interrupts (both CAT1 and CAT2) within the hook */			
			OS_SET_IPL_MAX();							/* raise IPL to lock out all interrupts (both CAT1 and CAT2) within the hook */
			MARK_OUT_KERNEL();							/* drop out of the kernel, keeping IPL at max level  */
			/* $Req: artf1117 $ */
			StartupHook();								/* call the hook routine with IPL set to kernel level and stack checking on */
			MARK_IN_KERNEL();							/* go back into the kernel */
			OS_SET_IPL_KERNEL();						/* set IPL back to kernel level */
			DISABLE_STACKCHECK();						/* disable stack checking */
		}

		/* Start the scheduling activity */
		if (TASK_SWITCH_PENDING(os_curpri)) {
			os_ks_dispatch();
		}
		
		ENABLE_STACKCHECK();
		LEAVE_KERNEL();

		/* now running as the body of the os_idle task, with IPL at level 0 and priority of IDLEPRI */
		/* $Req: artf1116 $ */
		/* Note that interrupts will be enabled prior to here because the os_ks_dispatch() call above
		 * might switch to running autostarted tasks, which will lower interrupts to level 0
		 */		
		for ( ; ; ) {
			os_idle();										/* call the os_idle task entry function */
		}
		
		NOT_REACHED();
	}
	else {
														/* This half of the 'if' is the half that is RETURNING from a longjmp */
														/* We have come back from a ShutdownOS() call */
	}
	
	/* Reached here because ShutdownOS(E_OK) was called.
	 * 
	 * $Req: artf1219 $
	 */
	assert(KERNEL_LOCKED());
	assert(STACKCHECK_OFF());							/* Stack checking turned off prior to longjmp in shutdown() */
		
	/* Would normally stop the counters, reinitialise the kernel variables, etc. in case StartOS() were to be called again,
	 * but OS424 (artf1375) requires that StartOS() doesn't return, but instead goes into an infinite loop.
	 * Similarly, OS425 (artf1376) requires that interrupts are disabled before entering the loop.
	 * 
	 * If StartOS() is ever required to return to caller, see revision 568 of this file (startos.c) for the
	 * original code that did this.
	 * 
	 * $Req: artf1375 $
	 * $Req: artf1376 $
	 */
	OS_SET_IPL_MAX();
	for(;;)
		;
}
