package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import simulator.Memory.MemoryAccessException;

/**
 * <pre>
 * Class Name: Simulator
 * Description: simulates the URBAN environment by loading an executing a
 * loader file.
 * 
 * Version information:
 * $RCSfile: Simulator.java,v $
 * $Revision: 1.10 $
 * $Date: 2012/05/29 19:50:26 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public class Simulator {
	// [MISSING_HEADER_ERROR int local constant for error code]
	// [LINES_AFTER_END_ERROR int local constant for error code]
	// [INVALID_END_RECORD_ERROR int local constant for error code]
	// [INVALID_ASSEMBLER_NAME_ERROR int local constant for error code]
	// [INVALID_ADDRESS_ERROR int local constant for error code]
	// [INVALID_NAME_ERROR int local constant for error code]
	// [INVALID_INSTRUCTION_ERROR int local constant for error code]
	// [INVALID_RECORD_COUNT int local constant for error code]
	// [nopHex String local hex value for nop instruction]
	// [HEXADECIMAL int local value for base 16]
	// [RECORD_HEX_DIGITS int local number of hex digits for record entries]
	// [cpu Processor local instance of the Processor singleton]
	// [mem Memory local instance of the Memory singleton]
	// [errorTable ErrorTable local instance of the ErrorTable singleton]

	private static final int MISSING_HEADER_ERROR = 307;
	private static final int LINES_AFTER_END_ERROR = 111;
	private static final int INVALID_END_RECORD_ERROR = 229;
	private static final int INVALID_ASSEMBLER_NAME_ERROR = 112;
	private static final int INVALID_ADDRESS_ERROR = 306;
	private static final int INVALID_NAME_ERROR = 110;
	private static final int INVALID_INSTRUCTION_ERROR = 228;
	private static final int INVALID_RECORD_COUNT = 230;

	private static final String nopHex = "F4000000";
	private static final int HEXADECIMAL = 16;
	private static final int RECORD_HEX_DIGITS = 4;

	private static Processor cpu = Processor.getInstance();
	private static Memory mem = Memory.getInstance();
	private ErrorTable errorTable = ErrorTable.getInstance();

	/**
	 * <pre>
	 * Procedure Name: Simulate
	 * Description: simulate a given load file
	 * 
	 * Specification reference code(s): LMX
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested:
	 * Error messages generated: error messages attached to given line
	 * 
	 * Modification Log (who when and why):
	 * Michael May 20, 2012 updated to conform to documentation standards,
	 * added error handling
	 * Michael May 22, 2012 updated to reflect processor-memory instantiation
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author Michael
	 * @param loaderFile
	 *            is the file to be loaded into memory
	 * @param in
	 *            is the input for IO
	 * @param out
	 *            is the output for IO
	 * @param debug
	 */
	public void Simulate(final Reader loaderFile, InputStream in,
			OutputStream out, boolean debug) {
		// [fatalError boolean local flag for if a fatal error is encountered]
		// [initialLC int local execution start address for program]
		// [haltFlag boolean local flag for if a halt is encountered]
		// [ps PrintStream local print stream for simulator output]

		PrintStream ps = new PrintStream(out);

		Processor.setIO(in, out);
		if (debug) {
			cpu.debugMode();
		}
		boolean fatalError = loadMemory(loaderFile, out);
		if (fatalError) {
			ps.println("A fatal error has occurred. Program will not be executed");
			return;
		}

		ps.println("MEMORY DUMP:");
		mem.dump(out);

		int initialLC = mem.getExecutionStart();
		cpu.setMemory(mem);
		cpu.setLC(initialLC);

		ps.println("SIMULATION START");
		boolean haltFlag = false;
		while (!haltFlag) {
			haltFlag = cpu.executeInstruction();
		}

		ps.println("SIMULATION END");
	}

	/**
	 * <pre>
	 * Procedure Name: loadMemory
	 * Description: load a file into memory. Returns true if a fatal error
	 * has occurred
	 * 
	 * Specification reference code(s): LMX
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested: file not found
	 * Error messages generated: memory out of bounds error
	 * 
	 * Modification Log (who when and why):
	 * Michael May 22, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 17, 2012
	 * @author Michael
	 * @param in
	 *            is the input file stream
	 * @param os
	 *            is the output file stream
	 * @return true if a fatal error occurred
	 */
	private boolean loadMemory(Reader in, OutputStream os) {
		// [errors Set<Integer> local errors for current line]
		// [fatalError boolean local flag for if a fatal error occurs]
		// [input Scanner local scanner for reading loader file]
		// [numRecs int local number of records in loader file]
		// [record String local record for current line of loader file]

		Set<Integer> errors = new HashSet<Integer>();

		boolean fatalError = false;

		Scanner input = new Scanner(in);
		int numRecs = 0;

		String record = input.nextLine().trim();

		while (record.length() == 0) {
			record = input.nextLine().trim();
		}

		try {
			errors = parseHeader(record);
		} catch (MemoryAccessException e) {
			errors.add(INVALID_ADDRESS_ERROR);
		}
		fatalError = this.errorTable.fatalError(errors);

		displayLine(record, errors, os);

		numRecs++;

		while (input.hasNextLine()) {
			record = input.nextLine().trim();
			if (record.length() == 0) {
				continue;

			} else if (!record.substring(0, 1).equals("T")) {
				numRecs++;
				break;
			}

			numRecs++;
			try {
				errors = parseText(record);
			} catch (MemoryAccessException e) {
				errors.add(INVALID_ADDRESS_ERROR);
			}

			displayLine(record, errors, os);
			fatalError = this.errorTable.fatalError(errors);
		}

		errors = parseEnd(record, numRecs);
		if (input.hasNextLine()) {
			errors.add(LINES_AFTER_END_ERROR);
		}
		displayLine(record, errors, os);
		fatalError = this.errorTable.fatalError(errors);

		return fatalError;

	}

	/**
	 * <pre>
	 * Procedure Name: displayLine
	 * Description: display a record line
	 * 
	 * Specification reference code(s): LMX
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: error messages associated with line
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param record
	 *            is the record to print
	 * @param errors
	 *            are the errors associated with the line
	 * @param os
	 *            is the stream to output to
	 */
	private void displayLine(String record, Set<Integer> errors, OutputStream os) {
		// [out PrintStream local stream to print output to]

		PrintStream out = new PrintStream(os);

		out.println(record);
		for (int error : errors) {
			out.println("**ERROR " + error + "** "
					+ this.errorTable.getErrorMessage(error));
		}
	}

	/**
	 * <pre>
	 * Procedure Name: parseHeader
	 * Description: parse a header record from a loader file
	 * 
	 * Specification reference code(s): LM1X
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested: bad hex format, incorrect record, out of bounds
	 * location
	 * Error messages generated:  out of bounds value, invalid program name,
	 * 
	 * Modification Log (who when and why):
	 * Michael May 22, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param record
	 *            is the record to parse
	 * @return set of errors encountered
	 * @throws MemoryAccessException
	 *             if a memory out of bounds access occurs
	 */
	private Set<Integer> parseHeader(String record)
			throws MemoryAccessException {
		// [moduleName String local name of current module]

		Set<Integer> errors = new HashSet<Integer>();

		if (!record.substring(0, 1).equals("H")) {
			errors.add(MISSING_HEADER_ERROR);
		}

		String[] recordFields = record.split(":");

		String moduleName = recordFields[1];
		if (!isValidLabel(moduleName)) {
			errors.add(INVALID_NAME_ERROR);
		}

		String loadAddr = recordFields[2];
		int startAddr = getRecordValue(loadAddr);

		mem.setLowerBound(startAddr);

		String executionStart = recordFields[3];
		int execAddr = getRecordValue(executionStart);

		mem.setExecutionStart(execAddr);

		String progLength = recordFields[4];
		int length = getRecordValue(progLength);
		if (length < 0) {
			throw mem.new MemoryAccessException();
		}

		mem.setUpperBound(length + startAddr - 1);

		// String dateTime = recordFields[5];
		String assemblerName = recordFields[6];

		if (!assemblerName.equals("URBAN-LLM")) {
			errors.add(INVALID_ASSEMBLER_NAME_ERROR);
		}

		// String version = recordFields[7];

		String programName = recordFields[8];
		if (!isValidLabel(programName)) {
			errors.add(INVALID_NAME_ERROR);
		}
		mem.setProgramName(programName);

		return errors;
	}

	/**
	 * <pre>
	 * Procedure Name: parseText
	 * Description: parse a text record from a loader file
	 * 
	 * Specification reference code(s): LM2X
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested: bad hex format, incorrect record, out of bounds
	 * location
	 * Error messages generated:  out of bounds value, invalid program name
	 * 
	 * Modification Log (who when and why):
	 * Michael May 22, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param record
	 *            is the record to parse
	 * @return set of errors encountered
	 * @throws MemoryAccessException
	 *             if a memory out of bounds access occurs
	 */
	private Set<Integer> parseText(String record) throws MemoryAccessException {
		// [lc String local value of text record LC represented as a string]
		// [addr int local value of address of current text record]
		// [inst string local instruction code of current text record as a
		// string]
		// [instruction int local instruction code of current text record as an
		// integer]

		Set<Integer> errors = new HashSet<Integer>();

		String[] recordFields = record.split(":");

		String lc = recordFields[1];
		if (!isValidHex(lc) || lc.length() != 6) {
			throw mem.new MemoryAccessException();
		}
		int addr = Integer.parseInt(lc, HEXADECIMAL);

		// System.out.println("address= " + addr);
		String inst = recordFields[2];
		if (!isValidHex(inst) || inst.length() != 8) {
			errors.add(INVALID_INSTRUCTION_ERROR);
			inst = nopHex;
		}
		int instruction = (int) Long.parseLong(inst, HEXADECIMAL);

		mem.write(instruction, addr);

		String programName = recordFields[3];
		if (!programName.equals(mem.getProgramName())) {
			errors.add(INVALID_NAME_ERROR);
		}
		return errors;
	}

	/**
	 * <pre>
	 * Procedure Name: parseEnd
	 * Description: parse an end record from a loader file
	 * 
	 * Specification reference code(s): LM5X
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested: bad hex format, incorrect end record
	 * Error messages generated:  missing end record, invalid program name,
	 * invalid record count
	 * 
	 * Modification Log (who when and why):
	 * Michael May 22, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param record
	 *            is the record to parse
	 * @param numRecs
	 *            is the total number of records counted (including the end
	 *            record)
	 * @return set of errors encountered
	 */
	private Set<Integer> parseEnd(String record, int numRecs) {
		// [recordFields String[] local array of the record fields for the
		// current line]
		// [records String local number of records in loader file as a string]
		// [recs int local number of records in loader file as an integer]
		// [textRecords String local number of text records in a loader file as
		// a string]
		// [textRecs int local number of text records in a loader file as an
		// integer]
		// [programName String local name of program]

		Set<Integer> errors = new HashSet<Integer>();
		if (!record.substring(0, 1).equals("E")) {
			errors.add(INVALID_END_RECORD_ERROR);
		}

		String[] recordFields = record.split(":");

		String records = recordFields[1];
		int recs = getRecordValue(records);
		if (recs != numRecs) {
			errors.add(INVALID_RECORD_COUNT);
		}

		String textRecords = recordFields[2];
		int textRecs = getRecordValue(textRecords);
		if (textRecs != recs - 2) {
			errors.add(INVALID_RECORD_COUNT);
		}

		String programName = recordFields[3];
		if (!programName.equals(mem.getProgramName())) {
			errors.add(INVALID_NAME_ERROR);
		}
		return errors;
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
	 * <pre>
	 * Procedure Name: isValidHex
	 * Description: check if hex is valid
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 22, 2012
	 * @author Zak
	 * @param hex
	 *            is the string to check
	 * @return true iff hex is valid
	 */
	private static boolean isValidHex(String hex) {
		return hex.matches("[0-9A-F]{0,}");
	}

	/**
	 * <pre>
	 * Procedure Name: getRecordValue
	 * Description: get the integer value of a hex record, return -1 if failed
	 * 
	 * Specification reference code(s): LMX
	 * Calling Sequence: N/A
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Zak
	 * Testing standards met: Signed by Zak
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Michael
	 * @param hex
	 *            is the string to parse
	 * @return the integer value of the hex parameter, or -1 if failed
	 */
	private int getRecordValue(String hex) {
		// [val multiple use]

		int val;
		if (!isValidHex(hex) || hex.length() != RECORD_HEX_DIGITS) {
			return -1;
		}
		try {
			val = Integer.parseInt(hex, HEXADECIMAL);
		} catch (Exception e) {
			return -1;
		}
		return val;
	}

	public static void main(String[] args) {
		String filename = "testLoaderFile.txt";
		boolean debug = true;

		if (args.length != 2) {
			System.err.println("Incorrect usage");
			System.exit(-1);
		}

		filename = args[0];
		debug = (args[1].equals("1"));

		try {
			Reader loaderFile = new FileReader(new File(filename));
			OutputStream out = System.out;
			InputStream in = System.in;

			Simulator sim = new Simulator();
			sim.Simulate(loaderFile, in, out, debug);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
