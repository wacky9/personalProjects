package assembler;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * ErrorHandler is a class used to queue and invoke errors.
 *
 */
public class ErrorHandler {

    /**
     * ERROR_TYPE represents a specific type of error that occurs during program
     * execution.
     */
    public static enum ERROR_TYPE {

        //Assembler Errors
        FIRST_PASS_FAILURE, SECOND_PASS_FAILURE, ASSEMBLER_FILE_NOT_READABLE, ASSEMBLER_INVALID_INTERMEDIATE_FILE,

        //Parser Errors
        PARSER_FILE_NOT_READABLE, PARSER_INVALID_INTERMEDIATE_FILE, PARSER_INVALID_LABEL_LENGTH, PARSER_INVALID_OPERATOR_LENGTH, PARSER_LABEL_INVALID_CHARACTER, PARSER_NON_ALPHANUMERIC_LABEL, PARSER_INVALID_OPERATION_LENGTH, PARSER_INVALID_STRZ_OPERAND,

        // .ORIG Pass1 Errors
        ORIGIN_NOT_FOUND, ORIGIN_NOT_FIRST, ORIGIN_NO_LABEL, ORIGIN_INVALID_HEX_OPERAND,

        // .END Pass1 Errors
        END_NOT_FOUND, END_HAS_LABEL, END_INVALID_HEX_OPERAND, END_FORWARD_REFERENCING,

        // .BLKW Pass1 Errors
        BLKW_FORWARD_REFERENCING, BLKW_REL_SYMBOL, BLKW_INVALID_OPERAND_RANGE,

        //.EQU Pass1 Errors
        EQU_FORWARD_REFERENCING, EQU_NO_LABEL, EQU_INVALID_CONST_OPERAND_RANGE,

        //Literal Pass1 Errors
        LITERAL_NOT_LD_INSTR, LITERAL_INVALID_OPERAND_RANGE,

        //EXT/ENT Pass1 Errors
        EXT_ENT_LABEL, EXT_ENT_NO_OPERAND, EXT_ENT_FOLLOWS_INSTR, ENT_UNDEFINED, ENT_NOT_RELOCATABLE,
        BOTH_EXT_ENT, EXT_DEFINED_IN_FILE,
        
        // Other Pass1 Errors
        MULTIPLE_SYMBOL_DEFINITION, INVALID_STRZ_OPERAND, IMPROPER_INSTR_FORMAT, INPUT_FILE_UNOPENABLE, INVALID_INSTRUCTION,

        //Intermediate File Errors
        INTERMEDIATE_FILE_NOT_GENERATED, INTERMEDIATE_FILE_ENCODING_ERROR,

        //Pass 2 bounding errors
        IMM5_VAL_WRONG, REG_VAL_WRONG, INDEX6_VAL_WRONG, TRAP_VEC_WRONG,

        //More pass 2 bounding errors
        HEX_VAL_WRONG,

        //Pass 2 operand errors
        INCORRECT_OPERAND_NUMBER, INVALID_OPERAND_USAGE,
        //Pass 2 symbol errors
        IMPROPER_RELATIVE_SYMBOL, EMPTY_SYMBOL,
        //Pass 2 page errors
        RELOCATABLE_TOO_BIG, CROSS_PAGE_REFERENCE, FILE_TOO_BIG,
        //Pass 2 memory errors
        OUT_OF_MEM,
        //Non-fatal Warnings
        UNDEFINED_TRAP, NON_RELOCATABLE_ADDR;
    }

    /**
     * A queue that holds the errors of the program.
     *
     * This is set to public for testing but shouldn't be accessed directly.
     */
    public static Queue<Error> errorQueue;

    //Instantiate the static error queue
    static {
        errorQueue = new LinkedList<Error>();
    }

    /**
     * Queues an error to be later printed (or ignored).
     *
     * @param errType
     *            The type of the error
     * @param lineNumber
     *            The line number the error occurs on
     */
    public static void queueError(ERROR_TYPE errType, int lineNumber) {
        Error newError = new Error(errType, lineNumber);
        errorQueue.add(newError);
    }

    /**
     * Returns the number of errors waiting in the error queue.
     *
     * @return Returns the size of errorQueue
     */
    public static int getSize() {
        return errorQueue.size();
    }

