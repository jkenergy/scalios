/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-04-14 01:20:49 +0100 (Sat, 14 Apr 2007) $
 * $LastChangedRevision: 415 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/oslib.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		External
 */

#ifndef OSLIB_H_
#define OSLIB_H_

#include <assert.h>

#if !defined(OS_APPLICATION_BUILD) && !defined(OS_GEN_BUILD) && !defined(OS_INTERNAL_BUILD)
/* Library build by default */
#define OS_LIBRARY_BUILD
#endif

#ifdef OS_LIBRARY_BUILD
#if defined(OS_EXTENDED_STATUS) || defined(OS_STANDARD_STATUS)
#error Library cannot use OS with either OS_EXTENDED_STATUS or OS_STANDARD_STATUS defined.
#endif
#endif

/* include public api headers */
#include <oslibcompiler.h>
#include <oslibtarget.h>

/* Declarational elements; obsolete but included for legacy code */
#define DeclareAlarm(x)						/* $Req: artf1234 $ */
#define DeclareEvent(x)						/* $Req: artf1233 $ */
#define DeclareResource(x)					/* $Req: artf1232 $ */
#define DeclareTask(x)						/* $Req: artf1231 $ */

/* StatusType codes; could define this as an enum @todo */
typedef unat StatusType;

/* $Req: artf1123 $ */
#define E_OK						(0)		/* $Req: artf1122 $ */
#define E_OS_ACCESS					(1U)
#define E_OS_CALLEVEL				(2U)
#define E_OS_ID						(3U)
#define E_OS_LIMIT					(4U)
#define E_OS_NOFUNC					(5U)
#define E_OS_RESOURCE				(6U)
#define E_OS_STATE					(7U)
#define E_OS_VALUE					(8U)
#define E_OS_MISSINGEND				(9U)	/* $Req: artf1041 $ */
#define E_OS_DISABLEDINT			(10U)	/* $Req: artf1045 $ */

/* Macro to allow tasks and ISRs to be defined; entry function for task 'a' is named 'taskentry_a' */
#define TASK(a)				void os_taskentry_##a(void)			/* $Req: artf1143 $ */
#define ISR(a)				void os_isrentry_##a(void)			/* $Req: artf1150 $ */
#define ALARMCALLBACK(a)	void os_alarmcallback_##a(void)		/* $Req: artf1108 $ */

/* Defined for both GetTaskID() and GetTaskState() note: OSEK spec. shares same name for different types */
#define INVALID_TASK		(0)
#define INVALID_ISR			(0)

/*------------------------------------------------------------------
 * declare the handle types. These are declared as const (i.e. in ROM) because they should not change at
 * run-time (other than, perhaps, dynamic creation of tasks: potential future functionality). 
 */
typedef const struct os_taskcb * TaskType;
typedef const struct os_isrcb * ISRType;
typedef const struct os_rescb * ResourceType;
typedef const struct os_appmodecb * AppModeType;
typedef const struct os_alarmcb * AlarmType;
typedef const struct os_countercb * CounterType;
typedef const struct os_schedtabcb * ScheduleTableType;

/* $Req: artf1183 $ */
struct os_alarmbasecb {					/* Static (ROM) part of the alarmbase control block */
	TickType	maxallowedvalue;			/* Maximum possible allowed count value in ticks */
	TickType	ticksperbase;				/* Number of ticks required to reach a counter-specific (significant) unit */
	TickType	mincycle;					/* Smallest allowed value for the cycle-parameter of SetRelAlarm/SetAbsAlarm) */
};

/* This structure can appear in user code */
typedef struct os_alarmbasecb AlarmBaseType;

/* INVALID_TASK not specified here in this enum since defined differently for GetTaskID() API call in OSEK Specification
 * therefore need to define other states as having non-zero values */
typedef enum {RUNNING = 1, WAITING = 2, READY = 3, SUSPENDED = 4 } TaskStateType;
typedef TaskStateType *TaskStateRefType;
typedef TaskType *TaskRefType;
typedef EventMaskType *EventMaskRefType;
typedef AlarmBaseType *AlarmBaseRefType;
typedef TickType *TickRefType;
typedef enum {
	SCHEDULETABLE_NOT_STARTED,
	SCHEDULETABLE_NEXT,
	SCHEDULETABLE_WAITING,
	SCHEDULETABLE_RUNNING_AND_SYNCHRONOUS,
	SCHEDULETABLE_RUNNING} ScheduleTableStatus;
