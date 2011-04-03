package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetCpu.java $
 * 
 */

import java.math.BigInteger;
import java.util.*;

import com.jkenergy.rtos.config.osmodel.*;

/**
 * A SubClass of TargetElement that is used to store information on OS configurations to be generated.<br><br>
 * 
 * An instance of this class always acts as the "root" of a Target Model. Hence, this class defines
 * references to other {@link TargetElement} derived classes such as {@link TargetTask}, {@link TargetEvent} and
 * {@link TargetResource} etc.<br><br>
 * 
 * In addition to acting as the container for other Target Model Elements, this class provides a number of methods that are required
 * to prepare a target model for use during generation. Including self-population from an OS Model during construction.
 * 
 * @author Mark Dixon
 *
 */
public class TargetCpu extends TargetElement {

	
	/**
	 * Enum type that specifies the nature of the type of tasks defined within the TargetCpu
	 */
	public enum TasksetType {
		NO_TASKS,
		EXTENDED_ONLY,
		BASIC_ONLY,
		MIXED_TASKS
	}	
	
	/**
	 * Constant that represents attribute values that have not been explicitly defined within the model.
	 */
	private final static int NOT_DEFINED = -1;
	
	/**
	 * Constant that specifies the name of the RES_SCHEDULER resource
	 */	
	final static String RES_SCHEDULER_NAME = "RES_SCHEDULER";

	/**
	 * Constant that specifies the name of the COM hook resource
	 */		
	final static String COM_HOOK_RES_NAME = "COM_RES_OS";
	
	/**
	 * Constant that specifies the name of the default app mode
	 */
	private final static String OS_DEFAULT_APP_MODE = "OSDEFAULTAPPMODE";		
	
	/**
	 * The {@link DriverManager} used by the CPU
	 */
	private DriverManager driverManager = new DriverManager(this);

	/**
	 * Flag indicating extended status.
	 */ 
	private boolean isExtendedStatus;	
	
	/**
	 * Startup Hook flag
	 */
	private boolean startupHook;
	
	/**
	 * Error hook flag
	 */
	private boolean errorHook;
	
	/**
	 * Shutdown hook flag
	 */
	private boolean shutdownHook;
	
	/**
	 * Pre-task hook flag
	 */
	private boolean preTaskHook;
	
	/**
	 * Post-task hook flag
	 */
	private boolean postTaskHook;
	
	/**
	 * Use Get Service ID flag
	 */
	private boolean useGetServiceId;
	
	/**
	 * Use Parameter Access flag
	 */
	private boolean useParameterAccess;
	
	/**
	 * Use Resscheduler flag
	 */	
	private boolean useResScheduler;	
	
	/**
	 * OS Restartable flag (i.e. StartOS may be called more than once)
	 */
	private boolean isRestartable;
	
	/**
	 * OS StackChecking flag, specifies whether stack checking is globally enabled
	 */
	private boolean isStackCheckingEnabled;	
	
	/**
	 * Handle Osc Failure flag, indicates whether handler should be provided
	 */
	private boolean oscFailureHandled;
	
	/**
	 * Handle Addr Error flag, indicates whether handler should be provided
	 */
	private boolean addrErrorHandled;	
	
	/**
	 * Handle Math Error flag, indicates whether handler should be provided
	 */
	private boolean mathErrorHandled;		
	
	/**
	 * The size of the stack requested for the pre task hook.
	 */
	private long modelPreTaskHookStackSize;
	
	/**
	 * The size of the stack requested for the post task hook.
	 */
	private long modelPostTaskHookStackSize;	
	
	/**
	 * isAutoPreTaskHookStackSize flag that specifies whether the modelPreTaskHookStackSize is to be automatically calculated
	 */
	private boolean isAutoPreTaskHookStackSize=false;
	
	/**
	 * isAutoPostTaskHookStackSize flag that specifies whether the modelPostTaskHookStackSize is to be automatically calculated
	 */
	private boolean isAutoPostTaskHookStackSize=false;		
	
	/**
	 * The offset (in bytes) applied to a pre task hook stack that identifies the TOS check value
	 * This is defined in a positive sense but may be numerically negative in certain cases, e.g.
	 * on a dsPIC a task stack usage of 0 gives a stack offset of -2 when using hardware stack checking (SPLIM)
	 */
	private long preTaskHookStackOffset;		
	
	/**
	 * The offset (in bytes) applied to a post task hook stack that identifies the TOS check value
	 */	
	private long postTaskHookStackOffset;	
	
	
	/**
	 * The RES_SCHEDULER TargetResource (if required)
	 */
	private TargetResource resSchedulerResource;
	
	/**
	 * The COM_RES_OS TargetResource (if required)
	 */	
	private TargetResource resCOMHook;
	
	/**
	 * The idle target task.
	 */
	private TargetTask idleTask;
	
	
	/**
	 * The default target app mode
	 */
	private TargetAppMode defaultAppMode;
	
	/**
	 * Flag indicating whether the COM layer configuration is present.
	 */ 	
	private boolean COMPresent;	
	
	/**
	 * Flag indicating extended status within the COM layer.
	 */ 
	private boolean COMExtendedStatus;	
	
	/**
	 * COM Error hook flag
	 */
	private boolean COMErrorHook;	
	
	/**
	 * Use COM Get Service ID flag
	 */
	private boolean COMUseGetServiceId;
	
	/**
	 * Use COM Parameter Access flag
	 */
	private boolean COMUseParameterAccess;	
	
	/**
	 * Start COM extension flag
	 */
	private boolean COMStartCOMExtension;	
	
	/**
	 * Set of COM application models available
	 */
	private Collection<String> comAppModes = new LinkedHashSet<String>();	
	
	
	/**
	 * Map that relates OSModelElement instances to TargetElements that represent them.
	 * 
	 * This allows a target element to be located given a model element.
	 * 
	 * e.g. helps when translating associations from the OS Model to the Target Model.
	 */
	private Map<OSModelElement, TargetElement> modelElement2TargetElementMap = new HashMap<OSModelElement, TargetElement>();
	
	
	/**
	 * Map that relates COM Flag names to an index value.
	 * 
	 * This allows a single flag name to be shared between different COM messages
	 * * @see #preGenProcessing() 
	 */
	private Map<String, Integer> COMFlagNametoIndexMap = new LinkedHashMap<String, Integer>();
	
	
	
	/**
	 * Handles information relating to model priorities and their target counterparts. 
	 */
	private TargetPriorities targetPriorities = new TargetPriorities(this);	
		
	/**
	 * The Set of that contains TargetTask instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetTask> targetTasks = new LinkedHashSet<TargetTask>();	

	
	/**
	 * The Set of that contains TargetAlarm instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetAlarm> targetAlarms = new LinkedHashSet<TargetAlarm>();	
	
	/**
	 * The Set of that contains TargetScheduleTable instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetScheduleTable> targetScheduleTables = new LinkedHashSet<TargetScheduleTable>();	
	
	/**
	 * The Set of that contains TargetSendingMessage instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetSendingMessage> targetSendingMessages = new LinkedHashSet<TargetSendingMessage>();	

	/**
	 * The Set of that contains TargetReceivingMessage instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetReceivingMessage> targetReceivingMessages = new LinkedHashSet<TargetReceivingMessage>();	
	
	
	/**
	 * The Set of that contains TargetCounter instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetCounter> targetCounters = new LinkedHashSet<TargetCounter>();	
	
	
	/**
	 * The Set of that contains TargetISR instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetISR> targetISRs = new LinkedHashSet<TargetISR>();	

	/**
	 * The Set of that contains TargetAppMode instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetAppMode> targetAppModes = new LinkedHashSet<TargetAppMode>();	
	
	/**
	 * The Set of that contains TargetEvent instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetEvent> targetEvents = new LinkedHashSet<TargetEvent>();	
	
	
	/**
	 * The Set of that contains TargetResource instances
	 * @see #createTargetElements(Cpu)
	 */	
	private Collection<TargetResource> targetResources = new LinkedHashSet<TargetResource>();	
	

