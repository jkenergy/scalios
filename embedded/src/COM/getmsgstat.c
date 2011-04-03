/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/getmsgstat.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * GetMessageStatus() can be called from a COM error hook. $Req artf1268 $
 * Returns the current status of the given message. $Req: artf1296 $
 * Works for internal messages $Req: artf1255 $
 * 
 * @TODO task1041 review this call to see if there is a problem with error hooks calling and accessing COM layer.
 */

#include <comint.h>

StatusType GetMessageStatus(MessageIdentifier Message)
{
	StatusType rc;

	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_GET_MESSAGE_STATUS);
	COM_TRACE_HANDLE(Message);
	COM_TRACE_OFF();
	
	COM_API_ENTER();

#ifdef COM_EXTENDED_STATUS
	if (!COM_STARTED()) {
		rc = E_COM_SYS_STATE;							/* COM not yet started */
	}
	else if(!IS_RECEIVE_MESSAGE(Message)) {
		rc = E_COM_ID;									/* Not a valid receive Message $Req: artf1300 $ */
	}
	else {
		assert(Message->cb.receiver->driver->receiver_status);
		rc = Message->cb.receiver->driver->receiver_status(Message->cb.receiver);
	}
#else /* STANDARD_STATUS */
	rc = Message->cb.receiver->driver->receiver_status(Message->cb.receiver);
#endif

	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		/* $Req: artf1265 $ */	
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_1(GetMessageStatus, rc, Message);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	COM_API_LEAVE();	/* Leave API and shutdown COM layer if shutdown pending */
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_GET_MESSAGE_STATUS_FINISH);
	COM_TRACE_OFF();
	
	return rc;
}

