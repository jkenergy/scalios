package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Event.java $
 * 
 */

import java.util.*;
import java.math.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models an Event within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Event extends OSModelElement {

	
	/**
	 * The bit mask of this event
	 */
	private BigInteger mask=BigInteger.ZERO;

	/**
	 * isAutoMask flag that specifies whether the bit mask is to be automatically calculated
	 */
	private boolean isAutoMask=false;
	
	/**
	 * Set of Tasks that this event activates
	 */
	private Set<Task> tasks = new LinkedHashSet<Task>();
	
	/**
	 * Set of Alarms that set this event
	 */
	private Set<Alarm> alarms = new LinkedHashSet<Alarm>();	

	/**
	 * Set of Messages that set this event during normal notification and low threshold notifiction
	 */
	private Set<Message> messages = new LinkedHashSet<Message>();		
	
	/**
	 * Set of ScheduleTableActions that set this event
	 */
	private Set<ScheduleTableAction> tableActions = new LinkedHashSet<ScheduleTableAction>();					/* $Req: AUTOSAR $ */
	
	/**
	 * Sets the mask value of the Event
	 * @param newMask the new mask value of the event
	 */
	public void setMask(BigInteger newMask)  {
					
		mask=newMask;
	}

	/**
	 * 
	 * @return the current mask value of the Event
	 */
	public BigInteger getMask() {
		return mask;
	}
	
	/**
	 * 
	 * @param newIsAutoMask the new isAutoMask flag
	 */
	public void isAutoMask(boolean newIsAutoMask) {
		
		isAutoMask=newIsAutoMask;
	}
	
	/**
	 * 
	 * @return isAutoMask flag
	 */
	public boolean isAutoMask() {
		return isAutoMask;
	}
	
	/**
	 * Adds a Task to the collection of tasks that this event activates.
	 * This method creates a two way relationship.
	 * 
	 * @param task the Task to be added
	 */
	public void addTask(Task task) {
		
		if ( task!=null ) {
			if ( tasks.add(task) )
				task.addEvent(this);	// inform task that it reacts to this event
		}
	}
	
	/**
	 * Returns the collection of Tasks to which this Event activates
	 * @return Collection of Tasks
	 */
	public Collection<Task> getTasks() {
		return tasks;
	}	
	
	/**
	 * Adds an Alarm to the collection of alarms that set this event.
	 * This method creates a two way relationship.
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addAlarm(Alarm alarm) {
		
		if ( alarm!=null ) {
			if ( alarms.add(alarm) )
				alarm.setEvent(this);	// inform alarm that it sets this event
		}
	}
	
	/**
	 * Returns the collection of Alarms that set this event.
	 * @return Collection of Alarms
	 */
	public Collection<Alarm> getAlarms() {
		return alarms;
	}
	
	/**
	 * Adds a Message to the collection of messages that set this event during notification.
	 * This method creates a two way relationship.
	 * 
	 * @param message the Message to be added
	 */
	public void addMessage(Message message) {
		
		if ( message!=null ) {
			if ( messages.add(message) )
				message.setNotificationEvent(this);	// inform message that it sets this event
		}
	}
	
	/**
	 * Adds a Message to the collection of messages that set this event during low threshold notification.
	 * This method creates a two way relationship.
	 * 
	 * @param message the Message to be added
	 */
	public void addLowMessage(Message message) {
		
		if ( message!=null ) {
			if ( messages.add(message) )
				message.setLowNotificationEvent(this);	// inform message that it sets this event
		}
	}	
	
	/**
	 * Returns the collection of Messages that set this event.
	 * @return Collection of Messages
	 */
	public Collection<Message> getMessages() {
		return messages;
	}	

	/**
	 * Adds a ScheduleTableAction to the collection of actions that set this event.
	 * This method creates a two way relationship.
	 * 
	 * @param action the ScheduleTableAction to be added
	 */
	public void addScheduleTableAction(ScheduleTableAction action) {
		
		if ( action!=null ) {
			if ( tableActions.add(action) )
				action.setEvent(this);	// inform action that it sets this event
		}
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		// [1] An event should be reacted to by one or more tasks.
		if ( tasks.isEmpty() ) {
			
			problems.add(new Problem(Problem.INFORMATION, "Event '"+getName()+"' is not associated with any Tasks"));			
		}
			
		// [2] If the event has a non-auto mask value, then the mask value must be greater than zero.
		if ( isAutoMask==false ) {
			if ( mask == null || mask.compareTo(BigInteger.ZERO) != 1 ) {
				problems.add(new Problem(Problem.ERROR, "Event '"+getName()+"' has an invalid mask value"));
			}
		}		
	}	
	

	
	/**
	 * Returns the collection of actions that set this event.
	 * @return Collection of ScheduleTableActions
	 */
	public Collection<ScheduleTableAction> getScheduleTableAction() {
		return tableActions;
	}	
	
	public Event(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
