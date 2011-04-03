package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ResourceKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * Enumeration type that represents different types of resources.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class ResourceKind extends EnumKind {

	public static final int STANDARD = 0;
	public static final int LINKED = 1;
	public static final int INTERNAL = 2;
	
	
	/**
	 * The '<em><b>STANDARD</b></em>' literal object.
	 * @see #STANDARD
	 */
	public static final ResourceKind STANDARD_LITERAL = new ResourceKind(STANDARD, "STANDARD");

	/**
	 * The '<em><b>LINKED</b></em>' literal object.
	 * @see #LINKED
	 */
	public static final ResourceKind LINKED_LITERAL = new ResourceKind(LINKED, "LINKED");

	/**
	 * The '<em><b>INTERNAL</b></em>' literal object.
	 * @see #INTERNAL
	 */
	public static final ResourceKind INTERNAL_LITERAL = new ResourceKind(INTERNAL, "INTERNAL");
	

	
	/**
	 * An array of all the '<em><b>Resource Kind</b></em>' enumerators.
	 */
	private static final ResourceKind[] VALUES_ARRAY =
		new ResourceKind[] {
			STANDARD_LITERAL,
			LINKED_LITERAL,
			INTERNAL_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>Resource Kind</b></em>' enumerators.
	 */
	public static final List<ResourceKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Resource Kind</b></em>' literal with the specified name.
	 */
	public static ResourceKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ResourceKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Resource Kind</b></em>' literal with the specified value.
	 */
	public static ResourceKind get(int value) {
		switch (value) {
			case STANDARD: return STANDARD_LITERAL;
			case LINKED: return LINKED_LITERAL;
			case INTERNAL: return INTERNAL_LITERAL;
		}
		return null;	
	}
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ResourceKind(int value, String name) {

		super(value,name);
	}	
	
}
