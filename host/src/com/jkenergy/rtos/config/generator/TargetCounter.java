package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetCounter.java $
 * 
 */

import java.util.Collection;
import java.util.LinkedHashSet;

import com.jkenergy.rtos.config.osmodel.Counter;


/**
 * Intermediate target element used to store information on counters to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetCounter extends TargetElement {

	/**
	 * The maximum allowed counter value in ticks
	 */
	private long maxAllowedValue=0; 
	
	/**
	 * Number of ticks required to reach a counter unit 
	 */
	private long ticksPerBase=0;
	
	/**
	 * The minimum allowed number of counter ticks for a cyclic alarm linked to the counter
	 * 
	 */
	private long minCycle=0;	
	
	/**
	 * The {@link TargetDevice} to which the counter is bound, null if device driver access failed
	 */
	private TargetDevice device = null;
	
	/**
	 * Flag that indicates an attempt was made to load a device driver not compatable with the one required.
	 * i.e. The device driver was not a kind of TargetCounterDriver.
	 */
	private boolean invalidDeviceType=false;		
	
	/**
	 * Flag indicating if the counter is a nano-second, rather than tick, counter
	 */
	private boolean isNanoSecondCounter = false;
	
	/****************************************************************************
	 * Data members setup by {@link #initialiseModelAssociations()}
	 */

	/**
	 * Set of {@link TargetAlarm} instances associated with the Counter
	 */
	private Collection<TargetAlarm> targetAlarms = new LinkedHashSet<TargetAlarm>();	
	
	
	/**
	 * @return the maxAllowedValue
	 */
	public long getMaxAllowedValue() {
		return maxAllowedValue;
	}

	/**
	 * @return the ticksPerBase
	 */
	public long getTicksPerBase() {
		return ticksPerBase;
	}

	/**
	 * @return the minCycle
	 */
	public long getMinCycle() {
		return minCycle;
	}	
	
	/**
	 * @return true if this is a singleton counter (i.e. associated with exactly one alarm)
	 */
	public boolean isSingleton() {
		
		return (targetAlarms.size() == 1);
	}
	
	/**
	 * @return true if the TargetCounter is a nano-second based counter
	 */
	public boolean isNanoSecondCounter() {
		return isNanoSecondCounter;
	}	
	
	/**
	 * @return the invalidDeviceType flag
	 */
	public boolean isInvalidDeviceType() {
		return invalidDeviceType;
	}	
	
	
	/**
	 * 
	 * @return the single {@link TargetAlarm} that the singleton counter is associated with.
	 * @see #isSingleton()
	 */
	public TargetAlarm getSingleAlarm() {
		
		assert ( isSingleton() );
		
		return targetAlarms.iterator().next();
	}
	
	/**
	 * Maps from nano seconds to ticks using the counter's device driver
	 * 
	 * @param nseconds the nano second value that needs to be converted to ticks
	 * @return the number of ticks that represent the given nanoseconds
	 */	
	protected long getTicksFromNanoseconds(long nseconds) {
		
		if (device != null) {
			// ask device's driver for this information
			return getCounterDriver().getTicksFromNanoseconds(device, nseconds);
		}
		
		return nseconds;	// no device driver, so return unmapped value
	}
	
	/**
	 * Maps from a single tick to the equivalent duration in nanoseconds
	 * using the counter's device driver
	 * 
	 * @return the number of nanoseconds that represent a single tick of the counter.
	 */	
	protected long getNanosecondsPerTick() {
		
		if (device != null) {
			// ask device's driver for this information
			return getCounterDriver().getNanosecondsPerTick(device);
		}
		
		return 1;	// no device driver, so return unmapped value
	}	
	
	/**
	 * Sets up the internal associations to referenced target elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
	
		Counter counter = getCounter();
		
		if (counter != null) {
			
			targetAlarms = getAllTargetElements(counter.getAlarms());
			
			// if this is a nano-second counter then need to use the associated device to map
			// the timing attribute values to ticks
			if (isNanoSecondCounter) {
				
				maxAllowedValue = getTicksFromNanoseconds(maxAllowedValue);
				ticksPerBase = getTicksFromNanoseconds(ticksPerBase);
				minCycle = getTicksFromNanoseconds(minCycle);
			}
		}		
	}		
	
	/**
	 * @return Returns the OS Model Counter on which the TargetCounter is based (if any)
	 */
	public Counter getCounter() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Counter;
		
		return (Counter)getOsModelElement();
	}	
	
	
	/**
	 * The {@link TargetDevice} that drives the counter
	 * @return the {@link TargetDevice} that drives the counter
	 */
	public TargetDevice getDevice() {
		return device;
	}
	
	/**
	 * The {@link TargetCounterDriver} that drives the counter's device
	 * 
	 * @return the {@link TargetCounterDriver} that drives the counter's device
	 */
	public TargetCounterDriver getCounterDriver() {
		if ( device != null ) {
			
			TargetDriver driver = device.getDriver();
			
			assert driver instanceof TargetCounterDriver;	// the driver must be a TargetCounterDriver, see constructor
			
			return (TargetCounterDriver)driver;
		}
		
		return null;
	}	
	
	/**
	 * Standard Constructor that creates a TargetCounter that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetCounter(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy constructor that creates a TargetCounter that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetCounter(TargetCpu cpu, Counter osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		maxAllowedValue = osModelElement.getMaxAllowedValue();
		ticksPerBase = osModelElement.getTicksPerBase();
		minCycle = osModelElement.getMinCycle();
		isNanoSecondCounter = osModelElement.isNanoSecondCounter();

		///////////////////////////////////////////////////////
		// setup the TargetDevice that drives the TargetCounter

		try {
			TargetCounterDriver driver = null;
			
			if (osModelElement.isSoftwareCounter() ) {
				// the counter is a software counter, so use the predefined software counter driver

				String driverName = DriverManager.getDriverName(TargetDriver_soft.class);
				
				// get the software counter driver from the manager
				driver = cpu.getDriverManager().getDriver(driverName);	
				
				if ( driver != null ) {
					// driver available, so create the device using the driver name as the device name
					device = driver.createNewDevice("device=soft");	// TODO make this tidier, specify soft driver in a smarter way
					
					device.setHasHandle(false);	// "soft" devices do not have a device handle generated 				
				}				
			}
			else if (osModelElement.isHardwareCounter() ) {
				// the counter is a hardware counter, so get the name of the device from the OS model element
				
				// attempt to get the driver name from the device options
				String driverName = TargetDevice.checkForDriverNameOption(osModelElement.getDeviceOptions());
							
				// get the driver from the manager
				driver = cpu.getDriverManager().getDriver(driverName);
				
				if ( driver != null ) {
					// driver available, so create the device using the device name and device options from the OSModel element.
					device = driver.createNewDevice(osModelElement.getDeviceOptions());
				}
			}
		}
		catch (java.lang.ClassCastException e ) {
			// if this is caught, then the loaded driver was not an appropriate type for this class
			// i.e. not a TargetCounterDriver
			device = null;
			invalidDeviceType = true;	// set flag that identifies device driver was loaded, but of wrong type
		}		
		
		// TODO check for null device in pregen check, then report as error (can't load driver xxxxxx)
		
		if ( device != null ) {
			
			assert device.getDriver() instanceof TargetCounterDriver;	// device must be based on correct driver type
			
			device.setCounter(this);
		}
	}

	/**
	 * Function to determine the complete size of the kernel stack (used for the kernel itself plus
	 * basic tasks). See wiki entry "OfflineKernelStackUsageCalculation" for details (wiki1079).
	 * 
	 * @return the maximum stack usage, in 32-bit stack words, of the kernel stack
	 */
	private long getKernelStackUsage() {
		
		return 0;
	}

	private long getKernelStackUsage(TargetRunnable runnable) {
		
	}
}
