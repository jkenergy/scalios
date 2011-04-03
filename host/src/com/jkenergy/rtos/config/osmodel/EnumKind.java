package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/EnumKind.java $
 * 
 */

/**
 * Abstract base class for all Enumeration types. This will eventually be replaced by the Enum type provided by Java 1.5 and above.
 * 
 * @author Mark Dixon
 *
 */
public abstract class EnumKind {
	
	private String name;
	private int value;


	public int getValue() {
		return value;
	}
	
	public boolean equals(Object o) {
		if ( o==this ) return true;
		
		if ( !(o instanceof EnumKind) || o.getClass()!=this.getClass() )
			return false;
		
		return (this.value==((EnumKind)o).getValue());
	}
	
	public String toString() {
		return name;
	}
		
	public int hashCode() {
		return value;
	}

	/**
	 * Only derived classes can construct instances.
	 */
	protected EnumKind(int value, String name) {

		this.name=name;
		this.value=value;
	}
	
}
