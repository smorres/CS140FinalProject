package project;

import java.io.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Scanner;
import jdk.*;
public class Loader {
	public static String load(Model model, File file, int codeOffset, int memoryOffset) {
		int codeSize = 0;
		if (model == null || file == null) {
			return null;
		}
		try (Scanner input = new Scanner(file)) {
			boolean incode = true;
			while (input.hasNextLine()) {
				String line = input.nextLine();
				Scanner parser = new Scanner(line);
				int op = parser.nextInt(16);
				int indirLvl = 0;
				int arg = 0;
				if (incode && op == -1) {
					incode = false;
				} else if (incode) {
					indirLvl = parser.nextInt(16);
					arg = parser.nextInt(16);
					Model.Mode mode = null;
					if (indirLvl > 0) {
						mode = Code.NUM_MODE.get(indirLvl);
					}
					model.setCode(codeOffset + codeSize, op, mode, arg);
					codeSize++;
				} else {
					int value = parser.nextInt(16);
					model.setData(memoryOffset + op, value);
				}
				parser.close();
			}
			return "" + codeSize;

		} catch (FileNotFoundException e) {
			return "File " + file.getName() + " Not Found";
		} catch (CodeAccessException e) {
			return "Code Memory Access Exception " + e.getMessage();

		} catch (MemoryAccessException e) {
			return "Data Memory Access Exception " + e.getMessage();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			return "From Scanner: NoSuchElementException";
		}

	}
	public static void main(String[] args) {
		Model model = new Model();
		String s = Loader.load(model, new File("out.pexe"),16,32);
		for(int i = 16; i < 16+Integer.parseInt(s); i++) {
			System.out.println(model.getCodeMemory().getText(i));			
		}
		System.out.println("--");
		System.out.println("4FF " + Integer.toHexString(model.getData(0x20+0x4FF)).toUpperCase());
		System.out.println("0 " + Integer.toHexString(model.getData(0x20)).toUpperCase());
		System.out.println("10 -" + Integer.toHexString(-model.getData(0x20+0x10)).toUpperCase());
	}

}
