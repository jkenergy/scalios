package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetDevice.java $
 * 
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * The class represents all Target Devices. Its main function is to provide a generic set of operations to allow management of value/option pairs.
 * 
 * 
 * @author Mark Dixon
 *
 */
public class TargetDevice extends TargetElement {

	/**
	 * The option name used to specify an alternative driver name during driver loading.
	 * This name may be used in the (name->value) option pairs associated with a device.
	 * note: this name is used in a non-case sensitive manner.
	 * 
	 * @see #checkForDriverNameOption(String)
	 */
	private final static String DRIVER_OPTION_NAME = "DRIVER";	
	
	/**
	 * The option name used to specify the device name to the driver.
	 * This name may be used in the (name->value) option pairs associated with a device.
	 * note: this name is used in a non-case sensitive manner.
	 * 
	 * @see #checkForDeviceNameOption(String)
	 */
	private final static String DEVICE_OPTION_NAME = "DEVICE";
	
	/**
	 * The {@link TargetDriver} that drives the device, never null.
	 */
	private TargetDriver driver;
	
		
	/**
	 * The map of optionName -> optionValue pairs.
	 * 
	 * This map allows arbitrary options to be specified for a particular device.
	 * 
	 * Option names are always converted to lowercase prior to processing, thus
	 * option names are not case sensitive, but option values are.
	 * 
	 */
	private Map<String, String> optionPairs = new HashMap<String, String>();
	
	/**
	 * The {@link TargetReceivingMessage} instances that use the device, if any.
	 */
	private Collection<TargetReceivingMessage> messages = new HashSet<TargetReceivingMessage>();
	
	/**
	 * The {@link TargetCounter} that uses the device, if any.
	 */
	private TargetCounter counter = null;
	
	/**
	 * The {@link TargetResource} to which the device is linked (if any)
	 */
	private TargetResource targetResource = null;	
	
	/**
	 * Parses the device option string. The format of this string must be {<name> = <value>,..}
	 * The parsed entries are placed in the returned name->value map.
	 * 
	 * If no options are specified then an empty map is returned.
	 * 
	 * The option names are converted to lower case prior to being store in the map, therefore
	 * the option names are not case sensitive, but the associated values are.
	 * 
	 * Only correctly formed <name>=<value> pairs are parsed correctly. If a name or value is missing from a
	 * pair then nothing is added to the returned map. Although the method is fairly robust and will continue
	 * to parse remaining pairs in such ill-formed strings.
	 * 
	 * Leading and trailing spaces are stripped from both the <name> and <value> strings. 
	 * 
	 * @param optionString the option string to parse in the form {<optionName>=<optionValue>,...}
	 * @return the (name<String> -> value<String>) map of option names and value pairs.
	 */
	private static Map<String,String> parseOptionString(String optionString) {
		
		Map<String,String> optionsMap = new HashMap<String,String>();
		
		if ( optionString != null && optionString.length() > 0 ) {
			int currentIndex = 0;
			
			boolean finished = false;
			
			while ( !finished && currentIndex < optionString.length()) {
				
				// check for '=' within remaining section of the option string
				int eqIndex = optionString.indexOf('=', currentIndex);
				
				if ( eqIndex >= currentIndex ) {
					// '=' exists, so another pair to parse
					
					// extract the optionName first
					String optionName =  optionString.substring(currentIndex, eqIndex).trim();
						
					// Once optionName is known, attempt to extract the value			
					int commaIndex = optionString.indexOf(',', eqIndex);
					
					String optionValue = "";					
					
					if ( commaIndex > eqIndex ) {
						// comma found, so extract value between the '=' and the next','
						optionValue = optionString.substring(eqIndex+1, commaIndex).trim();

						currentIndex = commaIndex+1;
					}
					else {
						// no more commas in string so get remainder of options String for next value
						if ( eqIndex < optionString.length() ) {
							optionValue = optionString.substring(eqIndex+1).trim();
						}
						
						finished = true;
					}
					
					// Add name, value pair to map if both name and value exist
					if ( optionName.length()>0 && optionValue.length()>0 ) {
						// always store the option name as lower case
						optionsMap.put(optionName.toLowerCase(), optionValue);
					}
				}
				else {
					finished = true;
				}
			}
		}
		// else, nothing to parse
		
		return optionsMap;
	}
	
	
	/**
	 * Gets the option value associated with the given option name for the device.
	 * 
	 * The given name is always converted to lowercase prior to processing, thus
	 * option names are not case sensitive, but values are.
	 * 
	 * @param optionName the name of the option for which the value is required.
	 * @return the value that has been assigned to the named option (null if named option not set)
	 */
	public String getOptionValue(String optionName) {
		
		return optionPairs.get(optionName.toLowerCase());
	}

