/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-01 00:28:01 +0000 (Sat, 01 Mar 2008) $
 * $LastChangedRevision: 632 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/builds/common/framework.h $
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

#ifndef FRAMEWORK_H_
#define FRAMEWORK_H_

extern os_stackp NEAR(os_SPentry);
extern os_stackp NEAR(os_SPoffstore2);

void test_passed(void);
void test_failed(char *);

void init_testevents(void);
void add_testevent(char *desc);

/* Calls test_failed() if it doesn't match the appropriate event */
void check_testevent(char *desc, char *comment);

void test_finished(void);

/* Sets up an interrupt source and makes it pending; returns when it is pending and an interrupt will
 * occur. ISR is at lowest hardware priority. Target-specific implementation.
 */
void testing_trigger_isr(void);

/* Called by ISR to dismiss interrupt. Target-specific implementation. */
void testing_dismiss_isr(void);

/* Returns true if the cat1 interrupt in isr.c interrupts the cat2 */
unat cat1_interrupts_cat2(void);

/* Returns true if the cat1 will not interrupt the cat2, but does run before it */
unat cat1_precedes_cat2(void);

/* Trigger a cat1 interrupt */
void testing_trigger_cat1_isr(void);

/* Dismiss the cat1 interrupt */
void testing_dismiss_cat1_isr(void);


/* Generic fake timer driver. Uses the target-specific ISR and a set of fake
 * driver functions to simulate the passing of time, timer expiry interrupts,
 * etc.
 */

/* Fake timer driver functions: redirected from real driver 'stub' calls in isr.c. Note that the functions
 * are the same except for the 'dev' parameter being stripped (these generic fake drivers do not know how
 * to decode the target-specific device control block).
 */
void fake_timer_stop(void);
void fake_timer_start(CounterType);
void fake_timer_disable_ints(void);
void fake_timer_enable_ints(TickType, TickType);
TickType fake_timer_now(void);
void fake_timer_ISR(void);

/* Not part of the device driver per se. It can be called via the test itself to advance time by an amount.
 * If the fake timer is due to expire within the desired advance time then the general purpose ISR will
 * be made pending.
 * 
 * This function may be called reentrantly.
 */
void fake_timer_advance(TickType adv);

/* The fake timer count value, advanced by the fake_timer_advance() function.
 * The tests can use this to determine 'now' in fake time and ensure that
 * periodic alarms are truly periodic with the requested period.
 */
extern volatile TickType fake_timer_count;

/* Model a task switch by adding the expected events to the log */
void define_task_switch(TaskType, TaskType);

/* Model an error hook call by adding the expected events to the log */
void define_error_hook_call(StatusType rc, OSServiceIdType service, char *runnable);

/* Looks for a match for the handle and returns the string name of the handle, or a different string if not found */
char *handle2string(void *, char*);

/* Map arrays instantiated in osgen.c; compiled in by defining INCLUDE_HANDLE_NAME_MAP */
extern void *os_all_handles[];
extern char *os_all_names[];

/* Return the text name of a given StatusType code */
char *StatusType2string(StatusType, char *);
char *OSServiceIdType2string(OSServiceIdType service, char *);
char *TaskType2string(TaskType);
char *ResourceType2string(ResourceType);
char *ISRType2string(ISRType);

#define DEFINE_TESTEVENT(s)		add_testevent(s)

#define SET_TESTEVENT(s)		check_testevent(s, OS_HERE)

#ifdef USEERRORHOOK
/* Macro that checks to see the parameters passed to error hook were correct */
#define TESTP(param, val)		{if((param) != (val)) {test_failed(OS_HERE);}; (param) = 0;}
#else
#define TESTP(param, val)		/* No test done, since error hook won't run */
#endif

#ifdef OS_EXTENDED_STATUS
#define CHECK_STATUS(r, e, s)		{if((r) != (e)) os_test_failed(OS_HERE);}		
#else
#define	CHECK_STATUS(r, e, s)		{if((r) != (s)) os_test_failed(OS_HERE);}
#endif
 
#define TEST(var, val)			{if((var) != (val)) {test_failed(OS_HERE);}}

extern TaskType param_TaskType;
extern TaskRefType param_TaskRefType;
extern TaskStateRefType param_TaskStateRefType;
extern ResourceType param_ResourceType;
extern EventMaskType param_EventMaskType;
extern EventMaskRefType param_EventMaskRefType;
extern AlarmType param_AlarmType;
extern AlarmBaseRefType param_AlarmBaseRefType;
extern TickRefType param1_TickRefType;
extern TickRefType param2_TickRefType;
extern TickType param1_TickType;
extern TickType param2_TickType;
extern AppModeType param_AppModeType;
extern StatusType param_StatusType;
extern CounterType param_CounterType;
extern ScheduleTableType param1_ScheduleTableType;
extern ScheduleTableType param2_ScheduleTableType;
extern ScheduleTableStatusRefType param_ScheduleTableStatusRefType;
extern DeviceControlCodeType param_DeviceControlCodeType;
extern DeviceControlDataType param_DeviceControlDataType;	

extern int test_shutdown_code;
extern StatusType shutdown_code;

#endif /*FRAMEWORK_H_*/
