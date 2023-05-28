package assembler;

import assembler.ErrorHandler.ERROR_TYPE;

/**
 * Error class that the ErrorHandler uses to represent errors.
 */
public class Error {

	/**
	 * The type of error that occurred.
	 */
    private ERROR_TYPE errorType;
    
    /**
     * The line number on which the error occurred.
     */
    private int lineNumber;

    /**
     * Constructs a new error.
     * @param errorType
     * 		The type of error that occurred.
     * @param lineNumber
     * 		The line number of the error that occurred.
     */
    public Error(ERROR_TYPE errorType, int lineNumber) {
        this.errorType = errorType;
        this.lineNumber = lineNumber;
    }

    /**
     * Gets the type of error associated with the Error object.
     * @return
     * 		An ERROR_TYPE enum representing the error.
     */
    public ERROR_TYPE getErrType() {
        return this.errorType;
    }

    /**
     * Gets the line number of the error associated with the Error object.
     * @return
     * 		An integer corresponding to the line number the error occurred at.
     */
    public int getLineNumber() {
        return this.lineNumber;
    }
}