	/**
	 * The subset targetTasks that contains TargetTask instances that are basic tasks
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetTask> targetBasicTasks = new LinkedHashSet<TargetTask>();	

	/**
	 * The subset targetTasks that contains TargetTask instances that are extended tasks
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetTask> targetExtendedTasks = new LinkedHashSet<TargetTask>();	
	
	
	/**
	 * The subset targetResources that contains TargetResource instances that are internal resources.
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetResource> targetAccessedInternalResources = new LinkedHashSet<TargetResource>();	
	
	/**
	 * The subset targetResources that contains TargetResource instances that are non-internal resources.
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetResource> targetAccessedNonInternalResources = new LinkedHashSet<TargetResource>();	
	
	/**
	 * Contains TargetQueue instances.
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetQueue> targetQueues = new LinkedHashSet<TargetQueue>();
	
	/**
	 * The subset of targetQueues that contains TargetQueue instances that are optimized queues (i.e. contain no dynamic queue element).
	 * @see #preGenProcessing(PlatformInfo)
	 */	
	private Collection<TargetQueue> targetOptimizedQueues = new LinkedHashSet<TargetQueue>();
	
	/**
	 * The subset of targetQueues that contains TargetQueue instances that are standard (non-optimized) queues.
	 * @see #preGenProcessing(PlatformInfo)
	 */		
	private Collection<TargetQueue> targetStandardQueues = new LinkedHashSet<TargetQueue>();
	
		
	
	
	/**
	 * The total number of stackwords required for the extended tasks' stack.
	 * @see #preGenProcessing(PlatformInfo)
	 */
	private int extTaskStackWords = NOT_DEFINED;	

	/**
	 * The total number of slots required for the priority queues.
	 * @see #preGenProcessing(PlatformInfo)
	 */
	private int queueSlots = NOT_DEFINED;	
	
	
	/**
	 * @return Returns the addrErrorHandled.
	 */
	public boolean isAddrErrorHandled() {
		return addrErrorHandled;
	}


	/**
	 * @return Returns the mathErrorHandled.
	 */
	public boolean isMathErrorHandled() {
		return mathErrorHandled;
	}


	/**
	 * @return Returns the oscFailureHandled.
	 */
	public boolean isOscFailureHandled() {
		return oscFailureHandled;
	}


	/**
	 * @return Returns the isAutoPostTaskHookStackSize.
	 */
	protected boolean isAutoPostTaskHookStackSize() {
		return isAutoPostTaskHookStackSize;
	}


	/**
	 * @return Returns the isAutoPreTaskHookStackSize.
	 */
	protected boolean isAutoPreTaskHookStackSize() {
		return isAutoPreTaskHookStackSize;
	}


	/**
	 * @return Returns the defaultAppMode.
	 */
	protected TargetAppMode getDefaultAppMode() {
		return defaultAppMode;
	}


	/**
	 * @return Returns the postTaskHookStackOffset.
	 */
	protected long getPostTaskHookStackOffset() {
		return postTaskHookStackOffset;
	}


	/**
	 * @return Returns the preTaskHookStackOffset.
	 */
	protected long getPreTaskHookStackOffset() {
		return preTaskHookStackOffset;
	}


	/**
	 * @return Returns the modelPostTaskHookStackSize.
	 */
	protected long getModelPostTaskHookStackSize() {
		return modelPostTaskHookStackSize;
	}


	/**
	 * @return Returns the modelPreTaskHookStackSize.
	 */
	protected long getModelPreTaskHookStackSize() {
		return modelPreTaskHookStackSize;
	}


	/**
	 * @return Returns the isRestartable.
	 */
	protected boolean isRestartable() {
		return isRestartable;
	}

	/**
	 * @return Returns the isStackCheckingEnabled.
	 */
	protected boolean isStackCheckingEnabled() {
		return isStackCheckingEnabled;
	}

	/**
	 * @return Returns the idleTask.
	 */
	protected TargetTask getIdleTask() {
		return idleTask;
	}
	
	/**
	 * @return the COMPresent flag
	 */
	protected boolean isCOMPresent() {
		return COMPresent;
	}	

	/**
	 * @return the COMExtendedStatus flag
	 */
	protected boolean isCOMExtendedStatus() {
		return COMExtendedStatus;
	}


	/**
	 * @return the COMErrorHook flag
	 */
	protected boolean isCOMErrorHook() {
		return COMErrorHook;
	}


	/**
	 * @return the COMUseGetServiceId flag
	 */
	protected boolean isCOMUseGetServiceId() {
		return COMUseGetServiceId;
	}


	/**
	 * @return the COMUseParameterAccess flag
	 */
	protected boolean isCOMUseParameterAccess() {
		return COMUseParameterAccess;
	}


	/**
	 * @return the COMStartCOMExtension flag
	 */
	protected boolean isCOMStartCOMExtension() {
		return COMStartCOMExtension;
	}


	/**
	 * @return the comAppModes
	 */
	protected Collection<String> getComAppModes() {
		return comAppModes;
	}


	/**
	 * @return the modelElement2TargetElementMap
	 */
	protected Map<OSModelElement, TargetElement> getModelElement2TargetElementMap() {
		return modelElement2TargetElementMap;
	}


	/**
	 * @return Returns the resSchedulerResource.
	 */
	protected TargetResource getResSchedulerResource() {
		return resSchedulerResource;
	}

	/**
	 * @return Returns the resCOMResource.
	 */
	protected TargetResource getResCOMResource() {
		return resCOMHook;
	}	

	/**
	 * @return Returns the targetAccessedNonInternalResources.
	 */
	protected Collection<TargetResource> getTargetAccessedNonInternalResources() {
		return targetAccessedNonInternalResources;
	}


	/**
	 * @return Returns the targetBasicTasks.
	 */
	protected Collection<TargetTask> getTargetBasicTasks() {
		return targetBasicTasks;
	}


	/**
	 * @return Returns the targetExtendedTasks.
	 */
	protected Collection<TargetTask> getTargetExtendedTasks() {
		return targetExtendedTasks;
	}


	/**
	 * @return Returns the targetQueues.
	 */
	protected Collection<TargetQueue> getTargetQueues() {
		return targetQueues;
	}


	/**
	 * @return Returns the targetOptimizedQueues.
	 */
	protected Collection<TargetQueue> getTargetOptimizedQueues() {
		return targetOptimizedQueues;
	}


	/**
	 * @return Returns the targetStandardQueues.
	 */
	protected Collection<TargetQueue> getTargetStandardQueues() {
		return targetStandardQueues;
	}


	/**
	 * @return Returns the extTaskStackWords.
	 */
	protected int getExtTaskStackWords() {
		return extTaskStackWords;
	}


	/**
	 * @param extTaskStackWords The extTaskStackWords to set.
	 */
	protected void setExtTaskStackWords(int extTaskStackWords) {
		this.extTaskStackWords = extTaskStackWords;
	}


	/**
	 * @return Returns the errorHook.
	 */
	protected boolean isErrorHook() {
		return errorHook;
	}


	/**
	 * @return Returns the isExtendedStatus.
	 */
	protected boolean isExtendedStatus() {
		return isExtendedStatus;
	}


