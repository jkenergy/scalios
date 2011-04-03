package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/PICVectorNameChecker.java $
 * 
 */


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Class that provides a number of static methods that allow the validation of textual names
 * associated with PIC24/30/33 devices.
 * 
 * @author Mark Dixon
 *
 */

public abstract class PICVectorNameChecker {

	// NOTE the interrupt vectors supported by each device can be found in -
	// C:\Program Files\Microchip\MPLAB C30\support\gld\*.gld
	
	// Also see Section 7.4 (Page 100) of the C30_Users_Guide_V3.0
	
	// At the moment this is too simple, since it only devides into famalies (24/30/33)
	// But must find way of finding exact device, since supported vector names vary
	// for each different part number. May decide to read this info.
	// from a file, since new parts appearing all the time.
	
	/**
	 * List of Interrupt vector names for PIC24  devices.
	 */ 
	private static final String[] PIC24_VECTOR_NAMES =
		new String[] {
			"INT0",
			"IC1",
			// etc
		};

	/**
	 * Set of Interrupt vector names for PIC24  devices.
	 * 
	 * @see #isValidVectorName(String, String)
	 */
	private static Collection<String> PIC24_VECTOR_NAMES_SET = new HashSet<String>(Arrays.asList(PIC24_VECTOR_NAMES));	
	
	
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * List of Interrupt vector names for PIC30  devices.
	 */ 
	private static final String[] PIC30_VECTOR_NAMES =
		new String[] {
			"INT0",
			"IC1",
			// etc
		};

	/**
	 * Set of Interrupt vector names for PIC30  devices.
	 * 
	 * @see #isValidVectorName(String, String)
	 */
	private static Collection<String> PIC30_VECTOR_NAMES_SET = new HashSet<String>(Arrays.asList(PIC30_VECTOR_NAMES));	
	
	
	///////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * List of Interrupt vector names for PIC33  devices.
	 */ 
	private static final String[] PIC33_VECTOR_NAMES =
		new String[] {
			"INT0",
			"IC1",
			// etc
		};

	/**
	 * Set of Interrupt vector names for PIC33  devices.
	 * 
	 * @see #isValidVectorName(String, String)
	 */
	private static Collection<String> PIC33_VECTOR_NAMES_SET = new HashSet<String>(Arrays.asList(PIC33_VECTOR_NAMES));	
		
	//////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Checks whether the given name is a valid interrupt vector name for the specified device series.
	 * 
	 * 
	 * @param name the name to be checked
	 * @param seriesID the PIC series to be checked
	 * @return true if the name begins with a reserved name space sequence of characters
	 */
	public static boolean isValidVectorName(String name, String seriesID) {
		
		if ( seriesID.equals("24") ) {
			return PIC24_VECTOR_NAMES_SET.contains(name);			
		}
		else if ( seriesID.equals("30") ) {
			
			return PIC30_VECTOR_NAMES_SET.contains(name);
		}
		else if ( seriesID.equals("33") ) {
			
			return PIC33_VECTOR_NAMES_SET.contains(name);
		}		
		
		return false;
	}	
	
}
