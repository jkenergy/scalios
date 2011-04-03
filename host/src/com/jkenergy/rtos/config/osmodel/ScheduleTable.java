package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ScheduleTable.java $
 * 
 */

import java.math.BigInteger;
import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models a Schedule Table within the OS.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 *  
 * @author Mark Dixon
 *
 *
 */
public class ScheduleTable extends OSModelElement {

	
	/**
	 * List of actions performed by the schedule table
	 */
	private List<ScheduleTableAction> actions = new ArrayList<ScheduleTableAction>();		/* $Req: AUTOSAR $ */
	
	/**
	 * The Counter to which the ScheduleTable is assigned
	 */
	private Counter counter=null;	
	
	/**
	 * Autostart flag
	 */
	private boolean autoStart=false;							/* $Req: AUTOSAR $ */
	
	/**
	 * Offset (relative from the start the operating system) to be used when ScheduleTable is autostarted
	 * Only valid when {@link #autoStart} is true   
	 * 
	 */
	private BigInteger autoStartOffset=BigInteger.ZERO;			/* $Req: AUTOSAR $ */	
	
	/**
	 * Set of AppModes in which the ScheduleTable is auto-started
	 * Only valid when {@link #autoStart} is true 
	 */
	private Set<AppMode> appModes = new LinkedHashSet<AppMode>();						/* $Req: AUTOSAR $ */
	
	/**
	 * Periodic flag
	 */
	private boolean periodic=false;								/* $Req: AUTOSAR $ */	
	
	/**
	 * Period length 
	 * 
	 */
	private BigInteger length=BigInteger.ZERO;					/* $Req: AUTOSAR $ */	
	
	
	/**
	 * localToGlobalTimeSynchronization flag
	 */
	private boolean localToGlobalTimeSync=false;				/* $Req: AUTOSAR $ */	
	
	/**
	 * Synchronization Strategy
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * @see SyncStrategyKind								
	 */ 
	private SyncStrategyKind syncStrategy=SyncStrategyKind.HARD_LITERAL;	/* $Req: AUTOSAR $ */
	
	/**
	 * Max increase value
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * 
	 */
	private BigInteger maxIncrease=BigInteger.ZERO;				/* $Req: AUTOSAR $ */	
	
	/**
	 * Max decrease value
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * 
	 */
	private BigInteger maxDecrease=BigInteger.ZERO;				/* $Req: AUTOSAR $ */	
	
	/**
	 * Max increase async value
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * 
	 */
	private BigInteger maxIncreaseAsync=BigInteger.ZERO;		/* $Req: AUTOSAR $ */	
	
	/**
	 * Max decrease async value
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * 
	 */
	private BigInteger maxDecreaseAsync=BigInteger.ZERO;		/* $Req: AUTOSAR $ */	
	
	/**
	 * precision value
	 * Only valid when {@link #localToGlobalTimeSync} is true
	 * 
	 */
	private BigInteger precision=BigInteger.ZERO;				/* $Req: AUTOSAR $ */		
	
	
	
	/**
	 * Sets the Counter to which this ScheduleTable is assigned.
	 * This method creates a two way relationship. 
	 * @param newCounter the Counter to which the ScheduleTable is assigned.
	 */
	public void setCounter(Counter newCounter) {
		
		if ( newCounter!=null ) {
			
			if ( counter!=newCounter ) {
				
				counter=newCounter;
				
				counter.addScheduleTable(this);
			}
		}
	}

	/**
	 * Returns the Counter to which this ScheduleTable is assigned.
	 * @return Counter to which this ScheduleTable is assigned.
	 */
	public Counter getCounter() {
		return counter;
	}	
	
	
	/**
	 * Sets the autoStarted flag of the ScheduleTable
	 * @param newAutoStart the new autostart flag
	 */
	public void setAutostart(boolean newAutoStart) {
		autoStart=newAutoStart;
	}
	
	/**
	 * @return autoStart flag of the ScheduleTable
	 */
	public boolean getAutostart() {
		return autoStart;
	}	
	
