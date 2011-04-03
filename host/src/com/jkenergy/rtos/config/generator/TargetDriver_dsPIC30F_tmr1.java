package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetDriver_dsPIC30F_tmr1.java $
 * 
 */

/**
 * A device driver for dsPIC30F tmr1.
 * 
 * This class provides code generation specifically tailored to the tmr1 dsPIC device.
 * 
 * @author Mark Dixon
 *
 */
public class TargetDriver_dsPIC30F_tmr1 extends TargetCounterDriver {

	private final static String TIMER_NAME = "timer1";
	// Type Names of control blocks defined within the timer1 based counter driver
	private final static String COUNTER_TIMER_DEVICE_CB_TYPE = OSAnsiCGenerator.DEVICE_CB_TYPE+"_"+TIMER_NAME;	
	
	/**
	 * 
	 * @return the embedded C name of target driver to be used within driver function names.
	 * 
	 * See dsPIC/drivers/timer1.h for the embedded driver
	 */
	@Override
	String getCDriverName() {
		
		return TIMER_NAME;
	}
	
	@Override
	public void genCInitCode(TargetDevice device) {
		// get the single TargetCounter that uses the specified device
		TargetCounter counter = device.getCounter();
			
		if ( counter != null ) {
			int index = device.getControlBlockIndex();
	
			writeln(comment("\""+TIMER_NAME+"\" counter device control blocks, used by counter '"+counter.getName()+"'"));		
			
			////////////////////////////////////////////////////////////////////////////////
			// 1. Generate RAM based dynamic control block for the associated counter device			
			// No dynamic control block required for this driver.
			
			////////////////////////////////////////////////////////////////////////////////
			// 2. Generate ROM based control block for the associated counter device		
			
			// static const struct os_devicecb_timer1 os_device_<index> = {
			//	uint16 PR1_init;
			//	uint16 T1CON_init;
			//	CounterType this_counter;			
			//}
		
			write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COUNTER_TIMER_DEVICE_CB_TYPE+" "+OSAnsiCGenerator.DEVICE_CB_NAME+"_"+index+" = { ");
			incTabs();
			writeNL();
			write("0U,");	// TODO output PR1_init
			append(verboseComment("uint16 PR1_init;"));
			writeNL();			
			write("0U,");
			append(verboseComment("uint16 T1CON_init;"));
			writeNL();			
			write(OSAnsiCGenerator.getTargetElementArrayReference(counter));
			append(verboseComment("CounterType this_counter;"));
			writeNL();				
			decTabs();
			writeln("};");
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

	/* Uncomment if driver provides a ctl driver called com_driver_dsPIC30F_tmr1_ctl()
	@Override
	public String getCtlFnName() {
	
		return COM_DRV_NAME+getName()+"_ctl";	// default to: com_driver_dsPIC30F_tmr1_ctl()
	}
	*/		
	

	@Override
	public long getNanosecondsPerTick(TargetDevice device) {
		
		// TODO need to work on this! For H/W counter Will probably use the options in the TargetDevice
		// to determine the mapping that needs to be done.			
		
		return 1;
	}

}
