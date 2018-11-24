package project;

import java.util.Map;
import static project.Model.Mode.*;

public class Code {
	public static final int CODE_MAX = 1024;
	private long[] code = new long[CODE_MAX];
	public static Map<Model.Mode, Integer> MODE_NUMBER = Map.of(IMMEDIATE, 1, DIRECT, 2, INDIRECT, 3);
	public static Map<Integer, Model.Mode> NUM_MODE = Map.of(1, IMMEDIATE, 2, DIRECT, 3, INDIRECT);

	public void setCode(int index, int op, Model.Mode mode, int arg) {
		// the opcode will use 29 bits, multiplying by
		// 8 moves 3 bits to the left
		long longOp = op * 8;
		// put the indirection level in those last 3 bits
		int modeNum = 0;
		if (mode != null)
			modeNum = MODE_NUMBER.get(mode);
		longOp += modeNum;
		long longArg = arg;
		// move the opcode and indirLvl to the upper 32 bits
		long OpAndArg = longOp << 32;
		// if arg was negative, longArg will have 32 leading 1s,
		// remove them:
		longArg = longArg & 0x00000000FFFFFFFFL;
		// join the upper 32 bits and the lower 32 bits
		code[index] = OpAndArg | longArg;
	}

	public int getOp(int i) {
		// move upper half to the lower half discarding lower half
		// and the 3 bit of the indirLvl
		return (int) (code[i] >> 35);
	}

	public Model.Mode getMode(int i) {
		// move upper half to the lower half discarding lower half
		// then get last 3 bits
		int modeNum = (int) (code[i] >> 32) % 8;
		if (modeNum == 0)
			return null;
		return NUM_MODE.get(modeNum);
	}

	public int getModeNumber(int i) {
		// move upper half to the lower half discarding lower half
		// then get last 3 bits
		return (int) (code[i] >> 32) % 8;
	}

	public int getArg(int i) {
		// cut out upper half keeping lower half
		return (int) (code[i] & 0x00000000FFFFFFFFL);
	}

	public void clear(int start, int end) {
		for (int i = start; i < end; i++) {
			code[i] = 0L;
		}
	}

	public String getHex(int i) {
		int arg = getArg(i);
		if (arg >= 0) {
			return Integer.toHexString(getOp(i)).toUpperCase() + " "
					+ Integer.toHexString(getModeNumber(i)).toUpperCase() + " "
					+ Integer.toHexString(arg).toUpperCase();
		}
		return Integer.toHexString(getOp(i)).toUpperCase() + " " + Integer.toHexString(getModeNumber(i)).toUpperCase()
				+ " -" + Integer.toHexString(-arg).toUpperCase();
	}

	public String getText(int i) {
		StringBuilder builder = new StringBuilder();
		String mnem = Model.MNEMONICS.get(getOp(i));
		builder.append(mnem);
		int k = getModeNumber(i);
		switch (k) {
		case 0:
			//TODO This is not working due to the model code
		/*
		 * if(Model.NO_ARG_MNEMONICS.contains(mnem)) { builder.append(" "); } else
		 */ {
			builder.append(" &");
		}
			break;
		case 1:
			builder.append(" #");
			break;
		case 2:
			builder.append(" ");
			break;
		case 3:
			builder.append(" @");
		}
		int arg = getArg(i);
		if (arg >= 0) {
			builder.append(Integer.toHexString(arg).toUpperCase());
		} else {
			builder.append('-');
			builder.append(Integer.toHexString(-arg).toUpperCase());
		}
		return builder.toString();
	}
}
