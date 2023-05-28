package assembler;

/**
 * A variety of Constants.
 */

public class Constants {

    /**
     * The standard index for a label in the inputTokens array.
     */
    public static final int LABEL_INDEX = 0;

    /**
     * The standard index for an operation in the inputTokens array.
     */
    public static final int OPERATION_INDEX = 1;

    /**
     * The standard index for the first operand in the inputTokens array.
     */
    public static final int FIRST_OPERAND_INDEX = 2;

    /**
     * The standard index for the second operand in the inputTokens array.
     */
    public static final int SECOND_OPERAND_INDEX = 3;

    /**
     * The standard index for the third operand in the inputTokens array.
     */
    public static final int THIRD_OPERAND_INDEX = 4;

    /**
     * The standard length for the imm5 operand type.
     */
    public static final int IMMEDIATE_5_OPERAND_BIT_LENGTH = 5;

    /**
     * The standard length for the imm5 operand type.
     */
    public static final int INDEX_6_OPERAND_BIT_LENGTH = 6;

    /**
     * The standard length for the index6 operand type.
     */
    public static final int ADDR_OPERAND_BIT_LENGTH = 9;

    /**
     * The standard length for the R# operand type.
     */
    public static final int REGISTER_OPERAND_BIT_LENGTH = 3;

    /**
     * The standard length for the trapvect8 operand type.
     */
    public static final int TRAP_VECTOR_OPERAND_BIT_LENGTH = 8;

    /**
     * The length of a word in hex for the 3903 machine.
     */
    public static final int WORD_HEX_LENGTH = 4;

    /**
     * The maximum length for an absolute program.
     */
    public static final int MAX_SEGMENT_LENGTH = 65535;

    /**
     * Standard line number indicating an error not related to a specific line.
     */
    public static final int NON_LINE_ERROR_INDICTATOR = -1;

    /**
     * Character that is written before non-code information in the intermediate
     * file as an indicator.
     */
    public static final char IM_META_INFO_INDICATOR = ':';

    /**
     * Character that is written before code written in the intermediate file as
     * an indicator.
     */
    public static final char IM_CODE_INDICATOR = '$';
    

    /**
     * The path that object files are generated to.
     */
    public static final String GENERATED_OBJECT_FILE_FOLDER_PATH = "./temp/object/";
    
    /**
     * The path that object files are generated to.
     */
    public static final String GENERATED_LISTING_FILE_FOLDER_PATH = "./temp/listing/";

    /**
     * The suffix included on any object files that are generated.
     */
    public static final String OBJECT_FILE_SUFFIX = "objectFile.txt";

    /**
     * The suffix included on any listing files that are generated.
     */
    public static final String LISTING_FILE_SUFFIX = "listingFile.txt";
    
    /**
     * The default value of an EXT symbol when put into a symbol table
     */
    public static final int EXT_DEFAULT_VAL = 0;
}
