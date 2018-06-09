package simulator;

/**
 * <pre>
 * Class Name: Instruction
 * Description: loose wrapper around an instruction code, allows getting different fields
 * 
 * Version information:
 * $RCSfile: Instruction.java,v $
 * $Revision: 1.6 $
 * $Date: 2012/05/24 00:56:54 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public final class Instruction {
	// [code int local value of the instruction code]
	// [INSTRUCTION_LENGTH int local constant for number of bits in instruction]
	private int code;
	private static final int INSTRUCTION_LENGTH = 32;

	public Instruction(int instructionCode) {
		this.code = instructionCode;
	}

	/**
	 * <pre>
	 * Procedure Name: getBits
	 * Description: get consecutive bits from the instruction
	 * 
	 * Specification reference code(s): SX none
	 * 
	 * 
	 * Error Conditions Tested: values out of bounds of instruction bits
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
	 * @param start
	 *            is the start of the bit sequence to retrieve (indexed at 1)
	 * @param length
	 *            is the number of bits to retrieve
	 * @return the value of the given bits
	 */
	private int getBits(int start, int length) {
		start = INSTRUCTION_LENGTH + 1 - start - length;
		return (this.code & (~0 << INSTRUCTION_LENGTH - length >>> INSTRUCTION_LENGTH
				- length - start)) >>> start;
	}

	/**
	 * <pre>
	 * Procedure Name: getOpcode
	 * Description: get the opcode for the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: values out of bounds of instruction bits
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
	 * @return the value of the given bits
	 */
	public int getOpcode() {
		int start = 1, length = 6;
		return this.getBits(start, length);
	}

	/**
	 * <pre>
	 * Procedure Name: getBinary
	 * Description: get the binary of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: value not set
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
	 * @return a string of the binary digits of the instruction
	 */
	public String getBinary() {
		String result = Integer.toBinaryString(this.code);
		while (result.length() < INSTRUCTION_LENGTH) {
			result = "0" + result;
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: getOrderFlag
	 * Description: get the order bit of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: values out of bounds of instruction bits
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
	 * @return true iff order bit is 1
	 */
	public boolean getOrderFlag() {
		int start = 7, length = 1;
		return this.getBits(start, length) == 1;
	}

	/**
	 * <pre>
	 * Procedure Name: getReg
	 * Description: get the value of the register field of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return the value of the given bits
	 */
	public int getReg() {
		int start = 8, length = 3;
		return this.getBits(start, length);
	}

	/**
	 * <pre>
	 * Procedure Name: getXReg
	 * Description: get the value of the index register field of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return the value of the given bits
	 */
	public int getXReg() {
		int start = 11, length = 3;
		return this.getBits(start, length);
	}

	/**
	 * <pre>
	 * Procedure Name: getNumberWords
	 * Description: get the value fo the number of words field of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return the value of the given bits
	 */
	public int getNumberWords() {
		int start = 14, length = 4;
		return this.getBits(start, length);
	}

	/**
	 * <pre>
	 * Procedure Name: getStackFlag
	 * Description: get the value of the stack bit of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return true iff stack bit is active
	 */
	public boolean getStackFlag() {
		int start = 18, length = 1;
		return this.getBits(start, length) == 1;
	}

	/**
	 * <pre>
	 * Procedure Name: getLiteral
	 * Description: get the literal bit of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return true iff literal bit is active
	 */
	public boolean getLiteralFlag() {
		int start = 19, length = 1;
		return this.getBits(start, length) == 1;
	}

	/**
	 * <pre>
	 * Procedure Name: getMemory
	 * Description: get the value of the memory field of the instruction
	 * 
	 * Specification reference code(s): SX
	 * 
	 * Error Conditions Tested: N/A
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
	 * @return the value of the given bits
	 */
	public int getMemory() {
		int start = 20, length = 13;
		int val = this.getBits(start, length);
		// need to extend sign bit from 20th bit
		return (val << (INSTRUCTION_LENGTH - length)) >> (INSTRUCTION_LENGTH - length);
	}

}
