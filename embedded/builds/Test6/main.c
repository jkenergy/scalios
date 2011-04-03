/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-11 22:36:26 +0000 (Tue, 11 Mar 2008) $
 * $LastChangedRevision: 671 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test6/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test6. Tests counter and alarm API.
 */

#include <osapp.h>
#include <framework.h>


/*****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 ****** 
 ******    P R E - T E S T    S E T U P    O F    E X P E C T E D    R E S U L T S
 ******
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 */

/* Test does not work with pre/post task hooks enabled: too many events dropped into log */
#if defined(USEPRETASKHOOK) || defined(USEPOSTTASKHOOK)
#error "Test will not work with pre/post task hooks enabled"
#endif 

/*
 * Test of StatusType returns in Standard and Extended status to check for requirements
 * being met. Function called by TaskA.
 */
void define_disabled_int_api_calls(void)
{
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_SetRelAlarm, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_SetAbsAlarm, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_CancelAlarm, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_GetAlarm, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_GetAlarmBase, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_IncrementCounter, "TaskA");
	define_error_hook_call(E_OS_DISABLEDINT, OSServiceId_ExpireCounter, "TaskA");
#endif
}

int do_test() {
	init_testevents();

	/* Subtest 0. */
	DEFINE_TESTEVENT("Fast interrupt macros");
	DEFINE_TESTEVENT("Disabling all interrupts");
	DEFINE_TESTEVENT("Enabling all interrupts");
	
	DEFINE_TESTEVENT("Running cat1");

	DEFINE_TESTEVENT("Suspending all interrupts");
	DEFINE_TESTEVENT("Resuming all interrupts");
	DEFINE_TESTEVENT("Running cat1");
	
	DEFINE_TESTEVENT("Nested suspending all interrupts");
	DEFINE_TESTEVENT("Resuming all interrupts - inner");
	DEFINE_TESTEVENT("Resuming all interrupts - outer");
	DEFINE_TESTEVENT("Running cat1");

	DEFINE_TESTEVENT("Prior enable of interrupts");
	DEFINE_TESTEVENT("Disabling all interrupts");
	DEFINE_TESTEVENT("Enabling all interrupts");
	DEFINE_TESTEVENT("Running cat1");

	DEFINE_TESTEVENT("Prior resume of interrupts");
	DEFINE_TESTEVENT("Nested suspending all interrupts");
	DEFINE_TESTEVENT("Resuming all interrupts - inner");
	DEFINE_TESTEVENT("Resuming all interrupts - outer");
	DEFINE_TESTEVENT("Running cat1");
	
	DEFINE_TESTEVENT("Before StartOS");
#ifdef USESTARTUPHOOK
	DEFINE_TESTEVENT("StartupHook");
#endif
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("TaskA");
#endif
	DEFINE_TESTEVENT("Start A");				/* Task A is autostarted */

	/***************************************************************************************************
	 *
	 * subtest 1.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("E_OS_DISABLEDINT test");
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	define_disabled_int_api_calls();
	
	/***************************************************************************************************
	 *
	 * subtest 2.
	 * 
	 ***************************************************************************************************/
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_ID, OSServiceId_SetRelAlarm, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_SetAbsAlarm, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_CancelAlarm, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_GetAlarm, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_GetAlarmBase, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_IncrementCounter, "TaskA");
	define_error_hook_call(E_OS_ID, OSServiceId_ExpireCounter, "TaskA");
#endif
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_GetAlarm, "TaskA");
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetRelAlarm, "TaskA");
	define_error_hook_call(E_OS_STATE, OSServiceId_SetRelAlarm, "TaskA");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetRelAlarm, "TaskA");
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetRelAlarm, "TaskA");
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetRelAlarm, "TaskA");
#endif
	define_error_hook_call(E_OS_STATE, OSServiceId_SetAbsAlarm, "TaskA");
#ifdef OS_EXTENDED_STATUS
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetAbsAlarm, "TaskA");
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetAbsAlarm, "TaskA");
	define_error_hook_call(E_OS_VALUE, OSServiceId_SetAbsAlarm, "TaskA");
#endif
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_CancelAlarm, "TaskA");
	
	/***************************************************************************************************
	 *
	 * subtest 2a.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 2b.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("ALARMCALLBACK(AlarmX2_SW_callback) start");
	DEFINE_TESTEVENT("ALARMCALLBACK(AlarmX2_SW_callback) end");
	
	/***************************************************************************************************
	 *
	 * subtest 3.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Software counter checks");

	DEFINE_TESTEVENT("1st IncrementCounter()");
	DEFINE_TESTEVENT("2nd IncrementCounter()");
	DEFINE_TESTEVENT("3rd IncrementCounter()");
	DEFINE_TESTEVENT("4th IncrementCounter()");
	DEFINE_TESTEVENT("5th IncrementCounter()");
	DEFINE_TESTEVENT("Start B1_SW");
	DEFINE_TESTEVENT("End B1_SW");
	DEFINE_TESTEVENT("6th IncrementCounter()");
	
	/***************************************************************************************************
	 *
	 * subtest 4.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("IncrementCounter() loop start");
	DEFINE_TESTEVENT("99th call to IncrementCounter()");
	DEFINE_TESTEVENT("100th call to IncrementCounter()");
	DEFINE_TESTEVENT("Start B2_SW");
	DEFINE_TESTEVENT("End B2_SW");
	DEFINE_TESTEVENT("Start B1_SW");
	DEFINE_TESTEVENT("End B1_SW");
	DEFINE_TESTEVENT("101st call to IncrementCounter()");
	DEFINE_TESTEVENT("109th call to IncrementCounter()");
	DEFINE_TESTEVENT("110th call to IncrementCounter()");
	DEFINE_TESTEVENT("Start B1_SW");
	DEFINE_TESTEVENT("End B1_SW");
	DEFINE_TESTEVENT("111th call to IncrementCounter()");
	DEFINE_TESTEVENT("IncrementCounter() loop end");

	/***************************************************************************************************
	 *
	 * subtest 4a.
	 * 
	 ***************************************************************************************************/	
	DEFINE_TESTEVENT("IncrementCounter() E_OK test begin");
	define_error_hook_call(E_OS_STATE, OSServiceId_SetEvent, "TaskA");
	DEFINE_TESTEVENT("IncrementCounter() E_OK test 2");
	define_error_hook_call(E_OS_LIMIT, OSServiceId_ActivateTask, "TaskA");
	DEFINE_TESTEVENT("IncrementCounter() E_OK test end");
	
	/***************************************************************************************************
	 *
	 * subtest 5.
	 * 
	 ***************************************************************************************************/	
	DEFINE_TESTEVENT("Begin alarm control");
	DEFINE_TESTEVENT("Done SetRelAlarm()");

	DEFINE_TESTEVENT("ISR Timer start");
	DEFINE_TESTEVENT("ISR Timer end");

	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");
	
	DEFINE_TESTEVENT("Periodic alarm");

	DEFINE_TESTEVENT("Periodic alarm about to expire (1st)");
	DEFINE_TESTEVENT("ISR Timer start");
	DEFINE_TESTEVENT("ISR Timer end");
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");
	
	DEFINE_TESTEVENT("Periodic alarm about to expire (2nd)");
	DEFINE_TESTEVENT("ISR Timer start");
	DEFINE_TESTEVENT("ISR Timer end");
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");

	DEFINE_TESTEVENT("Periodic alarm about to expire (3rd)");
	DEFINE_TESTEVENT("ISR Timer start");
	DEFINE_TESTEVENT("ISR Timer end");
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");

	DEFINE_TESTEVENT("Time passed after cancel");

