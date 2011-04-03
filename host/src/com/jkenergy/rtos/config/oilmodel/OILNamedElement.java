package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/OILNamedElement.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * Abstract base class for all OIL elements that may contain a name and/or description.
 * 
 * @author Mark Dixon
 *
 */
public abstract class OILNamedElement extends OILElement {

	/**
	 * The name associated with this element
	 * @see #getName()
	 */
	private String name;	
	
	/**
	 * The description associated with this element
	 * @see #getDescription()
	 */
	private String description;	

	/**
	 * @return Name associated with the element
	 */
	public String getName() {
		return name;
	}	

	/**
	 * Sets the name associated with the element
	 * @param newName
	 */
	public void setName(String newName) {
		name = newName;
	}	
	
	/**
	 * @return Description associated with the element
	 */
	public String getDescription() {
		return description;
	}	

	/**
	 * Sets the description associated with the element
	 * @param newDescription
	 */
	public void setDescription(String newDescription) {
		description = newDescription;
	}	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
	
	}	
	
	/**
	 * 
	 * @param lineNo the line number at which the element appears.
	 */
	protected OILNamedElement(int lineNo) {
		super(lineNo);
	}	
	
	/**
	 * @param name the name of the Element
	 * @param lineNo the line number at which the element appears.
	 */
	protected OILNamedElement(String name,int lineNo) {
		super(lineNo);
		this.name=name;
	}
}
