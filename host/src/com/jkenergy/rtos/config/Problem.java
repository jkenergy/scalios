package com.jkenergy.rtos.config;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/Problem.java $
 * 
 */

/**
 * The Problem class is used to allow recording of broken constraint information during model checking.
 *
 * @see Checkable
 * 
 * @author Mark Dixon
 * 
 */
public class Problem {

	// Constants to represent available severity levels
	public static final int UNKNOWN = 0;
	public static final int ERROR = 1;
	public static final int WARNING = 2;
	public static final int INFORMATION = 3;
		
	/**
	 * Constant that represents an unspecified line number
	 */
	public static final int UNSPECIFIED = 0;
	
	/**
	 * The Object for which problem has been reported
	 */
	private Object problemObject;
	
	/**
	 * The severity level of the problem
	 */
	private int level=UNKNOWN;
	
	/**
	 * The textual description of the problem
	 */
	private String description;
	
	/**
	 * 	The textual URI of the source of the problem, e.g. file name
	 */
	private String uri;
	
	/**
	 * The line number of the problem
	 * 
	 * @see #UNSPECIFIED
	 */
	private int lineNo = UNSPECIFIED; 
	
	
	/**
	 * @return the line number of the problem
	 */
	public int getLineNo() {
		return lineNo;
	}


	/**
	 * @param lineNo the line number of the problem
	 */
	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}	

	
	/**
	 * @return the textual URI of the source of the problem
	 */
	public String getUri() {
		return uri;
	}


	/**
	 * @param uri the textual URI of the source of the problem
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the Object for which problem has been reported
	 */
	protected Object getProblemObject() {
		return problemObject;
	}

	
	/**
	 * 
	 * @return true if the problem was fatal, i.e. serious enough to prevent system generation.
	 */
	public boolean isFatalProblem() {
		return ( level == ERROR );	// at the moment ERROR is the only fatal level
	}
	
	/**
	 * 
	 * @return a textual representation of the problem 
	 */
	public String getMessage() {
		
		String msg=description;
			
		if ( level==WARNING )
			msg = "Warning: "+msg;

		else if ( level==ERROR )
			msg = "Error: "+msg;		
		
		else if ( level==INFORMATION )
			msg = "Information: "+msg;			
		
		if ( lineNo!=UNSPECIFIED )
			msg +=", at line "+String.valueOf(lineNo);
		
		if ( uri!=null )
			msg+=" of "+uri;
		
		//if ( problemObject!=null )
		//	msg+=" :"+problemObject.toString();
		
		return msg;
	}
	
	
	/**
	 * @param problemObject the Object for which the problem has been reported (may be null)
	 * @param level the severity of the problem
	 * @param description the textual description of the problem
	 */
	public Problem(Object problemObject, int level,String description) {
		this.problemObject=problemObject;
		this.level=level;
		this.description=description;
	}

	/**
	 * @param problemObject the Object for which the problem has been reported (may be null) 
	 * @param level the severity of the problem
	 * @param description the textual description of the problem
	 * @param uri the URI of the source of the problem
	 */
	public Problem(Object problemObject,int level,String description,String uri) {
		this.problemObject=problemObject;
		this.level=level;
		this.description=description;
		this.uri=uri;
	}

	/**
	 * @param problemObject the Object for which the problem has been reported (may be null) 
	 * @param level the severity of the problem
	 * @param description the textual description of the problem
	 * @param uri the URI of the source of the problem
	 * @param lineNo the line number within the URI at which the problem occurred
	 */	
	public Problem(Object problemObject,int level,String description,String uri, int lineNo) {
		this.problemObject=problemObject;
		this.level=level;
		this.description=description;
		this.uri=uri;
		this.lineNo=lineNo;		
	}
	
	/**
	 * @param level the severity of the problem
	 * @param description the textual description of the problem
	 * @param lineNo the line number within the URI at which the problem occurred
	 */
	public Problem(int level,String description,int lineNo) {
		this.level=level;
		this.description=description;
		this.lineNo=lineNo;
	}
	
	/**
	 * @param level the severity of the problem
	 * @param description the textual description of the problem
	 */
	public Problem(int level,String description) {
		this.level=level;
		this.description=description;
	}

}
