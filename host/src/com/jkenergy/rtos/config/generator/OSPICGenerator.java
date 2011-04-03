package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-04-19 01:19:14 +0100 (Sat, 19 Apr 2008) $
 * $LastChangedRevision: 703 $
 * $LastChangedBy: kentindell $
 * $HeadURL: http://10.211.55.3/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/OSPICGenerator.java $
 * 
 */


import java.io.IOException;
import java.io.PrintWriter;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

import java.math.BigInteger;
import java.util.List;


/**
 * Concrete SubClass of OSAnsiCGenerator that generates target specific code for dsPIC based uController devices.<br><br>
 * 
 * This class overrides selected methods form the super classes in order to allow constraint checking and code generation of the
 * dsPIC family of uControllers.<br><br>
 * 
 * Specifically this class generates TRAP handlers and ISR wrapper macro invocations, as well as providing methods that allow
 * mapping of target priorities to mask values and IPL values for the dsPIC family.<br><br>
 * 
 * During in instantiation the appropriate {@link PlatformInfo} values are defined and passed to the super class ready
 * for target specific generation.
 * 
 * @author Mark Dixon
 *
 */
public class OSPICGenerator extends OSAnsiCGenerator {
	
	// Names of files to be generated
	protected final static String GEN_ASM_FILE = "osgenasm.s";
	
	// Names of files to be included
	protected final static String KERNEL_INC_FILE = "kernelasm.inc";
	
	// Prefix added to vector name to identify interrupt source for the target
	protected final static String VECTOR_PREFIX = "__";
	
	// Suffix added to vector name to identify interrupt source for the target
	protected final static String VECTOR_SUFFIX = "Interrupt";
		
	// Names of trap handler macro on this target
	protected final static String TRAP_HANDLER_NAME = "OS_TRAP_HANDLER";
	
	// Names of trap vectors
	protected final static String TRAP0_VECTOR = "ReservedTrap0";
	protected final static String OSCFAILURE_VECTOR = "OscillatorFail";
	protected final static String ADDRERROR_VECTOR = "AddressError";
	protected final static String STACKERROR_VECTOR = "StackError";
	protected final static String MATHERROR_VECTOR = "MathError";
	protected final static String TRAP5_VECTOR = "ReservedTrap5";
	protected final static String TRAP6_VECTOR = "ReservedTrap6";
	protected final static String TRAP7_VECTOR = "ReservedTrap7";
		
	// Name of trap error code constants
	protected final static String TRAP0_CODE = "E_OS_TRAP0";
	protected final static String OSCFAILURE_CODE = "E_OS_OSCFAILURE";
	protected final static String ADDRERROR_CODE = "E_OS_ADDRERROR";
	protected final static String STACKERROR_CODE = "E_OS_STACKFAULT";
	protected final static String MATHERROR_CODE = "E_OS_MATHERROR";
	protected final static String TRAP5_CODE = "E_OS_TRAP5";
	protected final static String TRAP6_CODE = "E_OS_TRAP6";
	protected final static String TRAP7_CODE = "E_OS_TRAP7";
	
	
	/**
	 * @return asm comment start delimiter
	 */
	@Override
	protected String getAsmCommentStart() {
		return ";";
	}

	/**
	 * @return asm comment end delimiter
	 */
	@Override
	protected String getAsmCommentEnd() {
		return "";
	}
	
		
	/**
	 * Maps the given target priority to a priority mask value for this target.
	 * For this target we set the bit at the position indicated by the priority,
	 * e.g. a priority of 10 would return a value of 0x200 (see wiki1036)
	 * 
	 * @param targetPriority the target priority value to be mapped
	 * @return string version of the mask value (in 'C' format hex)
	 */
	@Override
	protected String getTargetPriorityMaskString(int targetPriority) {
		
		assert targetModel.getTargetPriorities().isISRTargetPriority(targetPriority) == false; // should not be called with an ISR target priority
		
		if(targetPriority > 0) {
			// Map given priority to a mask value and return as a hex string
			BigInteger value = BigInteger.ZERO;
			
			// set appropriate bit	
			value = value.setBit(targetPriority - 1); // bit 0 corresponds to priority 1, since a priority of 0 is reserved for the idle task
	
			// return as hex string
			return "0x"+value.toString(16)+"U";
		}
		else {
			return "0"; // A priority of 0 is reserved for the idle task; the bit mask for idle is never used
		}
	}

