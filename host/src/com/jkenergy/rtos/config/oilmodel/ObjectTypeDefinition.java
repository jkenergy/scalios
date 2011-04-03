package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ObjectTypeDefinition.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * This class is used to define an available Object type (e.g. a Task type, ISR type, Event type etc.) within the OIL (meta) Model.
 * 
 * @see ObjectKind
 * @author Mark Dixon
 *
 */
public class ObjectTypeDefinition extends OILNamedElement {

	
	/**
	 * The default value of the '{@link #getObjectType() <em>objectType</em>}' attribute.
	 * @see #getObjectType()
	 */
	private static final ObjectKind OBJECT_TYPE_DEFAULT = ObjectKind.UNDEFINED_LITERAL;
	
	
	/**
	 * The object type for which this definition is defined
	 * @see #getObjectType()
	 */
	private ObjectKind objectType=OBJECT_TYPE_DEFAULT;	
	

	/**
	 * The map of that contains {@link FeatureDefinition} instance: maps from {@link String}(name)->{@link FeatureDefinition}
	 */
	private Map<String, FeatureDefinition> features=new HashMap<String, FeatureDefinition>();	

	
	/**
	 * @return the objectType of the object type definition
	 * @see ObjectKind
	 */
	public ObjectKind getObjectType() {
		return objectType;
	}

	/**
	 * Sets the object type of the object type definition
	 * @param newObjectType
	 * @see ObjectKind 
	 */
	public void setObjectType(ObjectKind newObjectType) {
		objectType = (newObjectType == null) ? OBJECT_TYPE_DEFAULT : newObjectType;
	}
	
	/**
	 * @return the {@link FeatureDefinition} instances associated with the object type definition
	 */
	public Collection<FeatureDefinition> getFeatures() {
		return features.values();
	}	
	
	/**
	 * Adds the given {@link FeatureDefinition} to the collection maintained by the ObjectTypeDefinition.
	 * If an existing {@link FeatureDefinition} has the same name as the one given then - <br>
	 * 	
	 * 		If the new feature is a restricted version of the existing feature, the existing feature is
	 * 		replaced and true is returned.<br>
	 * 
	 * 		If the new feature is not a restricted version of the existing feature, the existing feature is
	 * 		not replaced and false is returned.
	 *  
	 * @param featureDef The {@link FeatureDefinition} to be added
	 * @return <code>true</code> if feature added (or existing feature restricted), <code>false</code> if named feature already exists 
	 */
	public boolean addFeature(FeatureDefinition featureDef) {
		
		if ( featureDef!=null ) {
			
			// check for existing feature with same name
			FeatureDefinition existingFeature=findNamedFeature(featureDef.getName());
			
			if ( existingFeature!=null ) {
				// an Existing feature has the same name, so check if new feature is a restriction of that
				if ( featureDef.isRestrictedVersionOf(existingFeature) ) {
					
					// replace existing feature with new restricted version
					features.put(featureDef.getName(),featureDef);
					return true;
				}
			}
			else {
				// existing feature with same name does not exist, so just add new feature
				features.put(featureDef.getName(),featureDef);	
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns a contained {@link FeatureDefinition} with the given name.
	 * 
	 * @param name the name of the {@link FeatureDefinition} to find
	 * @return a {@link FeatureDefinition} with the given name, <code>null</code> if no contained FeatureDefinition has the given name
	 */	
	public FeatureDefinition findNamedFeature(String name) {
		
		return (FeatureDefinition)features.get(name);
	}
	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// NOTE: The following constraint is implciitly applied (see addFeature()), so no need to explicitly check for this
		// [1] The name of each contained FeatureDefinition must be unique when compared to each other
		// contained FeatureDefinition.
		
		// Do check on contained elements (use helper defined in avstract class)
		checkCollection(features.values(),problems,deepCheck);
	}	
	
	
	/**
	 * @param objectType the object type of the new definition
	 * @param lineNo the line number at which the element appears.
	 * @see ObjectKind
	 */	
	public ObjectTypeDefinition(ObjectKind objectType,int lineNo) {
		super(lineNo);
		this.objectType=objectType;
	}
	
}
