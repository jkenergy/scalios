package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/StatusKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of system build status.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class StatusKind extends EnumKind {

	public static final int STANDARD = 0;
	public static final int EXTENDED = 1;
	
	
	/**
	 * The '<em><b>STANDARD</b></em>' literal object.
	 * @see #STANDARD
	 */
	public static final StatusKind STANDARD_LITERAL = new StatusKind(STANDARD, "STANDARD");

	/**
	 * The '<em><b>EXTENDED</b></em>' literal object.
	 * @see #EXTENDED
	 */
	public static final StatusKind EXTENDED_LITERAL = new StatusKind(EXTENDED, "EXTENDED");


	/**
	 * An array of all the '<em><b>Status Kind</b></em>' enumerators.
	 */
	private static final StatusKind[] VALUES_ARRAY =
		new StatusKind[] {
			STANDARD_LITERAL,
			EXTENDED_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>Status Kind</b></em>' enumerators.
	 */
	public static final List<StatusKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Status Kind</b></em>' literal with the specified name.
	 */
	public static StatusKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			StatusKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Status Kind</b></em>' literal with the specified value.
	 */
	public static StatusKind get(int value) {
		switch (value) {
			case STANDARD: return STANDARD_LITERAL;
			case EXTENDED: return EXTENDED_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private StatusKind(int value, String name) {

		super(value,name);
	}
	
}
