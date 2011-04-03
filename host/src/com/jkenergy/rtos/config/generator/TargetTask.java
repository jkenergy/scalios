package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetTask.java $
 * 
 */

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;

import com.jkenergy.rtos.config.osmodel.Task;

/**
 * Intermediate target element used to store information on tasks to be generated.
 * 
 * @author Mark Dixon
 *
 */

class TargetTask extends TargetRunnable {
	
	/**
	 * The mask of all events that the task may wait for.
	 */
	private BigInteger eventsMask = BigInteger.ZERO;
	
	/**
	 * Constant that specifies the name of the idle task
	 */
	private final static String IDLE_TASK_NAME = "IDLE_TASK";
	
	/**
	 * Constant that specifies the model priority of the idle task
	 */	
	public final static long IDLE_TASK_MODEL_PRIORITY = -1;
	
	/**
	 * Constant that specifies the activation count of the idle task
	 */	
	private final static int IDLE_TASK_ACT_COUNT = 1;

	/**
	 * Flag the indicates whether this represents the Idle Task.
	 */
	private boolean isIdle = false;
	
	/**
	 * Flag indicating whether target task is non pre-emptable
	 */ 
	private boolean isNonPreemptable;
	
	/**
	 * Flag indicating whether target task is extended
	 */
	private boolean isExtended;
	
	/**
	 * Maximum activation count requested for the target task 
	 */
	private long modelActivation=1;
	

	/**
	 * The initial index (in stackwords) into the stack space for the target task (extended task only)
	 * could be -1 for a pre-increment (ascending) stack
	 */
	private int initialStackPointerIndex;
	
	/**
	 * The index (in stackwords) into the stack space for the top of the stack (extended task only).
	 */	
	private int topOfStackIndex;
	

	/**
	 * The boost target priority of the task.
	 */
	private int boostPriority = TargetPriorities.INVALID_PRIORITY;
	
	
	/**
	 * Set of TargetAppMode instances in which this task is auto-started
	 */
	private Collection<TargetAppMode> targetAppModes = new HashSet<TargetAppMode>();	
	
	/**
	 * Set of TargetEvent instances that may set events for this task
	 */
	private Collection<TargetEvent> targetEvents = new HashSet<TargetEvent>();	
	
	
	/**
	 * @return Returns the eventsMask.
	 */
	protected BigInteger getEventsMask() {
		return eventsMask;
	}

	/**
	 * Combines the given mask with the task's event mask.
	 * 
	 * @param mask the mask to be combined with the events mask of the task.
	 */
	protected void addToEventsMask(BigInteger mask) {
		
		eventsMask = eventsMask.or(mask);
	}
	
	/**
	 * @return Returns the targetAppModes.
	 */
	protected Collection<TargetAppMode> getTargetAppModes() {
		return targetAppModes;
	}

	/**
	 * Associates the given TargetAppMode with the task.
	 * @param appMode the TargetAppMode to be associated with the task
	 */
	protected void addTargetAppMode(TargetAppMode appMode) {
		targetAppModes.add(appMode);
	}
		
	/**
	 * @return Returns the targetEvents.
	 */
	protected Collection<TargetEvent> getTargetEvents() {
		return targetEvents;
	}
	
	/**
	 * Associates the given TargetEvent with the task.
	 * @param event the TargetEvent to be associated with the task
	 */
	protected void addTargetEvent(TargetEvent event) {
		targetEvents.add(event);
	}	
	
	/**
	 * @return Returns the boostPriority.
	 */
	protected int getBoostPriority() {
		
		assert boostPriority != TargetPriorities.INVALID_PRIORITY; // can't be called until setBoostPriority() called
		
		return boostPriority;
	}

	/**
	 * Sets the boost priority of the target task. 
	 */
	protected void setBoostPriority() {
		
		assert boostPriority == TargetPriorities.INVALID_PRIORITY;	//shouldn't be called more than once

		if (isNonPreemptable) {
			// If the task is non pre-emptable then set boost priority to the highest task priority,
			// this has the effect of making sure no tasks can pre-empt this task when running.
			boostPriority = getTargetCpu().getTargetPriorities().getHighestTaskTargetPriority();
		}
		else {
			// Pre-emptable task, so calculate the boost priority as the highest ceiling of all accessed internal resources
			
			// initial boost priority to the existing (base) target priority.
			boostPriority = getTargetPriority();
			
			for (TargetResource resource : getTargetResources()) {
				
				if (resource.isInternal()) {
					
					if (resource.getCeiling() > boostPriority) {
						boostPriority = resource.getCeiling();
					}
				}
			}
		}
	}

