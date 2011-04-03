/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-11 23:35:25 +0000 (Tue, 11 Mar 2008) $
 * $LastChangedRevision: 677 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/Test7/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
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
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
#endif
	DEFINE_TESTEVENT("Start A");
		
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
	
	/***********Finish and shutdown to return to main()************/
	SET_TESTEVENT("End A");
	
	SET_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	SET_TESTEVENT("ShutdownHook");
#endif
	ShutdownOS(E_OK);
	
	test_failed(OS_HERE);
}

TASK(TaskB)
{	
	StatusType rc;
	TaskStateType state;
	int i;

	SET_TESTEVENT("Start B");
	
	SET_TESTEVENT("End B");
	
	TerminateTask();
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
