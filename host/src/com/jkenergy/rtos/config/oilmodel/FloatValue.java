package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/FloatValue.java $
 * 
 */

import java.math.BigDecimal;
import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of FeatureValue used to represent a floating point OIL attribute value.
 * 
 * @author Mark Dixon
 *
 */
public class FloatValue extends FeatureValue {

	/**
	 * The primitive Value of the FloatValue 
	 */
	private BigDecimal value;

	/**
	 * @return the assigned value
	 */
	public BigDecimal getValue() {
		return value;
	}	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		FeatureDefinition featureDef=getFeatureDefinition();

		if ( featureDef!=null ) {		
		
			if ( featureDef instanceof FloatAttributeDef ) {

				// [2] The value must be within the float range specified by the FloatAttributeDef typed featureDefinition.
				if ( !((FloatAttributeDef)featureDef).isWithinRange(value) )
					problems.add(new Problem(Problem.ERROR,"Attempt to specify a float value for '"+featureDef.getName()+"' that is not within the valid range",getLineNo()));
			}
			else {
				// [1] The featureDefinition of a FloatValue must be a FloatAttributeDef instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign a float value to the non-float type attribute '"+featureDef.getName()+"'",getLineNo()));
			}
		}
	}
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param value the {@link BigDecimal} value this value represents
	 * @param lineNo the line number at which the element appears. 
	 */
	public FloatValue(Parameter parameter,BigDecimal value,int lineNo) {
		super(parameter,lineNo);
		this.value=value;
	}	
}
