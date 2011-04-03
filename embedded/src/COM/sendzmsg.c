/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/sendzmsg.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 * 
 * SendZeroMessage() isn't in OSEK COM CCCB but is provided as an extension to CCCB $Req: artf1295 $
 */

#include <comint.h>

/* $Req: artf1256 $ */
StatusType SendZeroMessage(MessageIdentifier Message)
{
	StatusType rc;
	uint16 cnt;
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_ZERO_MESSAGE);
	COM_TRACE_HANDLE(Message);
	COM_TRACE_OFF();
	
	COM_API_ENTER();

#ifdef COM_EXTENDED_STATUS
	if (!COM_STARTED()) {
		rc = E_COM_SYS_STATE;		/* COM not yet started */
	}
	else if(!IS_SEND_MESSAGE(Message)) {
		rc = E_COM_ID;				/* not a valid sending Message */
	}
	else {
#endif
		com_senderh sender = Message->cb.sender;
#ifdef COM_EXTENDED_STATUS
		if(sender->message_type == COM_ZERO_LENGTH) {
			rc = E_COM_ID;			/* sender not a zero length message */
		}
		else {
#endif
			cnt = sender->num_receivers;
			com_receiverh receiver = sender->first_receiver;
			
			rc = E_OK;

			/* Potentially could have zero receivers */	
			/* Either 1:0, 1:1 or 1:n for receivers.
			 */
			 	
			/* Walk down the block of receivers, invoking each driver to deal with each */
			while (cnt > 0) {
	
				assert(receiver->driver->send_zero);
				
				StatusType drv_rc = receiver->driver->send_zero(receiver);
				
				if ( drv_rc != E_OK ) {
					rc = drv_rc;
				}
	
				receiver++;
				
				cnt--;
			}
#ifdef COM_EXTENDED_STATUS
		}
	}
#endif

	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		/* $Req: artf1265 $ */
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_1(SendZeroMessage, rc, Message);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	COM_API_LEAVE();	/* Leave API and shutdown COM layer if shutdown pending */
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_ZERO_MESSAGE_FINISH);
	COM_TRACE_OFF();
	
	return rc;
}

