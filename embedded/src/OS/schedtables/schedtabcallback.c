/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2008-03-24 06:32:28 +0000 (Mon, 24 Mar 2008) $
 * $LastChangedRevision: 692 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/schedtables/schedtabcallback.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

static void process_elements(int i, const struct os_xpoint_element *element)
{
	/* Process all the actions associated with the current expiry point (note: i may be zero for a null expiry point)
	 * 
	 * An action may be the activation of a task ($Req: artf1363 $)
	 * An action may be the setting of an event ($Req: artf1364 $) 
	 */
	while(i) {
		assert(element);
		assert(element->action);
		assert(element->process);
		element->process(element->action);
		element++;
		i--;
	}
}

/* Logically this belongs in the counter subsystem, but it is only called from here and is better inlined
 * by the compiler by being a static function. Can be moved elsewhere should it be needed to be called from
 * more than one place.
 */
static void substitute_alarm(CounterType c, AlarmType old, AlarmType new, TickType cycle)
{
	assert(!SINGLETON_COUNTER(c));
	assert(old->dyn.c->running == 1U);
	assert(new->dyn.c->running == 0);
	assert(c == old->counter);
	assert(c == new->counter);
	assert(c->dyn->head == old);
	
	/* Copy alarm settings */
	new->dyn.c->due = old->dyn.c->due;
	new->dyn.c->running = 1U;
	new->dyn.c->cycle = cycle;
	
	new->dyn.m->next = old->dyn.m->next;

	/* Old alarm at the head of the queue; new alarm replaces the head */
	c->dyn->head = new;
	/* new->dyn.m->prev = "don't care" since it's the first item in the queue */
	/* Stitch the subsequent alarm back to the new one (from the old one) */
	if(new->dyn.m->next) {
		assert(new->dyn.m->next->dyn.m->prev == old);
		new->dyn.m->next->dyn.m->prev = new;
	}
	
	old->dyn.c->running = 0;
}

/* Function is a handler to process a schedule table
 * 
 * $Req: artf1032 $
 * 
 * Called before the alarm is processed. The value of 'cycle' within the
 * dynamic alarm control block indicates the time for the alarm to next
 * go off (0 indicates 'stop the alarm').
 * 
 * All schedule tables are linked lists of expiry points (null terminated),
 * and all have a zero-offset expiry point at the start (which may or may
 * not contain expiry actions).
 * 
 * The processing of the first expiry point is handled differently
 * depending on circumstances.
 * 
 * 1. If the schedule table has been started up and this is the first
 *    run through, or is periodic and there is no 'nexted' table to
 *    replace it then the expiry point is processed as normal.
 * 
 * 2. If the table is to be stopped because:
 *      i)   It is one-shot
 *      ii)  It has been replaced by a 'nexted' table
 *    then the point is not processed.
 * 
 * If the table is to be replaced by a 'nexted' table then some more
 * work is required:
 *   i)   The first expiry point of the new table is to be processed
 *   ii)  The new table is set running with the next expiry point. This
 *        is done by substituting the alarms for the old and new tables.
 *
 * Depending on circumstances, the final expiry point is handled
 * differently:
 * 
 * 1. If the schedule table has been 'nexted' then the cycle time of
 *    the alarm is set to the delta from 1st to 2nd expiry points on
 *    the new table.
 * 2. If the schedule table has not been nexted then the cycle time of
 *    the alarm is set to the delta from the 1st to 2nd expiry points
 *    on the old table.
 * 
 * Furthermore, when a table is 'nexted' and the final expiry point of
 * the old table has already been processed then the cycle time of the
 * alarm is set to the delta from the 1st to the 2nd expiry points on
 * the new table.
 * 
 * All the above ensures that the table is deemed 'running' until the end, and
 * that at any point prior to the end it is possible to chain a table
 * to a next one. Thus a task activated after the last expiry point can
 * still 'next' a table.
 */
