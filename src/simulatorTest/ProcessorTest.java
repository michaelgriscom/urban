package simulatorTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Memory;
import simulator.Memory.MemoryAccessException;
import simulator.Processor;

/**
 * <pre>
 * Class Name:
 * Description:
 * 
 * Version information:
 * $RCSfile: ProcessorTest.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/05/30 21:13:20 $
 * </pre>
 * 
 * @author Michael
 * 
 */
public class ProcessorTest {

	private static Processor cpu;
	private static Memory mem;
	private static InstrucTable inst = InstrucTable.getInstance();

	@Before
	public void setUp() {
		cpu = Processor.getInstance();
		mem = Memory.getInstance();
		cpu.setMemory(mem);
		Processor.setIO(System.in, System.out);
		try {
			mem.setLowerBound(0);
			mem.setUpperBound(4095);
		} catch (MemoryAccessException e) {
			fail("Memory bounds set error");
		}

	}

	@After
	public void tearDown() {
		Processor.reset();
		Memory.reset();
	}

	// =================================================================
	// ================ Data Movement ==================================
	// =================================================================

	@Test
	public void movd1() throws MemoryAccessException {
		int LC = 0;
		int reg = 1;
		int memory = LC;

		// create instruction
		String mnem = "MOVD";
		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(memory)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		// instruction code
		int val = line.getVal();

		// write to memory, execute that instruction
		mem.write(val, LC);
		cpu.setLC(LC);
		cpu.executeInstruction();

		// check results
		assertEquals("Register and read value should be equal", val,
				cpu.getArithmeticRegister(reg));
	}

	@Test
	public void movd2() throws MemoryAccessException {
		int LC = 0;
		int reg = 1;
		int memory = LC;

		String mnem = "MOVD";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DM", Integer.toString(memory)));
		ops.add(new Operand("FR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int val = line.getVal();

		mem.write(val, LC);
		cpu.setArithmeticRegister(reg, val);

		cpu.setLC(LC);
		cpu.executeInstruction();

		assertEquals("Register and read value should be equal", val,
				mem.read(LC));
	}

	// =================================================================
	// ================ Arithmetic =====================================
	// =================================================================

	@Test
	public void add1() throws MemoryAccessException {
		// test of IADD with DR, FM

		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IADD";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		// System.out.println("binary = "
		// + new Instruction(instruction).getBinary());
		// System.out.println("arg = " + mem.read(argLocation));
		// System.out.println("inst = " + mem.read(instructionLocation)
		// + " read from " + instructionLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 + arg2,
				result);
	}

	@Test
	public void add2() throws MemoryAccessException {
		// test of IADD with DM, FR

		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IADD";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		// System.out.println("arg = " + mem.read(argLocation));
		// System.out.println("inst = " + mem.read(instructionLocation)
		// + " read from " + instructionLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 + arg2,
				result);
	}

