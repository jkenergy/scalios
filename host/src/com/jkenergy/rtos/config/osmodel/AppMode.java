package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/AppMode.java $
 * 
 */

import java.util.*;
import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models an Application mode within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class AppMode extends OSModelElement {


	/**
	 * Set of Tasks that are started within this AppMode
	 */
	private Set<Task> tasks = new LinkedHashSet<Task>();	

	/**
	 * Set of Alarms that are started within this AppMode
	 */
	private Set<Alarm> alarms = new LinkedHashSet<Alarm>();
	
	/**
	 * Set of ScheduleTables that are started within this AppMode
	 */
	private Set<ScheduleTable> scheduleTables = new LinkedHashSet<ScheduleTable>();					/* $Req: AUTOSAR $ */
	
	/**
	 * Adds a Task that is started within this AppMode.
	 * This method creates a two way relationship.
	 * 
	 * @param task the Task to be added
	 */
	public void addTask(Task task) {
		
		if ( task!=null ) {
			if ( tasks.add(task) )
				task.addAppMode(this);	// inform task that it is started in this mode
		}
	}	
	
	/**
	 * Returns the collection of Tasks that are started in the AppMode
	 * @return Collection of Tasks
	 */
	public Collection<Task> getTasks() {
		return tasks;
	}	

	/**
	 * Adds an Alarm that is started within this AppMode.
	 * This method creates a two way relationship.
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addAlarm(Alarm alarm) {
		
		if ( alarm!=null ) {
			if ( alarms.add(alarm) )
				alarm.addAppMode(this);	// inform alarm that it is started in this mode
		}
	}
	
	/**
	 * Returns the collection of Alarms that are started in the AppMode
	 * @return Collection of Alarms
	 */
	public Collection<Alarm> getAlarms() {
		return alarms;
	}	
	
	/**
	 * Adds a ScheduleTable that is started within this AppMode.
	 * This method creates a two way relationship.
	 * 
	 * @param table the ScheduleTable to be added
	 */
	public void addScheduleTable(ScheduleTable table) {
		
		if ( table!=null ) {
			if ( scheduleTables.add(table) )
				table.addAppMode(this);	// inform ScheduleTable that it is started in this mode
		}
	}
	
	/**
	 * Returns the collection of ScheduleTable that are started in the AppMode
	 * @return Collection of ScheduleTable
	 */
	public Collection<ScheduleTable> getScheduleTables() {
		return scheduleTables;
	}		
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		// [1] Any tasks identified as being auto-started in the AppMode should have an autoStart value of true.
		for ( Task nextTask : tasks ) {
			if ( nextTask.getAutostart() == false ) {
				problems.add(new Problem(Problem.INFORMATION, "AppMode '"+getName()+"' contains task '"+nextTask.getName()+"' that is not autostarted"));
			}
		}
				
		// [2] Any alarms identified as being auto-started in the AppMode should have an autoStart value of true.
		for ( Alarm nextAlarm : alarms ) {
			if ( nextAlarm.getAutostart() == false ) {
				problems.add(new Problem(Problem.INFORMATION, "AppMode '"+getName()+"' contains alarm '"+nextAlarm.getName()+"' that is not autostarted"));
			}
		}
			
		// [3] Any schedule tables identified as being auto-started in the AppMode should have an autoStart value of true. [AUTOSAR]
		for ( ScheduleTable nextTable : scheduleTables ) {
			if ( nextTable.getAutostart() == false ) {
				problems.add(new Problem(Problem.INFORMATION, "AppMode '"+getName()+"' contains schedule table '"+nextTable.getName()+"' that is not autostarted"));
			}
		}
		
		// [4]  All name of the AppMode must be unique when compared to all comAppMode names and generated flag macro names.[error]
		Collection<String> usedNamesSet = new LinkedHashSet<String>();
		
		usedNamesSet.addAll(getCpu().getAllCOMAppModeNames());			// all COM AppMode names
		usedNamesSet.addAll(getCpu().getAllFlagMacroNames());			// all flag macro names
		
		if ( usedNamesSet.contains(getName()) ) {
			problems.add(new Problem(Problem.ERROR, "AppMode name '"+getName()+"' clashes with another name in the system"));
		}		
	}	

	
	public AppMode(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
