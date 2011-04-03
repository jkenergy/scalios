/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-04-14 01:20:49 +0100 (Sat, 14 Apr 2007) $
 * $LastChangedRevision: 415 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/osappkernel.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */
 
#ifndef KERNEL_H_
#define KERNEL_H_
																									 
/* Finding the highest priority ready task
 * ---------------------------------------
 * 
 * To find the highest priority runnable task the first set bit in the os_priqueuestatus word is 
 * found (often there is a special CPU instruction for this). The bit number is used to index
 * the os_priqueue array which points to the queue control block. Following this to the dynamic part
 * gives the head of the queue.
 * 
 * Optimization to reduce RAM
 * --------------------------
 * 
 * OSEK requires that tasks are queued in strict FIFO order, so if task A and task B are at the same priority and activated in
 * order A, B, A, B then they are dispatched A, B, A, B. For tasks with a large activation count this would normally result in
 * a large amount of RAM. But there is an optimization for a priority queue where there is exactly one task with a base priority
 * with the corresponding priority: the queue is storage is eliminated and the task activation count is used to indicate the number
 * of 'queued' entries of the task (this count is required anyway in order to return E_OS_LIMIT correctly).
 * 
 * If this optimization is triggered (at application build time) then the os_queuecb first and last pointers are null and a special
 * pointer to uniquetask indicates the single unique task with the corresponding priority. In the task control block of that
 * task the queue member of os_taskcb is null (indicating there is no need to look at any queue in order to enqueue/dequeue the task).
 * 
 * It follows from this optimization that an active FIFO queue will never have just one element (ie. first will never be last).
 */

typedef const struct os_queuecb * os_queueh;

struct os_queuecb_dyn {
	/* Head points to the first filled item; Tail points to the first free slot (unless Head == Tail, in which case the
	 * queue is empty)
	 * 
	 * Queue is composed of TaskType items. Head/Tail is therefore a pointer-to-a-pointer.
	 */
	TaskType *head;									/* Points to the head of the corresponding FIFO priority queue; size is 0 .. (sizeof(os_primask) * 8) - 1 */
	TaskType *tail;									/* Points to the tail of the corresponding FIFO priority queue */
};

struct os_queuecb {
	struct os_queuecb_dyn *dyn;						/* Access to dynamic part of the os_queuecb (null if uniquetask != 0) */	
	TaskType uniquetask;								/* Indicates the unique task that can be in the queue (null if more than one task shares the same base priority) */
	TaskType *first;									/* Indicates the first piece of memory reserved for the corresponding FIFO priority queue (null if uniquetask != 0) */
	TaskType *last;									/* Indicates the last piece of memory reserved for the corresponding FIFO priority queue (null if uniquetask != 0) */
};

/* A priority queue consists of a ROM-based queue control block (os_queuecb) that indicates the start and end of a queue. A queue is a block
 * of os_taskcb pointers. The dynamic part of the os_queuecb indicates the head and the tail. A ring-buffer system is operated, with tasks taken
 * from the head, and added to the tail (which points to the first free slot, unless the queue is full in which case head == tail).
 * 
 * The array os_priqueue provides an index from numeric priority to the corresponding priority queue.
 * os_priqueue[0] corresponds to numeric priority 1 (the idle task has numeric priority 0 but does not
 * appear in the priority queues since the idle task is always active and is never terminated or
 * activated).
 *
 * $Req: artf1088 $
 * $Req: artf1096 $ 
 */ 
 
extern os_queueh FASTROM(os_priqueue[]);						/* Queue is an array in ROM of os_queuecb structures corresponding to the bits in os_priqueuestatus.
																 *
													 			 * If no task has the corresponding priority then the pointer is null (the bit in
																 * os_priqueuestatus will never be set).
											 					 */
/* Auto-start information for alarms */
struct os_auto_alarm {
	AlarmType alarm;											/* The alarm being autostarted */
	TickType rel;												/* the relative time at which to start (or, if this alarm is bound to an autostarted schedule table, the expiry time of the first expiry point of a schedule table) */
	TickType cycle;												/* the cycle time (0 for singleshot alarms; "delta" from the first expiry point if the alarm is bound to an autostarted schedule table) */
};

