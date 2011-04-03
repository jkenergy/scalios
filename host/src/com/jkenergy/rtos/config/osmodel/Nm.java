package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Nm.java $
 * 
 */

/**
 * A SubClass of OSModelElement that models a Nm within the OS.<br><br>
 * 
 * These are very simple objects that contain no attribute values, except those inherited from OSModelElement (e.g. a name).
 * 
 * @author Mark Dixon
 *
 */
public class Nm extends OSModelElement {

	public Nm(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
