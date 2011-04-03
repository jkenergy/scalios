/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-13 18:54:41 +0000 (Wed, 13 Feb 2008) $
 * $LastChangedRevision: 619 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test4/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test4. Tests priorities are handled properly.
 * 
 * API calls that can result in a task switch:
 * 
 *   *ExpireCounter()			// Note: extension to AUTOSAR/OSEK framework for non-tick counters
 *   *IncrementCounter()
 *   *SetEvent()
 *   *ReleaseResource()
 *   *ActivateTask()
 *   ChainTask()				// Note: switch not due to preemption
 *   TerminateTask()			// Note: switch not due to preemption
 *   Schedule()
 *   StartOS()
 * 
 * Also note that some of the above calls can be called from ISRs (marked *) and these should result in a task
 * switch when the ISR terminates (or when the nesting of ISRs finished if the ISR interrupted another ISR).
 * 
 * The test operates in several phases. The first phase examines resource locks and checks that preemption operates
 * correctly.
 * 
 */

#include <osapp.h>
#include <framework.h>

int first_run_TaskJ;

/***************************************************************************************************
 *                                                                                                 *
 * PRE-TEST SETUP OF EXPECTED RESULTS                                                              *
 *                                                                                                 *
 ***************************************************************************************************/
int do_test() {
	int i;

	init_testevents();
	first_run_TaskJ = 1;
	
	DEFINE_TESTEVENT("Before StartOS");
#ifdef USESTARTUPHOOK
	DEFINE_TESTEVENT("StartupHook");
#endif
	/* TaskA and TaskB are autostarted. TaskB is higher priority than TaskB so should run first */
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("TaskB");
#endif
	DEFINE_TESTEVENT("Start B");
	
	DEFINE_TESTEVENT("Unlock resource to switch to C");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (1)");
	
	DEFINE_TESTEVENT("Still in B (1)");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (2)");

	DEFINE_TESTEVENT("Still in B (2)");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (3)");
	
	DEFINE_TESTEVENT("Still in B (3)");
	DEFINE_TESTEVENT("Start X");
	DEFINE_TESTEVENT("End X");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (4)");
	
	DEFINE_TESTEVENT("Still in B (4)");
	if(cat1_interrupts_cat2()) {
		DEFINE_TESTEVENT("Running cat1");
	}
	DEFINE_TESTEVENT("Still in B (4a)");
	if(!cat1_interrupts_cat2()) {
		DEFINE_TESTEVENT("Running cat1");
	}
	DEFINE_TESTEVENT("Start X");
	DEFINE_TESTEVENT("End X");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (5)");
	
	DEFINE_TESTEVENT("Still in B (5)");
	DEFINE_TESTEVENT("Resuming OS interrupts");
	DEFINE_TESTEVENT("Resuming all interrupts");
	if(cat1_interrupts_cat2() || cat1_precedes_cat2()) {
		DEFINE_TESTEVENT("Running cat1");
		DEFINE_TESTEVENT("Start X");
		DEFINE_TESTEVENT("End X");
		define_task_switch(TaskB, TaskC);
	}
	else {
		DEFINE_TESTEVENT("Start X");
		DEFINE_TESTEVENT("End X");
		define_task_switch(TaskB, TaskC);
		DEFINE_TESTEVENT("Running cat1");
	}
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Back in B (6)");
	
	DEFINE_TESTEVENT("Still in B (6)");
	DEFINE_TESTEVENT("Resuming OS interrupts");
	DEFINE_TESTEVENT("Enabling all interrupts");
	if(cat1_interrupts_cat2() || cat1_precedes_cat2()) {
		DEFINE_TESTEVENT("Running cat1");
		DEFINE_TESTEVENT("Start X");
		DEFINE_TESTEVENT("End X");
		define_task_switch(TaskB, TaskC);
	}
	else {
		DEFINE_TESTEVENT("Start X");
		DEFINE_TESTEVENT("End X");
		define_task_switch(TaskB, TaskC);
		DEFINE_TESTEVENT("Running cat1");
	}
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);	
	DEFINE_TESTEVENT("Back in B (7)");
	
	DEFINE_TESTEVENT("End B");
	
	define_task_switch(TaskB, TaskJ);
	DEFINE_TESTEVENT("Start J");
	DEFINE_TESTEVENT("Chain J");
	define_task_switch(TaskJ, TaskI);
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
	define_task_switch(TaskI, TaskJ);
	define_task_switch(TaskJ, TaskJ);
	DEFINE_TESTEVENT("Start J");
	DEFINE_TESTEVENT("End J");
	define_task_switch(TaskJ, TaskA);

	DEFINE_TESTEVENT("Start A");
	DEFINE_TESTEVENT("Activating D");
	
	define_task_switch(TaskA, TaskD);
	DEFINE_TESTEVENT("Start D");
	DEFINE_TESTEVENT("Activate E");
	DEFINE_TESTEVENT("D continues");
	define_task_switch(TaskD, TaskE);
	DEFINE_TESTEVENT("Start E");
	DEFINE_TESTEVENT("End E");
	define_task_switch(TaskE, TaskD);
	DEFINE_TESTEVENT("Back in D");
	DEFINE_TESTEVENT("End D");
	define_task_switch(TaskD, TaskE);
	DEFINE_TESTEVENT("Start E");
	DEFINE_TESTEVENT("End E");
	define_task_switch(TaskE, TaskA);
	DEFINE_TESTEVENT("Activating F");
	define_task_switch(TaskA, TaskF);
	DEFINE_TESTEVENT("Start F");
	DEFINE_TESTEVENT("F continues");
	define_task_switch(TaskF, TaskE);
	DEFINE_TESTEVENT("Start E");
	DEFINE_TESTEVENT("End E");
	define_task_switch(TaskE, TaskF);
	DEFINE_TESTEVENT("End F");
	define_task_switch(TaskF, TaskA);
	DEFINE_TESTEVENT("Back in A (1)");

