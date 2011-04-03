/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-04-09 07:29:58 +0100 (Wed, 09 Apr 2008) $
 * $LastChangedRevision: 700 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/common/framework.c $
 * 
 * $CodeReview: kentindell, 2006-10-16 $
 * $CodeReviewItem: update comments for the extended task dispatch to reflect restorecx being a flag not a function pointer $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Application
 * 
 * Testing framework: for use by testing applications, using testing hooks built into
 * Scalios.
 */

#include <string.h>
#include <osapp.h>
#include <framework.h>

#define MAX_EVENTS				540


/* Fake timer state for managing events. These should really be declared 'static' but some rubbish debuggers can't see them then */
volatile TickType fake_timer_match;					/* Match value of fake timer */
volatile int fake_timer_running;					/* Set when timer is running; when not set timer will not advance */
volatile int fake_timer_interrupts_enabled;			/* Set when interrupts are enabled; no fake interrupt will occcur otherwise */
CounterType this_counter;							/* Set when timer started; cache of which counter this is so ISR knows API parameter */

/* The value of 'now' in fake time */
volatile TickType fake_timer_count;					/* Count value of fake timer */

/* Set of time values for the time taken to handle various operations; can be changed so that
 * race conditions can be investigated. By default these are all zero.
 */
TickType fake_timer_disable_entry_delay = 0;
TickType fake_timer_disable_exit_delay = 0;
TickType fake_timer_enable_entry_delay = 0;
TickType fake_timer_enable_exit_delay = 0;
TickType fake_timer_now_entry_delay = 0;
TickType fake_timer_now_exit_delay = 0;
TickType fake_timer_isr_entry_delay = 0;
TickType fake_timer_isr_exit_delay = 0;

/* Fake timer driver functions: redirected from real driver 'stub' calls in isr.c. Note that the functions
 * are the same except for the 'dev' parameter being stripped (these generic fake drivers do not know how
 * to decode the target-specific device control block).
 */

/* Called when Scalios shuts down */
void fake_timer_stop(void)
{	
	fake_timer_interrupts_enabled = 0;
	fake_timer_running = 0;
}

/* Called when Scalios is starting up. Set count and match registers into a known state ready for events. 
 * Note that this fake driver function is not quite the same: it needs the 'stub' function in isr.c to
 * pull out the counter from the device-specific control block and pass it over as a parameter.
 */
void fake_timer_start(CounterType counter)
{	
	this_counter = counter;
	fake_timer_interrupts_enabled = 0;
	fake_timer_running = 1;
	fake_timer_count = 0;
	fake_timer_match = 0;
}

void fake_timer_disable_ints(void)
{
	fake_timer_advance(fake_timer_disable_entry_delay);

	fake_timer_interrupts_enabled = 0;
	
	/* Ensure real interrupt won't raise again in case it became pending */
	testing_stop_isr();

	fake_timer_advance(fake_timer_disable_exit_delay);
}

/* This is the guts of the driver. It must resolve a race where the time has now
 * advanced past the point of the requested time, in which case it must mark an
 * interrupt pending so that when this returns the interrupt occurs.
 */
void fake_timer_enable_ints(TickType past, TickType rel)
{
	TickType ticks_to_current_time;

	/* It takes a little time to get from the ExpireCounter() API call to this
	 * driver function so simulate this time passing. Note that for this call
	 * no ISR will run because the kernel is locked.
	 */
	fake_timer_advance(fake_timer_enable_entry_delay);
	
	/* Dismiss real interrupt */
	testing_dismiss_isr();
		
	/* Set the comparitor to match against the new event-due time */
	fake_timer_match = past + rel;							/* Done in modulo arithmetic */
		
	fake_timer_interrupts_enabled = 1;
	
	/* Resolve the race condition: check to see if we missed the time we set the event for. */
	
	/* Step 1: work out how long from recent-past until current time */
	ticks_to_current_time = fake_timer_count - past; 				/* Done in modulo arithmetic */

	/* Step 2: check to see if this time has already expired */
	if(rel <= ticks_to_current_time) {
		/* The comparitor might not have been set when the counter rolled around */
		/* to the event-due time. In any case, the event is already due. */
		/* Step 3: manually set the interrupt pending. */
		testing_trigger_isr();				/* Set real interrupt pending again; ISR won't actually run yet, since kernel is locked */
	}
	
	fake_timer_advance(fake_timer_enable_exit_delay);
}

