package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/BoolAttributeDef.java $
 * 
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of AttributeDefinition used to define a boolean type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class BoolAttributeDef extends AttributeDefinition {
	

	/**
	 * The contained true {@link BoolValueDef} instance (models any sub-parameter definitions).
	 */
	private BoolValueDef trueValue = null;
	
	/**
	 * The contained false {@link BoolValueDef} instance
	 */	
	private BoolValueDef falseValue = null;

	
	/**
	 * The default value for the BoolAttributeDef
	 * @see AttributeDefinition#hasDefault
	 */
	private boolean defaultValue=false;
	
	
	/**
	 * @return The default value for the BoolAttributeDef
	 */
	public boolean getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * Sets the new default value for the BoolAttributeDef
	 * @param newDefaultValue
	 */
	public void setDefaultValue(boolean newDefaultValue) {
		
		defaultValue=newDefaultValue;
		hasDefault(true);
	}
	
	/**
	 * Gets (and creates if required) the true value contained by the BoolAttributeDef
	 * 
	 * @return The true {@link BoolValueDef} associated with the BoolAttributeDef
	 */
	public BoolValueDef getTrueValue() {
		
		if ( trueValue==null )
			trueValue=new BoolValueDef(getLineNo());
		
		return trueValue;
	}

	/**
	 * @return <code>true</code> if the a true {@link BoolValueDef} is associated with the BoolAttributeDef
	 */
	public boolean hasTrueValue() {
		
		return ( trueValue!=null );
	}	
	
	/**
	 * Gets (and creates if required) the false value contained by the BoolAttributeDef
	 * 
	 * @return The false {@link BoolValueDef} associated with the BoolAttributeDef
	 */
	public BoolValueDef getFalseValue() {
		
		if ( falseValue==null )
			falseValue=new BoolValueDef(getLineNo());
		
		return falseValue;
	}	

	/**
	 * @return <code>true</code> if the a false {@link BoolValueDef} is associated with the BoolAttributeDef
	 */
	public boolean hasFalseValue() {
		
		return ( falseValue!=null );
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
			BoolAttributeDef otherAttribute = (BoolAttributeDef)otherFeature;
			
			// ensure that otherAttribute does not have its true/false values defined if this BoolAttributeDef does
			if ( !otherAttribute.hasTrueValue() && trueValue!=null )
				return false; // this can't be a restricted version, since other has no true value
			
			if ( !otherAttribute.hasFalseValue() && falseValue!=null )
				return false; // this can't be a restricted version, since other has no false value
			
			return true; // this can be a restricted version of the other attribute
			
		}
		return false;
	}	
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element

		// [1] If subParameters defined within any contained BoolValueDef instances have the same name,
		// then they must also be based on the same type.
		
		if ( trueValue!=null && falseValue!=null ) {
			
			Map<String, FeatureDefinition> tmpMap=new HashMap<String, FeatureDefinition>();
					
			for (FeatureDefinition nextFeature : trueValue.getSubFeatures()) {
				
				tmpMap.put(nextFeature.getName(),nextFeature);
			}
			
			for (FeatureDefinition nextFeature : falseValue.getSubFeatures()) {
			
				FeatureDefinition otherFeature = tmpMap.get(nextFeature.getName());
				
				if ( otherFeature!=null ) {
					// another subFeature has the same name, so confirm it has same type
					if ( !nextFeature.isSameTypeAs(otherFeature) )
						problems.add(new Problem(Problem.ERROR,"Attempt to redeclare existing Parameter type '"+nextFeature.getName()+"' within common conditional attribute",otherFeature.getLineNo()));
				}				
			}
		}
		
		// Do check on contained elements
		if ( deepCheck ) {
		
			if ( trueValue!=null )
				trueValue.doModelCheck(problems,deepCheck);
			
			if ( falseValue!=null )
				falseValue.doModelCheck(problems,deepCheck);			
		}
	}	
	
	/**
	 * @param lineNo the line number at which the element appears.
	 */
	public BoolAttributeDef(int lineNo) {
		super(lineNo);

	}	
}
