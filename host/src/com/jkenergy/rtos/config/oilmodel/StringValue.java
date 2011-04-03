package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/StringValue.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of FeatureValue used to represent a string OIL attribute value.
 * 
 * @author Mark Dixon
 *
 */
public class StringValue extends FeatureValue {

	/**
	 * The primitive Value of the StringValue 
	 */
	private String value;

	
	/**
	 * 
	 * @return The assigned value
	 */
	public String getValue() {
		return value;
	}
	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		FeatureDefinition featureDef=getFeatureDefinition();

		if ( featureDef!=null ) {
			
			if ( !(featureDef instanceof StringAttributeDef) ) {
			
				// [1] The featureDefinition of a StringValue must be a StringAttributeDef instance.
				problems.add(new Problem(Problem.ERROR,"Attempt to assign a string value to the non-string type attribute '"+featureDef.getName()+"'",getLineNo()));
			}
		}		
	}
	
	/**
	 * @param parameter the {@link Parameter} instance for which this value is defined
	 * @param value the string value that this value represents
	 * @param lineNo the line number at which the element appears.
	 */
	public StringValue(Parameter parameter, String value,int lineNo) {
		super(parameter,lineNo);
		this.value=value;
	}
}
