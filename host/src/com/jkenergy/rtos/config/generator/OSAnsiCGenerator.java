package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/OSAnsiCGenerator.java $
 * 
 */


import java.io.*;
import java.math.BigInteger;
import java.util.*;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

/**
 * An abstract SubClass of the OSGenerator specifically designed as a super class to generator classes which
 * are required to generate for the 'C' Language.<br><br>
 * 
 * This class provides a large amount of both target independent, and target dependent 'C' code generation.<br><br>
 * 
 * Although target dependent code is generated, it is achieved using parameters specified during instantiation, thus
 * different targets may all share this single implementation for the majority of code generation. Only very specific
 * code generation needs to be handled by the concrete generator classes, such as control block instantiation for target
 * specific devices. 
 * 
 * @author Mark Dixon
 *
 */
public abstract class OSAnsiCGenerator extends OSGenerator {

	// Constants common for all Ansi-C based generation
	
	protected final static String OSPREFIX="os_";
	protected final static String COMPREFIX="com_";
	
	private static String COMMENT_START = "/*";		// Comment start delimiter for "C" generators
	private static String COMMENT_END = "*/";		// Comment end delimiter for "C" generators
	
	// separator comment line
	private final static String SEP_COMMENT = "/****************************************************************/";
		
	// Names of files to be generated
	protected final static String GEN_C_FILE = "osgen.c";
	protected final static String GEN_HANDLES_FILE = "oshandles.h";

	// Names of files to be included
	protected final static String OS_INCLUDE_FILE = "osgen.h";
	
	// Entry function prefix names
	protected final static String TASK_ENTRY_FN = OSPREFIX+"taskentry_";
	protected final static String ISR_ENTRY_FN = OSPREFIX+"isrentry_";
	
	// Names of MACROs used to declare task/isr/callback entry functions.
	protected final static String TASK_PROTO = "TASK_PROTO";
	protected final static String ISR_PROTO = "ISR_PROTO";
	protected final static String ALARMCALLBACK_PROTO = "ALARMCALLBACK";
	protected final static String ALARMCALLBACK_PREFIX = OSPREFIX+"alarmcallback_";
	
	// Type Names of control blocks defined within the RTOS
	protected final static String TASK_CB_TYPE = OSPREFIX+"taskcb";
	protected final static String TASK_DYN_CB_TYPE = OSPREFIX+"ext_taskcb_dyn";
	protected final static String ISR_CB_TYPE = OSPREFIX+"isrcb";
	protected final static String APPMODE_CB_TYPE = OSPREFIX+"appmodecb";
	protected final static String RESOURCE_CB_TYPE = OSPREFIX+"rescb";
	protected final static String RESOURCE_DYN_CB_TYPE = OSPREFIX+"rescb_dyn";
	protected final static String QUEUE_CB_TYPE = OSPREFIX+"queuecb";
	protected final static String QUEUE_DYN_CB_TYPE = OSPREFIX+"queuecb_dyn";
	protected final static String IPL_TYPE = OSPREFIX+"ipl";
	protected final static String FLAGS_TYPE = OSPREFIX+"flags";
	protected final static String COUNTER_CB_TYPE = OSPREFIX+"countercb";
	protected final static String COUNTER_DYN_CB_TYPE = OSPREFIX+"countercb_dyn";
	protected final static String SCHEDTAB_CB_TYPE = OSPREFIX+"schedtabcb";
	protected final static String SCHEDTAB_DYN_CB_TYPE = OSPREFIX+"schedtabcb_dyn";
	protected final static String EXPIRY_CB_TYPE = OSPREFIX+"xpoint";
	protected final static String AUTO_ALARM_CB_TYPE = OSPREFIX+"auto_alarm";
	protected final static String ALARM_CB_TYPE = OSPREFIX+"alarmcb";
	protected final static String ALARM_C_DYN_CB_TYPE = OSPREFIX+"alarmcb_common_dyn";
	protected final static String ALARM_M_DYN_CB_TYPE = OSPREFIX+"alarmcb_multi_dyn";
	protected final static String ALARM_EXPIRY_CB_TYPE = OSPREFIX+"expirycb";
	protected final static String EXPIRY_ACTION_CB_TYPE = OSPREFIX+"xpoint_element";
	protected final static String ALARM_EXPIRY_ACTTASK_CB_TYPE = OSPREFIX+"expiry_taskcb";
	protected final static String ALARM_EXPIRY_CALLBACK_CB_TYPE  = OSPREFIX+"expiry_callbackcb";
	protected final static String ALARM_EXPIRY_INCCOUNTER_CB_TYPE  = OSPREFIX+"expiry_countercb";
	protected final static String ALARM_EXPIRY_SETEVENT_CB_TYPE  = OSPREFIX+"expiry_eventcb";
	protected final static String ALARM_EXPIRY_SCHEDULETAB_CB_TYPE  = OSPREFIX+"expiry_schedtabcb";
	protected final static String DEVICE_HANDLE_CB_TYPE = OSPREFIX+"device_handlecb";
	protected final static String DRIVER_HANDLE_CB_TYPE = OSPREFIX+"driver_handle_cb";
	protected final static String DEVICE_CB_TYPE = OSPREFIX+"devicecb";

	// Type Names of control blocks defined within the RTOS COM layer
	protected final static String COM_DEVICE_INIT_CB_TYPE = COMPREFIX+"device_initcb";
	protected final static String COM_MESSAGE_CB_TYPE = COMPREFIX+"messagecb"; 
	protected final static String COM_SENDER_HANDLE_CB_TYPE = COMPREFIX+"sender_handlecb";
	protected final static String COM_RECEIVER_CB_TYPE = COMPREFIX+"receivercb";
	protected final static String COM_RECEIVER_HANDLE_CB_TYPE = COMPREFIX+"receiver_handlecb";
	protected final static String COM_NOTIFICATION_CB_TYPE = COMPREFIX+"notifycb";
	protected final static String COM_NOTIFICATION_ACTTASK_CB_TYPE = COMPREFIX+"notify_taskcb";
	protected final static String COM_NOTIFICATION_CALLBACK_CB_TYPE  = COMPREFIX+"notify_callbackcb";
	protected final static String COM_NOTIFICATION_SETEVENT_CB_TYPE  = COMPREFIX+"notifiy_eventcb";
	protected final static String COM_FLAG_TYPE = "FlagValue";
	
	// Sending COM message types
	protected final static String COM_ZERO_LENGTH_TYPE = "COM_ZERO_LENGTH";
	protected final static String COM_QUEUED_OR_UNQUEUED_TYPE = "COM_QUEUED_OR_UNQUEUED";
	protected final static String COM_STREAM_TYPE = "COM_STREAM";

	// Handle type names
	protected final static String TASK_H = "TaskType";
	protected final static String ISR_H = "ISRType";
	protected final static String RES_H = "ResourceType";
	protected final static String APPMODE_H = "AppModeType";
	protected final static String ALARM_H = "AlarmType";
	protected final static String COUNTER_H = "CounterType";
	protected final static String SCHEDTAB_H = "ScheduleTableType";
	protected final static String QUEUE_H = OSPREFIX+"queueh";
	protected final static String COM_MESSAGE_H = "MessageIdentifier";
	protected final static String COM_APP_MODE_H = "COMApplicationModeType";
	protected final static String TICK_TYPE = "TickType";
	
	// Name of generated control block arrays/variables
	protected final static String EXT_TASK_CB_NAME = OSPREFIX+"e_tasks";
	protected final static String BASIC_TASK_CB_NAME = OSPREFIX+"b_tasks";
	protected final static String ISR_CB_NAME = OSPREFIX+"isrs";
	protected final static String APPMODE_CB_NAME = OSPREFIX+"appmodes";
	protected final static String RESOURCE_CB_NAME = OSPREFIX+"resources";
	protected final static String COUNTER_CB_NAME = OSPREFIX+"counters";
	protected final static String ALARM_CB_NAME = OSPREFIX+"alarms";
	protected final static String SCHEDULETABLE_CB_NAME = OSPREFIX+"schedtables";
	protected final static String EXPIRY_CB_NAME = OSPREFIX+"xpoints";
	protected final static String EXPIRY_ACTION_CB_NAME = OSPREFIX+"xpoint_actions";
	protected final static String STD_QUEUE_CB_NAME = OSPREFIX+"s_queues";	
	protected final static String OPT_QUEUE_CB_NAME = OSPREFIX+"o_queues";
	protected final static String DRIVER_CB_NAME = OSPREFIX+"driver";			// used for os_drivercb variables
	protected final static String DRIVER_HANDLE_CB_NAME = OSPREFIX+"drivers";	// used for os_driver_handlecb variables	
	protected final static String DEVICE_CB_NAME = OSPREFIX+"device";			// used for os_devicecb variables
	protected final static String DEVICE_HANDLE_CB_NAME = OSPREFIX+"deviceh";	// used for device_handlecb variables	
	protected final static String AUTO_ALARM_M_CB_NAME = OSPREFIX+"auto_m_alarms";
	protected final static String AUTO_ALARM_S_CB_NAME = OSPREFIX+"auto_s_alarms";
	protected final static String ALARM_ACTION_CB_NAME = OSPREFIX+"alarm_exp";
	protected final static String HANDLE_REF_TASK_NAME = OSPREFIX+"expiry_task";
	protected final static String COM_DEVICE_INIT_CB_NAME = COMPREFIX+"init_devices";
	protected final static String COM_SENDING_MESSAGE_CB_NAME = COMPREFIX+"sndmsgs";
	protected final static String COM_RECEIVING_MESSAGE_CB_NAME = COMPREFIX+"rcvmsgs";
	protected final static String COM_RECEIVE_DRV_CB_NAME = COMPREFIX+"rcvdrvmsgs";
	protected final static String COM_NOTIFY_CB_NAME = COMPREFIX+"message_notify";
	protected final static String COM_SCRATCH_FLAG_NAME = COMPREFIX+"scratch_flag";
	protected final static String COM_FLAG_NAME = COMPREFIX+"flag";
	protected final static String COM_INIT_VALUE_NAME = COMPREFIX+"init_value";
	protected final static String COM_QUEUED_DRV_NAME = "queued";
	protected final static String COM_UNQUEUED_DRV_NAME = "unqueued";
	protected final static String COM_ZERO_LENGTH_DRV_NAME = "zero";
	protected final static String COM_STREAM_DRV_NAME = "stream";
	
	// Name of generated dynamic control block variables	
	protected final static String TASK_DYN_NAME = OSPREFIX+"task_dyn";
	protected final static String SCRATCH_DYN_NAME = OSPREFIX+"scratch_dyn";
	protected final static String RESOURCE_DYN_NAME = OSPREFIX+"res_dyn";
	protected final static String QUEUE_DYN_NAME = OSPREFIX+"queue_dyn";
	protected final static String COUNTER_DYN_NAME = OSPREFIX+"counter_dyn";
	protected final static String ALARM_C_DYN_NAME = OSPREFIX+"alarm_c_dyn";
	protected final static String ALARM_M_DYN_NAME = OSPREFIX+"alarm_m_dyn";
	protected final static String SCHEDTAB_DYN_NAME = OSPREFIX+"schedtab_dyn";
		
	// Name of generated variables/functions
	protected final static String ALL_HANDLES_ARRAY_NAME = OSPREFIX+"all_handles";
	protected final static String ALL_ALL_NAMES_ARRAY_NAME = OSPREFIX+"all_names";
	protected final static String QUEUE_DATA_ARRAY_NAME = OSPREFIX+"queue_data";
	protected final static String EXT_STACK_ARRAY_NAME = OSPREFIX+"stacks";
	protected final static String ETASK_NUM_NAME = OSPREFIX+"num_e_tasks";
	protected final static String BTASK_NUM_NAME = OSPREFIX+"num_b_tasks";
	protected final static String ETASK_ACT_NAME = OSPREFIX+"ecounts";
	protected final static String BTASK_ACT_NAME = OSPREFIX+"bcounts";
	protected final static String RES_NUM_NAME = OSPREFIX+"num_res";
	protected final static String PRIQUEUE_NAME = OSPREFIX+"priqueue";
	protected final static String TASK_HIGHEST_PRI_NAME = OSPREFIX+"highest_task_pri";
	protected final static String PRI_2_IPL_NAME = OSPREFIX+"pri2ipl";
	protected final static String KERNEL_IPL_NAME = OSPREFIX+"kernelipl";
	protected final static String KERNEL_PRI_NAME = OSPREFIX+"kernelpri";
	protected final static String REINIT_F_NAME = OSPREFIX+"reinitf";
	protected final static String REINIT_NAME = OSPREFIX+"reinit";
	protected final static String PREHOOK_STACK_NAME = OSPREFIX+"pretask_hook_offset";
	protected final static String POSTHOOK_STACK_NAME = OSPREFIX+"posttask_hook_offset";
	protected final static String APPMODE_TASK_ARRAY_PREFIX = OSPREFIX+"autotasks_";
	protected final static String FLIH_NAME = OSPREFIX+"flih";
	protected final static String QUEUETASK_F_NAME = OSPREFIX+"queuetask";
	protected final static String QUEUETASK_STD_NAME = OSPREFIX+"queuetask_std";
	protected final static String QUEUETASK_OPT_NAME = OSPREFIX+"queuetask_opt";
	protected final static String DEQUEUETASK_F_NAME = OSPREFIX+"dequeuetask";
	protected final static String DEQUEUETASK_STD_NAME = OSPREFIX+"dequeuetask_std";
	protected final static String DEQUEUETASK_OPT_NAME = OSPREFIX+"dequeuetask_opt";
	protected final static String KS_DISPATCH_F_NAME = OSPREFIX+"ks_dispatch";
	protected final static String KS_DISPATCH_BT_NAME = OSPREFIX+"ks_dispatch_bt";
	protected final static String KS_DISPATCH_ET_NAME = OSPREFIX+"ks_dispatch_et";
	protected final static String KS_DISPATCH_MIX_NAME = OSPREFIX+"ks_dispatch_mix";
	protected final static String SWST_DISPATCH_F_NAME = OSPREFIX+"swst_dispatch";	
	protected final static String SWST_DISPATCH_ET_NAME = OSPREFIX+"swst_dispatch_et";
	protected final static String SWST_DISPATCH_MIX_NAME = OSPREFIX+"swst_dispatch_mix";
	protected final static String TERMINATE_F_NAME = OSPREFIX+"terminate";
	protected final static String TERMINATE_BT_NAME = OSPREFIX+"terminate_bt";
	protected final static String TERMINATE_ET_NAME = OSPREFIX+"terminate_et";
	protected final static String TERMINATE_MIX_NAME = OSPREFIX+"terminate_mix";
	protected final static String COUNTER_EXP_MULTI_F_NAME = OSPREFIX+"counter_expired_multi";
	protected final static String COUNTER_EXP_SINGLE_F_NAME = OSPREFIX+"counter_expired_single";	
	protected final static String COUNTER_SETREL_MULTI_F_NAME = OSPREFIX+"setrelalarm_multi";
	protected final static String COUNTER_SETREL_SINGLE_F_NAME = OSPREFIX+"setrelalarm_single";
	protected final static String COUNTER_CANCEL_MULTI_F_NAME = OSPREFIX+"cancel_alarm_multi";
	protected final static String COUNTER_CANCEL_SINGLE_F_NAME = OSPREFIX+"cancel_alarm_single";	
	protected final static String ALARM_EXPIRY_ACTTASK_F_NAME = OSPREFIX+"expiry_activatetask";
	protected final static String ALARM_EXPIRY_CALLBACK_F_NAME = OSPREFIX+"expiry_callback";
	protected final static String ALARM_EXPIRY_INCCOUNTER_F_NAME = OSPREFIX+"expiry_incrementcounter";
	protected final static String ALARM_EXPIRY_SETEVENT_F_NAME = OSPREFIX+"expiry_setevent";
	protected final static String ALARM_EXPIRY_SCHEDULETAB_F_NAME = OSPREFIX+"expiry_schedtab";
	protected final static String AUTOSTART_ALARM_S_F_NAME = OSPREFIX+"autostart_alarms_singleton";
	protected final static String AUTOSTART_ALARM_M_F_NAME = OSPREFIX+"autostart_alarms_multi";
	protected final static String FLAGS_NAME = OSPREFIX+"flags";
	protected final static String INIT_FLAGS_NAME = OSPREFIX+"init_flags";
	protected final static String FIRST_E_TASK_NAME = OSPREFIX+"first_e_task";
	protected final static String LAST_E_TASK_NAME = OSPREFIX+"last_e_task";
	protected final static String FIRST_B_TASK_NAME = OSPREFIX+"first_b_task";
	protected final static String LAST_B_TASK_NAME = OSPREFIX+"last_b_task";
	protected final static String COUNTER_NUM_NAME = OSPREFIX+"num_counters";	
	protected final static String FIRST_COUNTER_NAME = OSPREFIX+"first_counter";
	protected final static String LAST_COUNTER_NAME = OSPREFIX+"last_counter";
	protected final static String LAST_RES_NAME = OSPREFIX+"last_res";	
	protected final static String ALARM_NUM_NAME = OSPREFIX+"num_alarms";	
	protected final static String FIRST_ALARM_NAME = OSPREFIX+"first_alarm";
	protected final static String LAST_ALARM_NAME = OSPREFIX+"last_alarm";
	protected final static String SCHEDTAB_NUM_NAME = OSPREFIX+"num_schedtables";
	protected final static String FIRST_SCHEDTAB_NAME = OSPREFIX+"first_schedtab";
	protected final static String LAST_SCHEDTAB_NAME = OSPREFIX+"last_schedtab";
	protected final static String COM_MESSAGE_LAST_SEND_NAME = COMPREFIX+"last_send_msg";
	protected final static String COM_MESSAGE_LAST_RECEIVE_NAME = COMPREFIX+"last_rcv_msg";
	protected final static String COM_RECEIVE_NUM_NAME = COMPREFIX+"num_rcv_msgs";
	protected final static String COM_MODES_NUM = COMPREFIX+"mode_count";
	protected final static String COM_CALL_EXTENSION_NAME = COMPREFIX+"call_StartCOMExtension";
	protected final static String COM_HOOK_CALLABLE_NAME = COMPREFIX+"COMErrorHook_callable";
	protected final static String COM_NUM_INIT_DEVICES = COMPREFIX+"num_init_devices";
	protected final static String OSMAXALLOWEDVALUE = "OSMAXALLOWEDVALUE";
	protected final static String OSTICKSPERBASE = "OSTICKSPERBASE";
	protected final static String OSMINCYCLE = "OSMINCYCLE";
	protected final static String OSTICKDURATION = "OSTICKDURATION";
	
	// Name of generated link check variable instances
	protected final static String LINK_EXTENDED_NAME = OSPREFIX+"eslnk";
	protected final static String LINK_STANDARD_NAME = OSPREFIX+"sslnk";
	protected final static String LINK_STACK_MONITORING_NAME = OSPREFIX+"smlnk";
	protected final static String LINK_NO_STACK_MONITORING_NAME = OSPREFIX+"nosmlnk";
	
