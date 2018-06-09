package simulatorTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * <pre>
 * Class Name: InstrucTable
 * Description: Class used to validate operand keywords and 
 * retrieve the opcode  for a mnemonic.
 * 
 * Version information:
 * $RCSfile: InstrucTable.java,v $
 * $Revision: 1.4 $
 * $Date: 2012/05/29 21:52:24 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public final class InstrucTable {

	private class Instruction {
		// [validOps Set<Set<String>> local stores a set with values that are
		// all
		// possible combinations of valid keywords]
		private Set<Set<String>> validOps;

		// [opCode int local stores the opcode for the instruction]
		private int opCode;

		public Instruction(int opCode, Set<Set<String>> validOps) {
			this.validOps = validOps;
			this.opCode = opCode;
		}

		/**
		 * 
		 * <pre>
		 * Procedure Name: checkOperands
		 * Description: Checks that the passed in operands match a pattern of 
		 * operands allowed for that specific instruction.
		 * 
		 * Specification reference code(s): S2.5, DS3
		 * Calling Sequence: pass 1
		 * 
		 * Error Conditions Tested: Duplicate or invalid keywords
		 * Error messages generated: 211
		 * 
		 * Modification Log (who when and why):
		 * Zak 4/13/12 Added pseudo code comments for errors
		 * Zak 4/16/12 Added code to add comments to list
		 * 
		 * Coding standards met: Signed by Michael
		 * Testing standards met: Signed by Michael
		 * </pre>
		 * 
		 * @since Apr 14, 2012
		 * @author Zak
		 * @param ops
		 *            The operands whose keywords to check
		 * @param errors
		 *            The set to which any errors encountered are added
		 * @return if operand keywords are valid (no errors were found)
		 * 
		 */
		public final boolean checkKeywords(final List<Operand> ops,
				final Set<Integer> errors) {
			// [result boolean local the return value for the operation]
			// [keywords Set<String> local the keywords for the line]
			// [kw String local keyword for the operand]
			boolean result = true;

			// Check that operands have the correct keywords
			Set<String> keywords = new HashSet<String>();
			for (Operand op : ops) {
				String kw = op.getKeyword();
				// All mnemonics except ENT and EXT do not have duplicates
				if (!keywords.add(kw)) {
					// Duplicate keywords
					errors.add(211);
					result = false;
				}
			}

			if (!this.validOps.contains(keywords)) {
				// Invalid Keywords
				errors.add(211);
				result = false;
			}

			return result;
		}

		/**
		 * <pre>
		 * Procedure Name: opCode
		 * Description: Returns the opCode as stored in the field.
		 * 
		 * Specification reference code(s): S1
		 * Calling Sequence: pass 1
		 * 
		 * Error Conditions Tested: none
		 * Error messages generated: none 
		 * 
		 * Modification Log (who when and why):
		 * Coding standards met: Signed by Michael
		 * Testing standards met: Signed by Michael
		 * </pre>
		 * 
		 * @since Apr 17, 2012
		 * @author Zak
		 * @return the opcode
		 */
		public final int getOpCode() {
			return this.opCode;
		}
	}

	// [rep Map<String, Instruction> local stores the correspondence from a
	// mnemonic to an instruction with the opcode and valid keywords]
	private Map<String, Instruction> rep;

	// [dir Set<String> local stores the set of all valid directives]
	private static Set<String> dir;

	// [nonCodeGeneratingDirectives Set<String> local set of directives that
	// don't generate code]
	private static Set<String> nonCodeGeneratingDirectives;

	// [instance InstrucTable global instance of the InstrucTable]
	private static InstrucTable instance = new InstrucTable();

	private static int OUT_OF_RANGE_ERROR = 214;

	/**
	 * 
	 * <pre>
	 * Procedure Name: generateInstructions
	 * Description: Parses the specific file and generates the 
	 * map of Instructions. Each line in the file contains the 
	 * Mnemonic, Opcode, Valid Operands, and Max Values separated by tabs.
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: invalid file
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform with documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 14, 2012
	 * @author Zak
	 * @param filename
	 *            is the name of the file to read the instructions from
	 */
	private void generateInstructions(final String filename) {
		// [keyWords String[] local keywords for instructions]
		// [input Scanner local scanner to read instruction file]
		// [validOps Set<Set<String>> local valid operations for each
		// instruction]
		// [kw String local keyword for a given operand]
		// [k String local individual operand]

		try {

			Scanner input = new Scanner(new File(filename));
			while (input.hasNext()) {
				String mnemonic = input.next();
				int opCode = input.nextInt(2);
				Set<Set<String>> validOps = new HashSet<Set<String>>();

				String[] keyWords = input.nextLine().trim().split(":");
				for (String kw : keyWords) {
					Set<String> ops = new HashSet<String>();
					for (String k : kw.split(",")) {
						ops.add(k.trim());
					}
					validOps.add(ops);
				}

				if (validOps.size() == 0) {
					validOps.add(new HashSet<String>());
				}
				Instruction inst = new Instruction(opCode, validOps);
				this.rep.put(mnemonic, inst);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private InstrucTable() {
		this.rep = new HashMap<String, Instruction>();

		nonCodeGeneratingDirectives = new HashSet<String>();
		nonCodeGeneratingDirectives.addAll(Arrays.asList(new String[] {
				"KICKO", "EXT", "ENT", "AEXS", "EQU", "EQUE", "END", "NEWLC",
				"SKIPS", "NONE" }));

		dir = new HashSet<String>();
		dir.addAll(Arrays
				.asList(new String[] { "KICKO", "EXT", "ENT", "ADRC", "NUM",
						"CHAR", "AEXS", "EQU", "EQUE", "END", "NEWLC", "SKIPS" }));

		this.generateInstructions("ValidOps.txt");
	}

	/**
	 * <pre>
	 * Procedure Name: getInstance
	 * Description: get the singleton instance of the instruction table
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: null reference of instance
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 14, 2012
	 * @author Zak
	 * @return instance of the instructable
	 */
	public static InstrucTable getInstance() {
		return instance;
	}

	/**
	 * <pre>
	 * Procedure Name: generatesCode
	 * Description: returns whether or not a given mnemonic generates code
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: invalid mnemonic
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic to check
	 * @return true iff the mnemonic is code-generating
	 */
	public static boolean generatesCode(String mnemonic) {
		return !nonCodeGeneratingDirectives.contains(mnemonic);
	}

	/**
	 * <pre>
	 * Procedure Name: isDirective
	 * Description: returns whether or not a given mnemonic is defined
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: invalid mnemonic
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Zak
	 * @param str
	 *            is the mnemonic to check
	 * @return true if the mnemonic is defined
	 */
	public boolean isDefined(String str) {
		return this.rep.containsKey(str);
	}

	/**
	 * <pre>
	 * Procedure Name: isInstruction
	 * Description: returns whether or not a given mnemonic is an instruction
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: invalid mnemonic
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Zak
	 * @param str
	 *            is the mnemonic to check
	 * @return true if the mnemonic is a instruction
	 */
	public boolean isInstruction(String str) {
		return this.rep.containsKey(str) && !dir.contains(str);
	}

	/**
	 * <pre>
	 * Procedure Name: isDirective
	 * Description: returns whether or not a given mnemonic is a directive
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: invalid mnemonic
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Zak
	 * @param str
	 *            is the mnemonic to check
	 * @return true if the mnemonic is a directive
	 */
	public boolean isDirective(String str) {
		return dir.contains(str);
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: checkOperandKeywords
	 * Description: checks that the operands have keywords allowed for 
	 * the specific mnemonic.
	 * 
	 * Specification reference code(s): S2.5, DS3
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested:  
	 * Extra or Invalid ENT/EXT operands,
	 * 
	 * 
	 * Error messages generated: 102, 211 
	 * 
	 * Modification Log (who when and why):
	 * Zak 4/13/12 Added pseudo code comments for errors
	 * Zak 4/16/12 Added code to add comments to list
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param mnemonic
	 *            The mnemonic that the keywords belong to
	 * @param ops
	 *            The operands whose keywords will be checked
	 * @param errors
	 *            The set to which any errors encountered are added
	 * @return if operand keywords are valid (no errors were found)
	 * @requires mnemonic is a valid mnemonic as defined in specs
	 */
	public boolean checkOperandKeywords(final String mnemonic,
			final List<Operand> ops, final Set<Integer> errors) {
		boolean result = true;
		// [inst Instruction local stores the instruction corresponding to the
		// mnemonic]
		Instruction inst = this.rep.get(mnemonic);
		if (mnemonic.equals("ENT") || mnemonic.equals("EXT")) {

			// First loop removes all non LR operands
			for (int i = 0; i < ops.size(); i++) {
				if (!ops.get(i).getKeyword().equals("LR")) {
					errors.add(102);
					ops.remove(i);
				}
			}

			// Second loop removes any extra operands
			if (ops.size() > 4) {
				errors.add(102);
				for (int i = 4; i < ops.size(); i++) {
					ops.remove(i);
				}
			}
		} else {
			result = inst.checkKeywords(ops, errors);
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: generateZeros
	 * Description: generates a string of the given number of zeros
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Michael
	 * @param num
	 *            is the number of zeros to generate
	 * @return a string composed of zero repeated num times
	 */
	private static String generateZeros(int num) {
		String result = "";
		while (result.length() < num) {
			result += "0";
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: opCode
	 * Description: Reports the opcode for a specific mnemonic
	 * 
	 * Specification reference code(s): S1
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none 
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param mnemonic
	 *            The mnemonic whose opcode should be returned
	 * @return the opcode for that specific mnemonic or -1 if it has no opcode
	 */
	public int opCode(String mnemonic) {
		try {
			return this.rep.get(mnemonic).getOpCode();
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getBinary
	 * Description: gets the binary equivalent for a given line
	 * 
	 * Specification reference code(s): S1, S2, AO2, DSX, DX, MVX,
	 * IAX, ISX, IMX, JTX, IOX,
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * May 8, 2012 Michael added checking for errors
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic for the instruction or directive
	 * @param ops
	 *            are the operands for the lime
	 * @param errors
	 *            are the errors for the line
	 * @return the binary equivalent for the line
	 */
	public String getBinary(Line line) {
		String binary = "";
		boolean error = false;
		String mnemonic = line.instruction;
		List<Operand> ops = line.ops;
		Set<Integer> errors = line.errors;
		try {

			if (Line.maximumError(errors) >= 200) {

				binary = parseInvalid(mnemonic);

			} else if (mnemonic.equals("HLT") || mnemonic.equals("DMP")) {
				binary = parseFormat3(line, mnemonic, ops);

			} else if (mnemonic.equals("CLRA") || mnemonic.equals("CLRX")) {
				binary = parseFormat4(line, mnemonic, ops);

			} else if (mnemonic.equals("IRKB") || mnemonic.equals("CRKB")) {
				binary = parseFormat5(line, mnemonic, ops);

			} else if (mnemonic.equals("IWSR") || mnemonic.equals("CWSR")) {
				binary = parseFormat6(line, mnemonic, ops);

			} else if (InstrucTable.dir.contains(mnemonic)) {
				binary = parseDirective(line, mnemonic, ops);

			} else if (this.rep.containsKey(mnemonic)) {
				// [hasLiteral boolean local flag for if the line has a literal]
				boolean hasLiteral = false;
				for (Operand op : ops) {
					if (op.getOperandType() == Operand.OpType.LITERAL
							|| op.getOperandType() == Operand.OpType.CONSTANT) {
						hasLiteral = true;
						break;
					}
				}

				if (hasLiteral) {
					binary = parseFormat2(line, mnemonic, ops);
				} else {
					binary = parseFormat1(line, mnemonic, ops);
				}
			} else {
				binary = "";
			}
		} catch (Exception e) {
			errors.add(OUT_OF_RANGE_ERROR);

		}
		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseInvalid
	 * Description: get the binary for an invalid mnemonic
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic to parse
	 * @return nop if the mnemonic is code generating
	 */
	private String parseInvalid(String mnemonic) {
		String binary = "";
		if (generatesCode(mnemonic)) {
			binary = getOpcodeBinary("NOP") + generateZeros(26);
		}
		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseDirective
	 * Description: parse a directive for its binary equivalent
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic for the directive
	 * @param ops
	 *            are the operands for the directive
	 * @param errors
	 *            are the errors of the line
	 * @return the binary equivalent for the given directive
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseDirective(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {
		// [op Operand local operand for the directive]

		Operand op;
		String binary = "";
		if (!nonCodeGeneratingDirectives.contains(mnemonic)) {
			if (mnemonic.equals("AEXS")) {
				op = new Operand("FM", ops.get(0).getString());
				// run get operand binary regardless in case there is an out of
				// range operand
			} else if (mnemonic.equals("NUM") || mnemonic.equals("ADRC")) {
				op = ops.get(0);
				// The operand string has already been evaluated, so it must be
				// a
				// valid Java integer
				binary = Integer
						.toBinaryString(Integer.parseInt(op.getString()));
				binary = generateZeros(32 - binary.length()) + binary;
			} else {
				op = ops.get(0);
				binary = op.getOperandBinary(line);
			}
		}

		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: getOpcodeBinary
	 * Description: returns the binary equivalent of a mnemonic
	 * 
	 * Specification reference code(s): DSX, S1
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7, 2012 updated to conform with documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the line
	 * @return the opcode for the mnemonic
	 */
	private static String getOpcodeBinary(String mnemonic) {
		// [binary String local binary equivalent for the opcode]
		String binary = Integer.toBinaryString(getInstance().opCode(mnemonic));

		// trim to size
		while (binary.length() < 6) {
			binary = "0" + binary;
		}

		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat1
	 * Description: parses instruction format 1 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseFormat1(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {
		// [order String local order bit of instruction]
		// [reg String local arithmetic register binary of instruction]
		// [xreg String local index register binary of instruction]
		// [nwords String local binary for the number of words in instruction]
		// [stack String local binary for the stack bit of the instruction]
		// [lit String local binary for the lit bit of the instruction]
		// [mem String local binary for the memory of the instruction]
		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);

		String order = "0", reg = generateZeros(3), xreg = generateZeros(3);
		String nwords = generateZeros(4), stack = generateZeros(1), lit = "0";
		String mem = generateZeros(13);

		for (Operand op : ops) {
			String opBinary = op.getOperandBinary(line);

			switch (op.getOperandType()) {
			case ARITHMETIC_REGISTER:
				reg = opBinary;
				if (op.getKeyword().equals("DR")) {
					order = "1";
				}
				break;
			case INDEX_REGISTER:
				xreg = opBinary;
				break;
			case MEMORY:
				mem = opBinary;
				break;
			case NUMBER_WORDS:
				nwords = opBinary;
				break;
			}

		}

		binary = opcode + order + reg + xreg + nwords + stack + lit + mem;

		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat2
	 * Description: parses instruction format 2 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseFormat2(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {

		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);

		String order = "0", reg = generateZeros(3), xreg = generateZeros(3);
		String nwords = "0000", stack = "0", lit = "1";
		String literal = generateZeros(13);

		for (Operand op : ops) {
			String opBinary = op.getOperandBinary(line);

			switch (op.getOperandType()) {
			case ARITHMETIC_REGISTER:
				reg = opBinary;
				if (op.getKeyword().equals("DR")) {
					order = "1";
				}
				break;
			case INDEX_REGISTER:
				xreg = opBinary;
				break;
			case LITERAL:
			case CONSTANT:
				literal = opBinary;
				break;
			}
		}

		binary = opcode + order + reg + xreg + nwords + stack + lit + literal;

		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat3
	 * Description: parses instruction format 3 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseFormat3(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {
		// [opcode string local opcode for the instruction]
		// [unused string local string of unused bits for the instruction]
		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);
		String unused = generateZeros(13);

		String opBinary = ops.get(0).getOperandBinary(line);

		binary = opcode + unused + opBinary;
		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat4
	 * Description: parses instruction format 4 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 */
	private static String parseFormat4(Line line, String mnemonic,
			List<Operand> ops) {
		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);
		String unused = generateZeros(26);

		binary = opcode + unused;
		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat5
	 * Description: parses instruction format 5 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseFormat5(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {
		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);

		String xreg = generateZeros(3), nwords = generateZeros(4), mem = generateZeros(13);

		for (Operand op : ops) {
			String opBinary = op.getOperandBinary(line);

			switch (op.getOperandType()) {
			case INDEX_REGISTER:
				xreg = opBinary;
				break;
			case MEMORY:
				mem = opBinary;
				break;
			case NUMBER_WORDS:
				nwords = opBinary;
				break;
			}
		}

		binary = opcode + generateZeros(4) + xreg + nwords + generateZeros(2)
				+ mem;

		return binary;
	}

	/**
	 * <pre>
	 * Procedure Name: parseFormat6
	 * Description: parses instruction format 6 into its binary
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: out of bounds values
	 * Error messages generated: 213, 207, 214
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @param mnemonic
	 *            is the mnemonic of the instruction/directive of the line
	 * @param ops
	 *            are the operands of the line
	 * @return the binary equivalent of the line
	 * @throws IllegalAccessException
	 *             if an out of bounds value is encountered
	 */
	private static String parseFormat6(Line line, String mnemonic,
			List<Operand> ops) throws IllegalAccessException {
		String binary = "";
		String opcode = getOpcodeBinary(mnemonic);

		String xreg = generateZeros(3), nwords = generateZeros(4), mem = generateZeros(13), lit = generateZeros(1);

		for (Operand op : ops) {
			String opBinary = op.getOperandBinary(line);

			switch (op.getOperandType()) {
			case INDEX_REGISTER:
				xreg = opBinary;
				break;
			case MEMORY:
				mem = opBinary;
				lit = "0";
				break;
			case LITERAL:
				mem = opBinary;
				lit = "1";
				break;
			case NUMBER_WORDS:
				nwords = opBinary;
				break;
			}
		}

		binary = opcode + generateZeros(4) + xreg + nwords + generateZeros(1)
				+ lit + mem;

		return binary;
	}
}
