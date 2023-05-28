package simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Provides functionality for loading data from input files into memory and private fields of the loader class
 */
public class Loader {

    /* Private fields: FOR INTERNAL USE ONLY */
	
	/**
	 * The main memory.
	 */
    private MainMemory memory;
    /**
     * An input file being loaded.
     */
    private File inputFile;
    /**
     * The name of the program.
     */
    private String characterSegmentName;
    /**
     * The length of the memory defined for the program.
     */
    private short segmentLength;
    /**
     * The address of the first memory loaded in the program.
     */
    private short initialLoadAddress;
    /**
     * The address at which execution starts.
     */
    private short executionAddress;

    /**
     * The minimum length a header record can be.
     */
    private final int HEADER_RECORD_MIN_LENGTH = 15;
    /**
     * The minimum length a text record can be.
     */
    private final int TEXT_RECORD_MIN_LENGTH = 9;
    /**
     * The minimum length an end record can be.
     */
    private final int END_RECORD_MIN_LENGTH = 5;

    /**
     * Constructor for a Loader Object. Using this method will instantiate a
     * given loader object by providing reference to a main memory object and a
     * file path to load from. This will set up the loader for future use with
     * any other available method.
     *
     * @param memory
     *            The memory that the loader will load into
     * @param inputFilePath
     *            The file path to access directing to a file to load from
     */
    public Loader(MainMemory memory, String inputFilePath) {
        this.memory = memory;
        this.inputFile = new File(inputFilePath);

    }

    /**
     * This method loads an input file defined by the file path in the Loader
     * constructor into memory. This loads into the MainMemory referenced in the
     * Loader constructor. This method will set multiple fields which can be
     * accessed by other methods including the beginning executing address and
     * the character segment name. This method also deals with a variety of file
     * reading errors.
     *
     * @return Returns true if file loading was successful and false otherwise
     */
    public boolean loadToMemory() {

        //Booleans for checking correct parsing (assume correct at beginning)
        boolean headerParsedCorrectly = true;
        boolean textParsedCorrectly = true;
        boolean endParsedCorrectly = true;

        //Open scanner
        Scanner inputFileScanner;
        try {
            inputFileScanner = new Scanner(this.inputFile);
        } catch (FileNotFoundException e) {
        	System.out.println("Error: Input File Not Found");
            return false;
        }

        //First Process the header record
        if (inputFileScanner.hasNextLine()) {
            headerParsedCorrectly = this
                    .parseHeaderRecord(inputFileScanner.nextLine());
        } else {
            System.out.println("Error: Header Record Parsed Incorrectly");
            inputFileScanner.close();
            return false;
        }
        
        if (!headerParsedCorrectly) {
        	System.out.println("Error: Header Record Parsed Incorrectly");
        }

        //Call mutable memory methods to set initial address + segment length
        //of main memory

        this.memory.setInitialLoadAddress(this.initialLoadAddress);
        this.memory.setSegmentLength(this.segmentLength);

        boolean endRecordRead = false;
        
        //Now try to process the text and end records
        
        while (inputFileScanner.hasNextLine() && !endRecordRead) {

            String currentLine = inputFileScanner.nextLine(); //Get the line
            //Now check if the line is a text record or end record
            if (currentLine.length() > 0 && currentLine.charAt(0) == 'T') {
            	
                //Current line is a text record
                if (!this.parseTextRecord(currentLine)) {
                    textParsedCorrectly = false;
                    System.out.println("Error: Text Record Parsed Incorrectly");
                }
                
            } else if (currentLine.length() > 0 && currentLine.charAt(0) == 'E'){
            	
                //Current line is an end record
                endParsedCorrectly = this.parseEndRecord(currentLine);
                endRecordRead = true;
                
                if (!endParsedCorrectly) {
                    System.out.println("Error: End Record Parsed Incorrectly");
                }
                
            }
            
            else {
            	System.out.println("Error: Missing End Record or Text Record");
            	endParsedCorrectly = false;
            	textParsedCorrectly = false;
            }
        }
        
        //Final safety check for if end record is parsed correctly (This is necessary)
        if (!endRecordRead) {
        	System.out.println("Error: Missing End Record");
        	endParsedCorrectly = false;
        }

        //Finally, close the scanner and return
        inputFileScanner.close();
        return headerParsedCorrectly && textParsedCorrectly
                && endParsedCorrectly;
    }

    /**
     * This method REQUIRES a successful call to loadToMemory() first. This gets
     * the character record name from the loaded input file.
     *
     * @return Returns character segment name of loaded input file
     */
    public String getCharacterSegmentName() {

        return this.characterSegmentName;
    }

