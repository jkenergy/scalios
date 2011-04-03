package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ScheduleTableAction.java $
 * 
 */

import java.math.BigInteger;
import java.util.List;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

/**
 * Class to represent ScheduleTable actions.<br><br>
 * This class implements the Comparable interface in order to allow sorting of actions based on their relative offset values.<br><br>
 *
 * Note: This model element is only required for AUTOSAR conformance.<br><br>
 *
 * @author Mark Dixon
 *
 */
public class ScheduleTableAction implements Comparable<ScheduleTableAction>, Checkable {

	/**
	 * Type of action to be performed
	 * @see ScheduleTableActionKind								
	 */ 
	private ScheduleTableActionKind action=ScheduleTableActionKind.ACTIVATETASK_LITERAL;	/* $Req: AUTOSAR $ */		
	
	/**
	 * Offset (relative from the start of a period) to action being performed
	 * 
	 */
	private BigInteger offset=BigInteger.ZERO;					/* $Req: AUTOSAR $ */		
	
	/**
	 * The Event that is set by the ScheduleTableAction (if any)
	 * Only applicable if action==SETEVENT
	 */
	private Event event=null;									/* $Req: AUTOSAR $ */
	
	/**
	 * The Task that is activated by the ScheduleTableAction (if any)
	 * Only applicable if action==ACTIVATETASK | action==SETEVENT 
	 */
	private Task task=null;										/* $Req: AUTOSAR $ */
	
	/**
	 * The Counter which this ScheduleTableAction increments (if any)
	 * Only applicable if action==INCREMENTCOUNTER
	 */
	private Counter incrementedCounter=null;					/* $Req: EXTENSION $ */	
	
	/**
	 * The name of the callback function to call to implement the action (if any)
	 * Only applicable if action==ACTIONCALLBACK
	 */
	private String actionCallbackName=null;						/* $Req: EXTENSION $ */	
	
	/**
	 * The ScheduleTable that owns the action
	 */
	private ScheduleTable scheduleTable;
	
	/**
	 * Sets the offset value of the ScheduleTableAction.
	 * @param newOffset the new offset value of the ScheduleTableAction
	 */
	public void setOffset(BigInteger newOffset) {
			
		offset=newOffset;
	}
	
	/**
	 * @return the offset of the ScheduleTableAction.
	 */
	public BigInteger getOffset() {
		return offset;
	}

	/**
	 * @return the action to be performed
	 */
	public ScheduleTableActionKind getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(ScheduleTableActionKind action) {
		this.action = action;
	}		
	
	/**
	 * Sets the Event that the ScheduleTableAction sets.
	 * This method creates a two way relationship. 
	 * @param newEvent the Event that is to be set by the ScheduleTableAction
	 */
	public void setEvent(Event newEvent) {
		
		if ( newEvent!=null ) {
			
			if ( event!=newEvent ) {
				
				event=newEvent;
				
				event.addScheduleTableAction(this);
			}
		}
	}

	/**
	 * Returns the event that the ScheduleTableAction sets.
	 * @return Event that is set (may be null)
	 */
	public Event getEvent() {
		return event;
	}	
	
	/**
	 * Sets the Task that the ScheduleTableAction activates.
	 * This method creates a two way relationship. 
	 * @param newTask the Task that is to be activated by the ScheduleTableAction
	 */
	public void setTask(Task newTask) {
		
		if ( newTask!=null ) {
			
			if ( task!=newTask ) {
				
				task=newTask;
				
				task.addScheduleTableAction(this);
			}
		}
	}

	/**
	 * Returns the task that the ScheduleTableAction activates.
	 * @return Task that is activated (may be null)
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * @return the ScheduleTable that owns the action
	 */
	public ScheduleTable getScheduleTable() {
		return scheduleTable;
	}		
	
	/**
	 * Sets the Counter which the action increments on expiry.
	 * 
	 * This method creates a two way relationship. 
	 * @param newCounter the Counter which the ScheduleTableAction increments on expiry.
	 */
	public void setIncrementedCounter(Counter newCounter) {
		
		if ( newCounter!=null ) {
			
			if ( incrementedCounter!=newCounter ) {
				
				incrementedCounter=newCounter;
				
				incrementedCounter.addIncrementingAction(this);
			}
		}
	}

	/**
	 * Returns the Counter which this ScheduleTableAction increments on expiry.
	 * @return Counter which this ScheduleTableAction increments on expiry.
	 */
	public Counter getIncrementedCounter() {
		return incrementedCounter;
	}		
	
	
	/**
	 * Sets the name of the action callback function
	 * @param newActionCallbackName the name of the callback function
	 */
	public void setActionCallbackName(String newActionCallbackName) {
		actionCallbackName=newActionCallbackName;
	}
	
	/**
	 * 
	 * @return the name of the action callback function
	 */
	public String getActionCallbackName() {
		return actionCallbackName;
	}	
	
	
	/**
	 * 
	 * @return true if this action activates a Task on expiry
	 */
	public boolean activatesTask() {
		return ScheduleTableActionKind.ACTIVATETASK_LITERAL.equals(action);
	}
	
