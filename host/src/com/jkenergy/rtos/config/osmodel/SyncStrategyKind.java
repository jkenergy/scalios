package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/SyncStrategyKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of synchronisation strategy.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 *
 */
public final class SyncStrategyKind extends EnumKind {

	public static final int HARD = 0;
	public static final int SMOOTH = 1;
	
	
	/**
	 * The '<em><b>HARD</b></em>' literal object.
	 * @see #HARD
	 */
	public static final SyncStrategyKind HARD_LITERAL = new SyncStrategyKind(HARD, "HARD");

	/**
	 * The '<em><b>SMOOTH</b></em>' literal object.
	 * @see #SMOOTH
	 */
	public static final SyncStrategyKind SMOOTH_LITERAL = new SyncStrategyKind(SMOOTH, "SMOOTH");


	/**
	 * An array of all the '<em><b>SyncStrategyKind</b></em>' enumerators.
	 */
	private static final SyncStrategyKind[] VALUES_ARRAY =
		new SyncStrategyKind[] {
			HARD_LITERAL,
			SMOOTH_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>SyncStrategyKind</b></em>' enumerators.
	 */
	public static final List<SyncStrategyKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>SyncStrategyKind</b></em>' literal with the specified name.
	 */
	public static SyncStrategyKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SyncStrategyKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>SyncStrategyKind</b></em>' literal with the specified value.
	 */
	public static SyncStrategyKind get(int value) {
		switch (value) {
			case HARD: return HARD_LITERAL;
			case SMOOTH: return SMOOTH_LITERAL;
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private SyncStrategyKind(int value, String name) {

		super(value,name);
	}
	
}
