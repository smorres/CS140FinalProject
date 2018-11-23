package project;

import static project.Model.Mode.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.awt.*;

public class Model {
	

	private final Instruction[] INSTR = new Instruction[16];
	
	CPU cpu = new CPU();
	Data dataMemory = new Data();
	Code codeMemory = new Code();
	HaltCallBack callBack;
	Job[] jobs = new Job[4];
	Job currentJob;

	static enum Mode {
		INDIRECT, DIRECT, IMMEDIATE;
		Mode next() {
			if (this == DIRECT)
				return IMMEDIATE;
			if (this == INDIRECT)
				return DIRECT;
			return null;
		}
	}

	static interface Instruction {
		void execute(int arg, Mode mode);
	}

	static interface HaltCallBack {
		void halt();
	}
	public static final Map<Integer, String> MNEMONICS = Map.ofEntries(
			entry(0, "NOP"), entry(1, "LOD"), entry(2, "STO"), entry(3, "ADD"),
			entry(4, "SUB"), entry(5, "MUL"), entry(6, "DIV"), entry(7, "AND"),
			entry(8, "NOT"), entry(9, "CMPL"), entry(10, "CMPZ"), entry(11, "JUMP"),
			entry(12, "JMPZ"), entry(15, "HALT"));
	
		// NOTE THERE IS A DELIBERATE GAP for 13 and 14
	
		public static final Map<String, Integer> OPCODES = Map.ofEntries(
			entry("NOP", 0), entry("LOD", 1), entry("STO", 2), entry("ADD", 3),entry("SUB", 4), entry("MUL", 5), entry("DIV", 6), entry("AND", 7),
			entry("NOT", 8), entry("CMPL", 9), entry("CMPZ", 10), entry("JUMP", 11),
			entry("JMPZ", 12), entry("HALT", 15));
	
		public static final Set<String> NO_ARG_MNEMONICS = Set.of("NOP", "NOT", "HALT"); 
	
			static class CPU {
		private int accumulator, instructionPointer, memoryBase;
	};
	
	public Model() {
		this(() -> System.exit(0));
	}



