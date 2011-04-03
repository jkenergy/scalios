package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/StringAttributeDef.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of AttributeDefinition used to define a string type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class StringAttributeDef extends AttributeDefinition {

	/**
	 * The default value for the StringAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	private String defaultValue;
	
	
	/**
	 * @return The default value for the StringAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Sets the new default value for the StringAttributeDef
	 * @param newDefaultValue
	 */
	public void setDefaultValue(String newDefaultValue) {
		
		defaultValue=newDefaultValue;
		hasDefault( newDefaultValue!=null );
	}

	
	/**
	 * Check if the feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is a restricted version
	 * @return <code>true</code> if restriced version, else <code>false</code>
	 */
	@Override
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		return false; // StringAttributeDef instances can't be further restricted 
	}		
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
	}	
	
	/**
	 * 
	 * @param lineNo the line number of the element
	 */
	public StringAttributeDef(int lineNo) {

		super(lineNo);
	}	
}
