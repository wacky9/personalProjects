package linker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Scanner;

import linker.ErrorHandler.ERROR_TYPE;

/**
 * Runs the 3903 Linker.
 */
public class Linker {

    /**
     * Main method that runs the 3903 linker
     *
     * @param args
     *            A list of file names which may be linked by the linker
     */
    public static void main(String[] args) {
        // Scanner to get user input from the keyboard
        Scanner inKeyboard = new Scanner(System.in);

        // Initialize PrintWriter for error printing
        PrintWriter pw = new PrintWriter(System.out);

        // Initialize the external symbol table
        HashMap<String, Integer> externalSymbolTable = new HashMap<>();

        // Get object files from user
        String[] objectFiles;
        if (args.length > 0) {
            objectFiles = args;
        } else {
            objectFiles = getObjFiles(inKeyboard, pw);
        }

        // Get IPLA from user
        int IPLA = getIPLA(inKeyboard, pw);

        /*
         * Execute first pass loading. Fills the external symbol table including
         * each segment's PLA
         */
        LinkerPassOne pass1 = new LinkerPassOne(externalSymbolTable,
                objectFiles, IPLA);
        boolean firstPassStatus = pass1.FirstPassLoad();

        // Print first pass error messages
        ErrorHandler.invokeAllErrors(pw);
        // End the linking on failed first pass
        if (!firstPassStatus) {
            System.out.println("Linker terminated.");
            return;
        }

        // Second pass success status
        boolean secondPassStatus = true;

        /*
         * If the file set consists of 1 absolute file, then copy that file to
         * the output folder. If the file set is relocatable, then link the
         * programs.
         */
        if (pass1.getAbsolute()) {
            // Create file paths to the absolute program and the linked directory
            Path copy = Paths.get(Constants.LINKED_FILE_OUTPUT_PATH
                    + Constants.ABSOLUTE_FILE_DEFAULT_NAME);
            Path original = Paths.get(objectFiles[0]);

            // Copy the absolute file
            try {
                Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Print an error message for not being able to open the file
                ErrorHandler.queueError(ERROR_TYPE.OBJECT_FILE_CANNOT_OPEN,
                        objectFiles[0]);
            }
        } else {
            /*
             * Execute second pass loading. Relocates text records and generates
             * linked object file.
             */
            LinkerPassTwo pass2 = new LinkerPassTwo(externalSymbolTable,
                    objectFiles, IPLA, pass1.getTotalLength());
            secondPassStatus = pass2.SecondPassLoad();
        }

        // Print second pass error messages
        ErrorHandler.invokeAllErrors(pw);

        // Exit Loader, printing an exit message
        if (firstPassStatus && secondPassStatus) {
            System.out.println("Linker linked successfully.");
        } else {
            System.out.println("Linker terminated.");
        }
    }

    /**
     * Gets the paths of object files to link together from the user.
     *
     * @param inKeyboard
     *            The input scanner for the keyboard
     * @param pw
     *            The output location to print prompts
     * @return A String array with the names of the object files to link
     */
    public static String[] getObjFiles(Scanner inKeyboard, PrintWriter pw) {
        // Array of file names
        String[] objectFiles;
        // Loop condition for valid file names
        boolean validFiles = true;

        // Get valid file paths from the user
        do {
            // Assume input will be valid
            validFiles = true;

            // Prompt the user to input the names of the files separated by spaces
            System.out.print(
                    "Enter the name of the object files to link and load (separated by spaces): ");

            // Parse the input into an array of file names
            objectFiles = inKeyboard.nextLine().split(" ");

            // Check that all files exist
            for (String file : objectFiles) {
                // Create a File object for the supposed object file
                File readableFile = new File(file);

                // If even one file does not exist, re prompt the user
                if (!readableFile.canRead()) {
                    System.out.println("File '" + file + "' does not exist.");
                    validFiles = false;
                    break;
                }
            }
        } while (!validFiles);

        // Return the user inputed file names
        return objectFiles;
    }

    /**
     * Gets the initial program load address from the user.
     *
     * @param inKeyboard
     *            The input scanner for the keyboard
     * @return The initial load address of the program as an integer
     */
    public static int getIPLA(Scanner inKeyboard, PrintWriter pw) {
        // Initial load address
        int IPLA = 0;
        // Loop condition for valid address
        boolean validAddress;

        // Get valid initial load address from the user
        do {
            // Assume input will be valid
            validAddress = true;

            // Prompt the user to input the names of the files separated by spaces
            System.out.print(
                    "Enter the initial load address (precede hex with 'x' and integer with '#'): ");

            // Read input as a string to be able to process
            String input = inKeyboard.nextLine();

            // Check if the input is a hex or integer address
            if (input.charAt(0) == 'x') {
                // Process address as hex

                // Check if the hex value is in the address range
                if (Bits.isValidHexStringInFFFFRange(input)) {
                    // Convert the input to an int
                    IPLA = Integer.parseInt(input.substring(1), 16);
                } else {
                    // On invalid hex value, set while condition to re prompt.
                    System.out.println("Invalid hex value.");
                    validAddress = false;
                }
            } else if (input.charAt(0) == '#') {
                // Process address as int

                // Convert the input to an int
                IPLA = -1;
                try {
                    IPLA = Integer.parseInt(input.substring(1));
                } catch (NumberFormatException ex) {
                    // On invalid int value, set while condition to re prompt
                    System.out.println("Invalid int value.");
                    validAddress = false;
                }

                // Check that the integer is in the address range
                if (IPLA < Constants.LOWEST_ADDRESS
                        || IPLA > Constants.HIGHEST_ADDRESS) {
                    // On invalid int value, set while condition to re prompt
                    System.out.println("Int value not in range.");
                    validAddress = false;
                }
            } else {
                // On invalid input, set while condition to re prompt
                System.out.println("Invalid input.");
                validAddress = false;
            }
        } while (!validAddress);

        // Return the user inputed file names
        return IPLA;
    }
}