	/**
	 * @return Returns the postTaskHook.
	 */
	protected boolean isPostTaskHook() {
		return postTaskHook;
	}


	/**
	 * @return Returns the preTaskHook.
	 */
	protected boolean isPreTaskHook() {
		return preTaskHook;
	}


	/**
	 * @return Returns the queueSlots.
	 */
	protected int getQueueSlots() {
		return queueSlots;
	}


	/**
	 * @return Returns the shutdownHook.
	 */
	protected boolean isShutdownHook() {
		return shutdownHook;
	}


	/**
	 * @return Returns the startupHook.
	 */
	protected boolean isStartupHook() {
		return startupHook;
	}


	/**
	 * @return Returns the useGetServiceId.
	 */
	protected boolean isUseGetServiceId() {
		return useGetServiceId;
	}


	/**
	 * @return Returns the useParameterAccess.
	 */
	protected boolean isUseParameterAccess() {
		return useParameterAccess;
	}

	/**
	 * @return Returns the targetPriorities.
	 */
	protected TargetPriorities getTargetPriorities() {
		return targetPriorities;
	}


	/**
	 * @return Returns the targetISRs.
	 */
	protected Collection<TargetISR> getTargetISRs() {
		return targetISRs;
	}

	/**
	 * @return Returns the targetAppModes.
	 */
	protected Collection<TargetAppMode> getTargetAppModes() {
		return targetAppModes;
	}
	
	/**
	 * @return Returns the targetEvents.
	 */
	protected Collection<TargetEvent> getTargetEvents() {
		return targetEvents;
	}
	
	/**
	 * @return Returns the targetResources.
	 */
	protected Collection<TargetResource> getTargetResources() {
		return targetResources;
	}

	/**
	 * @return Returns the targetCounters.
	 */
	protected Collection<TargetCounter> getTargetCounters() {
		return targetCounters;
	}
	
	/**
	 * @return Returns the targetCounters that are not singleton counters.
	 */
	protected Collection<TargetCounter>	getTargetNonSingletonCounters() {
		
		Collection<TargetCounter> counters = new LinkedHashSet<TargetCounter>();
		
		for (TargetCounter next : targetCounters) {
			if ( !next.isSingleton() ) {
				counters.add(next);
			}
		}
		
		return counters;
	}
	
	/**
	 * Returns the default system counter. No system counter actually exists in the model
	 * and at the moment the tool relies on this being present as the first counter
	 * within OIL. This approach may be changed so that a "SystemCounter" is always
	 * added automatically as the first counter. note: this would be done in the constructor
	 * of this class in the same way that an idle task is added.
	 * 
	 * @return Returns the system counters (if any), else null.
	 */
	protected TargetCounter getTargetSystemCounter() {
		
		if ( !targetCounters.isEmpty() ) {
			return targetCounters.iterator().next();	// return first TargetCounter in the model.
		}
		
		return null;		// no TargetCounters in the model, so return null
	}	
	
	/**
	 * @return Returns the targetAlarms.
	 */
	protected Collection<TargetAlarm> getTargetAlarms() {
		return targetAlarms;
	}	
	
	/**
	 * @return Returns the targetAlarms that are not singleton alarms.
	 */
	protected Collection<TargetAlarm>	getTargetNonSingletonAlarms() {
		
		Collection<TargetAlarm> alarms = new LinkedHashSet<TargetAlarm>();
		
		for (TargetAlarm next : targetAlarms) {
			if ( !next.isSingleton() ) {
				alarms.add(next);
			}
		}
		
		return alarms;
	}	
	
	/**
	 * @return Returns the targetScheduleTables.
	 */
	protected Collection<TargetScheduleTable> getTargetScheduleTables() {
		return targetScheduleTables;
	}
	
	/**
	 * @return Returns the targetSendingMessages.
	 */
	protected Collection<TargetSendingMessage> getTargetSendingMessages() {
		return targetSendingMessages;
	}	
	
	/**
	 * @return Returns the targetReceivingMessages.
	 */
	protected Collection<TargetReceivingMessage> getTargetReceivingMessages() {
		return targetReceivingMessages;
	}		
	
