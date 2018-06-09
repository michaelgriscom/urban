package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * <pre>
 * Class Name: ErrorTable
 * Description: table of error codes keyed to their corresponding string values.
 * 
 * Version information:
 * $RCSfile: ErrorTable.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/05/09 00:40:24 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public class ErrorTable {
	// [instance ErrorTable local singleton instance of the ErrorTable]
	private static ErrorTable instance = new ErrorTable();

	private Map<Integer, String> errors;

	/**
	 * <pre>
	 * Procedure Name: parseErrors
	 * Description: opens a given file containing errors and populates the
	 * error table.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: null file name, file not found
	 * Error messages generated: file not found exception
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 16, 2012
	 * @author Zak
	 * @param filename
	 *            is the name of the file to be read
	 */
	private void parseErrors(final String filename) {
		// [number int local temporary value of a given error code]
		// [e Exception local exception thrown when error occurs]
		// [description String local description of the error]
		// [in Scanner local scanner to read the error file]

		Scanner in;
		try {
			in = new Scanner(new File(filename));
			while (in.hasNext()) {
				int number = in.nextInt();
				String description = in.nextLine().trim();
				errors.put(number, description);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private ErrorTable() {
		// [errors HashMap local map from error codes to error messages]
		errors = new HashMap<Integer, String>();
		parseErrors("errors.txt");
	}

	/**
	 * <pre>
	 * Procedure Name: getInstance
	 * Description: gets the instance of the error table singleton.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
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
	 * @since Apr 16, 2012
	 * @author Zak
	 * @return the instance of the error table
	 */
	public static ErrorTable getInstance() {
		return instance;
	}

	/**
	 * <pre>
	 * Procedure Name: getErrorMessage
	 * Description: looks up a given error code and returns the error message.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: error number not in table
	 * Error messages generated: returns error corresponding to code
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 16, 2012
	 * @author Zak
	 * @param number
	 *            is the error number to look up
	 * @return the string corresponding to the given error code
	 */
	public String getErrorMessage(int number) {
		// [message string local the error message corresponding to a given
		// code]
		String message = errors.get(number);
		if (message == null) {
			message = "No error message associated with this number";
		} else if (number < 200) {
			message = "Warning: " + message;
		} else if (number >= 200 && number < 300) {
			message = "Serious: " + message;
		} else {
			message = "Fatal: " + message;
		}

		return message;
	}

}
