package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-03-06 18:16:31 +0000 (Thu, 06 Mar 2008) $
 * $LastChangedRevision: 666 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetScheduleTable.java $
 * 
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


import com.jkenergy.rtos.config.osmodel.ScheduleTable;
import com.jkenergy.rtos.config.osmodel.ScheduleTableAction;

/**
 * Intermediate target element used to store information on schedule tables to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetScheduleTable extends TargetElement {
	
	/**
	 * Set of {@link TargetAppMode} instances in which this schedule table is auto-started
	 */
	private Collection<TargetAppMode> targetAppModes = new HashSet<TargetAppMode>();	
	
	/**
	 * List of {@link TargetScheduleTableXP} instances that represent expiry points of the table
	 */
	private List<TargetScheduleTableXP> expiryPoints = new ArrayList<TargetScheduleTableXP>();	
	
	
	/**
	 * The internal {@link TargetAlarm} that drives the schedule table
	 */
	private TargetAlarm internalAlarm = null;
	
	/**
	 * Periodic flag
	 */
	private boolean periodic=false;	
	
	/**
	 * autostarted flag
	 */
	private boolean autostarted = false;
	
	/**
	 * @return the periodic flag
	 */
	public boolean isPeriodic() {
		return periodic;
	}	
	
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
	 * @return Returns the targetAppModes.
	 */
	protected Collection<TargetAppMode> getTargetAppModes() {
		return targetAppModes;
	}

	/**
	 * @return the internalAlarm
	 */
	public TargetAlarm getInternalAlarm() {
		return internalAlarm;
	}

	
	/**
	 * @return the list of {@link TargetScheduleTableXP} instances (expiry points)
	 */
	public Collection<TargetScheduleTableXP> getExpiryPoints() {
		
		return expiryPoints;
	}	
	
	/**
	 * @return the first expiry point ({@link TargetScheduleTableXP}) owned by the table (if any)
	 */
	public TargetScheduleTableXP getFirstXP() {
		
		return expiryPoints.isEmpty() ? null : expiryPoints.iterator().next();
	}
	
	/**
	 * Associates the given TargetAppMode with the ScheduleTable.
	 * @param appMode the TargetAppMode to be associated with the ScheduleTable
	 */
	protected void addTargetAppMode(TargetAppMode appMode) {
		targetAppModes.add(appMode);
	}		
	
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		assert internalAlarm != null;	// an internal TargetAlarm must be set prior to initialisation
		
		ScheduleTable scheduleTable = getScheduleTable();
			
		if (scheduleTable != null) {
			// This TargetScheduleTable represents a ScheduleTable from the OS model
			
			targetAppModes = getAllTargetElements(scheduleTable.getAppModes());

			// Decide whether the timing attributes within the table are in nano-seconds and therefore need to be mapped
			TargetCounter targetCounter = (TargetCounter)getTargetElement(scheduleTable.getCounter());
			boolean bMapToTicks = (targetCounter != null && targetCounter.isNanoSecondCounter());
			
			////////////////////////////////////////////////////////////////////////////////////////////////
			// Create expiry points (XP) (TargetScheduleTableXP instances) for the ScheduleTableAction instances
			// owned by the OS ScheduleTable. Any ScheduleTableAction instances that have the same offset
			// are represented by a single expiry point, which has multiple actions.
			BigInteger currentOffset = BigInteger.ZERO;
			TargetScheduleTableXP currentXP = null;
							
			// iterate over ScheduleTableAction instances in offset order
			for (ScheduleTableAction action : scheduleTable.getOrderedActions()) {
				
				if ( currentXP == null || action.getOffset().equals(currentOffset) == false ) {

					// need to create a new expiry point, since next action has different offset
					// but first need to set the delta of the previous expiry point (if there was one)
					
					if ( currentXP == null ) {
						// this is the first XP from the OS model, so check whether it has an offset of 0
						if ( action.getOffset().equals(BigInteger.ZERO) == false ) {
							// does not have an offset of zero, so create a dummy XP at point 0
							TargetScheduleTableXP dummyXP = new TargetScheduleTableXP(this);
							
							expiryPoints.add(dummyXP);
							
							// Calculate delta value (to next XP). Okay to convert to long value since OS model constraints
							// ensure that this fits into a "long" type. See constraint [20] in ScheduleTable.java.
							long delta = action.getOffset().longValue();
							
							if (bMapToTicks) {
								// need to map from nano-seconds to ticks according to associated counter
								delta = targetCounter.getTicksFromNanoseconds(delta);
							}
							
							dummyXP.setDelta(delta);
						}
					}					
					else {
						// this is the first action with this current offset
						
						// calculate delta value. Okay to convert to long value once delta found, since OS model constraints
						// ensure that this fits into a "long" type. See constraint [21] in ScheduleTable.java. This ensures
						// the delta values are less of equal to the maxAllowedValue for the assigned counter (which is a long value).						
						long delta = action.getOffset().subtract(currentOffset).longValue();
						
						if (bMapToTicks) {
							// need to map from nano-seconds to ticks according to associated counter
							delta = targetCounter.getTicksFromNanoseconds(delta);
						}
						currentXP.setDelta(delta);				
					}

					
					currentXP = new TargetScheduleTableXP(this);					
					
					expiryPoints.add(currentXP);
					
					currentOffset = action.getOffset();
				}
				
				// Add the next action to be performed by the expiry point
				currentXP.addAction(action);
			}
			

			// The last expiry point must have a delta that identifies the number of ticks
			// remaining to the end of the table itself, rather than to the next expiry point
			if (currentXP != null) {
				long delta = scheduleTable.getLength().subtract(currentOffset).longValue();
				
				if (bMapToTicks) {
					// need to map from nano-seconds to ticks according to associated counter
					delta = targetCounter.getTicksFromNanoseconds(delta);
				}
				currentXP.setDelta(delta);
			}			


			//////////////////////////////////////////////////////////////////////////////////////////////
			// if the table is autostarted, then need to setup the auto-start expiry/cycle time within
			// the internal alarm (mapping to ticks if required).
			if (autostarted) {
				
				// the cycle time is the delta value extracted from the first expiry point in the table's list of points
				
				TargetScheduleTableXP firstXP = getFirstXP();
				
				if (firstXP != null) {
					long startOffset = scheduleTable.getAutostartOffset().longValue(); // this becomes the alarm expiry time					
					long cycleTime = firstXP.getDelta();	// this will already have been mapped to ticks if required
					
					if ( bMapToTicks ) {
						startOffset = targetCounter.getTicksFromNanoseconds(startOffset);
					}					
					
					internalAlarm.setAlarmTime(startOffset);
					internalAlarm.setCycleTime(cycleTime);
					internalAlarm.setAutostarted(true);
				}
			}
		}	
	}	
	
	/**
	 * @return Returns the OS Model ScheduleTable on which the TargetSheduleTable is based (if any)
	 */
	public ScheduleTable getScheduleTable() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof ScheduleTable;
		
		return (ScheduleTable)getOsModelElement();
	}	
	
	
	/**
	 * Standard Constructor that creates a TargetScheduleTable that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 * @param internalAlarm the internal TargetAlarm that drives the schedule table
	 */
	protected TargetScheduleTable(TargetCpu cpu, String name, TargetAlarm internalAlarm) {
		super(cpu, name);
		
		this.internalAlarm = internalAlarm;
		
		internalAlarm.setDrivenScheduleTable(this); // inform the alarm that it is driving this table
	}

	/**
	 * Copy contructor that creates a TargetScheduleTable that represents an OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 * @param internalAlarm the internal TargetAlarm that drives the schedule table
	 */
	protected TargetScheduleTable(TargetCpu cpu, ScheduleTable osModelElement, TargetAlarm internalAlarm) {
		
		super(cpu, osModelElement);
		
		this.internalAlarm = internalAlarm;
		
		internalAlarm.setDrivenScheduleTable(this);	// inform the alarm that it is driving this table
		
		// copy required info. from the given OSModelElement
		this.periodic = osModelElement.isPeriodic();
		this.autostarted = osModelElement.getAutostart();
	}
}
