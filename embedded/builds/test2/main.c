/* Copyright (C) 2004, 2005, 2006, 2007, 2008 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-02-12 02:35:49 +0000 (Tue, 12 Feb 2008) $
 * $LastChangedRevision: 617 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/test2/main.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Test
 * 
 * Test2.
 * 
 * This test works through the dispatcher and checks that registers are restored properly between
 * task switching. It uses 32 32-bit values in order to ensure that all registers are flushed with
 * data (assuming the CPU has no more than 32 general purpose registers).
 * 
 * The 32-bit numbers were generated randomly using a radioactive decay source.
 * 
 * Note: stack space required by tasks is potentially high (the locals require 128 bytes to store and on a
 * machine with few registers the stack will be used).
 */

#include <osapp.h>
#include <framework.h>


/*
 * Test runs as follows:
 * 
 * Task A is lowest priority. Task C is the next highest. Task B is higher still. Task D is the highest.
 * 
 * Tasks C and D are extended tasks.
 * 
 * $Req: artf1085 $
 * $Req: artf1095 $
 * 
 * 1. A auto-starts, activates task B, which simply finishes. A resumes.
 * 2. A activates C, which activates B. B runs and returns. C resumes and activates D.
 * 3. D runs and sets off an ISR telling it to activate B. D blocks, B runs and finishes and then C resumes.
 * 4. C finishes and A resumes. A sends an event to D, which wakes D up. D finishes and waits again. A resumes.
 * 5. A sets off an ISR which sends an event to D. The ISR finishes and D resumes.
 * 6. D finishes and A resumes.
 * 7. A finishes and the test completes.
 * 
 * Each task and ISR checks its context at each stage to make sure registers are not disturbed.
 */
int do_test() {
	/************************PRE TEST SETUP OF EXPECTED RESULTS**************************/
	init_testevents();

	DEFINE_TESTEVENT("Before StartOS");
#ifdef USESTARTUPHOOK
	/* The startup hook should run before any tasks run or alarms expire. But note that it is not possible to detect this
	 * in the hook itself since the hook cannot make API calls that look at the state of alarms and tasks. The sequencing
	 * is checked via the testing log.
	 * 
	 * $Req: artf1215 $ 
	 * $Req: artf1117 $
	 */
	 
	DEFINE_TESTEVENT("StartupHook");			
#endif
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT("TaskA");
#endif
	DEFINE_TESTEVENT("Start A");				/* Task A is autostarted */
	DEFINE_TESTEVENT("A activating B");
	define_task_switch(TaskA, TaskB);
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");
	define_task_switch(TaskB, TaskA);
	DEFINE_TESTEVENT("A resuming");
	DEFINE_TESTEVENT("A activating C");
	define_task_switch(TaskA, TaskC);
	DEFINE_TESTEVENT("Start C");
	DEFINE_TESTEVENT("C activating B");
	define_task_switch(TaskC, TaskB);
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("C resuming");
	DEFINE_TESTEVENT("C activating D");
	define_task_switch(TaskC, TaskD);
	DEFINE_TESTEVENT("Start D");
	DEFINE_TESTEVENT("Triggering ISR");
	DEFINE_TESTEVENT("ISR starting");
	DEFINE_TESTEVENT("ISR activating requested task");		
	DEFINE_TESTEVENT("ISR finishing");
	DEFINE_TESTEVENT("D waiting");
	define_task_switch(TaskD, TaskB);
	DEFINE_TESTEVENT("Start B");
	DEFINE_TESTEVENT("End B");
	define_task_switch(TaskB, TaskC);
	DEFINE_TESTEVENT("C resuming");
	DEFINE_TESTEVENT("End C");
	define_task_switch(TaskC, TaskA);
	DEFINE_TESTEVENT("A resuming");	
	DEFINE_TESTEVENT("A setting Dummy to D");
	define_task_switch(TaskA, TaskD);
	DEFINE_TESTEVENT("D resuming after wait");
	DEFINE_TESTEVENT("D waiting 2nd");
	define_task_switch(TaskD, TaskA);
	DEFINE_TESTEVENT("A resuming");
	DEFINE_TESTEVENT("Triggering ISR");
	DEFINE_TESTEVENT("ISR starting");
	DEFINE_TESTEVENT("ISR setting Dummy to D");		
	DEFINE_TESTEVENT("ISR finishing");
	DEFINE_TESTEVENT("D resuming after 2nd wait");	
	DEFINE_TESTEVENT("End D");
	define_task_switch(TaskD, TaskA);
	DEFINE_TESTEVENT("End A");
	
	/* No call to PostTaskHook() when shutting down */
	/* $Req: artf1043 $ */
	DEFINE_TESTEVENT("Calling ShutdownOS");
#ifdef USESHUTDOWNHOOK
	DEFINE_TESTEVENT("ShutdownHook");
#endif
	DEFINE_TESTEVENT("After StartOS");
	
	/************************BEGIN TEST AND CHECK AGAINST EXPECTED RESULTS**************************/
	
	SET_TESTEVENT("Before StartOS");
	/* The default application mode is pre-defined.
	 * 
	 * $Req: artf1221 $
	 */
	StartOS(OSDEFAULTAPPMODE);
	SET_TESTEVENT("After StartOS");
	
	test_finished();
	return 0;
}

