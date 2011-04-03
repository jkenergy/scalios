package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ComStatusKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different COM status types.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class ComStatusKind extends EnumKind {

	public static final int COMSTANDARD = 0;
	public static final int COMEXTENDED = 1;
	
	
	/**
	 * The '<em><b>COMSTANDARD</b></em>' literal object.
	 * @see #COMSTANDARD
	 */
	public static final ComStatusKind COMSTANDARD_LITERAL = new ComStatusKind(COMSTANDARD, "COMSTANDARD");

	/**
	 * The '<em><b>COMEXTENDED</b></em>' literal object.
	 * @see #COMEXTENDED
	 */
	public static final ComStatusKind COMEXTENDED_LITERAL = new ComStatusKind(COMEXTENDED, "COMEXTENDED");


	/**
	 * An array of all the '<em><b>ComStatusKind</b></em>' enumerators.
	 */
	private static final ComStatusKind[] VALUES_ARRAY =
		new ComStatusKind[] {
			COMSTANDARD_LITERAL,
			COMEXTENDED_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>ComStatusKind</b></em>' enumerators.
	 */
	public static final List<ComStatusKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>ComStatusKind</b></em>' literal with the specified name.
	 */
	public static ComStatusKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ComStatusKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>ComStatusKind</b></em>' literal with the specified value.
	 */
	public static ComStatusKind get(int value) {
		switch (value) {
			case COMSTANDARD: return COMSTANDARD_LITERAL;
			case COMEXTENDED: return COMEXTENDED_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ComStatusKind(int value, String name) {

		super(value,name);
	}
	
}