	/**
	 * Maps from a target priority to the IPL string for a specific target.
	 * 
	 * @param targetPriority the target priority to be mapped
	 * @return the IPL string for the target.
	 */
	@Override
	protected String getTargetIPLString(int targetPriority) {
		
		long value;
		
		if (targetModel.getTargetPriorities().isISRTargetPriority(targetPriority)) {
			value = targetModel.getTargetPriorities().getModelPriority(targetPriority) << 5;
		}
		else {
			value = 0;
		}

		// return as string
		return value+"U";
	}


	/**
	 * Maps from a target priority to an embedded priority number for a specific target. (typedef'd as pri on the target)
	 * On this target Bits 5:7 of the priority store the IPL.
	 *   
	 * @param targetPriority the target priority to be mapped
	 * @return the embedded priority number of the priority for the target.
	 */
	@Override
	protected int mapToEmbeddedPriority(int targetPriority) {
		
		if ( platformInfo.isPri2IPLLookupRequired() ) {
			// target requires a lookup for Pri2IPL, so use generic base class implementation
			return super.mapToEmbeddedPriority(targetPriority);
		}
		else {	
			assert (1<<5) > targetPriority;		// targetPriority always fits below IPL bits (5:7)
			
			// target is not using lookup for Pri2IPL, so map directly to IPL for this target
			if (targetModel.getTargetPriorities().isISRTargetPriority(targetPriority)) {
			
				long modelPriority = targetModel.getTargetPriorities().getModelPriority(targetPriority);
				
				assert modelPriority > 0;		// ISRs must have a priority of more than 0
				assert modelPriority <= 7;		// ISRs must have a priority of less than 8 on the PIC
				return (int)(modelPriority<<5);
			}
			else {
				// IPL always 0 for tasks
				return (int)targetPriority;		
			}
		}
	}

	/**
	 * Implements target specific constraints checking for the dsPIC devices.
	 * 
	 * @param problems List of {@link Problem} objects, appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	@Override
	public void doModelCheck(List<Problem> problems,boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do dsPIC target dependent constraint checks of target model here

		
		// [1] The stack size of each TargetRunnable must be a multiple of 2. [error]
		for ( TargetRunnable next : targetModel.getTargetTasks() ) {
			if ( (next.getModelStackSize() & 1) == 1 ) {
				problems.add(new Problem(Problem.ERROR, "Task '"+next.getName()+"' has a stack size that is not a multiple of 2"));
			}
		}
		for ( TargetRunnable next : targetModel.getTargetISRs() ) {
			if ( (next.getModelStackSize() & 1) == 1 ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has a stack size that is not a multiple of 2"));
			}
		}		

		// [2] The ModelVector of a TargetISR must be valid for the specified by the target [error]
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			
			String vector = next.getModelVector();
			// TODO need way of identifying the device + part number
			if ( PICVectorNameChecker.isValidVectorName(vector, "30") == false ) {	
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has an interrupt vector name specified that is not supported by the target device"));
			}
		}
		
	}


	/**
	 * outputs a .include "file"
	 * @param fileName name of file to be included
	 */
	private void include_local(String fileName) {
		writeln(".include \""+fileName+"\"");
	}	
	
	
	/**
	 * Epilogue called after all ANSI C code has been output to the C source code file.
	 * The current writer is still setup for output to the the C source when this is called.
	 * 
	 * When overridden, this method allows target specific information to be added to the
	 * generated C source file.
	 *
	 * @see OSAnsiCGenerator#generate(String)
	 */
	@Override
	public void SourceCFileEpilogue() {
		
		/////////////////////////////////////////////////////////////////
		// 1. Generate required trap handler macro instantiations
		writeln(comment("Trap handlers"));
		
		if ( targetModel.isStackCheckingEnabled() ) {
			writeln(TRAP_HANDLER_NAME+"("+STACKERROR_VECTOR+", "+STACKERROR_CODE+");");
		}
		
		if ( targetModel.isOscFailureHandled() ) {
			writeln(TRAP_HANDLER_NAME+"("+OSCFAILURE_VECTOR+", "+OSCFAILURE_CODE+");");
		}
		
		if ( targetModel.isAddrErrorHandled() ) {
			writeln(TRAP_HANDLER_NAME+"("+ADDRERROR_VECTOR+", "+ADDRERROR_CODE+");");
		}

		if ( targetModel.isMathErrorHandled() ) {
			writeln(TRAP_HANDLER_NAME+"("+MATHERROR_VECTOR+", "+MATHERROR_CODE+");");
		}	
	}
	
