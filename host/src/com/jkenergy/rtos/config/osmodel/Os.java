package com.jkenergy.rtos.config.osmodel;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/osmodel/Os.java $
 * 
 */

import java.util.List;

import com.jkenergy.rtos.config.Problem;

/**
 * A SubClass of OSModelElement that models OS wide configuration details.<br><br>
 * 
 * @author Mark Dixon
 *
 */
public class Os extends OSModelElement {
	
	
	/**
	 * The default stacksize of the pre/post task hooks
	 */
	private static final long DEFAULT_STACKSIZE = 0;	
	
	/**
	 * Os status specifies either standard or extended status.
	 * @see StatusKind
	 */ 
	private StatusKind status=StatusKind.STANDARD_LITERAL;	
	
	/**
	 * Startup Hook flag
	 */
	private boolean startupHook=false;
	
	/**
	 * Error hook flag
	 */
	private boolean errorHook=false;
	
	/**
	 * Shutdown hook flag
	 */
	private boolean shutdownHook=false;
	
	/**
	 * Pre-task hook flag
	 */
	private boolean preTaskHook=false;
	
	/**
	 * Post-task hook flag
	 */
	private boolean postTaskHook=false;
	
	/**
	 * Use Get Service ID flag
	 */
	private boolean useGetServiceId=false;
	
	/**
	 * Use Parameter Access flag
	 */
	private boolean useParameterAccess=false;
	
	/**
	 * Use Rescheduler flag
	 */	
	private boolean useResScheduler=false;
	
	/**
	 * Protection Hook flag
	 */
	private boolean protectionHook=false;  /* $Req: AUTOSAR $ */
	
	/**
	 * Scalability class.
	 * @see ScalabilityClassKind
	 */ 
	private ScalabilityClassKind scalabilityClass=ScalabilityClassKind.SC1_LITERAL;	/* $Req: AUTOSAR $ */
		
	/**
	 * Flag to indicate that the scalabilityClass should be automatically calculated
	 */
	private boolean autoScalability;	/* $Req: AUTOSAR $ */
	
	/**
	 * Stack Monitoring flag
	 */	
	private boolean isStackCheckingEnabled=false;	/* $Req: AUTOSAR $ */	
	
	/**
	 * Restart flag
	 */	
	private boolean isRestartable=false;	
	
	/**
	 * Handle Osc Failure flag
	 */
	private boolean oscFailureHandled;
	
	/**
	 * Handle Addr Error flag
	 */
	private boolean addrErrorHandled;	
	
	/**
	 * Handle Math Error flag
	 */
	private boolean mathErrorHandled;	
	
	/**
	 * The size of the stack allocated to the pretask hook
	 */
	private long preTaskHookStackSize = DEFAULT_STACKSIZE;		
	
	/**
	 * The size of the stack allocated to the posttask hook
	 */
	private long postTaskHookStackSize = DEFAULT_STACKSIZE;	

	
	/**
	 * isAutoPreTaskHookStackSize flag that specifies whether the preTaskHookStackSize is to be automatically calculated
	 */
	private boolean isAutoPreTaskHookStackSize=false;
	
	/**
	 * isAutoPostTaskHookStackSize flag that specifies whether the postTaskHookStackSize is to be automatically calculated
	 */
	private boolean isAutoPostTaskHookStackSize=false;	
	
	
	/**
	 * @return the scalabilityClass
	 */
	public ScalabilityClassKind getScalabilityClass() {
		return scalabilityClass;
	}

	/**
	 * @param scalabilityClass the scalabilityClass to set
	 */
	public void setScalabilityClass(ScalabilityClassKind scalabilityClass) {
		this.scalabilityClass = scalabilityClass;
	}
	
	/**
	 * @param autoScalability the autoScalability flag value
	 */
	public void setAutoScalabilityClass(boolean autoScalability) {
		this.autoScalability = autoScalability;
	}
	
	/**
	 * 
	 * @return the autoScalability flag
	 */
	public boolean getAutoScalabilityClass() {
		return autoScalability;
	}
	
