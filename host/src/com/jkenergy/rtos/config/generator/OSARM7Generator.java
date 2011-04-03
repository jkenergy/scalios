package com.jkenergy.rtos.config.generator;


/*
 * $LastChangedDate: 2008-01-27 18:36:16 +0000 (Sun, 27 Jan 2008) $
 * $LastChangedRevision: 596 $
 * $LastChangedBy: markdixon $
 * $HeadURL: http://192.168.1.240/svn/repos/ertcs/rtos/trunk/host/src/com/jkenergy/rtos/config/generator/OSPICGenerator.java $
 * 
 */


import java.io.IOException;
import java.io.PrintWriter;

import com.jkenergy.rtos.config.Checkable;
import com.jkenergy.rtos.config.Problem;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.lang.Math;

/**
 * Concrete SubClass of OSAnsiCGenerator that generates target specific code for ARM7 based uController devices.<br><br>
 * 
 * This class overrides selected methods form the super classes in order to allow constraint checking and code generation of the
 * ARM7 family of uControllers.<br><br>
 * 
 * Specifically this class generates ISR vector tables, as well as providing methods that allow
 * mapping of target priorities to mask values and IPL values for the ARM7 family.<br><br>
 * 
 * During in instantiation the appropriate {@link PlatformInfo} values are defined and passed to the super class ready
 * for target specific generation.
 * 
 * @author Ken Tindell
 *
 */
public class OSARM7Generator extends OSAnsiCGenerator {
	// Names of files to be generated
	protected final static String GEN_ASM_FILE = "osgenasm.s";

	// Used for word sizes TODO best declared somewhere target-independent perhaps
	private static final long UINT32 = 0xffffffffL;
	private static final long UINT16 = 0xffffL;
	
	/////////////////////////////////////////////////////////////////
	// Stack overheads in bytes for IRQ ISRs. See the file irqwrapper.s and VERIFY BY HAND.
	//
	// Pre-ISR stack overheads are the overheads placed on the stack prior to the ISR entry function
	private static final long ARM7_PRE_ISR_IRQ_STACK_OVERHEADS = 16;
	//
	// Pre-dispatch stack overheads are the overheads left on the IRQ stack when jumping to the kernel
	// stack to call the dispatcher
	private static final long ARM7_PRE_DISPATCH_IRQ_STACK_OVERHEADS = 16;

	
	/////////////////////////////////////////////////////////////////

	// Namespace taken by the ARM7 target
	protected final static String ARM7_PREFIX = "os_lpc21xx_";
	protected final static String ARM7_SPURIOUS_CAT2_ISR_HANDLE = "os_lpc21xx_spurious_cat2_isr";
	
	private final int INVALID_VECTOR = -1;
	
	/**
	 * Function to determine the complete size of the kernel stack (used for the kernel itself plus
	 * basic tasks). See wiki entry "OfflineKernelStackUsageCalculation" for details (wiki1079).
	 * 
	 * @return the maximum stack usage, in bytes, of the kernel stack
	 */
	private long getKernelStackUsage() {
		return getKernelStackUsage(getIdleTask());
	}

	/**
	 * Give the maximum FIQ stack usage. Note that there should be at most one declared
	 * FIQ ISR (i.e. one category 1 ISR). There are no wrappers to FIQ ISRs so the model
	 * declared usage is the required stack size.
	 * 
	 * @return the maximum FIQ stack usage in bytes
	 */
	private long getFIQStackUsage() {
		long usage = 0;
		
		for (TargetISR next : targetModel.getTargetISRs() ) {
			if( !next.isCategory2() ) {
				usage = Math.max (usage,  next.getModelStackSize() );				
			}
		}
		
		return usage;
	}
	
