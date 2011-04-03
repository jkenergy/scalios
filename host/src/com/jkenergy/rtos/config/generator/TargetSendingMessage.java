package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetSendingMessage.java $
 * 
 */

import java.util.Collection;
import java.util.LinkedHashSet;



import com.jkenergy.rtos.config.osmodel.Message;

/**
 * Intermediate target element used to store information on sending messages to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetSendingMessage extends TargetMessage {
	
	/**
	 * Set of {@link TargetReceivingMessage} instances to which the message sends data. Inverse of {@link TargetReceivingMessage#getTargetSender()}
	 */
	private Collection<TargetReceivingMessage> targetReceivers = new LinkedHashSet<TargetReceivingMessage>();	
	

	/**
	 * The datatype name of the data sent by the sending message, e.g. int
	 */
	private String dataTypeName = null;
	
	
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		
		Message message = getMessage();
		
		if (message != null) {
			// This TargetSendingMessage represents a Message from the OS model
			targetReceivers = getAllTargetElements(message.getReceivingMessages());
		}
		
	}	
	
	
	/**
	 * @return the targetReceivers
	 */
	public Collection<TargetReceivingMessage> getTargetReceivers() {
		return targetReceivers;
	}

	/**
	 * 
	 * @return the first {@link TargetReceivingMessage} to which the message sends, null if no receivers
	 */
	public TargetReceivingMessage getFirstTargetReceiver() {
		
		return targetReceivers.isEmpty() ? null : targetReceivers.iterator().next();
	}
	
	/**
	 * @return the dataTypeName
	 */
	public String getDataTypeName() {
		return dataTypeName;
	}


	/**
	 * Standard Constructor that creates a TargetReceivingMessage that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetSendingMessage(TargetCpu cpu, String name) {
		super(cpu, name);
	}
	
	/**
	 * Copy contructor that creates a TargetReceivingMessage that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetSendingMessage(TargetCpu cpu, Message osModelElement) {
		
		super(cpu, osModelElement);

		assert osModelElement.isSendingMessage();		// TargetSendingMessage must be based on sending Message instances only
		
		// copy required info. from the given OSModelElement
		dataTypeName = osModelElement.getCDataType();
	}
}
