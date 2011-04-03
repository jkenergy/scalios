package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/EnumerationAttributeDef.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of AttributeDefinition used to define an enumeration type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class EnumerationAttributeDef extends AttributeDefinition {
	
	/**
	 * The default Enumerator value (name) for the EnumerationAttributeDef
	 */
	private String defaultValue;
	
	/**
	 * The map of that contains {@link EnumeratorDef} instances: maps from {@link String}(name)->{@link EnumeratorDef}.
	 * The insertion and iteration order is maintained, i.e. elements are ordered on insertion time
	 */
	private Map<String, EnumeratorDef> enumerators = new LinkedHashMap<String, EnumeratorDef>();	
	
	/**
	 * @return The default value for the EnumerationAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Sets the new default value for the EnumerationAttributeDef
	 * @param newDefaultValue
	 * @see AttributeDefinition#hasDefault
	 */
	public void setDefaultValue(String newDefaultValue) {
		
		defaultValue=newDefaultValue;
		hasDefault(( newDefaultValue!=null ));
	}

	/**
	 * Adds the given {@link EnumeratorDef} to the collection maintained by the EnumerationAttributeDef.
	 * If an existing {@link EnumeratorDef} has the same name as the one given then the new {@link EnumeratorDef}
	 * is not added and <code>false</code> is returned. i.e. {@link EnumeratorDef} names must be unique.
	 *
	 * @param enumDef The {@link EnumeratorDef} to be added 
	 * @return <code>true</code> if enumerator added, <code>false</code> if enumerator already exists with the given name 
	 */	
	public boolean addEnumerator(EnumeratorDef enumDef) {
		
		if ( enumDef!=null ) {
			
			// Check if existing enumerator has same name as the new enumerator
			if ( findNamedEnumerator(enumDef.getName())==null ) {
				// no existing enumerator has the same name so add to the map
				enumerators.put(enumDef.getName(),enumDef);
				return true;
			}
		}
		return false;		
	}	
	
	/**
	 * @return Collection of {@link EnumeratorDef} instances associated with the EnumerationAttributeDef
	 */
	public Collection<EnumeratorDef> getEnumerators() {
		return enumerators.values();
	}
	
	/**
	 * Identifies the actual {@link EnumeratorDef} that is the default for the EnumerationAttributeDef
	 * @return {@link EnumeratorDef} that is the default, <code>null</code> if no default specified
	 */
	public EnumeratorDef getDefaultEnumerator() {
		
		if ( hasDefault() )
			return findNamedEnumerator(defaultValue);
		
		return null;
	}	

	/**
	 * Returns a contained {@link EnumeratorDef} with the given name.
	 * 
	 * @param name the name of the {@link EnumeratorDef} to find
	 * @return the {@link EnumeratorDef} with the given name, <code>null</code> if no contained EnumeratorDef has the given name
	 */	
	public EnumeratorDef findNamedEnumerator(String name) {
		
		return (EnumeratorDef)enumerators.get(name);
	}	

	/*
	 * Check if the feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is a restricted version
	 * @return true if restricted version, else false
	 */
	@Override
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		if ( super.isRestrictedVersionOf(otherFeature) ) {
			
			// can safely cast since we know that FeatureDefinition is based on same class as this instance
			EnumerationAttributeDef otherAttribute = (EnumerationAttributeDef)otherFeature;
			
			// Confirm that each contained EnumeratorDef exists in the otherAttribute
			for (EnumeratorDef next : enumerators.values() ) {
				if ( otherAttribute.findNamedEnumerator(next.getName())==null )
					return false;
			}			
			
			return true;	// this is a restricted version of the otherAttribute
		}
		return false;
	}

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// NOTE: The following constraint is implicitly applied (see addEnumerator()), so no need to explicitly check for this		
		// [1] The name of each contained EnumeratorDef must be unique when compared to each other contained EnumeratorDef.
		
		// [2] If given, the defaultValue attribute must be equal to the name of one of the contained EnumeratorDef objects.
		if ( hasDefault()==true && findNamedEnumerator(defaultValue)==null )
			problems.add(new Problem(Problem.ERROR, "Attempt to specify a default value to '"+getName()+"' that is not defined as an enumerator value",getLineNo()));

		// [3] If subParameters defined within any contained EnumeratorDef instances have the same name,
		// then they must also be based on the same type.
		
		Map<String, FeatureDefinition>  tmpMap = new HashMap<String, FeatureDefinition>();
			
		for (EnumeratorDef next : enumerators.values() ) {

			Collection<FeatureDefinition> subFeatures = next.getSubFeatures();
			
			for (FeatureDefinition nextFeature : subFeatures ) {
								
				FeatureDefinition otherFeature = tmpMap.get(nextFeature.getName());
				
				if ( otherFeature!=null ) {
					// another subFeature has the same name, so confirm it has same type
					if ( !nextFeature.isSameTypeAs(otherFeature) )
						problems.add(new Problem(Problem.ERROR,"Attempt to redeclare existing Parameter type '"+nextFeature.getName()+"' within common conditional attribute",otherFeature.getLineNo()));
				}
				else {
					tmpMap.put(nextFeature.getName(),nextFeature);
				}
			}
		}
		
		// Do check on contained elements (use helper defined in abstract class)
		checkCollection(enumerators.values(),problems,deepCheck);
	}	
	
	/**
	 * @param lineNo the line number at which the element appears.
	 */
	public EnumerationAttributeDef(int lineNo) {
		super(lineNo);

	}	
}
