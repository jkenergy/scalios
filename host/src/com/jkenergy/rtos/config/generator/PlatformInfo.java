package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-02-25 21:38:01 +0000 (Mon, 25 Feb 2008) $
 * $LastChangedRevision: 623 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/PlatformInfo.java $
 * 
 */


/**
 * This class stores both language specific and target specific information that may be used by the generic
 * generators during code generation. 
 * 
 * @see OSGenerator
 * @see OSAnsiCGenerator
 * 
 * @author Mark Dixon
 *
 */

public class PlatformInfo {

	// Name of generated variable/array instances for a specific target language, e.g. 'C'
	private String extTaskCBName;
	private String basicTaskCBName;
	private String isrCBName;
	private String appModeCBName;
	private String resourceCBName;
	private String counterCBName;
	private String alarmCBName;
	private String scheduleTableCBName;
	private String sendingMessageCBName;
	private String receivingMessageCBName;
	private String stdQueueCBName;
	private String optQueueCBName;
	private String driverCBName;
	private String deviceCBName;
	
	
	/**
	 * Datatype used to define the OSTICKDURATION variable
	 */
	private String tickDurationType;	
	
	/**
	 * Flag to specify whether or not the target/compiler requires a Priority-to-IPL lookup table to be generated.
	 */
	private boolean pri2IPLLookupRequired;
	
		
	/**
	 * The number of bytes put on the stack of an extended task by the kernel (built for extended status) between 
	 * the point where stack switching is turned off and the switch to the kernel stack is made (i.e. after DISABLE_STACKCHECK
	 * or similar in the code, and the save of the extended task's stack pointer in the call to the switch2ks() function).
	 */
	private int stdStatusExtStackKernelUsage;
	
	/**
	 * The number of bytes put on the stack of an extended task by the kernel but for the standard status builds.
	 */
	private int extStatusExtStackKernelUsage;
	
	/**
	 * Stack space to allow for stack overflow handling.
	 * The additional space (in bytes) that needs to be added above top of stack for a task.
	 */	
	private int stackOverflowSpace;
	

	/**
	 * The additional space (in bytes) that needs to be added to the basic task declared os model stack usage.
	 * 
	 * This value is the extra number of bytes put on the stack between the point where the kernel
	 * reads the stack pointer (via OFFSET_CURTOS_TASK) and the first (machine) instruction of the task
	 * entry function (i.e. the kernel stack usage between the get of the stack and the user task running).
	 * 
	 * This is going to be at least the size of a return address on the stack.
	 * 
	 */	
	private int kernelPreTaskEntryUsage;
	

	/**
	 * The additional space (in bytes) that needs to be added to the ISR declared os model stack usage.
	 * 
	 * This value is the extra number of bytes put on the stack between the point where the kernel
	 * reads the stack pointer (via OFFSET_CURTOS_ISR) and the first (machine) instruction of the ISR
	 * entry function (i.e. the kernel stack usage between the get of the stack and the user ISR handler running).
	 * 
	 */		
	private int kernelPreISREntryUsage;
	
	
	/**
	 *
	 * The offset (in bytes) that is to be applied to the top of stack value for a specific target.
	 * A negative value means below the stack pointer value, i.e. pointing into the used stack,
	 * A positive value means above the stack pointer value, i.e. pointing above the used stack.
	 * NOTE: this does not specify target stack direction, but stack checking offset requirements.
	 * 
	 * e.g. For the dsPIC target offset stack checking by -2 for the SPLIM h/w check.
	 * 
	 */
	private int topOfStackCheckOffset;

	
	/**
	 * The additional space (in bytes) that needs to be added to the task hook stack declared os model stack usage.
	 * 
	 * This value is the extra number of bytes put on the stack between the point where the kernel
	 * reads the stack pointer (via OFFSET_CURTOS_HOOK) and the first (machine) instruction of the hook
	 * entry function (i.e. the kernel stack usage between the get of the stack and the user hook running).
	 * 
	 */	
	private int kernelTaskHookEntryUsage;	
	

	/**
	 * 
	 * true if the stack is ascending for the target, i.e. a stack that grows upwards in memory
	 */
	private boolean isAscendingStack;
	