	public Model(HaltCallBack cb) {
		callBack = cb;
		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new Job();
			jobs[i].setId(i);
			jobs[i].setStartcodeIndex(i * Code.CODE_MAX / 4);
			jobs[i].setStartmemoryIndex(i * Data.DATA_SIZE / 4);
			jobs[i].getCurrentState().enter();
		}
		currentJob = jobs[0];
		/*
		 * ADD AND CMPL CMPZ DIV HALT JMPZ JUMP LOD MUL NOP NOT STO SUB
		 */
		INSTR[0x0] = (arg, mode) -> {
			if (mode != null)
				throw new IllegalArgumentException("Illegal Mode in NOP instruction");
			cpu.instructionPointer++;
		};
		INSTR[0x1] = (arg, mode) -> {
			// LOD
			if (mode == null)
				throw new IllegalArgumentException("Illegal Mode in LOD instruction");
			if (mode != IMMEDIATE)
				INSTR[0x1].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			else {
				cpu.accumulator = arg;
				cpu.instructionPointer++;

			}
		};
		INSTR[0x2] = (arg, mode) -> {
			// STO
			if (mode == null || mode == IMMEDIATE) {
				throw new IllegalArgumentException("Illegal Mode in STO instruction");
			}
			if (mode != DIRECT) {
				INSTR[0x2].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				dataMemory.setData(cpu.memoryBase + arg, cpu.accumulator);
				cpu.instructionPointer++;
			}
		};
		INSTR[0x3] = (arg, mode) -> {
			// ADD
			if (mode == null) {
				throw new IllegalArgumentException("Illegal Mode in ADD instruction");
			}
			if (mode != IMMEDIATE) {
				INSTR[0x3].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator += arg;
				cpu.instructionPointer++;
			}
		};
		INSTR[0x4] = (arg, mode) -> {
			// SUB
			if (mode == null) {
				throw new IllegalArgumentException("Illegal Mode in SUB instruction");
			}
			if (mode != IMMEDIATE) {
				INSTR[0x3].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator -= arg;
				cpu.instructionPointer++;
			}

		};
		INSTR[0x5] = (arg, mode) -> {
			// MULT
			if (mode == null) {
				throw new IllegalArgumentException("Illegal Mode in MULT instruction");
			}
			if (mode != IMMEDIATE) {
				INSTR[0x3].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				cpu.accumulator *= arg;
				cpu.instructionPointer++;
			}
		};
		INSTR[0x6] = (arg, mode) -> {
			// DIV
			if (mode == null) {
				throw new IllegalArgumentException("Illegal Mode in DIV instruction");
			}
			if (mode != IMMEDIATE) {
				INSTR[0x3].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				if (arg == 0) {
					throw new DivideByZeroException("Divide by Zero");
				}
				cpu.accumulator /= arg;
				cpu.instructionPointer++;
			}
		};
		INSTR[0x7] = (arg, mode) -> {
			// AND
			if (mode == null) {
				throw new IllegalArgumentException("Illegal Mode in AND instruction");
			}
			if (arg != 0 && cpu.accumulator != 0) {
				cpu.accumulator = 1;

			} else {
				cpu.accumulator = 0;
				cpu.instructionPointer++;

			}

		};
		INSTR[0x8] = (arg, mode) -> {
			// NOT
			if (mode != null)
				throw new IllegalArgumentException("Illegal Mode in NOT instruction");
			if (cpu.accumulator != 0) {
				cpu.accumulator = 0;
			} else {
				cpu.accumulator = 1;
				cpu.instructionPointer++;

			}

		};
		INSTR[0x9] = (arg, mode) -> {
			// CMPL
			if (mode == null || mode == IMMEDIATE) {
				throw new IllegalArgumentException("Illegal Mode in CMPL instruction");
			}
			if (mode != DIRECT) {
				INSTR[0x9].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				int arg1 = dataMemory.getData(cpu.memoryBase + arg);
				if (arg1 < 0) {
					cpu.accumulator = 1;
				} else {
					cpu.accumulator = 0;
				}
				cpu.instructionPointer++;

			}
		};
		INSTR[0xa] = (arg, mode) -> {
			// CMPZ
			if (mode == null || mode == IMMEDIATE) {
				throw new IllegalArgumentException("Illegal Mode in CMPL instruction");
			}
			if (mode != DIRECT) {
				INSTR[0xa].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());
			} else {
				int arg1 = dataMemory.getData(cpu.memoryBase + arg);
				if (arg1 == 0) {
					cpu.accumulator = 1;
				} else {
					cpu.accumulator = 0;
				}
				cpu.instructionPointer++;
			}
		};
		INSTR[0xb] = (arg, mode) -> {
			// JUMP
			if (mode == null) {
				int arg1 = dataMemory.getData(cpu.memoryBase + arg);
				cpu.instructionPointer = arg1 + currentJob.getStartcodeIndex();
			} else if (mode != IMMEDIATE) {
				INSTR[0xb].execute(dataMemory.getData(cpu.memoryBase + arg), mode.next());

			} else {
				cpu.instructionPointer = cpu.instructionPointer + arg;
			}
		};
		INSTR[0xf] = (arg, mode) -> {
			callBack.halt();
		};
	}

	public int[] getData() {
		return dataMemory.getData();
	}

	public int getInstrPtr() {
		return cpu.instructionPointer;
	}

	public int getAccum() {
		return cpu.accumulator;
	}

	public Instruction get(int i) {
		return INSTR[i];
	}

	public void setData(int index, int value) {
		dataMemory.setData(index, value);
	}

	public int getData(int index) {
		return dataMemory.getData(index);
	}

	public void setAccum(int accInit) {
		cpu.accumulator = accInit;
	}

	public void setInstrPtr(int ipInit) {
		cpu.instructionPointer = ipInit;
	}

	public void setMemBase(int offsetInit) {
		cpu.memoryBase = offsetInit;
	}

	public Job getCurrentJob() {
		return currentJob;
	}
}
