/* Not intended for actual source code, used to test code generation etc. */

#include <osapp.h>
#include <framework.h>
#include <osint.h>

/*
 * Calibration test to determine stack overheads of kernel for extended tasks. It gives the space the kernel uses
 * on the extended task's own stack. This overhead needs to be added to all extended tasks because the stack checking
 * is turned off when the kernel is performing a switch and an overflow will not be detected (thus a buffer is needed
 * in case the stack was nearly full before the API call is made).
 * 
 * After passing, the variable "largest_overhead" indicates the largest stack usage (measured in stack words).
 *
 * The API calls that can result in a call to dispatch are:
 * 
 * 1. ActivateTask()
 * 2. ReleaseResource()
 * 3. SetEvent()
 * 4. WaitEvent()
 * 5. ChainTask()
 * 6. Schedule()
 * 7. Terminate()
 * 8. IncrementCounter()
 * 9. ExpireCounter()
 * 
 * NOTE:
 * 
 * There are several stack calibrations to do:
 * 1. (This one). Determine the kernel overheads left on an extended task's stack. Includes context save, etc.
 * 2. ISR stub overheads left on an extended task's stack.
 * 3. ISR+kernel overheads when an ISR causes a pre-emption (on a kernel stack, depending on whether it switched from ext. stack or not).
 * 4. kernel overheads when a basic task runs an API call that causes a pre-emption to a basic or extended task (c.f. #1).
 * 
 * 5. The stack offset applied to SP when running a pre or post task hook.
 * 6. The stack offset applied to SP when running a basic task entry.
 * 7. The stack offset applied to SP when running an ISR entry.
 * 
 * 8. The overheads put on to an extended task's stack before the entry function is called.
 * 
 * f(pri)
 * {
 * 	for all in objects where theirbasepri > dispatchpri
 * 		if(f(theirpri) > bigestsofar) {
 * 			biggestsofar=
 * 		} 
 * loop
 * return biggestsofar + myoverheads;
 * }
 * 
 * 
 * 
 */

#define NUM_SWITCHING_API_CALLS (7U)
/* @todo need to do counter expiry/increment calls too */

volatile os_stackp start[NUM_SWITCHING_API_CALLS];
volatile os_stackp end[NUM_SWITCHING_API_CALLS];
volatile long size[NUM_SWITCHING_API_CALLS];
volatile int call;

/* Pulls out the stack overheads the kernel uses. This is measured from the point at which the kernel
 * stops the hardware stack checking (and thus the kernel is running), which is obtained with os_SPoffstore.
 *
 */
void set_size(TaskType t)
{
	assert(EXTENDEDTASK(t));
	
	start[call] = os_SPoffstore;
	end[call] = t->dyn->savesp;
#ifdef OS_ASCENDING_STACK
	size[call] = t->dyn->savesp - os_SPoffstore;
#else
	size[call] = os_SPoffstore - t->dyn->savesp;
#endif
}

#define ACTIVATE_TASK		(0)
#define RELEASE_RESOURCE	(1)
#define SET_EVENT			(2)
#define WAIT_EVENT			(3)
#define CHAIN_TASK			(4)
#define SCHEDULE			(5)
#define TERMINATE			(6)
#define EXPIRECOUNTER		(7)
#define INCREMENTCOUNTER	(8)

int largest_overhead;

int main() {
	int i;
	
	StartOS(OSDEFAULTAPPMODE);

	/* Run over the array and determine the max value */

	largest_overhead = -1;
	for(i= 0; i < NUM_SWITCHING_API_CALLS; i++) {
		if(size[i] > largest_overhead) {
			largest_overhead = size[i];
		}
	}
	if(largest_overhead >= 0) {
		test_passed();
	}
	else {
		test_failed(OS_HERE);
	}
}

/* Basic task, priority 1 */
TASK(TaskA)
{
	set_size(TaskC);
	
	SetEvent(TaskC, E1);
	TerminateTask();
}

/* Basic task, priority 3 */
TASK(TaskB)
{
	set_size(TaskC);
	TerminateTask();
}

/* Extended task, priority 2, autostarted.
 * 
 * This is the main driver of the calibration test. It blocks and then calls
 * a worker task to pick up the stack space used by task C on its extended stack.
 * It calls TaskB, which preempts, to pick up preempting calls. It calls TaskA
 * to pick up after a block (which handshakes back). It calls TaskD as an extended
 * task for the event setting preemption.
 * 
 * It calls on to TaskE via a chain in order to continue operating (and pick up
 * the overheads for ChainTask).
 * 
 */
TASK(TaskC)
{
	call = ACTIVATE_TASK;
	ActivateTask(TaskB);
	
	call = RELEASE_RESOURCE;
	GetResource(RES_SCHEDULER);
	ActivateTask(TaskB);
	ReleaseResource(RES_SCHEDULER);

	call = SET_EVENT;
	ActivateTask(TaskD);
	SetEvent(TaskD, E1);
	
	call = WAIT_EVENT;
	ActivateTask(TaskA);
	WaitEvent(E1);
	
	call = CHAIN_TASK;
	ChainTask(TaskE);
	test_failed(OS_HERE);
}

/* Extended task, priority 4 */
TASK(TaskD)
{
	WaitEvent(E1);
	set_size(TaskC);

	TerminateTask();
}

/* Extended task, priority 5, non-preemptive */
TASK(TaskE)
{
	set_size(TaskC);

	call = SCHEDULE;
	ActivateTask(TaskF);
	Schedule();

	call = TERMINATE;
	ActivateTask(TaskF);
	TerminateTask();
}

/* Basic task, priority 6 */
TASK(TaskF)
{
	if(call == TERMINATE) {
		/* The termination path doesn't write to the task control block save stack pointer, but instead
		 * drops the old SP into a global variable (os_SPsave); need to copy this out and put it into the TCB
		 * to let the set_size() function work properly
		 */
		TaskE->dyn->savesp = os_SPsave;
	}
	set_size(TaskE);
	
	TerminateTask();
}

void os_idle(void)
{
	ShutdownOS(E_OK);
	test_failed(OS_HERE);
}