typedef ScheduleTableStatus * ScheduleTableStatusRefType;

/* Globals to support error hook functions */
union os_param {
	TaskType ActivateTask_TaskID;
	TaskType ChainTask_TaskID;
	TaskRefType GetTaskID_TaskID;
	TaskType GetTaskState_TaskID;
	TaskStateRefType GetTaskState_State;
	
	ResourceType GetResource_ResID;
	ResourceType ReleaseResource_ResID;
	
	TaskType SetEvent_TaskID;
	EventMaskType SetEvent_Mask;
	EventMaskType ClearEvent_Mask;
	TaskType GetEvent_TaskID;
	EventMaskRefType GetEvent_Event;
	EventMaskType WaitEvent_Mask;
	
	AlarmType GetAlarmBase_AlarmID;
	AlarmBaseRefType GetAlarmBase_Info;
	AlarmType GetAlarm_AlarmID;
	TickRefType GetAlarm_Tick;
	AlarmType SetRelAlarm_AlarmID;
	TickType SetRelAlarm_increment;
	TickType SetRelAlarm_cycle;
	AlarmType SetAbsAlarm_AlarmID;
	TickType SetAbsAlarm_start;
	TickType SetAbsAlarm_cycle;
	AlarmType CancelAlarm_AlarmID;
	
	AppModeType StartOS_Mode;
	StatusType ShutdownOS_Error;
	
	CounterType IncrementCounter_CounterID;
	CounterType ExpireCounter_CounterID;
	
	ScheduleTableType GetScheduleTableStatus_ScheduleID;
	ScheduleTableStatusRefType GetScheduleTableStatus_ScheduleStatus;
	ScheduleTableType StartScheduleTableRel_ScheduleTableID;
	TickType StartScheduleTableRel_Offset;
	ScheduleTableType StartScheduleTableAbs_ScheduleTableID;
	TickType StartScheduleTableAbs_Tickvalue;
	ScheduleTableType NextScheduleTable_ScheduleTableID_current;
	ScheduleTableType NextScheduleTable_ScheduleTableID_next;
	ScheduleTableType StopScheduleTable_ScheduleTableID;
};

typedef enum {
	OSServiceId_GetAlarm,
	OSServiceId_SetRelAlarm,
	OSServiceId_SetAbsAlarm,
	OSServiceId_CancelAlarm,
	OSServiceId_GetActiveApplicationMode,
	OSServiceId_StartOS,
	OSServiceId_ShutdownOS,
	OSServiceId_ActivateTask,
	OSServiceId_TerminateTask,
	OSServiceId_ChainTask,
	OSServiceId_Schedule,
	OSServiceId_GetTaskState,
	OSServiceId_GetResource,
	OSServiceId_ReleaseResource,
	OSServiceId_SetEvent,
	OSServiceId_ClearEvent,
	OSServiceId_GetEvent,
	OSServiceId_WaitEvent,
	OSServiceId_IncrementCounter,
	OSServiceId_ExpireCounter,
	OSServiceId_StartScheduleTableRel,
	OSServiceId_StartScheduleTableAbs,
	OSServiceId_NextScheduleTable,
	OSServiceId_StopScheduleTable,
	OSServiceId_GetScheduleTableStatus,
	OSServiceId_GetAlarmBase} OSServiceIdType;

/* Up to three parameters in an API call */
extern union os_param os_param1;
extern union os_param os_param2;
extern union os_param os_param3;

/* Global to indicate which API call failed */
extern OSServiceIdType os_serviceid;

/* Macros defined by API to access error values; see OSEK OS specification page 41 $Req: artf1114 $ */
#define OSErrorGetServiceId()								(os_serviceid)

#define OSError_GetAlarm_AlarmID()							(os_param1.GetAlarm_AlarmID)
#define OSError_GetAlarm_Tick()								(os_param2.GetAlarm_Tick)

#define OSError_SetRelAlarm_AlarmID()						(os_param1.SetRelAlarm_AlarmID)
#define OSError_SetRelAlarm_increment()						(os_param2.SetRelAlarm_increment)
#define OSError_SetRelAlarm_cycle()							(os_param3.SetRelAlarm_cycle)

#define OSError_SetAbsAlarm_AlarmID()						(os_param1.SetAbsAlarm_AlarmID)
#define OSError_SetAbsAlarm_start()							(os_param2.SetAbsAlarm_start)
#define OSError_SetAbsAlarm_cycle()							(os_param3.SetAbsAlarm_cycle)

