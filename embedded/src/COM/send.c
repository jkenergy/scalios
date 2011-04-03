/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-21 08:30:36 +0100 (Tue, 21 Aug 2007) $
 * $LastChangedRevision: 466 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/send.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

/* @TODO artf1310 need to define target-specific assembler for flag test, set, clear to ensure atomicity and avoid need to lock interrupts */

//#define COM_API_ENTER()		{if (os_tst_inc()) return E_COM_SYS_STATE; }

StatusType SendMessage(MessageIdentifier Message, ApplicationDataRef DataRef)
{
	StatusType rc;

	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_REGULAR_MESSAGE);
	COM_TRACE_HANDLE(Message);
	COM_TRACE_DATA_REF(DataRef);
	COM_TRACE_OFF();
	
	COM_API_ENTER();

#ifdef COM_EXTENDED_STATUS
	if(!COM_STARTED()) {
		rc = E_COM_SYS_STATE;							/* COM not yet started */
	}	
	else if(!IS_SEND_MESSAGE(Message)) {
		rc = E_COM_ID;									/* Not a valid sending Message $Req: artf1285 $ */
	}
	else if(Message->cb.sender->message_type != COM_QUEUED_OR_UNQUEUED) {
			rc = E_COM_ID;								/* Sender is not a queued or unqueued message $Req: artf1285 $ */
	}
	else {
#endif
		com_senderh sender = Message->cb.sender;
		uint16 cnt = sender->num_receivers;			
		com_receiverh receiver = sender->first_receiver;
	
		rc = E_OK;

		/* Either 1:0, 1:1 or 1:n for receivers.
		 * 
		 * Local receivers can be queued or unqueued. Hardware/remote receivers are
		 * target-specific.
		 */
		 	
		/* Walk down the block of receivers, invoking each driver to deal with each */
		while (cnt > 0) {
			assert(receiver->driver->send);
			
			StatusType drv_rc = receiver->driver->send(receiver, DataRef);
			
			if ( drv_rc != E_OK ) {
				rc = drv_rc;
			}

			receiver++;
			cnt--;
		}
#ifdef COM_EXTENDED_STATUS		
	}
#endif

	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_2(SendMessage, rc, Message, DataRef);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	COM_API_LEAVE();	/* Leave API and shutdown COM layer if shutdown pending */
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_REGULAR_MESSAGE_FINISH);
	COM_TRACE_OFF();
	
	return rc;
}
