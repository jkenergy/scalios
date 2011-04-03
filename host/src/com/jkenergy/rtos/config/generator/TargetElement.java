package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetElement.java $
 * 
 */

import java.util.Collection;

import com.jkenergy.rtos.config.osmodel.OSModelElement;

/**
 * Abstract base class for all intermediate target elements used during generation.
 * 
 * @author Mark Dixon
 *
 */

abstract class TargetElement {

	/**
	 * Constant used to indicate no control block for the element.
	 * @see #getControlBlockIndex()
	 */
	public static final int NO_CONTROL_BLOCK = -1;
	
	/**
	 * The OSModelElement within the OS model which this TargetElement represents.
	 * May be null if doesn't represent an OSModelElement.
	 */
	private OSModelElement osModelElement;
	
	/**
	 * The name of the element
	 * @see #getName()
	 */
	private String name;
	
	/**
	 * Flag indicating whether this element should have a handle during generation.
	 */
	private boolean hasHandle = true;
	
	/**
	 * The TargetCpu that contains this element
	 * @see #getTargetCpu()
	 */
	private TargetCpu targetCpu;
	

	/**
	 * The index of the element within the array of control blocks, NO_CONTROL_BLOCK indicates no control block for the element.
	 * @see #setControlBlockDetails(int, String)
	 * @see #getControlBlockIndex() 
	 */
	private int controlBlockIndex = NO_CONTROL_BLOCK;	
	
	
	/**
	 * The name of the control block to which the index applies.
	 * @see #setControlBlockDetails(int, String)
	 * @see #getControlBlockName() 
	 */
	private String controlBlockName;
	
	/**
	 * @param controlBlockIndex The controlBlockIndex to set.
	 * @param controlBlockName The controlBlockName to set.
	 */
	protected void setControlBlockDetails(int controlBlockIndex, String controlBlockName) {
		
		assert this.controlBlockIndex == NO_CONTROL_BLOCK;	// these details can only be set once
		
		this.controlBlockIndex = controlBlockIndex;
		this.controlBlockName = controlBlockName;
	}

	/**
	 * @return Returns the index of the element within the collection of control blocks, NO_CONTROL_BLOCK indicates no control block for the element.
	 */
	protected int getControlBlockIndex() {
		return controlBlockIndex;
	}
	
	/**
	 * @return Returns the name of the element's control block collection, null indicates no control block for the element.
	 */
	protected String getControlBlockName() {
		return controlBlockName;
	}	
	
	
	/**
	 * Sets the name of the element
	 * @param newName the new name of the element
	 */
	public void setName(String newName) {
		name=newName;
	}	
	
	/**
	 * 
	 * @return the name of the element
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the owning TargetCpu of the element
	 * @param newCpu the owning TargetCpu of the element
	 */
	public void setTargetCpu(TargetCpu newCpu) {
		targetCpu=newCpu;
	}
	
	/**
	 * Gets the owning TargetCpu of the element
	 * @return the cpu that owns the element
	 */
	public TargetCpu getTargetCpu() {
		return targetCpu;
	}


	/**
	 * Returns the TargetElement that represents the given OSModelElement (if any)
	 * 
	 * @param modelElement the OSModelElement from which the TargetElement was created.
	 * @return the TargetElement, null if no such TargetElement created.
	 * @see TargetCpu#addTargetElementMapping(TargetElement)
	 */
	protected <E extends TargetElement> E getTargetElement(OSModelElement modelElement) {
		
		return getTargetCpu().getTargetElement(modelElement);
	}	
	
	/**
	 * Returns a collection of TargetElement instances that represent the given OSModelElement instances.
	 * 
	 * @param modelElements collection of OSModelElement instances from which the TargetElement were created.
	 * @return the collection of TargetElement instance, empty if no TargetElement instances were created.
	 */
	protected <E extends TargetElement> Collection<E> getAllTargetElements(Collection<? extends OSModelElement> modelElements) {

		return getTargetCpu().getAllTargetElements(modelElements);
	}
	

	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	protected void initialiseModelAssociations() {
		// do nothing in base class
	}

	/**
	 * @return Returns the osModelElement.
	 */
	protected OSModelElement getOsModelElement() {
		return osModelElement;
	}	

	/**
	 * Static helper that returns the last TargetElement of the given element collection (if one exists)
	 * @param elements the collection of TargetElement instances
	 * @return the last element in the collection, else null if the collection is empty
	 */
	public final static TargetElement getLastElement(Collection<TargetElement> elements) {
		
		if ( !elements.isEmpty() ) {
			TargetElement[] elementArray = elements.toArray(new TargetElement[0]);
		
			return elementArray[elementArray.length-1];
		}

		return null;
	}
	
	
	/**
	 * Standard Constructor that creates a TargetElement that does not represent a OSModelElement.
	 * @param targetCpu the TargetCpu that owns the element
	 */
	protected TargetElement(TargetCpu targetCpu) {
		this.targetCpu = targetCpu;
		
		// Not created to represent an OSModelElement so do not add to OSModelElement->TargetElement map
	}	
	
	/**
	 * Standard Constructor that creates a TargetElement that does not represent a OSModelElement.
	 * @param targetCpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetElement(TargetCpu targetCpu, String name) {
		this.targetCpu = targetCpu;
		this.name = name;
		
		// Not created to represent an OSModelElement so do not add to OSModelElement->TargetElement map
	}

	/**
	 * Copy constructor that creates a TargetElement that represents an OSModelElement.
	 * 
	 * @param targetCpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetElement(TargetCpu targetCpu, OSModelElement osModelElement) {
		this.targetCpu = targetCpu;
		this.osModelElement = osModelElement;

		// copy required info. from the given OSModelElement
		this.name = osModelElement.getName();
		
		if (targetCpu != null) {
			// created to represent an OSModelElement so add self to the OSModelElement->TargetElement map
			targetCpu.addTargetElementMapping(this);
		}
	}

	/**
	 * @return the hasHandle
	 */
	public boolean getHasHandle() {
		return hasHandle;
	}

	/**
	 * @param hasHandle the hasHandle to set
	 */
	public void setHasHandle(boolean hasHandle) {
		this.hasHandle = hasHandle;
	}	
	
}
