/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-09 06:48:38 +0000 (Fri, 09 Mar 2007) $
 * $LastChangedRevision: 366 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/tasks/iact.c $
 * 
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <osint.h>

/* Worker function for activate task. Returns 1 if hit the counter limit, otherwise returns 0. Called
 * from API call, alarm handler @TODO and COM notification
 */
unat os_activate_task(TaskType t)
{
	if (*t->count < t->countlimit) {		/* Hit the counter limit? $Req: artf1086 $ */
		/* If an extended task it must have a count limit of 1 and be suspended;
		 * if a basic task then all share the same 'scratch' dynamic part with wait == 0
		 */
		assert(t->dyn->wait == 0);			
		
		
		/* Clear down any events that have been set for the task. Extended tasks have their own dynamic TCB part;
		 * All basic tasks share a scratch dynamic part
		 */
		t->dyn->set = 0;					/* $Req: artf1126 $ */
		
		/* $Req: artf1125 $ */
		os_queuetask(t);						/* place the task on the appropriate priority queue */

		return 0;
	}
	else {
		return 1;
	}
}
