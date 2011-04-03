package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/FloatAttributeDef.java $
 * 
 */

import java.math.*;
import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of AttributeDefinition used to define a floating point type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class FloatAttributeDef extends AttributeDefinition {

	
	/**
	 * The minimum value that may be stored in this type, as defined by IEEE-754 standard
	 */
	private static final BigDecimal MIN_TYPE_VALUE = new BigDecimal(Float.MIN_VALUE);
	
	/**
	 * The maximum value that may be stored in this type, as defined by IEEE-754 standard
	 */	
	private static final BigDecimal MAX_TYPE_VALUE = new BigDecimal(Float.MAX_VALUE);
	
	/**
	 * The default value for the FloatAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	private BigDecimal defaultValue=new BigDecimal(BigInteger.ZERO);
	
	
	/**
	 * minValue that may be assigned to value instances, (specifies sub-range)
	 */	
	private BigDecimal minValue=MIN_TYPE_VALUE;
	
	/**
	 * maxValue that may be assigned to value instances, (specifies sub-range)
	 */		
	private BigDecimal maxValue=MAX_TYPE_VALUE;
	
	/**
	 * @return The default value for the FloatAttributeDef
	 * @see AttributeDefinition#hasDefault 
	 */
	public BigDecimal getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Sets the new default value for the FloatAttributeDef
	 * @param newDefaultValue
	 */
	public void setDefaultValue(BigDecimal newDefaultValue) {
		
		defaultValue=newDefaultValue;
		hasDefault(( newDefaultValue!=null ));
	}
	
	
	/**
	 * Sets the sub-range of allowed values that may be assigned to instances of this FloatAttributeDef
	 * @param newMinValue
	 * @param newMaxValue
	 * @see #isWithinRange
	 */
	public void setRange(BigDecimal newMinValue,BigDecimal newMaxValue) {
		minValue=newMinValue;
		maxValue=newMaxValue;
	}
	
	/**
	 * 
	 * @return minValue
	 */
	public BigDecimal getMinValue() { 
		return minValue;
	}

	/**
	 * 
	 * @return minValue
	 */
	public BigDecimal getMaxValue() { 
		return maxValue;
	}

	/**
	 * Checks whether the given value is within the range specified by this FloatAttributeDef
	 * based on the IEEE-754 standard.
	 * 
	 * @param value the value to check
	 * @return <code>true</code> if within range defined by this FloatAttributeDef, else <code>false</code>
	 */	
	private boolean isWithinTypeRange(BigDecimal value) {
		
		if ( value!=null ) {

			if ( value.compareTo(MIN_TYPE_VALUE)!=-1 && value.compareTo(MAX_TYPE_VALUE)!=1 )
				return true;	// value within range, i.e. minTypeValue<= value <=maxTypeValue
		}
		
		return false;
	}
	
	/**
	 * Checks whether the given value is within the range specified by this FloatAttributeDef
	 * based on any defined sub-range (min/maxValue limits)
	 * 
	 * @param value the value to check
	 * @return <code>true</code> if within sub-range defined by this FloatAttributeDef, else <code>false</code>
	 * @see #setRange
	 */
	public boolean isWithinRange(BigDecimal value) {
		
		// ensure within general type range first
		if ( isWithinTypeRange(value) ) {
			
			// no valueList given compare against min/max values
				
			if ( value.compareTo(minValue)!=-1 && value.compareTo(maxValue)!=1 )
				return true;	// value within range, i.e. minValue<= value <=maxValue
		}
		
		return false;
	}
	

	/**
	 * Check if the feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is a restricted version
	 * @return true if restriced version, else false
	 */
	@Override
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		if ( super.isRestrictedVersionOf(otherFeature) ) {
			
			// can safely cast since we know that FeatureDefinition is based on same class as this instance
			FloatAttributeDef otherAttribute = (FloatAttributeDef)otherFeature;
			
			// check that min and max values are within the range of the otherAttribute
			if ( otherAttribute.isWithinRange(minValue) && otherAttribute.isWithinRange(maxValue) )
				return true;
		}
		return false;
	}	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element

		// [1] Any given minValue and maxValue must be within the valid range of the float as determined by the IEEE-754 standard.
		if ( !isWithinTypeRange(minValue) || !isWithinTypeRange(maxValue) )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify a range on '"+getName()+"' outside that permitted for the type float",getLineNo()));
		
		// [2] Any given minValue must be less or equal to the given maxValue.
		if ( minValue.compareTo(maxValue)==1 )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify an invalid float range on '"+getName()+"'",getLineNo()));
		
		// [3] If given the defaultValue must be within any specified sub-range and the valid range of the float as determined by IEEE-754 standard.
		if ( hasDefault() && !isWithinRange(defaultValue) )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify a default float value on '"+getName()+"' that is not within the valid range",getLineNo()));
	}
	
	
	/**
	 * @param lineNo the line number at which the element appears.
	 */
	public FloatAttributeDef(int lineNo) {
		super(lineNo);

	}	
}
