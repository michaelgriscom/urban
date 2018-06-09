package linker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * Class Name: ErrorDesc
 * Description: Describes a linking error - error code and line number
 * 
 * Version information:
 * $RCSfile: ErrorDesc.java,v $
 * $Revision: 1.6 $						
 * $Date: 2012/05/25 11:28:48 $
 * </pre>
 * 
 * @author andrew
 * 
 */
public class ErrorDesc {
	// [int errorCode the numerical identifier for this error]
	private final int errorCode;

	// [Map stringMap translates numeric error codes to human-readable strings]
	private static final Map<Integer, String> stringMap;

	static {
		stringMap = new HashMap<Integer, String>();
		try {
			// [BufferedReader reader used to read in the error messages file]
			BufferedReader reader = new BufferedReader(new FileReader(
					"src/linker/linker_errors.txt"));
			// [String line ranges over the lines in the error messages file]
			String line;
			while ((line = reader.readLine()) != null) {
				// [String[] parts results of parsing a line from the error
				// messages
				String[] parts = line.split("\\s", 2);
				try {
					stringMap.put(Integer.parseInt(parts[0], 10), parts[1]);
				} catch (NumberFormatException nfe) {
					// pass
				}
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Warning: could not load error messages.");
			System.err.println("Error reporting may be rather unhelpful.");
		}
	}

	public ErrorDesc(int err) {
		this.errorCode = err;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: getErrorCode
	 * Description: Get the numeric code of this error
	 * 						
	 * Specification reference code(s): 
	 * Calling Sequence: (pass 1, pass 2, or both) both
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author andrew
	 * @return the error code
	 */
	public int getErrorCode() {
		return this.errorCode;
	}

	/**
	 * 
	 * <pre>
	 * Procedure Name: toString
	 * Description: Print an error for human consumption
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both) pass 2
	 * 
	 * Error Conditions Tested: none
	 * Error messages generated: none
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 19, 2012
	 * @author andrew
	 * @return A human-readable string
	 */
	@Override
	public String toString() {
		// If only a DED weren't inflicted upon us, so I could use temporary
		// variables painlessly... This shall be my (ineffective) protest!
		return String.format("Error %d: %s", this.errorCode, (stringMap
				.containsKey(this.errorCode) ? stringMap.get(this.errorCode)
				: "Unknown error"));
	}
}
