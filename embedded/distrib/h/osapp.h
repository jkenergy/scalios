/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-02-12 13:22:44 +0000 (Mon, 12 Feb 2007) $
 * $LastChangedRevision: 361 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/osapp.h $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		External
 */

#ifndef OSAPP_H_
#define OSAPP_H_

#define OS_APPLICATION_BUILD

/* Include library-level api headers */
#include <oslib.h>

/* Include public generation headers */
#include <osappcompiler.h>
#include <osapptarget.h>
#include <osappcore.h>

/* Include build-time generated header, which makes handles etc. visible */
#include "oshandles.h"

/* There are optimizations that can be made when at build time we know if we are using standard status */
#ifdef OS_STANDARD_STATUS
#undef ChainTask
/* ChainTask can return only E_OS_LIMIT in standard status (if it returns at all) */
#define ChainTask(t)	 				(os_ChainTask(t), E_OS_LIMIT)

#undef TerminateTask
#define TerminateTask() 				(os_TerminateTask(), E_OK)

#undef Schedule
#define Schedule() 						(os_Schedule(), E_OK)

#undef GetTaskState
#define GetTaskState(tid, tsr) 			(os_GetTaskState((tid), (tsr)), E_OK)

#undef GetResource
#define GetResource(rid) 				(os_GetResource(rid), E_OK)

#undef ReleaseResource
#define ReleaseResource(rid)			(os_ReleaseResource(rid), E_OK)

#undef SetEvent
#define SetEvent(tid, m)				(os_SetEvent((tid), (m)), E_OK)

#undef ClearEvent
#define ClearEvent(m) 					(os_ClearEvent(m), E_OK)

#undef GetEvent
#define GetEvent(tid, m) 				(os_GetEvent((tid), (m)), E_OK)

#undef WaitEvent
#define WaitEvent(m) 					(os_WaitEvent(m), E_OK)

#undef GetAlarmBase
#define GetAlarmBase(AlarmID, Info)		((*(Info) = (AlarmID)->counter->alarmbase), E_OK)

#undef IncrementCounter
#define IncrementCounter(c)				(os_IncrementCounter(c), E_OK)

#undef ExpireCounter
#define ExpireCounter(c) 				(os_ExpireCounter(c), E_OK)

#undef GetScheduleTableStatus
#define GetScheduleTableStatus(sid, ss)	(os_GetScheduleTableStatus((sid), (ss)), E_OK)

#endif /* OS_STANDARD_STATUS */

#endif /*OSAPP_H_*/
