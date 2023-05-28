package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import assembler.ErrorHandler.ERROR_TYPE;

/**
 * Contains functionality for and performs the first pass of the assembling
 * algorithm.
 */
public class FirstPass {

    /**
     * Table to keep track of literals and their name, value, and sizes
     */
    private LiteralTable literalTable;
    /**
     * Table to keep track of symbols and their values.
     */
    private SymbolTable symbolTable;
    /**
     * Lookup table for pseudo-operation information.
     */
    private PseudoOpTable pseudoOpTable;
    /**
     * Lookup table for instruction information.
     */
    private MachineOpTable machineOpTable;
    /**
     * Stores the address of an instruction.
     */
    private int locationCounter;
    /**
     * The segment name of the input program.
     */
    private String segmentName;
    /**
     * The segment length of the input program.
     */
    private int segmentLength;
    /**
     * The initial execution address of the input program.
     */
    private short initialExecutionAddress;
    /**
     * The initial load address of the input program.
     */
    private short initialLoadAddress;
    /**
     * Whether the input file is relocatable or not.
     */
    private boolean isRelocatable;
    /**
     * Keeps track of which line of the assembly program is currently being
     * processed to log helpful error messages.
     */
    private int currentLine;
    /**
     * Holds all the symbols defined by the .ENT pseudo-op
     */
    private HashSet<String> entSet;
    /**
     * Holds all the symbols defined by the .EXT pseduo-op
     */
    private HashSet<String> extSet;
    

    /**
     * Constructs a new first pass object with all necessary object references
     * for running a first pass.
     *
     * @param litTable
     *            The literal table.
     * @param symTable
     *            The symbol table.
     * @param psTable
     *            The pseudo-op table.
     * @param mopTable
     *            The machine-op table.
     */

    public FirstPass(LiteralTable litTable, SymbolTable symTable,
            PseudoOpTable psTable, MachineOpTable mopTable) {
        this.literalTable = litTable;
        this.symbolTable = symTable;
        this.pseudoOpTable = psTable;
        this.machineOpTable = mopTable;
        this.locationCounter = 0;
        
        entSet = new HashSet<String>();
        extSet = new HashSet<String>();
        
        
    }

    /**
     * Gets if the input program is relocatable. Calling this method requires a
     * successful call to runFirstPass().
     *
     * @return The segment name of the input program.
     */
    public boolean isRelocatable() {

        return this.isRelocatable;
    }

    /**
     * Gets the segment name of the input program. Calling this method requires
     * a successful call to runFirstPass().
     *
     * @return The segment name of the input program.
     */
    public String getSegmentName() {

        return this.segmentName;
    }

    /**
     * Gets the segment load address of the input program. Calling this method
     * requires a successful call to runFirstPass().
     *
     * @return The segment load address of the input program.
     */
    public short getInitialLoadAddress() {

        return this.initialLoadAddress;
    }

    /**
     * Gets the segment length of the input program. Calling this method
     * requires a successful call to runFirstPass().
     *
     * @return The segment length of the input program.
     */
    public int getSegmentLength() {

        return this.segmentLength;
    }

    /**
     * Gets the initial execution address of the input program. Calling this
     * method requires a successful call to runFirstPass().
     *
     * @return The initial execution address of the input program.
     */
    public short getInitialExecutionAddress() {

        return this.initialExecutionAddress;
    }

    /**
     * Gets the current value of the location counter of the first pass class.
     * 
     * @return The current location counter.
     */

    public int getCurrentLocationCounter() {
        return this.locationCounter;
    }
    
    /**
     * Gets the set of symbols defined by the .ENT pseudo-op.
     *
     * @return The segment name of the input program.
     */
    public HashSet<String> getEntSet() {

        return this.entSet;
    }
    
    /**
     * Gets the set of symbols defined by the .EXT pseudo-op.
     *
     * @return The segment name of the input program.
     */
    public HashSet<String> getExtSet() {

        return this.extSet;
    }

