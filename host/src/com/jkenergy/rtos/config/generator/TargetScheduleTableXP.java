package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-02-28 18:23:34 +0000 (Thu, 28 Feb 2008) $
 * $LastChangedRevision: 627 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetScheduleTableXP.java $
 * 
 */

import java.util.ArrayList;
import java.util.List;

import com.jkenergy.rtos.config.osmodel.ScheduleTableAction;

/**
 * Intermediate target element used to store information on each schedule table expiry point to be generated.
 * An expiry point represents one or more actions from the OS model that share the same expiry time.
 * 
 * @author Mark Dixon
 *
 */

public class TargetScheduleTableXP {
	
	/**
	 * The next index position to be assigned to the next created instance
	 */	
	private static int nextIndex = 0;
	
	/**
	 * The unique index position of the TargetScheduleTableXP instance
	 * @see #nextIndex
	 */
	private int uniqueIndex = 0;
	
	/**
	 * The {@link TargetScheduleTable} to which this expiry point belongs
	 */
	private TargetScheduleTable table = null;
	
	
	/**
	 * A list of {@link TargetExpiry} instances that determine the task activation action(s) to be performed on expiry
	 */
	private List<TargetExpiry> expiryTaskActions = new ArrayList<TargetExpiry>();	
	
	/**
	 * A list of {@link TargetExpiry} instances that determine the set event action(s) to be performed on expiry
	 */
	private List<TargetExpiry> expiryEventActions = new ArrayList<TargetExpiry>();
	
	/**
	 * A list of {@link TargetExpiry} instances that determine the increment counter action(s) to be performed on expiry
	 */
	private List<TargetExpiry> expiryIncrementActions = new ArrayList<TargetExpiry>();	
	
	/**
	 * A list of {@link TargetExpiry} instances that determine the callback action(s) to be performed on expiry
	 */
	private List<TargetExpiry> expiryCallbackActions = new ArrayList<TargetExpiry>();
	
	/**
	 * Number of ticks to next expiry point in the table
	 */
	private long delta = 0;
	
	/**
	 * Creates and adds a new TargetExpiry action to the expiry point.
	 * 
	 * The nature of the TargetExpiry (i.e. whether it set and event, activates a task, increments a counter
	 * or calls a handler) if determined by examining the given {@link ScheduleTableAction}
	 * 
	 * @param action the {@link ScheduleTableAction} from which the new TargetExpiry action is to be derived
	 * @return the created TargetExpiry, null if action type unknown
	 */
	public TargetExpiry addAction(ScheduleTableAction action) {
		
		TargetExpiry expiryAction = null;
		
		// create an appropriate TargetExpiry action instance that handles the expiry
		if ( action.incrementsCounter() ) {
			expiryAction = new TargetExpiry(table.<TargetCounter>getTargetElement(action.getIncrementedCounter()));
			expiryIncrementActions.add(expiryAction);
		}
		else if ( action.callsHandler() ) {
			expiryAction = new TargetExpiry(action.getActionCallbackName());
			expiryCallbackActions.add(expiryAction);
		}
		else if ( action.setsEvent() ) {
			expiryAction = new TargetExpiry(table.<TargetEvent>getTargetElement(action.getEvent()), table.<TargetTask>getTargetElement(action.getTask()));
			expiryEventActions.add(expiryAction);
		}
		else if ( action.activatesTask() ) {
			expiryAction = new TargetExpiry(table.<TargetTask>getTargetElement(action.getTask()));
			expiryTaskActions.add(expiryAction);
		}
		

		
		return expiryAction;
	}
	
	/**
	 * @return the unique index of the TargetScheduleTableXP
	 */
	public int getUniqueIndex() {
		return uniqueIndex;
	}

	/**
	 * @return the number of ticks to next expiry point in the table
	 */
	public long getDelta() {
		return delta;
	}

	/**
	 * @param delta the number of ticks to next expiry point in the table (must be >0)
	 */
	public void setDelta(long delta) {
		assert delta > 0;	// delta values must always be more than 0
		
		this.delta = delta;
	}

	/**
	 * @return the {@link TargetScheduleTable} to which this expiry point belongs
	 */
	public TargetScheduleTable getScheduleTable() {
		return table;
	}	
	
	/**
	 * @return the expiryActions
	 */
	public List<TargetExpiry> getExpiryActions() {
		
		List<TargetExpiry> expiryActions = new ArrayList<TargetExpiry>();
		
		// Add expiry actions in an order required by AUTOSAR 3.0, i.e. TaskActivations prior to Setting of events
		// $Req: artf1371 $
		expiryActions.addAll(expiryCallbackActions);
		expiryActions.addAll(expiryIncrementActions);
		expiryActions.addAll(expiryTaskActions);
		expiryActions.addAll(expiryEventActions);
		
		return expiryActions;
	}

	/**
	 * @param table the owning TargetScheduleTable
	 */
	protected TargetScheduleTableXP(TargetScheduleTable table) {
	
		this.table = table;	
		this.uniqueIndex = nextIndex++;
	}	
}
