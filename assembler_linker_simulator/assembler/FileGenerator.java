package assembler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.lang.StringBuilder;


/**
 * This class is used to generate the two output files for the user to read, namely the
 * Listing File and the Object File
 * @author Winston Basso-Schricker
 */
public class FileGenerator {

    /** Writes all information a list of records to PrintWriter
     * @param recordList
     *      A list of all records to be printed out to the file. The first record in the list is a header record
     *      and the last list is an end record. Every record that is not a partial record contains valid text
     *      in both fields. The ObjectFileText of a each Record must have a valid text record if it doesn't equal the
     *      empty string
     * @param output
     *      A PrintWriter pointing to a valid file
     */
    public static void generateObjectFile(ArrayList<Record> recordList, PrintWriter output){
        for(Record R: recordList){
            /*We only want to print out the object file text here and we don't want to print out any blank lines.
            Thus, we want to print out the object text for every non-partial record and for every partial record where the
            object text does not equal the empty string*/
            if(!(R.isPartialRecord() && R.getObjectFileText().equals("")))
                output.println(R.getObjectFileText());

        }
    }

    /**
     * Outputs a listing file. Each line of the listing file pertains to a single Record. In order, each line has
     * The opcode, the operation in hex, the operation in binary in accordance with the output of HexRecordFormatter, the
     * line number, and the location counter, and the text of the corresponding assembly code.
     * When appropriate some of these fields will not be present but at least some of them will be.
     * @param recordList
     *      A list of Record objects to be printed out
     * @param output
     *      A valid PrintWriter to write the listing file to
     * @param machineOps
     *      A valid machineOps table
     */
    public static void generateListingFile(
            ArrayList<Record> recordList,
            PrintWriter output,
            MachineOpTable machineOps){
        /*The length from the beginning to the line number field is of variable length based on the format of the
        instruction. To ensure that it doesn't ugly, we ensure that the object file portion of each line is exactly the
        same length. 34 was chosen because 34 is the maximum length of the object file portion +1 for readability */
        final int STRING_LENGTH = 34;
        int lineNumber = 1;
        for(Record R: recordList){
            /*Get the object file text*/
            String hexRecord = R.getObjectFileText();
            String formatted = "";
            /*If the object file text exists, format it according to the instruction*/
            if(!hexRecord.equals("") && hexRecord.charAt(0) != 'E' && hexRecord.charAt(0) == 'T') {
                /*This will format the object file text into a binary representation in accordance with the contract*/
                formatted = hexRecordFormatter(hexRecord,R.getInstruction(),machineOps);
            } else if(!hexRecord.equals("") && hexRecord.charAt(0) == 'N'){
                /*We don't want to print anything at all if it is an N record*/
                continue;
            }
            /*Pad the hex record so that it is exactly 34 character long. An empty string will just be 34 spaces*/
            formatted = rightSpacePadded(formatted,STRING_LENGTH);
            /*Create a StringBuilder to combine the various values efficiently*/
            StringBuilder combiner = new StringBuilder(formatted);
            combiner.append("  (");
            combiner.append(lineNumber);
            combiner.append(")  ");
            /*Add the listing file text exactly as it exists in the record*/
            combiner.append(R.getListingFileText());
            lineNumber++;
            /*Print line*/
            output.println(combiner);
        }
    }

    /**
     * Right-pads a given string with spaces
     * @param s
     *      A string to be padded. |s| {@literal <}= length must be true
     * @param length
     *      The desired length of the returned string
     * @return
     *      The string s with spaces added to the right end of the string so that |s| = length
     */
    public static String rightSpacePadded(
            String s,
            int length){
        /*Calculate how many spaces to add*/
        int diff = length - s.length();
        /*Add the correct number of spaces*/
        return s + (" ").repeat(diff);
    }

    /** Formats a given hex record appropriately for the listing file
     * @param hexRecord
     *      A record with 8 valid hex characters and that could have two markers at either end of said 8 characters
     * @return
     * Returns the opcode in parentheses, the instruction in hex, and the instruction in binary. Each
     * operand is separated by an underscore and arbitrary bits are marked with x's
     * For example, the instruction TRAP x25 at location 405A will result in:
     * (405A) F025  1111_xxxx_00100101
     * being returned.
     */
    public static String hexRecordFormatter(
            String hexRecord,
            Instructions I,
            MachineOpTable machineOps){
        /*Indicates where a relocation marker*/
        final char RELOCATION_MARKER_CHAR = '_';
        /*The radix of hexadecimal*/
        final int HEXADECIMAL = 16;
        /*Create a StringBuilder to efficiently combine*/
        StringBuilder formatter = new StringBuilder();
        String pureHex;
        /*If the hex record has an underscore, it's relocatable and so the relocation marker needs to be trimmed.
        Otherwise, only the beginning marker needs to be removed*/
        int relocationMarkerIndex = hexRecord.indexOf(RELOCATION_MARKER_CHAR);
        if(relocationMarkerIndex != -1){
            pureHex = hexRecord.substring(1,relocationMarkerIndex);
        } else {
            pureHex = hexRecord.substring(1);
        }
        /*The first 4 characters are the address in hex*/
        String address = pureHex.substring(0,4);
        /*The last 4 characters is the parsed instruction in hex*/
        String contents = pureHex.substring(4);
        formatter.append('(');
        /*Add the hex address surrounded by parentheses*/
        formatter.append(address);
        formatter.append(") ");
        /*Add the hex instruction exactly*/
        formatter.append(contents);
        formatter.append("  ");
        /*If the Instruction is a pseudo-op, don't format it*/
        if(I == Instructions.INVALID){
            /*If the value isn't an instruction, convert the hex to a short and then convert the short to binary*/
            formatter.append(Bits.paddedBinaryShortString(Bits.hexStringToShort(contents),HEXADECIMAL));
        } else {
            /*If the value is an instruction, parse it in accordance with the contract*/
            int [][] format = machineOps.getFormat(I);
            formatter.append(Bits.formatBinaryInstruction(contents,format));
        }
        return formatter.toString();
    }
}