TickType fake_timer_now(void)
{
	TickType now;
	
	fake_timer_advance(fake_timer_now_entry_delay);

	now = fake_timer_count;
	
	fake_timer_advance(fake_timer_now_exit_delay);
	
	return now;
}

/* Called via a real ISR, itself invoked by the test, to simulate an interrupt occurring.
 */
void fake_timer_ISR(void)
{	
	StatusType rc;
	
	/* ISR takes a little time to get into this handler; simulate this */
	fake_timer_advance(fake_timer_isr_entry_delay);
	
	rc = ExpireCounter(this_counter);
	TEST(rc, E_OK);
	
	/* Again, a little time passes to get out of the ISR */
	fake_timer_advance(fake_timer_isr_exit_delay);
}

/* Called via the test to execute fake CPU time for a requested amount. If a fake timer interrupt is due in this
 * execution time window then the interrupt is made pending at the correct fake timer count. The real ISR may be handled
 * before this function returns (if this call is made outside the kernel, for example).
 * 
 * Note that the count value may have moved more than the requested time on return, since the ISR and drivers
 * will also call fake_timer_advance() to move time forwards. Thus to execute until a specific time value it is necessary
 * to busy/wait (i.e. poll) the count for a specific time, just as in a real system.
 * 
 * This function may be called reentrantly.
 */
void fake_timer_advance(TickType adv)
{	
	while(fake_timer_running && adv > 0) {
		TickType time_to_expiry = fake_timer_match - fake_timer_count; 		/* Time from now to (potential) expiry; Done in modulo arithmetic */
		
		/* Is expiry before adv? If so, process the interrupt first at the right time */
		if(time_to_expiry <= adv && fake_timer_interrupts_enabled) {
			TickType fake_timer_match_prev = fake_timer_match;
			fake_timer_count += time_to_expiry;								/* Move time to fake timer expiry due; Done in modulo arithmetic */
			adv -= time_to_expiry;											/* Acount for time just passed; Done in modulo arithmetic */
			
			/* Trigger a real interrupt to handle the fake timer expiry */
			testing_trigger_isr();
			if(fake_timer_match == fake_timer_match_prev) {					/* ISR never ran (e.g. kernel locked), so time will run on */
				fake_timer_count += adv;									/* Modulo arithmetic */
				adv = 0;
			}
		}
		else {
			fake_timer_count += adv;										/* Account for total time requested; Done in modulo arithmetic */
			adv = 0;
		}
	}
}

/* Used to keep track of SP on entry to a task */
os_stackp NEAR(os_SPentry);
/* A copy of os_SPoffstore saved before it gets overwritten */
os_stackp NEAR(os_SPoffstore2);

static unat in_error_hook;			/* Set when error hook is running; used to test for recursive calls */

void define_task_switch(TaskType from, TaskType to)
{
#ifdef USEPOSTTASKHOOK
	DEFINE_TESTEVENT("PostTaskHook");
#ifdef INCLUDE_HANDLE_NAME_MAP
	DEFINE_TESTEVENT(TaskType2string(from));
#else
	DEFINE_TESTEVENT("A task");
#endif
#endif	
#ifdef USEPRETASKHOOK
	DEFINE_TESTEVENT("PreTaskHook");
	DEFINE_TESTEVENT(TaskType2string(to));
#endif
}

/* Standard startup hook
 * 
 * $Req: artf1226 $
 */
void StartupHook(void)
{	
#ifdef USESTARTUPHOOK
	SET_TESTEVENT("StartupHook");
#else
	test_failed(OS_HERE);
#endif
}

int test_shutdown_code;
StatusType shutdown_code;

/* Standard shutdown hook
 * 
 * $Req: artf1227 $
 */
