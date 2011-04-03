package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ActionKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different alarm expiry actions.
 * 
 * @author Mark Dixon
 *
 */
public final class ActionKind extends EnumKind {

	public static final int ACTIVATETASK = 0;
	public static final int SETEVENT = 1;
	public static final int ALARMCALLBACK = 2;
	public static final int INCREMENTCOUNTER = 3;	/* $Req: AUTOSAR $ */
	
	
	/**
	 * The '<em><b>ACTIVATETASK</b></em>' literal object.
	 * @see #ACTIVATETASK
	 */
	public static final ActionKind ACTIVATETASK_LITERAL = new ActionKind(ACTIVATETASK, "ACTIVATETASK");

	/**
	 * The '<em><b>SETEVENT</b></em>' literal object.
	 * @see #SETEVENT
	 */
	public static final ActionKind SETEVENT_LITERAL = new ActionKind(SETEVENT, "SETEVENT");

	/**
	 * The '<em><b>ALARMCALLBACK</b></em>' literal object.
	 * @see #ALARMCALLBACK
	 */
	public static final ActionKind ALARMCALLBACK_LITERAL = new ActionKind(ALARMCALLBACK, "ALARMCALLBACK");
	
	/**
	 * The '<em><b>INCREMENTCOUNTER</b></em>' literal object.
	 * @see #INCREMENTCOUNTER
	 */
	public static final ActionKind INCREMENTCOUNTER_LITERAL = new ActionKind(INCREMENTCOUNTER, "INCREMENTCOUNTER");
	

	
	/**
	 * An array of all the '<em><b>Action Kind</b></em>' enumerators.
	 */
	private static final ActionKind[] VALUES_ARRAY =
		new ActionKind[] {
			ACTIVATETASK_LITERAL,
			SETEVENT_LITERAL,
			ALARMCALLBACK_LITERAL,
			INCREMENTCOUNTER_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>Action Kind</b></em>' enumerators.
	 */
	public static final List<ActionKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Action Kind</b></em>' literal with the specified name.
	 */
	public static ActionKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ActionKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Action Kind</b></em>' literal with the specified value.
	 */
	public static ActionKind get(int value) {
		switch (value) {
			case ACTIVATETASK: return ACTIVATETASK_LITERAL;
			case SETEVENT: return SETEVENT_LITERAL;
			case ALARMCALLBACK: return ALARMCALLBACK_LITERAL;
			case INCREMENTCOUNTER: return INCREMENTCOUNTER_LITERAL;
		}
		return null;	
	}
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ActionKind(int value, String name) {

		super(value,name);
	}	

	
}