	/**
	 * Return the maximum stack usage for the IRQ stack.
	 * 
	 * @return the maximum FIQ stack usage in bytes
	 */
	private long getIRQStackUsage() {
		long depth = getTaskPreemptionDepth(getIdleTask());		// Worst-case nesting of preemption. For an empty system this is 1 (idle task).

		// Determine the IRQ ISR function with the most stack usage
		long maxISRusage = 0;
		for (TargetISR next : targetModel.getTargetISRs() ) {
			if( next.isCategory2() ) {
				maxISRusage = Math.max (maxISRusage,  next.getModelStackSize() );				
			}
		}
		
		// The IRQ stack usage is the pre-dispatch overheads between each preemption level plus the overheads of an IRQ
		// ISR where no dispatch can take place.
		return (depth - 1) * ARM7_PRE_DISPATCH_IRQ_STACK_OVERHEADS + ARM7_PRE_ISR_IRQ_STACK_OVERHEADS + maxISRusage;
	}
	
	long getTaskPreemptionDepth(TargetTask i) {
		long depth = 0;
		
		for( TargetTask j: getPreemptingTasks(i) ) {
			depth = Math.max(depth, getTaskPreemptionDepth(j));
		}
		
		return depth + 1;
	}
	
	/**
	 * Finds the idle task in the system.
	 * 
	 * @return the idle TargetTask, or null if none defined
	 * 
	 * TODO this should be put into the TargetTask class
	 */
	private TargetTask getIdleTask() {
		TargetTask idleTask = null;
		
		for (TargetTask next : targetModel.getTargetTasks()) {
			if ( next.isIdle() ) {
				assert idleTask == null;
				idleTask = next;
			}
		}
		assert idleTask != null;
		return idleTask;
	}
	
	/**
	 * Determines which ISRs can preempt a given runnable.
	 * 
	 * @param i is the baseline runnable
	 * @return a collection of ISRs that can preempt i while it is running
	 */
	private Collection<TargetISR> getPreemptingISRs(TargetRunnable i) {
		Collection<TargetISR> preemptingISRs = new LinkedHashSet<TargetISR>();

		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( next.getModelPriority() > i.getModelPriority()) {
				preemptingISRs.add(next);
			}
		}
		
