package assembler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <pre>
 * Class Name:
 * Description:
 * 
 * Version information:
 * $RCSfile: Line.java,v $
 * $Revision: 1.15 $
 * $Date: 2012/05/10 00:30:21 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public class Line {

	// [instTable InstrucTable local instruction table object for lookups]
	// [undefined String local string used to refer to undefined values]
	// [rawLine String local representing raw source code of line instance]
	// [label String local representing label of line instance]
	// [directive String local representing directive of line instance]
	// [instruction String local representing instruction of line instance]
	// [ops List<operand> local list of operands for a given line]
	// [errors Set<Integer> local errors set of errors for a given line]
	// [binary String global binary equivalents for the line]
	// [modifyRecords List<String> local list of the modification records for
	// the line]
	// [relocationFlag char global relocation flag for the given line (A/E/R/C)]

	private InstrucTable instTable = InstrucTable.getInstance();
	private static String undefined = "";

	public String rawLine, label, directive, instruction;
	public int address;
	public List<Operand> ops;
	public Set<Integer> errors;
	public String binary;

	// each string looks like "sign:label" e.g. "+:LBL1"
	public List<String> modifyRecords;

	// OK, this is slightly confusing:
	// If an ADRC references multiple local labels, then we have to have
	// multiple modifications of the form ":+:prgm_name:". For example, given
	// the line:
	// ADRC EX:loc1 + loc2 + loc3 - loc4;
	// where the LC of loc1 = 10, loc2 = 20, loc3 = 40, loc4 = 5
	// We might generate the following records:
	// T:XXXX:65:c:3:prgm_name
	// M:XXXX:3:+:prgm_name:+:prgm_name:+:prgm_name:prgm_name
	// This would be represented by a localModifies count of 3 (it may be
	// negative or zero as well)
	public int localModifies;
	public char relocationFlag;

	public Line(int address, String rawLine, String label, String instruction,
			String directive, List<Operand> ops, Set<Integer> errors) {
		this.address = address;
		this.label = label;
		this.instruction = instruction;
		this.directive = directive;
		this.ops = ops;
		this.errors = errors;
		this.rawLine = rawLine;
		this.relocationFlag = 'A';
		this.modifyRecords = new ArrayList<String>();
		this.binary = "";
	}

	/**
	 * <pre>
	 * Procedure Name: isUndefined
	 * Description: returns whether or not a given string is undefined (empty).
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: tried checking empty string,
	 * nonempty string, null string
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 added null check
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @param temp
	 *            is the string to check
	 * @return true iff string is undefined
	 */
	private static boolean isUndefined(String temp) {
		if (temp == null) {
			return false;
		}
		return temp.equals(undefined);
	}

	/**
	 * <pre>
	 * Procedure Name: isDirective
	 * Description: check if a line is a directive.
	 * 
	 * Specification reference code(s): DSX, DX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: checked with empty string, nonempty string
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @return true iff line is a directive
	 */
	public boolean isDirective() {
		return !isUndefined(this.directive);
	}

	/**
	 * <pre>
	 * Procedure Name: hasLabel
	 * Description: check if a line has a label.
	 * 
	 * Specification reference code(s): DS3.X, DS1.X, DX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: checked with labeled line, directive line
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @return true iff line has a label
	 */
	public boolean hasLabel() {
		return !this.label.equals(undefined);
	}

	/**
	 * <pre>
	 * Procedure Name: isInstruction
	 * Description: check if a line is an instruction.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: checked with instruction line,
	 * directive line
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @return true iff line is an instruction
	 */
	public boolean isInstruction() {
		return !this.instruction.equals(undefined);
	}

	/**
	 * <pre>
	 * Procedure Name: getOpcodeBinary
	 * Description: get the binary equivalent of an opcode.
	 * 
	 * Specification reference code(s): AO2
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: nonexistant instruction,
	 * null reference to instruction table
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 15, 2012 now uses instruction table for lookup
	 * Michael Apr 16, 2012 trims binary representation
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @return the string binary representation of the binary equivalent of
	 *         the instruction
	 */
	public String getOpcodeBinary() {
		String binary = Integer.toBinaryString(instTable
				.opCode(this.instruction));

		// trim to size
		while (binary.length() < 6) {
			binary = "0" + binary;
		}
		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: maximumError
	 * Description: get the maximum error number for the line
	 * 
	 * Specification reference code(s): AO2
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @return the maximum error for the line
	 */
	public static int maximumError(Set<Integer> errors) {
		// [max int local maximum error number for a line]
		// [error int local error number for the line]
		int max = 0;
		for (int error : errors) {
			if (error > max) {
				max = error;
			}
		}
		return max;

	}

	/**
	 * <pre>
	 * Procedure Name: setBinary
	 * Description: sets the binary equivalent for a line
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: invalid mnemonic
	 * Error messages generated: out of bounds errors
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7, 2012 moved into line class, updated to conform with documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param line
	 *            is the line to set the binary of
	 * @param newOps
	 *            are the evaluated operands of the line
	 */
	public void setBinary(List<Operand> newOps) {
		// [binary String local binary equivalent of instruction]
		String binary;

		if (this.isInstruction()) {
			binary = instTable.getBinary(this, this.instruction, newOps,
					this.errors);
		} else if (this.isDirective()) {
			binary = instTable.getBinary(this, this.directive, newOps,
					this.errors);
		} else {
			binary = "";
		}
		// if (this.isInstruction()) {
		// binary = instTable.getBinary(this.instruction, newOps, this.errors);
		// }
		// else if (InstrucTable.generatesCode(this.directive)) {
		// binary = instTable.getBinary(this.directive, newOps, this.errors);
		// } else {
		// binary = undefined;
		// }
		this.binary = binary;
	}

	/**
	 * <pre>
	 * Procedure Name: getHex
	 * Description: get the hex value of the instruction
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: call when instruction binary not yet defined
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @return string of hex digits representing instruction
	 */
	public String getHex() {
		// [big BigInteger local temporary integer used to hold the value of the
		// instruction binary]
		String result = undefined;

		try {
			BigInteger big = new BigInteger(this.binary, 2);
			result = String.format("%08x", big);

		} catch (Exception e) {
			result = undefined;
		}

		return result.toUpperCase();
	}
}
