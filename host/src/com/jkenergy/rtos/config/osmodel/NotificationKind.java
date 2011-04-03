package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/NotificationKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents different types of message notification actions.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class NotificationKind extends EnumKind {

	public static final int NONE = 0;
	public static final int ACTIVATETASK = 1;
	public static final int SETEVENT = 2;
	public static final int COMCALLBACK = 3;
	public static final int FLAG = 4;
	
	
	/**
	 * The '<em><b>NONE</b></em>' literal object.
	 * @see #NONE
	 */
	public static final NotificationKind NONE_LITERAL = new NotificationKind(NONE, "NONE");

	/**
	 * The '<em><b>ACTIVATETASK</b></em>' literal object.
	 * @see #ACTIVATETASK
	 */
	public static final NotificationKind ACTIVATETASK_LITERAL = new NotificationKind(ACTIVATETASK, "ACTIVATETASK");

	/**
	 * The '<em><b>SETEVENT</b></em>' literal object.
	 * @see #SETEVENT
	 */
	public static final NotificationKind SETEVENT_LITERAL = new NotificationKind(SETEVENT, "SETEVENT");
	

	/**
	 * The '<em><b>COMCALLBACK</b></em>' literal object.
	 * @see #COMCALLBACK
	 */
	public static final NotificationKind COMCALLBACK_LITERAL = new NotificationKind(COMCALLBACK, "COMCALLBACK");
	
	/**
	 * The '<em><b>FLAG</b></em>' literal object.
	 * @see #FLAG
	 */
	public static final NotificationKind FLAG_LITERAL = new NotificationKind(FLAG, "FLAG");

	
	
	/**
	 * An array of all the '<em><b>NotificationKind</b></em>' enumerators.
	 */
	private static final NotificationKind[] VALUES_ARRAY =
		new NotificationKind[] {
			NONE_LITERAL,
			ACTIVATETASK_LITERAL,
			SETEVENT_LITERAL,
			COMCALLBACK_LITERAL,
			FLAG_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>NotificationKind</b></em>' enumerators.
	 */
	public static final List<NotificationKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>NotificationKind</b></em>' literal with the specified name.
	 */
	public static NotificationKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			NotificationKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>NotificationKind</b></em>' literal with the specified value.
	 */
	public static NotificationKind get(int value) {
		switch (value) {
			case NONE: return NONE_LITERAL;
			case ACTIVATETASK: return ACTIVATETASK_LITERAL;
			case SETEVENT: return SETEVENT_LITERAL;
			case COMCALLBACK: return COMCALLBACK_LITERAL;
			case FLAG: return FLAG_LITERAL;
		}
		return null;	
	}
	
	
	/**
	 * Only this class can construct instances.
	 */
	private NotificationKind(int value, String name) {

		super(value,name);
	}	

	
}