#ifndef OPTIMIZED_QUEUEING
	DEFINE_TESTEVENT("Activating G and H");
#endif
	DEFINE_TESTEVENT("Activating I");

	DEFINE_TESTEVENT("Letting go of scheduler");
	define_task_switch(TaskA, TaskI);
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
	define_task_switch(TaskI, TaskI);
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
	define_task_switch(TaskI, TaskI);
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
	define_task_switch(TaskI, TaskI);
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
	define_task_switch(TaskI, TaskI);		/* 5th run of TaskI */
	DEFINE_TESTEVENT("Start I");
	DEFINE_TESTEVENT("End I");
#ifndef OPTIMIZED_QUEUEING
	define_task_switch(TaskI, TaskG);		/* 1st run of TaskG */
	DEFINE_TESTEVENT("Start G");
	DEFINE_TESTEVENT("End G");
	define_task_switch(TaskG, TaskH);
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskG);		/* 2nd run of TaskG */
	DEFINE_TESTEVENT("Start G");
	DEFINE_TESTEVENT("End G");
	define_task_switch(TaskG, TaskH);		
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskH);
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskH);
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskH);
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskH);
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskH);		/* 7th run of TaskH */
	DEFINE_TESTEVENT("Start H");
	DEFINE_TESTEVENT("End H");
	define_task_switch(TaskH, TaskG);		/* 3rd run of TaskG */
	DEFINE_TESTEVENT("Start G");
	DEFINE_TESTEVENT("End G");
	define_task_switch(TaskG, TaskA);
#else /* non-optimized queueing */
	define_task_switch(TaskI, TaskA);
#endif
	
	DEFINE_TESTEVENT("Back in A (2)");

#ifndef OPTIMIZED_QUEUEING
	DEFINE_TESTEVENT("Bulk activations");
	for(i = 0; i < 7; i++) {
		DEFINE_TESTEVENT("Phase start");
		DEFINE_TESTEVENT("Activating G and H");
		DEFINE_TESTEVENT("Activating I");
		DEFINE_TESTEVENT("Activating C");
		
		define_task_switch(TaskA, TaskI);
		DEFINE_TESTEVENT("Start I");
		DEFINE_TESTEVENT("End I");
		define_task_switch(TaskI, TaskI);
		DEFINE_TESTEVENT("Start I");
		DEFINE_TESTEVENT("End I");
		define_task_switch(TaskI, TaskG);
		DEFINE_TESTEVENT("Start G");
		DEFINE_TESTEVENT("End G");
		define_task_switch(TaskG, TaskH);
		DEFINE_TESTEVENT("Start H");
		DEFINE_TESTEVENT("End H");
		define_task_switch(TaskH, TaskG);
		DEFINE_TESTEVENT("Start G");
		DEFINE_TESTEVENT("End G");
		define_task_switch(TaskG, TaskC);
		DEFINE_TESTEVENT("Start C");
		DEFINE_TESTEVENT("End C");		
		define_task_switch(TaskC, TaskA);
		
		DEFINE_TESTEVENT("Phase end");
	}
