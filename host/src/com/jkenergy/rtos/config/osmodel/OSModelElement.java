package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/OSModelElement.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

/**
 * Abstract base class to all SubClasses that exist within an OS Model.<br><br>
 * 
 * Declared as implementing the {@link Checkable} interface which allows all SubClasses to override
 * the constraint checking methods and provide their own specific implementations.
 * 
 * @author Mark Dixon
 *
 */
public abstract class OSModelElement implements Checkable {
	

	/**
	 * ID to assign to the next OSModelElement instance that is created
	 */
	private static long nextID = 0;
	
	
	/**
	 * The unique ID of the OSModelElement
	 */
	private long id;
	
		
	/**
	 * Constant to represent UINT64 maximum value (0xffffffffffffffff)
	 */
	//private final static BigInteger MAX_UINT64 = BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE);
	
	/**
	 * Constant to represent UINT32 maximum value
	 */	
	//private final static long MAX_UINT32=0xffffffffL;		


	/**
	 * Checks if the given value is within the range of a UINT32
	 * @param value the value to be checked
	 * @return true if within range,else false
	 */
	//public final static boolean isUINT32(long value) {
	//	return ( value>=0 && value<=MAX_UINT32 );
	//}
		
	/**
	 * Checks if the given value is within the range of a UINT64
	 * @param value the value to be checked
	 * @return true if within range,else false
	 */
	//public final static boolean isUINT64(BigInteger value) {
		
	//	if ( value != null ) {
	//		return ( value.compareTo(BigInteger.ZERO)>=0 && value.compareTo(MAX_UINT64)<=0 );
	//	}
		
	//	return false;
	//}		
	
	
	/**
	 * The name of the element
	 * @see #getName()
	 */
	private String name;
	
	/**
	 * The description of the element
	 * @see #getDescription()
	 */
	private String description=null;
	
	/**
	 * The Cpu that contains this element
	 * @see #getCpu()
	 */
	private Cpu cpu;
	
	
	/**
	 * The collection of Applications to which the OS Object is assigned
	 */	
	private Set<Application> owningApplications = new LinkedHashSet<Application>();			/* $Req: AUTOSAR $ */	
	
	/**
	 * The collection of Applications that may access the OS object
	 */
	private Set<Application> accessingApplications = new LinkedHashSet<Application>();		/* $Req: AUTOSAR $ */
	
	/**
	 * Relates attribute names to associated {@link AttributeDescription} instances.
	 * Allows the storing of description for both single and multi (collection) type attributes.
	 */
	private Map<String, AttributeDescription> attribDescriptions = new HashMap<String, AttributeDescription>();
	
	/**
	 * Returns the class name (element type name) of the OSModelElement, e.g. Task, Resource, Event
	 * @return the class name of the OSModelElement
	 */
	public String getClassName() {
		return this.getClass().getSimpleName();
	}
		
	
	/**
	 * Sets the name of the element
	 * @param newName the new name of the element
	 */
	public void setName(String newName) {
		
		name=newName;
	}	
	
	/**
	 * 
	 * @return the name of the element
	 */
	public String getName() {
		
		return name;
	}
	
	/**
	 * Sets the description associated with the element
	 * @param newDescription
	 */
	public void setDescription(String newDescription) {
		
		description = newDescription;
	}	
	
	/**
	 * @return Description associated with the element
	 */
	public String getDescription() {
		
		return description;
	}	
	
	/**
	 * Add an attribute description for the named attribute.
	 * If a description already exists for the named attribute then that is overwritten.
	 * 
	 * @param attribName attribName the name of the attribute for which the description is provided
	 * @param description the description to associated with the attribute
	 */
	public void addAttribDescription(String attribName, String description) {
		
		if ( description != null ) {
			attribDescriptions.put(attribName, new AttributeDescription(attribName, description));
		}
		else {
			attribDescriptions.remove(attribName);
		}
	}
		
	/**
	 * Gets an attribute description for the named attribute.
	 * 
	 * @param attribName the name of the attribute for which the description is required
	 * @return the description, null if attribute does not have a description
	 */
	public String getAttribDescription(String attribName) {
		
		AttributeDescription attribDesc = attribDescriptions.get(attribName);
		
		if ( attribDesc != null ) {
			return attribDesc.getDescription();
		}
		return null;
	}
	
	/**
	 * Add an attribute description for the named attribute.
	 * 
	 * This version creates a specific index for the description, allowing several descriptions to be
	 * associated with the same attribute.
	 * 
	 * If the named attribute, with the given index already has a description then this is overwritten.
	 * 
	 * @param attribName attribName the name of the attribute for which the description is provided
	 * @param index the index that identifies the specific description provided
	 * @param description the description to associated with the attribute, with the given index
	 */
	public void addMultiAttribDescription(String attribName, String description, int index) {
		
		AttributeDescription attribDesc = attribDescriptions.get(attribName);
		
		if ( attribDesc != null ) {
			attribDesc.addMultiAttribDescription(description, index);
		}
		else {
			if ( description != null ) {
				attribDescriptions.put(attribName, new AttributeDescription(attribName, description, index));
			}
		}
	}
	
	/**
	 * Gets an attribute description for the named attribute. This version looks
	 * for a specific index for the description, allowing several descriptions to be
	 * associated with the same attribute.
	 * 
	 * @param attribName the name of the attribute for which the description is required
	 * @param index the index that identifies the specific description required
	 * @return the description, null if attribute does not have a description
	 */
	public String getMultiAttribDescription(String attribName, int index) {
		AttributeDescription attribDesc = attribDescriptions.get(attribName);
		
		if ( attribDesc != null ) {
			return attribDesc.getMultiDescription(index);
		}
		return null;
	}	
	
