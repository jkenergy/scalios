package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Alarm.java $
 * 
 */

import java.util.*;
import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models an Alarm within the OS.
 * 
 * @author Mark Dixon
 *
 */
public class Alarm extends OSModelElement {
	

	
	/**
	 * The action to be performed when the alarm expires
	 * @see ActionKind
	 */
	private ActionKind action=ActionKind.ALARMCALLBACK_LITERAL;
	
	/**
	 * The name of the callback function to call when the alarm expires
	 * Only applicable if action==ALARMCALLBACK
	 */
	private String alarmCallbackName=null;
	
	/**
	 * Autostart flag
	 */
	private boolean autostart=false;	
	
	/**
	 * The time when the alarm will first expire when autostarted
	 * 
	 */
	private long alarmTime=0;
	
	/**
	 * The cyclic time of a cyclic alarm when autostarted
	 *
	 */	
	private long cycleTime=0;	
	
	/**
	 * The Counter to which this alarm is assigned
	 */
	private Counter counter=null;	
	
	/**
	 * The Counter which this alarm increments on expiry
	 */
	private Counter incrementedCounter=null;					/* $Req: AUTOSAR $ */	
	
	
	/**
	 * The Event that is set by this alarm (if any)
	 * Only applicable if action==SETEVENT
	 */
	private Event event=null;
	
	/**
	 * The Task that is activated by this alarm (if any)
	 * Only applicable if action==ACTIVATETASK | action==SETEVENT 
	 */
	private Task task=null;
	
	/**
	 * Set of AppModes in which the alarm is started
	 */
	private Set<AppMode> appModes = new LinkedHashSet<AppMode>();	
	
	
	/**
	 * 
	 * @param newAction the action to perform when the alarm expires
	 * @see ActionKind
	 */
	public void setAction(ActionKind newAction) {
		
		if ( newAction!=null )
			action=newAction;
	}
	
	/**
	 * 
	 * @return the action performed when the alarm expires
	 * @see ActionKind
	 */
	public ActionKind getAction() {
		return action;
	}
	
	/**
	 * Sets the name of the alarm callback function
	 * @param newAlarmCallbackName the name name of the callback function
	 */
	public void setAlarmCallbackName(String newAlarmCallbackName) {
		alarmCallbackName=newAlarmCallbackName;
	}
	
	/**
	 * 
	 * @return the name of the alarm callback function
	 */
	public String getAlarmCallbackName() {
		return alarmCallbackName;
	}
	
	/**
	 * Sets the autostart flag of the alarm
	 * @param newAutostart the new autostart flag
	 */
	public void setAutostart(boolean newAutostart) {
		autostart=newAutostart;
	}
	
	/**
	 * @return autostart flag of the alarm
	 */
	public boolean getAutostart() {
		return autostart;
	}	
	
	/**
	 * Sets the alarmTime value for the alarm
	 * @param newAlarmTime the time when the alarm will first expire when autostarted
	 */
	public void setAlarmTime(long newAlarmTime) {
	
		alarmTime=newAlarmTime;
	}
	
	/**
	 * 
	 * @return the time when the alarm will first expire when autostarted
	 */
	public long getAlarmTime() {
		return alarmTime;
	}	
	
	/**
	 * Sets the cycleTime value for the alarm
	 * @param newCycleTime the cyclic time of a cyclic alarm when autostarted
	 */
	public void setCycleTime(long newCycleTime) {

		cycleTime=newCycleTime;
	}
	
	/**
	 * 
	 * @return the cyclic time of a cyclic alarm when autostarted
	 */
	public long getCycleTime() {
		return cycleTime;
	}	
	
	/**
	 * Sets the Event that this alarm sets.
	 * This method creates a two way relationship. 
	 * @param newEvent the Event that is to be set by the Alarm
	 */
	public void setEvent(Event newEvent) {
		
		if ( newEvent!=null ) {
			
			if ( event!=newEvent ) {
				
				event=newEvent;
				
				event.addAlarm(this);
			}
		}
	}

	/**
	 * Returns the event that this alarm sets.
	 * @return Event that is set (may be null)
	 */
	public Event getEvent() {
		return event;
	}	
	
	/**
	 * Sets the Task that this alarm activates.
	 * This method creates a two way relationship. 
	 * @param newTask the Task that is to be activated by the Alarm
	 */
	public void setTask(Task newTask) {
		
		if ( newTask!=null ) {
			
			if ( task!=newTask ) {
				
				task=newTask;
				
				task.addAlarm(this);
			}
		}
	}

