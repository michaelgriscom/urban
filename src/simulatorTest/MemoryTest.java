package simulatorTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Memory;
import simulator.Memory.MemoryAccessException;

/**
 * <pre>
 * Class Name:
 * Description:
 * 
 * Version information:
 * $RCSfile: MemoryTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2012/05/23 00:51:37 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public class MemoryTest {

	private static Memory mem;

	@Before
	public void setUp() {
		mem = Memory.getInstance();
	}

	@After
	public void tearDown() {
		Memory.reset();
	}

	@Test(expected = MemoryAccessException.class)
	public void lowerBoundTestSmall() throws MemoryAccessException {
		// lower bound must be >=0, <=4095
		mem.setLowerBound(-1);
	}

	@Test(expected = MemoryAccessException.class)
	public void lowerBoundTestBig() throws MemoryAccessException {
		// lower bound must be >=0, <=4095
		mem.setLowerBound(4096);
	}

	@Test(expected = MemoryAccessException.class)
	public void upperBoundTestSmall() throws MemoryAccessException {
		// upper bound must be >=0, <=4095
		mem.setUpperBound(-1);
	}

	@Test(expected = MemoryAccessException.class)
	public void upperBoundTestBig() throws MemoryAccessException {
		// upper bound must be >=0, <=4095
		mem.setUpperBound(4096);
	}

	@Test(expected = MemoryAccessException.class)
	public void lowerGreaterThanUpper() throws MemoryAccessException {
		// lower bound must be <= upper bound
		try {
			mem.setLowerBound(10);
		} catch (MemoryAccessException e) {
			fail("Lower bound of 10 is acceptable");
		}
		mem.setUpperBound(0);
	}

	@Test(expected = MemoryAccessException.class)
	public void upperLessThanLower() throws MemoryAccessException {
		// lower bound must be <= upper bound
		try {
			mem.setUpperBound(10);
		} catch (MemoryAccessException e) {
			fail("Upper bound of 10 is acceptable");
		}
		mem.setLowerBound(11);
	}

	@Test
	public void equalBoundsMin() {
		try {
			mem.setLowerBound(0);
			mem.setUpperBound(0);
			mem.write(Integer.MAX_VALUE, 0);
		} catch (MemoryAccessException e) {
			fail("lower bound = upper bound = 0 is acceptable");
		}
	}

	@Test
	public void equalBoundsMax() {
		try {
			mem.setLowerBound(4095);
			mem.setUpperBound(4095);
			mem.write(Integer.MAX_VALUE, 4095);
		} catch (MemoryAccessException e) {
			fail("lower bound = upper bound = 4095 is acceptable");
		}
	}

	@Test
	public void testWriteMax() {
		final int addr = 4095;
		final int val = Integer.MAX_VALUE;
		try {
			mem.setLowerBound(0);
			mem.setUpperBound(4095);
			mem.write(val, addr);
			assertEquals("Writing max integer value to memory", val,
					mem.read(addr));
		} catch (MemoryAccessException e) {
			fail("lower bound = upper bound is acceptable");
		}
	}

	@Test
	public void testWriteMin() {
		final int addr = 0;
		final int val = Integer.MIN_VALUE;
		try {
			mem.setLowerBound(0);
			mem.setUpperBound(4095);
			mem.write(val, addr);
			assertEquals("Writing min integer value to memory", val,
					mem.read(addr));
		} catch (MemoryAccessException e) {
			fail("lower bound = upper bound is acceptable");
		}
	}
}
