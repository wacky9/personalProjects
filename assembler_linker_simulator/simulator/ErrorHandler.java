package simulator;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * ErrorHandler is a class used to deal to queue and invoke errors.
 *
 */
public class ErrorHandler {

    /**
     * ERROR_TYPE represents a specific type of error that occurs during program
     * execution.
     */
    public static enum ERROR_TYPE {

        INTERPRETER_ADDITION_OVERFLOW, INTERPRETER_INVALID_INT, INTERPRETER_INVALID_TRAP_VECTOR,
        INVALID_MEMORY_ACCESS, INVALID_INSTRUCTION, INSTRUCTION_PARSE_ERROR, INTERPRETER_INVALID_CHAR;

    }

    /**
     * A queue that holds the errors of the program.
     *
     * This is set to public for testing but shouldn't be accessed directly.
     */
    public static Queue<ERROR_TYPE> errorQueue;

    //Instantiate the static error queue
    static {
        errorQueue = new LinkedList<ERROR_TYPE>();
    }

    /**
     * Queues an error to be later printed (or ignored).
     *
     * @param err_type
     *            The type of the error
     */
    public static void queueError(ERROR_TYPE err_type) {
        errorQueue.add(err_type);
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
            ERROR_TYPE err_type = errorQueue.remove();
            printError(outputStream, err_type);
        }
    }

    /**
     * Prints an error of err_type to the output stream.
     *
     * @param outputStream
     *            The output stream the error messages are printed to
     * @param err_type
     *            The error type
     */
    private static void printError(PrintWriter outputStream,
            ERROR_TYPE err_type) {

        switch (err_type) {
            case INTERPRETER_ADDITION_OVERFLOW:
                outputStream.println(
                        "Error: Add opcode result exceeds max allowable value (65535)");
                break;
            case INTERPRETER_INVALID_INT:
                outputStream.println(
                        "Error: Could not parse user input to an integer");
                break;
            case INTERPRETER_INVALID_TRAP_VECTOR:
                outputStream.println(
                        "Error: Instruction trap vector was not recognized");
                break;
            case INVALID_MEMORY_ACCESS:
                outputStream.println(
                        "Error: Attempt to access out of bounds memory");
                break;
            case INVALID_INSTRUCTION:
                outputStream.println(
                        "Error: Attempt to execute invalid instruction");
                break;
            case INSTRUCTION_PARSE_ERROR:
                outputStream.println("Error: Could not parse instruction");
                break;
            case INTERPRETER_INVALID_CHAR:
                outputStream.println(
                        "Error: User character does not have an ASCII code");
            default:
                outputStream.println("Error: Unknown error");
                break;
        }

    }

}
