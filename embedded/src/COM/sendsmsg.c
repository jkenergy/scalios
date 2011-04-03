/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-07-23 19:32:38 +0000 (Mon, 23 Jul 2007) $
 * $LastChangedRevision: 444 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.2.249/svn/repos/ertcs/rtos/trunk/embedded/src/COM/send.c $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		Internal
 */

#include <comint.h>

/* @TODO artf1310 need to define target-specific assembler for flag test, set, clear to ensure atomicity and avoid need to lock interrupts */

//#define COM_API_ENTER()		{if (os_tst_inc()) return E_COM_SYS_STATE; }

StatusType SendStreamMessage(MessageIdentifier Message, ApplicationDataRef DataRef, LengthRef DataSize)
{
	StatusType rc;

	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_STREAM_MESSAGE);
	COM_TRACE_HANDLE(Message);
	COM_TRACE_DATA_REF(DataRef);
	COM_TRACE_LENGTH(DataSize);
	COM_TRACE_OFF();
	
	COM_API_ENTER();

#ifdef COM_EXTENDED_STATUS
	if(!COM_STARTED()) {
		rc = E_COM_SYS_STATE;							/* COM not yet started */
	}	
	else if(!IS_SEND_MESSAGE(Message)) {
		rc = E_COM_ID;									/* Not a valid sending Message $Req: artf1285 $ */
	}
	else if(Message->cb.sender->message_type != COM_STREAM) {
			rc = E_COM_ID;								/* Sender is not a stream message */
	}
	else {
#endif
		com_senderh sender = Message->cb.sender;
		uint16 cnt = sender->num_receivers;			
		com_receiverh receiver = sender->first_receiver;
		const COMLengthType req_size = *DataSize; /* Number of bytes eequested to send */
		
		rc = E_OK;

		/* 1:n for receivers. But the returned "number of bytes sent" comes from the
		 * "dominant" stream only: other streams are expected to keep up with the dominant
		 * stream (e.g. they are streams logging the data sent on the dominant stream,
		 * used for debugging).
		 */
		 	
		/* Walk down the block of receivers, invoking each driver to deal with each */
		while (cnt > 0) {

			*DataSize = req_size;	/* Ensure this is (re)set to the requested number
									 * because the following driver call will change it
									 * with the number actually sent */
			
			assert(receiver->driver->send_stream);
			
			StatusType drv_rc = receiver->driver->send_stream(receiver, DataRef, DataSize);
			
			if ( drv_rc != E_OK ) {
				rc = drv_rc;
			}

			receiver++;
			cnt--;
		}
		/* Note that the returned (via DataSize) number of bytes actually sent is
		 * the number sent by the call to send_stream() on the last stream in the
		 * list of attached receiver stream messages. This is the "dominant" stream,
		 * and can be marked up in the configuration process.
		 */
#ifdef COM_EXTENDED_STATUS		
	}
#endif

	if(rc != E_OK && COM_ERROR_HOOK_CALLABLE()) {		
		ENTER_COM_ERROR_HOOK();
		
		COM_ERRORHOOK_3(SendStreamMessage, rc, Message, DataRef, DataSize);
		
		LEAVE_COM_ERROR_HOOK();
	}
	
	COM_API_LEAVE();	/* Leave API and shutdown COM layer if shutdown pending */
	
	COM_TRACE_ON();
	COM_TRACE_CODE(COM_TRACE_SEND_STREAM_MESSAGE_FINISH);
	COM_TRACE_OFF();
	
	return rc;
}