	/**
	 * @return Returns the isIdle.
	 */
	protected boolean isIdle() {
		return isIdle;
	}

	/**
	 * @return Returns true if the target task is an extended task.
	 */
	protected boolean isExtended() {
		return isExtended;
	}

	/**
	 * @return Returns true if the target task is a basic task.
	 */
	protected boolean isBasic() {
		return !isExtended;
	}	
	
	
	/**
	 * @return Returns the isPreemptable.
	 */
	protected boolean isNonPreemptable() {
		return isNonPreemptable;
	}

	/**
	 * @param isNonPreemptable The isNonPreemptable to set.
	 */
	protected void setPreemptable(boolean isNonPreemptable) {
		this.isNonPreemptable = isNonPreemptable;
	}

	/**
	 * @return Returns the modelActivation.
	 */
	protected long getModelActivation() {
		return modelActivation;
	}

	/**
	 * @param modelActivation The modelActivation to set.
	 */
	protected void setModelActivation(long modelActivation) {
		this.modelActivation = modelActivation;
	}

	/**
	 * @return Returns true if autostarted.
	 */
	protected boolean isAutostarted() {
		return (targetAppModes.size() > 0);
	}


	/**
	 * @return Returns the stackPointerIndex.
	 */
	public int getInitialStackPointerIndex() {
		return initialStackPointerIndex;
	}

	/**
	 * @param stackPointerIndex The stackPointerIndex to set.
	 */
	public void setInitialStackPointerIndex(int stackPointerIndex) {
		this.initialStackPointerIndex = stackPointerIndex;
	}

	/**
	 * @return Returns the topOfStackIndex.
	 */
	public int getTopOfStackIndex() {
		return topOfStackIndex;
	}

	/**
	 * @param topOfStackIndex The topOfStackIndex to set.
	 */
	public void setTopOfStackIndex(int topOfStackIndex) {
		this.topOfStackIndex = topOfStackIndex;
	}

	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		Task task = getTask();
		
		if (task != null) {
			
			targetAppModes = getAllTargetElements(task.getAppModes());
		}		
	}

	/**
	 * 
	 * @return true if the target task is activated by an alarm or schedule table
	 */
	public boolean isActivated() {
		Task task = getTask();
		
		if ( task !=null ) {
			return task.isActivated();
		}
		
		return false;
	}
	
	/**
	 * @return Returns the OS Model Task on which the TargetTask is based (if any)
	 */
	public Task getTask() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Task;
		
		return (Task)getOsModelElement();
	}
	
	/**
	 * Sets up the stack values for the basic task.
	 * 
	 * @param platformInfo information about the platform for which the stack data is to be setup.
	 */
	protected void setupStackData(PlatformInfo platformInfo) {
			
		assert isBasic();	// only basic tasks should setup stack offset.
		
		// Calculate stack offset value in bytes
		
		// The stack offset is always positive, the kernel applies the offset in a target specific manner
		long stackOffset;
		
		if (isAutoStackSize()) {
			// Use default Task stack size recommended for the target if auto stacksize required.
			stackOffset = platformInfo.getDefaultTaskStackSize();
		}
		else {
			// Use stack size requested by the user if non-auto stacksize.
			stackOffset = getModelStackSize();
		}
		
		stackOffset += platformInfo.getKernelPreTaskEntryUsage();
		stackOffset += platformInfo.getTopOfStackCheckOffset();
			
		setStackOffset(stackOffset);
	}
	
	
	
	/**
	 * Create a Target task to represent the idle task
	 * @param cpu the TargetCpu that owns the element
	 */
	protected TargetTask(TargetCpu cpu) {
		super(cpu, IDLE_TASK_NAME);
		
		setModelPriority(IDLE_TASK_MODEL_PRIORITY);
			
		modelActivation = IDLE_TASK_ACT_COUNT;
		
		isIdle = true;
	}
	
	/**
	 * Standard Constructor that creates a TargetTask that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetTask(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy constructor that creates a TargetTask that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetTask(TargetCpu cpu, Task osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		isNonPreemptable = osModelElement.isNonPreemptable();
		isExtended = osModelElement.isExtendedTask();
		modelActivation = osModelElement.getActivation();
		

	}
}