	/**
	 * Returns the task that this alarm activates.
	 * @return Task that is activated (may be null)
	 */
	public Task getTask() {
		return task;
	}	
	
	/**
	 * Sets the Counter to which this Alarm is assigned.
	 * This method creates a two way relationship. 
	 * @param newCounter the Counter to which the Alarm is assigned.
	 */
	public void setCounter(Counter newCounter) {
		
		if ( newCounter!=null ) {
			
			if ( counter!=newCounter ) {
				
				counter=newCounter;
				
				counter.addAlarm(this);
			}
		}
	}

	/**
	 * Returns the Counter to which this Alarm is assigned.
	 * @return Counter to which this Alarm is assigned.
	 */
	public Counter getCounter() {
		return counter;
	}		
	
	/**
	 * Sets the Counter which this Alarm increments on expiry.
	 * This method creates a two way relationship. 
	 * @param newCounter the Counter which this Alarm increments on expiry.
	 */
	public void setIncrementedCounter(Counter newCounter) {
		
		if ( newCounter!=null ) {
			
			if ( incrementedCounter!=newCounter ) {
				
				incrementedCounter=newCounter;
				
				incrementedCounter.addIncrementingAlarm(this);
			}
		}
	}

	/**
	 * Returns the Counter which this Alarm increments on expiry.
	 * @return Counter which this Alarm increments on expiry.
	 */
	public Counter getIncrementedCounter() {
		return incrementedCounter;
	}	
	
	/**
	 * Adds an AppMode to the collection of AppModes in which this alarm is started
	 * This method creates a two way relationship.
	 * 
	 * @param appMode the AppMode to be added
	 */
	public void addAppMode(AppMode appMode) {
		
		if ( appMode!=null ) {
			if ( appModes.add(appMode) )
				appMode.addAlarm(this);	// inform the appMode that this alarm is started
		}
	}	
	
	/**
	 * 
	 * @return true if this Alarm activates a Task on expiry
	 */
	public boolean activatesTask() {
		return ActionKind.ACTIVATETASK_LITERAL.equals(action);
	}
	
	/**
	 * 
	 * @return true if this Alarm sets an Event on expiry
	 */
	public boolean setsEvent() {
		return ActionKind.SETEVENT_LITERAL.equals(action);
	}	
	
	/**
	 * 
	 * @return true if this Alarm calls a callback handler on expiry
	 */
	public boolean callsHandler() {
		return ActionKind.ALARMCALLBACK_LITERAL.equals(action);
	}	
	
	/**
	 * 
	 * @return true if this Alarm increments a Counter on expiry
	 */
	public boolean incrementsCounter() {
		return ActionKind.INCREMENTCOUNTER_LITERAL.equals(action);
	}	
	
