package assembler;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * <pre>
 * Class Name: ParserTest
 * Description:	Tests parsing functionality
 * 
 * Version information:
 * $RCSfile: ParserTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/05/09 02:03:49 $
 * </pre>
 * 
 * @author Zak
 * 
 */
public class ParserTest {
	// [p Program used for testing]
	Program p;

	/**
	 * <pre>
	 * Procedure Name: setUp
	 * Description:	Clears the program instance
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
	 * @since Apr 14, 2012
	 * @author Zak
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() {
		p = Program.getInstance();
	}

	@Test
	public final void emptyFirstPass() {
		Reader in = new StringReader("");
		p = Parser.runFirstPass(in);
		assertEquals(" has no entries in listing table", 0,
				p.listingTableSize());
		assertEquals(" has no entries in symbol table", 0, p.symbolTableSize());
	}

	@Test
	public final void shortFirstPass() {
		Reader in = new StringReader("ZAK KICKO FC:0;\n END LR:ZAK;");
		p = Parser.runFirstPass(in);
		assertEquals("Both lines added into listing table", 2,
				p.listingTableSize());
		assertEquals("Only one symbol added into symbol table", 1,
				p.symbolTableSize());
	}

	@Test
	public final void commentLineFirstPass() {
		Reader in = new StringReader(
				"ZAK KICKO FC:0;\n; comment line\n END LR:ZAK;");
		p = Parser.runFirstPass(in);
		assertEquals("Comment line added to listing table", 3,
				p.listingTableSize());
	}

	@Test
	public final void emptyLineFirstPass() {
		Reader in = new StringReader("ZAK KICKO FC:0;\n\n END LR:ZAK;");
		p = Parser.runFirstPass(in);
		assertEquals("Empty line added to listing table", 3,
				p.listingTableSize());
	}

	@Test
	public final void longFirstPass() {
		Reader in;
		try {
			in = new FileReader("alt1.txt");
			p = Parser.runFirstPass(in);
			assertEquals("Listing table contains all lines", 46,
					p.listingTableSize());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
