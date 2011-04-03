package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Application.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;


/**
 * A SubClass of OSModelElement that models an Application within the OS.<br><br>
 *  
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 *
 * 
 */
public class Application extends OSModelElement {

	
	/**
	 * Trusted flag	(OS application that is executed in priveliged mode)
	 */
	private boolean trusted=false;							/* $Req: AUTOSAR $ */
	
	/**
	 * Collection of trusted function names
	 */
	private Set<String> trustedFunctions = new LinkedHashSet<String>();			/* $Req: AUTOSAR $ */
	
	
	/**
	 * OS-Application specific Startup Hook flag
	 */
	private boolean startupHook=false;						/* $Req: AUTOSAR $ */
	
	/**
	 * OS-Application specific Error hook flag
	 */
	private boolean errorHook=false;						/* $Req: AUTOSAR $ */
	
	/**
	 * OS-Application specific Shutdown hook flag
	 */
	private boolean shutdownHook=false;						/* $Req: AUTOSAR $ */
	
	/**
	 * The Task that is restarted when the Application is restarted
	 */
	private Task restartedTask = null;						/* $Req: AUTOSAR $ */	
		
	/**
	 * The Collection of OS Elements that are assigned to the Application
	 */
	private Set<OSModelElement> assignedElements = new LinkedHashSet<OSModelElement>();			/* $Req: AUTOSAR $ */
	
	/**
	 * The Collection of OS Elements that are accessible from the Application
	 */
	private Set<OSModelElement> accessibleElements = new LinkedHashSet<OSModelElement>();			/* $Req: AUTOSAR $ */	


	/**
	 * @return the trusted flag
	 */
	public boolean isTrusted() {
		return trusted;
	}

	/**
	 * @param trusted the trusted flag value to set
	 */
	public void setTrusted(boolean trusted) {
		this.trusted = trusted;
	}
	
	/**
	 * Adds the given function name to the collection fo trusted functions.
	 * 
	 * @param functionName the name of the trusted function to add
	 */
	public void addTrustedFunction(String functionName) {
		
		trustedFunctions.add(functionName);
	}
	
	/**
	 * 
	 * @return the collection of trusted function names.
	 */
	public Collection<String> getTrustedFunctions() {
	
		return trustedFunctions;
	}

	/**
	 * 
	 * @param newStartupHook enable flag
	 */
	public void setStartupHook(boolean newStartupHook) {
		startupHook=newStartupHook;
	}
	
	/**
	 * 
	 * @return startupHook flag 
	 */
	public boolean getStartupHook() {
		return startupHook;
	}
	
	/**
	 * 
	 * @param newErrorHook enable flag
	 */
	public void setErrorHook(boolean newErrorHook) {
		errorHook=newErrorHook;
	}
	
	/**
	 * 
	 * @return errorHook flag
	 */
	public boolean getErrorHook() {
		return errorHook;
	}
	
	/**
	 * 
	 * @param newShutdownHook enable flag
	 */
	public void setShutdownHook(boolean newShutdownHook) {
		shutdownHook=newShutdownHook;
	}
	
	/**
	 * 
	 * @return shutdownHook flag
	 */
	public boolean getShutdownHook() {
		return shutdownHook;
	}	
	
	
	/**
	 * Sets the task that is be restarted by the Application
	 * This method creates a two way relationship.
	 * 
	 * @param newRestartedTask the Task to be restarted by the Application
	 */
	public void setRestartedTask(Task newRestartedTask) {
		
		if ( restartedTask != newRestartedTask ) {
			
			restartedTask = newRestartedTask;
					
			if ( restartedTask != null ) {
				restartedTask.addRestartingApplication(this);
			}
		}
	}

	/**
	 * @return the Task to be restarted by the Application (may be null)
	 */
	public Task getRestartedTask() {
		return restartedTask;
	}	
	
	/**
	 * Adds an OSModelElement that is assigned to the Application
	 * This method creates a two way relationship.
	 * 
	 * @param element the OSModelElement to be added
	 */
	public void addAssignedElement(OSModelElement element) {
		if ( element!=null ) {
			if ( assignedElements.add(element) ) {
				element.addOwningApplication(this);	// inform the OS Model Element that it is assigned to this Application
			}
		}		
	}
	
	/**
 
	 * @return the Collection of elements that are assigned to the Application 
	 */
	public Collection<OSModelElement> getAssignedElements() {
		return assignedElements;
	}
	
	/**
	 * Helper function that filters the collection of elements that are assigned to the Application.
	 * 
	 * @param classType the type of the OSModelElement class that is required
	 * @return the Collection of elements of the specified type that are assigned to the Application 
	 */
	@SuppressWarnings("unchecked")
	private <E extends OSModelElement> Collection<E> getAssignedElementType(Class<? extends OSModelElement> classType) {
		
		Collection<E> result = new LinkedHashSet<E>();
		
		for (OSModelElement next : assignedElements) {
			
			if ( next.getClass() == classType ) {
				result.add((E)next);		// can cast next to E since we have just confirmed the class
			}
		}
		
		return result;
	}
	
	/**
	 * 
	 * @return the Collection of Task elements that are assigned to the Application
	 */
	public Collection<Task> getAssignedTasks() {
		
		return getAssignedElementType(Task.class);
	}
	