void ShutdownHook(StatusType error)
{
#ifdef USESHUTDOWNHOOK
	SET_TESTEVENT("ShutdownHook");
	/* Check that the status passed to ShutdownOS() gets passed to this hook.
	 * 
	 * $Req: artf1218 $
	 */
	if(test_shutdown_code && error != shutdown_code) {
		test_failed(OS_HERE);
	}
	/* Check that the system shuts down if a stack overflow detected (when expected).
	 * 
	 * $Req: artf1040 $
	 */ 
	if(test_shutdown_code && error == shutdown_code && error == E_OS_STACKFAULT) {
		test_finished();
		test_passed();
	}
#else
	test_failed(OS_HERE);
#endif
}

TaskType pretaskhook_task = 0;

/* Standard pretask hook.
 * 
 * $Req: artf1224 $
 */
void PreTaskHook(void)
{
	TaskType task;
	TaskStateType state;
	StatusType rc;

#ifdef USEPRETASKHOOK
	/* PreTaskHook() should be called with cat2 interrupts locked.
	 * 
	 * $Req: artf1110 $
	 * 
	 * Following code will trigger the cat2 ISR here but it should not actually run. Dismiss it later.
	 * Safe to do this because PreTaskHook() is never called from an ISR and so won't mess with the cat2 ISR in
	 * the tests.
	 * 
	 * In mosts tests the ISR running is logged, and this would cause the test to fail
	 * because there should be no expect ISR log entries at this point.
	 */
	testing_trigger_isr();
	
	SET_TESTEVENT("PreTaskHook");
	/* Task should now be RUNNING when the hook is called
	 * 
	 * $Req: artf1140 $
	 */
	rc = GetTaskID(&task);			/* $Req: artf1139 $ */
	if(rc != E_OK) {
		test_failed(OS_HERE);
	}
	if(task == INVALID_TASK) {		/* $Req: artf1119 $ */
		test_failed(OS_HERE);
	}
	GetTaskState(task, &state);		/* $Req: artf1141 $ */
	if(rc != E_OK) {
		test_failed(OS_HERE);
	}
	if(state != RUNNING) {			/* $Req: artf1119 $ */
		test_failed(OS_HERE);
	}
#ifdef INCLUDE_HANDLE_NAME_MAP
	GetTaskID(&task);
	SET_TESTEVENT(TaskType2string(task));
#else
	SET_TESTEVENT("A task");
#endif
#ifdef USEPOSTTASKHOOK
	if(pretaskhook_task != 0) {
		test_failed(OS_HERE);
	}
#endif
	GetTaskID(&pretaskhook_task);
	testing_dismiss_isr();
#else
	test_failed(OS_HERE);
#endif
}

/* Standard posttask hook
 * 
 * $Req: artf1225 $
 */
void PostTaskHook(void)
{
	TaskType task;
	StatusType rc;
	TaskStateType state;
#ifdef USEPOSTTASKHOOK
	/* PostTaskHook() should be called with cat2 interrupts locked.
	 * 
	 * $Req: artf1110 $
	 * 
	 * Following code will trigger the cat2 ISR here but it should not actually run. Dismiss it later.
	 * Safe to do this because PostTaskHook() is never called from an ISR and so won't mess with the cat2 ISR in
	 * the tests.
	 * 
	 * In mosts tests the ISR running is logged, and this would cause the test to fail
	 * because there should be no expect ISR log entries at this point.
	 */
	SET_TESTEVENT("PostTaskHook");
	testing_trigger_isr();
	/* Task should now be RUNNING when the hook is called
	 * 
	 * $Req: artf1140
	 */
	rc = GetTaskID(&task);			/* $Req: artf1139 $ */
	if(rc != E_OK) {
		test_failed(OS_HERE);
	}
	if(task == INVALID_TASK) {		/* $Req: artf1118 $ */
		test_failed(OS_HERE);
	}
	GetTaskState(task, &state);		/* $Req: artf1141 $ */
	if(rc != E_OK) {
		test_failed(OS_HERE);
	}
	if(state != RUNNING) {			/* $Req: artf1118 $ */
		test_failed(OS_HERE);
	}
	
#ifdef INCLUDE_HANDLE_NAME_MAP
	SET_TESTEVENT(TaskType2string(task));
#else
	SET_TESTEVENT("A task");
#endif
#ifdef USEPRETASKHOOK
	/* If there is a pretask hook then make sure the posttask hook gets called in a paired sequence */
	if(pretaskhook_task != task) {
		test_failed(OS_HERE);
	}
	else {
		pretaskhook_task = 0;
	}
#endif
	testing_dismiss_isr();
#else
	test_failed(OS_HERE);
#endif
}

