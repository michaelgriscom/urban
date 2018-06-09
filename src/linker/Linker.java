package linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * <pre>
 * Class Name: Linker
 * Description: Links object file and generates output using 2 pass loader.
 * 
 * Version information:
 * $RCSfile: Linker.java,v $
 * $Revision: 1.50 $
 * $Date: 2012/05/30 20:35:42 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public class Linker {

	/**
	 * 
	 * <pre>
	 * Class Name: RecordType
	 * Description: Enumeration containing all record types.
	 * 
	 * Version information:
	 * $RCSfile: Linker.java,v $
	 * $Revision: 1.50 $
	 * $Date: 2012/05/30 20:35:42 $
	 * </pre>
	 * 
	 * @author Zak
	 * 
	 */
	public enum RecordType {
		// U = Undefined
		H, L, T, M, E, U;
	}

	/**
	 * 
	 * <pre>
	 * Class Name: Intermediate
	 * Description: The information required for an intermediate record.
	 * 
	 * Version information: 
	 * $RCSfile: Linker.java,v $
	 * $Revision: 1.50 $
	 * $Date: 2012/05/30 20:35:42 $
	 * </pre>
	 * 
	 * @author Zak
	 * 
	 */
	public static class Intermediate {
		// [record String local the record as passed in through input]
		public String record;

		// [lineIndex int local the position of the record in the list of input
		// lines]
		public int lineIndex;

		public Intermediate(String record, int lineIndex) {
			this.record = record;
			this.lineIndex = lineIndex;
		}
	}

	/**
	 * 
	 * <pre>
	 * Class Name: SymbolInfo
	 * Description: Stores the information for a loader symbol. 
	 * 
	 * Version information:
	 * $RCSfile: Linker.java,v $
	 * $Revision: 1.50 $
	 * $Date: 2012/05/30 20:35:42 $
	 * </pre>
	 * 
	 * @author Zak
	 * 
	 */
	public static class SymbolInfo {
		// [assemblerAddr int local assembler assigned address]
		// [loaderAddr int local loader assigned address]
		// [length int local length of the symbol]
		// [adjustment int local adjustment amount]
		public int assemblerAddr, loaderAddr, length, adjustment;

		// [type char local "H" if header record; "L" if linking record]
		public char type;

		public SymbolInfo(int assemblerAddr, int loaderAddr, int length,
				int adjustment, char type) {
			this.assemblerAddr = assemblerAddr;
			this.loaderAddr = loaderAddr;
			this.length = length;
			this.adjustment = adjustment;
			this.type = type;
		}

		@Override
		public String toString() {
			return "[" + padZeros(this.assemblerAddr, 4) + ","
					+ padZeros(this.loaderAddr, 4) + ","
					+ padZeros(this.length, 4) + ","
					+ Integer.toHexString(this.adjustment) + "]";
		}
	}

	/**
	 * 
	 * <pre>
	 * Class Name: TextRecordInfo
	 * Description: Stores the information for a text record.
	 * 
	 * Version information:
	 * $RCSfile: Linker.java,v $
	 * $Revision: 1.50 $
	 * $Date: 2012/05/30 20:35:42 $
	 * </pre>
	 * 
	 * @author Zak
	 * 
	 */
	public static class TextRecordInfo {
		// [progName String local program in which the text record is found]
		public String progName;
		// [instHex int local integer representation of the hex instruction]
		// [remainingModCount int local number of remaining modifications
		// specified]
		// [relocation int local memory relocation amount]
		// [lineIndex int local the position of the record in the list of input
		// lines]
		public int instHex, remainingModCount, relocation, lineIndex;

		public TextRecordInfo(String progName, int lineIndex, int instHex,
				int modCount, int relocation) {
			this.progName = progName;
			this.instHex = instHex;
			this.remainingModCount = modCount;
			this.relocation = relocation;
			this.lineIndex = lineIndex;
		}

		@Override
		public String toString() {
			return "[" + padZeros(this.instHex, 8) + "," + this.progName + ","
					+ padZeros(this.remainingModCount, 4) + "]";
		}
	}

	/**
	 * 
	 * <pre>
	 * Class Name: RecordErrors
	 * Description: Stores the record line and any corresponding errors.
	 * 
	 * Version information:
	 * $RCSfile: Linker.java,v $
	 * $Revision: 1.50 $
	 * $Date: 2012/05/30 20:35:42 $
	 * </pre>
	 * 
	 * @author Zak
	 * 
	 */
	private static class RecordErrors {
		/**
		 * @param record2
		 * @param arrayList
		 */
		public RecordErrors(String record, Set<ErrorDesc> errors) {
			this.record = record;
			this.errors = errors;
		}

		private String record;
		private Set<ErrorDesc> errors;
	}

	// [TOO_MANY_FIELDS ErrorDesc constant Marks an erroneous line with more
	// fields than expected]
	private static final ErrorDesc TOO_MANY_FIELDS = new ErrorDesc(100);
	// [INCONSISTENT_NAME ErrorDesc constant Marks an header record with two
	// nonidentical program names]
	private static final ErrorDesc INCONSISTENT_NAME = new ErrorDesc(101);
	// [MODS_RANGE_WARNING ErrorDesc constant Marks a text record with more than
	// four modifications (non-standard)]
	private static final ErrorDesc MODS_RANGE_WARNING = new ErrorDesc(107);
	// [MISSING_END_RECORD ErrorDesc constant Marks an unclosed module]
	private static final ErrorDesc MISSING_END_RECORD = new ErrorDesc(108);
	// [TOO_MANY_MODS ErrorDesc constant Marks a modification record with more
	// modifications than indicated in the fourth field]
	private static final ErrorDesc TOO_MANY_MODS = new ErrorDesc(109);

	// [INVALID_LABEL ErrorDesc constant Marks a line with an unsyntactical
	// label ]
	private static final ErrorDesc INVALID_LABEL = new ErrorDesc(201);
	// [INVALID_ADDRESS_FORMAT ErrorDesc constant Marks a line where some monkey
	// forgot that a hex string means only hex digits are allowed]
	private static final ErrorDesc INVALID_ADDRESS_FORMAT = new ErrorDesc(202);
	// [INVALID_ADDRESS_RANGE ErrorDesc constant Marks an out of range address]
	private static final ErrorDesc INVALID_ADDRESS_RANGE = new ErrorDesc(203);
	// [WRONG_MODULE_NAME ErrorDesc constant Marks a line with a different
	// program name than the current module]
	private static final ErrorDesc WRONG_MODULE_NAME = new ErrorDesc(206);
	// [TEXT_FORMAT ErrorDesc constant Marks a text record with illegal digits
	// in the 8-digit hex word]
	private static final ErrorDesc TEXT_FORMAT = new ErrorDesc(208);
	// [TEXT_RANGE ErrorDesc constant Marks a text record with an excessively
	// long code word]
	private static final ErrorDesc TEXT_RANGE = new ErrorDesc(209);
	// [INVALID_FLAG ErrorDesc constant Marks an illegal relocation flag]
	private static final ErrorDesc INVALID_FLAG = new ErrorDesc(210);
	// [MODS_RANGE_ERROR ErrorDesc constant Marks a nonsensical (ie negative)
	// number of relocations indicated on a text record]
	private static final ErrorDesc MODS_RANGE_ERROR = new ErrorDesc(211);
	// [MODS_FORMAT ErrorDesc constant Marks a text record with illegal
	// characters attempting to describe the number of modifications]
	private static final ErrorDesc MODS_FORMAT = new ErrorDesc(212);
	// [EXECUTION_ADDRESS_RANGE ErrorDesc constant Execution start address is
	// out of range]
	private static final ErrorDesc EXECUTION_ADDRESS_RANGE = new ErrorDesc(213);
	// [ADDRESS_IN_USE ErrorDesc constant Someone tried to cram two pigeons into
	// one pigeonhole]
	private static final ErrorDesc ADDRESS_IN_USE = new ErrorDesc(214);
	// [END_WITHOUT_HEAD ErrorDesc constant Someone put the cart before the
	// horse (ie an END record outside of a module)]
	private static final ErrorDesc END_WITHOUT_HEAD = new ErrorDesc(215);
	// [INVALID_RECORD_TYPE ErrorDesc constant Unrecognized record type]
	private static final ErrorDesc INVALID_RECORD_TYPE = new ErrorDesc(216);
	// [NOT_ENOUGH_FIELDS ErrorDesc constant There aren't enough fields in a
	// record]
	private static final ErrorDesc NOT_ENOUGH_FIELDS = new ErrorDesc(223);
	// [OUT_OF_MEMORY_RANGE ErrorDesc constant After relocation, a module falls
	// outside of memory]
	private static final ErrorDesc OUT_OF_MEMORY_RANGE = new ErrorDesc(224);
	// [HEADER_LENGTH_FORMAT ErrorDesc constant The header length field is not
	// valid hex]
	private static final ErrorDesc HEADER_LENGTH_FORMAT = new ErrorDesc(225);
	// [HEADER_MODULE_RANGE ErrorDesc constant The header indicates that the
	// module is too long]
	private static final ErrorDesc HEADER_MODULE_RANGE = new ErrorDesc(226);
	// [EXECUTION_ADDRESS_FORMAT ErrorDesc constant The execution start address
	// is illegible]
	private static final ErrorDesc EXECUTION_ADDRESS_FORMAT = new ErrorDesc(227);
	// [MOD_BAD_SIGN ErrorDesc constant A modification isn't satisfied with
	// being either positive or negative]
	private static final ErrorDesc MOD_BAD_SIGN = new ErrorDesc(228);
	// [MOD_SYMBOL_FORMAT ErrorDesc constant A modification refers to a symbol
	// with an illegal name]
	private static final ErrorDesc MOD_SYMBOL_FORMAT = new ErrorDesc(229);
	// [MOD_SYMBOL_UNDEF ErrorDesc constant A modification refers to an
	// undefined symbol]
	private static final ErrorDesc MOD_SYMBOL_UNDEF = new ErrorDesc(230);
	// [MOD_COUNT_RANGE ErrorDesc constant Too many modifications on one line]
	private static final ErrorDesc MOD_COUNT_RANGE = new ErrorDesc(231);
	// [MOD_COUNT_FORMAT ErrorDesc constant Modification record count is not
	// valid hex]
	private static final ErrorDesc MOD_COUNT_FORMAT = new ErrorDesc(232);
	// [ADDRESS_FORMAT ErrorDesc constant Text or modification address is not
	// valid hex]
	private static final ErrorDesc ADDRESS_FORMAT = new ErrorDesc(233);
	// [ADDRESS_RANGE ErrorDesc constant Address of record is out of range]
	private static final ErrorDesc ADDRESS_RANGE = new ErrorDesc(234);
	// [TEXT_RECORD_FIELD_COUNT ErrorDesc constant Text record has the wrong
	// number of fields]
	private static final ErrorDesc TEXT_RECORD_FIELD_COUNT = new ErrorDesc(235);
	// [MOD_RECORD_FIELD_COUNT ErrorDesc constant Modification record has the
	// wrong number of fields]
	private static final ErrorDesc MOD_RECORD_FIELD_COUNT = new ErrorDesc(236);
	// [ADDRESS_OUT_OF_MODULE ErrorDesc constant A record address is outside of
	// the current module]
	private static final ErrorDesc ADDRESS_OUT_OF_MODULE = new ErrorDesc(237);
	// [RELOCATION_OUT_OF_BOUNDS ErrorDesc constant After relocation, an
	// instruction is trying to refer to a nonexistent word]
	private static final ErrorDesc RELOCATION_OUT_OF_BOUNDS = new ErrorDesc(238);
	// [NO_TEXT_RECORD ErrorDesc local Record attempts to modify nonexistent
	// record]
	private static final ErrorDesc NO_TEXT_RECORD = new ErrorDesc(239);

	// [NO_HEADER ErrorDesc local Object file has no header record]
	private static final ErrorDesc NO_HEADER = new ErrorDesc(300);

	// [containsHeader boolean local if the object file has a header record]
	private boolean containsHeader = false;

	// [nextAddr int local next available loader address for next module]
	private int nextAddr;

	// [loadAddr int local combined module load address]
	private int loadAddr;

	// [execStartAddr int local execution start address of first module]
	private int execStartAddr;

	// [execStartEvaluated boolean local whether execution start has been
	// extracted from first module]
	private boolean execStartEvaluated;

	// [currentModule String local name of the currently started module]
	private String currentModule;

	// [mainProgName String local name of the first program encountered]
	private String mainProgName;

	// [IntermediateRecords Queue<Intermediate> local all header, text, and
	// modification records needed for pass 2]
	// Should contain any header, text and modification records
	private Queue<Intermediate> IntermediateRecords;

	// [loadRecords SortedMap<Integer, TextRecordInfo> local text records that
	// are modified by object file, and output after running the second pass]
	private SortedMap<Integer, TextRecordInfo> loadRecords;

	// [symTable SortedMap<String, SymbolInfo> local assembler address, loader
	// address, adjustment, and length for each symbol]
	private SortedMap<String, SymbolInfo> symTable;

	// [occupiedAddresses BitSet local addresses occupied by loader]
	private BitSet occupiedAddresses;

	// [records List<RecordErrors> local list of each line (assumed to be a
	// record) as passed in by input and any errors on that line]
	private List<RecordErrors> records;

	public Linker() {
		this.currentModule = "";
		this.mainProgName = "";
		this.loadAddr = 0;
		this.execStartAddr = 0;
		this.execStartEvaluated = false;
		this.IntermediateRecords = new LinkedList<Linker.Intermediate>();
		this.symTable = new TreeMap<String, Linker.SymbolInfo>();
		this.loadRecords = new TreeMap<Integer, Linker.TextRecordInfo>();
		this.occupiedAddresses = new BitSet(4096);
		this.records = new ArrayList<Linker.RecordErrors>();
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: padZeros
	 * Description: Converts the hex integer to a hex string and returns it padded 
	 * with zeros until it reaches the specific length.
	 * 
	 * Specification reference code(s): N/A
	 * 
	 * Error Conditions Tested: hex larger than length, hex 0
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why): 
	 * Zak 5/22/2012 Corrected to use StringBuffer
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 30, 2012
	 * @author Zak
	 * @param hex
	 *            the integer to be parsed into hex
	 * @param length
	 *            the length of the output string
	 * @return the hex string with zeros padded on it
	 */
	private static String padZeros(int hex, int length) {
		StringBuffer result = new StringBuffer(Integer.toHexString(hex)
				.toUpperCase());
		while (result.length() < length) {
			result = result.insert(0, '0');
		}

		return result.toString();

	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: trimZeros
	 * Description: trims any extra zeros to make the string a certain length. 
	 * 
	 * Specification reference code(s): N/A
	 * 
	 * Error Conditions Tested: length is 0, length is less than string length
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 30, 2012
	 * @author Zak
	 * @param str
	 *            the string that must be trimmed
	 * @param length
	 *            the length the string must be trimmed to
	 * @return the hex string with zeros trimmed off it
	 */
	private String trimZeros(String str, int length) {
		String result = str;
		if (result.length() > length) {
			if (result.charAt(0) == '0') {
				result = trimZeros(result.substring(1), length);
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
		// [result boolean local result of function]
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
		// [result boolean local result of function]
		boolean result = true;
		if (label != null && label.length() > 0) {
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
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: isValidHex
	 * Description: Checks that a string is valid hex.
	 * 
	 * Specification reference code(s):
	 * 
	 * Error Conditions Tested: Empty string, Large string, mix of hex and not hex
	 * Error messages generated: Record is invalid hex
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 30, 2012
	 * @author Zak
	 * @param hex
	 *            The string that is checked for valid hex
	 * @return whether the string is valid hex
	 */
	private static boolean isValidHex(String hex) {
		return hex.matches("[0-9A-F]{0,}");
	}

	/**
	 * <pre>
	 * Procedure Name: addToSymbolTable
	 * Description: Add a module or symbol to the symbol table.
	 * 
	 * Specification reference code(s): OB1, OB2
	 * Calling Sequence: (pass 1, pass 2, or both): pass 1
	 * 
	 * Error Conditions Tested: invalid record, symbol exists, symbol out of range
	 * Error messages generated: invalid record, symbol already in table, 
	 * symbol out of range
	 * 
	 * Modification Log (who when and why):
	 * Zak 5/23/2012 added line index
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 15, 2012
	 * @author Zak
	 * @param record
	 *            the header or linking record containing the symbol
	 * @param lineIndex
	 *            the position of the record in the list of records
	 * @return whether the symbol was added to the table.
	 */
	private boolean addToSymbolTable(String record, int lineIndex) {
		// / [symbolAdded Boolean local whether the symbol has been added]
		boolean symbolAdded = false;
		// [symName String local name of the symbol added]
		String symName = "";
		// Note: Does nothing if record is not H || L
		// [recordFields String[] local the fields obtained by separating at
		// each ":"
		String[] recordFields = record.split(":");
		// Decision: declare all values within if statement because symbol is
		// not always added; change decision after implementation of reasonable

		String recType = recordFields[0];
		if (recType.equals("H")) {
			if (recordFields.length >= 9) {
				if (recordFields.length > 9) {
					// Too many record fields
					this.records.get(lineIndex).errors.add(TOO_MANY_FIELDS);
				}

				// First check that every field is valid
				if (isReasonableRecord(recordFields, lineIndex)) {

					symName = recordFields[1];
					int assemAddr = Integer.parseInt(recordFields[2], 16);
					int adjustment = (assemAddr < this.nextAddr) ? (this.nextAddr - assemAddr)
							: 0;
					int loaderAddr = assemAddr + adjustment;
					int length = Integer.parseInt(recordFields[3], 16);
					if (!this.execStartEvaluated) {
						this.execStartAddr = Integer.parseInt(recordFields[4],
								16);
						this.loadAddr = loaderAddr;
						this.execStartEvaluated = true;
					}

					// Adjust addr for next module
					if (loaderAddr + length <= 4096) {
						this.nextAddr = loaderAddr + length;
						this.currentModule = symName;
						this.symTable.put(symName, new SymbolInfo(assemAddr,
								loaderAddr, length, adjustment, 'H'));
						this.IntermediateRecords.add(new Intermediate(record,
								lineIndex));
						symbolAdded = true;
					} else {
						// program out of memory range
						this.records.get(lineIndex).errors
								.add(OUT_OF_MEMORY_RANGE);
					}

				}
			} else {
				this.records.get(lineIndex).errors.add(NOT_ENOUGH_FIELDS);
			}

		} else if (recType.equals("L")) {
			if (recordFields.length >= 4) {
				if (recordFields.length > 4) {

					this.records.get(lineIndex).errors.add(TOO_MANY_FIELDS);
				}
				if (isReasonableRecord(recordFields, lineIndex)) {
					symName = recordFields[1];
					int assemAddr = Integer.parseInt(recordFields[2], 16);
					String progName = recordFields[3];
					int adjustment = getAdjustment(progName);

					int loaderAddr = assemAddr + adjustment;

					this.symTable.put(symName, new SymbolInfo(assemAddr,
							loaderAddr, 1, adjustment, 'L'));
					symbolAdded = true;
				}
			} else {
				this.records.get(lineIndex).errors.add(NOT_ENOUGH_FIELDS);
			}

		} else {
			System.out.println("\n !!!!!! \n Zak?!");
		}

		return symbolAdded;
	}

	/**
	 * <pre>
	 * Procedure Name: isReasonableRecord
	 * Description: Test if header or linker record is valid.
	 * 
	 * Specification reference code(s): OB1,OB2
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: each of the record fields is invalid
	 * Error messages generated: 201-207, 213-215
	 * 
	 * Modification Log (who when and why):
	 * Zak 5/23/2012 Added line index
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author Zak
	 * @param recordFields
	 *            the fields of the record
	 * @param lineIndex
	 *            the position of the record within list of records
	 * @return if the record can be parsed
	 */
	private boolean isReasonableRecord(String[] recordFields, int lineIndex) {
		boolean result = true;
		// [recType String local the type of the record being parsed]
		String recType = recordFields[0];
		// [label String local label given to the symbol]
		String label = recordFields[1];
		if (!isValidLabel(label)) {
			this.records.get(lineIndex).errors.add(INVALID_LABEL);
			result = false;
		}

		recordFields[2] = trimZeros(recordFields[2], 4);

		if (recType.equals("H")) {
			int maxHex = Integer.parseInt("0FFF", 16);
			String modLength = recordFields[3];
			String exAdr = recordFields[4];

			if (!isValidHex(recordFields[2]) || recordFields[2].length() > 4) {
				// Load address had invalid hex
				this.records.get(lineIndex).errors.add(INVALID_ADDRESS_FORMAT);
				result = false;
			} else {
				int addr = Integer.parseInt(recordFields[2], 16);
				if (addr > 4095 || addr < 0) {
					this.records.get(lineIndex).errors.add(ADDRESS_RANGE);
					result = false;
				}
			}

			if (!isValidHex(modLength)) {
				this.records.get(lineIndex).errors.add(HEADER_LENGTH_FORMAT);
				result = false;
			} else {
				try {
					if (Integer.parseInt(modLength, 16) < 0
							|| Integer.parseInt(modLength, 16) > maxHex) {
						this.records.get(lineIndex).errors
								.add(HEADER_MODULE_RANGE);
						result = false;
					}
				} catch (Exception e) {
					// This only happens if the hex is so large that parseInt
					// cannot parse it
					this.records.get(lineIndex).errors.add(HEADER_MODULE_RANGE);
					result = false;
				}
			}

			if (!isValidHex(exAdr)) {
				this.records.get(lineIndex).errors
						.add(EXECUTION_ADDRESS_FORMAT);
				result = false;
			} else {
				try {
					if (Integer.parseInt(exAdr, 16) < 0
							|| Integer.parseInt(exAdr, 16) > maxHex) {
						this.records.get(lineIndex).errors
								.add(EXECUTION_ADDRESS_RANGE);
						result = false;
					}
				} catch (Exception e) {
					this.records.get(lineIndex).errors
							.add(EXECUTION_ADDRESS_RANGE);
					result = false;
				}
			}

			String lastProgName = recordFields[8];
			if (!lastProgName.equals(label)) {
				this.records.get(lineIndex).errors.add(INCONSISTENT_NAME);

			}
		} else if (recType.equals("L")) {

			if (!recordFields[3].equals(this.currentModule)) {
				// linking record in incorrect module
				this.records.get(lineIndex).errors.add(WRONG_MODULE_NAME);
				result = false;
			}

			if (!isValidHex(recordFields[2]) || recordFields[2].length() > 4) {
				this.records.get(lineIndex).errors.add(INVALID_ADDRESS_FORMAT);
				result = false;
			} else {
				int addr = Integer.parseInt(recordFields[2], 16);
				if (addr >= (this.symTable.get(this.currentModule).assemblerAddr + this.symTable
						.get(this.currentModule).length) || addr < 0) {
					// Address out of range
					this.records.get(lineIndex).errors
							.add(INVALID_ADDRESS_RANGE);
					result = false;
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: getAdjustment
	 * Description: gets the adjustment for a specific module.
	 * 
	 * Specification reference code(s): OB1, OB2, OB3, OB4, OB5
	 * 
	 * Error Conditions Tested: program name not defined or has negative adjustment
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Andrew 5/28/2012 Corrected call for program name that is not defined
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 30, 2012
	 * @author Zak
	 * @param progName
	 *            The program name whose adjustment is returned.
	 * @return the adjustment corresponding to the specific program name
	 */
	public final int getAdjustment(String progName) {
		if (this.symTable.containsKey(progName)) {
			return this.symTable.get(progName).adjustment;
		} else {
			return 0;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: runFirstPass
	 * Description: run the first pass: Read the input and produce intermediate &
	 * symbol table.
	 * 
	 * Specification reference code(s): OB1, OB2, OB5
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author Zak
	 * @param in
	 *            file to read from
	 */
	public final void runFirstPass(final Reader in) {

		// [moduleInitiated boolean local if a module has been initiated (by
		// having a valid header record)]
		boolean moduleInitiated = false;
		// [headerIndex int local the position of the last header record in the
		// list of input records]
		int headerIndex = 0;
		// [recordCount int local the total number of records in the current
		// module]
		int recordCount = 0;
		// [textCount int local the number of text records in the current
		// module]
		int textCount = 0;
		// [modCount int local the number of modification records in the current
		// module]
		int modCount = 0;
		// [linkCount int local the number of linker records in the current
		// module]
		int linkCount = 0;

		Scanner input = new Scanner(in);
		while (input.hasNextLine()) {
			String record = input.nextLine().trim();
			int lineIndex = this.records.size();
			this.records
					.add(new RecordErrors(record, new HashSet<ErrorDesc>()));

			if (record.length() > 0) {
				RecordType recType = RecordType.U;
				try {
					recType = RecordType.valueOf(record.substring(0, 1));
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// Catch Error later
				} catch (StringIndexOutOfBoundsException e) {
					// Error: empty string
					// TODO: Delete if we agree on if (empty)
				}

				// If H || L, add to symbol table

				recordCount++;
				switch (recType) {
				case H:
					if (moduleInitiated) {
						this.records.get(lineIndex).errors.add(new ErrorDesc(
								222));
					} else {
						moduleInitiated = addToSymbolTable(record, lineIndex);
						headerIndex = moduleInitiated ? lineIndex : headerIndex;
						this.mainProgName = this.mainProgName.isEmpty() ? this.currentModule
								: this.mainProgName;
						if (moduleInitiated) {
							this.containsHeader = true;
						}

					}
					break;
				case L:
					if (moduleInitiated) {
						linkCount++;
						addToSymbolTable(record, lineIndex);
					} else {
						this.records.get(lineIndex).errors.add(new ErrorDesc(
								240));
					}
					break;
				case T:
					if (moduleInitiated) {
						textCount++;
						this.IntermediateRecords.add(new Intermediate(record,
								lineIndex));
					} else {
						this.records.get(lineIndex).errors.add(new ErrorDesc(
								240));
					}
					break;
				case M:
					modCount++;
					this.IntermediateRecords.add(new Intermediate(record,
							lineIndex));
					break;
				case E:
					if (!moduleInitiated) {
						this.records.get(lineIndex).errors
								.add(END_WITHOUT_HEAD);
					} else {
						// Only report errors; reset the module whether there
						// are errors or not
						isValidEndRecord(record, recordCount, linkCount,
								textCount, modCount, lineIndex);
						this.currentModule = "";
						moduleInitiated = false;
						recordCount = 0;
						linkCount = 0;
						modCount = 0;
						textCount = 0;
					}
					break;
				default:
					recordCount--;
					this.records.get(lineIndex).errors.add(INVALID_RECORD_TYPE);
				}
			} else {
				// Error: empty line; ignore
			}
		}

		if (moduleInitiated) {
			this.records.get(headerIndex).errors.add(MISSING_END_RECORD);
		}

	}

	/**
	 * <pre>
	 * Procedure Name: isValidEndRecord
	 * Description: checks that an end record is valid.
	 * 
	 * Specification reference code(s): OB5
	 * Calling Sequence: (pass 1, pass 2, or both): pass 1
	 * 
	 * Error Conditions Tested: empty record, lineIndex out of range
	 * Error messages generated: 103-106, 217-220 
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author Zak
	 * @param record
	 *            The string containing the record
	 * @param recordCount
	 *            the total number of records
	 * @param linkCount
	 *            the number of linking records
	 * @param textCount
	 *            the number of text records
	 * @param modCount
	 *            the number of modification records
	 * @param lineIndex
	 *            the position within the number of records
	 * 
	 * @return whether the end record is valid
	 */
	private boolean isValidEndRecord(String record, int recordCount,
			int linkCount, int textCount, int modCount, int lineIndex) {
		Boolean result = true;

		// Clean up this ugly code you fool
		String[] recordFields = record.split(":");

		if (recordFields.length < 6) {
			this.records.get(lineIndex).errors.add(NOT_ENOUGH_FIELDS);
			return false;
		} else if (recordFields.length > 6) {

			this.records.get(lineIndex).errors.add(new ErrorDesc(100));
		}
		if (!recordFields[5].equals(this.currentModule)) {
			result = false;
		}
		try {
			if (Integer.parseInt(recordFields[1], 16) != recordCount) {
				this.records.get(lineIndex).errors.add(new ErrorDesc(103));
			}
		} catch (Exception e) {
			this.records.get(lineIndex).errors.add(new ErrorDesc(217));
			result = false;
		}
		try {
			if (Integer.parseInt(recordFields[2], 16) != linkCount) {
				this.records.get(lineIndex).errors.add(new ErrorDesc(104));
			}
		} catch (Exception e) {
			this.records.get(lineIndex).errors.add(new ErrorDesc(218));
			result = false;
		}

		// Text records
		try {
			if (Integer.parseInt(recordFields[3], 16) != textCount) {
				this.records.get(lineIndex).errors.add(new ErrorDesc(105));
			}
		} catch (Exception e) {
			this.records.get(lineIndex).errors.add(new ErrorDesc(219));
			result = false;
		}

		// Modification records
		try {
			if (Integer.parseInt(recordFields[4], 16) != modCount) {
				this.records.get(lineIndex).errors.add(new ErrorDesc(106));

			}
		} catch (Exception e) {
			this.records.get(lineIndex).errors.add(new ErrorDesc(220));
			result = false;
		}
		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: runSecondPass
	 * Description: Convert intermediate representation to final output, now that the actual relocation of all modules is known
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: Address in range, records valid
	 * Error messages generated: Address out of range, invalid records
	 * 
	 * Modification Log (who when and why): Andrew, date unknown, implementation
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since Unknown
	 * @author Zak
	 */
	public void runSecondPass() {
		// [currModuleInfo SymbolInfo local contains the information for the
		// module that the modification or text record is currently within]
		SymbolInfo currModuleInfo = null;
		while (this.IntermediateRecords.size() > 0) {
			Intermediate currentRecord = this.IntermediateRecords.remove();
			String record = currentRecord.record;

			String[] recordFields = record.split(":");
			if (record.startsWith("H")) {
				this.currentModule = recordFields[1];
				currModuleInfo = this.symTable.get(this.currentModule);
			} else if (record.startsWith("T")) {
				if (isValidTextRecord(recordFields, currentRecord.lineIndex)) {
					int lc = Integer.parseInt(recordFields[1], 16);
					if (lc < currModuleInfo.assemblerAddr
							|| lc >= currModuleInfo.assemblerAddr
									+ currModuleInfo.length) {
						this.records.get(currentRecord.lineIndex).errors
								.add(ADDRESS_OUT_OF_MODULE);
					} else {
						int instHex = (int) Long.parseLong(recordFields[2], 16);
						String relocType = recordFields[3];
						int modCount = Integer.parseInt(recordFields[4], 16);
						String progName = recordFields[5];
						lc += getAdjustment(progName);
						int relocation = 0;

						if (this.occupiedAddresses.get(lc)) {
							this.records.get(currentRecord.lineIndex).errors
									.add(ADDRESS_IN_USE);
						} else {
							this.occupiedAddresses.set(lc);

							if (relocType.equals("A")) {
								modCount = 0;
							} else if (relocType.equals("R")) {
								relocation = getAdjustment(progName);
								modCount = 0;
							} else if (relocType.equals("E")
									|| relocType.equals("C")) {
								// Relocate at a later time, upon reading
								// the M
								// record
								modCount = Integer
										.parseInt(recordFields[4], 16);
								if (relocType.equals("E") && modCount != 1) {
									this.records.get(currentRecord.lineIndex).errors
											.add(MODS_RANGE_ERROR);
								}
							} else {
								this.records.get(currentRecord.lineIndex).errors
										.add(new ErrorDesc(221));
							}

							this.loadRecords.put(lc, new TextRecordInfo(
									progName, currentRecord.lineIndex, instHex,
									modCount, relocation));
						}
					}
				} else {
					// Not a valid text record. Replace with NOP if we have a
					// good address
					try {
						// [addr int local Holds the address, if it can be
						// parsed]
						int addr = Integer
								.parseInt(recordFields.length > 1 ? recordFields[1]
										: "");
						if (addr >= 0 && addr < 4096) {
							this.loadRecords.put(addr
									+ getAdjustment(this.currentModule),
									new TextRecordInfo(this.currentModule,
											currentRecord.lineIndex,
											0xF8000000, 0, 0));
							this.occupiedAddresses.set(addr);
						}
					} catch (NumberFormatException e) {
						// pass
					}
				}
			} else if (record.startsWith("M")) {
				if (isValidModRecord(recordFields, currentRecord.lineIndex)) {
					int lc = Integer.parseInt(recordFields[1], 16)
							+ getAdjustment(recordFields[recordFields.length - 1]);
					relocateInstruction(this.loadRecords.get(lc), record,
							currentRecord.lineIndex);
				}
			}
		}
		Iterator<Entry<Integer, TextRecordInfo>> iter = this.loadRecords
				.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, TextRecordInfo> entry = iter.next();
			if (!applyRelocation(entry.getValue())) {
				this.records.get(entry.getValue().lineIndex).errors
						.add(RELOCATION_OUT_OF_BOUNDS);
				iter.remove();
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: isValidTextRecord
	 * Description:
	 * 
	 * Specification reference code(s): OB3
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why): Andrew, 23 May 2012, Implementation
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Zak
	 * @param recordFields
	 *            fields of the record, in order
	 * @param lineIndex
	 *            index of the current record in the records[] list
	 * @return true iff the record appears valid
	 */
	private boolean isValidTextRecord(String[] recordFields, int lineIndex) {
		// [result boolean local true iff the text record is valid]
		boolean result = true;
		if (recordFields.length != 6) {
			this.records.get(lineIndex).errors.add(TEXT_RECORD_FIELD_COUNT);
			result = false;
			recordFields = Arrays.copyOf(recordFields, 6);
		}

		// Assume first field is a 'T'

		// Test address field
		try {
			// [int addr Test the address to ensure they it's in range]
			int addr = Integer.parseInt(recordFields[1], 16);
			if (addr < 0 || addr >= 4096) {
				this.records.get(lineIndex).errors.add(ADDRESS_RANGE);
				result = false;
			}
		} catch (NumberFormatException e) {
			this.records.get(lineIndex).errors.add(ADDRESS_FORMAT);
			result = false;
		}

		// Test text field
		try {
			// [int text Test the code word to make sure it's in range]
			long text = Long.parseLong(recordFields[2], 16);
			if (text < 0 || text > 0xFFFFFFFFl) {
				this.records.get(lineIndex).errors.add(TEXT_RANGE);
				result = false;
			}
		} catch (NumberFormatException e) {
			this.records.get(lineIndex).errors.add(TEXT_FORMAT);
			result = false;
		}

		// Test the flag
		if (recordFields[3] == null
				|| !Pattern.matches("^[AREC]$", recordFields[3])) {
			this.records.get(lineIndex).errors.add(INVALID_FLAG);
			result = false;
		}

		// Test the number of adjustments
		try {
			// [int mods Test the number of modifications. Warn if more than 4]
			int mods = Integer.parseInt(recordFields[4], 16);
			if (mods < 0) {
				this.records.get(lineIndex).errors.add(MODS_RANGE_ERROR);
				result = false;
			} else if (mods > 4) {
				this.records.get(lineIndex).errors.add(MODS_RANGE_WARNING);
			}
		} catch (NumberFormatException e) {
			this.records.get(lineIndex).errors.add(MODS_FORMAT);
			result = false;
		}

		// Test the program name
		if (!this.currentModule.equals(recordFields[5])) {
			this.records.get(lineIndex).errors.add(WRONG_MODULE_NAME);
			result = false;
		}

		if (this.currentModule.equals("")) {
			this.records.get(lineIndex).errors.add(new ErrorDesc(240));
			result = false;
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: isValidModRecord
	 * Description: Determine if a modification record is correct
	 * 
	 * Specification reference code(s): OB4
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why): Andrew, 23 May 2012, Implementation
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 20, 2012
	 * @author Zak
	 * @param recordFields
	 * @return
	 */
	private boolean isValidModRecord(String[] recordFields, int lineIndex) {
		// [result boolean local true iff the M record is valid]
		boolean result = true;
		if (recordFields.length < 6 || recordFields.length > 12
				|| recordFields.length % 2 != 0) {
			this.records.get(lineIndex).errors.add(MOD_RECORD_FIELD_COUNT);
			return false;
		}

		// Make sure address in valid
		try {
			int addr = Integer.parseInt(recordFields[1], 16);
			if (addr < 0 || addr >= 4096) {
				this.records.get(lineIndex).errors.add(ADDRESS_RANGE);
				result = false;
			} else if (!this.loadRecords.containsKey(addr)) {
				this.records.get(lineIndex).errors.add(NO_TEXT_RECORD);
				result = false;
			}
		} catch (NumberFormatException e) {
			this.records.get(lineIndex).errors.add(ADDRESS_FORMAT);
			result = false;
		}

		// Check length against reported number of mods
		try {
			int num = Integer.parseInt(recordFields[2], 16);
			if (num < 1 || num > 4) {
				this.records.get(lineIndex).errors.add(MOD_COUNT_RANGE);
				result = false;
			}
			if (recordFields.length != 4 + 2 * num) {
				this.records.get(lineIndex).errors.add(MOD_RECORD_FIELD_COUNT);
				result = false;
			}
		} catch (NumberFormatException e) {
			this.records.get(lineIndex).errors.add(MOD_COUNT_FORMAT);
			result = false;
		}

		// Make sure program name is correct
		if (!this.currentModule.equals(recordFields[recordFields.length - 1])) {
			this.records.get(lineIndex).errors.add(WRONG_MODULE_NAME);
		}

		if (!this.symTable.containsKey(recordFields[recordFields.length - 1])) {
			this.records.get(lineIndex).errors.add(WRONG_MODULE_NAME);
		}

		// [i int local loop counter]
		int i;
		for (i = recordFields.length - 3; i >= 3; i -= 2) {
			if (!"+".equals(recordFields[i]) && !"-".equals(recordFields[i])) {
				this.records.get(lineIndex).errors.add(MOD_BAD_SIGN);
				result = false;
			}
			if (!isValidLabel(recordFields[i + 1])) {
				this.records.get(lineIndex).errors.add(MOD_SYMBOL_FORMAT);
				result = false;
			}
			if (!this.symTable.containsKey(recordFields[i + 1])) {
				this.records.get(lineIndex).errors.add(MOD_SYMBOL_UNDEF);
				result = false;
			}
		}

		return result;
	}

	/**
	 * <pre>
	 * Procedure Name: applyRelocation
	 * Description: actually relocate a E or C text record
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: relocation out of bounds
	 * Error messages generated: same
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 2012
	 * @author Andrew
	 * @param record
	 *            the text record to relocate
	 * @return true iff the relocation is in range
	 */
	private boolean applyRelocation(TextRecordInfo record) {
		// [mem int local the memory address of the instruction]
		int mem = record.instHex & 0xFFF;
		mem += record.relocation;
		if (mem < 0 || mem >= 4096) {
			return false;
		} else {
			record.instHex &= ~0xFFF;
			record.instHex |= mem;
			return true;
		}
	}

	/**
	 * <pre>
	 * Procedure Name: relocateInstruction
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
	 * @since May 18, 2012
	 * @author Zak
	 * @param instHex
	 * @param record
	 * @return
	 * @return
	 */
	private void relocateInstruction(TextRecordInfo record, String rawRecord,
			int lineIndex) {
		String[] recordFields = rawRecord.split(":");
		// [lastAdjustment int local position of last adjustment within
		// recordFields]
		int lastAdjustment = 2;

		int adjustmentCount = Integer.parseInt(recordFields[2], 16);
		for (int i = 0; i < adjustmentCount; i++) {
			if (record.remainingModCount > 0) {
				String operator = recordFields[lastAdjustment + 1];
				String symbol = recordFields[lastAdjustment + 2];
				int adjustment = this.symTable.get(symbol).loaderAddr;

				// [sign int local the sign of the modification to be added]
				int sign = 0;
				if (operator.equals("+")) {
					sign = 1;
				} else if (operator.equals("-")) {
					sign = -1;
				} else {
					// ERROR: Invalid operator
				}
				record.relocation += sign * adjustment;

				lastAdjustment += 2;
				record.remainingModCount--;
			} else {
				this.records.get(lineIndex).errors.add(TOO_MANY_MODS);
				// Replace with NOP
				this.loadRecords.get(Integer.parseInt(recordFields[1], 16)).instHex = 0xF8000000;
			}
		}

	}

	/**
	 * <pre>
	 * Procedure Name: outputLoadMap
	 * Description:
	 * 
	 * Specification reference code(s): 
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 26, 2012
	 * @author Andrew
	 * @param out
	 *            stream to output to
	 */
	public void outputLoadMap(OutputStream out) {
		// [fmt Formatter local used to output easily formatted information]
		Formatter fmt = new Formatter(out);

		fmt.format("Label          \tLocal Addr\tAdjustment\tReloc. Addr\tModule Len%n");
		// [entry Entry local iterator over set of symbols]
		for (Entry<String, SymbolInfo> entry : this.symTable.entrySet()) {
			SymbolInfo info = entry.getValue();
			if (info.type == 'H') {
				fmt.format("%-15s\t%10s\t%10s\t%11s\t%10s%n", entry.getKey(),
						info.assemblerAddr, info.adjustment, info.loaderAddr,
						info.length);
			} else {
				fmt.format("%-15s\t%10s\t%10s\t%11s\t%10s%n", entry.getKey(),
						info.assemblerAddr, info.adjustment, info.loaderAddr,
						"---");
			}
		}
		fmt.flush();
	}

	/**
	 * <pre>
	 * Procedure Name: outputLoadFile
	 * Description: Output the loading file
	 * 
	 * Specification reference code(s): LM1-3
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 10, 2012
	 * @author Zak
	 * @param out
	 *            stream to output to
	 */
	public void outputLoadFile(OutputStream out) {
		if (this.containsHeader) {
			// [output PrintStream local used for outputting information]
			PrintStream output = new PrintStream(out);
			// [dateFormat DateFormat local date format used within the header
			// record]
			DateFormat dateFormat = new SimpleDateFormat("yyyyD,HH,mm,ss");
			// [date Date local date and time of assembly]
			Date date = new Date();
			// [time String local formatted date and time of assembly]
			String time = dateFormat.format(date);

			// [version String local assembly version]
			String version = "0019";
			String combinedLength = padZeros(this.nextAddr - this.loadAddr, 4);
			output.println("H:" + this.mainProgName + ":"
					+ padZeros(this.loadAddr, 4) + ":"
					+ padZeros(this.execStartAddr, 4) + ":" + combinedLength
					+ ":" + time + ":" + "URBAN-LLM" + ":" + version + ":"
					+ this.mainProgName);

			for (int textLC : this.loadRecords.keySet()) {
				output.println("T:" + padZeros(textLC, 6) + ":"
						+ padZeros(this.loadRecords.get(textLC).instHex, 8)
						+ ":" + this.mainProgName);
			}

			// total # of records is just # of text records + 2 (header and end)
			output.println("E:" + padZeros(this.loadRecords.size() + 2, 4)
					+ ":" + padZeros(this.loadRecords.size(), 4) + ":"
					+ this.mainProgName);
		} else {
			// TODO: Maybe correct loader for having no header
		}
	}

	/**
	 * <pre>
	 * Procedure Name: outputRecords
	 * Description: print the loading records
	 * 
	 * Specification reference code(s): LM2
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 10, 2012
	 * @author Zak
	 * @param out
	 *            stream to output to
	 */
	public void outputRecords(OutputStream out) {
		PrintStream output = new PrintStream(out);
		for (RecordErrors record : this.records) {
			output.println(record.record);
			for (ErrorDesc error : record.errors) {
				output.println("  " + error.toString());
			}
			if (record.errors.size() > 0) {
				output.println();
			}
		}

		if (!this.containsHeader) {
			output.println("  " + NO_HEADER.toString());
		}
	}

	/**
	 * <pre>
	 * Procedure Name: main
	 * Description: Main entry point.  Instantiate and run the linker.
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both): pass 2
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Andrew
	 * Testing standards met: Signed by Andrew
	 * </pre>
	 * 
	 * @since May 10, 2012
	 * @author Zak
	 * @param args
	 *            command line arguments (path to input)
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage:");
			System.err
					.println("    java [javaopts] Linker program.obj /outputDirectory/");
			System.err.println("    javaopts - arguments to the Java VM");
			System.err.println("    program.obj - name of program to link");
			System.err
					.println("    outputDirectory - directory for linker output");
			System.exit(-1);
		}
		try {
			File inputFile = new File(args[0]);
			String outputDir = args[1];

			String inputName = inputFile.getName();
			if (inputName.lastIndexOf('.') > 0) {
				inputName = inputName.substring(0, inputName.lastIndexOf('.'));
			}
			Reader in = new FileReader(inputFile);
			FileOutputStream mapOut = new FileOutputStream(outputDir + "/"
					+ inputName + "_sym.out");
			FileOutputStream repOut = new FileOutputStream(outputDir + "/"
					+ inputName + "_report.out");
			FileOutputStream finalOut = new FileOutputStream(outputDir + "/"
					+ inputName + ".out");
			Linker link = new Linker();
			link.runFirstPass(in);
			link.runSecondPass();
			link.outputLoadMap(mapOut);
			link.outputRecords(repOut);
			link.outputLoadFile(finalOut);
			mapOut.close();
			repOut.close();
			finalOut.close();
			in.close();
		} catch (FileNotFoundException e) {
			System.out
					.println("Error reading from input or writing to output: "
							+ e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
