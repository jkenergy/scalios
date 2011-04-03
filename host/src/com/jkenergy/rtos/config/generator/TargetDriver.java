package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetDriver.java $
 * 
 */

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Abstract base class for all Target Driver classes.<br><br>
 * 
 * The driver classes derived from this class provide device driver specific code generation, thus many
 * of the methods provided within this base class provide generator like functionality.<br><br>
 * 
 * Concrete instances of this class are created and managed by the {@link DriverManager}.<br><br>
 * 
 * A TargetDriver drives zero or more {@link TargetDevice} instances.
 * 
 * @author Mark Dixon
 *
 */

public abstract class TargetDriver extends TargetElement {

	/**
	 * Name of the default device control function, used if the driver does not provide a ctl implementation.
	 */
	private final static String DEFAULT_CONTROL_DEVICE_FN = OSAnsiCGenerator.OSPREFIX+"driver_default_ctl";
	
	/**
	 * The {@link OSGenerator} that should be used for generation.
	 * 
	 * This is a static member since all instances share the same generator.
	 */
	private static OSGenerator generator;

	
	/**
	 * Collection of {@link TargetDevice} instances which are driven by the driver.
	 */
	private Collection<TargetDevice> devices = new LinkedHashSet<TargetDevice>();
	
	
	/**
	 * @return the devices driven by the driver
	 */
	public Collection<TargetDevice> getTargetDevices() {
		return devices;	
	}
	
	/**
	 * Returns the name of the device ctl function provided by the driver.
	 * 
	 * Drivers should override this if they provide their own ctl function.
	 * 
	 * @return the name of the device ctl function provided by the driver
	 */
	public String getCtlFnName() {
		return DEFAULT_CONTROL_DEVICE_FN;
	}
	
	
	/**
	 * Returns true if the device driver requires the use of a {@link TargetResource} when driving the
	 * specified {@link TargetDevice}.
	 * 
	 * A resource is often required by devices that are sharable. e.g. an ethernet device.
	 * 
	 * @param device the {@link TargetDevice} which is driven by the driver
	 * @return true if the device requires a resource on the final target
	 */
	public abstract boolean requiresDeviceResource(TargetDevice device);	
	
	/**
	 * Returns true if the device driver supports sharable devices.
	 * 
	 * e.g. COM type drivers allow sharing of devices such as ethernet controllers,
	 * whereas Counter type drivers no not allow sharing of devices, since a Counter
	 * requires its own counter device. 
	 * 
	 * Each type of driver needs to override this an return an approprirtae value.
	 * 
	 * @return true if the driver supports sharable devices
	 */
	public abstract boolean supportsSharableDevices();
	
	/**
	 * Generates the ANSI C compliant code for the driver (rather than a device that uses the driver).
	 */
	public abstract void genCDriverCode();	
	
	/**
	 * Generates the ANSI C compliant initialisation code for a particular device.
	 * 
	 * @param device the {@link TargetDevice} for which generation is being performed
	 */
	public abstract void genCInitCode(TargetDevice device);
	
	/**
	 * Generates the ANSI C compliant header code for a particular device.
	 * 
	 * @param device the {@link TargetDevice} for which generation is being performed
	 */
	public abstract void genCHeaderCode(TargetDevice device);
	
