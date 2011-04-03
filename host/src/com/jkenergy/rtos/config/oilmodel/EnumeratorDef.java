package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/EnumeratorDef.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * This class stores information relating to an available enumeration label and the allowable sub-parameter definitions.
 * 
 * @author Mark Dixon
 *
 */
public class EnumeratorDef extends ParameterizedAttributeDef {
	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
	}	
	
	/**
	 * 
	 * @param name the name of the enumerator definition
	 * @param lineNo the line number at which the element appears.
	 */
	public EnumeratorDef(String name,int lineNo) {
		super(name,lineNo);
	}
	
}
