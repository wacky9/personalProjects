package linker;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import linker.ErrorHandler.ERROR_TYPE;

/**
 * Contains code to first relocate object files, link the object files together,
 * and finally combine them into one object file for the simulator to load
 */
public class LinkerPassTwo {
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
     * Constructor for pass one object.
     */
    public LinkerPassTwo(HashMap<String, Integer> EST, String[] OF, int addr,
            int len) {
        this.externalSymbolTable = EST;
        this.objFiles = OF;
        this.IPLA = addr;
        this.totalLength = len;
    }

    /**
     * Executes the second pass of linking. Combines all text records with
     * updated addresses and contents based on relocation information for the
     * Assembler and first pass, and writes the header and end records. The
     * object file that is produced is the correct format to be run by the
     * Simulator.
     *
     * @return true if the second pass is successful, false if an error occurs
     */
    public boolean SecondPassLoad() {
        // Open main object file for reading
        Scanner fileScanner = FileHandler.readObjectFile(this.objFiles[0]);
        // If the file failed to open, then exit linking
        if (fileScanner == null) {
            // Return a failed linking status
            return false;
        }

        /*
         * Process the main segment
         */
        // Read the main header record
        String[] mainHeader = Parser.parseAssembledLine(fileScanner.nextLine());

        // Check the success of record parsing
        if (mainHeader[Constants.RECORD_TYPE_INDEX].length() == 0) {
            // Print an error for an invalid record
            ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD,
                    this.objFiles[0]);

            // Return an erroneous address
            return false;
        }

        // Open file to write the linked object file
        PrintWriter objectWriter = FileHandler.writeObjectFile(
                mainHeader[Constants.HEADER_SEGMENT_NAME_INDEX]);
        // If the file failed to open, then exit linking
        if (objectWriter == null) {
            // Close the main file scanner
            fileScanner.close();

            // Return a failed linking status
            return false;
        }

        // Write the header record
        objectWriter.println(this.makeHeaderRecord(mainHeader));

        // Skip all entry records (record should contain a text or end record)
        String[] record = this.skipEntryRecords(fileScanner);

        // Initialize the program load address
        int PLA = this.IPLA;

