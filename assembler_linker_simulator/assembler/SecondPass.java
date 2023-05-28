package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

/**
 * This class fulfills the requirements of the second pass. It takes in the
 * intermediate file and creates a list of Record objects that contains all the
 * object code in the program paired with the lines of assembly code in the
 * program
 *
 * @author Winston Basso-Schricker
 */
public class SecondPass {
    /**
     * A list of Records that will store the entirety of this process
     * record-by-record
     */
    private ArrayList<Record> records;
    /**
     * All the literals used in this process
     */
    private LiteralTable literals;
    /**
     * A table of machine ops used with this language
     */
    private MachineOpTable machineOps;
    /**
     * All the symbols used in this process
     */
    private SymbolTable symbols;

    /**
     * All the external symbols in this program
     */
    private HashSet<String> externalSymbols;
    /**
     * A table of pseudoOps associated with this language
     */
    private PseudoOpTable pseudoOps;

    /**
     * The current line being parsed
     */
    private int currentLineNumber = 0;

    /**
     * The name of the segment
     */
    private String segmentName;

    /**
     * Used to track whether the current program is relocatable or not
     */
    private boolean relocatable;

    public SecondPass(LiteralTable literals, MachineOpTable MachineOps,
                      SymbolTable symbols, PseudoOpTable PseudoOps, HashSet<String> external) {
        this.literals = literals;
        this.machineOps = MachineOps;
        this.symbols = symbols;
        this.pseudoOps = PseudoOps;
        this.externalSymbols = external;
    }

    /**
     * Goes over desired intermediate file and creates a complete list of
     * Records that pairs each line of assembly with the necessary object code,
     * filling in Records of pure object code when necessary
     *
     * @param intrFile
     *            An input file with no pass 1 errors, comments removed and the
     *            location counter marked at the beginning of every line
     * @return Returns a list of records that is correct
     */
    public ArrayList<Record> pass(File intrFile) throws FileNotFoundException {
        final int USHORT_MAX = 65535;
        /* Records whether a critical error has been discovered */
        boolean successful = true;
        /* These two Scanners should always be on the same line */
        Scanner intermediateFile = new Scanner(intrFile);
        this.records = new ArrayList<>();
        /*Gets the header information and adds it to the Record List*/
        short origin = this.loadHeader(intermediateFile, this.records);
        int segLength = Parser.getSegmentLength();
        /*
         * Check to see if the program tries to use memory outside of the bounds
         * of the program
         */
        if (origin -1 + segLength > USHORT_MAX) {
            ErrorHandler.queueError(ErrorHandler.ERROR_TYPE.OUT_OF_MEM, -1);
            /*
             * Need to return early here because if these values are too large,
             * nothing that follows will work properly
             */
            return null;
        }
        /*
         * Check to see if a relocatable program can be contained on a single
         * page
         */
        if (this.relocatable && !onSamePage(origin,
                (short) (origin - 1 + (short) Parser.getSegmentLength()))) {
            successful = false;
            ErrorHandler.queueError(ErrorHandler.ERROR_TYPE.RELOCATABLE_TOO_BIG,
                    -1);
        }
        boolean noEnd = false;
        while (intermediateFile.hasNextLine() && !noEnd) {
            /* Take in the next line */
            String[] tokenizedLine = Parser.pass2Tokenize(intermediateFile);

            /*Check if it's an N Record*/
            if (tokenizedLine[0].equals("N")) {
                addNRecord(tokenizedLine);
            } else {
                /* Get the most recently parsed line */
                String line = Parser.getLastTokenizedLine();

                /*Get the line number of the most recently parsed line*/
                this.currentLineNumber = Parser.getLineNumber();
                SingleAssemblyLine assemblyPartitioned = Partition(tokenizedLine,
                        origin);
                boolean valid = assemblyPartitioned.resolve(this.symbols,
                        this.literals, this.externalSymbols, this.segmentName, this.currentLineNumber);
                if (valid) {
                    /*Parse the assembly line*/
                    Instructions I = assemblyPartitioned.getInstr();
                    String parsedLine = this.Parse(assemblyPartitioned, origin, I);
                    /*Add the new Record(s) from the line to the Record List*/
                    noEnd = this.generateRecords(this.records, parsedLine, I, line);
                } else {
                    successful = false;
                }
            }
        }
        /*
         * If the end record does not contain the execution start, use the
         * origin
         */
        if (this.records.get(this.records.size() - 1).getObjectFileText()
                .equals("E")) {
            this.records.get(this.records.size() - 1)
                    .setObjectFileText("E" + Bits.shortToHexString(origin));
        }
        if (successful) {
            return this.records;
        } else {
            return null;
        }
    }