#ifndef SINGLETON_ALARMCOUNTER
	/***************************************************************************************************
	 *
	 * subtest 6.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("TaskC periodic activation");
	DEFINE_TESTEVENT("TaskC one-shot activation via SetAbsAlarm()");
	define_error_hook_call(E_OS_NOFUNC, OSServiceId_GetAlarm, "TaskA");
	DEFINE_TESTEVENT("One-shot test end");

	/***************************************************************************************************
	 *
	 * subtest 6a.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 7.
	 * 
	 ***************************************************************************************************/	
	DEFINE_TESTEVENT("TaskC/D/E periodic activation");
	
	/***************************************************************************************************
	 *
	 * subtest 8.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Alarm canceling");

	/***************************************************************************************************
	 *
	 * subtest 9.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 10.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 11.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 12.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 13.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 14.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 15.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Multiple one-shot alarms");
	/***************************************************************************************************
	 *
	 * subtest 16.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 17.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 18.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("ISR delay race conditions");
	
	/***************************************************************************************************
	 *
	 * subtest 19.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 20.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 21.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * subtest 22.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Alarm actions testing");
	DEFINE_TESTEVENT("Alarm callback test begin");
	DEFINE_TESTEVENT("ALARMCALLBACK(AlarmX1_HW_callback)");
	DEFINE_TESTEVENT("Alarm callback test end");

	/***************************************************************************************************
	 *
	 * subtest 22a.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Alarm set event test begin");
	DEFINE_TESTEVENT("TaskX2 start");
	DEFINE_TESTEVENT("AlarmX2_HW started");
	DEFINE_TESTEVENT("TaskX2 end");	
	DEFINE_TESTEVENT("Alarm set event test end");

	/***************************************************************************************************
	 *
	 * subtest 22b.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Alarm increment counter test");

	/***************************************************************************************************
	 *
	 * subtest 23.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("Alarm action error test");
	DEFINE_TESTEVENT("Advanced time");
	define_error_hook_call(E_OS_LIMIT, OSServiceId_ActivateTask, "Timer");
	DEFINE_TESTEVENT("ISR run");
	
#ifdef OS_EXTENDED_STATUS
	/***************************************************************************************************
	 *
	 * subtest 24.
	 * 
	 ***************************************************************************************************/	
	DEFINE_TESTEVENT("Alarm set event failure test begin");
	DEFINE_TESTEVENT("Alarm X2 about to expire");
	define_error_hook_call(E_OS_STATE, OSServiceId_SetEvent, "Timer");
	DEFINE_TESTEVENT("Alarm set event failure test end");
#endif
	
#endif
	/***************************************************************************************************
	 *
	 * subtest 25.
	 * 
	 ***************************************************************************************************/
	/***************************************************************************************************
	 *
	 * End of testing.. shut down OS and finish.
	 * 
	 ***************************************************************************************************/
	DEFINE_TESTEVENT("End A");
	
	DEFINE_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	DEFINE_TESTEVENT("ShutdownHook");
#endif
	DEFINE_TESTEVENT("After StartOS");
	
#undef DEFINE_TESTEVENT					/* Helps avoid test code errors: don't need DEFINE_TESTEVENT() past this point */
	
	/************************BEGIN TEST AND CHECK AGAINST EXPECTED RESULTS**************************/
	if(E_OK != 0) {						/* $Req: artf1122 $ */
		test_failed(OS_HERE);
	}
	
	/* Subtest 0. Test that the fast interrupt functions work prior to a StartOS() call
	 * 
	 * $Req: artf1069 $
	 */
	
	/* Check that disabling all interrupts works (i.e. holds out a Cat1 ISR)
	 * 
	 * $Req: artf1145 $
	 * $Req: artf1147 $
	 */
	SET_TESTEVENT("Fast interrupt macros");
	SET_TESTEVENT("Disabling all interrupts");
	DisableAllInterrupts();
	testing_trigger_cat1_isr();
	SET_TESTEVENT("Enabling all interrupts");
	EnableAllInterrupts(); /* <--- Cat1 ISR should be handled here */

	/* Check that suspending all interrupts works (i.e. holds out a Cat1 ISR)
	 * 
	 * $Req: artf1144 $
	 * $Req: artf1146 $
	 */
	SET_TESTEVENT("Suspending all interrupts");
	SuspendAllInterrupts();
	testing_trigger_cat1_isr();
	SET_TESTEVENT("Resuming all interrupts");
	ResumeAllInterrupts(); /* <--- Cat1 ISR should be handled here */
	
	/* Check that nested suspending of all interrupts works.
	 * 
	 * $Req: artf1046 $
	 */
	SET_TESTEVENT("Nested suspending all interrupts");
	SuspendAllInterrupts();
	SuspendAllInterrupts();
	testing_trigger_cat1_isr();
	SET_TESTEVENT("Resuming all interrupts - inner");
	ResumeAllInterrupts();
	SET_TESTEVENT("Resuming all interrupts - outer");
	ResumeAllInterrupts(); /* <--- Cat1 ISR should be handled here */

	/* Check that if an enable call is made before a disable, the macros continue to work.
	 * 
	 * $Req: artf1044 $
	 */
	SET_TESTEVENT("Prior enable of interrupts");
	EnableAllInterrupts();
	SET_TESTEVENT("Disabling all interrupts");
	DisableAllInterrupts();
	testing_trigger_cat1_isr();
	SET_TESTEVENT("Enabling all interrupts");
	EnableAllInterrupts(); /* <--- Cat1 ISR should be handled here */

	/* Check that if a resume call is made before a suspend, the macros continue to work.
	 * 
	 * $Req: artf1044 $
	 */
	SET_TESTEVENT("Prior resume of interrupts");
	ResumeAllInterrupts();
	SET_TESTEVENT("Nested suspending all interrupts");
	SuspendAllInterrupts();
	SuspendAllInterrupts();
	testing_trigger_cat1_isr();
	SET_TESTEVENT("Resuming all interrupts - inner");
	ResumeAllInterrupts();
	SET_TESTEVENT("Resuming all interrupts - outer");
	ResumeAllInterrupts(); /* <--- Cat1 ISR should be handled here */

	SET_TESTEVENT("Before StartOS");
	StartOS(OSDEFAULTAPPMODE);
	SET_TESTEVENT("After StartOS");
	
	test_finished();
	return 0;
}

/***************************************************************************************************
 *
 * Entry point to whole test
 *
 ***************************************************************************************************/
int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}

/*****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 ******* 
 *******      M A I N     T E S T     C O D E
 *******
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 *****************************************************************************************
 */

TickType expected_TaskB_start_time;				/* Time that tasks are expected to run; task code checks this */
TickType expected_TaskC_start_time;
TickType expected_TaskD_start_time;
TickType expected_TaskE_start_time;
TickType expected_TaskF_start_time;
TickType TaskC_period;							/* Periodicity of tasks; task uses this to compute expected run time */
TickType TaskD_period;
TickType TaskE_period;
unat TaskC_activation_count;					/* Activation count; task keeps track of the number of times it runs */
unat TaskD_activation_count;
unat TaskE_activation_count;
unat TaskF_activation_count;
unat ISR_invocation_count;						/* ISR also keeps track of how often it has run */
unat log_isr;									/* Set if the ISR handler should drop an event into the log; clear otherwise */
AlarmType ISR_check_alarm;						/* Set to the alarm that ISR should check has a time-to-expire of 0; set to 0 if ISR is not to check */
AlarmType ISR_SetRelAlarm_alarm;				/* Set to the alarm that ISR should set running via SetRelAlarm() call */
AlarmType ISR_SetAbsAlarm_alarm;				/* Set to the alarm that ISR should set running via SetAbsAlarm() call */
AlarmType ISR_CancelAlarm_alarm;				/* Set to the alarm that ISR should set stopped via CancelAlarm() call */

/* Prototype definition for alarm callback; see OS223 spec 9.3 */
ALARMCALLBACK(AlarmX1_HW_callback);				/* $Req: artf1108 $ Prototype defined using this macro */

/* Function called with interrupts disabled. Makes alarm and counter API calls and checks E_OS_DISABLEDINT
 * returned correctly. Called in various 'interrupts disabled' configurations.
 */
void disabled_int_all_api_calls(void)
{
	StatusType rc;
	AlarmBaseType base;
	TickType tick;
	
#ifdef OS_EXTENDED_STATUS
	rc = SetRelAlarm(AlarmB_SW, 1234U, 0);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_AlarmType, AlarmB_SW);
	TESTP(param1_TickType, 1234U);
	TESTP(param2_TickType, 0);
	rc = SetAbsAlarm(AlarmB_SW, 1234U, 0);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_AlarmType, AlarmB_SW);
	TESTP(param1_TickType, 1234U);
	TESTP(param2_TickType, 0);
	rc = CancelAlarm(AlarmB_SW);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_AlarmType, AlarmB_SW);
	rc = GetAlarm(AlarmB_SW, &tick);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_AlarmType, AlarmB_SW);
	TESTP(param1_TickRefType, &tick);
	rc = GetAlarmBase(AlarmB_SW, &base);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_AlarmType, AlarmB_SW);
	TESTP(param_AlarmBaseRefType, &base);
	rc = IncrementCounter(Counter_SW);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_CounterType, Counter_SW);
	rc = ExpireCounter(Counter_HW);
	TEST(rc, E_OS_DISABLEDINT);
	TESTP(param_CounterType, Counter_HW);
