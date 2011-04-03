package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Cpu.java $
 * 
 */

import java.util.*;
import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that is used to store information on ALL operating system configuration requirements.<br><br>
 * 
 * An instance of this class always acts as the "root" of an OS Model. Hence, this class defines
 * references to other {@link OSModelElement} derived classes such as {@link Task}, {@link Event} and
 * {@link Resource} etc.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Cpu extends OSModelElement {
	
	/**
	 * The single Com instance maintained by the Cpu
	 */
	private Com com=null;
	
	/**
	 * The single Os instance maintained by the Cpu
	 */	
	private Os os=null;
	
	/**
	 * The single Nm instance maintained by the Cpu
	 */	
	private Nm nm=null;
	
	/**
	 * The map of that contains Event instances: maps from String(name)->Event
	 */
	private Map<String, Event> events=new LinkedHashMap<String, Event>();
	
	/**
	 * The map of that contains Counter instances: maps from String(name)->Counter
	 */	
	private Map<String, Counter> counters=new LinkedHashMap<String, Counter>();

	/**
	 * The map of that contains Alarm instances: maps from String(name)->Alarm
	 */	
	private Map<String, Alarm> alarms=new LinkedHashMap<String, Alarm>();
	
	/**
	 * The map of that contains Task instances: maps from String(name)->Task
	 */	
	private Map<String, Task> tasks=new LinkedHashMap<String, Task>();
	
	/**
	 * The map of that contains Isr instances: maps from String(name)->Isr
	 */	
	private Map<String, Isr> isrs=new LinkedHashMap<String, Isr>();
	
	/**
	 * The map of that contains Resource instances: maps from String(name)->Resource
	 */	
	private Map<String, Resource> resources=new LinkedHashMap<String, Resource>();
	
	/**
	 * The map of that contains AppMode instances: maps from String(name)->AppMode
	 */	
	private Map<String, AppMode> appModes=new LinkedHashMap<String, AppMode>();
	
	/**
	 * The map of that contains Message instances: maps from String(name)->Message
	 */	
	private Map<String, Message> messages=new LinkedHashMap<String, Message>();
	
	/**
	 * The map of that contains ScheduleTable instances: maps from String(name)->ScheduleTable
	 */	
	private Map<String, ScheduleTable> scheduleTables=new LinkedHashMap<String, ScheduleTable>();		/* $Req: AUTOSAR $ */
	
	/**
	 * The map of that contains Application instances: maps from String(name)->Application
	 */	
	private Map<String, Application> applications=new LinkedHashMap<String, Application>();		/* $Req: AUTOSAR $ */
	
	/**
	 * Creates and sets the Com instance maintained by this Cpu
	 * 
	 * @param name the name of the Com object to create and set
	 * @return the created Com instance
	 */
	public Com createCom(String name) {
		
		com=new Com(this,name);
		
		return com;
	}	
	
	/**
	 * Sets the Com instance maintained by this Cpu
	 * 
	 * @param newCom the new Com to be maintained by this Cpu
	 */
	public void setCom(Com newCom) {
		
		com=newCom;
	}
	
	/**
	 * 
	 * @return The Com instance that is maintained by this Cpu
	 */
	public Com getCom() {
		return com;
	}

	/**
	 * Returns the Com maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Com to return
	 * @return Com that has the given name, null if no Com exists with the given name.
	 */
	public Com getNamedCom(String name) {
		
		if ( com!=null && com.getName().equals(name) )
			return com;
		
		return null;
	}	
	
	/**
	 * Creates and sets the Os instance maintained by this Cpu
	 * 
	 * @param name the name of the Os object to create and set
	 * @return the created Os instance
	 */
	public Os createOs(String name) {
		
		os = new Os(this,name);
		
		return os;
	}
	
	/**
	 * Sets the Os instance maintained by this Cpu
	 * 
	 * @param newOs the new Os to be maintained by this Cpu
	 */
	public void setOs(Os newOs) {
		
		os=newOs;
	}
	
	/**
	 * 
	 * @return The Os instance that is maintained by this Cpu
	 */
	public Os getOs() {
		return os;
	}
	
	/**
	 * Returns the Os maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Os to return
	 * @return Os that has the given name, null if no Os exists with the given name.
	 */
	public Os getNamedOs(String name) {
		
		if ( os!=null && os.getName().equals(name) )
			return os;
		
		return null;
	}	
	
	/**
	 * Creates and sets the Nm instance maintained by this Cpu
	 * 
	 * @param name the name of the Nm object to create and set
	 * @return the created Nm instance
	 */
	public Nm createNm(String name) {
		
		nm = new Nm(this,name);
		
		return nm;
	}	
	
	/**
	 * Sets the Nm instance maintained by this Cpu
	 * 
	 * @param newNm the new Nm to be maintained by this Cpu
	 */
	public void setNm(Nm newNm) {
		
		nm=newNm;
	}
	
	/**
	 * 
	 * @return The Nm instance that is maintained by this Cpu
	 */
	public Nm getNm() {
		return nm;
	}	
	
	/**
	 * Returns the Nm maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Nm to return
	 * @return Nm that has the given name, null if no Nm exists with the given name.
	 */
	public Nm getNamedNm(String name) {
		
		if ( nm!=null && nm.getName().equals(name) )
			return nm;
		
		return null;
	}	
	
	/**
	 * Creates and adds an Event instance maintained by this Cpu
	 * 
	 * @param name the name of the Event to create and add
	 * @return the Event instance created
	 */
	public Event createEvent(String name) {
		
		Event event = new Event(this,name);
		
		events.put(name, event);
		
		return event;
	}	
	
	/**
	 * Adds an Event to the collection maintained by this Cpu
	 * 
	 * @param event the Event to be added
	 */
	public void addEvent(Event event) {
		
		events.put(event.getName(),event);
	}
	
	/**
	 * Returns the collection of Events maintained by this Cpu
	 * @return Collection of Events
	 */
	public Collection<Event> getEvents() {
		return events.values();
	}
	
	/**
	 * Returns the Events maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Event to return
	 * @return Event that has the given name, null if no Event exists with the given name.
	 */
	public Event getNamedEvent(String name) {
		return (Event)events.get(name);
	}
	
	/**
	 * Creates and adds a Counter instance maintained by this Cpu
	 * 
	 * @param name the name of the Counter to be created and added to the CPU.
	 */
	public Counter createCounter(String name) {
		
		Counter counter = new Counter(this,name);
			
		counters.put(name, counter);
		
		return counter;
	}		
	
	/**
	 * Adds a Counter to the collection maintained by this Cpu
	 * 
	 * @param counter the Counter to be added
	 *
	 */
	public void addCounter(Counter counter) {
		counters.put(counter.getName(),counter);
	}
	
	/**
	 * Returns the collection of Counters maintained by this Cpu
	 * @return Collection of Counters
	 */
	public Collection<Counter> getCounters() {
		return counters.values();
	}
	
	/**
	 * Returns the Counter maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Counter to return
	 * @return Counter that has the given name, null if no Counter exists with the given name.
	 */
	public Counter getNamedCounter(String name) {
		return (Counter)counters.get(name);
	}	

	
	/**
	 * Creates and adds an Alarm instance maintained by this Cpu
	 * 
	 * @param name the name of the Alarm to be created and added to the CPU.
	 */
	public Alarm createAlarm(String name) {
		
		Alarm alarm = new Alarm(this,name);
		
		alarms.put(name, alarm);
		
		return alarm;
	}	
	
	/**
	 * Adds an Alarm to the collection maintained by this Cpu
	 * 
	 * @param alarm the Alarm to be added
	 */
	public void addAlarm(Alarm alarm) {
		
		alarms.put(alarm.getName(),alarm);
	}
	
	/**
	 * Returns the collection of Alarm maintained by this Cpu
	 * @return Collection of Alarm
	 */
	public Collection<Alarm> getAlarms() {
		return alarms.values();
	}
	
	/**
	 * Returns the Alarms maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Alarm to return
	 * @return Alarm that has the given name, null if no Alarm exists with the given name.
	 */
	public Alarm getNamedAlarm(String name) {
		return (Alarm)alarms.get(name);
	}
	
	
	/**
	 * Creates and adds a Task instance maintained by this Cpu
	 * 
	 * @param name the name of the Task to create and add
	 * @return the Task instance created
	 */
	public Task createTask(String name) {
		
		Task task = new Task(this, name);
		
		tasks.put(name, task);
		
		return task;
	}	
	
	/**
	 * Adds a Task to the collection maintained by this Cpu
	 * 
	 * @param task the Task to be added
	 */
	public void addTask(Task task) {
		
		tasks.put(task.getName(),task);
	}
	
	/**
	 * Returns the collection of Tasks maintained by this Cpu
	 * @return Collection of Tasks
	 */
	public Collection<Task> getTasks() {
		return tasks.values();
	}

	/**
	 * Returns the collection of Extended Tasks maintained by this Cpu
	 * @return Collection of Extended Tasks
	 */
	public Collection<Task> getExtendedTasks() {
		
		Collection<Task> extendedTasks = new LinkedHashSet<Task>();
		
		Iterator<Task> iter = tasks.values().iterator();
		
		while (iter.hasNext()) {
			Task nextTask=iter.next();
			
			if (nextTask.isExtendedTask()) {
				extendedTasks.add(nextTask);
			}
		}
		
		return extendedTasks;
	}	

	/**
	 * Returns the collection of Basic Tasks maintained by this Cpu
	 * @return Collection of Basic Tasks
	 */
	public Collection<Task> getBasicTasks() {
		
		Collection<Task> basicTasks = new LinkedHashSet<Task>();
		
		Iterator<Task> iter = tasks.values().iterator();
		
		while (iter.hasNext()) {
			Task nextTask=iter.next();
			
			if (nextTask.isBasicTask()) {
				basicTasks.add(nextTask);
			}
		}
		
		return basicTasks;
	}	
	
	
	/**
	 * Returns the collection of Tasks maintained by this Cpu that have the given priority.
	 * @param priority the priority to be checked against
	 * @return Collection of Tasks that have the given priority
	 */
	public Collection<Task> getTasksWithPriority(long priority) {
		
		Collection<Task> resultTasks = new LinkedHashSet<Task>();
		
		Iterator<Task> iter = tasks.values().iterator();
		
		while (iter.hasNext()) {
			Task next = iter.next();
			
			if ( next.getPriority() == priority ) {
				resultTasks.add(next);
			}
		}

		return resultTasks;
	}

	
	/**
	 * Returns the Task maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Task to return
	 * @return Task that has the given name, null if no Task exists with the given name.
	 */
	public Task getNamedTask(String name) {
		return tasks.get(name);
	}	

	/**
	 * Creates and adds an ISR instance maintained by this Cpu
	 * 
	 * @param name the name of the ISR to create and add
	 * @return the Isr instance created
	 */
	public Isr createIsr(String name) {
		
		Isr isr = new Isr(this, name);
		
		isrs.put(name, isr);
		
		return isr;
	}	
	
	/**
	 * Adds an Isr to the collection maintained by this Cpu
	 * 
	 * @param isr the Isr to be added
	 */
	public void addIsr(Isr isr) {
		
		isrs.put(isr.getName(),isr);
	}
	
	/**
	 * Returns the collection of Isrs maintained by this Cpu
	 * @return Collection of Isrs
	 */
	public Collection<Isr> getIsrs() {
		return isrs.values();
	}
	
	/**
	 * Returns the Isrs maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Isr to return
	 * @return Isr that has the given name, null if no Isr exists with the given name.
	 */
	public Isr getNamedIsr(String name) {
		return isrs.get(name);
	}	

	/**
	 * Creates and adds a Resource instance maintained by this Cpu
	 * 
	 * @param name the name of the Resource to create and add
	 * @return the Resource instance created
	 */
	public Resource createResource(String name) {
		
		Resource res = new Resource(this, name);
		
		resources.put(name, res);
		
		return res;
	}		
	
	/**
	 * Adds a Resource to the collection maintained by this Cpu
	 * 
	 * @param resource the Resource to be added
	 */
	public void addResource(Resource resource) {
		
		resources.put(resource.getName(),resource);
	}
	
	/**
	 * Returns the collection of Resources maintained by this Cpu
	 * @return Collection of Resources
	 */
	public Collection<Resource> getResources() {
		return resources.values();
	}
	
	/**
	 * Returns the Resource maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Resource to return
	 * @return Resource that has the given name, null if no Resource exists with the given name.
	 */
	public Resource getNamedResource(String name) {
		return resources.get(name);
	}	

	/**
	 * Returns the collection of (non-internal) Resources maintained by this Cpu
	 * @return Collection of (non-internal) Resources
	 * @see Resource#isInternal()
	 */	
	public Collection<Resource> getExternalResources() {
		
		Collection<Resource> extResources = new LinkedHashSet<Resource>();
		
		Iterator<Resource> iter = resources.values().iterator();
		
		while (iter.hasNext()) {
			Resource next = iter.next();
			
			if ( !next.isInternal()) {
				extResources.add(next);
			}
		}
		
		return extResources;
	}
	
	/**
	 * Creates and adds an AppMode instance maintained by this Cpu
	 * 
	 * @param name the name of the AppMode to create and add
	 * @return the AppMode instance created
	 */
	public AppMode createAppMode(String name) {
		
		AppMode appMode = new AppMode(this, name);
		
		appModes.put(name,appMode);
		
		return appMode;
	}	
	
	/**
	 * Adds an AppMode to the collection maintained by this Cpu
	 * 
	 * @param appMode the AppMode to be added
	 */
	public void addAppMode(AppMode appMode) {
		
		appModes.put(appMode.getName(),appMode);
	}
	
	/**
	 * Returns the collection of AppModes maintained by this Cpu
	 * @return Collection of AppModes
	 */
	public Collection<AppMode> getAppModes() {
		return appModes.values();
	}
	
	/**
	 * Returns the AppMode maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the AppMode to return
	 * @return AppMode that has the given name, null if no AppMode exists with the given name.
	 */
	public AppMode getNamedAppMode(String name) {
		return appModes.get(name);
	}

	
	/**
	 * Creates and adds a Message instance maintained by this Cpu
	 * 
	 * @param name the name of the Message to create and add
	 * @return the Message instance created
	 */
	public Message createMessage(String name) {
		
		Message message = new Message(this, name);
		
		messages.put(name, message);
		
		return message;
	}
	
	/**
	 * Adds a Message to the collection maintained by this Cpu
	 * 
	 * @param message the Message to be added
	 */
	public void addMessage(Message message) {
		
		messages.put(message.getName(),message);
	}
	
	/**
	 * Returns the collection of Messages maintained by this Cpu
	 * @return Collection of Messages
	 */
	public Collection<Message> getMessages() {
		return messages.values();
	}
	
	/**
	 * Returns the Message maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Message to return
	 * @return Message that has the given name, null if no Message exists with the given name.
	 */
	public Message getNamedMessage(String name) {
		return messages.get(name);
	}	
	
	/**
	 * Returns the collection of Sending Messages maintained by this Cpu
	 * @return Collection of (sending) Messages
	 */
	public Collection<Message> getSendingMessages() {
		
		Collection<Message> sendingMessages = new LinkedHashSet<Message>();
		
		for (Message next : messages.values() ) {
			
			if ( next.isSendingMessage() ) {
				sendingMessages.add(next);
			}
			
		}
		return sendingMessages;
	}
	
	/**
	 * Returns the collection of Receiving Messages maintained by this Cpu
	 * @return Collection of (receiving) Messages
	 */
	public Collection<Message> getReceivingMessages() {
		
		Collection<Message> receivingMessages = new LinkedHashSet<Message>();
		
		for (Message next : messages.values() ) {
			
			if ( next.isReceivingMessage() ) {
				receivingMessages.add(next);
			}
			
		}
		return receivingMessages;
	}	
	
	
	
	/**
	 * Creates and adds a ScheduleTable instance maintained by this Cpu
	 * 
	 * @param name the name of the ScheduleTable to create and add
	 * @return the ScheduleTable instance created
	 */
	public ScheduleTable createScheduleTable(String name) {
		
		ScheduleTable scheduleTable = new ScheduleTable(this, name);
		
		scheduleTables.put(name, scheduleTable);
		
		return scheduleTable;
	}
	
	/**
	 * Adds a ScheduleTable to the collection maintained by this Cpu
	 * 
	 * @param scheduleTable the ScheduleTable to be added
	 */
	public void addScheduleTable(ScheduleTable scheduleTable) {
		
		scheduleTables.put(scheduleTable.getName(), scheduleTable);
	}
	
	/**
	 * Returns the collection of ScheduleTables maintained by this Cpu
	 * @return Collection of ScheduleTables
	 */
	public Collection<ScheduleTable> getScheduleTables() {
		return scheduleTables.values();
	}
	
	/**
	 * Returns the ScheduleTable maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the ScheduleTable to return
	 * @return ScheduleTable that has the given name, null if no ScheduleTable exists with the given name.
	 */
	public ScheduleTable getNamedScheduleTable(String name) {
		return scheduleTables.get(name);
	}	
	
	/**
	 * Creates and adds an Application instance maintained by this Cpu
	 * 
	 * @param name the name of the Application to create and add
	 * @return the Application instance created
	 */
	public Application createApplication(String name) {
		
		Application application = new Application(this, name);
		
		applications.put(name, application);
		
		return application;
	}
	
	/**
	 * Adds an Application to the collection maintained by this Cpu
	 * 
	 * @param application the Application to be added
	 */
	public void addApplication(Application application) {
		
		applications.put(application.getName(), application);
	}
	
	/**
	 * Returns the collection of Applications maintained by this Cpu
	 * @return Collection of Applications
	 */
	public Collection<Application> getApplications() {
		return applications.values();
	}
	
	/**
	 * Returns the Application maintained by this Cpu that has the given name.
	 * 
	 * @param name the name of the Application to return
	 * @return Application that has the given name, null if no Application exists with the given name.
	 */
	public Application getNamedApplication(String name) {
		return applications.get(name);
	}		
	
	/**
	 * Returns the Set of all OSModelElement instances contained by the CPU
	 * 
	 * @return the Set of all {@link OSModelElement} contained by the CPU
	 */
	public Collection<OSModelElement> getAllContainedElements() {
		
		Set<OSModelElement> containeElements = new LinkedHashSet<OSModelElement>();
		
		if ( com != null ) {
			containeElements.add(com);
		}		
		if ( os != null ) {
			containeElements.add(os);
		}
		if ( nm != null ) {
			containeElements.add(nm);
		}		
		containeElements.addAll(alarms.values());
		containeElements.addAll(applications.values());
		containeElements.addAll(appModes.values());
		containeElements.addAll(counters.values());
		containeElements.addAll(events.values());
		containeElements.addAll(isrs.values());
		containeElements.addAll(messages.values());
		containeElements.addAll(resources.values());
		containeElements.addAll(scheduleTables.values());
		containeElements.addAll(tasks.values());

		return containeElements;
	}
	
	/**
	 * Returns the Set of all OSModelElement names contained by the CPU
	 * 
	 * @return the Set of all {@link OSModelElement} names contained by the CPU
	 */
	public Collection<String> getAllContainedElementNames() {
		
		Collection<String> allElementNames = new HashSet<String>();
		
		for ( OSModelElement nextElement : getAllContainedElements()) {
			allElementNames.add(nextElement.getName());
		}
		
		return allElementNames;
	}
	
	/**
	 * Returns the Set of all COM AppMode names contained by COM elements owned by the CPU
	 * 
	 * These names are extracted from the COM element.
	 * 
	 * @return the Set of all COM AppMode names contained by the CPU
	 */
	public Collection<String> getAllCOMAppModeNames() {
		
		Collection<String> allCOMAppModeNames = new HashSet<String>();
		
		if ( com != null ) {
			// Get all the app mode names form the COM element
			allCOMAppModeNames.addAll(com.getAppModes());
		}
		
		return allCOMAppModeNames;
	}	

	
	/**
	 * Returns the Set of all "Flag" Macro names contained in elements owned by the CPU
	 * 
	 * These names are extracted from elements that contain notification "Flag" name information.
	 * The names are then used to construct Macro names that are generated by a typical code generator.
	 * 
	 * i.e.
	 * 	ReadFlag_<flagName>
	 *	ResetFlag_<flagName>
	 * 
	 * @return the Set of all "Flag" Macro names contained by the CPU
	 */
	public Collection<String> getAllFlagMacroNames() {
		
		Collection<String> allFlagNames = new HashSet<String>();
		
		// Get all message notification names
		for ( Message nextElement : getMessages() ) {
			
			// Get notificationflag macro names
			String flagName = nextElement.getReadFlagMacroName();
			
			if ( flagName != null ) {
				allFlagNames.add(flagName);
			}
			
			flagName = nextElement.getResetFlagMacroName();
			if ( flagName != null ) {
				allFlagNames.add(flagName);
			}			
			
			// Get low notification flag name and generate typical access macro names			
			flagName = nextElement.getLowReadFlagMacroName();
			
			if ( flagName != null ) {
				allFlagNames.add(flagName);
			}
			
			flagName = nextElement.getLowResetFlagMacroName();
			if ( flagName != null ) {
				allFlagNames.add(flagName);
			}		
		}		
		
		return allFlagNames;		
	}
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		Collection<OSModelElement> allElements = getAllContainedElements();
		
		// [1] In a CPU exactly one OS object must be defined. 
		if ( os == null ) {	
			problems.add(new Problem(Problem.WARNING, "CPU '"+getName()+"' has no explicit OS information defined."));			
		}
		
		// [2] If the CPU contains one or more messages, then a single COM object should also exist.
		if ( messages.size() > 0 && com == null ) {
			problems.add(new Problem(Problem.INFORMATION, "CPU '"+getName()+"' has no explicit COM information defined, but COM messages are used."));
		}
		
		// [3] If a COM objects exists, then one or more messages should also exist.
		if ( com != null && messages.size() == 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "CPU '"+getName()+"' defines a COM object, but has no COM messages."));
		}
		
		// [4] The CPU should contain at least one Runnable (Task or ISR) object.
		if ( tasks.isEmpty() && isrs.isEmpty() ) {
			problems.add(new Problem(Problem.INFORMATION, "CPU '"+getName()+"' has no Tasks or ISRs defined."));
		}
	
		// [5] The name value of all contained objects must be unique. [error]
		Set<String> existingNames = new HashSet<String>();
		
		for ( OSModelElement nextElement : allElements) {
			
			if ( existingNames.add(nextElement.getName()) == false ) {
				// name already present
				problems.add(new Problem(Problem.ERROR, "CPU '"+getName()+"' contains duplicate object name '"+nextElement.getName()+"'. All Object names must be unique"));
			}
		}
		
		// Check contained elements if doing full deep check
		if ( deepCheck ) {

			for ( OSModelElement nextElement : allElements) {
				nextElement.doModelCheck(problems, true);
			}
		}
		
	}	
	
	public Cpu() {
		super(null,null);
		
		setCpu(this);	// set a Cpu to be maintained by itself
	}		
	
	public Cpu(String name) {
		super(null,name);
		
		setCpu(this);
	}	
}