    /**
     * Runs the first-pass algorithm on an input file which updates the symbol
     * table, updates the literal table, along with many internal values which
     * represent the program. This generates an intermediate file to be used for
     * pass2.
     *
     * @param inputFile
     *            An input file for the assembler
     * @return An intermediate file. If the intermediate file is NULL then an
     *         error ocurred.
     */
    public File runFirstPass(File inputFile) {

        //Assume successful until discovered otherwise and start LC at 0
        boolean firstPassSuccess = true;
        //Initialize location counter to 0 by default
        this.locationCounter = 0;
        //Keep current line variable for error logging purposes
        this.currentLine = 0;
        //Make an empty queue for the lines
        Queue<String> lineQueue = new LinkedList<String>();
        //Scanner for the Parser to use
        Scanner fileScanner;
        try {

            /////////////////////////////////////////////////////////// ORIGIN PROCESSING BELOW /////////////////////////////////////////////////////
            fileScanner = new Scanner(inputFile);

            //Check if origin has been found or not.
            boolean foundOrigin = false;

            //Check if origin was parsed correctly.
            boolean successfulOriginParse = false;

            //Check for existence of ORIGIN LINE and process the line by setting segment info/LC info
            while (fileScanner.hasNextLine() && !foundOrigin) {

                //Get the next tokenized line
                String[] originTokenized = Parser.TokenizeLine(fileScanner);

                //Go to the next line
                this.currentLine += 1;

                //Check if the parser failed ran into an error
                if (originTokenized == null) {
                    firstPassSuccess = false;
                    break;
                }
                //Now we know the parser didn't find an error
                else if (originTokenized.length > 0) {

                    //We found the origin
                    foundOrigin = true;
                    //Process that origin
                    successfulOriginParse = this.processOrigin(originTokenized);
                    lineQueue.add(Constants.IM_CODE_INDICATOR  +""+ this.currentLine + " "+
                            Constants.IM_CODE_INDICATOR + "" + this.locationCounter + " "
                            + Parser.getLastTokenizedLine());

                }

            }

            //Origin failed to be processed
            if (!foundOrigin || !successfulOriginParse) {
                firstPassSuccess = false;
            }

            //If the origin wasn't found then log an error
            if (!foundOrigin) {
                ErrorHandler.queueError(ERROR_TYPE.ORIGIN_NOT_FOUND,
                        this.currentLine);
            }
            

            /////////////////////////////////////////////////////////// MAIN LOOP PROCESSING BELOW //////////////////////////////////////////////////////

            //Boolean to check if end was found or not.
            boolean foundEnd = false;

            //Boolean to check if end was parsed correctly.
            boolean successfulEndParse = false;
            
            boolean encounteredFirstStandardOp = false;

            //Loop through all lines in input file while not reaching a .END pseudo-op,
            //or running out of lines in the input file
            while (fileScanner.hasNextLine() && !foundEnd) {

                //Get next tokenized line
                String[] currentTokenizedLine = Parser
                        .TokenizeLine(fileScanner);
                //Increment the line number counter
                this.currentLine += 1;

                //Check if Parser found errors while parsing the line
                if (currentTokenizedLine == null) {
                    firstPassSuccess = false;
                }
                //Otherwise, the parser didn't detect any errors
                else if (currentTokenizedLine.length > 0) {

                    //Add to the line queue
                    lineQueue.add(Constants.IM_CODE_INDICATOR  + "" + this.currentLine + " "+
                            Constants.IM_CODE_INDICATOR + "" + this.locationCounter + " "
                            + Parser.getLastTokenizedLine());

                    
                    if (isExtEntLine(currentTokenizedLine)) {
                    	
                    	if (encounteredFirstStandardOp) {
                        	ErrorHandler.queueError(ERROR_TYPE.EXT_ENT_FOLLOWS_INSTR, this.currentLine);
                        	firstPassSuccess = false;
                    	}
                    	else if (!processExtEnt(currentTokenizedLine)) {
                    		firstPassSuccess = false;
                    	}
                    }
                    	
                    //Exit out of the loop if an .END pseudo-operation is found.
                    else if (this.isEndLine(currentTokenizedLine)) {

                        //Check if the .END line gets parsed correctly
                        successfulEndParse = this
                                .processEnd(currentTokenizedLine);
                        //We found the end
                        foundEnd = true;

                        //First pass isn't a success if end isn't parsed correctly
                        if (!successfulEndParse) {
                            firstPassSuccess = false;
                        }

                    }

                    //Process each line and extract symbol and literal information.
                    else if (this.processLine(currentTokenizedLine)) {
                        encounteredFirstStandardOp = true;
                    }
                    else {
                    	firstPassSuccess = false;
                    }
                }
            }

            //If END wasn't found then queue an error and fail
            if (!foundEnd) {
                ErrorHandler.queueError(ERROR_TYPE.END_NOT_FOUND,
                        this.currentLine);
                firstPassSuccess = false;
            }

            //Done with reading from the file so close the file scanner to prevent memory leakage
            fileScanner.close();

        } catch (FileNotFoundException e) {
            //The operating System had trouble opening the file so fail and give an error
            firstPassSuccess = false;
            ErrorHandler.queueError(ERROR_TYPE.INPUT_FILE_UNOPENABLE,
                    this.currentLine);
        }

        /////////////////////////////////////////////////////////////// PASS 1 FINALIZATIONS BELOW /////////////////////////////////////////////////////////////

        //Update any literal addresses and the segment length and check all .ENT symbols are defined
        if (!this.doPassFinalizations()) {
        	firstPassSuccess = false;
        }
        
        //If the first pass didn't succed then return a failure state
        if (!firstPassSuccess) {
            return null;
        }

        //Generate the intermediate file
        IntermediateFileGenerator imfg = new IntermediateFileGenerator(
                lineQueue, this.isRelocatable, this.entSet, this.symbolTable);
        imfg.loadHeaderInformation(this.segmentName, this.segmentLength,
                this.initialExecutionAddress, this.initialLoadAddress);
        File intermediateFile = imfg.generateIntermediateFile();

        //Return the intermediate file (Null indicates an error)
        return intermediateFile;
    }

