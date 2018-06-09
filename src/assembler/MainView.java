package assembler;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * <pre>
 * Class Name: MainView
 * Description: main GUI for URBAN assembly interaction.
 * 
 * Version information:
 * $RCSfile: MainView.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/05/08 00:00:34 $
 * </pre>
 * 
 * @author Anna Lin
 * 
 */
public final class MainView extends JFrame {

	// [serialVersionUID long local serial version for JFrame]
	private static final long serialVersionUID = 174837012706140542L;
	// [textFieldLength int Global constant of lenght of text fields in GUI]
	private final int textFieldLength = 25;

	// ========================================================================
	// ... Components
	// ========================================================================

	// [panel JPanel Global main panel]
	private JPanel panel = new JPanel();

	// [inputLbl JLabel Global JLabel saying "input"]
	private JLabel inputLbl = new JLabel("Input File");

	// [inputTxtFld JTextField Global text field showing input file path]
	private JTextField inputTxtFld = new JTextField(this.textFieldLength);

	// [inputChooserBtn JButton Global button for open file chooser]
	private JButton inputChooserBtn = new JButton("Browse...");

	// [outputLbl JLabel Global JLabel saying "output"]
	private JLabel outputLbl = new JLabel("Output Directory");

	// [outputTxtFld JTextField Global text field showing output file path]
	private JTextField outputTxtFld = new JTextField(this.textFieldLength);

	// [outputChooserBtn JButton Global button for save file chooser]
	private JButton outputChooserBtn = new JButton("Browse...");

	// [fc JFileChooser Global the file chooser GUI]
	private JFileChooser fc = new JFileChooser();

	// [assembleBtn JButton Global button to start assembling]
	private JButton assembleBtn = new JButton("Assemble");

	// [clearBtn JButton Global button to clear all fields]
	private JButton clearBtn = new JButton("Clear");

	// [tabbedPane JTabbedPane Global shows different outputs]
	private JTabbedPane tabbedPane = new JTabbedPane();

	// [inputpnl JPanel Global panel for input in tabbedPane]
	private JPanel inputPnl = new JPanel(new BorderLayout());

	// [outputpnl JPanel Global panel for output in tabbedPane]
	private JPanel outputPnl = new JPanel(new BorderLayout());

	// [symTblPnl JPanel Global panel for symbol table in tabbedPane]
	private JPanel symTblPnl = new JPanel(new BorderLayout());

	// [objFilePnl JPanel Global panel for object file in tabbedPane]
	private JPanel objFilePnl = new JPanel(new BorderLayout());

	// [usrReportPnl JPanel Global panel for user report in tabbedPane]
	private JPanel usrReportPnl = new JPanel(new BorderLayout());

	private static final Font monospacedFont = new Font("Monospaced",
			Font.PLAIN, 14);