	/**
	 * @return true if ScalabilityClass is class 1
	 */
	public boolean isScalabilityClass1() {
		return ScalabilityClassKind.SC1_LITERAL == scalabilityClass;
	}	

	/**
	 * @return the protectionHook
	 */
	public boolean hasProtectionHook() {
		return protectionHook;
	}

	/**
	 * @param protectionHook the protectionHook to set
	 */
	public void setProtectionHook(boolean protectionHook) {
		this.protectionHook = protectionHook;
	}

	/**
	 * @return Returns the mathErrorHandled.
	 */
	public boolean isMathErrorHandled() {
		return mathErrorHandled;
	}

	/**
	 * @param handleMathError the mathErrorHandled flag value to set.
	 */
	public void setMathErrorHandled(boolean handleMathError) {
		this.mathErrorHandled = handleMathError;
	}

	/**
	 * @return Returns the addrErrorHandled.
	 */
	public boolean isAddrErrorHandled() {
		return addrErrorHandled;
	}

	/**
	 * @param handleAddrError The addrErrorHandled flag value to set.
	 */
	public void setAddrErrorHandled(boolean handleAddrError) {
		this.addrErrorHandled = handleAddrError;
	}

	/**
	 * @return Returns the oscFailureHandled.
	 */
	public boolean isOscFailureHandled() {
		return oscFailureHandled;
	}

	/**
	 * @param handleOscFailure The oscFailureHandled flag value to set.
	 */
	public void setOscFailureHandled(boolean handleOscFailure) {
		this.oscFailureHandled = handleOscFailure;
	}

	/**
	 * @return Returns the isAutoPreTaskHookStackSize.
	 */
	public boolean isAutoPreTaskHookStackSize() {
		return isAutoPreTaskHookStackSize;
	}

	/**
	 * @param isAutoPreTaskHookStackSize The isAutoPreTaskHookStackSize to set.
	 */
	public void setAutoPreTaskHookStackSize(boolean isAutoPreTaskHookStackSize) {
		this.isAutoPreTaskHookStackSize = isAutoPreTaskHookStackSize;
	}	
	
	/**
	 * @return Returns the isAutoPostTaskHookStackSize.
	 */
	public boolean isAutoPostTaskHookStackSize() {
		return isAutoPostTaskHookStackSize;
	}

	/**
	 * @param isAutoPostTaskHookStackSize The isAutoPostTaskHookStackSize to set.
	 */
	public void setAutoPostTaskHookStackSize(boolean isAutoPostTaskHookStackSize) {
		this.isAutoPostTaskHookStackSize = isAutoPostTaskHookStackSize;
	}

	/**
	 * Sets the status of the Os
	 * @param newStatus the new status of the Os
	 * @see StatusKind
	 */
	public void setStatus(StatusKind newStatus) {

		status=newStatus;
	}
	
	/**
	 * @return the status of the Os
	 * @see StatusKind
	 */
	public StatusKind getStatus() {
		return status;
	}	

	/**
	 * @return true if the os status is EXTENDED
	 */
	public boolean isExtendedStatus() {
		return StatusKind.EXTENDED_LITERAL == status;
	}
	
	/**
	 * 
	 * @param newStartupHook enable flag
	 */
	public void setStartupHook(boolean newStartupHook) {
		startupHook=newStartupHook;
	}
	
	/**
	 * 
	 * @return startupHook flag 
	 */
	public boolean getStartupHook() {
		return startupHook;
	}
	
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
	 * @param newShutdownHook enable flag
	 */
	public void setShutdownHook(boolean newShutdownHook) {
		shutdownHook=newShutdownHook;
	}
	
	/**
	 * 
	 * @return shutdownHook flag
	 */
	public boolean getShutdownHook() {
		return shutdownHook;
	}	
	
	/**
	 * 
	 * @param newPreTaskHook enable flag
	 */
	public void setPreTaskHook(boolean newPreTaskHook) {
		preTaskHook=newPreTaskHook;
	}
	
