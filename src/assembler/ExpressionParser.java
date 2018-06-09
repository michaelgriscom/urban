package assembler;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * Class Name: ExpressionParser
 * Description: Static class to parse expressions (all three kinds)
 * 
 * Version information:
 * $RCSfile: ExpressionParser.java,v $ 
 * $Revision: 1.16 $
 * $Date: 2012/05/09 20:16:59 $
 * </pre>
 * 
 * @author andrew
 * 
 */
public class ExpressionParser {
	// Fine, I'll explain this beauty...
	// Match a sign, followed by either a decimal number or a label, then the
	// rest of the string
	// The sign, the number or label, and the rest are all captured.
	// Spaces are allowed between each part
	// This should be used iteratively, to parse out the first part of an
	// expression then leave the rest,
	// Until the rest is length 0.
	// [termPattern Pattern global Horrendously ugly regex used to pick apart
	// complex
	// equate expressions]
	private static final Pattern termPattern = Pattern
			.compile("([+|\\-])\\s*(\\d+|[A-Za-z][A-Za-z0-9!@#$%^&()_=\\[\\]{}'\".<>/?]{0,31})\\s*(.*)");
	private static final Integer ERROR_STAR_AND_LABEL = 225;
	private static final Integer ERROR_SIGNED_LABEL = 226;
	private static final Integer ERROR_BAD_STAR = 218;
	private static final Integer ERROR_FORMAT = 221;

	/**
	 * <pre>
	 * Class Name: BadAsteriskException
	 * Description: Thrown when an asterisk appears in an expression, other than as the first term
	 * 
	 * Version information:
	 * $RCSfile: ExpressionParser.java,v $
	 * $Revision: 1.16 $
	 * $Date: 2012/05/09 20:16:59 $
	 * </pre>
	 * 
	 * @author andrew
	 * 
	 */
	public static class BadAsteriskException extends Exception {
		/**
		 * Create a BadAsteriskException.
		 */
		public BadAsteriskException() {
		}
	}

	/**
	 * <pre>
	 * Class Name: BadFormatException
	 * Description: Thrown when the syntax of an expression is wrong.
	 * 
	 * Version information:
	 * $RCSfile: ExpressionParser.java,v $
	 * $Revision: 1.16 $
	 * $Date: 2012/05/09 20:16:59 $
	 * </pre>
	 * 
	 * @author andrew
	 * 
	 */
	public static class BadFormatException extends Exception {
		/**
		 * Create a BadAsteriskException.
		 */
		public BadFormatException() {
		}
	}

	/**
	 * <pre>
	 * Procedure Name: parse
	 * Description: Parse an expression, for a DM or FM field
	 * 
	 * Specification reference code(s): EX1,EX2,EX3
	 * Calling Sequence: (pass 1, pass 2, or both) both
	 * 
	 * Error Conditions Tested: Expression syntax, undefined symbols
	 * Error messages generated: 202,207,213,214,218,221
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author andrew
	 * @param expr
	 *            An expression to parse
	 * @param prog
	 *            A program object, used mainly for its symbol table
	 * @param addr
	 *            The value of the LC at the time the equate was defined, ie the
	 *            value of *
	 * @return
	 *         The parsed value of the expression
	 * @throws NumberFormatException
	 *             If something that appears to be a number cannnot be parsed
	 * @throws NoSuchFieldException
	 *             In case of an undefined symbol
	 * @throws BadAsteriskException
	 *             If an asterisk appears outside of the first term
	 * @throws BadFormatException
	 *             If the expression is malformed
	 */
	public static int parseEquExpression(String expr, Program prog, int addr)
			throws BadFormatException, BadAsteriskException,
			NoSuchFieldException {

		// [res int local The value of an expression]
		int res = 0;

		expr.trim();

		if (expr.charAt(0) == '*') {
			res = addr;
			expr = expr.substring(1).trim();
		} else if (expr.charAt(0) != '+' && expr.charAt(0) != '-') {
			expr = "+" + expr;
		}

		while (expr.length() > 0) {
			// [m Matcher local A regex matcher]
			Matcher m = termPattern.matcher(expr);
			if (!m.matches()) {
				throw new BadFormatException();
			}
			if (m.group(2).equals("*")) {
				throw new BadAsteriskException();
			}
			// The sheer ridiculousness of the DED makes this easier to write
			// than proper code...
			// If the operator is '+', then add, otherwise subtract
			// Either way, the absolute value is the result of resolving the 2nd
			// group of the regex.
			res += (m.group(1).equals("+") ? 1 : -1)
					* prog.equateSymbol(m.group(2));
			expr = m.group(3);
		}

		return res;
	}

