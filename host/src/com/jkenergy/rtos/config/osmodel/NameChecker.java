package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/NameChecker.java $
 * 
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class provides a number of static methods that allow the validation of textual names
 * used within the OSModel.
 * 
 * 
 * @author Mark Dixon
 *
 */

public abstract class NameChecker {

	/**
	 * Reserved identifier namespace prefixes.
	 * The test is case-insensitive so these prefixes should always be specified in upper-case.
	 * 
	 * @see #usesReservedNamespace(String)
	 */ 
	private static final String[] OS_NAMESPACE_PREFIXES =
		new String[] {
			"OS_",					// OS prefixes
			"OSSERVICEID_",
			"OSERROR_",
			"E_OS_",
			"OSMAXALLOWEDVALUE_",
			"OSTICKSPERBASE_",
			"OSMINCYCLE_",
			
			"COM_",					// COM prefixes
			"COMSERVICEID_",
			"COMERROR_",
			"E_COM_",
		};

	/**
	 * The globally visible datatypes used by the OS
	 */
	private static final String[] OS_TYPES =
		new String[] {
			"TaskStateType",			// OS related Datatypes
			"TaskStateRefType",
			"TaskType",
			"TaskRefType",
			"EventMaskType",
			"EventMaskRefType",
			"AlarmBaseType",
			"AlarmBaseRefType",
			"TickType",
			"TickRefType",
			"ScheduleTableStatus",
			"ScheduleTableStatusRefType",
			"StatusType",
			"OSServiceIdType",
			"DeviceControlCodeType",	// extension to OSEK/AUTOSAR
			"DeviceControlDataType",	// extension to OSEK/AUTOSAR
			"DeviceType",				// extension to OSEK/AUTOSAR
			"DeviceId",					// extension to OSEK/AUTOSAR
			
			"MessageIdentifier",		// COM related datatypes
			"ApplicationDataRef",
			"COMLengthType",
			"LengthRef",
			"FlagValue",
			"COMApplicationModeType",
			"COMShutdownModeType",
			"COMServiceIdType",
			
			"uint8",					// Implementation specific declared datatypes
			"uint16",
			"uint32",
			"int32",
			"int16",
			"unat",
			"nat"
		};

	/**
	 * Set of globally visible datatypes used by the OS implementation.
	 * 
	 * @see #usesOSDatatypeName(String)
	 */
	private static Collection<String> OS_TYPES_SET = new HashSet<String>(Arrays.asList(OS_TYPES));	
	
