package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ScheduleTableActionKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of schedule table expiry actions.<br><br>
 * 
 * @author Mark Dixon
 *
 * Note: This model element is only required for AUTOSAR conformance.
 */
public final class ScheduleTableActionKind extends EnumKind {

	public static final int ACTIVATETASK = 0;		/* $Req: AUTOSAR $ */
	public static final int SETEVENT = 1;			/* $Req: AUTOSAR $ */
	public static final int ACTIONCALLBACK = 2;		/* $Req: EXTENSION $ */
	public static final int INCREMENTCOUNTER = 3;	/* $Req: EXTENSION $ */
	
	// NOTE: the action callback and increment counter actions are not OSEK/AUTOSAR standard. They are extensions.
	
	/**
	 * The '<em><b>ACTIVATETASK</b></em>' literal object.
	 * @see #ACTIVATETASK
	 */
	public static final ScheduleTableActionKind ACTIVATETASK_LITERAL = new ScheduleTableActionKind(ACTIVATETASK, "ACTIVATETASK");

	/**
	 * The '<em><b>SETEVENT</b></em>' literal object.
	 * @see #SETEVENT
	 */
	public static final ScheduleTableActionKind SETEVENT_LITERAL = new ScheduleTableActionKind(SETEVENT, "SETEVENT");

	/**
	 * The '<em><b>ACTIONCALLBACK</b></em>' literal object.
	 * @see #ACTIONCALLBACK
	 */
	public static final ScheduleTableActionKind ACTIONCALLBACK_LITERAL = new ScheduleTableActionKind(ACTIONCALLBACK, "ACTIONCALLBACK");
	
	/**
	 * The '<em><b>INCREMENTCOUNTER</b></em>' literal object.
	 * @see #INCREMENTCOUNTER
	 */
	public static final ScheduleTableActionKind INCREMENTCOUNTER_LITERAL = new ScheduleTableActionKind(INCREMENTCOUNTER, "INCREMENTCOUNTER");
	

	
	/**
	 * An array of all the '<em><b>ScheduleTableActionKind</b></em>' enumerators.
	 */
	private static final ScheduleTableActionKind[] VALUES_ARRAY =
		new ScheduleTableActionKind[] {
			ACTIVATETASK_LITERAL,
			SETEVENT_LITERAL,
			ACTIONCALLBACK_LITERAL,
			INCREMENTCOUNTER_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>ScheduleTableActionKind</b></em>' enumerators.
	 */
	public static final List<ScheduleTableActionKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>ScheduleTableActionKind</b></em>' literal with the specified name.
	 */
	public static ScheduleTableActionKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ScheduleTableActionKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>ScheduleTableActionKind</b></em>' literal with the specified value.
	 */
	public static ScheduleTableActionKind get(int value) {
		switch (value) {
			case ACTIVATETASK: return ACTIVATETASK_LITERAL;
			case SETEVENT: return SETEVENT_LITERAL;
			case ACTIONCALLBACK: return ACTIONCALLBACK_LITERAL;
			case INCREMENTCOUNTER: return INCREMENTCOUNTER_LITERAL;
		}
		return null;	
	}
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ScheduleTableActionKind(int value, String name) {

		super(value,name);
	}	

	
}