	/**
	 * Generates the Assembly language code for a particular device.
	 * 
	 * @param device the {@link TargetDevice} for which generation is being performed
	 */	
	public abstract void genAssemblyCode(TargetDevice device);	

	
	/**
	 * Returns a driven {@link TargetDevice} that has the given name (if any).
	 * 
	 * @param deviceName the name of the device to be returned
	 * @return the {@link TargetDevice} instance, null if named device does not exist.
	 */
	public TargetDevice getNamedDevice(String deviceName) {
		
		for ( TargetDevice nextDevice : devices ) {
			if ( nextDevice.getName().equals(deviceName) ) {
				return nextDevice;
			}
		}
		return null;
	}
	
	
	/**
	 * Creates a new {@link TargetDevice} instance which is driven by the driver.
	 *
	 * If the driver allows sharable devices and a device already exists with the given name
	 * then that is returned (thus allowing sharing of devices where appropriate), otherwise
	 * a new {@link TargetDevice} instance is created.
	 * 
	 * @see #supportsSharableDevices()
	 * 
	 * @param options the device options (name,value) pair string
	 * @return the created {@link TargetDevice} instance.
	 */
	public TargetDevice createNewDevice(String options) {
		
		// First check to see if the named device already exists
		TargetDevice device = null;
		
		// TODO test what happens when no deviceName is specified.
		String deviceName = TargetDevice.checkForDeviceNameOption(options);
			
		if ( supportsSharableDevices() ) {
			// if sharable devices are supported by the driver then attempt to find device with given name
			device = getNamedDevice(deviceName);
		}
				
		if ( device == null ) {
			// Create a TargetDevice instance that is driven by this driver
			device = new TargetDevice(this, deviceName, options);
		}
		
		// add to list of devices driven by the driver
		devices.add(device);
				
		return device;		
	}
	
	
	/**
	 * 
	 * @return true if any devices driven by the driver are associated with queued messages
	 */
	protected boolean drivesQueuedMessages() {
		
		for ( TargetDevice device : getTargetDevices() ) {
			
			if ( device.hasQueuedMessages() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return true if any devices driven by the driver are associated with unqueued messages
	 */
	protected boolean drivesUnqueuedMessages() {
		
		for ( TargetDevice device : getTargetDevices() ) {
			
			if ( device.hasUnqueuedMessages() ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return true if any devices driven by the driver are associated with zero length messages
	 */
	protected boolean drivesZeroLengthMessages() {
		
		for ( TargetDevice device : getTargetDevices() ) {
			
			if ( device.hasZeroLengthMessages() ) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * 
	 * @return true if any devices driven by the driver are associated with stream messages
	 */
	protected boolean drivesStreamMessages() {
		
		for ( TargetDevice device : getTargetDevices() ) {
			
			if ( device.hasStreamMessages() ) {
				return true;
			}
		}
		return false;
	}	
	
	/**
	 * 
	 * @return the embedded C name of target driver as provided by {@link TargetElement#getName()} used within driver function names.
	 */
	String getCDriverName() {
		
		return this.getName();
	}
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * 
	 * @param text the text to be written
	 */
	protected static void writeln(String text) {
		generator.writeln(text);
	}
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * 
	 * @param text the text to be written
	 */
	protected static void write(String text) {
		generator.write(text);
	}	

	/**
	 * Wrapper method that calls the generator implementation.
	 * 
	 * @param text the text to be appended
	 */
	protected static void append(String text) {
		generator.append(text);
	}	
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * @param count the number of NLs to be written
	 */
	protected static void writeNLs(int count) {
		
		generator.writeNLs(count);
	}

	/**
	 * Wrapper method that calls the generator implementation.
	 */
	protected static void writeNL() {
		
		generator.writeNL();
	}	

	/**
	 * Wrapper method that calls the generator implementation.
	 *
	 */
	protected static void incTabs() {
		generator.incTabs();
	}

	/**
	 * Wrapper method that calls the generator implementation.
	 *
	 */	
	protected static void decTabs() {
		generator.decTabs();
	}		
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * @param comment the text part of the comment to be generated
	 * @return the full comment text using the appropriate delimiters
	 */
	protected static String comment(String comment) {
		
		return generator.comment(comment);
	}
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * @param comment the text part of the comment to be generated
	 * @return the full comment text using the appropriate assembler delimiters
	 */
	protected static String asmcomment(String comment) {
		
		return generator.asmcomment(comment);
	}	
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * @param comment
	 * @return the full verbose comment text using the appropriate delimiters
	 */
	protected static String verboseComment(String comment) {
		
		return generator.verboseComment(comment);
	}
	
	/**
	 * Wrapper method that calls the generator implementation.
	 * 
	 * @param comment
	 * @return the full verbose comment text using the appropriate delimiters
	 */
	protected static String verboseAsmComment(String comment) {
		
		return generator.verboseAsmComment(comment);
	}	
	

	/**
	 * @return the {@link OSGenerator} used for generation purposes.
	 */
	public static OSGenerator getGenerator() {
		return generator;
	}

	/**
	 * @param generator the {@link OSGenerator} to use during generation.
	 */
	public static void setGenerator(OSGenerator generator) {
		TargetDriver.generator = generator;
	}	
	
	/**
	 * Returns the name to #include for the driver
	 *  
	 * This may be overridden in the derived classes if required
	 * 
	 * @return the textual name used to include 
	 */
	public String getIncludeName() {
		return getCDriverName()+".h";
	}	

	/**
	 * Requires a default constructor since instances of this class are created dynamically.
	 * 
	 * See {@link DriverManager}
	 */
	protected TargetDriver() {
		super(null);
	}
}