	@Test
	public void multAdd1() throws MemoryAccessException {
		// test of IMAD with FM, DR
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IMAD";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 * arg2
				+ arg2, result);
	}

	@Test
	public void multAdd2() throws MemoryAccessException {
		// test of IMAD with FR, DM
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IMAD";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 * arg2
				+ arg2, result);
	}

	@Test
	public void addAbsolute1() throws MemoryAccessException {
		// test of IAA with FM, DR
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = -3;

		String mnem = "IAA";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal",
				arg1 + Math.abs(arg2), result);
	}

	@Test
	public void addAbsolute2() throws MemoryAccessException {
		// test of IAA with FR, DM
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IAA";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();
		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal",
				arg1 + Math.abs(arg2), result);
	}

	@Test
	public void sub1() throws MemoryAccessException {
		// test of ISUB with FM, DR
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 7;
		int arg2 = 3;

		String mnem = "ISUB";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 - arg2,
				result);
	}

	@Test
	public void sub2() throws MemoryAccessException {
		// test of ISUB with FR, DM
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 7;
		int arg2 = 3;

		String mnem = "ISUB";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 - arg2,
				result);
	}

	@Test
	public void mult1() throws MemoryAccessException {
		// test of IMUL with FM, DR
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "IMUL";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 * arg2,
				result);
	}

	@Test
	public void mult2() throws MemoryAccessException {
		// test of IMUL with FR, DM
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 6;
		int arg2 = 3;

		String mnem = "IMUL";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);

		mem.write(arg2, argLocation);
		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 * arg2,
				result);
	}

	@Test
	public void divide1() throws MemoryAccessException {
		// test of IDIV with FM, DR
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 6;
		int arg2 = 3;

		String mnem = "IDIV";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg2, argLocation);

		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 / arg2,
				result);
	}

	@Test
	public void divide2() throws MemoryAccessException {
		// test of IDIV with FR, DM
		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 6;
		int arg2 = 3;

		String mnem = "IDIV";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 / arg2,
				result);
	}

	@Test
	public void power1() throws MemoryAccessException {
		// test of PWR
		int LC = 0;
		int reg = 1;
		int root = 2;
		int pow = 3;

		String mnem = "PWR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(pow)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, root);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);

		int actual = 1;
		for (int i = 0; i < pow; i++) {
			actual *= root;
		}

		assertEquals("Register and read value should be equal", actual, result);
	}

	// =================================================================
	// ================ Register Tools =================================
	// =================================================================

	@Test
	public void clear1() throws MemoryAccessException {
		// CLR for arithmetic register
		int LC = 0;
		int reg = 1;
		String mnem = "CLR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int val = line.getVal();

		cpu.setArithmeticRegister(reg, 5);

		mem.write(val, LC);
		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register should be reset to 0", 0, result);
	}

	@Test
	public void clear2() throws MemoryAccessException {
		// CLR for index register
		int LC = 0;
		int reg = 3;
		String mnem = "CLR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DX", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();
		int instruction = line.getVal();

		cpu.setIndexRegister(reg, 5);

		mem.write(instruction, LC);
		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getIndexRegister(reg);
		assertEquals("Register should be reset to 0", 0, result);
	}

	@Test
	public void clearA() throws MemoryAccessException {
		// CLRA
		int LC = 0;
		String mnem = "CLRA";

		Line line = new Line(0, "", "", mnem, "", new ArrayList<Operand>(),
				new HashSet<Integer>());
		line.setBinary();
		int instruction = line.getVal();

		cpu.setArithmeticRegister(1, 5);
		cpu.setArithmeticRegister(3, 6);
		cpu.setArithmeticRegister(4, 1);

		mem.write(instruction, LC);
		cpu.setLC(LC);
		cpu.executeInstruction();

		boolean result = true;
		final int NUM_ARITHMETIC_REGISTERS = 8;
		for (int i = 0; i < NUM_ARITHMETIC_REGISTERS; i++) {
			if (cpu.getArithmeticRegister(i) != 0) {
				result = false;
				break;
			}
		}
		assertTrue("All arithmetic registers should be reset to 0", result);
	}

	@Test
	public void clearX() throws MemoryAccessException {
		// CLRX
		int LC = 0;
		String mnem = "CLRX";

		Line line = new Line(0, "", "", mnem, "", new ArrayList<Operand>(),
				new HashSet<Integer>());
		line.setBinary();
		int instruction = line.getVal();

		cpu.setIndexRegister(1, 5);
		cpu.setIndexRegister(3, 6);
		cpu.setIndexRegister(4, 1);

		mem.write(instruction, LC);
		cpu.setLC(LC);
		cpu.executeInstruction();

		boolean result = true;
		final int NUM_INDEX_REGISTERS = 7;
		for (int i = 0; i < NUM_INDEX_REGISTERS; i++) {
			if (cpu.getArithmeticRegister(i) != 0) {
				result = false;
				break;
			}
		}
		assertTrue("All index registers should be reset to 0", result);
	}

	// =================================================================
	// ================ Shift/Manipulate ===============================
	// =================================================================

	@Test
	public void ishr() throws MemoryAccessException {
		// ISHR
		int LC = 0;
		String mnem = "ISHR";
		int reg = 1;
		int value = 9;
		int shift = 2;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		int pseudo_result = value;
		for (int i = 0; i < shift; i++) {
			pseudo_result /= 2;
		}

		assertEquals("Register and read value should be equal", pseudo_result,
				result);
	}

	@Test
	public void ishr2() throws MemoryAccessException {
		// ISHR full shift
		int LC = 0;
		String mnem = "ISHR";
		int reg = 1;
		int value = 9;
		int shift = 32;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);

		assertEquals("Register and read value should be equal", value, result);
	}

	@Test
	public void ishl() throws MemoryAccessException {
		// ISHL
		int LC = 0;
		String mnem = "ISHL";
		int reg = 1;
		int value = 9;
		int shift = 4;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		int pseudo_result = value;
		for (int i = 0; i < shift; i++) {
			pseudo_result *= 2;
		}

		assertEquals("Register and read value should be equal", pseudo_result,
				result);
	}

	@Test
	public void ishl2() throws MemoryAccessException {
		// ISHL full shift
		int LC = 0;
		String mnem = "ISHL";
		int reg = 1;
		int value = 9;
		int shift = 32;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);

		assertEquals("Register and read value should be equal", value, result);
	}

	@Test
	public void isra() throws MemoryAccessException {
		// ISRA
		int LC = 0;
		String mnem = "ISRA";
		int reg = 1;
		int value = -16;
		int shift = 3;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		int pseudo_result = value;
		for (int i = 0; i < shift; i++) {
			pseudo_result /= 2;
		}

		assertEquals("Register and read value should be equal", pseudo_result,
				result);
	}

	@Test
	public void isla() throws MemoryAccessException {
		// ISLA
		int LC = 0;
		String mnem = "ISLA";
		int reg = 1;
		int value = -11;
		int shift = 3;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		int pseudo_result = value;
		for (int i = 0; i < shift; i++) {
			pseudo_result *= 2;
		}

		assertEquals("Register and read value should be equal", pseudo_result,
				result);
	}

	@Test
	public void ror() throws MemoryAccessException {
		// ROR
		int LC = 0;
		String mnem = "ROR";
		int reg = 1;
		int value = -16;
		int shift = 3;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		int pseudo_result = value;
		for (int i = 0; i < shift; i++) {
			pseudo_result /= 2;
		}

		assertEquals("Register and read value should be equal", pseudo_result,
				result);
	}

	@Test
	public void rol() throws MemoryAccessException {
		// ROL
		int LC = 0;
		String mnem = "ROL";
		int reg = 1;
		int value = -11;
		int shift = 1;

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DR", Integer.toString(reg)));
		ops.add(new Operand("FC", Integer.toString(shift)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.setArithmeticRegister(reg, value);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);

		assertEquals("Register and read value should be equal",
				(value << shift) | (value >> (32 - shift)), result);
	}

	// =================================================================
	// ================ Logical ========================================
	// =================================================================

	@Test
	public void and1() throws MemoryAccessException {
		// test of AND with DM, FR
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "AND";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", arg1 & arg2,
				result);
	}

	@Test
	public void and2() throws MemoryAccessException {
		// test of AND with DR, FM
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "AND";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, instructionLocation);

		mem.write(arg2, argLocation);
		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", arg1 & arg2,
				result);
	}

	@Test
	public void or1() throws MemoryAccessException {
		// test of OR with DM, FR
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 3;
		int arg2 = 2;

		String mnem = "OR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(arg1, argLocation);

		cpu.setArithmeticRegister(reg, arg2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Register and read value should be equal", (arg2 | arg1),
				result);
	}

	@Test
	public void or2() throws MemoryAccessException {
		// test of OR with DR, FM
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int arg1 = 2;
		int arg2 = 3;

		String mnem = "OR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, instructionLocation);

		mem.write(arg2, argLocation);
		cpu.setArithmeticRegister(reg, arg1);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Register and read value should be equal", (arg1 | arg2),
				result);
	}

	// =================================================================
	// ================ Jump ===========================================
	// =================================================================

	@Test
	public void treq() throws MemoryAccessException {
		// test of TREQ
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int regValue = 0;

		String mnem = "TREQ";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, instructionLocation);

		cpu.setIndexRegister(reg, regValue);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		if (regValue == 0) {
			assertEquals("Register and read value should be equal",
					argLocation, cpu.getLC());
		} else {
			assertEquals("Register and read value should be equal",
					instructionLocation, cpu.getLC());
		}
	}

	@Test
	public void trlt() throws MemoryAccessException {
		// test of TRLT
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int regValue = -2;

		String mnem = "TRLT";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, instructionLocation);

		cpu.setArithmeticRegister(reg, regValue);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		if (regValue < 0) {
			assertEquals("Register and read value should be equal",
					argLocation, cpu.getLC());
		} else {
			assertEquals("Register and read value should be equal",
					instructionLocation, cpu.getLC());
		}
	}

	@Test
	public void trgt() throws MemoryAccessException {
		// test of TRGT
		int LC = 0;
		int reg = 2;
		int instructionLocation = LC;
		int argLocation = 100;
		int regValue = 8;

		String mnem = "TRGT";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FR", Integer.toString(reg)));
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, instructionLocation);

		cpu.setArithmeticRegister(reg, regValue);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		if (regValue > 0) {
			assertEquals("Register and read value should be equal",
					argLocation, cpu.getLC());
		} else {
			assertEquals("Register and read value should be equal",
					instructionLocation, cpu.getLC());
		}
	}

	@Test
	public void tr() throws MemoryAccessException {
		// test of TR
		int LC = 0;
		int argLocation = 100;
		int jump = 4;

		String mnem = "TR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		mem.write(jump, argLocation);

		cpu.setLC(LC);
		cpu.executeInstruction();

		assertEquals("Register and read value should be equal", argLocation,
				cpu.getLC());
	}

	// =================================================================
	// ================ I/O ============================================
	// =================================================================

	@Test
	public void iwsr() throws MemoryAccessException {
		// test of IWSR
		int LC = 0;
		int instructionLocation = LC;
		int argLocation = 100;
		int numWords = 3;
		int word1 = 0;
		int word2 = -10000;
		int word3 = 8675309;
		ByteArrayOutputStream tempOut = new ByteArrayOutputStream();

		Processor.setIO(System.in, tempOut);

		String mnem = "IWSR";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("NW", Integer.toString(numWords)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(word1, argLocation);
		mem.write(word2, argLocation + 1);
		mem.write(word3, argLocation + 2);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		Processor.setIO(System.in, System.out);
		assertEquals("Expected result printed to stdout", "0-100008675309",
				new String(tempOut.toByteArray()));
	}

	// =================================================================
	// ================ Stack Management ===============================
	// =================================================================

	@Test
	public void psh() throws MemoryAccessException {
		// test of PSH
		int LC = 0;
		int argLocation = 100;
		int stuff = 5;
		int stuff2 = 6;

		String mnem = "PSH";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.getStack().push(stuff2);
		mem.write(stuff, argLocation);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = cpu.getStack().pop();
		assertEquals("Top of stack is pushed", stuff, result);
	}

	@Test
	public void pop() throws MemoryAccessException {
		// test of POP
		int LC = 0;
		int argLocation = 100;
		int stuff = 5;

		String mnem = "POP";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("DM", Integer.toString(argLocation)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();
		mem.write(instruction, LC);

		cpu.getStack().push(stuff);

		cpu.setLC(LC);
		cpu.executeInstruction();

		int result = mem.read(argLocation);
		assertEquals("Top of stack is pushed", stuff, result);
	}

	@Test
	public void pst() throws MemoryAccessException {
		// test of PST with DR, FM

		int LC = 0;
		int reg = 1;
		int instructionLocation = LC;
		int argLocation = 100;
		int stuff = 2;
		int stuff2 = 3;

		String mnem = "PST";

		List<Operand> ops = new ArrayList<Operand>();
		ops.add(new Operand("FM", Integer.toString(argLocation)));
		ops.add(new Operand("DR", Integer.toString(reg)));

		Line line = new Line(0, "", "", mnem, "", ops, new HashSet<Integer>());
		line.setBinary();

		int instruction = line.getVal();

		mem.write(instruction, instructionLocation);
		mem.write(stuff2, argLocation);

		cpu.getStack().push(stuff);

		cpu.setLC(instructionLocation);
		cpu.executeInstruction();

		int result = cpu.getArithmeticRegister(reg);
		assertEquals("Test equals", 2, result);
	}
}