#endif
	
	DEFINE_TESTEVENT("End A");

	DEFINE_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	DEFINE_TESTEVENT("ShutdownHook");
#endif
	DEFINE_TESTEVENT("After StartOS");

#undef DEFINE_TESTEVENT
	
	/************************************************************************************************
	 *************************BEGIN TEST AND CHECK AGAINST EXPECTED RESULTS**************************
	 ************************************************************************************************
	 */
	SET_TESTEVENT("Before StartOS");
	StartOS(OSDEFAULTAPPMODE);
	/* Call returns after ShutDownOS() called.
	 * 
	 * $Req: artf1219 $
	 * $Req: artf1217 $
	 */
	SET_TESTEVENT("After StartOS");
	
	test_finished();
	return 0;
}

/***************************************************************************************************
 *                                                                                                 *
 * TaskA                                                                                           *
 *                                                                                                 *
 * 	TASK TaskA {                                                                                   *
 *		PRIORITY = 1;                                                                              *
 *		AUTOSTART = TRUE;                                                                          *
 *		ACTIVATION = 1;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		STACKSIZE = 220;                                                                           *
 *		RESOURCE = ResA;                                                                           *
 *	};                                                                                             *
 ***************************************************************************************************/
TASK(TaskA)
{	
	StatusType rc;
	TaskStateType state;
	int i;

	SET_TESTEVENT("Start A");
	
	SET_TESTEVENT("Activating D");
	/* $Req: artf1097 $ case 3a: task activation (see case 3b in Test 6 for activation via alarm expiry) */
	ActivateTask(TaskD);
	
	SET_TESTEVENT("Activating F");
	ActivateTask(TaskF);
	SET_TESTEVENT("Back in A (1)");

	/*********Test queued activations*********
	 * Tasks G and H are queued in a particular pattern. Should be dispatched in a FIFO
	 * order.
	 * 
	 * $Req: artf1096 $
	 * $Req: artf1088 $
	 * $Req: artf1086 $
	 * 
	 * Use of RES_SCHEDULER as priority as high as any task.
	 * 
	 * $Req: artf1100 $
	 */
	GetResource(RES_SCHEDULER);
#ifndef OPTIMIZED_QUEUEING
	SET_TESTEVENT("Activating G and H");
	ActivateTask(TaskG);
	ActivateTask(TaskH);
	ActivateTask(TaskG);
	ActivateTask(TaskH);
	ActivateTask(TaskH);
	ActivateTask(TaskH);
	ActivateTask(TaskH);
	ActivateTask(TaskH);
	ActivateTask(TaskH);
	ActivateTask(TaskG);
#endif
	SET_TESTEVENT("Activating I");
	ActivateTask(TaskI);
	ActivateTask(TaskI);
	ActivateTask(TaskI);
	ActivateTask(TaskI);
	ActivateTask(TaskI);

#ifndef OPTIMIZED_QUEUEING
	GetTaskState(TaskG, &state);
	TEST(state, READY);
	GetTaskState(TaskH, &state);
	TEST(state, READY);
#endif
	GetTaskState(TaskI, &state);
	TEST(state, READY);
	
	SET_TESTEVENT("Letting go of scheduler");
	
	ReleaseResource(RES_SCHEDULER);
	SET_TESTEVENT("Back in A (2)");

#ifndef OPTIMIZED_QUEUEING
	GetTaskState(TaskG, &state);
	TEST(state, SUSPENDED);
	GetTaskState(TaskH, &state);
	TEST(state, SUSPENDED);
#endif
	GetTaskState(TaskI, &state);
	TEST(state, SUSPENDED);

#ifndef OPTIMIZED_QUEUEING
	SET_TESTEVENT("Bulk activations");
	for(i = 0; i < 7; i++) {
		SET_TESTEVENT("Phase start");
		GetResource(RES_SCHEDULER);
		SET_TESTEVENT("Activating G and H");
		ActivateTask(TaskG);
		ActivateTask(TaskH);
		ActivateTask(TaskG);
		SET_TESTEVENT("Activating I");
		ActivateTask(TaskI);
		ActivateTask(TaskI);
		SET_TESTEVENT("Activating C");
		ActivateTask(TaskC);
		ReleaseResource(RES_SCHEDULER);
		SET_TESTEVENT("Phase end");
	}
#endif
	
	
	/***********Finish and shutdown to return to main()************/
	SET_TESTEVENT("End A");
	
	SET_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	SET_TESTEVENT("ShutdownHook");
#endif

	/* Shut down the OS, passing a code to the shutdown hook.
	 * 
	 * $Req: artf1218 $
	 */
	shutdown_code = E_OK;
	test_shutdown_code = 1U;
	ShutdownOS(shutdown_code);
	
	test_failed(OS_HERE);
}