void os_expiry_schedtab(const struct os_expirycb *exp)
{
	ScheduleTableType const tab = ((const struct os_expiry_schedtabcb *)exp)->schedtable;
	struct os_schedtabcb_dyn * const tab_dyn = tab->dyn;
	AlarmType const a = tab->alarm;
	CounterType const c = a->counter;
	const struct os_xpoint * current = tab_dyn->current_xpoint;
	unat const i = current->num_actions;
	const struct os_xpoint_element * element = current->actions;
	
	assert(KERNEL_LOCKED());
	
#ifdef OS_EXTENDED_STATUS
	assert(VALID_SCHEDTAB(tab));
	assert(VALID_ALARM(a));
	assert(VALID_COUNTER(c));
#else
	/* Can't use VALID_x macros outside of extended status, so just assert non-zero values */
	assert(tab);
	assert(a);
	assert(c);
#endif
	assert(current);

	/* Process the expiry point and set up for the next one if any of:
	 * 
	 * (i) The first run of the table since started 
	 * (ii) Not the first expiry point
	 * (iii) Periodic and not nexted
	 * 
	 * If the above doesn't apply then either the next table should be
	 * run (if there is one set) or else the current table should be stopped.
	 */
	if(tab_dyn->first_run || current != tab->first_xpoint || (tab->periodic && !tab_dyn->next_tab)) {
#ifdef NDEBUG
		if(tab_dyn->first_run) {
			assert(current == tab->first_xpoint);
		}
		else {
			assert(current != tab->first_xpoint);
		}
#endif
		/* Future expiry points are not the first time (unless the table is restarted
		 * from scratch at some point in the future).
		 */ 
		tab_dyn->first_run = 0;
		
		/* Set the alarm to expire at the right point */
		a->dyn.c->cycle = current->delta;
		/* Process the expiry point */
		process_elements(i, element);
		/* Move along the table to the next point */
		tab_dyn->current_xpoint = current = current->next_xpoint;	
	}
	else {
		ScheduleTableType const next_tab = tab_dyn->next_tab;

		if(next_tab) {
			/* If there is a next table then we must process the first point of that one
			 * and stop this processing this one.
			 */

			/* It is not possible for the table to be running and also nexted. If it's running, can't make
			 * it next. Currently an ambiguity in the spec to allow it to be set running if it is already nexted.
			 * (we interpret "started" in 8.4.8 and 8.4.9 of the AUTOSAR OS spec to mean "running right now or nexted").
			 * 
			 * If interpretation is invalid then need to guard the substitute_alarm() call with an if(!running)
			 */
			assert(!next_tab->alarm->dyn.c->running);
			
			/* The 'next' attribute is reset once the chaining takes place */
			tab_dyn->next_tab = 0;
			
			/* Process the first element of the next table instead of the first element of the current one */
			process_elements(next_tab->first_xpoint->num_actions, next_tab->first_xpoint->actions);
			
			/* Should set the cycle for the current alarm to zero and queue the new table's alarm in at the right
			 * point, but much faster to simply swap the next table's alarm in the counter queue.
			 */
			substitute_alarm(c, tab->alarm, next_tab->alarm, next_tab->first_xpoint->delta);
			
			/* Old alarm is now stopped, new alarm (in new table) now running with the same timing behaviour */
			assert(tab->alarm->dyn.c->running == 0);
			assert(next_tab->alarm->dyn.c->running == 1U);
			
			/* Make sure that the first_run flag is cleared down: we have just (in effect) processed
			 * the first expiry point of the next table, and the next table is now running.
			 */
			next_tab->dyn->first_run = 0;
			
			/* Move along to next point in new table */
			next_tab->dyn->current_xpoint = next_tab->first_xpoint->next_xpoint;
		}
		else {
			/* One shot, not nexted, and finished, so stop the alarm and don't process anything
			 * at the beginning of the table.
			 */
			a->dyn.c->cycle = 0;
		}
	}
}

/* 
 * 
 * The standard processing on a point is done if:
 * 
 * not the first point || first_run || (periodic and not nexted)
 * 
 * else
 * 
 * if nexted, substitute etc.
 * else stop
 */
