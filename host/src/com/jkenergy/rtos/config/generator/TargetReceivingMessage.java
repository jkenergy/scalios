package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetReceivingMessage.java $
 * 
 */

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.jkenergy.rtos.config.osmodel.Message;


/**
 * Intermediate target element used to store information on receiving messages to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetReceivingMessage extends TargetMessage {
	
	
	/**
	 * The name of the flag to be set on receive notification
	 */
	private String flagName = null;
	
	/**
	 * The name of the flag to be set on low threshold notification
	 */
	private String lowFlagName = null;	
	
	/**
	 * True if the receiving message sets a flag on receipt notification
	 */
	private boolean setsFlag = false;
	
	/**
	 * True if the receiving message sets a flag on low threshold notification
	 */
	private boolean setsLowFlag = false;	
	
	/**
	 * The number of messages that may be stored within a queued receiving message, 0 if unqueued message, stream message or zero length message
	 */
	private long queueSize = 0;
	
	/**
	 * The size of the buffer for a stream receiving message, 0 if queued, unqueued message or zero length message
	 */
	private long bufferSize = 0;	
	
	/**
	 * The low threshold notification level for a stream receiving message, 0 if queued, unqueued message or zero length message
	 */
	private long lowThreshold = 0;	
	
	/**
	 * The high threshold notification level for a stream receiving message, 0 if queued, unqueued message or zero length message
	 */
	private long highThreshold = 0;	
	
	/**
	 * The initial value for an unqueued message, null if queued message or zero length message 
	 */
	private BigInteger initialValue = null;
	
	/**
	 * The {@link TargetExpiry} that determines the action to be performed on notification of message receipt
	 * If the message is a tream message then this notifiation action is used for high threshold notifiction
	 */
	private TargetExpiry notificationAction = null;	
	
	/**
	 * The {@link TargetExpiry} that determines the action to be performed on low threshold notification for stream messages
	 */
	private TargetExpiry lowNotificationAction = null;	
	
	/**
	 * The device to which the message is bound
	 */
	private TargetDevice device = null;	
	
	/**
	 * Flag that indicates an attempt was made to load a device driver not compatable with the one required.
	 * i.e. The device driver was not a kind of TargetCOMDriver.
	 */
	private boolean invalidDeviceType=false;	
	
	/**
	 * The list of messages that are sent and/or received by the callback routine. Inverse of {@link TargetMessage#getMessageUsers()}
	 * Only applicable when {@link TargetExpiry} represents a callback handler
	 */
	private Collection<TargetMessage> callbackMessages = new HashSet<TargetMessage>();
	
	/**
	 * The {@link TargetSendingMessage} instance which sends to this message. Inverse of {@link TargetSendingMessage#getTargetReceivers()}
	 */
	private TargetSendingMessage targetSender = null;	
		
	/**
	 * The {@link TargetResource} to which the message is linked (if any)
	 */
	private TargetResource targetResource = null;

	

	
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();	
		
		Message message = getMessage();
		
		if (message != null) {
			// This TargetReceivingMessage represents a Message from the OS model
			
			targetSender = getTargetElement(message.getSendingMessage());
			
			callbackMessages = getAllTargetElements(message.getCallbackMessages());
			
			// create an appropriate TargetExpiry instance that handles the receipt notification (or high threshold notification for stream messages)
			if ( message.callsHandler() ) {
				notificationAction = new TargetExpiry(message.getNotificationCallbackRoutineName());
			}
			else if ( message.setsEvent() ) {
				notificationAction = new TargetExpiry(this.<TargetEvent>getTargetElement(message.getNotificationEvent()), this.<TargetTask>getTargetElement(message.getNotificationTask()));
			}
			else if ( message.activatesTask() ) {
				notificationAction = new TargetExpiry(this.<TargetTask>getTargetElement(message.getNotificationTask()));
			}
			else if ( message.doesNothing() || message.setsFlag()  ) {	
				// no need to create a TargetExpiry
			}	
			
			// create an appropriate TargetExpiry instance that handles low threshold notification for stream messages
			if ( message.lowCallsHandler() ) {
				lowNotificationAction = new TargetExpiry(message.getLowNotificationCallbackRoutineName());
			}
			else if ( message.lowSetsEvent() ) {
				lowNotificationAction = new TargetExpiry(this.<TargetEvent>getTargetElement(message.getLowNotificationEvent()), this.<TargetTask>getTargetElement(message.getLowNotificationTask()));
			}
			else if ( message.lowActivatesTask() ) {
				lowNotificationAction = new TargetExpiry(this.<TargetTask>getTargetElement(message.getLowNotificationTask()));
			}
			else if ( message.lowDoesNothing() || message.lowSetsFlag()  ) {	
				// no need to create a TargetExpiry
			}				
		}
	}	
	

	/**
	 * {@inheritDoc}
	 * <p>
	 * This override ensures that the sender of this receiver is also processed.
	 * </p>
	 */
	@Override
	void getAllSendingMessages(Set<TargetMessage> messages) {
		
		super.getAllSendingMessages(messages);
		
		// Get the sender of this receiver.
		if ( targetSender != null ) {
			if ( messages.add(targetSender) ) {
				// sending message was not already in the set, so ask that message to get all of its related sending messages
				targetSender.getAllSendingMessages(messages);
			}	
		}
	}
	
	/**
	 * Returns the collection of all {@link TargetRunnable} instances that may directly or indirectly
	 * access the message. Indirect accessors are calculated by idenitfiying all sending messages
	 * that can potentially cause this receiver to be accessed from within their callback handler notification.
	 * 
	 * @return the collection of all {@link TargetRunnable} instances that may access the message
	 */
	protected Collection<TargetRunnable> getAllAccessors() {
				
		Set<TargetRunnable> allAccessors = new HashSet<TargetRunnable>();
		
		/*
		 * Call helper that returns the set of all sending messages related to this receiver via the messageUsers relationship,
		 * i.e. all senders that can potentially cause this receiver to be accessed from within their callback handler
		 * notification.
		 * 
		 * Only the sending messages are traced by this method, since it is Tasks/ISRs that call SendMessage() type API
		 * calls that can cause COM callback handlers to run, hence the ceiling of a receiver needs only be concerned
		 * with Tasks/ISRs that access senders related via the COM callback messageUsers relationsip.
		 */
		Set<TargetMessage> allMessages = new HashSet<TargetMessage>();
		
		getAllSendingMessages(allMessages); 
		
		allMessages.add(this);	// add this to the set since its accessors need to be accessed also
	
		// Iterate over all related messages to construct set of accessing TargetRunnables
		for (TargetMessage next : allMessages) {
				
			allAccessors.addAll(next.getTargetRunnables());
		}
		
		return allAccessors;
	}	
	
	
	/**
	 * @return the targetSender
	 */
	public TargetSendingMessage getTargetSender() {
		return targetSender;
	}

	/**
	 * @return the flagName, may be null if no flag set
	 */
	public String getFlagName() {
		return flagName;
	}
	
	/**
	 * 
	 * @return true if this message sets a flag for receipt notification
	 */
	public boolean setsFlag() {
		return setsFlag;
	}

	/**
	 * @return the lowFlagName, may be null if no low threshold flag set
	 */
	public String getLowFlagName() {
		return lowFlagName;
	}
	
	/**
	 * 
	 * @return true if this message sets a flag for low threshold notification
	 */
	public boolean lowSetsFlag() {
		return setsLowFlag;
	}	
	
	/**
	 * @return the queueSize
	 */
	public long getQueueSize() {
		return queueSize;
	}

	/**
	 * @return the bufferSize
	 */
	public long getBufferSize() {
		return bufferSize;
	}
	
	/**
	 * @return the lowThreshold
	 */
	public long getLowThreshold() {
		return lowThreshold;
	}	
	
	/**
	 * @return the highThreshold
	 */
	public long getHighThreshold() {
		return highThreshold;
	}
	
	/**
	 * @return the notificationAction, null if only flag needs setting or no notification required
	 */
	public TargetExpiry getNotificationAction() {
		return notificationAction;
	}	
	
	/**
	 * @return the lowNotificationAction, null if only flag needs setting or no low threshold notification required
	 */
	public TargetExpiry getLowNotificationAction() {
		return lowNotificationAction;
	}	
	
	/**
	 * @return the callbackMessages
	 */
	public Collection<TargetMessage> getCallbackMessages() {
		return callbackMessages;
	}

	/**
	 * @return the initialValue (if any).
	 */
	public BigInteger getInitialValue() {
		return initialValue;
	}
	
	/**
	 * 
	 * @param newTargetResource the {@link TargetResource} to which the message is linked
	 */
	public void setTargetResource(TargetResource newTargetResource) {
		targetResource = newTargetResource;
	}
	
	/**
	 * 
	 * @return the {@link TargetResource} to which the message is linked, null if no resource used
	 */
	public TargetResource getTargetResource() {
		return targetResource;
	}	
	

	/**
	 * 
	 * @return true if the message is queued, else false
	 */
	public boolean isQueuedMessage() {
		return queueSize > 0;	// queue size is always more than 0 for queued messages
	}
	
	/**
	 * 
	 * @return true if the message is unqueued (non-zero length, non-stream message), else false
	 */
	public boolean isUnqueuedMessage() {
		return (queueSize == 0 && !isZeroLengthMessage() && !isStreamMessage());	// queue size is always 0 for unqueued messages
	}	
	
	
	/**
	 * 
	 * @return true if the message is initialised (i.e. is non-queued, non-zero length and has an initial value other than 0), else false
	 */
	public boolean isInitialisedMessage() {
		
		if ( initialValue != null ) {
			// an initial value exists, so must be unqueued non-zero length, non-stream message.
			// The message is only classed as being initialised if the initial value is not 0.
			if ( initialValue.equals(BigInteger.ZERO)==false ) {
				return true;	// initial value is <>0, so message is initialised
			}
		}
		return false;
	}
	
	/**
	 * @return the dataTypeName from the associated {@link TargetSendingMessage}
	 */
	public String getDataTypeName() {
		return (targetSender != null) ? targetSender.getDataTypeName() : "";
	}	

	/**
	 * @return the invalidDeviceType flag
	 */
	public boolean isInvalidDeviceType() {
		return invalidDeviceType;
	}
	
	/**
	 * The {@link TargetDevice} that handles the message transmission
	 * 
	 * @return the {@link TargetDevice} that handles the message transmission
	 */
	public TargetDevice getDevice() {
		return device;
	}	
	
	/**
	 * The {@link TargetCOMDriver} that drives the message's device
	 * 
	 * @return the {@link TargetCOMDriver} that drives the message's device
	 */
	public TargetCOMDriver getMessageDriver() {
		
		if ( device != null ) {
			
			TargetDriver driver = device.getDriver();
			
			assert driver instanceof TargetCOMDriver;	// the driver must be a TargetCOMDriver, see constructor
			
			return (TargetCOMDriver)driver;
		}
		
		return null;
	}		
	
	
	/**
	 * Returns true if the TargetReceivingMessage requires a resource on the final target.
	 * Whether a resource is required depends on the {@link TargetDevice} driver to which the
	 * message is attached.
	 * 
	 * @return true if the TargetReceivingMessage requires a resource on the final target
	 */
	public boolean requiresMessageResource() {
		
		if ( device != null ) {
		
			// ask the driver of the device for this information
			return getMessageDriver().requiresMessageResource(this);
		}
		
		return false;	// if device unknown then no resource required.
	}
	
	/**
	 * Standard Constructor that creates a TargetReceivingMessage that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetReceivingMessage(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetReceivingMessage that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetReceivingMessage(TargetCpu cpu, Message osModelElement) {
		
		super(cpu, osModelElement);

		assert osModelElement.isReceivingMessage();		// TargetReceivingMessage must be based on receiving Message instances only 
		
		// copy required info. from the given OSModelElement
		
		setsFlag = osModelElement.setsFlag();
		
		if ( setsFlag ) {
			flagName = osModelElement.getNotificationFlagName();
		}
		
		setsLowFlag = osModelElement.lowSetsFlag();
		
		if ( setsLowFlag ) {
			lowFlagName = osModelElement.getLowNotificationFlagName();
		}		
		
		if ( osModelElement.isQueuedMessage() ) {
			// queued message, so read the queue size
			queueSize = osModelElement.getQueueSize();
		}
		else if ( osModelElement.isUnqueuedMessage() ) {
			// unqueued message, so read the initial value 
			initialValue = osModelElement.getInitialValue();
		}
		else if ( osModelElement.isStreamMessage() ) {
			// stream message, so read the buffer size 
			bufferSize = osModelElement.getBufferSize();
			lowThreshold = osModelElement.getLowThreshold();
			highThreshold = osModelElement.getHighThreshold();
		}		
		// else, zero length message. No attributes to be read.
		
		
		///////////////////////////////////////////////////////////////
		// Setup the TargetDevice that drives the TargetReceivingMessage
			
		try {
			TargetCOMDriver driver = null;
			
			// attempt to get the driver name from the device options
			String driverName = TargetDevice.checkForDriverNameOption(osModelElement.getDeviceOptions());
			
			if ( driverName == null ) {
				// no driver name in the options so use the device name
				driverName = osModelElement.getDeviceName();	// TODO remove device name from the OS Message (see Counter for same behaviour).
			}	
			
			// get the driver from the manager
			driver = cpu.getDriverManager().getDriver(driverName);				
							
			if ( driver != null ) {
				// driver available, so create the device using the device name and device options from the OSModel element.
				
				// Since devices can be shared by Messages, e.g. all messages may use device "CAN1"
				// the called method may return an existing device that is already used by a message.
				device = driver.createNewDevice(osModelElement.getDeviceOptions());
			}
		}
		catch (java.lang.ClassCastException e ) {
			// if this is caught, then the loaded device driver was not an appropriate type for this class
			// i.e. not a TargetCOMDriver
			device = null;
			invalidDeviceType = true;	// set flag that identifies device driver was loaded, but of wrong type
		}
		
		// TODO check for null device in pregen check, then report as error (can't load driver xxxxxx)
		
		if ( device != null ) {
	
			assert device.getDriver() instanceof TargetCOMDriver;	// device must be based on correct driver type
			
			device.addMessage(this);								// associate the device with this Message
		}		
		
	}



}