	/**
	 * Static method that gets the option value associated with the given option name
	 * from an unparsed option string.
	 * 
	 * The given name is always converted to lowercase prior to processing, thus
	 * option names are not case sensitive, but values are.
	 * 
	 * @param optionName the name of the option for which the value is required.
	 * @param optionString the device options list in the form {<name>=<value>,...}
	 * @return the value that has been assigned to the named option (null if named option not set)
	 */
	public static String getOptionValueFromString(String optionName, String optionString) {
		
		return parseOptionString(optionString).get(optionName.toLowerCase());	
	}	
	
	
	/**
	 * Static method that returns a driver name specified within the given option string.
	 * 
	 * @param optionString the device options list in the form {<name>=<value>,...}
	 * @return value of the DRIVER_OPTION_NAME option, if one exists (null if DRIVER_OPTION_NAME option not set)
	 */
	public static String checkForDriverNameOption(String optionString) {
		
		return getOptionValueFromString(DRIVER_OPTION_NAME, optionString);
	}
	
	/**
	 * Static method that returns a device name specified within the given option string.
	 * 
	 * @param optionString the device options list in the form {<name>=<value>,...}
	 * @return value of the DEVICE_OPTION_NAME option, if one exists (null if DEVICE_OPTION_NAME option not set)
	 */
	public static String checkForDeviceNameOption(String optionString) {
		
		return getOptionValueFromString(DEVICE_OPTION_NAME, optionString);
	}	
	
	/**
	 * Returns the {@link TargetDriver} that drives the device.
	 * 
	 * @return the {@link TargetDriver} that drives the device, always valid.
	 */
	public TargetDriver getDriver() {
		
		assert driver != null;
		
		return driver;
	}
	
	
	/**
	 * @return the {@link TargetCounter} that uses the device, null if {@link TargetCounter} does not use the device.
	 */
	public TargetCounter getCounter() {
		return counter;
	}	
	
	/**
	 * Sets the {@link TargetCounter} that uses the device.
	 * 
	 * @param counter the {@link TargetCounter} that uses the device.
	 */
	public void setCounter(TargetCounter counter) {
		
		assert messages.size() == 0;	// a device cannot be associated with both a Counter and Messages.
		
		this.counter = counter;
	}	
	
	/**
	 * Sets the {@link TargetResource} that is associated with the device.
	 * 
	 * @param newTargetResource the {@link TargetResource} to which the device is linked
	 */
	public void setTargetResource(TargetResource newTargetResource) {
		targetResource = newTargetResource;
	}
	
	/**
	 * 
	 * @return the {@link TargetResource} to which the device is linked, null if no resource used
	 */
	public TargetResource getTargetResource() {
		return targetResource;
	}		
	
	
	/**
	 * @return the {@link TargetReceivingMessage} instances that use the device
	 */
	public Collection<TargetReceivingMessage> getMessages() {
		return messages;
	}


	/**
	 * Adds a {@link TargetReceivingMessage} to the collection of messages that use the device.
	 * 
	 * @param message a {@link TargetReceivingMessage} instance that uses the device
	 */
	public void addMessage(TargetReceivingMessage message) {
		
		assert counter == null;	// a device cannot be associated with both a Counter and Messages.		
		
		messages.add(message);
	}	
	
	/**
	 * 
	 * @return true if any messages associated with the device are queued messages
	 */
	public boolean hasQueuedMessages() {
		
		for ( TargetReceivingMessage message : messages ) {
			if ( message.isQueuedMessage() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return true if any messages associated with the device are unqueued messages
	 */
	public boolean hasUnqueuedMessages() {
		
		for ( TargetReceivingMessage message : messages ) {
			if ( message.isUnqueuedMessage() ) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * 
	 * @return true if any messages associated with the device are zero length messages
	 */
	public boolean hasZeroLengthMessages() {
		
		for ( TargetReceivingMessage message : messages ) {
			if ( message.isZeroLengthMessage() ) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * 
	 * @return true if any messages associated with the device are zero length messages
	 */
	public boolean hasStreamMessages() {
		
		for ( TargetReceivingMessage message : messages ) {
			if ( message.isStreamMessage() ) {
				return true;
			}
		}
		return false;
	}		
	
	/**
	 * Returns true if the TargetDevice requires a resource on the final target.
	 * Whether a resource is required depends on the {@link TargetDriver} that drives the device.
	 * 
	 * @return true if the TargetDevice requires the use of a resource for mutually exclusive access
	 */
	public boolean requiresDeviceResource()	 {
		
		// ask the driver for this information
		return driver.requiresDeviceResource(this);
	}
	
	
	/**
	 * 
	 * @param driver the {@link TargetDriver} that drives the device 
	 * @param name the name of the device
	 * @param optionString the options string in the form {<optionName>=<optionValue>,...}
	 */
	public TargetDevice(TargetDriver driver, String name, String optionString) {
		super(driver.getTargetCpu(), name);
		
		this.driver = driver;
		
		// Setup the option,value pairs for the device
		optionPairs = parseOptionString(optionString);		
	}	
	
}
