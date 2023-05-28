package linker;

/**
 * Provides parsing functionality for the 3903 linker.
 */
public class Parser {

    /**
     * Splits a line into the different sections depending on the type of
     * Assembler record it is. Header record: ["H", segment name, PLA, segment
     * length]. Entry record: ["N", symbol name, symbol value]. Text record:
     * ["T", address, contents, relocation tag, relocating symbol]. End record:
     * ["E", execution address]. Invalid line: [""].
     *
     * @param line
     *            A line from the Assembler output file
     * @return a parsed array of the sections of the line
     */
    public static String[] parseAssembledLine(String line) {
        // Parsed string array
        String[] parsed;

        if (line.charAt(0) == 'H') { // Parse HEADER record
            parsed = new String[4];

            // Header record signifier
            parsed[Constants.RECORD_TYPE_INDEX] = "H";

            // Segment name
            String segName = line.substring(1, 7);
            // Remove trailing spaces
            if (segName.contains(" ")) {
                segName = segName.substring(0, segName.indexOf(" "));
            }
            parsed[Constants.HEADER_SEGMENT_NAME_INDEX] = segName;

            // Segment load address
            parsed[Constants.HEADER_SEGMENT_LOAD_INDEX] = line.substring(7, 11);

            // Segment length
            parsed[Constants.HEADER_SEGMENT_LENGTH_INDEX] = line.substring(11,
                    15);
        } else if (line.charAt(0) == 'N') { // Parse ENTRY record
            parsed = new String[3];

            // Entry record signifier
            parsed[Constants.RECORD_TYPE_INDEX] = "N";

            // Symbol name
            parsed[Constants.ENTRY_SYMBOL_NAME] = line.substring(1,
                    line.indexOf('='));

            // Symbol value
            parsed[Constants.ENTRY_SYMBOL_VALUE] = line
                    .substring(line.indexOf('=') + 2);
        } else if (line.charAt(0) == 'T') { // Parse TEXT record
            parsed = new String[5];

            // Text record signifier
            parsed[Constants.RECORD_TYPE_INDEX] = "T";

            // Address
            parsed[Constants.TEXT_ADDRESS] = line.substring(1, 5);

            // Contents
            parsed[Constants.TEXT_CONTENTS] = line.substring(5, 9);

            // Absolute files have text records that are only 9 characters long
            if (line.length() > 9) {
                // Relocation Tag
                parsed[Constants.TEXT_RELOCATION_TAG] = "" + line.charAt(10);

                // Relocation symbol
                parsed[Constants.TEXT_RELOCATION_SYMBOL] = line.substring(11);
            } else {
                // Relocation Tag
                parsed[Constants.TEXT_RELOCATION_TAG] = "";

                // Relocation symbol
                parsed[Constants.TEXT_RELOCATION_SYMBOL] = "";

            }
        } else if (line.charAt(0) == 'E') { // Parse END record
            parsed = new String[2];

            // End record signifier
            parsed[Constants.RECORD_TYPE_INDEX] = "E";

            // Execution address
            parsed[Constants.END_EXECUTION_START] = line.substring(1);
        } else { // Parse invalid record
            parsed = new String[1];

            // Empty record signifier
            parsed[Constants.RECORD_TYPE_INDEX] = "";
        }

        // Return the parse line
        return parsed;
    }
}
