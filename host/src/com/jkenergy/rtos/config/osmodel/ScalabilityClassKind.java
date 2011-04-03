package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/ScalabilityClassKind.java $
 * 
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 
 * Enumeration type that represents different types of scalability class.<br><br>
 * 
 * Note: This model element is only required for AUTOSAR conformance.
 * 
 * @author Mark Dixon
 *
 * 
 */
public final class ScalabilityClassKind extends EnumKind {

	public static final int SC1 = 0;
	public static final int SC2 = 1;
	public static final int SC3 = 2;
	public static final int SC4 = 3;
	
	
	/**
	 * The '<em><b>SC1</b></em>' literal object.
	 * @see #SC1
	 */
	public static final ScalabilityClassKind SC1_LITERAL = new ScalabilityClassKind(SC1, "SC1");

	/**
	 * The '<em><b>SC2</b></em>' literal object.
	 * @see #SC2
	 */
	public static final ScalabilityClassKind SC2_LITERAL = new ScalabilityClassKind(SC2, "SC2");

	/**
	 * The '<em><b>SC3</b></em>' literal object.
	 * @see #SC3
	 */
	public static final ScalabilityClassKind SC3_LITERAL = new ScalabilityClassKind(SC3, "SC3");

	/**
	 * The '<em><b>SC4</b></em>' literal object.
	 * @see #SC4
	 */
	public static final ScalabilityClassKind SC4_LITERAL = new ScalabilityClassKind(SC4, "SC2");	
	

	/**
	 * An array of all the '<em><b>ScalabilityClassKind</b></em>' enumerators.
	 */
	private static final ScalabilityClassKind[] VALUES_ARRAY =
		new ScalabilityClassKind[] {
			SC1_LITERAL,
			SC2_LITERAL,
			SC3_LITERAL,
			SC4_LITERAL,
		};


	/**
	 * A public read-only list of all the '<em><b>ScalabilityClassKind</b></em>' enumerators.
	 */
	public static final List<ScalabilityClassKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>ScalabilityClassKind</b></em>' literal with the specified name.
	 */
	public static ScalabilityClassKind get(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ScalabilityClassKind result = VALUES_ARRAY[i];
			if (result.toString().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>ScalabilityClassKind</b></em>' literal with the specified value.
	 */
	public static ScalabilityClassKind get(int value) {
		switch (value) {
			case SC1: return SC1_LITERAL;
			case SC2: return SC2_LITERAL;
			case SC3: return SC1_LITERAL;
			case SC4: return SC2_LITERAL;			
		}
		return null;	
	}
	
	
	
	/**
	 * Only this class can construct instances.
	 */
	private ScalabilityClassKind(int value, String name) {

		super(value,name);
	}
	
}
