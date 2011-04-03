package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/ObjectKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration type that represents the basic OIL object types.<br><br>
 *
 * @author Mark Dixon
 *
 */
public final class ObjectKind {

	public static final int UNDEFINED = 0;
	public static final int OS = 1;
	public static final int TASK = 2;
	public static final int COUNTER = 3;
	public static final int ALARM = 4;
	public static final int RESOURCE = 5;
	public static final int EVENT = 6;
	public static final int ISR = 7;
	public static final int MESSAGE = 8;
	public static final int COM = 9;
	public static final int NM = 10;
	public static final int APPMODE = 11;
	public static final int IPDU = 12;
	public static final int APPLICATION = 13;				/* $Req: AUTOSAR $ */
	public static final int SCHEDULETABLE = 14;				/* $Req: AUTOSAR $ */
	
	private String name;
	private int value;

	
	/**
	 * The '<em><b>UNDEFINED</b></em>' literal object.
	 * @see #UNDEFINED
	 */
	public static final ObjectKind UNDEFINED_LITERAL = new ObjectKind(UNDEFINED, "UNDEFINED");

	
	/**
	 * The '<em><b>OS</b></em>' literal object.
	 * @see #OS
	 */
	public static final ObjectKind OS_LITERAL = new ObjectKind(OS, "OS");

	/**
	 * The '<em><b>TASK</b></em>' literal object.
	 * @see #TASK
	 */
	public static final ObjectKind TASK_LITERAL = new ObjectKind(TASK, "TASK");

	/**
	 * The '<em><b>COUNTER</b></em>' literal object.
	 * @see #COUNTER
	 */
	public static final ObjectKind COUNTER_LITERAL = new ObjectKind(COUNTER, "COUNTER");
	
	/**
	 * The '<em><b>ALARM</b></em>' literal object.
	 * @see #ALARM
	 */
	public static final ObjectKind ALARM_LITERAL = new ObjectKind(ALARM, "ALARM");
	
	/**
	 * The '<em><b>RESOURCE</b></em>' literal object.
	 * @see #RESOURCE
	 */
	public static final ObjectKind RESOURCE_LITERAL = new ObjectKind(RESOURCE, "RESOURCE");
	
	/**
	 * The '<em><b>EVENT</b></em>' literal object.
	 * @see #EVENT
	 */
	public static final ObjectKind EVENT_LITERAL = new ObjectKind(EVENT, "EVENT");
	
	/**
	 * The '<em><b>ISR</b></em>' literal object.
	 * @see #ISR
	 */
	public static final ObjectKind ISR_LITERAL = new ObjectKind(ISR, "ISR");
	
	/**
	 * The '<em><b>MESSAGE</b></em>' literal object.
	 * @see #MESSAGE
	 */
	public static final ObjectKind MESSAGE_LITERAL = new ObjectKind(MESSAGE, "MESSAGE");

	/**
	 * The '<em><b>COM</b></em>' literal object.
	 * @see #COM
	 */
	public static final ObjectKind COM_LITERAL = new ObjectKind(COM, "COM");
	
	/**
	 * The '<em><b>NM</b></em>' literal object.
	 * @see #NM
	 */
	public static final ObjectKind NM_LITERAL = new ObjectKind(NM, "NM");

	/**
	 * The '<em><b>APPMODE</b></em>' literal object.
	 * @see #APPMODE
	 */
	public static final ObjectKind APPMODE_LITERAL = new ObjectKind(APPMODE, "APPMODE");
	
	/**
	 * The '<em><b>IPDU</b></em>' literal object.
	 * @see #IPDU
	 */
	public static final ObjectKind IPDU_LITERAL = new ObjectKind(IPDU, "IPDU");
		
	/**
	 * The '<em><b>APPLICATION</b></em>' literal object.
	 * @see #APPLICATION
	 */
	public static final ObjectKind APPLICATION_LITERAL = new ObjectKind(APPLICATION, "APPLICATION");
	
	/**
	 * The '<em><b>SCHEDULETABLE</b></em>' literal object.
	 * @see #SCHEDULETABLE
	 */
	public static final ObjectKind SCHEDULETABLE_LITERAL = new ObjectKind(SCHEDULETABLE, "SCHEDULETABLE");	
	
	
	/**
	 * An array of all the '<em><b>Object Kind</b></em>' enumerators.
	 */
	private static final ObjectKind[] VALUES_ARRAY =
		new ObjectKind[] {
			UNDEFINED_LITERAL,
			OS_LITERAL,
			TASK_LITERAL,
			COUNTER_LITERAL,
			ALARM_LITERAL,
			RESOURCE_LITERAL,
			EVENT_LITERAL,
			ISR_LITERAL,
			MESSAGE_LITERAL,
			COM_LITERAL,
			NM_LITERAL,
			APPMODE_LITERAL,
			IPDU_LITERAL,
			APPLICATION_LITERAL,
			SCHEDULETABLE_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>Object Kind</b></em>' enumerators.
	 */
	public static final List<ObjectKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Object Kind</b></em>' literal with the specified name.
	 */
	public static ObjectKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ObjectKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Object Kind</b></em>' literal with the specified value.
	 */
	public static ObjectKind get(int value) {
		switch (value) {
			case UNDEFINED: return UNDEFINED_LITERAL;
			case OS: return OS_LITERAL;
			case TASK: return TASK_LITERAL;
			case COUNTER: return COUNTER_LITERAL;
			case ALARM: return ALARM_LITERAL;
			case RESOURCE: return RESOURCE_LITERAL;
			case EVENT: return EVENT_LITERAL;
			case ISR: return ISR_LITERAL;
			case COM: return COM_LITERAL;
			case NM: return NM_LITERAL;
			case APPMODE: return APPMODE_LITERAL;
			case IPDU: return IPDU_LITERAL;
			case APPLICATION: return APPLICATION_LITERAL;
			case SCHEDULETABLE: return SCHEDULETABLE_LITERAL;
		}
		return null;	
	}

	public int getValue() {
		return value;
	}
	
	public boolean equals(Object o) {
		if ( o==this ) return true;
		
		if ( !(o instanceof ObjectKind)) return false;
		
		return (this.value==((ObjectKind)o).getValue());
	}
	
	public String toString() {
		return name;
	}
		
	public int hashCode() {
		return value;
	}

	
	/**
	 * Only this class can construct instances.
	 */
	private ObjectKind(int value, String name) {

		this.name=name;
		this.value=value;
	}
}