TaskType requested_task;			/* Indicates the task that the ISR should activate; 0 if none requested */
unat set_Dummy_to_D;				/* Flag indicates whether to set event Dummy to D */

#define DECLARE_LOCALS				volatile register uint32 c00, c01, c02, c03, c04, c05, c06, c07;\
									volatile register uint32 c08, c09, c10, c11, c12, c13, c14, c15;\
									volatile register uint32 c16, c17, c18, c19, c20, c21, c22, c23;\
									volatile register uint32 c24, c25, c26, c27, c28, c29, c30, c31

#define SET_LOCALS(p00, p01, p02, p03, p04, p05, p06, p07,\
				   p08, p09, p10, p11, p12, p13, p14, p15,\
				   p16, p17, p18, p19, p20, p21, p22, p23,\
				   p24, p25, p26, p27, p28, p29, p30, p31)\
									c00 = p00;\
									c01 = p01;\
									c02 = p02;\
									c03 = p03;\
									c04 = p04;\
									c05 = p05;\
									c06 = p06;\
									c07 = p07;\
									c08 = p08;\
									c09 = p09;\
									c10 = p10;\
									c11 = p11;\
									c12 = p12;\
									c13 = p13;\
									c14 = p14;\
									c15 = p15;\
									c16 = p16;\
									c17 = p17;\
									c18 = p18;\
									c19 = p19;\
									c20 = p20;\
									c21 = p21;\
									c22 = p22;\
									c23 = p23;\
									c24 = p24;\
									c25 = p25;\
									c26 = p26;\
									c27 = p27;\
									c28 = p28;\
									c29 = p29;\
									c30 = p30;\
									c31 = p31;

#define TEST_LOCALS(p00, p01, p02, p03, p04, p05, p06, p07,\
				    p08, p09, p10, p11, p12, p13, p14, p15,\
				    p16, p17, p18, p19, p20, p21, p22, p23,\
				    p24, p25, p26, p27, p28, p29, p30, p31)\
									TEST(c00, p00);\
									TEST(c01, p01);\
									TEST(c02, p02);\
									TEST(c03, p03);\
									TEST(c04, p04);\
									TEST(c05, p05);\
									TEST(c06, p06);\
									TEST(c07, p07);\
									TEST(c08, p08);\
									TEST(c09, p09);\
									TEST(c10, p10);\
									TEST(c11, p11);\
									TEST(c12, p12);\
									TEST(c13, p13);\
									TEST(c14, p14);\
									TEST(c15, p15);\
									TEST(c16, p16);\
									TEST(c17, p17);\
									TEST(c18, p18);\
									TEST(c19, p19);\
									TEST(c20, p20);\
									TEST(c21, p21);\
									TEST(c22, p22);\
									TEST(c23, p23);\
									TEST(c24, p24);\
									TEST(c25, p25);\
									TEST(c26, p26);\
									TEST(c27, p27);\
									TEST(c28, p28);\
									TEST(c29, p29);\
									TEST(c30, p30);\
									TEST(c31, p31);


