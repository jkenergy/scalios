package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-02-25 21:38:01 +0000 (Mon, 25 Feb 2008) $
 * $LastChangedRevision: 623 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Task.java $
 * 
 */

import java.math.BigInteger;
import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of Runnable that models a Task within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Task extends Runnable {
	
	/**
	 * Task preemptability
	 * @see ScheduleKind
	 */ 
	private ScheduleKind schedule=ScheduleKind.NON_LITERAL;
	
	/**
	 * Task Activation count
	 */
	private long activation=1;
	
	/**
	 * Autostart flag
	 */
	private boolean autostart=false;
	
	/**
	 * Maximum time budget (in nanoseconds) of the Task (when Timing Protection active)
	 * 
	 * @see Runnable#timingProtection
	 */
	private BigInteger timeFrame=BigInteger.ZERO;		/* $Req: AUTOSAR $ */
		
	/**
	 * Set of Events that activate the task
	 */
	private Set<Event> events = new LinkedHashSet<Event>();
	
	/**
	 * Set of Alarms that activate the task
	 */
	private Set<Alarm> alarms = new LinkedHashSet<Alarm>();		
	
	/**
	 * Set of AppModes in which the task is started
	 */
	private Set<AppMode> appModes = new LinkedHashSet<AppMode>();	
	
	/**
	 * Set of Messages that activate the task during normal notification and low threshold notifiction
	 */
	private Set<Message> messages = new LinkedHashSet<Message>();	
	
	/**
	 * Set of ScheduleTableActions that activate the task
	 */
	private Set<ScheduleTableAction> tableActions = new LinkedHashSet<ScheduleTableAction>();		/* $Req: AUTOSAR $ */	
	
	/**
	 * Set of Applications that restart the task
	 */	
	private Set<Application> restartingApplications = new LinkedHashSet<Application>();			/* $Req: AUTOSAR $ */
	
	/**
	 * Static method that returns the index of the given task within the collection of tasks
	 * 
	 * @param task
	 * @return zero based index of the task position within the collection, -1 if task not in the collection
	 */
	public static int getTaskIndex(Collection<Task> tasks, Task task) {

		int index = 0;
		
		Iterator<Task> iter = tasks.iterator();
		
		while (iter.hasNext()) {
			Task next = iter.next();
			
			if ( next == task ) {
				return index;
			}
			index++;
		}

		return -1;
	}
	
	/**
	 * Adds an Event to the collection of events that activate this task.
	 * This method creates a two way relationship.
	 * 
	 * @param event the Event to be added
	 */
	public void addEvent(Event event) {
		
		if ( event!=null ) {
			if ( events.add(event) )
				event.addTask(this);	// inform event that it activates this task
		}
	}

	/**
	 * Returns the collection of Events that activate this task
	 * @return Collection of Events
	 */
	public Collection<Event> getEvents() {
		return events;
	}

	/**
	 * Adds an Alarm to the collection of alarms that activate this task.
	 * This method creates a two way relationship.
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addAlarm(Alarm alarm) {
		
		if ( alarm!=null ) {
			if ( alarms.add(alarm) )
				alarm.setTask(this);	// inform alarm that it activates this task
		}
	}
	
	/**
	 * Returns the collection of Alarms that activate the task
	 * @return Collection of Alarms
	 */
	public Collection<Alarm> getAlarms() {
		return alarms;
	}

	/**
	 * Adds an AppMode to the collection of AppModes in which this task is activated
	 * This method creates a two way relationship.
	 * 
	 * @param appMode the AppMode to be added
	 */
	public void addAppMode(AppMode appMode) {
		
		if ( appMode!=null ) {
			if ( appModes.add(appMode) )
				appMode.addTask(this);	// inform the appMode that this task is activated
		}
	}	
	
	/**
	 * Returns the collection of AppModes in which the task is auto-started.
	 * 
	 * @return Collection of AppModes
	 */
	public Collection<AppMode> getAppModes() {
		return appModes;
	}
	
	/**
	 * Adds a Message to the collection of messages that activate this task during notification.
	 * This method creates a two way relationship.
	 * 
	 * @param message the Message to be added
	 */
	public void addMessage(Message message) {
		
		if ( message!=null ) {
			if ( messages.add(message) )
				message.setNotificationTask(this);	// inform message that it activates this task
		}
	}
	
	/**
	 * Adds a Message to the collection of messages that activate this task during low threshold notification.
	 * This method creates a two way relationship.
	 * 
	 * @param message the Message to be added
	 */
	public void addLowMessage(Message message) {
		
		if ( message!=null ) {
			if ( messages.add(message) )
				message.setLowNotificationTask(this);	// inform message that it activates this task
		}
	}	
	
	/**
	 * Returns the collection of Messages that activate this task.
	 * @return Collection of Messages
	 */
	public Collection<Message> getMessages() {
		return messages;
	}	
	
	/**
	 * Adds a ScheduleTableAction to the collection of actions that activate this task.
	 * This method creates a two way relationship.
	 * 
	 * @param action the ScheduleTableAction to be added
	 */
	public void addScheduleTableAction(ScheduleTableAction action) {
		
		if ( action!=null ) {
			if ( tableActions.add(action) )
				action.setTask(this);	// inform action that it that activates this task.
		}
	}
	
	/**
	 * Returns the collection of actions that activate this task.
	 * @return Collection of ScheduleTableActions
	 */
	public Collection<ScheduleTableAction> getScheduleTableAction() {
		return tableActions;
	}		
	
	
	/**
	 * Adds an Application to the collection of applications that restart this task.
	 * This method creates a two way relationship.
	 * 
	 * @param application the Application to be added
	 */
	public void addRestartingApplication(Application application) {
		if ( application!=null ) {
			if ( restartingApplications.add(application) )
				application.setRestartedTask(this);	// inform the Application that it restarts this Task
		}		
	}
	
	/**
	 * Returns the collection of Applications that restart this task.
	 * @return Collection of Applications
	 */
	public Collection<Application> getRestartingApplications() {
		return restartingApplications;
	}	
	
	
	/**
	 * Sets the preempability state of the task
	 * @param newSchedule the new preempability state of the task
	 * @see ScheduleKind
	 */
	public void setSchedule(ScheduleKind newSchedule) {

		schedule=newSchedule;
	}
	
	/**
	 * 
	 * @return the preempability of the task
	 * @see ScheduleKind
	 */
	public ScheduleKind getSchedule() {
		return schedule;
	}
	

	/**
	 * Sets the activation count of the task.
	 * @param newActivition the new activation count value
	 */
	public void setActivation(long newActivition) {
		
		activation=(int)newActivition;
	}
	
	/**
	 * 
	 * @return the activation count of the task
	 */
	public long getActivation() {
		return activation;
	}
	
	/**
	 * Sets the autostart flag of the task
	 * @param newAutostart the new autostart flag
	 */
	public void setAutostart(boolean newAutostart) {
		autostart=newAutostart;
	}
	
	/**
	 * @return autostart flag of the task
	 */
	public boolean getAutostart() {
		return autostart;
	}
	
	/**
	 * Sets the timeFrame value (budget) of the Task.
	 * @param newTimeFrame the new timeframe value of the Task
	 * 
	 */
	public void setTimeFrame(BigInteger newTimeFrame)  {
					
		timeFrame=newTimeFrame;
	}
	
	/**
	 * @return the timeFrame of the Task.
	 */
	public BigInteger getTimeFrame() {
		return timeFrame;
	}	
	
	/**
	 * 
	 * @return true if this task is a basic task (basic tasks have no associated events)
	 */
	public boolean isBasicTask() {
		return ( events.isEmpty() );
	}	

	/**
	 * 
	 * @return true if this task is an extended task (extended tasks have one or more associated events)
	 */
	public boolean isExtendedTask() {
		return ( !events.isEmpty() );
	}		
	
	/**
	 * 
	 * @return true if this task is non pre-emptable
	 */
	public boolean isNonPreemptable() {
		return ScheduleKind.NON_LITERAL.equals(schedule);
	}	
	
	/**
	 * 
	 * @return true if the target task is activated by an alarm or schedule table
	 */
	public boolean isActivated() {
				
		for ( Alarm next : alarms ) {
			if ( next.activatesTask() )
				return true;
		}
		
		for ( ScheduleTableAction next : tableActions ) {
			if ( next.activatesTask() )
				return true;
		}			
		
		return false;
	}	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element

		// [1] If the task reacts to one or more events (i.e. is an extended task), then the activationCount value must be equal to 1.
		if ( events.size() > 0 && activation != 1 ) {
			problems.add(new Problem(Problem.ERROR, "Task '"+getName()+"' is an extended task (reacts to events), but has an activation count not equal to 1"));
		}
		
		// [2] If the schedule value equals NON then all accessed resources must have a resourceProperty value not equal to INTERNAL.
		if ( isNonPreemptable() ) {
			for ( Resource next : getResources() ) {
				
				if ( next.isInternal() ) {
					problems.add(new Problem(Problem.WARNING, "Task '"+getName()+"' is non pre-emptable but accesses the internal resource '"+next.getName()+"'"));
				}
			}			
		}

		// [3] If the schedule value equals FULL then at most one of the accessed resources may have a resourceProperty value equal to INTERNAL.
		if ( !isNonPreemptable() ) {
			
			int resCount = 0;
			
			for ( Resource next : getResources() ) {
				
				if ( next.isInternal() ) {
					resCount++;
				}
			}	
			if ( resCount > 1 ) {
				problems.add(new Problem(Problem.WARNING, "Task '"+getName()+"' attempts to access more than one internal resource"));
			}			
		}		
		
		// [4] The activationCount value must be greater than zero. 
		if ( activation <= 0 ) {
			problems.add(new Problem(Problem.ERROR, "Task '"+getName()+"' has an invalid (less than 1) activation count value"));
		}		

		// [5] The priority value must be greater than, or equal to, zero. 
		if ( getPriority() < 0 ) {
			problems.add(new Problem(Problem.ERROR, "Task '"+getName()+"' has an invalid (negative) priority value"));
		}		

		// [6] If the autoStart value is true, then at least one appModes should be specified.
		if ( autostart == true && appModes.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Task '"+getName()+"' is autostarted, but has no application modes specified in which it should be started" ));
		}		
		
		// [7] If the timingProtection flag set to false then a timeFrame value other than 0 should not be specified. [AUTOSAR]
		if ( hasTimingProtection() == false && timeFrame.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Task '"+getName()+"' has timing protection off, but has a time frame value specified"));
		}
		
		// [8] If specified, the timeFrame value must be greater than, or equal to, zero. [AUTOSAR]
		if ( timeFrame.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Task '"+getName()+"' has an invalid (negative) time frame value"));
		}
		
		// [9] If the autoStart value is false, then no appModes should be specified.
		if ( autostart == false && appModes.isEmpty() == false ) {
			problems.add(new Problem(Problem.INFORMATION, "Task '"+getName()+"' has autostart application modes specified, when it is not autostarted" ));
		}			
	}	
	
	
	
	public Task(Cpu cpu,String name) {
		super(cpu,name);
	}

}