	/**
	 * @return Returns the targetReceivingMessages that are queued
	 */
	protected Collection<TargetReceivingMessage> getTargetQueuedReceivingMessages() {
		Collection<TargetReceivingMessage> queuedMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( next.isQueuedMessage() ) {
				queuedMessages.add(next);
			}
		}
		return queuedMessages;
	}
	
	/**
	 * @return Returns the targetReceivingMessages that are un-queued and not zero length
	 */
	protected Collection<TargetReceivingMessage> getTargetUnqueuedReceivingMessages() {
		Collection<TargetReceivingMessage> unqueuedMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( next.isUnqueuedMessage() ) {
				unqueuedMessages.add(next);
			}
		}
		return unqueuedMessages;
	}	
	
	/**
	 * @return Returns the targetReceivingMessages that are zero length
	 */
	protected Collection<TargetReceivingMessage> getTargetZeroLengthReceivingMessages() {
		Collection<TargetReceivingMessage> zeroLengthMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( next.isZeroLengthMessage() ) {
				zeroLengthMessages.add(next);
			}
		}
		return zeroLengthMessages;
	}
	
	/**
	 * @return Returns the targetReceivingMessages that are stream messages
	 */
	protected Collection<TargetReceivingMessage> getTargetStreamReceivingMessages() {
		Collection<TargetReceivingMessage> streamMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( next.isStreamMessage() ) {
				streamMessages.add(next);
			}
		}
		return streamMessages;
	}	
	
	
	/**
	 * Returns the Receiving messages that are initialised 
	 * i.e. messages that are non-queued and non-zero length and that have an initial value of > 0.
	 * 
	 * @return Returns the initialised targetReceivingMessages
	 */
	protected Collection<TargetReceivingMessage> getTargetInitialisedReceivingMessages() {
		Collection<TargetReceivingMessage> initialisedMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( next.isInitialisedMessage() ) {
				initialisedMessages.add(next);
			}
		}
		return initialisedMessages;
	}	
	
	/**
	 * @return Returns the targetReceivingMessages that are non-zero length (i.e. queued, unqueue or stream receiving messages)
	 */
	protected Collection<TargetReceivingMessage> getTargetNonZeroLengthReceivingMessages() {
		Collection<TargetReceivingMessage> nonZeroLenMessages = new LinkedHashSet<TargetReceivingMessage>();
		
		for ( TargetReceivingMessage next : targetReceivingMessages ) {
			
			if ( !next.isZeroLengthMessage() ) {
				nonZeroLenMessages.add(next);
			}
		}
		return nonZeroLenMessages;
	}	
	
	/**
	 * 
	 * @return the map that maps from COM flag names to their unique index
	 */
	protected Map<String, Integer> getFlagNameMap() {
		return COMFlagNametoIndexMap;
		
	}
	
	
	/**
	 * @return Returns the target drivers owned by the driver manager.
	 */
	protected Collection<TargetDriver> getTargetDrivers() {
		return driverManager.getTargetDrivers();
	}
	
	/**
	 * @return Returns the target devices owned by the driver manager.
	 */
	protected Collection<TargetDevice> getTargetDevices() {
		return driverManager.getTargetDevices();
	}	
	
	/**
	 * Returns the set of all {@link TargetDevice} instances that are related to auto started/stopped {@link TargetCOMDriver} instances.
	 * 
	 * All the returned devices will be associated with at least one {@link TargetReceivingMessage} instance,
	 * i.e. they are COM devices.
	 * 
	 * @return set of all {@link TargetDevice} instances that are related to auto started/stopped COM drivers
	 */
	protected Collection<TargetDevice> getAutoStartedCOMDevices() {
		
		Collection<TargetDevice> autoStartedDevices = new HashSet<TargetDevice>();
		
		// Find COM type devices by iterating over TargetReceivingMessage instances 
		for (TargetReceivingMessage next : targetReceivingMessages) {
			
			TargetDevice device = next.getDevice();
			
			if ( device != null ) {

				assert device.getDriver() instanceof TargetCOMDriver;	// driver of COM device should always be a COM type driver
				
				// If driver supports auto start/stop then add device to the set
				if ( ((TargetCOMDriver)device.getDriver()).providesStartStopFunctions() ) {			
	
					autoStartedDevices.add(device);		// if device already in set then not added again!			
				}
			}
		}	
		
		return autoStartedDevices;
	}	
	
	
	/**
	 * @return Returns the targetTasks.
	 */
	protected Collection<TargetTask> getTargetTasks() {
		return targetTasks;
	}

	/**
	 * @return Returns the collection of all target tasks (except the idle task)
	 */
	protected Collection<TargetTask> getNonIdleTargetTasks() {
		
		// Construct a collection of all tasks (except the idle task)
		Collection<TargetTask> tasks = new LinkedHashSet<TargetTask>(targetTasks);
		
		if (idleTask !=null) {
			tasks.remove(idleTask);
		}
		
		return tasks;
	}	
	

	/**
	 * Returns the TargetElement that represents the given OSModelElement (if any)
	 * 
	 * @param modelElement the OSModelElement from which the TargetElement was created.
	 * @return the TargetElement, null if no such TargetElement created.
	 * @see #addTargetElementMapping(TargetElement)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected <E extends TargetElement> E getTargetElement(OSModelElement modelElement) {
		
		// Note the cast warning below has been suppressed in order to allow a single map
		// to be used to map from OSModelElements to TargetElements. To use full type
		// safety a separate map would need to be used for each different model element type
		return (E)modelElement2TargetElementMap.get(modelElement); 
	}

	/**
	 * Returns a collection of TargetElement instances that represent the given OSModelElement instances.
	 * 
	 * @param modelElements collection of OSModelElement instances from which the TargetElement were created.
	 * @return the collection of TargetElement instances, empty if no TargetElement instances were created.
	 */
	@Override
	protected <E extends TargetElement> Collection<E> getAllTargetElements(Collection<? extends OSModelElement> modelElements) {

		Collection<E> targetCollection = new LinkedHashSet<E>();
		
		for (OSModelElement next : modelElements) {
			
			E targetElement = getTargetElement(next);
			
			if (targetElement != null) {
				targetCollection.add(targetElement);
			}
		}
		
		return targetCollection;
	}

	
	/**
	 * Adds a target element to the modelElement2TargetElementMap.
	 * 
	 * @param targetElement the TargetElement that was created from a OSModelElement
	 * @see #getTargetElement(OSModelElement)
	 */
	protected void addTargetElementMapping(TargetElement targetElement) {
		
		assert targetElement.getOsModelElement() != null;	// given target not created from OSModelElement
		
		modelElement2TargetElementMap.put(targetElement.getOsModelElement(), targetElement);
	}
	
	
	/**
	 * Creates representative TargetElements for each OSModelElement that exists in the OS Model CPU.
	 * i.e. creates a shadow of all the elements that are of interest to the Target model generators.
	 *
	 * @param osModelCpu the OS Model CPU on which to base the created target elements.
	 */
	private void createTargetElements(Cpu osModelCpu) {
			
	
		// Create TargetTask instances
		for (Task next : osModelCpu.getTasks()) {
			targetTasks.add(new TargetTask(this, next));
		}		
		
		// Create TargetISR instances
		for (Isr next : osModelCpu.getIsrs()) {
			targetISRs.add(new TargetISR(this, next));
		}
		
		// Create TargetAlarm instances
		for (Alarm next : osModelCpu.getAlarms()) {
			targetAlarms.add(new TargetAlarm(this, next));
		}		
		
		// Create TargetScheduleTable instances and an internal TargetAlarm instance for each
		for (ScheduleTable next : osModelCpu.getScheduleTables()) {
			
			// Create a TargetAlarm that drives the schedule table			
			TargetAlarm internalAlarm = new TargetAlarm(this, "_alarm"+next.getName() );						
			internalAlarm.setHasHandle(false);	// no handle should be generated for this internal alarm
			
			// Create a new ScheduleTable, that uses the new TargetAlarm as its internal alarm
			TargetScheduleTable scheduleTable = new TargetScheduleTable(this, next, internalAlarm);

			targetScheduleTables.add(scheduleTable);
			targetAlarms.add(internalAlarm);
		}		
	
		// Create TargetSendingMessage instances
		for (Message next : osModelCpu.getSendingMessages()) {
			targetSendingMessages.add(new TargetSendingMessage(this, next));
			
			// Now create the TargetReceivingMessage instances targetted by each sending message
			// This is done in here to ensure the ordering of the TargetReceivingMessage instances is grouped by sender.
			for (Message nextReceiver : next.getReceivingMessages()) {
				targetReceivingMessages.add(new TargetReceivingMessage(this, nextReceiver));
			}			
		}	
		
		// Create TargetCounter instances
		for (Counter next : osModelCpu.getCounters()) {
			targetCounters.add(new TargetCounter(this, next));
		}	
		
		// Create TargetAppMode instances
		for (AppMode next : osModelCpu.getAppModes()) {
			targetAppModes.add(new TargetAppMode(this, next));
		}		
		
		// Create TargetEvent instances
		for (Event next : osModelCpu.getEvents()) {
			
			//if ( ((Event)next).getTasks().size() > 0 ) {
				targetEvents.add(new TargetEvent(this, next));
			//}
		}		
		
		// Create TargetResource instances
		for (Resource next : osModelCpu.getResources()) {
			targetResources.add(new TargetResource(this, next));
		}		
	}
	
	/**
	 * 
	 * @return a collection of all TargetElement instances owned by the TargetCpu
	 */
	public Collection<TargetElement> getAllTargetCpuElements() {
		
		Collection<TargetElement> allTargetElements = new LinkedHashSet<TargetElement>();
		
		allTargetElements.addAll(targetTasks);
		allTargetElements.addAll(targetISRs);
		allTargetElements.addAll(targetAlarms);
		allTargetElements.addAll(targetScheduleTables);
		allTargetElements.addAll(targetSendingMessages);
		allTargetElements.addAll(targetReceivingMessages);
		allTargetElements.addAll(targetCounters);
		allTargetElements.addAll(targetAppModes);
		allTargetElements.addAll(targetEvents);
		allTargetElements.addAll(targetResources);				

		return allTargetElements;
	}
	
	
	/**
	 * Sets up the internal associations to referenced elements.
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {

		super.initialiseModelAssociations();
		
		// Init all TargetTasks
		for (TargetTask next : targetTasks) {
			next.initialiseModelAssociations();
		}		
		
		// Init all TargetISRs
		for (TargetISR next : targetISRs) {
			next.initialiseModelAssociations();
		}
		
		// Init all TargetAlarms
		for (TargetAlarm next : targetAlarms) {
			next.initialiseModelAssociations();
		}

		// Init all TargetScheduleTables
		for (TargetScheduleTable next : targetScheduleTables) {
			next.initialiseModelAssociations();
		}
	
		// Init all TargetSendingMessages
		for (TargetSendingMessage next : targetSendingMessages) {
			next.initialiseModelAssociations();
		}		
		
		// Init all TargetReceivingMessages
		for (TargetReceivingMessage next : targetReceivingMessages) {
			next.initialiseModelAssociations();
		}		
		
		// Init all TargetCounters
		for (TargetCounter next : targetCounters) {
			next.initialiseModelAssociations();
		}			
		
		// Init all TargetAppModes
		for (TargetAppMode next : targetAppModes) {
			next.initialiseModelAssociations();
		}		
		
		// Init all TargetEvents
		for (TargetEvent next : targetEvents) {
			next.initialiseModelAssociations();
		}
		
		// Init all TargetResources
		for (TargetResource next : targetResources) {
			next.initialiseModelAssociations();
		}		
	}
	
	
	/**
	 * @return Returns the OS Model Cpu on which the TargetCpu is based (if any)
	 */
	public Cpu getCpu() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Cpu;
		
		return (Cpu)getOsModelElement();
	}		
	
	/**
	 * Helper that sets Control Block Index of each element on the given collection of TargetElement instances.
	 * 
	 * @param targetElements collection of TargetElement instances to have their CB indexes set.
	 * @param controlBlockName the name of the element's control block collection
	 */
	private static void setControlBlockIndexValues(Collection<? extends TargetElement> targetElements, String controlBlockName) {
		
		int index = 0;
		
		for (TargetElement next : targetElements) {
			
			next.setControlBlockDetails(index++, controlBlockName);
		}
	}
	
	
	/** 
	 * Does the pre-generation processing. This should be called prior to generation in order
	 * to setup internal data structures etc.
	 *
	 * @param platformInfo information about the platform to which the build applies.
	 */
	protected void preGenProcessing(PlatformInfo platformInfo) {
		
		assert extTaskStackWords == NOT_DEFINED;	// should not be called more than once
		assert queueSlots == NOT_DEFINED;
	
		setupStackData(platformInfo);
		
		/////////////////////////////////////////////////////////////////////////////////////
		// Create two task subset collections containing the basic and extended tasks
		for (TargetTask next : targetTasks) {
			
			if ( next.isBasic() ) {
				targetBasicTasks.add(next);
			}
			else {
				targetExtendedTasks.add(next);
			}
		}

		
		////////////////////////////////////////////////
		// Create the RES_SCHEDULER resource if required
		if (useResScheduler) {
			resSchedulerResource = new TargetResource(this, RES_SCHEDULER_NAME);
			resSchedulerResource.setHasHandle(false);	// RES_SCHEDULER defined by variable instance, so no handle required
			targetResources.add(resSchedulerResource);
		
			// Specify that all TargetTasks access the RES_SCHEDULER resource.
			for (TargetTask next : targetTasks) {
				next.addAccessedResource(resSchedulerResource);
				resSchedulerResource.addTargetRunnable(next);
			}
		}

		///////////////////////////////////////////////////////////////////////////////
		// Create a TargetResource for each TargetReceivingMessage instance that requires one
		// and attach to all TargetRunnable instances that can access the message.
		if ( isCOMPresent() ) {
			for (TargetReceivingMessage nextMessage : targetReceivingMessages ) {
				
				if ( nextMessage.requiresMessageResource() ) {
					// next message requires a resource, so create one
					
					TargetResource res = new TargetResource(this, nextMessage.getName()+"_res");
					res.setHasHandle(false);	// Only used internally by COM, so no handle required
					targetResources.add(res);
					nextMessage.setTargetResource(res);
					
					// Identify the set of runnables that may use the message, and associate these with the new resource
					
					Collection<TargetRunnable> accessors = nextMessage.getAllAccessors();
					
					accessors.add(idleTask);	// add Idle task in case no runnable declared as accessing the Message
					
					for (TargetRunnable runnable : accessors) {
						
						runnable.addAccessedResource(res);
						res.addTargetRunnable(runnable);
					}
				}
			}
			
			// Also setup the COM HOOK resource
			resCOMHook = new TargetResource(this, COM_HOOK_RES_NAME);
			resCOMHook.setHasHandle(false);	// COM hook defined by variable instance, so no handle required
			targetResources.add(resCOMHook);
		
			// Specify that all TargetTasks access the COM hook resource.
			for (TargetTask next : targetTasks) {
				next.addAccessedResource(resCOMHook);
				resCOMHook.addTargetRunnable(next);
			}

			// Specify that all TargetISRs access the COM hook resource.
			for (TargetISR next : targetISRs) {
				next.addAccessedResource(resCOMHook);
				resCOMHook.addTargetRunnable(next);
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////
		// Create a TargetResource for each TargetDevice instance that requires one
		// and attach to all TargetRunnable instances that can access the device.
		
		// These resources allow mutually exclusive access to non-sharable devices, e.g. an ethernet controller
		for ( TargetDevice device : getDriverManager().getTargetDevices() ) {
			
			if ( device.requiresDeviceResource() ) {
				// This device requires a resource, so create one
				
				TargetResource res = new TargetResource(this, device.getName()+"_res");
				res.setHasHandle(false);	// Only used internally (i.e. in generated C control blocks), so no handle required
				targetResources.add(res);
				device.setTargetResource(res);				
				
				// Need to trace all TargetRunnable instances that can access the device.
				
				// At the moment only TargetReceivingMessage instances are accessed by TargetRunnable instances,
				// since TargetCounter instances are not accessed via Runnables (also they never share devices).
				// If in future TargetDevice is used by other objects that are accessed by TargetRunnables, then these also need to be interrogated.
				
				Set<TargetRunnable> allAccessors = new HashSet<TargetRunnable>();
				
				// iterate over all TargetReceivingMessage instances that use the device, and get all the TargetRunnable instances that access the message.
				for (TargetReceivingMessage next : device.getMessages() ) {
					allAccessors.addAll(next.getAllAccessors());
				}
				
				allAccessors.add(idleTask);	// add Idle task in case no runnable declared as accessing the device
				
				for (TargetRunnable runnable : allAccessors) {
					
					runnable.addAccessedResource(res);
					res.addTargetRunnable(runnable);
				}				
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Calculate the stack size (in stack words) for all tasks within the targetTask collection
		
		extTaskStackWords = calculateExtTaskStackArraySize(targetExtendedTasks, platformInfo);
		
		//////////////////////////////////////////////////////////////////////////////////////////
		// Create two task subset collections containing the internal and non-internal
		// (i.e. standard and linked) resources. Only resources that are actually accessed are added.
		for (TargetResource next : targetResources) {
			
			if ( next.isAccessedDirectly() ) {
				if ( next.isInternal() ) {
					targetAccessedInternalResources.add(next);
				}
				else {
					targetAccessedNonInternalResources.add(next);
				}
			}
		}

	
		///////////////////////////////////////////////////////////////////////////////////////////
		// Add in all the priorities of Runnable instances (i.e. tasks and ISRs) to the target
		// priority handler.
		
		for (TargetTask next : targetTasks) {
			
			targetPriorities.addNewPriority(next.getModelPriority(),false);
		}		
		
		for (TargetISR next : targetISRs) {
			targetPriorities.addNewPriority(next.getModelPriority(),true);
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Compress all the priorities into a contiguous range and setup the target priorities.
		targetPriorities.createTargetPriorities();		
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Setup the target priorities within the TargetRunnable instances.
		for (TargetTask next : targetTasks) {
			next.setTargetPriority(targetPriorities.getTargetPriority(next.getModelPriority(), false));
		}		
		
		for (TargetISR next : targetISRs) {
			next.setTargetPriority(targetPriorities.getTargetPriority(next.getModelPriority(), true));
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Setup resource ceiling priorities
		for (TargetResource next : targetResources) {
			
			next.setCeiling();
		}	
	
		///////////////////////////////////////////////////////////////////////////////////////////
		// Setup task boost priorities		
		for (TargetTask next : targetTasks) {
			
			next.setBoostPriority();
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Setup the stack data values for the tasks and ISRs 
	
		// Call helper to setup extended task index values
		setupStackIndexes(targetExtendedTasks, platformInfo);		
		
		for (TargetTask next : targetBasicTasks) {
			
			next.setupStackData(platformInfo);
		}			
			
		for (TargetISR next : targetISRs) {
			
			next.setupStackData(platformInfo);
		}		
		
		////////////////////////////////////////////////////////////////////////////////////////////
		// setup internal target queue objects
		
		for ( int targetPriority = 1; targetPriority <= targetPriorities.getHighestTaskTargetPriority(); targetPriority++ ) {
			
			targetQueues.add(new TargetQueue(this, targetPriority));
		}
			
		// Create two queue subset collections containing the optimized and non-optimized queues
		for (TargetQueue next : targetQueues) {
			
			if (next.isOptimized()) {
				
				targetOptimizedQueues.add(next);
			}
			else {
				targetStandardQueues.add(next);
			}
		}
		
		// Setup the "first" value for each queue and the queueSlots which indicates
		// total amount of slots required for the queues.
		queueSlots = calculateQueueFirst(targetStandardQueues);
			
		///////////////////////////////////////////////////////////////////////////////////
		// Setup the Event masks
	
		setupEventMasks(platformInfo);
			
		/////////////////////////////////////////////////////////
		// Create a collection that maps from COM flag names to index values
		
		int flagIndex = 0;
		
		for ( TargetReceivingMessage nextReceiver : targetReceivingMessages ) {
			
			String nextFlagName = nextReceiver.getFlagName();
			
			if ( nextFlagName != null ) {
				// If flag name already exists then don't re-add, i.e. share index values with common flag names
				if ( !COMFlagNametoIndexMap.containsKey(nextFlagName) ) {
					COMFlagNametoIndexMap.put(nextFlagName, flagIndex++);
				}
			}
			
			// also add low threshold flag name, if one is avilable
			nextFlagName = nextReceiver.getLowFlagName();
			
			if ( nextFlagName != null ) {
				// If flag name already exists then don't re-add, i.e. share index values with common flag names
				if ( !COMFlagNametoIndexMap.containsKey(nextFlagName) ) {
					COMFlagNametoIndexMap.put(nextFlagName, flagIndex++);
				}
			}			
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// Setup the Control Block indexes of each element appears in a control block array
		setControlBlockIndexValues(targetBasicTasks, platformInfo.getBasicTaskCBName());
		setControlBlockIndexValues(targetExtendedTasks, platformInfo.getExtTaskCBName());
		setControlBlockIndexValues(targetISRs, platformInfo.getIsrCBName());
		setControlBlockIndexValues(targetAppModes, platformInfo.getAppModeCBName());
		setControlBlockIndexValues(targetAccessedNonInternalResources, platformInfo.getResourceCBName());		
		setControlBlockIndexValues(targetOptimizedQueues, platformInfo.getOptQueueCBName());
		setControlBlockIndexValues(targetStandardQueues, platformInfo.getStdQueueCBName());
		setControlBlockIndexValues(targetCounters, platformInfo.getCounterCBName());
		setControlBlockIndexValues(targetAlarms, platformInfo.getAlarmCBName());
		setControlBlockIndexValues(targetScheduleTables, platformInfo.getScheduleTableCBName());
		setControlBlockIndexValues(targetSendingMessages, platformInfo.getSendingMessageCBName());
		setControlBlockIndexValues(targetReceivingMessages, platformInfo.getReceivingMessageCBName());		
		setControlBlockIndexValues(driverManager.getTargetDrivers(), platformInfo.getDriverCBName());
		setControlBlockIndexValues(driverManager.getTargetDevices(), platformInfo.getDeviceCBName());
		
		// no need to setup TargetEvent (targetEvents) index values, since not placed within a control block array
	}
	
	/**
	 * Sets the the masks for the events (and associated tasks). This is a complex algorithm that
	 * auto-assigns mask values to each event, depending on how the mask is shared between
	 * different tasks.
	 * 
	 * @param platformInfo
	 */
	private void setupEventMasks(PlatformInfo platformInfo) {
		
		Collection<TargetEvent> targetAutoEvents = new HashSet<TargetEvent>();
		Collection<TargetEvent> targetNonAutoEvents = new HashSet<TargetEvent>();
		
		for (TargetEvent nextEvent : targetEvents) {
			
			if (nextEvent.isAutoMask() == false) {
				targetNonAutoEvents.add(nextEvent);
			}
			else {
				targetAutoEvents.add(nextEvent);
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////
		// 1. Setup the targetMask values of all events that have a known (non-auto) mask
		
		for (TargetEvent nextEvent : targetNonAutoEvents) {
			
			BigInteger mask = nextEvent.getModelMask();
			
			nextEvent.setTargetMask(mask);
			
			for (TargetTask nextTask : nextEvent.getTargetTasks()) {
				nextTask.addToEventsMask(mask);
			}
		}

		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2. For each event, create a set of events that do not have tasks in common, and therefore can share same mask bit (friends)
		// 3. For each event, create a set of events that do have tasks in common, and therefore can not share same mask bit (avoids)
		
		for (TargetEvent nextEvent : targetAutoEvents) {
						
			for (TargetEvent innerEvent : targetAutoEvents) {
			
				if (nextEvent != innerEvent) {
					// get set of tasks accessed by each other event
					Set<TargetTask> tasks = new HashSet<TargetTask>(nextEvent.getTargetTasks());
					
					// find the tasks that are accessed by both innerEvent and nextEvent
					tasks.retainAll(innerEvent.getTargetTasks());
					
					if (tasks.size() == 0) {
						nextEvent.addFriend(innerEvent);// innerEvent and nextEvent do not have any tasks in common.
					}
					else {
						nextEvent.addAvoid(innerEvent);	// innerEvent and nextEvent do have tasks in common.
					}
				}
			}
		}

		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4. For each event, create a set of events where none of the members need to avoid each other (clubs)

		for (TargetEvent nextEvent : targetAutoEvents) {
		
			for (TargetEvent proposedEvent : nextEvent.getFriends()) {
				
				Set<TargetEvent> avoids = new HashSet<TargetEvent>(proposedEvent.getAvoids());
				
				avoids.retainAll(nextEvent.getFriends());
				
				if (avoids.size() == 0) {
					nextEvent.addToClub(proposedEvent);	// proposedEvent is friend of all the other friends.
				}
			}
			
			nextEvent.addToClub(nextEvent);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////		
		// 5. For each club, allocate the next free bit available within all tasks accessed by all contained events
			
		TargetEvent nextEvent = findLargestEventClub(targetAutoEvents);

		while (nextEvent != null) {
	
			// Get collection of tasks that are accessed by all events within the club
			Collection<TargetTask> clubTasks = new HashSet<TargetTask>();
			
			for (TargetEvent clubEvent : nextEvent.getClub()) {
				clubTasks.addAll(clubEvent.getTargetTasks());
			}
			
			// Gets a mask (bit) that is not already used by any tasks accessed by the club events
			BigInteger mask = BigInteger.ZERO; 

			int nextBitPos = findNextFreeMaskBit(clubTasks);
						
			mask = mask.setBit(nextBitPos);
			
			// Mark the mask (bit) in each task accessed by the club events
			for (TargetTask next : clubTasks) {
				
				assert next.getEventsMask().testBit(nextBitPos) == false; // auto-bit should not already be allocated
				
				next.addToEventsMask(mask);
			}
			
			// Set the target mask (bit) for each event in the club
			for (TargetEvent clubEvent : nextEvent.getClub()) {
				clubEvent.setTargetMask(mask);
			}
			
			// Now the events have been processed eliminate from all other clubs
			for (TargetEvent nextClubEvent : targetAutoEvents) {
				
				if (nextClubEvent != nextEvent) {
					nextClubEvent.getClub().removeAll(nextEvent.getClub());
				}
			}
			
			// remove all events from this club since now processed
			nextEvent.getClub().clear();
			
			// get the next club to be processed
			nextEvent = findLargestEventClub(targetAutoEvents);
		}
	}
	
	/**
	 * Returns the TargetEvent that has the largest number of members in its club.
	 * 
	 * @param targetEvents
	 * @return the TargetEvent with the largest number of member, null if all events have no members.
	 */
	private TargetEvent findLargestEventClub(Collection<TargetEvent> targetEvents) {
		
		TargetEvent biggestClub = null;
		
		int biggestSize = 0;
		
		for (TargetEvent event : targetEvents) {
			
			if (event.getClub().size() > biggestSize) {
				biggestClub = event;
				biggestSize = event.getClub().size();
			}
		}
		return biggestClub;
	}
	
	
	/**
	 * Finds the bit position of the first free mask bit that is available in all the given tasks 
	 * @param clubTasks set of task in which to find first free mask bit
	 * @return the bit number of the first free bit (0 based)
	 */
	private int findNextFreeMaskBit(Collection<TargetTask> clubTasks) {
	
		BigInteger allMasks = BigInteger.ZERO;
		
		for (TargetTask nextTask : clubTasks) {
			allMasks = allMasks.or(nextTask.getEventsMask());
		}

		allMasks = allMasks.not();
		
		assert allMasks.getLowestSetBit() >= 0;	// can't have infinite number of bits set in the tasks
		
		return  allMasks.getLowestSetBit();
	}
	
	
	/**
	 * Sets up the "First" value for each targetQueue in the given collection
	 * 
	 * @param queues the collection of TargetQueue instances
	 * @return total number of slots required by all the queues.
	 */
	private int calculateQueueFirst(Collection<TargetQueue> queues) {

		int current = 0;
		
		for (TargetQueue next : queues) {
			
			next.setFirst(current);
			
			current += next.getQueueSize();
		}
		
		return current;
	}
	
	
	/**
	 * Calculate the total stack space required for all extended tasks within the given collection.
	 * 
	 * @param extTasks the collection of target tasks containing the extended tasks from which to calculate stack size
	 * @return the total stack array size (in stack words)
	 */
	private int calculateExtTaskStackArraySize(Collection<TargetTask> extTasks, PlatformInfo platformInfo) { 
	
		long allExtendedTasksStackBytes = 0;
		
		// Calculate the total amount of stack space required for extended tasks
		for (TargetTask next : extTasks) {

			assert next.isExtended();	// only extended tasks require stack index values
			
			if (next.isAutoStackSize()) {
				// Use default stack size recommended for the target if auto stacksize required.
				allExtendedTasksStackBytes += platformInfo.getDefaultTaskStackSize();
			}
			else {
				// Use stack size requested by the user if non-auto stacksize.
				allExtendedTasksStackBytes += next.getModelStackSize();
			}

			allExtendedTasksStackBytes += platformInfo.getStackHeadroom(isExtendedStatus);		
		}			
		
		return platformInfo.asStackAlignmentWord(allExtendedTasksStackBytes);
	}	
	
	
	/**
	 * Sets up the stack index values for the extended task.
	 * 
	 * @param platformInfo information about the platform for which the stack data is to be setup.
	 */
	protected void setupStackIndexes(Collection<TargetTask> extTasks, PlatformInfo platformInfo) {
			
		if (extTasks.size() > 0) {
			assert extTaskStackWords != NOT_DEFINED;	// must be setup prior to calling this method.
			
			int stackIndex;				// index in stackwords
	
			// decide on the initial index value depending on direction and offset nature of the target's stack
			if (platformInfo.isAscendingStack()) {
				
				if (platformInfo.isPostOffsetSP()) {
					stackIndex = 0;
				}
				else {
					stackIndex = -1;
				}
			}
			else {
				if (platformInfo.isPostOffsetSP()) {
					stackIndex = extTaskStackWords - 1;
				}
				else {
					stackIndex = extTaskStackWords;
				}
			}
			
			// Iterate over all tasks and setup initial SP and top of stack value (for extended tasks)
			
			for (TargetTask next : extTasks) {
	
				assert next.isExtended();	// only extended tasks require stack index values
	
				// setting up initial stack pointer index and top of stack index for extended task,
				// both index values are in stackwords.
				
				next.setInitialStackPointerIndex(stackIndex);
				
				long size;
				
				if (next.isAutoStackSize()) {
					// Use default stack size recommended for the target if auto stacksize required.
					size = platformInfo.getDefaultTaskStackSize();
				}
				else {
					// Use stack size requested by the user if non-auto stacksize.
					size = next.getModelStackSize();
				}				
				
				// Allocate space in the stack array for a given extended task. The space includes the amount requested by the task for application
				// data plus an amount to allow for ISR nesting on the stack (i.e. the ISR code that is not switched to execute on the kernel stack).
				// The stack checking (hardware or software) then attempt to enforce this usage for the task (or the ISR stubs executing on the stack).
				// An extra amount of stack is further allocated to allow the kernel to put extra data on the stack: once the kernel decides a task
				// switch is needed it disables stack checking and puts saved registers (and other parts of the context) on to the stack. This extra
				// "padding" cannot be used by the application (the stack checking detects/enforces this).
				if (platformInfo.isAscendingStack()) {
					// The space requested for the application to use.
					stackIndex += platformInfo.asStackAlignmentWord(size);
					
					// Set the top of stack used for stack checking (hardware or software).
					next.setTopOfStackIndex(stackIndex + platformInfo.asStackAlignmentWord(platformInfo.getTopOfStackCheckOffset()));

					// The extra space that ISRs could use on the stack before the kernel got a chance to switch them to running on the kernel stack.
					stackIndex += platformInfo.asStackAlignmentWord(platformInfo.getStackHeadroom(isExtendedStatus));
				}
				else {
					// Same as above, but stack goes in the opposite direction, depending on how the CPU operates.
					stackIndex -= platformInfo.asStackAlignmentWord(size);
					
					next.setTopOfStackIndex(stackIndex - platformInfo.asStackAlignmentWord(platformInfo.getTopOfStackCheckOffset()));

					stackIndex -= platformInfo.asStackAlignmentWord(platformInfo.getStackHeadroom(isExtendedStatus));
				}				 
			}
		}
	}	
	
	/**
	 * Maps from the given target priority to the TargetQueue that has that priority.
	 * 
	 * @param priority the target priority of the queue required
	 * @return the TargetQueue that has the given target priority
	 */
	protected TargetQueue getQueueWithPriority(int priority) {
		assert priority > 0;	// Only idle task has priority 0, and there are no queues for this
		
		for (TargetQueue next : targetQueues) {
			
			if (next.getTargetPriority() == priority) {
				return next;
			}
		}
		return null;
	}	
	
	/**
	 * Sets up the pre/post task hook stack values for the TargetCPU
	 * 
	 * @param platformInfo information about the platform for which the stack data is to be setup.
	 */
	protected void setupStackData(PlatformInfo platformInfo) {
			
		// Calculate stack offset value in bytes
		
		// The stack offsets are always positive, the kernel applies the offset in a target specific manner.
		
		if ( isAutoPreTaskHookStackSize ) {
			preTaskHookStackOffset = platformInfo.getDefaultPreTaskHookStackSize();
		}
		else {
			preTaskHookStackOffset = modelPreTaskHookStackSize;
		}
		preTaskHookStackOffset += platformInfo.getKernelTaskHookEntryUsage();
		preTaskHookStackOffset += platformInfo.getTopOfStackCheckOffset();

		if ( isAutoPostTaskHookStackSize ) {
			postTaskHookStackOffset = platformInfo.getDefaultPostTaskHookStackSize();
		}
		else {
			postTaskHookStackOffset = modelPostTaskHookStackSize;
		}		
		postTaskHookStackOffset += platformInfo.getKernelTaskHookEntryUsage();
		postTaskHookStackOffset += platformInfo.getTopOfStackCheckOffset();
	}	

	/**
	 * 
	 * @return the {@link TasksetType} value that determines the type of tasks that exist in the TargetCpu (i.e. just basic, just extended, or a mix of both)
	 */
	protected TasksetType getTasksetType() {
		
		if ( getTargetExtendedTasks().size() > 0 || getTargetBasicTasks().size() > 1 ) {
			// some user tasks exist, so decide whether they are just extended, basic or a mix of both
			if ( getTargetBasicTasks().size() <=1 ) {
				return TasksetType.EXTENDED_ONLY;
			}
			
			if ( getTargetExtendedTasks().size() == 0 ) {
				return TasksetType.BASIC_ONLY;
			}
			
			return TasksetType.MIXED_TASKS;
		}
		else {
			return TasksetType.NO_TASKS;	// no user tasks exist
		}
	}
	
	
	/**
	 * @return the driverManager
	 */
	public DriverManager getDriverManager() {
		return driverManager;
	}	

	
	/**
	 * Constructor that creates a TargetCpu that represents a OSModelElement.
	 * 
	 * All other contained target elements are also created, by examining the OS Model.
	 * 
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetCpu(Cpu osModelElement) {
		
		super(null, osModelElement);
		
		assert osModelElement !=null;		// a valid osModelElement should always be provided
		
		// Do this up here rather than in base class, since can't pass "this" to super().
		addTargetElementMapping(this);
		
		// Gets flag directly from the OS Model OS
		isExtendedStatus = osModelElement.getOs().isExtendedStatus();
		errorHook = osModelElement.getOs().getErrorHook();
		preTaskHook = osModelElement.getOs().getPreTaskHook();
		postTaskHook = osModelElement.getOs().getPostTaskHook();
		startupHook = osModelElement.getOs().getStartupHook();
		shutdownHook = osModelElement.getOs().getShutdownHook();
		useGetServiceId = osModelElement.getOs().getUseGetServiceId();
		useParameterAccess = osModelElement.getOs().getUseParameterAccess();
		useResScheduler = osModelElement.getOs().getUseResScheduler();
		isRestartable =  osModelElement.getOs().isRestartable();
		isStackCheckingEnabled = osModelElement.getOs().isStackCheckingEnabled();
		modelPreTaskHookStackSize =  osModelElement.getOs().getPreTaskHookStackSize();
		modelPostTaskHookStackSize =  osModelElement.getOs().getPostTaskHookStackSize();
		isAutoPreTaskHookStackSize = osModelElement.getOs().isAutoPreTaskHookStackSize();
		isAutoPostTaskHookStackSize = osModelElement.getOs().isAutoPostTaskHookStackSize();
		oscFailureHandled = osModelElement.getOs().isOscFailureHandled();
		addrErrorHandled = osModelElement.getOs().isAddrErrorHandled();
		mathErrorHandled = osModelElement.getOs().isMathErrorHandled();
		
		// Init COM related flags
		Com com = osModelElement.getCom();
		
		if ( com !=null )	{
			COMPresent = true;
			COMExtendedStatus = com.isExtendedStatus();
			comAppModes = com.getAppModes();
			COMErrorHook = com.getErrorHook();
			COMUseGetServiceId = com.getUseGetServiceId();
			COMUseParameterAccess = com.getUseParameterAccess();
			COMStartCOMExtension = com.getStartComExtension();		
		}
		else {
			COMPresent = false;
		}
		
		//////////////////////////////////////////////////
		// Create and add an Idle Task to the target tasks
		// This is created as the first target task so that it is always the first target task
		// in the basic task array (The embedded OS assumes this).
		
		idleTask = new TargetTask(this);
		targetTasks.add(idleTask);
		
		///////////////////////////////////////////////////
		// note: could create a default SystemCounter here
		
		////////////////////////////////////////////////////////////
		// Create and add a default app mode to the target app modes
		// This always exists as an app mode within the target model
		// This is always created as the first AppMode since the OS assumes this. 
		defaultAppMode = new TargetAppMode(this,OS_DEFAULT_APP_MODE);
		defaultAppMode.setHasHandle(false);	// OSDEFAULTAPPMODE defined by embedded code, so no handle required
		assert(targetAppModes.isEmpty());	// OSDEFAULTAPPMODE should be first element in the targetAppModes set
		targetAppModes.add(defaultAppMode);
	
		// Create required target elements, extracting information from the OS Model CPU
		createTargetElements(osModelElement);
		
		// Setup the internal associations of the created target elements.
		initialiseModelAssociations();
			
		// Associate any autostarted tasks that are not associated with an app mode to the default app mode.
		for (TargetTask next : targetTasks) {
			
			Task modelTask = next.getTask();
			
			if (modelTask != null) {
				
				if (modelTask.getAutostart() && next.getTargetAppModes().size() == 0) {
					// Task is autostarted that is not associated with any specific app mode,
					// so associate with the default appmode.
					defaultAppMode.addTargetTask(next);
					next.addTargetAppMode(defaultAppMode);
				}
			}
		}
		
		// Associate any autostarted alarms that are not associated with an app mode to the default app mode.
		for (TargetAlarm next : targetAlarms) {
			
			Alarm modelAlarm = next.getAlarm();
			
			if (modelAlarm != null) {
				
				if (modelAlarm.getAutostart() && next.getTargetAppModes().size() == 0) {
					// Alarm is autostarted that is not associated with any specific app mode,
					// so associate with the default appmode.
					defaultAppMode.addTargetAlarm(next);
					next.addTargetAppMode(defaultAppMode);
				}
			}
		}
		
		// Associate the internal alarms of any autostarted scheduletables that are not associated with an app mode to the default app mode.
		// It is the internal alarms that are associated since starting these is what actually starts autostarted schedule tables, since
		// schedule tables are driven by their internal alarms.
		for (TargetScheduleTable next : targetScheduleTables) {
			
			ScheduleTable modelScheduleTable = next.getScheduleTable();
			
			if (modelScheduleTable != null) {
				
				if (modelScheduleTable.getAutostart() && next.getTargetAppModes().size() == 0) {
					// ScheduleTable is autostarted that is not associated with any specific app mode,
					// so associate the internal alarm with the default appmode.
					TargetAlarm internalAlarm = next.getInternalAlarm();
					
					assert internalAlarm != null;	// all TargetScheduleTable should have an internal alarm
					
					defaultAppMode.addTargetAlarm(internalAlarm);
					defaultAppMode.addTargetScheduleTable(next);
					internalAlarm.addTargetAppMode(defaultAppMode);
					next.addTargetAppMode(defaultAppMode);
				}
			}
		}	
		
	}


	
}
