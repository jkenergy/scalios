package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetAppMode.java $
 * 
 */


import java.util.Collection;
import java.util.HashSet;

import com.jkenergy.rtos.config.osmodel.AppMode;


/**
 * Intermediate target element used to store information on AppModes to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetAppMode extends TargetElement {

	/**
	 * Set of TargetTask instances that are auto-started within the AppMode
	 */
	private Collection<TargetTask> targetTasks = new HashSet<TargetTask>();	

	/**
	 * Set of TargetAlarm instances that are auto-started within the AppMode
	 */
	private Collection<TargetAlarm> targetAlarms = new HashSet<TargetAlarm>();	
	
	/**
	 * Set of TargetScheduleTable instances that are auto-started within the AppMode
	 */
	private Collection<TargetScheduleTable> targetScheduleTables = new HashSet<TargetScheduleTable>();		

	/**
	 * @return Returns the auto-started targetTasks.
	 */
	protected Collection<TargetTask> getTargetTasks() {
		return targetTasks;
	}

	/**
	 * Associates the given TargetTask with the app mode.
	 * @param task the TargetTask to be auto-started within the app mode
	 */
	protected void addTargetTask(TargetTask task) {
		targetTasks.add(task);
	}
	
	/**
	 * @return Returns the auto-started targetAlarms.
	 */
	protected Collection<TargetAlarm> getTargetAlarms() {
		return targetAlarms;
	}

	/**
	 * @return Returns the auto-started targetAlarms that are not singleton alarms.
	 */
	protected Collection<TargetAlarm>	getTargetNonSingletonAlarms() {
		
		Collection<TargetAlarm> alarms = new HashSet<TargetAlarm>();
		
		for (TargetAlarm next : targetAlarms) {
			if ( !next.isSingleton() ) {
				alarms.add(next);
			}
		}
		
		return alarms;
	}
	
	/**
	 * @return Returns the auto-started targetAlarms that are singleton alarms.
	 */
	protected Collection<TargetAlarm>	getTargetSingletonAlarms() {
		
		Collection<TargetAlarm> alarms = new HashSet<TargetAlarm>();
		
		for (TargetAlarm next : targetAlarms) {
			if ( next.isSingleton() ) {
				alarms.add(next);
			}
		}
		
		return alarms;
	}
	
	
	/**
	 * Associates the given TargetAlarm with the app mode.
	 * @param alarm the TargetAlarm to be auto-started within the app mode
	 */
	protected void addTargetAlarm(TargetAlarm alarm) {
		targetAlarms.add(alarm);
	}	
	
	/**
	 * @return Returns the auto-started targetScheduleTables.
	 */
	protected Collection<TargetScheduleTable> getTargetScheduleTables() {
		return targetScheduleTables;
	}

	/**
	 * Associates the given TargetScheduleTable with the app mode.
	 * @param scheduleTable the TargetScheduleTable to be auto-started within the app mode
	 */
	protected void addTargetScheduleTable(TargetScheduleTable scheduleTable) {
		targetScheduleTables.add(scheduleTable);
	}	
	
	/**
	 * @return Returns the auto-started targetTasks plus the idle task
	 */
	protected Collection<TargetTask> getTargetTasksWithIdle() {
		
		Collection<TargetTask> targetTasksWithIdle = new HashSet<TargetTask>(targetTasks);	
		
		targetTasksWithIdle.add(getTargetCpu().getIdleTask());
		
		return targetTasksWithIdle;
	}
		
	/**
	 * @return The highest priority TargetTask that is auto-started by the appmode (idle task if no autostarted tasks)
	 */	
	protected TargetTask getHighestPriorityTask() {
		
		if (targetTasks.size() > 0) {
			return (TargetTask)TargetPriorities.getHighestTargetPriorityRunnable(targetTasks);
		}
		else {
			return getTargetCpu().getIdleTask();
		}
	}
	
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		AppMode appMode = getAppMode();
		
		if (appMode != null) {
			
			targetTasks = getAllTargetElements(appMode.getTasks());
			
			targetAlarms = getAllTargetElements(appMode.getAlarms());
			
			targetScheduleTables = getAllTargetElements(appMode.getScheduleTables());			
		}
	}	
	
	/**
	 * @return Returns the OS Model AppMode on which the TargetAppMode is based (if any)
	 */
	public AppMode getAppMode() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof AppMode;
		
		return (AppMode)getOsModelElement();
	}	
	
	/**
	 * Standard Constructor that creates a TargetAppMode that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetAppMode(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetAppMode that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetAppMode(TargetCpu cpu, AppMode osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
	}	
}
