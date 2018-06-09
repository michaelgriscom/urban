package assembler;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * <pre>
 * Class Name:
 * Description:
 * 
 * Version information:
 * $RCSfile: SimpleAssembler.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/05/24 19:05:30 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public class SimpleAssembler {

	public static class UrbanFileFilter implements FileFilter {
		private String fileExtension = "urban";

		@Override
		public boolean accept(File file) {
			return (file.getName().toLowerCase().endsWith("."
					+ this.fileExtension));
		}
	}

	// First string is directory to find file, second is directory where files
	// are output
	public static void main(String[] args) {
		String inputDir = "";
		String outputDir = "test-programs/output/";
		if (args.length > 0) {
			inputDir = args[0];
		}

		if (args.length > 1) {
			outputDir = args[1];
		}

		File directory = new File(inputDir);
		File[] programs = directory.listFiles(new UrbanFileFilter());
		if (programs != null) {
			for (File prog : programs) {
				FileReader in;
				try {
					// At this point only ".urban" files are in Programs array
					String progName = getFileName(prog);

					if (outputDir.length() > 0 && !outputDir.endsWith("/")
							&& !outputDir.endsWith("\\")) {
						if (outputDir.contains("\\")) {
							outputDir = outputDir + "\\";
						} else {
							outputDir = outputDir + "/";
						}
					}

					in = new FileReader(prog);
					Program p = Parser.runFirstPass(in);
					p.runSecondPass();
					p.printSymbols(new FileOutputStream(new File(outputDir
							+ progName + "_sym.txt")));
					p.printAssemblyReport(new FileOutputStream(new File(
							outputDir + progName + "_report.txt")));
					p.generateObjectFile(new FileOutputStream(new File(
							outputDir + progName + "_obj.txt")));
					System.out.println("Generated output for \"" + progName
							+ "\" in directory: \"" + outputDir + "\"");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out
					.println("Did not find any files in the directory specified");
		}
	}

	/**
	 * <pre>
	 * Procedure Name: getFileName
	 * Description: Gets the file name of a passed in file
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 10, 2012
	 * @author Zak
	 * @param prog
	 *            The file whose name will be extracted
	 * @return
	 *         The program name of the file
	 */
	private static String getFileName(File prog) {
		String progName = "";

		String entirePath = prog.toString();
		int lastFwdSlash = entirePath.lastIndexOf('/');
		int lastBackSlash = entirePath.lastIndexOf('\\');

		int pathEnd = lastFwdSlash > lastBackSlash ? lastFwdSlash
				: lastBackSlash;
		if (pathEnd > 0 && entirePath.length() > pathEnd) {
			progName = entirePath.substring(pathEnd + 1);

		} else if (entirePath.length() == pathEnd) {
			progName = "unnamed";
		} else {
			progName = entirePath;
		}

		if (progName.endsWith(".urban")) {
			progName = progName.substring(0, progName.length() - 6);
		}

		return progName;
	}
}
