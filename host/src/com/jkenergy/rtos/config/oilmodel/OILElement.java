package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/OILElement.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

/**
 * Abstract base class for all OIL model elements.<br><br>
 * 
 * Declared as implementing the Checkable interface, which must therefore be implemented in derived classes.
 *
 * @author Mark Dixon
 * @see Checkable
 */
public abstract class OILElement implements Checkable {

	
	/**
	 * The line number at which the element appears within the source file
	 */
	private int lineNo=0;
	

	/**
	 * Sets the line number of the element
	 * @param newLineNo the new line number of the element
	 */
	public void setLineNo(int newLineNo) {
		
		lineNo=newLineNo;
	}
	
	/**
	 * 
	 * @return the line number at which the lement appeared within the source OIL file.
	 */
	public int getLineNo() {
		return lineNo;
	}
	
	/**
	 * Method used to normalise elements within the model
	 *
	 * A model should be normalised prior to being checked using the Checkable interface methods.
	 * 
	 * Derived classes should override this as required.
	 */
	public void normaliseModel() {
		
	}
	
	
	/**
	 * Helper method that calls normaliseModel() for each OILElement within the
	 * given collection.
	 * 
	 * @param collection the collection that contains the elements on which to call normaliseModel() 
	 */
	protected void normaliseCollection(Collection<? extends OILElement> collection) {

		for (OILElement next : collection) {
				((OILElement)next).normaliseModel();
		}
	}
	
	/**
	 * Helper method that calls doModelCheck() for each OILElement within the
	 * given collection.
	 * 
	 * @param collection the collection that contains the elements on which to call doModelCheck() 
	 * @param problems the list of problems to be added to during the model check
	 * @param deepCheck flag that indicates whether or not the collection elements should be checked
	 */
	protected void checkCollection(Collection<? extends OILElement> collection, List<Problem> problems, boolean deepCheck) {

		if ( deepCheck ) {
			
			for (OILElement next : collection) {
				
				((OILElement)next).doModelCheck(problems,deepCheck);
			}
		}
	}

	/**
	 * Helper method that checks if the given Collection contains a {@link OILNamedElement} with the given name
	 * 
	 * @param collection the collection that contains the elements to check for an element with the given name
	 * @param name the name to check for
	 * @param ignoreObject an object that is to be ignored during the search, if null no objects are ignored
	 * @return an OILNamedElement from the collection with the given name, null if no element has the given name
	 */
	protected OILNamedElement findNamedElement(Collection<? extends OILElement> collection,String name,Object ignoreObject) {
		
		if ( name!=null ) {	
			for (OILElement next : collection) {
				
				if (next instanceof OILNamedElement) {
					if ( name.equals( ((OILNamedElement)next).getName() ) && next!=ignoreObject )
						return (OILNamedElement)next;
				}
			}
		}
		
		return null;
	}	
	
	/**
	 * Helper method that checks if the given Collection contains a {@link OILNamedElement} with the given name
	 * 
	 * @param collection the collection that contains the elements to check for an element with the given name
	 * @param name the name to check for
	 * @return an OILNamedElement from the collection with the given name, null if no element has the given name
	 */
	protected OILNamedElement findNamedElement(Collection<? extends OILElement> collection,String name) {
		
		// use overloaded version, passing null as ignoreObject
		return findNamedElement(collection,name,null);
	}
	

	/**
	 * Implements the constraint checking method that is declared within the {@link Checkable} interface
	 * 
	 * @param problems List of {@link Problem} objects to be appended to when problems found.
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		// Do check of this element
	}	
	
	/**
	 * 
	 * @param lineNo the line number at which the element appears.
	 */
	protected OILElement(int lineNo) {
		this.lineNo=lineNo;
	}
}