/***************************************************************************************************
 *                                                                                                 *
 * TaskB                                                                                           *
 *                                                                                                 *
 * 	TASK TaskB {                                                                                   *
 *		PRIORITY = 0;                                                                              *
 *		AUTOSTART = FALSE;                                                                         *
 *		ACTIVATION = 2;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		STACKSIZE = 180;                                                                           *
 *	};                                                                                             *
 ***************************************************************************************************/
TASK(TaskB)
{
	StatusType rc;
	TaskStateType state;
	
	/************** STARTING POINT IN TEST **************/
	
	SET_TESTEVENT("Start B");
	/* At this point TaskA is READY (i.e. autostarted) and TaskB is RUNNING. Task C is SUSPENDED. */
	GetTaskState(TaskA, &state);
	/* No need to check error return: it should be OK. If it's not there will be an unexpected call
	 * to ErrorHook(), which will be logged. The trace will not match with the expected trace and
	 * the test will (correctly) fail.
	 */
	TEST(state, READY);
	
	GetTaskState(TaskB, &state);
	TEST(state, RUNNING);
	
	GetTaskState(TaskC, &state);
	TEST(state, SUSPENDED);
	
	/* Lock a resource with ceiling of C, Activate TaskC, make sure no switch occurs until unlocking resource.
	 * 
	 * $Req: artf1151 $
	 */
	GetResource(Res_ceilC);
	ActivateTask(TaskC);
	SET_TESTEVENT("Unlock resource to switch to C");
	/* $Req: artf1097 $ case 6: release a resource with a higher priority task pending */
	ReleaseResource(Res_ceilC);
	SET_TESTEVENT("Back in B (1)");

	/* Lock a nested resource, with the inner having a lower ceiling than the outer. The priority should not
	 * be lowered and no task switch should take place until the outer resource is released.
	 * 
	 * $Req: artf1156 $
	 * $Req: artf1157 $
	 * $Req: artf1151 $
	 */
	GetResource(Res_ceilC);
	GetResource(Res_ceilB);
	ActivateTask(TaskC);
	ReleaseResource(Res_ceilB);
	SET_TESTEVENT("Still in B (1)");
	ReleaseResource(Res_ceilC);
	SET_TESTEVENT("Back in B (2)");
	
	/* Lock a nested resource, with the inner having a higher ceiling than the outer. The priority should not
	 * be lowered below the ceiling of the outer resource. No task switch should take place until the outer resource is released.
	 * 
	 * $Req: artf1151 $
	 */
	GetResource(Res_ceilC);
	ActivateTask(TaskC);				/* <--- this activate won't cause a task switch until... */
	GetResource(Res_ceilD_linked);
	ReleaseResource(Res_ceilD_linked);
	SET_TESTEVENT("Still in B (2)");
	ReleaseResource(Res_ceilC);			/* <--- ... here */
	SET_TESTEVENT("Back in B (3)");
	
	/* Lock a resource shared with ISRX, then kick off ISRX
	 * 
	 * $Req: artf1101 $
	 * $Req: artf1151 $
	 */
	GetResource(Res_ceilX);
	testing_trigger_isr();				/* <--- this call won't cause the ISR to run  until... */
	SET_TESTEVENT("Still in B (3)");
	ReleaseResource(Res_ceilX);			/* <--- ... here */
	SET_TESTEVENT("Back in B (4)");
	
	/* Test that SuspendOSInterrupts() locks out ISRX but not the cat 1 handler. Also check that it
	 * nests properly.
	 * 
	 * $Req: artf1148 $
	 */
	SuspendOSInterrupts();
	SuspendOSInterrupts();
	testing_trigger_isr();				/* This call won't cause the ISR to run  */
	SET_TESTEVENT("Still in B (4)");
	testing_trigger_cat1_isr();			/* This WILL cause the cat1 ISR to run if cat 1 interrupts are higher than OS level */
	
	ResumeOSInterrupts();
	SET_TESTEVENT("Still in B (4a)");
	/* ISRX will run after the following call, activate TaskC, which will be dispatched, run, and eventually
	 * thread of execution will return here.
	 * 
	 * $Req: artf1097 $ case 7: dispatch on cat2 ISR termination
	 */
	ResumeOSInterrupts();
	SET_TESTEVENT("Back in B (5)");

	/* Check that ResumeOSInterrupts() and ResumeAllInterrupts() without a corresponding suspend call are null operations.
	 * 
	 * $Req: artf1044 $
	 * 
	 * Check that all interrupts are locked out by SuspendAllInterrupts() and that it nests properly.
	 * Also check that SuspendOSInterrupts() does not lower the IPL.
	 * 
	 * $Req: artf1148 $
	 * $Req: artf1144 $
	 * $Req: artf1146 $
	 * $Req: artf1149 $
	 */
	ResumeAllInterrupts();
	ResumeOSInterrupts();
	SuspendAllInterrupts();
	testing_trigger_isr();				/* This call won't cause the ISR to run  */
	testing_trigger_cat1_isr();			/* This call won't cause the Category 1 ISR to run now */
	SuspendOSInterrupts();				/* This should not lower interrupts to OS level; cat1 will still not run */
	SuspendAllInterrupts();
	SET_TESTEVENT("Still in B (5)");
	ResumeAllInterrupts();
	SET_TESTEVENT("Resuming OS interrupts");
	ResumeOSInterrupts();				/* The cat1 handler will still not run here */
	SET_TESTEVENT("Resuming all interrupts");
	ResumeAllInterrupts();				/* The cat1 handler and then ISRX will run here */
	SET_TESTEVENT("Back in B (6)");
	
	/* Check that EnableAllInterrupts() without a corresponding DisableAllInterrupts() is a null operation.
	 * 
	 * $Req: artf1044 $
	 * 
	 * Check that DisableAllInterrupts() locks out both ISRX and cat1 handlers.
	 * 
	 * $Req: artf1145 $
	 * $Req: artf1147 $
	 * $Req: artf1149 $
	 * 
	 * Check that SuspendOSInterrupt() does not lower the IPL.
	 */
	EnableAllInterrupts();
	DisableAllInterrupts();
	testing_trigger_isr();				/* This call won't cause the ISR to run  */
	testing_trigger_cat1_isr();			/* This call won't cause the Category 1 ISR to run */
	SuspendOSInterrupts();				/* This should not lower interrupts to OS level; cat1 will still not run */
	SET_TESTEVENT("Still in B (6)");
	SET_TESTEVENT("Resuming OS interrupts");
	ResumeOSInterrupts();				/* The cat 1 handler will still not run here */
	SET_TESTEVENT("Enabling all interrupts");
	EnableAllInterrupts();				/* The cat 1 handler then ISRX will run here */
	SET_TESTEVENT("Back in B (7)");
	
	SET_TESTEVENT("End B");

	TerminateTask();
	
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *                                                                                                 *
 * TaskC                                                                                           *
 *                                                                                                 *                                                                                         *
 *	TASK TaskC {                                                                                   *
 *		PRIORITY = 3;                                                                              *
 *		AUTOSTART = FALSE;                                                                         *
 *		ACTIVATION = 1;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		EVENT = <Dummy if we want to test with extended tasks only>;                               *
 *		STACKSIZE = 180;                                                                           *
 *		RESOURCE = Res_ceilC;                                                                      *
 * 	};                                                                                             *
 ***************************************************************************************************/
TASK(TaskC)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start C");
	/* This task does nothing except log it has run */
	SET_TESTEVENT("End C");

	TerminateTask();
	
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *                                                                                                 *
 * TaskD                                                                                           *
 *                                                                                                 *                                                                                         *
 *	TASK TaskD {                                                                                   *
 *		PRIORITY = 4;                                                                              *
 *		AUTOSTART = FALSE;                                                                         *
 *		ACTIVATION = 1;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		EVENT = <Dummy if we want to test with extended tasks only>;                               *
 *		STACKSIZE = 180;                                                                           *
 *		RESOURCE = Res_ceilD;                                                                      *
 * 	};                                                                                             *
 ***************************************************************************************************/
TASK(TaskD)
{
	StatusType rc;
	TaskStateType state;
	
	/* TaskD and TaskE are mutually non-preemptible due to shared internal resource. */
	SET_TESTEVENT("Start D");
	SET_TESTEVENT("Activate E");
	ActivateTask(TaskE);
	/* Will not run due to shared internal resource
	 * 
	 * $Req: artf1102 $
	 */
	SET_TESTEVENT("D continues");
	/* Temporarily release internal resource to let TaskE run, re-acquire the internal resource,
	 * activate TaskE again. TaskE is held over until TaskD terminates.
	 * 
	 * $Req: artf1136 $
	 * $Req: artf1131 $
	 */
	Schedule();									/* <--- this should cause a switch to running TaskE */
	SET_TESTEVENT("Back in D");
	ActivateTask(TaskE);
	SET_TESTEVENT("End D");

	TerminateTask();
	
	test_failed(OS_HERE);
}

TASK(TaskE)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start E");
	/* This task does nothing except log it has run */
	SET_TESTEVENT("End E");

	TerminateTask();
	
	test_failed(OS_HERE);
}

