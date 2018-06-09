package simulatorTest;

/**
 * <pre>
 * Class Name: Operand
 * Description: holds operand data
 * 
 * Version information:
 * $RCSfile: Operand.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/05/23 00:51:36 $
 * </pre>
 * 
 * @author Andrew
 * 
 */
public class Operand {

	private static final int OUT_OF_RANGE_ERROR = 214;
	private static final int BYTES_IN_WORD = 4;
	private static final int BITS_IN_BYTE = 8;

	/*
	 * Initialize a string-valued operand.
	 * 
	 * This should only be used with EX, DR, FR, LR, or ST Keywords
	 * 
	 * @param kw
	 * The keyword
	 * 
	 * @param str
	 * The string value
	 */
	public Operand(String kw, String str) {
		this.kw = kw;
		this.str = str;
	}

	/**
	 * <pre>
	 * Procedure Name: getKeyword
	 * Description: gets the keyword for a given operand.
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 5, 2012
	 * @author Andrew
	 * @return the keyword for the operand
	 */
	public String getKeyword() {
		return kw;
	}

	/**
	 * <pre>
	 * Procedure Name: getString
	 * Description: get the string value of the operand.
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: NullPointerException if operand has numeric value
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 5, 2012
	 * @author Andrew
	 * @return the string value of the perand
	 */
	public final String getString() {
		if (str == null) {
			throw new NullPointerException(kw + " doesn't have a string value");
		}
		return str;
	}

	@Override
	public String toString() {
		return this.kw.toString() + ":" + this.str;
	}

	// [kw String local The keyword of this operand]
	// [str String local The string vaue of this operand]
	// [OpType enumeration global collection of possible operand types]
	private final String kw;

	private final String str;

	/**
	 * <pre>
	 * Class Name:
	 * Description:
	 * 
	 * Version information:
	 * $RCSfile: Operand.java,v $
	 * $Revision: 1.1 $
	 * $Date: 2012/05/23 00:51:36 $
	 * </pre>
	 * 
	 * @author Michael
	 * 
	 */
	public enum OpType {
		INDEX_REGISTER, ARITHMETIC_REGISTER, LITERAL, CONSTANT, EXPRESSION, NUMBER_WORDS, STRING, MEMORY, LABEL, UNDEFINED
	}

	/**
	 * <pre>
	 * Procedure Name: getFieldLength
	 * Description: gets the field length for a given operand type.
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: operand of nonexistant type
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @param t
	 *            is the OpType to look up
	 * @return the field length for the given OpType
	 */
	public static int getFieldLength(OpType t) {
		// [t OpType local temporary operand type to be looked up]
		// [fieldLength int local field length for the given operand type]
		int fieldLength = 0;
		switch (t) {
		case INDEX_REGISTER:
		case ARITHMETIC_REGISTER:
			fieldLength = 3;
			break;

		case LITERAL:
			fieldLength = 13;
			break;

		case CONSTANT:
			fieldLength = 13;
			break;
		case STRING:
			fieldLength = 32;
			break;

		case NUMBER_WORDS:
			fieldLength = 4;
			break;

		case MEMORY:
			fieldLength = 13;
			break;

		default:
			fieldLength = 0;
			break;
		}
		return fieldLength;
	}