    /**
     * This method REQUIRES a successful call to loadToMemory() first. This gets
     * the starting execution address of the loaded input file.
     *
     * @return Returns starting execution address of loaded input file
     */
    public short getStartingExecutionAddress() {

        return this.executionAddress;
    }

    /**
     * This method REQUIRES a successful call to loadToMemory() first. This gets
     * the initial load address.
     *
     * @return Returns initial load address
     */

    public short getInitialLoadAddress() {

        return this.initialLoadAddress;
    }

    /**
     * This method REQUIRES a successful call to loadToMemory() first. This gets
     * the segment length which is the max number of text records that can be
     * read into the input file.
     *
     * @return Returns the segment length
     */

    public short getSegmentLength() {

        return this.segmentLength;
    }

    /**
     * This method parses the information for a header record. This will obtain
     * the info for the initial program load address and the character segment
     * name.
     *
     * @param headerRecord
     *            The Header record string at the start of the input file
     * @return Returns true if the header record is successfully parsed and
     *         false otherwise.
     */
    private boolean parseHeaderRecord(String headerRecord) {

    	final int endSegmentNamePos = 7;
    	final int endLoadAddressStrPos = 11;
    	final int endSegmentLengthStrPos = 15;
    	
        if (headerRecord.length() < this.HEADER_RECORD_MIN_LENGTH
                || headerRecord.charAt(0) != 'H') {
            //Invalid Header Record Error
            return false;
        }

        //Read in character segment name
        this.characterSegmentName = headerRecord.substring(1, endSegmentNamePos);

        //Read in hex values for load addresses and segment lengths
        String initialLoadAddressStr = headerRecord.substring(endSegmentNamePos, endLoadAddressStrPos);
        String segmentLengthStr = headerRecord.substring(endLoadAddressStrPos, endSegmentLengthStrPos);

        if (Bits.isValidHexString(initialLoadAddressStr)
                && Bits.isValidHexString(segmentLengthStr)) {
            this.initialLoadAddress = Bits
                    .hexStringToShort(headerRecord.substring(endSegmentNamePos, endLoadAddressStrPos));
            this.segmentLength = Bits
                    .hexStringToShort(headerRecord.substring(endLoadAddressStrPos, endSegmentLengthStrPos));
            return true;

        } else {
            System.out.println("Error: Invalid Hex String in Header Record");
            return false;
        }
    }

    /**
     * This method parses the information for a text record. This will load
     * information into memory at various places depending on the input string.
     *
     * @param textRecord
     *            A Text record string contained in the input file
     * @return Returns true if the text record is successfully parsed and false
     *         otherwise.
     */
    private boolean parseTextRecord(String textRecord) {

    	final int endStoreAddressPos = 5;
    	final int endStoreContentPos = 9;
    	
        if (textRecord.length() < this.TEXT_RECORD_MIN_LENGTH
                || textRecord.charAt(0) != 'T') {
            //Invalid Text Record Error
            return false;
        }

        //Get the address and content to write
        String storeAddressStr = textRecord.substring(1, endStoreAddressPos);
        String addressContentStr = textRecord.substring(5, endStoreContentPos);

        if (Bits.isValidHexString(storeAddressStr)
                && Bits.isValidHexString(addressContentStr)) {
            short storeAddress = Bits
                    .hexStringToShort(storeAddressStr);
            short addressContent = Bits
                    .hexStringToShort(addressContentStr);

            //Write to memory!
            this.memory.writeToMemory(storeAddress, addressContent);
            return true;

        } else {
            System.out.println("Error: Invalid Hex String in Text Record"+ storeAddressStr);
            return false;
        }

    }

    /**
     * This method parses the information for an end record. This will obtain
     * the starting execution address.
     *
     * @param endRecord
     *            The end record string contained at the end of the input file
     * @return Returns true if the end record is successfully parsed and false
     *         otherwise.
     */
    private boolean parseEndRecord(String endRecord) {

    	final int endRecordSubstring = 5;
    	
        if (endRecord.length() < this.END_RECORD_MIN_LENGTH
                || endRecord.charAt(0) != 'E') {
            //Invalid End Record Error
        	System.out.println("Error: Invalid End Record");
            return false;
        }

        //Get the initial execution address
        String executionAddressStr = endRecord.substring(1, endRecordSubstring);
        if (Bits.isValidHexString(executionAddressStr)) {
            this.executionAddress = Bits
                    .hexStringToShort(endRecord.substring(1, endRecordSubstring));
        } else {
            //The hex read in is invalid
            System.out.println("Error: Invalid Hex String in End Record");
            return false;
        }

        return true;
    }

}
