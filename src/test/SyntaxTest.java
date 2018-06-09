package test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import assembler.Parser;
import assembler.Program;

/**
 * <pre>
 * Class Name: SyntaxTest
 * Description: JUnit parameterized test for running all syntax checks
 * 
 * Version information: 1.0
 * $RCSfile: SyntaxTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/04/19 06:35:55 $
 * </pre>
 * 
 * @author Andrew
 * 
 */
@RunWith(Parameterized.class)
public class SyntaxTest {
	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object[]> dat = null;
		try {
			File testdir = new File("test-programs/syntax/");
			File[] files = testdir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".urban");
				}
			});
			dat = new ArrayList<Object[]>(files.length);
			for (File f : files) {
				dat.add(new Object[] { f });
			}
		} finally {
			if (dat == null) {
				dat = Collections.<Object[]> emptyList();
			}
		}
		return dat;
	}

	private final Collection<Collection<String>> errors;
	private final File file;

	public SyntaxTest(File file) throws IOException {
		BufferedReader br = null;

		this.errors = new ArrayList<Collection<String>>();
		this.file = file;

		try {
			br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				if (line.length() > 0) {
					Matcher m = errorPattern.matcher(line);
					if (m.find()) {
						String listOfErrors = m.group(1);
						errors.add(Arrays.asList(listOfErrors.split(" ")));
					} else {
						errors.add(Collections.<String> emptyList());
					}
				}
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	@Test
	public void test() throws Exception {
		Reader in = new FileReader(file);
		ByteArrayOutputStream lstStream = new ByteArrayOutputStream();
		ByteArrayOutputStream symStream = new ByteArrayOutputStream();

		Program prog;
		try {
			System.err.println(file.getName());
			prog = Parser.runFirstPass(in);
			in.close();
		} catch (Exception e) {
			throw e;
		}

		prog.printListing(lstStream);
		prog.printSymbols(symStream);
		byte[] lst = lstStream.toByteArray();
		byte[] sym = symStream.toByteArray();
		lstStream.close();
		symStream.close();

		// TODO Compare input to list of expected errors

		OutputStream lstFile = new FileOutputStream(file.getCanonicalPath()
				.replace("syntax", "output").replace(".urban", "_list.txt"));
		lstFile.write(lst);
		lstFile.close();
		OutputStream symFile = new FileOutputStream(file.getCanonicalPath()
				.replace("syntax", "output").replace(".urban", "_sym.txt"));
		symFile.write(sym);
		symFile.close();

	}

	private static final Pattern errorPattern = Pattern
			.compile("Errors:\\s*([0-9 ]+)");
}