/* @TODO task1039 gather globals up into a global struct for namespace control */

struct os_appmodecb {								/* Static (ROM) based control block for each app mode */
	TaskType const *autotasks;						/* ptr to array of tasks that are autostarted in the appmode */
	unat numautotasks;								/* the number of tasks autostarted */
	const struct os_auto_alarm *auto_m_alarms;		/* ptr to array of auto started alarm instances (based on non-singleton counters) */
	unat num_auto_m_alarms;							/* the number of auto started non-singleton alarms */
	const struct os_auto_alarm *auto_s_alarms;		/* ptr to array of auto started alarm instances (based on singleton counters) */
	unat num_auto_s_alarms;							/* the number of auto started singleton alarms */

	void (*start_alarms_multi)(AppModeType);		/* Function that autostarts multi-alarms  (empty function if none) */
	void (*start_alarms_singleton)(AppModeType);	/* Function that autostarts singleton alarms (empty function if none) */
};

extern const struct os_appmodecb os_appmodes[];	/* Application mode control block instances */

void os_runcreatecx(void);						/* Create new context for an extended task then run it */

void os_reinit(void);
extern void (* const os_reinitf)(void);		/* Pointer to the os_reinit() if reinitialisation required, else null. */

extern void (* const os_queuetask)(TaskType);	/* Pointer to appropriate os_queuetask function (optimized or standard) */
extern void (* const os_dequeuetask)(void);	/* Pointer to appropriate os_queuetask function (optimized or standard) */

/* Pointer to appropriate dispatch function (set by configuration to os_ks_dispatch_bt() if basic tasks only,
 * or os_swst_dispatch_et() for a build with only extended tasks or os_swst_dispatch_mix() for builds with both extended and basic tasks)
 */
extern void (* const os_swst_dispatch)(void);

/* Pointer to appropriate dispatch function (set by configuration and called from ISR wrappers and start of OS; os_ks_dispatch_bt()
 * if basic tasks only, os_ks_dispatch_et() if extended only, os_ks_dispatch_mix() if both
 */
extern void (* const os_ks_dispatch)(void);

/* Pointer to appropriate terminate function (os_terminate_bt() basic tasks only,
 * os_terminate_et() extended tasks only, os_terminate_mix() for mixed). Instantiated by
 * build tool.
 */
extern void (* const os_terminate)(void);	

extern const unat os_num_e_tasks;				/* number of extended tasks; only used by os_reinit() */
extern const unat os_num_b_tasks;				/* number of basic tasks; only used by os_reinit() */
extern const unat os_num_res;					/* number of resources; only used by os_reinit() */
extern const unat os_highest_task_pri;		/* number of unique task priorities; only used by os_reinit() */

/* Build-time optimization where we select the appropriate one of these functions to link in */
void os_queuetask_opt(TaskType);	
void os_dequeuetask_opt(void);
void os_queuetask_std(TaskType);	
void os_dequeuetask_std(void);

/* Run all higher priority ready tasks */
void os_ks_dispatch_et(void);	
void os_ks_dispatch_bt(void);	
void os_ks_dispatch_mix(void);

/* Terminate current task */
void os_terminate_et(void);	
void os_terminate_bt(void);	
void os_terminate_mix(void);

/* (Potentially) switch to kernel stack and dispatch */
void os_swst_dispatch_mix(void);
void os_swst_dispatch_et(void);

/* Global flags in RAM; stored as bitfields because the compiler generates good code */
struct os_flags {
	unsigned errorhook:1;
	unsigned pretaskhook:1;
	unsigned posttaskhook:1;
	unsigned startuphook:1;
	unsigned shutdownhook:1;
	unsigned getserviceid:1;		/* enable use of OSErrorGetServiceId() Macro (@TODO task1027 check spec. conflict between OSEK and OIL) */
	unsigned parameteraccess:1;		/* enable use of Parameter Access Macros in ErrorHook */
};

extern struct os_flags NEAR(os_flags);
extern const struct os_flags os_init_flags;

#endif /*KERNEL_H_*/
