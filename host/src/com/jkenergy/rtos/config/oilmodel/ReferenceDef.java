package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ReferenceDef.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * This is a SubClass of FeatureDefinition that stores information relating to the (meta-model) definition of a reference OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class ReferenceDef extends FeatureDefinition {

	/**
	 * The default value of the '{@link #getRefType() <em>refType</em>}' attribute.
	 * @see #getRefType()
	 */
	private static final ObjectKind REF_TYPE_DEFAULT = ObjectKind.UNDEFINED_LITERAL;

	/**
	 * The referenced object Type of this reference
	 * @see #getRefType()
	 */
	private ObjectKind refType=REF_TYPE_DEFAULT;		


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
			ReferenceDef otherRef = (ReferenceDef)otherFeature;
			
			// check that both references are based on same refType
			if ( refType.equals(otherRef.getRefType()) ) {
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
		
		return false; // ReferenceDef instances can't be further restricted 
	}	
	
	/**
	 * @return type of the referenced object
	 * @see ObjectKind
	 */
	public ObjectKind getRefType() {
		return refType;
	}
	
	/**
	 * Sets the type of the referenced object.
	 * 
	 * @param newRefType the type of the referenced object
	 * @see ObjectKind 
	 */
	public void setRefType(ObjectKind newRefType) {
		refType = (newRefType == null) ? REF_TYPE_DEFAULT : newRefType;
	}

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
	}	
	
	/**
	 * 
	 * @param refType the type of the referenced object
	 * @param name hte name of the ReferenceDef
	 * @param lineNo the line number at which the element appears.
	 * @see ObjectKind
	 */
	public ReferenceDef(ObjectKind refType,String name,int lineNo) {
		super(name,lineNo);
		setRefType(refType);
	}
}