/* Basic task, priority 1, auto-started
 * 
 * $Req: artf1143 $
 * 
 */
TASK(TaskA)
{
	StatusType rc;
	
	DECLARE_LOCALS;

	SET_TESTEVENT("Start A");
	
	SET_LOCALS(
			0x36DB5F73U,
			0x43388A08U,
			0x9184CFDEU,
			0xF734AF27U,
			0x708B2C20U,
			0x1CBDECC0U,
			0x285B97A3U,
			0x8B1B19C8U,
			0x5709472FU,
			0x402DBE27U,
			0x62DB1650U,
			0x8BF8849BU,
			0xB8CA623FU,
			0x8045D2D7U,
			0x38DA51A8U,
			0xAA220E71U,
			0xA4AAD29FU,
			0x381A9D54U,
			0xFF59A1B7U,
			0xC5C2FC17U,
			0x7F83BCA2U,
			0x1367A6F9U,
			0x02DA800AU,
			0xB3487F89U,
			0x5BD8D30FU,
			0xFE19F4ABU,
			0x4F222DA3U,
			0xCBD518B7U,
			0x4AE8D5A3U,
			0x80660E6EU,
			0x0BE49218U,
			0x0EB374E1U);

	SET_TESTEVENT("A activating B");
	rc = ActivateTask(TaskB); TEST(rc, E_OK);
	SET_TESTEVENT("A resuming");
		
	TEST_LOCALS(
			0x36DB5F73U,
			0x43388A08U,
			0x9184CFDEU,
			0xF734AF27U,
			0x708B2C20U,
			0x1CBDECC0U,
			0x285B97A3U,
			0x8B1B19C8U,
			0x5709472FU,
			0x402DBE27U,
			0x62DB1650U,
			0x8BF8849BU,
			0xB8CA623FU,
			0x8045D2D7U,
			0x38DA51A8U,
			0xAA220E71U,
			0xA4AAD29FU,
			0x381A9D54U,
			0xFF59A1B7U,
			0xC5C2FC17U,
			0x7F83BCA2U,
			0x1367A6F9U,
			0x02DA800AU,
			0xB3487F89U,
			0x5BD8D30FU,
			0xFE19F4ABU,
			0x4F222DA3U,
			0xCBD518B7U,
			0x4AE8D5A3U,
			0x80660E6EU,
			0x0BE49218U,
			0x0EB374E1U);
	
	SET_TESTEVENT("A activating C");
	rc = ActivateTask(TaskC); TEST(rc, E_OK);
	SET_TESTEVENT("A resuming");

	TEST_LOCALS(
			0x36DB5F73U,
			0x43388A08U,
			0x9184CFDEU,
			0xF734AF27U,
			0x708B2C20U,
			0x1CBDECC0U,
			0x285B97A3U,
			0x8B1B19C8U,
			0x5709472FU,
			0x402DBE27U,
			0x62DB1650U,
			0x8BF8849BU,
			0xB8CA623FU,
			0x8045D2D7U,
			0x38DA51A8U,
			0xAA220E71U,
			0xA4AAD29FU,
			0x381A9D54U,
			0xFF59A1B7U,
			0xC5C2FC17U,
			0x7F83BCA2U,
			0x1367A6F9U,
			0x02DA800AU,
			0xB3487F89U,
			0x5BD8D30FU,
			0xFE19F4ABU,
			0x4F222DA3U,
			0xCBD518B7U,
			0x4AE8D5A3U,
			0x80660E6EU,
			0x0BE49218U,
			0x0EB374E1U);

	SET_TESTEVENT("A setting Dummy to D");
	rc = SetEvent(TaskD, Dummy); TEST(rc, E_OK);
	SET_TESTEVENT("A resuming");
	
	TEST_LOCALS(
			0x36DB5F73U,
			0x43388A08U,
			0x9184CFDEU,
			0xF734AF27U,
			0x708B2C20U,
			0x1CBDECC0U,
			0x285B97A3U,
			0x8B1B19C8U,
			0x5709472FU,
			0x402DBE27U,
			0x62DB1650U,
			0x8BF8849BU,
			0xB8CA623FU,
			0x8045D2D7U,
			0x38DA51A8U,
			0xAA220E71U,
			0xA4AAD29FU,
			0x381A9D54U,
			0xFF59A1B7U,
			0xC5C2FC17U,
			0x7F83BCA2U,
			0x1367A6F9U,
			0x02DA800AU,
			0xB3487F89U,
			0x5BD8D30FU,
			0xFE19F4ABU,
			0x4F222DA3U,
			0xCBD518B7U,
			0x4AE8D5A3U,
			0x80660E6EU,
			0x0BE49218U,
			0x0EB374E1U);
	
	SET_TESTEVENT("Triggering ISR");
	set_Dummy_to_D = 1;
	testing_trigger_isr();
	
	TEST_LOCALS(
			0x36DB5F73U,
			0x43388A08U,
			0x9184CFDEU,
			0xF734AF27U,
			0x708B2C20U,
			0x1CBDECC0U,
			0x285B97A3U,
			0x8B1B19C8U,
			0x5709472FU,
			0x402DBE27U,
			0x62DB1650U,
			0x8BF8849BU,
			0xB8CA623FU,
			0x8045D2D7U,
			0x38DA51A8U,
			0xAA220E71U,
			0xA4AAD29FU,
			0x381A9D54U,
			0xFF59A1B7U,
			0xC5C2FC17U,
			0x7F83BCA2U,
			0x1367A6F9U,
			0x02DA800AU,
			0xB3487F89U,
			0x5BD8D30FU,
			0xFE19F4ABU,
			0x4F222DA3U,
			0xCBD518B7U,
			0x4AE8D5A3U,
			0x80660E6EU,
			0x0BE49218U,
			0x0EB374E1U);
	
	SET_TESTEVENT("End A");
	
	SET_TESTEVENT("Calling ShutdownOS");
	ShutdownOS(E_OK);	
	
	test_failed(OS_HERE);
}

