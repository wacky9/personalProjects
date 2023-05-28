package linker;

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
        // Parser errors
        INVALID_RECORD,

        // First pass linker errors
        FILE_NOT_FOUND, FILES_NOT_ON_ONE_PAGE, MULTI_DEF_EXT_SYMBOL, MULTI_ABSOLUTE_OBJECT_FILE,

        // Second pass linker errors
        OBJECT_FILE_CANNOT_OPEN, EXT_SYMBOL_NOT_DEF;
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
     * @param filename
     *            The file name the error occurred in
     */
    public static void queueError(ERROR_TYPE errType, String filename) {
        Error newError = new Error(errType, filename);
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
        outputStream.print("Linker - [ " + error.getFileName() + " ] ");
        outputStream.print("ERROR: " + error.getErrType().toString() + "\n\t");

        switch (error.getErrType()) {
            case INVALID_RECORD:
                outputStream.println("Error: Invalid record in object file.");
                break;
            case FILE_NOT_FOUND:
                outputStream.println("Error: File not found.");
                break;
            case FILES_NOT_ON_ONE_PAGE:
                outputStream.println("Error: Segments do not fit on the page.");
                break;
            case MULTI_DEF_EXT_SYMBOL:
                outputStream.println(
                        "Error: Multiple definitions of an external symbol.");
                break;
            case OBJECT_FILE_CANNOT_OPEN:
                outputStream
                        .println("Error: Cannot write to linked output file.");
                break;
            case EXT_SYMBOL_NOT_DEF:
                outputStream.println(
                        "Error: An external symbol is used but never defined.");
                break;
            case MULTI_ABSOLUTE_OBJECT_FILE:
                outputStream.println(
                        "Error: Object files must be relative to be linked.");
                break;
            default:
                outputStream.println("Error: Unknown error.");
                break;
        }
        // Flush at the end to display.
        outputStream.flush();
    }
}
