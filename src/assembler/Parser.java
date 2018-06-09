package assembler;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * 
 * <pre>
 * Class Name: Parser
 * Description: Parses input urban files and generates the program object 
 * while checking for syntax related errors.
 * 
 * Version information:
 * $RCSfile: Parser.java,v $
 * $Revision: 1.25 $
 * $Date: 2012/05/10 05:17:59 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public final class Parser {

	/**
	 * Private constructor. Used to remove default public constructor.
	 */
	private Parser() {

	}

	/**
	 * Trims end whitespace.
	 * 
	 * <pre>
	 * Procedure Name: trim
	 * Description: Trims beginning whitespace from the passed in StringBuilder
	 * 
	 * Specification reference code(s): S2.1, S2.5.2.1, S2.5.3.6
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested:	
	 * trim empty StringBuilder
	 * trim StringBuilder with no preceding spaces
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The StringBuilder that will be trimmed.
	 */
	private static void trim(final StringBuilder str) {
		// [empty MULTIPLE USE]
		int empty = firstNonWhiteSpace(str);
		if (empty > 0) {
			str.delete(0, empty);
		}
	}

	/**
	 * Parses a string into an integer.
	 * 
	 * <pre>
	 * Procedure Name: parseInputInt
	 * Description: Correctly parses strings that may be followed with leading 
	 * +,- or zeros
	 * If the string does not represent an integer, throws NumberFormatException
	 * 
	 * Specification reference code(s): S2.5.3.8, S2.2, S2.5.2.5
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: parse Empty string
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Zak 4/17/2012 Fixed nonterminating recursion for '-'
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The string whose integer value should be returned
	 * @return
	 *         The integer corresponding to the passed in string.
	 */
	private static int parseInputInt(final String str) {
		int result = 0;
		if (str.length() > 0) {
			if (str.charAt(0) == '-') {
				result = -parseInputInt(str.substring(1));
			} else if (str.charAt(0) == '+') {
				result = parseInputInt(str.substring(1));
			} else {
				result = Integer.parseInt(str);
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: isValidLabel
	 * Description: Checks if the passed in string meets the syntax 
	 * requirements for a label.
	 * 
	 * Specification reference code(s): S2.1, S2.5.2.2, S2.5.3.6, DS1, DS1.2
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: 
	 * 
	 * Modification Log (who when and why): 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 14, 2012
	 * @author Zak
	 * @param label
	 *            The string that must be checked for validity
	 * @return if the string is a valid label
	 */
	private static boolean isValidLabel(final String label) {
		boolean result = true;
		if (label.length() > 0) {
			// Must begin with alpha character
			if (!isLabelStart(label.charAt(0))) {
				result = false;
			}

			if (label.length() > 32) {
				result = false;
			}

			for (char ch : label.toCharArray()) {
				if (Character.isWhitespace(ch)) {
					result = false;
				} else if (ch == ';' || ch == ':' || ch == ',' || ch == '+'
						|| ch == '-' || ch == '*' || ch == '/') {
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: isLabelStart
	 * Description: Checks if a specific character is a valid first character
	 * of a label. Specifically, checks if {@code ch} is an alpha character.
	 * 
	 * Specification reference code(s): S2.1, DS1, DS3
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Zak 4/16/2012 Correctly incorporated upper-case numbers as well
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param ch
	 *            The character whose value will be checked
	 * @return if the character is the first character of a label
	 */
	private static boolean isLabelStart(final Character ch) {
		Boolean result = false;
		if (ch.toString().matches("[a-zA-z]")) {
			result = true;
		}
		return result;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: isApostrophe
	 * Description: Checks if {@code ch} is an apostrophe (single quote).
	 * 
	 * Specification reference code(s): S2.5, S2.5.3.7
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: Empty character string, no apostrophe
	 * Error messages generated: 217
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param ch
	 *            The character whose value will be checked
	 * @return whether the character passed in is an apostrophe
	 */
	private static boolean isApostrophe(final Character ch) {
		Boolean result = false;
		if (ch == '‘' || ch == '\'' || ch == '’') {
			result = true;
		}
		return result;
	}

	/**
	 * Gets the label from StringBuilder.
	 * 
	 * <pre>
	 * Procedure Name: getLabel
	 * Description: Gets the label as the first string in the incoming 
	 * StringBuilder followed by a whitespace and removes from {@code str}. 
	 * Returns empty string if there is no label.
	 * 
	 * Specification reference code(s): S2.1, S2.5.2.1, S2.5.3.6, DS3.2, DS1
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: get label from empty line, invalid label
	 * Error messages generated: 101, 202
	 * 
	 * Modification Log (who when and why): 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            StringBuilder from which label will be extracted
	 * @param errors
	 *            Set to which any errors encountered will be added
	 * @return the label
	 */
	private static String getLabel(final StringBuilder str,
			final Set<Integer> errors) {
		// [label String local stores the label for the current line]
		String label = "";
		if (!Character.isWhitespace(str.charAt(0))) {
			int empty = firstWhiteSpace(str);
			if (empty > 0) {
				label = str.substring(0, empty);

				if (!isValidLabel(label)) {
					errors.add(202);
					// In this case simply delete the label
					label = "";
				}
				str.delete(0, empty);
			} else {
				// Empty line
				errors.add(101);
			}
		}
		trim(str);
		return label;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: getMnemonic
	 * Description: Gets the mnemonic as the first string in the incoming 
	 * StringBuilder followed by a whitespace and removes from {@code str}. 
	 * Returns empty string if there is no label.
	 * 
	 * Specification reference code(s): S2.4, DS2
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: empty string, no mnemonic
	 * Error messages generated: 201
	 * 
	 * Modification Log (who when and why):
	 * Zak 4/15/12 Implemented addition of errors if no mnemonic
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The StringBuilder from which to extract the Mnemonic
	 * @param errors
	 *            Set to which any errors encountered will be added
	 * @return The mnemonic
	 */
	private static String getMnemonic(final StringBuilder str,
			final Set<Integer> errors) {
		// [mnemonic String local stores the mnemonic for the current line]
		String mnemonic = "";
		int empty = firstWhiteSpace(str);
		if (empty > 0) {
			mnemonic = str.substring(0, empty);
			str.delete(0, empty);
		} else {
			errors.add(201);
		}
		trim(str);
		return mnemonic.toUpperCase();
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: getOperands
	 * Description: Gets the operands as the first string in the StringBuilder
	 * until a semicolon or end of line is reached. Removes the operands from 
	 * the StringBuilder. 
	 * 
	 * Specification reference code(s): S2.5, DS3
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: No semicolon, Empty Keyword or Value, 
	 * Invalid operand syntax (not a number or label), Extra comma(s)
	 * Error messages generated: 108, 219, 220, 222, 106
	 * 
	 * Modification Log (who when and why):
	 * Zak 4/10/12 Added code blocks for where errors are checked
	 * Zak 4/16/12 Corrected values added to set of errors
	 * Zak 4/17/12 Corrected parsing of input for integers
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The StringBuilder from which to extract the operands
	 * @param errors
	 *            Set to which any errors encountered will be added
	 * @return the List of Operands
	 */
	private static List<Operand> getOperands(final StringBuilder str,
			final Set<Integer> errors) {
		List<Operand> operands = new ArrayList<Operand>();
		// [endOperandPos int local position of end of operands String]
		int endOperandPos = str.indexOf(";");
		if (endOperandPos < 0) {
			// No semicolon
			errors.add(108);
			endOperandPos = str.length();
		}

		if (str.substring(0, endOperandPos).endsWith(",")) {
			errors.add(106);
		}

		// [inOps String[] local Stores the operands as separated by commas in
		// input]
		String[] inOps = str.substring(0, endOperandPos).split(",");
		str.delete(0, endOperandPos + 1);
		if (inOps.length == 1 && inOps[0].trim().isEmpty()) {
			// No Operands: Needed because regex adds an empty operand
		} else {
			for (String inOp : inOps) {
				if (!inOp.isEmpty()) {
					// Necessary because split will not catch
					if (inOp.endsWith(":")) {
						errors.add(224);
					}
					// [fields String[] local stores the fields of an operand as
					// separated by colons in input]
					String[] fields = inOp.split(":");
					// [kw MULTIPLE USE]
					String kw = "";
					// [value MULTIPLE USE]
					String value = "";

					if (fields.length >= 2) {
						if (fields.length > 2) {
							errors.add(222);
						}
						kw = fields[0].trim().toUpperCase();
						value = fields[1].trim();

					} else if (fields.length == 1) {
						kw = fields[0].trim().toUpperCase();
					} else {
						errors.add(222);
					}

					if (kw.isEmpty()) {
						errors.add(219);
					}

					if (value.isEmpty()) {
						errors.add(220);
					} else {
						try {
							value = "" + parseInputInt(value);
						} catch (NumberFormatException e) {
							// Value is just not an integer and will be
							// validated later
						}
					}
					operands.add(new Operand(kw, value));
				} else {
					errors.add(106);
				}
			}
		}
		return operands;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: firstNonWhiteSpace
	 * Description: Reports the position of the first non whitespace cahracter
	 * (as defined by Java) in the StringBuilder. Returns the end of the string
	 * if no whitespace is encountered.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: All whitespace
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The StringBuilder whose first non whitespace is reported
	 * @return the positoin of the first non whitespace in {@code str}
	 */
	private static int firstNonWhiteSpace(final StringBuilder str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return str.length() - 1;
	}

	/**
	 * <pre>
	 * Procedure Name: firstWhiteSpace
	 * Description: Reports the position of the first whitespace character 
	 * (as defined by Java) in the StringBuilder. Returns the end of the 
	 * string if no whitespace is encountered.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: No whitespace
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param str
	 *            The StringBuilder whose first whitespace will be reported
	 * @return The position of the first whitespace in {@code str}
	 */
	private static int firstWhiteSpace(final StringBuilder str) {
		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i)) || str.charAt(i) == ';') {
				return i;
			}
		}
		return str.length();
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: checkOperandValues
	 * Description: checks that the operands have proper values.
	 * Specification reference code(s): S2.5, S2.5.3.7, S2.5.3.8, DS3
	 * 
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: 
	 * Invalid EXT/ENT operand value
	 * Invalid ST operand value
	 * Invalid LR operand value
	 * Otherwise, operand value is neither a label nor an integer
	 * 
	 * Error messages generated: 102, 217, 212, 207
	 * 
	 * Modification Log (who when and why): 
	 * Zak 4/18/12 Added code for validating expressions and * notation
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param mnemonic
	 *            The mnemonic correspoding to the operands
	 * @param ops
	 *            The operands whose values must be checked
	 * @param errors
	 *            Set to which any errors encountered will be added
	 * @return if the Mnemonic has correct operand values
	 */
	private static boolean checkOperandValues(final String mnemonic,
			final List<Operand> ops, final Set<Integer> errors) {
		Boolean result = true;
		// EXT and ENT will always have valid op values by simply deleting all
		// invalid ones
		// if (mnemonic.equals("EXT") || mnemonic.equals("ENT")) {
		// Iterator<Operand> it = ops.iterator();
		// while (it.hasNext()) {
		// Operand op = it.next();
		// if (!isValidLabel(op.getString())) {
		// errors.add(102);
		// it.remove();
		// }
		// }
		// } else if (mnemonic.equals("EQU")) {
		// for (Operand op : ops) {
		// if (op.getKeyword().equals("FC")) {
		// String value = op.getString();
		//
		// if (value.length() > 0) {
		// // Decide whether the value must be an integer or a
		// // string
		// // based
		// // on the first character
		// if (isLabelStart(value.charAt(0))) {
		// if (!isValidLabel(value)) {
		// errors.add(212);
		// result = false;
		// }
		// } else {
		// try {
		// int val = Integer.parseInt(value);
		// } catch (NumberFormatException e) {
		// errors.add(207);
		// result = false;
		// }
		// }
		// }
		// }
		// }
		// } else {
		// But in every other case you cannot recover from an invalid
		// operand value and must return false
		for (Operand op : ops) {
			if (op.getKeyword().equals("ST")) {
				String strLit = op.getString();
				if (!isApostrophe(strLit.charAt(0))
						|| !isApostrophe(strLit.charAt(strLit.length() - 1))) {
					errors.add(217);
					result = false;
				} else if (strLit.length() > 6) {
					// Too many characters
					errors.add(217);
					result = false;
				} else {
					// TODO: Check that all characters are valid ASCII
				}
			} else if (op.getKeyword().equals("LR")) {
				if (!isValidLabel(op.getString())) {
					errors.add(212);
					result = false;
				}
			} else if (op.getKeyword().equals("EX")) {
				// TODO: Check syntax of expression
				// } else {
				// String value = op.getString();
				//
				// if (value.length() > 0) {
				// // Decide whether the value must be an integer or a
				// // string
				// // based
				// // on the first character
				// if (isLabelStart(value.charAt(0))) {
				// if (!isValidLabel(value)) {
				// errors.add(212);
				// result = false;
				// }
				// } else {
				// try {
				// Integer.parseInt(value);
				// } catch (NumberFormatException e) {
				// errors.add(207);
				// result = false;
				// }
				// }
				// }
			}
		}
		// }
		return result;

	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: runFirstPass
	 * Description: runs the First Pass, where it parses the input as an 
	 * urban file and creates a program object. Tokenizes the values passed in 
	 * as the label followed by mnemonic, then operands, and finally 
	 * any comments after the semicolon. Then validates syntax of each token, 
	 * adding all errors to a list. Each line is added to the Program.
	 * 
	 * Specification reference code(s): S1, S2, S2.1, S2.2, S2.4, S2.5, 
	 * S2.5.2.1, S2.5.2.2, S2.5.2.3, S2.5.3.6, S2.5.3.7, S2.5.3.8, S3.0, 
	 * S4.0, S4.1, S5.0, DS1, DS1.1, DS1.2, DS1.3, DS2, DS3, DS3.1, DS3.2,
	 * DS3.4, DS3.5
	 * Calling Sequence: pass 1
	 * 
	 * Error Conditions Tested: Empty line(s), Invalid mnemonic, No KICKO, 
	 * invalid KICKO label, lines after end of program, program ends before END
	 * Also, any error conditions tested in any operation that is called
	 * Error messages generated: 101, 201, 305, 104, 109
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param in
	 *            The input from which to read the Urban Program
	 * @return the input parsed as a program
	 */
	public static Program runFirstPass(final Reader in) {
		Program p = Program.getInstance();
		p.clearInstance();
		Scanner input = new Scanner(in);

		// [programInitiated boolean local stores whether or not the program has
		// been initiated]
		Boolean programInitiated = false;
		// [programTerminated boolean local stores whether or not the program
		// has been terminated]
		Boolean programTerminated = false;

		while (input.hasNextLine() && !programTerminated) {
			// [origLine String local the original line from input]
			String origLine = input.nextLine();
			// [line StringBuilder local the line of input that gets modified as
			// it is parsed]
			StringBuilder line = new StringBuilder(origLine);
			// [errors Set<Integer> local the set containing any errors
			// encountered during parsing]
			Set<Integer> errors = new HashSet<Integer>();

			if (origLine.isEmpty()) {
				errors.add(101);
				p.addLine(origLine, "", "", "", new ArrayList<Operand>(),
						errors);
			} else if (line.charAt(0) == ';') {
				p.addLine(origLine, "", "", "", new ArrayList<Operand>(),
						errors);
			} else {

				// Get the label
				String label = getLabel(line, errors);
				String mnemonic = getMnemonic(line, errors);
				// [ops MULTIPLE USE]
				List<Operand> ops = getOperands(line, errors);

				// Check that program has valid KICKO
				if (mnemonic.equals("KICKO")) {
					if (!programInitiated) {
						programInitiated = true;
						if (isValidLabel(label) && !label.isEmpty()) {
							p.setName(label);
						} else {
							// KICKO must have valid label
							errors.add(305);
						}
					} else {
						// Expecting KICKO to start program
						errors.add(306);
					}
				}

				if (!programInitiated) {
					// Missing KICKO
					errors.add(305);
				}

				// Check that certain mnemonics have labels
				if (mnemonic.equals("EQU") || mnemonic.equals("EQUE")
						|| mnemonic.equals("NEWLC")) {
					if (label.isEmpty()) {
						errors.add(223);
					}
				}

				// Only check operands if mnemonic is valid
				// [directive String local stores the directive of the current
				// line]
				String directive = "";
				// [instruction String local stores the instruction of the
				// current line]
				String instruction = "";

				// [hasValidKeywords boolean local whether or not the current
				// line has valid keywords]
				boolean hasValidKeywords = false;

				// [instTable MULTIPLE USE]
				InstrucTable instTable = InstrucTable.getInstance();
				if (instTable.isDefined(mnemonic)) {
					if (instTable.isDirective(mnemonic)) {
						directive = mnemonic;
					} else {
						instruction = mnemonic;
					}
					hasValidKeywords = InstrucTable.getInstance()
							.checkOperandKeywords(mnemonic, ops, errors);
					checkOperandValues(mnemonic, ops, errors);

				} else {
					// Invalid mnemonic
					errors.add(201);
					instruction = "NOP";
				}

				// If we reach end, label the program as terminated
				if (mnemonic.equals("END")) {
					programTerminated = true;
					if (hasValidKeywords) {
						if (!ops.get(0).getString().equals(p.getName())) {
							errors.add(105);
						}
					}
				}
				if (programTerminated && input.hasNext()) {
					// Lines after end of program
					errors.add(104);
				}

				if (!programTerminated && !input.hasNext()) {
					errors.add(109);
				}

				p.addLine(origLine, label, instruction, directive, ops, errors);
			}

		}

		return p;
	}

	/**
	 * Used to run any quick tests on runFirstPass.
	 * Attempts to parse an input file and output listing + symbol table.
	 * 
	 * @since Apr 17, 2012
	 * @author Zak
	 * @param args
	 *            Array of Input Parameters
	 */
	public static void main(final String[] args) {
		// NO MORE BULLSHIT

	}
}