#endif
}

/* Useful sub-function to set Alarms C, D and E to three given expiry times,
 * initializes activation counts and expected start times for their corresponding
 * tasks.
 */
void set_3phase_alarms(TickType timeC, TickType timeD, TickType timeE)
{
	StatusType rc;
	
	rc = SetRelAlarm(AlarmC_HW, timeC, 0);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmD_HW, timeD, 0);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmE_HW, timeE, 0);
	TEST(rc, E_OK);

	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	TaskE_activation_count = 0;
	ISR_invocation_count = 0;
	
	expected_TaskC_start_time = fake_timer_count + timeC;
	expected_TaskD_start_time = fake_timer_count + timeD;
	expected_TaskE_start_time = fake_timer_count + timeE;
}

/***************************************************************************************************
 *
 * TaskA.
 * 
 * This task runs at the lowest priority in the system and acts as a background task for running
 * each of the subtests.
 * 
 ***************************************************************************************************/
TASK(TaskA)
{	
	StatusType rc;
	TaskStateType state;
	AlarmBaseType base;
	TickType tick;
	unat i;
	
	/***************************************************************************************************
	 *
	 * Initialization in case no C run-time has set them to zero.
	 * 
	 ***************************************************************************************************/
	expected_TaskB_start_time = 0;
	expected_TaskC_start_time = 0;
	expected_TaskD_start_time = 0;
	expected_TaskE_start_time = 0;
	expected_TaskF_start_time = 0;
	TaskC_period = 0;
	TaskD_period = 0;
	TaskE_period = 0;
	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	TaskE_activation_count = 0;
	TaskF_activation_count = 0;
	ISR_invocation_count = 0;
	log_isr = 0;
	ISR_check_alarm = 0;
	ISR_SetRelAlarm_alarm = 0;
	ISR_SetAbsAlarm_alarm = 0;
	
	/* Mark the start of the background task */
	SET_TESTEVENT("Start A");

	/***************************************************************************************************
	 *
	 * subtest 1.
	 * 
	 * Checks that E_OS_DISABLEDINT is returned from the counter and alarm API calls when the interrupts
	 * are disabled using the 'fast' interrupt API.
	 * 
	 ***************************************************************************************************/	
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
	
	/***************************************************************************************************
	 *
	 * subtest 2.
	 * 
	 * Checks that E_OS_ID returned from API calls when alarm and counter handles are not correct.
	 * Checks that other error returns are as expected.
	 * 
	 ***************************************************************************************************/
	/* Test E_OS_ID returns from alarm and counter API calls */
#ifdef OS_EXTENDED_STATUS
	rc = SetRelAlarm(0, 1234U, 0); TEST(rc, E_OS_ID);				/* $Req: artf1194 $ */
	rc = SetAbsAlarm(0, 1234U, 0); TEST(rc, E_OS_ID);				/* $Req: artf1327 $ */
	rc = CancelAlarm(0); TEST(rc, E_OS_ID);							/* $Req: artf1204 $ */
	rc = GetAlarm(0, &tick); TEST(rc, E_OS_ID);						/* $Req: artf1189 $ */
	rc = GetAlarmBase(0, &base); TEST(rc, E_OS_ID);					/* $Req: artf1186 $ */
	rc = IncrementCounter(0); TEST(rc, E_OS_ID);					/* $Req: artf1063 $ */
	rc = ExpireCounter(0); TEST(rc, E_OS_ID);						/* $Req: artf1328 $ */
#endif
	/* In standard and extended status E_OS_NOFUNC is returned from GetAlarm() if the alarm isn't running
	 *
	 * $Req: artf1188 $ 
	 */
	rc = GetAlarm(AlarmNegTests, &tick);
	TEST(rc, E_OS_NOFUNC);
	TESTP(param_AlarmType, AlarmNegTests);
	TESTP(param1_TickRefType, &tick);
	
	/* In standard and extended status E_OS_VALUE is returned from SetRelAlarm() if the increment is zero
	 * 
	 * $Req: artf1071 $
	 */
	rc = SetRelAlarm(AlarmNegTests, 0, 0);
	TEST(rc, E_OS_VALUE);
	
	/* In standard and extended status E_OS_STATE is returned from SetRelAlarm() if the alarm is already
	 * running.
	 * 
	 * $Req: artf1193 $
	 */
	rc = SetRelAlarm(AlarmNegTests, 1, 0);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmNegTests, 1, 0);
	TEST(rc, E_OS_STATE);
	/* Tidy up */
	rc = CancelAlarm(AlarmNegTests);
	TEST(rc, E_OK);
	
	/* In extended status E_OS_VALUE is returned from SetRelAlarm() if the increment or cycle exceed
	 * the maximum allowed value for the counter, or the cycle time is shorter than the minimum value.
	 *
	 * $Req: artf1195 $
	 */
#ifdef OS_EXTENDED_STATUS
	rc = GetAlarmBase(AlarmNegTests, &base);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmNegTests, base.maxallowedvalue + 1U, 0);
	TEST(rc, E_OS_VALUE);
	TESTP(param_AlarmType, AlarmNegTests);
	TESTP(param1_TickType, base.maxallowedvalue + 1U);
	TESTP(param2_TickType, 0);
	
	rc = SetRelAlarm(AlarmNegTests, 1U, base.mincycle - 1U);
	TEST(rc, E_OS_VALUE);
	TESTP(param_AlarmType, AlarmNegTests);
	TESTP(param1_TickType, 1U);
	TESTP(param2_TickType, base.mincycle - 1U);
	
	rc = SetRelAlarm(AlarmNegTests, 1U, base.maxallowedvalue + 1U);
	TEST(rc, E_OS_VALUE);
	TESTP(param_AlarmType, AlarmNegTests);
	TESTP(param1_TickType, 1U);
	TESTP(param2_TickType, base.maxallowedvalue + 1U);
#endif
	
	/* Testing SetAbsAlarm() for similar error cases to SetRelAlarm() */
	
	/* In standard and extended status SetAbsAlarm() returns E_OS_STATE if the alarm is
	 * in use.
	 * 
	 * $Req; artf1199 $
	 */
	rc = SetRelAlarm(AlarmNegTests, 1U, 0);
	TEST(rc, E_OK);
	rc = SetAbsAlarm(AlarmNegTests, 1U, 0);
	TEST(rc, E_OS_STATE);
	TESTP(param_AlarmType, AlarmNegTests);
	TESTP(param1_TickType, 1U);
	TESTP(param2_TickType, 0);
	/* Tidy up */
	rc = CancelAlarm(AlarmNegTests);
	TEST(rc, E_OK);
	
	/* In extended status E_OS_VALUE is returned from SetAbsAlarm() if the increment or cycle exceed
	 * the maximum allowed value for the counter, or the cycle time is shorter than the minimum value.
	 *
	 * $Req: artf1200 $
	 */
#ifdef OS_EXTENDED_STATUS
	rc = GetAlarmBase(AlarmNegTests, &base);
	TEST(rc, E_OK);
	rc = SetAbsAlarm(AlarmNegTests, base.maxallowedvalue + 1U, 0);
	TEST(rc, E_OS_VALUE);
	rc = SetAbsAlarm(AlarmNegTests, 1U, base.mincycle - 1U);
	TEST(rc, E_OS_VALUE);
	rc = SetAbsAlarm(AlarmNegTests, 1U, base.maxallowedvalue + 1U);
	TEST(rc, E_OS_VALUE);
