/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-08 01:44:39 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 670 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/trace/osinttrace.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
/* Definitions for trace functions of kernel */

#ifndef OS_INTTRACE_H_
#define OS_INTTRACE_H_

#define OS_API_TRACE_SETRELALARM(AlarmID, start, cycle, short_now)						{TRACE_CODE(1U);TRACE_HANDLE(AlarmID);TRACE_TICK(start);TRACE_TICK(cycle);TRACE_TICK(short_now);TRACE_DONE();}
#define OS_API_TRACE_SETABSALARM(AlarmID, start, cycle, short_now)						{TRACE_CODE(2U);TRACE_HANDLE(AlarmID);TRACE_TICK(start);TRACE_TICK(cycle);TRACE_TICK(short_now);TRACE_DONE();}
#define OS_API_TRACE_CANCEL_ALARM_MULTI_AT_HEAD(short_now)								{TRACE_CODE(5U); TRACE_TICK(short_now);TRACE_DONE();}
#define OS_API_TRACE_CANCEL_ALARM(AlarmID)												{TRACE_CODE(6U);TRACE_HANDLE(AlarmID);TRACE_DONE();}

#define	OS_API_TRACE_COUNTER_EXPIRED_MULTI(short_now)									{TRACE_CODE(8U);TRACE_TICK(short_now);TRACE_DONE();}

#define OS_API_TRACE_EXPIRE_COUNTER(CounterID)											{TRACE_CODE(9U);TRACE_HANDLE(CounterID);TRACE_DONE();}

#define OS_API_TRACE_GET_ALARM(AlarmID)													{TRACE_CODE(11U);TRACE_HANDLE(AlarmID);TRACE_DONE();}
#define OS_API_TRACE_GET_ALARM_RUNNING(short_now)										{TRACE_CODE(12U);TRACE_TICK(short_now);TRACE_DONE();}

#define OS_API_TRACE_GET_ALARM_BASE(AlarmID)											{TRACE_CODE(14U);TRACE_HANDLE(AlarmID);TRACE_DONE();}

#define OS_API_TRACE_INCREMENT_COUNTER(CounterID)										{TRACE_CODE(15U);TRACE_HANDLE(CounterID);TRACE_DONE();}

#define OS_API_TRACE_CONTROL_DEVICE(Device, Code, Data)									{TRACE_CODE(17U);TRACE_HANDLE(Device);TRACE_UINT16(Code);TRACE_REF(Data);TRACE_DONE();}

#define OS_API_TRACE_CLEAR_EVENT(Mask)													{TRACE_CODE(19U);TRACE_MASK(Mask);TRACE_DONE();}

#define OS_API_TRACE_GET_EVENT(TaskID)													{TRACE_CODE(21U);TRACE_HANDLE(TaskID);TRACE_DONE();}

#define OS_API_TRACE_SET_EVENT(TaskID, Mask)											{TRACE_CODE(23U);TRACE_HANDLE(TaskID);TRACE_MASK(Mask);TRACE_DONE();}

#define OS_API_TRACE_WAIT_EVENT(Mask)													{TRACE_CODE(25U);TRACE_MASK(Mask);TRACE_DONE();}

#define OS_API_TRACE_GET_RESOURCE(ResID)												{TRACE_CODE(27U);TRACE_HANDLE(ResID);TRACE_DONE();}

#define OS_API_TRACE_SCHEDULE()															{TRACE_CODE(29U);TRACE_DONE();}

#define OS_API_TRACE_RELEASE_RESOURCE(ResID)											{TRACE_CODE(31U);TRACE_HANDLE(ResID);TRACE_DONE();}

#define OS_API_TRACE_GET_SCHED_TAB_STATUS(ScheduleID)									{TRACE_CODE(33U);TRACE_HANDLE(ScheduleID);TRACE_DONE();}

#define OS_API_TRACE_NEXT_SCHED_TAB(ScheduleTableID_current, ScheduleTableID_next)		{TRACE_CODE(35U);TRACE_HANDLE(ScheduleTableID_current);TRACE_HANDLE(ScheduleTableID_next);TRACE_DONE();}

#define OS_API_TRACE_START_SCHEDTAB_ABS(ScheduleTableID, Tickvalue)			{TRACE_CODE(37U);TRACE_HANDLE(ScheduleTableID);TRACE_TICK(Tickvalue);TRACE_DONE();}

#define OS_API_TRACE_START_SCHEDTAB_REL(ScheduleTableID, Offset)			{TRACE_CODE(39U);TRACE_HANDLE(ScheduleTableID);TRACE_TICK(Offset);TRACE_DONE();}
#define OS_API_TRACE_START_SCHED_TAB_NOW(now)								{TRACE_CODE(41U);TRACE_TICK(now);TRACE_DONE();}