    /**
     * Removes all errors from the error queue and prints all error messages.
     *
     * @param outputStream
     *            The output stream that is being printed to
     */
    public static void invokeAllErrors(PrintWriter outputStream) {

        while (errorQueue.size() > 0) {
            invokeSingleError(outputStream);
        }
    }

    /**
     * Removes a single error from the error queue and prints an error message.
     *
     * @param outputStream
     *            The output stream the error is printed to
     */
    public static void invokeSingleError(PrintWriter outputStream) {

        if (errorQueue.size() > 0) {
            Error error = errorQueue.remove();
            printError(outputStream, error);
        }
    }

    /**
     * Prints an error of err_type to the output stream.
     *
     * @param outputStream
     *            The output stream the error messages are printed to
     * @param error
     *            The error type
     */
    private static void printError(PrintWriter outputStream, Error error) {

        outputStream.print("[ LINE " + error.getLineNumber() + " ] ");
        outputStream.print("ERROR: " + error.getErrType().toString() + "\n\t");

        switch (error.getErrType()) {

            case ORIGIN_NOT_FOUND:
                outputStream.println(" .ORIG instruction not found.");
                break;

            case ORIGIN_NOT_FIRST:
                outputStream
                        .println(" .ORIG not the first record of the program.");
                break;

            case ORIGIN_NO_LABEL:
                outputStream.println(" .ORIG is missing a label.");
                break;

            case ORIGIN_INVALID_HEX_OPERAND:
                outputStream.println(
                        " .ORIG operand is not a hex number in range [x0, xFFFF] range.");
                break;

            case END_NOT_FOUND:
                outputStream.println(" .END instruction not found.");
                break;

            case END_HAS_LABEL:
                outputStream.println(" .END instruction has a label.");
                break;

            case END_INVALID_HEX_OPERAND:
                outputStream.println(
                        " .END operand is not a hex number in [x0, xFFFF] range.");
                break;

            case END_FORWARD_REFERENCING:
                outputStream.println(
                        " .END operand is a symbol that is not previously defined.");
                break;

            case BLKW_FORWARD_REFERENCING:
                outputStream.println(
                        " .BLKW operand is a symbol that is not previously defined.");
                break;

            case BLKW_REL_SYMBOL:
                outputStream.println(" .BLKW operand is a relative symbol.");
                break;

            case BLKW_INVALID_OPERAND_RANGE:
                outputStream
                        .println(" .BLKW operand is not in [x1, xFFFF] range.");
                break;

            case EQU_FORWARD_REFERENCING:
                outputStream.println(
                        " .EQU operand is a symbol that is not previously defined.");
                break;

            case EQU_INVALID_CONST_OPERAND_RANGE:
                outputStream.println(
                        " .EQU operand equated to a constant out of range.");
                break;

            case EQU_NO_LABEL:
                outputStream.println(" .EQU is missing a label.");
                break;

            case LITERAL_NOT_LD_INSTR:
                outputStream
                        .println(" Literal is used for a non-LD instruction.");
                break;

            case LITERAL_INVALID_OPERAND_RANGE:
                outputStream.println(
                        " Invalid Literal format. Not in [#-32768, #32767] or [x0, xFFFF].");
                break;

            case MULTIPLE_SYMBOL_DEFINITION:
                outputStream.println(" Symbol defined multiple times.");
                break;

            case INVALID_STRZ_OPERAND:
                outputStream
                        .println(".STRZ format is missing quotation marks.");
                break;

            case IMPROPER_INSTR_FORMAT:
                outputStream.println("Improper Instruction format.");
                break;

            case EXT_ENT_LABEL:
            	outputStream.println(".EXT/.ENT instruction has a label.");
            	break;
            
            case EXT_ENT_NO_OPERAND:
            	outputStream.println(".EXT/.ENT instruction missing an operand.");
            	break;
            
            case EXT_ENT_FOLLOWS_INSTR:
            	outputStream.println(".EXT/.ENT follows a non-.ORIG instruction.");
            	break;
            
            case ENT_UNDEFINED:
            	outputStream.println(".ENT is undefined.");
            	break;
            
            case ENT_NOT_RELOCATABLE:
            	outputStream.println(".ENT is not a RELOCATABLE symbol.");
            	break;
            	
            case EXT_DEFINED_IN_FILE:
            	outputStream.println(".EXT symbol was defined in the current file. ");
            	break;
            	
            case BOTH_EXT_ENT:
            	outputStream.println("Symbol defined as both .ENT and .EXT.");
            
            case INPUT_FILE_UNOPENABLE:
                outputStream.println("Input File Un-openable.");
                break;

            case INVALID_INSTRUCTION:
                outputStream.println("Invalid Instruction.");
                break;

            case INTERMEDIATE_FILE_NOT_GENERATED:
                outputStream
                        .println("Intermediate File could not be written to.");
                break;

            case INTERMEDIATE_FILE_ENCODING_ERROR:
                outputStream
                        .println("Intermediate File incorrect encoding error.");
                break;

            case HEX_VAL_WRONG:
                outputStream.println("Operand is not in [x0, xFFFF].");
                break;

            case INDEX6_VAL_WRONG:
                outputStream.println(
                        "Index 6 operand is not in [#0, #63] or [x0, x3F].");
                break;
            case INVALID_OPERAND_USAGE:
                outputStream.println(
                        "Use of the wrong kind of operand for this instruction");
                break;

            case INCORRECT_OPERAND_NUMBER:
                outputStream.println(
                        "Use of the incorrect number of operands for this instruction");
                break;

            case IMM5_VAL_WRONG:
                outputStream.println(
                        "Imm5 operand is not between [#-16, #15] or [x0, x1F].");
                break;

            case IMPROPER_RELATIVE_SYMBOL:
                outputStream.println(
                        "Forbidden use of a relocatable symbol as an Operand.");
                break;

            case EMPTY_SYMBOL:
                outputStream.println("Attempt to use an undefined symbol.");
                break;

            case REG_VAL_WRONG:
                outputStream.println("Register operand is not in [0, 7]");
                break;

            case TRAP_VEC_WRONG:
                outputStream.println("TRAP operand is not in [x0, xFF]");
                break;

            case UNDEFINED_TRAP:
                outputStream.println(
                        "Warring: use of an unknown trap instruction; however, program assembled");
                break;

            case NON_RELOCATABLE_ADDR:
                outputStream
                        .println("Non-relocatable address in relocatable file");
                break;
            case RELOCATABLE_TOO_BIG:
                outputStream.println(
                        "A file cannot be relocatable if it uses multiple pages");
                break;

            case FILE_TOO_BIG:
                outputStream.println(
                        "No file may contain more than 65,535 words of memory.");
                break;

            case CROSS_PAGE_REFERENCE:
                outputStream.println("Address operand points across a page");
                break;

            case OUT_OF_MEM:
                outputStream.println(
                        "Program is attempting to access memory outside of the bounds of the machine");
                break;
            case PARSER_LABEL_INVALID_CHARACTER:
                outputStream.println(
                        "The label can not start with the R register designation or the x hex designation.");
                break;
            case PARSER_INVALID_LABEL_LENGTH:
                outputStream.println(
                        "The length of the label in the instruction must be 6 chars at most.");
                break;
            case PARSER_INVALID_OPERATOR_LENGTH:
                outputStream.println(
                        "The length of the operator in the instruction must be 5 chars at most.");
                break;
            case ASSEMBLER_FILE_NOT_READABLE:
                outputStream.println("The file could not be read.");
                break;
            case ASSEMBLER_INVALID_INTERMEDIATE_FILE:
                outputStream.println(
                        "The format of the intermediate file is invalid");
                break;
            case PARSER_NON_ALPHANUMERIC_LABEL:
                outputStream.println("All program labels must be alphanumeric");
                break;
            case PARSER_INVALID_OPERATION_LENGTH:
                outputStream.println(
                        "The length of the operation token is too long (max 5)");
                break;
            case PARSER_INVALID_STRZ_OPERAND:
                outputStream
                        .println("The STRZ operand must be enclosed in quotes");
                break;
            default:
                outputStream.println("Error: Unknown error.");
                break;
        }
        //Flush at the end to display.
        outputStream.flush();
    }
}
