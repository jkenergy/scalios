package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/IntegerValue.java $
 * 
 */

import java.math.BigInteger;
import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of FeatureValue used to represent an integer OIL attribute value.
 * 
 * @author Mark Dixon
 *
 */
public class IntegerValue extends FeatureValue {

	/**
	 * The primitive Value of the IntegerValue 
	 */
	private BigInteger value;

	
	/**
	 * @return The assigned value
	 */
	public BigInteger getValue() {
		return value;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);

		// Do check of this element
		
		FeatureDefinition featureDef=getFeatureDefinition();

		if ( featureDef!=null ) {		
		
			if ( featureDef instanceof IntegerAttributeDef ) {

				// [2] The value must be within the integer range specified by the IntegerAttributeDef typed featureDefinition.
				if ( !((IntegerAttributeDef)featureDef).isWithinRange(value) )
					problems.add(new Problem(Problem.ERROR,"Attempt to specify an integer value for '"+featureDef.getName()+"' that is not within the valid range",getLineNo()));
			}
			else {
				// [1] The featureDefinition of an IntegerValue must be an IntegerAttributeDef instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign an integer value to the non-integer type attribute '"+featureDef.getName()+"'",getLineNo()));
			}
		}
	}
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param value the {@link BigInteger} value this value represents
	 * @param lineNo the line number at which the element appears. 
	 */
	public IntegerValue(Parameter parameter,BigInteger value,int lineNo) {
		super(parameter,lineNo);
		this.value=value;
	}	
}
