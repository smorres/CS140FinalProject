package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static project.Model.Mode.*;

public class Assembler {
	private boolean readingCode = true;
	
	private String makeOutputCode(String[] parts) {
		int opcode = Model.OPCODES.get(parts[0]);
		if(parts.length == 1) return Integer.toHexString(opcode).toUpperCase() + " 0 0";
		else {
			Model.Mode mode = DIRECT; // the default mode is DIRECT
			if(parts[1].startsWith("#")) {
				mode = IMMEDIATE;
				parts[1] = parts[1].substring(1);
			}
			if(parts[1].startsWith("@")) {
				//TODO same as "#" case but mode = INDIRECT
				mode = INDIRECT;
				parts[1] = parts[1].substring(1);
			}
			if(parts[1].startsWith("&")) {
			//TODO same as "#" case but mode = null
				mode = null;
				parts[1] = parts[1].substring(1);
			}
			int indirLvl = 0;
			if(mode != null) {
				indirLvl = Code.MODE_NUMBER.get(mode);
			}			
			return Integer.toHexString(opcode).toUpperCase() + " " + indirLvl + " " + parts[1];
		}
	}
	
	public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors) {
		// START OF METHOD
		Map<Boolean, List<String>> lists = null;
		try (Stream<String> lines = Files.lines(Paths.get(inputFileName))) {
			lists = lines
				.map(line -> line.trim()) //remove any spaces or tabs from the end (or start) of the file
				.filter(line -> line.length() > 0) // remove blank lines at end of file
				.peek(line -> {if(line.equals("DATA")) readingCode = false;}) // change flag if DATA appears
				.filter(line -> !line.equals("DATA")) // discard the "DATA" line from the stream
				.collect(Collectors.partitioningBy(line -> readingCode)); // store separately for 
				// readingCode = true and readingCode = false 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(lists.get(true));
		System.out.println(lists.get(false));
		
		// REST OF METHOD
		try (PrintWriter output = new PrintWriter(outputFileName)){
			lists.get(true).stream() // get the code lines
			.map(line -> line.split("\\s+")) // split into an array of either one or two strings
			.map(this::makeOutputCode).forEach(output::println); // turn that into output format and save to the file 
			// note this:: is used in the method reference for an instance method in the class
			// for a static method it would be Assembler::methodName
			output.println(-1); // signal replacing "DATA" to separate code from data
			
			// now just save the data as-is since the file is assumed to be in correct format
			lists.get(false).stream().forEach(output::println);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		return 0;
	}

}
