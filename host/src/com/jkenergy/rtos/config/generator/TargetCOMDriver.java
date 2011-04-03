package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetCOMDriver.java $
 * 
 */

/**
 * Abstract base class for all Target COM Device classes.<br><br>
 * 
 * This class is the abstract super class for all drivers that drive devices related to {@link TargetReceivingMessage} instances.
 *  
 * COM messages do not require exclusive use of their devices, e.g. several messages may share a single Ethernet device,
 * thus devices that use this type of driver are sharable.
 * 
 * @author Mark Dixon
 *
 */
public abstract class TargetCOMDriver extends TargetDriver {

	// COM driver function names
	private final static String COM_DRV_NAME = OSAnsiCGenerator.COMPREFIX+"driver_";	// prefix for all fn names
	
	private final static String COM_SEND_QUEUED_FN = "send_queued";
	private final static String COM_RECV_QUEUED_FN = "recv_queued";
	private final static String COM_INIT_RECEIVER_QUEUED_FN = "init_receiver_queued";
	private final static String COM_REINIT_RECEIVER_QUEUED_FN = "reinit_receiver_queued";
	private final static String COM_RECEIVER_STATUS_QUEUED_FN = "status_queued";
	
	private final static String COM_SEND_UNQUEUED_FN = "send_unqueued";
	private final static String COM_RECV_UNQUEUED_FN = "recv_unqueued";
	private final static String COM_INIT_RECEIVER_UNQUEUED_FN = "init_receiver_unqueued";
	private final static String COM_REINIT_RECEIVER_UNQUEUED_FN = "reinit_receiver_unqueued";
	
	private final static String COM_SEND_ZERO_FN = "send_zero";
	private final static String COM_REINIT_RECEIVER_ZERO_FN = "reinit_receiver_zero";
	
	private final static String COM_SEND_STREAM_FN = "send_stream";
	private final static String COM_RECV_STREAM_FN = "recv_stream";
	private final static String COM_INIT_RECEIVER_STREAM_FN = "init_receiver_stream";
	private final static String COM_REINIT_RECEIVER_STREAM_FN	= "reinit_receiver_stream";
	
	private final static String COM_START_DEVICE_FN = "start";
	private final static String COM_STOP_DEVICE_FN = "stop";
		
	private final static String COM_NULL_FN="0";
	
	// Names of "default" COM driver functions
	private final static String COM_DEFAULT_INIT_RECEIVER_DEVICE_FN = COM_DRV_NAME+"default_init_receiver";
	private final static String COM_DEFAULT_RECEIVE_DEVICE_FN = COM_DRV_NAME+"default_recv";
	private final static String COM_DEFAULT_RECEIVE_STREAM_DEVICE_FN = COM_DRV_NAME+"default_recv_stream";
	private final static String COM_DEFAULT_STATUS_DEVICE_FN = COM_DRV_NAME+"default_status";

	
	protected final static String COM_DRV_CB_TYPE = OSAnsiCGenerator.COMPREFIX+"drivercb";
	
	
	/**
	 * Returns true if the device driver requires the use of a {@link TargetResource} when being
	 * used to drive the specified {@link TargetReceivingMessage}.
	 * 
	 * A resource is often required by drivers that need to control access to the shared data structures
	 * of the message, e.g. an IPC driver.
	 * 
	 * @param receiver the {@link TargetReceivingMessage} which uses the COM driver
	 * @return true if the device requires a resource for message access on the final target
	 */
	public abstract boolean requiresMessageResource(TargetReceivingMessage receiver);
	