	/**
	 * All the OS API calls/constant tokens.
	 */
	private static final String[] OS_KEYWORDS =
		new String[] {
			"GetActiveApplicationMode",			// OSEK/AUTOSAR API/Macros
			"DisableAllInterrupts",
			"EnableAllInterrupts",
			"GetTaskID",
			"GetISRID",
			"ErrorHook",
			"PreTaskHook",
			"PostTaskHook",
			"StartupHook",
			"ShutdownHook",
			"ActivateTask",
			"ChainTask",
			"GetAlarm",
			"SetRelAlarm",
			"SetAbsAlarm",
			"CancelAlarm",
			"StartScheduleTableRel",
			"StartScheduleTableAbs",
			"StopScheduleTable",
			"NextScheduleTable",
			"StartOS",
			"ShutdownOS",
			"TerminateTask",
			"Schedule",
			"GetTaskState",
			"GetResource",
			"ReleaseResource",
			"SetEvent",
			"ClearEvent",
			"GetEvent",
			"WaitEvent",
			"GetAlarmBase",
			"IncrementCounter",
			"ExpireCounter",
			"GetScheduleTableStatus",
			"SuspendOSInterrupts",
			"ResumeOSInterrupts",
			"SuspendAllInterrupts",
			"ResumeAllInterrupts",
			"OSErrorGetServiceId",	
			"DeclareAlarm",
			"DeclareEvent",
			"DeclareResource",
			"DeclareTask",
			"TASK",
			"TASK_PROTO",
			"ISR",
			"ISR_PROTO",
			"ALARMCALLBACK",
			"ControlDevice",				// extention to OSEK/AUTOSAR
		
			"StartCOM",						// COM API calls
			"StopCOM",
			"GetCOMApplicationMode",
			"InitMessage",
			"SendMessage",
			"SendStreamMessage",
			"ReceiveMessage",
			"SendZeroMessage",				// extention to OSEK COM
			"ReceiveStreamMessage",			// extention to OSEK COM
			"GetMessageStatus",
			"COMErrorGetServiceId",
			"StartCOMExtension",
			"COMErrorHook",
			"COMCallback",		
			"CalloutReturnType",			// not used in COM CCCA/CCCB but provided for future compatability
			"StartPeriodic",				// not used in COM CCCA/CCCB but provided for future compatability
			"StopPeriodic",					// not used in COM CCCA/CCCB but provided for future compatability
			"SendDynamicMessage",			// not used in COM CCCA/CCCB but provided for future compatability
			"ReceiveDynamicMessage",		// not used in COM CCCA/CCCB but provided for future compatability
			"COMCallout",					// not used in COM CCCA/CCCB but provided for future compatability
							
			"RES_SCHEDULER",				// OSEK/AUTOSAR related constants
			"OSDEFAULTAPPMODE",
			"OSMAXALLOWEDVALUE",
			"OSTICKSPERBASE",
			"OSMINCYCLE",
			"OSTICKDURATION",
			"E_OK",
			"INVALID_TASK",	
			"INVALID_ISR",
			"RUNNING",
			"WAITING",
			"READY",
			"SUSPENDED",
			"SCHEDULETABLE_NOT_STARTED",
			"SCHEDULETABLE_NEXT",
			"SCHEDULETABLE_WAITING",
			"SCHEDULETABLE_RUNNING_AND_SYNCHRONOUS",
			"SCHEDULETABLE_RUNNING",			

			"COM_RES_OS",					// Implementation specific declared datatypes
			"FASTROM",
			"NEAR",
			"FAR",
			"OS_IDLE_TASK",					// not really required since OS_ prefix			
			"SET_IPL",						// TODO: will probably remove these once changed to OS_SET_IPL in source
			"SET_IPL_MAX",					// TODO: will probably remove these once changed to OS_SET_IPL_MAX in source
		};
	
	/**
	 * Set of globally visible keywords used by the OS implementation.
	 * 
	 * @see #usesOSKeyword(String)
	 */
	private static Collection<String> OS_KEYWORD_SET = new HashSet<String>(Arrays.asList(OS_KEYWORDS));		
	
		
	/**
	 * The datatypes, compound types, storage classes and type qualifiers used by the C language
	 */
	private static final String[] C_TYPE_SPECIFIER =
		new String[] {
			"void",
			"char",
			"short",
			"int",
			"long",
			"float",
			"double",
			"signed",
			"unsigned",
			"struct",
			"union",		// compound types
			"enum",
			"typedef",
			"auto",			// storage classes
			"register",
			"static",
			"extern",
			"const",		// type qualifiers
			"volatile"		
		};	
	
	/**
	 * Set of datatypes specifier names used by the C language.
	 * 
	 * @see #usesCDatatypeName(String)
	 */
	private static Collection<String> C_TYPE_SPECIFIER_SET = new HashSet<String>(Arrays.asList(C_TYPE_SPECIFIER));	
	
	/**
	 * An array of all the ANSI C statements/predefined constants.
	 */	
	private static final String[] C_STATEMENTS =
		new String[] {
			"break",
			"case",
			"continue",
			"default",
			"do",
			"else",
			"for",
			"goto",
			"if",
			"return",
			"sizeof",
			"switch",
			"while",
			"asm",
			"fortran",
			"__LINE__",			// predefined preprocessor identifiers
			"__FILE__",
			"__DATE__",
			"__TIME__",
			"__STDC__"
		};
	
	/**
	 * Set of statement names used by the C language.
	 * 
	 * @see #usesCKeyword(String)
	 */
	private static Collection<String> C_STATEMENT_SET = new HashSet<String>(Arrays.asList(C_STATEMENTS));	
		
	
	/**
	 * Checks whether the given name begins with one of the reserved OS namespaces.
	 * This test ignores the case of the name.
	 * 
	 * @see #OS_NAMESPACE_PREFIXES
	 * 
	 * @param name the name to be checked
	 * @return true if the name begins with a reserved namespace sequence of characters
	 */
	public static boolean usesReservedNamespace(String name) {
		
		name = name.toUpperCase();	// always work in upper case
		
		for (int i = 0; i < OS_NAMESPACE_PREFIXES.length; i++) {

			if ( name.startsWith(OS_NAMESPACE_PREFIXES[i]) ) {
				return true;	// name starts with one of the prefixes that is reserved
			}			
		}

		return false;
	}	
	
