package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/OSGenerator.java $
 * 
 */

import java.io.*;

import java.util.List;
import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

/**
 * Abstract base class for all OS Generator classes.<br><br>
 * 
 * This class is declared as implementing both the {@link Generator} interface and the {@link Checkable} interface.
 * Although this class provides basic implementation of both these interfaces the SubClasses are expected to
 * provide the bulk of implementation logic for both these interfaces.<br><br>
 * 
 * The class stores references to the {@link TargetCpu} from which generation is to be performed and to a {@link PlatformInfo} instance
 * that helps define a standard set of platform specific parameters to be used during generation.<br><br>
 * 
 * A number of convenience methods are provided by this class that allow ease of textual output and formatting from within 
 * SubClass generation methods.
 * 
 * @author Mark Dixon
 *
 */
public abstract class OSGenerator implements Generator, Checkable {

	protected final static String AUTOGEN_COMMENT = "Auto-generated file.  Never edit the contents of this file";

	/**
	 * The PlatformInfo information to be used by the generator.
	 */
	protected PlatformInfo platformInfo;
	
	/**
	 * The root of the Target Model from which generation takes place
	 */
	protected TargetCpu targetModel;

	/**
	 * Current tab value (used for offset during output)
	 */
	protected int tab=0;
	
	/**
	 * Current writer been used for the target of generation
	 */
	protected PrintWriter writer;
	
	/**
	 * The pathName associated with the current writer, only valid when writer != null
	 */
	protected String writerPathName;
		
	/**
	 * Current logger been used for logging information
	 */
	private PrintWriter logger;	
	
	/**
	 * Prefix string attached to each logged message.
	 */
	private String loggerPrefix;
	
	/**
	 * Generate verbose comments flag
	 * @see #verboseComment(String)
	 */
	protected boolean verboseComments = false;
	
	
	/**
	 * @return comment start delimiter
	 */
	protected abstract String getCommentStart();

	/**
	 * @return comment end delimiter
	 */
	protected abstract String getCommentEnd();
	
	/**
	 * @return asm comment start delimiter
	 */
	protected abstract String getAsmCommentStart();

	/**
	 * @return asm comment end delimiter
	 */
	protected abstract String getAsmCommentEnd();	
	
	/**
	 * @return separator comment line
	 */	
	protected abstract String separatorComment();
	
		
	/**
	 * Outputs a separator line to the current writer
	 *
	 */
	protected void separator() {
		
		writeNLs(2);
		writeln(separatorComment());
		writeNL();
	}
	
	/**
	 * @return Returns the verboseComments.
	 */
	public boolean isVerboseComments() {
		return verboseComments;
	}

	/**
	 * @param verboseComments The verboseComments to set.
	 */
	public void setVerboseComments(boolean verboseComments) {
		this.verboseComments = verboseComments;
	}	
	
	/**
	 * @return Returns the writer.
	 */
	protected PrintWriter getWriter() {
		return writer;
	}
	
	/**
	 * Sets up the current writer to be a writer to the named file.
	 * The file is created as required.
	 * 
	 * @param pathname the path name of the output directory (if null uses current working directory)
	 * @param filename the name of the file to be associated with the writer
	 * @return the new PrintWriter
	 * @throws IOException
	 */
	protected PrintWriter setupWriter(String pathname, String filename) throws IOException {
		
		// close (and flush) any existing writer
		if ( writer != null ) {
			writer.close();
			writer = null;
			writerPathName = "";
		}
		
		if ( pathname != null ) {
			// Path name given so use the File class to construct the full path to the output file
			File file = new File(pathname, filename);
			
			// create a print writer based on the name file
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			writerPathName = file.getCanonicalPath();
		}
		else {
			// no path given so use cwd
			writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			
			writerPathName = "."+File.separator+filename;
		}
		
		return writer;
	}
	
	/**
	 * Closes the current writer then sets current writer to null
	 * This should be called to ensure output to the writer is flushed when writing has finished.
	 *
	 */
	protected void closeWriter() {
		
		if ( writer != null ) {
			writer.close();
			writer = null;
			writerPathName = "";
		}
	}
	
	
	/**
	 * Increments the tab count by 1
	 *
	 */
	protected void incTabs() {
		tab++;
	}

	/**
	 * Decrements the tab count by 1, protects from going negative.
	 *
	 */	
	protected void decTabs() {
		if (tab > 0)
			tab--;
	}	
	
	/**
	 * Resets the tabs count to 0
	 *
	 */
	protected void resetTabs() {
		tab=0;
	}	
	
