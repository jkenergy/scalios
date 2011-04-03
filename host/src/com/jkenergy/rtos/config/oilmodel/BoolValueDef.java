package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/BoolValueDef.java $
 * 
 */

import java.util.List;
import com.jkenergy.rtos.config.Problem;

/**
 * This class stores information relating to the sub-parameter definitions of a boolean type OIL attribute.
 * 
 * @author Mark Dixon
 *
 */
public class BoolValueDef extends ParameterizedAttributeDef {


	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
	}	
	
/**
 * 
 * @param lineNo the line number at which the element appears.
 */
	public BoolValueDef(int lineNo) {
		super(lineNo);
	}
}