/* Set global variables to the hook API call parameters. This is called from the
 * error hook so that outside the error hook the test code can see what was passed
 * through.
 * 
 * $Req: artf1230 $
 */
void set_error_hook_parameters(OSServiceIdType service)
{
	switch(service) {
		case OSServiceId_NoServiceId:
			break;
		case OSServiceId_GetAlarm:
			param_AlarmType = OSError_GetAlarm_AlarmID();
			param1_TickRefType = OSError_GetAlarm_Tick();
			break;
		case OSServiceId_SetRelAlarm:
			param_AlarmType = OSError_SetRelAlarm_AlarmID();
			param1_TickType = OSError_SetRelAlarm_increment();
			param2_TickType = OSError_SetRelAlarm_cycle();
			break;
		case OSServiceId_SetAbsAlarm:
			param_AlarmType = OSError_SetAbsAlarm_AlarmID();
			param1_TickType = OSError_SetAbsAlarm_start();
			param2_TickType = OSError_SetAbsAlarm_cycle();
			break;
		case OSServiceId_CancelAlarm:
			param_AlarmType = OSError_CancelAlarm_AlarmID();
			break;
		case OSServiceId_GetActiveApplicationMode:
			param_AppModeType = OSError_StartOS_Mode();
			break;
		case OSServiceId_StartOS:
			break;
		case OSServiceId_ShutdownOS:
			break;
		case OSServiceId_ActivateTask:
			param_TaskType = OSError_ActivateTask_TaskID();
			break;
		case OSServiceId_TerminateTask:
			break;
		case OSServiceId_ChainTask:
			param_TaskType = OSError_ChainTask_TaskID();
			break;
		case OSServiceId_Schedule:
			break;
		case OSServiceId_GetTaskState:
			param_TaskType = OSError_GetTaskState_TaskID();
			param_TaskStateRefType = OSError_GetTaskState_State();
			break;
		case OSServiceId_GetResource:
			param_ResourceType = OSError_GetResource_ResID();
			break;
		case OSServiceId_ReleaseResource:
			param_ResourceType = OSError_ReleaseResource_ResID();
			break;
		case OSServiceId_SetEvent:
			param_EventMaskType = OSError_SetEvent_Mask();
			param_TaskType = OSError_SetEvent_TaskID();
			break;
		case OSServiceId_ClearEvent:
			param_EventMaskType = OSError_ClearEvent_Mask();
			break;
		case OSServiceId_GetEvent:
			param_TaskType = OSError_GetEvent_TaskID();
			param_EventMaskRefType = OSError_GetEvent_Event();
			break;
		case OSServiceId_WaitEvent:
			param_EventMaskType = OSError_WaitEvent_Mask();
			break;
		case OSServiceId_IncrementCounter:
			param_CounterType = OSError_IncrementCounter_CounterID();
			break;
		case OSServiceId_ExpireCounter:
			param_CounterType = OSError_ExpireCounter_CounterID();
			break;
		case OSServiceId_StartScheduleTableRel:
			param1_ScheduleTableType = OSError_StartScheduleTableRel_ScheduleTableID();
			param1_TickType = OSError_StartScheduleTableRel_Offset();
			break;
		case OSServiceId_StartScheduleTableAbs:
			param1_ScheduleTableType = OSError_StartScheduleTableAbs_ScheduleTableID();
			param1_TickType = OSError_StartScheduleTableAbs_Tickvalue();
			break;
		case OSServiceId_NextScheduleTable:
			param1_ScheduleTableType = OSError_NextScheduleTable_ScheduleTableID_current();
			param2_ScheduleTableType = OSError_NextScheduleTable_ScheduleTableID_next();
			break;
		case OSServiceId_StopScheduleTable:
			param1_ScheduleTableType = OSError_StopScheduleTable_ScheduleTableID();
			break;
		case OSServiceId_GetScheduleTableStatus:
			param1_ScheduleTableType = OSError_GetScheduleTableStatus_ScheduleID();
			param_ScheduleTableStatusRefType = OSError_GetScheduleTableStatus_ScheduleStatus();
			break;
		case OSServiceId_GetAlarmBase:
			param_AlarmType = OSError_GetAlarmBase_AlarmID();
			param_AlarmBaseRefType = OSError_GetAlarmBase_Info();
			break;
		case OSServiceId_GetCounterValue:
			param_CounterType = OSError_GetCounterValue_CounterID();
			param1_TickRefType = OSError_GetCounterValue_Value();
		case OSServiceId_GetElapsedCounterValue:
			param_CounterType = OSError_GetElapsedCounterValue_CounterID();
			param1_TickRefType = OSError_GetElapsedCounterValue_Value();
			param2_TickRefType = OSError_GetElapsedCounterValue_ElapsedValue();
		case OSServiceId_ControlDevice:
			break;
	}
}