    /**
     * Sets the addresses of all literals and calculates the segments length. Additionally,
     * this method checks that all .ENTs are defined in the symbol table.
     * This method assumes the program has been fully and succesfully parsed
     * until the .END pseudo-operation
     * @return
     * 		True if no errors occurr and false if errors occurr.
     */
    public boolean doPassFinalizations() {

    	boolean success = true;
    	
        //Iterate through and set addresses for everything
        ArrayList<String> literalList = this.literalTable.getLiterals();
        for (String s : literalList) {

            //Iterate the location counter and set the literal address for each literal
            this.literalTable.setAddress(s,
                    this.locationCounter + this.initialLoadAddress);
            this.locationCounter += 1;
        }

        //Set the segment length
        this.segmentLength = this.locationCounter;
        
        //Check that all .ENT symbols are defined and relative
        for (String entSymbol : entSet) {
        	//Check that symbol table has symbol
        	if (!symbolTable.hasSymbol(entSymbol)) {
        		
        		ErrorHandler.queueError(ERROR_TYPE.ENT_UNDEFINED, Constants.NON_LINE_ERROR_INDICTATOR);
        		success = false;
        		
        	}
        	//Check that .ENT symbols are relative
            else if (!symbolTable.isRelative(entSymbol)) {
        		ErrorHandler.queueError(ERROR_TYPE.ENT_NOT_RELOCATABLE, Constants.NON_LINE_ERROR_INDICTATOR);
        	}
        }
        
		//Add ext values into the symbol table
        
        for (String extSymbol : extSet) {
        	if (!symbolTable.hasSymbol(extSymbol)) {
        		symbolTable.put(extSymbol, Constants.EXT_DEFAULT_VAL, true);
        	}
        	else {
        		//Otherwise the symbol was already defined in the symbol table (which is an error)
        		ErrorHandler.queueError(ERROR_TYPE.EXT_DEFINED_IN_FILE, Constants.NON_LINE_ERROR_INDICTATOR);
        	}
        }
        
        
        
        return success;
    }

    /**
     * Processes a single line of input with the first-pass algorithm.
     *
     * @param inputTokens
     *            An array of input tokens generated by the parser.
     * @return True if no errors are detected and False if errors are detected.
     */
    public boolean processLine(String[] inputTokens) {

        //Boolean to keep track if errors have been found yet
        boolean success = true;
        	
        // Process a symbol if there is one.
        if (success) {
            success = this.processLabel(inputTokens);
        }

        // Update the location counter if an error hasn't already been found.
        if (success) {
            success = this.updateLocationCounter(inputTokens);
        }

        // Process any literals if an error hasn't already been found.
        if (success) {
            success = this.processLiteral(inputTokens);
        }

        return success;
    }

