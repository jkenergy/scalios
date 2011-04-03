package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetCounterDriver.java $
 * 
 */

/**
 * Abstract class for all Target Counter Device classes.<br><br>
 * 
 * This class is the abstract super class for all drivers that drive devices related to {@link TargetCounter} instances.
 * 
 * A counter requires exclusive control over its device, thus counter devices that use this type of driver
 * are not sharable.
 * 
 * @author Mark Dixon
 *
 */

public abstract class TargetCounterDriver extends TargetDriver {
	
	// Counter driver function names
	private final static String COUNTER_DRV_NAME = OSAnsiCGenerator.OSPREFIX+"cdevicedrv_";	// prefix for all fn names
	private final static String COUNTER_STOP_FN = "stop";
	private final static String COUNTER_START_FN = "start";
	private final static String COUNTER_DISABLE_INTS_FN = "disable_ints";
	private final static String COUNTER_ENABLE_INTS_FN = "enable_ints";
	private final static String COUNTER_NOW_FN = "now";	
	
	private final static String COUNTER_DRV_CB_TYPE = OSAnsiCGenerator.OSPREFIX+"counter_drivercb";
		
	
	/**
	 * Maps from a single tick to the equivalent duration in nanoseconds
	 * for a particular device
	 * 
	 * @param device the {@link TargetDevice} for which to do the conversion
	 * @return the number of nanoseconds that represent a single tick of the counter device.
	 */
	public abstract long getNanosecondsPerTick(TargetDevice device);


	/**
	 * Maps from nanoseconds to ticks for a particular counter device
	 * 
	 * @see TargetCounterDriver#getNanosecondsPerTick(TargetDevice)
	 * 
	 * @param device the {@link TargetDevice} for which to do the conversion
	 * @param nseconds the nano second value that needs to be converted to ticks
	 * @return the number of ticks that represent the given nanoseconds
	 */
	public long getTicksFromNanoseconds(TargetDevice device, long nseconds) {
		
		// Use driver specific information to determine ratio of ticks to nanoseconds
		return nseconds/getNanosecondsPerTick(device);
	}	
	
	
	@Override
	public final void genCDriverCode() {
		// generate the CB for a counter driver. This is basically a list of function pointers
		// to the driver implementation functions.
		
		// This could actually be overridden within derived concrete driver classes, but there
		// should be no need unless the driver uses a different naming convention for its
		// driver functions.
	
		String driverName = getCDriverName();	// get the driver name to be used within the generated C names
		
		//static const struct os_counter_drivercb os_driver_<index> = { ... };
		write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COUNTER_DRV_CB_TYPE+" ");
		append(getControlBlockName()+"_"+getControlBlockIndex()+" = {");
		incTabs();
		append(verboseComment(" ptrs to stop, start, disable, enable and now driver functions"));
		writeNL();
		
		//////////////////////////////////////////////////////////
		//struct os_counter_drivercb {
		//     void (*stop)(DeviceId);                        		/* Stop the underlying hardware counter from running */
		//     void (*start)(DeviceId);                       		/* Start the underlying hardware counter running; set count to zero if possible (see 11.3 p42 OS spec. 2.2.3) */
		//     void (*disable_ints)(DeviceId);                		/* Stop the counter from interrupting (clear down any pending interrupts; leave counter running) */
		//     void (*enable_ints)(DeviceId, TickType, TickType);	/* Start the counter interrupting rel after a given time 'now' (defined to be in the past); clear down any prior pending interrupt */
		//     TickType (*now)(DeviceId);							/* Time 'now' for the given counter hardware */
		//};	
		
		writeln(COUNTER_DRV_NAME+driverName+"_"+COUNTER_STOP_FN+",");
		writeln(COUNTER_DRV_NAME+driverName+"_"+COUNTER_START_FN+",");
		writeln(COUNTER_DRV_NAME+driverName+"_"+COUNTER_DISABLE_INTS_FN+",");
		writeln(COUNTER_DRV_NAME+driverName+"_"+COUNTER_ENABLE_INTS_FN+",");
		writeln(COUNTER_DRV_NAME+driverName+"_"+COUNTER_NOW_FN);
		
		decTabs();			
		writeln("};");
		
		writeNL();
	}
	
	@Override
	public final boolean requiresDeviceResource(TargetDevice device) {
		// Counter type devices never need device level resources, since counter devices are bound to
		// exactly one counter. "final" is used to prevent overriding in sub-classes.
		return false;
	}		
	
	@Override
	public final boolean supportsSharableDevices() {
		return false; 	// Counter Drivers may NOT share devices, since counter devices are bound to exactly one counter.
	}	
	
	/**
	 * Requires default constructor since instances of this class are created dynamically.
	 * See {@link DriverManager}
	 */
	protected TargetCounterDriver() {
	}
}
