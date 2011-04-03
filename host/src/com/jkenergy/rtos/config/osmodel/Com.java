package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Com.java $
 * 
 */

import java.util.*;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models a COM instance within the OS.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Com extends OSModelElement {


	
	
	/**
	 * Error hook flag
	 */
	private boolean errorHook=false;
	
	/**
	 * Use Get Service ID flag
	 */
	private boolean useGetServiceId=false;
	
	/**
	 * Use Parameter Access flag
	 */
	private boolean useParameterAccess=false;	

	/**
	 * Start Com Extension flag
	 */
	private boolean startComExtension=false;	
	
	/**
	 * Set of COM application modes (names) that are supported
	 * 
	 * @see #addAppModeName(String)
	 * @see Cpu#getAllCOMAppModeNames()
	 */
	private Set<String> appModes=new LinkedHashSet<String>();
	
	/**
	 * Com status
	 */ 
	private ComStatusKind status=ComStatusKind.COMSTANDARD_LITERAL;		
	
	/**
	 * 
	 * @param newErrorHook enable flag
	 */
	public void setErrorHook(boolean newErrorHook) {
		errorHook=newErrorHook;
	}
	
	/**
	 * 
	 * @return errorHook flag
	 */
	public boolean getErrorHook() {
		return errorHook;
	}	
	
	/**
	 * 
	 * @param newUseGetServiceId enable flag
	 */
	public void setUseGetServiceId(boolean newUseGetServiceId) {
		useGetServiceId=newUseGetServiceId;
	}
	
	/**
	 * 
	 * @return useGetServiceId flag
	 */
	public boolean getUseGetServiceId() {
		return useGetServiceId;
	}	
	
	/**
	 * 
	 * @param newUseParameterAccess enable flag
	 */
	public void setUseParameterAccess(boolean newUseParameterAccess) {
		useParameterAccess=newUseParameterAccess;
	}

	/**
	 * 
	 * @return useParameterAccess flag
	 */
	public boolean getUseParameterAccess() {
		return useParameterAccess;
	}	
	
	/**
	 * 
	 * @param newStartComExtension enable flag
	 */
	public void setStartComExtension(boolean newStartComExtension) {
		startComExtension=newStartComExtension;
	}

	/**
	 * 
	 * @return startComExtension flag
	 */
	public boolean getStartComExtension() {
		return startComExtension;
	}
	
	
	/**
	 * Adds the given name to the set of COM application modes supported
	 * @param name the name of the COM application mode to be added
	 */
	public void addAppModeName(String name) {
		
		if ( name!=null ) {
			appModes.add(name);
		}
	}
	
	/**
	 * @return Set of supported application mode names
	 */
	public Collection<String> getAppModes() {
		return appModes;
	}
	
	/**
	 * Sets the status of the Com
	 * @param newStatus the new status of the Com
	 * @see ComStatusKind
	 */
	public void setStatus(ComStatusKind newStatus) {

		if ( newStatus!=null )
			status=newStatus;
		else
			status=ComStatusKind.COMSTANDARD_LITERAL;
	}
	
	/**
	 * @return the status of the Com
	 * @see ComStatusKind
	 */
	public ComStatusKind getStatus() {
		return status;
	}	
	
	/**
	 * @return true if the COM status is EXTENDED
	 */
	public boolean isExtendedStatus() {
		return ComStatusKind.COMEXTENDED_LITERAL == status;
	}	

	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element
		
		// [1] All comAppMode name values must conform to ANSI C identifier rules and must not clash with an ANSI C keywords or OS symbols [warning]
		validateIdentifierNames(problems, appModes);
				
		// [2] At least one comAppMode name must exist. 
		if ( appModes.isEmpty() ) {	
			problems.add(new Problem(Problem.INFORMATION, "COM '"+getName()+"' does not define any application modes"));			
		}
		
		// [3] All comAppMode name values must be unique when compared to each other [information]
		// Enforced by use of Set to store comAppMode names.

		// [4]  All comAppMode names values must be unique when compared to all other Object names or generated flag macro names.[error]
		if ( appModes.isEmpty() == false ) {
			
			Collection<String> usedNamesSet = new LinkedHashSet<String>();
			
			usedNamesSet.addAll(getCpu().getAllContainedElementNames());	// all object names (inc app mode names)
			usedNamesSet.addAll(getCpu().getAllFlagMacroNames());			// all flag macro names
			
			for ( String nextAppModeName : appModes ) {
				if ( usedNamesSet.contains(nextAppModeName) ) {
					problems.add(new Problem(Problem.ERROR, "COM '"+getName()+"' contains an application mode name '"+nextAppModeName+"' that clashes with another name in the system"));
				}
			}
		}
		
	}

	
	
	/**
	 * @param cpu the {@link Cpu} in which the Com object exists
	 * @param name the name of the Com object
	 */
	public Com(Cpu cpu,String name) {
		super(cpu,name);
	}	
}
