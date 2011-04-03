package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-02-25 21:38:01 +0000 (Mon, 25 Feb 2008) $
 * $LastChangedRevision: 623 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetPriorities.java $
 * 
 */

import java.util.*;


/**
 * Class that handles information relating to OS Model priorities and their Target Model counterparts. 
 * 
 * @author Mark Dixon
 *
 */

public class TargetPriorities extends TargetElement {

	
	protected final static int INVALID_PRIORITY = -1;
	
	/**
	 * A flag that specifies that the target priorities have been assigned and are ready for use.
	 */
	private boolean areAssigned;
	
	// Maps that provide a relationship between model priorities and target priorities
	private Map<Long, Integer> taskModel2TargetMap = new TreeMap<Long, Integer>();	// (model priority -> target priority)
	private Map<Long, Integer> isrModel2TargetMap = new TreeMap<Long, Integer>();	// (model priority -> target priority)

	// Maps that provide a relationship between target priorities and model priorities
	private Map<Integer, Long> taskTarget2ModelMap = new HashMap<Integer, Long>();	// (target priority -> model priority)
	private Map<Integer, Long> isrTarget2ModelMap = new HashMap<Integer, Long>();	// (target priority -> model priority)
	
	/**
	 * The highest target priority assigned to a task.
	 */
	private int highestTaskTargetPriority = INVALID_PRIORITY;	
	
	/**
	 * The lowest target priority assigned to an ISR.
	 */
	private int lowestISRTargetPriority = INVALID_PRIORITY;

	/**
	 * The highest target priority assigned to an ISR.
	 */
	private int highestISRTargetPriority = INVALID_PRIORITY;
	
	
	/**
	 * Add a new model priority to the collection of priorities managed by this class.
	 * 
	 * NOTE: All calls to this method should be made prior to calling {@link #createTargetPriorities()}
	 *  
	 * @param modelPriority the model priority to be added.
	 * @param isISR flag indicating that the given model priority owned by an ISR.
	 */
	protected void addNewPriority(long modelPriority, boolean isISR) {
		
		assert areAssigned == false;		// should not be called after createTargetPriorities()
		
		// Add the priorities to the appropriate map. The maps are key sorted (and set based) so
		// the given model priority is added at the appropriate position.
		if ( isISR ) {
			isrModel2TargetMap.put(modelPriority, null);			
		}
		else {
			taskModel2TargetMap.put(modelPriority, null);
		}
	}
	
	/**
	 * Creates the target priorities.
	 * 
	 * NOTE: this method should be called after all calls to {@link #addNewPriority(long, boolean)}
	 * 		 once this method has been called all accessor methods may then be called.
	 */
	protected void createTargetPriorities() {
		
		assert areAssigned == false;		// already called?
		
		// Since the model priorities were added to a sorted map they are already compressed in the sense
		// that the map only contains model priorities of interest, i.e. no spaces exist in the map between
		// the priorities.
		
		// Target priorities are model priorities compressed to be contiguous integer values (starting at 0).
		// Since the maps are sorted on their key (model priority) then the target priorities can be assigned
		// by iterating over them in their stored order.
		
		// Create a number of maps that relate model priorities to target priorities.
		
		int targetPriority = 0;
					
		///////////////////////////////////////////////////////////////////////
		// map all task model priorities to target priorities
		for (Long next : taskModel2TargetMap.keySet()) {
			
			taskModel2TargetMap.put(next, targetPriority);

			taskTarget2ModelMap.put(targetPriority, next);
			
			targetPriority++;			
		}	
			
		// record the highest target priority assigned to a task
		highestTaskTargetPriority = targetPriority - 1;
		
		////////////////////////////////////////////////////////////////////////
		// map all ISR model priorities to target priorities (if any ISRs exist)
		if (isrModel2TargetMap.keySet().size() > 0) {
			// ISRs exist!
			
			// record the lowest target priority assigned to an ISR
			lowestISRTargetPriority = targetPriority;
			
			for (Long next : isrModel2TargetMap.keySet()) {
				isrModel2TargetMap.put(next, targetPriority);
				
				isrTarget2ModelMap.put(targetPriority, next);
				
				targetPriority++;			
			}
			
			// record the highest target priority assigned to an ISR
			highestISRTargetPriority = targetPriority - 1;
		}
		
		///////////////////////////////////////////////////////////
		
		areAssigned = true;
	}

	/**
	 * @return Returns the highestISRTargetPriority, INVALID_PRIORITY if no ISRs.
	 */
	protected int getHighestISRTargetPriority() {
		
		assert areAssigned == true;	// createTargetPriorities() not called?
		
		return highestISRTargetPriority;
	}

