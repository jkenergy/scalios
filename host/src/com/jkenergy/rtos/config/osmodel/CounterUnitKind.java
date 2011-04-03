package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/CounterUnitKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of counter unit.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 */
public final class CounterUnitKind extends EnumKind {

	public static final int TICKS = 0;
	public static final int NANOSECONDS = 1;
	
	
	/**
	 * The '<em><b>TICKS</b></em>' literal object.
	 * @see #TICKS
	 */
	public static final CounterUnitKind TICKS_LITERAL = new CounterUnitKind(TICKS, "TICKS");

	/**
	 * The '<em><b>NANOSECONDS</b></em>' literal object.
	 * @see #NANOSECONDS
	 */
	public static final CounterUnitKind NANOSECONDS_LITERAL = new CounterUnitKind(NANOSECONDS, "NANOSECONDS");


	/**
	 * An array of all the '<em><b>CounterUnitKind</b></em>' enumerators.
	 */
	private static final CounterUnitKind[] VALUES_ARRAY =
		new CounterUnitKind[] {
			TICKS_LITERAL,
			NANOSECONDS_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>CounterUnitKind</b></em>' enumerators.
	 */
	public static final List<CounterUnitKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>CounterUnitKind</b></em>' literal with the specified name.
	 */
	public static CounterUnitKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			CounterUnitKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>CounterUnitKind</b></em>' literal with the specified value.
	 */
	public static CounterUnitKind get(int value) {
		switch (value) {
			case TICKS: return TICKS_LITERAL;
			case NANOSECONDS: return NANOSECONDS_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private CounterUnitKind(int value, String name) {

		super(value,name);
	}
	
}
