package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetDriver_IPC.java $
 * 
 */

/**
 * A device driver for internal COM messages.
 * 
 * This class provides code generation specifically tailored to internal COM messages.
 * 
 * @author Mark Dixon
 *
 */

public class TargetDriver_IPC extends TargetCOMDriver {

	private final static String PREFIX=OSAnsiCGenerator.COMPREFIX;
	
	private final static String COM_DEVICE_CB_TYPE = PREFIX+"devicecb_ipc";	
	
	// Type Names of control blocks defined within the IPC based COM layer driver
	private final static String COM_QUEUE_CB_TYPE = PREFIX+"internal_queued_receivercb";
	private final static String COM_QUEUE_DYN_CB_TYPE = PREFIX+"queuecb_dyn";
	private final static String COM_UNQUEUE_CB_TYPE = PREFIX+"internal_unqueued_receivercb";
	private final static String COM_ZERO_CB_TYPE = PREFIX+"internal_zero_receivercb";
	private final static String COM_STREAM_CB_TYPE = PREFIX+"internal_stream_receivercb";
	private final static String COM_STREAM_DYN_CB_TYPE = PREFIX+"stream_queuecb_dyn";
	
	// Names of variables defined within the IPC based COM driver
	private final static String COM_BUFFER_NAME = PREFIX+"buffer";
	private final static String COM_QUEUE_DYN_NAME = PREFIX+"queue_dyn";
	private final static String COM_STREAM_DYN_NAME = PREFIX+"stream_dyn";
	
	/**
	 * Generates the C initialisation code for IPC devices.
	 * 
	 * For each TargetReceivingMessage that uses the specified device, generates:
	 *  
	 * 1. The RAM based buffer space for non-zero length receiving message
	 * 2. The RAM based dynamic queue control blocks for each queued message
	 * 3. The ROM based control blocks for queued messages
	 * 4. The ROM based control blocks for unqueued messages
	 * 5. The ROM based control blocks for zero length messages
	 * 6. The RAM based dynamic buffer control blocks for each stream message 
	 * 7. The ROM based control blocks for stream messages
	 */
	@Override
	public void genCInitCode(TargetDevice device) {
			
		writeln(comment("Device CB for IPC COM device '"+device.getName()+"'"));
		
		////////////////////////////////////////////////////////////////////////////////
		// 0. Generate pseudo device cb, since IPC does not actually have a device
		
		//static const struct os_devicecb_ipc os_device_<index> = { };
		write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_DEVICE_CB_TYPE+" "+OSAnsiCGenerator.DEVICE_CB_NAME+"_"+device.getControlBlockIndex()+" = {};");
		append(verboseComment("'IPC' provides a pseudo device only"));
		writeNLs(2);
		
		// Iterate over each TargetReceivingMessage that uses the device
		for ( TargetReceivingMessage receiver : device.getMessages() ) {
			
			////////////////////////////////////////////////////////////////////////////////
			// 1. Generate RAM based buffer space for non-zero length receiving messages		
			
			if ( !receiver.isZeroLengthMessage() ) {
				
				// Declare the RAM based buffer space for each non-zero length receiving message
				
				if ( receiver.isQueuedMessage() ) {
					// queued message, so need to multiply created buffer space by queue size
					// static uint8 com_buffer_<messageIndex>[sizeof(<dataType>)*<queueSize>U];
					write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.UINT8+" "+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[sizeof("+receiver.getDataTypeName()+")*"+receiver.getQueueSize()+"U];");
					append(verboseComment("Buffer for queued message "+receiver.getName()));
				}
				else if ( receiver.isUnqueuedMessage() ) {
					// unqueued, so just need to reserve enough space for single piece of data
					// static uint8 com_buffer_<messageIndex>[sizeof(<dataType>)];
					write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.UINT8+" "+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[sizeof("+receiver.getDataTypeName()+")];");
					append(verboseComment("Buffer for unqueued message "+receiver.getName()));
				}
				else if ( receiver.isStreamMessage() ) {
					// stream message, so need to reserve buffer space 
					// static uint8 com_buffer_<messageIndex>[<bufferSize>U];
					write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.UINT8+" "+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"["+receiver.getBufferSize()+"U];");
					append(verboseComment("Buffer for stream message "+receiver.getName()));
				}
				
				writeNLs(2);
			}
			
			
			///////////////////////////////////////////////////////////////////////////////////////
			// 2. Generate RAM based dynamic queue control blocks for each queued message	
			
