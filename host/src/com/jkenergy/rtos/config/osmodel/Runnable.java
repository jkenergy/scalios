package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Runnable.java $
 * 
 */

import java.math.BigInteger;
import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * This abstract class acts as a super class for {@link Task} and {@link Isr} classes, modelling stack size, priority details, timing protection information, access to {@link Resource} instance and {@link Message} instances. (since all are common to tasks and ISRs).
 * 
 * @author Mark Dixon
 *
 */
public abstract class Runnable extends OSModelElement {

	/**
	 * The default stacksize of the runnable
	 */
	private static final long DEFAULT_STACKSIZE = 0;	
	
	/**
	 * The size of the stack allocated to the runnable
	 */
	private long stackSize = DEFAULT_STACKSIZE;	
	
	
	/**
	 * Timing Protection flag
	 */
	private boolean timingProtection=false;					/* $Req: AUTOSAR $ */
	
	/**
	 * WCET (in nanoseconds) of the Runnable (task or ISR) when Timing Protection active.
	 * 
	 */
	private BigInteger executionBudget=BigInteger.ZERO;		/* $Req: AUTOSAR $ */	
	
	/**
	 * Time limit (in nanoseconds) of the Runnable (task or ISR) when Timing Protection active.
	 * 
	 */
	private BigInteger timeLimit=BigInteger.ZERO;			/* $Req: AUTOSAR $ */	
	
	/**
	 * Collection of locking times associated with the runnable
	 */
	private Set<LockingTime> lockingTimes = new LinkedHashSet<LockingTime>();			/* $Req: AUTOSAR $ */	
	
	
	/**
	 * isAutoStackSize flag that specifies whether the stack size is to be automatically calculated
	 */
	private boolean isAutoStackSize=false;	
	
	/**
	 * Runnable priority (0=lowest)
	 */
	private long priority=0;	
	
	
	/**
	 * Set of Resources accessed by the runnable
	 */
	private Set<Resource> resources = new LinkedHashSet<Resource>();
	
	/**
	 * Set of Messages accessed by the runnable.
	 */
	private Set<Message> accessedMessages = new LinkedHashSet<Message>();	
	
	
	/**
	 * @return the timingProtection
	 */
	public boolean hasTimingProtection() {
		return timingProtection;
	}

	/**
	 * @param timingProtection the timingProtection to set
	 */
	public void setTimingProtection(boolean timingProtection) {
		this.timingProtection = timingProtection;
	}

	/**
	 * Sets the executionBudget of the Runnable.
	 * @param newExecutionBudget the new executionBudget of the Runnable
	 */
	public void setExecutionBudget(BigInteger newExecutionBudget)  {
			
		executionBudget=newExecutionBudget;
	}
	
	/**
	 * @return the executionBudget of the Runnable.
	 */
	public BigInteger getExecutionBudget() {
		return executionBudget;
	}	
	
	/**
	 * 
	 * @return true if a non-zero executionBudget value is defined
	 */
	public boolean hasNonExecutionBudget() {
		return executionBudget.compareTo(BigInteger.ZERO) != 0;
	}	
	
	
	/**
	 * Sets the timeLimit value of the Runnable.
	 * @param newTimeLimit the new timeLimit value of the Runnable
	 * 
	 */
	public void setTimeLimit(BigInteger newTimeLimit)  {
							
		timeLimit=newTimeLimit;
	}
	
	/**
	 * @return the timeLimit of the Runnable.
	 */
	public BigInteger getTimeLimit() {
		return timeLimit;
	}	
	
	/**
	 * 
	 * @return true if a non-zero time limit value is defined
	 */
	public boolean hasNonZeroTimeLimit() {
		return timeLimit.compareTo(BigInteger.ZERO) != 0;
	}
	
	/**
	 * @return Returns the isAutoStackSize.
	 */
	public boolean isAutoStackSize() {
		return isAutoStackSize;
	}

	/**
	 * @param isAutoStackSize The isAutoStackSize to set.
	 */
	public void setAutoStackSize(boolean isAutoStackSize) {
		this.isAutoStackSize = isAutoStackSize;
	}

	/**
	 * Sets the priority of the Runnable.
	 * @param newPriority the new priority for the Runnable
	 */
	public void setPriority(long newPriority)  {
		
		priority=newPriority;
	}
	