	/**
	 * <pre>
	 * Procedure Name: getOperandType
	 * Description: Gets the operand type for a given operand.
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: nonexistant operand
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 16, 2012 fixed error with nonexistant operand
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 13, 2012
	 * @author Michael
	 * @return the operand type for a given operand
	 */
	public OpType getOperandType() {
		// [o Optype local temporary OpType to be returned based on given
		// oeprand]
		OpType o = null;

		if (this.kw.equals("LR")) {
			o = OpType.LABEL;
		} else if (this.kw.equals("FL")) {
			o = OpType.LITERAL;
		} else if (this.kw.equals("EX")) {
			o = OpType.EXPRESSION;
		} else if (this.kw.equals("DM") || this.kw.equals("FM")) {
			o = OpType.MEMORY;
		} else if (this.kw.equals("DR") || this.kw.equals("FR")) {
			o = OpType.ARITHMETIC_REGISTER;
		} else if (this.kw.equals("DX") || this.kw.equals("FX")) {
			o = OpType.INDEX_REGISTER;
		} else if (this.kw.equals("FC")) {
			o = OpType.CONSTANT;
		} else if (this.kw.equals("NW")) {
			o = OpType.NUMBER_WORDS;
		} else if (this.kw.equals("ST")) {
			o = OpType.STRING;
		} else {
			o = OpType.UNDEFINED;
		}

		return o;
	}

	/**
	 * <pre>
	 * Procedure Name: parseStringLiteral
	 * Description: parse a string literal operand.
	 * 
	 * Specification reference code(s): DSX
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since Apr 17, 2012
	 * @author Michael
	 * @param opArg
	 *            is the operand to parse
	 * @return the binary ascii equivalent of opArg
	 */
	private int parseStringLiteral(String opArg) {
		// [ch char local character of the string literal]
		// [iCode int local integer value for the instruction code]

		int iCode = 0;

		char[] chars = opArg.substring(1, opArg.length() - 1).toCharArray();

		// add ascii characters to instruction code
		for (char ch : chars) {
			// chars is array of characters of argument
			iCode = (iCode << BITS_IN_BYTE) + (int) ch;
		}

		// pad with space characters to ensure 4 total chars
		for (int i = chars.length; i < BYTES_IN_WORD; i++) {
			iCode = (iCode << BITS_IN_BYTE) + (int) ' ';
		}

		return iCode;
	}

	/**
	 * <pre>
	 * Procedure Name: getOperandBinary
	 * Description: get the binary equivalent for a given operand
	 * 
	 * Specification reference code(s): SX, DSX
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested: invalid operand
	 * Error messages generated: out of bounds error
	 * 
	 * Modification Log (who when and why):
	 * May 8, 2012 Michael added in program singleton design changes
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @author Michael
	 * @param errors
	 * @return operand binary equivalent
	 */
	public String getOperandBinary(Line line) throws IllegalAccessException {
		// [p Program local program instance]
		// [opArg string local argument for a given operand]

		String opArg = this.getString();
		Operand.OpType opType = this.getOperandType();
		int iCode = 0;
		String binary = "";

		switch (opType) {
		case MEMORY:
			try {
				iCode = Integer.parseInt(opArg);
			} catch (Exception e) {
				// TODO: Add error?
				binary = "";
			}
			break;

		case ARITHMETIC_REGISTER:
			iCode = Integer.parseInt(opArg);
			break;

		case INDEX_REGISTER:
			iCode = Integer.parseInt(opArg);
			break;

		case NUMBER_WORDS:
			iCode = Integer.parseInt(opArg);
			break;

		case CONSTANT: // must be positive 4095
		case LITERAL:
			iCode = Integer.parseInt(opArg);
			/*
			 * if (instruction.equals("DMP")) {
			 * if (iCode < 1 || iCode > 3) {
			 * errors.add(OUT_OF_RANGE_ERROR);
			 * }
			 * } else
			 */
			break;

		case STRING:
			iCode = this.parseStringLiteral(opArg);
			break;

		case LABEL:
			iCode = Integer.parseInt(opArg);
			break;

		default:
			break;
		}

		// [fieldLength int local maximum length of operand field]
		int fieldLength = Operand.getFieldLength(opType);

		// only add operand equivalent if it isn't out of range
		binary = Integer.toBinaryString(iCode);

		// trim/extend to size
		while (binary.length() > fieldLength) {
			binary = binary.substring(1);
		}
		while (binary.length() < fieldLength) {
			binary = "0" + binary;
		}
		return binary;
	}
}