			if (receiver.isQueuedMessage()) {
				// Define the RAM based dynamic control blocks for queued receiving messages
				
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.STRUCT+" "+COM_QUEUE_DYN_CB_TYPE+" "+COM_QUEUE_DYN_NAME+"_"+receiver.getControlBlockIndex()+";");
				append(verboseComment("Dyn queue CB for queued message "+receiver.getName()));
				writeNLs(2);			
			}
	
			////////////////////////////////////////////////////////////////////////////
			// 3. Generate ROM based control blocks if queued receiving message	
			
			if (receiver.isQueuedMessage()) {
				// Define the ROM based (driver specific) control blocks for queued receiving messages
				
				//struct com_internal_queued_receivercb {
				//  COMLengthType message_size;						/* Equal to sizeof(message-type-specified-in-OIL-file); */
				//	uint8 *first;									/* Pointer to the first byte of the first element in queue */
				//	uint8 *last;									/* Pointer to the first byte of the last element in the buffer space; element of size "message_size" */
				//	uint16 num_slots;								/* Total number of slots in the buffer space */
				//	struct com_queuecb_dyn *dyn;					/* Dynamic part of message queue control block */
				//	enum com_notify_action notify_action;
				//	const struct com_notifycb *notify_data;			/* Details of what type of notification $Req: artf1246 $ */
				//	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */	
				//	ResourceType guard;								/* Guards access to this message's buffer/queue */
				//};				
				
				// static const struct com_internal_queued_receivercb rcvdrvmsgs_<messageIndex> = { ... };
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_QUEUE_CB_TYPE+" "+OSAnsiCGenerator.COM_RECEIVE_DRV_CB_NAME+"_"+receiver.getControlBlockIndex()+" = {");
				incTabs();	
				append(comment("Control block for "+receiver.getName()+" queued receiving message"));
				writeNL();
				
				// COMLengthType message_size;		
				write("sizeof("+receiver.getDataTypeName()+"),");	// output the sizeof(<dataTypeName>) info.
				append(verboseComment("COMLengthType message_size; Message size"));
				writeNL();				
				
				// uint8 *first;
				write("&"+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[0],");
				append(verboseComment("uint8 *first; Pointer to the first byte of receive buffer"));
				writeNL();
				
				// uint8 *last;
				write("&"+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[sizeof("+receiver.getDataTypeName()+")*"+(receiver.getQueueSize()-1)+"U],");
				append(verboseComment("uint8 *last; Pointer to the first byte of the last element in the buffer space"));
				writeNL();					
				
				// uint16 num_slots;
				write(receiver.getQueueSize()+"U,");
				append(verboseComment("uint16 num_slots; number of slots in the buffer space"));
				writeNL();				
				
				// struct com_queuecb_dyn *dyn;
				write("&"+COM_QUEUE_DYN_NAME+"_"+receiver.getControlBlockIndex()+",");
				append(verboseComment("struct com_queuecb_dyn *dyn; Dynamic part of message queue"));
				writeNL();			
			
				// enum com_notify_action notify_action;				
				write(OSAnsiCGenerator.getNotificationEnumString(receiver.getNotificationAction())+",");
				append(verboseComment("enum com_notify_action notify_action; Type of notification"));
				writeNL();
	
				// const struct com_notifycb *notify_data;
				write(OSAnsiCGenerator.getCOMNotificationFunctionDataRef(receiver.getNotificationAction())+",");
				append(verboseComment("const struct com_notifycb *notify_data;"));			
				writeNL();				
				
				// FlagValue *flag;				
				if ( receiver.setsFlag() ) {
					write("&"+OSAnsiCGenerator.COM_FLAG_NAME+"_"+getTargetCpu().getFlagNameMap().get(receiver.getFlagName())+",");
					append(verboseComment("FlagValue *flag; Point to associated flag set on receipt notification"));					
				}
				else {
					// next message notification does not set flag, so point to shared scratch flag
					write("&"+OSAnsiCGenerator.COM_SCRATCH_FLAG_NAME+",");
					append(verboseComment("FlagValue *flag; Use shared flag, since no set flag notification"));
				}
				writeNL();
				
				// ResourceType guard;
				write(OSAnsiCGenerator.getTargetElementArrayReference(receiver.getTargetResource()));				
				append(verboseComment("ResourceType guard; Guards access to this message's data queue"));
				writeNL();
				
				decTabs();			
				writeln("};");
				writeNL();
			}
	
			
			////////////////////////////////////////////////////////////////////////////
			// 4. Generate ROM based control blocks for unqueued receiving message
			
			if (receiver.isUnqueuedMessage()) {
				// Define the ROM based (driver specific) control blocks for unqueued receiving messages
				
				//struct com_internal_unqueued_receivercb {
				//  COMLengthType message_size;						/* Equal to sizeof(message-type-specified-in-OIL-file); */
				//	uint8 *init;									/* Initial data for message (valid if message is of size <= 64 bits) */
				//	uint8 *msg_data;								/* Pointer to message data element */
				//	enum com_notify_action notify_action;
				//	const struct com_notifycb *notify_data;			/* Details of what type of notification $Req: artf1246 $ */
				//	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */	
				//	ResourceType guard;								/* Guards access to this message's buffer/queue */
				//};				
	
				// static const struct com_internal_unqueued_receivercb rcvdrvmsgs_<messageIndex> = { ... };
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_UNQUEUE_CB_TYPE+" "+OSAnsiCGenerator.COM_RECEIVE_DRV_CB_NAME+"_"+receiver.getControlBlockIndex()+" = {");
				incTabs();	
				append(comment("Control block for "+receiver.getName()+" unqueued receiving message"));
				writeNL();
				
				// COMLengthType message_size;		
				write("sizeof("+receiver.getDataTypeName()+"),");	// output the sizeof(<dataTypeName>) info.
				append(verboseComment("COMLengthType message_size; Message size"));
				writeNL();				
				
				// const uint8 *init;
				if ( receiver.isInitialisedMessage() ) {
					write("("+OSAnsiCGenerator.CONST+" "+OSAnsiCGenerator.UINT8+" *)&"+OSAnsiCGenerator.COM_INIT_VALUE_NAME+"_"+receiver.getControlBlockIndex()+",");
					append(verboseComment("const uint8 *init; Ptr to initial data for unqueued message"));
				}
				else {
					write("0,");
					append(verboseComment("uint8 *init; Ptr to initial data for message. Null since message not initialised"));					
				}
				writeNL();				
				
				// uint8 *msg_data;
				write("&"+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[0],");
				append(verboseComment("uint8 *msg_data; Pointer to message data element"));
				writeNL();
			
				// enum com_notify_action notify_action;					
				write(OSAnsiCGenerator.getNotificationEnumString(receiver.getNotificationAction())+",");
				append(verboseComment("enum com_notify_action notify_action; Type of notification"));
				writeNL();
	
				// const struct com_notifycb *notify_data;
				write(OSAnsiCGenerator.getCOMNotificationFunctionDataRef(receiver.getNotificationAction())+",");
				append(verboseComment("const struct com_notifycb *notify_data;"));			
				writeNL();				
				
				// FlagValue *flag;				
				if ( receiver.setsFlag() ) {
					write("&"+OSAnsiCGenerator.COM_FLAG_NAME+"_"+getTargetCpu().getFlagNameMap().get(receiver.getFlagName())+",");
					append(verboseComment("FlagValue *flag; Point to associated flag set on receipt notification"));					
				}
				else {
					// next message notification does not set flag, so point to shared scratch flag
					write("&"+OSAnsiCGenerator.COM_SCRATCH_FLAG_NAME+",");
					append(verboseComment("FlagValue *flag; Use shared flag, since no set flag notification"));
				}
				writeNL();
				
				// ResourceType guard;
				write(OSAnsiCGenerator.getTargetElementArrayReference(receiver.getTargetResource()));				
				append(verboseComment("ResourceType guard; Guards access to this message's data buffer"));
				writeNL();
				
				decTabs();			
				writeln("};");
				writeNL();
			}
			
			///////////////////////////////////////////////////////////////////////////////
			// 5. Generate ROM based control blocks for each zero length receiving message
			
			if ( receiver.isZeroLengthMessage() ) {
				
				// Define the ROM based (driver specific) control blocks for zero length receiving messages
						
				//struct com_internal_zero_receivercb {				
				//	enum com_notify_action notify_action;
				//	const struct com_notifycb *notify_data;			/* Details of what type of notification $Req: artf1246 $ */
				//	FlagValue *flag;								/* Point to associated flag; always valid (if not flag defined, points to scratch word) */		
				//};			
	
				// static const struct com_internal_zero_receivercb rcvdrvmsgs_<messageIndex> = { ... };
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_ZERO_CB_TYPE+" "+OSAnsiCGenerator.COM_RECEIVE_DRV_CB_NAME+"_"+receiver.getControlBlockIndex()+" = {");
				incTabs();	
				append(comment("Control block for "+receiver.getName()+" zero length receiving message"));
				writeNL();
				
				// enum com_notify_action notify_action;				
				write(OSAnsiCGenerator.getNotificationEnumString(receiver.getNotificationAction())+",");
				append(verboseComment("enum com_notify_action notify_action; Type of notification"));
				writeNL();
	
				// const struct com_notifycb *notify_data;
				write(OSAnsiCGenerator.getCOMNotificationFunctionDataRef(receiver.getNotificationAction())+",");
				append(verboseComment("const struct com_notifycb *notify_data;"));			
				writeNL();				
				
				// FlagValue *flag;				
				if ( receiver.setsFlag() ) {
					write("&"+OSAnsiCGenerator.COM_FLAG_NAME+"_"+getTargetCpu().getFlagNameMap().get(receiver.getFlagName()));
					append(verboseComment("FlagValue *flag; Point to associated flag set on receipt notification"));					
				}
				else {
					// next message notification does not set flag, so point to shared scratch flag
					write("&"+OSAnsiCGenerator.COM_SCRATCH_FLAG_NAME);
					append(verboseComment("FlagValue *flag; Use shared flag, since no set flag notification"));
				}
				writeNL();
				
				// ResourceType guard;
				//write(OSAnsiCGenerator.getTargetElementArrayReference(receiver.getTargetResource()));				
				//append(verboseComment("ResourceType guard; Guards access to this message's buffer/queue"));
				//writeNL();
				
				decTabs();			
				writeln("};");
				writeNL();
			}
			
			///////////////////////////////////////////////////////////////////////////////////////
			// 6. Generate RAM based dynamic buffer control blocks for each stream message	

			if (receiver.isStreamMessage()) {
				// Define the RAM based dynamic control blocks for stream receiving messages
				
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.STRUCT+" "+COM_STREAM_DYN_CB_TYPE+" "+COM_STREAM_DYN_NAME+"_"+receiver.getControlBlockIndex()+";");
				append(verboseComment("Dyn CB for stream message "+receiver.getName()));
				writeNLs(2);				
			}			
			
			///////////////////////////////////////////////////////////////////////////////
			// 7. Generate ROM based control blocks for each stream receiving message
			
			if ( receiver.isStreamMessage() ) {
				
				// Define the ROM based (driver specific) control blocks for stream receiving messages
				
				//struct com_internal_stream_receivercb {
				//	uint8 *first;									/* Pointer to the first byte of the allocted buffer */
				//	uint8 *last;									/* Pointer to the last byte of the allocated buffer + 1 (last - first = size of buffer) */
				//	COMLengthType size;								/* Size of the allocated buffer (@TODO equal to last - first) */
				//	struct com_stream_queuecb_dyn *dyn;				/* Dynamic part of ring buffer management */
				//	enum com_notify_action low_notify_action;		/* Notification when size in buffer crosses high threshold */
				//	enum com_notify_action high_notify_action;		/* Notification when size in buffer drops below low threshold */
				//	const struct com_notifycb *low_notify_data;		/* Details of what type of notification */
				//	const struct com_notifycb *high_notify_data;	/* Details of what type of notification */
				//	FlagValue *low_flag;							/* Flag to set when receiving */
				//	FlagValue *high_flag;							/* Flag to set when sending */
				//	COMLengthType low_threshold;					/* When amount of data in buffer drops below low_threshold in a 'receive' call, process notification */
				//	COMLengthType high_threshold;					/* When amount of data in buffer goes above high_threshold in a 'send' call, process notification */
				//	ResourceType guard;
				//};
				
				// static const struct com_internal_stream_receivercb rcvdrvmsgs_<messageIndex> = { ... };
				write(OSAnsiCGenerator.STATIC+" "+OSAnsiCGenerator.CONST_STRUCT+" "+COM_STREAM_CB_TYPE+" "+OSAnsiCGenerator.COM_RECEIVE_DRV_CB_NAME+"_"+receiver.getControlBlockIndex()+" = {");
				incTabs();	
				append(comment("Control block for "+receiver.getName()+" stream receiving message"));
				writeNL();
				
				// uint8 *first;
				write("&"+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"[0],");
				append(verboseComment("uint8 *first; Pointer to the first byte of the allocted buffer"));
				writeNL();
				
				// uint8 *last;
				write("&"+COM_BUFFER_NAME+"_"+receiver.getControlBlockIndex()+"["+receiver.getBufferSize()+"U],");
				append(verboseComment("uint8 *last; Pointer to the last byte of the allocated buffer + 1 (last - first = size of buffer)"));
				writeNL();					
				
				//COMLengthType size;
				write(receiver.getBufferSize()+"U,");
				append(verboseComment("COMLengthType size; Size of the allocated buffer"));
				writeNL();				
				
				// struct com_stream_queuecb_dyn *dyn;
				write("&"+COM_STREAM_DYN_NAME+"_"+receiver.getControlBlockIndex()+",");
				append(verboseComment("struct com_stream_queuecb_dyn *dyn; Dynamic part of ring buffer management"));
				writeNL();			
			
				// enum com_notify_action low_notify_action;				
				write(OSAnsiCGenerator.getNotificationEnumString(receiver.getLowNotificationAction())+",");
				append(verboseComment("enum com_notify_action low_notify_action; Type of low threshold notification"));
				writeNL();
	
				// enum com_notify_action high_notify_action;				
				write(OSAnsiCGenerator.getNotificationEnumString(receiver.getNotificationAction())+",");
				append(verboseComment("enum com_notify_action high_notify_action; Type of high threshold notification"));
				writeNL();				
				
				// const struct com_notifycb *low_notify_data;
				write(OSAnsiCGenerator.getCOMNotificationFunctionDataRef(receiver.getLowNotificationAction())+",");
				append(verboseComment("const struct com_notifycb *low_notify_data;"));			
				writeNL();				
				
				// const struct com_notifycb *high_notify_data;
				write(OSAnsiCGenerator.getCOMNotificationFunctionDataRef(receiver.getNotificationAction())+",");
				append(verboseComment("const struct com_notifycb *high_notify_data;"));			
				writeNL();				
				
				// FlagValue *low_flag;				
				if ( receiver.lowSetsFlag() ) {
					write("&"+OSAnsiCGenerator.COM_FLAG_NAME+"_"+getTargetCpu().getFlagNameMap().get(receiver.getLowFlagName())+",");
					append(verboseComment("FlagValue *low_flag; Point to associated flag set on low threshold notification"));					
				}
				else {
					// next message notification does not set low threshold flag, so point to shared scratch flag
					write("&"+OSAnsiCGenerator.COM_SCRATCH_FLAG_NAME+",");
					append(verboseComment("FlagValue *low_flag; Use shared flag, since no low threshold flag notification"));
				}
				writeNL();
				
				// FlagValue * high_flag;				
				if ( receiver.setsFlag() ) {
					write("&"+OSAnsiCGenerator.COM_FLAG_NAME+"_"+getTargetCpu().getFlagNameMap().get(receiver.getFlagName())+",");
					append(verboseComment("FlagValue *high_flag; Point to associated flag set on high threshold notification"));					
				}
				else {
					// next message notification does not set high threshold flag, so point to shared scratch flag
					write("&"+OSAnsiCGenerator.COM_SCRATCH_FLAG_NAME+",");
					append(verboseComment("FlagValue *high_flag; Use shared flag, since no high threshold flag notification"));
				}
				writeNL();				
				
				// ResourceType guard;
				write(OSAnsiCGenerator.getTargetElementArrayReference(receiver.getTargetResource()));				
				append(verboseComment("ResourceType guard; Guards access to this stream message's ring buffer"));
				writeNL();
				
				decTabs();			
				writeln("};");
			}
			
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
	
	@Override
	public boolean requiresMessageResource(TargetReceivingMessage receiver) {
		// An IPC device driver needs resources for queued, unqueued and stream messages.
		// Zero length messages have no data buffer and therefore access control is not required.
		return (receiver.isQueuedMessage() || receiver.isUnqueuedMessage() || receiver.isStreamMessage() ) ? true : false;
	}
	
	@Override
	public boolean requiresDeviceResource(TargetDevice device) {
		// An IPC device driver never needs device level resources, since the IPC implementation uses a psuedo device.
		return false;
	}
	
	@Override
	public boolean providesStartStopFunctions() {
		// IPC driver does not provide start/stop functions for a device, since IPC only uses a psuedo device
		return false;
	}
	
	
	/* Uncomment if IPC provides a ctl driver called com_driver_IPC_ctl()
	@Override
	public String getCtlFnName() {
	
		return COM_DRV_NAME+getName()+"_ctl";	// default to: com_driver_IPC_ctl()
	}
	*/	
	
	/**
	 * Returns the name to #include for this device 
	 * Overridden as an example
	 * @return the textual name used to include 
	 */
	@Override
	public String getIncludeName() {
		return "internal.h";
	}	
	
	public TargetDriver_IPC() {

	}
}
