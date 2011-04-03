/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-12 02:35:49 +0000 (Tue, 12 Feb 2008) $
 * $LastChangedRevision: 617 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/test3/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test3. Tests API error conditions are met: tests that errors are generated under the right
 * conditions and that the calls do not generate errors under other conditions.
 */

#include <osapp.h>
#include <framework.h>

/*
 * Test of StatusType returns in Standard and Extended status to check for requirements
 * being met.
 */
void define_disabled_int_api_calls(void)
{
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_ActivateTask, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_TerminateTask, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_ChainTask, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_Schedule, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_GetResource, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_ReleaseResource, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_ClearEvent, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_SetEvent, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_GetEvent, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_WaitEvent, "TaskC");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_GetTaskState, "TaskC");
	/* @todo GetTaskID() maybe should return E_OS_DISABLEDINT too, but our implementation doesn't mind it called with interrupts locked */
	/* @todo GetISRID() maybe should call error hook with E_OS_DISABLEDINT too, but our implementation doesn't mind */
#endif
}

/* Called from TaskC with interrupts disabled. All calls should return E_OS_DISABLEDINT */
void disabled_int_all_api_calls(void)
{
	EventMaskType event;
	TaskStateType state;
	TaskType task;
	ISRType isr;
	StatusType rc;
#ifdef OS_EXTENDED_STATUS
	rc = ActivateTask(TaskA); TEST(rc, E_OS_DISABLEDINT); TESTP(param_TaskType, TaskA);
	rc = TerminateTask(); TEST(rc, E_OS_DISABLEDINT);
	rc = ChainTask(TaskA); TEST(rc, E_OS_DISABLEDINT); TESTP(param_TaskType, TaskA);
	rc = Schedule(); TEST(rc, E_OS_DISABLEDINT);
	rc = GetResource(ResD); TEST(rc, E_OS_DISABLEDINT); TESTP(param_ResourceType, ResD);
	rc = ReleaseResource(ResD); TEST(rc, E_OS_DISABLEDINT); TESTP(param_ResourceType, ResD);
	rc = ClearEvent(E2); TEST(rc, E_OS_DISABLEDINT); TESTP(param_EventMaskType, E2);
	rc = SetEvent(TaskD, E1); TEST(rc, E_OS_DISABLEDINT); TESTP(param_TaskType, TaskD); TESTP(param_EventMaskType, E1);
	rc = GetEvent(TaskD, &event); TEST(rc, E_OS_DISABLEDINT); TESTP(param_TaskType, TaskD); TESTP(param_EventMaskRefType, &event);
	rc = WaitEvent(E2); TEST(rc, E_OS_DISABLEDINT); TESTP(param_EventMaskType, E2);
	rc = GetTaskState(TaskA, &state); TEST(rc, E_OS_DISABLEDINT); TESTP(param_TaskType, TaskA); TESTP(param_TaskStateRefType, &state);
	/* Could do:
	 *   ShutdownOS(E_OK);
	 * but this will reset the processor and stop the test. This is tested in a different
	 * test and uses the debugger manually to check for a reset.
	 */
#endif
}

/* Test of obsolete declarational elements from OSEK OS */
DeclareTask(ATask);				/* $Req: artf1231 */
DeclareEvent(AnEvent);			/* $Req: artf1233 */
DeclareResource(AResource);		/* $Req: artf1232 */
DeclareAlarm(AnAlarm);			/* $Req: artf1234 */

/***************************************************************************************************
 *                                                                                                 *
 * PRE-TEST SETUP OF EXPECTED RESULTS                                                              *
 *                                                                                                 *
 ***************************************************************************************************/
int do_test() {
	init_testevents();

	DEFINE_TESTEVENT("Before StartOS");
#ifdef USESTARTUPHOOK
	DEFINE_TESTEVENT("StartupHook");
#endif
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("TaskA");
#endif
	DEFINE_TESTEVENT("Start A");				/* Task A is autostarted */
	DEFINE_TESTEVENT("ActivateTask()");
	define_error_hook_call(E_OS_LIMIT, OSServiceId_ActivateTask, "TaskA");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ID, OSServiceId_ActivateTask, "TaskA");
