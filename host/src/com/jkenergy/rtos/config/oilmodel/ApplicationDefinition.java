package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ApplicationDefinition.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * The class manages all the data (that defines the object names, parameters values etc.) that exist within the imported OIL file.
 *  
 * @author Mark Dixon
 *
 */
public class ApplicationDefinition extends OILNamedElement {

	/**
	 * The OILDefinition in which this ApplicationDefinition is defined
	 */
	private OILDefinition definition;

	/**
	 * The map of that contains ObjectDefinitions: maps from {@link String}(name)->{@link ObjectDefinition}.
	 * Uses an ordered map so iteration (e.g. for model checks) is done in predictable order.
	 */
	private Map<String, ObjectDefinition> objects = new LinkedHashMap<String, ObjectDefinition>();

	/**
	 * Adds a new {@link ObjectDefinition} instance to this
	 * ApplicationDefinition
	 * 
	 * @param obj the {@link ObjectDefinition} to be added
	 */
	public void addObjectDefinition(ObjectDefinition obj) {

		if (obj != null) {
			objects.put(obj.getName(), obj);
		}
	}

	/**
	 * @return {@link ObjectDefinition} instances associated with the
	 *         ApplicationDefinition
	 */
	public Collection<ObjectDefinition> getObjectDefinitions() {
		return objects.values();
	}

	/**
	 * Returns a contained {@link ObjectDefinition} with the given name.
	 * 
	 * @param name
	 *            the name of the {@link ObjectDefinition} to find
	 * @return an {@link ObjectDefinition} with the given name, null if no
	 *         contained {@link ObjectDefinition} has the given name
	 */
	public ObjectDefinition findNamedObjectDefinition(String name) {

		return (ObjectDefinition) objects.get(name);
	}

	/**
	 * Returns the {@link ObjectTypeDefinition} that has the given objectType
	 * 
	 * @param objType
	 *            for which the {@link ObjectTypeDefinition} is required
	 * @return the {@link ObjectTypeDefinition} that has the given objectType
	 */
	public ObjectTypeDefinition getObjectTypeDefinition(ObjectKind objType) {

		return definition.getObjectTypeDefinition(objType);
	}

	@Override
	public void doModelCheck(List<Problem> problems, boolean deepCheck) {
		super.doModelCheck(problems, deepCheck);

		// Do check of this element

		// Do check on contained elements (use helper defined in abstract class)
		checkCollection(objects.values(), problems, deepCheck);
	}

	/**
	 * @param definition
	 *            the {@link OILDefinition} in which this ApplicationDefinition
	 *            is defined
	 * @param name
	 *            the name of the ApplicationDefinition
	 * @param lineNo
	 *            the line number at which the element appears.
	 */
	public ApplicationDefinition(OILDefinition definition, String name,
			int lineNo) {
		super(name, lineNo);
		this.definition = definition;
	}
}
