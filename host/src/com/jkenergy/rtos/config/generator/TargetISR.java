package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-26 22:59:46 +0000 (Sat, 26 Jan 2008) $
 * $LastChangedRevision: 588 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/TargetISR.java $
 * 
 */

import com.jkenergy.rtos.config.osmodel.Isr;


/**
 * Intermediate target element used to store information on ISRs to be generated.
 * 
 * @author Mark Dixon
 *
 */

public class TargetISR extends TargetRunnable {
	
	
	/**
	 * String that indicates the ISR modelVector. How this value is interpreted is platform specific.
	 */
	private String modelVector;
	
	/**
	 * Flag indicating category of the ISR
	 */	
	private boolean isCategory2;
	
	/**
	 * Flag indicating whether stack checking is enabled for this specific ISR
	 */
	private boolean isStackCheckingEnabled;	
	
	/**
	 * Returns true if stack checking is enabled for this ISR and globally within the TargetCPU.
	 * 
	 * @return Returns the isStackCheckingEnabled.
	 */
	public boolean isStackCheckingEnabled() {
		return (getTargetCpu().isStackCheckingEnabled() && isStackCheckingEnabled);
	}

	/**
	 * @return Returns the modelVector.
	 */
	protected String getModelVector() {
		return modelVector;
	}

	/**
	 * @return Returns the isCategory2.
	 */
	protected boolean isCategory2() {
		return isCategory2;
	}

	/**
	 * @param isCategory2 The isCategory2 to set.
	 */
	protected void setCategory2(boolean isCategory2) {
		this.isCategory2 = isCategory2;
	}

	/**
	 * Sets up the internal associations to referenced elements
	 *
	 */
	@Override
	protected void initialiseModelAssociations() {
		super.initialiseModelAssociations();
	}	
	
	
	/**
	 * @return Returns the OS Model ISR on which the TargetISR is based (if any)
	 */
	public Isr getISR() {
		
		assert  getOsModelElement() == null || getOsModelElement() instanceof Isr;
		
		return (Isr)getOsModelElement();
	}		


	/**
	 * Standard Constructor that creates a TargetISR that does not represent a OSModelElement.
	 * @param cpu the TargetCpu that owns the element
	 * @param name the name of the element
	 */
	protected TargetISR(TargetCpu cpu, String name) {
		super(cpu, name);
	}

	
	/**
	 * Sets up the stack values for the ISR.
	 * 
	 * @param platformInfo information about the platform for which the stack data is to be setup.
	 */
	protected void setupStackData(PlatformInfo platformInfo) {
			
		// Calculate stack offset value in bytes
		
		// The stack offset is always positive, the kernel applies the offset in a target specific manner.
		long stackOffset;
		
		if (isAutoStackSize()) {
			// Use default ISR stack size recommended for the target if auto stacksize required.
			stackOffset = platformInfo.getDefaultISRStackSize();
		}
		else {
			// Use stack size requested by the user if non-auto stacksize.
			stackOffset = getModelStackSize();
		}
		
		stackOffset += platformInfo.getKernelPreISREntryUsage();
		stackOffset += platformInfo.getTopOfStackCheckOffset();

		setStackOffset(stackOffset);
	}	
	
	
	/**
	 * Copy contructor that creates a TargetISR that represents a OSModelElement.
	 * 
	 * @param cpu the TargetCpu that owns the element
	 * @param osModelElement the osModelElement which this element is to represent.
	 */
	protected TargetISR(TargetCpu cpu, Isr osModelElement) {
		
		super(cpu, osModelElement);
		
		// copy required info. from the given OSModelElement
		isCategory2 = (osModelElement.getCategory() == 2);
		modelVector = osModelElement.getVector();
		isStackCheckingEnabled = osModelElement.isStackCheckingEnabled();
	}
}