#endif
	
	/* In both standard and extended status CancelAlarm() returns E_OS_NOFUNC if it was not running.
	 * 
	 * $Req: artf1203 $
	 */
	rc = CancelAlarm(AlarmNegTests);
	TEST(rc, E_OS_NOFUNC);
	TESTP(param_AlarmType, AlarmNegTests);
	
	/***************************************************************************************************
	 *
	 * subtest 2a.
	 * 
	 * Checks counter and alarm constants are as defined in the OIL file.
	 * 
	 * $Req: artf1183 $
	 * $Req: artf1205 $
	 * $Req: artf1185 $
	 * $Req: artf1206 $
	 * 
	 ***************************************************************************************************/
	rc = GetAlarmBase(AlarmNegTests, &base);
	TEST(rc, E_OK);
	TEST(base.maxallowedvalue, 5999U);
	TEST(base.mincycle, 590U);
	TEST(base.ticksperbase, 1000U);

	TEST(OSMAXALLOWEDVALUE_Counter_SW_NegTests, 5999U);
	TEST(OSMINCYCLE_Counter_SW_NegTests, 590U);
	TEST(OSTICKSPERBASE_Counter_SW_NegTests, 1000U);
	
	rc = GetAlarmBase(AlarmB_SW, &base);
	TEST(rc, E_OK);
	TEST(base.maxallowedvalue, 4999U);
	TEST(base.mincycle, 0U);
	TEST(base.ticksperbase, 1000U);

	TEST(OSMAXALLOWEDVALUE_Counter_SW, base.maxallowedvalue);
	TEST(OSMINCYCLE_Counter_SW, base.mincycle);
	TEST(OSTICKSPERBASE_Counter_SW, base.ticksperbase);
	
	/* @TODO: OSTICKDURATION will need testing; can't be done yet because OSTICKDURATION is tied to
	 * the driver, and the configuration part of the driver will determine this from prescaler settings,
	 * clock frequencies, etc. The concept of a "system counter" taking a hardware device is not
	 * appropriate for Scalios in any case. This whole subject needs revisiting in the light of AUTOSAR 3.0
	 * requirements on timer drivers.
	 */
	
	
	/***************************************************************************************************
	 *
	 * subtest 2b.
	 * 
	 * Checks alarm callback runs at OS level.
	 * 
	 * $Req: artf1211 $
	 * 
	 ***************************************************************************************************/
	/* Test an alarm callback occurs for a software counter */
	rc = SetRelAlarm(AlarmX2_SW_callback, 1, 0);
	TEST(rc, E_OK);
	rc = IncrementCounter(Counter_SW_multi);
	TEST(rc, E_OK);
	
	/***************************************************************************************************
	 *
	 * subtest 3.
	 * 
	 * Checks software counters and the IncrementCounter() API call.
	 * 
	 * $Req: artf1064 $
	 * $Req: artf1093 $
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Software counter checks");
	
	/* Test increment counter expires a singleton alarm at the right time */
	SetRelAlarm(AlarmB_SW, 5U, 0);
	SET_TESTEVENT("1st IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);
	SET_TESTEVENT("2nd IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);
	SET_TESTEVENT("3rd IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);
	SET_TESTEVENT("4th IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);
	SET_TESTEVENT("5th IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);
	/* AlarmB_SW should now have expired, and should not be running; TaskB1_SW should have run
	 * and dropped an event in the log.
	 * 
	 * This tests task activation from alarm expiry; $Req: artf1107 $
	 */
	SET_TESTEVENT("6th IncrementCounter()");
	rc = IncrementCounter(Counter_SW); TEST(rc, E_OK);

	/***************************************************************************************************
	 *
	 * subtest 4.
	 * 
	 * Tests that IncrementCounter() works for two alarms expiring at the same time.
	 * 
	 * $Req: artf1064 $
	 * 
	 ***************************************************************************************************/
	/* Test increment counter loop: 2 alarms expire at same time are handled OK. */
	rc = SetRelAlarm(AlarmB1_SW, 100U, 10U); TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmB2_SW, 100U, 15U); TEST(rc, E_OK);
	
	SET_TESTEVENT("IncrementCounter() loop start");
	for(i = 1; i <= 112; i++) {
		if(i == 99) {
			SET_TESTEVENT("99th call to IncrementCounter()");
		}
		if(i == 100) {
			SET_TESTEVENT("100th call to IncrementCounter()");
		}
		if(i == 101) {
			SET_TESTEVENT("101st call to IncrementCounter()");
		}
		if(i == 109) {
			SET_TESTEVENT("109th call to IncrementCounter()");
		}
		if(i == 110) {
			SET_TESTEVENT("110th call to IncrementCounter()");
		}
		if(i == 111) {
			SET_TESTEVENT("111th call to IncrementCounter()");
		}
		rc = IncrementCounter(Counter_SW_multi); TEST(rc, E_OK);
	}
	SET_TESTEVENT("IncrementCounter() loop end");

	/***************************************************************************************************
	 *
	 * subtest 4a.
	 * 
	 * Checks IncrementCounter() returns E_OK when there is an error in the alarm expiry event.
	 *
	 * $Req: artf1073 $
	 *  
	 ***************************************************************************************************/	
	/* Check that TaskX2 is suspended */
	rc = GetTaskState(TaskX2, &state);
	TEST(rc, E_OK);
	TEST(state, SUSPENDED);
	
	/* Set up AlarmX2_SW_multi to expire on the next tick. Alarm configured to send event X2 to TaskX2.
	 * Should fail with an E_OS_STATE error (and a service ID of "set event"), but the IncrementCounter()
	 * call should return E_OK.
	 * 
	 * $Req: artf1113 $
	 * $Req: artf1115 $
	 */
	rc = SetRelAlarm(AlarmX2_SW_multi, 1U, 0);
	TEST(rc, E_OK);
	
	SET_TESTEVENT("IncrementCounter() E_OK test begin");
	rc = IncrementCounter(Counter_SW_multi);
	TEST(rc, E_OK);
	
	/* Do the same for task activation: check that E_OS_LIMIT for TaskC is given as an error, service ID reported
	 * is "activate task", but E_OK returned
	 *
	 * $Req: artf1113 $
	 * $Req: artf1115 $ 
	 */
	SET_TESTEVENT("IncrementCounter() E_OK test 2");
	
	/* Activate TaskC and make sure that it doesn't run */
	rc = GetResource(RES_SCHEDULER);
	TEST(rc, E_OK);
	expected_TaskC_start_time = fake_timer_count;			/* TaskC normally expects to run from the hardware timer; set this to keep it happy */
	rc = ActivateTask(TaskC);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmC_SW, 1U, 0);
	TEST(rc, E_OK);
	rc = IncrementCounter(Counter_SW);						/* E_OS_LIMIT should be dropped by the error hook into the test event log */
	TEST(rc, E_OK);
	rc  = ReleaseResource(RES_SCHEDULER);
	TEST(rc, E_OK);

	SET_TESTEVENT("IncrementCounter() E_OK test end");
	
	
	/* @todo Need to test wrapping of timer works, so make sure we have long striding alarms
	 */
	
	/***************************************************************************************************
	 *
	 * subtest 5.
	 * 
	 * Checks hardware counters handle one-shot and periodic alarms correctly.
	 * 
	 * $Req: artf1190 $
	 * $Req; artf1191 $
	 * 
	 ***************************************************************************************************/	
	SET_TESTEVENT("Begin alarm control");
	log_isr = 1U;					/* Turn on ISR logging: needed to drop test events into the log */

	/* Set the alarm for 100 ticks into the future, one-shot alarm */
	expected_TaskB_start_time = 100U;

	rc = SetRelAlarm(AlarmB_HW, 100U, 0);
	TEST(rc, E_OK);
	
	SET_TESTEVENT("Done SetRelAlarm()");	
	
	/* Now do a 'busy/wait' on the time for a long 'time' to check that the alarm is not periodic */
	fake_timer_advance(10000U);
	TEST(fake_timer_count, 10000U);
	
	SET_TESTEVENT("Periodic alarm");
	
	rc = SetRelAlarm(AlarmB_HW, 1000U, 125U);
	TEST(rc, E_OK);
	
	fake_timer_advance(999U);
	
	SET_TESTEVENT("Periodic alarm about to expire (1st)");
	expected_TaskB_start_time = 11000U;
	fake_timer_advance(1U);
	TEST(fake_timer_count, 11000U);

	SET_TESTEVENT("Periodic alarm about to expire (2nd)");
	expected_TaskB_start_time = 11125U;
	fake_timer_advance(249U);
	TEST(fake_timer_count, 11249U);
	
	SET_TESTEVENT("Periodic alarm about to expire (3rd)");
	expected_TaskB_start_time = 11250U;
	fake_timer_advance(1U);
	TEST(fake_timer_count, 11250U);
	
	/* Test we can cancel the alarm and no events occur
	 * 
	 * $Req: artf1201 $ 
	 */
	rc = CancelAlarm(AlarmB_HW);
	TEST(rc, E_OK);
	
	/* Let a full wrap of the timer pass and see that TaskB never runs */
	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW);
	fake_timer_advance(1U);
	
	/* Drop an event into the log to check that no TaskB events are in there erroneously */
	SET_TESTEVENT("Time passed after cancel");
	log_isr = 0;										/* Tidy up: Turn off ISR logging */
	
#ifndef SINGLETON_ALARMCOUNTER
	/***************************************************************************************************
	 *
	 * subtest 6.
	 * 
	 * Checks multiple alarms on a hardware counter. Sets TaskC running periodically (note that TaskC entry
	 * function will self-check that it is activated at the right time).
	 * 
	 * $Req: artf1196 $
	 * $Req: artf1197 $
	 * $Req: artf1104 $
	 * 
	 ***************************************************************************************************/
	
	/* Timer must be left at value 11250 from previous phase before this phase starts */
	TEST(fake_timer_count, 11250U);
	
	SET_TESTEVENT("TaskC periodic activation");
	TaskC_period = 233U;								/* Prime number period so should walk through lots of different values */
	TaskC_activation_count = 0;
	expected_TaskC_start_time = 20000U;
	
	rc = SetAbsAlarm(AlarmC_HW, 20000U, TaskC_period);
	TEST(rc, E_OK);

	fake_timer_advance(10000U);
	TEST(fake_timer_count, 21250U);
	/* TaskC starts at 22000, so 1250 elapsed since then, which is triggering at:
	 * 
	 * 20000 1st
	 * 20233 2nd
	 * 20466 3rd
	 * 20699 4th
	 * 20932 5th
	 * 21165 6th
	 * 21398 <-- not happened yet since time is 21250 (see above)
	 */
	TEST(TaskC_activation_count, 6);					
	
	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW);
	TEST(TaskC_activation_count, 6 + (OSMAXALLOWEDVALUE_Counter_HW / TaskC_period));

	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);

	SET_TESTEVENT("TaskC one-shot activation via SetAbsAlarm()");

	/* Check a one-shot alarm goes off one only when set via SetAbsAlarm() */
	TaskC_activation_count = 0;
	expected_TaskC_start_time = 500U;
	rc = SetAbsAlarm(AlarmC_HW, 500U, 0);
	TEST(rc, E_OK);
	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW);
	TEST(TaskC_activation_count, 1);

	/* Alarm should not only not have expired again, but also be registered as stopped */
	rc = GetAlarm(AlarmC_HW, &tick);
	/* Note: error hook should be called here and this registered in the expected test event log */
	TEST(rc, E_OS_NOFUNC);
	SET_TESTEVENT("One-shot test end");

	/***************************************************************************************************
	 *
	 * subtest 6a.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that SetRelAlarm(), SetAbsAlarm(),
	 * CancelAlarm(), GetAlarm(), and GetAlarmBase() work when called from an ISR.
	 * when called from an ISR.
	 * 
	 * $Req: artf1198 $
	 * $Req: artf1192 $
	 * 
	 ***************************************************************************************************/	
	
	TaskC_activation_count = 0;
	/* Set alarm to activate task C before TaskD and TaskE */
	expected_TaskC_start_time = fake_timer_count + 1U;
	
	TaskD_activation_count = 0;
	/* See ISR(Timer). Will set alarm for TaskD to expire at 10000 */
	expected_TaskD_start_time = 10000U;
	
	TaskE_activation_count = 0;
	/* See ISR(Timer). Will set alarm for TaskE to expire at 10 from when the ISR first runs to service TaskC */
	expected_TaskE_start_time = expected_TaskC_start_time + 10U;

	/* Tell ISR to set absolute time for AlarmD_HW and relative time for AlarmE_HW */
	ISR_SetAbsAlarm_alarm = AlarmD_HW;
	ISR_SetRelAlarm_alarm = AlarmE_HW;
	
	/* Get ISR to run for TaskC, then to add two more alarms running, then expire those. */	
	rc = SetRelAlarm(AlarmC_HW, 1U, 0);
	TEST(rc, E_OK);
	/* $Req: artf1097 $ case 3b: task activation via alarm expiry (see case 3a in Test4 for other case) */
	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW);
	
	/* Expect three tasks to be activated once. Tasks will have self-checked the time at which they run. */
	TEST(TaskC_activation_count, 1);
	TEST(TaskD_activation_count, 1);
	TEST(TaskE_activation_count, 1); 
	
	/* Tidy up; not strictly necessary, since ISR should have done it already */
	ISR_SetRelAlarm_alarm = 0;
	ISR_SetAbsAlarm_alarm = 0;
	
	/* Get ISR to run for TaskC periodically, and tell the ISR to cancel it on first expiry. Tests
	 * that CancelAlarm() works when called from an ISR. ISR will go off, process alarm (and activate
	 * task), and then cancel further alarm.
	 * 
	 * $Req: artf1202 $
	 */
	TaskC_activation_count = 0;
	expected_TaskC_start_time = fake_timer_count + 1U;
	ISR_CancelAlarm_alarm = AlarmC_HW;
	rc = SetRelAlarm(AlarmC_HW, 1U, 100U);
	TEST(rc, E_OK);

	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW);
	/* Periodic alarm would have gone off many times from now if not canceled but if
	 * cancel operation works then should just be once.
	 */
	TEST(TaskC_activation_count, 1);
	
	/* Tidy up; not strictly necessary, since ISR should have done it already */
	ISR_CancelAlarm_alarm = 0;

	/***************************************************************************************************
	 *
	 * subtest 7.
	 * 
	 * Checks multiple alarms on a hardware counter. Sets TaskC, TaskD and TaskE running periodically
	 * with periods of 7, 11 and 13 ticks. These periods are relatively prime so all phasings with respect
	 * to each other are tested. A simultaneous expiry (i.e. all alarms due together releasing all
	 * three tasks together) are also tested.
	 * 
	 ***************************************************************************************************/	
	SET_TESTEVENT("TaskC/D/E periodic activation");

	/* Relatively prime periods, with short LCM (least common multiple) so that
	 * all relative phases are tried over a long cycle of 7 x 11 x 13 = 1001 ticks.
	 */
	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	TaskE_activation_count = 0;
	ISR_invocation_count = 0;
	
	expected_TaskC_start_time = fake_timer_count + 10000U;
	expected_TaskD_start_time = fake_timer_count + 10000U;
	expected_TaskE_start_time = fake_timer_count + 10000U;

	TaskC_period = 7U;
	TaskD_period = 11U;
	TaskE_period = 13U;
		
	/* Safe to use SetRelAlarm() here because time doesn't advance between the calls, so the alignment
	 * is a "critical instant" (i.e. all tasks released together)
	 */
	rc = SetRelAlarm(AlarmC_HW, 10000U, TaskC_period);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmD_HW, 10000U, TaskD_period);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmE_HW, 10000U, TaskE_period);
	TEST(rc, E_OK);
	
	/* Run the tasks periodically for a long period (as long as we run them for more than the LCM
	 * then all phasings will have been seen).
	 */
	fake_timer_advance(62000);

	TEST(TaskC_activation_count, 1 + (52000 / TaskC_period));
	TEST(TaskD_activation_count, 1 + (52000 / TaskD_period));
	TEST(TaskE_activation_count, 1 + (52000 / TaskE_period));

	/* One interrupt per timing event: design of drivers enforces this so that the execution time of the interrupt
	 * handler is bounded to one event. This means that the WCET of the handler is short and predictable and can
	 * be enforced with timing protection in the future.
	 */
	TEST(ISR_invocation_count, TaskC_activation_count + TaskD_activation_count + TaskE_activation_count);

	/* Tidy up alarms before moving to next phase */
	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);
	rc = CancelAlarm(AlarmE_HW);
	TEST(rc, E_OK);

	/***************************************************************************************************
	 *
	 * subtest 8.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that the head of a queue of alarms (i.e. the
	 * soonest to expire) can be canceled properly. 
	 * 
	 ***************************************************************************************************/
	
	/* Now test that canceling an alarm works in various situations
	 * 
	 * $Req: artf1201 $
	 */
	SET_TESTEVENT("Alarm canceling");
	
	/* Head of queue cancelled */
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 1U);
	TEST(TaskE_activation_count, 1U);
	TEST(ISR_invocation_count, 2U);

	/***************************************************************************************************
	 *
	 * subtest 9.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that alarm that is neither the soonest nor
	 * the latest in a queue can be canceled properly.
	 * 
	 ***************************************************************************************************/

	/* Middle one in queue cancelled */
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 1U);
	TEST(TaskD_activation_count, 0);
	TEST(TaskE_activation_count, 1U);
	TEST(ISR_invocation_count, 2U);

	/***************************************************************************************************
	 *
	 * subtest 10.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that the latest alarm in a queue can be
	 * canceled properly.
	 * 
	 ***************************************************************************************************/

	/* End of queue cancelled */
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmE_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 1U);
	TEST(TaskD_activation_count, 1U);
	TEST(TaskE_activation_count, 0);
	TEST(ISR_invocation_count, 2U);

	/***************************************************************************************************
	 *
	 * subtest 11.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that leaving a queue with just one alarm
	 * remaining works properly.
	 * 
	 ***************************************************************************************************/

	/* Check that dropping to one alarm in the queue works */
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmC_HW);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(TaskE_activation_count, 1U);
	TEST(ISR_invocation_count, 1U);

	/***************************************************************************************************
	 *
	 * subtest 12.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that canceling alarms from the head only
	 * works.
	 * 
	 ***************************************************************************************************/

	/* Check that dropping to zero alarms works (cancel from head only) */
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmC_HW);
	rc = CancelAlarm(AlarmD_HW);
	rc = CancelAlarm(AlarmE_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(TaskE_activation_count, 0);
	TEST(ISR_invocation_count, 0);
	
	/***************************************************************************************************
	 *
	 * subtest 13.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that canceling all alarms from the tail only
	 * works.
	 * 
	 ***************************************************************************************************/
	set_3phase_alarms(10U, 11U, 12U);
	rc = CancelAlarm(AlarmE_HW);
	rc = CancelAlarm(AlarmD_HW);
	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(TaskE_activation_count, 0);
	TEST(ISR_invocation_count, 0);
	
	/***************************************************************************************************
	 *
	 * subtest 14.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that canceling alarms with identical expiry
	 * times works.
	 * 
	 ***************************************************************************************************/
	set_3phase_alarms(11U, 11U, 11U);
	rc = CancelAlarm(AlarmC_HW);
	rc = CancelAlarm(AlarmE_HW);
	TEST(rc, E_OK);	
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 1U);
	TEST(TaskE_activation_count, 0);
	TEST(ISR_invocation_count, 1U);
	
	/***************************************************************************************************
	 *
	 * subtest 15.
	 * 
	 * Checks multiple alarms on a hardware counter. Tests that one-shot alarms work.
	 ***************************************************************************************************/
	SET_TESTEVENT("Multiple one-shot alarms");
	set_3phase_alarms(10U, 11U, 11U);
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 1U);
	TEST(TaskD_activation_count, 1U);
	TEST(TaskE_activation_count, 1U);
	TEST(ISR_invocation_count, 3U);

	/***************************************************************************************************
	 *
	 * subtest 16.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that a mix of one-shot and cyclic alarms work
	 * OK.
	 * 
	 ***************************************************************************************************/
	set_3phase_alarms(10U, 11U, 11U);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);
	TaskD_period = 30U;
	SetRelAlarm(AlarmD_HW, 11U, 30U);	/* Should go off at 11 and 41 */
	
	fake_timer_advance(50U);
	TEST(TaskC_activation_count, 1U);
	TEST(TaskD_activation_count, 2U);
	TEST(TaskE_activation_count, 1U);
	TEST(ISR_invocation_count, 4U);
	
	/* Tidy up for next phase */
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);

	/***************************************************************************************************
	 *
	 * subtest 17.
	 * 
	 * Deleted.
	 * 
	 ***************************************************************************************************/

	/***************************************************************************************************
	 *
	 * subtest 18.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks when an ISR takes some time to run
	 * and doesn't get around to processing the alarms until after the time the second alarm in the
	 * queue is due to expire.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("ISR delay race conditions");
	
	/* Lock interrupts out (via a shared resource lock) and then
	 * hold the ISR out until past the expiry point of the 2nd alarm in the queue.
	 */
	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	ISR_invocation_count = 0;
	
	rc = GetResource(ResISR);
	TEST(rc, E_OK);
	
	/* Set two alarms going into the future, and hold resource over until second is due to expire */
	SetRelAlarm(AlarmC_HW, 50U, 100U);
	SetRelAlarm(AlarmD_HW, 60U, 100U);
	
	/* We are about to hold out time for 100 ticks, and so by the time the two tasks get to run
	 * time should be advanced by 100 ticks.
	 */
	expected_TaskC_start_time = fake_timer_count + 100U;
	expected_TaskD_start_time = fake_timer_count + 100U;
	
	/* Move time forwards; do not expect ISR or tasks to run */
	fake_timer_advance(100U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(ISR_invocation_count, 0);
	
	rc = ReleaseResource(ResISR);
	TEST(rc, E_OK);
	
	/* ISR and two tasks should have run now */
	TEST(TaskC_activation_count, 1U);
	TEST(TaskD_activation_count, 1U);
	TEST(ISR_invocation_count, 2U);

	/* Tidy up */
	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);
	
	/***************************************************************************************************
	 *
	 * subtest 19.
	 * 
	 * Checks multiple alarms on a hardware counter. Tests the race where the 2nd alarm in the queue is
	 * already due (and of course the 1st alarm is due) when the 1st alarm is canceled. Checks this works
	 * properly.
	 * 
	 ***************************************************************************************************/

	/* Holding out the ISR past the expiry time of the 2nd alarm in the queue and then cancel the 1st
	 * alarm before the ISR for either is handled. Similar setup to the previous test (subtest 18).
	 */
	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	ISR_invocation_count = 0;
	
	rc = GetResource(ResISR);
	TEST(rc, E_OK);
	
	/* Set two alarms going into the future, and hold resource over until after 2nd is due to expire */
	rc = SetRelAlarm(AlarmC_HW, 50U, 100U);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmD_HW, 60U, 100U);
	TEST(rc, E_OK);
	
	/* We expect TaskD to run, but not TaskC because we will cancel TaskC's alarm. TaskD will go off
	 * when we release the ISR, not when the alarm is due.
	 */
	expected_TaskD_start_time = fake_timer_count + 100U;

	/* Move time forwards; do not expect ISR or tasks to run */
	fake_timer_advance(100U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(ISR_invocation_count, 0);

	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);
	
	rc = ReleaseResource(ResISR);
	TEST(rc, E_OK);
	
	/* ISR and TaskD should have run now, but not TaskC */
	TEST(TaskC_activation_count, 0U);
	TEST(TaskD_activation_count, 1U);
	TEST(ISR_invocation_count, 1U);
	
	/* Tidy up */
	rc = CancelAlarm(AlarmD_HW);
	
	/***************************************************************************************************
	 *
	 * subtest 20.
	 * 
	 * Checks multiple alarms on a hardware counter. Tests that two due alarms can be canceled while
	 * an ISR is pending and no ISR ever ends up running.
	 * 
	 ***************************************************************************************************/	
	TaskC_activation_count = 0;
	TaskD_activation_count = 0;
	ISR_invocation_count = 0;
	
	/* Lock out the ISR */
	rc = GetResource(ResISR);
	TEST(rc, E_OK);
	
	/* Set two alarms going into the future, and hold resource over until after 2nd is due to expire */
	rc = SetRelAlarm(AlarmC_HW, 50U, 100U);
	TEST(rc, E_OK);
	rc = SetRelAlarm(AlarmD_HW, 60U, 100U);
	TEST(rc, E_OK);
	
	/* Move time forwards; do not expect ISR or tasks to run */
	fake_timer_advance(100U);
	TEST(TaskC_activation_count, 0);
	TEST(TaskD_activation_count, 0);
	TEST(ISR_invocation_count, 0);

	/* Cancel both alarms (both are due and an ISR should be pending) */
	rc = CancelAlarm(AlarmC_HW);
	TEST(rc, E_OK);
	rc = CancelAlarm(AlarmD_HW);
	TEST(rc, E_OK);
	
	/* Let ISR back in, except that it should have gone away now */
	rc = ReleaseResource(ResISR);
	TEST(rc, E_OK);
	
	/* ISR should not have run and neither should the tasks */
	TEST(TaskC_activation_count, 0U);
	TEST(TaskD_activation_count, 0U);
	TEST(ISR_invocation_count, 0U);
	
	/***************************************************************************************************
	 *
	 * subtest 21.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that the driver resolves the race where a
	 * cyclic alarm becomes due again before the original expiry was processed.
	 * 
	 ***************************************************************************************************/

	/* Hold out the ISR for a whole cycle period. Use TaskF with a limit count of 2 because
	 * two outstanding activations will have queued up for the task.
	 */
	TaskF_activation_count = 0;
	ISR_invocation_count = 0;
	
	rc = GetResource(ResISR);
	TEST(rc, E_OK);
	
	/* Set an alarm going into the future, and hold resource over until after due to expire for the second time */
	rc = SetRelAlarm(AlarmF_HW, 50U, 100U);
	TEST(rc, E_OK);
	
	/* Moving time forward by 200 ticks; task due to be released at 50 and 150, but will be held over until 200. The task
	 * will run twice at fake time 200
	 */
	expected_TaskF_start_time = fake_timer_count + 200U;
	
	/* Move time forwards; do not expect ISR or task to run */
	fake_timer_advance(200U);
	TEST(TaskF_activation_count, 0);
	TEST(ISR_invocation_count, 0);

	/* Check that periodic alarm has expired twice back-to-back with a 'time to expire' of 0
	 * (via GetAlarm() in the ISR handler) for each expiry of the alarm; set up a variable to the ISR to check this.
	 */
	ISR_check_alarm = AlarmF_HW;
	
	/* Let ISR loose.. */
	rc = ReleaseResource(ResISR);			/* <---- will let ISR now run, which will dispatch TaskF */
	TEST(rc, E_OK);
	
	/* ISR and TaskF should now have run twice */
	TEST(TaskF_activation_count, 2U);
	TEST(ISR_invocation_count, 2U);
	
	/* Tidy up */
	ISR_check_alarm = 0;
	rc = CancelAlarm(AlarmF_HW);
	TEST(rc, E_OK);

	/***************************************************************************************************
	 *
	 * subtest 22.
	 * 
	 * Checks multiple alarms on a hardware counter. Check callback alarm expiry action. Do not need
	 * to test task activation action since previous tests effectively cover this.
	 * 
	 * Test applied only for multiple alarms on a counter because the singleton/multi operation is
	 * orthogonal to the event expiry handling mechanism.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Alarm actions testing");
	SET_TESTEVENT("Alarm callback test begin");

	/* Set alarm for immediate expiry */
	rc = SetRelAlarm(AlarmX1_HW, 1U, 0);
	TEST(rc, E_OK);
	fake_timer_advance(2U);
	
	/* By now the alarm callback (see ALARMCALLBACK() usage below) will have run and added to the log.
	 * If it failed to do so then the following log entry would not match with the expected log.
	 */
	SET_TESTEVENT("Alarm callback test end");	

	/***************************************************************************************************
	 *
	 * subtest 22a.
	 * 
	 * Checks multiple alarms on a hardware counter. Check set event alarm expiry action.
	 * 
	 * Test applied only for multiple alarms on a counter because the singleton/multi operation is
	 * orthogonal to the event expiry handling mechanism.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Alarm set event test begin");
	
	/* Kick off an extended task which blocks and comes back to here */
	rc = ActivateTask(TaskX2);
	TEST(rc, E_OK);
		
	/* Kick off an alarm to run and set an event to the task
	 * 
	 * This tests event setting from alarm expiry; $Req: artf1107 $
	 */
	SET_TESTEVENT("AlarmX2_HW started");
	rc = SetRelAlarm(AlarmX2_HW, 1U, 0);
	TEST(rc, E_OK);
	
	/* Advance time to make the alarm expire */
	fake_timer_advance(2U);
	
	/* Task should have woken and terminated by now */
	/* $Req: artf1097 $ case 5b: set event via alarm expiry (see case 5a for direct set event) */
	SET_TESTEVENT("Alarm set event test end");

	/***************************************************************************************************
	 *
	 * subtest 22b.
	 * 
	 * Checks multiple alarms on a hardware counter. Check increment counter alarm expiry action.
	 * 
	 * $Req: artf1070 $
	 * 
	 * Test applied only for multiple alarms on a counter because the singleton/multi operation is
	 * orthogonal to the event expiry handling mechanism.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Alarm increment counter test");
	
	/* Set a software alarm running */
	rc = SetRelAlarm(AlarmX3_SW, 1000U, 0);
	TEST(rc, E_OK);
	
	/* Set a hardware alarm running */
	rc = SetRelAlarm(AlarmX4_HW, 1U, 0);
	TEST(rc, E_OK);
	
	/* Determine time to expiry of alarm on the software counter: should be 1000
	 * 
	 * $Req: artf1187 $
	 */
	rc = GetAlarm(AlarmX3_SW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 1000U);
	
	/* Advance time to make the hardware alarm expire */
	fake_timer_advance(2U);
	
	/* Should have expired; now check that the time to expiry for the alarm on the
	 * sofware counter has gone down to 999 (i.e. the increment counter action was
	 * undertaken).
	 * 
	 * This tests that increment counter as an alarm expiry operates as if the API were called
	 * 
	 * $Req: artf1107 $
	 */
	rc = GetAlarm(AlarmX3_SW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 999U);
	
	/* Tidy up */
	rc = CancelAlarm(AlarmX3_SW);
	TEST(rc, E_OK);
	
	/***************************************************************************************************
	 *
	 * subtest 23.
	 * 
	 * Checks multiple alarms on a hardware counter. Checks that a task activation on a task that has
	 * reached its limit causes an error with E_OS_LIMIT as the code.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Alarm action error test");
		
	/* Set a hardware alarm running to run TaskF. The alarm has a short cycle period and we hold the
	 * alarm over from running until three periods have expired. The ISR will run, dispatch TaskF,
	 * and then run twice more. The 3rd run of the ISR will handle the alarm expiry but this will
	 * give an error because TaskF has an activation limit of 2.
	 */
	TaskF_activation_count = 0;
	rc = SetRelAlarm(AlarmF_HW, 5U, 10U);
	TEST(rc, E_OK);
	
	/* Hold out the ISR from running until three periods have expired */
	rc = GetResource(ResISR);	
	TEST(rc, E_OK);
	fake_timer_advance(26U);	/* The alarm is due at (relative times) 5, 15 and 25 */
	SET_TESTEVENT("Advanced time");
	
	/* All TaskF runs are compressed together in time and so should report the same time when TaskF it runs */
	expected_TaskF_start_time = fake_timer_count;
	
	rc = ReleaseResource(ResISR);
	TEST(rc, E_OK);

	/* An error code should be dropped into the log here; see expected log definition.
	 * 
	 * Note: All ExpireCounter() calls via the ISR (including this one) are checked to return E_OK even if
	 * there are errors in the alarm expiry itself (see fake_timer_ISR()for details).
	 */
	
	SET_TESTEVENT("ISR run");
	
	/* Task F should have run twice by now */
	TEST(TaskF_activation_count, 2U);	
	
	/* Tidy up */
	rc = CancelAlarm(AlarmF_HW);
	
