/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-21 20:03:32 +0000 (Fri, 21 Mar 2008) $
 * $LastChangedRevision: 687 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test9/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test9: Test of schedule tables.
 * 
 * Subtest 1:
 *   Negative API tests (check that error conditions are generated)
 *
 * Subtest 2:
 *   Test of long-term periodic behaviour. Pseudo-hardware counter is used to drive a periodic
 *   alarm with period 1000, which increments a software counter. This counter drives three tables,
 *   A, B and C, which run like this:
 * 
 *     A, A, B, C, C, A, A, B, C, C, and so on.
 * 
 *   On the second run of A, the table is 'nexted' to run B. B is one-shot runs and alternatively 'nexts' C or A. C runs
 *   like A (i.e. 'nexting' B after the second run).
 * 
 *   Simultaneouly with the above, an unrelated schedule table D runs periodically (driven by the same software counter).
 * 
 *   Each schedule table runs a set of tasks that self-check against their expected running times, both in terms of 
 *   the 'time' on the hardware counter and on the software counter, using the GetCounterValue() API call. Note that
 *   the expected times are calculated in 32-bits and using the modulo operator in C.
 *
 *   In schedule table C there are no tasks at an offset of zero (i.e. the initial expiry point is non-zero).
 * 
 *   The actions associated with expiry points cover both task activations and indirect task activation via event setting.
 *
 *   In table C there are tasks that are activated at the same expiry point, and multiple expiry points.
 * 
 *   In table B there is just one expiry point - at time zero - consisting of several task activations and several event settings.
 * 
 *   In table A there are several expiry points each consisting of just one task activation or one event setting.
 * 
 *   This subtest runs long enough for there to be more than 2^32 ticks of the software counter. Consequently, no logs
 *   are dropped into the event log during the run of the subtest.
 * 
 * Subtest 3:
 */

#include <osapp.h>
#include <framework.h>

/***************************************************************************************************
 *                                                                                                 *
 * PRE-TEST SETUP OF EXPECTED RESULTS                                                              *
 *                                                                                                 *
 ***************************************************************************************************/
int do_test() {
	int i;

	init_testevents();
	
	DEFINE_TESTEVENT("Before StartOS");
#ifdef USESTARTUPHOOK
	DEFINE_TESTEVENT("StartupHook");
#endif

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
	/* Support multiple application modes.
	 * 
	 * $Req: artf1094 $
	 */
	StartOS(OSDEFAULTAPPMODE);
	SET_TESTEVENT("After StartOS");
	
	test_finished();
	return 0;
}

TASK(TaskX)
{
	SET_TESTEVENT("Calling ShutdownOS");
	ShutdownOS(E_OK);
}

/* These should agree with the definitions in the OIL file */
TickType TableA_length = 997U;
TickType TableB_length = 991U;
TickType TableC_length = 983U;
TickType TableD_length = 977U;
TickType TableE_length = 977U;

TickType TableA_start = 0U;
TickType TableB_start = 0U;
TickType TableC_start = 0U;
TickType TableD_start = 0U;
TickType TableE_start = 500U;

TickType TaskA_1_offset = 0;
TickType TaskA_2_offset = 245U;
TickType TaskA_3_offset = 392U;

TickType TaskB_1_offset = 0;
TickType TaskB_2_offset = 0;
TickType TaskB_3_offset = 0;
TickType TaskB_4_offset = 0;

TickType TaskC_1_offset = 445U;
TickType TaskC_2_offset = 445U;
TickType TaskC_3_offset = 665U;
TickType TaskC_4_offset = 666U;

TickType TaskD_1_offset = 123U;
TickType TaskD_2_offset = 777U;

TickType TaskE_1_offset = 111U;
TickType TaskE_2_offset = 888U;

TickType TaskA_1_expected_time;
TickType TaskA_2_expected_time;
TickType TaskA_3_expected_time;
TickType TaskB_1_expected_time;
TickType TaskB_2_expected_time;
TickType TaskB_3_expected_time;
TickType TaskB_4_expected_time;
TickType TaskC_1_expected_time;
TickType TaskC_2_expected_time;
TickType TaskC_3_expected_time;
TickType TaskC_4_expected_time;
TickType TaskD_1_expected_time;
TickType TaskD_2_expected_time;
TickType TaskE_1_expected_time;
TickType TaskE_2_expected_time;

TickType now;
StatusType rc;

int TableA_count;
int TableB_count;
int TableC_count;
int TableD_count;
int TableE_count;

int tmp_time;

TickType now_Counter_SW;

TickType advance_Counter_SW(TickType a, TickType b)
{
	int tmp = a + b;
	tmp = tmp % (OSMAXALLOWEDVALUE_Counter_SW + 1);
	assert(tmp <= OSMAXALLOWEDVALUE_Counter_SW);
	return (TickType)tmp;
}

TASK(Init)
{
	/* Set hardware alarm going; could have just autostarted it, but this is just as good.
	 * 
	 * Set the hardware alarm ticking so that 1000 hardware ticks = one software counter tick (the
	 * software counter drives most of the schedule tables in this test).
	 */
	rc = SetAbsAlarm(Alarm_HW, 500U, 1000U);
	TEST(rc, E_OK);
	
	/* Set table A going; rest will follow */
	TaskA_1_expected_time = advance_Counter_SW(50U, TaskA_1_offset);
	rc = StartScheduleTableAbs(A, 50U);
	TEST(rc, E_OK);
	
	TerminateTask();
}

TASK(Idle)
{
	for(;;) {
		fake_timer_advance(789U);
	}
}

/* A, A, B, C, C, A, A, B, C, C, and so on.
 */