	/**
	 * 
	 * true if the stack is post-offset for the target,
	 * i.e. a stack pointer that is incremented/decremented (depending on stack direction)
	 * after a PUSH type operation.
	 */
	private boolean isPostOffsetSP;
	
	
	/**
	 * The number of bytes required for the stack alignment, e.g. 2 for a 16bit aligned stack.
	 */
	private int stackAlignmentBytes;
	

	/**
	 * Flag that indicates whether API calls can be made above kernel IPL level (the OSEK standard forbids this)
	 */
	private boolean hasAPICallsAboveKernelLevel;

	/**
	 * @return Returns the defaultISRStackSize.
	 */
	protected int getDefaultISRStackSize() {
		return defaultISRStackSize;
	}

	/**
	 * @return Returns the defaultPostTaskHookStackSize.
	 */
	protected int getDefaultPostTaskHookStackSize() {
		return defaultPostTaskHookStackSize;
	}

	/**
	 * @return Returns the defaultPreTaskHookStackSize.
	 */
	protected int getDefaultPreTaskHookStackSize() {
		return defaultPreTaskHookStackSize;
	}

	/**
	 * @return Returns the defaultTaskStackSize.
	 */
	protected int getDefaultTaskStackSize() {
		return defaultTaskStackSize;
	}


	/**
	 * maximum number of bits allowed in an event mask for the target.
	 */
	private int maxEventBits;
	
	/**
	 * maximum number of unique task priorities allowed for the target.
	 */
	private int maxTaskPriorities;	

	/**
	 * maximum number of unique ISR priorities allowed for the target.
	 */
	private int maxISRPriorities;		
	
	/**
	 * max value that can be assigned to TickType typed values on the target.
	 */
	private long tickTypeSize;
	
	
	/**
	 * max value that can be assigned to unat typed values on the target.
	 */
	private long unatTypeSize;
	
	/**
	 * max value that can be assigned to os_block_type values on the target.
	 */
	private long osBlockTypeSize;	

	/**
	 * max value that can be assigned to stack size values on the target.
	 */
	private long maxStackSize;
	
	/**
	 * default size to use for tasks if not explicitly specified by the user.
	 */
	private int defaultTaskStackSize;	
	
	/**
	 * default size to use for ISRs if not explicitly specified by the user.
	 */
	private int defaultISRStackSize;	
	
	/**
	 * default size to use for pre-task hooks if not explicitly specified by the user.
	 */
	private int defaultPreTaskHookStackSize;	
	
	/**
	 * default size to use for post-task hooks if not explicitly specified by the user.
	 */
	private int defaultPostTaskHookStackSize;	
	
	/**
	 * 
	 * @return the tickDurationType value.
	 */
	public String getTickDurationType() {
		return tickDurationType;
	}
	
	/**
	 * @param tickDurationType the datatype name used to define the OSTICKDURATION variable
	 */
	public void setTickDurationType(String tickDurationType) {
		this.tickDurationType = tickDurationType;
	}
	 
	/**
	 * @return the maxEventBits value.
	 */
	protected int getMaxEventBits() {
		return maxEventBits;
	}

	/**
	 * @return the maxTaskPriorities value.
	 */
	protected int getMaxTaskPriorities() {
		return maxTaskPriorities;
	}
	
	/**
	 * @return the maxISRPriorities value.
	 */
	protected int getMaxISRPriorities() {
		return maxISRPriorities;
	}
	
	/**
	 * @return the tickTypeSize value.
	 */
	protected long getTickTypeSize() {
		return tickTypeSize;
	}		

	/**
	 * @return the unatTypeSize value.
	 */
	protected long getUnatTypeSize() {
		return unatTypeSize;
	}
	
	/**
	 * @return the nat type size (which is unat>>1)
	 */
	protected long getNatTypeSize() {
		return unatTypeSize>>1;
	}	
	
	/**
	 * @return the osBlockTypeSize value.
	 */
	protected long getOsBlockTypeSize() {
		return osBlockTypeSize;
	}		
	
	/**
	 * @return the maxStackSize value.
	 */	
	protected long getMaxStackSize() {
		return maxStackSize;
	}
	
	/**
	 * @return the kernelTaskHookEntryUsage value.
	 */
	protected int getKernelTaskHookEntryUsage() {
		return kernelTaskHookEntryUsage;
	}

