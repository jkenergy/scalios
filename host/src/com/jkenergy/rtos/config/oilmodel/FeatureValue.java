package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/FeatureValue.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * Abstract class that acts as the base class to all parameter (attribute) values within the OIL Model.
 *   
 * @author Mark Dixon
 *
 */
public abstract class FeatureValue extends OILElement {

	/**
	 * Reference to the {@link Parameter} for which this value is defined
	 */
	private Parameter parameter;
		
	/**
	 * 
	 * @return {@link Parameter} for which this value is defined
	 */
	protected Parameter getParameter() {
		return parameter;
	}

	/**
	 * Gets the {@link FeatureDefinition} of which this values owning {@link Parameter} is an instance
	 * @return the {@link FeatureDefinition} of which the owning {@link Parameter} is an instance, null if there is no associated {@link FeatureDefinition}
	 */	
	public FeatureDefinition getFeatureDefinition() {
		
		if ( parameter!=null )
			return parameter.getFeatureDefinition();
		
		return null;
	}
	
	/**
	 * 
	 * @return the description associated with the {@link Parameter} for which this value is defined
	 */
	public String getDescription() {

		if ( parameter!=null ) {
			return parameter.getDescription();
		}
		return null;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element	
	}	
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param lineNo the line number at which the element appears.
	 */
	protected FeatureValue(Parameter parameter,int lineNo) {
		super(lineNo);
		this.parameter=parameter;
	}
}
