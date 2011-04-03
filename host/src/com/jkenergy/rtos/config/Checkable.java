package com.jkenergy.rtos.config;

import java.util.List;


/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/Checkable.java $
 * 
 */

/**
 * The Checkable interface defines the methods that need to be implemented
 * by a model element that provides semantic constraint checking facilities.
 * 
 * @author Mark Dixon
 *
 */
public interface Checkable {

	/**
	 * Performs a check of the associated model.
	 * 
	 * @param problems List of Problem objects to which broken constraints should be added.
	 * @param deepCheck flag indicating whether a deep check of the model should be performed.
	 * @see Problem
	 */
	public void doModelCheck(List<Problem> problems,boolean deepCheck);
}