	/**
	 * Implementation of generator for PIC target.
	 * 
	 * This method overrides the normal generate in order that it can produce a target specific 
	 * assembly language file for the dsPIC target.
	 * 
	 * @param rootPath the path name of where the files are to be located (if null uses current working directory)
	 * @throws IOException
	 */	
	@Override
	public void generate(String rootPath) throws IOException {
		
		super.generate(rootPath);	// do generation performed by the superclass
		
		////////////////////////////////////////////////////////////////////
		// Generate the assembly language file
				
		setupWriter(rootPath,GEN_ASM_FILE);	// create and open the ASM file
		
		log("Generating '"+writerPathName+"'");
		
		resetTabs();
		
		writeln(asmcomment(AUTOGEN_COMMENT));	// output the standard header
			
		writeNLs(2);	// write 2 new line chars
		
		///////////////////////////////////////////////////////
		// Include the required headers
		include_local(KERNEL_INC_FILE);
		
		writeNLs(2);	// write 2 new line chars
		
		//////////////////////////////////////////////////////////
		// Generate zero level handler (wrapper) for each CAT2 ISR
		
		writeln(verboseAsmComment("Generic Zero Level Interrupt Handler (ZLIH) macro that is expanded for each ISR handler"));
		writeNL();
		writeln(verboseAsmComment("zlih stackcheck, stacksw, nestrace, isrcb_number"));
		writeNL();
		writeln(verboseAsmComment("stchchk=1 for stack checking, 0 for no stack checking (no overflow checking needed)"));
		writeln(verboseAsmComment("stksw=1 for stack switching, 0 for no stack switching required (basic tasks only)"));
		writeln(verboseAsmComment("nestrace=1 if nesting race can occur, 0 if it can't (e.g. only cat 2 ISRs at one IPL, or NSTDIS set)"));
		
		writeNLs(2);	// write 2 new line chars
		
		for (TargetISR next : targetModel.getTargetISRs()) {
			
			if (next.isCategory2()) {
				
				writeln(asmcomment("zero level wrapper for ISR "+next.getName()));
				
				// Use the model vector name to identify interrupt source on this target
				// Adding a prefix of VECTOR_PREFIX and a suffix of VECTOR_SUFFIX
				String intSourceName = VECTOR_PREFIX+next.getModelVector()+VECTOR_SUFFIX;				
				
				writeln(".global "+intSourceName);
				writeln(intSourceName+":");

				write("zlih ");
				
				///////////////////////
				// Stack checking flag
				if ( next.isStackCheckingEnabled() ) {
					// Stack checking required for this ISR
					write("1, ");
				}
				else {
					// Stack checking not required for this ISR
					write("0, ");
				}
				
				////////////////////////
				// Stack Switching flag
				if ( next.getTargetCpu().getTargetExtendedTasks().size() > 0 ) {
					// stack switching required, since extended tasks exist
					write("1, ");
				}
				else {
					// no stack switching required, since only basic tasks exist
					write("0, ");
				}
				
				//////////////////////////
				// Nested race check flag
				// TODO nested race also not possible if nested interrupts are disabled for this target.
				if ( next.getTargetCpu().getTargetPriorities().getLowestISRTargetPriority() != next.getTargetPriority() ) {
					// next ISR does not have the lowest target priority of all ISRs, so nested race check required
					write("1,");
				}
				else {
					// next ISR does have the lowest target priority of all ISRs, so nested race check not required
					write("0,");
				}
				
				/////////////////
				// ISR cb number
				writeln(" "+next.getControlBlockIndex());
				
				writeNL();
			}
		}
		
		writeNL();
		
		////////////////////////////////////////////////////////////////////////
		// Generate driver specific assembly file code for each device
		for (TargetDevice device : targetModel.getTargetDevices() ) {
			
			// Use the driver to generate this, since the kind of driver determines the contents
			// of the header file code.
			device.getDriver().genAssemblyCode(device);
		}
		writeNL();
		
		// Close the write for the file
		closeWriter();
		
		log("Completed '"+GEN_ASM_FILE+"'");
	}	
	

