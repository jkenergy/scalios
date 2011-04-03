/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/recsmsg.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

StatusType ReceiveStreamMessage(MessageIdentifier Message, ApplicationDataRef DataRef, LengthRef DataSize)
{
	StatusType rc;
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_RECEIVE_STREAM_MESSAGE);
	COM_TRACE_HANDLE(Message);
	COM_TRACE_DATA_REF(DataRef);
	COM_TRACE_LENGTH(DataSize);
	COM_TRACE_OFF();
	
	COM_API_ENTER();
	
#ifdef COM_EXTENDED_STATUS
	if(!COM_STARTED()) {
		rc = E_COM_SYS_STATE;							/* COM not yet started */
	}
	else if(!IS_RECEIVE_MESSAGE(Message)) {
		rc = E_COM_ID;									/* Not a valid receiving Message $Req: artf1291 $ */
	}
	else {
		assert(Message->cb.receiver->driver->recv_stream);
		rc = Message->cb.receiver->driver->recv_stream(Message->cb.receiver, DataRef, DataSize);
		/* rc is E_OK if the driver operation succeeded */
	}
#else	
	rc = Message->cb.receiver->driver->recv_stream(Message->cb.receiver, DataRef, DataSize);
#endif
	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		/* $Req: artf1265 $ */
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_3(ReceiveStreamMessage, rc, Message, DataRef, DataSize);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	COM_API_LEAVE();	/* Leave API and shutdown COM layer if shutdown pending */
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_RECEIVE_STREAM_MESSAGE_FINISH);
	COM_TRACE_OFF();
	
	return rc;
}