	/**
	 * 
	 * @return the Collection of ISR elements that are assigned to the Application
	 */
	public Collection<Isr> getAssignedISRs() {
		return getAssignedElementType(Isr.class);
	}	
	
	
	/**
	 * 
	 * @return the Collection of Alarm elements that are assigned to the Application
	 */
	public Collection<Alarm> getAssignedAlarms() {
		return getAssignedElementType(Alarm.class);
	}	

	
	/**
	 * 
	 * @return the Collection of ScheduleTable elements that are assigned to the Application
	 */
	public Collection<ScheduleTable> getAssignedScheduleTables() {
		return getAssignedElementType(ScheduleTable.class);
	}		
	

	/**
	 * 
	 * @return the Collection of Counter elements that are assigned to the Application
	 */
	public Collection<Counter> getAssignedCounters() {
		return getAssignedElementType(Counter.class);
	}	
	

	/**
	 * 
	 * @return the Collection of Resource elements that are assigned to the Application
	 */
	public Collection<Resource> getAssignedResources() {
		return getAssignedElementType(Resource.class);
	}	
	

	/**
	 * 
	 * @return the Collection of Message elements that are assigned to the Application
	 */
	public Collection<Message> getAssignedMessages() {
		return getAssignedElementType(Message.class);
	}
	
	/**
	 * Adds an element that is accessible from the Application
	 * This method creates a two way relationship.
	 * 
	 * @param element the OSModelElement to be added
	 */
	public void addAccessibleElement(OSModelElement element) {
		if ( element!=null ) {
			if ( accessibleElements.add(element) )
				element.addAccessingApplication(this);	// inform the Element is may be accessed bu this Application
		}		
	}	
	
	/**
	 * 
	 * @return the Collection of OSModelElement that are accessible from the Application
	 */
	public Collection<OSModelElement> getAccessibleElements() {
		return accessibleElements;
	}
	
	/**
	 * Helper function that filters the collection of elements that are accessible from the Application.
	 * 
	 * @param classType the type of the OSModelElement class that is required
	 * @return the Collection of elements of the specified type that are accessible to the Application 
	 */
	@SuppressWarnings("unchecked")
	private <E extends OSModelElement> Collection<E> getAccessibleElementType(Class<? extends OSModelElement> classType) {
		
		Collection<E> result = new LinkedHashSet<E>();
		
		for (OSModelElement next : accessibleElements) {
			
			if ( next.getClass() == classType ) {
				result.add((E)next);	// can cast next to E since we have just confirmed the class
			}
		}
		
		return result;
	}	
	
	/**
	 * 
	 * @return the Collection of Task elements that are accessible from the Application
	 */
	public Collection<Task> getAccessibleTasks() {
		
		return getAccessibleElementType(Task.class);
	}
	
	/**
	 * 
	 * @return the Collection of Counter elements that are accessible from the Application
	 */
	public Collection<Counter> getAccessibleCounters() {
		
		return getAccessibleElementType(Counter.class);
	}	
	
	/**
	 * 
	 * @return the Collection of Alarm elements that are accessible from the Application
	 */
	public Collection<Alarm> getAccessibleAlarms() {
		
		return getAccessibleElementType(Alarm.class);
	}
	
	/**
	 * 
	 * @return the Collection of Resource elements that are accessible from the Application
	 */
	public Collection<Resource> getAccessibleResources() {
		
		return getAccessibleElementType(Resource.class);
	}
	
	/**
	 * 
	 * @return the Collection of Message elements that are accessible from the Application
	 */
	public Collection<Message> getAccessibleMessages() {
		
		return getAccessibleElementType(Message.class);
	}	
	
	/**
	 * 
	 * @return the Collection of ScheduleTable elements that are accessible from the Application
	 */
	public Collection<ScheduleTable> getAccessibleScheduleTables() {
		
		return getAccessibleElementType(ScheduleTable.class);
	}	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		

		//[1] Only Tasks, ISRs, Alarms, Schedule Tables, Counters, Resources and Messages may be assignedElements of an application.[AUTOSAR]	
		for ( OSModelElement next : assignedElements ) {
			
			if ( (next instanceof Task == false) &&
				 (next instanceof Isr == false) &&
				 (next instanceof Alarm == false) &&
				 (next instanceof ScheduleTable == false) &&
				 (next instanceof Counter == false) &&
				 (next instanceof Resource == false) &&
				 (next instanceof Message == false)
			) {
				
					problems.add(new Problem(Problem.ERROR, "Application '"+getName()+"' may not own '"+next.getClassName()+"' type objects"));
			}
		}
		
		
		//[2] Only Tasks, Counters, Alarms, Resources, Messages, and Schedule Tables may be accessibleElements of an application.[AUTOSAR]
		for ( OSModelElement next : accessibleElements ) {
			
			if ( (next instanceof Task == false) &&
				 (next instanceof Alarm == false) &&
				 (next instanceof ScheduleTable == false) &&
				 (next instanceof Counter == false) &&
				 (next instanceof Resource == false) &&
				 (next instanceof Message == false)
			) {
				
					problems.add(new Problem(Problem.ERROR, "Application '"+getName()+"' may not access '"+next.getClassName()+"' type objects"));
			}
		}
		
		
		//[3] If present, the restartedTask must refer to a Task that is owned by the application.[AUTOSAR]
		if ( restartedTask != null ) {
			
			if ( assignedElements.contains(restartedTask) == false ) {
				problems.add(new Problem(Problem.ERROR, "Application '"+getName()+"' does not own the restarted task '"+restartedTask.getName()+"'"));			
			}
		}		
	}
	
	
	public Application(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
