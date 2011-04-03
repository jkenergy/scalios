/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-11 23:35:25 +0000 (Tue, 11 Mar 2008) $
 * $LastChangedRevision: 677 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test8/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test8: build a sizeable test to ensure large numbers of OSEK OS objects are supported.
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
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("Task16");
#endif
	define_task_switch(Task16, Task15);
	define_task_switch(Task15, Task14);
	define_task_switch(Task14, Task13);
	define_task_switch(Task13, Task12);
	define_task_switch(Task12, Task11);
	define_task_switch(Task11, Task10);
	define_task_switch(Task10, Task6);
	define_task_switch(Task6, Task3);
	define_task_switch(Task3, Task7);
	define_task_switch(Task7, Task5);
	define_task_switch(Task5, Task2);
	define_task_switch(Task2, Task8);
	define_task_switch(Task8, Task4);
	define_task_switch(Task4, Task1);
	define_task_switch(Task1, Task9);
	define_task_switch(Task9, Task5);
	define_task_switch(Task5, Task1);
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
	StartOS(Mode2);
	SET_TESTEVENT("After StartOS");
	
	test_finished();
	return 0;
}

TASK(Task1)
{
	/* Support more than 8 events per task.
	 * 
	 * $Req: artf1089 $
	 */
	SetEvent(Task1, E1 | E2 | E3 | E4 | E5 | E6 | E7 | E8 | E9 | E10 | E11 | E12 | E13 | E14 | E15);
	ActivateTask(Task9);
	ActivateTask(Task5);
	Schedule();
	SET_TESTEVENT("Calling ShutdownOS");
	ShutdownOS(E_OK);
}

TASK(Task2)
{
	ActivateTask(Task4);
	ActivateTask(Task8);
	TerminateTask();
}

TASK(Task3)
{
	ActivateTask(Task5);
	ActivateTask(Task7);
	/* When this task terminates, the internal resource is released and Task7 then Task5 runs.
	 * 
	 * $Req: artf1131 $
	 */
	TerminateTask();
}

TASK(Task4)
{
	TerminateTask();
}

TASK(Task5)
{
	TerminateTask();
}

TASK(Task6)
{
	TerminateTask();
}

TASK(Task7)
{
	TerminateTask();
}

TASK(Task8)
{
	/* Support at least 8 resources.
	 * 
	 * $Req: artf1091 $
	 */
	GetResource(Res8);
	ReleaseResource(Res8);
	TerminateTask();
}

/* This task is in an internal resource group. Supports more than two internal resources.
 * 
 * $Req: artf1092 $
 */
TASK(Task9)
{
	TerminateTask();
}

TASK(Task10)
{
	TerminateTask();
}

TASK(Task11)
{
	TerminateTask();
}
TASK(Task12)
{
	TerminateTask();
}

TASK(Task13)
{
	TerminateTask();
}

TASK(Task14)
{
	TerminateTask();
}

TASK(Task15)
{
	TerminateTask();
}

/* 16th task. All tasks have unique priorities.
 * 
 * $Req: artf1087 $
 * $Req: artf1090 $
 */
TASK(Task16)
{
	TerminateTask();
}

int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
