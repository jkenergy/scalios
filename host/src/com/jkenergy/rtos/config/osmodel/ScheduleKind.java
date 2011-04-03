package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ScheduleKind.java $
 * 
 */

import java.util.*;

/**
 * Enumeration type that represents different types of task schedulability.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class ScheduleKind extends EnumKind {

	public static final int NON = 0;
	public static final int FULL = 1;
	
	
	/**
	 * The '<em><b>NON</b></em>' literal object.
	 * @see #NON
	 */
	public static final ScheduleKind NON_LITERAL = new ScheduleKind(NON, "NON");

	/**
	 * The '<em><b>FULL</b></em>' literal object.
	 * @see #FULL
	 */
	public static final ScheduleKind FULL_LITERAL = new ScheduleKind(FULL, "FULL");


	/**
	 * An array of all the '<em><b>Schedule Kind</b></em>' enumerators.
	 */
	private static final ScheduleKind[] VALUES_ARRAY =
		new ScheduleKind[] {
			NON_LITERAL,
			FULL_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>Schedule Kind</b></em>' enumerators.
	 */
	public static final List<ScheduleKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Schedule Kind</b></em>' literal with the specified name.
	 */
	public static ScheduleKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ScheduleKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Schedule Kind</b></em>' literal with the specified value.
	 */
	public static ScheduleKind get(int value) {
		switch (value) {
			case NON: return NON_LITERAL;
			case FULL: return FULL_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ScheduleKind(int value, String name) {

		super(value,name);
	}	
	
}
