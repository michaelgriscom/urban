package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import simulator.Memory.MemoryAccessException;

/**
 * <pre>
 * Class Name: Processor
 * Description: CPU for the urban simulator, fetches and executes instructions
 * from memory
 * 
 * Version information:
 * $RCSfile: Processor.java,v $
 * $Revision: 1.17 $
 * $Date: 2012/05/30 21:11:04 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public final class Processor {

	class OverflowException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	class DivideByZeroException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	class InvalidBinaryException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	// [mem Memory local instance of the Memory singleton]
	// [in InputStream local stream to get user input from]
	// [out OutputStream local stream to output to]
	// [aRegisters int[] local arithmetic register values]
	// [xRegisters int[] local index register values]
	// [stack Stack<Integer> local values of simulator]
	// [NUM_ARITHMETIC_REGISTERS int local constant for the number of arithmetic
	// registers]
	// [NUM_INDEX_REGISTERS int local constant for the number of index
	// registers]
	// [BITS_IN_WORD int local constant for the number of bits in a word]

	private Memory mem;
	// IO
	private static InputStream in;
	private static PrintStream out;

	// registers
	int[] aRegisters;
	int[] xRegisters;
	int LC;

	// stack
	Stack<Integer> stack;

	private static Processor instance = new Processor();
	private final static int NUM_ARITHMETIC_REGISTERS = 8;
	private final static int NUM_INDEX_REGISTERS = 7;
	private final static int BITS_IN_WORD = 32;

	private Processor() {
		this.aRegisters = new int[NUM_ARITHMETIC_REGISTERS];
		this.xRegisters = new int[NUM_INDEX_REGISTERS];
		this.stack = new Stack<Integer>();
		loadInstructions("instructionTypes.txt");
	}

	public enum InstructionType {
		DATA_MOVEMENT, ARITHMETIC, REGISTER_TOOLS,

		SHIFT_MANIPULATE, LOGICAL, JUMP, IO, STACK,

		CONTROL, UNDEFINED
	}

	// [mnemonics Map<Integer,String> local map from opcodes to string
	// mnemonics]
	// [types Map<Integer,InstructionType> local map from opcodes to instruction
	// types]
	// [debug boolean local flag for debug mode]

	private Map<Integer, String> mnemonics = new HashMap<Integer, String>();
	private Map<Integer, InstructionType> types = new HashMap<Integer, InstructionType>();
	private boolean debug = false;

	/**
	 * <pre>
	 * Procedure Name: loadInstructions
	 * Description: load urban instructions to create opcode->mnemonic/type maps
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform with documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param filename
	 *            is the name of the file containing the instructions
	 */
	public void loadInstructions(final String filename) {
		// [opCode int local opcode of instruction]
		// [type String local type of instruction]
		// [mnemonic String local mnemonic of instruction]

		try {
			Scanner input = new Scanner(new File(filename));
			while (input.hasNext()) {
				String type = input.next();
				String mnemonic = input.next();
				int opCode = input.nextInt(2);

				this.mnemonics.put(opCode, mnemonic);
				this.types.put(opCode, InstructionType.valueOf(type));

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getInstance
	 * Description: return the instance of the processor singleton
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @return the singleton instance of processor
	 */
	public static Processor getInstance() {
		return instance;
	}

	/**
	 * <pre>
	 * Procedure Name: reset
	 * Description: reset the singleton instance
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: unreset memory
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 */
	public static void reset() {
		instance = new Processor();
	}

	/**
	 * <pre>
	 * Procedure Name: setMemory
	 * Description: set the memory of the processor
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: reset memory after setting
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param mem
	 *            is the memory instance
	 */
	public void setMemory(Memory mem) {
		this.mem = mem;
	}

	/**
	 * <pre>
	 * Procedure Name: setLC
	 * Description: set the initial LC of the processor
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param initialLC
	 *            is the address of the first instruction to execute
	 */
	public void setLC(int initialLC) {
		// lc is incremented on execute, so store the decremented value
		this.LC = initialLC - 1;
	}

	public int getLC() {
		return this.LC;
	}

	public Stack<Integer> getStack() {
		return this.stack;
	}

	/**
	 * <pre>
	 * Procedure Name: getArithmeticRegister
	 * Description: get the value for a given arithmetic register
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: out of bounds index
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param reg
	 *            is the number of the register to get
	 * @return the value of the given register
	 */
	public int getArithmeticRegister(int reg) {
		return this.aRegisters[reg];
	}

	/**
	 * <pre>
	 * Procedure Name: setArithmeticRegister
	 * Description: set the value of an arithmetic register
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param reg
	 *            is the number of the register to set
	 * @param val
	 *            is the value to set the register to
	 */
	public void setArithmeticRegister(int reg, int val) {
		this.aRegisters[reg] = val;
	}

	/**
	 * <pre>
	 * Procedure Name: getIndexRegister
	 * Description: get the value of a given index register
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 23, 2012
	 * @author Michael
	 * @param reg
	 *            is the number of the register to get
	 * @return the value of the index register
	 */
	public int getIndexRegister(int reg) {
		if (reg == 0) {
			return 0;
		} else {
			return this.xRegisters[reg - 1];
		}
	}

	/**
	 * <pre>
	 * Procedure Name: setIndexRegister
	 * Description: set the value for a given index register
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 23, 2012
	 * @author Michael
	 * @param reg
	 *            is the register number to set
	 * @param val
	 *            is the value to set the register to
	 */
	public void setIndexRegister(int reg, int val) {
		this.xRegisters[reg - 1] = val;
	}

	/**
	 * <pre>
	 * Procedure Name: executeInstruction
	 * Description: execute the next instruction
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: out of bounds LC, address out of bounds, invalid binary
	 * Error messages generated: divide by zero, overflow, invalid opcode
	 * 
	 * Modification Log (who when and why):
	 * Michael May 20, 2012 added overflow testing
	 * Michael May 21, 2012 corrected invalid opcode error
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @return true if the execution should be halted
	 */
	public boolean executeInstruction() {
		// [instructionCode int local instruction value]
		// [haltFlag boolean local flag if execution must be halted]
		// [inst Instruction local current instruction]
		// [bin String local binary of current instruction]

		this.LC++;

		int instructionCode;

		try {
			instructionCode = this.mem.read(this.LC);
		} catch (MemoryAccessException e) {
			printError("LC out of bounds");
			return true;
		}

		boolean haltFlag = false;

		Instruction inst = new Instruction(instructionCode);

		try {
			if (this.debug) {
				out.println("---------------------------------------");
				out.println("LC=" + Memory.convertAddressToHex(this.LC)
						+ "\tMEM(LC)=" + inst.getBinary() + "\topc="
						+ inst.getBinary().substring(0, 6) + "\tMEM(LC)="
						+ Memory.convertInstructionToHex(instructionCode));

				out.println("Mnemonic: " + this.mnemonics.get(inst.getOpcode())
						+ "\tInstruction type: "
						+ this.types.get(inst.getOpcode()));

			}

			if (this.debug) {
				out.println("Before execution:");
				out.println("literal?=" + inst.getLiteralFlag()
						+ "\tdestination reg?=" + inst.getOrderFlag());
				if (inst.getLiteralFlag()) {
					out.print("values:\tConstant="
							+ Integer.toHexString(inst.getMemory()));
				} else {
					out.print("values:\tMEM="
							+ Memory.convertAddressToHex((inst.getMemory()))
							+ "\tMEM(S)="
							+ Memory.convertInstructionToHex(this.mem.read(inst
									.getMemory())));
				}
				out.println("\treg="
						+ Memory.convertInstructionToHex(getArithmeticRegister(inst
								.getReg()))
						+ "\txreg="
						+ Memory.convertInstructionToHex(getIndexRegister(inst
								.getXReg())) + "\tNwords="
						+ Memory.convertAddressToHex(inst.getNumberWords()));
			}

			switch (this.types.get(inst.getOpcode())) {
			case ARITHMETIC:

				executeArithmetic(inst);
				break;
			case DATA_MOVEMENT:
				executeDataMovement(inst);
				break;

			case REGISTER_TOOLS:
				executeRegisterTool(inst);
				break;

			case SHIFT_MANIPULATE:
				executeShiftManipulate(inst);
				break;

			case LOGICAL:
				executeLogical(inst);
				break;

			case JUMP:
				executeJump(inst);
				break;

			case IO:
				executeIO(inst);
				break;

			case STACK:
				executeStack(inst);
				break;

			case CONTROL:
				haltFlag = executeControl(inst);
				break;

			default:
				throw new InvalidBinaryException();
			}
		} catch (MemoryAccessException e) {
			printError("Memory access out of bounds");

		} catch (InvalidBinaryException e) {
			printError("Invalid binary encountered");

		} catch (NullPointerException e2) {
			printError("Invalid opcode encountered");

		} catch (DivideByZeroException e) {
			printError("Divide by zero");

		} catch (OverflowException e) {
			printError("Overflow");

		} catch (IOException e) {
			printError("IO failure");

		} finally {
			if (this.debug) {
				out.println("After execution:");
				out.println("literal?=" + inst.getLiteralFlag()
						+ "\tdestination reg?=" + inst.getOrderFlag());
				if (inst.getLiteralFlag()) {
					out.print("values:\tConstant="
							+ Integer.toHexString(inst.getMemory()));
				} else {
					try {
						out.print("values:\tMEM="
								+ Memory.convertAddressToHex((inst.getMemory()))
								+ "\tMEM(S)="
								+ Memory.convertInstructionToHex(this.mem
										.read(inst.getMemory())));
					} catch (MemoryAccessException e) {

					}
				}
				out.println("\treg="
						+ Memory.convertInstructionToHex(getArithmeticRegister(inst
								.getReg()))
						+ "\txreg="
						+ Memory.convertInstructionToHex(getIndexRegister(inst
								.getXReg())) + "\tNwords="
						+ Memory.convertAddressToHex(inst.getNumberWords()));
			}

		}

		return haltFlag;
		// [e multiple use]
	}

	/**
	 * <pre>
	 * Procedure Name: printError
	 * Description: print a given runtime error
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: current runtime error
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param string
	 *            is the error string
	 */
	private void printError(String str) {
		out.println("ERROR: " + str + " at LC "
				+ Memory.convertAddressToHex(this.LC));
	}

	/**
	 * <pre>
	 * Procedure Name: executeControl
	 * Description: execute a control type instruction
	 * 
	 * Specification reference code(s): CX
	 * 
	 * 
	 * Error Conditions Tested: wrong instruction type
	 * Error messages generated: incorrect binary, out of bounds value
	 * 
	 * Modification Log (who when and why):
	 * Michael May 21, 2012 added DMP implementation
	 * Michael May 20, 2012 added out of bounds error check
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @return true iff halt is encountered
	 * @throws InvalidBinaryException
	 *             if the binary encountered is incorrect
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 */
	private boolean executeControl(Instruction inst)
			throws InvalidBinaryException, MemoryAccessException {
		String mnem = this.mnemonics.get(inst.getOpcode());
		boolean haltFlag = false;
		if (mnem.equals("DMP")) {
			int fc = inst.getMemory();
			switch (fc) {
			case 1:
				dump1();
				break;
			case 2:
				dump2();
				break;
			case 3:
				dump1();
				dump2();
				break;
			default:
				throw new InvalidBinaryException();
			}

		} else if (mnem.equals("HLT")) {
			haltFlag = true;
			out.println(inst.getMemory());
		} // otherwise, nop

		return haltFlag;
	}

	/**
	 * <pre>
	 * Procedure Name: dump2
	 * Description: dump format 2
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: memory not set
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 */
	private void dump2() {
		this.mem.dump(out);
	}

	/**
	 * <pre>
	 * Procedure Name: dump1
	 * Description: perform level 1 dump
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: out of bounds value
	 * Error messages generated: memory out of bounds
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @throws MemoryAccessException
	 *             if a memory access was out of bounds
	 */
	private void dump1() throws MemoryAccessException {
		// [regsPerLine int local number of registers to print per line]
		// [registerVal int local value of a register in decimal]
		// [registerVal String local value of a register in hex]

		final int regsPerLine = 4;

		out.println("LC pg," + this.LC + " WORD="
				+ Memory.convertInstructionToHex(this.mem.read(this.LC)));
		for (int i = 0; i < NUM_ARITHMETIC_REGISTERS; i++) {
			int registerVal = getArithmeticRegister(i);
			String regVal = Memory.convertInstructionToHex(registerVal);

			out.print("R" + i + "=" + regVal);
			if (i % regsPerLine == (regsPerLine - 1)) {
				out.println();
			}
		}
		for (int i = 1; i <= NUM_INDEX_REGISTERS; i++) {
			int registerVal = getIndexRegister(i);
			String regVal = Memory.convertInstructionToHex(registerVal);

			out.print("XR" + (i + 1) + "=" + regVal);
			if (i % regsPerLine == (regsPerLine - 1)) {
				out.println();
			}
		}
		out.println();
	}

	/**
	 * <pre>
	 * Procedure Name: executeStack
	 * Description: execute a stack instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * 
	 * Error Conditions Tested: invalid binary, out of range value
	 * Error messages generated: memory out of bounds
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws InvalidBinaryException
	 *             if invalid binary is encountered
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 */
	private void executeStack(Instruction inst) throws InvalidBinaryException,
			MemoryAccessException {
		// [constantField int local constant field value of memory]
		// [arg int local argument for the instruction]
		// [top int local value on top of stack]
		// [memoryLocation int local value of memoryLocation in instruction]

		String mnem = this.mnemonics.get(inst.getOpcode());
		int constantField = inst.getMemory();
		int arg, top;

		if (mnem.equals("PSH")) {
			if (inst.getLiteralFlag()) {
				arg = constantField;
			} else {
				int memoryLocation = getEffectiveAddress(inst);
				arg = this.mem.read(memoryLocation);
			}
			this.stack.push(arg);

		} else if (mnem.equals("POP")) {
			if (inst.getLiteralFlag()) {
				throw new InvalidBinaryException();
			} else {
				int memoryLocation = getEffectiveAddress(inst);
				top = this.stack.pop();
				this.mem.write(top, memoryLocation);
			}
		} else { // mnem is PST
			int result;
			top = this.stack.pop();
			if (inst.getLiteralFlag()) {
				arg = constantField;
			} else {
				int memoryLocation = getEffectiveAddress(inst);
				arg = this.mem.read(memoryLocation);
			}
			if (top == arg) {
				result = 0;
			} else if (top < arg) {
				result = 2;
			} else { // (top > arg)
				result = 3;
			}
			setArithmeticRegister(inst.getReg(), result);
		}
	}

	/**
	 * <pre>
	 * Procedure Name: executeIO
	 * Description: execute an IO instruction
	 * 
	 * Specification reference code(s): IOX
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 * @throws IOException
	 *             if there is an error with IO
	 */
	private void executeIO(Instruction inst) throws MemoryAccessException,
			IOException {
		// [startAddr int local value of address]

		String mnem = this.mnemonics.get(inst.getOpcode());
		int startAddr = getEffectiveAddress(inst);

		if (mnem.equals("IWSR")) {
			int val = 0;
			for (int i = 0; i < inst.getNumberWords(); i++) {
				val = this.mem.read(startAddr + i);
				out.print(val);
			}
		} else if (mnem.equals("IRKB") || mnem.equals("CRKB")) {
			int val = 0;
			for (int i = 0; i < inst.getNumberWords(); i++) {
				val = in.read();

				this.mem.write(val, startAddr + i);
			}
		} else { // if (mnem.equals("CWSR")) {
			int val = 0;
			for (int i = 0; i < inst.getNumberWords(); i++) {
				val = this.mem.read(startAddr + i);
				out.print((char) val);
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: executeJump
	 * Description: execute jump instruction
	 * 
	 * Specification reference code(s): JTX
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards,
	 * fixed RET to reflect changes to specs, added debug
	 * Michael May 22, 2012 corrected register access to use local function
	 * Michael May 18, 2012 corrected jump logic
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 * @throws InvalidBinaryException
	 *             if instruction has invalid binary
	 */
	private void executeJump(Instruction inst) throws InvalidBinaryException,
			MemoryAccessException {
		// [reg int local arithmetic register number of instruction]
		// [xreg int local index register of instruction]
		// [jump boolean local flag if jump should be taken]
		// [dest int local destination of jump]
		// [mnem String local mnemonic of instruction]

		String mnem = this.mnemonics.get(inst.getOpcode());
		int reg = inst.getReg();
		int xreg = inst.getXReg();

		boolean jump = false;
		int dest = getEffectiveAddress(inst);

		if (mnem.equals("TREQ")) {
			jump = (this.getArithmeticRegister(reg) == 0);
		} else if (mnem.equals("TRLT")) {
			jump = (this.getArithmeticRegister(reg) < 0);
		} else if (mnem.equals("TRGT")) {
			jump = (this.getArithmeticRegister(reg) > 0);
		} else if (mnem.equals("TR")) {
			// no xreg allowed
			if (inst.getXReg() != 0) {
				throw new InvalidBinaryException();
			}
			jump = true;
		} else if (mnem.equals("RET")) {
			// no xreg allowed
			if (inst.getXReg() != 0) {
				throw new InvalidBinaryException();
			}
			jump = true;
			dest = getArithmeticRegister(inst.getReg());

		} else if (mnem.equals("TRDR")) {
			if (getIndexRegister(xreg) == 0 || getArithmeticRegister(reg) == 0) {
				jump = true;
			} else {
				this.xRegisters[xreg] -= 1;
			}
		} else if (mnem.equals("TRLK")) {
			setArithmeticRegister(reg, this.LC + 1);

			jump = true;
		} else if (mnem.equals("SKT")) {
			setArithmeticRegister(reg, this.stack.size());
		}

		if (this.debug) {
			out.println("jump?=" + jump + "\tdestination="
					+ Memory.convertAddressToHex(dest));
		}

		if (jump) {
			this.LC = dest;
			// attempt a read to see if LC is out of bounds
			this.mem.read(this.LC);
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getEffectiveAddress
	 * Description: get the effective address of the instruction (mem field +
	 * contents of index register)
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to get the effective address of
	 * @return the effective address of the instruction
	 */
	private int getEffectiveAddress(Instruction inst) {
		return getIndexRegister(inst.getXReg()) + inst.getMemory();
	}

	/**
	 * <pre>
	 * Procedure Name: executeLogical
	 * Description: execute a logical instruction
	 * 
	 * Specification reference code(s): IMX
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards, added debug
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 */
	private void executeLogical(Instruction inst) throws MemoryAccessException {
		// [orderBit boolean local flag for if order bit is active]

		String mnem = this.mnemonics.get(inst.getOpcode());
		boolean orderBit = inst.getOrderFlag();

		int result = 0, reg = inst.getReg(), memory = getEffectiveAddress(inst);

		if (mnem.equals("AND")) {
			result = this.getArithmeticRegister(reg) & this.mem.read(memory);
		} else { // mnem == "OR"
			result = this.getArithmeticRegister(reg) | this.mem.read(memory);
		}

		if (orderBit) { // destination register
			setArithmeticRegister(reg, result);

		} else { // destination mem
			this.mem.write(result, memory);
		}
		if (this.debug) {
			out.println("result=" + result);
		}

	}

	/**
	 * <pre>
	 * Procedure Name: executeShiftManipulate
	 * Description: execute a shift of register manipulate instruction
	 * 
	 * Specification reference code(s): IMX
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards,
	 * fixed result storage, added debug
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws OverflowException
	 *             if the operation overflows
	 * @throws InvalidBinaryException
	 *             if invalid binary is encountered
	 */
	private void executeShiftManipulate(Instruction inst)
			throws OverflowException, InvalidBinaryException {
		String mnem = this.mnemonics.get(inst.getOpcode());
		int constantField = inst.getMemory() % BITS_IN_WORD;
		int reg = inst.getReg();
		int regVal = getArithmeticRegister(reg);

		if (mnem.equals("ISHR")) {
			regVal = regVal >>> constantField;
		} else if (mnem.equals("ISHL")) {
			regVal = regVal << constantField;
		} else if (mnem.equals("ISRA")) {
			regVal = regVal >> constantField;
		} else if (mnem.equals("ISLA")) {
			boolean wasNegative = (regVal < 0);
			regVal = regVal << constantField;
			if (!wasNegative && regVal < 0) {
				// sign change indicates overflow
				throw new OverflowException();
			}
		} else if (mnem.equals("ROL")) {
			regVal = (regVal << constantField)
					| (regVal >> (BITS_IN_WORD - constantField));

		} else if (mnem.equals("ROR")) {
			regVal = (regVal >> constantField)
					| (regVal << (BITS_IN_WORD - constantField));
		} else {
			throw new InvalidBinaryException();
		}

		setArithmeticRegister(reg, regVal);

		if (this.debug) {
			out.println("result=" + regVal);
		}

	}

	/**
	 * <pre>
	 * Procedure Name: executeRegisterTool
	 * Description: execute a register tool instruction
	 * 
	 * Specification reference code(s): ISX
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws InvalidBinaryException
	 *             if invalid binary is encountered
	 */
	private void executeRegisterTool(Instruction inst)
			throws InvalidBinaryException {
		String mnem = this.mnemonics.get(inst.getOpcode());
		if (mnem.equals("CLR")) {
			int xreg = inst.getXReg();
			int reg = inst.getReg();

			if (inst.getXReg() > 0) {
				setIndexRegister(xreg, 0);

			} else { // arithmetic
				if (reg == 0) { // can't be used to clear arithmetic 0
					throw new InvalidBinaryException();
				}
				this.aRegisters[reg] = 0;
			}
		} else if (mnem.equals("CLRA")) {
			this.aRegisters = new int[NUM_ARITHMETIC_REGISTERS];
		} else { // CLRX
			this.xRegisters = new int[NUM_INDEX_REGISTERS];
		}

	}

	/**
	 * <pre>
	 * Procedure Name: executeDataMovement
	 * Description: execute a data movement instruction
	 * 
	 * Specification reference code(s): MVX
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 * @throws OverflowException
	 *             if an overflow is encountered
	 */
	private void executeDataMovement(Instruction inst)
			throws MemoryAccessException, OverflowException {
		String mnem = this.mnemonics.get(inst.getOpcode());
		boolean orderBit = inst.getOrderFlag();

		int reg = inst.getReg(), memory = getEffectiveAddress(inst);

		if (mnem.equals("MOVD")) {
			if (orderBit) { // destination register
				this.aRegisters[reg] = this.mem.read(memory);

			} else { // destination mem
				this.mem.write(this.aRegisters[reg], memory);
			}
		} else { // movdn
			if (orderBit) { // destination register
				int val = this.mem.read(memory);
				if (val == Integer.MIN_VALUE) { // can't negate -2^31
					throw new OverflowException();
				}
				this.aRegisters[reg] = -val;

			} else { // destination mem
				int val = this.aRegisters[reg];
				if (val == Integer.MIN_VALUE) { // can't negate -2^31
					throw new OverflowException();
				}
				this.mem.write(-val, memory);
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: executeArithmetic
	 * Description: execute an arithmetic instruction
	 * 
	 * Specification reference code(s): IAX
	 * 
	 * 
	 * Error Conditions Tested: 
	 * Error messages generated: see exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael May 18, 2012 added storage of result, fixed effective memory calculation
	 * Michael May 23, 2012 updated to conform to documentation standards, added debug
	 * Michael May 22, 2012 corrected register access to use local function
	 * 
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param inst
	 *            is the instruction to execute
	 * @throws MemoryAccessException
	 *             if an out of bounds value is accessed
	 * @throws DivideByZeroException
	 *             if a divide by zero occurs
	 * @throws InvalidBinaryException
	 *             if the incorrect binary is encountered
	 * @throws OverflowException
	 *             if an overflow occurs
	 */
	private void executeArithmetic(Instruction inst)
			throws MemoryAccessException, DivideByZeroException,
			InvalidBinaryException, OverflowException {
		// [litBit boolean local flag for if literal bit is active]
		// [sourceArg int local value of source's argument]
		// [destinationArg int local value of destination's argument]

		int reg = inst.getReg(), nWords = inst.getNumberWords(), opCode = inst
				.getOpcode();
		int constantField = inst.getMemory();

		boolean litBit = inst.getLiteralFlag(), orderBit = inst.getOrderFlag();

		String mnem = this.mnemonics.get(opCode);
		int result = 0, destinationArg, sourceArg;

		if (orderBit) { // destination register
			destinationArg = this.aRegisters[reg];
			if (litBit) {
				sourceArg = constantField;
			} else {
				int memory = getEffectiveAddress(inst);
				sourceArg = this.mem.read(memory);
			}
		} else { // destination mem
			sourceArg = this.aRegisters[reg];
			if (litBit) {
				destinationArg = constantField;
			} else {
				int memory = getEffectiveAddress(inst);
				destinationArg = this.mem.read(memory);
			}
		}

		if (mnem.equals("IADD")) {
			result = sAdd(destinationArg, sourceArg);

		} else if (mnem.equals("IMAD")) {
			result = sAdd(sMult(destinationArg, sourceArg), sourceArg);

		} else if (mnem.equals("IAA")) {
			if (sourceArg == Integer.MIN_VALUE) {
				throw new OverflowException();
			}
			result = sAdd(destinationArg, Math.abs(sourceArg));

		} else if (mnem.equals("ISRG")) {
			if (!orderBit) { // destination must be a register
				throw new InvalidBinaryException();
			}
			result = destinationArg;
			for (int i = 0; i <= nWords; i++) {
				int memory = getEffectiveAddress(inst);
				result = sAdd(result, this.mem.read(memory + i));
			}

		} else if (mnem.equals("ISUB")) {
			result = sSub(destinationArg, sourceArg);

		} else if (mnem.equals("IMUL")) {
			result = sMult(sourceArg, destinationArg);

		} else if (mnem.equals("IDIV")) {
			result = sDiv(destinationArg, sourceArg);

		} else if (mnem.equals("PWR")) {
			int memory = getEffectiveAddress(inst);
			if (!orderBit) { // destination must be a register
				throw new InvalidBinaryException();
			}

			result = 1;
			for (int i = 0; i < memory; i++) {
				result = sMult(result, destinationArg);

			}
		} else {
			assert false : "Invalid arithmetic";
		}

		if (orderBit) { // destination register
			setArithmeticRegister(reg, result);

		} else { // destination mem
			int memory = getEffectiveAddress(inst);
			this.mem.write(result, memory);
		}

		if (this.debug) {
			out.println("result=" + result);
		}
	}

	/**
	 * <pre>
	 * Procedure Name: sDiv
	 * Description: safe divide with runtime error detection
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: see exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param a
	 *            is the first argument
	 * @param b
	 *            is the second argument
	 * @return the result of the operation
	 * @throws OverflowException
	 *             if an overflow occurs
	 * @throws DivideByZeroException
	 *             if a divide by zero occurs
	 */
	private int sDiv(int a, int b) throws OverflowException,
			DivideByZeroException {
		int result = 0;
		if (a == Integer.MIN_VALUE && b == -1) {
			throw new OverflowException();
		}
		try {
			result = a / b;
		} catch (ArithmeticException e) {
			throw new DivideByZeroException();
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: sSub
	 * Description: safe subtract with runtime error detection
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: see exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param a
	 *            is the first argument
	 * @param b
	 *            is the second argument
	 * @return the result of the operation
	 * @throws OverflowException
	 *             if an overflow occurs
	 */
	private int sSub(int a, int b) throws OverflowException {
		// [test long local value to test against integer result in order to
		// detect overflow]
		Long test = (long) a - (long) b;
		int result = a - b;
		if (test != result) {
			throw new OverflowException();
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: sMult
	 * Description: safe multiply with runtime error detection
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: see exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param a
	 *            is the first argument
	 * @param b
	 *            is the second argument
	 * @return the result of the operation
	 * @throws OverflowException
	 *             if an overflow occurs
	 */
	private int sMult(int a, int b) throws OverflowException {
		Long test = (long) a * (long) b;
		int result = a * b;
		if (test != result) {
			throw new OverflowException();
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: sAdd
	 * Description: safe add with runtime error detection
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: see exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Michael
	 * @param a
	 *            is the first argument
	 * @param b
	 *            is the second argument
	 * @return the result of the operation
	 * @throws OverflowException
	 *             if an overflow occurs
	 */
	private int sAdd(int a, int b) throws OverflowException {
		Long test = (long) a + (long) b;
		int result = a + b;
		if (test != result) {
			throw new OverflowException();
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: setIO
	 * Description: set the IO for instructions
	 * 
	 * Specification reference code(s):
	 * 
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param in
	 *            is the input stream
	 * @param out
	 *            is the output stream
	 */
	public static void setIO(InputStream in, OutputStream out) {
		Processor.in = in;
		Processor.out = new PrintStream(out);
	}

	/**
	 * <pre>
	 * Procedure Name: debugMode
	 * Description: turns on debug mode
	 * 
	 * Specification reference code(s): none
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 23, 2012
	 * @author Michael
	 */
	public void debugMode() {
		this.debug = true;
	}
}