/* Basic task, priority 3, activated by A and C */
TASK(TaskB)
{
	StatusType rc;
	DECLARE_LOCALS;
	
	SET_TESTEVENT("Start B");

	SET_LOCALS(
			0x6F7A1882U,
			0x607B02AAU,
			0x7A9406B4U,
			0xFD9825EBU,
			0x40E862E3U,
			0xDA8569CAU,
			0xFC0F3E31U,
			0xE7DC2A78U,
			0x9485022BU,
			0x802FCE59U,
			0x3C341D41U,
			0xB5378DF8U,
			0x7581ED98U,
			0xE120C1E0U,
			0x26C4660FU,
			0x82151640U,
			0xD998E17BU,
			0x9C2DA1BFU,
			0x21B3BF27U,
			0x39AA065DU,
			0xB457088CU,
			0x84FA5C2FU,
			0xA02F22BEU,
			0x0B38D799U,
			0xA5344923U,
			0x05B1DE59U,
			0x06BF6F9FU,
			0x83E2795BU,
			0x5D92FB34U,
			0x8A7DD22CU,
			0x55FA2DA4U,
			0xF96B6028U);
	
	/* This isn't really needed since there is no pre-emption between setting and testing,
	 * but included here for completeness: a future revision of this test might have interrupts
	 * going off at random points.
	 */
	TEST_LOCALS(
			0x6F7A1882U,
			0x607B02AAU,
			0x7A9406B4U,
			0xFD9825EBU,
			0x40E862E3U,
			0xDA8569CAU,
			0xFC0F3E31U,
			0xE7DC2A78U,
			0x9485022BU,
			0x802FCE59U,
			0x3C341D41U,
			0xB5378DF8U,
			0x7581ED98U,
			0xE120C1E0U,
			0x26C4660FU,
			0x82151640U,
			0xD998E17BU,
			0x9C2DA1BFU,
			0x21B3BF27U,
			0x39AA065DU,
			0xB457088CU,
			0x84FA5C2FU,
			0xA02F22BEU,
			0x0B38D799U,
			0xA5344923U,
			0x05B1DE59U,
			0x06BF6F9FU,
			0x83E2795BU,
			0x5D92FB34U,
			0x8A7DD22CU,
			0x55FA2DA4U,
			0xF96B6028U);
		
	SET_TESTEVENT("End B");
	rc = TerminateTask(); TEST(rc, E_OK);
	test_failed(OS_HERE);
}