	/**
	 * Default constructor.
	 */
	public MainView() {
		// [ fill int Local amount of space to put in GUI spacers]
		final int fill = 10, mainHeight = 500, mainWidth = 500;

		// ====================================================================
		// ... Initialize components
		// ====================================================================
		this.panel.setPreferredSize(new Dimension(mainWidth, mainHeight));
		this.inputChooserBtn.addActionListener(new OpenFileChooserListener());
		this.outputChooserBtn.addActionListener(new SaveFileChooserListener());
		this.clearBtn.addActionListener(new ClearListener());
		this.tabbedPane.addTab("Input", this.inputPnl);
		this.tabbedPane.addTab("Output", this.outputPnl);
		this.tabbedPane.addTab("Symbol Table", this.symTblPnl);
		this.tabbedPane.addTab("Object File", this.objFilePnl);
		this.tabbedPane.addTab("User Report", this.usrReportPnl);

		// ====================================================================
		// ... Layout the components
		// ====================================================================
		// [inputPnl JPanel Local holds the widgets that get input file path]
		JPanel inputPnl = new JPanel();
		inputPnl.setLayout(new BoxLayout(inputPnl, BoxLayout.LINE_AXIS));
		inputPnl.add(Box.createHorizontalGlue());
		inputPnl.add(this.inputLbl);
		inputPnl.add(Box.createRigidArea(new Dimension(fill, 0)));
		inputPnl.add(this.inputTxtFld);
		inputPnl.add(Box.createRigidArea(new Dimension(fill, 0)));
		inputPnl.add(this.inputChooserBtn);
		inputPnl.add(Box.createHorizontalGlue());

		// [outputPnl JPanel Local holds the widgets that get output file path]
		JPanel outputPnl = new JPanel();
		outputPnl.setLayout(new BoxLayout(outputPnl, BoxLayout.LINE_AXIS));
		outputPnl.add(Box.createHorizontalGlue());
		outputPnl.add(this.outputLbl);
		outputPnl.add(Box.createRigidArea(new Dimension(fill, 0)));
		outputPnl.add(this.outputTxtFld);
		outputPnl.add(Box.createRigidArea(new Dimension(fill, 0)));
		outputPnl.add(this.outputChooserBtn);
		outputPnl.add(Box.createHorizontalGlue());

		// [buttonPnl JPanel Local holds the buttons that run the program]
		JPanel buttonPnl = new JPanel();
		buttonPnl.setLayout(new BoxLayout(buttonPnl, BoxLayout.LINE_AXIS));
		buttonPnl.add(Box.createHorizontalGlue());
		buttonPnl.add(this.assembleBtn);
		buttonPnl.add(Box.createHorizontalGlue());
		buttonPnl.add(this.clearBtn);
		buttonPnl.add(Box.createHorizontalGlue());

		// [topPnl JPanel Local holds the input, output and button panels]
		JPanel topPnl = new JPanel();
		topPnl.setLayout(new BoxLayout(topPnl, BoxLayout.Y_AXIS));
		topPnl.add(Box.createRigidArea(new Dimension(0, fill)));
		topPnl.add(inputPnl);
		topPnl.add(Box.createRigidArea(new Dimension(0, fill)));
		topPnl.add(outputPnl);
		topPnl.add(Box.createRigidArea(new Dimension(0, fill)));
		topPnl.add(buttonPnl);
		topPnl.add(Box.createRigidArea(new Dimension(0, fill)));

		this.panel.setLayout(new BorderLayout());
		this.panel.add(topPnl, BorderLayout.PAGE_START);
		this.panel.add(this.tabbedPane, BorderLayout.CENTER);

		// ====================================================================
		// ... finalize the layout
		// ====================================================================
		this.setContentPane(this.panel);
		this.pack();

		this.setTitle("View");
		// The window closing event should probably be passed to the
		// Controller in a real program, but this is a short example.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * <pre>
	 * Procedure Name: clearTabbedPanel
	 * Description: clears the fields of the GUI to default.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: empty panel
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why): 
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 5, 2012
	 * @author Anna
	 */
	private void clearTabbedPanel() {
		this.tabbedPane.removeAll();
		this.inputPnl.removeAll();
		this.outputPnl.removeAll();
		this.symTblPnl.removeAll();
		this.tabbedPane.addTab("Input", this.inputPnl);
		this.tabbedPane.addTab("Output", this.outputPnl);
		this.tabbedPane.addTab("Symbol Table", this.symTblPnl);
		this.tabbedPane.addTab("Object File", this.objFilePnl);
		this.tabbedPane.addTab("User Report", this.usrReportPnl);
	}

	/**
	 * <pre>
	 * Procedure Name: getInputFile
	 * Description: returns the file chosen within the GUI.
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: (pass 1, pass 2, or both)
	 * 
	 * Error Conditions Tested: field empty
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 8, 2012
	 * @author Anna
	 * @return the input file object
	 */
	public File getInputFile() {
		return new File(this.inputTxtFld.getText());
	}

	/**
	 * <pre>
	 * Procedure Name: getOutputPath
	 * Description: returns the path of the directory that the user wants the files to be saved to
	 * 
	 * Specification reference code(s):
	 * Calling Sequence: both
	 * 
	 * Error Conditions Tested:
	 * Error messages generated:
	 * 
	 * Modification Log (who when and why):
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @author Anna_Lin
	 * @return the path of the directory for the output files
	 */
	public String getOutputPath() {
		return this.outputTxtFld.getText();
	}

	/**
	 * <pre>
	 * Procedure Name: setInputTabText
	 * Description: sets the content of the input tab to the given string.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: text previously set, panel not empty
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Andrew Apr 8, 2012 switched from labels to text areas
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 8, 2012
	 * @author Anna
	 * @param text
	 *            is the string of the text to be shown in the input tab
	 */
	public void setInputTabText(String text) {
		this.inputPnl.removeAll();
		// [textArea JTextArea Local the text area in the scrollable input tab]
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(monospacedFont);
		this.inputPnl.add(new JScrollPane(textArea));
	}

	/**
	 * <pre>
	 * Procedure Name: setOutputTabText
	 * Description: sets the content of the output tab to given string.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: nonempty panel
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Andrew Apr 8, 2012 switched from labels to text areas
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 8, 2012
	 * @author Anna
	 * @param text
	 *            is the string of the text to be shown in the output tab
	 */
	public void setOutputTabText(String text) {
		this.outputPnl.removeAll();
		// [textArea JTextArea Local the text area in the scrollable output tab]
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(monospacedFont);
		this.outputPnl.add(new JScrollPane(textArea));
	}