#define OSError_CancelAlarm_AlarmID()						(os_param1.CancelAlarm_AlarmID)

/* GetActiveApplicationMode missing because no parameters */

#define OSError_StartOS_Mode()								(os_param1.StartOS_Mode)

/* ShutdownOS missing because no parameters */
/* TerminateTask missing because no parameters */
/* Schedule missing because no parameters */

#define OSError_GetTaskState_TaskID()						(os_param1.GetTaskState_TaskID)
#define OSError_GetTaskState_State()						(os_param2.GetTaskState_State)

#define OSError_GetResource_ResID()							(os_param1.GetResource_ResID)

#define OSError_ReleaseResource_ResID()						(os_param1.ReleaseResource_ResID)

#define OSError_SetEvent_TaskID()							(os_param1.SetEvent_TaskID)
#define OSError_SetEvent_Mask()								(os_param2.SetEvent_Mask)

#define OSError_ClearEvent_Mask()							(os_param1.ClearEvent_Mask)

#define OSError_GetEvent_TaskID()							(os_param1.GetEvent_TaskID)
#define OSError_GetEvent_Event()							(os_param2.GetEvent_Event)

#define OSError_WaitEvent_Mask()							(os_param1.WaitEvent_Mask)

#define OSError_GetAlarmBase_AlarmID()						(os_param1.GetAlarmBase_AlarmID)
#define OSError_GetAlarmBase_Info()							(os_param2.GetAlarmBase_Info)

#define OSError_StartScheduleTableRel_ScheduleTableID()		(os_param1.StartScheduleTableRel_ScheduleTableID)
#define OSError_StartScheduleTableRel_Offset()				(os_param2.StartScheduleTableRel_Offset)

#define OSError_StartScheduleTableAbs_ScheduleTableID()		(os_param1.StartScheduleTableAbs_ScheduleTableID)
#define OSError_StartScheduleTableAbs_Tickvalue()			(os_param2.StartScheduleTableAbs_Tickvalue)

#define OSError_NextScheduleTable_ScheduleTableID_current()	(os_param1.NextScheduleTable_ScheduleTableID_current)
#define OSError_NextScheduleTable_ScheduleTableID_next()	(os_param2.NextScheduleTable_ScheduleTableID_next)

#define OSError_StopScheduleTable_ScheduleTableID()			(os_param1.StopScheduleTable_ScheduleTableID)

#define OSError_GetScheduleTableStatus_ScheduleID()			(os_param1.GetScheduleTableStatus_ScheduleID)

StatusType os_ActivateTask(TaskType);
StatusType os_ChainTask(TaskType);

void os_ResumeAllInterrupts(void);
void os_SuspendAllInterrupts(void);
void os_ResumeOSInterrupts(void);
void os_SuspendOSInterrupts(void);

StatusType os_GetAlarm(AlarmType, TickRefType);
StatusType os_SetRelAlarm(AlarmType, TickType, TickType);
StatusType os_SetAbsAlarm(AlarmType, TickType, TickType);
StatusType os_CancelAlarm(AlarmType);

StatusType os_StartScheduleTableRel(ScheduleTableType, TickType);
StatusType os_StartScheduleTableAbs(ScheduleTableType, TickType);
StatusType os_StopScheduleTable(ScheduleTableType);
StatusType os_NextScheduleTable(ScheduleTableType, ScheduleTableType);

/* $Req: artf1213 */
#define GetActiveApplicationMode() ((AppModeType)os_appmode)		/* AppModeType GetActiveApplicationMode(void); */

void os_StartOS(AppModeType);
void os_ShutdownOS(StatusType);

/* $Req: artf1145 $ */
#define DisableAllInterrupts()		{													\
										os_ipl tmp;										\
										SAVE_IPL(tmp);									\
										SET_IPL_MAX();									\
										os_dis_all_cnt++;	/* $Req: artf1044 $ */ 		\
										os_save_all_ipl = tmp;							\
									}

/* $Req: artf1147 $ */
#define EnableAllInterrupts()	(os_dis_all_cnt ? SET_IPL(os_save_all_ipl), os_dis_all_cnt = 0 : 0)		/* restore IPL to level when DisableAllInterrupts() was called $Req: artf1044 $ */

