/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-02-12 13:22:44 +0000 (Mon, 12 Feb 2007) $
 * $LastChangedRevision: 361 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/OS/events/osintevent.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#ifndef IEVENT_H_
#define IEVENT_H_

/* Worker function to do the work of setting an event; called by API and alarm handler */
unat os_set_event(TaskType, EventMaskType);

#endif /*IEVENT_H_*/