		return preemptingISRs;
	}
	

	/**
	 * Determines which tasks can preempt a given runnable.
	 * 
	 * @param i is the given runnable
	 * @return a collection of tasks that can preempt
	 */
	private Collection<TargetTask> getPreemptingTasks(TargetRunnable i) {
		Collection<TargetTask> preemptingTasks = new LinkedHashSet<TargetTask>();

		if(i instanceof TargetTask) {
			// Tasks can preempt if their base priorities exceed the boost priorities of the task,
			// or if they exceed the base priority of the task and the task makes a Schedule() call.
			//
			// TODO update the OIL to allow a NOSCHEDULE tag that indicates the task won't make
			// a schedule call, then update the following so that get getModelPriority() can be
			// replaced with getBoostPriority() for such tasks.
			for ( TargetTask next : targetModel.getTargetTasks() ) {
				if ( next.getModelPriority() > ((TargetTask)i).getModelPriority()) {
					preemptingTasks.add(next);
				}
			}
		}
		
		return preemptingTasks;
	}
	
	/**
	 * Determines the overheads of the kernel itself for a specific preemption. The overheads of are
	 * the maximum stack usage of the kernel itself across any pathway for the given preemption. These
	 * overheads are determined by calibration of the final builds of the Scalios library, and is also
	 * target-specific due to the way each platform uses the stack (e.g. on the ARM7 ISRs have their own
	 * stack, whereas on other microcontrollers the 'wrapper' code switches to the kernel stack to
	 * execute ISRs).
	 * 
	 * See wiki1067 (KernelStackOverheads) and wiki1079 (OfflineKernelStackUsageCalculation) for more details.
	 * 
	 * @param i is the base runnable
	 * @param j is the preempting runnable
	 * @return the number of bytes left on the kernel stack by the kernel itself when going from i to j
	 */
	long getKernelStackOverheads(TargetRunnable i, TargetRunnable j) {
		// Four general cases:
		//
		// 1. API call on kernel stack causing a task switch
		// 2. API call on extended task's stack causing a task switch
		// 3. Interrupt that occurs on the kernel stack
		// 4. Interrupt that occurs on an extended task's stack
		//
		// On the ARM7, (4) is effectively the same as (3) since ISRs run automatically
		// on their own private stack.
		//
		// TODO placeholder below needs replacing with calibration values
		return 0;
	}
	
	/**
	 * Gets the kernel stack used by the application code in a given runnable. This is
	 * the requested (and hence enforced) stack usage: the extra overheads placed on
	 * by a kernel API call are included elsewhere. For extended tasks, the kernel stack
	 * usage is zero.
	 * 
	 * On the ARM7, neither IRQ nor FIQ ISRs run on the kernel stack. Therefore ISRs
	 * on this platform have zero kernel stack usage.
	 * 
	 * @param i is the runnable for which the stack usage is requested
	 * @return the kernel stack usage of the runnable
	 */
	private long getRunnableFunctionKernelStackUsage(TargetRunnable i) {
		if(i instanceof TargetTask && !((TargetTask)i).isExtended()) {
			return i.getModelStackSize();
		}
		else {
			return 0;
		}
	}
	
	/**
	 * Gets the maximum kernel stack usage for preemptions of a given runnable.
	 * @param i is the given baseline runnable
	 * @return the maximum kernel stack usage for preemptions of i
	 */
	private long getKernelStackUsage(TargetRunnable i) {
		long usage = 0;
		
		for( TargetTask j: getPreemptingTasks(i) ) {
			usage = Math.max(usage, getKernelStackOverheads(i, j) + getKernelStackUsage(j));
		}
		for( TargetISR j: getPreemptingISRs(i) ) {
			usage = Math.max(usage, getKernelStackOverheads(i, j) + getKernelStackUsage(j));
		}
		return usage + getRunnableFunctionKernelStackUsage(i);
	}
	
	/**
	 * @return asm comment start delimiter
	 */
	@Override
	protected String getAsmCommentStart() {
		return "/*";
	}

	/**
	 * @return asm comment end delimiter
	 */
	@Override
	protected String getAsmCommentEnd() {
		return "*/";
	}
	
	private static final String targetLPC21XXDeviceName[] = {
		"WDT",				// 0
		"",					// 1
		"",					// 1
		"ARMCore0",			// 2
		"ARMCore1",			// 3
		"TIMER0",			// 4
		"TIMER1",			// 5
		"UART0",			// 6
		"UART1",			// 7
		"PWM",				// 8
		"I2C",				// 9
		"SPI0",				// 10
		"SPI1_SSP",			// 11
		"PLL",				// 12
		"RTC",				// 13
		"EINT0",			// 14
		"EINT1",			// 15
		"EINT2",			// 16
		"EINT3",			// 17
		"ADC",				// 18
		"CAN_common",		// 19
		"CAN1_TX",			// 20
		"CAN2_TX",			// 21
		"CAN3_TX",			// 22
		"CAN4_TX",			// 23
		"",					// 24
		"",					// 25
		"CAN1_RX",			// 26
		"CAN2_RX",			// 27
		"CAN3_RX",			// 28
		"CAN4_RX",			// 29
		"",					// 30
		""					// 31
	};

	/**
	 * Returns the source interrupt number of a given ISR from its vector string.
	 * 
	 * @return the source interrupt number. VECTOR_INVALID if no match.
	 */
	private int getLPC21XXInterruptSource(String vector) {
		int bit = INVALID_VECTOR;
		
		for(int index = 0; index <= 31; index++) {
			if(vector.compareToIgnoreCase(targetLPC21XXDeviceName[index]) == 0) {
				bit = index;
			}
		}
		return bit;
	}
	
	/**
	 * Return a collection of strings representing the devices that a given ISR will
	 * handle.
	 * 
	 * The vector string is a comma-separated list of devices.
	 * 
	 * @return the collection of strings describing the devices handled by the ISR
	 * @param isr the ISR
	 * 
	 * TODO this is fairly generic and should be somewhere else too
	 */
	private Collection<String> getDevices(TargetISR isr) {
		String vector = isr.getModelVector();

		int from = 0;
		int to;
		
		Collection<String> devices = new LinkedHashSet<String>();
		
		do {
			String tmp;
			
			to = vector.indexOf(',', from);
			
			if ( to == -1 ) {
				// string is from .. end-of-tmp
				tmp = vector.substring(from);
			}
			else {
				// string is from .. to
				tmp = vector.substring(from, to - 1);
			}
			devices.add(tmp);
			from = to;
		} while (to != -1);
		
		return devices;
	}

	/**
	 * Returns a BigInteger mask representing the devices serviced by the given ISR. The
	 * bits are set according to the devices listed in the {@link targetLPC21XXDeviceName}
	 * @param isr the ISR
	 * @return a BigInteger mask with devices marked according to the LPC21xx VIC format
	 */
	private BigInteger getTargetISRDeviceMask(TargetISR isr) {
		BigInteger result = BigInteger.ZERO;
		
		for(String next: getDevices(isr)) {
			result.setBit(getLPC21XXInterruptSource(next));
		}
		
		return result;
	}
	
	/**
	 * Returns a collection of ISRs that have a given model priority.
	 * 
	 * @param modelPriority the model priority of the ISRs
	 * @return set of ISRs with given model priority
	 * 
	 * TODO this is generic enough to live somewhere other than in the ARM7 generator.
	 */
	private Collection<TargetISR> getTargetISRsByModelPriority(long modelPriority) {
		Collection<TargetISR> matchingISRs = new LinkedHashSet<TargetISR>();
		
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( next.getModelPriority() == modelPriority) {
				matchingISRs.add(next);
			}
		}
		
		return matchingISRs;
	}
	
	/**
	 * Returns the handle of an ISR at the given model priority (or null if none).
	 * Raises an assertion failure if there is more than one ISR at the given
	 * model priority (model check should have already detected this).
	 * 
	 * @param modelPriority the model priority of the ISR
	 * @return the ISR with the given model priority or null if none
	 * 
	 * TODO this is generic enough to live somewhere other than in the ARM7 generator.
	 */
	private TargetISR getTargetISRByModelPriority(long modelPriority) {
		TargetISR matchingISR = null;
		
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( next.getModelPriority() == modelPriority) {
				assert matchingISR == null;
				matchingISR = next;
			}
		}
		
		return matchingISR;
	}
	
	/**
	 * Maps the given target priority to a priority mask value for this target.
	 * For this target we set the bit at the position indicated by the priority,
	 * e.g. a priority of 10 would return a value of 0x200 (see wiki1036)
	 * 
	 * @param targetPriority the target priority value to be mapped
	 * @return string version of the mask value (in 'C' format hex)
	 * 
	 * TODO this appears to be target-independent functionality. Why is it overridden?
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
	 * Maps from a target priority to the IPL string for a specific target. See wiki1148 for details of the ARM7
	 * priority mapping scheme.
	 * 
	 * @param targetPriority the target priority to be mapped
	 * @return the IPL string for the target.
	 */
	@Override
	protected String getTargetIPLString(int targetPriority) {
		
		long value;
		
		if (targetModel.getTargetPriorities().isISRTargetPriority(targetPriority)) {
			assert targetModel.getTargetPriorities().getModelPriority(targetPriority) == 1;
			value = 128;		// IPL = 1; IRQ disabled
		}
		else {
			value = 0;			// Software priority; IPL = 0
		}

		// return as string
		return value+"U";
	}


	/**
	 * Maps from a target priority to an embedded priority number for a specific target. (typedef'd as pri on the target)
	 * See wiki1148 for details of the ARM7 priority mapping scheme.
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
			// target is not using lookup for Pri2IPL, so map directly to IPL for this target
			if (targetModel.getTargetPriorities().isISRTargetPriority(targetPriority)) {
			
				long modelPriority = targetModel.getTargetPriorities().getModelPriority(targetPriority);
				
				// The model priority for ISRs is described in wiki1148
				// and summarised here:
				//
				// 0 = lowest priority, shared IRQ vector (category 2 ISR). IPL = 1.
				// 1-16 = IRQ (category 2 ISR) with priority 1 = lowest, and priority 16 = highest. IPL = 1.
				// 17 = FIQ (category 1 ISR)
				assert modelPriority >= 0;
				assert modelPriority <= 17;
				
				return (int)(1<<7);					// Set bit 7 to indicate IRQ disabled
			}
			else {
				assert (1<<5) > targetPriority;		// targetPriority always fits in bits 4:0 for tasks

				// IPL always 0 for tasks
				return (int)targetPriority;
			}
		}
	}

	/**
	 * Implements target specific constraints checking for the ARM7 devices.
	 * 
	 * @param problems List of {@link Problem} objects, appended to when problems found
	 * @param deepCheck flag to cause deep model check
	 * @see Checkable
	 * @see Problem
	 */
	@Override
	public void doModelCheck(List<Problem> problems, boolean deepCheck)
	{
		super.doModelCheck(problems,deepCheck);
		
		// Do ARM7 target dependent constraint checks of target model here

		// [1] The stack size of each TargetRunnable must be a multiple of 4. [error]
		for ( TargetRunnable next : targetModel.getTargetTasks() ) {
			if ( (next.getModelStackSize() % 4) != 0 ) {
				problems.add(new Problem(Problem.ERROR, "Task '"+next.getName()+"' has a stack size that is not a multiple of 4"));
			}
		}
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( (next.getModelStackSize() % 4) != 0 ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has a stack size that is not a multiple of 4"));
			}
		}		

		// [2] The model priority for an ISR must be 0 .. 17 (see wiki1148) [error]
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( next.getModelPriority() < 0 || next.getModelPriority() > 17) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has a priority outside the range 0 .. 17"));
			}
		}

		// [3] Each model priority must have at most one ISR associated [error]
		for (long modelPriority = 1; modelPriority <= 16; modelPriority++) {
			if ( getTargetISRsByModelPriority(0).size() > 1 ) {
				problems.add(new Problem(Problem.ERROR, "More than one ISR with priority "+modelPriority));
			}
		}
		
		// [4] Any ISR with priority 0-16 must be category 2; priority 17 must be category 1
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( next.getModelPriority() == 17 && next.isCategory2() ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' has priority 17 but is not a category 1 ISR"));
			}
			if ( next.getModelPriority() >= 0 && next.getModelPriority() <= 16 && !next.isCategory2() ) {
				problems.add(new Problem(Problem.ERROR, "ISR '"+next.getName()+"' does not have priority 17 but is a category 1 ISR"));
			}
		}		
		
		// TODO what other checking for the ARM7 follows? [error]
	}

	/**
	 * Epilogue called after all ANSI C code has been output to the C source code file.
	 * The current writer is still setup for output to the the C source when this is called.
	 * 
	 * When overridden, this method allows target specific information to be added to the
	 * generated C source file.
	 * 
	 * For the ARM7 this includes:
	 * 
	 * 1. Generating the FIQ, IRQ, Protected (kernel) stacks, as well as dummy remaining ARM7 stacks.
	 * 2. The ARM PrimeCell VIC setup values (e.g. handles for Cat 2 ISRs).
	 *
	 * @see OSAnsiCGenerator#generate(String)
	 */
	@Override
	public void SourceCFileEpilogue() {
		
		/////////////////////////////////////////////////////////////////
		// 1. Generate stacks
		writeln(comment("ARM7 stacks"));

		// TODO generate stacks
		
		/////////////////////////////////////////////////////////////////
		// 2. Generate ARM PrimeCell VIC initial values
		writeln(comment("ARM PrimeCell VIC initial values"));
		
		// ISRType os_lpc21xx_VICDefVectAddr_init;
		//
		// Write out handle for default IRQ ISR
		write("ISRType "+ARM7_PREFIX+"VICDefVectAddr_init = ");
		TargetISR defaultIRQISR = getTargetISRByModelPriority(0);
		
		if(defaultIRQISR != null) {
			write(defaultIRQISR.getControlBlockName());
		}
		else {
			write(ARM7_SPURIOUS_CAT2_ISR_HANDLE);
		}
		
		// ISRType os_lpc21xx_VICVectAddr[0-15]_init;
		//
		// Write out handles for each IRQ priority (0-15)
		for(long modelPriority = 1; modelPriority <= 16; modelPriority++) {
			long reg = 16 - modelPriority;
			TargetISR vectIRQISR = getTargetISRByModelPriority(modelPriority);
			
			write("ISRType "+ARM7_PREFIX+"VICVectAddr"+reg+"_init = ");
			if(vectIRQISR != null) {
				assert vectIRQISR.isCategory2();
				writeln(vectIRQISR.getControlBlockName());
			}
			else {
				writeln(ARM7_SPURIOUS_CAT2_ISR_HANDLE);
			}			
		}
		
		// uint32 os_lpc21xx_VICVectCntl[0-15]_init;
		//
		// Write out bit masks for the devices that will be handled by the corresponding IRQ ISR
		for(long modelPriority = 1; modelPriority <= 16; modelPriority++) {
			long reg = 16 - modelPriority;
			TargetISR vectIRQISR = getTargetISRByModelPriority(modelPriority);

			assert vectIRQISR.isCategory2();
			
			write("uint32 "+ARM7_PREFIX+"VICVectCntl"+reg+"_init = ");
			if(vectIRQISR != null) {
				write("0x"+getTargetISRDeviceMask(vectIRQISR).toString(16)+"U");
			}
			else {
				writeln("0;");
			}			
		}
		
		// uint32 os_lpc21xx_VICIntSelect_init;
		//
		// Write out a bit mask where the interrupt sources are marked as FIQ or IRQ (1 = FIQ)
		BigInteger deviceFIQ = BigInteger.ZERO; 
		
		for ( TargetISR next : targetModel.getTargetISRs() ) {
			if ( !next.isCategory2() ) {
				deviceFIQ.or(this.getTargetISRDeviceMask(next));
			}
		}
		writeln("uint32 "+ARM7_PREFIX+"VICIntSelect_init = 0x"+deviceFIQ.toString(16)+"U");
	}
	
	/**
	 * Implementation of generator for ARM7 target.
	 * 
	 * This method overrides the normal generate in order that it can produce a target specific 
	 * assembly language file for the ARM7 target.
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
		
		//////////////////////////////////////////////////////////
		// No specific ARM7 generated assembly

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
	 * Constructor used to instantiate the OSARM7Generator.
	 * 
	 * @param targetModel the root TargetCpu of the Target Model for which generation is to be performed
	 * @param logger a PrintWriter that is to be used for logging during generation (may be null)
	 * @param loggerPrefix the prefix attached to each logged message (may be null)	 * 
	 */
	public OSARM7Generator(TargetCpu targetModel, PrintWriter logger, String loggerPrefix) {	
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
								32,					// maxEventBits
								32,					// maxTaskPriorities
								17,					// maxISRPriorities
								UINT32,				// tickTypeSize TickType is a uint32 on this platform
								UINT32,				// unatTypeSize unat is uint32 on this platform
								UINT32,				// osBlockTypeSize
								4096				// maxStackSize, TODO decide on a sensible value for this
		);
		
		// Setup the platformInfo stack info. for this target/compiler
		// TODO calibration tests will provide these numbers semi-automatically. See also wiki1148.
		platformInfo.setUpStackDetais(
				40, 	// defaultTaskStackSize
				32, 	// defaultISRStackSize
				32, 	// defaultPreTaskHookStackSize
				32,		// defaultPostTaskHookStackSize								
				0, 		// stackOverflowSpace  					TODO calc and add this value
				4,		// kernelPreTaskEntryUsage				TODO calc and add this value
				0,		// kernelPreISREntryUsage 				TODO calc and add this value
				-2,		// topOfStackCheckOffset
				4,		// kernelPreTaskHookEntryUsage
				false,	// isAscendingStack						ARM7 ABI defines stacks as descending..
				false,	// isPostOffsetSP						.. and "full" (i.e. SP points to last used item)
				4		// stackAlignmentBytes
		);		
		
		// setup the stack overhead values, the platform uses the maximum of the passed values
		platformInfo.setupStdStatusExtStackKernelUsage(0x16); // TODO add numbers generated from calib1 program
		platformInfo.setupExtStatusExtStackKernelUsage(0x16); // TODO add numbers generated from calib1 program		

		// Now that architecture information is known, ask the target model to prepare for generation.
		targetModel.preGenProcessing(platformInfo);
	}	
}