	/**
	 * Returns true if the device driver provides start_device(DeviceId) and stop_device(DeviceId) driver functions.
	 * 
	 * When a device does not provide start/stop functions then the StartCOM/StopCOM implementations do not even
	 * attempt to call these, improving startup/shutdown speed of the COM layer.
	 * 
	 * @return true if the device provides driver functions for starting and stopping
	 */
	public abstract boolean providesStartStopFunctions();	
	
	
	/**
	 * Helper for {@link #genCDriverCode()}
	 * 
	 * @param suffix
	 * @param sendFn
	 * @param sendZeroFn
	 * @param sendStreamFn
	 * @param rcvStreamFn
	 * @param rcvFn
	 * @param initFn
	 * @param reinitFn
	 * @param statusFn
	 */
	private void genCBCode(String suffix, String sendFn, String sendZeroFn, String sendStreamFn, String rcvStreamFn, String rcvFn, String initFn, String reinitFn, String statusFn) {
		
		//static const struct com_drivercb os_driver_<suffix>_<index> = { ... };
		
		write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_DRV_CB_TYPE+" ");
		append(getControlBlockName()+"_"+suffix+"_"+getControlBlockIndex()+" = {");
		incTabs();
		append(verboseComment(" ptrs to send, send_zero, send stream, recv stream, recv, init_receiver, reinit_receiver and receiver status driver functions"));
		writeNL();
		
		//struct com_drivercb {
		//	StatusType (*send)(com_receiverh, ApplicationDataRef);						/* Send data. Specific receiver message details to operate on; null if not a queued or unqueued type message  */
		//	StatusType (*send_zero)(com_receiverh);										/* Send zero length data. Specific receiver message details to operate on; null if not a zero length type message  */
		//	StatusType (*send_stream)(com_receiverh, ApplicationDataRef, LengthRef);	/* Send a number of bytes; null if not a stream type message */
		//	StatusType (*recv_stream)(com_receiverh, ApplicationDataRef, LengthRef);	/* Receive a number of bytes; points to com_driver_default_recv_stream() if not stream type message  */
		//	StatusType (*recv)(com_receiverh, ApplicationDataRef);						/* Receive message. Copy message into the application data space; points to com_driver_default_recv() if not a queued or unqueued type message  */
		//	StatusType (*init_receiver)(com_receiverh, ApplicationDataRef);				/* Initialise the receiver (in InitMessage()); points com_driver_default_init_receiver() if is a zero length message */
		//	StatusType (*reinit_receiver)(com_receiverh);								/* Reinitialise the receiver (in StartCOM())*/
		//	StatusType (*receiver_status)(com_receiverh);								/* Return status of specified receiver; points to com_driver_default_status() if not a queued type message */
		//};		
		
		writeln(sendFn+",");
		writeln(sendZeroFn+",");
		writeln(sendStreamFn+",");
		writeln(rcvStreamFn+",");
		writeln(rcvFn+",");
		writeln(initFn+",");
		writeln(reinitFn+",");
		writeln(statusFn);		
		
		decTabs();			
		writeln("};");
		