#endif
	DEFINE_TESTEVENT("ChainTask()");
	define_error_hook_call(E_OS_LIMIT, OSServiceId_ChainTask, "TaskA");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ID, OSServiceId_ChainTask, "TaskA");
	define_error_hook_call(E_OS_RESOURCE, OSServiceId_ChainTask, "TaskA");
	define_error_hook_call(E_OS_RESOURCE, OSServiceId_Schedule, "TaskA");
#endif
	
	DEFINE_TESTEVENT("End A");
	define_task_switch(TaskA, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("E_OS_DISABLEDINT test");
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	
	DEFINE_TESTEVENT("Activate D");				/* $Req: artf1129 $ */
	define_task_switch(TaskC, TaskD);
	DEFINE_TESTEVENT("Start D");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetEvent, "TaskD");
	define_error_hook_call(E_OS_ID, OSServiceId_GetEvent, "TaskD");
	define_error_hook_call(E_OS_STATE, OSServiceId_GetEvent, "TaskD");
	
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_ReleaseResource, "TaskD");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetResource, "TaskD");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetResource, "TaskD");
	define_error_hook_call(E_OS_RESOURCE, OSServiceId_WaitEvent, "TaskD");
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_ReleaseResource, "TaskD");
#endif
	DEFINE_TESTEVENT("Wait D (1)");
	DEFINE_TESTEVENT("Wait D (2)");
	DEFINE_TESTEVENT("Wait D (3)");
	
	define_task_switch(TaskD, TaskC);
	DEFINE_TESTEVENT("SetEvent to TaskD (1)");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ID, OSServiceId_SetEvent, "TaskC");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_SetEvent, "TaskC");
#endif
	DEFINE_TESTEVENT("SetEvent to TaskD (2)");
	DEFINE_TESTEVENT("SetEvent to TaskD (3)");
	define_task_switch(TaskC, TaskD);
	DEFINE_TESTEVENT("End D");
	define_error_hook_call(E_OS_MISSINGEND, OSServiceId_TerminateTask, "TaskD");

	define_task_switch(TaskD, TaskC);
	DEFINE_TESTEVENT("TerminateTask()");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_RESOURCE, OSServiceId_TerminateTask, "TaskC");
#endif	
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("GetTaskState()");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ID, OSServiceId_GetTaskState, "TaskB");
	define_error_hook_call(E_OS_ID, OSServiceId_GetResource, "TaskB");
	define_error_hook_call(E_OS_ID, OSServiceId_ReleaseResource, "TaskB");
#endif
	DEFINE_TESTEVENT("ISRX starting");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_CALLEVEL, OSServiceId_TerminateTask, "ISRX");
	define_error_hook_call(E_OS_CALLEVEL, OSServiceId_ChainTask, "ISRX");
	define_error_hook_call(E_OS_CALLEVEL, OSServiceId_Schedule, "ISRX");
	define_error_hook_call(E_OS_CALLEVEL, OSServiceId_WaitEvent, "ISRX");
	define_error_hook_call(E_OS_CALLEVEL, OSServiceId_ClearEvent, "ISRX");
#endif
	DEFINE_TESTEVENT("ISRX finishing");
	
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ACCESS, OSServiceId_ClearEvent, "TaskB");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_WaitEvent, "TaskB");
#endif
	DEFINE_TESTEVENT("Activate D 2nd");
	define_task_switch(TaskB, TaskD);
	DEFINE_TESTEVENT("Start D");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetEvent, "TaskD");
	define_error_hook_call(E_OS_ID, OSServiceId_GetEvent, "TaskD");
	define_error_hook_call(E_OS_STATE, OSServiceId_GetEvent, "TaskD");
	
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_ReleaseResource, "TaskD");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetResource, "TaskD");
	define_error_hook_call(E_OS_ACCESS, OSServiceId_GetResource, "TaskD");
	define_error_hook_call(E_OS_RESOURCE, OSServiceId_WaitEvent, "TaskD");
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_ReleaseResource, "TaskD");
#endif
	DEFINE_TESTEVENT("Wait D (1)");
	DEFINE_TESTEVENT("Wait D (2)");
	DEFINE_TESTEVENT("Wait D (3)");	
	define_task_switch(TaskD, TaskB);
	
	DEFINE_TESTEVENT("End B");
	
	/* No call to PostTaskHook() when shutting down */
	/* $Req: artf1043 $ */
	DEFINE_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	DEFINE_TESTEVENT("ShutdownHook");