	/**
	 * <pre>
	 * Procedure Name: parseOperandExpression
	 * Description: Parse an operand expression, as might appear with a MOVD or similar instruction.
	 * The expression must either (1) start with an asterisk, optionally followed by a + or - then a number,
	 * or (2) start with a number, with or without a sign.  No other formats are allowed.
	 * 
	 * Specification reference code(s): EX1
	 * Calling Sequence: (pass 1, pass 2, or both) pass2
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why): Andrew, 7 May, Start implementation
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Michael
	 * @param expr
	 *            An expression to parse
	 * @param prog
	 *            A reference to current program object
	 * @param addr
	 *            The value of the current LC; ie, the value of *
	 * @return The value of the evaluated expression
	 * @throws BadFormatException
	 *             If there is a format error
	 * @throws NoSuchFieldException
	 * @throws NumberFormatException
	 */
	public static int parseOperandExpression(String expr, Program prog,
			Line line) throws BadFormatException, BadAsteriskException,
			NumberFormatException, NoSuchFieldException {
		// [res int local The value of the expression]
		// [nul int local A multiplier, to represent the sign of the constant
		// term of
		// the operand expression]
		// [hadStar boolean local True iff the operand expression started with a
		// star]
		int res = 0, mul = 0;
		boolean hadStar = false, gotSymbol = false;
		expr = expr.trim();

		// Assume it's a literal until we discover otherwise
		// Check that it is a memory
		boolean isMemory = false;
		for (Operand op : line.ops) {
			if (op.getOperandType().equals(Operand.OpType.MEMORY)) {
				isMemory = true;
			}
		}

		if (isMemory) {
			line.relocationFlag = 'R';
		} else {
			line.relocationFlag = 'A';
		}

		// Look for a star at the beginning.
		if (expr.charAt(0) == '*') {
			res = line.address;
			line.relocationFlag = 'R';
			line.modifyRecords.add("+:" + prog.getName());
			expr = expr.substring(1).trim();
			// Special case: FM:* just returns the current LC
			if (expr.length() == 0) {
				return res;
			}
			// Otherwise, we got more to do
			hadStar = true;
		}

		// If there's a star anywhere else, die early
		if (expr.indexOf("*") >= 0) {
			line.errors.add(ERROR_BAD_STAR);
		}

		// We either got the star, or not. Either way, continue on happily.
		if (expr.charAt(0) == '+') {
			mul = 1;
			expr = expr.substring(1).trim();
		} else if (expr.charAt(0) == '-') {
			mul = -1;
			expr = expr.substring(1).trim();
		} else if (hadStar) {
			// We didn't have an operand, but we had a star. That's an error.
			line.errors.add(ERROR_FORMAT);
		}

		if (prog.symbolDefined(expr)) {
			// [usage String local The usage of a label (assuming the expression
			// comprises a label]
			String usage = prog.symbolUsage(expr);
			if (usage.equals("LABEL") || usage.equals("PRGM_NAME")) {
				if (hadStar) {
					line.errors.add(ERROR_STAR_AND_LABEL);
					res = 0;
				} else if (mul != 0) {
					line.errors.add(ERROR_SIGNED_LABEL);
					res = 0;
				} else {
					line.relocationFlag = 'R';
					line.modifyRecords.add("+:" + prog.getName());
					res = prog.symbolLocation(expr);
				}
				gotSymbol = true;
			} else if (usage.equals("EXT")) {
				if (hadStar) {
					line.errors.add(ERROR_STAR_AND_LABEL);
				} else if (mul != 0) {
					line.errors.add(ERROR_SIGNED_LABEL);
				} else {
					line.relocationFlag = 'E';
					line.modifyRecords.add("+:" + expr);
				}
				res = 0;
				gotSymbol = true;
			}
		}

		if (!gotSymbol) {
			// mul will still be zero if there wasn't an operator
			// In that case, assume it's positive
			if (mul == 0) {
				mul = 1;
			}
			res += mul * prog.equateSymbol(expr);
		}
		// All done!
		return res;
	}