    /**
     * Updates the location counter based on the current input line in tokenized
     * form. This uses either the machine op table or the symbol op table to get
     * this information. This method also detects a variety of errors including
     * .STRZ, .BLKW, and forward-referencing errors.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if no errors were detected and false if errors were
     *         detected.
     */
    public boolean updateLocationCounter(String[] inputTokens) {

        //Tokenized parts of the input line for use in the method
        String operation = inputTokens[Constants.OPERATION_INDEX];
        String operand = inputTokens[Constants.FIRST_OPERAND_INDEX];

        //Boolean for whether the method encounters an error or not.
        boolean success = true;

        //Check if the operation is in the machine-op table.
        if (!this.machineOpTable.getOpCode(operation)
                .equals(Instructions.INVALID)) {
            /*
             * Get the specific instruction enum and increment the location
             * counter by the size of that instruction from the machine-op table
             */
            Instructions instr = this.machineOpTable.getOpCode(operation);
            this.locationCounter += this.machineOpTable.getSize(instr);
        }
        //Check if the instruction is in the pseudo-op table.
        else if (!this.pseudoOpTable.getPseudoOp(operation)
                .equals(PseudoOps.INVALID)) {

            //Get the instruction
            PseudoOps psInstr = this.pseudoOpTable.getPseudoOp(operation);
            int size = this.pseudoOpTable.getSize(psInstr);

            /*
             * Check if the size is -1 or not. If not then just increase the
             * location counter by the size. If not then more parsing needs to
             * be done.
             */
            if (size != -1) {
                this.locationCounter += size;
            }

            //Do further parsing
            else {

                //Check if the instruction is a block of words
                if (psInstr.equals(PseudoOps.BLKW)) {

                    //Check if the operand is an immediate
                    if (Bits.isLiteralOrImmediate(operand)) {

                        //If the operand is an immediate hex value in [x1, xFFFF] then
                        //Increment the location counter by that value
                        if (Bits.isInValidBLKWRange(operand)) {
                            this.locationCounter += Bits
                                    .parseLiteralOrImmediate(operand);
                        } else {
                            //Not in the range: [x1, xFFFF] so queue error
                            ErrorHandler.queueError(
                                    ERROR_TYPE.BLKW_INVALID_OPERAND_RANGE,
                                    this.currentLine);
                            success = false;
                        }
                    }
                    //Check if the operand is an absolute already defined symbol
                    else if (this.symbolTable.hasSymbol(operand)
                            && !this.symbolTable.isRelative(operand)) {

                        //If the operand is an absolute symbol in the symbol table.
                        //Add the value of the symbol to the location counter
                        int val = this.symbolTable.getVal(operand);
                        this.locationCounter += val;
                    }
                    //Check if the operand is a relative already defined symbol
                    else if (this.symbolTable.hasSymbol(operand)
                            && this.symbolTable.isRelative(operand)) {

                        //If the operand is a relative symbol in the symbol table
                        //then queue an error since this is not allowed.
                        ErrorHandler.queueError(ERROR_TYPE.BLKW_REL_SYMBOL,
                                this.currentLine);
                        success = false;

                    }
                    //Otherwise it is an undefined symbol
                    else {
                        //Symbol used isn't in the symbol table and forward referencing isn't allowed for .BLKW
                        //So queue an error.
                        ErrorHandler.queueError(
                                ERROR_TYPE.BLKW_FORWARD_REFERENCING,
                                this.currentLine);
                        success = false;
                    }
                }

                //Check if the instruction is a string
                else if (psInstr.equals(PseudoOps.STRZ)) {
                    //Check if the string has a starting quotation mark and an ending quotation mark
                    if (operand.length() >= 2 && operand.charAt(0) == '"'
                            && operand.charAt(operand.length() - 1) == '"') {
                        //Update the LC by 1 less than the operand length because you strip the 2 quotation marks off
                        //And then allocate space for the null-termination character at the end
                        this.locationCounter += operand.length() - 1;
                    } else {
                        ErrorHandler.queueError(ERROR_TYPE.INVALID_STRZ_OPERAND,
                                this.currentLine);
                        success = false;
                    }

                }
            }
        }
        //The instruction is neither in the machine-op table nor the pseudo-op table so fail and queue an error
        else {
            success = false;
            ErrorHandler.queueError(ERROR_TYPE.INVALID_INSTRUCTION,
                    this.currentLine);
        }

        return success;

    }

