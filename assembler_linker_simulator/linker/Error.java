package linker;

import linker.ErrorHandler.ERROR_TYPE;

/**
 * Error class that the ErrorHandler uses to represent errors.
 */
public class Error {

    /**
     * The type of error that occurred.
     */
    private ERROR_TYPE errorType;

    /**
     * The file in which the error occurred.
     */
    private String fileName;

    /**
     * Constructs a new error.
     *
     * @param errorType
     *            The type of error that occurred.
     * @param fileName
     *            The file name the error occurred in.
     */
    public Error(ERROR_TYPE errorType, String fileName) {
        this.errorType = errorType;
        this.fileName = fileName;
    }

    /**
     * Gets the type of error associated with the Error object.
     *
     * @return An ERROR_TYPE enum representing the error.
     */
    public ERROR_TYPE getErrType() {
        return this.errorType;
    }

    /**
     * Gets the name of the file the error associated with the Error object.
     *
     * @return A String corresponding to the file name the error occurred at.
     */
    public String getFileName() {
        return this.fileName;
    }

}