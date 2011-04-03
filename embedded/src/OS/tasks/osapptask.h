/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-01-11 02:29:43 +0000 (Fri, 11 Jan 2008) $
 * $LastChangedRevision: 555 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/osapptask.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

#ifndef TASK_H_
#define TASK_H_

/* Task control block; arbitrary number of tasks supported (limited only by memory) $Req: artf1087 $ */
/* Arbitrary number of internal resources due to boostpri mechanism; $Req: artf1092 $*/
struct os_taskcb {							/* Static (ROM) part of the task control block */
	struct os_ext_taskcb_dyn *dyn;			/* Accessor to the dynamic part of the control block (extended tasks only, basic tasks share a 'scratch' control block) */
	unat *count;							/* Accessor to the current Activation count (stored in RAM) $Req: artf1085 $ */
	unat countlimit;						/* Maximum number of task activations permitted */
	os_pri basepri;							/* Base priority of task */
	os_pri boostpri;						/* Boosted priority of task; based on the highest ceiling of the internal resources of this task; will always be IPL 0 */
	os_primask basepri_mask;				/* Bit mask with bit set corresponding to base priority of task */
	os_queueh queue;						/* Queue control block indicates head and tail of the ring buffer (i.e. FIFO queue) corresponding to task's base priority (0 when only task with this base-priority) $Req: artf1085 $ */
	os_entryf entry;						/* Entry function for task */
	os_stackp initsp;						/* Initial stack pointer (extended tasks only; 0 for basic tasks) used to test if extended task (see EXTENDEDTASK) */
	os_stackp tos;							/* Top-of-stack limit (extended tasks only); target-specific meaning (might mean last permissable used value, might mean highest permitted stack pointer value) */
	nat stackoffset;						/* Offset (in bytes) applied to the stack pointer to obtain the top-of-stack limit (basic tasks only); includes task stack usage, kernel overhead prior to task entry, and any target-specific offset to convert stack pointer into stack limit; @todo union with above */
};

/* ISR control block */
/* @port need to identify size of this structure and specify in platformInfo class of the generator */
struct os_isrcb {
	os_pri basepri;						/* Base priority of ISR, IPL always >0 (no boost os_pri because ISRs cannot have internal resources) */
	os_entryf handler;						/* Handler function for task */
	nat stackoffset;					/* Offset applied to the stack pointer (in bytes); includes stack usage, kernel overhead prior to ISR handler entry, and stack limit offsets */
};

struct os_ext_taskcb_dyn {					/* Dynamic (RAM) stored part of the task control block (extended tasks each have there own copy of this, basic tasks share single 'dummy' copy) */
	unat restore;						/* Run the extended task (create a context from scratch or switch to a new one) @todo change to flag (init to 0)*/
	os_stackp savesp;						/* Saved stack pointer */
	EventMaskType set;					/* Mask of all the events that have been set on this task */
	EventMaskType wait;					/* Mask of all the events that are waited for by this task; if waiting never zero (i.e. always have to wait on something) */
};

/* Used for ID checks */
extern const TaskType os_first_e_task;		/* Might be 0 if no extended tasks */
extern const TaskType os_last_e_task;		/* Might be 0 if no extended tasks */
extern const TaskType os_first_b_task;		/* Might be 0 if no basic tasks (the idle task does not count in this case) */
extern const TaskType os_last_b_task;		/* Might be 0 if no basic tasks (the idle task does not count in this case) */

extern const struct os_taskcb const os_e_tasks[];			/* Extended task control block instances */
extern const struct os_taskcb const os_b_tasks[];			/* Basic task control block instances */
extern const struct os_isrcb const os_isrs[];				/* (Cat 2) ISR control block instances */

#define OS_IDLE_TASK 	(&os_b_tasks[0])			/* Idle task is always first basic task in os_b_tasks array */

#endif /*TASK_H_*/
