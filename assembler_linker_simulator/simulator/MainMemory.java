package simulator;

import java.util.Arrays;

/**
 * Represents the main memory of the simulated machine and provides functionality for writing, reading, and other
 * memory manipulations.
 */

public class MainMemory {
	
	/**
	 * The memory of the currently loaded program.
	 */
    short[] memory;
    
    /**
     * The initial address of the program.
     */
    short initialAddress;
    /**
     * This is the valued returned whenever a program tries to access a value out of memory
     */
    private final short outOfMem = (short) 0x0000;

    /**
     * Constructs a MainMemory object with initial load address of 0 and a segment length of 10
     */
    public MainMemory() {
        /*
         * These values should always be overridden so their value will remain
         * arbitrary for now
         */
        this.memory = new short[10];
        this.initialAddress = 0;
    }


    /**
     * Constructs a new MainMemory object with provided memory size and starting address.
     * 
     * @param memorySize
     * 		How much memory the program wants to allocate
     * @param startingAddress
     * 		The initial address in memory the program starts at
     */
    public MainMemory(int memorySize, short startingAddress) {
        this.memory = new short[(0x0000FFFF & memorySize) + 1];
        this.initialAddress = startingAddress;
    }

    /**
     * Sets the initial address for loading from memory.
     *
     * @param initialAddress
     *            The initial address of the main memory. Note that any valid
     *            short, including a "negative" number is appropriate here
     */
    public void setInitialLoadAddress(short initialAddress) {
        this.initialAddress = initialAddress;
    }

    /**
     * Sets the segment length of main memory.
     *
     * @param segmentLength
     *            The segment length of the main memory. Note that any valid
     *            short, including a "negative" number is appropriate here
     */
    public void setSegmentLength(short segmentLength) {
        this.memory = new short[Short.toUnsignedInt(segmentLength) + 1];
    }

    /**
     * Writes a given value to the desired spot in memory
     *
     * @param address
     *            The address to write to
     * @param data
     *            The value to be written into memory
     */
    public void writeToMemory(short address, short data) {
        /*
         * Converts everything from shorts to ints while treating them as
         * unsigned This allows us to safely do subtraction without worrying
         * about whether the address has a leading 1 (and thus would otherwise
         * be considered negative)
         */
        int trueAddress = Short.toUnsignedInt(address);
        int trueInitialAddress = Short.toUnsignedInt(this.initialAddress);
        /*
         * Takes into account the fact that the memory array is smaller than
         * total memory
         */
        int index = trueAddress - trueInitialAddress;
        /*
         * To avoid potential errors, only write when actually in the range of
         * the array
         */
        if (index < this.memory.length && index >= 0) {
            this.memory[index] = data;
        } else {
            ErrorHandler
                    .queueError(ErrorHandler.ERROR_TYPE.INVALID_MEMORY_ACCESS);
        }
    }

    /**
     * Returns the information at the given location in memory. Sets a flag if
     * the last read instruction was to an area outside of memory.
     *
     * @param address
     *            The address to read from
     * @return The value in the desired spot in memory, as a short
     */
    public short readFromMemory(short address) {
        /*
         * Converts everything from shorts to ints while treating them as
         * unsigned This allows us to safely do subtraction without worrying
         * about whether the address has a leading 1 (and thus would otherwise
         * be considered negative)
         */
        int trueAddress = Short.toUnsignedInt(address);
        int trueInitialAddress = Short.toUnsignedInt(this.initialAddress);
        /*
         * Takes into account the fact that the memory array is smaller than
         * total memory
         */
        int index = trueAddress - trueInitialAddress;
        short returnVal;
        /*
         * Checks if the desired index is out of range of memory and returns
         * default value if true
         */
        if (index >= this.memory.length || index < 0) {
            returnVal = this.outOfMem;
            ErrorHandler
                    .queueError(ErrorHandler.ERROR_TYPE.INVALID_MEMORY_ACCESS);
        } else {
            returnVal = this.memory[index];
        }
        return returnVal;
    }