/* Extended task, priority 2, activated by A */
TASK(TaskC)
{
	StatusType rc;
	DECLARE_LOCALS;
	
	SET_TESTEVENT("Start C");

	SET_LOCALS(
			0x6E41F43DU,
			0x4EBA420CU,
			0x15E73537U,
			0xF44DAED9U,
			0xEDEBED80U,
			0xF80F1CE9U,
			0x1C843D7FU,
			0xBB5D65AAU,
			0x469AD9ACU,
			0x2CE4A63FU,
			0x39C5D4E7U,
			0x506E9543U,
			0x5C23A6C0U,
			0xEF8B0D85U,
			0xD206307DU,
			0x43799731U,
			0x63ED6D81U,
			0x4056C706U,
			0x46C1604CU,
			0x2AA53D41U,
			0xF66CFE52U,
			0xFA8901FBU,
			0x86AFEDF0U,
			0xC72AE682U,
			0xC9D760C4U,
			0x9BBEC8B7U,
			0xBA735A46U,
			0x09A5F7C2U,
			0x0F0A2B67U,
			0x63B799F7U,
			0x0FA6DCFCU,
			0x89A8C02AU);
	
	SET_TESTEVENT("C activating B");
	rc = ActivateTask(TaskB); TEST(rc, E_OK);
	SET_TESTEVENT("C resuming");
	
	TEST_LOCALS(
			0x6E41F43DU,
			0x4EBA420CU,
			0x15E73537U,
			0xF44DAED9U,
			0xEDEBED80U,
			0xF80F1CE9U,
			0x1C843D7FU,
			0xBB5D65AAU,
			0x469AD9ACU,
			0x2CE4A63FU,
			0x39C5D4E7U,
			0x506E9543U,
			0x5C23A6C0U,
			0xEF8B0D85U,
			0xD206307DU,
			0x43799731U,
			0x63ED6D81U,
			0x4056C706U,
			0x46C1604CU,
			0x2AA53D41U,
			0xF66CFE52U,
			0xFA8901FBU,
			0x86AFEDF0U,
			0xC72AE682U,
			0xC9D760C4U,
			0x9BBEC8B7U,
			0xBA735A46U,
			0x09A5F7C2U,
			0x0F0A2B67U,
			0x63B799F7U,
			0x0FA6DCFCU,
			0x89A8C02AU);

	SET_TESTEVENT("C activating D");
	rc = ActivateTask(TaskD); TEST(rc, E_OK);
	SET_TESTEVENT("C resuming");

	TEST_LOCALS(
			0x6E41F43DU,
			0x4EBA420CU,
			0x15E73537U,
			0xF44DAED9U,
			0xEDEBED80U,
			0xF80F1CE9U,
			0x1C843D7FU,
			0xBB5D65AAU,
			0x469AD9ACU,
			0x2CE4A63FU,
			0x39C5D4E7U,
			0x506E9543U,
			0x5C23A6C0U,
			0xEF8B0D85U,
			0xD206307DU,
			0x43799731U,
			0x63ED6D81U,
			0x4056C706U,
			0x46C1604CU,
			0x2AA53D41U,
			0xF66CFE52U,
			0xFA8901FBU,
			0x86AFEDF0U,
			0xC72AE682U,
			0xC9D760C4U,
			0x9BBEC8B7U,
			0xBA735A46U,
			0x09A5F7C2U,
			0x0F0A2B67U,
			0x63B799F7U,
			0x0FA6DCFCU,
			0x89A8C02AU);

	SET_TESTEVENT("End C");
	
	TerminateTask();
	test_failed(OS_HERE);
}