#ifdef OS_EXTENDED_STATUS
	/***************************************************************************************************
	 *
	 * subtest 24.
	 * 
	 * Checks multiple alarms on a hardware counter. Tests that an expiry action of setting an event on
	 * a task that is suspended causes an error (E_OS_STATE) to be generated.
	 * 
	 ***************************************************************************************************/
	SET_TESTEVENT("Alarm set event failure test begin");

	/* Send event X2 to TaskX2 as before; TaskX2 this time is suspended; error to send an event to a suspended task */
	rc = SetRelAlarm(AlarmX2_HW, 1U, 0);
	TEST(rc, E_OK);
	SET_TESTEVENT("Alarm X2 about to expire");
	fake_timer_advance(10U);
	/* Error code should be logged by now; drop another event into the log to make sure */
	SET_TESTEVENT("Alarm set event failure test end");
#endif
	
#endif /* Multi alarm only testing */
	
	/***************************************************************************************************
	 *
	 * subtest 25.
	 *
	 * Test that GetAlarm() returns the time to expiry. Also tests that for an expired (but not processed)
	 * alarms return a time-to-expiry of 0. 
	 * 
	 ***************************************************************************************************/

	/* Make an alarm pending but hold out the ISR processing it past the expiry point of the alarm.
	 */
	TaskF_activation_count = 0;
	ISR_invocation_count = 0;
	
	/* Set a one-shot alarm going in the future */
	rc = SetRelAlarm(AlarmB_HW, 125U, 0);
	
	/* Time to expiry should be 125 ticks */
	rc = GetAlarm(AlarmB_HW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 125U);
	
	/* Move time nearer the expiry */
	fake_timer_advance(100U);
	
	/* Time to expiry should now be 25 ticks */
	rc = GetAlarm(AlarmB_HW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 25U);
	
	/* Lock out fake timer ISR */
	rc = GetResource(ResISR);
	TEST(rc, E_OK);
	
	/* Move time up to exactly the expiry time */
	fake_timer_advance(25U);
	
	/* Time to expiry should now be 0 ticks */
	rc = GetAlarm(AlarmB_HW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 0);
	
	/* Move time past the expiry time */
	fake_timer_advance(1000U);
	
	/* Time to expiry should still be 0 ticks */
	rc = GetAlarm(AlarmB_HW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 0);
	
	/* Move time on a full wrap less 1010 ticks so that the fake timer is 10 ticks 'before' the alarm is due.
	 * Should still report 0 (should have identified that the timer has wrapped).
	 */
	fake_timer_advance(OSMAXALLOWEDVALUE_Counter_HW - 1010U);
	
	/* Time to expiry should still be 0 ticks */
	rc = GetAlarm(AlarmB_HW, &tick);
	TEST(rc, E_OK);
	TEST(tick, 0);
	
	/* Check that nothing happens after the alarm is cancelled */
	rc = CancelAlarm(AlarmB_HW);
	TEST(rc, E_OK);
	
	rc = ReleaseResource(ResISR);	/* <---- no ISR should run now */
	TEST(rc, E_OK);
	TEST(ISR_invocation_count, 0);
	TEST(TaskF_activation_count, 0);
	
	/***************************************************************************************************
	 *
	 * End of testing.. shut down OS and finish.
	 * 
	 ***************************************************************************************************/
	
	SET_TESTEVENT("End A");
	SET_TESTEVENT("Calling ShutdownOS");
	ShutdownOS(E_OK);
	
	test_failed(OS_HERE);
}

