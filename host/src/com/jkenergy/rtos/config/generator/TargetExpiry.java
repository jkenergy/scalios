package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetExpiry.java $
 * 
 */

/**
 * Intermediate target element used to store information alarm expiry actions & COM notification actions.
 * 
 * @author Mark Dixon
 *
 */

public class TargetExpiry {

	/**
	 * The next index position to be assigned to the next created instance
	 */	
	private static int nextIndex = 0;
	
	/**
	 * The unique index position of the TargetExpiry instance
	 * @see #nextIndex
	 */
	private int uniqueIndex = 0;	
	
	/**
	 * The TargetCounter that is incremented on expiry
	 */
	private TargetCounter incrementedCounter = null;
	
	/**
	 * The TargetEvent that is set on expiry
	 */	
	private TargetEvent setEvent = null;
	
	/**
	 * The TargetTask that is activated on expiry
	 */		
	private TargetTask activatedTask = null;
	
	/**
	 * The name of the callback handler to be called on expiry
	 */		
	private String callbackName = null;
	
	/**
	 * The TargetScheduleTable that is to be driven on expiry
	 * This action is used internally only, as a mechanism for allowing
	 * alarms to drive schedule tables. This expiry action is never
	 * available to user configurations of the OS.
	 */
	private TargetScheduleTable drivenTable;
	
	
	/**
	 * @return the unique index of the TargetExpiry
	 */
	public int getUniqueIndex() {
		return uniqueIndex;
	}	
	
	/**
	 * 
	 * @return true if increments a Counter on expiry
	 */	
	boolean incrementsCounter() {
		return (incrementedCounter != null);
	}

	/**
	 * 
	 * @return true if handler called on expiry
	 */	
	boolean callsHandler()  {
		return (callbackName != null);
	}

	/**
	 * 
	 * @return true if event set on expiry
	 */		
	boolean setsEvent()  {
		return (activatedTask != null && setEvent != null);
	}

	/**
	 * 
	 * @return true if task activated called on expiry
	 */		
	boolean activatesTask()  {
		return (activatedTask != null && setEvent == null);
	}
	
	/**
	 * @return the incrementedCounter
	 */
	public TargetCounter getIncrementedCounter() {
		return incrementedCounter;
	}

	/**
	 * @return the setEvent
	 */
	public TargetEvent getSetEvent() {
		return setEvent;
	}

	/**
	 * @return the activatedTask
	 */
	public TargetTask getActivatedTask() {
		return activatedTask;
	}

	/**
	 * @return the callbackName
	 */
	public String getCallbackName() {
		return callbackName;
	}

	/**
	 * @return the drivenTable
	 */
	public TargetScheduleTable getDrivenTable() {
		return drivenTable;
	}

	/**
	 * 
	 * @return true if schedule table driven on expiry
	 */		
	boolean drivesScheduleTable()  {
		return (drivenTable != null);
	}	

	
	/**
	 * This version of the constructor creates a TargetExpiry that increments a counter
	 * @param incrementedCounter the counter to be inremented on expiry
	 */
	TargetExpiry(TargetCounter incrementedCounter) {
		this.incrementedCounter = incrementedCounter;
		uniqueIndex = nextIndex++;
	}
	
	/**
	 * This version of the constructor creates a TargetExpiry that sets an event on expiry
	 * @param setEvent the event that is set
	 * @param activatedTask the task that is activated
	 */
	TargetExpiry(TargetEvent setEvent, TargetTask activatedTask) {
		this.setEvent = setEvent;
		this.activatedTask = activatedTask;
		uniqueIndex = nextIndex++;
	}	
	
	/**
	 * This version of the constructor creates a TargetExpiry that activates a task on expiry
	 * @param activatedTask the task that is activated
	 */
	TargetExpiry(TargetTask activatedTask) {
		this.activatedTask = activatedTask;
		uniqueIndex = nextIndex++;
	}		
	
	/**
	 * This version of the constructor creates a TargetExpiry that calls a callback function on expiry
	 * @param callbackName the name of the callback function called
	 */
	TargetExpiry(String callbackName) {
		this.callbackName = callbackName;
		uniqueIndex = nextIndex++;
	}	
	
	/**
	 * This version of the constructor creates a TargetExpiry that drived a schedule table on expiry
	 * @param drivenTable the TargetScheduleTable that is driven
	 */	
	TargetExpiry(TargetScheduleTable drivenTable) {
		this.drivenTable = drivenTable;
		uniqueIndex = nextIndex++;
	}
}