    /**
     * Processes a label by assigning it a value and adding it to the symbol
     * table. This also handles .EQU and a variety of errors related to .EQU
     * along with multiple symbol definition.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if the symbol is processed properly and false otherwise.
     */
    public boolean processLabel(String[] inputTokens) {

        //Tokenized parts of the input line for use in the method
        String label = inputTokens[Constants.LABEL_INDEX];
        String operation = inputTokens[Constants.OPERATION_INDEX];
        String operand = inputTokens[Constants.FIRST_OPERAND_INDEX];

        //The pseudo-op enum that corresponds with the provided operation
        PseudoOps currentOp = this.pseudoOpTable.getPseudoOp(operation);
        //Boolean for whether the method encounters an error or not.
        boolean success = true;

        //Check if there no label when it is not a .EQU operation
        if (label.equals("") && !currentOp.equals(PseudoOps.EQU)) {
            //Imediately return true as there is nothing else to be done
            return true;
        }

        //Check if there is no label and a .EQU operation which is not allowed
        if (label.equals("") && currentOp.equals(PseudoOps.EQU)) {
            ErrorHandler.queueError(ERROR_TYPE.EQU_NO_LABEL, this.currentLine);
            success = false;
        }

        //Check if symbol is already in table which is not allowed
        if (this.symbolTable.hasSymbol(label)) {
            ErrorHandler.queueError(ERROR_TYPE.MULTIPLE_SYMBOL_DEFINITION,
                    this.currentLine);
            success = false;
        }

        //Check if current op is .EQU
        else if (currentOp.equals(PseudoOps.EQU)) {

            //Check if the operand is a constant
            if (Bits.isLiteralOrImmediate(operand)) {

                //Check if the value is in the correct range
                if (Bits.isValidHexStringInFFFFRange(operand)
                        || Bits.isValidDecStringInShortRange(operand)) {
                    this.symbolTable.put(label,
                            Bits.parseLiteralOrImmediate(operand), false);
                } else {
                    //Otherwise queue an error because the value is not in the correct range
                    ErrorHandler.queueError(
                            ERROR_TYPE.EQU_INVALID_CONST_OPERAND_RANGE,
                            this.currentLine);
                    success = false;
                }
            }
            //Check if the operand is a predefined symbol
            else if (this.symbolTable.hasSymbol(operand)) {

                //If the symbol being used is relative than this is relative too and put it in the symbol table
                boolean match = this.symbolTable.isRelative(operand);
                int val = this.symbolTable.getVal(operand);
                this.symbolTable.put(label, val, match);

            } else {
                //Symbol forward-referencing is not allowed for .EQU so queue an error
                ErrorHandler.queueError(ERROR_TYPE.EQU_FORWARD_REFERENCING,
                        this.currentLine);
                success = false;
            }

        }
        //If the label is not being equated then set it to the LC + the initial load address as a relative symbol
        else {
            this.symbolTable.put(label,
                    this.locationCounter + this.initialLoadAddress, true);
        }

        return success;
    }

    /**
     * Process any literals on a line and add to literal table with name, value,
     * and size fields populated. Additionally, this handles literal bound
     * errors and errors when literals are used for non-LD instructions.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if the literal is processed properly and false otherwise.
     */
    public boolean processLiteral(String[] inputTokens) {

        //Boolean for whether the method encounters an error or not.
        boolean success = true;

        //Check if the current line has a literal in it
        if (this.hasLiteral(inputTokens)) {

            //Tokenized parts of the input line for use in the method
            String operation = inputTokens[Constants.OPERATION_INDEX];
            String literal = inputTokens[Constants.SECOND_OPERAND_INDEX];
            //The literal string with the '=' stripped out
            String literalWithoutEquals = literal.substring(1);
            //The instruction enum corresponding to the line's operation
            Instructions instr = this.machineOpTable.getOpCode(operation);

            //If there is a literal then the instruction must be LD. Check for that.
            if (instr.equals(Instructions.LD)) {

                //Check if literal is already in literal table and if not then put it in
                if (!this.literalTable.hasLiteral(literal)) {
                    this.literalTable.put(literal);
                }

            } else {
                //A literal is included in a non-LD instruction so queue an error
                ErrorHandler.queueError(ERROR_TYPE.LITERAL_NOT_LD_INSTR,
                        this.currentLine);
                success = false;
                //Return because we don't care about bound-checking a literal not
                // in an LD instruction.
                return success;
            }
            
            //Check if literal is not in valid range and queue error if not
            if (!Bits.isValidDecStringInShortRange(literalWithoutEquals)
                    && !Bits.isValidHexStringInFFFFRange(
                            literalWithoutEquals)) {
                ErrorHandler.queueError(
                        ERROR_TYPE.LITERAL_INVALID_OPERAND_RANGE,
                        this.currentLine);
                success = false;
            }

        }

        return success;

    }