	/**
	 * Sets the autostartOffset value of the ScheduleTable.
	 * @param newAutostartOffset the new autostartOffset value of the ScheduleTable
	 * 
	 */
	public void setAutostartOffset(BigInteger newAutostartOffset)  {
			
			
		autoStartOffset=newAutostartOffset;
	}
	
	/**
	 * @return the autostartOffset of the ScheduleTable.
	 */
	public BigInteger getAutostartOffset() {
		return autoStartOffset;
	}	
	
	/**
	 * Adds an AppMode to the collection of AppModes in which the ScheduleTable is started
	 * This method creates a two way relationship.
	 * 
	 * @param appMode the AppMode to be added
	 */
	public void addAppMode(AppMode appMode) {
		
		if ( appMode!=null ) {
			if ( appModes.add(appMode) )
				appMode.addScheduleTable(this);	// inform the appMode that this ScheduleTable is started
		}
	}	
	
	/**
	 * Returns the Collection of AppModes in which the ScheduleTable is auto-started.
	 * 
	 * @return Collection of AppModes in which the ScheduleTable is auto-started
	 */
	public Collection<AppMode> getAppModes() {
		return appModes;
	}
	
	/**
	 * @return the periodic flag
	 */
	public boolean isPeriodic() {
		return periodic;
	}

	/**
	 * @param periodic the periodic flag to set
	 */
	public void setPeriodic(boolean periodic) {
		this.periodic = periodic;
	}	

	
	/**
	 * Sets the period length value of the ScheduleTable.
	 * @param newLength the new period length value of the ScheduleTable
	 * 
	 */
	public void setLength(BigInteger newLength)  {
			
		length=newLength;
	}
	
	/**
	 * @return the period length of the ScheduleTable.
	 */
	public BigInteger getLength() {
		return length;
	}	
	
	/**
	 * @return the localToGlobalTimeSync flag
	 */
	public boolean isLocalToGlobalTimeSync() {
		return localToGlobalTimeSync;
	}

	/**
	 * @param localToGlobalTimeSync the localToGlobalTimeSync to set
	 */
	public void setLocalToGlobalTimeSync(boolean localToGlobalTimeSync) {
		this.localToGlobalTimeSync = localToGlobalTimeSync;
	}	

	/**
	 * @return the syncStrategy
	 */
	public SyncStrategyKind getSyncStrategy() {
		return syncStrategy;
	}

	/**
	 * @param syncStrategy the syncStrategy to set
	 */
	public void setSyncStrategy(SyncStrategyKind syncStrategy) {
		this.syncStrategy = syncStrategy;
	}

	/**
	 * Sets the MaxIncrease value of the ScheduleTable.
	 * @param newMaxIncrease the new MaxIncrease value of the ScheduleTable
	 * 
	 */
	public void setMaxIncrease(BigInteger newMaxIncrease)  {
			
		
		maxIncrease=newMaxIncrease;
	}
	
	/**
	 * @return the MaxIncrease of the ScheduleTable.
	 */
	public BigInteger getMaxIncrease() {
		return maxIncrease;
	}	
	
	/**
	 * Sets the MaxDecrease value of the ScheduleTable.
	 * @param newMaxDecrease the new MaxDecrease value of the ScheduleTable
	 * 
	 */
	public void setMaxDecrease(BigInteger newMaxDecrease) {
			
			
		maxDecrease=newMaxDecrease;
	}
	
	/**
	 * @return the MaxDecrease of the ScheduleTable.
	 */
	public BigInteger getMaxDecrease() {
		return maxDecrease;
	}	
	
	/**
	 * Sets the MaxIncreaseAsync value of the ScheduleTable.
	 * @param newMaxIncreaseAsync the new MaxIncreaseAsync value of the ScheduleTable
	 * 
	 */
	public void setMaxIncreaseAsync(BigInteger newMaxIncreaseAsync)  {
			
			
		maxIncreaseAsync=newMaxIncreaseAsync;
	}
	