	/**
	 * 
	 * @return the AttributeDescription instances
	 */
	public Collection<AttributeDescription> getAttribDescriptions() {
		return attribDescriptions.values();
	}
	
	/**
	 * Sets the owning Cpu of the element
	 * @param newCpu the owning Cpu of the element
	 */
	public void setCpu(Cpu newCpu) {
		
		cpu=newCpu;
	}
	
	/**
	 * Gets the owning Cpu of the element
	 * @return the cpu that owns the element
	 */
	public Cpu getCpu() {
		
		return cpu;
	}		
	
	
	/**
	 * Adds an Application to the collection of applications to which this element is assigned.
	 * This method creates a two way relationship.
	 * 
	 * @param application the Application to be added
	 */
	public void addOwningApplication(Application application) {
				
		if ( application!=null ) {
			if ( owningApplications.add(application) )
				application.addAssignedElement(this);		// inform the Application that this element is assigned to it
		}		
	}	
	
	/**
	 * Returns the collection of Applications to which the element is assigned.
	 * @return Collection of Applications
	 */
	public Collection<Application> getOwningApplications() {
				
		return owningApplications;
	}	
	
	
	/**
	 * Adds the given Application to the collection of applications that may access the element at run-time
	 * This method creates a two way relationship.
	 * 
	 * @param application the Application to be added to the collection of accessing applications
	 */
	public void addAccessingApplication(Application application) {
		
		if ( application!=null ) {
			if ( accessingApplications.add(application) )
				application.addAccessibleElement(this);	// inform the Application that is may access this element
		}		
	}
	
	/**
	 * @return the Collection of Application objects that may access the element at run-time.
	 */
	public Collection<Application> getAccessingApplications() {
		
		return accessingApplications;
	}
	

	
	/** Define implementation of check method that is declared within the {@link Checkable} interface
	 * 
	 * @param problems List of {@link Problem} objects, should be appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		// Do check of this element

		// [1] The name value must conform to ANSI C identifier rules.
		validateIdentifierName(problems, name);
		
		// [2] The element can have at most one owningApplications. [AUTOSAR]
		if ( owningApplications.size() > 1 ) {
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' is associated with more than one Application"));				
		}
	}	


	
	/**
	 * Validates a textual name to ensure it is a valid C type identifier.
	 * 
	 * @see NameChecker
	 * 
	 * @param problems the List of {@link Problem} objects, that should be appended to when problems found
	 * @param name the name to be checked
	 */
	void validateTypeName(List<Problem> problems, String name) {
		
		if ( NameChecker.isValidTypeName(name) ) {
			
			// type name has valid characters
			
			name = name.trim();		// trim to remove trailing spaces to ensure match with reserved names
			
			// Checks not as strict as validateIdentifierName, since must allow C type names, spaces etc, e.g. int
			if ( NameChecker.usesReservedNamespace(name) ) {
				// identifier uses reserved namespaces prefix
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a namespace prefix reserved for OS usage"));
			}
			else if ( NameChecker.usesOSKeyword(name) ) {
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a keyword defined by the OS"));
			}			
			else if ( NameChecker.usesCKeyword(name) ) {
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a keyword defined by target language"));
			}			
		}
		else {
			// the name is is not a valid type 
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an illegal typename '"+name+"'"));
		}		
	}
	

	
	/**
	 * Validates a textual name to ensure it is a valid C identifier.
	 * 
	 * The name is validated in several different ways, inlcuding identifier rules,namespace clash tests and keyword clashes.
	 * 
	 * @see NameChecker
	 * 
	 * @param problems the List of {@link Problem} objects, that should be appended to when problems found
	 * @param name the name to be checked
	 */
	void validateIdentifierName(List<Problem> problems, String name) {
		
		if ( NameChecker.isValidIdentifier(name) ) {
			
			// identifier has valid characters			
			name = name.trim();		// trim to remove trailing spaces to ensure match with reserved names
			
			if ( NameChecker.usesReservedNamespace(name) ) {
				// identifier uses reserved namespaces prefix
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a namespace prefix reserved for OS usage"));
			}	
			else if ( NameChecker.usesOSDatatypeName(name) ) {
				// identifier uses an OS datatype name
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a datatype name already declared by the OS"));
			}
			else if ( NameChecker.usesCDatatypeName(name) ) {
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a datatype name already declared by the target language"));
			}			
			else if ( NameChecker.usesOSKeyword(name) ) {
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a keyword defined by the OS"));
			}			
			else if ( NameChecker.usesCKeyword(name) ) {
				problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an identifier '"+name+"' that uses a keyword defined by target language"));
			}			
		}
		else {
			// the name is is not a valid identifier
			problems.add(new Problem(Problem.ERROR, getClassName()+" '"+getName()+"' has an illegal identifier '"+name+"'"));
		}
	}	
	
	/**
	 * Validates a list of names to ensure they are valid C identifiers.
	 * @see #validateIdentifierName(List, String)
	 * @param problems the List of {@link Problem} objects, that should be appended to when problems found
	 * @param names the List of names to be checked
	 */
	void validateIdentifierNames(List<Problem> problems, Collection<String> names) {

		
		for ( String nextName : names ) {
			validateIdentifierName(problems, nextName);
		}
	}	
		
	/**
	 * Return the unique ID of the OSModelElement
	 * 
	 * @return the unique ID of the OSModelElement
	 */
	public long getID() {
		return id;
	}
	
	/**
	 * constructor
	 * 
	 * @param cpu the Cpu that owns the element
	 * @param name the name of the element
	 */
	protected OSModelElement(Cpu cpu, String name) {
		this.cpu=cpu;
		this.name=name;
		
		id = nextID++;	// assign unique id to the element
	}
}
