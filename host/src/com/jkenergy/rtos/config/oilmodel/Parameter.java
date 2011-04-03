package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/Parameter.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * This class is used to represent a Parameter value (e.g. a specific instance of a Task's priority.) within the OIL Model.<br><br>
 * 
 * This class is a proxy for the actual value, which is represented by a referenced FeatureValue. This class exists to allow
 * simpler modelling of sub-parameter values.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Parameter extends OILNamedElement {

	/**
	 * The owning element of this Parameter (this will be either an ObjectDefinition or ParameterizedValue)
	 */
	private OILElement owner=null;	
	
	/**
	 * The actual value assigned to the Parameter
	 */
	private FeatureValue value=null;

	
	/**
	 * The {@link FeatureDefinition} of which this Parameter is an instance.
	 * This is the cache for a derived value.
	 * @see #getFeatureDefinition
	 */
	private FeatureDefinition featureDefinition=null;	
	
	
	/**
	 * 
	 * @return the current value of the parameter
	 */
	public FeatureValue getValue() {
		return value;
	}

	/**
	 * Sets the value associated with the Parameter
	 * @param newValue
	 */
	public void setValue(FeatureValue newValue) {
		value = newValue;
	}	
		
	
	/**
	 * Returns the top level {@link ObjectDefinition} in which this Parameter is contained
	 * 
	 * @return the outermost {@link ObjectDefinition} that owns this Parameter
	 */
	public ObjectDefinition getObjectDefinition() {
		
		if ( owner instanceof ObjectDefinition ) {
			// owner is an ObjectDefinition, so can just cast and return
			return (ObjectDefinition)owner;
		}
		else if ( owner instanceof ParameterizedValue ) {
			
			// owner is a ParameterizedValue, so get Parameter in which that is defined
			// then call this operation on that Parameter
			Parameter param=((ParameterizedValue)owner).getParameter();
			
			if ( param!=null ) {
				return param.getObjectDefinition();
			}
		}
		return null;	// no recognised owner
	}
	
	/**
	 * @return {@link ApplicationDefinition} that is the ultimate container of this Parameter
	 */		
	public ApplicationDefinition getApplicationDefinition() {

		// Access ApplicationDefinition from the top level ObjectDefinition
		ObjectDefinition objDef=getObjectDefinition();
		
		if ( objDef!=null )
			return objDef.getApplicationDefinition();
		
		return null;
	}
	
	/**
	 * Gets the {@link FeatureDefinition} of which this Parameter is an instance
	 * @return the {@link FeatureDefinition} of which this Parameter is an instance, <code>null</code> if there is no associated {@link FeatureDefinition}
	 */
	public FeatureDefinition getFeatureDefinition() {
	
		// If cached value unknown then attempt to calculate
		if ( featureDefinition==null ) {
			
			if ( owner instanceof ObjectDefinition ) {
				
				// owner is an ObjectDefinition, so ask that to find the associated FeatureDefinition
				featureDefinition=((ObjectDefinition)owner).getNamedFeatureDefinition(getName());
			}
			else if ( owner instanceof ParameterizedValue ) {
				// owner is a ParameterizedValue, so ask that to find the associated sub-FeatureDefinition
				featureDefinition=((ParameterizedValue)owner).getNamedSubFeatureDefinition(getName());
			}
		}
		return featureDefinition;
	}
	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// Do check on contained elements
		if ( deepCheck ) {
	
			if ( value!=null )
				value.doModelCheck(problems,deepCheck);
		}
	}
	
	
	/**
	 * @param owner the owning {@link OILElement} of the Parameter
	 * @param name the name of the Parameter
	 * @param lineNo the line number at which the element appears.
	 */
	public Parameter(OILElement owner, String name,int lineNo) {

		super(name,lineNo);
		this.owner=owner;	
	}	
}
