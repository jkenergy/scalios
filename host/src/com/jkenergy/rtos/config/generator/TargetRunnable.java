package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetRunnable.java $
 * 
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import com.jkenergy.rtos.config.osmodel.Runnable;


/**
 * Intermediate target element used to store information on Runnables to be generated. This class acts as a super class
 * for {@link TargetTask} and {@link TargetISR} classes, modelling stack size and priority details (since both are common to tasks and ISRs).
 * 
 * @author Mark Dixon
 *
 */

public abstract class TargetRunnable extends TargetElement {

	/**
	 * The size of the stack requested for the runnable.
	 */
	private long modelStackSize;	
	
	/**
	 * isAutoStackSize flag that specifies whether the stack size is to be automatically calculated
	 */
	private boolean isAutoStackSize=false;	
	
	/**
	 * The model priority allocated to the Runnable.
	 */
	private long modelPriority;	
	
	
	/**
	 * The target priority of the Runnable,
	 * this may (or may not) be an index into a priority-to-IPL lookup array.
	 */
	private int targetPriority;	
	
	
	/**
	 * The offset (in bytes) applied to a Runnable's stack that identifies the TOS check value
	 * This is defined in a positive sense but may be numerically negative in certain cases, e.g.
	 * on a dsPIC a task stack usage of 0 gives a stack offset of -2 when using hardware stack checking (SPLIM)
	 */
	private long stackOffset;		
	
	
	/****************************************************************************
	 * Data members setup by {@link #initialiseModelAssociations()}
	 */

	/**
	 * Set of TargetResource instances accessed by the TargetRunnable
	 */
	private Collection<TargetResource> targetResources = new LinkedHashSet<TargetResource>();
	
	
	/**
	 * Set of messages that are sent and/or received by the TargetRunnable. Inverse of {@link TargetMessage#getTargetRunnables()}
	 */
	private Collection<TargetMessage> targetMessages = new HashSet<TargetMessage>();		
	
	
	///////////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the isAutoStackSize.
	 */
	protected boolean isAutoStackSize() {
		return isAutoStackSize;
	}

	/**
	 * @return Returns the stackOffset.
	 */
	protected long getStackOffset() {
		return stackOffset;
	}

	/**
	 * @param stackOffset The stackOffset to set.
	 */
	protected void setStackOffset(long stackOffset) {
		this.stackOffset = stackOffset;
	}

	/**
	 * @return the targetResources
	 */
	protected Collection<TargetResource> getTargetResources() {
		return targetResources;
	}

	/**
	 * @return the targetMessages that are accessed by the runnable
	 */
	public Collection<TargetMessage> getTargetMessages() {
		return targetMessages;
	}

	/**
	 * Adds the given resource to the list of TargetResources accessed by this Runnable.
	 * 
	 * @param resource the TargetResource to be added.
	 */
	protected void addAccessedResource(TargetResource resource) {
		targetResources.add(resource);
	}
	
	/**
	 * @return Returns the modelPriority.
	 */
	protected long getModelPriority() {
		return modelPriority;
	}

	/**
	 * @param modelPriority The modelPriority to set.
	 */
	protected void setModelPriority(long modelPriority) {
		this.modelPriority = modelPriority;
	}

	/**
	 * @return Returns the modelStackSize.
	 */
	protected long getModelStackSize() {
		return modelStackSize;
	}

	/**
	 * @param modelStackSize The modelStackSize to set.
	 */
	protected void setModelStackSize(long modelStackSize) {
		this.modelStackSize = modelStackSize;
	}

	/**
	 * @return Returns the targetPriority.
	 */
	public int getTargetPriority() {
		return targetPriority;
	}

	/**
	 * @param targetPriority The targetPriority to set.
	 */
	public void setTargetPriority(int targetPriority) {
		this.targetPriority = targetPriority;
	}	
		
	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		
		super.initialiseModelAssociations();
		
		Runnable runnable = getRunnable();
		
		if (runnable != null) {
			
			targetResources = getAllTargetElements(runnable.getResources());
			
			targetMessages = getAllTargetElements(runnable.getAccessedMessages());
		}
	}	
	
	
	/**
	 * @return Returns the OS Model Runnable on which the TargetRunnable is based (if any)
	 */
	public Runnable getRunnable() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Runnable;
		
		return (Runnable)getOsModelElement();
	}	
	
	/**
	 * Standard Constructor that creates a TargetRunnable that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetRunnable(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	/**
	 * Copy contructor that creates a TargetRunnable that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetRunnable(TargetCpu cpu, Runnable osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		modelPriority = osModelElement.getPriority();
		modelStackSize = osModelElement.getStackSize();
		isAutoStackSize = osModelElement.isAutoStackSize();
	}	
	
}