	// Name of generated constants in #defines
	protected final static String EXTENDED_STATUS_NAME = OSPREFIX.toUpperCase()+"EXTENDED_STATUS";
	protected final static String STANDARD_STATUS_NAME = OSPREFIX.toUpperCase()+"STANDARD_STATUS";
	protected final static String COM_EXTENDED_STATUS_NAME = COMPREFIX.toUpperCase()+"EXTENDED_STATUS";
	protected final static String COM_STANDARD_STATUS_NAME = COMPREFIX.toUpperCase()+"STANDARD_STATUS";

	// Name of valid COM notification action enum values
	protected final static String COM_NOTIFY_NONE = "COM_NOTIFY_NONE";
	protected final static String COM_NOTIFY_ACTIVATE_TASK = "COM_NOTIFY_ACTIVATE_TASK";
	protected final static String COM_NOTIFY_SET_EVENT = "COM_NOTIFY_SET_EVENT";
	protected final static String COM_NOTIFY_CALLBACK = "COM_NOTIFY_CALLBACK";
	
	// C modifiers/types used in the OS
	protected final static String FASTROM = "FASTROM";
	protected final static String STATIC = "static";
	protected final static String CONST = "const";
	protected final static String STRUCT = "struct";
	protected final static String CONST_STRUCT = "const struct";	
	protected final static String UNAT = "unat";
	protected final static String NAT = "nat";
	protected final static String CONST_UNAT = "const unat";
	protected final static String PRI = OSPREFIX+"pri";
	protected final static String IPL = OSPREFIX+"ipl";	
	protected final static String STACKP = OSPREFIX+"stackp";
	protected final static String STACKWORD = OSPREFIX+"stackword";
	protected final static String ENTRYF = OSPREFIX+"entryf";
	protected final static String EVENTMASK = OSPREFIX+"eventmask";
	protected final static String PRIMASK = OSPREFIX+"primask";
	protected final static String UINT8 = "uint8";
	protected final static String UINT16 = "uint16";
	protected final static String UINT32 = "uint32";
	protected final static String NEAR = "NEAR";
	
	protected final static String DEFINE = "#define";	


	
	/**
	 * Counts number of commas to be output.
	 * 
	 * @see #initComma(int)
	 * @see #doComma()
	 */
	private int commaCount;
	
	/**
	 * Initialises the commaCount.
	 * 
	 * @param count the number of items to be separated by commas
	 * @see #doComma() 
	 */
	private void initComma(int count) {
		commaCount = count;
	}
	
	/**
	 * Appends a comma unless the last item.
	 * 
	 * @see #initComma(int)
	 *
	 */
	private void doComma() {
		
		commaCount--;
		
		if (commaCount > 0) {
			append(",");
		}
	}
	
	/**
	 * @return comment start delimiter
	 */
	@Override
	protected String getCommentStart() {
		return COMMENT_START;
	}

	/**
	 * @return comment end delimiter
	 */
	@Override
	protected String getCommentEnd() {
		return COMMENT_END;
	}
	
	/**
	 * @return separator comment line
	 */	
	@Override
	protected String separatorComment() {
		return SEP_COMMENT;
	}	
	
	/**
	 * Helper that generates a string containing a cast version of the specified C datatype name.
	 * 
	 * @param type the type name to be cast
	 * @return the type name casted, using (type) syntax
	 */
	protected static String cast(String type) {
		return "("+type+")";
	}	
	
	/**
	 * outputs a #include "file"
	 * @param fileName name of file to be included
	 */
	/*
	private void include_local(String fileName) {
		writeln("#include \""+fileName+"\"");
	}
	*/
	
	/**
	 * outputs a #include <file>
	 * @param fileName name of file to be included
	 */
	private void include(String fileName) {
		writeln("#include <"+fileName+">");
	}	
	
	/**
	 * Generates a variable name for the given target element.
	 * @param element the element for which a var name is required
	 * @return the var name to use for the given element
	 */
	protected static String genCName(TargetElement element) {
		
		return element.getName();
	}
	
	
	/**
	 * Epilogue called after all ANSI C code has been output to the C source code file.
	 * The current writer is still setup for output to the the C source when this is called.
	 * 
	 * When overridden, this method allows target specific information to be added to the
	 * generated C source file.
	 *
	 * @see OSAnsiCGenerator#generate(String)
	 */
	public abstract void SourceCFileEpilogue();	
	
	/**
	 * Maps from a target priority to the appropriate primask for a specific target.
	 * @param targetPriority the target priority value to be mapped
	 * @return the primask string containing a hex number, e.g. 0x0080
	 */
	protected abstract String getTargetPriorityMaskString(int targetPriority);
	

	/**
	 * Maps from a target priority to the IPL string for a specific target.
	 * 
	 * @param targetPriority the target priority to be mapped
	 * @return the IPL string for the target.
	 */
	protected abstract String getTargetIPLString(int targetPriority);
	

	/**
	 * Maps from a target priority to an embedded priority number for a specific target. (typedef'd as pri on the target)
	 * In the generic version there is a 1:1 mapping between target priority number and embedded priority number.   
	 * @param targetPriority the target priority to be mapped
	 * @return the embedded priority number of the priority for the target.
	 */
	protected int mapToEmbeddedPriority(int targetPriority) {
	
		return targetPriority;
	}
	
	
	/**
	 * Returns a string that provides a reference to the appropriate control block 
	 * within an array of control blocks for the given TargetElement.
	 * 
	 * @param element the TargetElement for which the reference is required
	 * @return the textual reference.
	 */	
	public static String getTargetElementArrayReference(TargetElement element) {
			
		int cbIndex = element.getControlBlockIndex();
		
		if (cbIndex != TargetElement.NO_CONTROL_BLOCK) {
			
			return "&"+element.getControlBlockName()+"["+cbIndex+"]";
		}
		else {
			return "0";
		}
	}		

