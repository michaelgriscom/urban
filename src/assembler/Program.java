package assembler;

import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import assembler.ExpressionParser.BadAsteriskException;
import assembler.ExpressionParser.BadFormatException;

/**
 * <pre>
 * Class Name: Program
 * Description: contains the abstract state for a program.
 * 
 * Version information:
 * $RCSfile: Program.java,v $
 * $Revision: 1.45 $
 * $Date: 2012/05/16 03:39:12 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public class Program {

	// error codes
	// [LABEL_IN_USE int local constant for error 203]
	// [INVALID_NUMBER_ERROR int local constant for error 207]
	// [SYMBOL_UNDEFINED_ERROR int local constant for error 213]
	// [OUT_OF_RANGE_ERROR int local constant for error 214]
	// [LC_OUT_OF_BOUNDS int local constant for error 303]
	// [KICKO_OUT_OF_BOUNDS int local constant for error 304]
	private static final int LABEL_IN_USE = 203;
	private static final int INVALID_NUMBER_ERROR = 207;
	private static final int SYMBOL_UNDEFINED_ERROR = 213;
	private static final int OUT_OF_RANGE_ERROR = 214;
	private static final int LC_OUT_OF_BOUNDS = 303;
	private static final int KICKO_OUT_OF_BOUNDS = 304;
	private static final int EXPRESSION_BAD_FORMAT = 218;
	private static final int EXPRESSION_ASTERISK = 221;

	// size constants
	// [BYTES_IN_WORD int local constant for number of bytes in word]
	// [BITS_IN_BYTE int local constant for number of bits in byte]
	// [WORDS_IN_MEMORY int local constant for the number of words in memory]
	private static final int BYTES_IN_WORD = 4;
	private static final int BITS_IN_BYTE = 8;
	private static final int WORDS_IN_MEMORY = 4096;

	// linking file record counts
	// [linkingRecords int local number of linking records]
	// [textRecords int local number of text records]
	// [modificationRecords int local number of modification records]
	private int linkingRecords = 0;
	private int textRecords = 0;
	private int modificationRecords = 0;

	// [programName string local name of the program]
	private String programName;

	// [undefined string local empty string for use in checking undefined
	// values]
	private static String undefined = "";

	// [lines List<Line> local list of the lines in program]
	private List<Line> lines = new ArrayList<Line>();

	// [lc int local location counter of program]
	private int lc = 0;

	private static Program instance = new Program();

	private Program() {
		lc = 0;
		linkingRecords = 0;
		textRecords = 0;
		modificationRecords = 0;
		programName = "";
		lines = new ArrayList<Line>();
		symTable = new TreeMap<String, Program.SymbolInfo>();
	}

	/**
	 * <pre>
	 * Procedure Name: getInstance
	 * Description: get the singleton instance of the program
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: uninitialized program
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @return instance of the program
	 */
	public static Program getInstance() {
		return instance;
	}

	public void setName(String name) {
		this.programName = name;
	}

	public String getName() {
		return this.programName;
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
	 * Procedure Name: isUndefined
	 * Description: returns whether or not a given integer is undefined
	 * (negative).
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: checked on negative, zero, positive numbers
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @param temp
	 *            is the number to check
	 * @return true iff number is undefined (negative)
	 */
	private static boolean isUndefined(int temp) {
		return temp < 0;
	}

	/**
	 * <pre>
	 * Class Name: SymbolInfo
	 * Description: represents an entry in the symbol table.
	 * 
	 * Version information:
	 * $RCSfile: Program.java,v $
	 * $Revision: 1.45 $
	 * $Date: 2012/05/16 03:39:12 $
	 * </pre>
	 * 
	 * @author Michael
	 * 
	 */
	private static class SymbolInfo {

		// [address int local address of symbol instance]
		private int address;

		// [usage String local usage of symbol instance. Can be LABEL | EXT |
		// EQU | PGRM_NAME]
		private String usage;

		// [str String local representing equated value of symbol instance]
		private final String str;

		public SymbolInfo(final int address, String usage, String str) {
			this.address = address;
			this.usage = usage;
			this.str = str;
		}
	}

	/**
	 * Map from a label to the label's information, contains every entry of the
	 * symbol
	 * table.
	 */
	// [labelMap is a mapping from a label to the label's information within the
	// SymbolTable]
	private SortedMap<String, SymbolInfo> symTable = new TreeMap<String, SymbolInfo>();

	/**
	 * <pre>
	 * Procedure Name: printListing
	 * Description: print the listing table.
	 * 
	 * Specification reference code(s): AO1, AO2, AO4, AO5
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: empty table
	 * Error messages generated: Any error messages attached to a given line
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 13, 2012 added opcode binary equivalent for SP-1
	 * Michael Apr 14, 2012 added error code display
	 * Zak Apr 16, 2012 added string display of errors
	 * Michael Apr 17, 2012 added error number back in
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 10, 2012
	 * @author Michael
	 * @param os
	 *            is the output stream to print to
	 */
	public final void printListing(final OutputStream os) {
		// [out PrintStream local used for outputting the SymbolTable]
		// [line Line local used to store the current line]
		// [lbl string local used to temporarily store line label]
		// [command string local used to temporarily store line instruction]
		// [binaryEq string local used to temporarily store the binary
		// equivalent of a line]
		PrintStream out = new PrintStream(os);

		Line line;
		for (int i = 0; i < lines.size(); i++) {
			line = lines.get(i);

			out.println(line.rawLine);

			// Now print the tokenized information
			String lineNumber = "Line number: "
					+ String.format("%4s", i + 1).replace(' ', '0');
			String lbl;
			if (line.hasLabel()) {
				lbl = line.label;
			} else {
				lbl = "------";
			}

			out.println(lineNumber + "\t" + "LC: "
					+ Integer.toHexString(line.address).toUpperCase() + "\t"
					+ "Label: " + lbl);

			String binaryEq;
			String command;
			if (line.isInstruction()) {
				binaryEq = line.getOpcodeBinary();
				command = line.instruction;

			} else if (line.isDirective()) {
				binaryEq = "------";
				command = line.directive;

			} else {
				binaryEq = "------";
				command = "------";
			}

			out.println("Instruction/Directive: " + command + "\t"
					+ "Binary Equivalent: " + binaryEq);

			// Print all the operands, with each one on a separate line
			for (int j = 0; j < line.ops.size(); j++) {
				// binaryEq = line.operandBinary[j];

				if (isUndefined(binaryEq)) {
					binaryEq = "------";
				}

				// Outputs j+1 because operand at position j is the j+1-th
				// operand
				out.println("Operand " + (j + 1) + ": "
						+ line.ops.get(j).toString() + "\t\t"
						+ "Binary Equivalent: " + binaryEq);
			}

			if (line.errors.size() > 0) {
				out.println("Errors:");
				for (int error : line.errors) {
					out.println("**ERROR " + error + "** "
							+ ErrorTable.getInstance().getErrorMessage(error));
				}
			}

			out.println();
		}

	}

	/**
	 * <pre>
	 * Procedure Name: printSymbols
	 * Description: print the symbols of the symbol table.
	 * 
	 * Specification reference code(s): AO1
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: empty table
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 10, 2012
	 * @author Michael
	 * @param os
	 *            is the stream to output the symbol table to
	 */
	public final void printSymbols(final OutputStream os) {
		// [output PrintStream local used for outputting the SymbolTable]
		PrintStream output = new PrintStream(os);

		String label;

		// [info SymbolInfo local temporary variable to hold the information for
		// the label being printed in the SymbolTable]
		SymbolInfo info;

		output.println("Label\tLC\tUsage\tString");

		// [entry String->SymbolInfo local is a temporary variable to hold the
		// current
		// SymbolTable entry being printed]
		for (Map.Entry<String, SymbolInfo> entry : symTable.entrySet()) {
			label = entry.getKey();
			info = entry.getValue();

			if (isUndefined(info.address)) {
				output.println(label + '\t' + "----" + '\t' + info.usage + '\t'
						+ info.str);
			} else {
				output.println(label + '\t'
						+ Integer.toHexString(info.address).toUpperCase()
						+ '\t' + info.usage + '\t' + info.str);
			}
		}

	}

	/**
	 * <pre>
	 * (displayable) 
	 * Procedure Name: symbolDefined
	 * Description: check if symbol is defined in the symbol table.
	 * 
	 * Specification reference code(s): DS1.X, DS3.X
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Andrew
	 * @param label
	 *            is the label to look up in the symbol table
	 * @return true iff the symbol is defined
	 */
	public final boolean symbolDefined(final String label) {
		return symTable.containsKey(label);
	}

	/**
	 * <pre>
	 * Procedure Name: symbolUsage
	 * Description: look up usage in the symbol table.
	 * 
	 * Specification reference code(s): DS1.X, DS3.X
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: 
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Andrew
	 * @param label
	 *            is the label to look up in the symbol table
	 * @return usage of the symbol
	 */
	public final String symbolUsage(final String label) {
		return symTable.get(label).usage;
	}

	/**
	 * <pre>
	 * Procedure Name: symbolLocation
	 * Description: look up location in the symbol table.
	 * 
	 * Specification reference code(s): DS1.X, DS3.X
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 fixed documentation
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 10, 2012
	 * @author Michael
	 * @param label
	 *            is the label to look up in the symbol table
	 * @return address of the symbol
	 */
	public final int symbolLocation(final String label) {
		return symTable.get(label).address;
	}

	/**
	 * <pre>
	 * Procedure Name: equateSymbol
	 * Description: returns the equated value of a symbol.
	 * 
	 * Specification reference code(s): DS1.X, DS3.X
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: large values, null reference
	 * Error messages generated: 213,  207
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 14, 2012 added recursively equated labels
	 * Michael Apr 16, 2012 added error checking, removed nested try/catch
	 * Michael Apr 17, 2012 fixed documentation, error handling
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 10, 2012
	 * @author Michael
	 * @param label
	 *            is the symbol label to equate
	 * @return equated value of label
	 * @throws NoSuchFieldException
	 *             if error 213 occurs
	 * @throws NumberFormatException
	 *             if error 207 occurs
	 */
	public final int equateSymbol(final String label)
			throws NoSuchFieldException, NumberFormatException {
		int result;

		if (Character.isDigit(label.charAt(0)) || label.charAt(0) == '-') {
			result = Integer.parseInt(label);
		} else {
			try {
				SymbolInfo sym = symTable.get(label);
				if (sym == null) {
					throw new NoSuchFieldException();
				}

				// Base case: Symbol is equated to trivial value
				try {
					result = Integer.parseInt(sym.str);
				} catch (Exception e) {
					result = ExpressionParser.parseEquExpression(sym.str,
							Program.this, sym.address);
				}
			} catch (Exception e) {
				throw new NoSuchFieldException();
			}
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: addLine
	 * Description: add line to program.
	 * 
	 * Specification reference code(s): AO1, AO2, AO4, AO5, DSX, DX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: invalid operands, values out of range,
	 * null references
	 * Error messages generated: 214, 203, 303, 304
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 12, 2012 better handling of directives
	 * that don't generate code
	 * Michael Apr 14, 2012 added operand binary function
	 * Michael Apr 16, 2012 fixed operand binary calculation
	 * Michael Apr 17, 2012 fixed documentation, error handling
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 10, 2012
	 * @author Michael
	 * @param rawLine
	 *            is the raw text of the line
	 * @param label
	 *            is the label for the line, "" if none
	 * @param instruction
	 *            is the line instruction, "" if none
	 * @param directive
	 *            is the line directive, "" if none
	 * @param ops
	 *            are the operands for the line
	 * @param errors
	 *            are the errors attached to the line
	 */
	public final void addLine(String rawLine, String label, String instruction,
			String directive, List<Operand> ops, Set<Integer> errors) {

		String str = undefined;
		String usage = undefined;
		SymbolInfo info;

		int location = this.lc;
		try {
			location = getNewLC(instruction, directive, ops);

		} catch (NoSuchFieldException e) {
			errors.add(SYMBOL_UNDEFINED_ERROR);

			if (InstrucTable.generatesCode(directive)) {
				instruction = "NOP";
				directive = undefined;
			} else {
				directive = undefined;
			}

		} catch (NumberFormatException e) {
			errors.add(INVALID_NUMBER_ERROR);

			if (InstrucTable.generatesCode(directive)) {
				instruction = "NOP";
				directive = undefined;
			} else {
				directive = undefined;
			}

		} catch (IndexOutOfBoundsException e) {
			errors.add(KICKO_OUT_OF_BOUNDS);

		} catch (IllegalAccessException e) {
			errors.add(LC_OUT_OF_BOUNDS);
		}

		int address;

		if (directive.equals("EQU") || directive.equals("EQUE")) {
			str = ops.get(0).getString();
			try {
				ExpressionParser.parseEquExpression(str, this, location);
			} catch (NumberFormatException e) {
				errors.add(INVALID_NUMBER_ERROR);
			} catch (NoSuchFieldException e) {
				errors.add(SYMBOL_UNDEFINED_ERROR);
			} catch (BadAsteriskException e) {
				errors.add(EXPRESSION_ASTERISK);
			} catch (BadFormatException e) {
				errors.add(EXPRESSION_BAD_FORMAT);
			}
			address = -1;

		} else if (directive.equals("EXT")) {
			address = -1;
			for (Operand op : ops) {
				info = new SymbolInfo(address, "EXT", undefined);
				if (this.symTable.containsKey(op.getString())) {
					errors.add(LABEL_IN_USE);
				}
				symTable.put(op.getString(), info);
			}
		} else if (directive.equals("ENT")) {
			for (Operand op : ops) {
				if (this.symTable.containsKey(op.getString())) {
					info = this.symTable.remove(op.getString());
					if (info.usage == "LABEL") {
						info.usage = "ENT";
					} else {
						errors.add(LABEL_IN_USE);
					}
					symTable.put(op.getString(), info);
				} else {
					info = new SymbolInfo(-1, "ENT", undefined);
				}

				symTable.put(op.getString(), info);
			}
		}

		if (!isUndefined(label)) {
			address = location;
			usage = getUsage(instruction, directive);

			info = new SymbolInfo(address, usage, str);
			if (this.symTable.containsKey(label)) {
				info = this.symTable.remove(label);
				if (info.usage.equals("ENT")) {
					info.address = address;
				} else {
					errors.add(LABEL_IN_USE);
				}
			}
			symTable.put(label, info);
		}

		lines.add(new Line(location, rawLine, label, instruction, directive,
				ops, errors));
	}

	/**
	 * <pre>
	 * Procedure Name: getNewLC
	 * Description: modify the LC and return the LC value for the current line.
	 * 
	 * Specification reference code(s): AO1, DSX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: out of range values
	 * Error messages generated: 213, 303, 304
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 10, 2012 corrected NEWLC behavior
	 * Michael Apr 17, 2012 fixed LC incrementing,
	 * added check for too large of values, fixed documentation
	 * Michael May 7, 2012 fixed LC behavior for non code generating directives
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 11, 2012
	 * @author Michael
	 * @param instruction
	 *            is the line's instruction
	 * @param directive
	 *            is the line's directive
	 * @param ops
	 *            are the line's operands
	 * @return the LC for the new line
	 * @throws NoSuchFieldException
	 *             if error 213 occurred
	 * @throws IndexOutOfBoundsException
	 *             if error 303 occurred
	 * @throws IllegalAccessException
	 *             if error 304 occurred
	 */
	private int getNewLC(String instruction, String directive, List<Operand> ops)
			throws NoSuchFieldException, IndexOutOfBoundsException,
			IllegalAccessException {
		// [currentLC int local location counter of current line]
		// [nextLC int local location counter of next line]
		int currentLC = this.lc;
		int nextLC = currentLC;

		if (!isUndefined(instruction) || directive.equals("NUM")
				|| directive.equals("CHAR") || directive.equals("ADRC")) {
			nextLC++;

		} else if (directive.equals("SKIPS")) {
			nextLC += equateSymbol(ops.get(0).getString());

		} else if (directive.equals("NEWLC") || directive.equals("KICKO")) {
			currentLC = equateSymbol(ops.get(0).getString());
			nextLC = currentLC;
		}

		this.lc = nextLC;
		if (this.lc > WORDS_IN_MEMORY || this.lc < 0) {
			if (directive.equals("KICKO")) {
				throw new IndexOutOfBoundsException();
			} else {
				throw new IllegalAccessException();
			}
		}
		return currentLC;
	}

	/**
	 * <pre>
	 * Procedure Name: getUsage
	 * Description: get's the usage for a given
	 * instruction/directive for use in the symbol table.
	 * 
	 * Specification reference code(s): DS1.X, DS3.X
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: nonexistant instruction/directive
	 * Error messages generated: 
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 fixed documentation
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * result = directive;
	 * 
	 * @since Apr 11, 2012
	 * @author Michael
	 * @param instruction
	 *            is the line's instruction
	 * @param directive
	 *            is the line's directive
	 * @return the usage corresponding to the line's instruction or directive
	 */
	private String getUsage(String instruction, String directive) {
		String result;
		if (directive.equals("EXT") || directive.equals("ENT")
				|| directive.equals("EQU")) {
			result = directive;
		} else if (directive.equals("KICKO")) {
			result = "PRGM_NAME";
		} else {
			result = "LABEL";
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: listingTableSize
	 * Description: get the size of the listing table.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: used for testing purposes
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 fixed documentation
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @return the size of the listing table
	 */
	public final Object listingTableSize() {
		return this.lines.size();
	}

	/**
	 * <pre>
	 * Procedure Name: symbolTableSize
	 * Description: get the size of the symbol table.
	 * 
	 * Specification reference code(s): N/A
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 fixed documentation
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @return the size of the symbol table
	 */
	public final Object symbolTableSize() {
		return this.symTable.size();
	}

	/**
	 * <pre>
	 * Procedure Name: runSecondPass
	 * Description: run the second pass of the assembler
	 * 
	 * Specification reference code(s): SX, OBX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: out of bounds values, undefined labels
	 * Error messages generated: applicable 100, 200 and 300 level errors
	 * 
	 * Modification Log (who when and why):
	 * Michael May 6, 2012 restructured operation calls to conform to new class design
	 * Michael May 7, 2012 collated error checking to beginnning of call
	 * Michael May 8, 2012 moved get hex into line
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 5, 2012
	 * @author Michael
	 * @return true if no fatal error occurred, else return false
	 */
	public boolean runSecondPass() {
		for (Line line : this.lines) {

			// determine if line had fatal/serious error
			int maxError = Line.maximumError(line.errors);

			if (maxError >= 300) { // terminate for fatal errors
				return false;
			} else if (maxError >= 200) { // replace w/nop for serious errors
				if (line.isInstruction()
						|| InstrucTable.generatesCode(line.directive)) {
					line.directive = undefined;
					line.instruction = "NOP";
				} else {
					// TODO: what to do for error in non code generating
					line.directive = "NONE";
				}
				line.ops.clear();
			}

			// NOTE: EVALUATEOPS IS EXTREMELY POWERFUL; USE WITH CAUTION
			List<Operand> newOps = new ArrayList<Operand>();
			newOps = evaluateOps(line);

			line.setBinary(newOps);

		}

		return true;
	}

	/**
	 * <pre>
	 * Procedure Name: printAssemblyReport
	 * Description: print the assembly report for the program
	 * 
	 * Specification reference code(s): AOX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: undefined instruction binary, undefined relocation flag
	 * Error messages generated: display errors associated with each line
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @param os
	 *            is the output stream to output the assembly report to
	 */
	public void printAssemblyReport(OutputStream os) {
		PrintStream out = new PrintStream(os);
		Line line;
		String hex;
		out.println("LOC" + "\t" + "OBJ CODE" + "\t" + "A/R/E/C" + "\t"
				+ "STMT" + "\t" + "SOURCE STATEMENT");
		for (int i = 0; i < this.lines.size(); i++) {
			line = this.lines.get(i);

			hex = line.getHex();
			if (isUndefined(hex)) {
				hex = "--------";
			}
			out.println(convertToHex(line.address, 4) + "\t" + hex + "\t"
					+ line.relocationFlag + "\t" + (i + 1) + "\t"
					+ line.rawLine);

			if (line.errors.size() > 0) {
				System.out.println("Errors:");
				for (int error : line.errors) {
					System.out.println("**ERROR " + error + "** "
							+ ErrorTable.getInstance().getErrorMessage(error));
				}
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: convertToHex
	 * Description: convert an integer to hex with the specified number of digits
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: pass 2
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
	 * @param digits
	 *            are the number of digits desired in the resulting output
	 * @return a string representing the hex value of the passed integer
	 */
	private String convertToHex(int num, int digits) {
		// [form string local format for converting a BigInteger to hex]
		String result = undefined;

		try {
			BigInteger big = BigInteger.valueOf(num);
			String form = "%0" + digits + "x";
			result = String.format(form, big);

		} catch (Exception e) {

		}

		return result.toUpperCase();
	}

	/**
	 * <pre>
	 * Procedure Name: checkBounds
	 * Description: Check if a given value is in the appropriate bounds
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: operands out of bounds
	 * Error messages generated: out of bounds error
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 9, 2012
	 * @author Andrew
	 * @param line
	 *            is the line to evaluate the operands of
	 * @param type
	 *            is the type of operand to check
	 * @param val
	 *            the value that is to be checked
	 * @return true iff the operand value is in the appropriate bounds
	 */
	public boolean checkBounds(Line line, Operand.OpType type, int val) {
		switch (type) {
		case MEMORY:
			// if (line.relocationFlag == 'A'
			// || line.relocationFlag == 'R'
			// || (line.relocationFlag == 'C' && line.modifyRecords.size() ==
			// 0)) {
			// return this.getStartAddress() <= val
			// && val <= this.getEndAddress();
			// } else {
			return 0 <= val && val < 4096;
			// }
		case ARITHMETIC_REGISTER:
			return 0 <= val && val < 8;
		case INDEX_REGISTER:
			return 1 <= val && val < 8;
		case NUMBER_WORDS:
			return 1 <= val && val < 15;
		case CONSTANT:
			if (Arrays.asList("ISHL", "ISHR", "ISLA", "ISRA", "ROL", "ROR",
					"PWR").indexOf(line.instruction) >= 0) {
				return 0 <= val && val <= 32;
			} else if (line.instruction.equals("DMP")) {
				return 1 <= val && val <= 3;
			} else if (line.instruction.equals("HLT")) {
				return -4096 <= val && val < 4096;
			} else if (Arrays.asList("KICKO", "NEWLC", "SKIPS").indexOf(
					line.directive) >= 0) {
				return 0 <= val && val < 4096;
			} else if (line.directive.equals("AEXS")) {
				return this.getStartAddress() <= val && val <= 4095;
			} else if (line.directive.equals("NUM")
					|| line.directive.equals("EQU")) {
				return true;
			} else {
				line.errors.add(999);
				return false;
			}
		case LITERAL:
			return -4096 <= val && val < 4096;
		case STRING:
			return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * Procedure Name: evaluateOps
	 * Description: evaluate the values for the operands of a line
	 * 
	 * Specification reference code(s): SX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: undefined operands, null reference
	 * Error messages generated: out of bounds error
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
	 * @param line
	 *            is the line to evaluate the operands of
	 * @return a list of the line's operands after evaluation
	 */
	private List<Operand> evaluateOps(Line line) {
		// [evaluatedOps ArrayList<Operand> local list of evaluated operands]
		// [opVal string local value of operand]
		// TODO: modificationout log/flags

		List<Operand> evaluatedOps = new ArrayList<Operand>();
		for (Operand op : line.ops) {
			String opVal = undefined;

			try {
				switch (op.getOperandType()) {
				case MEMORY:
				case LABEL:
					if (line.directive.equals("ADRC")) {
						opVal = String
								.valueOf(ExpressionParser.parseAdrcExpression(
										op.getString(), this, line));
					} else {
						opVal = String.valueOf(ExpressionParser
								.parseOperandExpression(op.getString(), this,
										line));
					}
					break;

				case EXPRESSION:
					if (line.directive.equals("ADRC")) {
						opVal = String
								.valueOf(ExpressionParser.parseAdrcExpression(
										op.getString(), this, line));
					} else {
						opVal = String.valueOf(ExpressionParser
								.parseEquExpression(op.getString(), this,
										line.address));
					}
					break;
				case STRING:
					opVal = op.getString();
					break;

				default:
					opVal = String.valueOf(equateSymbol(op.getString()));
				}
			} catch (BadFormatException e2) {
				// TODO Auto-generated catch block
				opVal = undefined;
				line.errors.add(221);
				return evaluatedOps;

			} catch (BadAsteriskException e3) {
				// TODO Auto-generated catch block
				opVal = undefined;
				line.errors.add(218);
				return evaluatedOps;

			} catch (NoSuchFieldException e4) {
				// TODO Auto-generated catch block
				opVal = undefined;
				line.errors.add(227);
				return evaluatedOps;

			} catch (NumberFormatException e5) {
				opVal = undefined;
				line.errors.add(207);
				return evaluatedOps;
			}

			evaluatedOps.add(new Operand(op.getKeyword(), opVal));
		}
		return evaluatedOps;
	}

	/**
	 * <pre>
	 * Procedure Name: createLinkingRecord
	 * Description: create the linking record for the program
	 * 
	 * Specification reference code(s): OB2
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: N/A
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * Michael May 9, 2012 changed to output in hex
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Zak
	 * @param out
	 *            is the stream to output the record to
	 */
	public void createLinkingRecord(OutputStream out) {
		// [output PrintStream local stream to output linking record to]
		// [symName String local name of symbol]
		PrintStream output = new PrintStream(out);
		for (String symName : symTable.keySet()) {
			if (symTable.get(symName).usage == "ENT") {
				output.println("L:" + symName + ":"
						+ convertToHex(symTable.get(symName).address, 4) + ":"
						+ this.programName);
				// this needs to go inside your loop, it's used to count linking
				// records
				this.linkingRecords++;
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: createModificationRecord
	 * Description: create the modification record portion of the object file
	 * 
	 * Specification reference code(s): OB4
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 *  
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Andrew
	 * @param out
	 *            is the stream to output the modification record to
	 */
	public void createModificationRecord(OutputStream out) {
		// [ps PrintStream local stream used to output modification record]
		// [Line line local current line being processed]
		// [Operand memOp local A DM or FM operand, which might reference an
		// external
		// label]
		// [Operand op local Ranges over the operands associated with each line]
		// [SymbolInfo symInfo local Info for a symbol appearing in a operand
		// expression]
		// [int nMod local number of modifications already printed]
		// [int j local Loop counter]
		PrintStream ps = new PrintStream(out);
		for (Line line : this.lines) {
			if (!line.instruction.equals("")) {
				Operand memOp = null;
				// We got a real instruction, look to see if it accesses memory
				for (Operand op : line.ops) {
					if (op.getKeyword().equals("DM")
							|| op.getKeyword().equals("FM")) {
						memOp = op;
						break;
					}
				}

				if (memOp != null) {
					// There is a memory operand that needs to be output.
					// An operand expression can only take the form *+constant
					// or constant or label.
					if (this.symTable.containsKey(memOp.getString())) {
						SymbolInfo symInfo = this.symTable.get(memOp
								.getString());
						// If the expression consists of a single local label,
						// we do nothing. If it is an expression, then the only
						// possible relocation is local relocation via a star,
						// which is handles in the Text record. So, we only have
						// to deal with external labels here.
						if (symInfo.usage.equals("EXT")) {
							ps.println("M:" + convertToHex(line.address, 4)
									+ ":1:+:" + memOp.getString() + ":"
									+ this.programName);
							this.modificationRecords++;
						}
					}
				}
			} else if (line.directive.equals("ADRC")) {
				if (line.modifyRecords != null && line.modifyRecords.size() > 0) {
					ps.print("M:" + convertToHex(line.address, 4) + ":"
							+ Math.min(4, line.modifyRecords.size()));
					for (int j = 0; j < line.modifyRecords.size(); j++) {
						ps.print(":" + line.modifyRecords.get(j));
						if ((j % 4 == 3) && (j + 1 < line.modifyRecords.size())) {
							ps.println(":" + this.programName);
							this.modificationRecords++;
							ps.print("M:"
									+ convertToHex(line.address, 4)
									+ ":"
									+ Math.min(4, line.modifyRecords.size() - j
											- 1));
						}
					}
					ps.println(":" + this.programName);
					this.modificationRecords++;
				}
				if (line.localModifies != 0 && line.relocationFlag == 'C') {
					String oneLocal = (line.localModifies > 0 ? ":+" : ":-")
							+ ":" + this.programName;
					for (int j = 0; j + 4 <= Math.abs(line.localModifies); j += 4) {
						ps.println("M:" + convertToHex(line.address, 4) + ":4"
								+ oneLocal + oneLocal + oneLocal + oneLocal
								+ ":" + this.programName);
						this.modificationRecords++;
					}
					if (Math.abs(line.localModifies) % 4 != 0) {
						ps.print("M:" + convertToHex(line.address, 4) + ":"
								+ Math.abs(line.localModifies) % 4);
						for (int j = Math.abs(line.localModifies) % 4; j > 0; j--) {
							ps.print(oneLocal);
						}
						ps.println(":" + this.programName);
						this.modificationRecords++;
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getStartAddress
	 * Description: get the starting address of the program
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: N/A
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @return the starting address of the program
	 */
	public int getStartAddress() {
		return lines.get(0).address;
	}

	/**
	 * <pre>
	 * Procedure Name: getEndAddress
	 * Description: get the last address of the program
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Michael
	 * @return the last address of the program
	 */
	public int getEndAddress() {
		return lines.get(lines.size() - 1).address - 1;
	}

	/**
	 * <pre>
	 * Procedure Name: createHeaderRecord
	 * Description: creates the header record for the object file
	 * 
	 * Specification reference code(s): OB1.X
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 7,2012 filled in body of procedure
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Michael
	 * @param out
	 *            is the stream to output to
	 */
	public void createHeaderRecord(OutputStream out) {
		// [ps PrintStream local stream used to output header record]
		// [startAddress int local temporary value used to hold the starting
		// address of the program]
		// [endAddress int local temporary value used to hold the ending address
		// of the program]
		// [progLength int local temporary value used to hold the length of the
		// program in words]
		// [executionStart int local temporary value used to hold the execution
		// start of the program]
		// [line Line local temporary line used as an iterator through the lines
		// of the program]
		// [aexsOp List<Operand> local temporary list of operands for occurrence
		// of AEXS]
		// [startAddressHex String local temporary value used to hold the
		// starting address of the program in hex]
		// [progLengthHex String local temporary value used to hold the length
		// of the
		// program in words in hex]
		// [executionStartHex string local temporary value used to hold the
		// execution
		// start of the program in hex]
		// [dateFormat DateFormat local date format used within the header
		// record]
		// [date Date local date and time of assembly]
		// [time String local formatted date and time of assembly]
		// [version String local assembly version]

		PrintStream ps = new PrintStream(out);
		int startAddress = getStartAddress();
		int executionStart = startAddress;
		int endAddress = getEndAddress();
		int progLength = endAddress - startAddress + 1;

		// see if program contains AEXS, if so, modify the execution starting
		// point
		for (Line line : this.lines) {
			if (line.directive.equals("AEXS")) {
				List<Operand> aexsOp = evaluateOps(line);
				executionStart = Integer.parseInt(aexsOp.get(0).getString());
				break;
			}
		}

		// String startAddressHex = Integer.toHexString(startAddress)
		// .toUpperCase();
		// String progLengthHex = Integer.toHexString(progLength).toUpperCase();
		// String executionStartHex = Integer.toHexString(executionStart)
		// .toUpperCase();
		String startAddressHex = convertToHex(startAddress, 4);
		String progLengthHex = convertToHex(progLength, 4);
		String executionStartHex = convertToHex(executionStart, 4);

		DateFormat dateFormat = new SimpleDateFormat("yyyyD,HH:mm:ss");
		Date date = new Date();
		String time = dateFormat.format(date);

		String version = "0019";

		ps.println("H:" + this.programName + ":" + startAddressHex + ":"
				+ progLengthHex + ":" + executionStartHex + ":" + time + ":"
				+ version + ":" + "URBAN-ASM" + ":" + this.programName);
	}

	/**
	 * <pre>
	 * Procedure Name: createEndRecord
	 * Description: create the end record for the program
	 * 
	 * Specification reference code(s): OB5
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * Michael May 9, 2012 changed to output in hex
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Joel
	 * @param out
	 *            is the stream to output the record to
	 */
	public void createEndRecord(OutputStream out) {
		// [ps PrintStream local stream used to output header record]

		PrintStream ps = new PrintStream(out);
		ps.println("E:"
				+ convertToHex((this.linkingRecords + this.textRecords
						+ this.modificationRecords + 2), 4) + ":"
				+ convertToHex(this.linkingRecords, 4) + ":"
				+ convertToHex(this.textRecords, 4) + ":"
				+ convertToHex(this.modificationRecords, 4) + ":"
				+ this.programName);
	}

	/**
	 * <pre>
	 * Procedure Name: createTextRecord
	 * Description: create the text record for the program
	 * 
	 * Specification reference code(s): OB3
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Andrew
	 * @param out
	 *            is the stream to output the text record to
	 */
	public void createTextRecord(OutputStream out) {
		PrintStream ps = new PrintStream(out);
		for (Line line : this.lines) {

			if (line.binary.length() > 0) {
				ps.print("T:" + convertToHex(line.address, 4) + ":"
						+ line.getHex() + ":" + line.relocationFlag + ":");
				if (line.relocationFlag == 'E') {
					ps.print("1:");
				} else if (line.relocationFlag == 'C') {
					ps.print(line.modifyRecords.size()
							+ Math.abs(line.localModifies) + ":");
				} else {
					ps.print("0:");
				}
				ps.println(this.programName);

				this.textRecords++;
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: generateObjectFile
	 * Description: generate the object file for the program
	 * 
	 * Specification reference code(s): OBX
	 * Calling Sequence: pass 2
	 * 
	 * Error Conditions Tested: N/A
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael May 8, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 8, 2012
	 * @author Andrew
	 * @param out
	 *            is the stream to output the object file to
	 */
	public void generateObjectFile(OutputStream out) {
		createHeaderRecord(out);
		createLinkingRecord(out);
		createTextRecord(out);
		createModificationRecord(out);
		createEndRecord(out);
	}

	/**
	 * <pre>
	 * Procedure Name: clearInstance
	 * Description:
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 9, 2012
	 * @author Zak
	 */
	public void clearInstance() {
		instance = new Program();
	}
}
