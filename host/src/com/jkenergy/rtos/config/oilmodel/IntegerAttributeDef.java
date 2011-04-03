package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/IntegerAttributeDef.java $
 * 
 */

import java.util.*;
import java.math.BigInteger;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of AttributeDefinition used to define an integer type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class IntegerAttributeDef extends AttributeDefinition {

	/**
	 * specifies whether values may be signed or unsigned
	 */
	private boolean isUnsigned=false;
	
	/**
	 * specifies size of values that may be stored, e.g. 32 or 64
	 */	
	private int bitCount=32;
	
	
	/**
	 * The minimum value that may be stored in this type, derived from isUnsigned and bitCount values
	 */
	private BigInteger minTypeValue;
	
	/**
	 * The maximum value that may be stored in this type, derived from isUnsigned and bitCount values
	 */	
	private BigInteger maxTypeValue;
	
	/**
	 * The default value for the IntegerAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	private BigInteger defaultValue=BigInteger.ZERO;
	
	/**
	 * A list of values that defines the allowable values that may be assigned (specifies sub-range)
	 */
	private Collection<BigInteger> valueList=null;
	
	/**
	 * minValue that may be assigned to value instances (specifies sub-range)
	 */
	private BigInteger minValue;
	
	/**
	 * maxValue that may be assigned to value instances (specifies sub-range)
	 */	
	private BigInteger maxValue;


	/**
	 * @return isUnsigned flag
	 */
	public boolean isUnsigned() {
		return isUnsigned;
	}	

	/**
	 * @return bitCount of the integer attribute definition
	 */
	public int getBitCount() {
		return bitCount;
	}	
	
	/**
	 * @return The default value for the IntegerAttributeDef
	 */
	public BigInteger getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Sets the new default value for the IntegerAttributeDef
	 * @param newDefaultValue
	 */
	public void setDefaultValue(BigInteger newDefaultValue) {
		
		defaultValue=newDefaultValue;
		hasDefault(( newDefaultValue!=null ));
	}
	
	
	/**
	 * Sets the sub-range of allowed values that may be assigned to instances of this attribute
	 * @param newMinValue
	 * @param newMaxValue
	 * @see #isWithinRange
	 */
	public void setRange(BigInteger newMinValue,BigInteger newMaxValue) {
		minValue=newMinValue;
		maxValue=newMaxValue;
	}
	
	/**
	 * Appends the given value to the valueList that defines a valid sub-range
	 * 
	 * @param newValue
	 * @see #isWithinRange
	 */
	public void addListValue(BigInteger newValue) {
		
		if ( valueList==null )
			valueList=new ArrayList<BigInteger>();
		
		valueList.add(newValue);
	}

	/**
	 * @return The valueList of the IntegerAttributeDef
	 * @see #addListValue
	 */
	public Collection<BigInteger> getValueList() {
		return valueList;
	}	
	
	/**
	 * @return minValue
	 */
	public BigInteger getMinValue() { 
		return minValue;
	}

	/**
	 * @return maxValue
	 */
	public BigInteger getMaxValue() { 
		return maxValue;
	}	

	/**
	 * Checks whether the given value is within the range specified by this IntegerAttributeDef
	 * based on the minTypeValue and maxTypeValue attribute values.
	 * 
	 * @param value the value to check
	 * @return true if within range defined by this IntegerAttributeDef, else false
	 */	
	private boolean isWithinTypeRange(BigInteger value) {
		
		if ( value!=null ) {

			if ( value.compareTo(minTypeValue)!=-1 && value.compareTo(maxTypeValue)!=1 )
				return true;	// value within range, i.e. minTypeValue<= value <=maxTypeValue
		}
		
		return false;
	}
	
	/**
	 * Checks whether the given value is within the range specified by this IntegerAttributeDef
	 * based on any defined sub-range (either within the valueList or min//maxValue limits)
	 * 
	 * @param value the value to check
	 * @return true if within sub-range defined by this IntegerAttributeDef, else false
	 * @see #setRange
	 * @see #addListValue
	 */
	public boolean isWithinRange(BigInteger value) {
		
		// ensure within general type range first
		if ( isWithinTypeRange(value) ) {
			
			if ( valueList!=null ) {
				//	Check the value list for matching value
				for (BigInteger next : valueList ) {
					
					if ( value.compareTo(next)==0 )
						return true;	// value is within the valid valueList
				}
			}
			else {
				// no valueList given compare against min/max values
				
				if ( value.compareTo(minValue)!=-1 && value.compareTo(maxValue)!=1 )
					return true;	// value within range, i.e. minValue<= value <=maxValue
			}
		}
		
		return false;
	}

	/**
	 * Check if the feature is the same type as the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is to be compared
	 * @return <code>true</code> if based on same type, else <code>false</code>
	 */
	@Override
	boolean isSameTypeAs(FeatureDefinition otherFeature) {
		
		if ( super.isSameTypeAs(otherFeature) ) {
			
			// can safely cast since we know that FeatureDefinition is based on same class as this instance
			IntegerAttributeDef otherAttribute = (IntegerAttributeDef)otherFeature;
			
			// check sign and bitCount are the same
			if ( isUnsigned==otherAttribute.isUnsigned() && bitCount==otherAttribute.getBitCount() ) {
				return true;
			}
		}
		return false;
	}	
	
	
	/**
	 * Check if the feature is a restricted version of the given feature.
	 * 
	 * @param otherFeature the {@link FeatureDefinition} to which this one is a restricted version
	 * @return <code>true</code> if restriced version, else <code>false</code>
	 */
	@Override
	boolean isRestrictedVersionOf(FeatureDefinition otherFeature) {
		
		if ( super.isRestrictedVersionOf(otherFeature) ) {
			
			// can safely cast since we know that FeatureDefinition is based on same class as this instance
			IntegerAttributeDef otherAttribute = (IntegerAttributeDef)otherFeature;
			
			// check sign and bitCount are the same
			if ( isUnsigned==otherAttribute.isUnsigned() && bitCount==otherAttribute.getBitCount() ) {
				
				if ( valueList!=null ) {
					// Check that each member of value list is within range of the otherAttribute				
					for (BigInteger next : valueList ) {

						if ( !otherAttribute.isWithinRange(next) )
							return false;
					}
					
					return true;
				}
				else {
					// check that min and max values are within the range of the otherAttribute
					if ( otherAttribute.isWithinRange(minValue) && otherAttribute.isWithinRange(maxValue) )
						return true;
				}
			}
		}
		return false;
	}	
	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		//[1] Any given minValue and maxValue must be within the valid range of the integer as determined by the isUnsigned and bitCount attributes.
		if ( !isWithinTypeRange(minValue) || !isWithinTypeRange(maxValue) )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify a range for '"+getName()+"' outside that permitted for the type",getLineNo()));

		//[2] Any given valueList must only contain values that are within the valid range of the integer as determined by the isUnsigned and bitCount attributes.
		if ( valueList!=null ) {
			
			for (BigInteger next : valueList ) {				
				if ( !isWithinTypeRange(next) )
					problems.add(new Problem(Problem.ERROR,"Attempt to specify a range for '"+getName()+"' outside that permitted for the type",getLineNo()));
			}
		}
		
		//[3] Any given minValue must be less or equal to the given maxValue.
		if ( minValue.compareTo(maxValue)==1 )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify an invalid integer range for '"+getName()+"'",getLineNo()));
		
		//[4] If given the defaultValue must be within any specified sub-range and the valid range of the integer as determined by the isUnsigned and bitCount attributes.
		if ( hasDefault() && !isWithinRange(defaultValue) )
			problems.add(new Problem(Problem.ERROR,"Attempt to specify a default integer value for '"+getName()+"' that is not within the valid range",getLineNo()));
	}	

		
	
	/**
	 * 
	 * @param isUnsigned the isUnsigned flag
	 * @param bitCount the bitcount value
	 * @param lineNo the line number at which the element appears.
	 */
	public IntegerAttributeDef(boolean isUnsigned, int bitCount,int lineNo) {
		
		super(lineNo);
		
		this.isUnsigned=isUnsigned;
		this.bitCount=bitCount;
		
		// calculate the minTypeValue and maxTypeValue attributes depending on sign/bitCount
		
		if ( isUnsigned ) {
			minTypeValue=BigInteger.ZERO;
			
			// calc maxTypeValue as (2 raised to the power of bitCount)-1
			maxTypeValue=BigInteger.valueOf(2).pow(bitCount);
			maxTypeValue=maxTypeValue.subtract(BigInteger.ONE);			
		}
		else {
			// calc minTypeValue as -(2 raised to the power of bitCount-1)
			minTypeValue=BigInteger.valueOf(2).pow(bitCount-1);
			maxTypeValue=maxTypeValue.negate();
			
			// calc maxTypeValue as (2 raised to the power of bitCount-1)-1
			maxTypeValue=BigInteger.valueOf(2).pow(bitCount-1);
			maxTypeValue=maxTypeValue.subtract(BigInteger.ONE);			
		}
			
		// set the sub-range to be the same as the type range initially
		minValue = minTypeValue;
		maxValue = maxTypeValue;
	}
}