/* Checks for pre-defined constants and returns matching string
 * 
 * $Req: artf1229 $
 */
char *OSServiceIdType2string(OSServiceIdType service, char *fail)
{
	switch(service) {
		case OSServiceId_NoServiceId: return "OSServiceId_NoServiceId";
		case OSServiceId_GetAlarm: return "OSServiceId_GetAlarm";
		case OSServiceId_SetRelAlarm: return "OSServiceId_SetRelAlarm";
		case OSServiceId_SetAbsAlarm: return "OSServiceId_SetAbsAlarm";
		case OSServiceId_CancelAlarm: return "OSServiceId_CancelAlarm";
		case OSServiceId_GetActiveApplicationMode: return "OSServiceId_GetActiveApplicationMode";
		case OSServiceId_StartOS: return "OSServiceId_StartOS";
		case OSServiceId_ShutdownOS: return "OSServiceId_ShutdownOS";
		case OSServiceId_ActivateTask: return "OSServiceId_ActivateTask";
		case OSServiceId_TerminateTask: return "OSServiceId_TerminateTask";
		case OSServiceId_ChainTask: return "OSServiceId_ChainTask";
		case OSServiceId_Schedule: return "OSServiceId_Schedule";
		case OSServiceId_GetTaskState: return "OSServiceId_GetTaskState";
		case OSServiceId_GetResource: return "OSServiceId_GetResource";
		case OSServiceId_ReleaseResource: return "OSServiceId_ReleaseResource";
		case OSServiceId_SetEvent: return "OSServiceId_SetEvent";
		case OSServiceId_ClearEvent: return "OSServiceId_ClearEvent";
		case OSServiceId_GetEvent: return "OSServiceId_GetEvent";
		case OSServiceId_WaitEvent: return "OSServiceId_WaitEvent";
		case OSServiceId_IncrementCounter: return "OSServiceId_IncrementCounter";
		case OSServiceId_ExpireCounter: return "OSServiceId_ExpireCounter";
		case OSServiceId_StartScheduleTableRel: return "OSServiceId_StartScheduleTableRel";
		case OSServiceId_StartScheduleTableAbs: return "OSServiceId_StartScheduleTableAbs";
		case OSServiceId_NextScheduleTable: return "OSServiceId_NextScheduleTable";
		case OSServiceId_StopScheduleTable: return "OSServiceId_StopScheduleTable";
		case OSServiceId_GetScheduleTableStatus: return "OSServiceId_GetScheduleTableStatus";
		case OSServiceId_GetAlarmBase: return "OSServiceId_GetAlarmBase";
		case OSServiceId_ControlDevice: return "OSServiceId_ControlDevice";
		case OSServiceId_GetCounterValue: return "OSServiceId_GetCounterValue";
		case OSServiceId_GetElapsedCounterValue: return "OSServiceId_GetElapsedCounterValue";
		default: return fail;
	}
}