    /**
     * Process any EXT and ENT pseudo-ops by adding the operands into specified hash sets.
     * Calling this requires a .EXT symbol or .ENT symbol to be the current operation
     * in inputTokens
     * 
     * @param inputTokens
     * 			  Input tokens for the current line
     * @return True if the .EXT, .ENT pseudo-ops were processed properly and false otherwise.
     */
    public boolean processExtEnt(String[] inputTokens) {
    	
        //Tokenized parts of the input line for use in the method
        String label = inputTokens[Constants.LABEL_INDEX];
        String operation = inputTokens[Constants.OPERATION_INDEX];
        String firstOperand = inputTokens[Constants.FIRST_OPERAND_INDEX];
        
        //The pseudo-op enum that corresponds with the provided operation
        PseudoOps currentOp = this.pseudoOpTable.getPseudoOp(operation);
        //Boolean for whether the method encounters an error or not.
        boolean success = true;
        
        //Make sure there is no label for EXT/ENT
        if (!label.equals("")) {
        	ErrorHandler.queueError(ERROR_TYPE.EXT_ENT_LABEL, this.currentLine);
        	success = false;
        }
        
        //Make sure there is at least one operand for EXT/ENT
        if (firstOperand.equals("")) {
        	ErrorHandler.queueError(ERROR_TYPE.EXT_ENT_NO_OPERAND, this.currentLine);
        	success = false;
        }
        
        //If this is an .EXT pseudo-op
        if (currentOp.equals(PseudoOps.EXT)) {
        	
        	for (int i = Constants.FIRST_OPERAND_INDEX; i < inputTokens.length; i++) {
        		if (!extSet.contains(inputTokens[i])) {
        			extSet.add(inputTokens[i]);
        		}
        		//Check if a symbol is defined in a .EXT and .ENT which is not allowed
        		if (entSet.contains(inputTokens[i])) {
        			ErrorHandler.queueError(ERROR_TYPE.BOTH_EXT_ENT, this.currentLine);
        			success = false;
        		}
        		
        	}
        
        }
        //If this is an .ENT pseduo-op
        else {
        	
        	//Add all operands into hashset
        	for (int i = Constants.FIRST_OPERAND_INDEX; i < inputTokens.length; i++) {
        		if (!entSet.contains(inputTokens[i])) {
        			entSet.add(inputTokens[i]);
        		}
        		//Check if a symbol is defined in a .EXT and .ENT which is not allowed
        		if (extSet.contains(inputTokens[i])) {
        			ErrorHandler.queueError(ERROR_TYPE.BOTH_EXT_ENT, this.currentLine);
        			success = false;
        		}
        		
        	}
        } 
        
        return success;
    	
    }
    
    /**
     * Checks if a tokenized line contains an equal sign denoting a literal or
     * not at the start of any of the input tokens.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if the current line has a literal and false otherwise
     */
    public boolean hasLiteral(String[] inputTokens) {

        //Boolean to indicate whether the tokenized line has a literal or not
        boolean hasLiteral = false;

        //Iterate through each token
        for (int i = 0; i < inputTokens.length; i++) {
            //If the specific token has an '=' as the first token then it is a literal
            if (inputTokens[i].length() > 0
                    && inputTokens[i].charAt(0) == '=') {
                hasLiteral = true;
            }
        }

        return hasLiteral;
    }

    /**
     * Checks if a tokenized line is an external symbol line (Containing .ENT or .EXT pseudo-op)
     * 
     * @param inputTokens
     * 			Input tokens for the current line
     * @return True if the current line has a .ENT or .EXT pseudo-op and false otherwise
     */
    public boolean isExtEntLine(String[] inputTokens) {
    	
    	//Boolean to indicate whether the current line is an external-symbol line or not
    	boolean isExternalSymbolLine = false;
    	//Get the operation
        String operation = inputTokens[Constants.OPERATION_INDEX];
        //Get the pseudo-op from the pseudo-op table
        PseudoOps currentOp = this.pseudoOpTable.getPseudoOp(operation);
        //Check if the operation is a .ENT or .EXT pseudo-op
        isExternalSymbolLine = currentOp.equals(PseudoOps.EXT) || currentOp.equals(PseudoOps.ENT);
        
        return isExternalSymbolLine;
    	
    	
    }
    
