package com.jkenergy.rtos.config.serializers;

/*
 * $LastChangedDate: 2008-01-27 20:39:24 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 597 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/serializers/OILSerializer.java $
 * 
 */

import java.util.*;
import java.io.*;
import java.math.*;

import com.jkenergy.rtos.config.Problem;
import com.jkenergy.rtos.config.oilmodel.*;
import com.jkenergy.rtos.config.osmodel.*;
import com.jkenergy.rtos.config.osmodel.Runnable;
import com.jkenergy.rtos.config.parser.*;

/**
 * This is the OIL importer/exporter class.<br><br>
 * 
 * Import actions load, parse and populate an OS Model (via an OIL model) with information provided by file(s) containing
 * OIL based definitions.<br><br>
 *   
 * Export actions generate OIL format output from a given OS Model.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class OILSerializer {

	/**
	 * Define the OIL commands that provide the default definition of OIL objects and standard attributes
	 * supported by the target RTOS. This is based on the Subset for internal communication (CCCA and CCCB only).
	 */
	private final static String implementationSpec =
		
				"OIL_VERSION=\"3.0\";" +
				"IMPLEMENTATION Standard {"+	
		
				"OS {" +
				" ENUM [STANDARD, EXTENDED] STATUS;" +
				" BOOLEAN STARTUPHOOK;" +
				" BOOLEAN ERRORHOOK;" +
				" BOOLEAN SHUTDOWNHOOK;" +
				" BOOLEAN PRETASKHOOK;" +
				" BOOLEAN POSTTASKHOOK;" +
				" BOOLEAN USEGETSERVICEID;" +
				" BOOLEAN USEPARAMETERACCESS;" +
				" BOOLEAN USERESSCHEDULER = TRUE;" +
				" BOOLEAN STACKMONITORING = TRUE;" +								// non-standard OIL (AUTOSAR only), specifies whether stack checking should be globally enabled
				" BOOLEAN PROTECTIONHOOK = FALSE;" +								// non-standard OIL (AUTOSAR only)
				" ENUM WITH_AUTO [SC1, SC2, SC3, SC4] SCALABILITYCLASS = AUTO;" + 	// non-standard OIL (AUTOSAR only)
				" UINT32 WITH_AUTO PRETASKHOOK_STACKSIZE = AUTO;" +	// non-standard OIL, specifies stacksize for the pretask hook, AUTO causes target specific stack size to be used.
				" UINT32 WITH_AUTO POSTTASKHOOK_STACKSIZE = AUTO;" +// non-standard OIL, specifies stacksize for the posttask hook, AUTO causes target specific stack size to be used.
				" BOOLEAN RESTARTABLE = FALSE;" +					// non-standard OIL, specifies whether StartOS call be called more than once (causes reinit during shutdown)								
				" BOOLEAN HANDLE_OSCFAILURE = FALSE;" +				// non-standard OIL, specifies whether OS provides handler for Oscillator Failure. 
				" BOOLEAN HANDLE_ADDRERROR = FALSE;" +				// non-standard OIL, specifies whether OS provides handler for Address Error.
				" BOOLEAN HANDLE_MATHERROR = FALSE;" +				// non-standard OIL, specifies whether OS provides handler for Math Error.				
				"};" +
				
				"APPMODE {" +
				"};" +
				
				"APPLICATION {" +									// non-standard OIL (AUTOSAR only)
				"	BOOLEAN [" +
				"		TRUE {" +
				"			BOOLEAN [" +
				"				TRUE {STRING NAME;}," +
				"				FALSE" +
				"			] TRUSTED_FUNCTION[];" +
				"		}," +
				"		FALSE" +
				"	] TRUSTED = FALSE;" +
				"	BOOLEAN STARTUPHOOK;" +
				"	BOOLEAN SHUTDOWNHOOK;" +
				"	BOOLEAN ERRORHOOK;" +
				"	BOOLEAN [" +
				"		TRUE {TASK_TYPE RESTARTTASK;}," +
				"		FALSE" +
				"	] HAS_RESTARTTASK;" +
				"	TASK_TYPE TASK[];" +
				"	ISR_TYPE ISR[];" +
				"	ALARM_TYPE ALARM[];" +
				"	SCHEDULETABLE_TYPE SCHEDULETABLE[];" +
				"	COUNTER_TYPE COUNTER[];" +
				"	RESOURCE_TYPE RESOURCE[];" +
				"	MESSAGE_TYPE MESSAGE[];" +
				"};" +				
				
				"TASK {" +
				" BOOLEAN [" +
				"  TRUE {" +
				"   APPMODE_TYPE APPMODE[];" +
				"  }," +
				"  FALSE" +
				" ] AUTOSTART;" +
				" UINT32 PRIORITY;" +
				" UINT32 ACTIVATION;" +
				" ENUM [NON, FULL] SCHEDULE;" +
				" EVENT_TYPE EVENT[];" +
				" RESOURCE_TYPE RESOURCE[];" +
				" MESSAGE_TYPE MESSAGE[];" +
				" BOOLEAN ["+										// non-standard OIL (AUTOSAR only)
				"  TRUE {" +
				"   UINT64 EXECUTIONBUDGET;" +
				"   UINT64 TIMEFRAME;" +
				"   UINT64 TIMELIMIT;" +
				"   ENUM [" +
				"	 RESOURCELOCK {" +
				"	  RESOURCE_TYPE RESOURCE;" +
				"     UINT64 RESOURCELOCKTIME;" +
				"	 }," +
				"	 INTERRUPTLOCK {" +
				"	  UINT64 OSINTERRUPTLOCKTIME;" +
				"	  UINT64 ALLINTERRUPTLOCKTIME;" +
				"    }" +
				"   ] LOCKINGTIME [];" +
				"  }," +
				"  FALSE" +
				" ] TIMING_PROTECTION = FALSE;" +		
				" APPLICATION_TYPE ACCESSING_APPLICATION[];"+		// non-standard OIL (AUTOSAR only)
				" UINT32 WITH_AUTO STACKSIZE = AUTO;" +				// non-standard OIL, specifies stacksize for the task. AUTO causes target specific stack size to be used.				
				"};" +
				
				"ISR {" +
				" UINT32 [1, 2] CATEGORY;" +
				" RESOURCE_TYPE RESOURCE[];" +
				" MESSAGE_TYPE MESSAGE[];" +
				" BOOLEAN ["+										// non-standard OIL (AUTOSAR only)
				"  TRUE {" +
				"   UINT64 EXECUTIONBUDGET;" +
				"   UINT32 COUNTLIMIT;" +
				"   UINT64 TIMELIMIT;" +
				"   ENUM [" +
				"	 RESOURCELOCK {" +
				"	  RESOURCE_TYPE RESOURCE;" +
				"     UINT64 RESOURCELOCKTIME;" +
				"	 }," +
				"	 INTERRUPTLOCK {" +
				"	  UINT64 OSINTERRUPTLOCKTIME;" +
				"	  UINT64 ALLINTERRUPTLOCKTIME;" +
				"    }" +
				"   ] LOCKINGTIME [];" +
				"  }," +
				"  FALSE" +
				" ] TIMING_PROTECTION = FALSE;" +	
				" UINT32 PRIORITY = 1;" +							// non-standard OIL, specifies priority for the ISR.
				" BOOLEAN DISABLE_STACKMONITORING = FALSE;" +		// non-standard OIL, specifies whether stack checking should be disabled for this specific ISR (if enabled globally)
				" UINT32 WITH_AUTO STACKSIZE = AUTO;" +				// non-standard OIL, specifies stacksize for the ISR. AUTO causes target specific stack size to be used.
				" STRING WITH_AUTO VECTOR = AUTO;" +				// non-standard OIL, specifies vector info. for the ISR. AUTO causes name of ISR to be used as the VECTOR
				"};" +
				
				"COUNTER {" +
				" UINT32 MINCYCLE;" +
				" UINT32 MAXALLOWEDVALUE;" +
				" UINT32 TICKSPERBASE;" +
				" ENUM [ SOFTWARE, HARDWARE ] TYPE = HARDWARE;" +	// non-standard OIL, $Req: AUTOSAR $ */
				" ENUM [ TICKS, NANOSECONDS ] UNIT = TICKS;" +		// non-standard OIL, $Req: AUTOSAR $ */
				" APPLICATION_TYPE ACCESSING_APPLICATION[];"+		// non-standard OIL (AUTOSAR only)
				" STRING OPTIONS = \"\";" +							// non-standard OIL, identifies the driver options for the counter (device specific)
				"};" +
				
				"ALARM {" +
				" COUNTER_TYPE COUNTER;" +
				" ENUM [" +
				"  ACTIVATETASK {" +
				"   TASK_TYPE TASK;" +
				"  }," + 
				"  SETEVENT {" +
				"   TASK_TYPE TASK;" +
				"   EVENT_TYPE EVENT;" +
				"  }," +
				"  ALARMCALLBACK {" +
				"   STRING ALARMCALLBACKNAME;" +
				"  }," +
				"  INCREMENTCOUNTER {" +								// non-standard OIL, $Req: AUTOSAR $ */
				"	COUNTER_TYPE COUNTER;" +
				"  }" +
				" ] ACTION;" +
				" BOOLEAN [" +
				"  TRUE {" +
				"   UINT32 ALARMTIME;" +
				"   UINT32 CYCLETIME;" +
				"   APPMODE_TYPE APPMODE[];" +
				"  }," +
				"  FALSE" +
				" ] AUTOSTART;" +
				" APPLICATION_TYPE ACCESSING_APPLICATION[];"+			// non-standard OIL (AUTOSAR only)
				"};" +
				
				"EVENT {" +
				" UINT64 WITH_AUTO MASK;" +
				"};" +
				
				"RESOURCE {" +
				" ENUM [" +
				"  STANDARD," +
				"  LINKED {" +
				"   RESOURCE_TYPE LINKEDRESOURCE;" +
				"  }," +
				"  INTERNAL" +
				" ] RESOURCEPROPERTY;" +
				" APPLICATION_TYPE ACCESSING_APPLICATION[];"+			// non-standard OIL (AUTOSAR only)
				"};" +
				
				"MESSAGE {" +
				" ENUM [" +
				"  SEND_STATIC_INTERNAL {" +
				"   STRING CDATATYPE;" +
				"  }," +
				"  RECEIVE_UNQUEUED_INTERNAL {" +
				"   MESSAGE_TYPE SENDINGMESSAGE;" +
				"   UINT64 INITIALVALUE = 0;" +
				"   ENUM [ IPC {STRING OPTIONS;}, CAN {STRING OPTIONS;}, soft {STRING OPTIONS;}] DEVICE = IPC;" +		// non-standard OIL, identifies the driver for the message (device specific)
				"  }," +
				"  RECEIVE_QUEUED_INTERNAL {" +
				"   MESSAGE_TYPE SENDINGMESSAGE;" +
				"   UINT32 QUEUESIZE;" +
				"   ENUM [ IPC {STRING OPTIONS;}, CAN {STRING OPTIONS;}, soft {STRING OPTIONS;}] DEVICE = IPC;" +		// non-standard OIL, identifies the driver for the message (device specific)				
				"  }," +
				"  SEND_ZERO_INTERNAL {" +							// non-standard OIL, $Req: EXTENSION $ */
				"  },"+				
				"  RECEIVE_ZERO_INTERNAL {" +						// non-standard OIL, $Req: EXTENSION $ */
				"   MESSAGE_TYPE SENDINGMESSAGE;" +
				"   ENUM [ IPC {STRING OPTIONS;}, CAN {STRING OPTIONS;}, soft {STRING OPTIONS;}] DEVICE = IPC;" +		// non-standard OIL, identifies the driver for the message (device specific)				
				"  },"+
				"  SEND_STREAM_INTERNAL {" +						// non-standard OIL, $Req: EXTENSION $ */
				"  },"+
				"  RECEIVE_STREAM_INTERNAL {" +						// non-standard OIL, $Req: EXTENSION $ */
				"   MESSAGE_TYPE SENDINGMESSAGE;" +
				"   UINT32 BUFFERSIZE;" +
				"	UINT32 LOW_THRESHOLD = 0;" +
				"	UINT32 HIGH_THRESHOLD = 0;" +
				"   ENUM [ IPC {STRING OPTIONS;}, CAN {STRING OPTIONS;}, soft {STRING OPTIONS;}] DEVICE = IPC;" +		// non-standard OIL, identifies the driver for the message (device specific)				
				"  }" +				
				" ] MESSAGEPROPERTY;" +
				" ENUM [" +
				"  NONE," +
				"  ACTIVATETASK {" +
				"   TASK_TYPE TASK;" +
				"  }," +
				"  SETEVENT {" +
				"   TASK_TYPE TASK;" +
				"   EVENT_TYPE EVENT;" +
				"  }," +
				"  COMCALLBACK {" +
				"   STRING CALLBACKROUTINENAME;" +
				"   MESSAGE_TYPE MESSAGE[];" +
				"  }," +
				"  FLAG {" +
				"   STRING FLAGNAME;" +
				"  }" +
				" ] NOTIFICATION = NONE;" +
				" ENUM [" +											// non-standard OIL, $Req: EXTENSION $ */
				"  NONE," +											// Used to specify low threshold notification for STREAM messages
				"  ACTIVATETASK {" +
				"   TASK_TYPE TASK;" +
				"  }," +
				"  SETEVENT {" +
				"   TASK_TYPE TASK;" +
				"   EVENT_TYPE EVENT;" +
				"  }," +
				"  COMCALLBACK {" +
				"   STRING CALLBACKROUTINENAME;" +
				"   MESSAGE_TYPE MESSAGE[];" +
				"  }," +
				"  FLAG {" +
				"   STRING FLAGNAME;" +
				"  }" +
				" ] LOW_NOTIFICATION = NONE;" +
				" APPLICATION_TYPE ACCESSING_APPLICATION[];"+			// non-standard OIL (AUTOSAR only)
				"};" +	
				
				"COM {" +
				" BOOLEAN COMERRORHOOK = FALSE;" +
				" BOOLEAN COMUSEGETSERVICEID = FALSE;" +
				" BOOLEAN COMUSEPARAMETERACCESS = FALSE;" +
				" BOOLEAN COMSTARTCOMEXTENSION = FALSE;" +
				" STRING COMAPPMODE[];" +
				" ENUM [COMSTANDARD, COMEXTENDED] COMSTATUS = COMSTANDARD;" +
				"};" +

				"NM {" +
				"};" +
				
				"SCHEDULETABLE {" +												// All non-standard OIL, $Req: AUTOSAR $ */
				" COUNTER_TYPE COUNTER;" +
				" BOOLEAN [" +
				"  TRUE {" +
				"   UINT64 OFFSET;" +
				"   APPMODE_TYPE APPMODE[];" +
				"  }," +
				"  FALSE" +
				" ] AUTOSTART;" +	
				" BOOLEAN [" +
				"  TRUE {" +
				"   ENUM [HARD, SMOOTH] SYNC_STRATEGY = HARD;" +
				"   UINT64 MAX_INCREASE;" +
				"   UINT64 MAX_DECREASE;" +
				"   UINT64 MAX_INCREASE_ASYNC;" +
				"   UINT64 MAX_DECREASE_ASYNC;" +
				"   UINT64 PRECISION;" +
				"  }," +
				"  FALSE" +
				" ] LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION = FALSE;" +
				" BOOLEAN PERIODIC;" +
				" UINT64 LENGTH;" +
				" ENUM [" +
				"  ACTIVATETASK {" +
				"   UINT64 OFFSET;" +
				"   TASK_TYPE TASK;" +
				"  }," +
				"  SETEVENT {" +
				"   UINT64 OFFSET;" +
				"   EVENT_TYPE EVENT;" +
				"   TASK_TYPE TASK;" +
				"  }" +
				" ] ACTION [];" +			
				" APPLICATION_TYPE ACCESSING_APPLICATION[];" +
				"};" +
				
				"};"
				;

	/**
	* The end of line string for this machine.
	*/
	private final static String eol = System.getProperty("line.separator", "\n");
	
	/*
	 * Constants used to identify Parameter names from the OIL Model
	 */
	private final static String STATUS = "STATUS";
	private final static String STARTUPHOOK = "STARTUPHOOK";
	private final static String ERRORHOOK = "ERRORHOOK";
	private final static String SHUTDOWNHOOK = "SHUTDOWNHOOK";
	private final static String PRETASKHOOK = "PRETASKHOOK";
	private final static String POSTTASKHOOK = "POSTTASKHOOK";
	private final static String USEGETSERVICEID = "USEGETSERVICEID";
	private final static String USEPARAMETERACCESS = "USEPARAMETERACCESS";
	private final static String USERESSCHEDULER = "USERESSCHEDULER";
	private final static String RESTARTABLE = "RESTARTABLE";
	private final static String STACKMONITORING = "STACKMONITORING";
	private final static String HANDLE_OSCFAILURE = "HANDLE_OSCFAILURE";
	private final static String HANDLE_MATHERROR = "HANDLE_MATHERROR";
	private final static String HANDLE_ADDRERROR = "HANDLE_ADDRERROR";
	private final static String DISABLE_STACKMONITORING = "DISABLE_STACKMONITORING";
	private final static String PRETASKHOOK_STACKSIZE = "PRETASKHOOK_STACKSIZE";
	private final static String POSTTASKHOOK_STACKSIZE = "POSTTASKHOOK_STACKSIZE";
	private final static String PRIORITY="PRIORITY";
	private final static String ACTIVATION = "ACTIVATION";
	private final static String STACKSIZE = "STACKSIZE";
	private final static String VECTOR = "VECTOR";
	private final static String SCHEDULE = "SCHEDULE";
	private final static String AUTOSTART = "AUTOSTART";
	private final static String RESOURCE = "RESOURCE";
	private final static String EVENT = "EVENT";
	private final static String LOW_EVENT = "LOW_EVENT";
	private final static String MESSAGE = "MESSAGE";
	private final static String CALLBACK_MESSAGE = "CALLBACK_MESSAGE";
	private final static String LOW_CALLBACK_MESSAGE = "LOW_CALLBACK_MESSAGE";
	private final static String CATEGORY = "CATEGORY";
	private final static String MASK = "MASK";
	private final static String MAXALLOWEDVALUE = "MAXALLOWEDVALUE";
	private final static String TICKSPERBASE ="TICKSPERBASE";
	private final static String MINCYCLE = "MINCYCLE";
	private final static String RESOURCEPROPERTY = "RESOURCEPROPERTY";
	private final static String LINKEDRESOURCE = "LINKEDRESOURCE";
	private final static String APPMODE = "APPMODE";
	private final static String COMERRORHOOK = "COMERRORHOOK";
	private final static String COMUSEGETSERVICEID = "COMUSEGETSERVICEID";
	private final static String COMUSEPARAMETERACCESS = "COMUSEPARAMETERACCESS";
	private final static String COMSTARTCOMEXTENSION = "COMSTARTCOMEXTENSION";
	private final static String COMAPPMODE = "COMAPPMODE";
	private final static String COMSTATUS = "COMSTATUS";
	private final static String COUNTER = "COUNTER";
	private final static String ACTION = "ACTION";	
	private final static String TASK = "TASK";
	private final static String LOW_TASK = "LOW_TASK";
	private final static String ISR = "ISR";
	private final static String ALARM = "ALARM";
	private final static String ALARMCALLBACKNAME = "ALARMCALLBACKNAME";
	private final static String ALARMTIME = "ALARMTIME";
	private final static String CYCLETIME = "CYCLETIME";
	private final static String MESSAGEPROPERTY = "MESSAGEPROPERTY";
	private final static String CDATATYPE = "CDATATYPE";
	private final static String SENDINGMESSAGE = "SENDINGMESSAGE";
	private final static String INITIALVALUE = "INITIALVALUE";
	private final static String QUEUESIZE = "QUEUESIZE";
	private final static String BUFFERSIZE = "BUFFERSIZE";
	private final static String HIGH_THRESHOLD = "HIGH_THRESHOLD";
	private final static String LOW_THRESHOLD = "LOW_THRESHOLD";
	private final static String NOTIFICATION = "NOTIFICATION";
	private final static String LOW_NOTIFICATION = "LOW_NOTIFICATION";
	private final static String CALLBACKROUTINENAME = "CALLBACKROUTINENAME";
	private final static String LOW_CALLBACKROUTINENAME = "LOW_CALLBACKROUTINENAME";
	private final static String FLAGNAME = "FLAGNAME";
	private final static String LOW_FLAGNAME = "LOW_FLAGNAME";
	private final static String TRUSTED = "TRUSTED";															/* $Req: AUTOSAR $ */
	private final static String TRUSTED_FUNCTION = "TRUSTED_FUNCTION";											/* $Req: AUTOSAR $ */
	private final static String NAME = "NAME";																	/* $Req: AUTOSAR $ */
	private final static String HAS_RESTARTTASK = "HAS_RESTARTTASK";											/* $Req: AUTOSAR $ */
	private final static String RESTARTTASK = "RESTARTTASK";													/* $Req: AUTOSAR $ */
	private final static String SCHEDULETABLE = "SCHEDULETABLE";												/* $Req: AUTOSAR $ */
	private final static String ACCESSING_APPLICATION = "ACCESSING_APPLICATION";								/* $Req: AUTOSAR $ */
	private final static String OFFSET = "OFFSET";	
	private final static String ACTIONOFFSET = "ACTION_OFFSET";													/* $Req: AUTOSAR $ */
	private final static String LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION = "LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION";	/* $Req: AUTOSAR $ */
	private final static String SYNC_STRATEGY = "SYNC_STRATEGY";												/* $Req: AUTOSAR $ */
	private final static String MAX_INCREASE = "MAX_INCREASE";													/* $Req: AUTOSAR $ */
	private final static String MAX_DECREASE = "MAX_DECREASE";													/* $Req: AUTOSAR $ */
	private final static String MAX_INCREASE_ASYNC = "MAX_INCREASE_ASYNC";										/* $Req: AUTOSAR $ */
	private final static String MAX_DECREASE_ASYNC = "MAX_DECREASE_ASYNC";										/* $Req: AUTOSAR $ */
	private final static String PRECISION = "PRECISION";														/* $Req: AUTOSAR $ */	
	private final static String PERIODIC = "PERIODIC";															/* $Req: AUTOSAR $ */
	private final static String LENGTH = "LENGTH";																/* $Req: AUTOSAR $ */
	private final static String ST_ACTION = "ACTION";															/* $Req: AUTOSAR $ */
	private final static String INCREMENTCOUNTER = "INCREMENTCOUNTER";											/* $Req: AUTOSAR $ */
	private final static String SCALABILITYCLASS = "SCALABILITYCLASS";											/* $Req: AUTOSAR $ */
	private final static String PROTECTIONHOOK = "PROTECTIONHOOK";												/* $Req: AUTOSAR $ */
	private final static String TIMING_PROTECTION = "TIMING_PROTECTION";										/* $Req: AUTOSAR $ */
	private final static String EXECUTIONBUDGET = "EXECUTIONBUDGET";											/* $Req: AUTOSAR $ */
	private final static String TIMEFRAME = "TIMEFRAME";														/* $Req: AUTOSAR $ */
	private final static String TIMELIMIT = "TIMELIMIT";														/* $Req: AUTOSAR $ */
	private final static String COUNTLIMIT = "COUNTLIMIT";														/* $Req: AUTOSAR $ */
	private final static String LOCKINGTIME = "LOCKINGTIME";													/* $Req: AUTOSAR $ */
	private final static String RESOURCELOCKTIME = "RESOURCELOCKTIME";											/* $Req: AUTOSAR $ */
	private final static String OSINTERRUPTLOCKTIME = "OSINTERRUPTLOCKTIME";									/* $Req: AUTOSAR $ */
	private final static String ALLINTERRUPTLOCKTIME = "ALLINTERRUPTLOCKTIME";									/* $Req: AUTOSAR $ */
	private final static String COUNTER_TYPE = "TYPE";															/* $Req: AUTOSAR $ */
	private final static String COUNTER_UNIT = "UNIT";															/* $Req: AUTOSAR $ */
	private final static String COUNTER_DEVICE_OPTIONS = "OPTIONS";												/* $Req: EXTENSION $ */
	private final static String COM_DEVICE = "DEVICE";															/* $Req: EXTENSION $ */
	private final static String COM_DEVICE_OPTIONS = "OPTIONS";													/* $Req: EXTENSION $ */
	private final static String ACTIONCALLBACKNAME = "ACTIONCALLBACKNAME";										/* $Req: EXTENSION $ */
	
	/**
	 * The OILFileInfo that represents the root file during an import
	 * @see #doLoadAndExpand
	 */
	private OILFileInfo rootFile=null; 
	
	/**
	 * List of directory paths searched for when locating input files.
	 * @see #addSearchPath(String)
	 * @see #locateNamedFile(String, boolean)
	 */
	private List<String> paths = new ArrayList<String>();
	
	/**
	 * Set of included {@link OILFileInfo} instances used to prevent infinite recursion during #include expansion
	 * 
	 * @see #doLoadAndExpand
	 */
	private Set<OILFileInfo> includedFiles = new HashSet<OILFileInfo>();	
	
	/**
	 * Set of defined values created by #define
	 */
	private Set<String> definedValues;

	/**
	 * List (Stack) of defined values that are currently determining line inclusion
	 */
	private List<String> valueStack = new ArrayList<String>();
	
	/**
	 * Values that is causing lines to be excluded from the input, used by #ifdef, null if lines not being excluded
	 */
	private String excludeValue;
	
	/**
	 * The PrintWriter to use during OIL export
	 */
	private PrintWriter writer = null;
	
	/**
	 * Current tabulation count used during OIL export
	 */
	private int tabs = 0;
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OIL import (extraction) helper methods.
	
	/**
	 * Attempts to locate the file with the given name (optionally) by appending the given name
	 * to the set of paths.
	 * @param fileName the name of the file to be found
	 * @param useSearchPath a flag that specifies whether to search for included files using the search paths
	 * @return the File object that represents the file with the given name, null if no file found
	 * @see #addSearchPath(String)
	 */
	private File locateNamedFile(String fileName, boolean useSearchPath) {
			
		if ( useSearchPath ) {
			// search for file using the search paths only (not the cwd)
			
			for ( String nextPath : paths ) {
				
				File file = new File(nextPath, fileName);
				
				if ( file.exists() )
					return file;
			}
		}
		else {
			// no search path, so use current working directory only
			File file = new File(fileName);
			
			if ( file.exists() )
				return file;	// file found in cwd			
		}
		
		return null;
	}
	
	/**
	 * Helper method for {@link #loadAndExpand(String, Set)}
	 * This helper initialises the {@link #rootFile} attribute.
	 * 
	 * @param buffer the String Buffer where to place the loaded and expanded file information
	 * @param parentFileInfo the OILFileInfo that included the named file (null if root file)
	 * @param fileName fileName of the file to be loaded and expanded
	 * @param startLineNo the line number on which the file was included (1 for root file)
	 * @param useSearchPath a flag that specifies whether to search for included files using the search paths
	 * @return the number of lines within the loaded & expanded file
	 * @throws IOException
	 */
	private int doLoadAndExpand(StringBuffer buffer, OILFileInfo parentFileInfo, String fileName, int startLineNo, boolean useSearchPath) throws IOException {
		
		// attempt to find the named file
		File file = locateNamedFile(fileName, useSearchPath);
		
		if ( file!=null ) {
			
			// create a new OILFileInfo to represent the file, passing parent, full pathname, filename only and start line no
			OILFileInfo fileInfo = new OILFileInfo(parentFileInfo, file.getPath(), file.getName(), startLineNo);
			
			int lineNo=startLineNo;
			int realLineNo=1;	// actual line number in current file
			
			// Use includedFiles Set to prevent infinite recursion (i.e. inclucion of self)
			if ( includedFiles.add(fileInfo) ) {
	
				BufferedReader reader = new BufferedReader(new FileReader(file));
				
				int stackSize = valueStack.size();		// record value stack size for balance checking of #ifdef and #endif
				
				String line;
				
				// read each line of text from the reader
				while ( (line = reader.readLine()) !=null ) {

					if ( line.startsWith("#") ) {
						
						if ( line.startsWith("#ifdef") ) {
							// line begins with #ifdef, so check if value defined
							String value=line.substring(6).trim();
							
							if ( value.length() > 0 ) {
								
								valueStack.add(value);		// add value String stack, so can balance #else and #endif
								
								if ( excludeValue==null && !definedValues.contains(value) ) {
									excludeValue = value;		// value is not defined, so start excluding lines (until matching #else or #endif)
								}
							}
							else
								throw new PreProcessorException("#ifdef directive missing value in '"+fileName+"' line "+realLineNo);
						}
						else if ( line.startsWith("#ifndef") ) {
							// line begins with #ifndef, so check if value defined
							String value=line.substring(7).trim();
							
							if ( value.length() > 0 ) {
								
								valueStack.add(value);		// add value String stack, so can balance #else and #endif
								
								if ( excludeValue==null && definedValues.contains(value) ) {
									excludeValue = value;		// value is defined, so start excluding lines (until matching #else or #endif)
								}
							}
							else
								throw new PreProcessorException("#ifndef directive missing value in '"+fileName+"' line "+realLineNo);
						}					
						else if ( line.startsWith("#else") ) {
							
							if ( valueStack.isEmpty() ) {
								throw new PreProcessorException("#else directive with no matching #ifdef in '"+fileName+"' line "+realLineNo);
							}
							
							String value = (String)valueStack.get(valueStack.size()-1);
							
							if ( excludeValue==null ) {
								excludeValue = value;
							}
							else if ( value==excludeValue ) {	// use == rather than equals() so exact Object match done rather than string compare
								excludeValue = null;
							}
						}
						else if ( line.startsWith("#endif") ) {
	
							if ( valueStack.isEmpty() ) {
								throw new PreProcessorException("#endif directive with no matching #ifdef in '"+fileName+"' line "+realLineNo);
							}
							
							String value = (String)valueStack.get(valueStack.size()-1);
							
							valueStack.remove(valueStack.size()-1);
							
							if ( value==excludeValue )	{		// use == rather than equals() so exact Object match done rather than string compare
								excludeValue = null;
							}
						}
						else if ( line.startsWith("#include") ) {
							
							if ( excludeValue == null ) {	// only actually process the directive if not excluding due to #ifdef or #ifndef
								// #include directive, so decide whether <filename> or "filename" form
								String fileSpec=line.substring(8).trim();
									
								if ( fileSpec.startsWith("<") && fileSpec.endsWith(">")) {
									
									// extract included filename and do recursive call, passing true so searchPath is used when locating the file
									lineNo += doLoadAndExpand(buffer, fileInfo, fileSpec.substring(1, fileSpec.length()-1), lineNo, true);
								}
								else if ( fileSpec.startsWith("\"") && fileSpec.endsWith("\"")) {
			
									// extract included filename and do recursive call, passing false so searchPath is not used when locating the file
									
									// need to prefix filename with the path of the including file
									String parentPath = file.getParent();
									
									if ( parentPath.length()>0 )
										parentPath+=File.separator;
										
									lineNo+=doLoadAndExpand(buffer, fileInfo, parentPath+fileSpec.substring(1, fileSpec.length()-1), lineNo, false);
								}
								else {
									// unrecognised #include format
									throw new PreProcessorException("#include directive has invalid file specification in '"+fileName+"' line "+realLineNo);
								}
							}
						}
						else if ( line.startsWith("#define") ) {
							
							if ( excludeValue == null ) {	// only actually process the directive if not excluding due to #ifdef or #ifndef
								// line begins with #define, so define the value
								String value=line.substring(7).trim();
								
								if ( value.length()>0 ) {
									// add defined symbol to set of defined symbols, duplicates ignored.
									definedValues.add(value);
								}
							}
						}
						else
							throw new PreProcessorException("Unrecognised preprocessor directive in '"+fileName+"' line "+realLineNo);

						line = "";	// remove preprocessor directive from input, so not parsed as OIL
					}
					else if ( excludeValue==null ) {
						// not excluding lines, so add to returned string
						buffer.append(line);
					}
 
					buffer.append(eol);
					
					lineNo++;
					realLineNo++;
				}
				
				// remove own fileInfo from the recursion prevention Set
				includedFiles.remove(fileInfo);
				
				fileInfo.setEndLineNo(lineNo);
				
				if ( parentFileInfo==null )
					rootFile = fileInfo;
				else {
					parentFileInfo.addIncludedFile(fileInfo);
				}
				
				if ( valueStack.size()!=stackSize ) {
					throw new PreProcessorException("Missing #endif in "+fileName);
				}				
			}
			
			return lineNo-startLineNo;
		}
		throw new PreProcessorException("File "+fileName+" not found");
	}
	
	/**
	 * Loads and expands a file, by locating #include commands within lines read from the file.
	 * The files identified by the #include are loaded (and possibly expanded themselves) then 
	 * inserted within the returned value at the position at which the #include appeared.
	 * 
	 * This method protects against infinite recursion by ignoring attempts to include
	 * files that already exist within the current include chain.
	 * 
	 * @param fileName of the file to be loaded and expanded
	 * @param constants a set of constant names predefined by the caller (i.e. implicitly #define's values), may be null
	 * @return the String that contains all the loaded information
	 * @throws IOException
	 * @see #addSearchPath(String)
	 */
	private String loadAndExpand(String fileName, Set<String> constants) throws IOException {

		// clear the root file info.
		rootFile=null;
		
		// init the includedFiles set that prevent infinite recursion during #include
		includedFiles.clear();
		
		// init the defined values set
		if ( constants != null ) {
			// predefined constants exist, so copy that set
			definedValues = new HashSet<String>(constants);
		}
		else {
			definedValues = new HashSet<String>();
		}
		
		valueStack.clear();
		
		excludeValue = null;
		
		StringBuffer buffer=new StringBuffer();
		
		// call helper to perform the load, pass false so search paths are not used for the root file
		// this also initialises the rootFile attribute
		doLoadAndExpand(buffer, null,fileName,1,false);
		
		return buffer.toString();
	}	
	
	/**
	 * Checks whether the named parameter has a value of AUTO assigned for the given OSEK object.
	 * 
	 * This method checks whether "AUTO" has been explicitly assigned to the value,
	 * or whether no value was specified and "AUTO" is the default value.
	 * 
	 * 
	 * @param paramName - the name of the parameter for which to check for the AUTO value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return true if the given parameter was assigned a value of AUTO or has a default value of AUTO
	 */
	private static boolean isAutoParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);
		
		if ( value instanceof AutoValue ) {
			// the value of AUTO was explicitly specified for the paramater
			return true;
		}
		
		if ( value == null ) {
			// No value was specified so check if the definition of the parameter was marked as defaulting to AUTO
			FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
			
			if (definition instanceof AttributeDefinition) {
				// is an attribute definition (which may be AUTO by default)
				return ( ((AttributeDefinition)definition).isAuto() );
			}
		}
		
		return false;
	}	
	
	/**
	 * Returns any description that is associated with the named parameter for the given OSEK object.
	 * 
	 * @param paramName - the name of the parameter for which the description is required
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the description associated with the parameter
	 */
	private static String getParamDescription(String paramName, ObjectDefinition objDef) {
		
		Parameter param = objDef.getNamedParameter(paramName);
		
		if ( param != null ) {
			return param.getDescription();
		}
		return null;
	}
	
	/**
	 * Returns any description that is associated with the named sub-parameter for the given ParameterizedValue.
	 * 
	 * @param paramName - the name of the parameter for which the description is required
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the description associated with the parameter
	 */
	private static String getSubParamDescription(String paramName, ParameterizedValue value) {
		
		Parameter param = value.getNamedSubParameter(paramName);
		
		if ( param != null ) {
			return param.getDescription();
		}
		return null;
	}	
	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is a Boolean typed attribute (otherwise returns null).
	 *  
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static Boolean getBooleanParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);

		if ( !(value instanceof AutoValue) ) {	
			
			if ( value instanceof BooleanValue ) {
				// value has been specified for the named parameter
				return ((BooleanValue)value).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
				
				if (definition instanceof BoolAttributeDef) {
					
					BoolAttributeDef boolDefinition = (BoolAttributeDef)definition;
					
					if (boolDefinition.hasDefault()) {
						return boolDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}

	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is a Boolean typed attribute (otherwise returns null).
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */
	private static Boolean getBooleanSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof BooleanValue ) {
				// value has been specified for the named sub-parameter
				return ((BooleanValue)subValue).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = value.getNamedSubFeatureDefinition(subParamName);
				
				if (definition instanceof BoolAttributeDef) {
					
					BoolAttributeDef boolDefinition = (BoolAttributeDef)definition;
					
					if (boolDefinition.hasDefault()) {
						return boolDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	
	
	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is a String typed attribute (otherwise returns null).
	 * 
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static String getStringParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);

		if ( !(value instanceof AutoValue) ) {	
			
			if ( value instanceof StringValue ) {
				// value has been specified for the named parameter
				return ((StringValue)value).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
				
				if (definition instanceof StringAttributeDef) {
					
					StringAttributeDef strDefinition = (StringAttributeDef)definition;
					
					if (strDefinition.hasDefault()) {
						return strDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}	
	
	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is a String typed attribute (otherwise returns null).
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */
	private static String getStringSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof StringValue ) {
				// value has been specified for the named sub-parameter
				return ((StringValue)subValue).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = value.getNamedSubFeatureDefinition(subParamName);
				
				if (definition instanceof StringAttributeDef) {
					
					StringAttributeDef strDefinition = (StringAttributeDef)definition;
					
					if (strDefinition.hasDefault()) {
						return strDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	

	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is an Enum typed attribute (otherwise returns null).
	 *  
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static String getEnumParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);
		
		if ( !(value instanceof AutoValue) ) {
			
			if ( value instanceof NameValue ) {
				// value has been specified for the named parameter
				return ((NameValue)value).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
				
				if (definition instanceof EnumerationAttributeDef) {
					
					EnumerationAttributeDef enumDefinition = (EnumerationAttributeDef)definition;
					
					if (enumDefinition.hasDefault()) {
						return enumDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}	

	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is an Enum typed attribute (otherwise returns null).
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */
	private static String getEnumSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof NameValue ) {
				// value has been specified for the named sub-parameter
				return ((NameValue)subValue).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = value.getNamedSubFeatureDefinition(subParamName);
				
				if (definition instanceof EnumerationAttributeDef) {
					
					EnumerationAttributeDef enumDefinition = (EnumerationAttributeDef)definition;
					
					if (enumDefinition.hasDefault()) {
						return enumDefinition.getDefaultValue();
					}
				}				
			}
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	
	
	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is a Reference typed attribute (otherwise returns null).
	 *  
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist (no default values can be assigned)
	 */
	private static String getRefParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);
		
		if ( !(value instanceof AutoValue) ) {
			
			if ( value instanceof NameValue ) {
				// value has been specified for the named parameter
				return ((NameValue)value).getValue();
			}
			// else
				// No DEFAULT values can be assigned to Reference parameters
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}	
	
	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is a Reference typed attribute (otherwise returns null).
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist (no default values can be assigned)
	 */
	private static String getRefSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof NameValue ) {
				// value has been specified for the named sub-parameter
				return ((NameValue)subValue).getValue();
			}
			// else
				// No DEFAULT values can be assigned to Reference parameters
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	
	
	
	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is an Integer typed attribute (otherwise returns null).
	 * 
	 * This call should be used to read BigInteger values (i.e. UINT64 range values)
	 * 
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the BigInteger value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static BigInteger getBigIntegerParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);
		
		if ( !(value instanceof AutoValue) ) {
			
			if ( value instanceof IntegerValue ) {
				// value has been specified for the named parameter
				return ((IntegerValue)value).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
				
				if (definition instanceof IntegerAttributeDef) {
					
					IntegerAttributeDef intDefinition = (IntegerAttributeDef)definition;
					
					if (intDefinition.hasDefault()) {
						return intDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}	

	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is an Integer typed attribute (otherwise returns null).
	 * 
	 * This call should be used to read regular Integer values (i.e. UINT32 range values)
	 * 
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static Long getIntegerParam(String paramName, ObjectDefinition objDef) {
	
		BigInteger bigInteger = getBigIntegerParam(paramName, objDef);
		
		return (bigInteger != null) ? bigInteger.longValue() : null;
	}
	
	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is an Integer typed attribute (otherwise returns null).
	 * 
	 * This call should be used to read BigInteger values (i.e. UINT64 range values)
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the BigInteger value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */
	private static BigInteger getBigIntegerSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof IntegerValue ) {
				// value has been specified for the named sub-parameter
				return ((IntegerValue)subValue).getValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				
				FeatureDefinition definition = value.getNamedSubFeatureDefinition(subParamName);
				
				if (definition instanceof IntegerAttributeDef) {
					
					IntegerAttributeDef intDefinition = (IntegerAttributeDef)definition;
					
					if (intDefinition.hasDefault()) {
						return intDefinition.getDefaultValue();
					}
				}
			}
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	
	
	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is an Integer typed attribute (otherwise returns null).
	 * 
	 * This call should be used to read regular Integer values (i.e. UINT32 range values)
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */	
	private static Long getIntegerSubParam(String subParamName, ParameterizedValue value) {
		
		BigInteger bigInteger = getBigIntegerSubParam(subParamName, value);
		
		return (bigInteger != null) ? bigInteger.longValue() : null;
	}	
	
	
	/**
	 * Gets and returns the value assigned to the named parameter for the given OSEK object,
	 * providing that the value is a Float typed attribute (otherwise returns null).
	 * 
	 * @param paramName - the name of the parameter for which to extract the value
	 * @param objDef - the ObjectDefinition that represents the OSEK object in the OIL model
	 * @return the value of the named parameter, null if parameter does not exist or has no default value
	 */
	private static Double getFloatParam(String paramName, ObjectDefinition objDef) {
		
		FeatureValue value=objDef.getNamedParameterValue(paramName);
		
		if ( !(value instanceof AutoValue) ) {
			
			if ( value instanceof FloatValue ) {
				// value has been specified for the named parameter
				return ((FloatValue)value).getValue().doubleValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = objDef.getNamedFeatureDefinition(paramName);
				
				if (definition instanceof FloatAttributeDef) {
					
					FloatAttributeDef floatDefinition = (FloatAttributeDef)definition;
					
					if (floatDefinition.hasDefault()) {
						return floatDefinition.getDefaultValue().doubleValue();
					}
				}
			}
		}
		return null;	// unable to find named Parameter value (or is AUTO value)
	}	
	
	/**
	 * Gets and returns the value assigned to the named sub-parameter for the given ParameterizedValue,
	 * providing that the value is a Float typed attribute (otherwise returns null).
	 * 
	 * @param subParamName - the name of the sub-parameter for which to extract the value
	 * @param value - the ParameterizedValue that owns the named subParameter
	 * @return the value of the named sub-parameter, null if the sub-parameter does not exist or has no default value
	 */
	private static Double getFloatSubParam(String subParamName, ParameterizedValue value) {
		
		FeatureValue subValue = value.getNamedSubParameterValue(subParamName);
		
		if ( !(subValue instanceof AutoValue) ) {	
			
			if ( subValue instanceof FloatValue ) {
				// value has been specified for the named sub-parameter
				return ((FloatValue)subValue).getValue().doubleValue();
			}
			else {
				// if value is not specified then attempt to read the DEFAULT value from the OIL model
				FeatureDefinition definition = value.getNamedSubFeatureDefinition(subParamName);
				
				if (definition instanceof FloatAttributeDef) {
					
					FloatAttributeDef floatDefinition = (FloatAttributeDef)definition;
					
					if (floatDefinition.hasDefault()) {
						return floatDefinition.getDefaultValue().doubleValue();
					}
				}
			}
		}
		return null;	// unable to find named SubParameter value (or is AUTO value)
	}	
	
	
	/**
	 * Helper method that extracts the ACCESSING_APPLICATION parameter values from the given ObjectDefinition
	 * and sets up the specified OSModelElement as being accessed by the identified Application objects.
	 * 
	 * $Req: AUTOSAR $
	 * 
	 * @param element the OSModelElement being accessed
	 * @param objDef the ObjectDefinition that contains zero or more ACCESSING_APPLICATION parameters that refer to accessing Application objects
	 */
	private static void extractAccessingApps(OSModelElement element,ObjectDefinition objDef) {
		
		Object [] valueList=objDef.getNamedParameterValueList(ACCESSING_APPLICATION);
		
		int index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String appName = ((NameValue)value).getValue();
				
				element.addAccessingApplication(element.getCpu().getNamedApplication(appName));
				
				element.addMultiAttribDescription(ACCESSING_APPLICATION, value.getDescription(), index++);
			}
		}			
	}	
	
	/**
	 * Helper method that extracts a list of LOCKINGTIME details when TIMING_PROTECTION flag is true
	 * within a Runnable (Task or ISR)
	 * 
	 * $Req: AUTOSAR $
	 * 
	 * @param timingProtection the BooleanValue that represents the TIMING_PROTECTION flag
	 * @param runnable the Task or ISR within the OS model into which the locking time are to be extracted
	 */
	private static void extractLockingTimes(BooleanValue timingProtection, Runnable runnable) {
		
		// Get the list of LOCKINGTIME details
		FeatureValue [] valueList=((BooleanValue)timingProtection).getNamedSubParameterValueList(LOCKINGTIME);
		
		int index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue subValue=valueList[i];
			
			if ( subValue instanceof NameValue ) {
			
				// map locktime name to a LockingTimeKind instance
				LockingTimeKind lockingTimeKind = LockingTimeKind.get(((NameValue)subValue).getValue());
				
				runnable.addMultiAttribDescription(LOCKINGTIME, subValue.getDescription(), index);
				
				// Create a LockingTime instance within the Runnable, using the LockingTimeKind instance
				LockingTime lockingTime = runnable.createLockingTime(lockingTimeKind);
				
				// Now extract sub parameter values depending on the locking time kind specified
				if ( LockingTimeKind.RESOURCELOCK_LITERAL.equals(lockingTimeKind) ) {
					// is a RESOURCELOCK, so get the RESOURCE and RESOURCELOCKTIME values
					
					String resName = getRefSubParam(RESOURCE,(NameValue)subValue);
					
					if ( resName != null ) {
						lockingTime.setResource(runnable.getCpu().getNamedResource(resName));
						runnable.addMultiAttribDescription(RESOURCE, getSubParamDescription(RESOURCE, (NameValue)subValue), index);
					}							

					BigInteger resLockTime = getBigIntegerSubParam(RESOURCELOCKTIME,(NameValue)subValue);
					
					if ( resLockTime != null ) {
						lockingTime.setResourceLockTime(resLockTime);
						runnable.addMultiAttribDescription(RESOURCELOCKTIME, getSubParamDescription(RESOURCELOCKTIME, (NameValue)subValue), index);						
					}	
							
				}
				else if ( LockingTimeKind.INTERRUPTLOCK_LITERAL.equals(lockingTimeKind) ) {
					// is a INTERRUPTLOCK, so get the OSINTERRUPTLOCKTIME and ALLINTERRUPTLOCKTIME values
					
					BigInteger osIntLockTime = getBigIntegerSubParam(OSINTERRUPTLOCKTIME,(NameValue)subValue);
					
					if ( osIntLockTime != null ) {
						lockingTime.setOSInterruptLockTime(osIntLockTime);
						runnable.addMultiAttribDescription(OSINTERRUPTLOCKTIME, getSubParamDescription(OSINTERRUPTLOCKTIME, (NameValue)subValue), index);
					}
					
					BigInteger allIntLockTime = getBigIntegerSubParam(ALLINTERRUPTLOCKTIME,(NameValue)subValue);
					
					if ( allIntLockTime != null ) {
						lockingTime.setAllInterruptLockTime(allIntLockTime);
						runnable.addMultiAttribDescription(ALLINTERRUPTLOCKTIME, getSubParamDescription(ALLINTERRUPTLOCKTIME, (NameValue)subValue), index);						
					}							
				}
				index++;
			}
		}
	}

	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Os.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Os.
	 * @param os the Os into which to extract information
	 * @param objDef the ObjectDefinition that represents the Os in the OIL model
	 */		
	private static void extractOs(Os os,ObjectDefinition objDef) {

		Boolean boolVal;
		String strVal;
		Long longVal;

		os.setDescription(objDef.getDescription());	// setup the description
		
		// setup Os STATUS
		strVal = getEnumParam(STATUS, objDef);
		
		if ( strVal != null ) {
			os.setStatus(StatusKind.get(strVal));
			os.addAttribDescription(STATUS, getParamDescription(STATUS, objDef));
		}		
		
		// setup Os STARTUPHOOK flag
		boolVal = getBooleanParam(STARTUPHOOK, objDef);
		
		if ( boolVal != null ) {
			os.setStartupHook(boolVal);
			os.addAttribDescription(STARTUPHOOK, getParamDescription(STARTUPHOOK, objDef));
		}		

		// setup Os ERRORHOOK flag
		boolVal = getBooleanParam(ERRORHOOK, objDef);
		
		if ( boolVal != null ) {
			os.setErrorHook(boolVal);
			os.addAttribDescription(ERRORHOOK, getParamDescription(ERRORHOOK, objDef));
		}		
		
		// setup Os SHUTDOWNHOOK flag
		boolVal = getBooleanParam(SHUTDOWNHOOK, objDef);
		
		if ( boolVal != null ) {
			os.setShutdownHook(boolVal);
			os.addAttribDescription(SHUTDOWNHOOK, getParamDescription(SHUTDOWNHOOK, objDef));
		}		
		
		// setup Os PRETASKHOOK flag
		boolVal = getBooleanParam(PRETASKHOOK, objDef);
		
		if ( boolVal != null ) {
			os.setPreTaskHook(boolVal);
			os.addAttribDescription(PRETASKHOOK, getParamDescription(PRETASKHOOK, objDef));
		}		
		
		// setup Os POSTTASKHOOK flag
		boolVal = getBooleanParam(POSTTASKHOOK, objDef);
		
		if ( boolVal != null ) {
			os.setPostTaskHook(boolVal);
			os.addAttribDescription(POSTTASKHOOK, getParamDescription(POSTTASKHOOK, objDef));
		}		
				
		// setup Os USEGETSERVICEID flag
		boolVal = getBooleanParam(USEGETSERVICEID, objDef);
		
		if ( boolVal != null ) {
			os.setUseGetServiceId(boolVal);
			os.addAttribDescription(USEGETSERVICEID, getParamDescription(USEGETSERVICEID, objDef));
		}	
				
		// setup Os USEPARAMETERACCESS flag
		boolVal = getBooleanParam(USEPARAMETERACCESS, objDef);
		
		if ( boolVal != null ) {
			os.setUseParameterAccess(boolVal);
			os.addAttribDescription(USEPARAMETERACCESS, getParamDescription(USEPARAMETERACCESS, objDef));
		}			
		
		// setup Os USERESSCHEDULER flag
		boolVal = getBooleanParam(USERESSCHEDULER, objDef);
		
		if ( boolVal != null ) {
			os.setUseResScheduler(boolVal);
			os.addAttribDescription(USERESSCHEDULER, getParamDescription(USERESSCHEDULER, objDef));
		}
					
		// setup Os PROTECTIONHOOK flag
		boolVal = getBooleanParam(PROTECTIONHOOK, objDef);			/* $Req: AUTOSAR $ */
		
		if ( boolVal != null ) {
			os.setProtectionHook(boolVal);
			os.addAttribDescription(PROTECTIONHOOK, getParamDescription(PROTECTIONHOOK, objDef));
		}	
		
		// setup Os SCALABILITYCLASS flag
		strVal = getEnumParam(SCALABILITYCLASS, objDef);			/* $Req: AUTOSAR $ */
		
		if ( strVal != null ) {
			os.setScalabilityClass(ScalabilityClassKind.get(strVal));
			os.addAttribDescription(SCALABILITYCLASS, getParamDescription(SCALABILITYCLASS, objDef));
		}
		else if (isAutoParam(SCALABILITYCLASS, objDef)) {
			os.setAutoScalabilityClass(true);
			os.addAttribDescription(SCALABILITYCLASS, getParamDescription(SCALABILITYCLASS, objDef));
		}
		
		// setup Os STACKMONITORING flag
		boolVal = getBooleanParam(STACKMONITORING, objDef);			/* $Req: AUTOSAR $ */
		
		if ( boolVal != null ) {
			os.setStackChecking(boolVal);
			os.addAttribDescription(STACKMONITORING, getParamDescription(STACKMONITORING, objDef));
		}			
		
		// setup Os RESTARTABLE flag
		boolVal = getBooleanParam(RESTARTABLE, objDef);				/* $Req: EXTENSION $ */
		
		if ( boolVal != null ) {
			os.setRestartable(boolVal);
			os.addAttribDescription(RESTARTABLE, getParamDescription(RESTARTABLE, objDef));
		}
		
		// setup HANDLE_OSCFAILURE flag
		boolVal = getBooleanParam(HANDLE_OSCFAILURE, objDef);		/* $Req: EXTENSION $ */
		
		if ( boolVal != null ) {
			os.setOscFailureHandled(boolVal);
			os.addAttribDescription(HANDLE_OSCFAILURE, getParamDescription(HANDLE_OSCFAILURE, objDef));
		}		
		
		// setup HANDLE_ADDRERROR flag
		boolVal = getBooleanParam(HANDLE_ADDRERROR, objDef);		/* $Req: EXTENSION $ */
		
		if ( boolVal != null ) {
			os.setAddrErrorHandled(boolVal);
			os.addAttribDescription(HANDLE_ADDRERROR, getParamDescription(HANDLE_ADDRERROR, objDef));
		}
		
		// setup HANDLE_MATHERROR flag
		boolVal = getBooleanParam(HANDLE_MATHERROR, objDef);		/* $Req: EXTENSION $ */
		
		if ( boolVal != null ) {
			os.setMathErrorHandled(boolVal);
			os.addAttribDescription(HANDLE_MATHERROR, getParamDescription(HANDLE_MATHERROR, objDef));
		}		
		
		// setup OS PRETASKHOOK_STACKSIZE
		longVal = getIntegerParam(PRETASKHOOK_STACKSIZE, objDef);	/* $Req: EXTENSION $ */
		
		if ( longVal != null ) {		
			os.setPreTaskHookStackSize(longVal);
			os.addAttribDescription(PRETASKHOOK_STACKSIZE, getParamDescription(PRETASKHOOK_STACKSIZE, objDef));
		}
		else if (isAutoParam(PRETASKHOOK_STACKSIZE, objDef)) {
			os.setAutoPreTaskHookStackSize(true);
			os.addAttribDescription(PRETASKHOOK_STACKSIZE, getParamDescription(PRETASKHOOK_STACKSIZE, objDef));
		}
		
		// setup OS POSTTASKHOOK_STACKSIZE
		longVal = getIntegerParam(POSTTASKHOOK_STACKSIZE, objDef);	/* $Req: EXTENSION $ */
		
		if ( longVal != null ) {		
			os.setPostTaskHookStackSize(longVal);
			os.addAttribDescription(POSTTASKHOOK_STACKSIZE, getParamDescription(POSTTASKHOOK_STACKSIZE, objDef));
		}
		else if (isAutoParam(POSTTASKHOOK_STACKSIZE, objDef)) {
			os.setAutoPostTaskHookStackSize(true);
			os.addAttribDescription(POSTTASKHOOK_STACKSIZE, getParamDescription(POSTTASKHOOK_STACKSIZE, objDef));
		}		
	}
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Com.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Com.
	 * @param com the Com into which to extract information
	 * @param objDef the ObjectDefinition that represents the Com in the OIL model
	 */		
	private static void extractCom(Com com,ObjectDefinition objDef) {

		Object [] valueList;
		Boolean boolVal;

		com.setDescription(objDef.getDescription());	// setup the description
		
		// setup COMUSEGETSERVICEID flag
		boolVal = getBooleanParam(COMERRORHOOK, objDef);
		
		if ( boolVal != null ) {
			com.setErrorHook(boolVal);
			com.addAttribDescription(COMERRORHOOK, getParamDescription(COMERRORHOOK, objDef));
		}		
			
		// setup COMUSEGETSERVICEID flag
		boolVal = getBooleanParam(COMUSEGETSERVICEID, objDef);
		
		if ( boolVal != null ) {
			com.setUseGetServiceId(boolVal);
			com.addAttribDescription(COMUSEGETSERVICEID, getParamDescription(COMUSEGETSERVICEID, objDef));
		}		
	
		// setup COMUSEPARAMETERACCESS flag
		boolVal = getBooleanParam(COMUSEPARAMETERACCESS, objDef);
		
		if ( boolVal != null ) {
			com.setUseParameterAccess(boolVal);
			com.addAttribDescription(COMUSEPARAMETERACCESS, getParamDescription(COMUSEPARAMETERACCESS, objDef));
		}				

		// setup COMSTARTCOMEXTENSION flag
		boolVal = getBooleanParam(COMSTARTCOMEXTENSION, objDef);
		
		if ( boolVal != null ) {
			com.setStartComExtension(boolVal);
			com.addAttribDescription(COMSTARTCOMEXTENSION, getParamDescription(COMSTARTCOMEXTENSION, objDef));
		}	

		// setup COMAPPMODE name list
		valueList=objDef.getNamedParameterValueList(COMAPPMODE);
		
		int index = 0;
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof StringValue ) {
				
				String appModeName = ((StringValue)value).getValue();
				
				com.addAppModeName(appModeName);
				
				com.addMultiAttribDescription(COMAPPMODE, value.getDescription(), index++);
			}
		}
		
		// setup COMSTATUS
		String enumVal = getEnumParam(COMSTATUS, objDef);
		
		if ( enumVal != null ) {
			com.setStatus(ComStatusKind.get(enumVal));
			com.addAttribDescription(COMSTATUS, getParamDescription(COMSTATUS, objDef));
		}
	}

	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Nm.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Nm.
	 * @param nm the Nm into which to extract information
	 * @param objDef the ObjectDefinition that represents the Nm in the OIL model
	 */	
	private static void extractNm(Nm nm,ObjectDefinition objDef) {

		nm.setDescription(objDef.getDescription());	// setup the description
		
		// no OIL parameters defined for Nm

	}	

	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Task.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Task.
	 * @param task the Task into which to extract information
	 * @param objDef the ObjectDefinition that represents the task in the OIL model
	 */
	private static void extractTask(Task task,ObjectDefinition objDef) {
		
		Object [] valueList;
		Long longVal;
		String enumVal;
	
		task.setDescription(objDef.getDescription());	// setup the description
		
		// setup task PRIORITY
		longVal = getIntegerParam(PRIORITY, objDef);
		
		if ( longVal != null ) {

			task.setPriority(longVal);
			task.addAttribDescription(PRIORITY, getParamDescription(PRIORITY, objDef));
		}		
		
		// setup task ACTIVATION count
		longVal = getIntegerParam(ACTIVATION, objDef);
		
		if ( longVal != null ) {
			task.setActivation(longVal);
			task.addAttribDescription(ACTIVATION, getParamDescription(ACTIVATION, objDef));
		}		
		
		// setup task STACKSIZE
		longVal = getIntegerParam(STACKSIZE, objDef);
		
		if ( longVal != null ) {

			task.setStackSize(longVal);
			
			task.addAttribDescription(STACKSIZE, getParamDescription(STACKSIZE, objDef));
		}
		else if ( isAutoParam(STACKSIZE, objDef) ) {
			task.setAutoStackSize(true);
			task.addAttribDescription(STACKSIZE, getParamDescription(STACKSIZE, objDef));
		}
				
		// setup task preemptability SCHEDULE ability
		enumVal = getEnumParam(SCHEDULE, objDef);
		
		if ( enumVal != null ) {
			task.setSchedule(ScheduleKind.get(enumVal));
			task.addAttribDescription(SCHEDULE, getParamDescription(SCHEDULE, objDef));
		}

		// setup task AUTOSTART flag
		Boolean boolVal = getBooleanParam(AUTOSTART, objDef);
		
		if ( boolVal != null ) {
			
			boolean autostarted=boolVal;
			
			task.setAutostart(autostarted);
			
			task.addAttribDescription(AUTOSTART, getParamDescription(AUTOSTART, objDef));
			
			if ( autostarted ) {
				// if autostarted task, then get sub-parameter values that specify associated appmodes
				
				FeatureValue value = objDef.getNamedParameterValue(AUTOSTART);
				
				assert value instanceof BooleanValue;
				
				FeatureValue [] subValues=((BooleanValue)value).getNamedSubParameterValueList(APPMODE);
				
				int index = 0;
				for ( int i=0; i<subValues.length; i++) {
					
					FeatureValue subValue=subValues[i];
					
					if ( subValue instanceof NameValue ) {
						
						String appModeName = ((NameValue)subValue).getValue();
						
						task.addAppMode(task.getCpu().getNamedAppMode(appModeName));
						
						task.addMultiAttribDescription(APPMODE, subValue.getDescription(), index++);
					}
				}					
			}				
		}
		
		// setup accessed RESOURCEs (if any)
		valueList=objDef.getNamedParameterValueList(RESOURCE);
		
		int index = 0;
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String resName = ((NameValue)value).getValue();
				
				task.addResource(task.getCpu().getNamedResource(resName));
				
				task.addMultiAttribDescription(RESOURCE, value.getDescription(), index++);
			}
		}
		
		// setup activating EVENTs (if any)
		valueList=objDef.getNamedParameterValueList(EVENT);
		index = 0;
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String eventName = ((NameValue)value).getValue();
				
				task.addEvent(task.getCpu().getNamedEvent(eventName));
				
				task.addMultiAttribDescription(EVENT, value.getDescription(), index++);
			}
		}			
		
		// setup a list of MESSAGEs accessed by the task (runnable).
		valueList=objDef.getNamedParameterValueList(MESSAGE);
		index = 0;
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String messageName = ((NameValue)value).getValue();
				
				task.addAccessedMessage(task.getCpu().getNamedMessage(messageName));
				
				task.addMultiAttribDescription(MESSAGE, value.getDescription(), index++);
			}
		}
		
		// setup task TIMING_PROTECTION flag
		boolVal = getBooleanParam(TIMING_PROTECTION, objDef);	/* $Req: AUTOSAR $ */
		
		if ( boolVal != null ) {
			
			boolean timingProtection=boolVal;
			
			task.setTimingProtection(timingProtection);
			
			task.addAttribDescription(TIMING_PROTECTION, getParamDescription(TIMING_PROTECTION, objDef));
			
			if ( timingProtection ) {
				// if timingProtection flag set, then get sub-parameter values that specify associated details
				
				FeatureValue value = objDef.getNamedParameterValue(TIMING_PROTECTION);
				
				assert value instanceof BooleanValue;
				
				BigInteger execBudget = getBigIntegerSubParam(EXECUTIONBUDGET,(BooleanValue)value);
				
				if ( execBudget != null ) {
					task.setExecutionBudget(execBudget);
					task.addAttribDescription(EXECUTIONBUDGET, getSubParamDescription(EXECUTIONBUDGET, (BooleanValue)value));
				}
				
				BigInteger timeFrame = getBigIntegerSubParam(TIMEFRAME,(BooleanValue)value);
				
				if ( timeFrame != null ) {
					task.setTimeFrame(timeFrame);
					task.addAttribDescription(TIMEFRAME, getSubParamDescription(TIMEFRAME, (BooleanValue)value));					
				}
				
				BigInteger timeLimit = getBigIntegerSubParam(TIMELIMIT,(BooleanValue)value);
				
				if ( timeLimit != null ) {
					task.setTimeLimit(timeLimit);
					task.addAttribDescription(TIMELIMIT, getSubParamDescription(TIMELIMIT, (BooleanValue)value));					
				}					

				// extract the list of LOCKINGTIME details using helper method (shared with ISR extraction)
				extractLockingTimes((BooleanValue)value, task);
			}
		}

		// setup a list of APPLICATIONs that can access the task, use common helper method
		extractAccessingApps(task, objDef);		/* $Req: AUTOSAR $ */
		
	}	
	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model ISR.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the ISR.
	 * @param isr the ISR into which to extract information
	 * @param objDef the ObjectDefinition that represents the ISR in the OIL model
	 */	
	private static void extractIsr(Isr isr,ObjectDefinition objDef) {
	
		Object [] valueList;
		Long longVal;
		Boolean boolVal;
		String strVal;
	
		isr.setDescription(objDef.getDescription());	// setup the description
		
		// setup ISR CATEGORY
		longVal = getIntegerParam(CATEGORY, objDef);
		
		if ( longVal != null ) {
			isr.setCategory(longVal);	
			isr.addAttribDescription(CATEGORY, getParamDescription(CATEGORY, objDef));
		}
		
		// setup ISR PRIORITY
		longVal = getIntegerParam(PRIORITY, objDef);
		
		if ( longVal != null ) {
			isr.setPriority(longVal);
			isr.addAttribDescription(PRIORITY, getParamDescription(PRIORITY, objDef));
		}			
		
		// setup ISR DISABLE_STACKMONITORING flag
		boolVal = getBooleanParam(DISABLE_STACKMONITORING, objDef);
		
		if ( boolVal != null ) {
			isr.setStackCheckingEnabled(!boolVal);
			isr.addAttribDescription(DISABLE_STACKMONITORING, getParamDescription(DISABLE_STACKMONITORING, objDef));
		}		
		
		// setup ISR stacksize
		longVal = getIntegerParam(STACKSIZE, objDef);
		
		if ( longVal != null ) {

			isr.setStackSize(longVal);
			isr.addAttribDescription(STACKSIZE, getParamDescription(STACKSIZE, objDef));
		}
		else if ( isAutoParam(STACKSIZE, objDef) ) {
			isr.setAutoStackSize(true);
			isr.addAttribDescription(STACKSIZE, getParamDescription(STACKSIZE, objDef));
		}
	
		// setup ISR VECTOR
		strVal = getStringParam(VECTOR, objDef);
		
		if ( strVal != null ) {
			isr.setVector(strVal);
			isr.addAttribDescription(VECTOR, getParamDescription(VECTOR, objDef));
		}
		else if ( isAutoParam(VECTOR, objDef) ) {
			isr.setVector(isr.getName());	// AUTO specified rather than actual vector value, so use name of the ISR
			isr.addAttribDescription(VECTOR, getParamDescription(VECTOR, objDef));
		}
		
		// setup accessed RESOURCEs (if any)
		valueList=objDef.getNamedParameterValueList(RESOURCE);
		
		int index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String resName = ((NameValue)value).getValue();
				
				isr.addResource(isr.getCpu().getNamedResource(resName));
				
				isr.addMultiAttribDescription(RESOURCE, value.getDescription(), index++);
			}
		
		}
		
		// setup a list of MESSAGEs accessed by the ISR (runnable).
		valueList=objDef.getNamedParameterValueList(MESSAGE);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String messageName = ((NameValue)value).getValue();
				
				isr.addAccessedMessage(isr.getCpu().getNamedMessage(messageName));
				
				isr.addMultiAttribDescription(MESSAGE, value.getDescription(), index++);
			}
		}
		
		// setup task TIMING_PROTECTION flag
		boolVal = getBooleanParam(TIMING_PROTECTION, objDef);	/* $Req: AUTOSAR $ */
		
		if ( boolVal != null ) {
			
			boolean timingProtection=boolVal;
			
			isr.setTimingProtection(timingProtection);
			
			isr.addAttribDescription(TIMING_PROTECTION, getParamDescription(TIMING_PROTECTION, objDef));
			
			if ( timingProtection ) {
				// if timingProtection flag set, then get sub-parameter values that specify associated details
				
				FeatureValue value = objDef.getNamedParameterValue(TIMING_PROTECTION);
				
				assert value instanceof BooleanValue;
				
				BigInteger execBudget = getBigIntegerSubParam(EXECUTIONBUDGET,(BooleanValue)value);
				
				if ( execBudget != null ) {
					isr.setExecutionBudget(execBudget);
					isr.addAttribDescription(EXECUTIONBUDGET, getSubParamDescription(EXECUTIONBUDGET, (BooleanValue)value));
				}
				
				Long countLimit = getIntegerSubParam(COUNTLIMIT,(BooleanValue)value);
				
				if ( countLimit != null ) {
					isr.setCountLimit(countLimit);
					isr.addAttribDescription(COUNTLIMIT, getSubParamDescription(COUNTLIMIT, (BooleanValue)value));
				}
				
				BigInteger timeLimit = getBigIntegerSubParam(TIMELIMIT,(BooleanValue)value);
				
				if ( timeLimit != null ) {
					isr.setTimeLimit(timeLimit);
					isr.addAttribDescription(TIMELIMIT, getSubParamDescription(TIMELIMIT, (BooleanValue)value));
				}
				
				// extract the list of LOCKINGTIME details using helper method (shared with Task extraction)
				extractLockingTimes((BooleanValue)value, isr);
			}
		}
		
		
	}	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Event.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Event.
	 * @param event the Event into which to extract information
	 * @param objDef the ObjectDefinition that represents the Event in the OIL model
	 */	
	private static void extractEvent(Event event,ObjectDefinition objDef) {

		event.setDescription(objDef.getDescription());	// setup the description
		
		// setup event MASK value
		FeatureValue value=objDef.getNamedParameterValue(MASK);
		
		if ( value instanceof IntegerValue ) {
				
			event.setMask(((IntegerValue)value).getValue());
			
			event.addAttribDescription(MASK, value.getDescription());
		}
		else if ( isAutoParam(MASK, objDef) ) {
			event.isAutoMask(true);	// AUTO specified rather than actual Mask value
			
			event.addAttribDescription(MASK, getParamDescription(MASK, objDef));
		}
	}	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Alarm.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Alarm.
	 * @param alarm the Alarm into which to extract information
	 * @param objDef the ObjectDefinition that represents the Alarm in the OIL model
	 */		
	private static void extractAlarm(Alarm alarm,ObjectDefinition objDef) {

		String enumVal;
		Boolean boolVal;
		
		alarm.setDescription(objDef.getDescription());	// setup the description
		
		// setup COUNTER to which the alarm is assigned
		String refVal = getRefParam(COUNTER, objDef);
		
		if ( refVal != null ) {
			alarm.setCounter(alarm.getCpu().getNamedCounter(refVal));
			
			alarm.addAttribDescription(COUNTER, getParamDescription(COUNTER, objDef));
		}

		// setup ACTION of the alarm when it expires
		enumVal = getEnumParam(ACTION, objDef);
		
		if ( enumVal != null ) {
			String name = enumVal;
			
			alarm.setAction(ActionKind.get(name));
			
			FeatureValue value=objDef.getNamedParameterValue(ACTION);
			
			alarm.addAttribDescription(ACTION, value.getDescription());
			
			assert value instanceof NameValue;
			
			// Now extract sub parameter values depending on the action specified
			if ( ActionKind.ACTIVATETASK_LITERAL.equals(ActionKind.get(name)) ) {
			
				// An ACTIVATETASK action, so get the name of the task that is activated
				String taskName = getRefSubParam(TASK, (NameValue)value);
				
				if ( taskName != null ) {
					// inform alarm that it activates the task
					alarm.setTask(alarm.getCpu().getNamedTask(taskName));
					
					alarm.addAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value));
				}
			}
			else if ( ActionKind.SETEVENT_LITERAL.equals(ActionKind.get(name)) ) {

				// A SETEVENT action, so get the name of the task that is activated
				String taskName = getRefSubParam(TASK, (NameValue)value);
				
				if ( taskName != null ) {
					// inform alarm that it activates the task
					alarm.setTask(alarm.getCpu().getNamedTask(taskName));
					alarm.addAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value));
				}
				
				// and get the name of the event that is set
				String eventName = getRefSubParam(EVENT, (NameValue)value);
				
				if ( taskName != null ) {
					// inform alarm that it sets the event
					alarm.setEvent(alarm.getCpu().getNamedEvent(eventName));
					alarm.addAttribDescription(EVENT, getSubParamDescription(EVENT, (NameValue)value));
				}			
			}
			else if ( ActionKind.ALARMCALLBACK_LITERAL.equals(ActionKind.get(name)) ) {
				// An ALARMCALLBACK action, so get callback function name

				String callbackName = getStringSubParam(ALARMCALLBACKNAME, (NameValue)value);
					
				if ( callbackName != null ) {
					// inform alarm that it calls the named function
					alarm.setAlarmCallbackName(callbackName);
					alarm.addAttribDescription(ALARMCALLBACKNAME, getSubParamDescription(ALARMCALLBACKNAME, (NameValue)value));
				}				
			}
			else if ( ActionKind.INCREMENTCOUNTER_LITERAL.equals(ActionKind.get(name)) ) {
				// An INCREMENTCOUNTER action, so get name of counter that is incremented
			
				/* $Req: AUTOSAR $ */
				// NOTE: this is part of the AUTOSAR standard only (not OSEK)	
			
				// setup COUNTER to which the alarm increments
				String counterName = getRefSubParam(COUNTER, (NameValue)value);
				
				if ( counterName != null ) {
					alarm.setIncrementedCounter(alarm.getCpu().getNamedCounter(counterName));
					alarm.addAttribDescription(INCREMENTCOUNTER, getSubParamDescription(COUNTER, (NameValue)value));
				}
			}			
		}
		
		// setup alarm AUTOSTART flag
		boolVal = getBooleanParam(AUTOSTART, objDef);
		
		if ( boolVal != null ) {	
					
			boolean autostarted=boolVal;
			
			alarm.setAutostart(autostarted);
			
			alarm.addAttribDescription(AUTOSTART, getParamDescription(AUTOSTART, objDef));
			
			if ( autostarted ) {
				// if autostarted alarm, then get sub-parameter values that specify alarmtime, cycletime and associated appmodes
				FeatureValue value=objDef.getNamedParameterValue(AUTOSTART);
				
				assert value instanceof BooleanValue;
				
				// get ALARMTIME sub Parameter
				Long alarmTime = getIntegerSubParam(ALARMTIME, (BooleanValue)value);

				if ( alarmTime != null ) {
					alarm.setAlarmTime(alarmTime);
					alarm.addAttribDescription(ALARMTIME, getSubParamDescription(ALARMTIME, (BooleanValue)value));
				}
		
				// get CYCLETIME sub Parameter
				Long cycleTime = getIntegerSubParam(CYCLETIME, (BooleanValue)value);

				if ( cycleTime != null ) {
					alarm.setCycleTime(cycleTime);
					alarm.addAttribDescription(CYCLETIME, getSubParamDescription(CYCLETIME, (BooleanValue)value));
				}
						
				
				// get APPMODE sub-parameters
				FeatureValue [] subValues=((BooleanValue)value).getNamedSubParameterValueList(APPMODE);
				
				int index = 0;
				for ( int i=0; i<subValues.length; i++) {
					
					FeatureValue subValue=subValues[i];
					
					if ( subValue instanceof NameValue ) {
						
						String appModeName = ((NameValue)subValue).getValue();
						
						alarm.addAppMode(alarm.getCpu().getNamedAppMode(appModeName));
						
						alarm.addMultiAttribDescription(APPMODE, subValue.getDescription(), index++);
					}
				}					
			}				
		}
		
		// setup a list of APPLICATIONs that can access the alarm, use common helper method
		extractAccessingApps(alarm, objDef);		/* $Req: AUTOSAR $ */
		
	}	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Resource.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Resource.
	 * @param resource the Resource into which to extract information
	 * @param objDef the ObjectDefinition that represents the Resource in the OIL model
	 */		
	private static void extractResource(Resource resource,ObjectDefinition objDef) {
			
		resource.setDescription(objDef.getDescription());	// setup the description
		
		// setup RESOURCEPROPERTY type
		String enumVal = getEnumParam(RESOURCEPROPERTY, objDef);
		
		if ( enumVal != null ) {
			
			String name = enumVal;
			
			resource.setResourceProperty(ResourceKind.get(name));
			
			resource.addAttribDescription(RESOURCEPROPERTY, getParamDescription(RESOURCEPROPERTY, objDef));
			
			// if a LINKED resource, then get sub-parameter value that specifies target resource of the link
			if ( ResourceKind.LINKED_LITERAL.equals(ResourceKind.get(name)) ) {
				
				FeatureValue value=objDef.getNamedParameterValue(RESOURCEPROPERTY);
				
				assert value instanceof NameValue;

				// get the name of the resource to which this one links				
				String resourceName = getRefSubParam(LINKEDRESOURCE, (NameValue)value);
								
				if ( resourceName != null ) {
			
					// inform resource that it links to the target resource
					resource.setLinkedResource(resource.getCpu().getNamedResource(resourceName));
					
					resource.addAttribDescription(LINKEDRESOURCE, getSubParamDescription(LINKEDRESOURCE, (NameValue)value));
				}
			}
		}
		
		// setup a list of APPLICATIONs that can access the resource, use common helper method
		extractAccessingApps(resource, objDef);		/* $Req: AUTOSAR $ */
		
	}	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Counter.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Counter.
	 * @param counter the Counter into which to extract information
	 * @param objDef the ObjectDefinition that represents the Counter in the OIL model
	 */		
	private static void extractCounter(Counter counter,ObjectDefinition objDef) {
	
		counter.setDescription(objDef.getDescription());	// setup the description
		
		// setup counter MAXALLOWEDVALUE
		Long longVal = getIntegerParam(MAXALLOWEDVALUE, objDef);
		
		if ( longVal != null ) {
			counter.setMaxAllowedValue(longVal);

			counter.addAttribDescription(MAXALLOWEDVALUE, getParamDescription(MAXALLOWEDVALUE, objDef));
		}

		// setup TICKSPERBASE value
		longVal = getIntegerParam(TICKSPERBASE, objDef);
		
		if ( longVal != null ) {

			counter.setTicksPerBase(longVal);
			
			counter.addAttribDescription(TICKSPERBASE, getParamDescription(TICKSPERBASE, objDef));
		}	

		// setup min cycle per base value
		longVal = getIntegerParam(MINCYCLE, objDef);
		
		if ( longVal != null ) {
			counter.setMinCycle(longVal);
			
			counter.addAttribDescription(MINCYCLE, getParamDescription(MINCYCLE, objDef));
		}
		
		// Setup the Counter TYPE
		String strType = getEnumParam(COUNTER_TYPE, objDef);   /* $Req: AUTOSAR $ */
		
		if ( strType != null ) {
			counter.setCounterType(CounterTypeKind.get(strType));
			
			counter.addAttribDescription(COUNTER_TYPE, getParamDescription(COUNTER_TYPE, objDef));
		}
		
		// Setup the Counter UNITs
		String strUnit = getEnumParam(COUNTER_UNIT, objDef);   	/* $Req: AUTOSAR $ */
		
		if ( strUnit != null ) {
			
			counter.setCounterUnit(CounterUnitKind.get(strUnit));
			
			counter.addAttribDescription(COUNTER_UNIT, getParamDescription(COUNTER_UNIT, objDef));
		}		
		

		// Setup the Counter Device Driver Name and Options
		
		String deviceOptions = getStringParam(COUNTER_DEVICE_OPTIONS, objDef);
		
		
		if ( deviceOptions != null ) {
			
			counter.setDeviceOptions(deviceOptions);
			
			//counter.setDeviceName(strDeviceName); TODO
			
			counter.addAttribDescription(COUNTER_DEVICE_OPTIONS, getParamDescription(COUNTER_DEVICE_OPTIONS, objDef));		
		}
		
		// setup a list of APPLICATIONs that can access the counter, use common helper method
		extractAccessingApps(counter, objDef);		/* $Req: AUTOSAR $ */
	}

	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model AppMode.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the AppMode.
	 * @param appMode the AppMode into which to extract information
	 * @param objDef the ObjectDefinition that represents the AppMode in the OIL model
	 */		
	private static void extractAppMode(AppMode appMode,ObjectDefinition objDef) {

		appMode.setDescription(objDef.getDescription());	// setup the description
		
		// no OIL parameters defined for AppMode
	}
	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Message.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Message.
	 * @param message the Message into which to extract information
	 * @param objDef the ObjectDefinition that represents the Message in the OIL model
	 */		
	private static void extractMessage(Message message,ObjectDefinition objDef) {

		message.setDescription(objDef.getDescription());	// setup the description
		
		// setup MESSAGEPROPERTY type
		String enumVal = getEnumParam(MESSAGEPROPERTY, objDef);
		
		if ( enumVal != null ) {		
			
			String name = enumVal;
			
			message.setMessageProperty(MessageKind.get(name));

			FeatureValue value=objDef.getNamedParameterValue(MESSAGEPROPERTY);
			
			message.addAttribDescription(MESSAGEPROPERTY, value.getDescription());
			
			assert value instanceof NameValue;
			
			if ( MessageKind.SEND_STATIC_INTERNAL_LITERAL.equals(MessageKind.get(name))) {
				
				// SEND_STATIC_INTERNAL type message, so get sub-parameter value that specifies the C datatype name
				
				String cDataType = getStringSubParam(CDATATYPE, (NameValue)value);
							
				if ( cDataType != null ) {
					
					message.setCDataType(cDataType);
					message.addAttribDescription(CDATATYPE, getSubParamDescription(CDATATYPE, (NameValue)value));
				}
			}
			else if ( MessageKind.RECEIVE_UNQUEUED_INTERNAL_LITERAL.equals(MessageKind.get(name))) {
				
				// RECEIVE_UNQUEUED_INTERNAL type message, so get sub-parameter values that specify the sending message and initial value
				
				String sendingMessage = getRefSubParam(SENDINGMESSAGE, (NameValue)value);
				
				if ( sendingMessage != null ) {
					
					message.setSendingMessage(message.getCpu().getNamedMessage(sendingMessage));
					message.addAttribDescription(SENDINGMESSAGE, getSubParamDescription(SENDINGMESSAGE, (NameValue)value));
				}
				
				BigInteger initialValue = getBigIntegerSubParam(INITIALVALUE, (NameValue)value);

				if ( initialValue != null ) {
					
					message.setInitialValue(initialValue);
					message.addAttribDescription(INITIALVALUE, getSubParamDescription(INITIALVALUE, (NameValue)value));
				}
				
				// Setup the Message COM Device Driver Name and Options
				String strDeviceName = getEnumSubParam(COM_DEVICE, (NameValue)value);   	/* $Req: EXTENSION $ */
				
				if ( strDeviceName != null ) {
					message.setDeviceName(strDeviceName);
					
					message.addAttribDescription(COM_DEVICE, getSubParamDescription(COM_DEVICE, (NameValue)value));
					
					// Read the OPTIONS sub-parameter. note: will not be able to access this if the device name was the default option
					// since the COM_DEVICE param value does not actually exist. Therefore the returned value will be null
					// if the default device name was returned, just need to guard against this here.
					FeatureValue subValue=((NameValue)value).getNamedSubParameterValue(COM_DEVICE);	/* $Req: EXTENSION $ */
					
					if ( subValue != null ) {
						assert value instanceof NameValue;
					
						message.setDeviceOptions(getStringSubParam(COM_DEVICE_OPTIONS, (NameValue)subValue));
						
						message.addAttribDescription(COM_DEVICE_OPTIONS, getSubParamDescription(COM_DEVICE_OPTIONS, (NameValue)subValue));
					}
				}				
			}
			else if ( MessageKind.RECEIVE_QUEUED_INTERNAL_LITERAL.equals(MessageKind.get(name))) {
				
				// RECEIVE_QUEUED_INTERNAL type message, so get sub-parameter values that specify the sending message and queue size
				
				String sendingMessage = getRefSubParam(SENDINGMESSAGE, (NameValue)value);
				
				if ( sendingMessage != null ) {
					
					message.setSendingMessage(message.getCpu().getNamedMessage(sendingMessage));
					message.addAttribDescription(SENDINGMESSAGE, getSubParamDescription(SENDINGMESSAGE, (NameValue)value));
				}
				
				Long queueSize = getIntegerSubParam(QUEUESIZE, (NameValue)value);
				
				if ( queueSize != null ) {
					
					message.setQueueSize(queueSize);
					message.addAttribDescription(QUEUESIZE, getSubParamDescription(QUEUESIZE, (NameValue)value));
				}	
			
				// Setup the Message COM Device Driver Name and Options
				String strDeviceName = getEnumSubParam(COM_DEVICE, (NameValue)value);   	/* $Req: EXTENSION $ */
				
				if ( strDeviceName != null ) {
					message.setDeviceName(strDeviceName);
					
					message.addAttribDescription(COM_DEVICE, getSubParamDescription(COM_DEVICE, (NameValue)value));
					
					// Read the OPTIONS sub-parameter. note: will not be able to access this if the device name was the default option
					// since the COM_DEVICE param value does not actually exist. Therefore the returned value will be null
					// if the default device name was returned, just need to guard against this here.
					FeatureValue subValue=((NameValue)value).getNamedSubParameterValue(COM_DEVICE);	/* $Req: EXTENSION $ */
					
					if ( subValue != null ) {
						assert value instanceof NameValue;
					
						message.setDeviceOptions(getStringSubParam(COM_DEVICE_OPTIONS, (NameValue)subValue));
						message.addAttribDescription(COM_DEVICE_OPTIONS, getSubParamDescription(COM_DEVICE_OPTIONS, (NameValue)subValue));
					}
				}				
			}	
			else if ( MessageKind.SEND_ZERO_INTERNAL_LITERAL.equals(MessageKind.get(name))) {
				
				// SEND_ZERO_INTERNAL_LITERAL type message, no sub-parameter values exist for this message type
			}
			else if ( MessageKind.RECEIVE_ZERO_INTERNAL_LITERAL.equals(MessageKind.get(name))) {
				
				// RECEIVE_ZERO_INTERNAL_LITERAL type message, so get sub-parameter value that specifies the sending message
				String sendingMessage = getRefSubParam(SENDINGMESSAGE, (NameValue)value);
				
				if ( sendingMessage != null ) {
					
					message.setSendingMessage(message.getCpu().getNamedMessage(sendingMessage));
					message.addAttribDescription(SENDINGMESSAGE, getSubParamDescription(SENDINGMESSAGE, (NameValue)value));
				}
				
				// Setup the Message COM Device Driver Name and Options
				String strDeviceName = getEnumSubParam(COM_DEVICE, (NameValue)value);   	/* $Req: EXTENSION $ */
				
				if ( strDeviceName != null ) {
					message.setDeviceName(strDeviceName);
					message.addAttribDescription(COM_DEVICE, getSubParamDescription(COM_DEVICE, (NameValue)value));
					// Read the OPTIONS sub-parameter. note: will not be able to access this if the device name was the default option
					// since the COM_DEVICE param value does not actually exist. Therefore the returned value will be null
					// if the default device name was returned, just need to guard against this here.
					FeatureValue subValue=((NameValue)value).getNamedSubParameterValue(COM_DEVICE);	/* $Req: EXTENSION $ */
					
					if ( subValue != null ) {
						assert value instanceof NameValue;
					
						message.setDeviceOptions(getStringSubParam(COM_DEVICE_OPTIONS, (NameValue)subValue));
						message.addAttribDescription(COM_DEVICE_OPTIONS, getSubParamDescription(COM_DEVICE_OPTIONS, (NameValue)subValue));
					}
				}				
			}
			else if ( MessageKind.SEND_STREAM_INTERNAL_LITERAL.equals(MessageKind.get(name))) {		/* $Req: EXTENSION $ */
				
				// SEND_STREAM_INTERNAL_LITERAL type message, no sub-parameter values exist for this message type
			}
			else if ( MessageKind.RECEIVE_STREAM_INTERNAL_LITERAL.equals(MessageKind.get(name))) {	/* $Req: EXTENSION $ */
				
				// RECEIVE_STREAM_INTERNAL type message, so get sub-parameter values that specify the sending message and buffer size
				
				String sendingMessage = getRefSubParam(SENDINGMESSAGE, (NameValue)value);
				
				if ( sendingMessage != null ) {
					
					message.setSendingMessage(message.getCpu().getNamedMessage(sendingMessage));
					message.addAttribDescription(SENDINGMESSAGE, getSubParamDescription(SENDINGMESSAGE, (NameValue)value));
				}
				
				Long bufferSize = getIntegerSubParam(BUFFERSIZE, (NameValue)value);
				
				if ( bufferSize != null ) {
					
					message.setBufferSize(bufferSize);
					message.addAttribDescription(BUFFERSIZE, getSubParamDescription(BUFFERSIZE, (NameValue)value));
				}	
	
				/////////////////////////////////////////////////////////
				Long highThreshold = getIntegerSubParam(HIGH_THRESHOLD, (NameValue)value);
				
				if ( highThreshold != null ) {
					
					message.setHighThreshold(highThreshold);
					message.addAttribDescription(HIGH_THRESHOLD, getSubParamDescription(HIGH_THRESHOLD, (NameValue)value));
				}	
		
				/////////////////////////////////////////////////////////
				Long lowThreshold = getIntegerSubParam(LOW_THRESHOLD, (NameValue)value);

				if ( lowThreshold != null ) {
					
					message.setLowThreshold(lowThreshold);
					message.addAttribDescription(LOW_THRESHOLD, getSubParamDescription(LOW_THRESHOLD, (NameValue)value));
				}	
							
				/////////////////////////////////////////////////////////
				// Setup the Message COM Device Driver Name and Options
				String strDeviceName = getEnumSubParam(COM_DEVICE, (NameValue)value);   	/* $Req: EXTENSION $ */
				
				if ( strDeviceName != null ) {
					message.setDeviceName(strDeviceName);
					
					message.addAttribDescription(COM_DEVICE, getSubParamDescription(COM_DEVICE, (NameValue)value));
					
					// Read the OPTIONS sub-parameter. note: will not be able to access this if the device name was the default option
					// since the COM_DEVICE param value does not actually exist. Therefore the returned value will be null
					// if the default device name was returned, just need to guard against this here.
					FeatureValue subValue=((NameValue)value).getNamedSubParameterValue(COM_DEVICE);	/* $Req: EXTENSION $ */
					
					if ( subValue != null ) {
						assert value instanceof NameValue;
					
						message.setDeviceOptions(getStringSubParam(COM_DEVICE_OPTIONS, (NameValue)subValue));
						
						message.addAttribDescription(COM_DEVICE_OPTIONS, getSubParamDescription(COM_DEVICE_OPTIONS, (NameValue)subValue));
					}
				}				
			}	
			
		}
		
		// setup NOTIFICATION type
		enumVal = getEnumParam(NOTIFICATION, objDef);
		
		if ( enumVal != null ) {		
			
			String name = enumVal;
			
			message.setNotification(NotificationKind.get(name));
			
			message.addAttribDescription(NOTIFICATION, getParamDescription(NOTIFICATION, objDef));
			
			// Read the NOTIFICATION sub-parameter. note: will not be able to access this if the notification name was the default option
			// since the NOTIFICATION param value does not actually exist. Therefore the returned value will be null
			// if the default notification name was returned, so need to guard against this here.
			FeatureValue value=objDef.getNamedParameterValue(NOTIFICATION);
			
			if ( value != null ) {
				
				assert value instanceof NameValue;
				
				if ( NotificationKind.ACTIVATETASK_LITERAL.equals(NotificationKind.get(name))) {
					
					// ACTIVATETASK type notification, so get sub-parameter value that specifies the task that performs notification 
					
					String taskName = getRefSubParam(TASK, (NameValue)value);	// get the name of the task that is activated
									
					if ( taskName != null ) {
						// inform message that it activates the task
						message.setNotificationTask(message.getCpu().getNamedTask(taskName));
						message.addAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value));
					}
				}
				else if ( NotificationKind.SETEVENT_LITERAL.equals(NotificationKind.get(name)) ) {
					// A SETEVENT type notification, so get sub-parameter value that specifies target task to activate and event to set
	
					String taskName = getRefSubParam(TASK, (NameValue)value);	// get the name of the task that is activated
					
					if ( taskName != null ) {
						// inform message that it activates the task
						message.setNotificationTask(message.getCpu().getNamedTask(taskName));
						message.addAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value));
					}
					
					String eventName = getRefSubParam(EVENT, (NameValue)value);	// get the name of the event that is to be set
					
					if ( eventName != null ) {			
						// inform message that it sets the event
						message.setNotificationEvent(message.getCpu().getNamedEvent(eventName));
						message.addAttribDescription(EVENT, getSubParamDescription(EVENT, (NameValue)value));
					}				
				}
				else if ( NotificationKind.COMCALLBACK_LITERAL.equals(NotificationKind.get(name)) ) {
					// An COMCALLBACK type notification, so get sub-parameter value that specifies callback function name
	
					String callbackName = getStringSubParam(CALLBACKROUTINENAME, (NameValue)value);	// get the name of callback function
					
					if ( callbackName != null ) {				
						// inform message that it calls the named function to perform notification
						message.setNotificationCallbackRoutineName(callbackName);
						message.addAttribDescription(CALLBACKROUTINENAME, getSubParamDescription(CALLBACKROUTINENAME, (NameValue)value));
					}
					
					// get list of messages that are sent/received by the callback routine
					FeatureValue [] subValues=((NameValue)value).getNamedSubParameterValueList(MESSAGE);
					
					int index = 0;
					
					for ( int i=0; i<subValues.length; i++) {
						
						FeatureValue subValue=subValues[i];
						
						if ( subValue instanceof NameValue ) {
							
							String messageName = ((NameValue)subValue).getValue();
							
							message.addHighCallbackMessage(message.getCpu().getNamedMessage(messageName));
							
							message.addMultiAttribDescription(CALLBACK_MESSAGE, subValue.getDescription(), index++);
						}
					}					
				}
				else if ( NotificationKind.FLAG_LITERAL.equals(NotificationKind.get(name)) ) {
					// A FLAG type notification, so get sub-parameter value that specifies name of flag to be set
	
					String flagName = getStringSubParam(FLAGNAME, (NameValue)value);	// get the name of flag
									
					if ( flagName != null ) {				
						// inform message of the flag name to set
						message.setNotificationFlagName(flagName);	
						
						message.addAttribDescription(FLAGNAME, getSubParamDescription(FLAGNAME, (NameValue)value));
					}	
				}
			}
		}

		// setup LOW_NOTIFICATION type (for streams)
		enumVal = getEnumParam(LOW_NOTIFICATION, objDef);
		
		if ( enumVal != null ) {		
			
			String name = enumVal;
			
			message.setLowNotification(NotificationKind.get(name));
			
			message.addAttribDescription(LOW_NOTIFICATION, getParamDescription(NOTIFICATION, objDef));
			
			// Read the LOW_NOTIFICATION sub-parameter. note: will not be able to access this if the notification name was the default option
			// since the LOW_NOTIFICATION param value does not actually exist. Therefore the returned value will be null
			// if the default notification name was returned, so need to guard against this here.
			FeatureValue value=objDef.getNamedParameterValue(LOW_NOTIFICATION);
			
			if ( value != null ) {
				
				assert value instanceof NameValue;
				
				if ( NotificationKind.ACTIVATETASK_LITERAL.equals(NotificationKind.get(name))) {
					
					// ACTIVATETASK type notification, so get sub-parameter value that specifies the task that performs notification 
					
					String taskName = getRefSubParam(TASK, (NameValue)value);	// get the name of the task that is activated
									
					if ( taskName != null ) {
						// inform message that it activates the task
						message.setLowNotificationTask(message.getCpu().getNamedTask(taskName));
						
						message.addAttribDescription(LOW_TASK, getSubParamDescription(TASK, (NameValue)value));
					}
				}
				else if ( NotificationKind.SETEVENT_LITERAL.equals(NotificationKind.get(name)) ) {
					// A SETEVENT type notification, so get sub-parameter value that specifies target task to activate and event to set
	
					String taskName = getRefSubParam(TASK, (NameValue)value);	// get the name of the task that is activated
					
					if ( taskName != null ) {
						// inform message that it activates the task
						message.setLowNotificationTask(message.getCpu().getNamedTask(taskName));
						
						message.addAttribDescription(LOW_TASK, getSubParamDescription(TASK, (NameValue)value));
					}
					
					String eventName = getRefSubParam(EVENT, (NameValue)value);	// get the name of the event that is to be set
					
					if ( eventName != null ) {			
						// inform message that it sets the event
						message.setLowNotificationEvent(message.getCpu().getNamedEvent(eventName));
						
						message.addAttribDescription(LOW_EVENT, getSubParamDescription(EVENT, (NameValue)value));
					}				
				}
				else if ( NotificationKind.COMCALLBACK_LITERAL.equals(NotificationKind.get(name)) ) {
					// An COMCALLBACK type notification, so get sub-parameter value that specifies callback function name
	
					String callbackName = getStringSubParam(CALLBACKROUTINENAME, (NameValue)value);	// get the name of callback function
					
					if ( callbackName != null ) {				
						// inform message that it calls the named function to perform notification
						message.setLowNotificationCallbackRoutineName(callbackName);
						
						message.addAttribDescription(LOW_CALLBACKROUTINENAME, getSubParamDescription(CALLBACKROUTINENAME, (NameValue)value));
					}
					
					// get list of messages that are sent/received by the callback routine
					FeatureValue [] subValues=((NameValue)value).getNamedSubParameterValueList(MESSAGE);
					
					int index = 0;
					for ( int i=0; i<subValues.length; i++) {
						
						FeatureValue subValue=subValues[i];
						
						if ( subValue instanceof NameValue ) {
							
							String messageName = ((NameValue)subValue).getValue();
							
							message.addLowCallbackMessage(message.getCpu().getNamedMessage(messageName));
							
							message.addMultiAttribDescription(LOW_CALLBACK_MESSAGE, subValue.getDescription(), index++);
						}
					}					
				}
				else if ( NotificationKind.FLAG_LITERAL.equals(NotificationKind.get(name)) ) {
					// A FLAG type notification, so get sub-parameter value that specifies name of flag to be set
	
					String flagName = getStringSubParam(FLAGNAME, (NameValue)value);	// get the name of flag
									
					if ( flagName != null ) {				
						// inform message of the flag name to set
						message.setLowNotificationFlagName(flagName);
						
						message.addAttribDescription(LOW_FLAGNAME, getSubParamDescription(FLAGNAME, (NameValue)value));
					}	
				}
			}
		}
		
		// setup a list of APPLICATIONs that can access the message, use common helper method
		extractAccessingApps(message, objDef);		/* $Req: AUTOSAR $ */
	}	
	

	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model Application.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the Application.
	 * 
	 * $Req: AUTOSAR $
	 * 
	 * @param application the Application into which to extract information
	 * @param objDef the ObjectDefinition that represents the Application in the OIL model
	 */		
	private static void extractApplication(Application application,ObjectDefinition objDef) {

		Boolean boolVal;
		Object [] valueList;
				
		application.setDescription(objDef.getDescription());	// setup the description
		
		// setup TRUSTED flag
		boolVal = getBooleanParam(TRUSTED, objDef);
		
		if ( boolVal != null ) {	
					
			boolean trusted=boolVal;
			
			application.setTrusted(trusted);
			
			application.addAttribDescription(TRUSTED,getParamDescription(TRUSTED, objDef));
			
			if ( trusted ) {
				// if trusted application, then get sub-parameter values that specify trusted function names
				FeatureValue value=objDef.getNamedParameterValue(TRUSTED);
				
				assert value instanceof BooleanValue;
							
				// get TRUSTED_FUNCTION sub-parameters
				FeatureValue [] subValues=((BooleanValue)value).getNamedSubParameterValueList(TRUSTED_FUNCTION);
				
				int index = 0;
				// iterate over each trusted function sub-parameter boolean entry
				for ( int i=0; i<subValues.length; i++) {
					
					FeatureValue subValue=subValues[i];
					
					if ( subValue instanceof BooleanValue ) {
					
						if ( ((BooleanValue)subValue).getValue() == true ) {
						
							application.addMultiAttribDescription(TRUSTED_FUNCTION, subValue.getDescription(), index);
							
							// trusted function name available as a sub-parameter
							
							String functionName = getStringSubParam(NAME, (BooleanValue)subValue);	// get the name of trusted function
									
							if ( functionName != null ) {
								// inform application of the trusted function name to add
								application.addTrustedFunction(functionName);
								
								application.addMultiAttribDescription(NAME, getSubParamDescription(NAME, (BooleanValue)subValue), index);
							}
							index++;
						}						
						
					}
				}
			}
		}
		
		// setup Application STARTUPHOOK flag
		boolVal = getBooleanParam(STARTUPHOOK, objDef);
		
		if ( boolVal != null ) {
			application.setStartupHook(boolVal);
			application.addAttribDescription(STARTUPHOOK,getParamDescription(STARTUPHOOK, objDef));
		}		

		// setup Application ERRORHOOK flag
		boolVal = getBooleanParam(ERRORHOOK, objDef);
		
		if ( boolVal != null ) {
			application.setErrorHook(boolVal);
			application.addAttribDescription(ERRORHOOK,getParamDescription(ERRORHOOK, objDef));
		}		
		
		// setup Application SHUTDOWNHOOK flag
		boolVal = getBooleanParam(SHUTDOWNHOOK, objDef);
		
		if ( boolVal != null ) {
			application.setShutdownHook(boolVal);
			application.addAttribDescription(SHUTDOWNHOOK,getParamDescription(SHUTDOWNHOOK, objDef));
		}		
		
		// check HAS_RESTARTTASK flag
		boolVal = getBooleanParam(HAS_RESTARTTASK, objDef);
		
		if ( boolVal != null ) {	
					
			boolean hasRestartTask = boolVal;
			
			application.addAttribDescription(HAS_RESTARTTASK,getParamDescription(HAS_RESTARTTASK, objDef));
			
			if ( hasRestartTask ) {
				// has a restart task, so get sub-parameter value that specifies name of task to restart
				
				FeatureValue value=objDef.getNamedParameterValue(HAS_RESTARTTASK);	// get FeatureValue of flag so sub-parameter can be accessed
				
				assert value instanceof BooleanValue;
				
				String taskName = getRefSubParam(RESTARTTASK, (BooleanValue)value);	// get the name of the task that is restarted
								
				if ( taskName != null ) {
					// inform application that it restarts the task
					application.setRestartedTask(application.getCpu().getNamedTask(taskName));
					
					application.addAttribDescription(RESTARTTASK,getSubParamDescription(RESTARTTASK, (BooleanValue)value));
				}				
			}
		}
	

		// setup list of TASKs that are assigned to the application
		
		valueList=objDef.getNamedParameterValueList(TASK);
			
		int index = 0;
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedTask(name));
				
				application.addMultiAttribDescription(TASK, value.getDescription(), index++);
			}
		}
		
		// setup list of ISRs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(ISR);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedIsr(name));
				
				application.addMultiAttribDescription(ISR, value.getDescription(), index++);
			}
		}		
		
		// setup list of ALARMs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(ALARM);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedAlarm(name));
				
				application.addMultiAttribDescription(ALARM, value.getDescription(), index++);
			}
		}		
		
		// setup list of SCHEDULTABLEs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(SCHEDULETABLE);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedScheduleTable(name));
				
				application.addMultiAttribDescription(SCHEDULETABLE, value.getDescription(), index++);
			}
		}		
		
		// setup list of  COUNTERs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(COUNTER);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedCounter(name));
				
				application.addMultiAttribDescription(COUNTER, value.getDescription(), index++);
			}
		}		

		// setup list of RESOURCEs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(RESOURCE);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedResource(name));
				
				application.addMultiAttribDescription(RESOURCE, value.getDescription(), index++);
			}
		}		
		
		// setup list of MESSAGEs that are assigned to the application
		valueList=objDef.getNamedParameterValueList(MESSAGE);
		
		index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
				
				String name = ((NameValue)value).getValue();
				
				application.addAssignedElement(application.getCpu().getNamedMessage(name));
				
				application.addMultiAttribDescription(MESSAGE, value.getDescription(), index++);
			}
		}	
	}	
	
	/**
	 * Extracts information from an OIL ObjectDefinition into an OS Model ScheduleTable.
	 * As much information as possible is extracted, if values do not exist
	 * or are of the wrong type then they are not set within the ScheduleTable.
	 * @param table the ScheduleTable into which to extract information
	 * @param objDef the ObjectDefinition that represents the Application in the OIL model
	 */		
	private static void extractScheduleTable(ScheduleTable table,ObjectDefinition objDef) {

		table.setDescription(objDef.getDescription());	// setup the description
			
		// setup COUNTER to which the schedule table is assigned
		String refVal = getRefParam(COUNTER, objDef);
		
		if ( refVal != null ) {
			table.setCounter(table.getCpu().getNamedCounter(refVal));
		}		
		
		// setup schedule table AUTOSTART flag
		Boolean boolVal = getBooleanParam(AUTOSTART, objDef);
		
		if ( boolVal != null ) {
			
			boolean autostarted = boolVal;
			
			table.setAutostart(autostarted);
			
			table.addAttribDescription(AUTOSTART,getParamDescription(AUTOSTART, objDef));
			
			if ( autostarted ) {
				// if autostarted schedule table, then get sub-parameter values that specify offset and associated appmodes
				
				FeatureValue value = objDef.getNamedParameterValue(AUTOSTART);
				
				assert value instanceof BooleanValue;
				
				FeatureValue [] subValues=((BooleanValue)value).getNamedSubParameterValueList(APPMODE);
				
				int index = 0;
				
				for ( int i=0; i<subValues.length; i++) {
					
					FeatureValue subValue=subValues[i];
					
					if ( subValue instanceof NameValue ) {
						
						String appModeName = ((NameValue)subValue).getValue();
						
						table.addAppMode(table.getCpu().getNamedAppMode(appModeName));
						
						table.addMultiAttribDescription(APPMODE, subValue.getDescription(), index++);
					}
				}
				
				// get autostart offset value
				BigInteger offset = getBigIntegerSubParam(OFFSET, (BooleanValue)value);

				if ( offset != null ) {
					
					table.setAutostartOffset(offset);
					table.addAttribDescription(OFFSET,getSubParamDescription(OFFSET, (BooleanValue)value));
				}
			}				
		}
		
		// setup schedule table LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION flag
		boolVal = getBooleanParam(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION, objDef);
		
		if ( boolVal != null ) {		
		
			boolean localToGlobal = boolVal;
			
			table.setLocalToGlobalTimeSync(localToGlobal);
			
			table.addAttribDescription(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION,getParamDescription(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION, objDef));
			
			if (localToGlobal) {
				// if true then need to extract the sub-parameters
				FeatureValue value = objDef.getNamedParameterValue(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION);
				
				assert value instanceof BooleanValue;
				
				// get the SYNC_STRATEGY value
				String syncStrategyName = getEnumSubParam(SYNC_STRATEGY, (BooleanValue)value);
				
				if (syncStrategyName != null) {
					table.setSyncStrategy(SyncStrategyKind.get(syncStrategyName));
					table.addAttribDescription(SYNC_STRATEGY,getSubParamDescription(SYNC_STRATEGY, (BooleanValue)value));
				}

				//	get max_increase value
				BigInteger subValue = getBigIntegerSubParam(MAX_INCREASE, (BooleanValue)value);
				
				if ( subValue != null ) {
					table.setMaxIncrease(subValue);
					table.addAttribDescription(MAX_INCREASE,getSubParamDescription(MAX_INCREASE, (BooleanValue)value));
				}
				
				//	get max_decrease value
				subValue = getBigIntegerSubParam(MAX_DECREASE, (BooleanValue)value);
				
				if ( subValue != null ) {
					table.setMaxDecrease(subValue);
					table.addAttribDescription(MAX_DECREASE,getSubParamDescription(MAX_DECREASE, (BooleanValue)value));
				}
				
				//	get max_increase_async value
				subValue = getBigIntegerSubParam(MAX_INCREASE_ASYNC, (BooleanValue)value);
				
				if ( subValue != null ) {
					table.setMaxIncreaseAsync(subValue);
					table.addAttribDescription(MAX_INCREASE_ASYNC,getSubParamDescription(MAX_INCREASE_ASYNC, (BooleanValue)value));
				}
				
				//	get max_decrease_async value
				subValue = getBigIntegerSubParam(MAX_DECREASE_ASYNC, (BooleanValue)value);
				
				if ( subValue != null ) {
					table.setMaxDecreaseAsync(subValue);
					table.addAttribDescription(MAX_DECREASE_ASYNC,getSubParamDescription(MAX_DECREASE_ASYNC, (BooleanValue)value));
				}					
				
				//	get Precision value
				subValue = getBigIntegerSubParam(PRECISION, (BooleanValue)value);
				
				if ( subValue != null ) {
					table.setPrecision(subValue);
					table.addAttribDescription(PRECISION,getSubParamDescription(PRECISION, (BooleanValue)value));					
				}					
			}
		}
		
		// setup schedule table PERIODIC flag
		boolVal = getBooleanParam(PERIODIC, objDef);		
		
		if ( boolVal != null ) {
			table.setPeriodic(boolVal);
			table.addAttribDescription(PERIODIC,getParamDescription(PERIODIC, objDef));
		}
		
		// setup schedule table LENGTH flag
		BigInteger length = getBigIntegerParam(LENGTH, objDef);		
		
		if ( length != null ) {	
			table.setLength(length);
			table.addAttribDescription(LENGTH,getParamDescription(LENGTH, objDef));
		}		
		
		// setup the schedule table ACTION list
		Object [] valueList=objDef.getNamedParameterValueList(ST_ACTION);
		
		int index = 0;
		
		for ( int i=0; i<valueList.length; i++) {
			
			FeatureValue value=(FeatureValue)valueList[i];
			
			if ( value instanceof NameValue ) {
			
				// map action name to a ScheduleTableActionKind instance
				ScheduleTableActionKind actionKind = ScheduleTableActionKind.get(((NameValue)value).getValue());
				
				// Create a ScheduleTableAction instance within the table, using the action kind instance
				ScheduleTableAction action = table.createAction(actionKind);
				
				table.addMultiAttribDescription(ST_ACTION,value.getDescription(), index);
				
				// Now extract sub parameter values depending on the action specified
				if ( ScheduleTableActionKind.ACTIVATETASK_LITERAL.equals(actionKind) ) {
				
					// An ACTIVATETASK action, so get the name of the task that is activated
					String taskName = getRefSubParam(TASK, (NameValue)value);
					
					if ( taskName != null ) {
						// inform ScheduleTableAction that it activates the task
						action.setTask(table.getCpu().getNamedTask(taskName));
						table.addMultiAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value), index);
					}
				}
				else if ( ScheduleTableActionKind.SETEVENT_LITERAL.equals(actionKind) ) {

					// A SETEVENT action, so get the name of the task that is activated
					String taskName = getRefSubParam(TASK, (NameValue)value);
					
					if ( taskName != null ) {
						// inform ScheduleTableAction that it activates the task
						action.setTask(table.getCpu().getNamedTask(taskName));
						table.addMultiAttribDescription(TASK, getSubParamDescription(TASK, (NameValue)value), index);
					}
					
					// and get the name of the event that is set
					String eventName = getRefSubParam(EVENT, (NameValue)value);
					
					if ( taskName != null ) {
						// inform ScheduleTableAction that it sets the event
						action.setEvent(table.getCpu().getNamedEvent(eventName));
						table.addMultiAttribDescription(EVENT, getSubParamDescription(EVENT, (NameValue)value), index);
					}			
				}
				else if ( ScheduleTableActionKind.ACTIONCALLBACK_LITERAL.equals(actionKind) ) {
					// An ACTIONCALLBACK action, so get callback function name

					//	NOTE: this is not part of the OSEK/AUTOSAR standard, but an extension
					/* $Req: EXTENSION $ */
					
					String callbackName = getStringSubParam(ACTIONCALLBACKNAME, (NameValue)value);
						
					if ( callbackName != null ) {
						// inform action that it calls the named function
						action.setActionCallbackName(callbackName);
						table.addMultiAttribDescription(ACTIONCALLBACKNAME, getSubParamDescription(ACTIONCALLBACKNAME, (NameValue)value), index);						
					}				
				}				
				else if ( ScheduleTableActionKind.INCREMENTCOUNTER_LITERAL.equals(actionKind) ) {
					// An INCREMENTCOUNTER, so get name of counter that is incremented
					
					// NOTE: this is not part of the OSEK/AUTOSAR standard, but an extension
					/* $Req: EXTENSION $ */
					
					// setup COUNTER which the action increments
					String counterName = getRefSubParam(COUNTER, (NameValue)value);
					
					if ( counterName != null ) {
						action.setIncrementedCounter(table.getCpu().getNamedCounter(counterName));
						table.addMultiAttribDescription(INCREMENTCOUNTER, getSubParamDescription(COUNTER, (NameValue)value), index);						
					}
				}

				// get the action offset value (all action kinds specify this)
				BigInteger offset = getBigIntegerSubParam(OFFSET, (NameValue)value);

				if ( offset != null ) {
					action.setOffset(offset);
					table.addMultiAttribDescription(ACTIONOFFSET, getSubParamDescription(OFFSET, (NameValue)value), index);
				}
				
				index++;
			}
		}
		
		// setup a list of APPLICATIONs that can access the ScheduleTable, use common helper method
		extractAccessingApps(table, objDef);		/* $Req: AUTOSAR $ */
	}
	
	
	/**
	 * Populates the given OS model with the objects defined within the given OIL model.
	 * 
	 * The given OS Model should be empty when this method is called.
	 *  
	 * The process extracts as much information as possible from the given OIL model.
	 * If parameter values do not exist as expected, or are out of range then the default
	 * values that already exist within the OS instances are left unchanged.
	 * 
	 * i.e. The extraction is designed to be silent when faced with missing or invalid
	 * parameter values, hence an OIL model that is not semantically correct may still
	 * be used to populate an OS Model.
	 * 
	 * @param osModel the root Cpu element of the OS Model to be populated
	 * @param oilModel the OIL model from which to populate the OS model
	 */
	private static void extractOILModel(Cpu osModel,OILDefinition oilModel) {
		
		/* This method performs a two pass process. First all the objects are created 
		 * in the destination OS Model. Then the parameters for each of these is extracted.
		 * This approach is taken to allow x-references between named objects.
		 */
		
		// Get the ApplicationDefinition instance within the OILDefinition model
		ApplicationDefinition appDef=oilModel.getApplicationDef();
		
		if ( appDef!=null ) {
			
			// Use the ApplicationDefinition to iterate over each ObjectDefinition
			
			osModel.setName(appDef.getName());
			osModel.setDescription(appDef.getDescription());
			
			// For each ObjectDefinition add a new associated instance to the OS model
			// i.e. populate the Cpu with the OS objects
			
			for (ObjectDefinition next : appDef.getObjectDefinitions() ) {
				
				switch ( next.getObjectType().getValue() ) {
				
					case ObjectKind.OS :
						osModel.createOs(next.getName());
						break;	
						
					case ObjectKind.COM :
						osModel.createCom(next.getName());
						break;
						
					case ObjectKind.NM :
						osModel.createNm(next.getName());
						break;					
				
					case ObjectKind.TASK :
						osModel.createTask(next.getName());
						break;

					case ObjectKind.ISR :
						osModel.createIsr(next.getName());
						break;						
						
					case ObjectKind.EVENT :
						osModel.createEvent(next.getName());
						break;

					case ObjectKind.ALARM :
						osModel.createAlarm(next.getName());
						break;						
						
					case ObjectKind.RESOURCE :
						osModel.createResource(next.getName());
						break;						

					case ObjectKind.COUNTER :
						osModel.createCounter(next.getName());
						break;							
						
					case ObjectKind.APPMODE :
						osModel.createAppMode(next.getName());
						break;
						
					case ObjectKind.MESSAGE :
						osModel.createMessage(next.getName());
						break;	
						
					case ObjectKind.APPLICATION :
						osModel.createApplication(next.getName());
						break;	
						
					case ObjectKind.SCHEDULETABLE :
						osModel.createScheduleTable(next.getName());
						break;						
				}
			}
			
			// For each ObjectDefinition setup the associated OS instance using its parameter values
			// i.e. setup the attribute values and references of the OS objects
			
			for (ObjectDefinition next : appDef.getObjectDefinitions() ) {
								
				switch ( next.getObjectType().getValue() ) {
				
					case ObjectKind.OS :
						
						Os os = osModel.getNamedOs(next.getName());	// get the Os object
						
						if ( os!=null )	extractOs(os,next);
							
						break;	
						
					case ObjectKind.COM :
						
						Com com = osModel.getNamedCom(next.getName());	// get the Com object
						
						if ( com!=null ) extractCom(com,next);
						
						break;
						
					case ObjectKind.NM :
						
						Nm nm = osModel.getNamedNm(next.getName());	// get the Nm object
						
						if ( nm!=null )	extractNm(nm,next);
						
						break;					
				
					case ObjectKind.TASK :
						
						Task task = osModel.getNamedTask(next.getName()); // get the Task object
						
						if ( task!=null ) extractTask(task,next);
							
						break;

					case ObjectKind.ISR :
						
						Isr isr = osModel.getNamedIsr(next.getName()); // get the Isr object
						
						if ( isr!=null ) extractIsr(isr,next);

						break;						
						
					case ObjectKind.EVENT :
						
						Event event = osModel.getNamedEvent(next.getName()); // get the Event object
						
						if ( event!=null ) extractEvent(event,next);

						break;

					case ObjectKind.ALARM :
						
						Alarm alarm = osModel.getNamedAlarm(next.getName()); // get the Alarm object
						
						if ( alarm!=null ) extractAlarm(alarm,next);

						break;						
						
					case ObjectKind.RESOURCE :
						
						Resource resource = osModel.getNamedResource(next.getName()); // get the Resource object
						
						if ( resource!=null ) extractResource(resource,next);

						break;						

					case ObjectKind.COUNTER :
						
						Counter counter = osModel.getNamedCounter(next.getName()); // get the Counter object
						
						if ( counter!=null ) extractCounter(counter,next);

						break;							
						
					case ObjectKind.APPMODE :
						
						AppMode appMode = osModel.getNamedAppMode(next.getName()); // get the AppMode object
						
						if ( appMode!=null ) extractAppMode(appMode,next);

						break;
						
					case ObjectKind.MESSAGE :
						
						Message message= osModel.getNamedMessage(next.getName()); // get the Message object
						
						if ( message!=null ) extractMessage(message,next);

						break;	
						
					case ObjectKind.APPLICATION :
						
						Application application= osModel.getNamedApplication(next.getName()); // get the Application object
						
						if ( application!=null ) extractApplication(application,next);

						break;	
						
					case ObjectKind.SCHEDULETABLE :
						
						ScheduleTable table= osModel.getNamedScheduleTable(next.getName()); // get the ScheduleTable object
						
						if ( table!=null ) extractScheduleTable(table,next);

						break;						
				}
			}
		}		
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OIL export (generator) helper methods.
	
	/**
	 * Initialise the tab count used during export
	 * 
	 * @see #generateOILTabs()
	 */
	private void initTabs() {
		tabs = 0;
	}
	
	/**
	 * Increment the tab count used during export
	 * 
	 * @see #generateOILTabs()
	 */	
	private void incTabs() {
		tabs++;
	}	
	
	/**
	 * Decrement the tab count used during export
	 * 
	 * @see #generateOILTabs()
	 */	
	private void decTabs() {
		tabs--;
	}	
	
	/**
	 * Generate tabs to the writer
	 * 
	 * @see #initTabs()
	 * @see #incTabs()
	 * @see #decTabs()
	 */
	private void generateOILTabs() {
		for ( int i = 0 ; i < tabs; i++ ) {
			writer.print("\t");
		}		
	}
	
	/**
	 * Generates an OIL format name  for the given {@link OSModelElement}
	 * 
	 * The output is in the form <objectName> <name>
	 * 
	 * @param objectName the name of the OIL object
	 * @param element the {@link OSModelElement} for which the name is required
	 */
	private void generateOILName(String objectName, OSModelElement element) {
		
		generateOILTabs();

		writer.print(objectName+" ");
		writer.print(element.getName());
	}
	
	/**
	 * Generates an OIL format object end inc. description for the given {@link OSModelElement}
	 * 
	 * The output is in the form } [:"<description>"];<nl>
	 * 
	 * @param element the {@link OSModelElement} for which the description is required
	 */
	private void generateOILObjEnd(OSModelElement element) {
		
		generateOILTabs();
		
		writer.print("}");
		
		String description = element.getDescription();
		
		if ( description != null ) {
			writer.print(":\""+description+"\"");
		}
		writer.println(";");
		writer.println();
	}	
	
	/**
	 * Generates an OIL format attribute end inc. description if given
	 * @param attribDescription the attribute description, may be null
	 */
	private void generateOILAttribEnd(String attribDescription) {
		generateOILTabs();
		
		writer.print("}");
		
		if ( attribDescription != null ) {
			writer.print(":\""+attribDescription+"\"");
		}
		writer.println(";");		
	}
	
	
	/**
	 * Generates an OIL attribute value as either TRUE or FALSE depending on the given flag
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param flag the flag that indicates whether to output TRUE or FALSE
	 * @param attribDescription the description of the attribute, if null nothing output
	 */
	private void generateOILBooleanAttrib(String attribName, boolean flag, String attribDescription) {
		
		generateOILTabs();
		
		writer.print(attribName+" = ");
		if ( flag ) {
			writer.print("TRUE");
		}
		else {
			writer.print("FALSE");
		}
		
		if ( attribDescription != null ) {
			writer.print(":\""+attribDescription+"\"");
		}
		
		writer.println(";");
	}
	
	/**
	 * Generates an OIL attribute value in the format <attribName> = "<value>" [:<description>];
	 * 
	 * If the value is null, then "" is output as the value.
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param value the string value to be output
	 * @param attribDescription the description of the attribute, if null nothing output
	 */
	private void generateOILStringAttrib(String attribName, String value, String attribDescription) {
		
		if ( value != null ) {
			generateOILAttrib(attribName, "\""+value+"\"", attribDescription);
		}
		else {
			generateOILAttrib(attribName, "\"\"", attribDescription);
		}
	}		
	
	/**
	 * Generates an OIL attribute value in the format <attribName> = <value> [:<description>];
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param value the value to be output
	 * @param attribDescription the description of the attribute, if null nothing output
	 */
	private void generateOILAttrib(String attribName, String value, String attribDescription) {
		
		generateOILTabs();
				
		writer.print(attribName+" = ");
		writer.print(value);
		
		if ( attribDescription != null ) {
			writer.print(":\""+attribDescription+"\"");
		}
		
		writer.println(";");
	}
	
	/**
	 * Generates an OIL attribute value in the format <attribName> = <OSModelElement.getName()> [:<description>];
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param modelElement the {@link OSModelElement} from which to extract the name value
	 * @param attribDescription the description of the attribute, if null nothing output
	 */
	private void generateOILAttrib(String attribName, OSModelElement modelElement, String attribDescription) {
		
		if ( modelElement != null ) {
			generateOILAttrib(attribName, modelElement.getName(), attribDescription);
		}
	}	
		
	/**
	 * Generates an OIL attribute value in the format <attribName> = <value>
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param value the value to be output
	 */
	private void generateOILAttribNoTerm(String attribName, String value) {
		
		generateOILTabs();
				
		writer.print(attribName+" = ");
		writer.print(value);
	}	
	
	
	/**
	 * Generates an OIL attribute value in the format <attribName> = <value>;
	 * 
	 * But <value> is replaced with "AUTO" if the given autoFlag is true.
	 * 
	 * @param attribName the name of the OIL attribute to output
	 * @param value the value to be output
	 * @param autoFlag the flag that indicates whether the attribute should be output as "AUTO"
	 * @param attribDescription the description of the attribute, if null nothing output
	 */
	private void generateOILAutoAttrib(String attribName, String value, boolean autoFlag, String attribDescription) {
		
		generateOILTabs();		
		
		writer.print(attribName+" = ");
		if ( autoFlag ) {
			writer.print("AUTO");
		}
		else {
			writer.print(value);
		}
		
		if ( attribDescription != null ) {
			writer.print(":\""+attribDescription+"\"");
		}		
		
		writer.println(";");
	}	
	
	/**
	 * Generates OIL format output from the given OS Model {@link Os}.
	 * 
	 * @param os
	 */
	private void generateOsOIL(Os os) {
	
		generateOILName("OS", os);
		writer.println(" {");
		incTabs();
		
		generateOILAttrib(STATUS, os.getStatus().toString(), os.getAttribDescription(STATUS));
		generateOILBooleanAttrib(STARTUPHOOK, os.getStartupHook(), os.getAttribDescription(STARTUPHOOK));
		generateOILBooleanAttrib(ERRORHOOK, os.getErrorHook(), os.getAttribDescription(ERRORHOOK));
		generateOILBooleanAttrib(SHUTDOWNHOOK, os.getShutdownHook(), os.getAttribDescription(SHUTDOWNHOOK));
		generateOILBooleanAttrib(PRETASKHOOK, os.getPreTaskHook(), os.getAttribDescription(PRETASKHOOK));
		generateOILBooleanAttrib(POSTTASKHOOK, os.getPostTaskHook(), os.getAttribDescription(POSTTASKHOOK));
		generateOILBooleanAttrib(USEGETSERVICEID, os.getUseGetServiceId(), os.getAttribDescription(USEGETSERVICEID));
		generateOILBooleanAttrib(USEPARAMETERACCESS, os.getUseParameterAccess(), os.getAttribDescription(USEPARAMETERACCESS));
		generateOILBooleanAttrib(USERESSCHEDULER, os.getUseResScheduler(), os.getAttribDescription(USERESSCHEDULER));
		generateOILBooleanAttrib(STACKMONITORING, os.isStackCheckingEnabled(), os.getAttribDescription(STACKMONITORING));
		generateOILBooleanAttrib(PROTECTIONHOOK, os.hasProtectionHook(), os.getAttribDescription(PROTECTIONHOOK));
		generateOILAutoAttrib(SCALABILITYCLASS, os.getScalabilityClass().toString(), os.getAutoScalabilityClass(), os.getAttribDescription(PROTECTIONHOOK));			
		generateOILAutoAttrib(PRETASKHOOK_STACKSIZE, Long.toString(os.getPreTaskHookStackSize()), os.isAutoPreTaskHookStackSize(), os.getAttribDescription(PRETASKHOOK_STACKSIZE));
		generateOILAutoAttrib(POSTTASKHOOK_STACKSIZE, Long.toString(os.getPostTaskHookStackSize()), os.isAutoPostTaskHookStackSize(), os.getAttribDescription(POSTTASKHOOK_STACKSIZE));
		generateOILBooleanAttrib(RESTARTABLE, os.isRestartable(), os.getAttribDescription(RESTARTABLE));
		generateOILBooleanAttrib(HANDLE_OSCFAILURE, os.isOscFailureHandled(), os.getAttribDescription(HANDLE_OSCFAILURE));
		generateOILBooleanAttrib(HANDLE_ADDRERROR, os.isAddrErrorHandled(), os.getAttribDescription(HANDLE_ADDRERROR));
		generateOILBooleanAttrib(HANDLE_MATHERROR, os.isMathErrorHandled(), os.getAttribDescription(HANDLE_MATHERROR));			
	
		decTabs();
		generateOILObjEnd(os);		
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Application}.
	 * 
	 * @param application
	 */
	private void generateApplicationOIL(Application application) {
		
		generateOILName("APPLICATION", application);
		writer.println(" {");
		incTabs();
		
		if( application.isTrusted() ) {
			generateOILAttribNoTerm(TRUSTED, "TRUE");

			incTabs();
			writer.println(" {");			
			int index = 0;
			for ( String trustedFunction : application.getTrustedFunctions() ) {
			
				generateOILAttribNoTerm(TRUSTED_FUNCTION , "TRUE");
				incTabs();
				writer.println(" {");					
				
				generateOILStringAttrib(NAME, trustedFunction, application.getMultiAttribDescription(NAME, index));
				
				decTabs();
				generateOILAttribEnd(application.getMultiAttribDescription(TRUSTED_FUNCTION, index));	
				
				index++;
			}			
			decTabs();
			generateOILAttribEnd(application.getAttribDescription(TRUSTED));				
		}
		else {
			generateOILBooleanAttrib(TRUSTED, false, application.getAttribDescription(TRUSTED));
		}
		
		generateOILBooleanAttrib(STARTUPHOOK, application.getStartupHook(), application.getAttribDescription(STARTUPHOOK));
		generateOILBooleanAttrib(SHUTDOWNHOOK, application.getShutdownHook(), application.getAttribDescription(SHUTDOWNHOOK));
		generateOILBooleanAttrib(ERRORHOOK, application.getErrorHook(), application.getAttribDescription(ERRORHOOK));			
		
		if( application.getRestartedTask() != null ) {
			generateOILAttribNoTerm(HAS_RESTARTTASK, "TRUE");
			incTabs();
			writer.println(" {");			
			generateOILAttrib(RESTARTTASK, application.getRestartedTask(), application.getAttribDescription(RESTARTTASK));
			decTabs();
			generateOILAttribEnd(application.getAttribDescription(HAS_RESTARTTASK));				
		}
		else {
			generateOILBooleanAttrib(HAS_RESTARTTASK, false, application.getAttribDescription(HAS_RESTARTTASK));
		}			
		
		int index = 0;
		for ( Task nextTask : application.getAssignedTasks() ) {
			generateOILAttrib(TASK, nextTask, application.getMultiAttribDescription(TASK, index++));
		}

		index = 0;
		for ( Isr nextIsr : application.getAssignedISRs() ) {
			generateOILAttrib(ISR, nextIsr, application.getMultiAttribDescription(ISR, index++));
		}			
		
		index = 0;
		for ( Alarm nextAlarm : application.getAssignedAlarms() ) {
			generateOILAttrib(ALARM, nextAlarm, application.getMultiAttribDescription(ALARM, index++));
		}	
		
		index = 0;
		for ( ScheduleTable nextTable : application.getAssignedScheduleTables() ) {
			generateOILAttrib(SCHEDULETABLE, nextTable, application.getMultiAttribDescription(SCHEDULETABLE, index++));
		}
		
		index = 0;
		for ( Counter nextCounter : application.getAssignedCounters() ) {
			generateOILAttrib(COUNTER, nextCounter, application.getMultiAttribDescription(COUNTER, index++));
		}
		
		index = 0;
		for ( Resource nextResource : application.getAssignedResources() ) {
			generateOILAttrib(RESOURCE, nextResource, application.getMultiAttribDescription(RESOURCE, index++));
		}			
		
		index = 0;
		for ( Message nextMessage : application.getAssignedMessages() ) {
			generateOILAttrib(MESSAGE, nextMessage, application.getMultiAttribDescription(MESSAGE, index++));
		}				
		
		decTabs();
		generateOILObjEnd(application);					
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Event}.
	 * 
	 * @param event
	 */
	private void generateEventOIL(Event event) {
		generateOILName("EVENT", event);
		writer.println(" {");
		incTabs();
		
		generateOILAutoAttrib(MASK, "0x"+event.getMask().toString(16), event.isAutoMask(), event.getAttribDescription(MASK));
		
		decTabs();
		generateOILObjEnd(event);
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Resource}.
	 * 
	 * @param resource
	 */
	private void generateResourceOIL(Resource resource) {
		generateOILName("RESOURCE", resource);
		writer.println(" {");
		incTabs();	
		
		if ( resource.isLinked() ) {
			generateOILAttribNoTerm(RESOURCEPROPERTY, resource.getResourceProperty().toString());
			incTabs();
			writer.println(" {");
			generateOILAttrib(LINKEDRESOURCE, resource.getLinkedResource(), resource.getAttribDescription(LINKEDRESOURCE));
			decTabs();
			generateOILAttribEnd(resource.getAttribDescription(RESOURCEPROPERTY));

		}
		else {
			generateOILAttrib(RESOURCEPROPERTY, resource.getResourceProperty().toString(), resource.getAttribDescription(RESOURCEPROPERTY));
		}
		
		int index = 0;
		for ( Application nextApplication : resource.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, resource.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}
		
		decTabs();
		generateOILObjEnd(resource);		
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Task}.
	 * 
	 * @param task
	 */
	private void generateTaskOIL(Task task) {
		
		generateOILName("TASK", task);
		writer.println(" {");
		incTabs();
		
		if ( task.getAutostart() ) {
			generateOILAttribNoTerm(AUTOSTART, "TRUE");
			
			incTabs();
			writer.println(" {");

			int index = 0;
			for ( AppMode nextAppMode : task.getAppModes() ) {
				generateOILAttrib(APPMODE, nextAppMode, task.getMultiAttribDescription(APPMODE, index++));
			}				
			
			decTabs();
			generateOILAttribEnd(task.getAttribDescription(AUTOSTART));

		}
		else {
			generateOILBooleanAttrib(AUTOSTART, false, task.getAttribDescription(AUTOSTART));
		}			
	
		generateOILAttrib(PRIORITY, Long.toString(task.getPriority()), task.getAttribDescription(PRIORITY));
		generateOILAttrib(ACTIVATION, Long.toString(task.getActivation()), task.getAttribDescription(ACTIVATION));
		generateOILAttrib(SCHEDULE, task.getSchedule().toString(), task.getAttribDescription(SCHEDULE));			
		
		int index = 0;
		for ( Event nextEvent : task.getEvents() ) {
			generateOILAttrib(EVENT, nextEvent, task.getMultiAttribDescription(EVENT, index++));
		}			
		
		index = 0;
		for ( Resource nextResource : task.getResources() ) {
			generateOILAttrib(RESOURCE, nextResource, task.getMultiAttribDescription(RESOURCE, index++));
		}		
		
		index = 0;
		for ( Message nextMessage : task.getAccessedMessages() ) {
			generateOILAttrib(MESSAGE, nextMessage, task.getMultiAttribDescription(MESSAGE, index++));
		}	
		
		index = 0;
		for ( Application nextApplication : task.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, task.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}
	
		generateOILAutoAttrib(STACKSIZE, Long.toString(task.getStackSize()), task.isAutoStackSize(), task.getAttribDescription(STACKSIZE));			
		
		if ( task.hasTimingProtection() ) {
			generateOILAttribNoTerm(TIMING_PROTECTION, "TRUE");
			
			incTabs();
			writer.println(" {");
			
			generateOILAttrib(EXECUTIONBUDGET, task.getExecutionBudget().toString(), task.getAttribDescription(EXECUTIONBUDGET));			
			generateOILAttrib(TIMEFRAME, task.getTimeFrame().toString(), task.getAttribDescription(TIMEFRAME));			
			generateOILAttrib(TIMELIMIT, task.getTimeLimit().toString(), task.getAttribDescription(TIMELIMIT));			
			
			index = 0;
			for ( LockingTime nextLockingTime : task.getLockingTimes() ) {
	
				generateOILAttribNoTerm(LOCKINGTIME, nextLockingTime.getLockType().toString());
				
				incTabs();
				writer.println(" {");				
				
				if ( nextLockingTime.isResourceLockingTime() ) {
					generateOILAttrib(RESOURCE, nextLockingTime.getResource(), task.getMultiAttribDescription(RESOURCE, index));
					generateOILAttrib(RESOURCELOCKTIME, nextLockingTime.getResourceLockTime().toString(), task.getMultiAttribDescription(RESOURCELOCKTIME, index));					
				}
				else {
					generateOILAttrib(OSINTERRUPTLOCKTIME, nextLockingTime.getOSInterruptLockTime().toString(), task.getMultiAttribDescription(OSINTERRUPTLOCKTIME, index));
					generateOILAttrib(ALLINTERRUPTLOCKTIME, nextLockingTime.getAllInterruptLockTime().toString(), task.getMultiAttribDescription(ALLINTERRUPTLOCKTIME, index));	
				}
				
				decTabs();
				generateOILAttribEnd(task.getMultiAttribDescription(LOCKINGTIME, index));
				
				index++;
			}				
			
			decTabs();
			generateOILAttribEnd(task.getAttribDescription(TIMING_PROTECTION));

		}
		else {
			generateOILBooleanAttrib(TIMING_PROTECTION, false, task.getAttribDescription(TIMING_PROTECTION));
		}		
	
		decTabs();
		generateOILObjEnd(task);
	}

	/**
	 * Generates OIL format output from the given OS Model {@link Isr}.
	 * 
	 * @param isr
	 */
	private void generateIsrOIL(Isr isr) {
		
		generateOILName("ISR", isr);
		writer.println(" {");
		incTabs();
		
		generateOILAttrib(CATEGORY, Long.toString(isr.getCategory()), isr.getAttribDescription(CATEGORY));
		
		int index = 0;
		for ( Resource nextResource : isr.getResources() ) {
			generateOILAttrib(RESOURCE, nextResource, isr.getMultiAttribDescription(RESOURCE, index++));
		}		
		
		index = 0;
		for ( Message nextMessage : isr.getAccessedMessages() ) {
			generateOILAttrib(MESSAGE, nextMessage, isr.getMultiAttribDescription(MESSAGE, index++));
		}	

		generateOILAutoAttrib(STACKSIZE, Long.toString(isr.getStackSize()), isr.isAutoStackSize(), isr.getAttribDescription(STACKSIZE));			
		
		
		if ( isr.hasTimingProtection() ) {
			generateOILAttribNoTerm(TIMING_PROTECTION, "TRUE");
			
			incTabs();
			writer.println(" {");

			generateOILAttrib(EXECUTIONBUDGET, isr.getExecutionBudget().toString(), isr.getAttribDescription(EXECUTIONBUDGET));
			generateOILAttrib(COUNTLIMIT, Long.toString(isr.getCountLimit()), isr.getAttribDescription(COUNTLIMIT));
			generateOILAttrib(TIMELIMIT, isr.getTimeLimit().toString(), isr.getAttribDescription(TIMELIMIT));			
			
			index = 0;
			for ( LockingTime nextLockingTime : isr.getLockingTimes() ) {
	
				generateOILAttribNoTerm(LOCKINGTIME, nextLockingTime.getLockType().toString());
				
				incTabs();
				writer.println(" {");				
				
				if ( nextLockingTime.isResourceLockingTime() ) {
					generateOILAttrib(RESOURCE, nextLockingTime.getResource(), isr.getMultiAttribDescription(RESOURCE, index));
					generateOILAttrib(RESOURCELOCKTIME, nextLockingTime.getResourceLockTime().toString(), isr.getMultiAttribDescription(RESOURCELOCKTIME, index));					
				}
				else {
					generateOILAttrib(OSINTERRUPTLOCKTIME, nextLockingTime.getOSInterruptLockTime().toString(), isr.getMultiAttribDescription(OSINTERRUPTLOCKTIME, index));
					generateOILAttrib(ALLINTERRUPTLOCKTIME, nextLockingTime.getAllInterruptLockTime().toString(), isr.getMultiAttribDescription(ALLINTERRUPTLOCKTIME, index));	
				}
				
				decTabs();
				generateOILAttribEnd(isr.getMultiAttribDescription(LOCKINGTIME, index));
				
				index++;
			}				
			
			decTabs();
			generateOILAttribEnd(isr.getAttribDescription(TIMING_PROTECTION));

		}
		else {
			generateOILBooleanAttrib(TIMING_PROTECTION, false, isr.getAttribDescription(TIMING_PROTECTION));
		}		
	
		generateOILAttrib(PRIORITY, Long.toString(isr.getPriority()), isr.getAttribDescription(PRIORITY));
		generateOILAutoAttrib(STACKSIZE, Long.toString(isr.getStackSize()), isr.isAutoStackSize(), isr.getAttribDescription(STACKSIZE));
		generateOILStringAttrib(VECTOR, isr.getVector(), isr.getAttribDescription(VECTOR));		
		
		generateOILBooleanAttrib(DISABLE_STACKMONITORING, !isr.isStackCheckingEnabled(), isr.getAttribDescription(DISABLE_STACKMONITORING));
		
		decTabs();
		generateOILObjEnd(isr);
	}

	/**
	 * Generates OIL format output from the given OS Model {@link Counter}.
	 * 
	 * @param counter
	 */
	private void generateCounterOIL(Counter counter)	{
		
		generateOILName("COUNTER", counter);
		writer.println(" {");
		incTabs();
		
		generateOILAttrib(MINCYCLE, Long.toString(counter.getMinCycle()), counter.getAttribDescription(MINCYCLE));
		generateOILAttrib(MAXALLOWEDVALUE, Long.toString(counter.getMaxAllowedValue()), counter.getAttribDescription(MAXALLOWEDVALUE));
		generateOILAttrib(TICKSPERBASE, Long.toString(counter.getTicksPerBase()), counter.getAttribDescription(TICKSPERBASE));		
		
		generateOILAttrib(COUNTER_TYPE, counter.getCounterType().toString(), counter.getAttribDescription(COUNTER_TYPE));
		generateOILAttrib(COUNTER_UNIT, counter.getCounterUnit().toString(), counter.getAttribDescription(COUNTER_UNIT));

		generateOILAttrib(COUNTER_DEVICE_OPTIONS, counter.getDeviceOptions(), counter.getAttribDescription(COUNTER_DEVICE_OPTIONS));
		
		int index = 0;
		for ( Application nextApplication : counter.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, counter.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}	
		
		decTabs();
		generateOILObjEnd(counter);
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Alarm}.
	 * 
	 * @param alarm
	 */
	private void generateAlarmOIL(Alarm alarm)	{
		
		generateOILName("ALARM", alarm);
		writer.println(" {");
		incTabs();

		generateOILAttrib(COUNTER, alarm.getCounter(), alarm.getAttribDescription(COUNTER));
		
		generateOILAttribNoTerm(ACTION, alarm.getAction().toString());
		incTabs();
		writer.println(" {");
		
		if ( alarm.activatesTask() ) {
			generateOILAttrib(TASK, alarm.getTask(), alarm.getAttribDescription(TASK));
		}
		else if ( alarm.setsEvent() ) {
			generateOILAttrib(TASK, alarm.getTask(), alarm.getAttribDescription(TASK));
			generateOILAttrib(EVENT, alarm.getEvent(),  alarm.getAttribDescription(EVENT));
		}
		else if ( alarm.callsHandler() ) {
			generateOILStringAttrib(ALARMCALLBACKNAME, alarm.getAlarmCallbackName(), alarm.getAttribDescription(ALARMCALLBACKNAME));
		}
		else if ( alarm.incrementsCounter() ) {
			generateOILAttrib(COUNTER, alarm.getIncrementedCounter(), alarm.getAttribDescription(INCREMENTCOUNTER));
		}
		decTabs();
		generateOILAttribEnd(alarm.getAttribDescription(ACTION));		
		
		if ( alarm.getAutostart() ) {
			generateOILAttribNoTerm(AUTOSTART, "TRUE");
			
			incTabs();
			writer.println(" {");

			generateOILAttrib(ALARMTIME, Long.toString(alarm.getAlarmTime()), alarm.getAttribDescription(ALARMTIME));			
			generateOILAttrib(CYCLETIME, Long.toString(alarm.getCycleTime()), alarm.getAttribDescription(CYCLETIME));
			
			int index = 0;
			for ( AppMode nextAppMode : alarm.getAppModes() ) {
				generateOILAttrib(APPMODE, nextAppMode, alarm.getMultiAttribDescription(APPMODE, index++));
			}				
			
			decTabs();
			generateOILAttribEnd(alarm.getAttribDescription(AUTOSTART));
		}
		else {
			generateOILBooleanAttrib(AUTOSTART, false, alarm.getAttribDescription(AUTOSTART));
		}		

		int index = 0;
		for ( Application nextApplication : alarm.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, alarm.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}	
		
		decTabs();
		generateOILObjEnd(alarm);
	}	
	
	/**
	 * Generates OIL format output from the given OS Model {@link ScheduleTable}.
	 * 
	 * @param scheduleTable
	 */
	private void generateScheduleTableOIL(ScheduleTable scheduleTable)	{
		
		generateOILName("SCHEDULETABLE", scheduleTable);
		writer.println(" {");
		incTabs();
	
		generateOILAttrib(COUNTER, scheduleTable.getCounter(), scheduleTable.getAttribDescription(COUNTER));
		
		if ( scheduleTable.getAutostart() ) {
			generateOILAttribNoTerm(AUTOSTART, "TRUE");
			
			incTabs();
			writer.println(" {");

			generateOILAttrib(OFFSET, scheduleTable.getAutostartOffset().toString(), scheduleTable.getAttribDescription(OFFSET));			
			
			int index = 0;
			for ( AppMode nextAppMode : scheduleTable.getAppModes() ) {
				generateOILAttrib(APPMODE, nextAppMode, scheduleTable.getMultiAttribDescription(APPMODE, index++));
			}				
			
			decTabs();
			generateOILAttribEnd(scheduleTable.getAttribDescription(AUTOSTART));
		}
		else {
			generateOILBooleanAttrib(AUTOSTART, false, scheduleTable.getAttribDescription(AUTOSTART));
		}

		generateOILBooleanAttrib(PERIODIC, scheduleTable.isPeriodic(), scheduleTable.getAttribDescription(PERIODIC));		
		generateOILAttrib(LENGTH, scheduleTable.getLength().toString(), scheduleTable.getAttribDescription(LENGTH));
		
		int index = 0;
		for ( ScheduleTableAction nextAction : scheduleTable.getActions() ) {
						
			generateOILAttribNoTerm(ACTION, nextAction.getAction().toString());
			incTabs();
			writer.println(" {");
			
			if ( nextAction.activatesTask() ) {
				generateOILAttrib(OFFSET, nextAction.getOffset().toString(), scheduleTable.getMultiAttribDescription(ACTIONOFFSET, index));
				generateOILAttrib(TASK, nextAction.getTask(), scheduleTable.getMultiAttribDescription(TASK, index));
			}
			else if ( nextAction.setsEvent() ) {
				generateOILAttrib(OFFSET, nextAction.getOffset().toString(), scheduleTable.getMultiAttribDescription(ACTIONOFFSET, index));				
				generateOILAttrib(TASK, nextAction.getTask(), scheduleTable.getMultiAttribDescription(TASK, index));
				generateOILAttrib(EVENT, nextAction.getEvent(), scheduleTable.getMultiAttribDescription(EVENT, index));
			}
			else if ( nextAction.callsHandler() ) {
				generateOILAttrib(OFFSET, nextAction.getOffset().toString(), scheduleTable.getMultiAttribDescription(ACTIONOFFSET, index));				
				generateOILStringAttrib(ACTIONCALLBACKNAME, nextAction.getActionCallbackName(), scheduleTable.getMultiAttribDescription(ACTIONCALLBACKNAME, index));
			}
			else if ( nextAction.incrementsCounter() ) {
				generateOILAttrib(OFFSET, nextAction.getOffset().toString(), scheduleTable.getMultiAttribDescription(ACTIONOFFSET, index));				
				generateOILAttrib(COUNTER, nextAction.getIncrementedCounter(), scheduleTable.getMultiAttribDescription(INCREMENTCOUNTER, index));
			}			
	
			decTabs();
			generateOILAttribEnd(scheduleTable.getMultiAttribDescription(ACTION, index));	
			index++;
		}
		
		if ( scheduleTable.isLocalToGlobalTimeSync() ) {
			generateOILAttribNoTerm(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION, "TRUE");
			
			incTabs();
			writer.println(" {");

			generateOILAttrib(MAX_INCREASE, scheduleTable.getMaxIncrease().toString(), scheduleTable.getAttribDescription(MAX_INCREASE));			
			generateOILAttrib(MAX_DECREASE, scheduleTable.getMaxDecrease().toString(), scheduleTable.getAttribDescription(MAX_DECREASE));			
			generateOILAttrib(MAX_INCREASE_ASYNC, scheduleTable.getMaxIncreaseAsync().toString(), scheduleTable.getAttribDescription(MAX_INCREASE_ASYNC));			
			generateOILAttrib(MAX_DECREASE_ASYNC, scheduleTable.getMaxDecreaseAsync().toString(), scheduleTable.getAttribDescription(MAX_DECREASE_ASYNC));			
			generateOILAttrib(PRECISION, scheduleTable.getPrecision().toString(), scheduleTable.getAttribDescription(PRECISION));
			
			decTabs();
			generateOILAttribEnd(scheduleTable.getAttribDescription(AUTOSTART));
		}
		else {
			generateOILBooleanAttrib(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION, false, scheduleTable.getAttribDescription(LOCAL_TO_GLOBAL_TIME_SYNCHRONIZATION));
		}		
		
		
		index = 0;
		for ( Application nextApplication : scheduleTable.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, scheduleTable.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}			
		
		decTabs();
		generateOILObjEnd(scheduleTable);		
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Com}.
	 * 
	 * @param com
	 */
	private void generateComOIL(Com com)	{
		generateOILName("COM", com);
		writer.println(" {");
		incTabs();
		
		generateOILAttrib(COMSTATUS, com.getStatus().toString(), com.getAttribDescription(COMSTATUS));
		generateOILBooleanAttrib(COMERRORHOOK, com.getErrorHook(), com.getAttribDescription(COMERRORHOOK));
		generateOILBooleanAttrib(COMUSEGETSERVICEID, com.getUseGetServiceId(), com.getAttribDescription(COMUSEGETSERVICEID));
		generateOILBooleanAttrib(COMUSEPARAMETERACCESS, com.getUseParameterAccess(), com.getAttribDescription(COMUSEPARAMETERACCESS));
		generateOILBooleanAttrib(COMSTARTCOMEXTENSION, com.getStartComExtension(), com.getAttribDescription(COMSTARTCOMEXTENSION));
		
		int index = 0;
		for ( String nextAppMode : com.getAppModes() ) {
			generateOILStringAttrib(COMAPPMODE, nextAppMode, com.getMultiAttribDescription(COMAPPMODE, index++));
		}

		decTabs();
		generateOILObjEnd(com);		
	}
	
	/**
	 * Generates OIL format output from the given OS Model {@link Message}. 
	 * 
	 * @param message
	 */
	private void generateMessageOIL(Message message)	{
		
		generateOILName("MESSAGE", message);
		writer.println(" {");
		incTabs();
	
		generateOILAttribNoTerm(MESSAGEPROPERTY, message.getMessageProperty().toString());
		incTabs();
		writer.println(" {");	

		if ( message.isSendingMessage() ) {
			
			if ( message.isQueuedOrUnqueuedSendingMessage() ) {
				
				generateOILStringAttrib(CDATATYPE, message.getCDataType(), message.getAttribDescription(CDATATYPE));
			}
		}
		else {
			generateOILAttrib(SENDINGMESSAGE, message.getSendingMessage(), message.getAttribDescription(SENDINGMESSAGE));
			
			if ( message.isQueuedMessage() ) {
								
				generateOILAttrib(QUEUESIZE, Long.toString(message.getQueueSize()), message.getAttribDescription(QUEUESIZE));
			}
			else if ( message.isUnqueuedMessage() ) {
				generateOILAttrib(INITIALVALUE, message.getInitialValue().toString(), message.getAttribDescription(INITIALVALUE));
			}
			else if ( message.isStreamReceivingMessage() ) {
				
				generateOILAttrib(BUFFERSIZE, Long.toString(message.getBufferSize()), message.getAttribDescription(BUFFERSIZE));
				generateOILAttrib(LOW_THRESHOLD, Long.toString(message.getLowThreshold()), message.getAttribDescription(LOW_THRESHOLD));
				generateOILAttrib(HIGH_THRESHOLD, Long.toString(message.getHighThreshold()), message.getAttribDescription(HIGH_THRESHOLD));
			}
			
			generateOILAttribNoTerm(COM_DEVICE, message.getDeviceName());		
			incTabs();
			writer.println(" {");
			generateOILStringAttrib(COM_DEVICE_OPTIONS, message.getDeviceOptions(), message.getAttribDescription(COM_DEVICE_OPTIONS));
			decTabs();
			generateOILAttribEnd(message.getAttribDescription(COM_DEVICE));			
		}
				
		decTabs();
		generateOILAttribEnd(message.getAttribDescription(MESSAGEPROPERTY));
		
		//////////////////////////////////////////////////////////////////////////////
		
		generateOILAttribNoTerm(NOTIFICATION, message.getNotification().toString());
		incTabs();
		writer.println(" {");	
		if ( message.activatesTask() ) {
			generateOILAttrib(TASK, message.getNotificationTask(), message.getAttribDescription(TASK));
		}
		else if ( message.setsEvent() ) {
			generateOILAttrib(TASK, message.getNotificationTask(), message.getAttribDescription(TASK));
			generateOILAttrib(EVENT, message.getNotificationEvent(),  message.getAttribDescription(EVENT));
		}
		else if ( message.callsHandler() ) {
			generateOILStringAttrib(CALLBACKROUTINENAME, message.getNotificationCallbackRoutineName(), message.getAttribDescription(CALLBACKROUTINENAME));
			
			int index = 0;
			for ( Message nextMessage : message.getHighCallbackMessages() ) {
				generateOILAttrib(MESSAGE, nextMessage, message.getMultiAttribDescription(CALLBACK_MESSAGE, index++));
			}
		}
		else if ( message.setsFlag() ) {
			generateOILStringAttrib(FLAGNAME, message.getNotificationFlagName(), message.getAttribDescription(FLAGNAME));
		}
		decTabs();
		generateOILAttribEnd(message.getAttribDescription(NOTIFICATION));		
		
		//////////////////////////////////////////////////////////////////////////////
		
		if ( message.isStreamReceivingMessage() ) {		
			generateOILAttribNoTerm(LOW_NOTIFICATION, message.getLowNotification().toString());
			incTabs();
			writer.println(" {");	
			if ( message.lowActivatesTask() ) {
				generateOILAttrib(TASK, message.getLowNotificationTask(), message.getAttribDescription(LOW_TASK));
			}
			else if ( message.lowSetsEvent() ) {
				generateOILAttrib(TASK, message.getLowNotificationTask(), message.getAttribDescription(LOW_TASK));
				generateOILAttrib(EVENT, message.getLowNotificationEvent(),  message.getAttribDescription(LOW_EVENT));
			}
			else if ( message.lowCallsHandler() ) {
				generateOILStringAttrib(CALLBACKROUTINENAME, message.getLowNotificationCallbackRoutineName(), message.getAttribDescription(LOW_CALLBACKROUTINENAME));
				
				int index = 0;
				for ( Message nextMessage : message.getLowCallbackMessages() ) {
					generateOILAttrib(MESSAGE, nextMessage, message.getMultiAttribDescription(LOW_CALLBACK_MESSAGE, index++));
				}			
			}
			else if ( message.lowSetsFlag() ) {
				generateOILStringAttrib(FLAGNAME, message.getLowNotificationFlagName(), message.getAttribDescription(LOW_FLAGNAME));
			}
			decTabs();
			generateOILAttribEnd(message.getAttribDescription(LOW_NOTIFICATION));		
		}
		
		
		int index = 0;
		for ( Application nextApplication : message.getAccessingApplications() ) {
			generateOILAttrib(ACCESSING_APPLICATION, nextApplication, message.getMultiAttribDescription(ACCESSING_APPLICATION, index++));
		}			
		
		decTabs();
		generateOILObjEnd(message);
	
	}

	
	/**
	 * Generates OIL format output from the given OS Model {@link Cpu}. 
	 * 
	 * @param osModel the root Cpu element of the OS Model from which to generate
	 */
	private void generateOIL(Cpu osModel) {
		
		initTabs();
		writer.println("OIL_VERSION=\"3.0\";");
		writer.println();
		
		generateOILName("CPU", osModel);
		writer.println(" {");
		
		incTabs();
				
		////////////////////////////////////////////////////////////
		
		Os os = osModel.getOs();
		
		if ( os != null ) {
			generateOsOIL(os);
		}
	
		////////////////////////////////////////////////////////////
		
		for ( AppMode next : osModel.getAppModes() ) {

			generateOILName("APPMODE", next);
			writer.println(" {");
			generateOILObjEnd(next);
		}
		
		////////////////////////////////////////////////////////////
		
		for ( Application next : osModel.getApplications() ) {
			
			generateApplicationOIL(next);
		}
		
		////////////////////////////////////////////////////////////
		
		for ( Event next : osModel.getEvents() ) {
			
			generateEventOIL(next);
		}
		
		////////////////////////////////////////////////////////////
		
		for ( Resource next : osModel.getResources() ) {
			
			generateResourceOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		
		for ( Task next : osModel.getTasks() ) {
			
			generateTaskOIL(next);
		}	
	
		////////////////////////////////////////////////////////////
		
		for ( Isr next : osModel.getIsrs() ) {

			generateIsrOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		
		for ( Counter next : osModel.getCounters() ) {

			generateCounterOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		
		for ( Alarm next : osModel.getAlarms() ) {

			generateAlarmOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		
		for ( ScheduleTable next : osModel.getScheduleTables() ) {

			generateScheduleTableOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		Com com = osModel.getCom();
		
		if ( com != null ) {
			
			generateComOIL(com);
		}	
		
		////////////////////////////////////////////////////////////
		
		for ( Message next : osModel.getMessages() ) {

			generateMessageOIL(next);
		}		
		
		////////////////////////////////////////////////////////////
		Nm nm = osModel.getNm();
		
		if ( nm != null ) {
			generateOILName("NM", nm);
			writer.println(" {");
			generateOILObjEnd(nm);
		}		
		
		decTabs();

		generateOILObjEnd(osModel);
		
		writer.flush();
	}
	
	/**
	 * Generates and returns a textual message string from the given Problem object.
	 * 
	 * @param p the Problem containing the problem details
	 * @return a textual representation of the problem
	 */	
	private String getProblemMessage(Problem p) {

		// Could normally get the Problem class to do this, but need to adjust lineNo values
		// and filenames to take account of #include'd files etc.
		
		// get line no. of the problem
		int lineNo=p.getLineNo();
		
		if ( lineNo!=Problem.UNSPECIFIED && rootFile!=null ) {
			
			// Locate the actual file and the "real" line number of the problem
			OILFileInfo errorFile = rootFile.resolveLineNoOwner(lineNo);
			
			if ( errorFile!=null ) {
			
				// found the file in error, so use details to adjust line no. and uri of the Problem object
				
				p.setLineNo(errorFile.getLastResolvedNo());
				
				String oldUri=p.getUri();
				
				p.setUri(errorFile.getFileName());
				
				// use Problem classes method to get the message
				String msg=p.getMessage();
				
				// restore original line no. and uri values
				p.setUri(oldUri);
				
				p.setLineNo(lineNo);
						
				return msg;			
			}
			else
				return p.getMessage();	// unable to locate file/line no. so output message unchanged
		}
		else
			return p.getMessage(); // no line no. available so output message unchanged

	}
	
	/**
	 * Generates and returns an error message string from the given parse exception.
	 * 
	 * @param e the ParseException containing the exception details
	 * @return a textual representation of the ParseException
	 */
	private String getExceptionMessage(ParseException e) {

		// Could normally get the ParseException class to do this, but need to adjust lineNo values
		// and filenames to take account of #include'd etc.
		
		if ( e.currentToken!=null && rootFile!=null ) {

			StringBuffer retval = new StringBuffer();
			
			// Get an error message from the ParseException, but first adjust the stored line number info.
			// since this needs correcting to take account of included files
			
			// get line no. of the exception
			int lineNo;
			
			if ( e.currentToken.next!=null )
				lineNo=e.currentToken.next.beginLine;
			else
				lineNo=e.currentToken.beginLine;
		
			// Locate the actual file and the "real" line number of the error
			OILFileInfo errorFile = rootFile.resolveLineNoOwner(lineNo);
			
			if ( errorFile!=null ) {
				lineNo=errorFile.getLastResolvedNo();
				
				// Get the include chain path (if any) showing from where problem file was included
				retval.append(errorFile.getPathName() + errorFile.getIncludeChain());
				
				retval.append(eol);				
			}
			
			// Check if an expected token list is available, if so output into the message
			if ( e.expectedTokenSequences!=null ) {
				
				StringBuffer expected = new StringBuffer();
				
			    int maxSize = 0;
			    
			    for (int i = 0; i < e.expectedTokenSequences.length; i++) {
			      if (maxSize < e.expectedTokenSequences[i].length) {
			        maxSize = e.expectedTokenSequences[i].length;
			      }
			      for (int j = 0; j < e.expectedTokenSequences[i].length; j++) {
			        expected.append(e.tokenImage[e.expectedTokenSequences[i][j]]).append(" ");
			      }
			      if (e.expectedTokenSequences[i][e.expectedTokenSequences[i].length - 1] != 0) {
			        expected.append("...");
			      }
			      expected.append(eol).append("    ");
			    }
			    
			    retval.append("Encountered \"");
			    
			    Token tok = e.currentToken.next;
			    
			    for (int i = 0; i < maxSize; i++) {
			      if (i != 0) retval.append(" ");
			      
			      if (tok.kind == 0) {
			        retval.append(e.tokenImage[0]);
			        break;
			      }
			      retval.append(tok.image);
			      
			      tok = tok.next; 
			    }
			    
			    retval.append("\" at line " + lineNo + ", column " + e.currentToken.next.beginColumn);
			    retval.append("." + eol);
			    
			    if (e.expectedTokenSequences.length == 1) {
			      retval.append("Was expecting:" + eol + "    ");
			    } else {
			      retval.append("Was expecting one of:" + eol + "    ");
			    }
			    
			    retval.append(expected.toString());
			}
			else {
				// No expected token list available, so just show provided message and line number
			    retval.append(e.getMessage()+" at line " + lineNo);
			    retval.append("." + eol);			
			}
			return retval.toString();
		}
		else
			return e.getMessage();	// no token info. available, so just get any available text message
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// Public API
	
	
	/**
	 * Adds the given path to the list of paths searched for when locating input files during import.
	 * @param path the path to be added to the list of paths
	 */
	public void addSearchPath(String path) {
		
		if ( path!=null )
			paths.add(path);
	}	
	
	
	/**
	 * Imports the named OIL file, into the given OS Model.
	 * 
	 * If a fatal error (generally syntax type errors) is found during the parse operation the importation is NOT performed.
	 * 
	 * The given OS Model should be empty when this method is called.
	 * 
	 * @param osModel the root Cpu object of the OS Model to be populated during the import
	 * @param fileName the filename of the file to be parsed
	 * @param output the PrintWriter to use when generating import information (if null none generated)
	 * @param constants a set of constant names predefined by the caller (i.e. implicitly #define's values), may be null 
	 * @return success flag, true if import done (even with semantic errors), else false (i.e. file not found, or syntax error)
	 */
	public boolean importOIL(Cpu osModel, String fileName, PrintWriter output, Set<String> constants) {
			
		StringReader reader;
		String appName = "OIL Importer:  ";
		
		// Do loading and pre-processing of the named OIL file
		
		try {
			if( output != null )
				output.println(appName+"Parsing File " + fileName);
			
			// Call loadAndExpand() method to do loading and pre-processing (file inclusion)
			reader = new StringReader(loadAndExpand(fileName, constants)); 
		}
		catch(java.io.FileNotFoundException e){
			
			if( output != null )
				output.println(e.getMessage());
			
			return false;
        }
		catch(java.io.IOException e){
			
			if( output != null )
				output.println(e.getMessage());
			
			return false;
        }		
	
		// OIL File loaded and pre-processed ok, now available via "reader"
		
		// Do the actual parse of the file(s),
		// do a semantic check (if outputting),
		// then extract parsed info into the OS Model.
		
		try {
			// Create a OILDefinition, this is the root of the OIL Model that is populated during the parse
			OILDefinition oilModel = new OILDefinition();
			
			// Create a parser to parse the IMPLEMENTATION defined by implementationSpec
			OILParser implParser = new OILParser(new StringReader(implementationSpec));

			// Create a parser to parse the CPU (Application) defined by files loaded into the reader
			OILParser applParser = new OILParser(reader);
			
			// Parse the standard IMPLEMENTATION to populate the OIL Model with standard object/param definitions
			implParser.doParse(oilModel);
			
			// Parse the loaded OIL text, ignoring any contained IMPLEMENTATION
			applParser.doApplicationParse(oilModel);

			// Do semantic check of the model
			boolean fatalErrors = false;
			
			if( output != null ) {
				
				output.println(appName+"OIL syntax check completed successfully.");
			}

			List<Problem> problems = new ArrayList<Problem>();

			oilModel.doModelCheck(problems, true);

			if (problems.size() > 0) {
	
				if( output != null ) {				
					output.println(appName+"Semantic problems detected.");
				}
	
				for (Problem next : problems ) {
					if( output != null ) {
						output.println(getProblemMessage(next));
					}
					
					if ( next.isFatalProblem() ) {
						fatalErrors = true;	// identify if any fatal errors occurred, since can't generate if this is the case
					}
				}
			} else {
				if( output != null ) {	
					output.println(appName+"OIL definition parsed successfully.");
				}
			}
			
			
			if ( fatalErrors ) {
				return false;	// errors during constraint checks of the OIL model
			}
			
			// Now extract info from the OIL model and place into the OS Model
			extractOILModel(osModel,oilModel);
			
			if( output!=null ) {
				output.println(appName+"Extraction Complete.");
			}
		}
		catch (ParseException e) {
			
			// A ParseException is thrown due to errors found during in the parse (generally syntax errors)
			
			// If any syntax errors reported then don't import anything into the OS Model

			if( output!=null ) {
				output.println(appName+"Encountered errors during parse.");
				output.println(getExceptionMessage(e));
			}
			
			return false;	// failed to import
		}
		
		return true;	// did the import
	}
	
	/**
	 * Uses the given OSModel Cpu to export the model in OIL format.
	 * 
	 * No constraint checks are performed prior to generation, i.e. the model is taken "as-is".
	 * 
	 * @param osModel the OSModel Cpu that contains the model from which to export
	 * @param fileName the name of the file to use for output for the generated OIL, if null then outputs to stdout
	 * @param logger the logger output
	 */
	public void exportOIL(Cpu osModel, String fileName, PrintWriter logger) {
		
		try {
			if ( fileName != null ) {
				// filename given, so output to that file
				writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			
				generateOIL(osModel);	// call the generate method for the root Cpu of the model
			}
			else {
				writer = new PrintWriter(System.out);
				
				generateOIL(osModel);
			}
		} catch (IOException e) {
			logger.println("OIL Exporter:  no system generated due to I/O failure.");
		}	
		
		writer = null;
	}	
	
	/**
	 * Test main function that drives the importer.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length > 0) {

			// Create a test Cpu
			Cpu osModel=new Cpu();

			// Do an import into the test model
			OILSerializer importer = new OILSerializer();
			
			// do an import, outputting to stdout with autoflush on and no predefined constants
			importer.importOIL(osModel,args[0],new PrintWriter(new OutputStreamWriter(System.out),true), null);
		}
	} 
	
}
