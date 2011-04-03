package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/AttributeDefinition.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * This is an abstract SubClass of FeatureDefinition that stores information relating to the (meta-model) definition of a typed OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public abstract class AttributeDefinition extends FeatureDefinition {

	/**
	 * Defines whether AUTO values can be used as the default or attribute value.
	 */
	private boolean isWithAuto=false;
	
	/**
	 * Specifies an AUTO default value for the AttributeDefinition
	 * @see #isWithAuto
	 */
	private boolean isAuto=false;
	
	/**
	 * Specifies whether the definition contains a default value
	 */
	private boolean hasDefault=false;
	
	
	/**
	 * @return isWithAuto flag
	 */
	public boolean isWithAuto() {
		return isWithAuto;
	}	

	/**
	 * @param newIsWithAuto new flag value
	 */
	public void isWithAuto(boolean newIsWithAuto) {
		isWithAuto=newIsWithAuto;
	}
	
	/**
	 * @return isAuto flag
	 */
	public boolean isAuto() {
		return isAuto;
	}	

	/**
	 * @param newIsAuto new flag value
	 */
	public void isAuto(boolean newIsAuto) {
		isAuto=newIsAuto;
	}
	
	/**
	 * @return hasDefault flag
	 */
	public boolean hasDefault() {
		return hasDefault;
	}

	/**
	 * @param newHasDefault new flag value
	 */
	public void hasDefault(boolean newHasDefault) {
		hasDefault=newHasDefault;
	}	

	/*
	 * Check if this feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is a restricted version
	 * @return <code>true</code> if restriced version, else <code>false</code>
	 */
	@Override
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		if ( super.isRestrictedVersionOf(otherFeature) ) {
			
			// place other checks here if required in the future
			
			return true;
		}
		return false;
	}	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// [1] The isAuto attribute may only be true if isWithAuto attribute is also specified as true.
		if ( isAuto==true && isWithAuto==false )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify a default value of AUTO on '"+getName()+"' that is not defined using WITH_AUTO",getLineNo()));
	}
	
	/**
	 * 
	 * @param lineNo the line number at which the element appears.
	 */
	protected AttributeDefinition(int lineNo) {
		super(lineNo);
	}
}