	/**
	 * @return the MaxIncreaseAsync of the ScheduleTable.
	 */
	public BigInteger getMaxIncreaseAsync() {
		return maxIncreaseAsync;
	}	
	
	/**
	 * Sets the MaxDecreaseAsync value of the ScheduleTable.
	 * @param newMaxDecreaseAsync the new MaxDecreaseAsync value of the ScheduleTable
	 */
	public void setMaxDecreaseAsync(BigInteger newMaxDecreaseAsync)  {

			
		maxDecreaseAsync=newMaxDecreaseAsync;
	}
	
	/**
	 * @return the MaxDecreaseAsync of the ScheduleTable.
	 */
	public BigInteger getMaxDecreaseAsync() {
		return maxDecreaseAsync;
	}	
	
	/**
	 * Sets the precision value of the ScheduleTable.
	 * @param newPrecision the new Precision value of the ScheduleTable
	 */
	public void setPrecision(BigInteger newPrecision) {
			
		precision=newPrecision;
	}
	
	/**
	 * @return the precision of the ScheduleTable.
	 */
	public BigInteger getPrecision() {
		return precision;
	}	
	
	/**
	 * Creates, adds and returns an action maintained by this ScheduleTable
	 * 
	 * @param actionType the type of actions to be created
	 * @return the created ScheduleTableAction
	 * @see ScheduleTableActionKind 
	 */
	public ScheduleTableAction createAction(ScheduleTableActionKind actionType) {
		
		ScheduleTableAction action = new ScheduleTableAction(this, actionType);
		
		actions.add(action);
		
		return action;
	}	
	
	/**
	 * Creates, adds and returns an action maintained by this ScheduleTable
	 * 
	 * @return the created ScheduleTableAction
	 * @see ScheduleTableActionKind 
	 */
	public ScheduleTableAction addAction() {
		
		ScheduleTableAction action = new ScheduleTableAction(this);
		
		actions.add(action);
		
		return action;
	}		
	
	
	/**
	 * @return Collection of ScheduleTableActions owned by the ScheduleTable
	 */
	public Collection<ScheduleTableAction> getActions() {
	
		return actions;	
	}
	
	/**
	 * @return Collection of ScheduleTableAction instances owned by the ScheduleTable ordered by their expiry time offset
	 */
	public Collection<ScheduleTableAction> getOrderedActions() {
	
		List<ScheduleTableAction> orderedActions = new ArrayList<ScheduleTableAction>(actions);

		Collections.sort(orderedActions);	// use ScheduleTableAction.compareTo() method
		
		return orderedActions;	
	}
	
	/**
	 * @return Collection of all Counter instances that are incremented by owned ScheduleTableAction instances of the ScheduleTable
	 */
	public Collection<Counter>	getAllIncrementedCounters() {
		
		Collection<Counter> allCounters = new LinkedHashSet<Counter>();
		
		for ( ScheduleTableAction nextAction : actions ) {
			
			Counter counter = nextAction.getIncrementedCounter();
			
			if ( counter != null ) {
				allCounters.add(counter);
			}
		}
		return allCounters;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element

		// [1] A schedule table must be assigned to exactly one counter. [AUTOSAR]
		if ( counter == null ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' is not assigned to a counter"));
		}
		
		// [2] A schedule table must have at least one associated action with an offset value of 0. [AUTOSAR]
		// Deleted to conform to AUTOSAR 3.0. OS334 in 2.0 has been deleted in 3.0.
		
		// [3] If the autoStart value is true, then at least one appModes should be specified. [AUTOSAR]
		if ( autoStart == true && appModes.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' is autostarted, but has no application modes specified in which it should be started" ));
		}		
		
		// [4] If the autoStart value is false, then the autostart offset value should be 0. [AUTOSAR]
		if ( autoStart == false && autoStartOffset.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has autostart an offset timing value specified, when it is not autostarted" ));
		}
		
		// [5] The length value must be greater than zero. [AUTOSAR]
		if ( length.compareTo(BigInteger.ZERO) != 1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) length value"));
		}
		