	/**
	 * @return the hasAPICallsAboveKernelLevel flag.
	 */
	protected boolean isHasAPICallsAboveKernelLevel() {
		return hasAPICallsAboveKernelLevel;
	}


	/**
	 * @return the basicTaskCBName string.
	 */
	protected String getBasicTaskCBName() {
		return basicTaskCBName;
	}


	/**
	 * @return the extTaskCBName string.
	 */
	protected String getExtTaskCBName() {
		return extTaskCBName;
	}


	/**
	 * @return the isrCBName string.
	 */
	protected String getIsrCBName() {
		return isrCBName;
	}

	/**
	 * @return the AppModeCBName string.
	 */
	protected String getAppModeCBName() {
		return appModeCBName;
	}	


	/**
	 * @return the optQueueCBName string.
	 */
	protected String getOptQueueCBName() {
		return optQueueCBName;
	}



	/**
	 * @return the resourceCBName string.
	 */
	protected String getResourceCBName() {
		return resourceCBName;
	}

	/**
	 * @return the counterCBName string.
	 */
	protected String getCounterCBName() {
		return counterCBName;
	}
	
	/**
	 * @return the alarmCBName string.
	 */
	protected String getAlarmCBName() {
		return alarmCBName;
	}	
	
	/**
	 * @return the scheduleTableCBName string.
	 */
	protected String getScheduleTableCBName() {
		return scheduleTableCBName;
	}
	
	/**
	 * @return the sendingMessageCBName string.
	 */
	protected String getSendingMessageCBName() {
		return sendingMessageCBName;
	}	
	
	/**
	 * @return the receivingMessageCBName string.
	 */
	protected String getReceivingMessageCBName() {
		return receivingMessageCBName;
	}	

	/**
	 * @return the driverCBName string.
	 */
	protected String getDriverCBName() {
		return driverCBName;
	}
	
	/**
	 * @return the deviceCBName string.
	 */
	protected String getDeviceCBName() {
		return deviceCBName;
	}	
	
	/**
	 * @return the stdQueueCBName string.
	 */
	protected String getStdQueueCBName() {
		return stdQueueCBName;
	}



	/**
	 * @return the pri2IPLLookupRequired flag.
	 */
	public boolean isPri2IPLLookupRequired() {
		return pri2IPLLookupRequired;
	}



	/**
	 * @return the isAscendingStack flag.
	 */
	public boolean isAscendingStack() {
		return isAscendingStack;
	}



	/**
	 * @return the isPostOffsetSP flag.
	 */
	public boolean isPostOffsetSP() {
		return isPostOffsetSP;
	}


	/**
	 * @return the kernelPreISREntryUsage value.
	 */
	public int getKernelPreISREntryUsage() {
		return kernelPreISREntryUsage;
	}


	/**
	 * @return the kernelPreTaskEntryUsage value.
	 */
	public int getKernelPreTaskEntryUsage() {
		return kernelPreTaskEntryUsage;
	}


	/**
	 * @return the stackAlignmentBytes value.
	 */
	public int getStackAlignmentBytes() {
		return stackAlignmentBytes;
	}


	/**
	 * Calculates and returns the stack space to allow for interrupts; the additional space (in bytes) that needs to be added to top of stack.
	 * If target implementation uses software stack checking then this value includes space for the magic check word. 
	 *
	 * The value returned is calculated as -
	 * 	maximum( maximum of (KernelUsage, CAT2 race nesting, CAT1 usage at kernel level) + CAT1 nesting (above kernel level), stack overflow)
	 * 
	 * @param extendedStatus flag indicating whether headroom is required for an extended status build, or a standard status build
	 * @return the stackHeadroom value.
	 */
	public int getStackHeadroom(boolean extendedStatus) {
		
		int maxUsage = 0;
		
		// TODO need to calculate/add additional stack usage values into this calculation, see comment above.
		
		if ( extendedStatus ) {
			if ( extStatusExtStackKernelUsage > maxUsage) {
				maxUsage = extStatusExtStackKernelUsage;
			}			
		}
		else {
			if ( stdStatusExtStackKernelUsage > maxUsage) {
				maxUsage = stdStatusExtStackKernelUsage;
			}
		}
		
		if ( stackOverflowSpace > maxUsage) {
			maxUsage = stackOverflowSpace;
		}		
		
		return maxUsage;
	}