	/**
	 * Helper function that generates all the code required for each task defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  0. A reference to the task if it is activated by an alarm, schedule table or message receipt notification
	 *  1. Variables values to represent the number of extended tasks and basic tasks
	 * 	2. A scratch dynamic control block to be shared by the idle task and all basic tasks.
	 *  3. A dynamic control block for each extended task within the OS model.
	 *  4. An array to store the stack space for use by all extended tasks.
	 *  5. An array to store activation count values for each task.
	 *  6. An array of control blocks for each extended task in the OS model.
	 *  7. An array of control blocks for idle task and each basic task in the OS model.
	 */
	private void generateTasks() {

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 0. Generate a reference to the task if it is activated by an alarm or schedule table
		writeln(comment("Reference values to identify tasks that are activated by alarms or schedule tables."));
		
		for ( TargetTask nextTask : targetModel.getTargetTasks() ) {
			
			if ( nextTask.isActivated() ) {
				
				// static const struct os_expiry_taskcb os_expiry_task_<n> = { &tasks[n] };
				write(STATIC+" "+CONST_STRUCT+" "+ALARM_EXPIRY_ACTTASK_CB_TYPE+" "+HANDLE_REF_TASK_NAME+"_"+nextTask.getControlBlockIndex()+" = {");
				
				append(getTargetElementArrayReference(nextTask));
				append(" };");
				writeNL();
			}
		}
		writeNL();
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate variable values to represent the number of extended tasks and basic tasks (including idle task)

		writeln(comment("Variable values to represent the number of extended tasks and basic tasks (including idle task)."));
		
		if (targetModel.isRestartable()) {
			writeln(CONST_UNAT+" "+ETASK_NUM_NAME+" = "+targetModel.getTargetExtendedTasks().size()+"U;");
			writeln(CONST_UNAT+" "+BTASK_NUM_NAME+" = "+targetModel.getTargetBasicTasks().size()+"U;");
		}
		else {
			writeln(comment(ETASK_NUM_NAME+", "+BTASK_NUM_NAME+" not need since reinit not used."));
		}		
				
		/////////////////////////////////////////////////////////////////////////////////////////////////
		// Generate the first and last task pointer values for handle range checking (if extended status)
		// or for assertion checking (if standard status)		
		// See wiki1036
		
		writeNL();
		writeln(comment("Variable values to allow validity checking of task handles and assertion checks."));
		
		// In standard status there is a need for values for assertion checking only
		if(!targetModel.isExtendedStatus()) {
			writeln("#ifndef NDEBUG");
		}
		
		int extCount = targetModel.getTargetExtendedTasks().size();
		
		if ( extCount > 0 ) {
			writeln(CONST+" "+TASK_H+" "+FIRST_E_TASK_NAME+" = &"+platformInfo.getExtTaskCBName()+"[0];");
			writeln(CONST+" "+TASK_H+" "+LAST_E_TASK_NAME+" = &"+platformInfo.getExtTaskCBName()+"["+(extCount-1)+"];");
		}
		else {
			writeln(CONST+" "+TASK_H+" "+FIRST_E_TASK_NAME+" = 0;");
			writeln(CONST+" "+TASK_H+" "+LAST_E_TASK_NAME+" = 0;");				
		}
		
		int basicCount = targetModel.getTargetBasicTasks().size();
		
		if ( basicCount > 1 ) {
			// the first valid task handle is the one following the idle task in the control block
			writeln(CONST+" "+TASK_H+" "+FIRST_B_TASK_NAME+" = &"+platformInfo.getBasicTaskCBName()+"[1];");
			writeln(CONST+" "+TASK_H+" "+LAST_B_TASK_NAME+" = &"+platformInfo.getBasicTaskCBName()+"["+(basicCount-1)+"];");
		}
		else {
			// only idle task exists, so null handle check values
			writeln(CONST+" "+TASK_H+" "+FIRST_B_TASK_NAME+" = 0;");
			writeln(CONST+" "+TASK_H+" "+LAST_B_TASK_NAME+" = 0;");				
		}
		
		if(!targetModel.isExtendedStatus()) {
			writeln("#endif /* NDEBUG */");
		}
		writeNL();		
		
		// Generate the dynamic task control blocks (shared one for all basic tasks + one for each extended task)
		writeln(comment("Allocate the Dynamic (RAM) based parts of the task control blocks."));
	
		///////////////////////////////////////////////////////////////////////
		// 2. Generate the scratch dynamic control block for the idle task and basic tasks to share
 
		write(STATIC+" "+STRUCT+" "+TASK_DYN_CB_TYPE+" "+SCRATCH_DYN_NAME+";");
		writeNL();
		
		///////////////////////////////////////////////////////////////////////
		// 3. Generate the dynamic control block for each extended task
		
		if ( targetModel.getTargetExtendedTasks().size() > 0 ) {
			writeln(STATIC+" "+STRUCT+" "+TASK_DYN_CB_TYPE+" "+TASK_DYN_NAME+"["+targetModel.getTargetExtendedTasks().size()+"];");
		}

		writeNL();
		
		//////////////////////////////////////////////////////////////////
		// 4. Generate the stack space required for all the extended tasks
		
		if (targetModel.getExtTaskStackWords() > 0) {
			
			writeln(STATIC+" "+STACKWORD+" "+EXT_STACK_ARRAY_NAME+"["+targetModel.getExtTaskStackWords()+"];");
			writeNL();
		}
	
		//////////////////////////////////////////////////////////////////////
		// 5. Generate the array of current activation count values for each task
		
		writeln(comment("Allocate space for each activation count (one per user task + one for idle task)."));
		
		if (targetModel.getTargetExtendedTasks().size() > 0) {
			writeln(STATIC+" "+UNAT+" "+ETASK_ACT_NAME+"["+targetModel.getTargetExtendedTasks().size()+"];");
		}
		
		// array of activation counts for basic tasks includes the idle task.
		writeln(STATIC+" "+UNAT+" "+BTASK_ACT_NAME+"["+targetModel.getTargetBasicTasks().size()+"];");
		writeNL();
		
		///////////////////////////////////////////////////////////////////////
		// 6. Generate the ROM based task control blocks for extended tasks

		if (targetModel.getTargetExtendedTasks().size() > 0) {
			
			writeln(comment("Define the ROM based part of the task control blocks for all the extended tasks."));
			
			writeln(CONST_STRUCT+" "+TASK_CB_TYPE+" "+platformInfo.getExtTaskCBName()+"[] = {");
			incTabs();
						
			initComma(targetModel.getTargetExtendedTasks().size());
			
			for (TargetTask next : targetModel.getTargetExtendedTasks()) {
				
				writeln("{");
				
				incTabs();				
									
				String name = genCName(next);
				
				writeln(comment("Task control block for task "+next.getName()));
				
				// The order of the generated values is dependent upon the structure declaration within core.h
				
				//////////////////////////////////////////////////////////
				// struct ext_taskcb_dyn *dyn;
				write("&"+TASK_DYN_NAME+"["+next.getControlBlockIndex()+"],");
				append(verboseComment("struct ext_taskcb_dyn *dyn;"));
				writeNL();

				//////////////////////////////////////////////////////////
				// unat *count;
				write("&"+ETASK_ACT_NAME+"["+next.getControlBlockIndex()+"],");
				append(verboseComment("unat *count;"));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// unat countlimit;
				write(next.getModelActivation()+"U,");		// Maximum number of task activations permitted
				append(verboseComment("unat countlimit;"));
				writeNL();		
				
				//////////////////////////////////////////////////////////
				// pri basepri;
				write(mapToEmbeddedPriority(next.getTargetPriority())+"U,");
				append(verboseComment("pri basepri; model priority = "+next.getModelPriority()));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// pri boostpri;
				write(mapToEmbeddedPriority(next.getBoostPriority())+"U,");
				append(verboseComment("pri boostpri;"));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// primask basepri_mask;
				write(getTargetPriorityMaskString(next.getTargetPriority())+",");
				append(verboseComment("primask basepri_mask;"));
				writeNL();		
				
				//////////////////////////////////////////////////////////
				// queueh queue;
							
				// Get the queue in which the task is referenced
				assert next.isIdle() == false;
				
				TargetQueue queue = targetModel.getQueueWithPriority(next.getTargetPriority());
				
				assert queue != null;
				
				if (queue.isOptimized()) {
					// only task in the queue, so optimize out
					write("0,");
					append(verboseComment("queueh queue; not required since unique task in queue."));
				}
				else {
					// queue has more than one task, so reference from this task's control block
					write(getTargetElementArrayReference(queue)+",");
					append(verboseComment("queueh queue;"));
				}
				writeNL();
				
				//////////////////////////////////////////////////////////
				// entryf entry;
				write(TASK_ENTRY_FN+name+",");
				append(verboseComment("entryf entry;"));
				writeNL();

				//////////////////////////////////////////////////////////
				// stackp initsp;
				write("&"+EXT_STACK_ARRAY_NAME+"["+next.getInitialStackPointerIndex()+"]");
				append(",");
				
				append(verboseComment("stackp initsp;"));
				writeNL();
			
				//////////////////////////////////////////////////////////
				// stackp tos;
				
				if ( targetModel.isStackCheckingEnabled() ) {
					write("&"+EXT_STACK_ARRAY_NAME+"["+next.getTopOfStackIndex()+"],");
					append(verboseComment("stackp tos; Amount of stack space requested was "+next.getModelStackSize()+" bytes."));		
				}
				else {
					write("0,");
					append(verboseComment("stackp tos; no values used when stack monitoring off"));					
				}
				writeNL();
				
				//////////////////////////////////////////////////////////
				// nat stackoffset;
				write("0");			
				append(verboseComment("nat stackoffset; not used by extended tasks"));
				writeNL();
				
				decTabs();
				
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}
		else {
			writeln(comment("No extended tasks so create an empty task control block for extended tasks."));
			
			writeln(CONST_STRUCT+" "+TASK_CB_TYPE+" "+platformInfo.getExtTaskCBName()+"[] = {};");
			
			writeNL();
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// 7. Generate the ROM based task control blocks for idle task and all basic tasks
		
		writeln(comment("The ROM based part of the task control blocks for the idle task and all basic tasks."));
		
		writeln(CONST_STRUCT+" "+TASK_CB_TYPE+" "+platformInfo.getBasicTaskCBName()+"[] = {");
		incTabs();
		

		initComma(targetModel.getTargetBasicTasks().size());
		
		for (TargetTask next : targetModel.getTargetBasicTasks()) {
						
			writeln("{");
			incTabs();
			
			String name = genCName(next);
			
			writeln(comment("Task control block for task "+next.getName()));
			
			// The order of the generated values is dependent upon the structure declaration within core.h
			
			//////////////////////////////////////////////////////////
			// struct ext_taskcb_dyn *dyn;
			write("&"+SCRATCH_DYN_NAME+",");	// basic tasks share common scratch dyn control block
			append(verboseComment("struct ext_taskcb_dyn *dyn;"));
			writeNL();

			//////////////////////////////////////////////////////////
			// unat *count;
			write("&"+BTASK_ACT_NAME+"["+next.getControlBlockIndex()+"],");
			append(verboseComment("unat *count;"));
			writeNL();
			
			//////////////////////////////////////////////////////////
			// unat countlimit;
			write(next.getModelActivation()+"U,");		// Maximum number of task activations permitted
			append(verboseComment("unat countlimit;"));
			writeNL();		
			
			//////////////////////////////////////////////////////////
			// pri basepri;
			
			write(mapToEmbeddedPriority(next.getTargetPriority())+"U,");
			append(verboseComment("pri basepri; model priority = "+next.getModelPriority()));
			writeNL();
			
			//////////////////////////////////////////////////////////
			// pri boostpri;
						
			write(mapToEmbeddedPriority(next.getBoostPriority())+"U,");
			append(verboseComment("pri boostpri;"));
			writeNL();
			
			//////////////////////////////////////////////////////////
			// primask basepri_mask;
			write(getTargetPriorityMaskString(next.getTargetPriority())+",");
			append(verboseComment("primask basepri_mask;"));
			writeNL();		
			
			//////////////////////////////////////////////////////////
			// queueh queue;
			
			// The idle task never has a queue generated for it, so always output "0"
			
			if (next.isIdle()) {
				write("0,");
				append(verboseComment("queueh queue; idle task never has a queue."));
			}
			else {
				// Get the queue in which the task is referenced
				TargetQueue queue = targetModel.getQueueWithPriority(next.getTargetPriority());
				
				assert queue != null;
				
				if (queue.isOptimized()) {
					// only task in the queue, so optimize out
					write("0,");
					append(verboseComment("queueh queue; not required since unique task in queue."));
				}
				else {
					// queue has more than one task, so reference from this task's control block
					write(getTargetElementArrayReference(queue)+",");
					append(verboseComment("queueh queue;"));
				}
			}
			writeNL();
			
			//////////////////////////////////////////////////////////
			// entryf entry;
			
			if (next.isIdle()) {
				write("0,");
				append(verboseComment("entryf entry; 0 for the idle task"));
			}
			else {
				write(TASK_ENTRY_FN+name+",");
				append(verboseComment("entryf entry;"));
			}
			writeNL();

			//////////////////////////////////////////////////////////
			// stackp initsp;
			write("0,");
			append(verboseComment("stackp initsp;"));
			writeNL();		
			
			//////////////////////////////////////////////////////////
			// stackp tos;
			write("0,");
			append(verboseComment("stackp tos; not used by basic tasks"));
			writeNL();
		
			//////////////////////////////////////////////////////////
			// nat stackoffset;			
			if ( targetModel.isStackCheckingEnabled() ) {			

				write(Long.toString(next.getStackOffset()));

				if ( next.isAutoStackSize() ) {
					append(verboseComment("nat stackoffset; Amount of stack space AUTO requested as "+platformInfo.getDefaultTaskStackSize())+" bytes.");
				}
				else {				
					append(verboseComment("nat stackoffset; Amount of stack space requested was "+next.getModelStackSize()+" bytes."));	
				}		
			}
			else {
				write("0");
				append(verboseComment("nat stackoffset; not used when stack monitoring off"));
			}
			writeNL();
			
			decTabs();
			write("}");
			doComma();
			writeNL();
		}
		
		decTabs();
		writeln("};");
		
		writeNL();
	}

		
	/**
	 * Helper function that generates all the code required for each queue required for tasks within the 
	 * current OS model.
	 * 
	 * Generates:
	 * 
	 *  2. An array of queue storage space for all the queues
	 *  3. An array of dynamic (RAM) queue control blocks.
	 *  4. An array of standard queue control blocks (i.e. non-optimized queues with a dyn)
	 *  5. An array of optimized queue control blocks, i.e. queues with no dyn
	 *  6. An array of queue pointers that reference each queue at each priority band.  
 	 */	
	private void generateQueues() {
		
		
		////////////////////////////////////////////////////////
		// 2. An array of queue storage space for all the queues		
		if (targetModel.getQueueSlots() > 0 ) {
			writeln(comment("Allocate queue storage space for all the queues"));
			writeln(STATIC+" "+TASK_H+" "+QUEUE_DATA_ARRAY_NAME+"["+targetModel.getQueueSlots()+"];");
			writeNL();
		}
		
		///////////////////////////////////////////////////////
		// 3. Generate an array of dynamic queue control blocks

		if (targetModel.getTargetStandardQueues().size() > 0) {
		
			writeln(comment("An array of dynamic queue control blocks"));
			
			writeln(STATIC+" "+STRUCT+" "+QUEUE_DYN_CB_TYPE+" "+QUEUE_DYN_NAME+"[] = {");
			
			incTabs();
			
			initComma(targetModel.getTargetStandardQueues().size());
			
			for (TargetQueue next : targetModel.getTargetStandardQueues()) {

				assert next.isOptimized() == false;
								
				write("{");
				append(comment("dynamic queue control block for tasks at priority "+next.getTargetPriority()));
				writeNL();
				incTabs();
				
				write("&"+QUEUE_DATA_ARRAY_NAME+"["+next.getFirst()+"],");
				append(verboseComment("TaskType *head;"));
				writeNL();

				write("&"+QUEUE_DATA_ARRAY_NAME+"["+next.getFirst()+"]");
				append(verboseComment("TaskType *tail;"));
				writeNL();
				
				decTabs();
				write("}");	
				doComma();
				writeNL();
			}
			
			decTabs();
			writeln("};");
			
			writeNL();		
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Generate an array of standard queue control blocks (i.e. non-optimized queues with a dyn)
		
		if ( targetModel.getTargetStandardQueues().size() > 0 ) {
			
			writeln(comment("An array of standard (non-optimized) queue control blocks"));
			
			writeln(STATIC+" "+CONST_STRUCT+" "+QUEUE_CB_TYPE+" "+platformInfo.getStdQueueCBName()+"[] = {");
			
			incTabs();
				
			initComma(targetModel.getTargetStandardQueues().size());
			
			for (TargetQueue next : targetModel.getTargetStandardQueues()) {
	
				assert next.isOptimized() == false;				
				
				write("{");
				append(comment("queue control block for tasks at priority "+next.getTargetPriority()));
				writeNL();
				incTabs();
		
				/////////////////////////////////////////////////////////////////////////
				// struct queuecb_dyn *dyn;					
			
				write("&"+QUEUE_DYN_NAME+"["+next.getControlBlockIndex()+"],");
				append(verboseComment("struct queuecb_dyn *dyn;"));
				writeNL();
					
				/////////////////////////////////////////////////////////////////////////
				// TaskType uniquetask;
				write("0,");
				append(verboseComment("TaskType uniquetask; null since non-unique task priority"));
				writeNL();
					
				/////////////////////////////////////////////////////////////////////////
				// TaskType *first;
				write("&"+QUEUE_DATA_ARRAY_NAME+"["+next.getFirst()+"],");
				append(verboseComment("TaskType *first;"));
				writeNL();
					
				/////////////////////////////////////////////////////////////////////////
				// TaskType *last;
				write("&"+QUEUE_DATA_ARRAY_NAME+"["+next.getLast()+"]");
				append(verboseComment("TaskType *last;"));
				writeNL();
					
				decTabs();
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// 5. Generate an array of optimized queue control blocks, i.e. queues with no dyn
		
		writeln(comment("An array of optimized queue control blocks (i.e. no dyn)"));
		
		writeln(STATIC+" "+CONST_STRUCT+" "+QUEUE_CB_TYPE+" "+platformInfo.getOptQueueCBName()+"[] = {");
		
		incTabs();
		
		initComma(targetModel.getTargetOptimizedQueues().size());
		
		for (TargetQueue next : targetModel.getTargetOptimizedQueues()) {
			
			// we have a queue that contains at least one task, so output a CB entry for the queue				
			
			write("{");
			append(comment("queue control block for tasks at priority "+next.getTargetPriority()));
			writeNL();
			incTabs();
	
			/////////////////////////////////////////////////////////////////////////
			// struct queuecb_dyn *dyn;					
			// only one task in the queue, so no need to reference a full queue
			write("0,");
			append(verboseComment("struct queuecb_dyn *dyn; null since unique task priority"));
			writeNL();
				
			/////////////////////////////////////////////////////////////////////////
			// TaskType uniquetask;

			// is the queue that references the idle task
			
			TargetTask task = next.getTargetTasks().iterator().next();
			
			assert next.getTargetTasks().size() == 1;
			
			write(getTargetElementArrayReference(task)+",");
			append(verboseComment("unique task priority for the task "+task.getName()));
			writeNL();
				
			/////////////////////////////////////////////////////////////////////////
			// TaskType *first;
			write("0,");
			append(verboseComment("TaskType *first; null since unique task priority"));
			writeNL();
				
			/////////////////////////////////////////////////////////////////////////
			//  *last;

			write("0");
			append(verboseComment("TaskType *last; null since unique task priority"));
			writeNL();
			
			decTabs();
			write("}");
			doComma();
			writeNL();
		}
		decTabs();
		writeln("};");
		
		writeNL();
		
		//////////////////////////////////////////////////////////
		// 6. Generate the array of queue pointers (PRIQUEUE_NAME)
		
		writeln(comment("Reference to each queue from each priority band"));
		writeln(QUEUE_H+" "+FASTROM+"("+PRIQUEUE_NAME+"[]) = {");
		incTabs();
			
		initComma(targetModel.getTargetQueues().size());
		
		for (TargetQueue next : targetModel.getTargetQueues()) {
			
			assert next.getTargetTasks().size() > 0;
			
			write(getTargetElementArrayReference(next));
			doComma();
			append(comment("A queue for all tasks at priority "+next.getTargetPriority()));
			writeNL();
		}
		
		decTabs();
		writeln("};");
		
		writeNL();		
	}
	
	
	/**
	 * Helper function that generates all the code required for each ISR defined within the 
	 * current OS model.
	 * 
	 * Generates: 
	 *  1. An array of control blocks for each ISR in the OS model.
	 */
	private void generateISRs() {
		
		///////////////////////////////////////////////////////////////////////
		// 1. Generate the ROM based ISR control blocks for each ISR
		
		if (targetModel.getTargetISRs().size() > 0) {
			
			writeln(comment("The ROM based part of the ISR control blocks for all the ISRs."));
			
			writeln(CONST_STRUCT+" "+ISR_CB_TYPE+" "+platformInfo.getIsrCBName()+"[] = {");
			incTabs();

			initComma(targetModel.getTargetISRs().size());
			
			for (TargetISR next : targetModel.getTargetISRs()) {
				
				String name = genCName(next);		

				writeln("{");
				incTabs();
				
				writeln(comment("ISR control block for ISR "+next.getName()));
				
				// The order of the generated values is dependent upon the structure declaration within core.h
				
				//////////////////////////////////////////////////////////
				// pri basepri;

				write(mapToEmbeddedPriority(next.getTargetPriority())+"U,");
				append(verboseComment("pri basepri; model priority = "+next.getModelPriority()));
				writeNL();		
				
				//////////////////////////////////////////////////////////
				// entryf handler;	
				write(ISR_ENTRY_FN+name);
				append(",");

				append(verboseComment("entryf handler;"));
				writeNL();		
			
				/////////////////////////////////////////////////////////
				// nat stackoffset;					
				if ( targetModel.isStackCheckingEnabled() ) {
						
					write(Long.toString(next.getStackOffset()));
					
					if ( next.isAutoStackSize() ) {
						append(verboseComment("nat stackoffset; Amount of stack space AUTO requested as "+platformInfo.getDefaultISRStackSize()+" bytes."));
					}
					else {
						append(verboseComment("nat stackoffset; Amount of stack space requested was "+next.getModelStackSize()+" bytes."));
					}	
				}
				else {
					write("0");
					append(verboseComment("nat stackoffset; value not used when stack monitoring is off"));
				}
				
				writeNL();
				
				decTabs();
				write("}");	
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}		
	}	
	
	/**
	 * Helper function that generates all the code required for each resource defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  1. variable value to represent the number of (non-internal) accessed resources
	 *  2. An array of dynamic control blocks for each (non-internal) accessed resource within the OS model. 
	 *  3. An array of control blocks for each (non-internal) accessed resource in the OS model.
	 */
	private void generateResources() {
		
		int numResources = targetModel.getTargetAccessedNonInternalResources().size();		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate variable value to represent the number of (non-internal) accessed resources

		writeln(comment("Variable value to represent the number of (non-internal) accessed resources."));
		
		if (targetModel.isRestartable()) {
			writeln(CONST_UNAT+" "+RES_NUM_NAME+" = "+numResources+"U;");
		}
		else {
			writeln(comment(RES_NUM_NAME+" not needed since reinit not used."));
		}

		writeNL();		
		
		if (numResources > 0) {
			
			///////////////////////////////////////////////////////////////////////////////////////////
			// 2. Generate an array of dynamic control blocks for each (non-internal) accessed resource
			
			writeln(comment("Allocate the Dynamic (RAM) based parts of the resource control blocks."));
			
			writeln(STATIC+" "+STRUCT+" "+RESOURCE_DYN_CB_TYPE+" "+RESOURCE_DYN_NAME+"["+targetModel.getTargetAccessedNonInternalResources().size()+"];");
				
			writeNL();
			
			//////////////////////////////////////////////////////////////////////////////////////////////
			// 3. Generate the ROM based resource control blocks for each (non-internal) accessed resource
			
			writeln(comment("The ROM based part of the resource control blocks for all the non-internal resources."));
			
			writeln(CONST_STRUCT+" "+RESOURCE_CB_TYPE+" "+platformInfo.getResourceCBName()+"[] = {");
			incTabs();
			
			initComma(targetModel.getTargetAccessedNonInternalResources().size());
			
			for (TargetResource next : targetModel.getTargetAccessedNonInternalResources()) {
							
				writeln("{");
				
				incTabs();
				
				writeln(comment("Resource control block for resource "+next.getName()));		
				
				////////////////////////////////////////////////////////////////////////////////////////
				// struct rescb_dyn *dyn;
				write("&"+RESOURCE_DYN_NAME+"["+next.getControlBlockIndex()+"],");
				append(verboseComment("struct rescb_dyn *dyn;"));
				writeNL();
				
				////////////////////////////////////////////////////////////////////////////////////////
				// pri ceil;
				write(mapToEmbeddedPriority(next.getCeiling())+"U");
				append(verboseComment("pri ceil;"));
				
				writeNL();
				
				decTabs();
				
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			/////////////////////////////////////////////////////////////////////////
			// Generate the res_scheduler variable instance if required
			
			// This handle is generated as a variable rather than a macro, since it may possibly need to be linked
			// to by libraries etc.
			TargetResource resScheduler = targetModel.getResSchedulerResource();
			
			if ( resScheduler != null ) {
				// const ResourceType RES_SCHEDULER = &os_resources[n];
				
				writeln(CONST+" "+RES_H+" "+TargetCpu.RES_SCHEDULER_NAME+"="+getTargetElementArrayReference(resScheduler)+";");			
			}
			
			writeNL();	
			
			///////////////////////////////////////////////////////////////////////////////
			// Generat the last resource pointer values for handle range checking
			
			if ( targetModel.isExtendedStatus() ) {
		
				writeNL();
				writeln(comment("Variable values to allow validity checking of resource handles."));

				if ( numResources > 0 ) {
					writeln(CONST+" "+RES_H+" "+LAST_RES_NAME+" = &"+platformInfo.getResourceCBName()+"["+(numResources-1)+"];");
				}
				else {
					writeln(CONST+" "+RES_H+" "+LAST_RES_NAME+" = 0;");				
				}		
			}
			
			writeNL();			
		}
	}	
	
	/**
	 * Helper function that generates all the code required for each device.
	 * 
	 * Generates:
	 * 1. A ROM based driver specific control block for each individual driver
	 * 2. A ROM based generic control block for each driver
	 */		
	private void generateDeviceDrivers() {
		
		Collection<TargetDriver> drivers = targetModel.getTargetDrivers();
		
		////////////////////////////////////////////////////////////////////////
		// 1. A ROM based driver specific control block for each individual driver
		
		
		for (TargetDriver driver : drivers ) {

			writeln(comment("Driver specific control-block for '"+driver.getName()+"' driver"));
			
			// Use the driver to generate this, since the kind of driver determines the contents
			// of the control block, e.g. what fn pointers need to be generated.
			driver.genCDriverCode();
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// 2. A ROM based generic control block for each driver
		
		/*
		 * Code below generates an array that ties devices to drivers. This is not currently used
		 * but could be used if a generic device_ctl function is implemented.
		 * 
		 * TODO: check whether to remove this completely.
		if ( drivers.size() > 0 ) {
				
			writeln(comment("Common driver control-block for each driver"));
			
			//static const struct os_driver_handle_cb os_drivers[] = { {},... };
			writeln(STATIC+" "+CONST_STRUCT+" "+DRIVER_HANDLE_CB_TYPE+" "+DRIVER_HANDLE_CB_NAME+"[] = {");
			incTabs();
			
			initComma(drivers.size());
			
			for (TargetDriver driver : drivers ) {		
				
				//////////////////////////////////////////////////////////
				//struct os_driver_handle_cb {
				//	StatusType (*ctl_device)(DeviceId, DeviceControlCodeType, DeviceControlDataType);	// ctl function common to all device drivers; Points to os_driver_default_ctl() if not implemented by driver 
				//};

				// Get the name of the implemented ctl function from the driver.				
				write("{"+driver.getCtlFnName()+"}");

				doComma();
				append(comment("Common CB for '"+driver.getName()+"' driver"));
				writeNL();
			}
			decTabs();
			writeln("};");
			writeNL();
		}
		*/
		

	}	
	
	/**
	 * Helper function that generates all the code required for each device.
	 * 
	 * Generates:

	 * 1. The ROM based device control block that associates each device with a driver
	 */		
	private void generateDeviceDriverAssociations() {
		//////////////////////////////////////////////////////////////////////////////////
		// 1. The ROM based device control block that associates each device with a driver
		
		// This allows access to the device via a handle, therefore this is not generated
		// unless a handle is required.
		
		
		if ( targetModel.getTargetDevices().size() > 0 ) {
			writeln(comment("Control-block for each device for which a handle is provided"));
			writeNL();
		}
		
		for (TargetDevice device : targetModel.getTargetDevices()) {
			
			if ( device.getHasHandle() ) {
				
				/* Structure used to tie device to driver for the an API call to access ctl_device call */
				//struct os_device_handlecb {
				//	const struct os_devicecb *device;					/* ptr to the device specific control block */
				//	const struct os_driver_handle_cb *driver;			/* ptr to the driver for the device */
				//}; 				
				
				//const struct os_device_handlecb os_deviceh_<index> = { &os_device_<index>, &os_drivers[<index>] };
				write(CONST_STRUCT+" "+DEVICE_HANDLE_CB_TYPE+" "+DEVICE_HANDLE_CB_NAME+"_"+device.getControlBlockIndex()+" = {");
				writeln(comment("Device handle control-block for '"+device.getName()+"' device"));
				incTabs();
				writeln("("+CONST_STRUCT+" "+DEVICE_CB_TYPE+" *)&"+DEVICE_CB_NAME+"_"+device.getControlBlockIndex()+", ");
				writeln("&"+DRIVER_HANDLE_CB_NAME+"["+device.getDriver().getControlBlockIndex()+"]");
				decTabs();
				writeln("};");
				writeNL();
			}
		}
		
		writeNL();
	}
	
	/**
	 * Helper function that generates all the code required for each counter defined within the 
	 * current OS model.
	 * 
	 * See wiki1042
	 * 
	 * Generates:
	 *  1. the driver specific code for each counter device
	 *  2. variable value to represent the number of counters
	 *  3. the first and last counter pointer values for handle range checking
	 *  4. the RAM based dynamic counter control block variable for each non-singleton counter
	 *  5. the ROM based counter control blocks for each counter
	 *  6. the system counter constant access variables 
	 */
	private void generateCounters() {

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate the driver specific code for each counter device
		
		// Create set of all TargetDevice instances that are used by the counters
		Collection<TargetDevice> allDevices = new HashSet<TargetDevice>();
		
		for (TargetCounter next : targetModel.getTargetCounters()) {
			
			TargetDevice device = next.getDevice();
			
			if ( device != null ) {
				allDevices.add(device);				// if device already in set then not added again!
			}
		}		
		
		if ( allDevices.size() > 0 ) {
			
			writeln(comment("Define the device specific data for each counter"));
			writeNL();
			
			for (TargetDevice device : allDevices) {

				// use device's driver class to generate the required code
				device.getDriver().genCInitCode(device);
			}
			writeNL();
		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2.Generate variable value to represent the number of counters

		int numCounters = targetModel.getTargetCounters().size();
		
		writeln(comment("Variable value to represent the number of counters."));
		writeln(CONST_UNAT+" "+COUNTER_NUM_NAME+" = "+numCounters+"U;");
		
		///////////////////////////////////////////////////////////////////////////////
		// 3. Generate the first and last counter pointer values for handle range checking
		if ( targetModel.isExtendedStatus() ) {
	
			writeNL();
			writeln(comment("Variable values to allow validity checking of counter handles."));

			if ( numCounters > 0 ) {
				writeln(CONST+" "+COUNTER_H+" "+FIRST_COUNTER_NAME+" = &"+platformInfo.getCounterCBName()+"[0];");
				writeln(CONST+" "+COUNTER_H+" "+LAST_COUNTER_NAME+" = &"+platformInfo.getCounterCBName()+"["+(numCounters-1)+"];");
			}
			else {
				writeln(CONST+" "+COUNTER_H+" "+FIRST_COUNTER_NAME+" = 0;");
				writeln(CONST+" "+COUNTER_H+" "+LAST_COUNTER_NAME+" = 0;");				
			}		
		}
		
		writeNL();
			
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Generate the RAM based dynamic counter control block variable for each counter

		writeln(comment("Define the RAM based counter control blocks for counters"));
		
		for (TargetCounter next : targetModel.getTargetCounters()) {
					
			// static struct os_countercb_dyn os_countercb_dyn_<n>;
			
			writeln(STATIC+" "+STRUCT+" "+COUNTER_DYN_CB_TYPE+" "+COUNTER_DYN_NAME+"_"+next.getControlBlockIndex()+";");
		}
		
		writeNL();
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. Generate the ROM based counter control blocks each counter

		if (numCounters > 0) {
			
			writeln(comment("Define the ROM based part of the counter control blocks."));
			
			writeln(CONST_STRUCT+" "+COUNTER_CB_TYPE+" "+platformInfo.getCounterCBName()+"[] = {");
			incTabs();
						
			initComma(numCounters);
			
			for (TargetCounter next : targetModel.getTargetCounters()) {
				
				write("{");
				
				incTabs();				
				
				TargetDevice device = next.getDevice();
							
				append(comment("Counter control block for counter "+next.getName()));
				writeNL();
						
				//////////////////////////////////////////////////////////
				// struct os_countercb_dyn *dyn;
				write("&"+COUNTER_DYN_NAME+"_"+next.getControlBlockIndex()+",");
				append(verboseComment("struct os_countercb_dyn *dyn;"));
				writeNL();				
				
				//////////////////////////////////////////////////////////
				// AlarmBaseType alarmbase;
				write("{"+next.getMaxAllowedValue()+"U, "+next.getTicksPerBase()+"U, "+next.getMinCycle()+"U},");
				append(verboseComment("{ maxallowedvalue, ticksperbase, mincycle }"));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// AlarmType singleton_alarm;
				if ( next.isSingleton() ) {
					
					TargetAlarm alarm = next.getSingleAlarm();
					
					write(getTargetElementArrayReference(alarm)+",");
					
					append(verboseComment("AlarmType singleton_alarm; [singleton]"));
				}
				else {
					write("0,");
					append(verboseComment("AlarmType singleton_alarm; [non-singleton]"));
				}
				writeNL();
				
				//////////////////////////////////////////////////////////
				// AlarmType (*expired)(CounterType);
				if ( next.isSingleton() ) {
					write(COUNTER_EXP_SINGLE_F_NAME+",");
				}
				else {
					write(COUNTER_EXP_MULTI_F_NAME+",");
				}
				append(verboseComment("AlarmType (*expired)(CounterType);"));
				writeNL();
					
				//////////////////////////////////////////////////////////
				// void (*setrelalarm)(CounterType, AlarmType, os_longtick, TickType);	
				if ( next.isSingleton() ) {
					write(COUNTER_SETREL_SINGLE_F_NAME+",");
				}
				else {
					write(COUNTER_SETREL_MULTI_F_NAME+",");
				}
				append(verboseComment("void (*setrelalarm)(CounterType, AlarmType, os_longtick, TickType);"));
				writeNL();

				//////////////////////////////////////////////////////////
				// void (*cancel)(AlarmType, CounterType);	
				if ( next.isSingleton() ) {
					write(COUNTER_CANCEL_SINGLE_F_NAME+",");
				}
				else {
					write(COUNTER_CANCEL_MULTI_F_NAME+",");
				}
				append(verboseComment("void (*cancel)(AlarmType, CounterType);"));
				writeNL();				
					
				//////////////////////////////////////////////////////////
				// const struct os_devicecb *device;  /* Hardware-specific device control block pointer; this is cast to appropriate concrete type by driver functions */
				
				if ( device != null ) {
					// (const struct os_devicecb *)&os_device_<index>
					write("("+CONST_STRUCT+" "+DEVICE_CB_TYPE+" *)&"+DEVICE_CB_NAME+"_"+device.getControlBlockIndex()+",");
					append(verboseComment("const struct os_devicecb *device;"));
				}
				else {
					write("0,");
					append(verboseComment("[UNKNOWN DEVICE]"));
				}
				
				writeNL();
				
				///////////////////////////////////////////////////////////////////////////////////////
				// const struct os_counter_drivercb *driver;      /* Driver to handle counter device */
				if ( device != null ) {
					// &os_driver_<index>
					write("&"+DRIVER_CB_NAME+"_"+device.getDriver().getControlBlockIndex());
					append(verboseComment("const struct os_counter_drivercb *driver;"));
				}
				else {
					write("0");
					append(verboseComment("[UNKNOWN DRIVER]"));
				}

				writeNL();
		
				decTabs();			
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}
		else {
			writeln(comment("Define an empty ROM based array for counter control blocks, since no counters defined."));
			writeln(CONST_STRUCT+" "+COUNTER_CB_TYPE+" "+platformInfo.getCounterCBName()+"[] = {};");
			
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////////////////
		// 6. Generate the system counter constant access variables 
		// (see section 13.6.4 Constants of OSEK OS spec.) 
		
		TargetCounter systemCounter = targetModel.getTargetSystemCounter();

		if ( systemCounter != null ) {
		
			writeln(comment("Define the system counter constant access variables."));
			
			/* $Req: artf1206 $ */
			
			//const TickType OSMAXALLOWEDVALUE = <value>U;
			writeln(CONST+" "+TICK_TYPE+" "+OSMAXALLOWEDVALUE+" = "+systemCounter.getMaxAllowedValue()+"U;");
			
			//const TickType OSTICKSPERBASE = <value>U;
			writeln(CONST+" "+TICK_TYPE+" "+OSTICKSPERBASE+" = "+systemCounter.getTicksPerBase()+"U;");
			
			//const TickType OSMINCYCLE = <value>U;
			writeln(CONST+" "+TICK_TYPE+" "+OSMINCYCLE+" = "+systemCounter.getMinCycle()+"U;");
			
			//const <platformType> OSTICKDURATION = <value>U;		/* $Req: artf1207 $ */
			writeln(CONST+" "+platformInfo.getTickDurationType()+" "+OSTICKDURATION+" = "+systemCounter.getNanosecondsPerTick()+"U;");
			
			writeNL();
		}
	}
	
	/**
	 * Helper function that generates all the code required for each schedule table defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  1. variable value to represent the number of schedule tables
	 *  2. the first and last schedule table pointer values for handle range checking
	 *  3. the ROM based expiry action processing data for each action, within each expiry point, within each table
	 *  4. the ROM based array of expiry point actions for each expiry point in each table
	 *  5. the ROM based array of expiry point control blocks for each expiry point in each table
	 *  6. the RAM based array of dynamic control block variables for the tables
	 *  7. the ROM based schedule table control blocks for each schedule table
	 */
	private void generateScheduleTables() {
	
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate variable value to represent the number of schedule tables

		int numTables = targetModel.getTargetScheduleTables().size();
				
		writeln(comment("Variable value to represent the number of schedule tables."));
		writeln(CONST_UNAT+" "+SCHEDTAB_NUM_NAME+" = "+numTables+"U;");
		writeNL();
		
		///////////////////////////////////////////////////////////////////////////////
		// 2. Generate the first and last schedule table pointer values for handle range checking
		if ( targetModel.isExtendedStatus() ) {
	
			writeNL();
			writeln(comment("Variable values to allow validity checking of schedule table handles."));

			if ( numTables > 0 ) {
				writeln(CONST+" "+SCHEDTAB_H+" "+FIRST_SCHEDTAB_NAME+" = &"+platformInfo.getScheduleTableCBName()+"[0];");
				writeln(CONST+" "+SCHEDTAB_H+" "+LAST_SCHEDTAB_NAME+" = &"+platformInfo.getScheduleTableCBName()+"["+(numTables-1)+"];");
			}
			else {
				writeln(CONST+" "+SCHEDTAB_H+" "+FIRST_SCHEDTAB_NAME+" = 0;");
				writeln(CONST+" "+SCHEDTAB_H+" "+LAST_SCHEDTAB_NAME+" = 0;");				
			}		
		}
		
		writeNL();	

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. Generate the ROM based expiry action processing data for each action, within each expiry point, within each table

		if (numTables > 0) {
			
			writeln(comment("Define the ROM based expiry action processing data for each expiry point action."));
				
			for (TargetScheduleTable nextTable : targetModel.getTargetScheduleTables()) {

				int index = 1;	// xp index, used in verbose comment only
				
				for (TargetScheduleTableXP expiryPoint : nextTable.getExpiryPoints()) {
				
					writeln(verboseComment("expiry action data for table "+nextTable.getName()+", expiry point "+index));
					for (TargetExpiry action : expiryPoint.getExpiryActions()) {
						
						generateAlarmExpiryFunctionData(action);
						writeNL();
					}
					
					index++;
				}
			}
			
			writeNL();
		}		
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Generate the ROM based array of expiry point actions for each expiry point in each table
		
		if (numTables > 0) {
			writeln(comment("Define the ROM based part of the schedule table's expiry point actions."));

			for (TargetScheduleTable nextTable : targetModel.getTargetScheduleTables()) {
				
				int index = 0;	// XP index number used for verbose comment only
				
				for (TargetScheduleTableXP expiryPoint : nextTable.getExpiryPoints()) {
				
					Collection<TargetExpiry> actions = expiryPoint.getExpiryActions();
					
					index++;
					
					if ( actions.size() > 0 ) {
						// static const struct os_xpoint_element os_xpoint_actions_<xpIndex> [] = { {}, {}, {}... };					
					
						writeln(STATIC+" "+CONST_STRUCT+" "+EXPIRY_ACTION_CB_TYPE+" "+EXPIRY_ACTION_CB_NAME+"_"+expiryPoint.getUniqueIndex()+"[] = {");
						
						incTabs();
			
						initComma(actions.size());
						
						for (TargetExpiry action : actions) {
							
							writeln("{");
							incTabs();						
							
							///////////////////////////////////////////////////////////
							// void (*process)(const struct os_expirycb *);
							generateAlarmExpiryFunctionRef(action);
							append(",");
							
							append(verboseComment("void (*process)(const struct os_expirycb *);"));
							writeNL();
	
							///////////////////////////////////////////////////////////
							// const struct os_expirycb *action;	/* ptr to expiry action data passed to the function that processes the alarm expiry */
							generateExpiryFunctionDataRef(action);
							writeNL();
							
							decTabs();
							write("}");					
							doComma();
							writeNL();
						}
						decTabs();
						write("};");
						append(verboseComment("Expiry point actions for schedule table "+nextTable.getName()+", expiry point "+index));
						writeNLs(2);					
					}
				}
			}
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. Generate the ROM based array of expiry point control blocks for each expiry point in each table

		if (numTables > 0) {
			writeln(comment("Define the ROM based part of the schedule table's expiry point control blocks."));
			
			for (TargetScheduleTable next : targetModel.getTargetScheduleTables()) {
				
				// static const struct os_xpoint os_xpoints_<tableIndex> [] = { ... };
				
				writeln(STATIC+" "+CONST_STRUCT+" "+EXPIRY_CB_TYPE+" "+EXPIRY_CB_NAME+"_"+next.getControlBlockIndex()+"[] = {");
				incTabs();
				
				Collection<TargetScheduleTableXP> expiryPoints = next.getExpiryPoints();
				
				initComma(expiryPoints.size());
				
				int arrayIndex = 0;	// index position of each generated expiry point
				
				for (TargetScheduleTableXP expiryPoint : expiryPoints) {

					writeln("{");
					incTabs();
					
					arrayIndex++;
					
					///////////////////////////////////////////////////
					// const struct os_xpoint *next_xpoint;
					if ( arrayIndex < expiryPoints.size() ) {
						// need to reference next expiry point in the list (array)
						write("&"+EXPIRY_CB_NAME+"_"+next.getControlBlockIndex()+"["+arrayIndex+"],");
					}
					else {
						write("&"+EXPIRY_CB_NAME+"_"+next.getControlBlockIndex()+"[0],");	// last point, so point back to first point
					}
					append(verboseComment("const struct os_xpoint *next_xpoint;"));
					writeNL();
					
					///////////////////////////////////////////////////
					//	TickType delta;
					write(expiryPoint.getDelta()+"U,");
					append(verboseComment("TickType delta;"));
					writeNL();
					
					///////////////////////////////////////////////////
					//	const struct os_xpoint_element *actions;	/* Array of actions */
					if ( expiryPoint.getExpiryActions().size() > 0 ) {
						write("&"+EXPIRY_ACTION_CB_NAME+"_"+expiryPoint.getUniqueIndex()+"[0],");
					}
					else {
						write("0,");
					}
					
					append(verboseComment("const struct os_xpoint_element *actions;"));
					writeNL();
					
					///////////////////////////////////////////////////
					//	unat num_actions;							/* Number of actions */
					write(expiryPoint.getExpiryActions().size()+"U");
					append(verboseComment("unat num_actions;"));					
					writeNL();
					
					decTabs();
					write("}");					
					doComma();
					writeNL();	
				}
				
				decTabs();
				write("};");
				append(verboseComment("Expiry points for schedule table "+next.getName()));
				writeNL();
			}
			
			writeNL();
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 6. Generate the RAM based array of dynamic control block variables for the tables
		
		if ( numTables > 0 ) {
			writeln(comment("Define the RAM based schedule table control block variables"));
			
			// static struct os_schedtabcb_dyn os_schedtab_dyn[<numTables>];
			// writeln(STATIC+" "+STRUCT+" "+SCHEDTAB_DYN_CB_TYPE+" "+SCHEDTAB_DYN_NAME+"["+numTables+"];");
			
			for (TargetScheduleTable nextTable : targetModel.getTargetScheduleTables()) {
				
				if ( nextTable.isAutostarted() ) {
					// table is autostarted, so create initialised RAM variable.
					// static struct os_schedtabcb_dyn os_schedtab_dyn_<tableIndex> = {0, &first_xp[0],0 };
					write(STATIC+" "+STRUCT+" "+SCHEDTAB_DYN_CB_TYPE+" "+SCHEDTAB_DYN_NAME+"_"+nextTable.getControlBlockIndex()+" = { 0, 0, ");					
					append("&"+EXPIRY_CB_NAME+"_"+nextTable.getControlBlockIndex()+"[0], 0 };");
				}
				else {
					// table is not autostarted, so create uninitialised RAM variable.
					// static struct os_schedtabcb_dyn os_schedtab_dyn_<tableIndex>;
					write(STATIC+" "+STRUCT+" "+SCHEDTAB_DYN_CB_TYPE+" "+SCHEDTAB_DYN_NAME+"_"+nextTable.getControlBlockIndex()+";");
				}
				append(verboseComment("unat first_run, unat nexted, const struct os_xpoint *current_xpoint, ScheduleTableType next_tab"));
				writeNL();				
			}
			
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 7. Generate the ROM based array of schedule table control blocks for each table

		if (numTables > 0) {
			
			writeln(comment("Define the ROM based part of the schedule table control blocks."));
			
			writeln(CONST_STRUCT+" "+SCHEDTAB_CB_TYPE+" "+platformInfo.getScheduleTableCBName()+"[] = {");
			incTabs();
						
			initComma(numTables);
			
			for (TargetScheduleTable next : targetModel.getTargetScheduleTables()) {
				
				writeln("{");
				
				incTabs();				
							
				writeln(comment("Schedule Table control block for table "+next.getName()));
										
				/////////////////////////////////
				// AlarmType alarm; /* internal alarm that drives the table */
				write(getTargetElementArrayReference(next.getInternalAlarm())+",");
				append(verboseComment("// AlarmType alarm;"));
				writeNL();
				
				/////////////////////////////////
				// struct os_schedtabcb_dyn *dyn;
				write("&"+SCHEDTAB_DYN_NAME+"_"+next.getControlBlockIndex()+",");
				append(verboseComment("struct os_schedtabcb_dyn *dyn;"));
				writeNL();				
				
				///////////////////////////////////////////////
				// const struct os_xpoint *first_xpoint;			                        
				write("&"+EXPIRY_CB_NAME+"_"+next.getControlBlockIndex()+"[0],");
				append(verboseComment("const struct os_xpoint *first_xpoint;"));
				writeNL();
	
				/////////////////////////////////
				// unat periodic;
				write( toFlag(next.isPeriodic()) );

				append(verboseComment("unat periodic;"));
				writeNL();				

				decTabs();			
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}
		else {
			writeln(comment("Define an empty ROM based array for schedule table control blocks, since no tables defined."));
			writeln(CONST_STRUCT+" "+SCHEDTAB_CB_TYPE+" "+platformInfo.getScheduleTableCBName()+"[] = {};");
			
			writeNL();
		}		
	}
	
	/**
	 * Helper function that generates all the code required for each alarm defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  1. variable value to represent the number of alarms
	 *  2. the first and last alarm pointer values for handle range checking
	 *  3. the RAM based dynamic common alarm control block variable for each alarm  
	 *  4. the RAM based dynamic alarm control block variable for (multi) non-singleton alarm only
	 *  5. the ROM based expiry action processing data for the expiry action, within each alarm.
	 *  6. the ROM based alarm control blocks for each alarm
	 */
	private void generateAlarms() {
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate variable value to represent the number of alarms

		int numAlarms = targetModel.getTargetAlarms().size();
			
		writeln(comment("Variable value to represent the number of alarms."));
		writeln(CONST_UNAT+" "+ALARM_NUM_NAME+" = "+numAlarms+"U;");
		writeNL();
		
		///////////////////////////////////////////////////////////////////////////////
		// 2. Generate the first and last alarm pointer values for handle range checking
		
		if ( targetModel.isExtendedStatus() ) {
	
			writeNL();
			writeln(comment("Variable values to allow validity checking of alarm handles."));

			if ( numAlarms > 0 ) {
				writeln(CONST+" "+ALARM_H+" "+FIRST_ALARM_NAME+" = &"+platformInfo.getAlarmCBName()+"[0];");
				writeln(CONST+" "+ALARM_H+" "+LAST_ALARM_NAME+" = &"+platformInfo.getAlarmCBName()+"["+(numAlarms-1)+"];");
			}
			else {
				writeln(CONST+" "+ALARM_H+" "+FIRST_ALARM_NAME+" = 0;");
				writeln(CONST+" "+ALARM_H+" "+LAST_ALARM_NAME+" = 0;");				
			}		
		}
		
		writeNL();
	
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3. the RAM based dynamic common alarm control block variable for each alarm
	
		if ( numAlarms > 0 ) {
			writeln(comment("Define the RAM based alarm control blocks common to all alarms"));
			
			// static struct os_alarmcb_common_dyn os_alarm_c_dyn[<numAlarms>];
			writeln(STATIC+" "+STRUCT+" "+ALARM_C_DYN_CB_TYPE+" "+ALARM_C_DYN_NAME+"["+numAlarms+"];");
				
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Generate the RAM based dynamic alarm control block variable for (multi) non-singleton alarm only

		Collection<TargetAlarm> singletonAlarms = targetModel.getTargetNonSingletonAlarms();
		
		if ( singletonAlarms.size() > 0 ) {
			writeln(comment("Define the RAM based alarm control blocks for non-singleton (multi) alarms"));
			
			for (TargetAlarm next : singletonAlarms) {
						
				// static struct os_alarmcb_multi_dyn os_alarm_m_dyn_<n>;			
				writeln(STATIC+" "+STRUCT+" "+ALARM_M_DYN_CB_TYPE+" "+ALARM_M_DYN_NAME+"_"+next.getControlBlockIndex()+";");
			}
			
			writeNL();
		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. Generate the ROM based expiry action processing data for the expiry action, within each alarm.

		if (numAlarms > 0) {
			
			writeln(comment("Define the ROM based expiry action processing data for each alarm."));
				
			for (TargetAlarm next : targetModel.getTargetAlarms()) {

				generateAlarmExpiryFunctionData(next.getExpiryAction());
				append(verboseComment("expiry action processing data for alarm "+next.getName()));
				this.writeNLs(2);
			}
			
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 6. Generate the ROM based array of alarm control blocks for each alarm

		if (numAlarms > 0) {
			
			writeln(comment("Define the ROM based part of the alarm control blocks."));
			
			writeln(CONST_STRUCT+" "+ALARM_CB_TYPE+" "+platformInfo.getAlarmCBName()+"[] = {");
			incTabs();
						
			initComma(numAlarms);
			
			for (TargetAlarm next : targetModel.getTargetAlarms()) {
				
				writeln("{");
				
				incTabs();				
							
				writeln(comment("Alarm control block for alarm "+next.getName()));
					
				//////////////////////////////////////////////////////////
				// 	struct {
				// 		struct os_alarmcb_multi_dyn *m;				/* 0 if alarm is a singleton */
				// 		struct os_alarmcb_common_dyn *c;
				//  } dyn;
				writeln("{");
				incTabs();
				
				if ( next.isSingleton() ) {
					write("0,");
					append(verboseComment("struct os_alarmcb_multi_dyn *m; [singleton]"));
				}
				else {
					write("&"+ALARM_M_DYN_NAME+"_"+next.getControlBlockIndex()+",");
					append(verboseComment("struct os_alarmcb_multi_dyn *m; [non-singleton]"));
				}				
				writeNL();
				
				write("&"+ALARM_C_DYN_NAME+"["+next.getControlBlockIndex()+"]");
				append(verboseComment("struct os_alarmcb_common_dyn *c;"));
				writeNL();				
				
				decTabs();
				writeln("},");
	
				//////////////////////////////////////////////////////////
				// void (*process)(const struct os_expirycb *);		/* Function programmed to process the alarm action */
				
				generateAlarmExpiryFunctionRef(next.getExpiryAction());
	
				append(",");
				append(verboseComment("void (*process)(const struct os_expirycb *);"));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// CounterType counter;
				TargetCounter counter = next.getTargetCounter();
				
				write(getTargetElementArrayReference(counter)+",");
				append(verboseComment("CounterType counter;"));
				writeNL();
				
				//////////////////////////////////////////////////////////
				// const struct os_expirycb *action;	/* ptr to expiry action data passed to the function that processes the alarm expiry */
				generateExpiryFunctionDataRef(next.getExpiryAction());
				writeNL();
				
				decTabs();			
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			
			writeNL();
		}
		else {
			writeln(comment("Define an empty ROM based array for alarm control blocks, since no alarms defined."));
			writeln(CONST_STRUCT+" "+ALARM_CB_TYPE+" "+platformInfo.getAlarmCBName()+"[] = {};");
			
			writeNL();
		}		
	}
	
	/**
	 * Helper function that writes the text required to define an appropriate data variable required 
	 * by the alarm expiry function for the given TargetExpiry.
	 * 
	 * @param expiryAction the TargetExpiry for which to generate the function data
	 */
	private void generateExpiryFunctionDataRef(TargetExpiry expiryAction) {
		
		if ( expiryAction.activatesTask() ) {
			write("("+CONST_STRUCT+" "+ALARM_EXPIRY_CB_TYPE+" *)&"+HANDLE_REF_TASK_NAME+"_"+expiryAction.getActivatedTask().getControlBlockIndex());
		}
		else {
			write("("+CONST_STRUCT+" "+ALARM_EXPIRY_CB_TYPE+" *)&"+ALARM_ACTION_CB_NAME+"_"+expiryAction.getUniqueIndex());
		}
		append(verboseComment("const struct os_expirycb *action;"));
	}	
	
		
	/**
	 * Helper function that writes the text required to define an appropriate data variable required 
	 * by the alarm expiry function for the given TargetExpiry.
	 * 
	 * @param expiryAction the TargetExpiry for which to generate the function data
	 */
	private void generateAlarmExpiryFunctionData(TargetExpiry expiryAction) {
		
		if ( !expiryAction.activatesTask() ) {
			String expiryCBTypeName = null;
			
			// The actual type of the expiry control block struct depends on the expiry action type
			if ( expiryAction.callsHandler() ) {
				expiryCBTypeName = ALARM_EXPIRY_CALLBACK_CB_TYPE;
			}
			else if ( expiryAction.incrementsCounter() ) {
				expiryCBTypeName = ALARM_EXPIRY_INCCOUNTER_CB_TYPE;
			}
			else if ( expiryAction.setsEvent() ) {
				expiryCBTypeName = ALARM_EXPIRY_SETEVENT_CB_TYPE;
			}
			else if ( expiryAction.drivesScheduleTable() ) {
				expiryCBTypeName = ALARM_EXPIRY_SCHEDULETAB_CB_TYPE;
			}
			
			assert expiryCBTypeName != null;	// an expiry action must be specified
	
			// generate the variable type declaration:
			// static const struct <expiryCB_type_name> ALARM_ACTION_CB_NAME_<actionIndex> = { 
			writeln(STATIC+" "+CONST_STRUCT+" "+expiryCBTypeName+" "+ALARM_ACTION_CB_NAME+"_"+expiryAction.getUniqueIndex()+" = {");	
			
			incTabs();
			// now generate the appropriate struct initialisation contents depending on the 
			// type of expiry.
				
			if ( expiryAction.callsHandler() ) {
				write(ALARMCALLBACK_PREFIX + expiryAction.getCallbackName());
				append(verboseComment("os_callbackf callback;"));
			}
			else if ( expiryAction.incrementsCounter() ) {
				write(getTargetElementArrayReference(expiryAction.getIncrementedCounter()));
				append(verboseComment("CounterType counter;"));
			}
			else if ( expiryAction.setsEvent() ) {
				
				BigInteger mask = expiryAction.getSetEvent().getTargetMask();	
				write("0x"+mask.toString(16)+"U,");
				append(verboseComment("EventMaskType event;"));
				writeNL();					
				
				write(getTargetElementArrayReference(expiryAction.getActivatedTask()));
				append(verboseComment("TaskType task;"));					
			}
			else if ( expiryAction.drivesScheduleTable() ) {
				write(getTargetElementArrayReference(expiryAction.getDrivenTable()));
				append(verboseComment("ScheduleTableType schedtable;"));
			}	
			
			writeNL();
			decTabs();
			write("};");
		}
	}
	
	
	
	/**
	 * Helper function that returns the text required to specify the correct COM notification enumeration 
	 * for a given TargetExpiry.
	 * 
	 * @param notificationAction the {@link TargetExpiry} for which to generate enum text
	 * @return the correct COM notification enumeration string for the given {@link TargetExpiry}.
	 */	
	public static String getNotificationEnumString(TargetExpiry notificationAction) {

		String notificationEnum;
		
		if ( notificationAction != null ) {
			
			if ( notificationAction.activatesTask() ) {
				notificationEnum = OSAnsiCGenerator.COM_NOTIFY_ACTIVATE_TASK;
			}
			else if ( notificationAction.setsEvent() ) {
				notificationEnum = OSAnsiCGenerator.COM_NOTIFY_SET_EVENT;
			}
			else if ( notificationAction.callsHandler() ) {
				notificationEnum = OSAnsiCGenerator.COM_NOTIFY_CALLBACK;
			}
			else {
				// anything else, including SET FLAG is recorded as NOTIFY_NONE (since flag always set anyway)
				notificationEnum = OSAnsiCGenerator.COM_NOTIFY_NONE;
			}					
		}
		else {
			// no notification for this message
			notificationEnum = OSAnsiCGenerator.COM_NOTIFY_NONE;
		}
		
		return notificationEnum;
	}
	
	
	/**
	 * Helper function that returns the text required to declare a COM notification data variable.
	 * 
	 * To reference the created variable use {@link #getCOMNotificationFunctionDataRef(TargetExpiry)}
	 * 
	 * @param notificationAction the {@link TargetExpiry} for which to generate the function data, null if no notification done
	 * @return the COM notification data variable string for the given {@link TargetExpiry}, null if no notification required
	 */
	public static String getCOMNotificationFunctionData(TargetExpiry notificationAction) {
			
		if ( notificationAction != null ) {
			
			StringBuffer notificationVar = new StringBuffer();
			
			String notifyCBTypeName = null;
			
			// The actual type of the notification control block struct depends on the notification action type
			if ( notificationAction.callsHandler() ) {
				notifyCBTypeName = COM_NOTIFICATION_CALLBACK_CB_TYPE;
			}
			else if ( notificationAction.setsEvent() ) {
				notifyCBTypeName = COM_NOTIFICATION_SETEVENT_CB_TYPE;
			}
			else if ( notificationAction.activatesTask() ) {
				notifyCBTypeName = COM_NOTIFICATION_ACTTASK_CB_TYPE;
			}			
			
			assert notifyCBTypeName != null;	// a notification action must be specified
	
			// generate the variable type declaration:
			// static const struct <notifyCB_type_name> COM_NOTIFY_CB_NAME<actionIndex> = { <notify_data> };
			
			notificationVar.append(STATIC+" "+CONST_STRUCT+" "+notifyCBTypeName+" "+COM_NOTIFY_CB_NAME+"_"+notificationAction.getUniqueIndex()+" = {");	
			
			// now generate the appropriate struct initialisation contents depending on the 
			// type of notification.
						
			if ( notificationAction.callsHandler() ) {
				notificationVar.append(notificationAction.getCallbackName());
			}
			else if ( notificationAction.setsEvent() ) {			
				BigInteger mask = notificationAction.getSetEvent().getTargetMask();	
				notificationVar.append("0x"+mask.toString(16)+"U,");
				notificationVar.append(getTargetElementArrayReference(notificationAction.getActivatedTask()));			
			}
			else if ( notificationAction.activatesTask() ) {
				notificationVar.append(getTargetElementArrayReference(notificationAction.getActivatedTask()));
			}			

			notificationVar.append("};");
			
			return notificationVar.toString();
		}
		
		return null;
	}
	
	/**
	 * Helper function that returns the text required to reference a COM notification data variable 
	 *
	 * This references the variable created using {@link #getCOMNotificationFunctionData(TargetExpiry)}
	 *
	 * @param notificationAction the {@link TargetExpiry} for which to generate the function reference
	 * @return the COM notification data variable reference string for the given {@link TargetExpiry}.
	 */
	public static String getCOMNotificationFunctionDataRef(TargetExpiry notificationAction) {
		
		String notificationRef;
		
		if ( notificationAction != null ) {
			if ( notificationAction.activatesTask() || notificationAction.setsEvent() || notificationAction.callsHandler() ) {
				notificationRef = ("("+OSAnsiCGenerator.CONST_STRUCT+" "+OSAnsiCGenerator.COM_NOTIFICATION_CB_TYPE+" *)&"+OSAnsiCGenerator.COM_NOTIFY_CB_NAME+"_"+notificationAction.getUniqueIndex());
			}
			else {
				// sets flag, so no data required since handler function not called
				notificationRef = "0";
			}
		}
		else {
			// no notification, so no data required
			notificationRef = "0";
		}

		return notificationRef;
	}	
	
	/**
	 * Helper function that writes the text required to reference the appropriate alarm expiry function
	 * required for the given TargetExpiry
	 * 
	 * @param expiryAction the TargetExpiry for which to generate the function ref
	 */
	private void generateAlarmExpiryFunctionRef(TargetExpiry expiryAction) {
		
		if ( expiryAction.activatesTask() ) {
			write(ALARM_EXPIRY_ACTTASK_F_NAME);
		}
		else if ( expiryAction.callsHandler() ) {
			write(ALARM_EXPIRY_CALLBACK_F_NAME);
		}
		else if ( expiryAction.incrementsCounter() ) {
			write(ALARM_EXPIRY_INCCOUNTER_F_NAME);
		}
		else if ( expiryAction.setsEvent() ) {
			write(ALARM_EXPIRY_SETEVENT_F_NAME);
		}
		else if ( expiryAction.drivesScheduleTable() ) {
			write(ALARM_EXPIRY_SCHEDULETAB_F_NAME);
		}		
	}

	
	/**
	 * Helper function that generates all the code required for each app mode defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  1. A ROM based array of task handles to reference each task in each app mode.
	 *  2. A ROM based array of control blocks for each autostarted alarm in each app mode
	 *  3. The ROM based resource control blocks for each AppMode
	 */	
	private void generateAppModes()	{
		
		//////////////////////////////////////////////////////////////////////////////////
		// 1. A ROM based array of task handles to reference each autostarted task each app mode. 
		
		for (TargetAppMode next : targetModel.getTargetAppModes()) {
				
			Collection<TargetTask> autostartedTasks = next.getTargetTasks();
			
			if (autostartedTasks.size() > 0) {
							
				writeln(comment("An array of task handles for the app mode "+next.getName()));
				writeln(STATIC+" "+TASK_H+" "+CONST+" "+APPMODE_TASK_ARRAY_PREFIX+next.getControlBlockIndex()+"[] = {");
				
				incTabs();
				
				initComma(autostartedTasks.size());
				
				for (TargetTask task : autostartedTasks) {
					
					write(getTargetElementArrayReference(task));
					doComma();
					append(verboseComment(task.getName()));
					writeNL();
				}
				decTabs();
				writeln("};");
			}
		}
		
		writeNL();		
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		// 2. Generate A ROM based array of control blocks for each auto started alarm in each app mode
	
		for (TargetAppMode next : targetModel.getTargetAppModes()) {
					
			// do non-singleton alarms
			Collection<TargetAlarm> autostartedMultiAlarms = next.getTargetNonSingletonAlarms();
			
			if (autostartedMultiAlarms.size() > 0) {
							
				writeln(comment("An array of control blocks for autostarted (non-singleton) alarms for the app mode "+next.getName()));
				writeln(STATIC+" "+CONST_STRUCT+" "+AUTO_ALARM_CB_TYPE+" "+AUTO_ALARM_M_CB_NAME+"_"+next.getControlBlockIndex()+"[] = {");
				
				incTabs();
				
				initComma(autostartedMultiAlarms.size());
				
				for (TargetAlarm alarm : autostartedMultiAlarms) {
					
					// { <alarmID>, <rel>, <cycle> }
					writeln("{");
					incTabs();
					
					write(getTargetElementArrayReference(alarm)+",");			
					append(verboseComment("AlarmType alarm;"));
					writeNL();
					
					write(alarm.getAlarmTime()+"U,");			
					append(verboseComment("TickType rel;"));
					writeNL();
					
					write(alarm.getCycleTime()+"U");			
					append(verboseComment("TickType cycle;"));
					writeNL();					
					
					decTabs();
					write("}");				
					doComma();
					append(verboseComment(alarm.getName()));
					writeNL();
				}
				decTabs();
				writeln("};");
			}
			
			// do singleton alarms
			Collection<TargetAlarm> autostartedSingletonAlarms = next.getTargetSingletonAlarms();
			
			if (autostartedSingletonAlarms.size() > 0) {
							
				writeln(comment("An array of control blocks for autostarted (singleton) alarms for the app mode "+next.getName()));
				writeln(STATIC+" "+CONST_STRUCT+" "+AUTO_ALARM_CB_TYPE+" "+AUTO_ALARM_S_CB_NAME+"_"+next.getControlBlockIndex()+"[] = {");
				
				incTabs();
				
				initComma(autostartedSingletonAlarms.size());
				
				for (TargetAlarm alarm : autostartedSingletonAlarms) {
					
					// { <alarmID>, <rel>, <cycle> }
					writeln("{");
					incTabs();
					
					write(getTargetElementArrayReference(alarm)+",");			
					append(verboseComment("AlarmType alarm;"));
					writeNL();
					
					write(alarm.getAlarmTime()+"U,");			
					append(verboseComment("TickType rel;"));
					writeNL();
					
					write(alarm.getCycleTime()+"U");			
					append(verboseComment("TickType cycle;"));
					writeNL();					
					
					decTabs();
					write("}");				
					doComma();
					append(verboseComment(alarm.getName()));
					writeNL();
				}
				decTabs();
				writeln("};");
			}
		}
		
		writeNL();		

		
		/////////////////////////////////////////////////////////////////////
		// 3. Generate the ROM based resource control blocks for each AppMode
		
		writeln(comment("The ROM based part of the app mode control blocks for all app modes."));
		
		writeln(CONST_STRUCT+" "+APPMODE_CB_TYPE+" "+platformInfo.getAppModeCBName()+"[] = {");
		incTabs();
		
		initComma(targetModel.getTargetAppModes().size());
		
		for (TargetAppMode next : targetModel.getTargetAppModes()) {
		
			writeln("{");
			
			incTabs();
			
			writeln(comment("App Mode control block for AppMode "+next.getName()));		
				
			////////////////////////////////////////////////////////////////////////////////////////
			// TaskType const *autotasks;
			
			if (next.getTargetTasks().size() > 0) {
				write(APPMODE_TASK_ARRAY_PREFIX+next.getControlBlockIndex()+",");
				append(verboseComment("TaskType const *autotasks;"));
			}
			else {
				write("0,");
				append(verboseComment("TaskType const *autotasks; no autostarted tasks"));
			}
			writeNL();	
			
			////////////////////////////////////////////////////////////////////////////////////////
			// const struct taskcb *nexttask;
			
			/* this member is no longer included in the appmode struct
			write(getTargetElementArrayReference(next.getHighestPriorityTask())+",");
			append(verboseComment("const struct taskcb *nexttask;"));
			writeNL();
			*/
			
			////////////////////////////////////////////////////////////////////////////////////////
			// unat numautotasks;
			
			write(next.getTargetTasks().size()+"U,");
			append(verboseComment("unat numautotasks;"));
			writeNL();
			
			////////////////////////////////////////////////////////////////////////////////////////
			// Generate autostarted alarm data

			Collection<TargetAlarm> multiAlarms = next.getTargetNonSingletonAlarms();
			Collection<TargetAlarm> singletonAlarms = next.getTargetSingletonAlarms();
			
			////////////////////////////////////////////////////////////////////////////////////////
			// const struct os_auto_alarm *auto_m_alarms;		/* ptr to array of auto started alarm instances (based on non-singleton counters) */
			
			if ( multiAlarms.size() > 0 ) {
				write(AUTO_ALARM_M_CB_NAME+"_"+next.getControlBlockIndex()+",");
			}
			else {
				write("0,");
			}
			append(verboseComment("const struct os_auto_alarm *auto_m_alarms;"));
			writeNL();			
			
			////////////////////////////////////////////////////////////////////////////////////////
			// unat num_auto_m_alarms;							/* the number of auto started non-singleton alarms */
			write(multiAlarms.size()+"U,");
			append(verboseComment("unat num_auto_m_alarms;"));
			writeNL();
			
			////////////////////////////////////////////////////////////////////////////////////////
			// const struct os_auto_alarm *auto_s_alarms;		/* ptr to array of auto started alarm instances (based on singleton counters) */
			
			if ( singletonAlarms.size() > 0 ) {
				write(AUTO_ALARM_S_CB_NAME+"_"+next.getControlBlockIndex()+",");
			}
			else {
				write("0,");
			}
			append(verboseComment("const struct os_auto_alarm *auto_s_alarms;"));
			writeNL();			
			
			////////////////////////////////////////////////////////////////////////////////////////
			// unat num_auto_s_alarms;							/* the number of auto started singleton alarms */
			write(singletonAlarms.size()+"U,");
			append(verboseComment("unat num_auto_s_alarms;"));
			writeNL();			

			////////////////////////////////////////////////////////////////////////////////////////
			// void (*start_alarms_multi)(AppModeType);		/* Function that autostarts multi-alarms  (empty function if none) */
			// 
			
			if ( multiAlarms.size() > 0 ) {
				write(AUTOSTART_ALARM_M_F_NAME+",");
			}
			else {
				write("0,");
			}
			append(verboseComment("void (*start_alarms_multi)(AppModeType);"));
			writeNL();				
			
			////////////////////////////////////////////////////////////////////////////////////////			
			// void (*start_alarms_singleton)(AppModeType);	/* Function that autostarts singleton alarms (empty function if none) */
			// 
			if ( singletonAlarms.size() > 0 ) {
				write(AUTOSTART_ALARM_S_F_NAME);
			}
			else {
				write("0");
			}	
			append(verboseComment("void (*start_alarms_singleton)(AppModeType);"));
			writeNL();			
			
			decTabs();
			
			write("}");	
			doComma();
			writeNL();
		}
		
		decTabs();
		writeln("};");
		writeNL();			
	}

	
	
	/**
	 * Helper function that generates all the code required for each Message defined within the 
	 * current OS COM sub-system model.
	 * 
	 * Generates:
	 * 
	 * 0. Variable values to represent COM configuration and message count details
	 * 1. The array of COM devices that provide start/stop functions
	 * 2. The last message pointer values for handle range checking (extended COM status only)
	 * 3. The RAM based notification Flag variables, inc. single scratch flag
	 * 4. The ROM based non-zero initial values defined for non-queued; non-zero length receiving message
	 * 5. The ROM based notification processing data for notifications within each receiving message
	 * 6. The device specific code for each receiver message (generated via associated TargetDevice)
	 * 7. A ROM based device independent control block for each receiving message
	 * 8. A ROM based device independent control block for each sending message
	 * 9. A ROM based array of references to each receiving message cb (array of handles)
	 * 10. A ROM based array of references to each sending message cb (array of handles)
	 */	
	private void generateCOM() {
		
		// Create set of all TargetDevice instances that are used by receiving messages
		Collection<TargetDevice> allDevices = new HashSet<TargetDevice>();
		
		for (TargetReceivingMessage next : targetModel.getTargetReceivingMessages()) {
			
			TargetDevice device = next.getDevice();
			
			if ( device != null ) {
				allDevices.add(device);				// if device already in set then not added again!			
			}			
		}
				
		//////////////////////////////////////////////////////////////////////////////////////
		// 0.Generate variable values to represent COM configuration and message count details

		int receiverCount = targetModel.getTargetReceivingMessages().size();
		int senderCount = targetModel.getTargetSendingMessages().size();
		
		writeln(comment("COM Configuration."));
		
		// unat com_COMErrorHook_callable = <flag>;						/* Stored in RAM */
		writeln(UNAT+" "+COM_HOOK_CALLABLE_NAME+" = "+toFlag(targetModel.isCOMErrorHook())+";");
		
		//const unat com_call_StartCOMExtension = <flag>;				/* Stored in ROM; instantiated by configuration tool */
		writeln(CONST+" "+UNAT+" "+COM_CALL_EXTENSION_NAME+" = "+toFlag(targetModel.isCOMStartCOMExtension())+";");
		
		//const COMApplicationModeType com_mode_count = <modeCount>;	/* Stored in ROM; count of COM startup modes, instantiated by configuration tool */
		if ( targetModel.isCOMExtendedStatus() ) {
			writeln(CONST+" "+COM_APP_MODE_H+" "+COM_MODES_NUM+" = "+targetModel.getComAppModes().size()+"U;");
		}
		writeNL();
		
		// Count of receiving messages			
		writeln(comment("Variable value to represent the number of receiving messages."));
			
		// const uint16 com_num_rcv_msgs = <count>U;	
		writeln(CONST+" "+UINT16+" "+COM_RECEIVE_NUM_NAME+" = "+receiverCount+"U;");
		
		writeNL();
		
		// Need to generate COM_RES_OS variable
		TargetResource resCOMHook = targetModel.getResCOMResource();
		
		if ( resCOMHook != null ) {
			writeln(CONST+" "+RES_H+" "+TargetCpu.COM_HOOK_RES_NAME+"="+getTargetElementArrayReference(resCOMHook)+";");
			writeNL();
		}
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 1. Generate array of COM devices that provide start/stop functions
		 
		writeln(comment("ROM based array of COM devices that require starting/stopping within the COM layer."));
		// const struct com_device_initcb com_init_devices[] = { .. };
		
		int startedDeviceCount = 0;
		
		write(CONST_STRUCT+" "+COM_DEVICE_INIT_CB_TYPE+" "+COM_DEVICE_INIT_CB_NAME+"[] = {");
		
		incTabs();
		
		for ( TargetDevice device : allDevices ) {
			
			assert device.getDriver() instanceof TargetCOMDriver;	// driver of COM device should always be a COM type driver
			
			TargetCOMDriver comDriver = (TargetCOMDriver)device.getDriver();
			
			if ( comDriver.providesStartStopFunctions() ) {
				// this device has a driver that provides start/stop function. So generate CB for the device.
				
				if ( startedDeviceCount > 0 ) {
					append(",");
				}

				writeNL();
				
				writeln("{");
				incTabs();
				
				//struct com_device_initcb {
				//	DeviceId device;
				//	StatusType (*start_device)(DeviceId);										/* Startup the device */
				//	StatusType (*stop_device)(DeviceId);										/* Stop the device */
				//};				
				
				// (const struct os_devicecb *)&os_device_<index>
				write("("+CONST_STRUCT+" "+DEVICE_CB_TYPE+" *)&"+DEVICE_CB_NAME+"_"+device.getControlBlockIndex()+",");
				append(verboseComment("DeviceId device;"));
				writeNL();
				
				write(comDriver.getStartFnName()+",");
				append(verboseComment("StatusType (*start_device)(DeviceId);"));
				writeNL();
				
				write(comDriver.getStopFnName());
				append(verboseComment("StatusType (*stop_device)(DeviceId);"));
				writeNL();
				
				decTabs();
				write("}");
				startedDeviceCount++;		
			}
		}
		
		if ( startedDeviceCount > 0 ) {
			writeNL();
		}
		
		decTabs();
		append("};");
		writeNL();
		
		// extern const uint16 com_num_init_devices;	/* Stored in ROM; number of COM devices that require starting/stopping within the COM layer */
		writeln(CONST+" "+UINT16+" "+COM_NUM_INIT_DEVICES+" = "+startedDeviceCount+";");	
		writeNL();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. Generate the last message pointer values for handle range checking, only in extended COM status
		if ( targetModel.isCOMExtendedStatus() ) {
				
			// const com_messagecb com_last_send_msg = &com_send_msgs[<count>-1];
			write(CONST+" "+COM_MESSAGE_H+" "+COM_MESSAGE_LAST_SEND_NAME+" = ");
			if ( senderCount > 0 ) {
				append("&"+platformInfo.getSendingMessageCBName()+"["+(senderCount-1)+"];");
			}
			else {
				append("0;");
			}
			append(verboseComment("Addr of the last message in the sending message control block"));
			writeNL();
			
			// const com_messagecb com_last_rcv_msg = &com_rcv_msgs[<count>-1];
			write(CONST+" "+COM_MESSAGE_H+" "+COM_MESSAGE_LAST_RECEIVE_NAME+" = ");
			if ( receiverCount > 0 ) {
				append("&"+platformInfo.getReceivingMessageCBName()+"["+(receiverCount-1)+"];");
			}
			else {
				append("0;");
			}
			append(verboseComment("Addr of the last message in the receiving message control block"));
			writeNLs(2);
		}
		
		///////////////////////////////////////////////////////////////////////
		// 3. Generate RAM based notification Flag variables, inc. single scratch flag

		if (receiverCount > 0) {		
			writeln(comment("Declare the the flag variables, set by receiving message notifications"));
			
			
			// Check if a scratch flag is required
			boolean scratchRequired = false;
			
			for (TargetReceivingMessage next : targetModel.getTargetReceivingMessages()) {
				
				if ( next.setsFlag() == false || (next.lowSetsFlag() == false && next.isStreamMessage()) ) {
					// Found a message that does not set a flag, or a stream message that does not set a low threshold flag.
					// So need scratch flag.
					scratchRequired = true;
				}
			}	
			
			if ( scratchRequired ) {
				// static FlagValue com_scratch_flag_<messageIndex>;
				write(STATIC+" "+COM_FLAG_TYPE+" "+COM_SCRATCH_FLAG_NAME+";");
				append(verboseComment("Scratch flag, shared by receiving messages that don't set a flag"));
				writeNL();
			}
			
			// create a flag variable for each (uniquely named) flag that is set
			for (Integer nextFlagIndex : targetModel.getFlagNameMap().values()) {				
				// static FlagValue com_flag_<nextFlagIndex>;
				writeln(COM_FLAG_TYPE+" "+COM_FLAG_NAME+"_"+nextFlagIndex+";");
			}
			writeNLs(2);
		}
			
		
		//////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. Generate ROM based non-zero initial values defined for non-queued; non-zero length receiving message		
			
		Collection<TargetReceivingMessage> initialisedMessages = targetModel.getTargetInitialisedReceivingMessages();
		
		if (initialisedMessages.size() > 0) {
			
			writeln(comment("Declare the ROM based initial values defined for non-queued; non-zero receiving messages"));
			
			for (TargetReceivingMessage next : initialisedMessages) {
					
				// static const <dataTypeName> com_init_value_<messageIndex> = <initValue>U;
				write(STATIC+" "+CONST+" "+next.getDataTypeName()+" "+COM_INIT_VALUE_NAME+"_"+next.getControlBlockIndex()+" = "+next.getInitialValue()+"U;");				
				append(verboseComment("Initial value for message "+next.getName()));
				writeNL();
			}
			writeNL();
		}			
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5. Generate the ROM based notification processing data for notifications within each receiving message
		if (receiverCount > 0) {
			
			writeln(comment("Define the ROM based notification processing data for each receiving message"));
			
			for (TargetReceivingMessage next : targetModel.getTargetReceivingMessages()) {
				
				writeln(getCOMNotificationFunctionData(next.getNotificationAction()));
				
				// get low threshold notification actions as well (used by stream messages)
				writeln(getCOMNotificationFunctionData(next.getLowNotificationAction()));
			}
			writeNL();
		}		
		
		////////////////////////////////////////////////////////////////////////////////
		// 6. Generate device specific code for each receiver message device (generated via associated TargetDevice)
		if (allDevices.size() > 0) {
			
			writeln(comment("Define the device specific data for each receiving message device"));
			writeNL();
			
			// iterate over all COM devices used by the receiver messages
			for (TargetDevice device : allDevices) {
				
				// Ask associated device driver to do the generation work
				device.getDriver().genCInitCode(device);
			}
			writeNL();			
		}
		
		
		///////////////////////////////////////////////////////////////////////
		// 7. Generate a ROM based device independent control block for each receiving message		
	
		if (receiverCount > 0) {
			
			writeln(comment("Define the ROM based (driver independent) part of the receiving messages control blocks."));

			// static const struct com_receiver_handlecb com_rcv_msgs_data[] = { ... }		/* Stored in ROM; receiving message control blocks */
			writeln(STATIC+" "+CONST_STRUCT+" "+COM_RECEIVER_HANDLE_CB_TYPE+" "+platformInfo.getReceivingMessageCBName()+"_data[] = {");
			incTabs();
						
			initComma(receiverCount);
			
			for (TargetReceivingMessage next : targetModel.getTargetReceivingMessages()) {
				
				//struct com_receiver_handlecb {
				//	const struct com_receivercb *drv_receiver;			/* Device-specific receiver message; this is cast to appropriate concrete type by driver functions */
				//	const struct com_drivercb *driver;					/* Driver to handle the receiver message */
				//};				
							
				write("{");						
				append(comment("Receiving Message control block for "+next.getName()));
				writeNL();
				
				incTabs();		
				
				// struct com_receivercb *drv_receiver;				
				write("("+CONST_STRUCT+" "+COM_RECEIVER_CB_TYPE+" *)&"+COM_RECEIVE_DRV_CB_NAME+"_"+next.getControlBlockIndex()+",");
				append(verboseComment("const struct com_receivercb *drv_receiver; Ptr to the driver specific receiver cb"));
				writeNL();
				
				// struct com_drivercb *driver;
				TargetDevice device = next.getDevice();
				
				if ( device != null ) {
					write("&"+device.getDriver().getControlBlockName()+"_");
					
					if ( next.isQueuedMessage() ) {
						append(COM_QUEUED_DRV_NAME);
					
					} else if ( next.isUnqueuedMessage()) {
						append(COM_UNQUEUED_DRV_NAME);
					}
					else if ( next.isZeroLengthMessage() ) {
						append(COM_ZERO_LENGTH_DRV_NAME);
					}
					else if ( next.isStreamMessage() ) {
						append(COM_STREAM_DRV_NAME);
					}					
					append("_"+device.getDriver().getControlBlockIndex()+";");
				}
				else {
					// no device, so just write 0
					write("0;");
				}
				append(verboseComment("const struct com_drivercb *driver; Ptr to the driver cb"));
				writeNL();
				
				decTabs();			
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			writeNL();
		}
		
		
		///////////////////////////////////////////////////////////////////////
		// 8. Generate a ROM based device independent control block for each sending message

		if (senderCount > 0) {
			
			writeln(comment("Define the ROM based part of the sending messages control blocks."));
			
			// static const struct com_sender_handlecb com_send_msgs_data[] = { {},... };
			writeln(STATIC+" "+CONST_STRUCT+" "+COM_SENDER_HANDLE_CB_TYPE+" "+platformInfo.getSendingMessageCBName()+"_data[] = {");
			incTabs();
						
			initComma(senderCount);
			
			for (TargetSendingMessage next : targetModel.getTargetSendingMessages()) {
				
				//struct com_sender_handlecb {
				//	enum com_mtype message_type;					/* type of the sending message */
				//	uint16 num_receivers;								/* Number of receivers in the block (> 1 means fanout, 1 means 1:1) */	
				//	com_receiverh *receivers;						/* Block of receiver messages (local or hardware/remote) $Req: artf1239 $ */
				//};
				
				writeln("{");
				
				incTabs();				
							
				writeln(comment("Sending Message control block for "+next.getName()));
										
				/////////////////////////////////
				// enum com_mtype message_type;	

				// COM_QUEUED_OR_UNQUEUED, COM_ZERO_LENGTH, COM_STREAM			
				if ( next.isZeroLengthMessage() ) {
					write(COM_ZERO_LENGTH_TYPE);
				}
				else if ( next.isStreamMessage() ) {
					write(COM_STREAM_TYPE);
				}
				else {
					// must be queued/unqueued message
					write(COM_QUEUED_OR_UNQUEUED_TYPE);
				}
				append(", "+verboseComment("enum com_mtype message_type;"));
				writeNL();
				
				/////////////////////////
				// uint16 num_receivers;
				write(next.getTargetReceivers().size()+"U,");
				append(verboseComment("uint16 num_receivers; number of receivers for this sender"));
				writeNL();				
				
				///////////////////////////
				// com_receiverh receivers; Pointer to first receiver attached to this sender
				TargetReceivingMessage receiver = next.getFirstTargetReceiver();
				
				if ( receiver != null ) {		
					write("("+CONST_STRUCT+" "+COM_RECEIVER_HANDLE_CB_TYPE+" *)&"+receiver.getControlBlockName()+"_data["+receiver.getControlBlockIndex()+"],");				
					append(verboseComment("com_receiverh receivers; ptr to (first) receiver for this sender"));
				}
				else {
					write("0");
					append(verboseComment("com_receiverh receivers; no receivers for this sender"));
				}
				writeNL();

				decTabs();			
				write("}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
			writeNL();
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		// 9. Generate a ROM based array of references to each receiving message cb (array of handles)
		
		if (receiverCount > 0) {
			
			writeln(comment("Define the ROM based array of handles for each receiving message."));
			
			// const struct com_messagecb com_rcv_msgs[] = { };
			writeln(CONST_STRUCT+" "+COM_MESSAGE_CB_TYPE+" "+platformInfo.getReceivingMessageCBName()+"[] = {");
			incTabs();
						
			initComma(receiverCount);
			
			for (TargetReceivingMessage next : targetModel.getTargetReceivingMessages()) {
				
				writeln("{{");	// need double brace, since initialising contents of a union, i.e. one level down from normal
				
				incTabs();				
				
				// Always cast to type of first member of union, since C can only initialise unions to the type of the first member. Both pointers so not problems at run-time
				write("("+CONST_STRUCT+" "+COM_RECEIVER_HANDLE_CB_TYPE+" *)&"+next.getControlBlockName()+"_data["+next.getControlBlockIndex()+"]");
				
				append(comment("Reference receiving message "+next.getName()));
				writeNL();

				decTabs();			
				write("}}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
		}
		else {
			writeln(comment("Define an empty ROM based array for receiving message control blocks, since no receiving messages defined."));
			writeln(CONST_STRUCT+" "+COM_MESSAGE_CB_TYPE+" "+platformInfo.getReceivingMessageCBName()+"[] = {};");
		}		

		////////////////////////////////////////////////////////////////////////////////////////////
		// 10. Generate a ROM based array of references to each sending message cb (array of handles)
		
		if (senderCount > 0) {
			
			writeln(comment("Define the ROM based array of handles for each sending message."));
			
			// const struct com_messagecb com_send_msgs[] = { };
			writeln(CONST_STRUCT+" "+COM_MESSAGE_CB_TYPE+" "+platformInfo.getSendingMessageCBName()+"[] = {");
			incTabs();
						
			initComma(senderCount);
			
			for (TargetSendingMessage next : targetModel.getTargetSendingMessages()) {
				
				writeln("{{");	// need double brace, since initialising contents of a union, i.e. one level down from normal
				
				incTabs();				
				
				// Always cast to type of first member of union, since C can only initialise unions to the type of the first member. Both pointers so not problems at run-time
				write("("+CONST_STRUCT+" "+COM_RECEIVER_HANDLE_CB_TYPE+" *)&"+next.getControlBlockName()+"_data["+next.getControlBlockIndex()+"]");
				
				append(comment("Reference sending message "+next.getName()));
				writeNL();

				decTabs();			
				write("}}");
				doComma();
				writeNL();
			}
			decTabs();
			writeln("};");
		}
		else {
			writeln(comment("Define an empty ROM based array for sending message control blocks, since no sending messages defined."));
			writeln(CONST_STRUCT+" "+COM_MESSAGE_CB_TYPE+" "+platformInfo.getSendingMessageCBName()+"[] = {};");
		}
		
		writeNL();
	}	
	
	
	/**
	 * Converts a boolean to a "1U" (true) or "0" string version.
	 * @param flag
	 * @return string version
	 */
	private String toFlag(boolean flag) {
		
		return (flag) ? "1U" : "0";
	}
	
	/**
	 * Helper function that generates all the code required for configurable options defined within the 
	 * current OS model.
	 * 
	 * Generates:
	 *  1. A DECLAREFLAGS() instruction for the OS flags.
	 *  2. A reinit function pointer.
	 *  3. A pretask hook stack offset value (if required).
	 *  4. A posttask hook stack offset value (if required).
	 *	5. Generate queuetask function pointer
	 *	6. Generate dequeuetask function pointer
	 *	7. Generate dispatch function pointer
	 *	8. Generate terminate function pointer  
	 *  9. Generate link check variable instances
	 */		
	private void generateConfigDetails() {
		
		/////////////////////////////////////////////////////////////////
		// 1. Generate flag declarations
		writeln(comment("OS configuration flags"));
		writeln(verboseComment("errorhook, pretaskhook, posttaskhook, startuphook, shutdownhook, getserviceid, parameteraccess"));
		writeNL();
			
		if (targetModel.isRestartable()) {
			writeln(verboseComment("OS configured as restartable, so instantiate the flag reinit variable"));
			writeln(CONST_STRUCT+" "+FLAGS_TYPE+" "+INIT_FLAGS_NAME+" = {");
			incTabs();		
			writeln(toFlag(targetModel.isErrorHook())+", ");
			writeln(toFlag(targetModel.isPreTaskHook())+", ");
			writeln(toFlag(targetModel.isPostTaskHook())+", ");
			writeln(toFlag(targetModel.isStartupHook())+", ");
			writeln(toFlag(targetModel.isShutdownHook())+", ");
			writeln(toFlag(targetModel.isUseGetServiceId())+", ");
			writeln(toFlag(targetModel.isUseParameterAccess()));	
			decTabs();
			writeln("};");
			writeNL();
		}
		else {
			writeln(verboseComment("OS configured as NOT restartable, so no flag reinit variable"));
			writeNL();
		}
		
		writeln(STRUCT+" "+FLAGS_TYPE+" "+NEAR+"("+FLAGS_NAME+") = {");
		incTabs();		
		writeln(toFlag(targetModel.isErrorHook())+", ");
		writeln(toFlag(targetModel.isPreTaskHook())+", ");
		writeln(toFlag(targetModel.isPostTaskHook())+", ");
		writeln(toFlag(targetModel.isStartupHook())+", ");
		writeln(toFlag(targetModel.isShutdownHook())+", ");
		writeln(toFlag(targetModel.isUseGetServiceId())+", ");
		writeln(toFlag(targetModel.isUseParameterAccess()));	
		decTabs();
		writeln("};");
		writeNL();
		
		/////////////////////////////////////////////////////////////////
		// 2. Generate reinit function pointer (if required)
		
		writeln(comment("Constant pointer to reinitialise function (called during Shutdown())"));
		write("void (* "+CONST+" "+REINIT_F_NAME+")(void)");
		
		if (targetModel.isRestartable()) {
			 append(" = "+REINIT_NAME+";");
			 append(comment(" OS is restartable."));
		}
		else {
			append(" = 0;");
			append(comment(" OS is not restartable."));
		}
		writeNL();
		
		/////////////////////////////////////////////////////////////////
		// 3. Generate pretask hook stack offset value (if required)
		
		if (targetModel.isStackCheckingEnabled()) {
			
			if ( targetModel.isPreTaskHook() ) {
				write(NAT+" "+FASTROM+"("+PREHOOK_STACK_NAME+") = "+targetModel.getPreTaskHookStackOffset()+";");
				append(verboseComment(" Amount of stack space requested was "+targetModel.getModelPreTaskHookStackSize()+" bytes."));
			}
			else {
				write(NAT+" "+FASTROM+"("+PREHOOK_STACK_NAME+") = 0;");
				append(verboseComment(" Dummy pre-task hook stack space value to ensure link"));
			}
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////
		// 4. Generate posttask hook stack offset value (if required)
		
		if (targetModel.isStackCheckingEnabled()) {
			
			if ( targetModel.isPostTaskHook() ) {
				write(NAT+" "+FASTROM+"("+POSTHOOK_STACK_NAME+") = "+targetModel.getPostTaskHookStackOffset()+";");
				append(verboseComment(" Amount of stack space requested was "+targetModel.getModelPostTaskHookStackSize()+" bytes."));
			}
			else {
				write(NAT+" "+FASTROM+"("+POSTHOOK_STACK_NAME+") = 0;");
				append(verboseComment(" Dummy post-task hook stack space value to ensure link"));
			}
			writeNL();
		}
		writeNL();
		 
		/////////////////////////////////////////////////////////////////
		// 5. Generate queuetask function pointer
		
		writeln(comment("Constant pointer to queuetask function"));
		write("void (* "+CONST+" "+QUEUETASK_F_NAME+")("+TASK_H+") = ");
		
		if (targetModel.getTargetStandardQueues().size() > 0) {
			// need standard queuing mechanisms
			 append(QUEUETASK_STD_NAME+";");
			 append(comment(" standard task queueing"));
		}
		else {
			append(QUEUETASK_OPT_NAME+";");
			append(comment(" optimized task queueing"));
		}
		writeNLs(2);
		
		/////////////////////////////////////////////////////////////////
		// 6. Generate dequeuetask function pointer
		
		writeln(comment("Constant pointer to dequeuetask function"));
		write("void (* "+CONST+" "+DEQUEUETASK_F_NAME+")(void) = ");
		
		if (targetModel.getTargetStandardQueues().size() > 0) {
			// need standard dequeuing mechanisms
			 append(DEQUEUETASK_STD_NAME+";");
			 append(comment(" standard task dequeueing"));
		}
		else {
			append(DEQUEUETASK_OPT_NAME+";");
			append(comment(" optimized task dequeueing"));
		}

		writeNL();
				
		/////////////////////////////////////////////////////////////////
		// 7. Generate dispatch function pointers
		writeln(comment("Constant pointer to kernel stack dispatch function"));
		write("void (* "+CONST+" "+KS_DISPATCH_F_NAME+")(void) = ");
		
		switch (targetModel.getTasksetType()) {		
		case MIXED_TASKS :
			// both extended tasks and basic tasks exist (in addition to the idle task)
			append(KS_DISPATCH_MIX_NAME+";");
			append(comment(" kernel stack dispatch handles both basic and extended tasks"));				
			break;
		case EXTENDED_ONLY :
			// only extended tasks exist, no basic tasks exist (except the idle task)
			append(KS_DISPATCH_ET_NAME+";");
			append(comment(" kernel stack dispatch handles extended tasks only"));
			break;
		case BASIC_ONLY : 
			// at least one basic task exists (not including the idle task) and no extended tasks exist
			append(KS_DISPATCH_BT_NAME+";");
			append(comment(" kernel stack dispatch handles basic tasks only"));
			break;
		default :
			append("0;");
			append(comment(" kernel stack dispatch never used since no user-provided tasks"));
			break;
		};		
				
		writeNLs(2);

		writeln(comment("Constant pointer to dispatch function, which is called if stack switch may be needed"));
		write("void (* "+CONST+" "+SWST_DISPATCH_F_NAME+")(void) = ");
	
		switch (targetModel.getTasksetType()) {		
		case MIXED_TASKS :
			// both extended tasks and basic tasks exist (in addition to the idle task)
			append(SWST_DISPATCH_MIX_NAME+";");
			append(comment(" ks_dispatch handles both basic and extended tasks"));				
			break;
		case EXTENDED_ONLY :
			// only extended tasks exist, no basic tasks exist (except the idle task)
			append(SWST_DISPATCH_ET_NAME+";");
			append(comment(" ks_dispatch handles extended tasks only"));
			break;
		case BASIC_ONLY : 
			// at least one basic task exists (not including the idle task) and no extended tasks exist
			// so no stack switching ever required therefore can use the kernel stack dispatch for basic tasks.
			append(KS_DISPATCH_BT_NAME+";");
			append(comment(" ks_dispatch handles basic tasks only (no stack switching ever required)"));
			break;
		default :
			append("0;");
			append(comment(" ks_dispatch never used since no user-provided tasks"));
			break;
		};			
		
		writeNLs(2);
		
		/////////////////////////////////////////////////////////////////
		// 8. Generate terminate function pointer
		writeln(comment("Constant pointer to terminate function"));
		write("void (* "+CONST+" "+TERMINATE_F_NAME+")(void) = ");		
		
		switch (targetModel.getTasksetType()) {		
			case MIXED_TASKS :
				// both extended tasks and basic tasks exist (in addition to the idle task)
				append(TERMINATE_MIX_NAME+";");
				append(comment(" terminate handles both basic and extended tasks"));				
				break;
			case EXTENDED_ONLY :
				// only extended tasks exist, no basic tasks exist (except the idle task)
				append(TERMINATE_ET_NAME+";");
				append(comment(" terminate handles extended tasks only"));
				break;
			case BASIC_ONLY : 
				// at least one basic task exists (not including the idle task) and no extended tasks exist
				append(TERMINATE_BT_NAME+";");
				append(comment(" terminate handles basic tasks only"));
				break;
			default :
				append("0;");
				append(comment(" terminate never used since no user-provided tasks"));
				break;
		};
		
		writeNL();
		
		/////////////////////////////////////////////////////////////////
		// 9. Generate link check variable instances
		
		writeln(comment("Create link check variables, ensuring link to correct OS library"));
		
		if ( targetModel.isExtendedStatus() ) {
			write(STATIC+" "+CONST_UNAT+" * "+CONST+" "+LINK_EXTENDED_NAME+"lc = &"+LINK_EXTENDED_NAME+";");
			append(comment(" force link to extended status OS lib"));
			writeNL();
		}
		else {
			write(STATIC+" "+CONST_UNAT+" * "+CONST+" "+LINK_STANDARD_NAME+"lc = &"+LINK_STANDARD_NAME+";");
			append(comment(" force link to standard status OS lib"));
			writeNL();
		}
		
		if ( targetModel.isStackCheckingEnabled() ) {
			write(STATIC+" "+CONST_UNAT+" * "+CONST+" "+LINK_STACK_MONITORING_NAME+"lc = &"+LINK_STACK_MONITORING_NAME+";");
			append(comment(" force link to stack monitoring OS lib"));
			writeNL();
			
		}
		else {
			write(STATIC+" "+CONST_UNAT+" * "+CONST+" "+LINK_NO_STACK_MONITORING_NAME+"lc = &"+LINK_NO_STACK_MONITORING_NAME+";");
			append(comment(" force link to non-stack monitoring OS lib"));
			writeNL();			
		}
		writeNL();
		
	}
	
	/**
	 * Helper function that generates 
	 * 
	 * 1. A Priority-to-IPL lookup array (if required for the target).
	 * 2. A variable value to represent the highest task priority.
	 * 3. A variable value to represent the IPL of the kernel
	 * 4. A variable value to represent the PRI of the kernel (if required)
	 * 
	 * Generates:
	 *  1. An array that maps from priority index values to the IPL value for the target
	 */		
	private void generatePri2IPLDetails() {
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1.Generate Priority-to-IPL lookup array (if required for the target).
		
		if (platformInfo.isPri2IPLLookupRequired()) {
			
			writeln(comment("Priority index to IPL Lookup table"));	
		
			// iterate over each priority and generate mapping for the target
			write(CONST+" "+IPL_TYPE+" "+PRI_2_IPL_NAME+"[] = {");
			
			int topTargetPriority = targetModel.getTargetPriorities().getHighestTargetPriority();
			
			for ( int i = 0; i <= topTargetPriority ; i++) {
				
				if (i > 0) {
					append(", ");
				}
				
				// ask target to map from a target priority to an IPL string
				append(getTargetIPLString(i));
			}
			writeln("};");
			
			writeNL();
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2.Generate variable value to represent the highest task priority.

		writeln(comment("Variable value to represent the highest task priority."));
		
		if (targetModel.isRestartable()) {
			writeln(CONST_UNAT+" "+TASK_HIGHEST_PRI_NAME+" = "+targetModel.getTargetPriorities().getHighestTaskTargetPriority()+"U;");
		}
		else {
			writeln(comment(TASK_HIGHEST_PRI_NAME+" not needed since reinit not used."));
		}
		writeNL();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3.Generate variable value to represent the IPL of the kernel
		
		writeln(comment("Variable value to represent the IPL of the kernel."));
		writeln(IPL+" "+FASTROM+"("+KERNEL_IPL_NAME+") = "+getTargetIPLString(targetModel.getTargetPriorities().getHighestTargetPriority())+";");
		
		writeNL();
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4.Generate variable value to represent the PRI of the kernel
			
		writeln(comment("Variable value to represent the priority of the kernel."));
		writeln(PRI+" "+FASTROM+"("+KERNEL_PRI_NAME+") = "+mapToEmbeddedPriority(targetModel.getTargetPriorities().getHighestTargetPriority())+"U;");
		writeNL();
	}
	
	/**
	 * Helper that generates handle access macros for the target elements within the given collection.
	 * The output form is -
	 * 	#define name (&cb_name[<index>])
	 * 
	 *@param elements the collection of TargetElement instances for which the handles are to be generated.
	 */
	private void generateTargetHandles(Collection<? extends TargetElement> elements) {
		
		/**
		 * Generate all handles for the given collection
		 */
		for (TargetElement next : elements) {
			
			if ( next.getHasHandle() ) {
				writeln(DEFINE+" "+genCName(next)+" ("+getTargetElementArrayReference(next)+")");
			}
		}
	}	
	
	/**
	 * Generates two parallel arrays containing the handle ptrs and object names respectively. Used dynamically
	 * at run-time to translate between handles and meaningful strings (object names).
	 */
	private void generateHandleMap() {	
		
		Collection<? extends TargetElement> elements = targetModel.getAllTargetCpuElements();
		
		writeln("#ifdef INCLUDE_HANDLE_NAME_MAP");
		
		write("void *"+ALL_HANDLES_ARRAY_NAME+"[] = {");
		
		for (TargetElement next : elements) {
			write("(void *)("+getTargetElementArrayReference(next)+"),");
		}		

		write("(0)");
		writeln("};");
		writeNL();
		
		//////////////////////////////////////////////////////////////
		
		write("char *"+ALL_ALL_NAMES_ARRAY_NAME+"[] = {");
		
		for (TargetElement next : elements) {
			write("\""+genCName(next)+"\", ");
		}
		write("0");
		writeln("};");
		
		writeln("#endif /* INCLUDE_HANDLE_NAME_MAP */");
		
		writeNL();
	}
	
	
	/**
	 * Outputs a specified section name bounded by separator comment lines.
	 * 
	 * @param sectionName
	 */
	void section(String sectionName) {
		writeln(separatorComment());
		writeln(comment(sectionName));
		writeNL();
	}
	
	/**
	 * Generates an Ansi-C based system based on the osModel associated with the generator.
	 * 
	 * Generates:
	 *  1. A generated C source code file.
	 *  2. A generated C header file.
	 * 
	 * @param rootPath the path name of where the files are to be located (if null uses current working directory)
	 * @throws IOException
	 */
	@Override
	public void generate(String rootPath) throws IOException {
		
		super.generate(rootPath);	// call super class version to ensure ready for generation.
				
		//=================================================================================
		// 1. Generate the C source code file
		
		
		setupWriter(rootPath,GEN_C_FILE);	// create and open the C file

		log("Generating '"+writerPathName+"'");
		
		resetTabs();
		
		writeln(comment(AUTOGEN_COMMENT));	// output the standard header
		
		writeNLs(2);	// write 2 new line chars
		
		// include the required headers
		include(OS_INCLUDE_FILE);
			
		writeNLs(2);	// write 2 new line chars
		
		// Generate #include of all Device related instances
		writeln(comment("include the device driver specific headers."));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Generate the #include statements for each device driver required
		for (String next : targetModel.getDriverManager().getDriverIncludeNames() ) {

			include("drivers/"+next);
		}
	
		writeNL();		
		
		///////////////////////////////////////////////////////////////////////////////
		// Generate entry prototypes for tasks and ISRs
		
		section("ENTRY FUNCTION PROTOTYPES");
		
		writeln(comment("Prototype for each task entry function."));
		for (TargetTask next : targetModel.getNonIdleTargetTasks()) {
			// TASK(name);
			writeln(TASK_PROTO+"("+genCName(next)+");");
		}
		
		writeNL();
		
		if (targetModel.getTargetISRs().size() > 0) {
			writeln(comment("Prototype for each ISR handler function."));
			for (TargetISR next : targetModel.getTargetISRs()) {
				// ISR(name);
				writeln(ISR_PROTO+"("+genCName(next)+");");
			}
		}		
		
		writeNL();
		
		
		///////////////////////////////////////////////////////////////////////////////
		// Generate entry prototypes for alarm callback functions
		
		
		// Use set to ensure output prototypes name are unique, could actually redefine same name but otuput is tidy this way.
		Set<String> callbackNames = new HashSet<String>();
		
		for (TargetAlarm next : targetModel.getTargetAlarms()) {

			TargetExpiry action = next.getExpiryAction();

			if ( action.callsHandler() ) {
				callbackNames.add(action.getCallbackName());
				
			}
		}	
		
		if ( callbackNames.size() > 0 ) {
			writeln(comment("Prototype for each alarm callback function."));
		
			for (String nextName : callbackNames) {
				writeln(ALARMCALLBACK_PROTO+"("+nextName+");");
			}
			
			writeNL();
		}
		
		
		/////////////////////////////////////////////////////
		// Generate the Priority-to-IPL related info
		section("PRIORITY TO IPLS");
		generatePri2IPLDetails();		
		
		// Generate definitions of all queue related instances
		section("TASK QUEUES");
		generateQueues();		
		
		// Generate definitions of all Task related instances
		section("TASKS");
		generateTasks();
		
		// Generate definitions of all ISR related instances
		section("ISRs");
		generateISRs();

		// Generate definitions of all Resource related instances
		section("RESOURCES");
		generateResources();
	
		// Generate definitions of all Device Driver related instances
		section("DEVICE DRIVERS");
		generateDeviceDrivers();
		
		// Generate definitions of all Counter related instances
		section("COUNTERS");
		generateCounters();		
		
		// Generate associations between devices and drivers
		
		
		/*
		 * 
		 * Code below not currently used. Generates an instance control block for each device, allowing device_ctl
		 * TODO: check whether to remove this completely.
		 * 
		section("DEVICE DRIVER ASSOCIATIONS");
		generateDeviceDriverAssociations();
		*/

		// Generate definitions of all Alarm related instances
		section("ALARMS");
		generateAlarms();		
		
		// Generate definitions of all ScheduleTable related instances
		section("SCHEDULE TABLES");
		generateScheduleTables();		
		
		// Generate definitions of all Application Mode related instances
		section("APPLICATION MODES");
		generateAppModes();
		
		// Generate COM sub-system related instances, but only if a COM Object defined.
		if ( targetModel.isCOMPresent() ) {
			section("COM");
			generateCOM();
		}
		
		// Generate the configuration dependent details (e.g. flags)
		section("CONFIGURATION");
		generateConfigDetails();

		// Call the epilogue method allowing target specific output to the source file.
		SourceCFileEpilogue();
		
		writeNL();
		
		////////////////////////////////////////////////////////////////////////
		// Generate Map from Handle ptr values to textual object names
		
		generateHandleMap();		
		
		
		section("END OF AUTO-GENERATED SOURCE FILE");
		
		// Close the write for the file
		closeWriter();
		
		log("Completed '"+GEN_C_FILE+"'");
		
		//=================================================================================
		// 2. Generate the C handles Header file
			
		setupWriter(rootPath,GEN_HANDLES_FILE);
		
		log("Generating '"+writerPathName+"'");
		
		resetTabs();

		writeln(comment(AUTOGEN_COMMENT));	// output the standard header
		
		writeNL();

		// Define constants that are dependent on system generation options 
		if ( targetModel.isExtendedStatus() ) {
			writeln(DEFINE+" "+EXTENDED_STATUS_NAME);
		}
		else {
			writeln(DEFINE+" "+STANDARD_STATUS_NAME);
		}
		
		if ( targetModel.isCOMPresent() ) {
			if ( targetModel.isCOMExtendedStatus() ) {
				writeln(DEFINE+" "+COM_EXTENDED_STATUS_NAME);
			}
			else {
				writeln(DEFINE+" "+COM_STANDARD_STATUS_NAME);
			}
		}
		
		
		// include the required headers
		// none at the moment
		
		writeNLs(2);
		
		///////////////////////////////////////////////////////////////////////////////
		// Generate handles for each task  (except the idle task), resource and appmode
		
		section("HANDLES");
		
		writeln(comment("Handle for each task."));
		generateTargetHandles(targetModel.getNonIdleTargetTasks());
		writeNL();
		
		// For OSEK don't need handle for each ISR since no OSEK API call takes an ISR handle, however
		// AUTOSAR does require them, so output the handles for the ISRs.
		writeln(comment("Handle for each ISR."));
		generateTargetHandles(targetModel.getTargetISRs());
		writeNL();
		
		
		// Handle for each accessed non-internal resource
		writeln(comment("Handle for each accessed non-internal resource."));
		generateTargetHandles(targetModel.getTargetAccessedNonInternalResources());
		writeNL();
		
		// Handle for each app mode
		writeln(comment("Handle for each application mode."));
		generateTargetHandles(targetModel.getTargetAppModes());
		writeNL();
		
		// Handle for each alarm
		writeln(comment("Handle for each alarm."));
		generateTargetHandles(targetModel.getTargetAlarms());
		writeNL();		
		
		// Handle for each schedule table
		writeln(comment("Handle for each schedule table."));
		generateTargetHandles(targetModel.getTargetScheduleTables());
		writeNL();
		
		// Handle for each counter
		writeln(comment("Handle for each counter."));
		generateTargetHandles(targetModel.getTargetCounters());
		writeNL();		
		
		writeln(comment("Macros to allow access to constants of each counter."));
		// Macro for each counter (see section 13.6.4 Constants of OSEK OS spec.)
		for(TargetCounter next : targetModel.getTargetCounters()) {
			writeln(DEFINE+" "+OSMAXALLOWEDVALUE+"_"+genCName(next)+" ("+next.getMaxAllowedValue()+"U)");
			writeln(DEFINE+" "+OSTICKSPERBASE+"_"+genCName(next)+" ("+next.getTicksPerBase()+"U)");
			writeln(DEFINE+" "+OSMINCYCLE+"_"+genCName(next)+" ("+next.getMinCycle()+"U)");
		}
		writeNL();
		
		// Handle for each sending Message
		writeln(comment("Handle for each sending message."));
		generateTargetHandles(targetModel.getTargetSendingMessages());
		writeNL();	
		
		// Handle for each receiving Message
		writeln(comment("Handle for each receiving message."));
		generateTargetHandles(targetModel.getTargetReceivingMessages());
		writeNL();			
		
		// Handle for each device. Do dot use helper, since cb instances not created for devices without a handle
		
		/*
		 * Code not used any more since no support for device_ctl
		 * TODO: check whether to remove this completely.
		writeln(comment("Handle for each device."));
		for(TargetDevice next : targetModel.getTargetDevices()) {	
			if ( next.getHasHandle() ) {
				// #define <driverName> (&os_deviceh_<index>)
				writeln(DEFINE+" "+genCName(next)+" (&"+DEVICE_HANDLE_CB_NAME+"_"+next.getControlBlockIndex()+")");
			}
		}
		*/
		
		writeNLs(2);		
		
		///////////////////////////////////////////////////////////////////////////////
		// Generate access API for each COM flag
		
		if ( targetModel.isCOMPresent() ) {
			
			section("COM");
			
			// create a flag variable for each (uniquely named) flag that is set
			Map<String, Integer> flagMap = targetModel.getFlagNameMap();
			
			if ( flagMap.size() > 0 ) {
				writeln(comment("Access API for each COM notification flag"));
				
				for (String nextFlagName : flagMap.keySet()) {				
					writeln(DEFINE+" ReadFlag_"+nextFlagName+"()  ("+COM_FLAG_NAME+"_"+flagMap.get(nextFlagName)+")");
					writeln(DEFINE+" ResetFlag_"+nextFlagName+"() ("+COM_FLAG_NAME+"_"+flagMap.get(nextFlagName)+"=0)");
					writeNL();
				}
				writeNL();
			}
			
			if ( targetModel.getComAppModes().size() > 0 ) {
				writeln(comment("Handle for each COM Application Mode"));
				
				int appIndex = 1;
				
				for ( String next : targetModel.getComAppModes() ) {
					writeln(DEFINE+" "+next+" ("+appIndex+"U)");
					appIndex++;
				}
				
				writeNL();
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////////
		// Generate eventmask variables for each event
		
		if (targetModel.getTargetEvents().size() > 0) {
			
			section("EVENT MASKS");
			
			writeln(comment("Mask for each event."));
			writeNL();
			
			for (TargetEvent next : targetModel.getTargetEvents()) {
				
				BigInteger mask = next.getTargetMask();
				
				writeln(DEFINE+" "+genCName(next)+" (0x"+mask.toString(16)+"U)");
			}
			
			writeNL();
		}
			
		////////////////////////////////////////////////////////////////////////
		// Generate driver specific header file code for each device
			
		section("DEVICE DRIVER SPECIFIC CODE");
		
		for (TargetDevice device : targetModel.getTargetDevices() ) {
			
			// Use the driver to generate this, since the kind of driver determines the contents
			// of the header file code.
			device.getDriver().genCHeaderCode(device);
		}
		writeNL();
		
		
		
		
		section("END OF AUTO-GENERATED HEADER FILE");
		writeNL();
		closeWriter();
		log("Completed '"+GEN_HANDLES_FILE+"'");		
	}
	

	/**
	 * Implements constraint checking of the associated target model.
	 * 
	 * @param problems List of {@link Problem} objects, appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do ANSI C platform dependent constraint checks of target model here
		
		// [1] Any identified device driver must be of the correct type and available for generation.
		
		// Check that each counter has access to its driver
		for ( TargetCounter next : targetModel.getTargetCounters() ) {
			
			if ( next.getDevice() == null ) {
				// Counter did not get access to its driver
				
				if ( next.isInvalidDeviceType() ) {
					// driver of the wrong type (i.e unsuitable for a Counter) was specified
					problems.add(new Problem(Problem.ERROR, "Counter '"+next.getName()+"' has a device driver specified which is inapproriate for Counter use"));
				}
				else {
					// driver was not accessible, probably failed to load due to incorrect name, or non-present driver Class.
					problems.add(new Problem(Problem.ERROR, "Counter '"+next.getName()+"' has failed to access its device driver"));					
				}
			}
		}
		
		// Check that each receiving message has access to its driver
		for ( TargetReceivingMessage next : targetModel.getTargetReceivingMessages() ) {
			
			if ( next.getDevice() == null ) {
				// Message did not get access to its driver
				
				if ( next.isInvalidDeviceType() ) {
					// driver of the wrong type (i.e unsuitable for COM use) was specified
					problems.add(new Problem(Problem.ERROR, "Message '"+next.getName()+"' has a device driver specified which is inapproriate for COM use"));
				}
				else {
					// driver was not accessible, probably failed to load due to incorrect name, or non-present driver Class.
					problems.add(new Problem(Problem.ERROR, "Message '"+next.getName()+"' has failed to access its device driver"));					
				}
			}
		}		
		
        // [2] The maximum number of bits for each TargetEvent mask must not exceed the maxEventBits specified by the PlatformInfo [error]
		for (TargetEvent next : targetModel.getTargetEvents()) {
			
			if ( next.getHighestSetMaskBit() > platformInfo.getMaxEventBits() ) {
				problems.add(new Problem(Problem.ERROR, "Event '"+next.getName()+"' has a mask value where the number of bits exceeds the maximum allowed for this target ("+platformInfo.getMaxEventBits()+")"));				
			}
		}
		
		// [3] The maximum number of unique TargetTask priorities must not exceed the maxTaskPriorities specified by the PlatformInfo [error]
		if ( targetModel.getTargetPriorities().getUniqueNonIdleTaskPriorityCount() > platformInfo.getMaxTaskPriorities() ) {
			problems.add(new Problem(Problem.ERROR, "The number of unique task priorities exceeds the maximum for this target ("+platformInfo.getMaxTaskPriorities()+")"));
		}
		
		// [4] The maximum number of unique TargetISR priorities must not exceed the maxTaskPriorities specified by the PlatformInfo [error]
		if ( targetModel.getTargetPriorities().getUniqueISRPriorityCount() > platformInfo.getMaxISRPriorities() ) {
			problems.add(new Problem(Problem.ERROR, "The number of unique ISR priorities exceeds the maximum for this target ("+platformInfo.getMaxISRPriorities()+")"));
		}
		
		
		for (TargetAlarm next : targetModel.getTargetAlarms()) {
			
			// [5] The maximum alarmTime of a TargetAlarm must not exceed the tickTypeSize as specified by the PlatformInfo [error]
			if ( next.getAlarmTime() > platformInfo.getTickTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+next.getName()+"' has an alarm time that exceeds the maximum allowed for this target ("+platformInfo.getTickTypeSize()+")"));				
			}
			
			// [6] The maximum cycleTime of a TargetAlarm must must not exceed the tickTypeSize as specified by the PlatformInfo [error]
			if ( next.getCycleTime() > platformInfo.getTickTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+next.getName()+"' has a cycle time that exceeds the maximum allowed for this target ("+platformInfo.getTickTypeSize()+")"));				
			}
		}		
		
		
		for (TargetCounter next : targetModel.getTargetCounters()) {
			
			// [7] The maxAllowedValue of a TargetCounter must not exceed the tickTypeSize as specified by the PlatformInfo [error]
			if ( next.getMaxAllowedValue() > platformInfo.getTickTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Counter '"+next.getName()+"' has a max allowed value that exceeds the maximum for this target ("+platformInfo.getTickTypeSize()+")"));				
			}
			
			// [8] The minCycle of a TargetCounter must not exceed the tickTypeSize as specified by the PlatformInfo [error]
			if ( next.getMinCycle() > platformInfo.getTickTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Counter '"+next.getName()+"' has a min cycle value that exceeds the maximum for this target ("+platformInfo.getTickTypeSize()+")"));				
			}		
			
			// [9] The ticksPerBase of a TargetCounter must not exceed the tickTypeSize as specified by the PlatformInfo [error]
			if ( next.getTicksPerBase() > platformInfo.getTickTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Counter '"+next.getName()+"' has a ticks per base value that exceeds the maximum for this target ("+platformInfo.getTickTypeSize()+")"));				
			}			
		}		
				
		for (TargetScheduleTable nextTable : targetModel.getTargetScheduleTables()) {
					
			for (TargetScheduleTableXP next : nextTable.getExpiryPoints() ) {
				// [10] The delta of each TargetScheduleTableXP must not exceed the tickTypeSize as specified by the PlatformInfo [error]
				if ( next.getDelta() > platformInfo.getTickTypeSize() ) {
					problems.add(new Problem(Problem.ERROR, "Table '"+nextTable.getName()+"' has an expiry point offset that exceeds the maximum for this target ("+platformInfo.getTickTypeSize()+")"));				
				}
				// [11] The number of expiryActions within each TargetScheduleTableXP must not exceed the unatTypeSize specified by the PlatformInfo [error]
				if ( next.getExpiryActions().size() > platformInfo.getUnatTypeSize() ) {
					problems.add(new Problem(Problem.ERROR, "Table '"+nextTable.getName()+"' has too many expiry points with the same offset for this target ("+platformInfo.getUnatTypeSize()+")"));				
				}				
			}
		}
		
		// [12] The modelPreTaskHookStackSize of a TargetCPU must not exceed the maxStackSize as specified by the target [error] 
		if ( targetModel.getModelPreTaskHookStackSize() > platformInfo.getMaxStackSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has an pre-task hook stack size that exceeds the maximum for this target ("+platformInfo.getMaxStackSize()+")"));
		}
		
        // [13] The modelPostTaskHookStackSize of a TargetCPU must not exceed ths maxStackSize as specified by the target [error]
		if ( targetModel.getModelPostTaskHookStackSize() > platformInfo.getMaxStackSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has an post-task hook stack size that exceeds the maximum for this target ("+platformInfo.getMaxStackSize()+")"));
		}
		
		// [14] The bufferSize of a TargetReceivingMessage must not exceed the osBlockSize  specified by the PlatformInfo [error] 
		// Check that each receiving message has access to its driver
		for ( TargetReceivingMessage next : targetModel.getTargetReceivingMessages() ) {
			if ( next.getBufferSize() > platformInfo.getOsBlockTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+next.getName()+"' has an buffer size that exceeds the maximum for this target ("+platformInfo.getOsBlockTypeSize()+")"));
			}
		}
		
		// [15] The sizeof(dataTypeName) of a TargetSendingMessage must not exceed the osBlockSize specified by the PlatformInfo [error]
        // note: This check can not be implemented since it is generated as a  sizeof(dataTypeName) string, thus there is no way of knowing the actual size generated.
		
		
		// [16] The modelStackSize of each TargetRunnable must not exceed the maxStackSize specified by the PlatformInfo [error]
		for ( TargetRunnable next : targetModel.getTargetTasks() ) {
			if ( next.getModelStackSize() > platformInfo.getMaxStackSize() ) {
				problems.add(new Problem(Problem.ERROR, "Task '"+next.getName()+"' has a stack size that exceeds the maximum for this target ("+platformInfo.getMaxStackSize()+")"));
			}
		}
		for ( TargetRunnable next : targetModel.getTargetISRs() ) {
			if ( next.getModelStackSize() > platformInfo.getMaxStackSize() ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has a stack size that exceeds the maximum for this target ("+platformInfo.getMaxStackSize()+")"));
			}
		}	
		
		// [17] The modelActivation count of each TargetTask must not exceed the unatTypeSize specified by the PlatformInfo [error]
		for ( TargetTask next : targetModel.getTargetTasks() ) {
			if ( next.getModelActivation() > platformInfo.getUnatTypeSize() ) {
				problems.add(new Problem(Problem.ERROR, "Task '"+next.getName()+"' has an activation count that exceeds the maximum for this target ("+platformInfo.getUnatTypeSize()+")"));
			}
		}
		
		// [18] The number of targetReceivers for each TargetSendingMessage must not exceed the size of the uint16 dataType (0xffff) [error]
		for ( TargetSendingMessage next : targetModel.getTargetSendingMessages() ) {
			if ( next.getTargetReceivers().size() > 0xffff ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+next.getName()+"' has more receivers than that allowed for this target (65535)"));
			}
		}		
		
		// [19] The maximum queueSize of a TargetReceivingMessage must not exceed the size of the uint16 dataType (0xffff) [error]
		for ( TargetReceivingMessage next : targetModel.getTargetReceivingMessages() ) {
			if ( next.getQueueSize() > 0xffff ) {
				problems.add(new Problem(Problem.ERROR, "Message '"+next.getName()+"' has a queue size more than 65535"));
			}
		}
		
		// [20]  The maximum number of sending messages and receiving messages and initialized COM devices must each not exceed the size of the uint16 dataType (0xffff) [error]
		if ( targetModel.getTargetSendingMessages().size() > 0xffff ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more sending messages defined than that allowed for this target (65535)"));
		}
		if ( targetModel.getTargetReceivingMessages().size() > 0xffff ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more receiving messages defined than that allowed for this target (65535)"));
		}		
		if ( targetModel.getAutoStartedCOMDevices().size() > 0xffff ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more auto started/stopped COM devices defined than that allowed for this target (65535)"));
		}		
		
		// [21] The maximum number of tasks, ISRs, counters, alarms and schedule tables messages must each not exceed that of unat as specified by the target [error]
		if ( targetModel.getTargetTasks().size() > platformInfo.getUnatTypeSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more tasks defined than allowed for this platform ("+platformInfo.getUnatTypeSize()+")"));
		}
		if ( targetModel.getTargetISRs().size() > platformInfo.getUnatTypeSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more ISRs defined than allowed for this platform ("+platformInfo.getUnatTypeSize()+")"));
		}	
		if ( targetModel.getTargetCounters().size() > platformInfo.getUnatTypeSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more counters defined than allowed for this platform ("+platformInfo.getUnatTypeSize()+")"));
		}	
		if ( targetModel.getTargetAlarms().size() > platformInfo.getUnatTypeSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more alarms defined than allowed for this platform ("+platformInfo.getUnatTypeSize()+")"));
		}	
		if ( targetModel.getTargetScheduleTables().size() > platformInfo.getUnatTypeSize() ) {
			problems.add(new Problem(Problem.ERROR, "CPU '"+targetModel.getName()+"' has a more schedule tables defined than allowed for this platform ("+platformInfo.getUnatTypeSize()+")"));
		}	
		
	}	
		
	
	/**
	 * Constructor used to instantiate the OSAnsiCGenerator with a specific set of target specific parameter values.
	 * 
	 * @param targetModel the root TargetCpu of the Target Model for which generation is to be performed
	 * @param logger a PrintWriter that is to be used for logging during generation (may be null)
	 * @param loggerPrefix the prefix attached to each logged message (may be null)
	 */
	protected OSAnsiCGenerator(TargetCpu targetModel,PrintWriter logger,String loggerPrefix) {	
		super(targetModel,logger,loggerPrefix);
		
		// Setup the platformInfo for this target/compiler
		platformInfo = new PlatformInfo(	
										EXT_TASK_CB_NAME,	 			// extTaskCBName
										BASIC_TASK_CB_NAME,	 			// basicTaskCBName
										ISR_CB_NAME,		 			// isrCBName
										APPMODE_CB_NAME,	 			// appModeCBName
										RESOURCE_CB_NAME,	 			// resourceCBName
										COUNTER_CB_NAME,	 			// counterCBName
										ALARM_CB_NAME,	     			// alarmCBName
										SCHEDULETABLE_CB_NAME,			// scheduleTableCBName
										COM_SENDING_MESSAGE_CB_NAME,	// sendingMessageCBName
										COM_RECEIVING_MESSAGE_CB_NAME,	// receivingMessageCBName
										STD_QUEUE_CB_NAME,	 			// stdQueueCBName
										OPT_QUEUE_CB_NAME,	 			// optQueueCBName
										DRIVER_CB_NAME,					// driverCBName
										DEVICE_CB_NAME					// deviceCBName
		);		
	}
}
