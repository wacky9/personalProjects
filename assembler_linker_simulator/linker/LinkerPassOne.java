package linker;

import java.util.HashMap;
import java.util.Scanner;

import linker.ErrorHandler.ERROR_TYPE;

/**
 * Scans every object file for external symbols, and adds them to the external
 * symbol table.
 */
public class LinkerPassOne {

    /**
     * The table containing external symbols and their values.
     */
    private HashMap<String, Integer> externalSymbolTable;

    /**
     * The list of object files to link.
     */
    private String[] objFiles;

    /**
     * The initial load address.
     */
    private int IPLA;

    /**
     * The total combined segment length.
     */
    private int totalLength;

    /**
     * Whether the file is absolute
     */
    private boolean absolute;

    /**
     * Constructor for pass one object.
     */
    public LinkerPassOne(HashMap<String, Integer> EST, String[] OF, int addr) {
        this.externalSymbolTable = EST;
        this.objFiles = OF;
        this.IPLA = addr;
        this.absolute = false;
    }

    /**
     * Returns whether the file is absolute
     *
     * @return An true if the file set consists of 1 absolute program, false if
     *         not
     */
    public boolean getAbsolute() {
        return this.absolute;
    }

    /**
     * Returns the total combined segment length.
     *
     * @return An integer [0, 512]
     */
    public int getTotalLength() {
        return this.totalLength;
    }

    /**
     * Searches every file for external symbols from entry record and adds them
     * to the table. Ensures every file is relocatable.
     *
     * @return true if the loading was successful, false if an error occurred
     */
    public boolean FirstPassLoad() {
        // Track the success of the first pass
        boolean firstPassSuccess = true;

        // Track the program load address of each segment
        int PLA = this.IPLA;

        // Process every object file
        for (String file : this.objFiles) {
            PLA = this.processObjectFile(file, PLA);

            // Exit first pass if an error occurred
            if (PLA < Constants.LOWEST_ADDRESS) {
                // Return invalid link status
                firstPassSuccess = false;
            }
        }

        // Store the total combined segment length
        this.totalLength = PLA - this.IPLA;

        // Return valid link status
        return firstPassSuccess;
    }

    /**
     * Processes an object file for first pass linking. Adds the segment's name
     * and all external symbols defined in the segment to the external symbol
     * table. Returns the address immediately after the last record in the
     * segment. However, -1 is returned if a file or parsing error occurs.
     *
     * @param file
     *            The path of the file to linked
     * @param PLA
     *            The starting load address of the segment in the file
     * @return An integer with the value of load address the next segment should
     *         be loaded. -1 is returned if an error occurred
     */
    private int processObjectFile(String file, int PLA) {
        // Create fileScanner to scan the file
        Scanner fileScanner = FileHandler.readObjectFile(file);

        // Check success of file opening
        if (fileScanner == null) {
            // Return an erroneous address
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Parse the first line of the file (header record)
        String[] record = Parser.parseAssembledLine(fileScanner.nextLine());

        // Check the success of record parsing
        if (record[Constants.RECORD_TYPE_INDEX].length() == 0) {
            // Print an error for an invalid record
            ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD, file);

            // Return an erroneous address
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Store the segment name in the table with the PLA value
        this.externalSymbolTable
                .put(record[Constants.HEADER_SEGMENT_NAME_INDEX], PLA);
        // Increment to the next segment's PLA
        int segmentLength = Integer.parseInt(
                record[Constants.HEADER_SEGMENT_LENGTH_INDEX], Constants.RADIX);

        // Process all entry records
        record = this.processEntryRecords(PLA, fileScanner, file);

        // Check if parsing entry records was successful
        if (record[Constants.RECORD_TYPE_INDEX].length() == 0) {
            // Close the file scanner
            fileScanner.close();

            // Return erroneous address
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Check if the object file is relocatable
        if (record[Constants.RECORD_TYPE_INDEX].equals("T")
                && record[Constants.TEXT_RELOCATION_TAG].length() == 0) {

            // Throw an error if an absolute program is being linked with something else
            if (this.objFiles.length > 1) {
                // Print error message for linking an absolute file with others
                ErrorHandler.queueError(ERROR_TYPE.MULTI_ABSOLUTE_OBJECT_FILE,
                        file);

                // Close the file scanner
                fileScanner.close();

                // Return erroneous address
                return Constants.LOWEST_ADDRESS - 1;
            }

            // Close the file scanner
            fileScanner.close();

            // Mark the linking process as absolute
            this.absolute = true;

            // Return absolute segment length
            return PLA + segmentLength;
        }

        /*
         * Check that this segment is on the same page as every other segment.
         * This is done by checking that the last address in this segment (PLA -
         * 1) is on the same page as the main segment (IPLA).
         */
        if (segmentLength > 0
                && !Bits.onSamePage((short) (PLA + segmentLength - 1),
                        (short) (this.IPLA))) {
            // This segment loads onto a different page

            // Print error message
            ErrorHandler.queueError(ERROR_TYPE.FILES_NOT_ON_ONE_PAGE, file);

            // Close the file scanner
            fileScanner.close();

            // Return erroneous address
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Close the file scanner
        fileScanner.close();

        // Return the PLA of the next segment
        return PLA + segmentLength;
    }

    /**
     * Adds the symbols contained in the all entry records of the object file to
     * the external symbol table. Returns the parsed array for the first non
     * entry record or and invalid parsing array if an error occurred.
     *
     * @param PLA
     *            The program load address for the segment the is in the file
     * @param fileScanner
     *            The Scanner reading the object file
     * @param file
     *            The name of the segment in the file
     * @return The parsed array for the first record after the entry records
     */
    public String[] processEntryRecords(int PLA, Scanner fileScanner,
            String file) {
        // Parse the next line in the file
        String[] record = Parser.parseAssembledLine(fileScanner.nextLine());

        // Check the success of record parsing
        if (record[Constants.RECORD_TYPE_INDEX].length() == 0) {
            // Print an error for an invalid record
            ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD, file);
        }

        // Process every entry record adding symbols to the external symbol table
        while (record[Constants.RECORD_TYPE_INDEX].equals("N")) {
            // Read the symbol name and value
            String symbolName = record[Constants.ENTRY_SYMBOL_NAME];

            // Parse the symbol's hex value as an integer
            int symbolValue = Integer.parseInt(
                    record[Constants.ENTRY_SYMBOL_VALUE], Constants.RADIX);

            // Add the symbol to the table
            if (!this.externalSymbolTable.containsKey(symbolName)) {
                // Add the symbol and its adjusted value to the table
                this.externalSymbolTable.put(symbolName, symbolValue + PLA);
            } else {
                // Print error message for multiple symbol declarations
                ErrorHandler.queueError(ERROR_TYPE.MULTI_DEF_EXT_SYMBOL, file);

                // Close the file scanner
                fileScanner.close();

                // Return process failure status
                return new String[] { "" };
            }
            record = Parser.parseAssembledLine(fileScanner.nextLine());
        }

        // Return process success status
        return record;
    }

}
