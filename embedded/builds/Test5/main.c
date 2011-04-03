/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-11 23:35:25 +0000 (Tue, 11 Mar 2008) $
 * $LastChangedRevision: 677 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test5/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test5. Stack overflow is handled properly.
 * 
 * The test will hit test_passed() if stack checking is turned off (i.e. using the non-checking OS library).
 * The test will hit a stack overflow trap (which may stop the debugger) then test_passed(). If the test stops running
 * then hit "go" until test_passed() is reached.
 * 
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
	/* TaskA and TaskB are autostarted. TaskB is higher priority than TaskB so should run first */
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("Main");
#endif
	DEFINE_TESTEVENT("Start Main");

#ifdef USESTACKMONITORING
#ifdef TEST_BASIC_STACKOVERFLOW
	define_task_switch(Main, TaskBasic);
	DEFINE_TESTEVENT("Start TaskBasic");
#endif
#ifdef TEST_EXTENDED_STACKOVERFLOW
	define_task_switch(Main, TaskExtended);
	DEFINE_TESTEVENT("Start TaskExtended");
#endif
#ifdef TEST_ISR_STACKOVERFLOW
	DEFINE_TESTEVENT("Start ISRX");
#endif
#endif
	
#ifdef USESHUTDOWNHOOK
	DEFINE_TESTEVENT("ShutdownHook");
#endif

	/************************************************************************************************
	 *************************BEGIN TEST AND CHECK AGAINST EXPECTED RESULTS**************************
	 ************************************************************************************************
	 */

	SET_TESTEVENT("Before StartOS");
	StartOS(OSDEFAULTAPPMODE);
	
	test_finished();
	return 0;
}

/* Slightly complex recursive function to fool an optimizing compiler
 * into not trying to eliminate the function.
 */
volatile int y;
int recurse1(int);
int recurse2(int);

int recurse1(int x)
{
    y += recurse2(y + x);
}

int recurse2(int x)
{
    y += recurse1(y + x);
}

TASK(Main)
{
	SET_TESTEVENT("Start Main");
#ifdef USESTACKMONITORING
	/* Expect to shut down soon with a stack fault */
	test_shutdown_code = 1;
	shutdown_code = E_OS_STACKFAULT;
	
#ifdef TEST_BASIC_STACKOVERFLOW
	ActivateTask(TaskBasic);
#endif
#ifdef TEST_EXTENDED_STACKOVERFLOW
	ActivateTask(TaskExtended);
#endif
#ifdef TEST_ISR_STACKOVERFLOW
	testing_trigger_isr();
#endif
	test_failed(OS_HERE);
#else
	ShutdownOS(E_OK);
#endif
}

TASK(TaskExtended)
{
	SET_TESTEVENT("Start TaskExtended");
	/* $Req: artf1039 $ */
	recurse1(1);
}

TASK(TaskBasic)
{
	SET_TESTEVENT("Start TaskBasic");
	/* $Req: artf1039 $ */
	recurse1(1);
}

ISR(ISRX)
{
	SET_TESTEVENT("Start ISRX");
	/* $Req: artf1039 $ */
	recurse1(1);
}

int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