	/**
	 * Constructor used to instantiate the OSPICGenerator.
	 * 
	 * @param targetModel the root TargetCpu of the Target Model for which generation is to be performed
	 * @param logger a PrintWriter that is to be used for logging during generation (may be null)
	 * @param loggerPrefix the prefix attached to each logged message (may be null)	 * 
	 */
	public OSPICGenerator(TargetCpu targetModel, PrintWriter logger, String loggerPrefix) {	
		super(targetModel,logger,loggerPrefix);
		
		// Setup the platformInfo architecture for this target/compiler		
		platformInfo.setArchitecture(				
										false,	// pri2IPLLookupRequired
										false	// hasAPICallsAboveKernelLevel
		);
		
		// Setup the datatype to use for the OSTICKDURATION variable.
		platformInfo.setTickDurationType(OSAnsiCGenerator.UINT32);
		
		// Setup the platformInfo limits for this target/compiler
		platformInfo.setLimits(
								16,			// maxEventBits
								16,			// maxTaskPriorities
								6,			// maxISRPriorities
								0xffff,		// tickTypeSize TickType is a uint16 on this platform
								0xffff,		// unatTypeSize unat is uint16 on this platform
								0x3fff,		// osBlockTypeSize os_block_size is uint16 on this platform, but limited to 14 bits because of use of repeat instruction in block copy assembly language
								0xffff		// maxStackSize, @todo decide an a sensible value for this
		);
		
		// Setup the platformInfo stack info. for this target/compiler
		platformInfo.setUpStackDetais(
				40, 	// defaultTaskStackSize
				16, 	// defaultISRStackSize
				16, 	// defaultPreTaskHookStackSize
				16,		// defaultPostTaskHookStackSize								
				0, 		// stackOverflowSpace  TODO calc and add this value
				4,		// kernelPreTaskEntryUsage
				0,		// kernelPreISREntryUsage TODO calc and add this value
				-2,		// topOfStackCheckOffset
				4,		// kernelPreTaskHookEntryUsage
				true,	// isAscendingStack
				true,	// isPostOffsetSP
				2		// stackAlignmentBytes
		);		
		
		// setup the stack overhead values, the platform uses the maximum of the passed values
		platformInfo.setupStdStatusExtStackKernelUsage(0x16); // TODO add numbers generated from calib1 program
		platformInfo.setupExtStatusExtStackKernelUsage(0x16); // TODO add numbers generated from calib1 program		

		
		// Now that architecture information is known, ask the target model to prepare for generation.
		targetModel.preGenProcessing(platformInfo);
	}	
}