	/**
	 * @return the topOfStackCheckOffset value.
	 */
	public int getTopOfStackCheckOffset() {
		return topOfStackCheckOffset;
	}


	/**
	 * Maps an index (in bytes) value to a stackword value for a specific target.
	 * 
	 * @param index the index (in bytes) to be mapped
	 * @return the index mapped to the stackword for a specific target.
	 */
	protected int asStackAlignmentWord(long index) {
		
		assert (index % stackAlignmentBytes)==0;	// byte index offset must match stackAlignmentBytes
		
		return (int)(index/stackAlignmentBytes);
	}	

	
	/**
	 * Sets up the architecture information for the platform.
	 * 
	 * @param pri2IPLLookupRequired flag that specifies whether or not the target/compiler requires a Priority-to-IPL lookup table to be generated
	 * @param hasAPICallsAboveKernelLevel flag that specifies whether API calls can be made above kernel IPL level (the OSEK standard forbids this)
	 *
	 */
	protected void setArchitecture(boolean pri2IPLLookupRequired, boolean hasAPICallsAboveKernelLevel) {
		this.pri2IPLLookupRequired = pri2IPLLookupRequired;
		this.hasAPICallsAboveKernelLevel = hasAPICallsAboveKernelLevel;
	}
	
	/**
	 * Sets up the limits information for the platform.
	 * 
	 * @param eventMaskWordSize maximum number of bits in an EventMaskType for the target
	 * @param maxTaskPriorities maximum number of unique task priorities allowed for the target
	 * @param maxISRPriorities maximum number of unique ISR priorities allowed for the target
	 * @param tickTypeSize max value that can be assigned to TickType typed values on the target
	 * @param unatTypeSize max value that can be assigned to unat typed values on the target
	 * @param osBlockTypeSize max value that can be assigned to os_block_type values on the target
	 * @param maxStackSize max value that can be assigned to stack size values on the target
	 *
	 */
	protected void setLimits(int eventMaskWordSize, int maxTaskPriorities, int maxISRPriorities, long tickTypeSize, long unatTypeSize, long osBlockTypeSize, long maxStackSize) {
		this.maxEventBits = eventMaskWordSize - 1;	// top bit is reserved by the OS (See definition of DUMMY_EVENT in osinttarget.h)
		this.maxTaskPriorities = maxTaskPriorities;
		this.maxISRPriorities = maxISRPriorities;
		this.tickTypeSize = tickTypeSize;
		this.unatTypeSize = unatTypeSize;
		this.osBlockTypeSize = osBlockTypeSize;
		this.maxStackSize = maxStackSize;
	}	
	
	
	/**
	 * Sets up the stack information for the platform.
	 * 
	 * @param defaultTaskStackSize default size to use for tasks if not explicitly specified by the user.
	 * @param defaultISRStackSize default size to use for ISRs if not explicitly specified by the user.
	 * @param defaultPreTaskHookStackSize default size to use for pre-task hooks if not explicitly specified by the user.
	 * @param defaultPostTaskHookStackSize default size to use for post-task hooks if not explicitly specified by the user.
	 * @param stackOverflowSpace the stack space to allow for stack overflow handling.
	 * @param kernelPreTaskEntryUsage the additional space (in bytes) that needs to be added to the basic task declared os model stack usage.
	 * @param kernelPreISREntryUsage the additional space (in bytes) that needs to be added to the ISR declared os model stack usage.
	 * @param topOfStackCheckOffset the offset (in bytes) that is to be applied to the top of stack value for a specific target.
	 * @param kernelPreTaskHookEntryUsage the additional space (in bytes) that needs to be added to the task hook stack declared os model stack usage.
	 * @param isAscendingStack flag indicating that the stack is ascending for the target
	 * @param isPostOffsetSP flag indicating that the stack is post-offset for the target
	 * @param stackAlignmentBytes the number of bytes required for the stack alignment, e.g. 2 for a 16bit aligned stack.
	 */
	protected void setUpStackDetais(int defaultTaskStackSize, int defaultISRStackSize, int defaultPreTaskHookStackSize, int defaultPostTaskHookStackSize, int stackOverflowSpace, int kernelPreTaskEntryUsage, int kernelPreISREntryUsage, int topOfStackCheckOffset, int kernelPreTaskHookEntryUsage, boolean isAscendingStack, boolean isPostOffsetSP, int stackAlignmentBytes) {
		
		this.defaultTaskStackSize = defaultTaskStackSize;
		this.defaultISRStackSize = defaultISRStackSize;
		this.defaultPreTaskHookStackSize = defaultPreTaskHookStackSize;
		this.defaultPostTaskHookStackSize = defaultPostTaskHookStackSize;
		this.stackOverflowSpace = stackOverflowSpace;
		this.kernelPreTaskEntryUsage = kernelPreTaskEntryUsage;
		this.kernelPreISREntryUsage = kernelPreISREntryUsage;
		this.topOfStackCheckOffset = topOfStackCheckOffset;
		this.kernelTaskHookEntryUsage = kernelPreTaskHookEntryUsage;
		this.isAscendingStack = isAscendingStack;
		this.isPostOffsetSP = isPostOffsetSP;
		this.stackAlignmentBytes = stackAlignmentBytes;		
	}
	
	
	/**
	 * Sets the kernel stack usage for the standard status build on the extended stacks for the platform.
	 * 
	 * The value assigned is the maximum of the list given, over all calls to this method.
	 * 
	 * @param usageSizes list of usage size values (in bytes)
	 */
	protected void setupStdStatusExtStackKernelUsage(int... usageSizes) {
		
		for (int i = 0; i < usageSizes.length; i++) {
			int j = usageSizes[i];
			
			if ( j > stdStatusExtStackKernelUsage ) {
				stdStatusExtStackKernelUsage = j;
			}
		}
	}
	
	
	/**
	 * Sets the kernel stack usage for the extended status build on the extended stacks for the platform.
	 * 
	 * The value assigned is the maximum of the list given, over all calls to this method.
	 * 
	 * @param usageSizes list of usage size values (in bytes)
	 */	
	protected void setupExtStatusExtStackKernelUsage(int... usageSizes) {
		
		for (int i = 0; i < usageSizes.length; i++) {
			int j = usageSizes[i];
			
			if ( j > extStatusExtStackKernelUsage ) {
				extStatusExtStackKernelUsage = j;
			}
		}
	}
	