#endif
	DEFINE_TESTEVENT("After StartOS");
	
	/************************BEGIN TEST AND CHECK AGAINST EXPECTED RESULTS**************************/
	if(E_OK != 0) {						/* $Req: artf1122 $ */
		test_failed(OS_HERE);
	}
	
	SET_TESTEVENT("Before StartOS");
	StartOS(OSDEFAULTAPPMODE);
	/* Req: artf1216 $ */
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
	
	SET_TESTEVENT("Start A");
	/*********** Check error codes have right values
	 * 
	 * $Req: artf1123 $
	 */
	TEST(E_OS_ACCESS, 1);
	TEST(E_OS_CALLEVEL, 2);
	TEST(E_OS_ID, 3);
	TEST(E_OS_LIMIT, 4);
	TEST(E_OS_NOFUNC, 5);
	TEST(E_OS_RESOURCE, 6);
	TEST(E_OS_STATE, 7);
	TEST(E_OS_VALUE, 8);
	TEST(E_OK, 0);
	
	/*********** GetActiveApplicationMode *********
	 * 
	 * Checks that StartOS() operated with the correct application mode.
	 * 
	 * $Req: artf1214 $
	 * $Req: artf1213 $
	 */
	TEST(GetActiveApplicationMode(), OSDEFAULTAPPMODE);
	
	/*********** ActivateTask() ***********/
	SET_TESTEVENT("ActivateTask()");
	/* Check that TaskB,  TaskC and TaskD are SUSPENDED (they are not autoactivated) */
	rc = GetTaskState(TaskB, &state);		/* $Req: artf1130 $ */
	TEST(state, SUSPENDED);					/* $Req: artf1141 $ */
	TEST(rc, E_OK);
	
	rc = GetTaskState(TaskC, &state);
	TEST(state, SUSPENDED);
	TEST(rc, E_OK);
	
	rc = GetTaskState(TaskD, &state);
	TEST(state, SUSPENDED);
	TEST(rc, E_OK);
	
	rc = ActivateTask(TaskB);			/* No immediate switch to a lower priority task; $Req: artf1129 $ (verified by trace log) */
	TEST(rc, E_OK);						/* $Req: artf1125 $ */
	
	rc = GetTaskState(TaskB, &state);
	TEST(state, READY);					/* TaskB is lower priority and is transferred into the READY state */
	TEST(rc, E_OK);
	
	rc = ActivateTask(TaskB);			/* Activate it again: should work since count limit is 2 */
	TEST(rc, E_OK);
	
	rc = ActivateTask(TaskB);			/* Should fail because the activation count for TaskB is 2 */
	TEST(rc, E_OS_LIMIT);				/* $Req: artf1127 $ */
	TESTP(param_TaskType, TaskB);
	
#ifdef OS_EXTENDED_STATUS
	rc = ActivateTask(0);				/* Passing the wrong handle should cause a failure */
	TEST(rc, E_OS_ID);					/* $Req: artf1128 $ */
	TESTP(param_TaskType, 0);
#endif
	
	/*********** ChainTask() ***********/
	SET_TESTEVENT("ChainTask()");
	
	rc = ChainTask(TaskB);				/* Should fail because TaskB should already be at its limit */
	TEST(rc, E_OS_LIMIT);				/* $Req: artf1134 $ */
	TESTP(param_TaskType, TaskB);
	
#ifdef OS_EXTENDED_STATUS
	rc = ChainTask(0);					/* Should fail because passing the wrong handle should cause a failure */
	TEST(rc, E_OS_ID);					/* $Req: artf1134 $ */
	TESTP(param_TaskType, 0);
	
	rc = GetResource(ResA);
	TEST(rc, E_OK);

	rc = ChainTask(TaskC);				/* Should fail: Trying to chain (i.e. terminate) while resources are locked should fail */
	TEST(rc, E_OS_RESOURCE);			/* $Req: artf1134 $ */
	TESTP(param_TaskType, TaskC);
	
	rc = Schedule();					/* Should fail: Trying to release the CPU while resources are locked should fail */
	TEST(rc, E_OS_RESOURCE);			/* $Req: artf1138 $ */

	rc = ReleaseResource(ResA);	
	TEST(rc, E_OK);
