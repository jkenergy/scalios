package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Isr.java $
 * 
 */

import java.util.List;


import com.jkenergy.rtos.config.Problem;


/**
 * A SubClass of Runnable that models an ISR within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Isr extends Runnable {


	
	/**
	 * String that indicates the ISR vector. How this value is interpreted is platform specific.
	 */
	private String vector;	
	
	/**
	 * Category of the ISR
	 */
	private int category=1;	
	
	/**
	 * Count Limit of the ISR that determines maximum arrival rate (when Timing Protection active)
	 * 
	 * @see Runnable#timingProtection
	 */
	private long countLimit=0;			/* $Req: AUTOSAR $ */
		
	
	/**
	 * Flag indicating whether stack checking is enabled for this specific ISR
	 */
	private boolean stackCheckingEnabled;
	
	
	
	/**
	 * @return the countLimit
	 */
	public long getCountLimit() {
		return countLimit;
	}


	/**
	 * @param newCountLimit the countLimit to set
	 */
	public void setCountLimit(long newCountLimit)  {
		
		countLimit=newCountLimit;
	}	
	

	/**
	 * @return Returns the stackCheckingEnabled.
	 */
	public boolean isStackCheckingEnabled() {
		return stackCheckingEnabled;
	}

	/**
	 * @param stackCheckingEnabled The stackCheckingEnabled to set.
	 */
	public void setStackCheckingEnabled(boolean stackCheckingEnabled) {
		this.stackCheckingEnabled = stackCheckingEnabled;
	}

	/**
	 * @return Returns the vector.
	 */
	public String getVector() {
		return vector;
	}

	/**
	 * @param vector The vector to set.
	 */
	public void setVector(String vector) {
		this.vector = vector;
	}

	/**
	 * Sets the category of the ISR.
	 * @param newCategory the new category for the isr

	 */
	public void setCategory(long newCategory)  {
	
		category=(int)newCategory;
	}	
	
	/**
	 * 
	 * @return the category of the ISR
	 */
	public int getCategory() {
		return category;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		//[1] The category value of must equal 1 or 2. 

		if ( category !=1 && category != 2 ) {
			problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' has an illegal category value"));
		}
		
		//[2] An ISR with a category value of 1 may not have any accessedMessages. 
		if ( category == 1 && getAccessedMessages().size() > 0 ) {
			problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' is category 1 and therefore must not access messages"));
		}
		
		//[3] An ISR with a category value of 1 may not access any resources. 
		if ( category == 1 && this.getResources().size() > 0 ) {
			problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' is category 1 and therefore must not access resources"));
		}		

		//[4] An ISR with a category value of 1 must have its timingProtection flag set to false. [AUTOSAR]
		if ( category == 1 && hasTimingProtection() ) {
			problems.add(new Problem(Problem.WARNING, "ISR '"+getName()+"' is category 1, but has timing protection switched on"));
		}	
		
		//[5] If the timingProtection flag set to false then a countLimit value other than 0 should not be specified. [AUTOSAR]
		if ( hasTimingProtection() == false && countLimit != 0 ) {
			problems.add(new Problem(Problem.INFORMATION, "ISR '"+getName()+"' has timing protection off, but has a count limit specified"));
		}
		
		//[6] The countLimit value must be greater than, or equal to, zero. [AUTOSAR]
		if ( countLimit < 0 ) {
			problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' has an invalid (negative) count limit value"));
		}
		
		//[7] The priority value must be greater than, or equal to, zero.  [EXTENSION]		
		if ( getPriority() < 0 ) {
			problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' has an invalid (negative) priority value"));
		}
		
		// [8] An ISR may not access resources with a resourceProperty value that equals INTERNAL.
		for ( Resource next : getResources() ) {
			
			if ( next.isInternal() ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+getName()+"' may not access the internal resource '"+next.getName()+"'"));
			}
		}
	}
	
	
	
	public Isr(Cpu cpu,String name) {
		super(cpu,name);
	}

	
}
