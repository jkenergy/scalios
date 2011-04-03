package com.jkenergy.rtos.config.serializers;

/*
 * $LastChangedDate: 2008-01-27 00:09:36 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 589 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/serializers/OILFileInfo.java $
 * 
 */

import java.util.*;

/**
 * Instances of this class are used to represents OIL Files that have been loaded during
 * an import operation. The class is required so that information regarding #include'd files
 * can be maintained for error reporting etc.
 * 
 * @author Mark Dixon
 * @see OILSerializer
 */
class OILFileInfo {

	/**
	* The end of line string for this machine.
	*/
	private final static String eol = System.getProperty("line.separator", "\n");	
	
	/**
	 * The OILFileInfo instances which this file included
	 */
	private List<OILFileInfo> includedFiles = new ArrayList<OILFileInfo>();
	
	/**
	 * The OILFileInfo from which this file was included
	 */
	private OILFileInfo includer=null;
	
	/**
	 * The full path name of the file
	 */
	private String pathName;
	
	/**
	 * The file name only of the file
	 */
	private String fileName;	
	
	/**
	 * The start Line number at which the file was included
	 */	
	private int startLineNo;
	
	/**
	 * The end Line number at which the file was included
	 */
	private int endLineNo;
	
	/**
	 * The last line number that was found during a call to {@link #resolveLineNoOwner(int)}
	 */
	private int lastResolvedNo;
	
	
	/**
	 * @return Returns the includes.
	 */
	public List<OILFileInfo> getIncludedFiles() {
		return includedFiles;
	}
	
	/**
	 * Adds the given OILFileInfo to the list included by this OILFileInfo
	 * @param include the OILFileInfo which this OILFileInfo includes
	 */
	public void addIncludedFile(OILFileInfo include) {
		
		if ( include!=null ) {
			includedFiles.add(include);
		}
	}
	
	/**
	 * @return Returns the includer.
	 */
	public OILFileInfo getIncluder() {
		return includer;
	}

	/**
	 * @param includer The includer to set.
	 */
	public void setIncluder(OILFileInfo includer) {
		this.includer = includer;
	}	
	
	/**
	 * @return Returns the pathName.
	 */
	public String getPathName() {
		return pathName;
	}

	/**
	 * @param pathName The pathName to set.
	 */
	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	/**
	 * @return Returns the fileName.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName The fileName to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}	
	
	/**
	 * @return Returns the startLineNo.
	 */
	public int getStartLineNo() {
		return startLineNo;
	}

	/**
	 * @param startLineNo The startLineNo to set.
	 */
	public void setStartLineNo(int startLineNo) {
		this.startLineNo = startLineNo;
	}
	
	/**
	 * @return Returns the endLineNo.
	 */
	public int getEndLineNo() {
		return endLineNo;
	}

	/**
	 * @param endLineNo The endLineNo to set.
	 */
	public void setEndLineNo(int endLineNo) {
		this.endLineNo = endLineNo;
	}	
	
	/**
	 * @return The number of lines in the file info.
	 */
	public int getLineCount() {
		return endLineNo-(startLineNo);
	}
	
	/**
	 * @return Returns the lastResolvedNo.
	 */
	public int getLastResolvedNo() {
		return lastResolvedNo;
	}
	
	
	/**
	 * Override equals method so that OILFileInfo instances with same filename are seen at being equal
	 */
	@Override
	public boolean equals(Object o) {
		if ( o==this ) return true;
		
		if ( !(o instanceof OILFileInfo)) return false;
		
		return (this.pathName.equals( ((OILFileInfo)o).getPathName() ));
	}

	/**
	 * Override hashCode method so that OILFileInfo instances with same filename generate same hashCode
	 */	
	@Override
	public int hashCode() {
		return pathName.hashCode();	// use pathName value to calculate the hashCode
	}
	
	public String toString() {
		return pathName;
	}


	/**
	 * Identifies the OILFileInfo in which the given file number exists.
	 * If the given line number does exist within a contained OILFileInfo
	 * then calling {@link #getLastResolvedNo()} can be used to map to the local file number
	 * within the that OILFileInfo instance.
	 * @param lineNo
	 * @return the OILFileInfo in which the given line number occurs
	 * @see #getLastResolvedNo()
	 */
	public OILFileInfo resolveLineNoOwner(int lineNo) {
		
		if ( lineNo>=startLineNo && lineNo<=endLineNo ) {
			
			lastResolvedNo=lineNo-(startLineNo-1);
		
			for ( OILFileInfo next : includedFiles ) {
				
				if ( next.getStartLineNo() < lineNo )
					lastResolvedNo-=next.getLineCount();
				
				next=next.resolveLineNoOwner(lineNo);
					
				if ( next!=null )
					return next;
			}
				
			return this;
		}
		return null;
	}
	
	/**
	 * Returns a message that shows the includer hierarchy starting from this OILFileInfo. This message
	 * may be used during in error reporting etc. to show full include trace paths of files.
	 * @return a string representation of the parent OILFileInfo objects that included this OILFileInfo
	 */
	public String getIncludeChain() {
		
		StringBuffer msg = new StringBuffer();
			
		if ( includer!=null ) {
			
			StringBuffer offset = new StringBuffer();
			
			OILFileInfo next = includer;
			
			while ( next!=null ) {
				
				msg.append(eol);
				
				offset.append(" ");
				
				msg.append(offset+"Included from "+next.getFileName());
				
				next = next.getIncluder();
			}
		}
		
		return msg.toString();
	}
	
	/**
	 * @param includer
	 * @param pathName
	 * @param fileName
	 * @param startLineNo
	 */
	public OILFileInfo(OILFileInfo includer,String pathName,String fileName, int startLineNo) {
		this.includer=includer;
		this.pathName=pathName;
		this.fileName=fileName;
		this.startLineNo=startLineNo;
		this.endLineNo=startLineNo;
	}
	
}
