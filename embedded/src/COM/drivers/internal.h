/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/drivers/internal.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */

/* Standard internal messaging COM drivers. Included by library code, generated code, application
 * code, internal code.
 */
 
#ifndef INTERNAL_H_
#define INTERNAL_H_

/* Driver API function prototypes */
StatusType com_driver_internal_init_receiver_queued(com_receiverh, ApplicationDataRef);
StatusType com_driver_internal_init_receiver_unqueued(com_receiverh, ApplicationDataRef);
/* zero length receivers use com_driver_default_init_receiver() */
StatusType com_driver_internal_init_receiver_stream(com_receiverh, ApplicationDataRef);

StatusType com_driver_internal_reinit_receiver_queued(com_receiverh);
StatusType com_driver_internal_reinit_receiver_unqueued(com_receiverh);
StatusType com_driver_internal_reinit_receiver_zero(com_receiverh);
StatusType com_driver_internal_reinit_receiver_stream(com_receiverh);

StatusType com_driver_internal_recv_queued(com_receiverh, ApplicationDataRef);
StatusType com_driver_internal_recv_unqueued(com_receiverh, ApplicationDataRef);
/* zero length receivers use com_driver_default_recv() */
/* stream receivers use com_driver_default_recv() */

/* queued receivers use com_driver_default_recv_stream() */
/* unqueued receivers use com_driver_default_recv_stream() */
/* zero length receivers use com_driver_default_recv_stream() */
StatusType com_driver_internal_recv_stream(com_receiverh, ApplicationDataRef, LengthRef);

StatusType com_driver_internal_send_queued(com_receiverh, ApplicationDataRef);
StatusType com_driver_internal_send_unqueued(com_receiverh, ApplicationDataRef);
StatusType com_driver_internal_send_zero(com_receiverh);
StatusType com_driver_internal_send_stream(com_receiverh, ApplicationDataRef, LengthRef);

StatusType com_driver_internal_status_queued(com_receiverh);
StatusType com_driver_internal_status_unqueued(com_receiverh);
/* zero length receivers use com_driver_default_status() */
/* stream receivers use com_driver_default_status() */



/* @todo: may decide to move notification members to com_receiver_handlecb since always present in receivercb's
 * However, some network based drivers cannot notify the receiver since it will actually be running
 * on a remote ECU and therefore the receiving device driver at the other end must handle notification. 
 * */ 

struct com_devicecb_ipc { };						/* empty struct to allow creation of a psuedo IPC device instance */


/* Local IPC message receivers; each is a concrete implementation of the anonymous (abstract) com_receivercb type.
 * 
 * Generally a driver-specific receivercb will indicate which device the receiver is attached to
 * but for local IPC there is only one pseudo-device so this information is not needed
 */
   
/* cb for zero length internal receive messages */
struct com_internal_zero_receivercb {				
	enum com_notify_action notify_action;			/* Details of what type of notification $Req: artf1246 $ */
	const struct com_notifycb *notify_data;			/* Data associated with notification */
	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */		
};

/* cb for unqueued internal receive messages */
struct com_internal_unqueued_receivercb {
	COMLengthType message_size;						/* Equal to sizeof(message-type-specified-in-OIL-file) */			
	uint8 *init;									/* Initial data for message (valid if message is of size <= 64 bits) */
	uint8 *msg_data;								/* Pointer to message data element */
	enum com_notify_action notify_action;			/* Details of what type of notification $Req: artf1246 $ */
	const struct com_notifycb *notify_data;			/* Data associated with notification */
	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */	
	ResourceType guard;								/* Guards access to this message's buffer/queue */
};

/* cb for queued internal receive messages */

/* Dyn part of the queued internal receive messages */
struct com_queuecb_dyn {
	unsigned overflow:1;							/* Flag to indicate a message discarded due to buffer overflow */
	uint8 *head;									/* Pointer to the first byte of the first message in the buffer space */
	uint8 *tail;									/* Pointer to the first byte of the last free slot in the buffer space; valid only if there is a free slot */
	uint16 num_messages;							/* Number of messages in the queue */
};

struct com_internal_queued_receivercb {
	COMLengthType message_size;						/* Equal to sizeof(message-type-specified-in-OIL-file) */					
	uint8 *first;									/* Pointer to the first byte of the first element in queue */
	uint8 *last;									/* Pointer to the first byte of the last element in the buffer space; element of size "message_size" */
	uint16 num_slots;								/* Total number of slots in the buffer space */
	struct com_queuecb_dyn *dyn;					/* Dynamic part of message queue control block */
	enum com_notify_action notify_action;			/* Details of what type of notification $Req: artf1246 $ */
	const struct com_notifycb *notify_data;			/* Data associated with notification */
	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */	
	ResourceType guard;								/* Guards access to this message's buffer/queue */
};


/* cb for stream internal receive messages */

/* Dyn part of the internal stream receive messages */
struct com_stream_queuecb_dyn {
	COMLengthType used;								/* Number of bytes in the buffer */
	uint8 *head;									/* Head of buffer (first byte of data; data read from here) */
	uint8 *tail;									/* Tail of buffer (first free byte; new data added here) */
};

struct com_internal_stream_receivercb {
	uint8 *first;									/* Pointer to the first byte of the allocted buffer */
	uint8 *last;									/* Pointer to the last byte of the allocated buffer + 1 (last - first = size of buffer) */
	COMLengthType size;								/* Size of the allocated buffer (@TODO equal to last - first) */
	struct com_stream_queuecb_dyn *dyn;				/* Dynamic part of ring buffer management */
	enum com_notify_action low_notify_action;		/* Notification when size in buffer crosses high threshold */
	enum com_notify_action high_notify_action;		/* Notification when size in buffer drops below low threshold */
	const struct com_notifycb *low_notify_data;		/* Data associated with low threshold notification */
	const struct com_notifycb *high_notify_data;	/* Data associated with high threshold notification */
	FlagValue *low_flag;							/* Flag to set when receiving */
	FlagValue *high_flag;							/* Flag to set when sending */
	COMLengthType low_threshold;					/* When amount of data in buffer drops below low_threshold in a 'receive' call, process notification */
	COMLengthType high_threshold;					/* When amount of data in buffer goes above high_threshold in a 'send' call, process notification */
	ResourceType guard;								/* Guards access to this message's stream buffer */
};

#endif /*INTERNAL_H_*/