    /**
     * Pairs text records with a line of assembly. Creates Records as
     * appropriate and appends them to a list
     *
     * @param records
     *            A list of records to be appended to
     * @param parsedLine
     *            Zero or more lines of object code. Each record starts with the
     *            appropriate header (H,T,E) followed by the associated object
     *            code values in the record. Text records can have multiple
     *            lines of object code when necessary, separated by '\n'
     * @param I
     *            The instruction associated with the line of assembly
     * @param line
     *            The line of assembly in text format that has been parsed
     * @return Returns a true if the record in question is the end record
     */
    public boolean generateRecords(ArrayList<Record> records, String parsedLine,
                                   Instructions I, String line) {
        boolean notEnd = false;
        Record R = new Record();
        /* Doesn't matter what the instruction is */
        R.setInstruction(I);
        /*
         * Three cases to handle: An empty record (just has a "T"), single-line
         * record, multi-line record
         */
        if (parsedLine.equals("T")) {
            R.setObjectFileText("");
            R.setToPartial();
            R.setListingFileText(line);
            records.add(R);
        } else if (parsedLine.contains("\n")) /* Multi-line case */ {
            Scanner lineSplicer = new Scanner(parsedLine);
            String firstLine = lineSplicer.nextLine();
            R.setObjectFileText(firstLine);
            R.setListingFileText(line);
            records.add(R);
            /*
             * Add each of the text records to the file with no corresponding
             * code
             */
            while (lineSplicer.hasNextLine()) {
                Record R2 = new Record();
                R2.setInstruction(I);
                R2.setListingFileText("");
                R2.setObjectFileText(lineSplicer.nextLine());
                R2.setToPartial();
                records.add(R2);
            }
        } else /* Single-line case */ {
            if (parsedLine.charAt(0) == 'E') {
                /* Literals should be loaded before the end record */
                this.loadLiterals(records);
                notEnd = true;
            }
            R.setObjectFileText(parsedLine);
            R.setListingFileText(line);
            records.add(R);
        }
        return notEnd;
    }

    /**
     * Adds a complete N record to this.records
     * @param tokenized
     *      A line with a partial N record
     */
    public void addNRecord(String[] tokenized){
        String complete = tokenized[0] + tokenized[1];
        Record R = new Record(complete,"",true,Instructions.INVALID);
        this.records.add(R);
    }