TaskType param_TaskType;
TaskRefType param_TaskRefType;
TaskStateRefType param_TaskStateRefType;
ResourceType param_ResourceType;
EventMaskType param_EventMaskType;
EventMaskRefType param_EventMaskRefType;
AlarmType param_AlarmType;
AlarmBaseRefType param_AlarmBaseRefType;
TickRefType param1_TickRefType;
TickRefType param2_TickRefType;
TickType param1_TickType;
TickType param2_TickType;
AppModeType param_AppModeType;
StatusType param_StatusType;
CounterType param_CounterType;
ScheduleTableType param1_ScheduleTableType;
ScheduleTableType param2_ScheduleTableType;
ScheduleTableStatusRefType param_ScheduleTableStatusRefType;
DeviceControlCodeType param_DeviceControlCodeType;
DeviceControlDataType param_DeviceControlDataType;	

/* Defines the log for the default error hook call at this point, given the expected return code from
 * the API call, the service ID code for the API call, and the name of the calling runnable. This name must
 * be set to:
 * 
 * 		"<No runnable>" if the error hook call is not associated with a runnable
 */
void define_error_hook_call(StatusType rc, OSServiceIdType service, char *runnable)
{
#ifdef USEERRORHOOK
	DEFINE_TESTEVENT("ErrorHook");
#ifdef INCLUDE_HANDLE_NAME_MAP
	DEFINE_TESTEVENT(runnable);
#else
	DEFINE_TESTEVENT("<A runnable>");
#endif
	DEFINE_TESTEVENT(OSServiceIdType2string(service, "Service unknown"));
	DEFINE_TESTEVENT(StatusType2string(rc, "Status unknown"));
#endif
}

/* Error hook for all tests
 * 
 * $Req: artf1222 $
 */
void ErrorHook(StatusType rc)
{
#ifdef USEERRORHOOK
#ifdef EXPECTSERRORS
	TaskType from_task;
	ISRType from_isr;
	TaskStateType state;
	
	if(in_error_hook) {
		test_failed(OS_HERE);		/* Error hook not permitted to be called recursively $Req: artf1112 $ */
	}
	in_error_hook = 1U;
	
	SET_TESTEVENT("ErrorHook");
	
#ifdef OS_EXTENDED_STATUS
	/* Test for recursive error hook call by making a legal call which generates an error */
	GetTaskState(0, &state);
#endif

	/* $Req: artf1140 $ */
	GetTaskID(&from_task);
	from_isr = GetISRID();
	
	if(from_isr == INVALID_ISR) {
		if(from_task == INVALID_TASK) {
			SET_TESTEVENT("<No runnable>");		/* Not sure how to get an error hook called if no task or ISR running.. */
		}
		else {	/* Must be a task that caused the error hook to run since there is no ISR running */
#ifdef INCLUDE_HANDLE_NAME_MAP
			SET_TESTEVENT(TaskType2string(from_task));
#else
			SET_TESTEVENT("<A task>");
#endif
			/* ErrorHook() should be called with cat2 interrupts locked.
			 * 
			 * $Req: artf1110 $
			 * 
			 * Following code will trigger the cat2 ISR here but it should not actually run. Dismiss it immediately.
			 * 
			 * Only do this if the error hook is called from outside an ISR because otherwise this would mess up
			 * the only cat2 ISR. In mosts tests the ISR running is logged, and this would cause the test to fail
			 * because there should be no expect ISR log entries at this point.
			 */
			testing_trigger_isr();
			testing_dismiss_isr();
		}
	}
	else {	/* An ISR must be running; this takes precedence over a task: the error must have
			 * been raised in the ISR since the task can't be running (all ISRs have higher priorities
			 * than all tasks)
			 */
#ifdef INCLUDE_HANDLE_NAME_MAP
		SET_TESTEVENT(ISRType2string(from_isr));
#else
		SET_TESTEVENT("<An ISR>");
#endif
	}
	/* If E_OS_MISSING end called then the task is still valid and still RUNNING when the error hook called */
	if(rc == E_OS_MISSINGEND) {				/* $Req: artf1041 $ */
		if(from_task == INVALID_TASK) {
			test_failed(OS_HERE);
		}
		else {
			GetTaskState(from_task, &state);
			if(state != RUNNING) {
				test_failed(OS_HERE);
			}
		}
	}

	/* OSErrorGetServiceId() callable from ErrorHook()
	 * 
	 * $Req: artf1114 $
	 * $Req: artf1228 $
	 */

	SET_TESTEVENT(OSServiceIdType2string(OSErrorGetServiceId(), "<Unknown service ID>"));		
	SET_TESTEVENT(StatusType2string(rc, "<Unknown StatusType code>"));
	set_error_hook_parameters(OSErrorGetServiceId());

	in_error_hook = 0U;

#else /* don't expect errors */
	test_failed(OS_HERE);
#endif
#else
	/* don't use error hook
	 * 
	 * $Req: artf1223 $
	 */
	test_failed(OS_HERE);
#endif
}

