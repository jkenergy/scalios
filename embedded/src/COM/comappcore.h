/* Copyright (C) 2004, 2005, 2006 JK Energy Ltd.
 * 
 * $LastChangedDate: 2007-08-18 00:07:58 +0100 (Sat, 18 Aug 2007) $
 * $LastChangedRevision: 464 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/embedded/src/COM/comappcore.h $
 * 
 * Target CPU: 		Generic
 * Target compiler:	Standard ANSI C
 * Visibility:		User
 */
 
#ifndef COMAPPCORE_H_
#define COMAPPCORE_H_

/* @TODO artf1309 need to tidy namespace for COM: it's a right mess of pollution */

/* Data structures */

/* Messages can be:
 * 
 * Zero length
 * Fixed length (e.g. integers, structures)
 * 
 * Reception side to a message can be:
 * 
 * Unqueued (overwritten with latest)
 * Queued (latest discarded on a full queue)
 * 
 * A message is either a sender or a receiver.
 * 
 * 1:n arrangement for messages: one message drops into several receivers (potentially) $Req: artf1239 $
 * 
 * On reception, a notification is triggered (setting a flag, activating a task, setting an event, calling a callback)
 * 
 */

typedef const struct com_sender_handlecb * com_senderh;
typedef const struct com_receiver_handlecb * com_receiverh;


enum com_mtype { COM_QUEUED_OR_UNQUEUED, COM_ZERO_LENGTH, COM_STREAM };


/* ROM-based configuration defined at build time $Req: artf1238 $ */

/* This structure is required since MessageIdentifier is used to represent both sender and receiver Messages */
struct com_messagecb {
	union {
		com_receiverh receiver;
		com_senderh sender;
	} cb;
};


struct com_notifycb;                                                    /* Anonymous struct; can only assign pointers. This is cast by notification processing functions to appropriate type (see structs below) */
 
 
/* Structs to store the data required for possible actions upon receipt notification; Ptrs to these are stored and passed as com_notifycb */

struct com_notify_eventcb {
      EventMaskType event;                                 				/* Mask of event set during notification */ 
      TaskType task;                                              		/* Task to which event is sent */
};

struct com_notify_taskcb {
      TaskType task;                                                    /* Task activated for notification */
};

struct com_notify_callbackcb {
      os_callbackf callback;                                      		/* Callback function to be called during notification */
};


/* Action to be performed during notification */
enum com_notify_action {COM_NOTIFY_NONE = 0, COM_NOTIFY_ACTIVATE_TASK, COM_NOTIFY_SET_EVENT, COM_NOTIFY_CALLBACK};

/* generic notification helper function. */
StatusType com_notify(const struct com_notifycb *, enum com_notify_action);


/* Messages are 1:n, with a single sender and multiple receivers. In effect this is a publisher/subscriber model */
/* Queue size is a number of slots if the message is fixed size, and number of bytes if dynamic length */

struct com_receivercb;								/* Anonymous struct to represent all receivers (local IPC or hardware/remote) */

/* Handle for a receiver message; stores common information for all receive messages, along with ptr to driver specific receiver cb */
struct com_receiver_handlecb {
	const struct com_receivercb *drv_receiver;		/* Device-specific receiver message; this is cast to appropriate concrete type by driver functions */
	const struct com_drivercb *driver;				/* Driver to handle the receiver message */
};


/* Handle for a sender message */
struct com_sender_handlecb {
	enum com_mtype message_type;					/* type of the sending message */
	uint16 num_receivers;							/* Number of receivers in the block (> 1 means fanout, 1 means 1:1) */	
	com_receiverh first_receiver;					/* Block of receiver messages (local or hardware/remote) $Req: artf1239 $ */
};

/*
 * Structure that specifies driver functions for each COM Driver.
 * This is a concrete implementation of the os_drivercb anonymous struct
 */ 
struct com_drivercb {
	StatusType (*send)(com_receiverh, ApplicationDataRef);						/* Send data. Specific receiver message details to operate on; null if not a queued or unqueued type message  */
	StatusType (*send_zero)(com_receiverh);										/* Send zero length data. Specific receiver message details to operate on; null if not a zero length type message  */
	StatusType (*send_stream)(com_receiverh, ApplicationDataRef, LengthRef);	/* Send a number of bytes; null if not a stream type message */
	StatusType (*recv_stream)(com_receiverh, ApplicationDataRef, LengthRef);	/* Receive a number of bytes; points to com_driver_default_recv_stream() if not stream type message  */
	StatusType (*recv)(com_receiverh, ApplicationDataRef);						/* Receive message. Copy message into the application data space; points to com_driver_default_recv() if not a queued or unqueued type message  */
	StatusType (*init_receiver)(com_receiverh, ApplicationDataRef);				/* Initialise the receiver (in InitMessage()); points com_driver_default_init_receiver() if is a zero length message */
	StatusType (*reinit_receiver)(com_receiverh);								/* Reinitialise the receiver (in StartCOM())*/
	StatusType (*receiver_status)(com_receiverh);								/* Return status of specified receiver; points to com_driver_default_status() if not a queued type message */
};

/**
 * Structure that ties a device to its driver start/stop function. Required for starting/stopping of each COM device.
 * Instances of this are only created for devices that are bound to a driver that supports these functions.
 * NOTE: if they were any more than two functions then it would make sense to split the functions out into
 * a com_driver_initcb, since several devices can share the same driver. For now however it is faster to do it this way.
 */
struct com_device_initcb {
	DeviceId device;
	StatusType (*start_device)(DeviceId);										/* Startup the device */
	StatusType (*stop_device)(DeviceId);										/* Stop the device */
};


/* Flag to indicate if the error hook is callable. It is not callable either when there is a call
 * taking place (i.e. not reentrant) or if the off-line configuration disables calls to the error hook
 * (by initializing this flag to zero). It will be initialized to zero automatically by the C runtime
 * startup code (if called) therefore it is declared this file to be seen by the generated code that
 * will instantiate the value.
 * 
 * @TODO check to see if this is done the same way in the OS.
 */
extern unat com_COMErrorHook_callable;							/* Stored in RAM */
extern const unat com_call_StartCOMExtension;					/* Stored in ROM; instantiated by configuration tool */
extern COMApplicationModeType const com_mode_count;				/* Stored in ROM; count of com startup modes, instantiated by configuration tool */

extern const struct com_messagecb const com_rcv_msgs[];			/* Stored in ROM; receiving message control blocks */
extern const struct com_messagecb * const com_last_rcv_msg;		/* Stored in ROM; ptr to last receiving message in com_rcv_msgs */
extern const uint16 com_num_rcv_msgs;							/* Stored in ROM; count of receiving message control blocks */

extern const struct com_messagecb const com_send_msgs[];		/* Stored in ROM; sending message control blocks */
extern const struct com_messagecb * const com_last_send_msg;	/* Stored in ROM; ptr to last sending message in com_send_msgs */

extern const struct com_device_initcb const com_init_devices[];	/* Stored in ROM; array of pointers to COM devices that require starting/stopping within the COM layer */
extern const uint16 com_num_init_devices;						/* Stored in ROM; number of COM devices that require starting/stopping within the COM layer */

#endif /*COMAPPCORE_H_*/