	/**
	 * 
	 * @return a string representation of the current tab offset
	 */
	protected String tabs() {
		StringBuffer buffer = new StringBuffer(tab+1);
		
		int count=tab;
		
		while ((count--) > 0)
			buffer.append('\t');
		
		return buffer.toString();
	}
	
	/**
	 * Returns a comment that uses appropriate delimiters for the current concrete generator.
	 * @param comment the text part of the comment to be generated
	 * @return the full comment text using the appropriate delimiters
	 */
	protected String comment(String comment) {
		
		return getCommentStart()+" "+comment+" "+getCommentEnd();
	}
	
	/**
	 * Returns an assembly language comment that uses appropriate delimiters for the current
	 * concrete generator.
	 * @param comment the text part of the comment to be generated
	 * @return the full comment text using the appropriate assembler delimiters
	 */
	protected String asmcomment(String comment) {
		
		return getAsmCommentStart()+" "+comment+" "+getAsmCommentEnd();
	}	
	
	/**
	 * Returns a comment that uses appropriate delimiters for the current concrete generator.
	 * The comment is only returned if the {@link #verboseComments} flag is set, else returns null.
	 * @param comment
	 * @return the full verbose comment text using the appropriate delimiters
	 */
	protected String verboseComment(String comment) {
		
		return (verboseComments == true) ? comment(comment):null;
	}
	
	/**
	 * Returns an assembly language comment that uses appropriate delimiters for the current concrete generator.
	 * The comment is only returned if the {@link #verboseComments} flag is set, else returns null.
	 * @param comment
	 * @return the full verbose comment text using the appropriate delimiters
	 */
	protected String verboseAsmComment(String comment) {
		
		return (verboseComments == true) ? asmcomment(comment):null;
	}	
	
	/**
	 * Define implementation of check method that is declared within the {@link Checkable} interface
	 * 
	 * @param problems List of {@link Problem} objects, should be appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	public void doModelCheck(List<Problem> problems, boolean deepCheck)
	{
		// No generic target independent constraint checks currently exist
	}	
	
	/**
	 * Writes a line of text to the current writer (appending the required new line character)
	 * The line is prefixed with the current tabulation offset.
	 * @param text the text to be written
	 */
	protected void writeln(String text) {
		if (text != null) {
			writer.println(tabs()+text);
		}
	}
	
	/**
	 * Writes a line of text to the current writer (with no NL).
	 * The line is prefixed with the current tabulation offset.
	 * @param text the text to be written
	 */
	protected void write(String text) {
		if (text != null) {
			writer.print(tabs()+text);
		}
	}	

	/**
	 * Appends a line of text to the current writer (with no NL).
	 * The line is NOT prefixed with the current tabulation offset.
	 * @param text the text to be appended
	 */
	protected void append(String text) {
		if (text != null) {
			writer.print(text);
		}
	}	
	
	/**
	 * Writes a specified number of new line characters to the current writer
	 * @param count the number of NLs to be written
	 */
	protected void writeNLs(int count) {
		
		while ( (count--) > 0 ) {
			writer.println();
		}
	}

	/**
	 * Writes a single new line characters to the current writer
	 */
	protected void writeNL() {
		
		writer.println();
	}	
	
	/**
	 * Outputs the given text to the logger (if one exists)
	 * @param text
	 */
	protected void log(String text) {
		if ( logger!=null ) {
			if ( loggerPrefix!=null )
				logger.println(loggerPrefix+text);
			else
				logger.println(text);
		}
	}

	/**
	 * Base class implementation of generator method.<br><br>
	 * 
	 * Calls the pre-generation method to ensure internal structures ready for generation.
	 * 
	 * @param rootPath the path name of where the files are to be located (if null uses current working directory)
	 * @throws IOException
	 */
	public void generate(String rootPath) throws IOException {
		
		assert platformInfo != null;		// platformInfo must be setup prior to generation.
		
		// Setup the generator for all the {@link TargetDriver} Classes
		TargetDriver.setGenerator(this);
	}

	/**
	 * 
	 * @return the {@link TargetCpu} for which generation is taking place.
	 */
	public TargetCpu getTargetCpu() {
		return targetModel;
	}
	
	/**
	 * 
	 * @param targetModel the root TargetCpu of the Target Model for which generation is to be performed
	 * @param logger a PrintWriter that is to be used for logging during generation (may be null)
	 * @param loggerPrefix the prefix attached to each logged message (may be null)
	 */
	protected OSGenerator(TargetCpu targetModel, PrintWriter logger, String loggerPrefix) {
		this.targetModel = targetModel;
		this.logger = logger;
		this.loggerPrefix = loggerPrefix;
	}

}