/***************************************************************************************************
 *
 * TaskB
 * 
 ***************************************************************************************************/
TASK(TaskB)
{
	/* TaskB is activated by an alarm (on the hardware counter) expiry */
	SET_TESTEVENT("Start B");

	TEST(fake_timer_count, expected_TaskB_start_time);
	
	SET_TESTEVENT("End B");
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskB1_SW
 * 
 ***************************************************************************************************/
TASK(TaskB1_SW)
{
	/* TaskB is activated by an alarm (on a software counter) expiry */
	SET_TESTEVENT("Start B1_SW");
	
	SET_TESTEVENT("End B1_SW");
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskB2_SW
 * 
 ***************************************************************************************************/
TASK(TaskB2_SW)
{
	/* TaskB is activated by an alarm (on a software counter) expiry */
	SET_TESTEVENT("Start B2_SW");
	
	SET_TESTEVENT("End B2_SW");
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskC
 * 
 * Task will be activated by alarm, will not drop any log events (to save space).
 * It will compare an expected time with the current time. It will also use a period
 * variable to determine the next time it must awake.
 ***************************************************************************************************/
TASK(TaskC)
{
	TEST(fake_timer_count, expected_TaskC_start_time);
	if(TaskC_period) {
		expected_TaskC_start_time += TaskC_period;
	}
	TaskC_activation_count++;
	
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskD
 * 
 ***************************************************************************************************/
TASK(TaskD)
{
	TEST(fake_timer_count, expected_TaskD_start_time);
	if(TaskD_period) {
		expected_TaskD_start_time += TaskD_period;
	}
	TaskD_activation_count++;
	
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskE
 * 
 ***************************************************************************************************/
TASK(TaskE)
{
	TEST(fake_timer_count, expected_TaskE_start_time);
	if(TaskE_period) {
		expected_TaskE_start_time += TaskE_period;
	}
	TaskE_activation_count++;
	
	TerminateTask();
}

/***************************************************************************************************
 *
 * TaskF
 * 
 ***************************************************************************************************/
TASK(TaskF)
{
	TEST(fake_timer_count, expected_TaskF_start_time);
	TaskF_activation_count++;
	
	TerminateTask();
}

/***************************************************************************************************
 *
 * ISR Timer
 *
 * Raised when timer has expired. Call fake timer handler to process fake timer expiry.
 *  
 ***************************************************************************************************/
ISR(Timer)
{
	TickType tick;
	StatusType rc;
	AlarmBaseType base;

	if(log_isr) {
		SET_TESTEVENT("ISR Timer start");
	}
	
	/* GetAlarmBase can be called from an ISR
	 * 
	 * $Req: artf1184 $
	 */
	rc = GetAlarmBase(AlarmB_HW, &base);
	TEST(rc, E_OK);
	TEST(base.maxallowedvalue, 65535U);
	TEST(base.mincycle, 0U);
	TEST(base.ticksperbase, 1000U);
	TEST(OSMAXALLOWEDVALUE_Counter_HW, 65535U);
	TEST(OSMINCYCLE_Counter_HW, 0U);
	TEST(OSTICKSPERBASE_Counter_HW, 1000U);

	/* Also test the "system counter" constants. The system counter is not defined in the AUTOSAR or OSEK
	 * specifications and is left to the implementation to decide how to create one of these. Scalios defines
	 * that the system counter is the first one defined in OIL. In this test that is "Counter_HW". Check
	 * that the system counter values match with the specific counter values.
	 * 
	 * $Req: artf1206 $
	 */
	TEST(OSMAXALLOWEDVALUE, base.maxallowedvalue);
	TEST(OSMINCYCLE, base.mincycle);
	TEST(OSTICKSPERBASE, base.ticksperbase);
	
	ISR_invocation_count++;
	
	/* If the ISR_check_alarm handle is non-zero then we check that the alarm expiry
	 * time for that alarm is zero.
	 */
	if(ISR_check_alarm) {
		rc = GetAlarm(ISR_check_alarm, &tick);
		TEST(rc, E_OK);
		TEST(tick, 0);
	}
	
	fake_timer_ISR();
	
	/* If requested, the ISR will also set an alarm running via SetRelAlarm() or
	 * via SetAbsAlarm(), or will cancel and alarm.
	 * 
	 * For relative alarm setting, 10 ticks are used; for absolute alarm setting 10000 ticks are used.
	 */
	if(ISR_SetRelAlarm_alarm) {
		rc = SetRelAlarm(ISR_SetRelAlarm_alarm, 10U, 0);
		TEST(rc, E_OK);
		ISR_SetRelAlarm_alarm = 0;						/* Make the call just once */
	}
	if(ISR_SetAbsAlarm_alarm) {
		rc = SetAbsAlarm(ISR_SetAbsAlarm_alarm, 10000U, 0);
		TEST(rc, E_OK);
		ISR_SetAbsAlarm_alarm = 0;						/* Make the call just once */
	}
	if(ISR_CancelAlarm_alarm) {
		rc = CancelAlarm(ISR_CancelAlarm_alarm);
		TEST(rc, E_OK);
		ISR_CancelAlarm_alarm = 0;						/* Make the call just once */
	}
	if(log_isr) {
		SET_TESTEVENT("ISR Timer end");
	}
}

/***************************************************************************************************
 *
 * TaskX2
 *
 ***************************************************************************************************/
TASK(TaskX2)
{
	SET_TESTEVENT("TaskX2 start");
	WaitEvent(X2);
	SET_TESTEVENT("TaskX2 end");
	TerminateTask();
}

/***************************************************************************************************
 *
 * Alarm callback AlarmX1_HW_callback
 *
 ***************************************************************************************************/
ALARMCALLBACK(AlarmX1_HW_callback)
{
	SET_TESTEVENT("ALARMCALLBACK(AlarmX1_HW_callback)");
	
	/* Check that the API calls permitted work here
	 * 
	 * $Req: artf1212 
	 */
	SuspendAllInterrupts();
	/* The cat1 interrupt should not be handled here (tested via a log update and an error if it did).
	 */
	testing_trigger_cat1_isr();
	testing_dismiss_cat1_isr();
	ResumeAllInterrupts();
}

/***************************************************************************************************
 *
 * Alarm callback AlarmX1_HW_callback
 *
 ***************************************************************************************************/
ALARMCALLBACK(AlarmX2_SW_callback)
{
	/* Alarm callback routines run at OS level (i.e. no category 2 ISR can interrupt)
	 *
	 * $Req: artf1211 $
	 */

	SET_TESTEVENT("ALARMCALLBACK(AlarmX2_SW_callback) start");
	
	/* Turn on ISR logging */
	log_isr = 1;
	/* Try and make an ISR pending */
	testing_trigger_isr();
	/* Shouldn't have occurred; clear it down again */
	testing_dismiss_isr();
	
	log_isr = 0;
	
	SET_TESTEVENT("ALARMCALLBACK(AlarmX2_SW_callback) end");
}