	/**
	 * <pre>
	 * Procedure Name: parseAdrcExpression
	 * Description:
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * ption: contains the abstract state for a program.
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 6, 2012
	 * @author Michael
	 * @param exprk
	 *            An ADRC expression to parse
	 * @param prog
	 *            A program object, to resolve local labels and equates
	 * @param line
	 *            A line object, to query for the current LC and populate with
	 *            results
	 * @return The constant part of the expression (ie, the sum of the LCs of
	 *         the local labels and any constants)
	 * @throws BadAsteriskException
	 * @throws BadFormatException
	 * @throws NoSuchFieldException
	 * @throws NumberFormatException
	 */
	public static int parseAdrcExpression(String expr, Program prog, Line line)
			throws BadAsteriskException, BadFormatException,
			NumberFormatException, NoSuchFieldException {
		// [res int local The constant part of the ADRC expression being
		// calculated;
		// ie, the sum of the LC's of any local labels plus any constants]
		int res = 0;

		// Reset the state of the line, to be safe
		line.modifyRecords = new ArrayList<String>();
		line.localModifies = 0;
		// Is it a star expression?
		if (expr.charAt(0) == '*') {
			res = line.address;
			line.localModifies++;
			expr = expr.substring(1).trim();
		} else {
			// For the upcoming loop, we always assume the expression starts
			// with a plus or minus sign
			if (expr.charAt(0) != '+' && expr.charAt(0) != '-') {
				expr = "+" + expr;
			}
		}

		// Make sure there aren't any stars, except maybe at the beginning
		if (expr.indexOf('*') >= 0) {
			throw new BadAsteriskException();
		}

		// Pull out terms one at a time
		while (expr.length() > 0) {
			// [mul int local A multiplier, to represent the sign of the current
			// term
			// of the expression]
			int mul = 1;
			// [evaluatedTerm boolean True only if the term has been evaluated
			// already]
			boolean evaluatedTerm = false;
			if (expr.charAt(0) == '-') {
				mul = -1;
			} else if (expr.charAt(0) != '+') {
				throw new BadFormatException();
			}
			expr = expr.substring(1).trim();

			// Find the end of this next term
			int nextPlus = expr.indexOf('+');
			int nextMinus = expr.indexOf('-');
			int nextOp;
			if (nextPlus >= 0 && nextMinus >= 0) {
				nextOp = Math.min(nextPlus, nextMinus);
			} else if (nextPlus >= 0) {
				nextOp = nextPlus;
			} else if (nextMinus >= 0) {
				nextOp = nextMinus;
			} else {
				nextOp = expr.length();
			}

			String nextTerm = expr.substring(0, nextOp).trim();

			if (prog.symbolDefined(nextTerm)) {
				String usage = prog.symbolUsage(nextTerm);
				if (usage.equals("LABEL") || usage.equals("PRGM_NAME")) {
					res += mul * prog.symbolLocation(nextTerm);
					line.localModifies += mul;
					evaluatedTerm = true;
				} else if (usage.equals("EXT")) {
					if (mul > 0) {
						line.modifyRecords.add("+:" + nextTerm);
					} else {
						line.modifyRecords.add("-:" + nextTerm);
					}
					evaluatedTerm = true;
				}
			}

			if (!evaluatedTerm) {
				res += mul * prog.equateSymbol(nextTerm);
			}

			expr = expr.substring(nextOp);
		}

		if (line.localModifies == 0 && line.modifyRecords.size() == 0) {
			line.relocationFlag = 'A';
		} else if (line.modifyRecords.size() == 1 && line.localModifies == 0) {
			line.relocationFlag = 'E';
		} else if (line.localModifies == 1 && line.modifyRecords.size() == 0) {
			line.relocationFlag = 'R';
		} else {
			line.relocationFlag = 'C';
		}

		return res;
	}
}
