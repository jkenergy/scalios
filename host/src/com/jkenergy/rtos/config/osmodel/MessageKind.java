package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/MessageKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * Enumeration type that represents different types of messages.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public final class MessageKind extends EnumKind {

	public static final int SEND_STATIC_INTERNAL = 0;
	public static final int RECEIVE_UNQUEUED_INTERNAL = 1;
	public static final int RECEIVE_QUEUED_INTERNAL = 2;
	public static final int SEND_ZERO_INTERNAL = 3;					/* $Req: EXTENSION $ */
	public static final int RECEIVE_ZERO_INTERNAL = 4;				/* $Req: EXTENSION $ */
	public static final int SEND_STREAM_INTERNAL = 5;				/* $Req: EXTENSION $ */
	public static final int RECEIVE_STREAM_INTERNAL = 6;			/* $Req: EXTENSION $ */
	
	
	/**
	 * The '<em><b>SEND_STATIC_INTERNAL</b></em>' literal object.
	 * @see #SEND_STATIC_INTERNAL
	 */
	public static final MessageKind SEND_STATIC_INTERNAL_LITERAL = new MessageKind(SEND_STATIC_INTERNAL, "SEND_STATIC_INTERNAL");

	/**
	 * The '<em><b>RECEIVE_UNQUEUED_INTERNAL</b></em>' literal object.
	 * @see #RECEIVE_UNQUEUED_INTERNAL
	 */
	public static final MessageKind RECEIVE_UNQUEUED_INTERNAL_LITERAL = new MessageKind(RECEIVE_UNQUEUED_INTERNAL, "RECEIVE_UNQUEUED_INTERNAL");

	/**
	 * The '<em><b>RECEIVE_QUEUED_INTERNAL</b></em>' literal object.
	 * @see #RECEIVE_QUEUED_INTERNAL
	 */
	public static final MessageKind RECEIVE_QUEUED_INTERNAL_LITERAL = new MessageKind(RECEIVE_QUEUED_INTERNAL, "RECEIVE_QUEUED_INTERNAL");
	
	/**
	 * The '<em><b>SEND_ZERO_INTERNAL</b></em>' literal object.
	 * @see #SEND_ZERO_INTERNAL
	 */
	public static final MessageKind SEND_ZERO_INTERNAL_LITERAL = new MessageKind(SEND_ZERO_INTERNAL, "SEND_ZERO_INTERNAL");

	/**
	 * The '<em><b>RECEIVE_ZERO_INTERNAL</b></em>' literal object.
	 * @see #RECEIVE_ZERO_INTERNAL
	 */
	public static final MessageKind RECEIVE_ZERO_INTERNAL_LITERAL = new MessageKind(RECEIVE_ZERO_INTERNAL, "RECEIVE_ZERO_INTERNAL");

	/**
	 * The '<em><b>SEND_STREAM_INTERNAL</b></em>' literal object.
	 * @see #SEND_STREAM_INTERNAL
	 */
	public static final MessageKind SEND_STREAM_INTERNAL_LITERAL = new MessageKind(SEND_STREAM_INTERNAL, "SEND_STREAM_INTERNAL");
	
	/**
	 * The '<em><b>RECEIVE_STREAM_INTERNAL</b></em>' literal object.
	 * @see #RECEIVE_STREAM_INTERNAL
	 */
	public static final MessageKind RECEIVE_STREAM_INTERNAL_LITERAL = new MessageKind(RECEIVE_STREAM_INTERNAL, "RECEIVE_STREAM_INTERNAL");

	
	/**
	 * An array of all the '<em><b>MessageKind</b></em>' enumerators.
	 */
	private static final MessageKind[] VALUES_ARRAY =
		new MessageKind[] {
			SEND_STATIC_INTERNAL_LITERAL,
			RECEIVE_UNQUEUED_INTERNAL_LITERAL,
			RECEIVE_QUEUED_INTERNAL_LITERAL,
			SEND_ZERO_INTERNAL_LITERAL,
			RECEIVE_ZERO_INTERNAL_LITERAL,
			SEND_STREAM_INTERNAL_LITERAL,
			RECEIVE_STREAM_INTERNAL_LITERAL			
		};


	/**
	 * A public read-only list of all the '<em><b>MessageKind</b></em>' enumerators.
	 */
	public static final List<MessageKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>MessageKind</b></em>' literal with the specified name.
	 */
	public static MessageKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			MessageKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>MessageKind</b></em>' literal with the specified value.
	 */
	public static MessageKind get(int value) {
		switch (value) {
			case SEND_STATIC_INTERNAL: return SEND_STATIC_INTERNAL_LITERAL;
			case RECEIVE_UNQUEUED_INTERNAL: return RECEIVE_UNQUEUED_INTERNAL_LITERAL;
			case RECEIVE_QUEUED_INTERNAL: return RECEIVE_QUEUED_INTERNAL_LITERAL;
			case SEND_ZERO_INTERNAL: return SEND_ZERO_INTERNAL_LITERAL;
			case RECEIVE_ZERO_INTERNAL: return RECEIVE_ZERO_INTERNAL_LITERAL;
			case SEND_STREAM_INTERNAL: return SEND_STREAM_INTERNAL_LITERAL;
			case RECEIVE_STREAM_INTERNAL: return RECEIVE_STREAM_INTERNAL_LITERAL;
		}
		return null;	
	}
	
	
	/**
	 * Only this class can construct instances.
	 */
	private MessageKind(int value, String name) {

		super(value,name);
	}	

	
}