#endif
	SET_TESTEVENT("End A");

	/* Should run TaskC, which should return to TaskB when it is finished */
	rc = ChainTask(TaskC);				/* $Req: artf1134 $ */

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
	ISRType isr;
	TaskStateType state;
	
	SET_TESTEVENT("Start B");

	/*********** GetTaskState() ***********/
	SET_TESTEVENT("GetTaskState()");

	/* Check that TaskA,  TaskC and TaskD are SUSPENDED */
	rc = GetTaskState(TaskA, &state);		/* $Req: artf1130 $ */
	TEST(state, SUSPENDED);
	TEST(rc, E_OK);
	
	rc = GetTaskState(TaskC, &state);
	TEST(state, SUSPENDED);
	TEST(rc, E_OK);
	
	rc = GetTaskState(TaskD, &state);
	TEST(state, SUSPENDED);
	TEST(rc, E_OK);
	
	/* Check that this task is RUNNING */
	rc = GetTaskState(TaskB, &state);		/* $Req: artf1141 $ */
	TEST(state, RUNNING);
	TEST(rc, E_OK);

#ifdef OS_EXTENDED_STATUS
	/* Check that ID checks work */
	rc = GetTaskState(0, &state);			/* $Req: artf1142 $ */
	TEST(state, INVALID_TASK);
	TEST(rc, E_OS_ID);
	TESTP(param_TaskType, 0);
	TESTP(param_TaskStateRefType, &state);
#endif

	/********** GetResource() **********/
#ifdef OS_EXTENDED_STATUS
	/* Check that resource ID checks work on resources */
	rc = GetResource(0);					/* Should fail on ID check */
	TEST(rc, E_OS_ID);						/* $Req: artf1155 $ */
	TESTP(param_ResourceType, 0);
	
	rc = ReleaseResource(0);				/* Should fail on ID check */
	TEST(rc, E_OS_ID);						/* $Req: artf1326 $ */
	TESTP(param_ResourceType, 0);
#endif
	
	/********** Starting ISR **********/
	/* $Req: artf1116 $ */
	testing_trigger_isr();
	
	/********** Event API call failures *********/
#ifdef OS_EXTENDED_STATUS
	rc = ClearEvent(E1);					/* Should fail because caller is basic task */
	TEST(rc, E_OS_ACCESS);					/* $Req: artf1169 $ */
	TESTP(param_EventMaskType, E1);

	rc = WaitEvent(E1);						/* Should fail because caller is basic task */
	TEST(rc, E_OS_ACCESS);					/* $Req; artf1180 $ */
	TESTP(param_EventMaskType, E1);
#endif
	
	isr = GetISRID();						/* Should return INVALID_ISR because caller is a task */
	TEST(isr, INVALID_ISR);					/* $Req: artf1322 $ */
	
	/* Run D one more time to test that events are properly cleared */
	SET_TESTEVENT("Activate D 2nd");
	ActivateTask(TaskD);
	
	/* D is now waiting, but the test will finish anyway */
	
	/********** Finishing up tests **********/
	
	/* Even though B is activated twice, it will only run once because it shuts down the OS on
	 * the first run.
	 */
	SET_TESTEVENT("End B");
	SET_TESTEVENT("Calling ShutdownOS");
	ShutdownOS(E_OK);
	
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *                                                                                                 *
 * TaskC                                                                                           *
 *                                                                                                 *                                                                                         *
 *	TASK TaskC {                                                                                   *
 *		PRIORITY = 2;                                                                              *
 *		AUTOSTART = FALSE;                                                                         *
 *		ACTIVATION = 1;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		EVENT = Dummy;                                                                             *
 *		STACKSIZE = 180;                                                                           *
 *		RESOURCE = ResD;                                                                           *
 * 	};                                                                                             *
 ***************************************************************************************************/
