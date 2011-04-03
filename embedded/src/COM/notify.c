/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/notify.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

/* Notification processing function is called only when action is non-zero (i.e. there is a notification to process).
 * 
 * Notification does not handle flags since these are handled via a separate mechanism (pointers to
 * scratch words when not needed).
 * 
 * Returns a StatusType indicating E_COM_SYS_NOTIFICATION if attempted notification failed, or E_OK on success
 */
StatusType com_notify(const struct com_notifycb *data, enum com_notify_action action)
{
	StatusType rc = E_OK;
	
	assert(action);
	
	switch(action) {
		case COM_NOTIFY_ACTIVATE_TASK: 		/* Task activation $Req: artf1249 $ */
			{
				const TaskType TaskID = ((const struct com_notify_taskcb *)data)->task;
#ifdef COM_EXTENDED_STATUS				
				if ( ActivateTask(TaskID) != E_OK ) {
					rc = E_COM_SYS_NOTIFICATION;
				}
#else
				ActivateTask(TaskID);
#endif
			}
			break;
			
		case COM_NOTIFY_SET_EVENT: 			/* Setting an event $Req: artf1250 $ */
			{
				const TaskType TaskID = ((const struct com_notify_eventcb *)data)->task;
				const EventMaskType Mask = ((const struct com_notify_eventcb *)data)->event;
#ifdef COM_EXTENDED_STATUS			
				if ( SetEvent(TaskID, Mask) != E_OK ) {
					rc = E_COM_SYS_NOTIFICATION;
				}
#else
				SetEvent(TaskID, Mask);
#endif
			}
			break;
			
		case COM_NOTIFY_CALLBACK: 			/* Calling a callback $Req: artf1247 $ */
			{
				/* Runs at level of caller (see OSEK COM 3.0.3 spec 2.6.3 p32) */
				/* May call COM API calls $Req: artf1259 $ */
				/* @TODO task1041 does artf1259 lead to resource locking problems: the caller calls a notification that calls the COM layer that
				 * calls SendMessage? And what stops recursive notifications?
				 */
				((const struct com_notify_callbackcb *)data)->callback();
			}
			break;
			
		default:
			assert(0);
			/* NOTREACHED */
			break;
	}
	return rc;
}
