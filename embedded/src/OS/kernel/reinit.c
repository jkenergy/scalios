/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-21 19:55:27 +0000 (Fri, 21 Mar 2008) $
 * $LastChangedRevision: 680 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/kernel/reinit.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal 
 * 
 * This function restores to the state that the C runtime startup would have left
 * the data structures prior to a StartOS call.
 */

#include <osint.h>


/* 
 * re-initialise the OS variables ready for a call to StartOS() following a ShutdownOS()
 */
void os_reinit(void)
{
	int i;
	TaskType t;
#ifdef OS_EXTENDED_STATUS	
	ResourceType r;
#endif	

	os_curtask = OS_IDLE_TASK;							/* setup os_curtask to be os_idle task handle */
	os_curpri = IDLEPRI;

#ifdef STACK_CHECKING	
	os_curtos = OS_KS_TOP;
#endif
	
	os_curisr = 0;
	os_curenv = 0;
	os_kssp = 0;
#ifdef OS_EXTENDED_STATUS
	os_curlastlocked = 0;
	
	r = &os_resources[0];
	
	for (i = os_num_res; i > 0; i--) {
		
		r->dyn->prevlocked = 0;
		
		/* no need to clear r->dyn->prev but may decide to set to zero if accessed by new API call, e.g. TestResource(r) */
		
		r++;
	}
#endif
	
	os_chaintask = 0;
	
	os_dis_all_cnt = 0;		/* Clear counters for disable/suspend/resume API calls $Req: artf1069 $*/
	os_sus_all_cnt = 0;
	os_sus_os_cnt = 0;
	
	/* Reinitialise all extended task dynamic control block values */
	t = &os_e_tasks[0];
	
	for (i = os_num_e_tasks; i > 0; i--) {
		
		t->dyn->restore = 0;
		t->dyn->savesp = t->initsp;
		t->dyn->wait = 0;
		/* Don't need to clear dyn->set since this will be done when task is activated and accessing set via
		 * GetEvent illegal while task is suspended.
		 */
		 
		*(t->count) = 0;
		
		t++;
	}
	
	/* Reinitialise all basic task control block values */
	t = &os_b_tasks[0];

	for (i = os_num_b_tasks; i > 0; i--) {
		*(t->count) = 0;
		t++;
	}

	/* Reinitialise all dynamic parts of the task activation queues */
	
	/* No need to os_reinit os_priqueuestatus since always setup by autostarttasks() call by StartOS() */
		
	assert(os_highest_task_pri <= (sizeof(os_priqueuestatus) * 8));

	/* Initialize the priority queues */
	for (i = 0; i < os_highest_task_pri - 1; i++) {
		
		os_queueh q = os_priqueue[i];
		
		assert(q);
		if (q->uniquetask == 0) {
			assert(q->dyn);
			assert(q->first);
			assert(q->last);
			q->dyn->head = q->dyn->tail = q->first;
		}
	}
	/* Reinitialize counter dynamic parts */
	for (i = 0; i < os_num_counters; i++) {
		CounterType c = &os_counters[i];
		
		if(!SINGLETON_COUNTER(c)) {
			c->dyn->long_tick = 0;
			c->dyn->last_now = 0;
			c->dyn->head = 0;
		}
	}
	
	/* Reinitialize alarm dynamic parts */
	for (i = 0; i < os_num_alarms; i++) {
		AlarmType a = &os_alarms[i];
		
		a->dyn.c->running = 0;
	}

	/* Reinitialize schedule table dynamic parts */
	for (i = 0; i < os_num_schedtables; i++) {
		ScheduleTableType t = &os_schedtables[i];
		
		t->dyn->nexted = 0;
		t->dyn->next_tab = 0;
		t->dyn->current_xpoint = t->first_xpoint;	/* Needed for autostarted tables in any given application mode */
		assert(t->dyn->current_xpoint);
	}

	/* Reinitialise the OS flags using target specific os_reinit code */
	os_flags = os_init_flags;
}
