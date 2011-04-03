package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetMessage.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.osmodel.Message;


/**
 * Intermediate target element used to store information on messages to be generated.
 * 
 * @author Mark Dixon
 *
 */

public abstract class TargetMessage extends TargetElement {
	

	/**
	 * Flag indicating that the message is a zero length message
	 */
	private boolean zeroLength;
	
	/**
	 * Flag indicating that the message is a stream message
	 */
	private boolean streamMessage;
	
	/**
	 * Set of TargetRunnable instances that access the TargetMessage. Inverse of {@link TargetRunnable#getTargetMessages()}
	 */
	private Collection<TargetRunnable> targetRunnables = new LinkedHashSet<TargetRunnable>();		
	
	/**
	 * Set of TargetReceivingMessage instances that send or receive this message from their callback routine. Inverse of {@link TargetReceivingMessage#getCallbackMessages()}
	 */
	private Collection<TargetReceivingMessage> messageUsers = new HashSet<TargetReceivingMessage>();	
	
	
	/**
	 * @return Returns the targetRunnables.
	 */
	protected Collection<TargetRunnable> getTargetRunnables() {
		return targetRunnables;
	}	
	
	/**
	 * @return Returns the targetRunnables.
	 */
	protected Collection<TargetReceivingMessage> getMessageUsers() {
		return messageUsers;
	}	
	
	
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		Message message = getMessage();
		
		if (message != null) {
			// This TargetMessage represents a Message from the OS model
			targetRunnables = getAllTargetElements(message.getRunnables());
			
			messageUsers = getAllTargetElements(message.getMessageUsers());
		}	
	}
	
	
	/**
	 * Returns the set of all TargetSendingMessage instances to which this TargetMessage is related via
	 * the {@link #messageUsers} collection.
	 * 
	 * TargetSendingMessage instances are identified by getting the sender of each {@link TargetReceivingMessage}
	 * that is within the {@link #messageUsers} collection, then recursively calling this function on each TargetSendingMessage.
	 * 
	 * @param messages the Collection of ALL TargetSendingMessage instances to which this message is related
	 */
	void getAllSendingMessages(Set<TargetMessage> messages) {
			
		// Iterate over all receiving Messages that access (send/receive) this message from their callback routine
		for (TargetReceivingMessage next : messageUsers) {
			
			// next receiving message sends/receives this message from its callback routine
			TargetSendingMessage sender = next.getTargetSender();
			
			if ( sender != null ) {
			
				if ( messages.add(sender) ) {
					// next sending message was not already in the set, so ask that message to get all of its sending messages
					sender.getAllSendingMessages(messages);
				}
			}
		}
	}
	
	
	/**
	 * @return Returns the OS Model Message on which the TargetMessage is based (if any)
	 */
	public Message getMessage() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Message;
		
		return (Message)getOsModelElement();
	}	
		
	
	/**
	 * @return true if the message is a zero length message
	 */
	public boolean isZeroLengthMessage() {
		return zeroLength;
	}

	/**
	 * @return true if the message is a stream message
	 */
	public boolean isStreamMessage() {
		return streamMessage;
	}	
	
	
	/**
	 * Standard Constructor that creates a TargetMessage that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	TargetMessage(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetMessage that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	TargetMessage(TargetCpu cpu, Message osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		zeroLength = osModelElement.isZeroLengthMessage();
		streamMessage = osModelElement.isStreamMessage();
		
		assert zeroLength != streamMessage;	// can't be both zero length and a stream
	}
}
