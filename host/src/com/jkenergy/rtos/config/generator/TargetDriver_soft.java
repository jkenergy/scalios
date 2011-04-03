package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-03-08 01:43:53 +0000 (Sat, 08 Mar 2008) $
 * $LastChangedRevision: 669 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetDriver_soft.java $
 * 
 */


/**
 * A device driver for software counters.
 * 
 * This class provides code generation specifically tailored to software counters.
 * 
 * @author Mark Dixon
 *
 */

public class TargetDriver_soft extends TargetCounterDriver {

	// Type Names of control blocks defined within the software based counter driver
	private final static String COUNTER_SOFT_DEVICE_CB_TYPE = OSAnsiCGenerator.DEVICE_CB_TYPE+"_soft";
	private final static String COUNTER_SOFT_DEVICE_DYN_CB_TYPE = OSAnsiCGenerator.DEVICE_CB_TYPE+"_soft_dyn";

	// Names of variables defined within the software based counter driver
	private final static String COUNTER_SOFT_DEVICE_DYN_NAME = OSAnsiCGenerator.DEVICE_CB_NAME+"_soft_dyn";
	
	
	@Override
	public long getNanosecondsPerTick(TargetDevice device) {
		
		// TODO need to work on this! For H/W counter Will probably use the options in the TargetDevice
		// to determine the mapping that needs to be done.		
				
		return 1;
	}	
	
	@Override
	public void genCInitCode(TargetDevice device) {
				
		// get the single TargetCounter that uses the specified device
		TargetCounter counter = device.getCounter();
		
		if ( counter != null ) {
			int index = device.getControlBlockIndex();
	
			writeln(comment("\"soft\" counter device control blocks, used by counter '"+counter.getName()+"'"));		
			
			////////////////////////////////////////////////////////////////////////////////
			// 1. Generate RAM based dynamic control block for the associated counter device			
			
			// static struct os_devicecb_soft_dyn os_device_soft_dyn_<index>;
			// no initialisation required for the soft counter dyn
			writeln(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.STRUCT+" "+COUNTER_SOFT_DEVICE_DYN_CB_TYPE+" "+COUNTER_SOFT_DEVICE_DYN_NAME+"_"+index+";");
			
			////////////////////////////////////////////////////////////////////////////////
			// 2. Generate ROM based control block for the associated counter device		
			
			// static const struct os_devicecb_soft os_device_<index> = { &os_device_soft_dyn_<index> , <maxallowedvalue>};		
			write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COUNTER_SOFT_DEVICE_CB_TYPE+" "+OSAnsiCGenerator.DEVICE_CB_NAME+"_"+index+" = { ");
			append("&"+COUNTER_SOFT_DEVICE_DYN_NAME+"_"+index+","+counter.getMaxAllowedValue()+"U};");
			writeNLs(2);
		}
	}

	@Override
	public void genCHeaderCode(TargetDevice device) {
		// no header file code for this driver
	}	

	@Override
	public void genAssemblyCode(TargetDevice device) {
		// no assembly language code for this driver
	}

	/* Uncomment if soft driver provides a ctl driver called com_driver_soft_ctl()
	@Override
	public String getCtlFnName() {
	
		return COM_DRV_NAME+getName()+"_ctl";	// default to: com_driver_soft_ctl()
	}
	*/	
	
	
	/**
	 * Returns the name to #include for this device 
	 * Overridden since the soft device driver is part of the core OS, so no #include file is required.
	 * 
	 * @return the textual name used to include, null for this device. 
	 */
	@Override
	public String getIncludeName() {
		return null;
	}	
	
	public TargetDriver_soft() {

	}
}