	/**
	 * Checks whether the given name equals a datatype name reserved by the OS.
	 * 
	 * @see #OS_TYPES_SET
	 * 
	 * @param name the name to be checked
	 * @return true if the name equals a datatype name reserved by the OS.
	 */
	public static boolean usesOSDatatypeName(String name) {
		
		return OS_TYPES_SET.contains(name);
	}	
	
	/**
	 * Checks whether the given name equals a datatype specifier name reserved by the C language.
	 * 
	 * @see #C_TYPE_SPECIFIER_SET
	 * 
	 * @param name the name to be checked
	 * @return true if the name equals a datatype name reserved by the OS.
	 */
	public static boolean usesCDatatypeName(String name) {
		
		return C_TYPE_SPECIFIER_SET.contains(name);
	}	
	
	/**
	 * Checks whether the given name equals a keyword (API call/constant) name reserved by the OS.
	 * 
	 * @see #OS_KEYWORD_SET
	 * 
	 * @param name the name to be checked
	 * @return true if the name equals a keyword reserved by the OS.
	 */
	public static boolean usesOSKeyword(String name) {
		
		return OS_KEYWORD_SET.contains(name);
	}	
	
	/**
	 * Checks whether the given name equals a keyword used by the C language.
	 * 
	 * @see #C_STATEMENT_SET
	 * 
	 * @param name the name to be checked
	 * @return true if the name equals a keyword used by the C language.
	 */
	public static boolean usesCKeyword(String name) {
		
		return C_STATEMENT_SET.contains(name);
	}	
	
	/**
	 * Checks whether the given name is a valid identifier name.
	 * 
	 * A valid identifier -
	 * 		is a non-null/non-empty string
	 * 		begins with a letter or an underscore (_)
	 *  	contains letters, numbers of underscores.
	 * 
	 * @param name the name to be checked
	 * @return true if the name is a valid identifier name
	 */
	public static boolean isValidIdentifier(String name) {
		
		if ( name != null && name.length() > 0  ) {
			
			char nextChar = name.charAt(0);
			
			// check first char is legal (a-z, A-Z, _)
			if ( (nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z') || nextChar == '_' ) {
				
				for (int i = 1; i < name.length(); i++) {
					
					nextChar = name.charAt(i);
					
					if ( !((nextChar >= 'a' && nextChar <= 'z') || (nextChar >= 'A' && nextChar <= 'Z') || (nextChar >= '0' && nextChar <= '9') || nextChar == '_' ) ) {
						// one of the characters is illegal
						return false;
					}
				}
			}
			else {
				// first character is illegal
				return false;			
			}
		}
		else {
			// name is null or an empty string
			return false;				
		}
		
		return true;	// is a valid identifier
	}
	
	/**
	 * Checks whether the given name is a valid type name.
	 * 
	 * This method does not check against valid C datatypes, since the user may specify their
	 * own typename that is typdef'd elsewhere in their code. Therefore this method just
	 * ensures that valid characters are used at the beginning of the declaration.
	 * 
	 * Also use of struct, enum and pointers to function declarations means that just about any
	 * character can appear somewhere in a type declaration, e.g. enum {VAL1 = (2*PI)/RAD, VAL2 : 3} is a valid type. 
	 * 
	 * A valid type name -
	 * 		is a non-null/non-empty string
	 * 		begins with a letter or an underscore (_)
	 *  	contains any other character (to allow for enums, structs and function pointer declarations)
	 * 
	 * @param name the name to be checked
	 * @return true if the name is a valid type name
	 */
	public static boolean isValidTypeName(String name) {	
				
		if ( name != null && name.length() > 0  ) {
			
			char firstChar = name.charAt(0);	
			
			if ( (firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z') || firstChar == '_' ) {
				return true;
			}
		}
		
		return false;	// is an invalid type name
	}
	
	
}
