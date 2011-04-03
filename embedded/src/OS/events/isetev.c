/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-03-09 06:48:38 +0000 (Fri, 09 Mar 2007) $
 * $LastChangedRevision: 366 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/isetev.c $
 *  
 * Target CPU:		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * API call: StatusType SetEvent(TaskType <TaskID>, EventMaskType <Mask>)
 * 
 * The call sets a specified event.
 */

#include <osint.h>

/* This is a worker function to perform the API function. It is called from the API
 * and also can be called by the alarm processing handler.
 * 
 * Returns 1 if event ignored because task is suspended; 0 otherwise.
 */

unat os_set_event(TaskType t, EventMaskType e)
{
#ifdef OS_EXTENDED_STATUS
	if (*t->count == 0 && t->dyn->wait == 0) { /* If task is waiting for no events then wait == DUMMY_EVENT */
		/* $Req: artf1166 */
		return 1;				/* task t is suspended (i.e. not running/ready, not waiting; treated as an error */
	}
#endif
	/* $Req: artf1163 $ */
	t->dyn->set |= e;
	if (e & t->dyn->wait) {				/* Some of the events set are waited for */
		assert(*t->count == 0);			/* Extended task: must not be ready/running and waiting at the same time */
		t->dyn->wait = 0;				/* no longer waiting for ANY events */
		os_queuetask(t);
	}
	return 0;
}
