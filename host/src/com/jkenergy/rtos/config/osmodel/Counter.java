package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Counter.java $
 * 
 */

import java.util.*;
import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models a Counter within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Counter extends OSModelElement {


	/**
	 * The maximum allowed counter value 
	 */
	private long maxAllowedValue=0; 
	
	/**
	 * Number of ticks required to reach a counter unit 
	 */
	private long ticksPerBase=0;
	
	/**
	 * The minimum allowed number of counter ticks for a cyclic alarm linked to the counter
	 *
	 */
	private long minCycle=0;
	
	
	/**
	 * The options of the device associated with the Counter
	 * This is a comma separated list of name=value pairs
	 * e.g. "prescaler = X2, cntsource = CLK1"
	 */
	private String deviceOptions=null;	
	
	
	/**
	 * Set of Alarms that are assigned to the counter
	 */
	private Set<Alarm> alarms = new LinkedHashSet<Alarm>();		
	
	/**
	 * Set of ScheduleTables that are assigned to the counter
	 */
	private Set<ScheduleTable> scheduleTables = new LinkedHashSet<ScheduleTable>();								/* $Req: AUTOSAR $ */
	
	/**
	 * The type of the Counter (hardware or software)
	 * @see CounterTypeKind
	 */ 
	private CounterTypeKind counterType=CounterTypeKind.SOFTWARE_LITERAL;	/* $Req: AUTOSAR $ */	

	/**
	 * The units of counter (ticks or nanoseconds).
	 * @see CounterUnitKind
	 */ 
	private CounterUnitKind counterUnit=CounterUnitKind.TICKS_LITERAL;		/* $Req: AUTOSAR $ */	
	
	/**
	 * Set of Alarms that increment the counter
	 */
	private Set<Alarm> incrementingAlarms = new LinkedHashSet<Alarm>();							/* $Req: AUTOSAR $ */	
	
	/**
	 * Set of ScheduleTableActions that increment the counter
	 */
	private Set<ScheduleTableAction> incrementingActions = new LinkedHashSet<ScheduleTableAction>();	/* $Req: EXTENSION $ */	
	
	
	/**
	 * @return the counterType of the Counter
	 */
	public CounterTypeKind getCounterType() {
		return counterType;
	}

	/**
	 * @param newCounterType the new counterType of the Counter
	 */
	public void setCounterType(CounterTypeKind newCounterType) {
		counterType = newCounterType;
	}
	
	/**
	 * @return true if the Counter is a software counter
	 */
	public boolean isSoftwareCounter() {
		return CounterTypeKind.SOFTWARE_LITERAL == counterType;
	}
	
	/**
	 * @return true if the Counter is a hardware counter
	 */
	public boolean isHardwareCounter() {
		return CounterTypeKind.HARDWARE_LITERAL == counterType;
	}	
	
	/**
	 * @return the counterUnit of the Counter
	 */
	public CounterUnitKind getCounterUnit() {
		return counterUnit;
	}

	/**
	 * @param newCounterUnit the new counterUnit of the Counter
	 */
	public void setCounterUnit(CounterUnitKind newCounterUnit) {
		counterUnit = newCounterUnit;
	}
	
	/**
	 * @return true if the Counter is a tick based counter
	 */
	public boolean isTickCounter() {
		return CounterUnitKind.TICKS_LITERAL == counterUnit;
	}
	
	/**
	 * @return true if the Counter is a nano-second based counter
	 */
	public boolean isNanoSecondCounter() {
		return CounterUnitKind.NANOSECONDS_LITERAL == counterUnit;
	}	
	
	/**
	 * Sets the max allowable value for the counter
	 * @param newMaxAllowableValue
	 */
	public void setMaxAllowedValue(long newMaxAllowableValue) {

		
		maxAllowedValue=newMaxAllowableValue;
	}
	
	/**
	 * 
	 * @return maxAllowableValue
	 */
	public long getMaxAllowedValue() {
		return maxAllowedValue;
	}

	/**
	 * Sets the ticks per base value for the counter
	 * @param newTicksPerBase
	 */
	public void setTicksPerBase(long newTicksPerBase) {

		ticksPerBase=newTicksPerBase;
	}
	
	/**
	 * 
	 * @return ticksPerBase value
	 */
	public long getTicksPerBase() {
		return ticksPerBase;
	}	

	/**
	 * Sets the minimum cycle value for the counter
	 * @param newMinCycle
	 */
	public void setMinCycle(long newMinCycle) {
		
		minCycle=newMinCycle;
	}
	
	/**
	 * 
	 * @return minCycle value
	 */
	public long getMinCycle() {
		return minCycle;
	}	
	
	/**
	 * Adds an Alarm to the collection of alarms that are assigned to the counter.
	 * This method creates a two way relationship.
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addAlarm(Alarm alarm) {
		
		if ( alarm!=null ) {
			if ( alarms.add(alarm) )
				alarm.setCounter(this);	// inform alarm that it is assigned to this counter
		}
	}
	
	/**
	 * Returns the Alarms that are assigned to the counter
	 * @return Collection of Alarms
	 */
	public Collection<Alarm> getAlarms() {
		return alarms;
	}	
	
	

	
	/**
	 * Adds a ScheduleTable to the collection of ScheduleTables that are assigned to the counter.
	 * This method creates a two way relationship.
	 * 
	 * @param table the ScheduleTable to be added
	 */
	public void addScheduleTable(ScheduleTable table) {
		
		if ( table!=null ) {
			if ( scheduleTables.add(table) )
				table.setCounter(this);	// inform ScheduleTable that it is assigned to this counter
		}
	}
	
	/**
	 * Returns the ScheduleTables that are assigned to the counter
	 * @return Collection of ScheduleTables
	 */
	public Collection<ScheduleTable> getScheduleTables() {
		return scheduleTables;
	}		
	
	/**
	 * Returns the transitive closure of all Alarms that are assigned (driven) by the counter.
	 * The set is constructed by adding all directly assigned alarms, plus recursively adding all alarms
	 * that are assigned to counters incremented by those alarms.
	 *  
	 * @return Collection of all Alarms either directly or indirectly driven by the counter
	 */
	public Collection<Alarm> getAllAlarms() {
		
		Collection<Alarm> allAlarms = new HashSet<Alarm>();
		
		Collection<Counter> currentCounters = new HashSet<Counter>();
		Collection<Counter> nextCounters = new HashSet<Counter>();
		
		// Avoid use of recursive function calls by building internal set of counters to be processed
		currentCounters.add(this);
			
		while ( currentCounters.isEmpty() == false ) {
			
			for ( Counter nextCounter : currentCounters ) {
				
				for ( Alarm nextAlarm : nextCounter.getAlarms() ) {
					
					if ( allAlarms.add(nextAlarm) == true ) {
						// Alarm not yet visited (avoiding infinite recursion), so see if it increments a counter
						Counter incrementedCounter = nextAlarm.getIncrementedCounter();
						
						if ( incrementedCounter != null ) {
							// a counter in incremented, so add to next set of coutners to be processed
							nextCounters.add(incrementedCounter);
						}
					}
				}
			}
			
			currentCounters = nextCounters;			// get next set of counters to be processed
			nextCounters = new HashSet<Counter>();	// clear next set of counters, ready for next iteration
		}
		
		return allAlarms;
	}	
	
	
	/**
	 * Returns the transitive closure of all ScheduleTables that are assigned (driven) by the counter.
	 * The set is constructed by adding all directly assigned ScheduleTables, plus recursively adding all tables
	 * that are assigned to counters incremented by actions of those ScheduleTables.
	 *  
	 * @return Collection of all ScheduleTables either directly or indirectly driven by the counter
	 */
	public Collection<ScheduleTable> getAllScheduleTables() {
		
		Collection<ScheduleTable> allTables = new HashSet<ScheduleTable>();
		
		Collection<Counter> currentCounters = new HashSet<Counter>();
		Collection<Counter> nextCounters = new HashSet<Counter>();
		
		// Avoid use of recursive function calls by building internal set of counters to be processed
		currentCounters.add(this);
			
		while ( currentCounters.isEmpty() == false ) {
			
			for ( Counter nextCounter : currentCounters ) {
				
				for ( ScheduleTable nextTable: nextCounter.getScheduleTables() ) {
					
					if ( allTables.add(nextTable) == true ) {
						// ScheduleTable not yet visited (avoiding infinite recursion), so get all incremented counters
						nextCounters.addAll(nextTable.getAllIncrementedCounters());
					}
				}
			}
			
			currentCounters = nextCounters;			// get next set of counters to be processed
			nextCounters = new HashSet<Counter>();	// clear next set of counters, ready for next iteration
		}
		
		return allTables;
	}		
	
	/**
	 * Returns the transitive closure of all ScheduleTables and Alarms that are assigned (driven) by the counter.
	 * The set is constructed by adding all directly assigned ScheduleTables and Alarms, plus recursively adding all tables
	 * and alarm that are assigned to counters incremented by actions of those ScheduleTables, or counters incremented by the alarms.
	 *  
	 * @return Collection of all ScheduleTables and Alarms either directly or indirectly driven by the counter
	 */
	public Collection<OSModelElement> getAllIncrementers() {
		
		Collection<OSModelElement> allElements = new HashSet<OSModelElement>();
		
		Collection<Counter> currentCounters = new HashSet<Counter>();
		Collection<Counter> nextCounters = new HashSet<Counter>();
		
		// Avoid use of recursive function calls by building internal set of counters to be processed
		currentCounters.add(this);
			
		while ( currentCounters.isEmpty() == false ) {
			
			for ( Counter nextCounter : currentCounters ) {
				
				for ( ScheduleTable nextTable: nextCounter.getScheduleTables() ) {
					
					if ( allElements.add(nextTable) == true ) {
						// ScheduleTable not yet visited (avoiding infinite recursion), so get all incremented counters
						nextCounters.addAll(nextTable.getAllIncrementedCounters());
					}
				}
				
				for ( Alarm nextAlarm : nextCounter.getAlarms() ) {
					if ( allElements.add(nextAlarm) == true ) {
						// Alarm not yet visited (avoiding infinite recursion), so see if it increments a counter
						Counter incrementedCounter = nextAlarm.getIncrementedCounter();
						
						if ( incrementedCounter != null ) {
							// a counter in incremented, so add to next set of coutners to be processed
							nextCounters.add(incrementedCounter);
						}
					}
				}
			}
			
			currentCounters = nextCounters;			// get next set of counters to be processed
			nextCounters = new HashSet<Counter>();	// clear next set of counters, ready for next iteration
		}
		
		return allElements;
	}	
	
	/**
	 * Adds an Alarm to the collection of alarms that increment the counter.
	 * This method creates a two way relationship.
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addIncrementingAlarm(Alarm alarm) {
		
		if ( alarm!=null ) {
			if ( incrementingAlarms.add(alarm) )
				alarm.setIncrementedCounter(this);	// inform alarm that it increments this counter
		}
	}	
	
	/**
	 * Returns the Alarms that increment the counter when they expire
	 * @return Collection of Alarms that increment the counter
	 */
	public Collection<Alarm> getIncrementingAlarms() {
		return incrementingAlarms;
	}	
	
	/**
	 * Adds a ScheduleTableAction to the collection of actions that increment the counter.
	 * This method creates a two way relationship.
	 * 
	 * @param action the ScheduleTableAction to be added
	 */
	public void addIncrementingAction(ScheduleTableAction action) {
		
		if ( action!=null ) {
			if ( incrementingActions.add(action) )
				action.setIncrementedCounter(this);	// inform action that it increments this counter
		}
	}	
	
	/**
	 * Returns the ScheduleTableAction that increment the counter when they expire
	 * @return Collection of ScheduleTableAction that increment the counter
	 */
	public Collection<ScheduleTableAction> getIncrementingActions() {
		return incrementingActions;
	}
	

	/**
	 * @return the deviceOptions
	 */
	public String getDeviceOptions() {
		return deviceOptions;
	}

	/**
	 * @param deviceOptions the deviceOptions to set
	 */
	public void setDeviceOptions(String deviceOptions) {
		
		this.deviceOptions = deviceOptions;
	}	

	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		//[1] The maxAllowedValue must be greater than, or equal to, zero. 
		if ( maxAllowedValue < 0 ) {	
			problems.add(new Problem(Problem.ERROR, "Counter '"+getName()+"' has an invalid (negative) maximum allowed value"));			
		}		
		
		//[2] The minCycle value must be greater than, or equal to, zero. 
		if ( minCycle < 0 ) {	
			problems.add(new Problem(Problem.ERROR, "Counter '"+getName()+"' has an invalid (negative) minimum cycle value"));			
		}
			
		//[3] The ticksPerBase value must be greater than, or equal to, zero. 
		if ( ticksPerBase < 0 ) {	
			problems.add(new Problem(Problem.ERROR, "Counter '"+getName()+"' has an invalid (negative) ticks per base value"));			
		}
			
		//[4] The maxAllowedValue of a counter must be greater than the minCycle value. 
		if ( maxAllowedValue <= minCycle ) {
			problems.add(new Problem(Problem.ERROR, "Counter '"+getName()+"' has a minimum cycle value that is greater than the maximum allowed value"));
		}

		//[5] A counter should drive at least one associated Alarm or ScheduleTable.
		if ( alarms.isEmpty() && scheduleTables.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "Counter '"+getName()+"' does not drive any alarms or schedule tables"));			
		}
		
		// Constraints 6-9 moved since deviceName no longer present within the OS Counter. For original constraint code see revision 465.
		
	}		
	
	
	public Counter(Cpu cpu,String name) {
		super(cpu,name);
	}


}