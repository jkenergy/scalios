package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/FeatureDefinition.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * This is an abstract class that stores information relating to the (meta-model) definition of all OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public abstract class FeatureDefinition extends OILNamedElement {

	/**
	 * Specifies whether multiple instances of the attribute (parameter) may exist
	 */
	private boolean isMultiple=false;
	
	/**
	 * @return isMultiple flag
	 */
	public boolean isMultiple() {
		return isMultiple;
	}	

	/**
	 * @param newIsMultiple new flag value
	 */
	public void isMultiple(boolean newIsMultiple) {
		isMultiple=newIsMultiple;
	}	

	
	/**
	 * Checks if the feature is the same type as the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is to be compared
	 * @return <code>true</code> if based on same type, else <code>false</code>
	 */
	boolean isSameTypeAs(FeatureDefinition otherFeature) {
		
		if ( otherFeature!=null ) {
			// ensure exactly based on same class
			if ( getClass()==otherFeature.getClass() )
				return true;
		}
		return false;
	}

	/**
	 * Check if the feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition}  to which this one is a restricted version
	 * @return <code>true</code> if restriced version, else <code>false</code>
	 */
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		if ( otherFeature!=null ) {
			// ensure exactly same type and isMultiple flag
			if ( getClass()==otherFeature.getClass() && isMultiple==otherFeature.isMultiple() )
				return true;
		}
		return false;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
	
	}
	/**
	 * @param lineNo the line number at which the element appears.
	 *
	 */
	public FeatureDefinition(int lineNo) {
		super(lineNo);
	}	

	/**
	 * @param name the name of the definition
	 * @param lineNo the line number at which the element appears.
	 */
	public FeatureDefinition(String name,int lineNo) {
		super(name,lineNo);
	}		
}