/* Extended task, priority 4 */
TASK(TaskD)
{
	StatusType rc;
	DECLARE_LOCALS;
	
	SET_TESTEVENT("Start D");

	SET_LOCALS(
			0x46D3C6CBU,
			0xE3F36C75U,
			0x7D6301F9U,
			0x953F8D0AU,
			0x6E8795BEU,
			0x59E545DAU,
			0x5603F0CAU,
			0x2B7745BAU,
			0x1080574BU,
			0xE0F15346U,
			0xB968B633U,
			0xD2290A5CU,
			0xDA4FD12BU,
			0x12C06B7AU,
			0x40E31A48U,
			0x794DF662U,
			0xAE51D9BBU,
			0xF30704BBU,
			0x1CC0D8C8U,
			0x5B70FE4BU,
			0xDA34B424U,
			0xA3707D5CU,
			0x394AE944U,
			0x405294A9U,
			0xBB7F3F9AU,
			0xA833B102U,
			0xFCD1AFD4U,
			0x7A49BAC2U,
			0xFF74BECDU,
			0x75568B5CU,
			0x9C83442AU,
			0xDE52D1A8U);
	
	requested_task = TaskB;
	SET_TESTEVENT("Triggering ISR");
	testing_trigger_isr();
	
	SET_TESTEVENT("D waiting");
	rc = ClearEvent(Dummy); TEST(rc, E_OK);
	rc = WaitEvent(Dummy); TEST(rc, E_OK);
	SET_TESTEVENT("D resuming after wait");
			
	TEST_LOCALS(
			0x46D3C6CBU,
			0xE3F36C75U,
			0x7D6301F9U,
			0x953F8D0AU,
			0x6E8795BEU,
			0x59E545DAU,
			0x5603F0CAU,
			0x2B7745BAU,
			0x1080574BU,
			0xE0F15346U,
			0xB968B633U,
			0xD2290A5CU,
			0xDA4FD12BU,
			0x12C06B7AU,
			0x40E31A48U,
			0x794DF662U,
			0xAE51D9BBU,
			0xF30704BBU,
			0x1CC0D8C8U,
			0x5B70FE4BU,
			0xDA34B424U,
			0xA3707D5CU,
			0x394AE944U,
			0x405294A9U,
			0xBB7F3F9AU,
			0xA833B102U,
			0xFCD1AFD4U,
			0x7A49BAC2U,
			0xFF74BECDU,
			0x75568B5CU,
			0x9C83442AU,
			0xDE52D1A8U);

	SET_TESTEVENT("D waiting 2nd");
	rc = ClearEvent(Dummy); TEST(rc, E_OK);
	rc = WaitEvent(Dummy); TEST(rc, E_OK);
	SET_TESTEVENT("D resuming after 2nd wait");

	TEST_LOCALS(
			0x46D3C6CBU,
			0xE3F36C75U,
			0x7D6301F9U,
			0x953F8D0AU,
			0x6E8795BEU,
			0x59E545DAU,
			0x5603F0CAU,
			0x2B7745BAU,
			0x1080574BU,
			0xE0F15346U,
			0xB968B633U,
			0xD2290A5CU,
			0xDA4FD12BU,
			0x12C06B7AU,
			0x40E31A48U,
			0x794DF662U,
			0xAE51D9BBU,
			0xF30704BBU,
			0x1CC0D8C8U,
			0x5B70FE4BU,
			0xDA34B424U,
			0xA3707D5CU,
			0x394AE944U,
			0x405294A9U,
			0xBB7F3F9AU,
			0xA833B102U,
			0xFCD1AFD4U,
			0x7A49BAC2U,
			0xFF74BECDU,
			0x75568B5CU,
			0x9C83442AU,
			0xDE52D1A8U);
	
	SET_TESTEVENT("End D");
	
	TerminateTask();
	test_failed(OS_HERE);
}