os_assertion_failure(char *here)
{
	test_failed(here);
}

char *testevents[MAX_EVENTS];

int testevent_num;
int check_testevent_num;

void init_testevents(void)
{
	in_error_hook = 0;
	pretaskhook_task = 0;
	
	testevent_num = 0;
	check_testevent_num = 0;
}

void add_testevent(char *desc)
{
	if(testevent_num >= MAX_EVENTS) {
		test_failed("Event buffer overflow");
	}
	else {	
		testevents[testevent_num] = desc;
		testevent_num++;
	}
}

void test_finished()
{
	if(testevent_num != check_testevent_num) {
		test_failed("Not enough observed events");
	}
}
/* Calls test_failed() if it doesn't match the appropriate event */
void check_testevent(char *desc, char *comment)
{
	if(check_testevent_num > testevent_num) {
		test_failed(comment);
	}
	if(strcmp(desc, testevents[check_testevent_num])) {
		test_failed(comment);
	}
	check_testevent_num++;
}

char *StatusType2string(StatusType code, char *fail)
{
	switch(code) {
		case E_OK: return "E_OK";
		case E_OS_ACCESS: return "E_OS_ACCESS";
		case E_OS_CALLEVEL: return "E_OS_CALLEVEL";
		case E_OS_ID: return "E_OS_ID";
		case E_OS_LIMIT: return "E_OS_LIMIT";
		case E_OS_NOFUNC: return "E_OS_NOFUNC";
		case E_OS_RESOURCE: return "E_OS_RESOURCE";
		case E_OS_STATE: return "E_OS_STATE";
		case E_OS_VALUE: return "E_OS_VALUE";
		case E_OS_MISSINGEND: return "E_OS_MISSINGEND";
		case E_OS_DISABLEDINT: return "E_OS_DISABLEDINT";
		case E_OS_STACKFAULT: return "E_OS_STACKFAULT";

		/* Target-specific return codes */
#include "retcodes.h"
		default: return fail;
	}
}

#ifdef INCLUDE_HANDLE_NAME_MAP
char *TaskType2string(TaskType task)
{
	if(task == INVALID_TASK) {
		return "INVALID_TASK";
	}
	else {
		return handle2string((void *)task, "Task not found");
	}
}

char *ISRType2string(ISRType isr)
{
	if(isr == INVALID_ISR) {
		return "INVALID_ISR";
	}
	else {
		return handle2string((void *)isr, "ISR not found");
	}
}

char *ResourceType2string(ResourceType res)
{
	return handle2string((void *)res, "Resource not found");
}

char *handle2string(void *ptr, char *fail)
{
	int i;
	
	for(i = 0; os_all_handles[i] != 0; i++) {
		if(os_all_handles[i] == ptr) {
			return os_all_names[i];
		}
	}
	
	return fail;
}
#endif

/* Put a breakpoint on each "for" statement and see which one is triggered to determine
 * if the test passed or failed.
 * 
 * Added a local volatile to stop the compiler optimizing the code completely and preventing
 * (in some debuggers) the adding of a breakpoint to the "for" statement.
 * 
 * Note that these test functions can be overridden by alternatives (since these are in a library
 * module). This can be useful in automating the tests (e.g. signalling a pass or fail via a
 * networking link).
 */
void test_passed(void)
{
	volatile int x;
	
	for(;;)
		x++;
}
 
void test_failed(char *comment)
{
	volatile int x;
	
	for(;;)
		x++;
}
