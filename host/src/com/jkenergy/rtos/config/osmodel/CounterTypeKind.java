package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/CounterTypeKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types counter.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 *
 */
public final class CounterTypeKind extends EnumKind {

	public static final int SOFTWARE = 0;
	public static final int HARDWARE = 1;
	
	
	/**
	 * The '<em><b>SOFTWARE</b></em>' literal object.
	 * @see #SOFTWARE
	 */
	public static final CounterTypeKind SOFTWARE_LITERAL = new CounterTypeKind(SOFTWARE, "SOFTWARE");

	/**
	 * The '<em><b>HARDWARE</b></em>' literal object.
	 * @see #HARDWARE
	 */
	public static final CounterTypeKind HARDWARE_LITERAL = new CounterTypeKind(HARDWARE, "HARDWARE");


	/**
	 * An array of all the '<em><b>CounterTypeKind</b></em>' enumerators.
	 */
	private static final CounterTypeKind[] VALUES_ARRAY =
		new CounterTypeKind[] {
			SOFTWARE_LITERAL,
			HARDWARE_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>CounterTypeKind</b></em>' enumerators.
	 */
	public static final List<CounterTypeKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>CounterTypeKind</b></em>' literal with the specified name.
	 */
	public static CounterTypeKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			CounterTypeKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>CounterTypeKind</b></em>' literal with the specified value.
	 */
	public static CounterTypeKind get(int value) {
		switch (value) {
			case SOFTWARE: return SOFTWARE_LITERAL;
			case HARDWARE: return HARDWARE_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private CounterTypeKind(int value, String name) {

		super(value,name);
	}
	
}