	/**
	 * 
	 * @return true if this action sets an Event on expiry
	 */
	public boolean setsEvent() {
		return ScheduleTableActionKind.SETEVENT_LITERAL.equals(action);
	}	
	
	/**
	 * 
	 * @return true if this action calls a callback handler on expiry
	 */
	public boolean callsHandler() {
		return ScheduleTableActionKind.ACTIONCALLBACK_LITERAL.equals(action);
	}	
	
	/**
	 * 
	 * @return true if this action increments a Counter on expiry
	 */
	public boolean incrementsCounter() {
		return ScheduleTableActionKind.INCREMENTCOUNTER_LITERAL.equals(action);
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ScheduleTableAction o) {
		return offset.compareTo(o.getOffset());	// compare ScheduleTableAction instance using the relative offset
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		// Do check of this element
		
		String name = scheduleTable.getName();
		
		//[1] The offset value must be less than the length value within the owning scheduleTable. [AUTOSAR]	
		if ( offset.compareTo(scheduleTable.getLength()) != -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action with an offset greater than the table length"));
		}		

		//[2] The offset value must be greater than, or equal to, zero. [AUTOSAR]
		if ( offset.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action with an invalid (negative) offset value"));
		}

		//[3] If the action value equals ACTIVATETASK then the task that the table activates must be identified. [AUTOSAR]
		if ( activatesTask() && task == null ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that does not specify the task to be activated"));
		}

		//[4] If the action value equals ACTIVATETASK then no set event, incremented counter or actionCallbackName value should be specified. [AUTOSAR]
		if ( activatesTask() && (event!=null || incrementedCounter!=null || actionCallbackName !=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+name+"' contains an action that defines information not relevant for task activation"));
		}		

		//[5] If the action value equals SETEVENT then the event that the table sets and the specific task to be activated by the event must be identified. [AUTOSAR]
		if ( setsEvent() && (task == null || event == null)) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that does not specify the event to be set and the associated task"));
		}		

		//[6] If the action value equals SETEVENT then no incremented counter or actionCallbackName value should be specified. [AUTOSAR]
		if ( setsEvent() && (incrementedCounter!=null || actionCallbackName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+name+"' contains an action that defines information not relevant for event setting"));
		}			

		//[7] If the action value equals ACTIONCALLBACK then the actionCallbackName value must be given. [EXTENSION]
		if ( callsHandler() && actionCallbackName == null ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that does not specify the name of the handler to be called"));
		}		

		//[8] If the action value equals ACTIONCALLBACK then no activated task, set event or incremented counter should be specified. [EXTENSION]
		if ( callsHandler() && (event!=null || incrementedCounter!=null || task!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+name+"' contains an action that defines information not relevant for callback handler calling"));
		}		

		//[9] If the action value equals INCREMENTCOUNTER then the counter that the table increments must be identified. [EXTENSION]
		if ( incrementsCounter() && incrementedCounter == null ) {
			problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that does not specify the counter to be incremented"));
		}		

		//[10] If the action value equals INCREMENTCOUNTER then no activated task, set event, or actionCallbackName value should be specified. [EXTENSION]
		if ( incrementsCounter() && (event!=null || task!=null || actionCallbackName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Schedule Table '"+name+"' contains an action that defines information not relevant for counter incrementing"));
		}		

		//[11] If the action value equals SETEVENT any identified task must be one of the tasks to which the identified event reacts. [AUTOSAR]
		if ( setsEvent() && task != null && event != null ) {
			
			if ( event.getTasks().contains(task) == false ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that specifies a task '"+task.getName()+"', which does not react to event '"+event.getName()+"'"));
			}
		}		

		//[12] A schedule table may not have an incrementedCounter that directly or indirectly drives the schedule table. [EXTENSION]
		if ( incrementsCounter() && incrementedCounter != null ) {

			if ( incrementedCounter == scheduleTable.getCounter() ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that increments the counter that directly drives the table"));				
			}
			else if ( incrementedCounter.getAllIncrementers().contains(this) ) {
				problems.add(new Problem(Problem.ERROR, "Schedule Table '"+name+"' contains an action that increments a counter that indirectly drives the table"));
			}
		}		

		//[13] If specified, the actionCallbackName value must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols. [EXTENSION]
		if ( actionCallbackName != null ) {
			scheduleTable.validateIdentifierName(problems, actionCallbackName);
		}	
	}
	
	
	/**
	 * 
	 * @param scheduleTable the ScheduleTable that owns the actions
	 * @param action the type of action to be performed
	 */
	ScheduleTableAction(ScheduleTable scheduleTable, ScheduleTableActionKind action) {
		
		this.scheduleTable = scheduleTable;
		this.action = action;
	}

	/**
	 * 
	 * @param scheduleTable the ScheduleTable that owns the actions
	 */
	ScheduleTableAction(ScheduleTable scheduleTable) {
		
		this.scheduleTable = scheduleTable;
	}	
}

