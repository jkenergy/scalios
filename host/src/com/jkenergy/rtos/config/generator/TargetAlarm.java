package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetAlarm.java $
 * 
 */


import java.util.Collection;
import java.util.HashSet;

import com.jkenergy.rtos.config.osmodel.Alarm;
import com.jkenergy.rtos.config.osmodel.ScheduleTable;

/**
 * Intermediate target element used to store information on alarms to be generated.

 * @author Mark Dixon
 *
 */

public class TargetAlarm extends TargetElement {

	/**
	 * The time when the alarm will first expire when autostarted
	 */
	private long alarmTime=0;
	
	/**
	 * The cyclic time of a cyclic alarm when autostarted
	 */	
	private long cycleTime=0;		
	
	/**
	 * Set of {@link TargetAppMode} instances in which this alarm is auto-started
	 */
	private Collection<TargetAppMode> targetAppModes = new HashSet<TargetAppMode>();	
	
	/**
	 * The {@link TargetCounter} that to which the {@link TargetAlarm} is bound
	 */
	TargetCounter targetCounter = null;	
	
	
	/**
	 * The {@link TargetExpiry} that determines the action to be performed on alarm expiry
	 */
	private TargetExpiry expiryAction = null;
	
	
	/**
	 * The {@link TargetScheduleTable} that this alarm drives (if any)
	 */
	private TargetScheduleTable drivenTable = null;
	
	/**
	 * autostarted flag
	 */
	private boolean autostarted = false;	
	
	
	/**
	 * @return the periodic flag
	 */
	public boolean isAutostarted() {
		return autostarted;
	}		
	
	/**
	 * @param autostarted the autostarted flag
	 */
	public void setAutostarted(boolean autostarted) {
		this.autostarted = autostarted;
	}	
	
	/**
	 * @return Returns the targetAppModes in which the alarm is auto-started
	 */
	protected Collection<TargetAppMode> getTargetAppModes() {
		return targetAppModes;
	}

	/**
	 * @return the targetCounter
	 */
	public TargetCounter getTargetCounter() {
		return targetCounter;
	}

	/**
	 * @return the alarmTime
	 */
	public long getAlarmTime() {
		return alarmTime;
	}

	/**
	 * @param alarmTime the alarmTime to set
	 */
	public void setAlarmTime(long alarmTime) {
		this.alarmTime = alarmTime;
	}	
	
	/**
	 * @return the cycleTime
	 */
	public long getCycleTime() {
		return cycleTime;
	}

	/**
	 * @param cycleTime the cycleTime to set
	 */
	public void setCycleTime(long cycleTime) {
		this.cycleTime = cycleTime;
	}

	/**
	 * @return the expiryAction
	 */
	public TargetExpiry getExpiryAction() {
		return expiryAction;
	}

	/**
	 * @return the driven Table if the alarm drives a {@link TargetScheduleTable}
	 */
	public TargetScheduleTable getDrivenScheduleTable() {
		return drivenTable;
	}
	
	/**
	 * Stes the ScheduleTable that is driven by this TargetAlarm
	 * @param drivenTable
	 */
	public void setDrivenScheduleTable(TargetScheduleTable drivenTable) {
		
		assert drivenTable.getInternalAlarm() == this; // TargetScheduleTable must be using this as its internal alarm
		
		this.drivenTable = drivenTable;
	}

	/**
	 * Associates the given TargetAppMode with the Alarm.
	 * @param appMode the TargetAppMode in which the alarm is auto-started
	 */
	protected void addTargetAppMode(TargetAppMode appMode) {
		targetAppModes.add(appMode);
	}	
	
	/**
	 * @return true if this is a singleton alarm (i.e. its counter is associated with exactly one alarm)
	 */
	public boolean isSingleton() {
		
		if (targetCounter != null) {
			return targetCounter.isSingleton();
		}
		
		return false;
	}	
	
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		if (drivenTable != null) {
			// This TargetAlarm drives a schedule table
			
			// Derive the TargetCounter and TargetAppMode information from the driven table
			
			ScheduleTable table = drivenTable.getScheduleTable();
			
			targetAppModes = getAllTargetElements(table.getAppModes());
			
			targetCounter = getTargetElement(table.getCounter());
			
			// create a TargetExpiry instance that drives the TargetScheduleTable 
			expiryAction = new TargetExpiry(drivenTable);
			
			// No need to map timing attributes if using a nano-second counter, since this work
			// is done by the driven table. See TargetScheduleTable.initialiseModelAssociations()
		}
		else {
		
			Alarm alarm = getAlarm();
			
			if (alarm != null) {
				// This TargetAlarm represents an Alarm from the OS model
				
				targetAppModes = getAllTargetElements(alarm.getAppModes());
				
				targetCounter = getTargetElement(alarm.getCounter());
				
				// create an appropriate TargetExpiry instance that handles the expiry
				if ( alarm.incrementsCounter() ) {
					expiryAction = new TargetExpiry(this.<TargetCounter>getTargetElement(alarm.getIncrementedCounter()));
				}
				else if ( alarm.callsHandler() ) {
					expiryAction = new TargetExpiry(alarm.getAlarmCallbackName());
				}
				else if ( alarm.setsEvent() ) {
					expiryAction = new TargetExpiry(this.<TargetEvent>getTargetElement(alarm.getEvent()), this.<TargetTask>getTargetElement(alarm.getTask()));
				}
				else if ( alarm.activatesTask() ) {
					expiryAction = new TargetExpiry(this.<TargetTask>getTargetElement(alarm.getTask()));
				}
				
				// If the TargetCounter is known and it is a nano-second counter, then the timing attributes need
				// to converted to ticks using the associated counter's device driver
				if (targetCounter != null && targetCounter.isNanoSecondCounter()) {
					
					alarmTime = targetCounter.getTicksFromNanoseconds(alarmTime);
					cycleTime = targetCounter.getTicksFromNanoseconds(cycleTime);
				}			
			}
		}
	}	
	
	/**
	 * @return Returns the OS Model Alarm on which the TargetAlarm is based (if any)
	 */
	public Alarm getAlarm() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Alarm;
		
		return (Alarm)getOsModelElement();
	}	
	
	
	/**
	 * Standard Constructor that creates a TargetAlarm that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetAlarm(TargetCpu cpu, String name) {
		super(cpu, name);
	}


	/**
	 * Copy contructor that creates a TargetAlarm that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetAlarm(TargetCpu cpu, Alarm osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		alarmTime = osModelElement.getAlarmTime();
		cycleTime = osModelElement.getCycleTime();
		autostarted = osModelElement.getAutostart();
	}	
}
