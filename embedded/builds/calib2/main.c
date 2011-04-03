/* Not intended for actual source code, used to test code generation etc. */

#include <osapp.h>
#include <framework.h>
#include <osint.h>


/* Determine the overheads on the kernel stack. This needs to run several scenarios to
 * work this out:
 * 
 * BT->API->dispatch->BT
 * BT->API->dispatch->ET
 * ET->API->dispatch->BT
 * ET->API->dispatch->ET
 * 
 * To do this there are two "server" tasks, one basic and one extended. These tasks are
 * kicked off and record the stack overheads into an array and then return.
 * 
 */



#define NUM_SCENARIOS (14)

#define ACTIVATE_TASK_BT2ET			(0)
#define ACTIVATE_TASK_BT2BT			(1)
#define ACTIVATE_TASK_ET2ET			(2)
#define ACTIVATE_TASK_ET2BT			(3)

#define RELEASE_RESOURCE_BT2ET		(4)
#define RELEASE_RESOURCE_BT2BT		(5)
#define RELEASE_RESOURCE_ET2ET		(6)
#define RELEASE_RESOURCE_ET2BT		(7)

#define SET_EVENT_BT2ET				(8)
#define SET_EVENT_ET2ET				(9)

#define SCHEDULE_BT2ET				(10)
#define SCHEDULE_BT2BT				(11)
#define SCHEDULE_ET2ET				(12)
#define SCHEDULE_ET2BT				(13)

/* @todo need to do counter expiry/increment calls too */

volatile os_stackp start[NUM_SCENARIOS];
volatile os_stackp end[NUM_SCENARIOS];
volatile long size[NUM_SCENARIOS];
volatile int call;

/* The target-specific assember defines two working task functions:
 * 
 * TaskSPSave1
 * TaskSPSave2
 * 
 * Tasks named according to this will save the SP on entry to os_SPentry and
 * then immediately terminate.
 * 
 * This calibration test uses two tasks (TaskSPSave1 is a basic task and TaskKSSP
 * is an extended task) to capture stack overheads. The basic task copies the stack
 * pointer on entry into os_SPentry. The extended task copies the kernel saved stack
 * pointer (i.e. the kernel stack immediately before the switch to the extended task's
 * stack).
 */

/* Stores the kernel stack pointer on entry to the kernel from a basic task (os_SPksp_start)
 * and the stack pointer on exit while switching to an external task (os_SPksp_end)
 */
os_stackp os_SPksp_end;
os_stackp os_SPksp_start;

/* Basic worker task */
TASK(TaskA)
{
	ActivateTask(TaskSPSave1);	/* Basic task is higher priority and comes straight back */
	/* os_SPoffstore will be the stack at the point the kernel started, and
	 * os_SPentry will be the stack at the point the new basic task is running
	 */
	start[ACTIVATE_TASK_BT2BT] = os_SPoffstore2;
	end[ACTIVATE_TASK_BT2BT] = os_SPentry;
	
	GetResource(RES_SCHEDULER);
	ActivateTask(TaskSPSave1);
	ReleaseResource(RES_SCHEDULER);
	start[RELEASE_RESOURCE_BT2BT] = os_SPoffstore2;
	end[RELEASE_RESOURCE_BT2BT] = os_SPentry;	
	
	ActivateTask(TaskKSSP); /* Extended is higher priority and comes straight back */
	start[ACTIVATE_TASK_BT2ET] = os_SPksp_start;
	end[ACTIVATE_TASK_BT2ET] = os_SPksp_end;	/* A copy of kernel saved stack pointer */

	GetResource(RES_SCHEDULER);
	ActivateTask(TaskKSSP);
	ReleaseResource(RES_SCHEDULER);
	start[RELEASE_RESOURCE_BT2ET] = os_SPksp_start;
	end[RELEASE_RESOURCE_BT2ET] = os_SPksp_end;	
	
	SetEvent(TaskKSSP_E, E1);
	start[SET_EVENT_BT2ET] = os_kssp;
	end[SET_EVENT_BT2ET] = os_SPksp_end;
	
	ChainTask(TaskB);
	test_failed(OS_HERE);
}

/* Extended task that does the same as TaskA */
TASK(TaskB)
{
	/* Record the kernel stack before and after the dispatch to a basic task; this
	 * is KSSP before the dispatch until the SP at the entry to the basic task */
	start[ACTIVATE_TASK_ET2BT] = os_kssp;
	ActivateTask(TaskSPSave1);
	end[ACTIVATE_TASK_ET2BT] = os_SPentry;
	
	/* Record the kernel stack before and after the dispatch to an extended task;
	 * this is KSSP before the dispatch and KSSP after the dispatch
	 */
	start[ACTIVATE_TASK_ET2ET] = os_kssp;
	ActivateTask(TaskKSSP);
	end[ACTIVATE_TASK_ET2ET] = os_SPksp_end;
	
	GetResource(RES_SCHEDULER);
	ActivateTask(TaskSPSave1);
	start[RELEASE_RESOURCE_ET2BT] = os_kssp;
	ReleaseResource(RES_SCHEDULER);
	end[RELEASE_RESOURCE_ET2BT] = os_SPentry;
	
	GetResource(RES_SCHEDULER);
	ActivateTask(TaskKSSP);
	start[RELEASE_RESOURCE_ET2ET] = os_kssp;
	ReleaseResource(RES_SCHEDULER);
	end[RELEASE_RESOURCE_ET2ET] = os_SPksp_end;

	start[SET_EVENT_ET2ET] = os_kssp;
	SetEvent(TaskKSSP_E, E1);
	end[SET_EVENT_ET2ET] = os_SPksp_end;
	
	TerminateTask();
}

/* Records where the kernel got to when it switched to the extended task's stack */
TASK(TaskKSSP)
{
	os_SPksp_end = os_kssp;
	os_SPksp_start = os_SPoffstore;
	TerminateTask();
}

TASK(TaskKSSP_E)
{
	for(;;) {
		WaitEvent(E1);
		ClearEvent(E1);
		os_SPksp_end = os_kssp;
		os_SPksp_start = os_SPoffstore;
	}	
}

int main() {
	StartOS(OSDEFAULTAPPMODE);
	test_passed();
}

void os_idle(void)
{
	ShutdownOS(E_OK);
	test_failed(OS_HERE);
}