	/**
	 * <pre>
	 * Procedure Name: setSymbolTableTabText
	 * Description: sets the content of the symbol table tab to the given
	 * string.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 1
	 * 
	 * Error Conditions Tested: nonempty panel
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Andrew Apr 8, 2012 switched from labels to text areas
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 8, 2012
	 * @author Anna
	 * @param text
	 *            is the string of the text to be shown in the symbol table tab
	 */
	public void setSymbolTableTabText(String text) {
		this.symTblPnl.removeAll();
		// [textArea JTextArea Local the text area in the scrollable symbol
		// table tab]
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(monospacedFont);
		this.symTblPnl.add(new JScrollPane(textArea));
	}

	/**
	 * <pre>
	 * Procedure Name: setObjectFileTabText
	 * Description: sets the contents of the object file tab to the given string.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Pass 2
	 * 
	 * Error Conditions Tested:
	 * Error Messages generated:
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @aurthor Anna
	 * @param text
	 *            is the string of the text to be shown in the object file tab
	 */
	public void setObjectFileTabText(String text) {
		this.objFilePnl.removeAll();
		// [textArea JTextArea Local the text area in the scrollable object file
		// tab]
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(monospacedFont);
		this.objFilePnl.add(new JScrollPane(textArea));
	}

	/**
	 * <pre>
	 * Procedure Name: setUsrReportTabText
	 * Description: sets the contents of the user report tab to the given string.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence:
	 * 
	 * Error Conditions Tested:
	 * Error Messages generated:
	 * 
	 * Modification Log (who when and why):
	 * 
	 * Coding standards met: Signed by
	 * Testing standards met: Signed by
	 * </pre>
	 * 
	 * @since May 7, 2012
	 * @aurthor Anna
	 * @param text
	 *            is the string of the text to be shown in the user report tab
	 */
	public void setUsrReportTabText(String text) {
		this.usrReportPnl.removeAll();
		// [textArea JTextArea Local the text area in the scrollable user report
		// tab]
		JTextArea textArea = new JTextArea(text);
		textArea.setFont(monospacedFont);
		this.usrReportPnl.add(new JScrollPane(textArea));
	}

	/**
	 * <pre>
	 * Procedure Name: addAssembleListner
	 * Description: adds an ActionListener to the Assemble button.
	 * 
	 * Specification reference code(s): N/A
	 * Calling Sequence: Both
	 * 
	 * Error Conditions Tested: null reference
	 * Error messages generated: None
	 * 
	 * Modification Log (who when and why):
	 * Michael Apr 18, 2012 updated to conform to team standards
	 * 
	 * Coding standards met: Signed by Michael
	 * Testing standards met: Signed by Michael
	 * </pre>
	 * 
	 * @since Apr 5, 2012
	 * @author Anna
	 * @param assembleListener
	 *            is the ActionListener to be associated with the Assemble
	 *            button
	 */
	public void addAssembleListner(ActionListener assembleListener) {
		this.assembleBtn.addActionListener(assembleListener);
	}

	/**
	 * <pre>
	 * Class Name: ClearListener
	 * Description: clears all fields of GUI and sets to default
	 * 
	 * Version information:
	 * $RCSfile: MainView.java,v $
	 * $Revision: 1.3 $
	 * $Date: 2012/05/08 00:00:34 $
	 * </pre>
	 * 
	 * @author Anna
	 * 
	 */
	private class ClearListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			MainView.this.inputTxtFld.setText("");
			MainView.this.outputTxtFld.setText("");
			MainView.this.clearTabbedPanel();
		}
	}

	/**
	 * <pre>
	 * Class Name: OpenFielChooserListener
	 * Description: opens a file chooser
	 * 
	 * Version information:
	 * $RCSfile: MainView.java,v $
	 * $Revision: 1.3 $
	 * $Date: 2012/05/08 00:00:34 $
	 * </pre>
	 * 
	 * @author Anna
	 * 
	 */
	private class OpenFileChooserListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			// [returnVal int Local click result from FileChooser GUI]
			int returnVal = MainView.this.fc.showOpenDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// [file File Local the file selected by a file chooser]
				File file = MainView.this.fc.getSelectedFile();
				MainView.this.inputTxtFld.setText(file.getPath());
			} else {
				JOptionPane
						.showMessageDialog(MainView.this, "File Input Issue");
			}
		}
	}

	private class SaveFileChooserListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// [returnVal int Local click result from FileChooser GUI]
			int returnVal = MainView.this.fc.showSaveDialog(null);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// [file File Local the file selected by a file chooser]
				File file = MainView.this.fc.getSelectedFile();
				// This is where a real application would save the file.
				MainView.this.outputTxtFld.setText(file.getPath());
			} else {
				JOptionPane.showMessageDialog(MainView.this,
						"File Output Issue");
			}
		}
	}
}
