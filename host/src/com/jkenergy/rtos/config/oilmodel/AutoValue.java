package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/AutoValue.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of FeatureValue used to represent an AUTO OIL attribute value.
 * 
 * @author Mark Dixon
 *
 */
public class AutoValue extends FeatureValue {


	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
	
		FeatureDefinition featureDef=getFeatureDefinition();

		if ( featureDef!=null ) {
			
			if ( (featureDef instanceof AttributeDefinition) ) {
			
				// [2] The isWithAuto attribute of the AttributeDefinition typed featureDefinition must be set to true.
				if ( ((AttributeDefinition)featureDef).isWithAuto()==false )
					problems.add(new Problem(Problem.ERROR,"Attempt to specify a value of AUTO for '"+featureDef.getName()+"' which is not defined using WITH_AUTO",getLineNo()));
			}
			else {
				// [1] The featureDefinition of an AutoValue must be an AttributeDefinition derived instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign an AUTO value to the non-attribute type parameter '"+featureDef.getName()+"'",getLineNo()));
			}
		}
	}
	
	/**
	 * @param parameter The {@link Parameter} instance for which this value is defined
	 * @param lineNo the line number at which the element appears.
	 */
	public AutoValue(Parameter parameter,int lineNo) {
		super(parameter,lineNo);
	}	
}
