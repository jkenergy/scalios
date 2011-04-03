package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/AttributeDescription.java $
 * 
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class allows the storing of either single attribute, or multi-attribute descriptions.<br><br>
 * 
 * Instances of this class represent a description associated with a specific attribute within an OSModelElement.<br><br>
 * 
 * Multi-attribute descriptions are stored by specifying an "index" as a key that allows each description
 * to be identified. This is required in the case where a OSModelElement has a single attribute which represents
 * a collection of attribute values, and hence each attribute value may have its own description.
 * 
 * @author Mark Dixon
 *
 */
public class AttributeDescription {

	/**
	 * Name of the  Attribute which the Description represents
	 */
	private String name;	
	
	/**
	 * Description for a single AttributeDescription
	 */
	private String description;
	
	/**
	 * Descriptions for a multi AttributeDescription, with a description for each different index
	 */	
	private Map<Integer, String> multiAttribDescription;
	
	/**
	 * 
	 * @return the name of the attribute with which the description is associated
	 */	
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the description for a single AttributeDescription, null if no single description
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * 
	 * @param index the index for which the description is required
	 * @return the description for a multi AttributeDescription, null if no description for given index
	 */
	public String getMultiDescription(int index) {
		
		if ( multiAttribDescription != null ) {			
			return multiAttribDescription.get(index);
		}
		
		return null;
	}
	
	/**
	 * Adds a new description to a specific multi-description index
	 * @param description the description to be added
	 * @param index the index for which the description is provided
	 */
	public void addMultiAttribDescription(String description, int index) {
		
		if ( description != null ) {
			if ( multiAttribDescription == null ) {
				multiAttribDescription = new LinkedHashMap<Integer, String>();
			}	
			
			multiAttribDescription.put(index, description);
		}
		else {
			if ( multiAttribDescription != null ) {
				multiAttribDescription.remove(index);
			}
		}
	}
	
	
	/**
	 * 
	 * @return true if a multi-AttribDescription, else false
	 */
	public boolean isMultiDescription() {
		return multiAttribDescription != null;
		
	}
	
	/**
	 * 
	 * @return the set of index values within a multi-AttribDescription, null if no descriptions exist
	 */
	public Set<Integer> getMultiDescriptionIndexValues() {
		
		if ( multiAttribDescription != null ) {
			return multiAttribDescription.keySet();
		}	
		return null;
	}
	
	/**
	 * Constructor for single AttributeDescription
	 * 
	 * @param name the attribute for which the description is provided
	 * @param description the description to be added
	 */
	public AttributeDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	/**
	 * Constructor for multi AttributeDescription
	 * 
	 * @param name the attribute for which the description is provided 
	 * @param description description the description to be added
	 * @param index the index for which the description is provided
	 */
	public AttributeDescription(String name, String description, int index) {
		this.name = name;
		addMultiAttribDescription(description, index);
	}	
}
