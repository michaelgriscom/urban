package linker;

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
 * $RCSfile: SimpleLinker.java,v $
 * $Revision: 1.6 $
 * $Date: 2012/05/30 20:35:43 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public class SimpleLinker {
	public static class UrbanFileFilter implements FileFilter {
		private String fileExtension1 = "obj";
		private String fileExtension2 = "_obj.txt";

		@Override
		public boolean accept(File file) {
			return (file.getName().toLowerCase().endsWith("."
					+ this.fileExtension1))
					|| (file.getName().toLowerCase()
							.endsWith(this.fileExtension2));
		}
	}

	// First string is directory to find file, second is directory where files
	// are output
	/**
	 * 
	 * <pre>
	 * Procedure Name: main
	 * Description: Links and generates output for all files in a directory.
	 * 
	 * Specification reference code(s): None
	 * 
	 * Error Conditions Tested: no arguments, invalid arguments, invalid object file
	 * Error messages generated: All of them
	 * 
	 * Modification Log (who when and why):
	 * Zak 5/22/2012 set default directory to test-programs/linker/output
	 * Coding standards met: Signed by Michael 
	 * Testing standards met: Signed by Michale
	 * </pre>
	 * 
	 * @since May 30, 2012
	 * @author Zak
	 * @param args
	 */
	public static void main(String[] args) {
		// [inputDir String local directory containing all files that need to be
		// loaded]
		String inputDir = "";
		// [outputDir String local directory for outputting loaded files]
		String outputDir = "test-programs/linker/output/";
		if (args.length > 0) {
			inputDir = args[0];
		}

		if (args.length > 1) {
			outputDir = args[1];
		}

		// [directory File local file created from directory specified]
		File directory = new File(inputDir);

		// [programs File[] local all programs found in the input directory]
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
					Linker l = new Linker();
					l.runFirstPass(in);
					l.runSecondPass();
					l.outputLoadMap(new FileOutputStream(outputDir + progName
							+ "_sym.out"));
					l.outputRecords(new FileOutputStream(outputDir + progName
							+ "_report.out"));
					l.outputLoadFile(new FileOutputStream(outputDir + progName
							+ ".out"));
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

		// [entirePath String local the entire path name of the file]
		String entirePath = prog.toString();

		// [lastFwdSlash int local the position of the last forward slash in the
		// filename]
		// [lastBackSlash int local the position of the last backward slash in
		// the filename]
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

		if (progName.endsWith(".obj")) {
			progName = progName.substring(0, progName.length() - 4);
		} else if (progName.endsWith("_obj.txt")) {
			progName = progName.substring(0, progName.length() - 8);
		}

		return progName;
	}
}
