package assembler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.swing.JOptionPane;

/**
 * <pre>
 * Class Name: Urban
 * Description: main controller for the application.
 * 
 * Version information:
 * $RCSfile: Urban.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/05/09 18:17:17 $
 * </pre>
 * 
 * @author Andrew
 * 
 */
public class Urban {

	/**
	 * <pre>
	 * Procedure Name: getInput
	 * Description: gets urban assembly input.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: invalid IO references
	 * Error messages generated: java runtime exceptions
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 adjusted to work with redesign, added documentation
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 12, 2012
	 * @author Andrew
	 * @param gui
	 *            is the swing GUI used for interaction
	 * @return returns the input file
	 */
	private static String getInput(MainView gui) {
		// [input String local text of input source code]
		// [fileIn FileReader local reader for input source file]
		// [sb StringBuilder local string builder for input source file]
		// [c int local integer local value of character used when reading
		// source file]
		// [ex IOException local exception thrown when there is an error opening
		// input file]
		String input;
		try {
			FileReader fileIn = new FileReader(gui.getInputFile());
			StringBuilder sb = new StringBuilder();
			int c;

			while ((c = fileIn.read()) != -1) {
				sb.append((char) c);
			}
			input = sb.toString();
			fileIn.close();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(gui, "Error opening input file");
			return "<<ERROR>>";
		}
		return input;
	}

	/**
	 * <pre>
	 * Procedure Name: outputIfPossible
	 * Description: Write the given byte array to a file, if the file is writable
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested: None
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 adjusted to work with redesign, added documentation
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since May 9, 2012
	 * @author Andrew
	 * @param dir
	 *            Directory to output to
	 * @param name
	 *            Name of file, including extension, excluding directory
	 * @param bytes
	 *            A series of bytes to write
	 */
	private static void outputIfPossible(File dir, String name,
			ByteArrayOutputStream bytes) {
		// [FileOutputStream fos Holds an array of bytes output by the
		// assembler]
		FileOutputStream fos = null;
		// [File file reference to a file on disk]
		File file;
		if (dir != null) {
			file = new File(dir, name);
			if (file != null
					&& (file.canWrite() || (!file.exists() && file
							.getParentFile().canWrite()))) {
				try {
					fos = new FileOutputStream(file);
					fos.write(bytes.toByteArray());
				} catch (Exception e) {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (Exception e2) {
					}
				}
			}
		}
	}

	/**
	 * <pre>
	 * Procedure Name: assemble
	 * Description: Runs the first pass of the assembly.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: null reference, bad streams
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 adjusted to work with redesign, added documentation
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 12, 2012
	 * @author Andrew
	 * @param gui
	 *            is the swing GUI for interactions
	 */
	private static void assemble(MainView gui) {
		// [prog Program local abstract program created during the assembly
		// process]
		// [in Reader local string reader for input source code]
		// [listingOut ByteArrayOutputStream output stream for listing table]
		// [symTableOut ByteArrayOutputStream output stream for symbol table]

		String input = getInput(gui);
		String name = gui.getInputFile().getName();
		if (name.lastIndexOf(".") >= 0) {
			name = name.substring(0, name.lastIndexOf("."));
		}
		File output = gui.getOutputPath() != null
				&& gui.getOutputPath().length() > 0 ? new File(
				gui.getOutputPath()) : null;
		gui.setInputTabText(input);

		Reader in = new StringReader(input);

		Program prog;

		prog = Parser.runFirstPass(in);
		if (!prog.runSecondPass()) {
			// TODO: Fatal errors
		}

		// Print the output of the first pass - the intermediate listing file
		// and the symbol table
		ByteArrayOutputStream listingOut = new ByteArrayOutputStream();
		ByteArrayOutputStream symTableOut = new ByteArrayOutputStream();
		ByteArrayOutputStream objectOut = new ByteArrayOutputStream();
		ByteArrayOutputStream reportOut = new ByteArrayOutputStream();

		prog.printListing(listingOut);
		prog.printSymbols(symTableOut);
		prog.generateObjectFile(objectOut);
		prog.printAssemblyReport(reportOut);

		gui.setOutputTabText(new String(listingOut.toByteArray()));
		outputIfPossible(output, name + ".tmp", listingOut);
		gui.setSymbolTableTabText(new String(symTableOut.toByteArray()));
		outputIfPossible(output, name + ".sym", symTableOut);
		gui.setObjectFileTabText(new String(objectOut.toByteArray()));
		outputIfPossible(output, name + ".obj", objectOut);
		gui.setUsrReportTabText(new String(reportOut.toByteArray()));
		outputIfPossible(output, name + ".report", reportOut);

	}

	/**
	 * <pre>
	 * Procedure Name: main
	 * Description: assembles, links, and loads input program.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested:  N/A
	 * Error messages generated: N/A
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 17, 2012 updated to conform to documentation standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 12, 2012
	 * @author Andrew
	 * @param args
	 *            are unused command line arguments
	 */
	public static void main(String[] args) {
		// [gui MainView local main swing view used for interactions]
		final MainView gui = new MainView();
		gui.addAssembleListner(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				assemble(gui);
				gui.repaint();
			}
		});

		gui.setVisible(true);
	}
}
