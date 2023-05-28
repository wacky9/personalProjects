package linker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

import linker.ErrorHandler.ERROR_TYPE;

/**
 * This class contains methods to open files for readin and writing. These
 * methods check and handle file errors.
 */
public class FileHandler {
    /**
     * Opens the given file and returns a Scanner object tracking it. Returns
     * null if the file can not be opened.
     *
     * @param fileName
     *            The name of the file to be opened and scanned
     * @return A Scanner object reading the file "fileName" or null if that file
     *         can not be opened
     */
    public static Scanner readObjectFile(String fileName) {
        // Create a null Scanner object
        Scanner fileScanner = null;

        // Try to open the given object file
        try {
            // Set fileScanner to scan the object file
            fileScanner = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            // Print an error message for not being able to open the file
            ErrorHandler.queueError(ERROR_TYPE.FILE_NOT_FOUND, fileName);
        }

        // Return the Scanner object tracking the object file
        return fileScanner;
    }

    /**
     * Opens the given file and returns a PrintWriter object to write to it.
     * Returns null if the file can not be opened.
     *
     * @param segName
     *            The name of the file being written to
     * @return A PrintWriter object writing to the file "segName" or null if the
     *         file can not be opened/created
     */
    public static PrintWriter writeObjectFile(String segName) {
        // Create a null PrintWriter object
        PrintWriter objectWriter = null;

        // Try to open the given file for writing
        try {
            // Set objectWriter to write to the given file
            objectWriter = new PrintWriter(
                    new File(Constants.LINKED_FILE_OUTPUT_PATH + segName));
        } catch (FileNotFoundException e) {
            // Print an error message for not being able to open the file
            ErrorHandler.queueError(ERROR_TYPE.OBJECT_FILE_CANNOT_OPEN,
                    segName);
        }

        // Return the PrintWriter object writing to the object file
        return objectWriter;
    }
}