		writeNL();		
	}
	
	/**
	 * Returns the name of the device start function provided by the driver.
	 * Callers should not use this function name unless {@link #providesStartStopFunctions()} == true
	 * 
	 * @see #providesStartStopFunctions()
	 * 
	 * @return the name of the device start function provided by the driver
	 */
	public String getStartFnName() {
		assert providesStartStopFunctions() == true;	// name not valid if driver does not provide start
		
		// com_driver_<driverName>_start
		return COM_DRV_NAME+getName()+"_"+COM_START_DEVICE_FN;
	}
	
	/**
	 * Returns the name of the device stop function provided by the driver.
	 * Callers should not use this function name unless {@link #providesStartStopFunctions()} == true
	 * 
	 * @see #providesStartStopFunctions()
	 * 
	 * @return the name of the device stop function provided by the driver
	 */
	public String getStopFnName() {
		assert providesStartStopFunctions() == true;	// name not valid if driver does not provide stop
		
		// com_driver_<driverName>_stop
		return COM_DRV_NAME+getName()+"_"+COM_STOP_DEVICE_FN;
	}	
	
	
	@Override
	public final void genCDriverCode() {
		// generate the CB for a COM driver. This is basically a list of function pointers
		// to the driver implementation functions.
		
		// This could actually be overridden within derived concrete driver classes, but there
		// should be no need unless the driver uses a different naming convention for its
		// driver functions.
		
		String driverName = COM_DRV_NAME+getName()+"_";
				
		if (drivesQueuedMessages()) {
			// this driver is used for queued messages, so point to appropriate functions
			writeln(comment("'"+getName()+"' COM driver for queued messages"));
			genCBCode(
					OSAnsiCGenerator.COM_QUEUED_DRV_NAME,
					driverName+COM_SEND_QUEUED_FN,				// send
					COM_NULL_FN,								// send_zero
					COM_NULL_FN,								// send_stream
					COM_DEFAULT_RECEIVE_STREAM_DEVICE_FN,		// rcv_stream
					driverName+COM_RECV_QUEUED_FN,				// rcv
					driverName+COM_INIT_RECEIVER_QUEUED_FN,		// init
					driverName+COM_REINIT_RECEIVER_QUEUED_FN,	// reinit
					driverName+COM_RECEIVER_STATUS_QUEUED_FN	// status
					);
		}
			
		if ( drivesUnqueuedMessages() ) {
			// this driver is used for unqueued messages, so point to appropriate functions
			writeln(comment("'"+getName()+"' COM driver for unqueued messages"));
			genCBCode(
					OSAnsiCGenerator.COM_UNQUEUED_DRV_NAME,
					driverName+COM_SEND_UNQUEUED_FN,			// send
					COM_NULL_FN,								// send_zero
					COM_NULL_FN,								// send_stream
					COM_DEFAULT_RECEIVE_STREAM_DEVICE_FN,		// rcv_stream
					driverName+COM_RECV_UNQUEUED_FN,			// rcv
					driverName+COM_INIT_RECEIVER_UNQUEUED_FN,	// init
					driverName+COM_REINIT_RECEIVER_UNQUEUED_FN,	// reinit
					COM_DEFAULT_STATUS_DEVICE_FN				// status
					);
		}
		
		if (drivesZeroLengthMessages() ) {
			// this driver is used for zero length messages, so point to appropriate functions
			writeln(comment("'"+getName()+"' COM driver for zero-length messages"));
			genCBCode(
					OSAnsiCGenerator.COM_ZERO_LENGTH_DRV_NAME,
					COM_NULL_FN,								// send
					driverName+COM_SEND_ZERO_FN,				// send_zero
					COM_NULL_FN,								// send_stream
					COM_DEFAULT_RECEIVE_STREAM_DEVICE_FN,		// rcv_stream
					COM_DEFAULT_RECEIVE_DEVICE_FN,				// rcv
					COM_DEFAULT_INIT_RECEIVER_DEVICE_FN,		// init
					driverName+COM_REINIT_RECEIVER_ZERO_FN,		// reinit
					COM_DEFAULT_STATUS_DEVICE_FN				// status
					);
		}
		
		if (drivesStreamMessages() ) {
			// this driver is used for stream messages, so point to appropriate functions
			writeln(comment("'"+getName()+"' COM driver for stream messages"));
			genCBCode(
					OSAnsiCGenerator.COM_STREAM_DRV_NAME,
					COM_NULL_FN,								// send
					COM_NULL_FN,								// send_zero
					driverName+COM_SEND_STREAM_FN,				// send_stream
					driverName+COM_RECV_STREAM_FN,				// rcv_stream
					COM_DEFAULT_RECEIVE_DEVICE_FN,				// rcv
					driverName+COM_INIT_RECEIVER_STREAM_FN,		// init
					driverName+COM_REINIT_RECEIVER_STREAM_FN,	// reinit
					COM_DEFAULT_STATUS_DEVICE_FN				// status
					);
		}
	}	
	
	@Override
	public final boolean supportsSharableDevices() {
		return true; 	// COM Drivers may share devices, e.g. several messages may use the "CAN1" device
	}	
	
	/**
	 * Requires default constructor since instances of this class are created dynamically.
	 * See {@link DriverManager}
	 */
	protected TargetCOMDriver() {
	}
}
