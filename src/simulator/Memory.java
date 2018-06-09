package simulator;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * <pre>
 * Class Name: Memory
 * Description: Memory for the urban simulator
 * 
 * Version information:
 * $RCSfile: Memory.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/05/23 17:40:36 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public final class Memory {
	// [instance Memory local singleton instance of class]
	// [NUM_INSTRUCTION_HEX_DIGITS int local constant for number of hex digits
	// in instruction]
	// [NUM_ADDRESS_HEX_DIGITS int local constant for number of hex digits in
	// address]
	// [MEMORY_SIZE int local constant for number of words in memory]
	// [mem int[] local contents of memory]
	// [startAddr int local start address of memory]
	// [endAddr int local end address of memory]
	// [execAddr int local execution start of memory]
	// [name String local name of program in memory]

	private static Memory instance = new Memory();
	private static int NUM_INSTRUCTION_HEX_DIGITS = 8;
	private static int NUM_ADDRESS_HEX_DIGITS = 4;

	final int MEMORY_SIZE = 4096;

	private int[] mem;
	private int startAddr = 0, endAddr = 4095;
	private int execAddr;
	private String name;

	public class MemoryAccessException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private Memory() {
		this.mem = new int[this.MEMORY_SIZE];
	}

	/**
	 * <pre>
	 * Procedure Name: getInstance
	 * Description: get the instance of the singleton memory
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: singleton not instantiated
	 * Error messages generated:
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
	 * @return instance of the singeton
	 */
	public static Memory getInstance() {
		return instance;
	}

	/**
	 * <pre>
	 * Procedure Name: reset
	 * Description: reset the singleton memory
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
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
	 */
	public static void reset() {
		instance = new Memory();
	}

	/**
	 * <pre>
	 * Procedure Name: read
	 * Description: read value from memory
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to read from
	 * @return contents of memory at the given address
	 * @throws MemoryAccessException
	 *             if the memory access was out of bounds
	 */
	public int read(int addr) throws MemoryAccessException {
		if (!isValidAddr(addr)) {
			throw new MemoryAccessException();
		} else {
			return this.mem[addr];
		}
	}

	/**
	 * <pre>
	 * Procedure Name: write
	 * Description: write a value to memory
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to write to
	 * @param val
	 *            is the value to store at the given address
	 * @throws MemoryAccessException
	 *             if the memory access was out of bounds
	 */
	public void write(int val, int addr) throws MemoryAccessException {
		if (!isValidAddr(addr)) {
			throw new MemoryAccessException();
		} else {
			this.mem[addr] = val;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: setLowerBound
	 * Description: set the lower bound of active memory
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 21, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to set the bound to
	 * @throws MemoryAccessException
	 *             if the value was invalid
	 */
	public void setLowerBound(int addr) throws MemoryAccessException {
		if (!isValidAddr(addr)) {
			throw new MemoryAccessException();
		} else {
			this.startAddr = addr;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: setUpperBound
	 * Description: set the upper bound of active memory
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 21, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to set the bound to
	 * @throws MemoryAccessException
	 *             if the value was invalid
	 */
	public void setUpperBound(int addr) throws MemoryAccessException {
		if (!isValidAddr(addr)) {
			throw new MemoryAccessException();
		} else {
			this.endAddr = addr;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: setExecutionStart
	 * Description: set the start of execution for active memory
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 21, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to set as the start
	 * @throws MemoryAccessException
	 *             if the value was out of bounds
	 */
	public void setExecutionStart(int addr) throws MemoryAccessException {
		if (!isValidAddr(addr)) {
			throw new MemoryAccessException();
		} else {
			this.execAddr = addr;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getExecutionStart
	 * Description: get the address for the start of execution
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: execution start not set
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 21, 2012
	 * @author Michael
	 * @return the address of the start of execution
	 */
	public int getExecutionStart() {
		return this.execAddr;
	}

	/**
	 * <pre>
	 * Procedure Name: isValidAddr
	 * Description: determine if a given address is valid
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: start/end not set
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 21, 2012
	 * @author Michael
	 * @param addr
	 *            is the address to check
	 * @return true iff address is valid
	 */
	public boolean isValidAddr(int addr) {
		return (addr >= this.startAddr) && (addr <= this.endAddr);
	}

	/**
	 * <pre>
	 * Procedure Name: setProgramName
	 * Description: set the name for the program in memory
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param programName
	 *            is the name of the program
	 */
	public void setProgramName(String programName) {
		this.name = programName;
	}

	/**
	 * <pre>
	 * Procedure Name: getProgramName
	 * Description: get the name of the program in memory
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: program name not set
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @return name of the program
	 */
	public String getProgramName() {
		return this.name;
	}

	/**
	 * <pre>
	 * Procedure Name: dump
	 * Description: dump contents of memory
	 * 
	 * Specification reference code(s): none
	 * 
	 * Error Conditions Tested: memory value not yet set
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 23, 2012 updated to conform to documentation standards
	 * Michael May 19, 2012 added test for out of bounds value
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param out
	 *            is the stream to output to
	 */
	public void dump(OutputStream out) {
		PrintStream ps = new PrintStream(out);
		final int numPerLine = 5;

		for (int i = this.startAddr; i <= this.endAddr; i++) {
			if (i % numPerLine == 0) {
				ps.println();
				ps.print("LC:" + convertAddressToHex(i) + "\t");
			}

			ps.print(convertInstructionToHex(this.mem[i]) + "\t");
		}
		ps.println();
	}

	/**
	 * <pre>
	 * Procedure Name: convertinstructionToHex
	 * Description: convert an instruction value to hex
	 * 
	 * Specification reference code(s): N/A
	 * 
	 * Error Conditions Tested: negative number, invalid number of digits
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @author Michael
	 * @param num
	 *            is the number to be converted
	 * @return a string representing the hex value of the passed integer
	 */
	public static String convertInstructionToHex(int num) {
		String val = Integer.toHexString(num).toUpperCase();
		while (val.length() < NUM_INSTRUCTION_HEX_DIGITS) {
			val = "0" + val;
		}
		return val;
	}

	/**
	 * <pre>
	 * Procedure Name: convertAddressToHex
	 * Description: convert an address value to hex
	 * 
	 * Specification reference code(s): N/A
	 * 
	 * Error Conditions Tested: negative number, invalid number of digits
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @author Michael
	 * @param num
	 *            is the number to be converted
	 * @return a string representing the hex value of the passed integer
	 */
	public static String convertAddressToHex(int num) {
		String val = Integer.toHexString(num).toUpperCase();
		while (val.length() < NUM_ADDRESS_HEX_DIGITS) {
			val = "0" + val;
		}
		return val;
	}
}