    /**
     * Generates a string of zero or more text records associated with the given
     * SingleAssemblyLine object
     *
     * @param line
     *            A SingleAssemblyLine object with a correct operation string
     *            and all operands either decimal numbers, or text strings.
     *
     *            A correct operation string will be the exact name of either a
     *            pseudoOp or instruction enum.
     * @param origin
     *            The initial load address
     * @param I
     *            The instruction of the line, INVALID if a psuedo-op, corrected
     *            for immediate vs register
     * @return Returns a single string of zero or more text records. If an
     *         instruction, only 1 text record will be returned. If an
     *         appropriate pseudoOp, it will return multiple text records
     *         separated by \n. Otherwise it will return only the capital letter
     *         "T"
     */
    public String Parse(SingleAssemblyLine line, short origin, Instructions I) {
        final String NINE_BIT_SIGNIFIER = "_N";
        final String SIXTEEN_BIT_SIGNIFIER = "_S";
        final String ABSOLUTE_SIGNIFIER = "_A";
        /*By this time this location counter appears, it is equal to the address*/
        short address = line.getLocationCounter();
        String hexAddress = Bits.shortToHexString(address);
        StringBuilder hexRecord = new StringBuilder("T");

        /* This happens if it's a pseudo-op or error */
        if (I == Instructions.INVALID) {
            PseudoOps P = line.getPseudo();
            switch (P) {
                /*
                 * All origin and end information should be gleaned from
                 * intermediate file
                 */
                case ORIG,EQU, BLKW:
                    break;
                case END:
                    /* Replace T with E */
                    hexRecord.deleteCharAt(0);
                    hexRecord.append("E");
                    /* If no operand, the correct starting address is the origin */
                    if (line.getOperands().size() == 0) {
                        hexRecord.append(Bits.shortToHexString(origin));
                    } else {
                        short startingAddr = (short) Integer.parseInt(line.getOperands().get(0));
                        hexRecord.append(Bits.shortToHexString(startingAddr));
                    }
                    break;
                case FILL:
                    short value = (short) Integer.parseInt(line.getOperands().get(0));
                    hexRecord.append(hexAddress);
                    hexRecord.append(Bits.shortToHexString(value));
                    /*
                     * If the program is relocatable and the value being filled
                     * in is relocatable, move all 16 bits
                     */
                    if (this.relocatable) {
                        if (line.isRelocatable()) {
                            hexRecord.append(SIXTEEN_BIT_SIGNIFIER);
                        } else {
                            hexRecord.append(ABSOLUTE_SIGNIFIER);
                        }
                        hexRecord.append(line.getExternalSymbol());
                    }
                    break;
                case STRZ:
                    String text = line.getOperands().get(0);
                    /* Trim off quotation marks */
                    text = text.substring(1, text.length() - 1);
                    for (int i = 0; i < text.length(); i++) {
                        char c = text.charAt(i);
                        String hexCharacter = Bits.shortToHexString((short) c);
                        hexAddress = Bits
                                .shortToHexString((short) (address + i));
                        hexRecord.append(hexAddress);
                        hexRecord.append(hexCharacter);
                        if (this.relocatable) {
                            hexRecord.append(ABSOLUTE_SIGNIFIER);
                            hexRecord.append(line.getExternalSymbol());
                        }
                        hexRecord.append("\nT");
                    }
                    /* The string value of the null character */
                    String nullChar = "0000";
                    hexAddress = Bits.shortToHexString(
                            (short) (address + text.length()));
                    hexRecord.append(hexAddress);
                    hexRecord.append(nullChar);
                    if (this.relocatable) {
                        hexRecord.append(ABSOLUTE_SIGNIFIER);
                        hexRecord.append(line.getExternalSymbol());
                    }
            }
        } else {
            final int INDEX_OFFSET = 2;
            final int BINARY = 2;
            final int ARBITRARY_INDICATOR = -1;
            ArrayList<String> operandList = line.getOperands();
            int[][] format = this.machineOps.getFormat(I);
            hexRecord.append(hexAddress);
            StringBuilder bitString = new StringBuilder(
                    Bits.paddedBinaryShortString(I.value, 4));

            /* Start at 4 to ignore opcode information */
            for (int i = 4; i < format.length; i++) {
                int code = format[i][0];
                int length = format[i][1];
                if (code == ARBITRARY_INDICATOR) {
                    /* Appends the correct length of arbitrary bits */
                    bitString.append("0".repeat(length));
                } else if (code == 0 || code == 1) {
                    /* Appends the correct number of 1's or 0's */
                    bitString.append(("" + code).repeat(length));
                } else {
                    /*
                     * Indexing for operandList starts at 0 but for the format
                     * at 2
                     */
                    String operand = operandList.get(code - INDEX_OFFSET);
                    bitString.append(Bits.paddedBinaryShortString(
                            (short) Integer.parseInt(operand), length));
                }
            }
            hexRecord.append(Bits.shortToHexString(
                    (short) Integer.parseInt(bitString.toString(), BINARY)));

            /*
             * If an instruction symbol is relocatable, nine bits needs to be
             * moved
             */
            if (this.relocatable) {
                if (line.isRelocatable()) {
                    hexRecord.append(NINE_BIT_SIGNIFIER);
                } else {
                    /*
                     * If the instruction has an addr operand and the line isn't
                     * relocatable, warn the user
                     */
                    if (isAddrInstr(I)) {
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.NON_RELOCATABLE_ADDR,
                                this.currentLineNumber);
                    }
                    hexRecord.append(ABSOLUTE_SIGNIFIER);
                }
                hexRecord.append(line.getExternalSymbol());
            }
        }
        return hexRecord.toString();
    }

    /**
     * Parses a register token into a binary string with equivalent bits as
     * chars
     *
     * @param register
     *            Takes in a valid register string, such as "R0"
     * @return A 3-char string representation of the short value of the register
     */
    public static String parseRegister(String register) {
        /* First letter is R, everything else is numerical */
        String numericalPart = register.substring(1);
        short num = Short.parseShort(numericalPart);
        return Bits.paddedBinaryShortString(num, 3);
    }

    /**
     * Partitions a single line of assembly into an array of strings
     *
     * @param singleLine
     *            A single line of assembly
     * @param origin
     *            The initial load address
     * @return A SingleAssemblyLine object loaded with all the necessary
     *         information
     */
    public static SingleAssemblyLine Partition(String[] singleLine,
                                               short origin) {
        int LABEL_INDEX = 0;
        int OPERATION_INDEX = 1;
        int FIRST_OPERAND_INDEX = 2;
        short lc = (short) ((short) Parser.getLocationCounter() + origin);
        String label = singleLine[LABEL_INDEX];
        String operation = singleLine[OPERATION_INDEX];
        String[] operandArr;
        /* Get an array of operands */
        if (singleLine[FIRST_OPERAND_INDEX].equals("")) {
            operandArr = new String[0];
        } else {
            operandArr = Arrays.copyOfRange(singleLine, FIRST_OPERAND_INDEX,
                    singleLine.length);
        }
        return new SingleAssemblyLine(label, operation, operandArr, lc);
    }

    /**
     * Gets the instruction associated with a single assembly line.
     * @param line
     *            A complete SingleAssemblyLine object
     * @return The Instruction associated with this line or INVALID if the line
     *         is not an instruction
     */
    public static Instructions determineInstruction(SingleAssemblyLine line) {
        Instructions I;
        String operation = line.getOperation();
        try {
            I = Instructions.valueOf(operation);
        } catch (IllegalArgumentException e) {
            I = Instructions.INVALID;
        }
        return I;
    }

    /**
     * Loads the header record into a list of Records and returns the starting
     * load address of the program
     *
     * @param intermediateFile
     *            A scanner pointing to the beginning of a properly formatted
     *            intermediate file
     * @param records
     *            A list of Record objects to be appended to
     */
    private short loadHeader(Scanner intermediateFile,
                             ArrayList<Record> records) {
        int DESIRED_LABEL_LENGTH = 6;
        StringBuilder s = new StringBuilder("H");
        Parser.getProgramInfo(intermediateFile);
        this.relocatable = Parser.isRelocatable();
        String name = Parser.getProgName();
        this.segmentName = name;
        int diff = DESIRED_LABEL_LENGTH - name.length();
        name += (" ").repeat(diff);
        s.append(name);
        s.append(Bits.shortToHexString((short) Parser.getLoadAddress()));
        s.append(Bits.shortToHexString((short) Parser.getSegmentLength()));
        Record R = new Record();
        R.setObjectFileText(s.toString());
        records.add(R);
        return (short) Parser.getLoadAddress();
    }

    /**
     * Loads the literals into a a list of literals.
     * @param records
     *            A list of Record objects to be appended to
     */
    private void loadLiterals(ArrayList<Record> records) {
        final String ABSOLUTE_SIGNIFIER = "_A";
        /* Now add literals */
        ArrayList<String> literalList = this.literals.getLiterals();
        for (String S : literalList) {
            Record R = new Record();
            R.setToPartial();
            StringBuilder literalRecord = new StringBuilder("T");
            literalRecord.append(
                    Bits.shortToHexString((short) this.literals.getAddress(S)));
            literalRecord.append(this.literals.getVal(S));
            if (this.relocatable) {
                literalRecord.append(ABSOLUTE_SIGNIFIER);
                /*Literals will never be global*/
                literalRecord.append(segmentName);
            }
            R.setObjectFileText(literalRecord.toString());
            R.setListingFileText("");
            records.add(R);
        }
    }

    /**
     * Determines if two given addresses are on the same page
     *
     * @param addressOne
     *            The first address
     * @param addressTwo
     *            The second address
     * @return True if the two addresses are on the same page, false otherwise
     */
    public static boolean onSamePage(short addressOne, short addressTwo) {
        if(addressOne == 0 && addressTwo == -1){
            return true;
        }
        short pageOne = Bits.pageOffsetSplit(addressOne)[0];
        short pageTwo = Bits.pageOffsetSplit(addressTwo)[0];
        return pageOne == pageTwo;
    }

    /**
     * Checks to see if the given instruction has an addr operand
     *
     * @param I
     *            An instruction
     * @return Returns true if the instruction has an addr operand
     */
    public static boolean isAddrInstr(Instructions I) {
        return switch (I) {
            case BR, BRN, BRNP, JSR, JMP, LD, LDI, LEA, ST, STI, BRNZ, BRNZP, BRP, BRZ, BRZP -> true;
            default -> false;
        };
    }

    /**
     * Sets that this pass is relocatable. Solely used for testing.
     * @param val
     *      Whether the program is relocatable
     */
    public void setRelocatable(boolean val){
        this.relocatable = val;
    }
}