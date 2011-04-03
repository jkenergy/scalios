package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Message.java $
 * 
 */

import java.util.*;
import java.math.BigInteger;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models a Message within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Message extends OSModelElement {

	
	// Prefixes used to generate Macros names for notification flags
	// Although this may seem rather target language specific, this requirement is part of the OSEK COM
	// specification, thus all target languages C, C++, Ada etc. would all need to use these macro prefixes.
	private final static String ReadFlag="ReadFlag_";
	private final static String ResetFlag="ResetFlag_";	
	
	/**
	 * The type of the Message
	 * @see MessageKind
	 */
	private MessageKind messageProperty=MessageKind.SEND_STATIC_INTERNAL_LITERAL;
	
	/**
	 * The data type of the message data expressed as a C language type (e.g. int or a structure name)
	 * Only applicable when messageProperty==SEND_STATIC_INTERNAL
	 */
	private String cDataType=null;
	
	/**
	 * Identifies the sending Message of a receiving message
	 * Only applicable when messageProperty==RECEIVE_UNQUEUED_INTERNAL | messageProperty==RECEIVE_QUEUED_INTERNAL | messageProperty==RECEIVE_ZERO_INTERNAL
	 */
	private Message sendingMessage=null;

	/**
	 * The list of receiving messages (inverse of sendingMessage)
	 * Only applicable when messageProperty==SEND_STATIC_INTERNAL | SEND_ZERO_INTERNAL
	 */
	private Set<Message> receivingMessages = new LinkedHashSet<Message>();	
	
	/**
	 * Specifies the initial value of the message 
	 * Only applicable when messageProperty==RECEIVE_UNQUEUED_INTERNAL
	 */
	private BigInteger initialValue=BigInteger.ZERO;
	
	/**
	 * Specifies the maximum number of messages that the queue can store (for a queued message only). 
	 * When messageProperty==RECEIVE_QUEUED_INTERNAL, the value 0 is not allowed for this attribute.
	 * 
	 * Only applicable when messageProperty==RECEIVE_QUEUED_INTERNAL
	 */
	private long queueSize=0;
	
	/**
	 * Specifies the maximum buffer size that the stream can store (for a stream message only). 
	 * When messageProperty==RECEIVE_STREAM_INTERNAL, the value 0 is not allowed for this attribute.
	 * 
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private long bufferSize=0;	
	
	/**
	 * Specifies the high threshold notification level for a stream message;
	 * 
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private long highThreshold=0;
	 
	/**
	 * Specifies the low threshold notification level for a stream message;
	 * 
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private long lowThreshold=0;	
	
	
	/**
	 * The type of the notification given. Depending on the messageProperty this is either a send or a receive notification.
	 * If messageProperty==RECEIVE_STREAM_INTERNAL then this is used for the high threshold notifiction
	 * @see NotificationKind
	 */
	private NotificationKind notification=NotificationKind.NONE_LITERAL;	
	
	/**
	 * The type of the notification given when low threshold is hit for Stream type messages.
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 * @see NotificationKind
	 */
	private NotificationKind lowNotification=NotificationKind.NONE_LITERAL;	
	
	/**
	 * The callback routine that is called by this message (if any) to perform a notification.
	 * Only applicable when notificationProperty==COMCALLBACK.
	 * If messageProperty==RECEIVE_STREAM_INTERNAL then this is used for the high threshold notifiction
	 */
	private String notificationCallbackRoutineName=null;
	
	/**
	 * The callback routine that is called by this message (if any) to perform a low threshold notification.
	 * Only applicable when lowNotificationProperty==COMCALLBACK.
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private String lowNotificationCallbackRoutineName=null;	
	
	/**
	 * The list of messages that are sent and/or received by the callback routines.
	 * Only applicable when notificationProperty==COMCALLBACK 
	 */
	private Set<Message> highCallbackMessages = new LinkedHashSet<Message>();
	
	/**
	 * The list of messages that are sent and/or received by the callback routines.
	 * Only applicable when lowNotificationProperty==COMCALLBACK
	 */
	private Set<Message> lowCallbackMessages = new LinkedHashSet<Message>();	

	/**
	 * The list of messages that send and/or receive this message from their callback routines (inverse of callbackMessages).
	 * Only applicable when notificationProperty==COMCALLBACK or lowNotificationProperty==COMCALLBACK
	 */
	private Set<Message> messageUsers = new LinkedHashSet<Message>();	
	
	/**
	 * The name of the flag that is set by this message (if any) to perform a notification.
	 * Only applicable when notificationProperty==FLAG.
	 * If messageProperty==RECEIVE_STREAM_INTERNAL then this is used for the high threshold notifiction
	 * 
	 * @see Cpu#getAllFlagMacroNames()
	 */	
	private String notificationFlagName=null;
	
	/**
	 * The name of the flag that is set by this message (if any) to perform a low threshold notification.
	 * Only applicable when lowNotificationProperty==FLAG.
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 * 
	 * @see Cpu#getAllFlagMacroNames()
	 */	
	private String lowNotificationFlagName=null;	
	
	/**
	 * The Event that is set by this message (if any) to perform a notification.
	 * Only applicable when notificationProperty==SETEVENT.
	 * If messageProperty==RECEIVE_STREAM_INTERNAL then this is used for the high threshold notifiction
	 */
	private Event notificationEvent=null;
	
	/**
	 * The Event that is set by this message (if any) to perform a low threshold notification.
	 * Only applicable when lowNotificationProperty==SETEVENT.
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private Event lowNotificationEvent=null;	
	
	/**
	 * The Task that is activated by this message (if any) to perform a notification.
	 * Only applicable when notificationProperty==ACTIVATETASK | notificationProperty==SETEVENT.
	 * If messageProperty==RECEIVE_STREAM_INTERNAL then this is used for the high threshold notifiction
	 */
	private Task notificationTask=null;	
	
	/**
	 * The Task that is activated by this message (if any) to perform a low threshold notification.
	 * Only applicable when lowNotificationProperty==ACTIVATETASK | lowNotificationProperty==SETEVENT.
	 * Only applicable when messageProperty==RECEIVE_STREAM_INTERNAL
	 */
	private Task lowNotificationTask=null;		
	
	/**
	 * Set of Runnables (i.e. Tasks/ISRs) that access this Message.
	 */
	private Set<Runnable> runnables = new LinkedHashSet<Runnable>();	
	
	/**
	 * The name of the device associated with the Message // TODO  remove this, no need for device name in OS model. 
	 */
	private String deviceName;
	
	/**
	 * The options of the device associated with the Message
	 * This is a comma separated list of name=value pairs
	 * e.g. "endian = little, bitrate = 9200"
	 */
	private String deviceOptions;	

	/**
	 * Sets the type of the Message (the message property).
	 * @param newMessageProperty the new message type
	 * @see MessageKind
	 */
	public void setMessageProperty(MessageKind newMessageProperty) {
		
		messageProperty=newMessageProperty;
	}
	
	/**
	 * Gets the type of the Message (the message property).
	 * @return the type of the Message
	 * @see MessageKind
	 */
	public MessageKind getMessageProperty() {
		return messageProperty;
	}
	
	
	/**
	 * Sets the C data type name of the message data.
	 * @param newCDataType the name of the C datatype
	 */
	public void setCDataType(String newCDataType) {
		
		cDataType=newCDataType;
	}
	
	/**
	 * Gets the C data type name of the message data.
	 * @return the name of the C datatype
	 */
	public String getCDataType() {
		return cDataType;
	}

	
	/**
	 * Sets the sending message of this message.
	 * This method creates a two way relationship. 
	 * @param newMessage the new sending message
	 */
	public void setSendingMessage(Message newMessage) {
		
		if ( newMessage!=null ) {
			
			if ( sendingMessage!=newMessage ) {
				
				sendingMessage=newMessage;
				
				sendingMessage.addReceivingMessage(this);
			}
		}
	}

	/**
	 * Returns the sending message of this message.
	 * @return Message that is the sending message (may be null)
	 */
	public Message getSendingMessage() {
		return sendingMessage;
	}	
	
	/**
	 * Adds the message to the list of receiving messages .
	 * This method creates a two way relationship. 
	 * @param newMessage the Message to be added
	 */
	public void addReceivingMessage(Message newMessage) {
		
		if ( newMessage!=null ) {

			if ( receivingMessages.add(newMessage) ) {
				newMessage.setSendingMessage(this);
			}
		}
	}

	/**
	 * Returns the collection of receiving messages.
	 * @return Collection receiving Messages
	 */
	public Collection<Message> getReceivingMessages() {
		return receivingMessages;
	}	
	
	/**
	 * Sets the initial value of the Message.
	 * @param newInitialValue the new initial value of the message
	 * 
	 */
	public void setInitialValue(BigInteger newInitialValue) {
		
			
		initialValue=newInitialValue;
	}
	
	/**
	 * @return the initial value of the Message.
	 */
	public BigInteger getInitialValue() {
		return initialValue;
	}
	
	/**
	 * Sets the maximum queue size Message (must be >0).
	 * @param newQueueSize the new queue size of the message
	 */	
	public void setQueueSize(long newQueueSize) {
		
		queueSize=newQueueSize;		
	}
	
	/**
	 * Returns the maximum queue size of the message queue.
	 * @return the maximum queue size
	 */
	public long getQueueSize() {
		return queueSize;
	}
	
	/**
	 * Sets the maximum buffer size for a stream message (must be >0).
	 * @param newBufferSize the new buffer size of the message
	 * 
	 */	
	public void setBufferSize(long newBufferSize) {

		bufferSize=newBufferSize;		
	}
	
	/**
	 * Returns the maximum buffer size of the stream message buffer.
	 * @return the maximum buffer size
	 */
	public long getBufferSize() {
		return bufferSize;
	}	
	
	/**
	 * Sets the high threshold notification level for a stream message
	 * @param newThreshold the new high threshold
	 * 
	 */	
	public void setHighThreshold(long newThreshold) {
		
		highThreshold=newThreshold;		
	}
	
	/**
	 * Returns the high threshold notification level of the stream message buffer.
	 * @return the high threshold notification level
	 */
	public long getHighThreshold() {
		return highThreshold;
	}	
	
	/**
	 * Sets the low threshold notification level for a stream message
	 * @param newThreshold the new low threshold
	 */	
	public void setLowThreshold(long newThreshold)  {
		
		lowThreshold=newThreshold;		
	}
	
	/**
	 * Returns the low threshold notification level of the stream message buffer.
	 * @return the low threshold notification level
	 */
	public long getLowThreshold() {
		return lowThreshold;
	}	
	
	/**
	 * Adds a runnable to the collection of runnables that access this message.
	 * This method creates a two way relationship.
	 * 
	 * @param runnable the Runnable to be added
	 */
	public void addRunnable(Runnable runnable) {
		
		if ( runnable!=null ) {
			if ( runnables.add(runnable) )
				runnable.addAccessedMessage(this);	// inform event that it activates this task
		}
	}
	
	/**
	 * Gets the collection of all Runnables that access this message.
	 * @return the collection of runnables that access this message.
	 */
	public Collection<Runnable> getRunnables() {
		return runnables;
	}
	
	/**
	 * Sets the type of the Notification.
	 * @param newNotification the new notification type
	 * @see NotificationKind
	 */
	public void setNotification(NotificationKind newNotification) {
		
		notification=newNotification;
	}
	
	/**
	 * Gets the type of Notification.
	 * @return the type of the Notification
	 * @see NotificationKind
	 */
	public NotificationKind getNotification() {
		return notification;
	}	
	
	/**
	 * Sets the type of the low threshold Notification.
	 * @param newNotification the new low threshold notification type
	 * @see NotificationKind
	 */
	public void setLowNotification(NotificationKind newNotification) {
		
		lowNotification=newNotification;
	}
	
	/**
	 * Gets the type of low threshold Notification.
	 * @return the type of the low threshold Notification
	 * @see NotificationKind
	 */
	public NotificationKind getLowNotification() {
		return lowNotification;
	}	
	
	/**
	 * Sets the name of the callback routine used for notifications.
	 * @param newCallbackRoutineName the callback routine name
	 */
	public void setNotificationCallbackRoutineName(String newCallbackRoutineName) {
		
		notificationCallbackRoutineName = newCallbackRoutineName;
	}
	
	/**
	 * Gets the name of the callback routine used for notification.
	 * @return the notification callback routine name
	 */
	public String getNotificationCallbackRoutineName() {
		return notificationCallbackRoutineName;
	}
	
	/**
	 * Sets the name of the callback routine used for low threshold notifications.
	 * @param newCallbackRoutineName the low threshold callback routine name
	 */
	public void setLowNotificationCallbackRoutineName(String newCallbackRoutineName) {
		
		lowNotificationCallbackRoutineName = newCallbackRoutineName;
	}
	
	/**
	 * Gets the name of the callback routine used for low threshold notification.
	 * @return the notification low threshold callback routine name
	 */
	public String getLowNotificationCallbackRoutineName() {
		return lowNotificationCallbackRoutineName;
	}	
	
	
	/**
	 * Sets the name of the flag set for notification.
	 * @param newFlagName the new flag name
	 */
	public void setNotificationFlagName(String newFlagName) {
		
		notificationFlagName = newFlagName;
	}
	
	/**
	 * Gets the name of flag set for notification.
	 * @return the flag name
	 */
	public String getNotificationFlagName() {
		return notificationFlagName;
	}
	
	/**
	 * @return the name of the ReadFlag macro for the notification flag
	 */
	public String getReadFlagMacroName() {
		return ( notificationFlagName != null ) ? ReadFlag+notificationFlagName : null;
	}
	
	/**
	 * @return the name of the ResetFlag macro for the notification flag
	 */
	public String getResetFlagMacroName() {
		return ( notificationFlagName != null ) ? ResetFlag+notificationFlagName : null;
	}	
	
	/**
	 * Sets the name of the flag set for low threshold notification.
	 * @param newFlagName the new low threshold flag name
	 */
	public void setLowNotificationFlagName(String newFlagName) {
		
		lowNotificationFlagName = newFlagName;
	}
	
	/**
	 * Gets the name of flag set for low threshold notification.
	 * @return the low threshold flag name
	 */
	public String getLowNotificationFlagName() {
		return lowNotificationFlagName;
	}	
	
	/**
	 * @return the name of the ReadFlag macro for the low threshold notification flag
	 */
	public String getLowReadFlagMacroName() {
		return ( lowNotificationFlagName != null ) ? ReadFlag+lowNotificationFlagName : null;
	}
	
	/**
	 * @return the name of the ResetFlag macro for low threshold notification flag
	 */
	public String getLowResetFlagMacroName() {
		return ( lowNotificationFlagName != null ) ? ResetFlag+lowNotificationFlagName : null;
	}	
	
	/**
	 * Adds the message to the list of messages that are sent and/or received by the notification callback routines.
	 * This method creates a two way relationship. 
	 * @param newMessage the Message to be added
	 */
	public void addHighCallbackMessage(Message newMessage) {
		
		if ( newMessage!=null ) {

			if ( highCallbackMessages.add(newMessage) ) {
				newMessage.addMessageUser(this);
			}
		}
	}

	/**
	 * Adds the message to the list of messages that are sent and/or received by the notification callback routines.
	 * This method creates a two way relationship. 
	 * @param newMessage the Message to be added
	 */
	public void addLowCallbackMessage(Message newMessage) {
		
		if ( newMessage!=null ) {

			if ( lowCallbackMessages.add(newMessage) ) {
				newMessage.addMessageUser(this);
			}
		}
	}	
	
	/**
	 * Returns the messages that are sent and/or received by both the notification callback routines.
	 * @return Collection of sent and/or received Messages
	 */
	public Collection<Message> getCallbackMessages() {
		
		Collection<Message> callbackMessages = new LinkedHashSet<Message>();
		
		callbackMessages.addAll(highCallbackMessages);
		callbackMessages.addAll(lowCallbackMessages);
		
		return callbackMessages;
	}	
	
	/**
	 * Returns the messages that are sent and/or received by the high notification callback routines.
	 * @return Collection of sent and/or received Messages
	 */
	public Collection<Message> getHighCallbackMessages() {
	
		return highCallbackMessages;
	}	
	
	/**
	 * Returns the messages that are sent and/or received by the low notification callback routines.
	 * @return Collection of sent and/or received Messages
	 */
	public Collection<Message> getLowCallbackMessages() {
	
		return lowCallbackMessages;
	}	
	
	/**
	 * Helper method.
	 * 
	 * @param allMessages Collection to which to add callback messages
	 */	
	private void getAllCallbackMessages2(Collection<Message> allMessages) {

		for ( Message next : highCallbackMessages ) {
			
			if ( allMessages.add(next) ) {	// prevent recursion by checking if already in set
				next.getAllCallbackMessages2(allMessages);
			}
		}
		
		for ( Message next : lowCallbackMessages ) {
			
			if ( allMessages.add(next) ) {	// prevent recursion by checking if already in set
				next.getAllCallbackMessages2(allMessages);
			}
		}		
	}
	
	/**
	 * Returns ALL of the messages that are sent and/or received by the callback routine.
	 * The returned set is the transitive closure of all callback messsages, plus all of their
	 * callback messages, etc.
	 * 
	 * @return the transitive closure of ALL Messages called via the callback handler
	 */
	public Collection<Message> getAllCallbackMessages() {
		
		Set<Message> allMessages = new HashSet<Message>();
		
		getAllCallbackMessages2(allMessages);
		
		return allMessages;
	}
	
	
	/**
	 * Adds the message to the list of messages that send and/or receive this message from their callback routine.
	 * 
	 * @param newMessage the Message to be added
	 */
	public void addMessageUser(Message newMessage) {
		
		if ( newMessage!=null ) {
			messageUsers.add(newMessage);
		}
	}	
	
	/**
	 * Returns the messages that send and/or receive this message from their callback routine.
	 * @return Collection of Messages
	 */
	public Collection<Message> getMessageUsers() {
		return messageUsers;
	}	
	
	/**
	 * Helper method.
	 * 
	 * @param allUsers Collection to which to add message users
	 */	
	private void getAllMessagesUsers2(Collection<Message> allUsers) {

		for ( Message next : messageUsers ) {
			
			if ( allUsers.add(next) ) {	// prevent recursion by checking if already in set
				next.getAllMessagesUsers2(allUsers);
			}
		}
	}
	
	/**
	 * Returns ALL of the messages that use the message (i.e. send and/or receive the message within a callback routine).
	 * The returned set is the transitive closure of all using messsages, plus all of their
	 * using messages, etc.
	 * 
	 * @return the transitive closure of ALL Messages which call the message via their callback handler
	 */
	public Collection<Message> getAllMessageUsers() {
		
		Set<Message> allUsers = new HashSet<Message>();
		
		getAllMessagesUsers2(allUsers);
		
		return allUsers;
	}
		
	
	/**
	 * Sets the Event that this message sets for notification.
	 * This method creates a two way relationship. 
	 * @param newEvent the Event that is to be set by the message during notification
	 */
	public void setNotificationEvent(Event newEvent) {
		
		if ( newEvent!=null ) {
			
			if ( notificationEvent!=newEvent ) {
				
				notificationEvent=newEvent;
				
				notificationEvent.addMessage(this);
			}
		}
	}

	/**
	 * Returns the event that this message sets for notification.
	 * @return the notification Event that is set (may be null)
	 */
	public Event getNotificationEvent() {
		return notificationEvent;
	}		
	

	/**
	 * Sets the Event that this message sets for low threshold notification.
	 * This method creates a two way relationship. 
	 * @param newEvent the Event that is to be set by the message during low threshold notification
	 */
	public void setLowNotificationEvent(Event newEvent) {
		
		if ( newEvent!=null ) {
			
			if ( lowNotificationEvent!=newEvent ) {
				
				lowNotificationEvent=newEvent;
				
				lowNotificationEvent.addLowMessage(this);
			}
		}
	}

	/**
	 * Returns the event that this message sets for low threshold notification.
	 * @return the low threshold notification Event that is set (may be null)
	 */
	public Event getLowNotificationEvent() {
		return lowNotificationEvent;
	}	
		
	
	
	/**
	 * Sets the Task that this message activates for notification.
	 * This method creates a two way relationship. 
	 * @param newTask the Task that is to be activated by the message during notification
	 */
	public void setNotificationTask(Task newTask) {
		
		if ( newTask!=null ) {
			
			if ( notificationTask!=newTask ) {
				
				notificationTask=newTask;
				
				notificationTask.addMessage(this);
			}
		}
	}

	/**
	 * Returns the task that this message activates for notification.
	 * @return Task that is activated (may be null) during notification
	 */
	public Task getNotificationTask() {
		return notificationTask;
	}	
	
	/**
	 * Sets the Task that this message activates for low threshold notification.
	 * This method creates a two way relationship. 
	 * @param newTask the Task that is to be activated by the message during low threshold notification
	 */
	public void setLowNotificationTask(Task newTask) {
		
		if ( newTask!=null ) {
			
			if ( lowNotificationTask!=newTask ) {
				
				lowNotificationTask=newTask;
				
				lowNotificationTask.addLowMessage(this);
			}
		}
	}

	/**
	 * Returns the task that this message activates for low threshold notification.
	 * @return Task that is activated (may be null) during low threshold notification
	 */
	public Task getLowNotificationTask() {
		return lowNotificationTask;
	}	
	
	/**
	 * 
	 * @return true if the message is a sending message, else false
	 */
	public boolean isSendingMessage() {
		return  MessageKind.SEND_STATIC_INTERNAL_LITERAL.equals(messageProperty) || MessageKind.SEND_ZERO_INTERNAL_LITERAL.equals(messageProperty) || MessageKind.SEND_STREAM_INTERNAL_LITERAL.equals(messageProperty);
	}
	
	/**
	 * 
	 * @return true if the message is a receiving message, else false
	 */
	public boolean isReceivingMessage() {
		return  !isSendingMessage();	// if it not a sender then must be a receiver!
	}
	
	/**
	 * 
	 * @return true if the message is a queued or unqueued sending message (not zero length or stream), else false
	 */
	public boolean isQueuedOrUnqueuedSendingMessage() {
		return  MessageKind.SEND_STATIC_INTERNAL_LITERAL.equals(messageProperty);
	}	
	
	/**
	 * 
	 * @return true if the message is a queued or unqueued receiving message, else false
	 */
	public boolean isQueuedOrUnqueuedReceivingMessage() {
		return  MessageKind.RECEIVE_QUEUED_INTERNAL_LITERAL.equals(messageProperty) || MessageKind.RECEIVE_UNQUEUED_INTERNAL_LITERAL.equals(messageProperty);
	}	
	
	/**
	 * 
	 * @return true if the message is a queued (receiving) message, else false
	 */
	public boolean isQueuedMessage() {
		return  MessageKind.RECEIVE_QUEUED_INTERNAL_LITERAL.equals(messageProperty);
	}
	
	/**
	 * 
	 * @return true if the message is an un-queued (receiving) message, else false
	 */
	public boolean isUnqueuedMessage() {
		return  MessageKind.RECEIVE_UNQUEUED_INTERNAL_LITERAL.equals(messageProperty);
	}
	
	
	/**
	 * 
	 * @return true if the message is a zero length message (either sender or receiver), else false
	 */
	public boolean isZeroLengthMessage() {
		return  MessageKind.SEND_ZERO_INTERNAL_LITERAL.equals(messageProperty) || MessageKind.RECEIVE_ZERO_INTERNAL_LITERAL.equals(messageProperty);
	}	
	
	/**
	 * 
	 * @return true if the message is a stream message (either sender or receiver), else false
	 */
	public boolean isStreamMessage() {
		return  MessageKind.SEND_STREAM_INTERNAL_LITERAL.equals(messageProperty) || MessageKind.RECEIVE_STREAM_INTERNAL_LITERAL.equals(messageProperty);
	}	
	
	/**
	 * 
	 * @return true if the message is a stream receiving message, else false
	 */
	public boolean isStreamReceivingMessage() {
		return  MessageKind.RECEIVE_STREAM_INTERNAL_LITERAL.equals(messageProperty);
	}	
	
	/**
	 * 
	 * @return true if this Message activates a Task for receipt notification
	 */
	public boolean activatesTask() {
		return NotificationKind.ACTIVATETASK_LITERAL.equals(notification);
	}
	
	/**
	 * 
	 * @return true if this Message activates a Task for low threshold notification
	 */
	public boolean lowActivatesTask() {
		return NotificationKind.ACTIVATETASK_LITERAL.equals(lowNotification);
	}	
	
	/**
	 * 
	 * @return true if this Message sets an Event for receipt notification
	 */
	public boolean setsEvent() {
		return NotificationKind.SETEVENT_LITERAL.equals(notification);
	}	
	
	/**
	 * 
	 * @return true if this Message sets an Event for low threshold notification
	 */
	public boolean lowSetsEvent() {
		return NotificationKind.SETEVENT_LITERAL.equals(lowNotification);
	}		
	
	/**
	 * 
	 * @return true if this Message calls a callback handler for receipt notification
	 */
	public boolean callsHandler() {
		return NotificationKind.COMCALLBACK_LITERAL.equals(notification);
	}	
	
	/**
	 * 
	 * @return true if this Message calls a callback handler for low threshold notification
	 */
	public boolean lowCallsHandler() {
		return NotificationKind.COMCALLBACK_LITERAL.equals(lowNotification);
	}	
	
	/**
	 * 
	 * @return true if this Message sets a flag for receipt notification
	 */
	public boolean setsFlag() {
		return NotificationKind.FLAG_LITERAL.equals(notification);
	}
	
	/**
	 * 
	 * @return true if this Message sets a flag for low threshold notification
	 */
	public boolean lowSetsFlag() {
		return NotificationKind.FLAG_LITERAL.equals(lowNotification);
	}	
	
	/**
	 * 
	 * @return true if this Message does nothing for receipt notification
	 */
	public boolean doesNothing() {
		return NotificationKind.NONE_LITERAL.equals(notification);
	}
	
	/**
	 * 
	 * @return true if this Message does nothing for low threshold  notification
	 */
	public boolean lowDoesNothing() {
		return NotificationKind.NONE_LITERAL.equals(lowNotification);
	}	
	
	/**
	 * @return the deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName the deviceName to set
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return the deviceOptions
	 */
	public String getDeviceOptions() {
		return deviceOptions;
	}

	/**
	 * @param deviceOptions the deviceOptions to set
	 */
	public void setDeviceOptions(String deviceOptions) {
		this.deviceOptions = deviceOptions;
	}		
	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element	
		
		// [1] If the messageProperty equals SEND_STATIC_INTERNAL then a cDataType value must be specified.	
		if ( isQueuedOrUnqueuedSendingMessage() && cDataType == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a sender that must specify a datatype name"));
		}
		
		// [2] If the messageProperty does not equal SEND_STATIC_INTERNAL then a cDataType value should not be specified. 
		if ( isQueuedOrUnqueuedSendingMessage() == false && cDataType != null ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' has a datatype name specified, but does not require one"));
		}		

		// [3] If the message is a sending message then at least one receivingMessages should be specified.
		if ( isSendingMessage() && receivingMessages.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a sender with no local receivers specified"));
		}
		
		// [4] If the message is a receiving message then one sendingMessage should be specified. 
		// then one sendingMessage should be specified. [note: may make warning unless “device” specified]
		if ( isReceivingMessage() && sendingMessage == null ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a receiver with no local sender specified"));
		}		
		
		// [5] If the messageProperty does not equal RECEIVE_UNQUEUED_INTERNAL then no initialValue should be specified. 
		if ( isUnqueuedMessage() == false && initialValue.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not an unqueued receiver, but has an initial value specified"));
		}
		
		// [6] If the messageProperty equals RECEIVE_QUEUED_INTERNAL then a queueSize value of more than 0 must be specified.
		if ( isQueuedMessage() && queueSize <= 0 ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a queued receiver with an invalid queue size (less than 1)"));
		}
		
		// [7] If the messageProperty does not equal RECEIVE_QUEUED_INTERNAL then a queueSize value of 0 must be specified.		
		if ( isQueuedMessage() == false && queueSize != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not a queued receiver, but has a queue size specified"));
		}
		
		// [8] If the message is a sending message then the notificationProperty and the lowNotificationProperty values should equal NONE.
		if ( isSendingMessage() && (doesNothing() == false || lowDoesNothing() == false)) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a sender, but has a notification action specified"));
		}
		
		// [9] If the notificationProperty value equals NONE then no notificationTask, notificationEvent, notificationFlagName value, notificationCallbackRoutineName value or callbackMessages should be specified.
		if ( doesNothing() && (notificationTask != null || notificationEvent != null || notificationFlagName != null || notificationCallbackRoutineName != null || highCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies notification information not relevant for notification"));
		}		

		// [9a] If the lowNotificationProperty value equals NONE then no lowNotificationTask, lowNotificationEvent, lowNotificationFlagName value, lowNotificationCallbackRoutineName value or callbackMessages should be specified.
		if ( lowDoesNothing() && (lowNotificationTask != null || lowNotificationEvent != null || lowNotificationFlagName != null || lowNotificationCallbackRoutineName != null || lowCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies low threshold notification information not relevant for low threshold notification"));
		}		
		
		// [10] If the notificationProperty value equals ACTIVATETASK then the notificationTask that the message activates must be identified.
		if ( activatesTask() && notificationTask == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the task to be activated during notification"));
		}
		
		// [10a] If the lowNotificationProperty value equals ACTIVATETASK then the lowNotificationTask that the message activates must be identified.
		if ( lowActivatesTask() && lowNotificationTask == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the task to be activated during low threshold notification"));
		}		
		
		// [11] If the notificationProperty value equals ACTIVATETASK then no set notificationEvent, notificationFlagName value, notificationCallbackRoutineName value or callbackMessages should be specified.
		if ( activatesTask() && (notificationEvent!=null || notificationFlagName!=null || notificationCallbackRoutineName!=null || highCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies notification information not relevant for task activation"));
		}
		
		// [11a] If the lowNotificationProperty value equals ACTIVATETASK then no set lowNotificationEvent, lowNotificationFlagName value, lowNotificationCallbackRoutineName value or callbackMessages should be specified. [information]
		if ( lowActivatesTask() && (lowNotificationEvent!=null || lowNotificationFlagName!=null || lowNotificationCallbackRoutineName!=null || lowCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies low threshold notification information not relevant for task activation"));
		}		
		
		// [12] If the notificationProperty value equals SETEVENT then the notificationEvent that the message sets and the specific notificationTask to be activated by the notificationEvent must be identified.
		if ( setsEvent() && (notificationTask == null || notificationEvent == null)) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the event to be set and the associated activated task during notification"));
		}
		
		// [12a] If the lowNotificationProperty value equals SETEVENT then the lowNotificationEvent that the message sets and the specific lowNotificationTask to be activated by the lowNotificationEvent must be identified. [error]
		if ( lowSetsEvent() && (lowNotificationTask == null || lowNotificationEvent == null)) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the event to be set and the associated activated task during low threshold notification"));
		}		
		
		// [13] If the notificationProperty value equals SETEVENT then no notificationFlagName value, notificationCallbackRoutineName value or callbackMessages should be specified.
		if ( setsEvent() && (notificationFlagName!=null || notificationCallbackRoutineName!=null || highCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies notification information not relevant for event setting"));
		}	
		
		// [13a] If the lowNotificationProperty value equals SETEVENT then no lowNotificationFlagName value, lowNotificationCallbackRoutineName value or callbackMessages should be specified. [information]
		if ( lowSetsEvent() && (lowNotificationFlagName!=null || lowNotificationCallbackRoutineName!=null || lowCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies low threshold notification information not relevant for event setting"));
		}		
		
		// [14] If the notificationProperty value equals COMCALLBACK then the notificationCallbackRoutineName value must be given.
		if ( callsHandler() && notificationCallbackRoutineName == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the name of the handler to be called during notification"));
		}
		
		// [14a] If the lowNotificationProperty value equals COMCALLBACK then the lowNotificationCallbackRoutineName value must be given. [error]
		if ( lowCallsHandler() && lowNotificationCallbackRoutineName == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the name of the handler to be called during low threshold notification"));
		}
		
		// [15] If the notificationProperty value equals COMCALLBACK then no notificationTask task, set notificationEvent or notificationFlagName value should be specified.
		if ( callsHandler() && (notificationTask!=null || notificationEvent!=null || notificationFlagName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies notification information not relevant for callback handler calling"));
		}
		
		// [15a] If the lowNotificationProperty value equals COMCALLBACK then no activated lowNotificationTask, set lowNotificationEvent or lowNotificationFlagName value should be specified. [information]
		if ( lowCallsHandler() && (lowNotificationTask!=null || lowNotificationEvent!=null || lowNotificationFlagName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies low threshold notification information not relevant for callback handler calling"));
		}
		
		// [16] If the notificationProperty value equals FLAG then the notificationFlagName value must be identified.
		if ( setsFlag() && notificationFlagName == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the name of the flag to be set during notification"));
		}
		
		// [16a] If the lowNotificationProperty value equals FLAG then the lowNotificationFlagName value must be identified. [error]
		if ( lowSetsFlag() && lowNotificationFlagName == null ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' does not specify the name of the flag to be set during low threshold notification"));
		}		
		
		// [17] If the notificationProperty value equals FLAG then no activated notificationTask, set notificationEvent, notificationCallbackRoutineName value or callbackMessages should be specified.		
		if ( setsFlag() && (notificationTask!=null || notificationEvent!=null || notificationCallbackRoutineName!=null || highCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies notification information not relevant for setting a flag"));
		}
		
		// [17a] If the lowNotificationProperty value equals FLAG then no activated lowNotificationTask, set lowNotificationEvent, lowNotificationCallbackRoutineName value or callbackMessages should be specified. [information]
		if ( lowSetsFlag() && (lowNotificationTask!=null || lowNotificationEvent!=null || lowNotificationCallbackRoutineName!=null || lowCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' specifies low threshold notification information not relevant for setting a flag"));
		}

		
		// [18] If the notificationProperty value equals SETEVENT any identified notificationTask must be one of the tasks to which the identified notificationEvent reacts.
		if ( setsEvent() && notificationTask != null && notificationEvent != null ) {
			
			if ( notificationEvent.getTasks().contains(notificationTask) == false ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' specifies notification task '"+notificationTask.getName()+"', which does not react to event '"+notificationEvent.getName()+"'"));
			}
		}
		
		// [18a] If the lowNotificationProperty value equals SETEVENT any identified lowNotificationTask must be one of the tasks to which the identified lowNotificationEvent reacts. [error]
		if ( lowSetsEvent() && lowNotificationTask != null && lowNotificationEvent != null ) {
			
			if ( lowNotificationEvent.getTasks().contains(lowNotificationTask) == false ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' specifies a low threshold notification task '"+lowNotificationTask.getName()+"', which does not react to event '"+lowNotificationEvent.getName()+"'"));
			}
		}		
		
		// [19] Any callbackMessages that are identified should not directly or indirectly refer to the message itself.
		if ( getAllCallbackMessages().contains(this) ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' either directly or indirectly identifies itself as a message called from the callback handler"));
		}
		
		// [20] If the Message is a sending message then no callbackMessages may be specified.
		if ( isSendingMessage() && (highCallbackMessages.size() > 0 || lowCallbackMessages.size() > 0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a sender, but identifies messages accessed from a callback handler"));
		}
		
		// [21] If the Message is a sending message then no sendingMessage may be specified.
		if ( isSendingMessage() && sendingMessage != null ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a sender, but identifies a sending message"));
		}
		
		// [22] If the Message is a receiver message then no receivingkMessages may be specified.
		if ( isReceivingMessage() && receivingMessages.size() > 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a receiver, but identifies receiving messages"));
		}
		
		// [23] If specified, the initialValue value must be greater than, or equal to, zero.
		if ( initialValue.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' has an invalid (negative) initial value"));
		}	
		
		// [24] If specified, the notificationFlagName value must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols.
		if ( notificationFlagName != null ) {
			validateIdentifierName(problems, notificationFlagName);
		}
		
		// [24a] If specified, the lowNotificationFlagName value must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols.
		if ( lowNotificationFlagName != null ) {
			validateIdentifierName(problems, lowNotificationFlagName);
		}		
		
		// [25] If specified, the notificationCallbackRoutineName value must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols.
		if ( notificationCallbackRoutineName != null ) {
			validateIdentifierName(problems, notificationCallbackRoutineName);
		}
		
		// [25a] If specified, the lowNotificationCallbackRoutineName value must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols.
		if ( lowNotificationCallbackRoutineName != null ) {
			validateIdentifierName(problems, lowNotificationCallbackRoutineName);
		}		
		
		// [26] If specified, the cDataType value must conform to ANSI C type identifier rules and must not clash with or OS symbols.
		if ( cDataType != null ) {
			validateTypeName(problems, cDataType);
		}		
		
		// [27] If the messageProperty value equals SEND_STATIC_INTERNAL then any identified receivingMessages must have a messageProperty value of RECEIVE_UNQUEUED_INTERNAL or RECEIVE_QUEUED_INTERNAL.
		if ( isQueuedOrUnqueuedSendingMessage() ) {
			for ( Message nextMessage : receivingMessages ) {
				if ( nextMessage.isQueuedOrUnqueuedReceivingMessage() == false ) {
					problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a data sender that is related to an incompatible receiver '"+nextMessage.getName()+"'"));
				}
			}			
		}
		
		// [28] If the messageProperty value equals SEND_ZERO_INTERNAL then any identified receivingMessages must have a messageProperty value of RECEIVE_ZERO_INTERNAL.
		if ( isSendingMessage() && isZeroLengthMessage() ) {
			for ( Message nextMessage : receivingMessages ) {
				if ( nextMessage.isReceivingMessage() == false || nextMessage.isZeroLengthMessage() == false ) {
					problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a zero sender that is related to an incompatible receiver '"+nextMessage.getName()+"'"));
				}
			}
		}
			
		// [29] If the messageProperty value equals RECEIVE_ZERO_INTERNAL there should be no runnables declared as directly or indirectly accessing the message.
		if ( isReceivingMessage() && isZeroLengthMessage() ) {
			
			// Check if message has any direct accessors
			boolean hasAccesors = (runnables.size() > 0) ? true : false;
			
			if (hasAccesors) {
				problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a zero length receiver that is declared as being directly accessed by tasks/ISRs"));
			}
			else {
				// message has no direct accessors, so check if any Messages that can call this message
				// via their callback have accessors (i.e. find indirect accessors).
				for ( Message nextUser : getAllMessageUsers() ) {
					if ( nextUser.getRunnables().size() > 0 ) {
						hasAccesors = true;
					}
				}
				
				if (hasAccesors) {
					problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is a zero length receiver that is declared as being indirectly accessed by tasks/ISRs"));
				}				
			}
			

		}
		
		// [30] If the messageProperty value does not equal RECEIVE_ZERO_INTERNAL there should be at least one runnable declared as directly or indirectly accessing the message.
		if ( !(isReceivingMessage() && isZeroLengthMessage()) ) {
			
			// Check if message has any direct accessors
			boolean hasAccesors = (runnables.size() > 0) ? true : false;
			
			if (hasAccesors == false) {
				// message has no direct accessors, so check if any Messages that can call this message
				// via their callback have accessors (i.e. find indirect accessors).
				for ( Message nextUser : getAllMessageUsers() ) {
					if ( nextUser.getRunnables().size() > 0 ) {
						hasAccesors = true;
					}
				}
				
				if (hasAccesors == false) {
					problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not declared as being accessed by any tasks or ISRs"));
				}				
			}			
		}		
		
		// [31] If the Message is a receiving message then an associated deviceName must be specified. [error] [EXTENSION]
		if ( isReceivingMessage() ) {
			
			if ( deviceName == null || deviceName.length() == 0 ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a receiver, but has no device name specified"));
			}
		}
		
		// [32] If present then the deviceName must be a valid device name. [error] [EXTENSION]
		if ( deviceName != null && deviceName.length() > 0 ) {

			validateIdentifierName(problems, deviceName);
		}		
		
		// [33] If the messageProperty equals RECEIVE_STREAM_INTERNAL then a bufferSize value of more than 0 must be specified. [error] [EXTENSION]
		if ( isStreamReceivingMessage() && bufferSize <= 0 ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' is a stream receiver with an invalid buffer size (less than 1)"));
		}
		
		// [34] If the messageProperty does not equal RECEIVE_STREAM_INTERNAL then a bufferSize value of 0 must be specified. [information] [EXTENSION]	
		if ( isStreamReceivingMessage() == false && bufferSize != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not a stream receiver, but has a buffer size specified"));
		}
		
		// [35] If the messageProperty does not equal RECEIVE_STREAM_INTERNAL then a lowThreshold value of 0 must be specified. [information] [EXTENSION]	
		if ( isStreamReceivingMessage() == false && lowThreshold != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not a stream receiver, but has a low threshold value specified"));
		}		
		
		// [36] If the messageProperty does not equal RECEIVE_STREAM_INTERNAL then a highThreshold value of 0 must be specified. [information] [EXTENSION]	
		if ( isStreamReceivingMessage() == false && highThreshold != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not a stream receiver, but has a high threshold value specified"));
		}
		
		// [37] If the messageProperty does not equal RECEIVE_STREAM_INTERNAL then the lowNotificationProperty value should equal NONE. [information] [EXTENSION]
		if ( isStreamReceivingMessage() == false && lowDoesNothing() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' is not a stream receiver, but has low threshold notification action specified"));
		}
		
		// [38] If specified, the lowThreshold value must be greater than, or equal to, zero. [error] [EXTENSION]
		if ( lowThreshold < 0 ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' has an invalid low threshold value specified (less than 0)"));
		}

		// [39] If specified, the highThreshold value must be greater than, or equal to, zero. [error] [EXTENSION]
		if ( highThreshold < 0 ) {
			problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' has an invalid high threshold value specified (less than 0)"));
		}		

		// [40] If the lowThreshold value equals zero or the lowThreshold value is greater than the bufferSize value then the lowNotificationProperty value should equal NONE. [information] [EXTENSION]
		if ( (lowThreshold == 0 || lowThreshold > bufferSize) && lowDoesNothing() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' has a low threshold notification action specified that will never trigger"));
		}		

		// [41] If the highThreshold value equals, or is greater than, the bufferSize value then the notificationProperty value should equal NONE. [information] [EXTENSION]
		if ( highThreshold >= bufferSize && doesNothing() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Message '"+getName()+"' has a high threshold notification action specified that will never trigger"));
		}
		
		// [42] If present then the deviceName must be unique when compared to all other Object names, comAppMode names and generated flag macro names.[error]		
		if ( deviceName != null && deviceName.length() > 0 ) {
			
			Collection<String> usedNamesSet = new HashSet<String>();
			
			usedNamesSet.addAll(getCpu().getAllContainedElementNames());	// all object names (inc app mode names)
			usedNamesSet.addAll(getCpu().getAllCOMAppModeNames());			// all COM AppMode names
			usedNamesSet.addAll(getCpu().getAllFlagMacroNames());			// all flag macro names
			
			if ( usedNamesSet.contains(deviceName) ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' contains a device name '"+deviceName+"' that clashes with another name in the system"));
			}		
		}		
		
		// [43] If present then the notificationFlagName macro name and lowNotificationFlagName macro name must be unique when compared to all other Object names and comAppMode names.[error]
		if ( notificationFlagName != null || lowNotificationFlagName != null ) {
			Collection<String> usedNamesSet = new HashSet<String>();
			
			usedNamesSet.addAll(getCpu().getAllContainedElementNames());	// all object names (inc app mode names)
			usedNamesSet.addAll(getCpu().getAllCOMAppModeNames());			// all COM AppMode names
			
			
			if ( notificationFlagName != null && usedNamesSet.contains(getReadFlagMacroName()) || usedNamesSet.contains(getResetFlagMacroName()) ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' contains a flag name '"+notificationFlagName+"' that clashes with another name in the system"));
			}

			if ( lowNotificationFlagName != null && usedNamesSet.contains(getLowReadFlagMacroName()) ||  usedNamesSet.contains(getLowResetFlagMacroName()) ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+getName()+"' contains a low threshold flag name '"+notificationFlagName+"' that clashes with another name in the system"));
			}
		}
	}	
	
	
	/**
	 * 
	 * @param cpu the Cpu that contains the message
	 * @param name the name of the message
	 */
	public Message(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
