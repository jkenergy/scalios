package com.jkenergy.rtos.config.oilmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/oilmodel/OILDefinition.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;


/**
 * A SubClass of OILNamedElement that is used to store information on ALL operating system configuration details.<br><br>
 * 
 * An instance of this class always acts as the "root" of an OIL Model. Hence, this class defines
 * references to a {@link ImplementationDefinition} instance and a {@link ApplicationDefinition} instance, which
 * contains the meta-data and configuration data imported from an OIL format configuration file, respectively. 
 * 
 * @author Mark Dixon
 *
 */
public class OILDefinition extends OILNamedElement {


	/**
	 * The version associated with this OILDefinition
	 * @see #getVersion()
	 */
	private String version;	
	
	
	/**
	 * The {@link ImplementationDefinition} contained within the OILDefinition
	 * @see #getImplDef()
	 */
	private ImplementationDefinition implementation = null;
	

	/**
	 * The {@link ApplicationDefinition} contained within the OILDefinition
	 * @see #getApplicationDef()
	 */
	private ApplicationDefinition application = null;	
	
	/**
	 * @return version associated with the element
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version associated with the element
	 * @param newVersion
	 */
	public void setVersion(String newVersion) {
		version = newVersion;
	}
	
	/**
	 * @return the {@link ImplementationDefinition} associated with the OILDefinition
	 */
	public ImplementationDefinition getImplDef() {
		return implementation;
	}	
	
	/**
	 * Sets the {@link ImplementationDefinition} associated with the OILDefinition
	 * @param newImplementation
	 */
	public void setImplDef(ImplementationDefinition newImplementation) {
		implementation=newImplementation;
	}
	
	/**
	 * @return the {@link ApplicationDefinition} associated with the OILDefinition
	 */
	public ApplicationDefinition getApplicationDef() {
		return application;
	}	
	
	/**
	 * Sets the {@link ApplicationDefinition} associated with the OILDefinition
	 * @param newApplication
	 */	
	public void setApplicationDef(ApplicationDefinition newApplication) {
		application=newApplication;
	}

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do check of this element
		
		// [1] An ImplementationDefinition must exist within the OILDefinition
		if ( implementation==null )
			problems.add(new Problem(Problem.ERROR,"No Implementation is defined within this model"));
		
		// [2] An ApplicationDefinition must exist within the OILDefinition		
		if ( application==null )
			problems.add(new Problem(Problem.ERROR,"No Application is defined within this model"));
		
		
		// Do check on contained elements
		if ( deepCheck ) {
		
			if ( implementation!=null ) {
				
				// Only do further checks if an Implementation is defined
				
				implementation.doModelCheck(problems,deepCheck);
		
				if ( application!=null )
					application.doModelCheck(problems,deepCheck);
			}
		}
	}	

	/**
	 * Returns the {@link ObjectTypeDefinition} that has the given objectType
	 * 
	 * @param objType for which the {@link ObjectTypeDefinition} is required
	 * @return the {@link ObjectTypeDefinition} that has the given objectType
	 * @see ObjectKind
	 */
	public ObjectTypeDefinition getObjectTypeDefinition(ObjectKind objType) {
	
		if ( implementation!=null )
			return implementation.getObjectTypeDefinition(objType);
		
		return null;
	}	
	
	/**
	 * 
	 */
	public OILDefinition() {
		super(0);
	}
	
	/**
	 * @param version the version of OIL within the definition
	 */
	public OILDefinition(String version) {

		super(0);
		this.version=version;
	}
}
