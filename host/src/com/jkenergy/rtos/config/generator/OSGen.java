package com.jkenergy.rtos.config.generator;

/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/OSGen.java $
 * 
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jkenergy.rtos.config.Problem;
import com.jkenergy.rtos.config.osmodel.Cpu;
import com.jkenergy.rtos.config.serializers.OILSerializer;
import com.jkenergy.rtos.config.serializers.XMLSerializer;


/**
 * This is main driver class for the command line Configuration/code generation tool for the RTOS.<br><br>
 * 
 * usage:<br><br>
 * <code>
 * OSGen &lt;input_filename&gt;.oil | &lt;input_filename&gt;.xml [-D&lt;constant_name&gt;] [-I&lt;include_path_name&gt;] [-o &lt;output_directory&gt;] [-export &lt;filename&gt;.oil | &lt;filename&gt;.xml] <br><br>
 * </code><br>
 * Multiple occurrences of the <code>-D</code> and <code>-I</code> switches may be specified.<br><br><br>
 * 
 * <b>Switch descriptions</b><br><br>
 * <code>&lt;input_filename&gt;</code> identifies either an OIL or an XML input file.<br><br>
 * <code>-D</code> specifies the constants to be defined and passed to any <code>#ifdef</code> or <code>#ifndef</code> OIL file directives.<br><br>
 * <code>-I</code> specifies the includes paths to be defined and used by the <code>#include</code> OIL directives.<br><br>
 * <code>-o</code> specifies the directory in which the generated code is to be output.<br><br>
 * <code>-export</code> specifies that translation only (no generation) is to take place.<br><br><br>
 * 
 * @author Mark Dixon
 */
public class OSGen {

	private final static String SWITCH_EXPORT = "export";
	private final static String SWITCH_OUTPUT_DIR = "o";

	/**
	 * Checks if the given switch is present within the given argument list.
	 * 
	 * @param args the list of arguments to check
	 * @param switchName the name of the switch to be located (no - prefix required)
	 * @return true if the named switch is present in the argument list
	 */
	private static boolean isSwitchPresent(String[] args, String switchName) {
		
		for (String next : args) {		
			if ( next.startsWith("-")) {
				
				// found a switch prefix, so extract remaining characters and compare to given name
				if ( switchName.equals(next.substring(1).trim()) ) {
					return true;
				}
			}
		}
		
		return false;
	}	
	
	/**
	 * Returns any value following the given switch (if the switch is present)
	 * 
	 * The value is identified as a string that follows the switch, but does not have a "-" prefix.
	 * i.e. -<switchName> <switchValue>
	 * 
	 * @param args the list of arguments to check
	 * @param switchName the name of the switch for which the value is required (no - prefix required)
	 * @return the value associated with the switch, null if no switch or no value
	 */
	private static String getSwitchValue(String[] args, String switchName) {
		
		for (int i = 0; i < args.length; i++) {
			String next = args[i];
	
			if ( next.startsWith("-")) {
				
				// found a switch prefix, so extract remaining characters and compare to given name
				if ( switchName.equals(next.substring(1).trim()) ) {
					// found the switch, so see if there is an associated value
					int valIndex = i+1;
					if ( valIndex < args.length ) {
						String value = args[valIndex];
						
						if ( value.startsWith("-") == false ) {
							return value; // found the value, so return
						}
					}

					// no more arguments or argument has - prefix, so no value present
					return null;						
				}
			}
		}
		
		return null;
	}	
	
	
	/**
	 * Extracts all the constant names specified within the given argument list.
	 * A constant is defined in the argument list using -D<constant_name>
	 * 
	 * @param args the list of arguments from which to extract constant definitions
	 * @return the set of constant names defined within the given argument list
	 */
	private static Set<String> getConstants(String[] args) {
		
		Set<String> constants = new HashSet<String>();
		
		for (String next : args) {
			
			if ( next.startsWith("-D")) {
				
				String value = next.substring(2).trim();
				
				if ( value.length() > 0 ) {
					constants.add(value);
				}
			}
		}
		
		return constants;
	}
	
	/**
	 * Extracts all the Include path names specified within the given argument list.
	 * An include path is defined in the argument list using -I<path>
	 * 
	 * @param args the list of arguments from which to extract include path names
	 * @return the set of include path names defined within the given argument list
	 */
	private static Set<String> getIncludePaths(String[] args) {
		
		Set<String> includePaths = new HashSet<String>();
		
		for (String next : args) {
			
			if ( next.startsWith("-I")) {
				
				String value = next.substring(2).trim();
				
				if ( value.length() > 0 ) {
					includePaths.add(value);
				}
			}
		}
		
		return includePaths;
	}	
	
	
	/**
	 * Extracts the file name specified within the given argument list.
	 * A filename is defined in the argument list using <file_name>, i.e. not a switch prefixed with "-"
	 * 
	 * @param args the list of arguments from which to extract the file name
	 * @return the file name defined within the given argument list
	 */
	private static String getFilename(String[] args) {
				
		for (String next : args) {
			
			if ( !next.startsWith("-")) {
				// found argument that is not a switch
				return next;
			}
		}
		
		return null;
	}	
	