	/**
	 * @return Returns the lowestISRTargetPriority, INVALID_PRIORITY if no ISRs.
	 */
	protected int getLowestISRTargetPriority() {
		
		assert areAssigned == true;	// createTargetPriorities() not called?
		
		return lowestISRTargetPriority;
	}
	
	/**
	 * @return Returns the highest task Target Priority
	 */
	protected int getHighestTaskTargetPriority() {
		
		assert areAssigned == true;	// createTargetPriorities() not called?
		
		assert highestTaskTargetPriority >= 0;	// idle task should always be added
		
		return highestTaskTargetPriority;
	}
	
	/**
	 * Gets the highest Target Priority, which will be the highest ISR priority (if ISRs exist)
	 * else it will be the highest task priority.
	 * 
	 * @return Returns the highest Target Priority.
	 */
	protected int getHighestTargetPriority() {
		
		if (highestISRTargetPriority >= 0) {
			// ISRs exist, so return highest ISR target priority.
			return highestISRTargetPriority;
		}
		else {
			// ISRs do not exist, so return the highest task priority.
			assert highestTaskTargetPriority >= 0;	// idle task should always be added
			
			return highestTaskTargetPriority;
		}
	}
	
	/**
	 * Tests whether the given target priority represents an ISR.
	 * 
	 * NOTE: this method should only be called after {@link #createTargetPriorities()}
	 * 
	 * @param targetPriority the priority to be checked.
	 * @return true if the given target priority represents an ISR.
	 */
	protected boolean isISRTargetPriority(int targetPriority) {
		
		assert areAssigned == true;	// createTargetPriorities() not called?
		
		return (targetPriority > highestTaskTargetPriority);
	}

	/**
	 * Gets the model priority that is represented by the given target priority.
	 * 
	 * NOTE: this method should only be called after {@link #createTargetPriorities()} 
	 * 
	 * @param targetPriority the target priority to be converted
	 * @return the model priority
	 */
	protected long getModelPriority(int targetPriority) {

		assert areAssigned == true;	// createTargetPriorities() not called?
		
		Long modelPri;
		
		if (targetPriority > highestTaskTargetPriority) {
			modelPri = isrTarget2ModelMap.get(targetPriority);
		}
		else {
			modelPri = taskTarget2ModelMap.get(targetPriority);
		}
		
		return (modelPri != null) ? modelPri.longValue() : INVALID_PRIORITY ;
	}	
	

	/**
	 * Gets the target priority that represents the given model priority. 
	 * 
	 * NOTE: this method should only be called after {@link #createTargetPriorities()}
	 *  
	 * @param modelPriority the model priority to be converted
	 * @param isISR flag that specifies if priority required for an ISR
	 * @return the (compressed) target priority
	 */
	protected int getTargetPriority(long modelPriority, boolean isISR) {

		assert areAssigned == true;	// createTargetPriorities() not called?
		
		Integer targetPri;

		if ( isISR ) {
			targetPri = isrModel2TargetMap.get(modelPriority);
		}
		else {
			targetPri = taskModel2TargetMap.get(modelPriority);
		}
		
		return (targetPri != null) ? targetPri.intValue() : INVALID_PRIORITY;
	}
	
	/**
	 * 
	 * @return the number of unique Task priorities
	 */
	public int getUniqueNonIdleTaskPriorityCount() {
		assert areAssigned == true;	// createTargetPriorities() not called?
		assert taskTarget2ModelMap.size() > 0; // always contains at least the idle task
		assert taskTarget2ModelMap.get(0) == TargetTask.IDLE_TASK_MODEL_PRIORITY;	// the idle task is always in the map and is always 0 (see wiki1055)
		
		return taskTarget2ModelMap.size() - 1;
	}
	
	/**
	 * 
	 * @return the number of unique ISR priorities
	 */
	public int getUniqueISRPriorityCount() {
		
		assert areAssigned == true;	// createTargetPriorities() not called?
		
		return isrTarget2ModelMap.size();
	}	
	
	/**
	 * Gets the Runnable with highest target priority from the given collection. 
	 * @param runnables the collection of TargetRunnable instances.
	 * @return the TargetRunnable instance with the highest target priority (null if collection empty)
	 */
	protected static TargetRunnable getHighestTargetPriorityRunnable(Collection<? extends TargetRunnable> runnables) {
		
		TargetRunnable highestRunnable = null;
		
		for (TargetRunnable next : runnables) {
			
			if (highestRunnable != null) {
				if (next.getTargetPriority() > highestRunnable.getTargetPriority()) {
					highestRunnable = next;
				}
			}
			else {
				highestRunnable = next;
			}
		}
		
		return highestRunnable;
	}
	
	
	
	/**
	 *  
	 * @param targetCpu the TargetCpu that owns the element
	 */
	protected TargetPriorities(TargetCpu targetCpu) {
		super(targetCpu);
	}
}
