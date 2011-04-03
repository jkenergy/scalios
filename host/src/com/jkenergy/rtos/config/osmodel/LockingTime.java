package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/LockingTime.java $
 * 
 */

import java.math.BigInteger;
import java.util.List;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;


/**
 * A Class that models locking time information for a Runnable within the OS.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 *
 */

public class LockingTime implements Checkable {

	/**
	 * The Runnable that owns the LockingTime
	 */
	private Runnable runnable;													/* $Req: AUTOSAR $ */
	
	/**
	 * Type of locking
	 * 
	 * @see LockingTimeKind
	 */ 
	private LockingTimeKind lockType=LockingTimeKind.RESOURCELOCK_LITERAL;		/* $Req: AUTOSAR $ */
	

	
	/**
	 * Worst case time between getting and releasing a resource
	 * 
	 * Valid when lockType == RESOURCELOCK
	 */
	private BigInteger resourceLockTime=BigInteger.ZERO;						/* $Req: AUTOSAR $ */
	
	
	/**
	 * The Resource to which the locking time relates.
	 * Valid when lockType == RESOURCELOCK
	 */
	private Resource resource;													/* $Req: AUTOSAR $ */
	
	/**
	 * Amount of time that Category 2 interrupts are disabled by the Task/ISR
	 * 
	 * Valid when lockType == INTERRUPTLOCK
	 */
	private BigInteger osInterruptLockTime=BigInteger.ZERO;						/* $Req: AUTOSAR $ */

	/**
	 * Amount of time that Category 1 and Category 2 interrupts are disabled by the Task/ISR
	 * 
	 * Valid when lockType == INTERRUPTLOCK
	 */
	private BigInteger allInterruptLockTime=BigInteger.ZERO;					/* $Req: AUTOSAR $ */	

	/**
	 * @return the Runnable that owns the locking time
	 */
	public Runnable getRunnable() {
		return runnable;
	}	
	
	/**
	 * @return the locking time type
	 * @see LockingTimeKind
	 */
	public LockingTimeKind getLockType() {
		return lockType;
	}	
	
	/**
	 * @param type the new locking time type
	 * @see LockingTimeKind
	 */
	public void setLockType(LockingTimeKind type) {
		lockType = type;
	}	
	
	/**
	 * 
	 * @return true if the locking time is in relation to a Resource
	 */
	public boolean isResourceLockingTime() {
		return LockingTimeKind.RESOURCELOCK_LITERAL.equals(lockType);
	}	
	
	/**
	 * 
	 * @return true if the locking time is in relation to an ISR
	 */
	public boolean isInterruptLockingTime() {
		return LockingTimeKind.INTERRUPTLOCK_LITERAL.equals(lockType);
	}		
	
	
	/**
	 * Sets the Resource to which the locking time relates
	 * 
	 * This method creates a two way relationship. 
	 * @param newResource the new Resource to which the locking time relates
	 */
	public void setResource(Resource newResource) {
		
		if ( newResource!=null ) {
			
			if ( resource!=newResource ) {
				
				resource=newResource;
				
				resource.addLockingTime(this);
			}
		}
	}

	/**
	 * Returns the Resource to which the locking time relates
	 * @return Resource to which the locking time relates
	 */
	public Resource getResource() {
		return resource;
	}	
	
	/**
	 * Sets the worst case locking time of the resource
	 * @param newResourceLockTime the new resource lock time
	 */
	public void setResourceLockTime(BigInteger newResourceLockTime) {
					
		resourceLockTime=newResourceLockTime;
	}
	
	/**
	 * @return the worst case locking time of the resource
	 */
	public BigInteger getResourceLockTime() {
		return resourceLockTime;
	}
	
	/**
	 * Sets the amount of Category 2 interrupt lockout time
	 * @param newOSInterruptLockTime the new interrupt lockout time
	 * 
	 */
	public void setOSInterruptLockTime(BigInteger newOSInterruptLockTime)  {
			
			
		osInterruptLockTime=newOSInterruptLockTime;
	}
	