#define OS_API_TRACE_STOP_SCHED_TAB(ScheduleTableID)						{TRACE_CODE(42U);TRACE_HANDLE(ScheduleTableID);TRACE_DONE();}

#define OS_API_TRACE_ACTIVATE_TASK(TaskID)									{TRACE_CODE(44U);TRACE_HANDLE(TaskID);TRACE_DONE();}

#define OS_API_TRACE_CHAIN_TASK(TaskID)										{TRACE_CODE(46U);TRACE_HANDLE(TaskID);TRACE_DONE();}

#define OS_API_TRACE_GET_TASK_STATE(TaskID)									{TRACE_CODE(48U);TRACE_HANDLE(TaskID);TRACE_DONE();}

#define OS_API_TRACE_AUTOSTART_ALARM_MULTI(short_now)						{TRACE_CODE(52U);TRACE_TICK(short_now);TRACE_DONE();}

#define OS_API_TRACE_AUTOSTART_ALARM_SINGLETON(short_now)					{TRACE_CODE(53U);TRACE_TICK(short_now);TRACE_DONE();}

#define OS_API_TRACE_DISPATCH_TASK(t)										{TRACE_CODE(54U);TRACE_HANDLE(t);TRACE_DONE();}
#define OS_API_TRACE_DISPATCH_ISR(i)										{TRACE_CODE(54U);TRACE_HANDLE(i);TRACE_DONE();}

#define OS_API_TRACE_DISPATCH_TOP()											{TRACE_CODE(54U);TRACE_DONE();}
#define OS_API_TRACE_DISPATCH_BOTTOM()										{TRACE_CODE(61U);TRACE_DONE();}

#define OS_API_TRACE_ERROR_HOOK(e)											{TRACE_CODE(68U);TRACE_STATUS(e);TRACE_DONE();}

#define OS_API_TRACE_SHUTDOWN_HOOK(e)										{TRACE_CODE(68U);TRACE_STATUS(e);TRACE_DONE();}
#define OS_API_TRACE_SHUTDOWN_OS()											{TRACE_CODE(68U);TRACE_DONE();}

#define OS_API_TRACE_PRETASK_HOOK()											{TRACE_CODE(68U);TRACE_DONE();}
#define OS_API_TRACE_POSTTASK_HOOK()										{TRACE_CODE(68U);TRACE_DONE();}

#define OS_API_TRACE_START_OS()												{TRACE_CODE(71U);TRACE_DONE();}
#define OS_API_TRACE_FINISH_OS()											{TRACE_CODE(72U);TRACE_DONE();}

#define OS_API_TRACE_ACTIVATE_TASK_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_CANCEL_ALARM_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_CHAIN_TASK_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_CLEAR_EVENT_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_CONTROL_DEVICE_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}

#define OS_API_TRACE_DISPATCH(t)											{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}

#define OS_API_TRACE_EXPIRE_COUNTER_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_COUNTER_VALUE(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_COUNTER_VALUE_FINISH(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_ELAPSED_COUNTER_VALUE(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_ELAPSED_COUNTER_VALUE_FINISH(rc)					{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}

#define OS_API_TRACE_GET_ALARM_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_EVENT_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_RESOURCE_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_SCHED_TAB_STATUS_FINISH(rc)						{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_TASK_STATE_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_INCREMENT_COUNTER_FINISH(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_NEXT_SCHED_TAB_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_RELEASE_RESOURCE_FINISH(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_RESUME_ALL_INTERRUPTS()
#define OS_API_TRACE_RESUME_OS_INTERRUPTS()
#define OS_API_TRACE_SCHEDULE_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_SET_EVENT_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_SETABSALARM_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_SETRELALARM_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_START_SCHEDTAB_ABS_FINISH(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_START_SCHEDTAB_REL_FINISH(rc)							{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_STOP_SCHED_TAB_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_SUSPEND_ALL_INTERRUPTS()
#define OS_API_TRACE_SUSPEND_OS_INTERRUPTS()
#define OS_API_TRACE_DISABLE_ALL_INTERRUPTS()
#define OS_API_TRACE_ENABLE_ALL_INTERRUPTS()
#define OS_API_TRACE_WAIT_EVENT_FINISH(rc)									{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_GET_SCHED_TAB_STATUS_FINISH(rc)						{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}
#define OS_API_TRACE_NEXT_SCHED_TAB_FINISH(rc)								{TRACE_CODE(32U);TRACE_STATUS(rc);TRACE_DONE();}

#endif /*OS_INTTRACE_H_*/	