		// [6] If specified, the autostart offset value must be less than the length value. [AUTOSAR]
		if ( autoStart && autoStartOffset.compareTo(length) != -1 ) { 
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an autostart offset value that exceeds the table length"));
		}
		
		// [7] The autostart offset value must be greater than, or equal to, zero. [error] [AUTOSAR]
		if ( autoStartOffset.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) offset value"));
		}		
		
		// [8] If the localToGlobalTimeSynchronization value is false, then the maxIncrease value should be 0. [AUTOSAR]
		if ( localToGlobalTimeSync == false && maxIncrease.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has no local to global time sync, but has a max increase value specified"));
		}
		
		//[9] If specified, the maxIncrease value must be greater than, or equal to, zero. [AUTOSAR]
		if ( maxIncrease.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) max increase value"));
		}

		//[10] If the localToGlobalTimeSynchronization value is false, then the maxDecrease value should be 0. [AUTOSAR]
		if ( localToGlobalTimeSync == false && maxDecrease.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has no local to global time sync, but has a max decrease value specified"));
		}		

		//[11] If specified, the maxDecrease value must be greater than, or equal to, zero. [AUTOSAR]
		if ( maxDecrease.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) max decrease value"));
		}		

		//[12] If the localToGlobalTimeSynchronization value is false, then the maxIncreaseAsync value should be 0.[AUTOSAR]
		if ( localToGlobalTimeSync == false && maxIncreaseAsync.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has no local to global time sync, but has a max increase async value specified"));
		}
		
		//[13] If specified, the maxIncreaseAsync value must be greater than, or equal to, zero. [AUTOSAR]
		if ( maxIncreaseAsync.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) max increase async value"));
		}		

		//[14] If the localToGlobalTimeSynchronization value is false, then the maxDecreaseAsync value should be 0. [AUTOSAR]
		if ( localToGlobalTimeSync == false && maxDecreaseAsync.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has no local to global time sync, but has a max decrease async value specified"));
		}		

		//[15] If specified, the maxDecreaseAsync value must be greater than, or equal to, zero. [AUTOSAR]
		if ( maxDecreaseAsync.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) max decrease async value"));
		}			

		//[16] If the localToGlobalTimeSynchronization value is false, then the precision value should be 0. [AUTOSAR]
		if ( localToGlobalTimeSync == false && precision.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has no local to global time sync, but has a precision value specified"));
		}		

		//[17] If specified, the precision value must be greater than, or equal to, zero. [AUTOSAR]
		if ( precision.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an invalid (negative) precision value"));
		}		
		
		//[18] If the autoStart value is false, then no appModes should be specified.[AUTOSAR]
		if ( autoStart == false && appModes.isEmpty() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+getName()+"' has autostart application modes specified, when it is not autostarted" ));
		}
		
		// [19] Each action within a schedule table that expires at the same offset must not activate
		// the same task or set the same event more than once. See OS407 [AUTOSAR] 
		//
		// $Req: artf1366 $
		BigInteger prevOffset = null;
		Set<Task> taskSet = new HashSet<Task>();
		Map<Event, Set<Task>> eventSet = new HashMap<Event, Set<Task>>();

		for ( ScheduleTableAction nextAction : this.getOrderedActions() ) {
			BigInteger nextOffset = nextAction.getOffset();

			if ( nextOffset.equals(prevOffset) == false ) {
				// nextAction is on a new offset, so empty existing sets by creating new ones
				taskSet.clear();
				eventSet.clear();
				prevOffset = nextOffset;
			}

			// ensure any activated tasks and any set events are not already specified
			if ( nextAction.activatesTask() ) {
				if ( taskSet.add(nextAction.getTask()) == false ) {
					// task already activated at the specified offset
					problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' activates Task '"+nextAction.getTask().getName()+"' more than once at the same offset"));
				}
			}
			else if ( nextAction.setsEvent() ) {
				// Get set of tasks activated by the set event, this is null if no tasks yet associated with the set event
				Set<Task> eventTaskSet = eventSet.get(nextAction.getEvent());
				if ( eventTaskSet == null ) {
					// no tasks are currently activated by the set event, so create the set
					eventTaskSet = new HashSet<Task>();
					eventSet.put(nextAction.getEvent(), eventTaskSet);
				}

				if ( eventTaskSet.add(nextAction.getTask()) == false ) {
					// event already set against the specified task at the current offset
					problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' sets Event '"+nextAction.getEvent().getName()+"' for Task '"+nextAction.getTask().getName()+"' more than once at the same offset"));
				}
			}
		}
		taskSet.clear();
		eventSet.clear();
			
		
	    // [20] The offset value of the first action within the schedule table shall be zero OR in the range
		// minCycle .. maxAllowedValue of the assigned counter. [error] [AUTOSAR 3.0] See OS443
		// $Req: artf1381 $
		
		// [21] The difference between the offset values of each action must be greater than or equal to
		// minCycle and less than or equal to maxAllowedValue for the assigned counter. [error] [AUTOSAR 3.0]
		
		if ( counter != null && actions.size() > 0 ) {
			prevOffset = BigInteger.ZERO;
			BigInteger delta;
			
			for ( ScheduleTableAction nextAction : this.getOrderedActions() ) {
				BigInteger nextOffset = nextAction.getOffset();
				
				delta = nextOffset.subtract(prevOffset);
				
				if ( !delta.equals(BigInteger.ZERO) && delta.compareTo(BigInteger.valueOf(counter.getMinCycle())) == -1 ) {
					problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an offset period between subsequent actions that is less than the min cycle value of counter '"+counter.getName()+"'"));	
				}
				else if ( delta.compareTo(BigInteger.valueOf(counter.getMaxAllowedValue())) == 1 ) {
					problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an offset period between subsequent actions that is more than the max allowed value of counter '"+counter.getName()+"'"));
				}
				prevOffset = nextOffset;
			}
			
		    // [22] The value of final delay (calculated as schedule table length minus the offset value of the
			// final action) shall be in the range minCycle .. maxAllowedValue of the associated counter. [error] [AUTOSAR 3.0]
			// $Req: artf1382 $
			// See OS444
			delta = length.subtract(prevOffset);
			
			if ( delta.compareTo(BigInteger.valueOf(counter.getMinCycle())) == -1 ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has a final offset period (between the last action and the schedule table length) that is less than the min cycle value of counter '"+counter.getName()+"'"));	
			}
			else if ( delta.compareTo(BigInteger.valueOf(counter.getMaxAllowedValue())) == 1 ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has a final offset period (between the last action and the schedule table length) that is more than the max allowed value of counter '"+counter.getName()+"'"));
			}	
		}
	
	    // [23] If specified the autostart offset value of the schedule table shall be in the range
		// minCycle .. maxAllowedValue of the assigned counter, and not zero [error] [AUTOSAR]
		if ( counter != null && autoStart ) { 
			if (  autoStartOffset.compareTo(BigInteger.valueOf(counter.getMinCycle())) == -1 ) { 
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an autostart offset value that is less than the min cycle value of counter '"+counter.getName()+"'"));
			}
			else if ( autoStartOffset.compareTo(BigInteger.valueOf(counter.getMaxAllowedValue())) == 1 ) { 
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an autostart offset value that is greater than the max allowed value of counter '"+counter.getName()+"'"));
			}
			else if ( autoStartOffset.equals(BigInteger.ZERO) ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+getName()+"' has an autostart offset value of zero"));
			}
		}
		
		// Check contained elements if doing full deep check
		if ( deepCheck ) {
			for ( ScheduleTableAction nextElement : actions) {
				nextElement.doModelCheck(problems, true);
			}
		}
	}		
	
	public ScheduleTable(Cpu cpu,String name) {
		super(cpu,name);
	}
}
