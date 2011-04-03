/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-23 02:56:13 +0000 (Fri, 23 Mar 2007) $
 * $LastChangedRevision: 401 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/osinttask.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */
 
#ifndef ITASK_H_
#define ITASK_H_

/* os_first_e_task/os_first_b_task etc. is set to 0 if there are no extended/basic tasks
 * The valid task check also checks that the handle is non-zero otherwise zero would be treated as valid. 
 */
#define EXTENDEDTASK(t)				(t->initsp)											/* Indicates if task is an extended task */
#define BASICTASK(t)				(!EXTENDEDTASK(t))									/* Indicates if task is a basic task */
#define VALID_EXTENDEDTASK(t)		((t) && (t) >= os_first_e_task && (t) <= os_last_e_task)
#define VALID_BASICTASK(t)			((t) && (t) >= os_first_b_task && (t) <= os_last_b_task)
#define VALID_TASK(t)				(VALID_BASICTASK(t) || VALID_EXTENDEDTASK(t))

/* Worker function to activate a task; called from API, alarm expiry handler, etc.
 * Returns 1 if task activation limit was reached, 0 otherwise
 */
unat os_activate_task(TaskType t);

#endif /*ITASK_H_*/