    /**
     * Gets the entirety of the page that the specified address is in and that
     * the program has access to.
     *
     * @param address
     *            A valid address that the program has access to
     * @return An array of shorts with arr[0] being the bottom of the page or
     *         the first location the program has access to. The array is
     *         guaranteed to have all the page that the program running has
     *         access to. Note that this means that arr.length {@literal <} 512 is a valid
     *         return value. Also note that arr[0] is not necessarily the bottom
     *         of the page.
     *
     */
    public short[] getPage(short address) {
        /*
         * lowIndex and highIndex are the indices of the memory array that
         * correspond to the desired page
         */
        int lowIndex;
        int highIndex;
        int pageSize = 512;
        int trueAddress = Short.toUnsignedInt(address);
        int trueInitialAddress = Short.toUnsignedInt(this.initialAddress);
        int pageNumber = trueAddress / pageSize;
        /* pageBottom is the address of the beginning of the desired page */
        int pageBottom = pageNumber * pageSize;
        /*
         * If the bottom of the page is out of bounds of memory, it only fetches
         * what is in memory
         */
        if (pageBottom < trueInitialAddress) {
            lowIndex = 0;
        } else {
            lowIndex = pageBottom;
            lowIndex -= trueInitialAddress;
        }
        /*
         * If the top of the page is out of bounds of memory, it only fetches
         * what is in memory
         */
        if (pageBottom + pageSize > trueInitialAddress + this.memory.length) {
            highIndex = this.memory.length;
        } else {
            highIndex = pageBottom + pageSize;
            highIndex -= trueInitialAddress;
        }
        /*
         * If it gets passed a value completely out-of-bounds (contract
         * violation) return with a default value)
         */
        if (highIndex < lowIndex || trueAddress < trueInitialAddress) {
            ErrorHandler
                    .queueError(ErrorHandler.ERROR_TYPE.INVALID_MEMORY_ACCESS);
            return new short[] { (short) -1 };
        }
        /* Returns the correct portion of the array */
        return Arrays.copyOfRange(this.memory, lowIndex, highIndex);
    }

    /**
     * Takes in the address of an instruction in memory and returns the relevant
     * values of that instruction
     *
     * @param instructionLocation
     *            The location of the desired instruction in memory
     * @return This method returns an array of shorts with all the necessary
     *         fields split into their own part of the array. For example, if
     *         the instruction in memory is DBUG, getInfo() will return
     *         arr.length = 1 and arr[0] = 0b1000 while if the instruction in
     *         memory is STI getInfo() will return arr.length = 3 and arr[0] =
     *         0b1011, arr[1] = SR and arr[2] = pgoffset9
     */
    public short[] getInfo(short instructionLocation) {
        Instructions currentInstruction = null;
        short address = this.readFromMemory(instructionLocation);
        short opcode = Bits.getBitRange(address, 12, 16);
        for (Instructions I : Instructions.values()) {
            if (opcode == I.value) {
                currentInstruction = I;
            }
        }
        if (currentInstruction == null) {
            currentInstruction = Instructions.DBUG;
            ErrorHandler
                    .queueError(ErrorHandler.ERROR_TYPE.INVALID_INSTRUCTION);
        }
        short[] importantBits;
        switch (currentInstruction) {
            case AND:
            case ADD:
                importantBits = new short[5];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 9, 12);
                importantBits[2] = Bits.getBitRange(address, 6, 9);
                /* A switch between imm and SR mode */
                short fifthBit = Bits.getBitRange(address, 5, 6);
                if (fifthBit == 1) {
                    importantBits[4] = Bits.getBitRange(address, 0, 5);

                } else {
                    importantBits[4] = Bits.getBitRange(address, 0, 3);
                }
                importantBits[3] = fifthBit;
                break;
            case BRX:
                importantBits = new short[5];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 11, 12);
                importantBits[2] = Bits.getBitRange(address, 10, 11);
                importantBits[3] = Bits.getBitRange(address, 9, 10);
                importantBits[4] = Bits.getBitRange(address, 0, 9);
                break;
            case DBUG:
            case RET:
                importantBits = new short[1];
                importantBits[0] = opcode;
                break;
            case JSR:
                importantBits = new short[3];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 11, 12);
                importantBits[2] = Bits.getBitRange(address, 0, 9);
                break;
            case JSRR:
                importantBits = new short[4];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 11, 12);
                importantBits[2] = Bits.getBitRange(address, 6, 9);
                importantBits[3] = Bits.getBitRange(address, 0, 6);
                break;
            case LD:
            case LDI:
            case LEA:
            case ST:
            case STI:
                importantBits = new short[3];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 9, 12);
                importantBits[2] = Bits.getBitRange(address, 0, 9);
                break;
            case LDR:
            case STR:
                importantBits = new short[4];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 9, 12);
                importantBits[2] = Bits.getBitRange(address, 6, 9);
                importantBits[3] = Bits.getBitRange(address, 0, 6);
                break;
            case NOT:
                importantBits = new short[3];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 9, 12);
                importantBits[2] = Bits.getBitRange(address, 6, 9);
                break;
            case TRAP:
                importantBits = new short[2];
                importantBits[0] = opcode;
                importantBits[1] = Bits.getBitRange(address, 0, 8);
                break;
            default:
                importantBits = new short[5];
                /*Returns a no-op instruction*/
                importantBits[0] = 0b0000;
                importantBits[1] = 0b0;
                importantBits[2] = 0b0;
                importantBits[3] = 0b0;
                importantBits[4] = 0b000000000;
                ErrorHandler.queueError(
                        ErrorHandler.ERROR_TYPE.INSTRUCTION_PARSE_ERROR);
        }
        return importantBits;
    }

}