	/**
	 * 
	 * @return preTaskHook flag
	 */
	public boolean getPreTaskHook() {
		return preTaskHook;
	}	
	
	/**
	 * 
	 * @param newPostTaskHook enable flag
	 */
	public void setPostTaskHook(boolean newPostTaskHook) {
		postTaskHook=newPostTaskHook;
	}

	/**
	 * 
	 * @return postTaskHook flag
	 */
	public boolean getPostTaskHook() {
		return postTaskHook;
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
	 * @param newUseRescheduler enable flag
	 */
	public void setUseResScheduler(boolean newUseRescheduler) {
		useResScheduler=newUseRescheduler;
	}
	
	/**
	 * 
	 * @return useResScheduler flag
	 */
	public boolean getUseResScheduler() {
		return useResScheduler;
	}	
	
	/**
	 * @return Returns the isRestartable.
	 */
	public boolean isRestartable() {
		return isRestartable;
	}

	/**
	 * @param isRestartable The isRestartable to set.
	 */
	public void setRestartable(boolean isRestartable) {
		this.isRestartable = isRestartable;
	}	
	
	/**
	 * @return Returns the isStackCheckingEnabled.
	 */
	public boolean isStackCheckingEnabled() {
		return isStackCheckingEnabled;
	}

	/**
	 * @param isStackCheckingEnabled The isStackCheckingEnabled to set.
	 */
	public void setStackChecking(boolean isStackCheckingEnabled) {
		this.isStackCheckingEnabled = isStackCheckingEnabled;
	}		
	
	/**
	 * @return Returns the postTaskHookStackSize.
	 */
	public long getPostTaskHookStackSize() {
		return postTaskHookStackSize;
	}

	/**
	 * @param postTaskHookStackSize The postTaskHookStackSize to set.
	 * 
	 */
	public void setPostTaskHookStackSize(long postTaskHookStackSize) {

		
		this.postTaskHookStackSize = postTaskHookStackSize;
	}

	/**
	 * @return Returns the preTaskHookStackSize.
	 */
	public long getPreTaskHookStackSize() {
		return preTaskHookStackSize;
	}

	/**
	 * @param preTaskHookStackSize The preTaskHookStackSize to set.
	 */
	public void setPreTaskHookStackSize(long preTaskHookStackSize)  {

		this.preTaskHookStackSize = preTaskHookStackSize;
	}
	
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems, deepCheck);
		
		// Do check of this element	
		
		//[1] The scalabilityClass value must equal SC1[AUTOSAR] (since only SC1 is supported).
		if ( isScalabilityClass1() == false ) {
			problems.add(new Problem(Problem.WARNING, "OS '"+getName()+"' specifies unsupported scalability class, only class 1 is supported"));
		}

		//[2] If the protectionHook value equals false then no applications within the owning CPU may define a restartedTask.[AUTOSAR]
		if ( protectionHook == false ) {
			for ( Application next : getCpu().getApplications() ) {
				if ( next.getRestartedTask() != null ) {
					problems.add(new Problem(Problem.WARNING, "OS '"+getName()+"' has protection hook turned off, but application '"+next.getName()+"' defines a restarted task"));
				}
			}
		}
		
		// [3] The preTaskHookStackSize value must be greater than, or equal to, zero. [error] [EXTENSION]
		if ( preTaskHookStackSize <0 ) {
			problems.add(new Problem(Problem.ERROR, "OS '"+getName()+"' has an invalid (negative) pre-task hook stack size value"));
		}

        // [4] The postTaskHookStackSize value must be greater than, or equal to, zero. [error] [EXTENSION]
		if ( postTaskHookStackSize < 0 ) {
			problems.add(new Problem(Problem.ERROR, "OS '"+getName()+"' has an invalid (negative) post-task hook stack size value"));
		}	
	}




	
	
	public Os(Cpu cpu,String name) {
		super(cpu,name);
	}
}