TASK(TaskF)
{
	StatusType rc;
	TaskStateType state;
	
	/* This is a non-preemptive task; no other task should pre-empt while this task is
	 * running, unless the Schedule() call is made.
	 * 
	 * $Req: artf1099 $
	 */
	SET_TESTEVENT("Start F");

	ActivateTask(TaskE);
	SET_TESTEVENT("F continues");
	Schedule();								/* <---- switch to TaskE then return here */
	
	SET_TESTEVENT("End F");

	TerminateTask();
	
	test_failed(OS_HERE);
}

#ifndef OPTIMIZED_QUEUEING
TASK(TaskG)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start G");
	/* This task does nothing except log it has run */
	SET_TESTEVENT("End G");

	TerminateTask();
	
	test_failed(OS_HERE);
}

TASK(TaskH)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start H");
	/* This task does nothing except log it has run */
	SET_TESTEVENT("End H");

	TerminateTask();
	
	test_failed(OS_HERE);
}
#endif

TASK(TaskI)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start I");
	/* This task does nothing except log it has run */
	SET_TESTEVENT("End I");

	TerminateTask();
	
	test_failed(OS_HERE);
}

TASK(TaskJ)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start J");
	/* This task does nothing except log it has run */

	if(first_run_TaskJ) {
		first_run_TaskJ = 0;
		/* Checks self-chaining works, even when activation count limit is 1.
		 * 
		 * $Req: artf1135 $
		 * 
		 * Also checks that internal resources are released: TaskI shares the
		 * same internal resource but will run.
		 */
		ActivateTask(TaskI);			/* No task switch will occur here */
		SET_TESTEVENT("Chain J");		
		/* This will let TaskI run (by releasing internal resources); TaskJ will run again
		 * 
		 * $Req: artf1097 $ case 2: chain
		 */
		ChainTask(TaskJ);				/* $Req: artf1134 $ */	
	}
	else {
		/* $Req: artf1097 $ case 1: terminate */
		SET_TESTEVENT("End J");
		TerminateTask();
	}
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *                                                                                                 *
 * ISRX                                                                                            *
 *                                                                                                 *
 * 	ISR ISRX {                                                                                     *
 *		VECTOR = "INT0";                                                                           *
 *		CATEGORY = 2;                                                                              *
 *		PRIORITY = 1;                                                                              *
 *		RESOURCE = Res_ceilX;                                                                      *
 *		STACKSIZE = 180;                                                                           *
 *	};                                                                                             *
 ***************************************************************************************************/
ISR(ISRX)
{
	StatusType rc;
	ISRType isr;
	
	SET_TESTEVENT("Start X");
	testing_dismiss_isr();
	ActivateTask(TaskC);
	SET_TESTEVENT("End X");

}

int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