TASK(TaskA_1)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskA_1_expected_time);

	/* Set up rest of tasks in table A */
	TaskA_2_expected_time = advance_Counter_SW(TaskA_1_expected_time, TaskA_2_offset - TaskA_1_offset);
	TaskA_3_expected_time = advance_Counter_SW(TaskA_1_expected_time, TaskA_3_offset - TaskA_1_offset);

	/* First time through the table, the table loops;
	 * Second time through, table 'B' is nexted.
	 */
	TableA_count++;
	if(TableA_count % 2 == 1) {
		/* First pass */
		 TaskA_1_expected_time = advance_Counter_SW(TaskA_1_expected_time, TableA_length);
	}
	else {
		/* Second pass; set up for table B to run */
		TaskB_1_expected_time = advance_Counter_SW(TaskA_1_expected_time, TableA_length - TaskA_1_offset);
		TaskB_1_expected_time = advance_Counter_SW(TaskB_1_expected_time, TaskB_1_offset);
		
		rc = NextScheduleTable(A, B);
		TEST(rc, E_OK);
	}

	TerminateTask();
}

TASK(TaskA_2)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskA_2_expected_time);
	TerminateTask();
}

TASK(TaskA_3)
{
	for(;;) {
		ClearEvent(X1);
		WaitEvent(X1);
		rc = GetCounterValue(Counter_SW, &now);
		TEST(rc, E_OK);
		TEST(now, TaskA_3_expected_time);
	}
}

/* A, A, B, C, C, A, A, B, C, C, and so on.
 */
TASK(TaskB_1)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskB_1_expected_time);

	/* Set up start of remaining tasks in this table */	
	TaskB_2_expected_time = advance_Counter_SW(TaskB_1_expected_time, TaskB_2_offset - TaskB_1_offset);
	TaskB_3_expected_time = advance_Counter_SW(TaskB_1_expected_time, TaskB_3_offset - TaskB_1_offset);
	TaskB_4_expected_time = advance_Counter_SW(TaskB_1_expected_time, TaskB_4_offset - TaskB_1_offset);

	TableB_count++;
	
	/* Set up start of first task in table C */
	TaskC_1_expected_time = advance_Counter_SW(TaskB_1_expected_time, TableB_length - TaskB_1_offset);
	TaskC_1_expected_time = advance_Counter_SW(TaskC_1_expected_time, TaskC_1_offset);	

	rc = NextScheduleTable(B, C);
	TEST(rc, E_OK);
	
	TerminateTask();
}

TASK(TaskB_2)
{
	for(;;) {
		ClearEvent(X1);
		WaitEvent(X1);
		rc = GetCounterValue(Counter_SW, &now);
		TEST(rc, E_OK);
		TEST(now, TaskB_2_expected_time);
	}
}

TASK(TaskB_3)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskB_3_expected_time);
	TerminateTask();
}

TASK(TaskB_4)
{
	for(;;) {
		ClearEvent(X2);
		WaitEvent(X2);
		rc = GetCounterValue(Counter_SW, &now);
		TEST(rc, E_OK);
		TEST(now, TaskB_4_expected_time);
	}
	TerminateTask();
}

/* A, A, B, C, C, A, A, B, C, C, and so on.
 */
TASK(TaskC_1)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskC_1_expected_time);

	/* Set up expected times for the rest of the tasks in table C */
	TaskC_2_expected_time = advance_Counter_SW(TaskC_1_expected_time, TaskC_2_offset - TaskC_1_offset);
	TaskC_3_expected_time = advance_Counter_SW(TaskC_1_expected_time, TaskC_3_offset - TaskC_1_offset);
	TaskC_4_expected_time = advance_Counter_SW(TaskC_1_expected_time, TaskC_4_offset - TaskC_1_offset);

	TableB_count++;
	if(TableB_count % 2 == 1) {
		/* First pass through the table; loop again */
		TaskC_1_expected_time = advance_Counter_SW(TaskC_1_expected_time, TableC_length);
	}
	else {
		/* Second pass through the table */
		/* Set up start of first task in table A */
		TaskA_1_expected_time = advance_Counter_SW(TaskC_1_expected_time, TableC_length - TaskC_1_offset);
		TaskA_1_expected_time = advance_Counter_SW(TaskA_1_expected_time, TaskA_1_offset);
		
		rc = NextScheduleTable(C, A);
		TEST(rc, E_OK);
	}
		
	TerminateTask();
}

TASK(TaskC_2)
{
	for(;;) {
		ClearEvent(X2);
		WaitEvent(X2);
		rc = GetCounterValue(Counter_SW, &now);
		TEST(rc, E_OK);
		TEST(now, TaskC_2_expected_time);
	}
}

TASK(TaskC_3)
{
	rc = GetCounterValue(Counter_SW, &now);
	TEST(rc, E_OK);
	TEST(now, TaskC_3_expected_time);
	TerminateTask();
}

TASK(TaskC_4)
{
	for(;;) {
		ClearEvent(X2);
		WaitEvent(X2);
		rc = GetCounterValue(Counter_SW, &now);
		TEST(rc, E_OK);
		TEST(now, TaskC_4_expected_time);
	}
}

TASK(TaskD_1)
{
	TerminateTask();
}

TASK(TaskD_2)
{
	TerminateTask();
}

TASK(TaskE_1)
{
	TerminateTask();
}

TASK(TaskE_2)
{
	TerminateTask();
}

ISR(Timer)
{			
	fake_timer_ISR();
	rc = GetCounterValue(Counter_SW, &now_Counter_SW);
	TEST(rc, E_OK);
}


int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
