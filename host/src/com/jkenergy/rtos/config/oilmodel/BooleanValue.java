package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/BooleanValue.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of ParameterizedValue used to represent boolean OIL attribute value (i.e. a boolean value with sub-parameter values).
 * 
 * @author Mark Dixon
 *
 */
public class BooleanValue extends ParameterizedValue {

	/**
	 * The primitive Value of the BooleanValue 
	 */
	private boolean value;	

	/**
	 * @return The assigned boolean value
	 */
	public boolean getValue() {
		return value;
	}


	/**
	 * 
	 * @return A Collection of sub {@link FeatureDefinition} instances that are defined within the associated {@link FeatureDefinition}
	 */
	@Override
	public Collection<FeatureDefinition> getSubFeatures() {
		
		// Get the FeatureDefinition of the owning Parameter
		FeatureDefinition featureDef = getFeatureDefinition();
				
		// Ask the associated FeatureDefinition to return the sub-FeatureDefintiion
		if ( featureDef!=null ) {
			
			if ( featureDef instanceof BoolAttributeDef ) {
			
				BoolAttributeDef boolAttribDef = (BoolAttributeDef)featureDef;
					
				if ( value ) {
					return boolAttribDef.getTrueValue().getSubFeatures();
					
				} else {
					return boolAttribDef.getFalseValue().getSubFeatures();
				}
			}
		}
		
		return null;		
	}
	
	/**
	 * Returns a sub {@link FeatureDefinition} with the given name, that is defined within the
	 * {@link FeatureDefinition} of which this ParameterizedValue's owning {@link Parameter} is an instance.
	 * 
	 * @param name the name of the sub {@link FeatureDefinition} to find
	 * @return a sub {@link FeatureDefinition} with the given name, <code>null</code> if no contained sub {@link FeatureDefinition} has the given name
	 */	
	@Override
	public FeatureDefinition getNamedSubFeatureDefinition(String name) {
		
		// Get the FeatureDefinition of the owning Parameter
		FeatureDefinition featureDef = getFeatureDefinition();
				
		// Ask the associated FeatureDefinition to find the named sub-FeatureDefintiion
		if ( featureDef!=null ) {
			
			if ( featureDef instanceof BoolAttributeDef ) {
			
				BoolAttributeDef boolAttribDef = (BoolAttributeDef)featureDef;
					
				if ( value ) {
					return boolAttribDef.getTrueValue().findNamedSubFeature(name);
					
				} else {
					return boolAttribDef.getFalseValue().findNamedSubFeature(name);
				}
			}
		}
		
		return null;
	}	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		FeatureDefinition featureDef=getFeatureDefinition();

		if ( featureDef!=null ) {
			
			if ( !(featureDef instanceof BoolAttributeDef) ) {
			
				// [1] The featureDefinition of a BooleanValue must be a BoolAttributeDef instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign a boolean value to the non-boolean type attribute '"+featureDef.getName()+"'",getLineNo()));
			}
		}			
	}	
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param value the <code>boolean</code> value this value represents
	 * @param lineNo the line number at which the element appears.
	 */
	public BooleanValue(Parameter parameter,boolean value,int lineNo) {
		super(parameter,lineNo);
		this.value=value;
	}	
}