        // Write every text record in the main segment
        while (record[linker.Constants.RECORD_TYPE_INDEX].equals("T")) {
            // Write the relocation adjusted test record to the final object file
            String textRecord = this.makeTextRecord(record, this.IPLA,
                    mainHeader[linker.Constants.HEADER_SEGMENT_NAME_INDEX]);

            // Check the success of the text record processing
            if (textRecord.length() == 0) {
                // Close the object file being read from
                fileScanner.close();

                // Return a failure linking status
                return false;
            } else {
                // Write the text record to the linked object file
                objectWriter.println(textRecord);
            }

            // Read the next line in the file
            record = Parser.parseAssembledLine(fileScanner.nextLine());

            // Check the success of record parsing
            if (mainHeader[Constants.RECORD_TYPE_INDEX].length() == 0) {
                // Print an error for an invalid record
                ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD,
                        this.objFiles[0]);

                // Return an erroneous address
                return false;
            }
        }

        // Increment the PLA by the main segment length
        PLA += Integer.parseInt(
                mainHeader[Constants.HEADER_SEGMENT_LENGTH_INDEX],
                Constants.RADIX);

        // Store the main segment's initial execution address
        int execution = Integer.parseInt(record[Constants.END_EXECUTION_START],
                Constants.RADIX);

        // Close the main object file scanner
        fileScanner.close();

        /*
         * Process all non main segment
         */
        // Write the text records from each non main object file
        for (int i = 1; i < this.objFiles.length; i++) {
            // Process the object file, returning the PLA of the next segment
            PLA = this.processObjectFile(this.objFiles[i], PLA, objectWriter);

            // Exit linking if an error occurred
            if (PLA < Constants.LOWEST_ADDRESS) {
                // Return a failed linking status
                return false;
            }
        }

        /*
         * Complete the linked object file
         */
        // Write the end record with the main segment's beginning execution address
        objectWriter.println(this.makeEndRecord(execution));

        /*
         * End of Pass Two Linking
         */
        // Close the object file writer
        objectWriter.close();

        // Successfully end pass two linking
        return true;
    }

    /**
     * Skips all entry records (denoted by the "N" suffix). Returns the parsed
     * array for the first non entry record. Advances the Scanner past all entry
     * records.
     *
     * @param fileScanner
     *            A Scanner object reading the object file to skip records in.
     *            This Scanner should be advanced to the start of the entry
     *            records
     * @return A String array with the parsed sections of the first non entry
     *         record (should be either a text or end record)
     */
    public String[] skipEntryRecords(Scanner fileScanner) {
        // Parse the first entry record
        String[] record = Parser.parseAssembledLine(fileScanner.nextLine());

        // Read lines until all entry records have been read
        while (record[linker.Constants.RECORD_TYPE_INDEX].equals("N")) {
            // Parse the next line in the file
            record = Parser.parseAssembledLine(fileScanner.nextLine());
        }

        // Return the parsed array for the first non entry record
        return record;
    }

    /**
     * Processed the given file as a non main segment in the linked program.
     * Writes all the text records contained within the file and returns the PLA
     * for the next segment, or -1 f an error occurs.
     *
     * @param fileName
     *            The name of the file to process
     * @param PLA
     *            The program load address of the segment within the file
     * @param objectWriter
     *            A PrintWriter writing to the linked object file
     * @return A integer in range [0, 65535] representing the address
     *         immediately after the last record written. Returns -1 if an error
     *         occurs while processing the file
     */
    private int processObjectFile(String fileName, int PLA,
            PrintWriter objectWriter) {
        // Set fileScanner to scan the given object file
        Scanner fileScanner = FileHandler.readObjectFile(fileName);

        // If the object file cannot be opened, return an error
        if (fileScanner == null) {
            // Return an invalid PLA to signify an error
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Parse the header record of the object file for the name and length
        String[] objFileHeader = Parser
                .parseAssembledLine(fileScanner.nextLine());

        // Check the success of record parsing
        if (objFileHeader[Constants.RECORD_TYPE_INDEX].length() == 0) {
            // Print an error for an invalid record
            ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD, fileName);

            // Return an erroneous address
            return Constants.LOWEST_ADDRESS - 1;
        }

        // Parse the segment's length for incrementing the PLA
        int increment = Integer.parseInt(
                objFileHeader[Constants.HEADER_SEGMENT_LENGTH_INDEX],
                Constants.RADIX);

        // Save the segment length for potential error messages
        String segName = objFileHeader[linker.Constants.HEADER_SEGMENT_NAME_INDEX];

        // Skip all entry records (record should contain a text or end record)
        String[] record = this.skipEntryRecords(fileScanner);

        // Write every text record in the segment to the linked object file
        while (record[linker.Constants.RECORD_TYPE_INDEX].equals("T")) {
            // Link the text record
            String textRecord = this.makeTextRecord(record, PLA, segName);

            // Check the success of the text record processing
            if (textRecord.length() == 0) {
                // Close the object file being read from
                fileScanner.close();

                // Return an invalid PLA to signify an error
                return Constants.LOWEST_ADDRESS - 1;
            } else {
                // Write the text record to the linked object file
                objectWriter.println(textRecord);
            }

            record = Parser.parseAssembledLine(fileScanner.nextLine());

            // Check the success of record parsing
            if (objFileHeader[Constants.RECORD_TYPE_INDEX].length() == 0) {
                // Print an error for an invalid record
                ErrorHandler.queueError(ERROR_TYPE.INVALID_RECORD, fileName);

                // Return an invalid PLA to signify an error
                return Constants.LOWEST_ADDRESS - 1;
            }
        }

        // Close the object file being read from
        fileScanner.close();

        // Return the PLA the next segment would use
        return PLA + increment;
    }

    /**
     * Creates the header record for the linked program
     *
     * @param mainHeader
     *            The parsed header record array for the main file
     * @return A String containing the properly formatted header record:
     *         "H{@literal <}segName{@literal >}{@literal <}IPLA{@literal >}{@literal <}length{@literal >}"
     *         where {@literal <}segName{@literal >} is space-padded name of the
     *         main segment, {@literal <}IPLA{@literal >} is the 4 character hex
     *         string representation of the integer
     *         {@literal <}this.IPLA{@literal >}, {@literal <}length{@literal >}
     *         is the 4 character hex string representation of the integer
     *         {@literal <}this.totalLength{@literal >}
     */
    public String makeHeaderRecord(String[] mainHeader) {
        // Save the name of the main segment
        String mainSegName = mainHeader[linker.Constants.HEADER_SEGMENT_NAME_INDEX];

        // Pad the segment name with spaces to fit the header record format
        while (mainSegName.length() < 6) {
            mainSegName += " ";
        }

        // Return the header record formed form the segment name, IPLA, and length
        return "H" + mainSegName + Bits.shortToHexString((short) this.IPLA)
                + Bits.shortToHexString((short) this.totalLength);
    }

    /**
     * Process the given text record. Increments the address of the record by
     * the given PLA. Increments the low 9 bits of the contents by the listed
     * external symbol if a N relocation tag exists. Increments the full 16 bits
     * of the contents by the listed external symbol if a S relocation tag
     * exists. Does not increment the contents if a A relocation tag exists.
     * This adjusted text record is returned, ready to be written to the object
     * file, but an empty String is returned if an error occurs.
     *
     * @param record
     *            The parse String array for the text record to be processed
     * @param PLA
     *            The PLA of the segment the text record is in
     * @param segName
     *            The name of the segment the text record is in
     * @return A String containing the linked text record if the linking is
     *         successful, or an empty String if an error occurred. A successful
     *         String is in the format:
     *         "T{@literal <}address{@literal >}{@literal <}contents{@literal >}"
     *         where {@literal <}address{@literal >} is the PLA incremented text
     *         record address, and {@literal <}contents{@literal >} is the
     *         relocation adjusted machine instruction
     */
    public String makeTextRecord(String[] record, int PLA, String segName) {
        // Parse the record's address as an integer
        int address = Integer.parseInt(record[Constants.TEXT_ADDRESS], 16);

        // Increment the record's address by the segment's PLA
        address += PLA;

        // Parse the record's contents
        int contents = Integer.parseInt(record[Constants.TEXT_CONTENTS], 16);

        /*
         * Adjust the contents depending on the relocation tag and external
         * symbols. Records in absolute files have no symbol, so the linker
         * checks if a symbol exists in the record before checking in the table.
         */
        if (this.externalSymbolTable
                .containsKey(record[Constants.TEXT_RELOCATION_SYMBOL])) {
            // Process the record's relocation tag
            if (record[Constants.TEXT_RELOCATION_TAG].equals("N")) {
                // Increment the low 9 bits by the external symbol used
                int contentsLow9 = contents & 0x1FF;
                contentsLow9 += this.externalSymbolTable
                        .get(record[Constants.TEXT_RELOCATION_SYMBOL]) & 0x1FF;

                // Replace the 9 bits of contents with the relocated bits
                contents = (contents & 0xFE00) + (contentsLow9 & 0x1FF);
            } else if (record[Constants.TEXT_RELOCATION_TAG].equals("S")) {
                // Increment the low 16 bits by the external symbol used
                contents += this.externalSymbolTable
                        .get(record[Constants.TEXT_RELOCATION_SYMBOL]) & 0xFFFF;

                // Keep only 16 bit
                contents = contents & 0xFFFF;
            }
        } else if (record[Constants.TEXT_RELOCATION_SYMBOL].length() > 0) {
            // Add an error for an undefined external symbol
            ErrorHandler.queueError(ERROR_TYPE.EXT_SYMBOL_NOT_DEF, segName);

            // Return a failed linking status
            return "";
        }

        // Return the text record with adjusted address and contents to be written to the linked file
        return "T" + Bits.shortToHexString((short) address)
                + Bits.shortToHexString((short) contents);
    }

    /**
     * Creates the end record for the linked program.
     *
     * @param executionAddr
     *            An integer representing the starting execution address of the
     *            program in range [0,65535]
     * @return A String containing the properly formatted end record:
     *         "E{@literal <}executionAddrHex{@literal >}" where
     *         {@literal <}executionAddrHex{@literal >} is the 4 character hex
     *         string representation of the integer
     *         {@literal <}executionAddr{@literal >} offset by the initial load
     *         address
     */
    public String makeEndRecord(int executionAddr) {
        // Return the end record formed from the IPLA incremented execution address
        return "E" + Bits.shortToHexString((short) (executionAddr + this.IPLA));
    }
}