	/**
	 * Uses the given OSModel Cpu to generate an OS configuration.
	 * 
	 * Prior to generation constraint checks are performed on the OS Model and the target model.
	 * If any constraints are fatal (i.e. would result in a broken system being generated) then
	 * no generation is performed. 
	 * 
	 * @param osModel the OSModel Cpu that contains the model from which to generate
	 * @param logger the logger output
	 * @param pathname the path name of the output directory (if null uses current working directory)
	 * @return true if system generated, false if generation not performed due to fatal errors
	 */
	private static boolean generateSystem(Cpu osModel, PrintWriter logger, String pathname) {
		
		/////////////////////////////////////////////////////////////////
		// Do target independent constraint checks of the entire OS model
		List<Problem> problems = new ArrayList<Problem>();

		boolean fatalErrors = false;
		
		osModel.doModelCheck(problems, true);

		if (problems.size() > 0) {

			// problems detected, so output to log.
			logger.println("OS Model: Constraints broken.");

			for (Problem next : problems ) {
				logger.println("OS Model: "+next.getMessage());
				
				if ( next.isFatalProblem() ) {
					fatalErrors = true;	// identify if any fatal errors occurred, since can't generate if this is the case
				}
			}	
		}
		
		if ( fatalErrors == false )  {
			
			// no fatal errors detected during the model check, so create the target model and generate the system
			
			TargetCpu targetCpu = new TargetCpu(osModel);
			
			// Create a generator for the "dsPIC" target
			// TODO find a better way to support targets within a single tool than editing the following line
			OSARM7Generator generator = new OSARM7Generator(targetCpu, logger, "OSGen: ");
								
			// Do target specific constraint checks of the entire target model 
			List<Problem> targetProblems = new ArrayList<Problem>();
			
			generator.doModelCheck(targetProblems, true);
			
			if (targetProblems.size() > 0) {

				// problems detected, so output to log.
				logger.println("Target Model: Constraints broken.");

				for (Problem next : targetProblems ) {
					logger.println("Target Model: "+next.getMessage());
					
					if ( next.isFatalProblem() ) {
						fatalErrors = true;	// identify if any fatal errors occurred, since can't generate if this is the case
					}
				}	
			}					

			if ( fatalErrors == false ) {
				try {
					generator.setVerboseComments(true);
					
					// do the code generation to the specified output directory
					generator.generate(pathname);
					
					logger.println("OSGen: system generated successfully");
				}
				catch (IOException e) {
					logger.println("OSGen: "+e.getMessage());
					logger.println("OSGen: no system generated due to I/O failure.");
					fatalErrors = true;
				}
			}
			else {
				logger.println("OSGen: no system generated due to Target constraint errors.");
			}
		}
		else {
			logger.println("OSGen: no system generated due to OS constraint errors.");
		}
		
		return !fatalErrors;
	}
	

	/**
	 * The main() entry point to the configuration tool.
	 * 
	 * @param args - array of command line arguments passed from the shell
	 */
	public static void main(String[] args) {
		
		// Driver for OS generator application
		
		// output log, set to stdout with autoflush on
		PrintWriter log = new PrintWriter(new OutputStreamWriter(System.out),true);

		String fileName = getFilename(args);
		
		if ( fileName != null ) {
			
			Cpu osModel = null;
			
			// identify the type of the input file
			if ( fileName.endsWith(".oil") ) {
				// have an oil file as input
				
				osModel=new Cpu();									// Create a Cpu (root of the OS Model)
				
				OILSerializer importer = new OILSerializer();		// Create an OILSerializer instance
				
				// Setup the -I include paths within the importer. This is used to help locate #include'd OIL files.
				Set<String> includePaths = getIncludePaths(args);
				
				for (String nextIncludePath : includePaths) {
					importer.addSearchPath(nextIncludePath);
				}
				
				// Do an import into the OS model
				if ( importer.importOIL(osModel, fileName, log, getConstants(args)) == false ) {
					log.println("OSGen: no system generated due to OIL file importation failure.");
					osModel = null;
				}
			}
			else if ( fileName.endsWith(".xml") ) {
				// have an xml file as input
				osModel=new Cpu();	 // Create a Cpu (root of the OS Model)
				
				XMLSerializer xmlSerializer = new XMLSerializer();
				
				if ( xmlSerializer.importXML(osModel, fileName, log) == false ) {
					log.println("OSGen: no system generated due to XML importation failure.");
					osModel = null;					
				}
			}
			else {
				 log.println("OSGen: input file type not recognised, must be *.oil or *.xml");
			}

			
			if ( osModel != null ) {
				
				// A model has been created, so decide what to do with it
				
				if ( isSwitchPresent(args, SWITCH_EXPORT) ) {
					// user wants to export only, no system generation required
					
					String outFileName = getSwitchValue(args, SWITCH_EXPORT);
							
					if ( outFileName == null || outFileName.endsWith(".xml") ) {
						XMLSerializer xmlSerializer = new XMLSerializer();
						
						xmlSerializer.exportXML(osModel, outFileName, log);
					}
					else if ( outFileName == null || outFileName.endsWith(".oil") ) {
						
						OILSerializer oilSerializer = new OILSerializer();
						
						oilSerializer.exportOIL(osModel, outFileName, log);
					}
					else {
						log.println("OSGen: output file type not recognised, must be *.oil or *.xml");
						System.exit(1);
					}
				}
				else {
					// user wants a system generated
					if ( generateSystem(osModel, log, getSwitchValue(args, SWITCH_OUTPUT_DIR)) == false ){
						// system not generated due to fatal errors, so exit with failure status
						System.exit(1);
					}
				}
			}
			else {
				// system not generated due to importation failure, so exit with failure status
				System.exit(1);
			}
		}
		else {
			// no filename specified on the command line
			log.println("OSGen: usage OSGen <filename> {-D<constant_name>} ");
			System.exit(1);
		}
	}
}
