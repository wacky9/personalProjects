package linker;

/**
 * A variety of Constants.
 */

public class Constants {

    /**
     * The lowest address in the machine.
     */
    public static final int LOWEST_ADDRESS = 0;

    /**
     * The highest address in the machine.
     */
    public static final int HIGHEST_ADDRESS = 65536;

    /**
     * The radix of the object files of the machine
     */
    public static final int RADIX = 16;

    /**
     * The index of the record type in an Assembler record.
     */
    public static final int RECORD_TYPE_INDEX = 0;

    /**
     * The index of the segment name in a header record.
     */
    public static final int HEADER_SEGMENT_NAME_INDEX = 1;

    /**
     * The index of the load address in a header record.
     */
    public static final int HEADER_SEGMENT_LOAD_INDEX = 2;

    /**
     * The index of the segment length in a header record.
     */
    public static final int HEADER_SEGMENT_LENGTH_INDEX = 3;

    /**
     * The index of the symbol name for an entry record.
     */
    public static final int ENTRY_SYMBOL_NAME = 1;

    /**
     * The index of the symbol value for an entry record.
     */
    public static final int ENTRY_SYMBOL_VALUE = 2;

    /**
     * The index of the address for a text record.
     */
    public static final int TEXT_ADDRESS = 1;

    /**
     * The index of the contents for a text record.
     */
    public static final int TEXT_CONTENTS = 2;

    /**
     * The index of the relocation tag for a text record.
     */
    public static final int TEXT_RELOCATION_TAG = 3;

    /**
     * The index of the relocation symbol for a text record.
     */
    public static final int TEXT_RELOCATION_SYMBOL = 4;

    /**
     * The index of the starting execution address for an end record.
     */
    public static final int END_EXECUTION_START = 1;

    /**
     * The directory the outputed linked file generates to.
     */
    public static final String LINKED_FILE_OUTPUT_PATH = "./temp/linked/";

    /**
     * The default name for an absolute object file
     */
    public static String ABSOLUTE_FILE_DEFAULT_NAME = "abs_out";

}