	/**
	 * 
	 * @return the priority of the Runnable 
	 */
	public long getPriority() {
		return priority;
	}	
	
	
	/**
	 * @return Returns the stackSize.
	 */
	public long getStackSize() {
		return stackSize;
	}


	
	/**
	 * @param newStackSize The new stackSize value to set.
	 */
	public void setStackSize(long newStackSize) {

		this.stackSize = newStackSize;
	}	
	
	/**
	 * Adds a Resource to the collection of resources accessed by the runnable.
	 * This method creates a two way relationship.
	 * 
	 * @param resource the Resource to be added
	 */
	public void addResource(Resource resource) {
		
		if ( resource!=null ) {
			if ( resources.add(resource) )
				resource.addRunnable(this);	// inform resource that it is accessed by this runnable
		}
	}	

	/**
	 * Returns the collection of Resources that are accessed by the runnable
	 * @return Collection of Resources
	 */
	public Collection<Resource> getResources() {
		return resources;
	}

	
	/**
	 * Adds a Message to the collection of messages accessed by the runnable.
	 * This method creates a two way relationship.
	 * 
	 * @param message the Message to be added
	 */
	public void addAccessedMessage(Message message) {
		
		if ( message!=null ) {
			if ( accessedMessages.add(message) )
				message.addRunnable(this);	// inform message that it is accessed by this runnable
		}
	}	
	
	/**
	 * Returns the collection of Messages that are accessed by the runnable
	 * @return Collection of Messages
	 */
	public Collection<Message> getAccessedMessages() {
		return accessedMessages;
	}
	
	/**
	 * Creates, adds and returns a LockingTime associated with the Runnable
	 * 
	 * @param lockingType the type of locking time to be created
	 * @return the created LockingTime
	 * @see LockingTimeKind 
	 */
	public LockingTime createLockingTime(LockingTimeKind lockingType) {
		
		LockingTime lockingTime = new LockingTime(this, lockingType);
		
		lockingTimes.add(lockingTime);
		
		return lockingTime;
	}	
	
	/**
	 * Creates, adds and returns a LockingTime associated with the Runnable
	 * 
	 * @return the created LockingTime
	 */
	public LockingTime addLockingTime() {
		
		LockingTime lockingTime = new LockingTime(this);
		
		lockingTimes.add(lockingTime);
		
		return lockingTime;
	}		
	
	/**
	 * 
	 * @return Collection of LockingTimes associated with the Runnable
	 */
	public Collection<LockingTime> getLockingTimes() {
		return lockingTimes;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		// [1] The stackSize value must be greater than, or equal to, zero. [EXTENSION]
		if ( stackSize < 0 ) {
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an invalid (negative) stack size value"));
		}
		
		// [2] The executionBudget value must be greater than, or equal to, zero. [AUTOSAR]
		if ( executionBudget.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an invalid (negative) execution budget value"));
		}
		
		// [3] The timeLimit value must be greater than, or equal to, zero. [AUTOSAR]
		if ( timeLimit.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an invalid (negative) time limit value"));
		}

		//[4] If the timingProtection flag set to false then a executionBudget value other than 0 should not be specified. [AUTOSAR]
		if ( hasTimingProtection() == false && executionBudget.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, getClassName()+" '"+getName()+"' has timing protection off, but has an execution budget specified"));
		}
		
        //[5] If the timingProtection flag set to false then a timeLimit value other than 0 should not be specified . [AUTOSAR]
		if ( hasTimingProtection() == false && timeLimit.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, getClassName()+" '"+getName()+"' has timing protection off, but has a time limit value specified"));
		}
		
		//[6] If the timingProtection flag set to false then a no lockingTimes should be specified. [AUTOSAR]
		if ( hasTimingProtection() == false && lockingTimes.size() > 0 ) {
			problems.add(new Problem(Problem.INFORMATION, getClassName()+" '"+getName()+"' has timing protection off, but has locking times specified"));
		}	

		// Check contained elements if doing full deep check
		if ( deepCheck ) {
			for ( LockingTime next : lockingTimes ) {
				next.doModelCheck(problems, true);
			}
		}
	}
	
	
	protected Runnable(Cpu cpu,String name) {
		super(cpu,name);
	}		
}
