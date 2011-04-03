package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 00:09:36 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 589 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetQueue.java $
 * 
 */

import java.util.Collection;
import java.util.HashSet;


/**
 * Intermediate target element used to store information on task queues.
 * 
 * @author Mark Dixon
 *
 */

class TargetQueue extends TargetElement {

	private final static int NO_QUEUE = -1;
	
	/**
	 * Flag indicating whether the queue is an optimized queue, i.e. contains no dynamic element (just one task).
	 */
	private boolean isOptimized;

	/**
	 * The target priority of tasks contained by the queue.
	 */
	private int targetPriority;
	
	/**
	 * Total space required for this queue within the queue storage space (in number of slots, not bytes)
	 * Only used by non-optimized queues.
	 */
	private int queueSize = 0;		
	
	
	/**
	 * The first position in the slots used by the queue within the total queue slot array.
	 */
	private int first;
	
	/**
	 * The tasks that are stored within this queue.
	 */
	private Collection<TargetTask> targetTasks = new HashSet<TargetTask>();
	

	/**
	 * @return Returns the first.
	 */
	protected int getFirst() {
		return first;
	}


	/**
	 * @param first The first to set.
	 */
	protected void setFirst(int first) {
		this.first = first;
	}

	/**
	 * @return Returns the last position in the slots used by the queue within the total queue slot array.
	 */
	protected int getLast() {
		return first + (queueSize - 1);
	}
	
	
	/**
	 * @return Returns the isOptimized.
	 */
	protected boolean isOptimized() {
		return isOptimized;
	}


	/**
	 * @return Returns the targetTasks.
	 */
	protected Collection<TargetTask> getTargetTasks() {
		return targetTasks;
	}


	/**
	 * @return Returns the targetPriority.
	 */
	protected int getTargetPriority() {
		return targetPriority;
	}
	

	/**
	 * @return Returns the queueSize, NO_QUEUE if the queue is optimized.
	 */
	protected int getQueueSize() {
		
		if (isOptimized) {
			return NO_QUEUE;
		}
		else {
			return queueSize;
		}
	}


	/**
	 * Constructor to create a queue for a given target priority.
	 * 
	 * @param targetCPU the TargetCpu that owns the element
	 * @param targetPriority the priority of the tasks for which the queue is being created.
	 */
	protected TargetQueue(TargetCpu targetCPU, int targetPriority) {
		
		super(targetCPU);

		this.targetPriority = targetPriority;			
		
		for (TargetTask next : targetCPU.getTargetTasks()) {
			
			if (next.getTargetPriority() == targetPriority) {
				targetTasks.add(next);
				
				queueSize += next.getModelActivation();	// accumulate total size of queue.
			}
		}

		// decide whether the queue is optimised, i.e. has only one task at the given priority.
		isOptimized = (targetTasks.size() == 1);
	}
		
}
