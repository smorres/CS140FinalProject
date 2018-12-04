  package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;

public class FullAssembler extends Assembler {
	@Override
	public int assemble(String inputFileName, String outputFileName, TreeMap<Integer, String> errors) {
		if(errors == null) throw new IllegalArgumentException("Coding error: the error map is null");
		
		//we return the last line number we found an error on. you will have to update it
		//to the right value as you add error messages to the TreeMap errors, which you should see is passed
		//in as the 3rd parameter to this assemble method.
		int retVal = 0;

		//each time grab a line from the scanner, we increase lineNumber by 1.
		//this way lineNum is always equal to the lineNumber the line comes from in the file
		int lineNum = 0;

		//every pasm file starts wtih code, so we initially have inCode equal to true
		//note that as soon as we see DATA, everything following it
		//must be some DATA entry, as the pasm file is expected to be that way.

		//here is a small example to showcase what we mean
		//note the DATA delimeter is optional, it may be that the program does not load any data into memory

		/*
		ADD 8 <---- code
		SUB 4 <---- code
		DATA <----- DATA delimeter (everything before the delimeter must be Code, everything after the delimeter must be Data)
		0 8 <------ data
		1 5 <------ data
		*/

		//boolean meant to track whether we are reading code or data.
		//we set it to false if we see a DATA delimeter
		boolean inCode = true;

		//stuff for book keeping information about blankLines
		boolean blankLineFound = false;
		int firstBlankLineNum = 0;
		
		List<String> code = new ArrayList<>();
		List<String> data = new ArrayList<>();

		//handling the first pass.
		//in this pass, we will account for illegal white space, blank line errrors,
		//and errors associated with the DATA delimeter

		//note a blankLine is only illegal if it is followed by a non-blank line later on.
		//this means blankLines at the end of the file are considered legal!

		//we will also separate lines into code and data lists, so we can
		//check errors specific to code and data in our second pass
		try (Scanner input = new Scanner(new File(inputFileName))) {
			//this is the first pass
			while(input.hasNextLine()) {
				lineNum++;
				String line = input.nextLine();
				//if you are at a blank line (line.trim().length() == 0
				// and you have not found a blank line yet
				//then we keep track of the information associated with the blank line 
				if(line.trim().length() == 0 && !blankLineFound){
					blankLineFound = true;
					firstBlankLineNum = lineNum;
					//set blankLinefound to true
					//and set firstBlankLineNum equal to the current lineNum.
				}

				//check for errors associated with non blank lines in this if block
				else if(line.trim().length() > 0) {
					if(line.charAt(0) == ' ' || line.charAt(0) == '\t') {
						errors.put(lineNum, "error: illegal whitespace");
					}
					//look for illegal white space.
					//this occurs if line starts with a space or tab.
					//use charAt(0) to look at the first character of line
					//and check if it is a space '' or tab '\t'

					//if we have found a blankLine before this current non-blank line
					//then the blankLine found earlier is an Illegal Blank Line
					if(blankLineFound) {
						errors.put(firstBlankLineNum, "error: illegal blank line");
						blankLineFound = false;
						//report the error at the firstBlankLineNum, not the current lineNum. 
						//after reporting this error, set blankLineFound back to false 
						//so that we can search for other illegal blank lines if they are present.
					}
					//check if this non-blank line starts with the DATA delimeter 
					//we do the upper case to ensure we see DATA even if its not all uppercase
					if(line.trim().toUpperCase().contains("DATA")){
						if(inCode) {
							inCode = false;
						}
						
						else {
							errors.put(lineNum, "error: duplicate DATA delimeter");
						}
						
						if(!line.trim().toUpperCase().equals("DATA")) {
							errors.put(lineNum, "error: contains extra material on the line");
							inCode = true;
						}
						
						else if(!line.trim().equals("DATA")) {
							errors.put(lineNum, "error: 'data' should be uppercase");
							inCode = true;
						}
						//if we are in code, set the boolean inCode to false
						//else, report a duplicate DATA delimeter error
						//(observe the only way inCode could be false is if we already found a line with DATA). hopefully this justifies the else


						//if the trimmed line does not equal DATA without uppercasing it
						//then we report an error, saying the DATA delimeter must be all uppercase
					}
					
					else {
						errors.put(lineNum, "error: DATA delimeter must be all uppercase");
					}
				}
				
				if(inCode) {
					code.add(line.trim());
				}
				
				else {
					data.add(line.trim());
				}
				//if we are in code, then add line.trim() to the code arraylist
				//else add line.trim() to the data arraylist
				//note we add the trim of the line so that we don't have to deal with illegal whitespace
				//we already found and reported that to the user if it occurred in this first pass
			} 

			//this is the second pass

			//reset lineNum to zero, we are going to parse from the beginning of the file again.
			
			lineNum = 0;
			
			//now we find code specific errors
			for(String line : code){
				lineNum++;
				//we only want to look for errors on a non-blank line
				if(line.length() > 0){
					//we start by splitting the line of code into its parts
					//note that parts[0] is the MNEMONIC of the instruction, such as ADD
					//parts[1] (if it is present) will be the argument to the instruction

					//it is important to remember that some instructions will take
					//arguments, while some instructions do not take any arguments
					String[] parts = line.split("\\s+");
					
					//check that parts[0] is a valid mnemonic
					//you can do this by using the map OPCODES from Model.
					//if OPCODES.containsKey() returns true when you pass
					//in parts[0].toUpperCase(), then parts[0] is an valid mnemonic.
					if(Model.OPCODES.containsKey(parts[0].toUpperCase())) {
						//check if the valid mnemonic is all uppercase
						//if it is not, report an error message saying the mnemonic must be upper case
						



						//next, we check if the mnemonic takes no arguments 
						if(Model.NO_ARG_MNEMONICS.contains(parts[0])){
							errors.put(lineNum, "error: instruction was given an argument");
							//but now, if this instruction was given an argument
							//report an error saying the mnemonic cannot take arguments
							//you can check to see if an argument was provided by checking if 
							//parts.length is greater than 1
						}
						//otherwise, we have an instruction that must take
						//exactly 1 argument
						else{
							//report if the mnemonic has too many arguments (parts.length > 2)
							if(parts.length > 2){
								errors.put(lineNum, "error: mnemonic has too many args");

							}
							//in an else if, check and report whether the 
							//mnemonic has too few arguments (parts.length < 2)
							else if(parts.length < 2){
								errors.put(lineNum, "error: mnemonic has too few args");

							}
							//othwerise, we need to check the mode provided is allowed
							//for the given instruction
							//and also need to check that the argument provided 
							//is a valid hexadecimal number
							else{
								//check for immediate mode (this if block is completed for you!)
								if(parts[1].charAt(0) == '#'){
									//need to remove that special char so we have the argument
									parts[1] = parts[1].substring(1);
									//we are told to make this set in Model, see part 2 number 6 in the steps.
									if(!Model.IND_MNEMONICS.contains(parts[0])) {
										errors.put(lineNum, "Error on line " + lineNum + 
												": instruction does not allow immediate addressing");
										retVal = lineNum;
									}
								}
								//check for '@' for indirect mode.
								//all intructions can use this mode
								//however, we still need to remove that '@' char from 
								//parts[1], as we did similiarly in the '#' block
								else if(parts[1].charAt(0) == '@'){
									parts[1] = parts[1].substring(1);
									
									if(!Model.IND_MNEMONICS.contains(parts[0])) {
										errors.put(lineNum, "Error on line " + lineNum + 
												": instruction does not allow immediate addressing");
										retVal = lineNum;
									}
								}
								//check for '&' for the special "null" mode of JUMP and JMPZ
								else if(parts[1].charAt(0) == '&'){
									//update parts[1] so it removes the '&' at the beginning
									parts[1] = parts[1].substring(1);
									
									if(!Model.JMP_MNEMONICS.contains(parts[0])) {
										errors.put(lineNum, "Error on line " + lineNum + 
												": instruction does not allow jump addressing");
										retVal = lineNum;
									}

									//similar to the check in the '#' block above for IND_MNEMONICS,
									//if the mnemonic is not contained in Model.JMP_MNEMONICS
									//report an error saying the instruction doesn't allow jump addressing
								}

								//now, we check that the argument provided to the instruction is a valid hexadecimal number.
								//we do this for you so you can see what it looks like.
								//you will need to do this again for the data lines later on below!
								try{
									//try to convert parts[1] to hexadecimal, base 16
									Integer.parseInt(parts[1], 16);
								} catch(NumberFormatException e) {
									errors.put(lineNum, "Error on line " + lineNum + 
											": argument is not a hex number: " + parts[1]);
									retVal = lineNum;
								}
							}
						}
					} /*this is the end of the valid mnemonic error checks*/
					
					//otherwise report an invalid mnemonic error
					else{
						errors.put(lineNum, "error: invalid mnemonic");

					}
				}/*end of line.length() > 0 block*/
			}/*end of code specific errors*/

			//now we find data specific errors
			for(String line : data){
				lineNum++;
				//we only want to look for errors on a non-blank line 
				//that is not a DATA delimeter
				if(line.length() > 0 && !line.toUpperCase().contains("DATA")){
					String[] parts = line.split("\\s+");
					//there are only three things you need to check here.
					//recall that a DATA entry should just be two hexidecimal numbers

					//if parts.length is not 2, report an error
					//saying data entry does not consist of two numbers.
					if(parts.length != 2){
						errors.put(lineNum, "data entry must consist of two numbers");

					}
					//othwerise we need to make sure parts[0] and parts[1] are hexidecimal
					else{
						//using the try catch block as we did above, try to convert parts[0] to a hexadecimal number
						//if it fails, the error message is that the data's memory address in not a hex number	
						try{
							Integer.parseInt(parts[0], 16);
						} catch(NumberFormatException e) {
							errors.put(lineNum, "Error on line " + lineNum + 
									": argument is not a hex number: " + parts[0]);
							retVal = lineNum;
						}

						//using the try catch block as we did above, try to convert parts[1] to hex
						//if it fails, the error message is that the data's value is not a hex number
						
						try{
							//try to convert parts[1] to hexadecimal, base 16
							Integer.parseInt(parts[1], 16);
						} catch(NumberFormatException e) {
							errors.put(lineNum, "Error on line " + lineNum + 
									": argument is not a hex number: " + parts[1]);
							retVal = lineNum;
						}
					}
				}
			}

		} /*end of massive try block where we did all the error checking*/ 
		catch (FileNotFoundException e) {
			errors.put(-1, "Unable to open the input file");
		}

		//finally, if we found no errors, then we call the assemble method from our parent to actual assemble the pasm file
		//to an executable pexe file.
		if(errors.size() == 0) {
			super.assemble(inputFileName, outputFileName, errors);
		}
		return retVal;
	}
}