	/**
	 * Constructor for the PlatformInfo class.
	 * 
	 * @param extTaskCBName the variable name of the extended task control block
	 * @param basicTaskCBName the variable name of the basic task control block
	 * @param isrCBName the variable name of the ISR control block
	 * @param appModeCBName the variable name of the appMode control block
	 * @param resourceCBName the variable name of the resource control block
	 * @param counterCBName the variable name of the counter control block
	 * @param alarmCBName the variable name of the alarm control block
	 * @param scheduleTableCBName the variable name of the extended task control block
	 * @param sendingMessageCBName the variable name of the sending message control block
	 * @param receivingMessageCBName the variable name of the receiving message control block
	 * @param stdQueueCBName the variable name of the standard queue control block
	 * @param optQueueCBName the variable name of the opimized queue control block
	 * @param driverCBName the variable name of the driver control block
	 * @param deviceCBName the variable name of the device control block
	 */
	public PlatformInfo(String extTaskCBName, String basicTaskCBName, String isrCBName, String appModeCBName, String resourceCBName, String counterCBName, String alarmCBName, String scheduleTableCBName, String sendingMessageCBName, String receivingMessageCBName, String stdQueueCBName, String optQueueCBName, String driverCBName, String deviceCBName) {
		super();
		this.extTaskCBName = extTaskCBName;
		this.basicTaskCBName = basicTaskCBName;
		this.isrCBName = isrCBName;
		this.appModeCBName = appModeCBName;
		this.resourceCBName = resourceCBName;
		this.counterCBName = counterCBName;
		this.alarmCBName = alarmCBName;
		this.scheduleTableCBName = scheduleTableCBName;
		this.sendingMessageCBName = sendingMessageCBName;
		this.receivingMessageCBName = receivingMessageCBName;
		this.stdQueueCBName = stdQueueCBName;
		this.optQueueCBName = optQueueCBName;
		this.driverCBName = driverCBName;
		this.deviceCBName = deviceCBName;
	}

}
