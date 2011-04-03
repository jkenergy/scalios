package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/LockingTimeKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of locking time.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 *  
 * @author Mark Dixon
 *
 */
public final class LockingTimeKind extends EnumKind {

	public static final int RESOURCELOCK = 0;
	public static final int INTERRUPTLOCK = 1;
	
	
	/**
	 * The '<em><b>RESOURCELOCK</b></em>' literal object.
	 * @see #RESOURCELOCK
	 */
	public static final LockingTimeKind RESOURCELOCK_LITERAL = new LockingTimeKind(RESOURCELOCK, "RESOURCELOCK");

	/**
	 * The '<em><b>INTERRUPTLOCK</b></em>' literal object.
	 * @see #INTERRUPTLOCK
	 */
	public static final LockingTimeKind INTERRUPTLOCK_LITERAL = new LockingTimeKind(INTERRUPTLOCK, "INTERRUPTLOCK");


	/**
	 * An array of all the '<em><b>LockingTimeKind</b></em>' enumerators.
	 */
	private static final LockingTimeKind[] VALUES_ARRAY =
		new LockingTimeKind[] {
			RESOURCELOCK_LITERAL,
			INTERRUPTLOCK_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>LockingTimeKind</b></em>' enumerators.
	 */
	public static final List<LockingTimeKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>LockingTimeKind</b></em>' literal with the specified name.
	 */
	public static LockingTimeKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LockingTimeKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>LockingTimeKind</b></em>' literal with the specified value.
	 */
	public static LockingTimeKind get(int value) {
		switch (value) {
			case RESOURCELOCK: return RESOURCELOCK_LITERAL;
			case INTERRUPTLOCK: return INTERRUPTLOCK_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private LockingTimeKind(int value, String name) {

		super(value,name);
	}
	
}
