package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ParameterizedAttributeDef.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * 
 * An abstract class used to represent a parameterized OIL attribute (meta-model) definition (i.e. a type with sub-parameter type definitions).
 * 
 * @author Mark Dixon
 *
 */
public abstract class ParameterizedAttributeDef  extends OILNamedElement {


	/**
	 * A map that contains sub {@link FeatureDefinition}: maps from {@link String}(name)->{@link FeatureDefinition}.
	 * Uses an ordered map so iteration (e.g. for model checks) is done in predictable order.
	 */
	private Map<String, FeatureDefinition> subFeatures=new LinkedHashMap<String, FeatureDefinition>();		
	
	
	/**
	 * @return Collection of SubFeatures associated with the ParameterizedAttributeDef (each member will be unique)
	 */
	public Collection<FeatureDefinition> getSubFeatures() {
		return subFeatures.values();
	}	
	
	/**
	 * Adds the given {@link FeatureDefinition} to the collection maintained by the ParameterizedAttributeDef.
	 * If an existing {@link FeatureDefinition} has the same name as the one given then the new {@link FeatureDefinition}
	 * is not added and <code>false</code> is returned. i.e. {@link FeatureDefinition} names must be unique.
	 * 
	 * @param featureDef The {@link FeatureDefinition} to be added
	 * @return <code>true</code> if feature added, <code>false</code> if feature already exists with the given name 
	 */
	public boolean addSubFeature(FeatureDefinition featureDef) {
		
		if ( featureDef!=null ) {
			
			// Check if existing feature has same name as the new feature
			if ( findNamedSubFeature(featureDef.getName())==null ) {
				// no existing feature has the same name so add to the map
				subFeatures.put(featureDef.getName(),featureDef);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a contained sub {@link FeatureDefinition} with the given name.
	 * 
	 * @param name the name of the sub {@link FeatureDefinition} to find
	 * @return a {@link FeatureDefinition }with the given name, <code>null</code> if no contained sub {@link FeatureDefinition} has the given name
	 */	
	public FeatureDefinition findNamedSubFeature(String name) {
		
		return (FeatureDefinition)subFeatures.get(name);
	}
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element

		// NOTE: The following constraint is implicitly applied (see addSubFeature()), so no need to explicitly check for this
		// [1] The name of each contained FeatureDefinition must be unique when compared to each other
		// contained FeatureDefinition.
		
		
		// Do check on contained elements (use helper defined in abstract class)
		checkCollection(subFeatures.values(),problems,deepCheck);
	}		
	
	
	/**
	 * 
	 * @param lineNo the line number at which the element appears.
	 */
	protected ParameterizedAttributeDef(int lineNo) {
		super(lineNo);
	}	

	/**
	 * @param name the name of the ParameterizedAttributeDef
	 * @param lineNo the line number at which the element appears.
	 */	
	protected ParameterizedAttributeDef(String name,int lineNo) {
		super(name,lineNo);
	}
}
