package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ObjectDefinition.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * This class is used to represent an Object value (e.g. a specific instance of Task, ISR, Event etc.) within the OIL Model.
 * 
 * @author Mark Dixon
 *
 */
public class ObjectDefinition extends OILNamedElement {

	/**
	 * The default value of the '{@link #getObjectType() <em>objectType</em>}' attribute.
	 * @see #getObjectType()
	 */
	private static final ObjectKind OBJECT_TYPE_DEFAULT = ObjectKind.UNDEFINED_LITERAL;

	/**
	 * The object Type of this object
	 * @see #getObjectType()
	 */
	private ObjectKind objectType=OBJECT_TYPE_DEFAULT;		
	
	/**
	 * The ordered set of {@link Parameter} instances (attributes/reference values) associated with this ObjectDefinition
	 */
	private Collection<Parameter> parameters=new LinkedHashSet<Parameter>();		
	
	
	/**
	 * The {@link ApplicationDefinition} that is the owner of this ObjectDefinition
	 */
	private ApplicationDefinition appDefinition=null;
	
	/**
	 * Reference to the {@link ObjectTypeDefinition} of which this ObjectDefinition is an instance.
	 * This is the cache for a derived value.
	 * @see #getObjectTypeDefinition
	 */
	private ObjectTypeDefinition objectTypeDefinition=null;
	
	
	/**
	 * @return type of the object
	 * @see ObjectKind
	 */
	public ObjectKind getObjectType() {
		return objectType;
	}
	
	/**
	 * Sets the type of the object
	 * @param newObjectType
	 * @see ObjectKind
	 */
	public void setObjectType(ObjectKind newObjectType) {
		objectType = (newObjectType == null) ? OBJECT_TYPE_DEFAULT : newObjectType;
	}	

	/**
	 * @return the {@link ApplicationDefinition} that is the owner of this ObjectDefinition
	 */	
	public ApplicationDefinition getApplicationDefinition() {
		return appDefinition;
	}

	
	/**
	 * Adds a new {@link Parameter} instance to this ObjectDefinition
	 * 
	 * @param param the {@link Parameter} to be added
	 */
	public void addParameter(Parameter param) {
		
		parameters.add(param);
	}	
	
	/**
	 * @return the {@link Parameter} instances associated with the ObjectDefinition
	 */
	public Collection<Parameter> getParameters() {
		return parameters;
	}	

	/**
	 * Gets the first contained {@link Parameter} that has the given name.
	 * @param name the name of the {@link Parameter} to return
	 * @return the {@link Parameter} with the given name, <code>null</code> if no such parameter exists
	 */
	public Parameter getNamedParameter(String name) {
		
		for ( Parameter next : parameters ) {
		
			if ( next.getName().equals(name) )
				return next;
		}
		return null;
	}

	/**
	 * Gets all contained {@link Parameter} instances that have the given name.
	 * @param name the name of the {@link Parameter} to return
	 * @return array of the {@link Parameter} instances with the given name
	 */
	public Parameter [] getNamedParameterList(String name) {
		
		Collection<Parameter> paramList=new HashSet<Parameter>();
			
		for ( Parameter next : parameters ) {

			if ( next.getName().equals(name) )
				paramList.add(next);
		}

		return paramList.toArray(new Parameter[0]);
	}	
	
	/**
	 * Gets the {@link FeatureValue} of the first contained {@link Parameter} that has the given name.
	 * @param name the name of the {@link Parameter} value to return
	 * @return the {@link FeatureValue} of the {@link Parameter} with the given name, <code>null</code> if no such {@link Parameter} exists
	 */
	public FeatureValue getNamedParameterValue(String name) {
		
		Parameter param=getNamedParameter(name);
		
		if ( param !=null ) {
			return param.getValue();
		}
		
		return null;
	}	
	
	/**
	 * Gets the {@link FeatureValue} instances of all contained {@link Parameter} instances that have the given name.
	 * @param name the name of the {@link Parameter} instances of the values to return
	 * @return array of the {@link FeatureValue} instances of the {@link Parameter} instances with the given name
	 */
	public Object [] getNamedParameterValueList(String name) {
		
		Collection<FeatureValue> valueList=new HashSet<FeatureValue>();
				
		for ( Parameter next : parameters ) {
		
			if ( next.getName().equals(name) ) {
				
				FeatureValue value=next.getValue();
				
				if ( value!=null ) valueList.add(value);
			}
		}

		return valueList.toArray();
	}	
	
