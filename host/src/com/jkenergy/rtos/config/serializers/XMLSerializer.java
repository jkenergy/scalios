package com.jkenergy.rtos.config.serializers;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/serializers/XMLSerializer.java $
 * 
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


import com.jkenergy.rtos.config.osmodel.*;
import com.jkenergy.rtos.config.osmodel.Runnable;

/**
 * This is the XML importer/exporter class<br><br>
 * 
 * Import actions load, parse and populate an OS Model with information provided by file(s) containing
 * XML based definitions.<br><br>
 *   
 * Export actions generate XML format output from a given OS Model.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class XMLSerializer {
	
	/**
	 * XML Document used during population of the DOM
	 */
	private Document doc;
	
	/**
	 * Maps from XML element's attribute "id" value to the OSModelElement created to represent the XML element.
	 * This only exists when XML loading is being performed.
	 * 
	 * @see #processCpuDOM(Element, Cpu)
	 * @see #importXML(Cpu, String, PrintWriter)
	 */
	private Map<Long, OSModelElement> domMap = null;
	
	
	private final static String XML_ROOT_ELEMENT = "osconfig";	// name of root element in exported/imported XML documents
	private final static String XML_AUTO_VALUE = "auto";		// XML attribute value to indicates "auto" properties
	private final static String LIST_ATTRIBUTE_NAME = "value";	// XML attribute used to store string value for lists of strings	
	private final static String XML_REF_SUFFIX = "_ref";		// suffix added to attribute names when x-referencing elements
	
	
	
	
	// Names of attributes for each OSModelElement type
		
	private final static String ALARM_ACTION = "action";
	private final static String ALARM_CALLBACK_NAME = "alarmCallbackName";
	private final static String ALARM_AUTOSTART = "autoStart";
	private final static String ALARM_ALARMTIME = "alarmTime";
	private final static String ALARM_CYCLETIME = "cycleTime";
	private final static String ALARM_COUNTER = "counter";
	private final static String ALARM_INCREMENTED_COUNTER = "incrementedCounter";
	private final static String ALARM_ACTIVATED_TASK = "activatedTask";
	private final static String ALARM_SET_EVENT = "setEvent";		
	private final static String ALARM_APPMODES = "appModes";		
	
	private final static String APPLICATION_TRUSTED= "trusted";
	private final static String APPLICATION_TRUSTED_FUNCTION = "trustedFunction";
	private final static String APPLICATION_STARTUP_HOOK = "startupHook";
	private final static String APPLICATION_SHUTDOWN_HOOK = "shutdownHook";
	private final static String APPLICATION_ERROR_HOOK = "errorHook";
	private final static String APPLICATION_RESTARTED_TASK = "restartedTask";
	private final static String APPLICATION_ASSIGNED_ELEMENTS = "assignedElements";		
	
	private final static String COM_STATUS = "status";
	private final static String COM_ERROR_HOOK = "errorHook";
	private final static String COM_GETSERVICE_ID = "useGetServiceId";
	private final static String COM_PARAMETER_ACCESS = "useParameterAccess";
	private final static String COM_START_COM_EXTENSION = "startComExtension";
	private final static String COM_APP_MODE = "appMode";	
	
	private final static String COUNTER_TYPE = "counterType";
	private final static String COUNTER_MAX_ALLOWED_VALUE = "maxAllowedValue";
	private final static String COUNTER_TICKS_PER_BASE = "ticksPerBase";
	private final static String COUNTER_MIN_CYCLE = "minCycle";
	private final static String COUNTER_UNIT = "counterUnit";
	private final static String COUNTER_DEVICE_NAME = "deviceName";	// not currently used
	private final static String COUNTER_DEVICE_OPTIONS = "deviceOptions";	
	
	private static final String ELEMENT_ID = "id";
	private static final String ELEMENT_NAME = "name";
	private static final String ELEMENT_DESCRIPTION = "description";
	private final static String ELEMENT_ACCESSING_APPS = "accessingApplications";
	private static final String ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT = "attribDescription";
	private static final String ELEMENT_ATTRIBUTE_NAME = "name";
	private static final String ELEMENT_ATTRIBUTE_DESCRIPTION = "description";
	private static final String ELEMENT_ATTRIBUTE_INDEX = "index";	
	
	private static final String EVENT_MASK = "mask";
	
	private final static String ISR_CATEGORY = "category";	
	private final static String ISR_COUNT_LIMIT = "countLimit";
	private final static String ISR_VECTOR = "vector";
	private final static String ISR_STACK_CHECKING_ENABLED = "stackCheckingEnabled";	
	
	private final static String LOCKINGTIME_TYPE = "lockType";
	private final static String LOCKINGTIME_RESOURCE_LOCKTIME = "resourceLockTime";
	private final static String LOCKINGTIME_OSINTERRUPT_LOCKTIME = "osInterruptLockTime";
	private final static String LOCKINGTIME_ALLINTERRUPT_LOCKTIME = "allInterruptLockTime";
	private final static String LOCKINGTIME_LOCKED_RESOURCE = "lockedResource";		
	
	private final static String MESSAGE_PROPERTY = "messageProperty";
	private final static String MESSAGE_SENDING_MESSAGE = "sendingMessage";
	private final static String MESSAGE_CDATATYPE = "cDataType";
	private final static String MESSAGE_INITIAL_VALUE = "initialValue";
	private final static String MESSAGE_QUEUE_SIZE = "queueSize";
	private final static String MESSAGE_BUFFER_SIZE = "bufferSize";
	private final static String MESSAGE_HIGH_THRESHOLD = "highThreshold";
	private final static String MESSAGE_LOW_THRESHOLD = "lowThreshold";
	private final static String MESSAGE_NOTIFICATION = "notification";
	private final static String MESSAGE_NOTIFICATION_EVENT = "notificationEvent";
	private final static String MESSAGE_NOTIFICATION_TASK = "notificationTask";
	private final static String MESSAGE_NOTIFICATION_CALLBACK_ROUTINE_NAME = "notificationCallbackRoutineName";
	private final static String MESSAGE_NOTIFICATION_FLAG_NAME = "notificationFlagName";
	private final static String MESSAGE_LOW_NOTIFICATION = "lowNotification";
	private final static String MESSAGE_LOW_NOTIFICATION_EVENT = "lowNotificationEvent";
	private final static String MESSAGE_LOW_NOTIFICATION_TASK = "lowNotificationTask";
	private final static String MESSAGE_LOW_NOTIFICATION_CALLBACK_ROUTINE_NAME = "lowNotificationCallbackRoutineName";
	private final static String MESSAGE_LOW_NOTIFICATION_FLAG_NAME = "lowNotificationFlagName";	
	private final static String MESSAGE_DEVICE_NAME = "deviceName";
	private final static String MESSAGE_DEVICE_OPTIONS = "deviceOptions";
	private final static String MESSAGE_HIGH_CALLBACK_MESSAGES = "highCallbackMessages";
	private final static String MESSAGE_LOW_CALLBACK_MESSAGES = "lowCallbackMessages";
	
	private final static String OS_STATUS = "status";
	private final static String OS_STARTUP_HOOK = "startupHook";
	private final static String OS_ERROR_HOOK = "errorHook";
	private final static String OS_SHUTDOWN_HOOK = "shutdownHook";
	private final static String OS_PRETASK_HOOK = "preTaskHook";
	private final static String OS_POSTTASK_HOOK = "postTaskHook";
	private final static String OS_GETSERVICE_ID = "useGetServiceId";
	private final static String OS_PARAMATER_ACCESS = "useParameterAccess";
	private final static String OS_USE_RES_SCHEDULER = "useResScheduler";
	private final static String OS_PROTECTION_HOOK = "protectionHook";
	private final static String OS_SCALABILITY_CLASS = "scalabilityClass";
	private final static String OS_PRETASK_HOOK_STACKSIZE = "preTaskHookStackSize";
	private final static String OS_POSTTASK_HOOK_STACKSIZE = "postTaskHookStackSize";
	private final static String OS_STACK_MONITORING = "stackMonitoring";
	private final static String OS_IS_RESTARTABLE = "isRestartable";
	private final static String OS_OSC_FAILURE_HANDLED = "oscFailureHandled";
	private final static String OS_ADDR_ERROR_HANDLED = "addrErrorHandled";
	private final static String OS_MATH_ERROR_HANDLED = "mathErrorHandled";		
	
	private final static String RESOURCE_PROPERTY = "resourceProperty";
	private final static String RESOURCE_LINKED_RESOURCE = "linkedResource";		
	
	private final static String RUNNABLE_STACKSIZE = "stackSize";
	private final static String RUNNABLE_PRIORITY = "priority";
	private final static String RUNNABLE_TIMING_PROTECTION = "timingProtection";
	private final static String RUNNABLE_EXECUTION_BUDGET = "executionBudget";
	private final static String RUNNABLE_TIME_LIMIT = "timeLimit";
	private final static String RUNNABLE_RESOURCES = "resources";
	private final static String RUNNABLE_ACCESSED_MESSAGES = "accessedMessages";
	private final static String RUNNABLE_LOCKING_TIME = "lockingTime";
	
	private final static String SCHEDULETABLE_AUTOSTART = "autoStart";
	private final static String SCHEDULETABLE_AUTOSTART_OFFSET = "autoStartOffset";
	private final static String SCHEDULETABLE_PERIODIC = "periodic";
	private final static String SCHEDULETABLE_LENGTH= "length";
	private final static String SCHEDULETABLE_COUNTER = "counter";
	private final static String SCHEDULETABLE_APPMODES = "appModes";
	private final static String SCHEDULETABLE_LOCAL_TO_GLOBAL_TIME_SYNC = "localToGlobalTimeSync";
	private final static String SCHEDULETABLE_SYNC_STRATEGY = "syncStrategy";
	private final static String SCHEDULETABLE_MAX_INCREASE = "maxIncrease";
	private final static String SCHEDULETABLE_MAX_DECREASE = "maxDecrease";
	private final static String SCHEDULETABLE_MAX_INCREASE_ASYNC = "maxIncreaseAsync";
	private final static String SCHEDULETABLE_MAX_DECREASE_ASYNC = "maxDecreaseAsync";
	private final static String SCHEDULETABLE_PRECISION = "precision";
	private final static String SCHEDULETABLE_ACTION = "action";
	
	private final static String SCHEDULETABLE_ACTION_EXPIRY_ACTION = "expiryAction";
	private final static String SCHEDULETABLE_ACTION_OFFSET = "offset";
	private final static String SCHEDULETABLE_ACTION_INCREMENTED_COUNTER = "incrementedCounter";
	private final static String SCHEDULETABLE_ACTION_ACTIVATED_TASK = "activatedTask";
	private final static String SCHEDULETABLE_ACTION_SET_EVENT = "setEvent";
	private final static String SCHEDULETABLE_ACTION_CALLBACK_NAME = "alarmCallbackName";	
	
	private final static String TASK_SCHEDULE = "schedule";	
	private final static String TASK_ACTIVATION = "activation";
	private final static String TASK_AUTOSTART = "autoStart";
	private final static String TASK_TIME_FRAME = "timeFrame";
	private final static String TASK_APPMODES = "appModes";
	private final static String TASK_EVENTS = "events";
	
	
	
	/*---------------------------------------------------------------------------------------------------
	 * Helpers that support DOM creation
	 */
	
	/**
	 * Static helper that adds an attribute to the given DOM {@link Element} that conatins either 
	 * the {@link #XML_AUTO_VALUE} value or the given string value, depending on an isAuto flag.
	 * 
	 * @see #getAutoAttribute(Element, String)
	 * @see #getAutoLongAttribute(Element, String)
	 * @see #getAutoBigIntAttribute(Element, String)
	 * 
	 * 
	 * @param node the DOM {@link Element} to which the auto attribute is to be added
	 * @param attribName the name to be used for the attribute
	 * @param isAuto the flag that detarmines whether the value is an auto value
	 * @param value the non-auto value to be written if isAuto==false
	 */	
	private static void putAutoAttribute(Element node,  String attribName, boolean isAuto, String value) {
	
		if ( isAuto ) {
			node.setAttribute(attribName, XML_AUTO_VALUE);
		}
		else {
			node.setAttribute(attribName, value);
		}
	}	
	
	/**
	 * Static helper that adds an attribute to the given DOM {@link Element} that references the
	 * specified OSModelElement element, i.e. creates a reference attribute to a OSModelElement's ID.
	 * 
	 * The given attrib name has {@link #XML_REF_SUFFIX} appended in order to form the full attribute name.
	 * 
	 * @see #getDOMReference(Element, String, Class)
	 * 
	 * @param node the DOM {@link Element} to which the reference attribute is to be added
	 * @param element the OSModelElement to be referenced
	 * @param attribName the name to be used for the reference attribute
	 */
	private static void putDOMReference(Element node, OSModelElement element, String attribName) {
		
		if ( element != null ) {
			node.setAttribute(attribName+XML_REF_SUFFIX, Long.toString(element.getID()));
		}
	}	
	
	
	/**
	 * Static helper that adds an attribute to the given DOM {@link Element} that references the
	 * specified OSModelElements element, i.e. creates a multiple reference attribute to a OSModelElements IDs.
	 * 
	 * The value of the attribute is generated as a space separated list of integer string values
	 * 
	 * @see #getDOMReferences(Element, String, Class)
	 * 
	 * @param node the {@link Element} to which the references attribute is to be added
	 * @param elements the collection of OSModelElement instances to be referenced
	 * @param attribName the name to be used for the reference attribute
	 */
	private static void putDOMReferences(Element node, Collection<? extends OSModelElement> elements, String attribName) {
		
		if ( elements.size() > 0 ) {
			StringBuffer strBuffer = new StringBuffer();
			
			boolean doSpace = false;
			
			for ( OSModelElement next :  elements ) {
				if ( doSpace ) {
					strBuffer.append(" ");
				}
				else {
					doSpace=true;
				}
				strBuffer.append(next.getID());
			}	
			node.setAttribute(attribName+XML_REF_SUFFIX, strBuffer.toString());	
		}
	}	

	
	/**
	 * Static helper that adds a child element to the given DOM {@link Element} for each
	 * string value in the given list. The {@link #LIST_ATTRIBUTE_NAME} attribute of each child is set
	 * to contain the String.
	 * 
	 * This helper is useful for storing lists of strings as child elements rather than
	 * a list within an attribute, thus allowing any character to appear in the string.
	 * 
	 * @see #getDOMAttribList(Element, String)
	 * 
	 * @param element the DOM {@link Element} to which the child element is to be added
	 * @param values the list of String instances to be added
	 * @param elementName the name of the child element to be created for each entry in the list
	 */
	private static void putDOMAttribList(Element element, Collection<String> values, String elementName) {
			
		if ( values.size() > 0 ) {

			for (String next : values ) {

				// create a new Element for the next string
				Element childElement = element.getOwnerDocument().createElement(elementName);
				
				// set the value attrib to equal the string itself
				childElement.setAttribute(LIST_ATTRIBUTE_NAME, next);
				
				element.appendChild(childElement);
			}
		}
	}
	
	
	/**
	 * Static helper that adds a child element to the given DOM {@link Element} for each
	 * attribute description value conatined by the given {@link OSModelElement}. The child elements are named
	 * {@link #ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT}. The {@link #ELEMENT_ATTRIBUTE_DESCRIPTION} attribute
	 * holds the descriptions itself, and the optional {@link #ELEMENT_ATTRIBUTE_INDEX} attribute holds the
	 * index if the attribute has several descriptions.
	 * 
	 * @see #getDOMAttribDescriptions(Element, OSModelElement)
	 * 
	 * @param element the DOM {@link Element} to which the child element is to be added
	 *  @param modelElement the {@link OSModelElement} that is to have attrib descriptions added
	 */
	private static void putDOMAttribDescriptions(Element element, OSModelElement modelElement) {
		
		for (AttributeDescription next : modelElement.getAttribDescriptions() ) {

			if ( next.isMultiDescription() == false ) {
				// is a single attribute description
				String description = next.getDescription();
				
				if ( description != null ) {
					// create a new Element for the next description
					Element childElement = element.getOwnerDocument().createElement(ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT);
					
					childElement.setAttribute(ELEMENT_ATTRIBUTE_NAME, next.getName());
					
					// set the description attrib to equal the description string itself
					childElement.setAttribute(ELEMENT_ATTRIBUTE_DESCRIPTION, description);						
					element.appendChild(childElement);
				}
			}
			else {
				// is a multi-attribute description (with an index)
				for ( Integer nextIndex : next.getMultiDescriptionIndexValues() ) {
					
					String description = next.getMultiDescription(nextIndex);
					
					if ( description != null ) {
						// create a new Element for the next description
						Element childElement = element.getOwnerDocument().createElement(ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT);
						
						childElement.setAttribute(ELEMENT_ATTRIBUTE_NAME, next.getName());
						
						// set the description attrib to equal the description string itself
						childElement.setAttribute(ELEMENT_ATTRIBUTE_DESCRIPTION, description);
						
						// set the index attribute
						childElement.setAttribute(ELEMENT_ATTRIBUTE_INDEX, nextIndex.toString());
						element.appendChild(childElement);
					}						
				}
			}
		}
	}	
	
	
	/*---------------------------------------------------------------------------------------------------
	 * Helpers that support DOM extraction
	 */
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * Converts the attribute value to a Long prior to return. If the attribute does not
	 * exist, or if the value is not a parsable integer then 0 is returned.
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return the Long value of the attribute, 0 if no attribute value or not an integer value
	 */
	private static Long getLongAttribute(Element domElement, String attribName) {
		
		try {
			return new Long(domElement.getAttribute(attribName));
		}
		catch ( NumberFormatException e ) {
			// value is not a number, or attribute did not exist
			return new Long(0);
		}
	}	
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * Converts the attribute value to a BigInteger prior to return. If the attribute does not
	 * exist, or if the value is not a parsable integer then BigInteger.ZERO is returned.
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return the Long value of the attribute, BigInteger.ZERO if no attribute exists or not an integer value
	 */
	private static BigInteger getBigIntAttribute(Element domElement, String attribName) {
			
		try {
			return new BigInteger(domElement.getAttribute(attribName));
		}
		catch ( NumberFormatException e ) {
			// value is not a number, or attribute did not exist
			return BigInteger.ZERO;
		}
	}	
	
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * Converts the attribute value to a boolean prior to return. If the attribute does not
	 * exist, or if the value is not a parsable boolean then false is returned.
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return true if attribute present and represent a "true" value, else false
	 */
	private static boolean getBooleanAttribute(Element domElement, String attribName) {
		
		return new Boolean(domElement.getAttribute(attribName));
	}	
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * 
	 * If the value contains {@link #XML_AUTO_VALUE} then null is returned, else the value is converted to a
	 * Long and returned. If the attribute does not exist, or if the value is not a parsable integer then
	 * 0 is returned.
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return the Long value of the attribute, null if the value was {@link #XML_AUTO_VALUE}, 0 if no attribute exists or not an integer value
	 */
	private static Long getAutoLongAttribute(Element domElement, String attribName) {
		
		if ( domElement.getAttribute(attribName).equalsIgnoreCase(XML_AUTO_VALUE) ) {
			
			return null;	// attribute contained an "auto" value			
		}

		return getLongAttribute(domElement, attribName);
	}	
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * 
	 * If the value contains {@link #XML_AUTO_VALUE} then null is returned, else the value is converted to
	 * a BigInteger and returned. If the attribute does not exist, or if the value is not a parsable integer
	 * then BigInteger.ZERO is returned.
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return the BigInteger value of the attribute, null if the value was {@link #XML_AUTO_VALUE}, BigInteger.ZERO if no attribute exists or not a integer value
	 */
	private static BigInteger getAutoBigIntAttribute(Element domElement, String attribName) {
	
		
		if ( domElement.getAttribute(attribName).equalsIgnoreCase(XML_AUTO_VALUE) ) {
			
			return null;	// attribute contained an "auto" value			
		}

		return getBigIntAttribute(domElement, attribName);
	}		
	
	/**
	 * Static helper that gets a named attribute from the given DOM {@link Element}.
	 * 
	 * If the value contains {@link #XML_AUTO_VALUE} then null is returned, else the value returned.
	 * If the attribute does not exist then "" is returned.

	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the attribute
	 * @return the String value of the attribute, null if the value was {@link #XML_AUTO_VALUE}, "" if no attribute exists
	 */
	private static String getAutoAttribute(Element domElement, String attribName) {
	
		String value = domElement.getAttribute(attribName);
		
		if ( value.equalsIgnoreCase(XML_AUTO_VALUE) ) {
			
			return null;	// attribute contained an "auto" value			
		}

		return value;
	}		
	
	/**
	 * Gets an OSModelElement that is contained by the #domMap, that was created to represent
	 * an XML element during XML loading.
	 * 
	 * This method should be called in order to find referenced elements from their XML "id" attribute value.
	 * 
	 * @param id the XML attribute "id" value of the required OSModelElement
	 * @return the associated OSModelElement, null if no such element exists.
	 */
	private OSModelElement getContainedElementUsingXMLid(Long id) {
		
		assert domMap != null;		// this method can only be called from within XML importation
		
		return domMap.get(id);
	}	
	
	
	/**
	 * Helper that gets the OSModelElement that is referenced by the given DOM {@link Element}.
	 * 
	 * If the named attribute does not exist or if the value is not a valid reference, then null is returned.
	 * 
	 * @see #putDOMReference(Element, OSModelElement, String)
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the reference attribute
	 * @param expectedType the Class of the type of OSModelElement expected by the reference
	 * @return the OSModelElement derived instance that was referenced, null if attribute reference did not exist or was invalid
	 */
	@SuppressWarnings("unchecked")
	private <E extends OSModelElement> E getDOMReference(Element domElement, String attribName, Class<? extends OSModelElement> expectedType) {
				
		// Attempt to read the attribute that stores the ID reference value
		Long refID = getLongAttribute(domElement, attribName+XML_REF_SUFFIX);
		
		if ( refID != null ) {
			
			// a reference attribute value exists with the given name
			
			// Ask the Cpu to return the OSModelElement that represents the given XML "id"
			OSModelElement modelElement = getContainedElementUsingXMLid(refID);
			
			if ( expectedType.isInstance(modelElement) ) {
				// OSModelElement found and of requested type, so cast and return
				return (E)modelElement;
			}		
		}

		return null;
	}
	
	/**
	 * Helper that gets the OSModelElement that is referenced by the given DOM {@link Element}.
	 * 
	 * If the named attribute does not exist, then an empty set is returned.
	 * 
	 * The element reference values are extracted as if stored in a whitespace separated list.
	 * 
	 * @see #putDOMReferences(Element, Collection, String)
	 * 
	 * @param domElement the {@link Element} from which to get the attribute
	 * @param attribName the name of the reference attribute
	 * @param expectedType the Class of the type of OSModelElement expected by the references
	 * @return the ordered Set of the OSModelElement derived instances that were referenced, empty if attribute did not exist
	 */
	@SuppressWarnings("unchecked")
	private <E extends OSModelElement> Set<E> getDOMReferences(Element domElement, String attribName, Class<? extends OSModelElement> expectedType) {
				
		Set<E> modelElements = new LinkedHashSet<E>();
		
		// Attempt to read the attribute that stores the ID reference list value
		String refIDList = domElement.getAttribute(attribName+XML_REF_SUFFIX).trim();
		
		if ( refIDList != null && refIDList.length() > 0 ) {
			
			// split the list at the whitespace characters then process each ref value
			for (String refIDStr : refIDList.split("\\s")) {
			
				try {
					// Need to convert the refID string to a Long and ask the Cpu to return the OSModelElement
					// that represents the given XML "id"
					OSModelElement modelElement = getContainedElementUsingXMLid(new Long(refIDStr));
					
					if ( expectedType.isInstance(modelElement) ) {
						// OSModelElement found and of requested type, so cast and store in returned Set
						modelElements.add((E)modelElement);
					}
				}
				catch ( NumberFormatException e ) {
					// refIDStr was not a valid number, so ignore this entry
				}
			}
		}
		return modelElements;
	}
	
	/**
	 * Static helper that gets the Set of String values from the named child elements of the given DOM {@link Element}.
	 * 
	 * If the none of the named elements as children, then an empty set is returned.
	 * 
	 * The element values are extracted from the {@link #LIST_ATTRIBUTE_NAME} attribute of each child element.
	 * 
	 * @see #putDOMAttribList(Element, Collection, String)
	 * 
	 * @param domElement the {@link Element} from which to get the element
	 * @param elementName the name of the child Element from which to extract the strings
	 * @return the Set of the Strings contained in the child elements value, empty if no child elements with the given name exist
	 */
	private static Set<String> getDOMAttribList(Element domElement, String elementName) {
				
		Set<String> values = new LinkedHashSet<String>();
		
		for ( Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling() ) {
			
			if ( child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase(elementName)) {
				// found a child element with the correct name 
				Element childElement = (Element)child;
				
				String value = childElement.getAttribute(LIST_ATTRIBUTE_NAME).trim();
				
				if ( value.length() > 0 ) {
					values.add(value);
				}
			}
		}
		
		return values;
	}	

	
	/**
	 * Static helper that extracts the child {@link #ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT} elements from the
	 * given DOM {@link Element}. Description information from each child element is then added to the
	 * attribute descriptions of the given {@link OSModelElement}.
	 * 
	 * @see #putDOMAttribDescriptions(Element, OSModelElement)
	 * 
	 * 
	 * @param domElement the DOM {@link Element} to which the child element is to be added
	 * @param modelElement the {@link OSModelElement} that is to have attrib descriptions added
	 */
	private static void getDOMAttribDescriptions(Element domElement, OSModelElement modelElement) {
				
		for ( Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling() ) {
			
			if ( child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equalsIgnoreCase(ELEMENT_ATTRIBUTE_DESCRIPTION_ELEMENT)) {
				// found a child element with the correct name 
				Element childElement = (Element)child;
				
				String name = childElement.getAttribute(ELEMENT_ATTRIBUTE_NAME);
				String description = childElement.getAttribute(ELEMENT_ATTRIBUTE_DESCRIPTION).trim();
				
				if ( childElement.hasAttribute(ELEMENT_ATTRIBUTE_INDEX) ) {
					// has an index, so is a multi-attribute description
					try {
						int index = new Integer(childElement.getAttribute(ELEMENT_ATTRIBUTE_INDEX));
						modelElement.addMultiAttribDescription(name, description, index);
					} catch ( NumberFormatException e ) {
						// index was not a valid number, so ignore this entry
					}
				}
				else {
					// is a single attribute description
					modelElement.addAttribDescription(name, description);
				}
			}
		}		
	}		
	
	/*---------------------------------------------------------------------------------------------------
	 * Methods that provide DOM creation for each type of OSModelElement
	 */
	
	
	/**
	 * Creates an XML {@link Element} within the Document identified by the current {@link #doc}, then
	 * populates with XML attributes shared by all {@link OSModelElement} instances.
	 *   
	 *   The name of the created XML element is the simple class name of the {@link OSModelElement}.
	 *   
	 * @param modelElement the {@link OSModelElement} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateModelElementDOM(OSModelElement modelElement) {

		Element element = doc.createElement(modelElement.getClassName());	// use Class name as the XML element name
		
		element.setAttribute(ELEMENT_ID, Long.toString(modelElement.getID()));
		//element.setIdAttribute(ELEMENT_ID,true);
		
		element.setAttribute(ELEMENT_NAME, modelElement.getName());
		
		if ( modelElement.getDescription() != null ) {
			element.setAttribute(ELEMENT_DESCRIPTION,  modelElement.getDescription());
		}
		
		putDOMReferences(element, modelElement.getAccessingApplications(), ELEMENT_ACCESSING_APPS);
		
		putDOMAttribDescriptions(element, modelElement);
		
		return element;
	}
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param cpu the {@link Cpu} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateCpuDOM(Cpu cpu) {
	
		Element element = populateModelElementDOM(cpu);
		
		// Ask each contained element to populate the DOM
		
		Com com = cpu.getCom();
		
		if ( com != null ) {
			element.appendChild(populateComDOM(com));
		}
		
		Nm nm = cpu.getNm();
		
		if ( nm != null ) {
			element.appendChild(populateNmDOM(nm));
		}		
		
		Os os = cpu.getOs();
		
		if ( os != null ) {
			element.appendChild(populateOsDOM(os));
		}			
		
		for ( Alarm nextModelElement : cpu.getAlarms() ) {
			element.appendChild(populateAlarmDOM(nextModelElement));
		}
	
		for ( Application nextModelElement : cpu.getApplications() ) {
			element.appendChild(populateApplicationDOM(nextModelElement));
		}		
		
		for ( AppMode nextModelElement : cpu.getAppModes() ) {
			element.appendChild(populateAppModeDOM(nextModelElement));
		}	
				
		for ( Counter nextModelElement : cpu.getCounters() ) {
			element.appendChild(populateCounterDOM(nextModelElement));
		}	
		
		for ( Event nextModelElement : cpu.getEvents() ) {
			element.appendChild(populateEventDOM(nextModelElement));
		}		
	
		for ( Isr nextModelElement : cpu.getIsrs() ) {
			element.appendChild(populateIsrDOM(nextModelElement));
		}	
		
		for ( Message nextModelElement : cpu.getMessages() ) {
			element.appendChild(populateMessageDOM(nextModelElement));
		}	

		for ( Resource nextModelElement : cpu.getResources() ) {
			element.appendChild(populateResourceDOM(nextModelElement));
		}		
	
		for ( ScheduleTable nextModelElement : cpu.getScheduleTables() ) {
			element.appendChild(populateScheduleTableDOM(nextModelElement));
		}		
		
		for ( Task nextModelElement : cpu.getTasks() ) {
			element.appendChild(populateTaskDOM(nextModelElement));
		}		
		
		return element;	
	}

	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param alarm the {@link Alarm} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateAlarmDOM(Alarm alarm) {
		
		Element element = populateModelElementDOM(alarm);
		
		element.setAttribute(ALARM_ACTION, alarm.getAction().toString() );
		
		if ( alarm.getAlarmCallbackName() != null ) {
			element.setAttribute(ALARM_CALLBACK_NAME,  alarm.getAlarmCallbackName());
		}
		
		element.setAttribute(ALARM_AUTOSTART, Boolean.toString(alarm.getAutostart()));
		element.setAttribute(ALARM_ALARMTIME,Long.toString(alarm.getAlarmTime()));
		element.setAttribute(ALARM_CYCLETIME,Long.toString(alarm.getCycleTime()));
	
		putDOMReference(element, alarm.getCounter(), ALARM_COUNTER);							// reference the Counter element
		putDOMReference(element, alarm.getIncrementedCounter(), ALARM_INCREMENTED_COUNTER);		// reference the incremented Counter element
		putDOMReference(element, alarm.getTask(), ALARM_ACTIVATED_TASK);						// reference the activated task
		putDOMReference(element, alarm.getEvent(), ALARM_SET_EVENT);							// reference the set Event
		putDOMReferences(element, alarm.getAppModes(), ALARM_APPMODES );						// autostart appModes		
		
		return element;
	}	

	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param application the {@link Application} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateApplicationDOM(Application application) {
		
		Element element = populateModelElementDOM(application);
		
		element.setAttribute(APPLICATION_TRUSTED, Boolean.toString(application.isTrusted()));
		
		putDOMAttribList(element, application.getTrustedFunctions(), APPLICATION_TRUSTED_FUNCTION);
		
		element.setAttribute(APPLICATION_STARTUP_HOOK,Boolean.toString(application.getStartupHook()));
		element.setAttribute(APPLICATION_SHUTDOWN_HOOK,Boolean.toString(application.getShutdownHook()));
		element.setAttribute(APPLICATION_ERROR_HOOK,Boolean.toString(application.getErrorHook()));
		putDOMReference(element, application.getRestartedTask(), APPLICATION_RESTARTED_TASK);	// reference the restarted task
			
		putDOMReferences(element, application.getAssignedElements(), APPLICATION_ASSIGNED_ELEMENTS);		
		
		return element;
	}	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param appMode the {@link AppMode} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateAppModeDOM(AppMode appMode) {
		
		Element element = populateModelElementDOM(appMode);
		
		return element;
	}	

	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param com the {@link Com} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateComDOM(Com com) {
		
		Element element = populateModelElementDOM(com);
		
		element.setAttribute(COM_STATUS, com.getStatus().toString() );
		element.setAttribute(COM_ERROR_HOOK, Boolean.toString(com.getErrorHook()));
		element.setAttribute(COM_GETSERVICE_ID, Boolean.toString(com.getUseGetServiceId()));
		element.setAttribute(COM_PARAMETER_ACCESS, Boolean.toString(com.getUseParameterAccess()));
		element.setAttribute(COM_START_COM_EXTENSION, Boolean.toString(com.getStartComExtension()));
	
		putDOMAttribList(element, com.getAppModes(), COM_APP_MODE);		
		
		return element;
	}	

	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param counter the {@link Counter} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateCounterDOM(Counter counter) {
		
		Element element = populateModelElementDOM(counter);
					
		element.setAttribute(COUNTER_TYPE, counter.getCounterType().toString());
		element.setAttribute(COUNTER_MAX_ALLOWED_VALUE, Long.toString(counter.getMaxAllowedValue()));
		element.setAttribute(COUNTER_TICKS_PER_BASE, Long.toString(counter.getTicksPerBase()));
		element.setAttribute(COUNTER_MIN_CYCLE, Long.toString(counter.getMinCycle()));
		element.setAttribute(COUNTER_UNIT, counter.getCounterUnit().toString());
			
		if ( counter.getDeviceOptions() != null ) {
			element.setAttribute(COUNTER_DEVICE_OPTIONS, counter.getDeviceOptions());
		}
				
		return element;
	}
	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param event the {@link Event} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateEventDOM(Event event) {
		
		Element element = populateModelElementDOM(event);
				
		putAutoAttribute(element, EVENT_MASK, event.isAutoMask(), event.getMask().toString());
		
		return element;
	}	
		
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param isr the {@link Isr} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateIsrDOM(Isr isr) {
		
		Element element = populateRunnableDOM(isr);
				
		element.setAttribute(ISR_CATEGORY, Long.toString(isr.getCategory()) );
		element.setAttribute(ISR_COUNT_LIMIT, Long.toString(isr.getCountLimit()) );
		
		if ( isr.getVector() != null ) {
			element.setAttribute(ISR_VECTOR, isr.getVector());
		}
		
		element.setAttribute(ISR_STACK_CHECKING_ENABLED, Boolean.toString(isr.isStackCheckingEnabled()));
	
		return element;
	}	
	

	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param message the {@link Message} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateMessageDOM(Message message) {
		
		Element element = populateModelElementDOM(message);
			
		element.setAttribute(MESSAGE_PROPERTY, message.getMessageProperty().toString() );
		
		putDOMReference(element, message.getSendingMessage(), MESSAGE_SENDING_MESSAGE);
		
		if ( message.getCDataType() != null ) {
			element.setAttribute(MESSAGE_CDATATYPE, message.getCDataType());
		}
		
		element.setAttribute(MESSAGE_INITIAL_VALUE, message.getInitialValue().toString());
		element.setAttribute(MESSAGE_QUEUE_SIZE, Long.toString(message.getQueueSize()));
		
		element.setAttribute(MESSAGE_BUFFER_SIZE, Long.toString(message.getBufferSize()));
		element.setAttribute(MESSAGE_HIGH_THRESHOLD, Long.toString(message.getHighThreshold()));
		element.setAttribute(MESSAGE_LOW_THRESHOLD, Long.toString(message.getLowThreshold()));
		
		
		element.setAttribute(MESSAGE_NOTIFICATION, message.getNotification().toString() );
		putDOMReference(element, message.getNotificationEvent(), MESSAGE_NOTIFICATION_EVENT);
		putDOMReference(element, message.getNotificationTask(), MESSAGE_NOTIFICATION_TASK);	
		if ( message.getNotificationCallbackRoutineName() != null ) {
			element.setAttribute(MESSAGE_NOTIFICATION_CALLBACK_ROUTINE_NAME, message.getNotificationCallbackRoutineName());
		}		
		if ( message.getNotificationFlagName() != null ) {
			element.setAttribute(MESSAGE_NOTIFICATION_FLAG_NAME, message.getNotificationFlagName());
		}	
		
		
		element.setAttribute(MESSAGE_LOW_NOTIFICATION, message.getLowNotification().toString() );
		putDOMReference(element, message.getLowNotificationEvent(), MESSAGE_LOW_NOTIFICATION_EVENT);
		putDOMReference(element, message.getLowNotificationTask(), MESSAGE_LOW_NOTIFICATION_TASK);		
		if ( message.getLowNotificationCallbackRoutineName() != null ) {
			element.setAttribute(MESSAGE_LOW_NOTIFICATION_CALLBACK_ROUTINE_NAME, message.getLowNotificationCallbackRoutineName());
		}			
		if ( message.getLowNotificationFlagName() != null ) {
			element.setAttribute(MESSAGE_LOW_NOTIFICATION_FLAG_NAME, message.getLowNotificationFlagName());
		}			
		
		if ( message.getDeviceName() != null ) {
			element.setAttribute(MESSAGE_DEVICE_NAME, message.getDeviceName());
		}		

		if ( message.getDeviceOptions() != null ) {
			element.setAttribute(MESSAGE_DEVICE_OPTIONS, message.getDeviceOptions());
		}		
		
		putDOMReferences(element, message.getHighCallbackMessages(), MESSAGE_HIGH_CALLBACK_MESSAGES);
		putDOMReferences(element, message.getLowCallbackMessages(), MESSAGE_LOW_CALLBACK_MESSAGES);
			
		return element;
	}	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param nm the {@link Nm} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateNmDOM(Nm nm) {
		
		Element element = populateModelElementDOM(nm);
		
		return element;
	}	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param os the {@link Os} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateOsDOM(Os os) {
		
		Element element = populateModelElementDOM(os);
		
		element.setAttribute(OS_STATUS, os.getStatus().toString() );
		element.setAttribute(OS_STARTUP_HOOK, Boolean.toString(os.getStartupHook()));
		element.setAttribute(OS_ERROR_HOOK, Boolean.toString(os.getErrorHook()));
		element.setAttribute(OS_SHUTDOWN_HOOK, Boolean.toString(os.getShutdownHook()));
		element.setAttribute(OS_PRETASK_HOOK, Boolean.toString(os.getPreTaskHook()));
		element.setAttribute(OS_POSTTASK_HOOK, Boolean.toString(os.getPostTaskHook()));
		element.setAttribute(OS_GETSERVICE_ID, Boolean.toString(os.getUseGetServiceId()));
		element.setAttribute(OS_PARAMATER_ACCESS, Boolean.toString(os.getUseParameterAccess()));
		element.setAttribute(OS_USE_RES_SCHEDULER, Boolean.toString(os.getUseResScheduler()));
		element.setAttribute(OS_PROTECTION_HOOK, Boolean.toString(os.hasProtectionHook()));
		
		putAutoAttribute(element, OS_SCALABILITY_CLASS, os.getAutoScalabilityClass(), os.getScalabilityClass().toString());
		putAutoAttribute(element, OS_PRETASK_HOOK_STACKSIZE, os.isAutoPreTaskHookStackSize(), Long.toString(os.getPreTaskHookStackSize()));		
		putAutoAttribute(element, OS_POSTTASK_HOOK_STACKSIZE, os.isAutoPostTaskHookStackSize(), Long.toString(os.getPreTaskHookStackSize()));		
		
		element.setAttribute(OS_STACK_MONITORING, Boolean.toString(os.isStackCheckingEnabled()));
		element.setAttribute(OS_IS_RESTARTABLE, Boolean.toString(os.isRestartable()));
		element.setAttribute(OS_OSC_FAILURE_HANDLED, Boolean.toString(os.isOscFailureHandled()));
		element.setAttribute(OS_ADDR_ERROR_HANDLED, Boolean.toString(os.isAddrErrorHandled()));
		element.setAttribute(OS_MATH_ERROR_HANDLED, Boolean.toString(os.isMathErrorHandled()));		
		
		return element;
	}	
	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param resource the {@link Resource} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateResourceDOM(Resource resource) {
		
		Element element = populateModelElementDOM(resource);
				
		element.setAttribute(RESOURCE_PROPERTY, resource.getResourceProperty().toString() );
		
		putDOMReference(element, resource.getLinkedResource(), RESOURCE_LINKED_RESOURCE);
				
		return element;
	}	
	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param runnable the {@link Runnable} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateRunnableDOM(Runnable runnable) {
		
		Element element = populateModelElementDOM(runnable);

		putAutoAttribute(element, RUNNABLE_STACKSIZE, runnable.isAutoStackSize(), Long.toString(runnable.getStackSize()));
		
		element.setAttribute(RUNNABLE_PRIORITY,Long.toString(runnable.getPriority()));		
		element.setAttribute(RUNNABLE_TIMING_PROTECTION,Boolean.toString(runnable.hasTimingProtection()));
		element.setAttribute(RUNNABLE_EXECUTION_BUDGET,runnable.getExecutionBudget().toString());
		element.setAttribute(RUNNABLE_TIME_LIMIT,runnable.getTimeLimit().toString());
		
		putDOMReferences(element, runnable.getResources(), RUNNABLE_RESOURCES);	// reference the Resource elements
		putDOMReferences(element, runnable.getAccessedMessages(), RUNNABLE_ACCESSED_MESSAGES);	// reference the Message elements
		
		for ( LockingTime next : runnable.getLockingTimes() ) {
			
			Element lockingTimeElement = doc.createElement(RUNNABLE_LOCKING_TIME);
			
			lockingTimeElement.setAttribute(LOCKINGTIME_TYPE, next.getLockType().toString() );
			lockingTimeElement.setAttribute(LOCKINGTIME_RESOURCE_LOCKTIME, next.getResourceLockTime().toString() );
			lockingTimeElement.setAttribute(LOCKINGTIME_OSINTERRUPT_LOCKTIME, next.getOSInterruptLockTime().toString() );
			lockingTimeElement.setAttribute(LOCKINGTIME_ALLINTERRUPT_LOCKTIME, next.getAllInterruptLockTime().toString() );
					
			putDOMReference(lockingTimeElement, next.getResource(), LOCKINGTIME_LOCKED_RESOURCE);			
			
			element.appendChild(lockingTimeElement);
		}		
		
		return element;
	}		
		
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param scheduleTable the {@link ScheduleTable} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateScheduleTableDOM(ScheduleTable scheduleTable) {
		
		Element element = populateModelElementDOM(scheduleTable);

		element.setAttribute(SCHEDULETABLE_AUTOSTART, Boolean.toString(scheduleTable.getAutostart()));
		element.setAttribute(SCHEDULETABLE_AUTOSTART_OFFSET, scheduleTable.getAutostartOffset().toString() );
		element.setAttribute(SCHEDULETABLE_PERIODIC, Boolean.toString(scheduleTable.isPeriodic()));
		element.setAttribute(SCHEDULETABLE_LENGTH, scheduleTable.getLength().toString());
		element.setAttribute(SCHEDULETABLE_LOCAL_TO_GLOBAL_TIME_SYNC,  Boolean.toString(scheduleTable.isLocalToGlobalTimeSync()));
		element.setAttribute(SCHEDULETABLE_SYNC_STRATEGY, scheduleTable.getSyncStrategy().toString());
		element.setAttribute(SCHEDULETABLE_MAX_INCREASE, scheduleTable.getMaxIncrease().toString());
		element.setAttribute(SCHEDULETABLE_MAX_DECREASE, scheduleTable.getMaxDecrease().toString());
		element.setAttribute(SCHEDULETABLE_MAX_INCREASE_ASYNC, scheduleTable.getMaxIncreaseAsync().toString());
		element.setAttribute(SCHEDULETABLE_MAX_DECREASE_ASYNC, scheduleTable.getMaxDecreaseAsync().toString());
		element.setAttribute(SCHEDULETABLE_PRECISION, scheduleTable.getPrecision().toString());
		
		putDOMReference(element, scheduleTable.getCounter(), SCHEDULETABLE_COUNTER);	// reference the Counter element
		putDOMReferences(element, scheduleTable.getAppModes(), SCHEDULETABLE_APPMODES );// autostart appModes
			
		for ( ScheduleTableAction next : scheduleTable.getActions() ) {
			
			Element actionElement = doc.createElement(SCHEDULETABLE_ACTION);
					
			actionElement.setAttribute(SCHEDULETABLE_ACTION_EXPIRY_ACTION, next.getAction().toString());
			actionElement.setAttribute(SCHEDULETABLE_ACTION_OFFSET, next.getOffset().toString());
			
			if ( next.getActionCallbackName() != null ) {
				actionElement.setAttribute(SCHEDULETABLE_ACTION_CALLBACK_NAME, next.getActionCallbackName());
			}
			
			putDOMReference(actionElement, next.getIncrementedCounter(), SCHEDULETABLE_ACTION_INCREMENTED_COUNTER);
			putDOMReference(actionElement, next.getEvent(), SCHEDULETABLE_ACTION_SET_EVENT);
			putDOMReference(actionElement, next.getTask(), SCHEDULETABLE_ACTION_ACTIVATED_TASK);			

			element.appendChild(actionElement);
		}
		
		return element;
	}	
	
	
	/**
	 * Creates then populates an XML {@link Element} within the Document identified by the current {@link #doc}.
	 * 
	 * @param task the {@link Task} from which to populate the DOM
	 * @return the created XML {@link Element} node
	 */	
	private Element populateTaskDOM(Task task) {
		
		Element element = populateRunnableDOM(task);
		
		element.setAttribute(TASK_SCHEDULE, task.getSchedule().toString() );
		element.setAttribute(TASK_ACTIVATION, Long.toString(task.getActivation()) );
		element.setAttribute(TASK_AUTOSTART, Boolean.toString(task.getAutostart()));
		element.setAttribute(TASK_TIME_FRAME, task.getTimeFrame().toString());
				
		putDOMReferences(element, task.getAppModes(), TASK_APPMODES);		// autostart appModes
		putDOMReferences(element, task.getEvents(), TASK_EVENTS);
				
		return element;
	}
	
	
	/*---------------------------------------------------------------------------------------------------
	 * Method that provide DOM extraction to each type of OSModelElement
	 */
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link OSModelElement}
	 * 
	 * @see #populateModelElementDOM(OSModelElement)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param modelElement the {@link OSModelElement} to be setup using the extracted information
	 */
	private void processModelElementDOM(Element domElement, OSModelElement modelElement) {
		
		assert domElement != null;		// setDOMElement() must be called prior to calling this method
		
		// note "id" is not read back in and used here, since a new id value has already been setup in the constructor 
		
		modelElement.setName(domElement.getAttribute(ELEMENT_NAME));	// read name of the element
		
		if ( domElement.hasAttribute(ELEMENT_DESCRIPTION)) {
			// read description of the element
			modelElement.setDescription(domElement.getAttribute(ELEMENT_DESCRIPTION));	
		}
		
		for ( Application next : this.<Application>getDOMReferences(domElement, ELEMENT_ACCESSING_APPS, Application.class) ) {
			modelElement.addAccessingApplication(next);
		}
		
		getDOMAttribDescriptions(domElement, modelElement);		
	}
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Cpu}
	 * 
	 * @see #populateCpuDOM(Cpu)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param cpu the {@link Cpu} to be setup using the extracted information
	 */	
	private void processCpuDOM(Element domElement, Cpu cpu) {
		
		processModelElementDOM(domElement, cpu);		// ensure "name" and "description" setup correctly
			
		// Create map that allows the mapping of a new OSModelElement to the Element from which it was created
		Map<OSModelElement, Element> elementMap = new HashMap<OSModelElement, Element>();
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// Iterate over child nodes, looking for known types to process. Create OSModelElement instances as required
		
		for ( Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling() ) {
			
			OSModelElement newModelElement;
			
			if ( child.getNodeType() == Node.ELEMENT_NODE ) {
				// found an element node 
				Element childElement = (Element)child;
				
				String typeName = childElement.getNodeName();	// get element "name"
				
				/*
				 * Attempt to match element name to known OSModelElement name and create.
				 * Do not specify any names for created elements since this is extracted by
				 * the element itself during the call to processDOM.
				 */
				
				if (typeName.equalsIgnoreCase(Resource.class.getSimpleName())) {
					newModelElement = cpu.createResource(null);	
				}				
				else if (typeName.equalsIgnoreCase(Task.class.getSimpleName())) {
					newModelElement = cpu.createTask(null);
				}					
				else if (typeName.equalsIgnoreCase(Event.class.getSimpleName())) {
					newModelElement = cpu.createEvent(null);
				}	
				else if (typeName.equalsIgnoreCase(Message.class.getSimpleName())) {
					newModelElement = cpu.createMessage(null);
				}
				else if (typeName.equalsIgnoreCase(Alarm.class.getSimpleName())) {
					newModelElement = cpu.createAlarm(null);
				}				
				else if (typeName.equalsIgnoreCase(ScheduleTable.class.getSimpleName())) {
					newModelElement = cpu.createScheduleTable(null);
				}				
				else if (typeName.equalsIgnoreCase(Isr.class.getSimpleName())) {
					newModelElement = cpu.createIsr(null);
				}	
				else if (typeName.equalsIgnoreCase(AppMode.class.getSimpleName())) {
					newModelElement = cpu.createAppMode(null);
				}				
				else if (typeName.equalsIgnoreCase(Counter.class.getSimpleName())) {
					newModelElement = cpu.createCounter(null);
				}
				else if (typeName.equalsIgnoreCase(Application.class.getSimpleName())) {
					newModelElement = cpu.createApplication(null);
				}				
				else if (typeName.equalsIgnoreCase(Com.class.getSimpleName())) {
					newModelElement = cpu.createCom(null);
				}
				else if (typeName.equalsIgnoreCase(Os.class.getSimpleName())) {
					newModelElement = cpu.createOs(null);
				}
				else if (typeName.equalsIgnoreCase(Nm.class.getSimpleName())) {
					newModelElement = cpu.createNm(null);
				}
				else {
					newModelElement = null;
				}
				// ignore unknown element types
				
				if ( newModelElement != null ) {									
					try {
						// Populate the id->OSModelElement map
						domMap.put(new Long(childElement.getAttribute(ELEMENT_ID)), newModelElement);
						
						// Populate the OSModelElement->Element map
						elementMap.put(newModelElement, childElement);
					}
					catch ( NumberFormatException e ) {
						// "id" not a number, or "id" attribute did not exist so don't add to map
					}
				}
			}
		}
		
		// Now all OSModelElements have been created, need to populate each one using info. from the DOM
		
		Com com = cpu.getCom();
		
		if ( com != null ) {
			processComDOM(elementMap.get(com), com);
		}
		
		Nm nm = cpu.getNm();
		
		if ( nm != null ) {
			processNmDOM(elementMap.get(nm), nm);
		}		
		
		Os os = cpu.getOs();
		
		if ( os != null ) {
			processOsDOM(elementMap.get(os), os);
		}			
		
		for ( Alarm nextModelElement : cpu.getAlarms() ) {
			processAlarmDOM(elementMap.get(nextModelElement), nextModelElement);
		}
	
		for ( Application nextModelElement : cpu.getApplications() ) {
			processApplicationDOM(elementMap.get(nextModelElement), nextModelElement);
		}		
		
		for ( AppMode nextModelElement : cpu.getAppModes() ) {
			processAppModeDOM(elementMap.get(nextModelElement), nextModelElement);
		}	
				
		for ( Counter nextModelElement : cpu.getCounters() ) {
			processCounterDOM(elementMap.get(nextModelElement), nextModelElement);
		}	
		
		for ( Event nextModelElement : cpu.getEvents() ) {
			processEventDOM(elementMap.get(nextModelElement), nextModelElement);
		}		
	
		for ( Isr nextModelElement : cpu.getIsrs() ) {
			processIsrDOM(elementMap.get(nextModelElement), nextModelElement);
		}	
		
		for ( Message nextModelElement : cpu.getMessages() ) {
			processMessageDOM(elementMap.get(nextModelElement), nextModelElement);
		}	

		for ( Resource nextModelElement : cpu.getResources() ) {
			processResourceDOM(elementMap.get(nextModelElement), nextModelElement);
		}		
	
		for ( ScheduleTable nextModelElement : cpu.getScheduleTables() ) {
			processScheduleTableDOM(elementMap.get(nextModelElement), nextModelElement);
		}		
		
		for ( Task nextModelElement : cpu.getTasks() ) {
			processTaskDOM(elementMap.get(nextModelElement), nextModelElement);
		}		
		
	}	
	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Alarm}
	 * 
	 * @see #populateAlarmDOM(Alarm)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param alarm the {@link Alarm} to be setup using the extracted information
	 */		
	private void processAlarmDOM(Element domElement, Alarm alarm) {
		
		processModelElementDOM(domElement, alarm);		// ensure "name" and "description" setup correctly
		
		ActionKind newAction = ActionKind.get(domElement.getAttribute(ALARM_ACTION));
		
		if ( newAction != null ) {
			alarm.setAction(newAction);	// only write data member if value known
		}		
		
		if ( domElement.hasAttribute(ALARM_CALLBACK_NAME)) {
			alarm.setAlarmCallbackName(domElement.getAttribute(ALARM_CALLBACK_NAME));	
		}			
		
		alarm.setAutostart(getBooleanAttribute(domElement, ALARM_AUTOSTART));
		alarm.setAlarmTime(getLongAttribute(domElement, ALARM_ALARMTIME));
		alarm.setCycleTime(getLongAttribute(domElement, ALARM_CYCLETIME));
		
		Counter newCounter = getDOMReference(domElement, ALARM_COUNTER, Counter.class);
		alarm.setCounter(newCounter);
		
		newCounter = getDOMReference(domElement, ALARM_INCREMENTED_COUNTER, Counter.class);
		alarm.setIncrementedCounter(newCounter);
		
		Task newTask = getDOMReference(domElement, ALARM_ACTIVATED_TASK, Task.class);
		alarm.setTask(newTask);
		
		Event newEvent = getDOMReference(domElement, ALARM_SET_EVENT, Event.class);
		alarm.setEvent(newEvent);
		
		for ( AppMode next : this.<AppMode>getDOMReferences(domElement, ALARM_APPMODES, AppMode.class) ) {
			alarm.addAppMode(next);
		}		
		
	}
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Application}
	 * 
	 * @see #populateApplicationDOM(Application)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param application the {@link Application} to be setup using the extracted information
	 */		
	private void processApplicationDOM(Element domElement, Application application) {
		
		processModelElementDOM(domElement, application);		// ensure "name" and "description" setup correctly
		
		application.setTrusted(getBooleanAttribute(domElement, APPLICATION_TRUSTED));
		
		for ( String next : getDOMAttribList(domElement, APPLICATION_TRUSTED_FUNCTION) ) {
			application.addTrustedFunction(next);
		}
		
		application.setStartupHook(getBooleanAttribute(domElement, APPLICATION_STARTUP_HOOK));
		application.setShutdownHook(getBooleanAttribute(domElement, APPLICATION_SHUTDOWN_HOOK));
		application.setErrorHook(getBooleanAttribute(domElement, APPLICATION_ERROR_HOOK));
		
		Task newTask = getDOMReference(domElement, APPLICATION_RESTARTED_TASK, Task.class);
		application.setRestartedTask(newTask);
		
		for ( OSModelElement next : getDOMReferences(domElement, APPLICATION_ASSIGNED_ELEMENTS, OSModelElement.class) ) {
			application.addAssignedElement(next);
		}		
	}
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link AppMode}
	 * 
	 * @see #populateAppModeDOM(AppMode)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param appMode the {@link AppMode} to be setup using the extracted information
	 */		
	private void processAppModeDOM(Element domElement, AppMode appMode) {
		
		processModelElementDOM(domElement, appMode);		// ensure "name" and "description" setup correctly
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Com}
	 * 
	 * @see #populateComDOM(Com)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param com the {@link Com} to be setup using the extracted information
	 */		
	private void processComDOM(Element domElement, Com com) {
		
		processModelElementDOM(domElement, com);		// ensure "name" and "description" setup correctly
				
		ComStatusKind newStatus = ComStatusKind.get(domElement.getAttribute(COM_STATUS));
		
		if ( newStatus != null ) {
			com.setStatus(newStatus);	// only write data member if value known
		}		
		
		com.setErrorHook(getBooleanAttribute(domElement, COM_ERROR_HOOK));
		com.setUseGetServiceId(getBooleanAttribute(domElement, COM_GETSERVICE_ID));
		com.setUseParameterAccess(getBooleanAttribute(domElement, COM_PARAMETER_ACCESS));
		com.setStartComExtension(getBooleanAttribute(domElement, COM_START_COM_EXTENSION));
		
		for ( String next : getDOMAttribList(domElement, COM_APP_MODE) ) {
			com.addAppModeName(next);
		}
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Counter}
	 * 
	 * @see #populateComDOM(Com)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param counter the {@link Counter} to be setup using the extracted information
	 */		
	private void processCounterDOM(Element domElement, Counter counter) {
		
		processModelElementDOM(domElement, counter);		// ensure "name" and "description" setup correctly
		
		CounterTypeKind newType = CounterTypeKind.get(domElement.getAttribute(COUNTER_TYPE));
		
		if ( newType != null ) {
			counter.setCounterType(newType);
		}
		
		counter.setMaxAllowedValue(getLongAttribute(domElement, COUNTER_MAX_ALLOWED_VALUE));
		counter.setTicksPerBase(getLongAttribute(domElement, COUNTER_TICKS_PER_BASE));
		counter.setMinCycle(getLongAttribute(domElement, COUNTER_MIN_CYCLE));

		CounterUnitKind newUnit = CounterUnitKind.get(domElement.getAttribute(COUNTER_UNIT));
		
		if ( newUnit != null ) {
			counter.setCounterUnit(newUnit);
		}
		
		if ( domElement.hasAttribute(COUNTER_DEVICE_OPTIONS) ) {
			counter.setDeviceOptions(domElement.getAttribute(COUNTER_DEVICE_OPTIONS));
		}		
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Event}
	 * 
	 * @see #populateEventDOM(Event)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param event the {@link Event} to be setup using the extracted information
	 */		
	private void processEventDOM(Element domElement, Event event) {
	
		processModelElementDOM(domElement, event);		// ensure "name" and "description" setup correctly
					
		BigInteger newMask = getAutoBigIntAttribute(domElement, EVENT_MASK);
		
		if ( newMask != null ) {
			event.setMask(newMask);	// only set data member if non-auto
		}
		else {
			event.isAutoMask(true);
		}
	}		

	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Isr}
	 * 
	 * @see #populateIsrDOM(Isr)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param isr the {@link Isr} to be setup using the extracted information
	 */		
	private void processIsrDOM(Element domElement, Isr isr) {
		
		processRunnableDOM(domElement, isr);		// ensure Runnable info. setup correctly
		
		isr.setCategory(getLongAttribute(domElement, ISR_CATEGORY));
		isr.setCountLimit(getLongAttribute(domElement, ISR_COUNT_LIMIT));
				
		if ( domElement.hasAttribute(ISR_VECTOR) ) {
			isr.setVector(domElement.getAttribute(ISR_VECTOR));
		}
		
		isr.setStackCheckingEnabled(getBooleanAttribute(domElement, ISR_STACK_CHECKING_ENABLED));
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Message}
	 * 
	 * @see #populateMessageDOM(Message)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param message the {@link Message} to be setup using the extracted information
	 */		
	private void processMessageDOM(Element domElement, Message message) {
		
		processModelElementDOM(domElement, message);		// ensure "name" and "description" setup correctly
		
		MessageKind newMessageProperty = MessageKind.get(domElement.getAttribute(MESSAGE_PROPERTY));
		
		if ( newMessageProperty != null ) {
			message.setMessageProperty(newMessageProperty);
		}
		
		Message newSendingMessage = getDOMReference(domElement, MESSAGE_SENDING_MESSAGE, Message.class);
		message.setSendingMessage(newSendingMessage);
		
		message.setCDataType(domElement.getAttribute(MESSAGE_CDATATYPE));
		message.setInitialValue(getBigIntAttribute(domElement, MESSAGE_INITIAL_VALUE));	
		message.setQueueSize(getLongAttribute(domElement, MESSAGE_QUEUE_SIZE));	
		message.setBufferSize(getLongAttribute(domElement, MESSAGE_BUFFER_SIZE));		
		message.setHighThreshold(getLongAttribute(domElement, MESSAGE_HIGH_THRESHOLD));
		message.setLowThreshold(getLongAttribute(domElement, MESSAGE_LOW_THRESHOLD));

		NotificationKind newNotification = NotificationKind.get(domElement.getAttribute(MESSAGE_NOTIFICATION));
		
		if ( newNotification != null ) {
			message.setNotification(newNotification);
		}

		Event newEvent = getDOMReference(domElement, MESSAGE_NOTIFICATION_EVENT, Event.class);
		message.setNotificationEvent(newEvent);

		Task newTask = getDOMReference(domElement, MESSAGE_NOTIFICATION_TASK, Task.class);
		message.setNotificationTask(newTask);
		
		message.setNotificationCallbackRoutineName(domElement.getAttribute(MESSAGE_NOTIFICATION_CALLBACK_ROUTINE_NAME));	
		message.setNotificationFlagName(domElement.getAttribute(MESSAGE_NOTIFICATION_FLAG_NAME));		

		newNotification = NotificationKind.get(domElement.getAttribute(MESSAGE_LOW_NOTIFICATION));
		
		if ( newNotification != null ) {
			message.setLowNotification(newNotification);
		}

		newEvent = getDOMReference(domElement, MESSAGE_LOW_NOTIFICATION_EVENT, Event.class);
		message.setLowNotificationEvent(newEvent);

		newTask = getDOMReference(domElement, MESSAGE_LOW_NOTIFICATION_TASK, Task.class);
		message.setLowNotificationTask(newTask);
		
		message.setLowNotificationCallbackRoutineName(domElement.getAttribute(MESSAGE_LOW_NOTIFICATION_CALLBACK_ROUTINE_NAME));	
		message.setLowNotificationFlagName(domElement.getAttribute(MESSAGE_LOW_NOTIFICATION_FLAG_NAME));		
		
		message.setDeviceName(domElement.getAttribute(MESSAGE_DEVICE_NAME));
		message.setDeviceOptions(domElement.getAttribute(MESSAGE_DEVICE_OPTIONS));
		
		for ( Message next : this.<Message>getDOMReferences(domElement, MESSAGE_HIGH_CALLBACK_MESSAGES, Message.class)) {
			message.addHighCallbackMessage(next);
		}
		
		for ( Message next : this.<Message>getDOMReferences(domElement, MESSAGE_LOW_CALLBACK_MESSAGES, Message.class)) {
			message.addLowCallbackMessage(next);
		}		
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Nm}
	 * 
	 * @see #populateNmDOM(Nm)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param nm the {@link Nm} to be setup using the extracted information
	 */		
	private void processNmDOM(Element domElement, Nm nm) {
		
		processModelElementDOM(domElement, nm);		// ensure "name" and "description" setup correctly
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Os}
	 * 
	 * @see #populateOsDOM(Os)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param os the {@link Os} to be setup using the extracted information
	 */			
	private void processOsDOM(Element domElement, Os os) {
		
		processModelElementDOM(domElement, os);		// ensure "name" and "description" setup correctly
		
		StatusKind newStatus = StatusKind.get(domElement.getAttribute(OS_STATUS));
		
		if ( newStatus != null ) {
			os.setStatus(newStatus);
		}
		
		os.setStartupHook(getBooleanAttribute(domElement, OS_STARTUP_HOOK));
		os.setErrorHook(getBooleanAttribute(domElement, OS_ERROR_HOOK));
		os.setShutdownHook(getBooleanAttribute(domElement, OS_SHUTDOWN_HOOK));
		os.setPreTaskHook(getBooleanAttribute(domElement, OS_PRETASK_HOOK));
		os.setPostTaskHook(getBooleanAttribute(domElement, OS_POSTTASK_HOOK));
		os.setUseGetServiceId(getBooleanAttribute(domElement, OS_GETSERVICE_ID));
		os.setUseParameterAccess(getBooleanAttribute(domElement, OS_PARAMATER_ACCESS));
		os.setUseResScheduler(getBooleanAttribute(domElement, OS_USE_RES_SCHEDULER));
		os.setProtectionHook(getBooleanAttribute(domElement, OS_PROTECTION_HOOK));
			
		String newScalabilityClassName = getAutoAttribute(domElement, OS_SCALABILITY_CLASS);
		
		if ( newScalabilityClassName != null ) {
			ScalabilityClassKind newScalabilityClass = ScalabilityClassKind.get(newScalabilityClassName);
			
			if ( newScalabilityClass != null ) {
				os.setScalabilityClass(newScalabilityClass);
			}
		}	
		else {
			os.setAutoScalabilityClass(true);
		}
		
		Long newPretaskHookStackSize = getAutoLongAttribute(domElement, OS_PRETASK_HOOK_STACKSIZE);
		
		if ( newPretaskHookStackSize != null ) {
			os.setPreTaskHookStackSize(newPretaskHookStackSize);
		}
		else {
			os.setAutoPreTaskHookStackSize(true);
		}
		
		Long newPosttaskHookStackSize = getAutoLongAttribute(domElement, OS_POSTTASK_HOOK_STACKSIZE);
		
		if ( newPosttaskHookStackSize != null ) {
			os.setPostTaskHookStackSize(newPosttaskHookStackSize);
		}
		else {
			os.setAutoPostTaskHookStackSize(true);
		}		
				
		os.setStackChecking(getBooleanAttribute(domElement, OS_STACK_MONITORING));
		os.setRestartable(getBooleanAttribute(domElement, OS_IS_RESTARTABLE));
		os.setOscFailureHandled(getBooleanAttribute(domElement, OS_OSC_FAILURE_HANDLED));
		os.setAddrErrorHandled(getBooleanAttribute(domElement, OS_ADDR_ERROR_HANDLED));
		os.setMathErrorHandled(getBooleanAttribute(domElement, OS_MATH_ERROR_HANDLED));		
	}		
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Resource}
	 * 
	 * @see #populateResourceDOM(Resource)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param resource the {@link Resource} to be setup using the extracted information
	 */		
	private void processResourceDOM(Element domElement, Resource resource) {
		
		processModelElementDOM(domElement, resource);		// ensure "name" and "description" setup correctly
		
		ResourceKind newResourceProperty = ResourceKind.get(domElement.getAttribute(RESOURCE_PROPERTY));
		
		if ( newResourceProperty != null ) {
			resource.setResourceProperty(newResourceProperty);	// only write data member if value known
		}
		
		Resource newLinkedResource = getDOMReference(domElement, RESOURCE_LINKED_RESOURCE, Resource.class);
		
		resource.setLinkedResource(newLinkedResource);
	}	

	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Runnable}
	 * 
	 * @see #populateRunnableDOM(Runnable)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param runnable the {@link Runnable} to be setup using the extracted information
	 */		
	private void processRunnableDOM(Element domElement, Runnable runnable) {
		
		processModelElementDOM(domElement, runnable);		// ensure "name" and "description" setup correctly
		
		Long newStacksize = getAutoLongAttribute(domElement, RUNNABLE_STACKSIZE);
		
		if ( newStacksize != null ) {
			runnable.setStackSize(newStacksize);
		}
		else {
			runnable.setAutoStackSize(true);
		}
		
		runnable.setPriority(getLongAttribute(domElement, RUNNABLE_PRIORITY));
		runnable.setTimingProtection(getBooleanAttribute(domElement, RUNNABLE_TIMING_PROTECTION));
		runnable.setExecutionBudget(getBigIntAttribute(domElement, RUNNABLE_EXECUTION_BUDGET));
		runnable.setTimeLimit(getBigIntAttribute(domElement, RUNNABLE_TIME_LIMIT));
		
		for (Resource next : this.<Resource>getDOMReferences(domElement, RUNNABLE_RESOURCES, Resource.class) ) {
			runnable.addResource(next);
		}

		for (Message next : this.<Message>getDOMReferences(domElement, RUNNABLE_ACCESSED_MESSAGES, Message.class) ) {
			runnable.addAccessedMessage(next);
		}
		
		// Process the LockingTimes
		for ( Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling() ) {
						
			if ( child.getNodeType() == Node.ELEMENT_NODE ) {
				// found an element node 
				Element childElement = (Element)child;
				
				if (childElement.getNodeName().equalsIgnoreCase(RUNNABLE_LOCKING_TIME)) {
					// found a locking time element
					LockingTime lockingTime = runnable.addLockingTime();
					
					LockingTimeKind newType = LockingTimeKind.get(childElement.getAttribute(LOCKINGTIME_TYPE));
					
					if ( newType != null ) {
						lockingTime.setLockType(newType);
					}
					
					lockingTime.setResourceLockTime(getBigIntAttribute(childElement, LOCKINGTIME_RESOURCE_LOCKTIME));
					lockingTime.setOSInterruptLockTime(getBigIntAttribute(childElement, LOCKINGTIME_OSINTERRUPT_LOCKTIME));				
					lockingTime.setAllInterruptLockTime(getBigIntAttribute(childElement, LOCKINGTIME_ALLINTERRUPT_LOCKTIME));
					
					Resource newResource = getDOMReference(childElement, LOCKINGTIME_LOCKED_RESOURCE, Resource.class);
					lockingTime.setResource(newResource);	
				}
			}
		}		
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link ScheduleTable}
	 * 
	 * @see #populateScheduleTableDOM(ScheduleTable)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param scheduleTable the {@link ScheduleTable} to be setup using the extracted information
	 */		
	private void processScheduleTableDOM(Element domElement, ScheduleTable scheduleTable) {
		
		processModelElementDOM(domElement, scheduleTable);		// ensure "name" and "description" setup correctly
		
		
		Counter newCounter = getDOMReference(domElement, SCHEDULETABLE_COUNTER, Counter.class);
		scheduleTable.setCounter(newCounter);
		
		for ( AppMode next : this.<AppMode>getDOMReferences(domElement, SCHEDULETABLE_APPMODES, AppMode.class) ) {
			scheduleTable.addAppMode(next);
		}		
		
		scheduleTable.setAutostart(getBooleanAttribute(domElement, SCHEDULETABLE_AUTOSTART));
		scheduleTable.setAutostartOffset(getBigIntAttribute(domElement, SCHEDULETABLE_AUTOSTART_OFFSET));
		scheduleTable.setPeriodic(getBooleanAttribute(domElement, SCHEDULETABLE_PERIODIC));

		scheduleTable.setLength(getBigIntAttribute(domElement, SCHEDULETABLE_LENGTH));
		scheduleTable.setLocalToGlobalTimeSync(getBooleanAttribute(domElement, SCHEDULETABLE_LOCAL_TO_GLOBAL_TIME_SYNC));
		
		SyncStrategyKind newSyncStrategy = SyncStrategyKind.get(domElement.getAttribute(SCHEDULETABLE_SYNC_STRATEGY));
		
		if ( newSyncStrategy != null ) {
			scheduleTable.setSyncStrategy(newSyncStrategy);
		}
		
		scheduleTable.setMaxIncrease(getBigIntAttribute(domElement, SCHEDULETABLE_MAX_INCREASE));
		scheduleTable.setMaxDecrease(getBigIntAttribute(domElement, SCHEDULETABLE_MAX_DECREASE));
		scheduleTable.setMaxIncreaseAsync(getBigIntAttribute(domElement, SCHEDULETABLE_MAX_INCREASE_ASYNC));
		scheduleTable.setMaxDecreaseAsync(getBigIntAttribute(domElement, SCHEDULETABLE_MAX_DECREASE_ASYNC));
		scheduleTable.setPrecision(getBigIntAttribute(domElement, SCHEDULETABLE_PRECISION));
		
		// Process the ScheduleTableAction instances
		for ( Node child = domElement.getFirstChild(); child != null; child = child.getNextSibling() ) {
						
			if ( child.getNodeType() == Node.ELEMENT_NODE ) {
				// found an element node 
				Element childElement = (Element)child;
				
				if (childElement.getNodeName().equalsIgnoreCase(SCHEDULETABLE_ACTION)) {
					// found an action element
					ScheduleTableAction action = scheduleTable.addAction();

					ScheduleTableActionKind newAction = ScheduleTableActionKind.get(childElement.getAttribute(SCHEDULETABLE_ACTION_EXPIRY_ACTION));
					
					if ( newAction != null ) {
						action.setAction(newAction);
					}
					
					action.setOffset(getBigIntAttribute(childElement, SCHEDULETABLE_ACTION_OFFSET));
					
					action.setActionCallbackName(childElement.getAttribute(SCHEDULETABLE_ACTION_CALLBACK_NAME));
					
					Counter newIncCounter = getDOMReference(childElement, SCHEDULETABLE_ACTION_INCREMENTED_COUNTER, Counter.class);
					action.setIncrementedCounter(newIncCounter);
					
					Event newEvent = getDOMReference(childElement, SCHEDULETABLE_ACTION_SET_EVENT, Event.class);
					action.setEvent(newEvent);

					Task newTask = getDOMReference(childElement, SCHEDULETABLE_ACTION_ACTIVATED_TASK, Task.class);
					action.setTask(newTask);
				}
			}
		}
	}	
	
	/**
	 * Extracts the appropriate attribute information from the specified XML {@link Element} and uses
	 * it to setup the given {@link Task}
	 * 
	 * @see #populateTaskDOM(Task)
	 * 
	 * @param domElement the XML {@link Element} from which to extract information
	 * @param task the {@link Task} to be setup using the extracted information
	 */			
	private void processTaskDOM(Element domElement, Task task) {
		
		processRunnableDOM(domElement, task);		// ensure Runnable info. setup correctly
		
		ScheduleKind newSchedule = ScheduleKind.get(domElement.getAttribute(TASK_SCHEDULE));
		
		if ( newSchedule != null ) {
			task.setSchedule(newSchedule);
		}

		task.setActivation(getLongAttribute(domElement, TASK_ACTIVATION));		
		task.setAutostart(getBooleanAttribute(domElement, TASK_AUTOSTART));
		task.setTimeFrame(getBigIntAttribute(domElement, TASK_TIME_FRAME));
		
		
		for ( AppMode next : this.<AppMode>getDOMReferences(domElement, TASK_APPMODES, AppMode.class) ) {
			task.addAppMode(next);
		}			
		
		for ( Event next : this.<Event>getDOMReferences(domElement, TASK_EVENTS, Event.class) ) {
			task.addEvent(next);
		}		
	}
	
	
	/*---------------------------------------------------------------------------------------------------
	 * Public API to the serializer
	 */	
	
	/**
	 * Uses the given OSModel Cpu to export the model in XML format.
	 * 
	 * No constraint checks are performed prior to generation, i.e. the model is taken "as-is".
	 * 
	 * @param osModel the OSModel Cpu that contains the model from which to export
	 * @param fileName the name of the file to use for output for the generated XML, if null then outputs to stdout
	 * @param logger the logger output
	 */
	public void exportXML(Cpu osModel, String fileName, PrintWriter logger) {
		
		// Get a document builder factor instance
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder docBuilder = null;
		
		// create a new DocumentBuilder using the factory
		try {
			docBuilder = dbf.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			
			logger.println("XML Exporter: no system generated due to XML DOM creation failure.");
			//e.printStackTrace(logger);
			return;
		}
		
		// Use the DocumentBuilder to create a new DOM
		doc = docBuilder.getDOMImplementation().createDocument(null, XML_ROOT_ELEMENT, null);
		
		// Ask the root of the OS model (Cpu) to populate the DOM
		Element cpuElement = populateCpuDOM(osModel); 
		
		doc.getDocumentElement().appendChild(cpuElement);	
			
		// Now the DOM exists and has been populated, output using XSLT
		TransformerFactory tf = TransformerFactory.newInstance();
		
		try {
			Transformer output = tf.newTransformer();
			
			if ( fileName != null ) {
				// filename given, so output to that file
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
				
				output.transform(new DOMSource(doc), new StreamResult(writer));
			}
			else {
				output.transform(new DOMSource(doc), new StreamResult(System.out));
			}
		}
		catch (TransformerException e) {
			logger.println("XML Exporter: no system generated due to tranform output failure.");	
		} catch (IOException e) {
			logger.println("XML Exporter: no system generated due to I/O failure.");
		}
	}	
	
	/**
	 * Imports the named XML file, into the given OS Model.
	 * 
	 * If a fatal error (generally syntax type errors) is found during the parse operation the importation is NOT performed.
	 * 
	 * The given OS Model should be empty when this method is called.
	 * 
	 * @param osModel the root Cpu object of the OS Model to be populated during the import
	 * @param fileName the filename of the file to be parsed
	 * @param output the PrintWriter to use when generating import information (if null none generated)
	 * @return success flag, true if import done (even with semantic errors), else false (i.e. file not found, or syntax error)
	 */	
	public boolean importXML(Cpu osModel,String fileName,PrintWriter output) {
		
		// XML is parsed by XML parser creating a DOM
		
		// Get a document builder factor instance
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder docBuilder = null;
		
		// create a new DocumentBuilder using the factory
		try {
			docBuilder = dbf.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			
			if ( output != null ) {
				output.println("XML Importer: Error due to XML DOM creation failure.");
			}
		}
		
		// Use the DocumentBuilder to parse the XML and create a DOM
		try {
			
			doc = docBuilder.parse(fileName);
			
		} catch (SAXException e) {
			
			if( output!=null ) {
				output.println("XML Importer: "+e.getMessage());
			}
			
		} catch (IOException e) {
			
			if ( output != null ) {
				output.println("XML Importer: Error due to File I/O failure.");
			}
			
		}
		
		if ( doc != null ) {
			// Document opened and parsed ok

			// Ask the root osModel Cpu to interrogate via the XMLExporter interface implemented by the OSModelElements.	
			Element rootElement = doc.getDocumentElement();
			
			if ( rootElement != null && XML_ROOT_ELEMENT.equalsIgnoreCase(rootElement.getNodeName()) ) {
				// root element found and has the correct name
				
				Node node = rootElement.getFirstChild();
				
				if ( node.getNodeType() == Node.ELEMENT_NODE && osModel.getClassName().equalsIgnoreCase(node.getNodeName()) ) {
					// <cpu> element found, so can now ask the osModel root element to do rest of the work
							
					// Create the required (ID->OSModelElement) Map
					domMap = new HashMap<Long, OSModelElement>();
					
					processCpuDOM((Element)node, osModel);
					
					domMap = null;	// map no longer required, so allow gc if required
					
					return true;	// all ok
				}
				else {
					if ( output != null ) {
						output.println("XML Importer: Error due incorrect or missing CPU element, expected <"+osModel.getClassName()+">");
					}
				}
			}
			else {
				if ( output != null ) {
					output.println("XML Importer: Error due incorrect or missing root element, expected <"+XML_ROOT_ELEMENT+">");
				}				
			}
		}
		
		return false;
	}	
	
}
