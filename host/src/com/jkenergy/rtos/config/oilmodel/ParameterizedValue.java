package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ParameterizedValue.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * An abstract SubClass of FeatureValue used to represent a parameterized OIL attribute value (i.e. a value with sub-parameter values).
 * 
 * @author Mark Dixon
 *
 */
public abstract class ParameterizedValue extends FeatureValue {

	/*
	 * The ordered set of SubParameters associated with this BooleanValue
	 */
	private Collection<Parameter> subParameters=new LinkedHashSet<Parameter>();		
	
	
	public void addSubParameter(Parameter param) {
		subParameters.add(param);
	}
	
	/**
	 * @return SubParameters associated with the BooleanValue
	 */
	public Collection<Parameter> getSubParameters() {
		return subParameters;
	}

	/**
	 * Returns a sub-FeatureDefinition with the given name, that is defined within the
	 * FeatureDefinition of which this ParameterizedValue's owning Parameter is an instance.
	 * 
	 * @param name the name of the sub-FeatureDefinition to find
	 * @return a sub-FeatureDefinition with the given name, null if no contained sub-FeatureDefinition has the given name
	 */	
	public abstract FeatureDefinition getNamedSubFeatureDefinition(String name);
	
	/**
	 * 
	 * @return A Collection of sub-FeatureDefinition instances that are defined within the associated FeatureDefinition
	 */
	public abstract Collection<FeatureDefinition> getSubFeatures();

	
	/**
	 * Gets the first contained SubParameter that has the given name.
	 * @param name the name of the sub-parameter to return
	 * @return the Parameter with the given name, null if no such sub-parameter exists
	 */
	public Parameter getNamedSubParameter(String name) {
				
		for ( Parameter next : subParameters ) {
		
			if ( next.getName().equals(name) )
				return next;
		}
		return null;
	}

	/**
	 * Gets all contained SubParameters that have the given name.
	 * @param name the name of the sub-parameters to return
	 * @return array of the Parameters with the given name
	 */
	public Parameter [] getNamedSubParameterList(String name) {
		
		Collection<Parameter> paramList=new HashSet<Parameter>();
		
		for ( Parameter next : subParameters ) {
		
			if ( next.getName().equals(name) )
				paramList.add(next);
		}

		return paramList.toArray(new Parameter[0]);
	}	
	
	/**
	 * Gets the FeatureValue of the first contained SubParameter that has the given name.
	 * @param name the name of the sub-parameter value to return
	 * @return the FeatureValue of the Parameter with the given name, null if no such parameter exists
	 */
	public FeatureValue getNamedSubParameterValue(String name) {
		
		Parameter param=getNamedSubParameter(name);
		
		if ( param !=null ) {
			return param.getValue();
		}
		
		return null;
	}	
	
	/**
	 * Gets the FeatureValues of all contained Sub-Parameters that have the given name.
	 * @param name the name of the sub-parameters of the values to return
	 * @return array of the FeatureValues of the Parameters with the given name
	 */
	public FeatureValue [] getNamedSubParameterValueList(String name) {
		
		Collection<FeatureValue> valueList=new HashSet<FeatureValue>();
		
		for ( Parameter next : subParameters ) {
		
			if ( next.getName().equals(name) ) {
				
				FeatureValue value=next.getValue();
				
				if ( value!=null ) valueList.add(value);
			}
		}

		return valueList.toArray(new FeatureValue[0]);
	}	
	
	
	/** Define implementation of check method that is declared within the Checkable interface
	 * 
	 * @param problems List of Problem objects, should be appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Problem
	 */
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element

		Collection<FeatureDefinition> subFeatures = getSubFeatures();
		
		// Construct a set of All sub-FeatureDefinitions that are defined in the associated ObjectTypeDefinition
		Set<FeatureDefinition> availableFeatures = (subFeatures!=null) ? new HashSet<FeatureDefinition>(subFeatures) : new HashSet<FeatureDefinition>();
		
		// iterate over sub-parameter values defined within this value
		
		for (Parameter next : subParameters) {

			FeatureDefinition featureDef=next.getFeatureDefinition();
			
			if ( featureDef!=null ) {
				// Have identified associated FeatureDefinition of the parameter

				// Remove from the availableFeatures set
				if ( availableFeatures.remove(featureDef)==false ) {
					
					// FeatureDefinition already has a Parameter defined (since already removed from availableFeatures set)
					// so ensure it is allowed multiple values
					if ( !featureDef.isMultiple() ) {

						// [2] Each contained subParameter must refer to a different featureDefinition unless that
						// referenced FeatureDefinition’s isMultiple attribute is set to true.						
						problems.add(new Problem(Problem.ERROR,"Attempt to provide multiple values for singular parameter '"+featureDef.getName()+"'",next.getLineNo()));
					}
				}
				
				if ( deepCheck ) {
					// Do check on next contained Parameter
					next.doModelCheck(problems,deepCheck);
				}
			}
			else {
				// [1] The name of each contained subParameter must be equal to the name of a subFeature that is
				// contained by the owning parameter’s featureDefinition.
				problems.add(new Problem(Problem.ERROR,"The conditional Parameter '"+next.getName()+"' is not defined for this Implementation",next.getLineNo()));
			}
		}

		// availableFeatures now contains the FeatureDefinition instances for which no values were given within this ParameretizedValue
		
		// Check each of these to ensure that they have been assigned a default value or isMultiple
		for (FeatureDefinition next : availableFeatures) {
			
			// [3] A subParameter value must be provided for each FeatureDefinition that is contained by the associated
			// featureDefinition, unless the FeatureDefinition has its isMultiple attribute set to true,
			// or it is an AttributeDefinition that has a default value.
			if ( next instanceof AttributeDefinition ) {
				
				if ( !next.isMultiple() && !((AttributeDefinition)next).hasDefault() && !((AttributeDefinition)next).isAuto()) {
					String str="A value for the '"+next.getName()+"' Attribute must be provided within '"+getParameter().getName()+"'";
					problems.add(new Problem(Problem.ERROR,str,getLineNo()));
				}
			}
			else {
				if ( !next.isMultiple() ) {
					String str="A value for the '"+next.getName()+"' Reference must be provided within '"+getParameter().getName()+"'";
					problems.add(new Problem(Problem.ERROR,str,getLineNo()));
				}
			}
		}		
	}	
	
	/**
	 * @param parameter The Parameter instance for which this value is defined
	 * @param lineNo the line number at which the element appears.
	 */
	protected ParameterizedValue(Parameter parameter,int lineNo) {
		super(parameter,lineNo);
	}
}
