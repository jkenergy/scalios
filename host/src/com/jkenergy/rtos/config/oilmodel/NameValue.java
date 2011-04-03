package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/NameValue.java $
 * 
 */

import java.util.Collection;
import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of ParameterizedValue used to represent both enumeration and object reference OIL attribute values (i.e. an enum or reference value with sub-parameter values).
 * 
 * @author Mark Dixon
 *
 */
public class NameValue extends ParameterizedValue {

	/**
	 * This stores a value for either a {@link ReferenceDef} or a {@link EnumerationAttributeDef},
	 * since both of these are a Name based value. If the value represents a ReferenceDef
	 * then the {@link #referencedObject} destination is present. If the value represents an
	 * EnumerationAttributeDef then one or more "subParameters" may exist.
	 */
	
	/**
	 * The primitive Value of the NameValue. This should be the name of an {@link EnumeratorDef}
	 * or the name of an {@link ObjectDefinition} within the {@link ApplicationDefinition}.
	 */
	private String value;
	

	/**
	 * Reference to the {@link ObjectDefinition} to which this NameValue refers. ({@link ReferenceDef} based values only)
	 * This is the cache for a derived value.
	 * @see #getReferencedObject
	 */	
	private ObjectDefinition referencedObject=null;	
	
	
	/**
	 * @return The assigned value
	 */
	public String getValue() {
		return value;
	}	
	
	
	
	/**
	 * Identifies the actual {@link ObjectDefinition} that is referenced by this ReferenceValue  ({@link ReferenceDef} based values only)
	 * @return {@link ObjectDefinition} that is referenced by this NameValue, null if no {@link ObjectDefinition} has name==value
	 */
	public ObjectDefinition getReferencedObject() {
		
		if ( referencedObject==null ) {
			
			// referencedObject not cached, so need to calculate
			
			// Get owning Parameter
			Parameter param=getParameter();
			
			if ( param!=null ) {
				
				// Get the ApplicationDefinition that eventually owns the Parameter
				ApplicationDefinition appDef=param.getApplicationDefinition();
				
				// Ask the ApplicationDefinition to find an ObjectDefinition with the name equals to value
				if (appDef!=null)
					referencedObject=appDef.findNamedObjectDefinition(value);
			}
		}
		
		return referencedObject;
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
			
			if ( featureDef instanceof EnumerationAttributeDef ) {
				
				EnumeratorDef enumDef=((EnumerationAttributeDef)featureDef).findNamedEnumerator(value);
				
				if ( enumDef!=null ) {
					return enumDef.getSubFeatures();
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
				
		// Ask the associated FeatureDefinition to find the named sub-FeatureDefinition
		if ( featureDef!=null ) {
			
			if ( featureDef instanceof EnumerationAttributeDef ) {
			
				EnumeratorDef enumDef=((EnumerationAttributeDef)featureDef).findNamedEnumerator(value);
				
				if ( enumDef!=null ) {
					return enumDef.findNamedSubFeature(name);
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
		
			if ( featureDef instanceof EnumerationAttributeDef ) {

				// [2] If the featureDefinition refers to an EnumerationAttributeDef instance, then the value must
				// be equal to the name of one of the EnumeratorDef objects contained by the EnumerationAttributeDef instance.				
				if ( ((EnumerationAttributeDef)featureDef).findNamedEnumerator(value)==null )
					problems.add(new Problem(Problem.ERROR,"Attempt to specify a value for '"+featureDef.getName()+"' that is not defined as an enumerator value",getLineNo()));
				
			}
			else if ( featureDef instanceof ReferenceDef ) {
				
				ObjectDefinition refObj=getReferencedObject();

				if ( refObj!=null ) {
					// [4] If the featureDefinition refers to a ReferenceDef instance, then the referencedObject 
					// must be an ObjectDefinition whose objectType is equal to the refType specified within
					// the ReferenceDef instance.
					if ( !refObj.getObjectType().equals(((ReferenceDef)featureDef).getRefType()) ) {
						problems.add(new Problem(Problem.ERROR,"Attempt to reference an object of the incorrect type",getLineNo()));						
					}
				}
				else {
					// [3] If the featureDefinition refers to a ReferenceDef instance, then the value must be equal
					// to the name of one of the ObjectDefinition instances.
					problems.add(new Problem(Problem.ERROR,"Attempt to reference a non-existent object '"+getValue()+"'",getLineNo()));
				}
			}
			else {
				// [1] The featureDefinition of a NameValue must be either an EnumerationAttributeDef instance or a ReferenceDef instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign a name value to the non-name typed parameter '"+featureDef.getName()+"'",getLineNo()));
			}
		}		
	}
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param value the Name value this value represents
	 * @param lineNo the line number at which the element appears. 
	 */
	public NameValue(Parameter parameter, String value,int lineNo) {
		super(parameter,lineNo);
		this.value=value;
	}	
}