TASK(TaskC)
{
	StatusType rc;
	TaskStateType state;
	
	SET_TESTEVENT("Start C");
	
	/* TaskA should now be SUSPENDED because it activated C via a ChainTask() */
	rc = GetTaskState(TaskA, &state);
	TEST(rc, E_OK);
	TEST(state, SUSPENDED);
	
	/********* Disabled interrupt API call checks **********/
	SET_TESTEVENT("E_OS_DISABLEDINT test");
	
	/* Code checks that E_OS_DISABLEDINT is returned from all API calls when
	 * DisableAllInterrupts(), SuspendAllInterrupts() or SuspendOSInterrupts() has been called.
	 * The test includes checks for the nested cases for the 'suspend' calls.
	 * 
	 * $Req: artf1045 $
	 */
	DisableAllInterrupts();
	disabled_int_all_api_calls();
	EnableAllInterrupts();
	
	SuspendAllInterrupts();
	disabled_int_all_api_calls();
	SuspendAllInterrupts();
	disabled_int_all_api_calls();		
	ResumeAllInterrupts();
	disabled_int_all_api_calls();	
	ResumeAllInterrupts();
	
	SuspendOSInterrupts();
	disabled_int_all_api_calls();
	SuspendOSInterrupts();
	disabled_int_all_api_calls();
	ResumeOSInterrupts();
	disabled_int_all_api_calls();
	ResumeOSInterrupts();	
	
	SET_TESTEVENT("Activate D");
	
	/* Kick off TaskD, which should immediately block on an event and return */
	/* Immediate switch to a lower priority task; $Req: artf1129 $ (verified by trace log) */
	rc = ActivateTask(TaskD);
	TEST(rc, E_OK);
	
	/* Check that TaskD is WAITING; $Req: artf1178 $ */
	rc = GetTaskState(TaskD, &state);
	TEST(state, WAITING);
	TEST(rc, E_OK);

	/*********** SetEvent() ************/
	SET_TESTEVENT("SetEvent to TaskD (1)");

#ifdef OS_EXTENDED_STATUS
	rc = SetEvent(0, E1);				/* Should fail because task handle is invalid */
	TEST(rc, E_OS_ID);					/* $Req: artf1165 $ */
	TESTP(param_EventMaskType, E1);
	TESTP(param_TaskType, 0);

	rc = SetEvent(TaskB, E1);			/* TaskB is a basic task so call should fail */
	TEST(rc, E_OS_ACCESS);				/* $Req: artf1166 $ */
	TESTP(param_EventMaskType, E1);
	TESTP(param_TaskType, TaskB);
#endif
	SET_TESTEVENT("SetEvent to TaskD (2)");
	/* Check SetEvent() wakes up TaskD appropriately.
	 * 
	 * $Req: artf1163 $
	 */
	rc = SetEvent(TaskD, E3);			/* Should fail to wake up TaskD because TaskD is waiting only on E1 */
	TEST(rc, E_OK);
	
	SET_TESTEVENT("SetEvent to TaskD (3)");
	/* $Req: artf1097 $ case 5a: event setting (see case 5b in Test6 for via alarm expiry) */
	rc = SetEvent(TaskD, E1 | E3);		/* Should succeed in waking up TaskD */
	TEST(rc, E_OK);						/* $Req: artf1164 $ */

	/* Note that TaskD terminated without calling TerminateTask() and thus an E_OS_MISSINGEND call
	 * should be made. Furthermore, it disabled interrupts. The kernel should have re-enabled these
	 * on return to this task.
	 * 
	 * $Req: artf1042 $
	 * $Req: artf1049 $
	 */
	/* Should succeed without E_OS_DISABLEDINT or E_OS_ACCESS */
	rc = GetResource(ResD);
	TEST(rc, E_OK);
	rc = GetResource(ResD_inner);		
	TEST(rc, E_OK);
	rc = ReleaseResource(ResD_inner);
	TEST(rc, E_OK);
	rc = ReleaseResource(ResD);
	TEST(rc, E_OK);
	
	/*********** TerminateTask() ***********/
	SET_TESTEVENT("TerminateTask()");
#ifdef OS_EXTENDED_STATUS
	rc = GetResource(ResD);				/* Get a resource shared with D */
	TEST(rc, E_OK);
	
	rc = TerminateTask();				/* Try to terminate; should fail */
	TEST(rc, E_OS_RESOURCE);			/* $Req: artf1132 $ */

	rc = ReleaseResource(ResD);			/* Release the reource so we can terminate */
	TEST(rc, E_OK);
#endif
	
	SET_TESTEVENT("End C");
	
	TerminateTask();
	
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *                                                                                                 *
 * TaskD                                                                                           *
 *                                                                                                 *
 *  TASK TaskD {                                                                                    *
 * 		PRIORITY = 3;                                                                              *
 *		AUTOSTART = FALSE;                                                                         *
 *		ACTIVATION = 1;                                                                            *
 *		SCHEDULE = FULL;                                                                           *
 *		EVENT = E1;                                                                                *
 *		STACKSIZE = 180;                                                                           *
 *		RESOURCE = ResD;                                                                           *
 *		RESOURCE = ResD_inner;                                                                     *
 *  };                                                                                             *
 ***************************************************************************************************/
TASK(TaskD)
{
	StatusType rc;
	EventMaskType mask;
	TaskType task;
	
	SET_TESTEVENT("Start D");
	/* Check that events are clear on the way in: should have been cleared by an ActivateTask() call.
	 * 
	 * $Req: artf1126 $
	 * $Req: artf1171 $
	 */
	rc = GetEvent(TaskD, &mask);
	TEST(rc, E_OK);
	TEST(mask, 0);
	
	/*********** GetEvent **************/
	/* Check that GetEvent() returns E_OS_STATE when referenced task is SUSPENDED.
	 * Check that GetEvent() returns E_OS_ACCESS when the referenced task is not an extended task.
	 * Check that GetEvent() returns E_OS_ID when the task ID is invalid.
	 * 
	 * $Req: artf1173 $
	 * $Req: artf1174 $
	 * $Req: artf1175 $
	 * $Req: artf1176 $
	 */
#ifdef OS_EXTENDED_STATUS
	rc = GetEvent(TaskA, &mask);		/* TaskA is a basic task */
	TEST(rc, E_OS_ACCESS);
	TESTP(param_TaskType, TaskA);
	TESTP(param_EventMaskRefType, &mask);
	rc = GetEvent(0, &mask);
	TEST(rc, E_OS_ID);
	TESTP(param_TaskType, 0);
	TESTP(param_EventMaskRefType, &mask);
	rc = GetEvent(TaskE, &mask);		/* Extended task TaskE is never activated so always SUSPENDED */
	TEST(rc, E_OS_STATE);
	TESTP(param_TaskType, TaskE);
	TESTP(param_EventMaskRefType, &mask);
#endif
	
	/*********** GetTaskID *************/
	rc = GetTaskID(&task);				/* Can call GetTaskID() from a task; $Req: artf1140 $ */
	TEST(rc, E_OK);
	TEST(task, TaskD);
	
	/*********** GetResource ***********/
#ifdef OS_EXTENDED_STATUS
	rc = ReleaseResource(ResD);			/* Can't release a resource that's not yet locked */
	TEST(rc, E_OS_NOFUNC);				/* $Req: artf1159 $ */
	TESTP(param_ResourceType, ResD);
	
	rc = GetResource(ResA);				/* Can't lock a resource that's not defined as being accessed by this task */
	TEST(rc, E_OS_ACCESS);				/* $Req: artf1154 $ */
	TESTP(param_ResourceType, ResA);
	
	rc = GetResource(ResD);
	TEST(rc, E_OK);
	
	rc = GetResource(ResD_inner);
	TEST(rc, E_OK);
	
	rc = GetResource(ResD);				/* Can't lock a resource twice */
	TEST(rc, E_OS_ACCESS);				/* $Req: artf1154 $ */
	TESTP(param_ResourceType, ResD);
	
	rc = WaitEvent(E1);					/* Can't wait while holding a resource */
	TEST(rc, E_OS_RESOURCE);			/* $Req: artf1182 $ */
	TESTP(param_EventMaskType, E1);

	rc = ReleaseResource(ResD);			/* Can't unlock ResD without unlocking ResD_inner first */
	TEST(rc, E_OS_NOFUNC);				/* $Req: artf1159 $ */
	TESTP(param_ResourceType, ResD);
	
	rc = ReleaseResource(ResD_inner);	/* Unlock both the resources now */
	TEST(rc, E_OK);
	
	rc = ReleaseResource(ResD);
	TEST(rc, E_OK);
	
#else /* Standard status */
	rc = GetResource(ResD);
	TEST(rc, E_OK);						/* $Req: artf1153 $ */
	
	rc = ReleaseResource(ResD);
	TEST(rc, E_OK);						/* $Req: artf1158 $ */
#endif
	
	/* Set the events to a known state */
	rc = ClearEvent(0);
	TEST(rc, E_OK);
	
	rc = SetEvent(TaskD, E1 | E3);
	TEST(rc, E_OK);
	
	/* Now try to wait on E3; should drop straight through without blocking. Then try
	 * to wait on event that hasn't occurred, and therefore block.
	 * 
	 * $Req: artf1177 $
	 * $Req: artf1179 $
	 */
	SET_TESTEVENT("Wait D (1)");
	rc = WaitEvent(E3);
	TEST(rc, E_OK);
	SET_TESTEVENT("Wait D (2)");
	
	rc = ClearEvent(E1);				/* Clear one event back out again */
	TEST(rc, E_OK);						/* $Req: artf1168 $ */
	
	SET_TESTEVENT("Wait D (3)");
	/* $Req: artf1097 $ case 4: waiting for event */
	rc = WaitEvent(E1);					/* Waiting for the cleared event should cause TaskD to block now; $Req: artf1178 $ */
	TEST(rc, E_OK);

	/* Check that specific events can be cleared
	 * 
	 * $Req: artf1167 $
	 * $Req: artf1171 $
	 */
	rc = GetEvent(TaskD, &mask);
	TEST(rc, E_OK);
	TEST(mask, E1 | E3);				/* Both events should be set (by TaskC) */
	
	rc = ClearEvent(E3);				/* Should clear down just E3 now */
	TEST(rc, E_OK);
	rc = GetEvent(TaskD, &mask);
	TEST(rc, E_OK);
	TEST(mask, E1);						/* E3 should no longer be set */
	
	/* Check that events are clear on the way in: set E3 on the way out before exiting in order
	 * to check that it s cleared down by an ActivateTask() call.
	 * 
	 * $Req: artf1126 $
	 */
	rc = SetEvent(TaskD, E3);
	TEST(rc, E_OK);
	
	SET_TESTEVENT("End D");

	/* Lock ResD and ResD_inner then fall off the end; the resources should be automatically
	 * released by the kernel.
	 * 
	 * $Req: artf1042 $
	 */
	GetResource(ResD);
	GetResource(ResD_inner);
	
	/* Disable all interrupts before falling off the end; the interrupts
	 * should be automatically enabled by the kernel and further API
	 * calls should proceed without error.
	 * 
	 * $Req: artf1049 $
	 */
	DisableAllInterrupts();
	SuspendAllInterrupts();
	SuspendOSInterrupts();
	
	/* Falls off end without calling TerminateTask() or ChainTask().
	 * Should trigger an E_OS_MISSINGEND error code and terminate the task.
	 * 
	 * $Req: artf1041 $
	 * $Req: artf1038 $
	 */
}

/* Task is never run: always in SUSPENDED state */
TASK(TaskE)
{
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
 *		RESOURCE = ResX;                                                                           *
 *		STACKSIZE = 180;                                                                           *
 *	};                                                                                             *
 ***************************************************************************************************/
ISR(ISRX)
{
	StatusType rc;
	TaskType task;
	ISRType isr;
	
	SET_TESTEVENT("ISRX starting");
	testing_dismiss_isr();
	
#ifdef OS_EXTENDED_STATUS
	rc = TerminateTask();				/* Can't call terminate from ISR */
	TEST(rc, E_OS_CALLEVEL);			/* $Req: artf1133 $ */
	
	rc = ChainTask(TaskB);				/* Can't call chain from ISR */
	TEST(rc, E_OS_CALLEVEL);			/* $Req: artf1134 $ */
	TESTP(param_TaskType, TaskB);
	
	rc = Schedule();					/* Can't call schedule from ISR */
	TEST(rc, E_OS_CALLEVEL);			/* $Req: artf1137 $ */
	
	rc = WaitEvent(E1);					/* Can't wait for events from ISR */
	TEST(rc, E_OS_CALLEVEL);			/* $Req: artf1181 $ */
	TESTP(param_EventMaskType, E1);
	
	rc = ClearEvent(E1);				/* Can't clear events from ISR */
	TEST(rc, E_OS_CALLEVEL);			/* $Req: artf1170 $ */
	TESTP(param_EventMaskType, E1);
#endif
	/* GetResource() and ReleaseResource() can be called from an ISR.
	 * 
	 * $Req: artf1161 $
	 * $Req: artf1152 $
	 */
	rc = GetResource(ResX);
	TEST(rc, E_OK);
	
	rc = ReleaseResource(ResX);
	TEST(rc, E_OK);
	
	isr = GetISRID();					/* Can call GetISRID() from an ISR */
	TEST(isr, ISRX);					/* Should be this ISR */

	rc = GetTaskID(&task);				/* Can call GetTaskID() from an ISR; $Req: artf1140 $ */
	TEST(rc, E_OK);						/* Should be TaskB, since that triggers this ISR */
	TEST(task, TaskB);
	
	SET_TESTEVENT("ISRX finishing");
}

int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