StatusType os_TerminateTask(void);
StatusType os_Schedule(void);
StatusType os_GetTaskState(TaskType, TaskStateRefType);
StatusType os_GetResource(ResourceType);
StatusType os_ReleaseResource(ResourceType);
StatusType os_SetEvent(TaskType, EventMaskType);
StatusType os_ClearEvent(EventMaskType);
StatusType os_GetEvent(TaskType, EventMaskRefType);
StatusType os_WaitEvent(EventMaskType);
StatusType os_GetAlarmBase(AlarmType, AlarmBaseRefType);
StatusType os_IncrementCounter(CounterType);
StatusType os_ExpireCounter(CounterType);
StatusType os_GetScheduleTableStatus(ScheduleTableType, ScheduleTableStatusRefType);

/* GetTaskID always returns E_OK in both standard and extended status; instead of passing a
 * pointer to the TaskID, instead the function returns the TaskID and the macro does the assignment.
 * This allows the compiler to do a much better job at optimization.
 * 
 * Macro can be invoked from a task, ISR or hook. The only static data accessed is os_curtask. This is
 * safe from a task because it is restored after a preemption (obviously!). It is also safe from an ISR
 * or a hook since the value cannot be changed in these.
 * 
 */

extern TaskType NEAR(os_curtask);					/* Currently running task */

/* $Req: artf1139 artf1140 $ */
#define GetTaskID(t)		((*(t) = ((os_curtask == OS_IDLE_TASK) ? INVALID_TASK : os_curtask)), E_OK)

/* Req: artf1052 artf1322 $ */
#define GetISRID(i)			((*(i) = ((os_curisr) ? INVALID_ISR : os_curisr)), E_OK)

/* Hook functions; default ones in the library if user doesn't supply one */
void ErrorHook(StatusType);			/* $Req: artf1222 $ */
void PreTaskHook(void);				/* $Req: artf1224 $ */
void PostTaskHook(void);			/* $Req: artf1225 $ */
void StartupHook(void);				/* $Req: artf1226 $ */
void ShutdownHook(StatusType);		/* $Req: artf1227 $ */

#ifndef OS_INTERNAL_BUILD
/* Define the standard named API to access the link-time namespace protected names
 */
#define ActivateTask			os_ActivateTask
#define ChainTask 				os_ChainTask
#define GetAlarm 				os_GetAlarm
#define SetRelAlarm 			os_SetRelAlarm
#define SetAbsAlarm				os_SetAbsAlarm
#define CancelAlarm 			os_CancelAlarm
#define StartScheduleTableRel 	os_StartScheduleTableRel
#define StartScheduleTableAbs 	os_StartScheduleTableAbs
#define StopScheduleTable 		os_StopScheduleTable
#define NextScheduleTable		os_NextScheduleTable
#define StartOS 				os_StartOS
#define ShutdownOS 				os_ShutdownOS
#define TerminateTask 			os_TerminateTask
#define Schedule 				os_Schedule
#define GetTaskState 			os_GetTaskState
#define GetResource 			os_GetResource
#define ReleaseResource 		os_ReleaseResource
#define SetEvent 				os_SetEvent
#define ClearEvent 				os_ClearEvent
#define GetEvent 				os_GetEvent
#define WaitEvent 				os_WaitEvent
#define GetAlarmBase 			os_GetAlarmBase
#define IncrementCounter 		os_IncrementCounter
#define ExpireCounter 			os_ExpireCounter
#define GetScheduleTableStatus 	os_GetScheduleTableStatus
#define SuspendOSInterrupts		os_SuspendOSInterrupts
#define ResumeOSInterrupts		os_ResumeOSInterrupts
#define SuspendAllInterrupts	os_SuspendAllInterrupts
#define ResumeAllInterrupts		os_ResumeAllInterrupts
#endif

/* Standard pre-defined handles (cannot allow this to be done via configuration process since library code needs to
 * be compiled prior to the configuration process). These are populated at link-time by the generated configuration.
 */

/* $Req: artf1206 $ */
extern TickType OSMAXALLOWEDVALUE;
extern TickType OSTICKSPERBASE;
extern TickType OSMINCYCLE;

/* See also oslibtarget.h for target-specific OSTICKDURATION definition */

/* $Req: artf1221 $ */

/* Handle to the default app mode, which is always generated as the first element by the config tool */
#define OSDEFAULTAPPMODE (&os_appmodes[0])

/* $Req: artf1100 $ */
extern const ResourceType RES_SCHEDULER;

#endif /*OSLIB_H_*/