	/**
	 * 
	 * @return the {@link ObjectTypeDefinition} of which this ObjectDefinition is an instance.
	 */
	public ObjectTypeDefinition getObjectTypeDefinition() {
	
		if ( objectTypeDefinition==null ) {
			// calculate cached value if not available
			objectTypeDefinition=appDefinition.getObjectTypeDefinition(objectType);
		}
		
		return objectTypeDefinition;
	}
	
	
	/**
	 * Returns a {@link FeatureDefinition} with the given name, that is defined within the
	 * {@link ObjectTypeDefinition} of which this ObjectDefinition is an instance.
	 * 
	 * @param name the name of the {@link FeatureDefinition} to find
	 * @return a {@link FeatureDefinition} with the given name, <code>null</code> if no contained {@link FeatureDefinition} has the given name
	 */	
	public FeatureDefinition getNamedFeatureDefinition(String name) {
		
		ObjectTypeDefinition objectTypeDefinition=getObjectTypeDefinition();
		
		// Ask the associated ObjectTypeDefinition to find the named FeatureDefintiion
		if ( objectTypeDefinition!=null ) {
			return objectTypeDefinition.findNamedFeature(name);		
		}
		
		return null;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
	
		// Construct a set of All FeatureDefinitions that are defined in the associated ObjectTypeDefinition
		ObjectTypeDefinition objTypeDef=getObjectTypeDefinition();
		
		Collection<FeatureDefinition> features = objTypeDef.getFeatures();
		
		Set<FeatureDefinition> availableFeatures=(features!=null) ? new HashSet<FeatureDefinition>(features) : new HashSet<FeatureDefinition>();

		// iterate over parameter values defined within this ObjectDefinition	
		for (Parameter next : parameters) {

			FeatureDefinition featureDef=next.getFeatureDefinition();
			
			if ( featureDef!=null ) {
				// Have identified associated FeatureDefinition of the parameter

				// Remove from the availableFeatures set
				if ( availableFeatures.remove(featureDef)==false ) {
					
					// FeatureDefinition already has a Parameter defined (since already removed from availableFeatures set)
					// so ensure it is allowed multiple values
					if ( !featureDef.isMultiple() ) {

						// [2] Each contained Parameter must refer to a different featureDefinition unless that
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
				// [1] The name of each contained Parameter must be equal to the name of a FeatureDefinition that is 
				//      contained by the associated ObjectTypeDefinition of which the ObjectDefinition is an instance.
				problems.add(new Problem(Problem.ERROR,"The given Parameter '"+next.getName()+"' is not defined for this Implementation",next.getLineNo()));
			}
		}
		
		// availableFeatures now contains the FeatureDefinition instances for which no values were given within this Object
		
		// Check each of these to ensure that they have been assigned a default value or isMultiple		
		for (FeatureDefinition next : availableFeatures) {
			
			// [3] A Parameter value must be provided for each FeatureDefinition that is contained by the associated
			// objectTypeDefinition, unless the FeatureDefinition has its isMultiple attribute set to true,
			// or it is an AttributeDefinition that has a default value.
			if ( next instanceof AttributeDefinition ) {
				
				if ( !next.isMultiple() && !((AttributeDefinition)next).hasDefault() && !((AttributeDefinition)next).isAuto() ) {
					String str="A value for the '"+next.getName()+"' Attribute must be provided for '"+getName()+"'";
					problems.add(new Problem(Problem.ERROR,str,getLineNo()));
				}
			}
			else {
				if ( !next.isMultiple() ) {
					String str="A value for the '"+next.getName()+"' Reference must be provided for '"+getName()+"'";
					problems.add(new Problem(Problem.ERROR,str,getLineNo()));
				}
			}
		}
	}
	
	
	/**
	 * @param appDefinition the owning {@link ApplicationDefinition}
	 * @param objectType the type of the ObjectDefinition
	 * @param name the name of the ObjectDefinition
	 * @param lineNo the line number at which the element appears.
	 * @see ObjectKind
	 */
	public ObjectDefinition(ApplicationDefinition appDefinition,ObjectKind objectType, String name, int lineNo) {

		super(name,lineNo);
		this.appDefinition=appDefinition;
		this.objectType=objectType;
	}	
}
