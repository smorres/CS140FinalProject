package project;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static project.Model.Mode.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class InstructionTester {

	Model machine = new Model();
	int[] dataCopy = new int[Data.DATA_SIZE];
	int accInit;
	int ipInit;
	int offsetInit;

	@BeforeEach
	public void setup() {
		for (int i = 0; i < Data.DATA_SIZE; i++) {
			dataCopy[i] = -5*Data.DATA_SIZE + 10*i;
			machine.setData(i, -5*Data.DATA_SIZE + 10*i);
			// Initially the machine will contain a known spread
			// of different numbers: 
			// -10240, -10230, -10220, ..., 0, 10, 20, ..., 10230 
			// This allows us to check that the Model.Instructions do 
			// not corrupt machine unexpectedly.
			// 0 is at index 1024
		}
		accInit = 30;
		ipInit = 30;
		offsetInit = 200;
		machine.setAccum(accInit);
		machine.setInstrPtr(ipInit);
		machine.setMemBase(offsetInit);
	}

	@Test
	public void testNOP(){
		Model.Instruction instr = machine.get(0x0);
		instr.execute(0,null);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator untouched
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator unchanged")
				);
	}

	@Test 
	// Test whether NOP throws exception with immediate addressing mode
	public void testNOPimmediateMode() {
		Model.Instruction instr = machine.get(0x0);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Illegal Mode in NOP instruction", exception.getMessage());

	}
	
	@Test 
	// Test whether NOP throws exception with direct addressing mode
	public void testNOPdirectMode() {
		Model.Instruction instr = machine.get(0x0);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, DIRECT));		
		assertEquals("Illegal Mode in NOP instruction", exception.getMessage());

	}
	
	@Test 
	// Test whether NOP throws exception with indirect addressing mode
	public void testNOPindirectMode() {
		Model.Instruction instr = machine.get(0x0);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, INDIRECT));		
		assertEquals("Illegal Mode in NOP instruction", exception.getMessage());

	}
	
	@Test
	// Test whether load is correct with immediate addressing
	public void testLODimmediate(){
		Model.Instruction instr = machine.get(0x1);
		machine.setAccum(27);
		int arg = 12;
		// should load 12 into the accumulator
		instr.execute(arg, IMMEDIATE);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(12, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODdirect(){
		Model.Instruction instr = machine.get(0x1);
		machine.setAccum(27);
		int arg = 12;
		// should load dataCopy[offsetinit+12] into the accumulator
		instr.execute(arg, DIRECT);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(dataCopy[offsetInit+12], machine.getAccum(), "Accumulator modified")
				);
	}

	@Test
	// Test whether load is correct with direct addressing
	public void testLODindirect() {
		Model.Instruction instr = machine.get(0x1);
		machine.setAccum(-1);
		int arg = 1028-160;
		// if offset1 = dataCopy[offsetinit+1028-160] 
		// should load dataCopy[offsetinit+offset1] into the accumulator
		instr.execute(arg, INDIRECT);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> {
					int offset1 = dataCopy[offsetInit+1028-160]; 
					assertEquals(dataCopy[offsetInit+offset1], machine.getAccum(), "Accumulator modified");
				}
				);
	}	

	@Test 
	// Test whether LOD throws exception with null addressing mode
	public void testLODnullArg() {
		Model.Instruction instr = machine.get(0x1);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in LOD instruction", exception.getMessage());

	}
	
	@Test
	// Test whether store is correct with direct addressing
	public void testSTOdirect() {
		Model.Instruction instr = machine.get(0x2);
		int arg = 12;
		machine.setAccum(567);
		dataCopy[offsetInit + 12] = 567;
		instr.execute(arg, DIRECT);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(567, machine.getAccum(), "Accumulator unchanged")
				);
	}

	@Test
	// Test whether store is correct with indirect addressing
	public void testSTOindirect() {
		Model.Instruction instr = machine.get(0x2);
		int arg = 940; 
		machine.setAccum(567);
		// if offset1 = dataCopy[offsetinit + 940]
		// changed memory should be at offset1+offsetinit
		dataCopy[1360] = 567;
		instr.execute(arg, INDIRECT);
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(567, machine.getAccum(), "Accumulator unchanged")
				);
	}

	@Test 
	// Test whether STO throws exception with null addressing
	public void testSTOnullArg() {
		Model.Instruction instr = machine.get(0x2);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in STO instruction", exception.getMessage());

	}
	
	@Test 
	// Test whether STO throws exception with immediate addressing
	public void testSTOimmediateArg() {
		Model.Instruction instr = machine.get(0x2);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Illegal Mode in STO instruction", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is immediate
	public void testADDimmediate() {
		Model.Instruction instr = machine.get(0x3);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, IMMEDIATE); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(200+12, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is direct
	public void testADDdirect() {
		Model.Instruction instr = machine.get(0x3);
		int arg = 12; 
		machine.setAccum(250);
		// should add dataCopy[offsetinit+12] to the accumulator
		instr.execute(arg, DIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250-10240+2120, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the add is done correctly, when
	// addressing is indirect
	public void testADDindirect() {
		Model.Instruction instr = machine.get(0x3);
		int arg = 1028-160;
		machine.setAccum(250);
		// if offset1 = dataCopy[offsetinit+1028-160] = dataCopy[1068] = 10*(68-24) = 440
		// should add dataCopy[offsetinit+offset1] = dataCopy[640] = 6400-10240 to the accumulator	
		// -3840
		instr.execute(arg, INDIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250-3840, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// Test whether ADD throws exception with null addressing mode
	public void testADDnullArg() {
		Model.Instruction instr = machine.get(0x3);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in ADD instruction", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is immediate
	public void testSUBimmediate() {
		Model.Instruction instr = machine.get(0x4);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, IMMEDIATE); 
		// should have subtracted 12 from accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(200-12, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is direct
	public void testSUBdirect() {
		Model.Instruction instr = machine.get(0x4);
		int arg = 12; 
		machine.setAccum(250);
		// should subtract dataCopy[offsetinit+12] = dataCopy[212] = -10240 + 2120 from the accumulator
		instr.execute(arg, DIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250+10240-2120, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the subtraction is done correctly, when
	// addressing is indirect
	public void testSUBindirect() {
		Model.Instruction instr = machine.get(0x4);
		int arg = 1028-160;
		machine.setAccum(250);
		// if offset1 = dataCopy[offsetinit+1028-160] = dataCopy[1068] = 10*(68-24) = 440
		// should subtract dataCopy[offsetinit+offset1] = dataCopy[640] = 6400-10240 from the accumulator	
		// -3840
		instr.execute(arg, INDIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250+3840, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// Test whether SUB throws exception with null addressing mode
	public void testSUBnullArg() {
		Model.Instruction instr = machine.get(0x4);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in SUB instruction", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is immediate
	public void testMULimmediate() {
		Model.Instruction instr = machine.get(0x5);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, IMMEDIATE); 
		// should have multiplied the accumulator by 12
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(200*12, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is direct
	public void testMULdirect() {
		Model.Instruction instr = machine.get(0x5);
		int arg = 12; 
		machine.setAccum(250);
		// should multiply the accumulator by dataCopy[offsetinit+12] = dataCopy[212] = -10240 + 2120
		instr.execute(arg, DIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250*(-10240+2120), machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the multiplication is done correctly, when
	// addressing is indirect
	public void testMULindirect() {
		Model.Instruction instr = machine.get(0x5);
		int arg = 1028-160;
		machine.setAccum(250);
		// if offset1 = dataCopy[offsetinit+1028-160] = dataCopy[1068] = 10*(68-24) = 440
		// should multiply the accumulator by dataCopy[offsetinit+offset1] = dataCopy[640] = 6400-10240	
		// -3840
		instr.execute(arg, INDIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(250*(-3840), machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// Test whether MUL throws exception with null addressing mode
	public void testMULnullArg() {
		Model.Instruction instr = machine.get(0x5);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in MUL instruction", exception.getMessage());

	}
	
	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is immediate
	public void testDIVimmediate() {
		Model.Instruction instr = machine.get(0x6);
		int arg = 12; 
		machine.setAccum(200);
		instr.execute(arg, IMMEDIATE); 
		// should have multiplied the accumulator by 12
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(200/12, machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is direct
	public void testDIVdirect() {
		Model.Instruction instr = machine.get(0x6);
		int arg = 12; 
		machine.setAccum(1024011);
		// should divide the accumulator by dataCopy[offsetinit+12] = dataCopy[212] = -10240 + 2120
		instr.execute(arg, DIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1024011/(-10240+2120), machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// this test checks whether the division is done correctly, when
	// addressing is indirect
	public void testDIVindirect() {
		Model.Instruction instr = machine.get(0x6);
		int arg = 1028-160;
		machine.setAccum(400000);
		// if offset1 = dataCopy[offsetinit+1028-160] = dataCopy[1068] = 10*(68-24) = 440
		// should divide the accumulator by dataCopy[offsetinit+offset1] = dataCopy[640] = 6400-10240	
		// -3840
		instr.execute(arg, INDIRECT); 
		// should have added 12 to accumulator
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(400000/(-3840), machine.getAccum(), "Accumulator modified")
				);
	}

	@Test 
	// Test whether DIV throws exception with null addressing mode
	public void testDIVnullArg() {
		Model.Instruction instr = machine.get(0x6);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in DIV instruction", exception.getMessage());

	}
	
	@Test 
	// Test whether DIV throws divide by zero exception with immediate addressing mode
	public void testDIVzerodivisionImmed() {
		Model.Instruction instr = machine.get(0x6);
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Test whether DIV throws divide by zero exception with direct addressing mode
	public void testDIVzerodivisionDirect() {
		Model.Instruction instr = machine.get(0x6);
		// cpu.memoryBase = 200
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> instr.execute(824, DIRECT));		
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Test whether DIV throws divide by zero exception with indirect addressing mode
	public void testDIVzerodivisionIndirect() {
		Model.Instruction instr = machine.get(0x6);
		machine.setData(100+offsetInit, 1024-offsetInit);
		// cpu.memoryBase = 200
		Throwable exception = assertThrows(DivideByZeroException.class,
				() -> instr.execute(100, INDIRECT));		
		assertEquals("Divide by Zero", exception.getMessage());

	}

	@Test 
	// Check AND when accum and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(0);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccLT0argEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(-1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum > 0 and arg equal to 0 gives false
	// addressing is immediate
	public void testANDimmediateAccGT0argEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum = 0 and arg < 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = -1;
		machine.setAccum(0);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 and arg < 0 gives true
	// addressing is immediate
	public void testANDimmediateAccLT0argLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = -1;
		machine.setAccum(-1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum = 0 and arg > 0 gives false
	// addressing is immediate
	public void testANDimmediateAccEQ0argGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1;
		machine.setAccum(0);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum > 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccGT0argGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 0;
		machine.setAccum(1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccLT0argGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1;
		machine.setAccum(-1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum > 0 and arg > 0 gives true
	// addressing is immediate
	public void testANDimmediateAccGT0argLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = -1;
		machine.setAccum(1);
		instr.execute(arg, IMMEDIATE); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}
	
	@Test 
	// Check AND when accum direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024-offsetInit; 
		machine.setAccum(0);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccLT0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024-offsetInit; 
		machine.setAccum(-1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}
	
	@Test
	// Check AND when accum > 0 direct mem equal to 0 gives false
	// addressing is direct
	public void testANDdirectAccGT0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024-offsetInit; 
		machine.setAccum(1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum = 0 direct mem < 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 100; 
		machine.setAccum(0);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 direct mem < 0 gives true
	// addressing is direct
	public void testANDdirectAccLT0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 100; 
		machine.setAccum(-1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum > 0 direct mem < 0 gives true
	// addressing is direct
	public void testANDdirectAccGT0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 100; 
		machine.setAccum(1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum = 0 direct mem > 0 gives false
	// addressing is direct
	public void testANDdirectAccEQ0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1030-offsetInit;
		machine.setAccum(0);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 direct mem > 0 gives true
	// addressing is direct
	public void testANDdirectAccLT0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1030-offsetInit;
		machine.setAccum(-1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum > 0 direct mem > 0 gives true
	// addressing is direct
	public void testANDdirectAccGT0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1030-offsetInit;
		machine.setAccum(1);
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetInit;
		machine.setAccum(0);
		machine.setData(offsetInit, 0);
		dataCopy[offsetInit] = 0;
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccLT0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetInit;
		machine.setAccum(-1);
		machine.setData(offsetInit, 0);
		dataCopy[offsetInit] = 0;
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum > 0 indirect mem equal to 0 gives false
	// addressing is indirect
	public void testANDindirectAccGT0memEQ0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1024 - offsetInit;
		machine.setAccum(1);
		machine.setData(offsetInit, 0);
		dataCopy[offsetInit] = 0;
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum = 0 indirect mem < 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetInit;
		machine.setAccum(0);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 indirect mem < 0 gives true
	// addressing is indirect
	public void testANDindirectAccLT0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetInit;
		machine.setAccum(-1);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum > 0 indirect mem < 0 gives true
	// addressing is indirect
	public void testANDindirectAccGT0memLT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1020 - offsetInit;
		machine.setAccum(1);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum = 0 indirect mem > 0 gives false
	// addressing is indirect
	public void testANDindirectAccEQ0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1200 - offsetInit;
		machine.setAccum(0);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check AND when accum < 0 indirect mem > 0 gives true
	// addressing is indirect
	public void testANDindirectAccLT0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1200 - offsetInit;
		machine.setAccum(-1);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check AND when accum > 0 indirect mem > 0 gives true
	// addressing is indirect
	public void testANDindirectAccGT0memGT0() {
		Model.Instruction instr = machine.get(0x7);
		int arg = 1200 - offsetInit;
		machine.setAccum(1);
		instr.execute(arg, INDIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Test whether AND throws exception with null addressing mode
	public void testANDnullArg() {
		Model.Instruction instr = machine.get(0x7);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in AND instruction", exception.getMessage());

	}
	
	@Test 
	// Check NOT greater than 0 gives false
	// there is no argument and mode is null
	public void testNOTaccGT0() {
		Model.Instruction instr = machine.get(0x8);
		machine.setAccum(1);
		instr.execute(0, null); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check NOT equal to 0 gives true
	// there is no argument and mode is null
	public void testNOTaccEQ0() {
		Model.Instruction instr = machine.get(0x8);
		machine.setAccum(0);
		instr.execute(0, null); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check NOT less than 0 gives false
	// there is no argument and mode is null
	public void testNOTaccLT0() {
		Model.Instruction instr = machine.get(0x8);
		machine.setAccum(-1);
		instr.execute(0, null); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Test whether NON throws exception with immediate addressing mode
	public void testNOimmediateMode() {
		Model.Instruction instr = machine.get(0x8);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Illegal Mode in NOT instruction", exception.getMessage());
	}

	@Test 
	// Test whether NON throws exception with direct addressing mode
	public void testNOTdirectMode() {
		Model.Instruction instr = machine.get(0x8);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, DIRECT));		
		assertEquals("Illegal Mode in NOT instruction", exception.getMessage());
	}
	
	@Test 
	// Test whether NON throws exception with immediate addressing mode
	public void testNOTindirectMode() {
		Model.Instruction instr = machine.get(0x8);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, INDIRECT));		
		assertEquals("Illegal Mode in NOT instruction", exception.getMessage());
	}

	@Test 
	// Check CMPL when comparing less than 0 gives true
	// addressing is direct
	public void testCMPLdirectMemLT0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 100;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check CMPL when comparing grater than 0 gives false
	// addressing is direct
	public void testCMPLdirectMemGT0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 1024;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPL when comparing equal to 0 gives false
	// addressing is direct
	public void testCMPLdirectMemEQ0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 1024 - offsetInit ;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPL when comparing less than 0 gives true
	// addressing is indirect
	public void testCMPLindirectMemLT0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 850;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) < 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check CMPL when comparing greater than 0 gives false
	// addressing is indirect
	public void testCMPLindirectMemGT0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 950;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) > 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPL when comparing equal to 0 gives false
	// addressing is indirect
	public void testCMPLindirectMemEQ0() {
		Model.Instruction instr = machine.get(0x9);
		int arg = 1024 - offsetInit;
		machine.setData(offsetInit, 0);
		dataCopy[offsetInit] = 0;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) == 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Test whether CMPL throws exception with null addressing mode
	public void testCMPLnullMode() {
		Model.Instruction instr = machine.get(0x9);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in CMPL instruction", exception.getMessage());
	}

	@Test 
	// Test whether CMPL throws exception with immediate addressing mode
	public void testCMPLimmediateMode() {
		Model.Instruction instr = machine.get(0x9);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Illegal Mode in CMPL instruction", exception.getMessage());
	}
	
	@Test 
	// Check CMPZ when comparing less than 0 gives false
	// addressing is direct
	public void testCMPZdirectMemLT0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 100;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPZ when comparing grater than 0 gives false
	// addressing is direct
	public void testCMPZdirectMemGT0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 1024;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPZ when comparing equal to 0 gives true
	// addressing is direct
	public void testCMPZdirectMemEQ0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 1024 - offsetInit ;
		instr.execute(arg, DIRECT); 
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Check CMPL when comparing less than 0 gives false
	// addressing is indirect
	public void testCMPZindirectMemLT0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 850;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) < 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPZ when comparing greater than 0 gives false
	// addressing is indirect
	public void testCMPZindirectMemGT0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 950;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) > 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator 0")
				);
	}

	@Test 
	// Check CMPZ when comparing equal to 0 gives true
	// addressing is indirect
	public void testCMPZindirectMemEQ0() {
		Model.Instruction instr = machine.get(0xa);
		int arg = 1024 - offsetInit;
		machine.setData(offsetInit, 0);
		dataCopy[offsetInit] = 0;
		instr.execute(arg, INDIRECT); 
		assertAll (
				() -> {
					int index = machine.getData(arg+offsetInit);
					assertTrue(machine.getData(index+offsetInit) == 0); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(1, machine.getAccum(), "Accumulator 1")
				);
	}

	@Test 
	// Test whether CMPZ throws exception with null addressing mode
	public void testCMPZnullMode() {
		Model.Instruction instr = machine.get(0xa);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, null));		
		assertEquals("Illegal Mode in CMPZ instruction", exception.getMessage());
	}

	@Test 
	// Test whether CMPZ throws exception with immediate addressing mode
	public void testCMPZimmediateMode() {
		Model.Instruction instr = machine.get(0xa);
		Throwable exception = assertThrows(IllegalArgumentException.class,
				() -> instr.execute(0, IMMEDIATE));		
		assertEquals("Illegal Mode in CMPZ instruction", exception.getMessage());
	}
	
	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is immediate
	public void testJUMPimmediate() {
		Model.Instruction instr = machine.get(0xb);
		int arg = 260;  
		instr.execute(arg, IMMEDIATE); 
		// should set the instruction pointer to 260
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(260 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is direct
	public void testJUMPdirect() {
		Model.Instruction instr = machine.get(0xb);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, DIRECT); 
		// should set the instruction pointer to 400
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(400 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JUMP is done correctly, when
	// addressing is indirect
	public void testJUMPindirect() {
		Model.Instruction instr = machine.get(0xb);
		int arg = 910; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, INDIRECT); 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		assertAll (
				() -> {
					int index =  machine.getData(offsetInit + 910);
					assertTrue(machine.getData(index+offsetInit) == 360); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(360 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the non-relative JUMP is done correctly, when
	// addressing is not relative to current instruction pointer
	public void testJUMPnonrelative() {
		Model.Instruction instr = machine.get(0xb);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, null); 
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(400 + 777, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is immediate
	public void testJMPZimmediate() {
		Model.Instruction instr = machine.get(0xc);
		machine.setAccum(0);
		int arg = 260;  
		instr.execute(arg, IMMEDIATE); 
		// should set the instruction pointer to 260
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(260 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is direct
	public void testJMPZdirect() {
		Model.Instruction instr = machine.get(0xc);
		machine.setAccum(0);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, DIRECT); 
		// should set the instruction pointer to 400
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(400 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ is done like JUMP when accumulator is 0
	// addressing is indirect
	public void testJMPZindirect() {
		Model.Instruction instr = machine.get(0xc);
		machine.setAccum(0);
		int arg = 910; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, INDIRECT); 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		assertAll (
				() -> {
					int index =  machine.getData(offsetInit + 910);
					assertTrue(machine.getData(index+offsetInit) == 360); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(360 + ipInit, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the non-relative JMPZ is done like JUMP when accumulator is 0
	// addressing is not relative to current instruction pointer
	public void testJMPZnonrelative() {
		Model.Instruction instr = machine.get(0xc);
		machine.setAccum(0);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, null); 
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer modified
				() -> assertEquals(400 + 777, machine.getInstrPtr(), "Instruction pointer modified"),
				//Test accumulator modified
				() -> assertEquals(0, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is immediate
	public void testJMPZimmediateAccNZ() {
		Model.Instruction instr = machine.get(0xc);
		int arg = 260;  
		instr.execute(arg, IMMEDIATE); 
		// should set the instruction pointer to 260
		assertAll (
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is direct
	public void testJMPZdirectAccNZ() {
		Model.Instruction instr = machine.get(0xc);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, DIRECT); 
		// should set the instruction pointer to 400
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the relative JMPZ only increments instruction pointer
	// addressing is indirect
	public void testJMPZindirectAccNZ() {
		Model.Instruction instr = machine.get(0xc);
		int arg = 910; // the memory value is data[offsetinit-160 + 1024] = 400  
		instr.execute(arg, INDIRECT); 
		// if index = data[offsetinit + 910] = 860
		// then the memory value is data[offsetinit + 860] = data[1060] = 360
		assertAll (
				() -> {
					int index =  machine.getData(offsetInit + 910);
					assertTrue(machine.getData(index+offsetInit) == 360); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

	@Test 
	// this test checks whether the non-relative JMPZ only increments instruction pointer
	// addressing is not relative to current instruction pointer
	public void testJMPZnonrelativeAccNZ() {
		Model.Instruction instr = machine.get(0xc);
		int arg = 1024-160; // the memory value is data[offsetinit-160 + 1024] = 400  
		Job job = machine.getCurrentJob();
		job.setStartcodeIndex(777);
		instr.execute(arg, null); 
		assertAll (
				() -> {
					assertTrue(machine.getData(1024-160+offsetInit) == 400); 
				},
				//Test machine is not changed
				() -> assertArrayEquals(dataCopy, machine.getData()),
				//Test instruction pointer incremented
				() -> assertEquals(ipInit+1, machine.getInstrPtr(), "Instruction pointer incremented"),
				//Test accumulator modified
				() -> assertEquals(accInit, machine.getAccum(), "Accumulator was not changed")
				);
	}

}