	/**
	 * Returns the collection of AppModes in which the alarm is auto-started
	 * 
	 * @return Collection of AppModes
	 */
	public Collection<AppMode> getAppModes() {
		return appModes;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		//[1] An alarm must be assigned to exactly one counter.
		// $Req: artf1209 $

		if ( counter == null ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' is not assigned to a counter"));
		}
				
		//[2] If the action value equals ACTIVATETASK then the task that the alarm activates must be identified.
		if ( activatesTask() && task == null ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' does not specify the task to be activated"));
		}
		
		//[3] If the action value equals ACTIVATETASK then no set event, incremented counter or alarmCallbackName value should be specified.
		if ( activatesTask() && (event!=null || incrementedCounter!=null || alarmCallbackName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' specifies action information not relevant for task activation"));
		}
		
		//[4] If the action value equals SETEVENT then the event that the alarm sets and the specific task to be activated by the event must be identified.
		if ( setsEvent() && (task == null || event == null)) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' does not specify the event to be set and the associated task"));
		}
		
		//[5] If the action value equals SETEVENT then no incremented counter or alarmCallbackName value should be specified. 
		if ( setsEvent() && (incrementedCounter!=null || alarmCallbackName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' specifies action information not relevant for event setting"));
		}		

		//[6] If the action value equals ALARMCALLBACK then the alarmCallbackName value must be given. 
		if ( callsHandler() && alarmCallbackName == null ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' does not specify the name of the handler to be called"));
		}
		
		//[7] If the action value equals ALARMCALLBACK then no activated task, set event or incremented counter should be specified.
		if ( callsHandler() && (event!=null || incrementedCounter!=null || task!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' specifies action information not relevant for callback handler calling"));
		}
		
		//[8] If the action value equals INCREMENTCOUNTER then the counter that the alarm increments must be identified.
		if ( incrementsCounter() && incrementedCounter == null ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' does not specify the counter to be incremented"));
		}
		
		//[9] If the action value equals INCREMENTCOUNTER then no activated task, set event, or alarmCallbackName value should be specified.
		if ( incrementsCounter() && (event!=null || task!=null || alarmCallbackName!=null) ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' specifies action information not relevant for counter incrementing"));
		}
		
		//[10] If the action value equals SETEVENT any identified task must be one of the tasks to which the identified event reacts. 
		if ( setsEvent() && task != null && event != null ) {
			
			if ( event.getTasks().contains(task) == false ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' specifies task '"+task.getName()+"', which does not react to event '"+event.getName()+"'"));
			}
		}
		
		//[11] An alarm may not have an incrementedCounter that directly or indirectly drives the alarm. [AUTOSAR]
		if ( incrementsCounter() && incrementedCounter != null ) {

			if ( incrementedCounter == counter ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' increments the counter that directly drives the alarm"));				
			}
			else if ( incrementedCounter.getAllIncrementers().contains(this) ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' increments a counter that indirectly drives the alarm"));
			}
		}
			
		//[12] The alarmTime value must be greater than, or equal to, zero.
		if ( alarmTime < 0 ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has an invalid (negative) alarm time value"));
		}

		//[13] The cycleTime value must be greater than, or equal to, zero.
		if ( cycleTime < 0 ) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has an invalid (negative) cycle time value"));
		}
		
		//[14] The alarmTime must be less than or equal to the maxAllowedValue that is specified within the counter to which the alarm is assigned.
		if ( counter != null ) {
			if ( alarmTime > counter.getMaxAllowedValue() ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has an alarm time value that is greater than the max allowed value of counter '"+counter.getName()+"'"));
			}
		}
		
		//[15] The cycletime must be less than, or equal to, the maxAllowedValue that is specified within the counter to which the alarm is assigned.
		if ( counter != null ) {
			if ( cycleTime > counter.getMaxAllowedValue() ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has a cycle time value that is greater than the max allowed value of counter '"+counter.getName()+"'"));
			}
		}		

		//[16] The cycletime must be zero, or greater than the minCycle that is specified within the counter to which the alarm is assigned.
		if ( counter != null ) {
			if ( cycleTime > 0 && cycleTime < counter.getMinCycle() ) {
				problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has a cycle time value that is less than the min cycle value of counter '"+counter.getName()+"'"));
			}
		}		

		//[17] If specified, the alarmCallbackName value must conform to ANSI C identifier rules and must not clash with any ANSI C keywords.
		if ( alarmCallbackName != null ) {
			validateIdentifierName(problems, alarmCallbackName);
		}
		
		//[18] If the autoStart value is false, then the alarmTime value and cycleTime value should be 0.
		if ( autostart == false && (alarmTime != 0 || cycleTime !=0) ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' has autostart timing values specified, when it is not autostarted" ));
		}
		
		//[19] If the autoStart value is true, then at least one appModes should be specified.
		if ( autostart == true && appModes.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' is autostarted, but has no application modes specified in which it should be started" ));
		}
		
		//[20] If the autoStart value is false, then no appModes should be specified.
		if ( autostart == false && appModes.isEmpty() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Alarm '"+getName()+"' has autostart application modes specified, when it is not autostarted" ));
		}
		
		//[21] An alarm must have at least one expiry action specified [error]
		//$Req: artf1210 $
		if(! activatesTask() && !setsEvent() && !callsHandler() && incrementedCounter == null) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' does not specify an expiry action"));
		}
		
		//[22] If the autoStart value is true, then the alarmTime must be greater than zero
		//See 10.2.6 of AUTOSAR SWS 3.0 OsAlarmAlarmTime {ALARMTIME}
		//TODO: Check to see if there is a need for constraints to check that alarmTime and cycleTime is in the range min..max
		if ( autostart == true && alarmTime == 0) {
			problems.add(new Problem(Problem.ERROR, "Alarm '"+getName()+"' has an autostart offset of zero"));
		}
	}
	
	public Alarm(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
