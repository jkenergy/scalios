package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ImplementationDefinition.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * The class manages all the meta-data (that defines the allowed OIL object parameters etc.) that exist within the associated OIL application definition.
 * 
 * @author Mark Dixon
 *
 */
public class ImplementationDefinition extends OILNamedElement {

	
	/**
	 * The map of that contains {@link ObjectTypeDefinition} instances: maps from {@link ObjectKind}->{@link ObjectTypeDefinition}.
	 * Uses an ordered map so iteration (e.g. for model checks) is done in predictable order.
	 */
	private Map<ObjectKind, ObjectTypeDefinition> objectTypeDefinitions=new LinkedHashMap<ObjectKind, ObjectTypeDefinition>();	
	
	

	/**
	 * @return {@link ObjectTypeDefinition} instances associated with the ImplementationDefinition
	 */
	public Collection<ObjectTypeDefinition> getObjectTypeDefinitions() {
		return objectTypeDefinitions.values();
	}	
	
	
	/**
	 * Gets the {@link ObjectTypeDefinition} of the given {@link ObjectKind} type contained by this ImplementationDefinition
	 * If an {@link ObjectTypeDefinition} of the given type does not already exist, then creates one.
	 * 
	 * @param objType the {@link ObjectKind} of the {@link ObjectTypeDefinition} to be accessed/created.
	 * @return existing or created {@link ObjectTypeDefinition}
	 */	
	public ObjectTypeDefinition getObjectTypeDefinition(ObjectKind objType) {
		
		ObjectTypeDefinition objTypeDef = (ObjectTypeDefinition)objectTypeDefinitions.get(objType);
		
		if ( objTypeDef==null ) {
			// An ObjectTypeDefinition based on the given ObjectKind does not exist, so create one
			
			objTypeDef = new ObjectTypeDefinition(objType,0); // pass line number of 0 when created, caller must set this
			
			objectTypeDefinitions.put(objType,objTypeDef);
		}
		return objTypeDef;
	}
	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// Do check on contained elements (use helper defined in abstract class)
		checkCollection(objectTypeDefinitions.values(),problems,deepCheck);
	}
	
	
	/**
	 * 
	 * @param lineNo the line number at which the element appears.
	 */
	public ImplementationDefinition(int lineNo) {
		super(lineNo);
	}	

	/**
	 * @param name the name of the definition
	 * @param lineNo the line number at which the element appears.
	 */
	public ImplementationDefinition(String name,int lineNo) {

		super(name,lineNo);
	}	
	
}