	/**
	 * @return the amount of Category 2 interrupt lockout time
	 */
	public BigInteger getOSInterruptLockTime() {
		return osInterruptLockTime;
	}	
	
	/**
	 * Sets the amount of Category 1 and 2 interrupt lockout time
	 * @param newAllInterruptLockTime the new interrupt lockout time
	 * 
	 */
	public void setAllInterruptLockTime(BigInteger newAllInterruptLockTime)  {
			
		allInterruptLockTime=newAllInterruptLockTime;
	}
	
	/**
	 * @return the amount of Category 1 and 2 interrupt lockout time
	 */
	public BigInteger getAllInterruptLockTime() {
		return allInterruptLockTime;
	}	

	/**
	 * {@inheritDoc}
	 */
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		// Do check of this element
		
		//[1] The resourceLockTime value must be greater than, or equal to, zero. [AUTOSAR]
		if ( resourceLockTime.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Locking Time within '"+runnable.getName()+"' has an invalid (negative) resource lock time value"));
		}
		
		//[2] The osInterruptLockTime value must be greater than, or equal to, zero. [AUTOSAR]
		if ( osInterruptLockTime.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Locking Time within '"+runnable.getName()+"' has an invalid (negative) OS interrupt lock time value"));
		}
		
		//[3] The allInterruptLockTime value must be greater than, or equal to, zero. [AUTOSAR]
		if ( allInterruptLockTime.compareTo(BigInteger.ZERO) == -1 ) {
			problems.add(new Problem(Problem.ERROR, "Locking Time within '"+runnable.getName()+"' has an invalid (negative) all interrupt lock time value"));
		}
		
		//[4] If the lockType value equals RESOURCELOCK then a lockedResource must be identified. [AUTOSAR]
		if ( isResourceLockingTime() && resource == null) {
			problems.add(new Problem(Problem.ERROR, "Locking Time within '"+runnable.getName()+"' does not specify the locked resource"));
		}
		
		//[5] If the lockType value equals RESOURCELOCK then the osInterruptLockTime value should be 0. [information] [AUTOSAR]
		if ( isResourceLockingTime() && osInterruptLockTime.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Locking Time within '"+runnable.getName()+"' is resource related, but specifies an OS interrupt lock time"));
		}
		
		//[6] If the lockType value equals RESOURCELOCK then the allInterruptLockTime value should be 0. [information] [AUTOSAR]
		if ( isResourceLockingTime() && allInterruptLockTime.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Locking Time within '"+runnable.getName()+"' is resource related, but specifies an all interrupt lock time"));
		}		

		//[7] If the lockType value equals INTERRUPTLOCK then a lockedResource must not be specified. [information] [AUTOSAR]
		if ( this.isInterruptLockingTime() && resource != null ) {
			problems.add(new Problem(Problem.INFORMATION, "Locking Time within '"+runnable.getName()+"' is interrupt related, but specifies a locked resource"));
		}
		
		//[8] If the lockType value equals INTERRUPTLOCK then the resourceLockTime value should be 0. [information] [AUTOSAR]
		if ( this.isInterruptLockingTime() && resourceLockTime.compareTo(BigInteger.ZERO) != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "Locking Time within '"+runnable.getName()+"' is interrupt related, but specifies resource lock time value"));
		}		

		// [9] If specified, the lockedResource must be within the set of resources accessed by the owning runnable.[AUTOSAR]
		if ( resource != null ) {
			if ( runnable.getResources().contains(resource) == false ) {
				problems.add(new Problem(Problem.ERROR, "Locking Time within '"+runnable.getName()+"' attempts to lock a resource not accessed by '"+runnable.getName()+"'"));
			}
		}
	}
	
	
	public void processDOM() {
		
	}	
	
	/**
	 * 
	 * @param runnable the Runnable that owns the LockingTime
	 * @param lockType the type of the locking time
	 */
	LockingTime(Runnable runnable, LockingTimeKind lockType) {
		this.runnable = runnable;
		this.lockType = lockType;
	}

	/**
	 * 
	 * @param runnable the Runnable that owns the LockingTime
	 */
	LockingTime(Runnable runnable) {
		this.runnable = runnable;
	}	



}