    /**
     * Checks if a tokenized line is an end line (Containing .END)
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if the current line is an end line and false otherwise
     */
    public boolean isEndLine(String[] inputTokens) {

        //Boolean to indicate whether the current line is an end-line or not
        boolean isEndLine = false;
        //Get the operation
        String operation = inputTokens[Constants.OPERATION_INDEX];
        //Get the pseudo-op from the pseudo-op table
        PseudoOps currentOp = this.pseudoOpTable.getPseudoOp(operation);
        //Check if the operation is an END pseudo-op
        isEndLine = currentOp.equals(PseudoOps.END);

        return isEndLine;
    }

    /**
     * Processes the .ORIGIN pseudo-op by setting load address and whether the
     * program is relocatable or not.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if .ORIGIN processing is successful and False otherwise
     */
    public boolean processOrigin(String[] inputTokens) {

        //Segment Name
        String label = inputTokens[Constants.LABEL_INDEX];
        //The operation which should be .ORIGIN
        String operation = inputTokens[Constants.OPERATION_INDEX];
        //The load address
        String operand = inputTokens[Constants.FIRST_OPERAND_INDEX];

        //The pseudo-op enum which should be the .ORIGIN enum
        PseudoOps currentOp = this.pseudoOpTable.getPseudoOp(operation);

        //Check if the operation is a .ORIG pseudo-op and if not then queue an error
        if (!currentOp.equals(PseudoOps.ORIG)) {
            ErrorHandler.queueError(ERROR_TYPE.ORIGIN_NOT_FIRST,
                    this.currentLine);
            return false;
        }

        //Check if there is a label which is required and if not then queue an error
        if (label.equals("")) {
            ErrorHandler.queueError(ERROR_TYPE.ORIGIN_NO_LABEL,
                    this.currentLine);
            return false;
        }

        //Set the segment name
        this.segmentName = label;

        //Check if the operand is an empty string (Relocatable)
        if (operand.equals("")) {
            this.initialLoadAddress = 0;
            this.isRelocatable = true;
            return true;
        }

        //Check that the operand is a valid hex string in x0 to xFFFF range
        if (Bits.isValidHexStringInFFFFRange(operand)) {
            this.initialLoadAddress = Bits
                    .hexStringToShort(operand.substring(1));
            this.isRelocatable = false;
            return true;
        }
        //If not valid then queue an error
        else {
            ErrorHandler.queueError(ERROR_TYPE.ORIGIN_INVALID_HEX_OPERAND,
                    this.currentLine);
            return false;
        }

    }

    /**
     * Processes the .END pseudo-op by setting the beginning execution address.
     * Requires the .END pseudo-op to be present. This also deals with a variety
     * of errors related to the .END pseudo-op.
     *
     * @param inputTokens
     *            Input tokens for the current line
     * @return True if no errors are caught and false if errors are caught.
     */
    public boolean processEnd(String[] inputTokens) {

        //Tokenized parts of the input line for use in the method
        String label = inputTokens[Constants.LABEL_INDEX];
        String operand = inputTokens[Constants.FIRST_OPERAND_INDEX];

        //Check to make sure that the .END line doesn't have a label
        if (!label.equals("")) {
            ErrorHandler.queueError(ERROR_TYPE.END_HAS_LABEL, this.currentLine);
            return false;
        }

        //Check for no operand (Execution begins at first address in segment)
        if (operand.equals("")) {
            this.initialExecutionAddress = this.initialLoadAddress;
            return true;
        }

        //Check if it has a hex integer as an operand
        else if (Bits.isValidHexString(operand)) {

            //Error if the hex string for the operand isn't in the 0x to 0xFFFF range
            if (!Bits.isValidHexStringInFFFFRange(operand)) {
                ErrorHandler.queueError(ERROR_TYPE.END_INVALID_HEX_OPERAND,
                        this.currentLine);
                return false;
            }

            //Remove the x in front
            String trimmed = operand.substring(1);
            this.initialExecutionAddress = Bits.hexStringToShort(trimmed);
            return true;
        }
        //If a symbol in the symbol table  is used
        else if (this.symbolTable.hasSymbol(operand)) {

            this.initialExecutionAddress = (short) this.symbolTable
                    .getVal(operand);
            return true;
        }
        //If the symbol is not defined then queue an error
        else {

            ErrorHandler.queueError(ERROR_TYPE.END_FORWARD_REFERENCING,
                    this.currentLine);
            return false;
        }

    }
}