/* Category 2 ISRs declared according to OSEK specification.
 * 
 * $Req: artf1150 $
 */
ISR(ISRX)
{
	StatusType rc;
	DECLARE_LOCALS;
	
	SET_LOCALS(
			0xCEEF6DFDU,
			0x6D18D3FFU,
			0x477CD7E3U,
			0x0B6F9ABFU,
			0xE5AB39CEU,
			0xC7F81930U,
			0x100E514EU,
			0xA06F1DBDU,
			0x792B0F2EU,
			0x6A488AA9U,
			0xB2CAFF8CU,
			0x61FDD2A3U,
			0xED753218U,
			0xEFCAEBD3U,
			0x90488771U,
			0x27C363C4U,
			0xE1C88B6DU,
			0xDEFCD725U,
			0xD045E7A4U,
			0x14750012U,
			0xD7F9E792U,
			0x61FE2CAAU,
			0xDBD0DCF4U,
			0x1009AB25U,
			0x379BE392U,
			0xF19D5875U,
			0x37693C1FU,
			0xB2CD8252U,
			0xAFCF311BU,
			0xEF45E6E4U,
			0x48BC7D61U,
			0xCBA1BA5FU);
	
	SET_TESTEVENT("ISR starting");
	/* Carry out event setting if requested */
	if(set_Dummy_to_D) {
		/* $Req: artf1162 $ */
		SET_TESTEVENT("ISR setting Dummy to D");
		rc = SetEvent(TaskD, Dummy); TEST(rc, E_OK);
		set_Dummy_to_D = 0;
	}
	if(requested_task) {
		SET_TESTEVENT("ISR activating requested task");
		rc = ActivateTask(requested_task); TEST(rc, E_OK);
		requested_task = 0;
	}
	
	TEST_LOCALS(
			0xCEEF6DFDU,
			0x6D18D3FFU,
			0x477CD7E3U,
			0x0B6F9ABFU,
			0xE5AB39CEU,
			0xC7F81930U,
			0x100E514EU,
			0xA06F1DBDU,
			0x792B0F2EU,
			0x6A488AA9U,
			0xB2CAFF8CU,
			0x61FDD2A3U,
			0xED753218U,
			0xEFCAEBD3U,
			0x90488771U,
			0x27C363C4U,
			0xE1C88B6DU,
			0xDEFCD725U,
			0xD045E7A4U,
			0x14750012U,
			0xD7F9E792U,
			0x61FE2CAAU,
			0xDBD0DCF4U,
			0x1009AB25U,
			0x379BE392U,
			0xF19D5875U,
			0x37693C1FU,
			0xB2CD8252U,
			0xAFCF311BU,
			0xEF45E6E4U,
			0x48BC7D61U,
			0xCBA1BA5FU);
	
	testing_dismiss_isr();
	SET_TESTEVENT("ISR finishing");
}

int main()
{
	do_test();
#ifdef REINIT_FLAG
	do_test();
#endif
	test_passed();
}
