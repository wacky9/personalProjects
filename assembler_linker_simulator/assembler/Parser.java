package assembler;

import java.io.File;
import java.util.Scanner;

import assembler.ErrorHandler.ERROR_TYPE;

/**
 * Provides methods to parse the input and intermediate file and get data needed
 * for pass one and pass two
 */
public class Parser {

    /**
     * Assume true. Will get changed by pass one if it sees an operand after
     * .ORIG
     */
    private static boolean isRelative = true;

    /**
     * Since the program is assumed to be relocatable, assume it starts loading
     * at 0
     */
    private static int startLoadAddress = 0;;

    /**
     * The labe of the .ORIG pseudo op
     */
    private static String progName = "NoName";

    /**
     * Since the program is assumed to be relocatable, assume it starts
     * executing at 0
     */
    private static int startExecAddress = 0;

    /**
     * The length the program takes up
     */
    private static int segmentLength = 0;

    /**
     * The current value of the location counter
     */
    private static int locationCounter = 0;

    /**
     * The last line of the current lines input
     */
    private static String mostRecentLine = "";

    /**
     * The current line the parser is on
     */
    private static int lineNumber = 0;

    /**
     * Gets a user specified string and tries to open a file associated with
     * that string. Loops until a readable file is found.
     *
     * @return The input file as a file object
     */
    public static File getInputFile() {

        boolean fileNotFound = true;
        File inputFile = null;
        Scanner console = new Scanner(System.in);

        //Ask for new file if file can not be read
        while (fileNotFound) {
            System.out.print("File Name: ");
            inputFile = new File(console.nextLine());

            //If file isnt readable, restart loop
            if (inputFile.canRead()) {
                fileNotFound = false;
            } else {
                System.out
                        .println("Could not read from file. Please try again");
                continue;
            }
        }

        //Closes data stream to prevent memory leaks
        console.close();

        return inputFile;
    }

    /**
     * Consumes the location counter token at the beginning of the instruction
     * line. Queues an error if an instruction line does not start with the
     * location counter marker.
     *
     * @param intrFile
     *            The intermediate file created by pass1.
     * @return Returns a tokenized array of an intermediate file instruction
     *         line without the location counter token. If the intermediate file
     *         is invalid, returns a null value
     */
    public static String[] pass2Tokenize(Scanner intrFile) {
        //Get the next line from the intermediate file
        String nextLine = intrFile.nextLine();

        /*
         * Every line in the intermediate file besides the system info and N records should
         * start with a $ location counter marker
         */
        if (nextLine.charAt(0) == Constants.IM_CODE_INDICATOR) {
            //Identify where the line number ends (can be of variable length)
            int lineNumEnd = nextLine.indexOf(' ');
            //Get and parse the line number
            String lineNumStr = nextLine.substring(1,lineNumEnd);
            lineNumber = Integer.parseInt(lineNumStr);
            //Trim off the line number
            nextLine = nextLine.substring(lineNumEnd+1);
            mostRecentLine = nextLine;
            //Finds where the location counter token ends
            int lcEnd = nextLine.indexOf(' ');
            //Chops off the location counter from the instruction line
            String lcString = nextLine.substring(0, lcEnd);
            //Stores the location counter from the chopped of token
            locationCounter = Integer.parseInt(lcString.substring(1));
            /* Get rid of the space between the label and the lc */
            Scanner nextLineScan = new Scanner(nextLine.substring(lcEnd + 1));
            return TokenizeLine(nextLineScan);
        } else if(nextLine.charAt(0) == 'N'){   /*If it's an N record, it needs a custom tokenization*/
            String[] tokens = new String[2];
            tokens[0] = "N";
            tokens[1] = nextLine.substring(1);
            return tokens;
        } else {
            ErrorHandler.queueError(ERROR_TYPE.PARSER_INVALID_INTERMEDIATE_FILE,
                    Constants.NON_LINE_ERROR_INDICTATOR);
            System.out.println("test");
            return null;
        }


    }

    /**
     * If the line is an instruction, returns an array of strings with the
     * necessary info needed by both passes. Empty labels return a blank string.
     * If the line starts with a semicolon, skips the line. Bits 1-6 will
     * contain may contain a label/address; 7-9 is unused space; 10-14 operation
     * field; 15-17 unused space; 18-end Operands or comments
     *
     * @param input
     *            A scanner that reads from the user's input file
     *
     * @return Tokenized string array with element 0 containing a label if any,
     *         element 1 containing a operation filed, and the rest of the
     *         elements having operands if the line is an instruction. An empty
     *         string array if it is a comment
     */
    public static String[] TokenizeLine(Scanner input) {
        //Get the next line from the intermediate or assembly file
        String nextLine = input.nextLine();

        //Stores the most recent line for additional processing
        mostRecentLine = nextLine;

        /*
         * Array split contains invalid tokens that need to be removed. Regex
         * delimits by spaces and commas.
         */
        String[] split = nextLine.split("[\\s,]", -1);
        int size = 0;

        /*
         * Creates a new string array to store non seperator tokens. Length is
         * variable but does not exceed the split token array length.
         */
        String[] ordered = new String[split.length];
        char first = ' ';
        boolean comment = false;
        int stringStart = 0;

        //Goes through every token and adds valid tokens into valid string
        for (int i = 0; i < split.length; i++) {
            //Stops the for loop from processing if a comment has been detected
            if (!comment) {
                //Makes sure the token isn't empty so charAt can be called
                if (split[i].length() != 0) {
                    //Gets the first character of the token to determine what needs to be done
                    first = split[i].charAt(0);

                    if (first == ' ') {
                        //Do not want to add blank tokens
                    } else if (first == ';') {
                        //Tokens are not allowed to be after comments
                        comment = true;
                        //Comment without space from it
                    } else if (first == Constants.IM_META_INFO_INDICATOR) {
                        //Line contains program header info to be processed
                        getProgramInfo(input);
                    } else if (first == '"') {
                        /*
                         * Strings can have delimiters in them. String always
                         * goes at element 3. Increase final token array size
                         */
                        stringStart = Constants.FIRST_OPERAND_INDEX;
                        size++;
                        i++;

                        if (i < split.length) {
                            //Skip tokens containing the string for now
                            while ((split[i].indexOf('"') == -1)) {
                                i++;
                            }
                        }
                        //Token contains a line location counter
                    } else if (first == Constants.IM_CODE_INDICATOR) {
                        locationCounter = Integer
                                .parseInt(split[i].substring(1));
                        //Add token as is to ordered array and increase final array size
                    } else {
                        ordered[size] = split[i];
                        size++;
                    }
                }
            }
        }

        //Indexes used for adding elements of ordered to the final token array
        int j = 0;
        int k = 0;

        //Increases size of the final array if a blank label token is needed
        if (nextLine.charAt(0) == ' ') {
            size++;
        }

        //Now that the number of valid tokens are know, create the array and populate it
        String[] tokens = new String[size];

        //Add empty element if string label is empty
        if (nextLine.charAt(0) == ' ') {
            tokens[j] = "";
            j++;
        }

        //Moves ordered tokens to a final token array with size equal to the number of tokens
        while (j < size) {
            tokens[j] = ordered[k];
            j++;
            k++;
        }



        //Add in string to marked location
        if (stringStart != 0) {

            String firstQuote = "";
            //Gets the string after the first quote
            if (nextLine.indexOf("\"") != -1) {
                firstQuote = nextLine.substring(nextLine.indexOf("\"") + 1);
            } else {
                ErrorHandler.queueError(
                        ERROR_TYPE.PARSER_LABEL_INVALID_CHARACTER, -1);
                return null;
            }

            String strToken = "";
            //Gets the string before the second quote
            if (firstQuote.indexOf("\"") != -1) {
                strToken = firstQuote.substring(0, firstQuote.indexOf("\""));
            } else {
                ErrorHandler.queueError(
                        ERROR_TYPE.PARSER_LABEL_INVALID_CHARACTER, -1);
                return null;
            }

            //Adds the .STRZ operand in with quotes
            tokens[stringStart] = "\""
                    + strToken.substring(0, strToken.length()) + "\"";
        }

        //Changes ADD to clarify if they have an intermediate operand or not
        if (tokens.length > 4) {

            //Checks to see if the operation token is ADD
            if (tokens[Constants.OPERATION_INDEX].equals("ADD")) {
                //If the instruction is immediate addition, change the operation field
                if (tokens[Constants.THIRD_OPERAND_INDEX].charAt(0) == 'R') {
                    tokens[Constants.OPERATION_INDEX] = "ADD";
                } else {
                    tokens[Constants.OPERATION_INDEX] = "ADDI";
                }
            }
        }

        //Changes AND to clarify if they have an intermediate operand or not
        if (tokens.length > 4) {

            //Checks to see if the operation token is AND
            if (tokens[Constants.OPERATION_INDEX].equals("AND")) {
                //If the instruction is immediate bitwise AND, change the operation field
                if (tokens[Constants.THIRD_OPERAND_INDEX].charAt(0) == 'R') {
                    tokens[Constants.OPERATION_INDEX] = "AND";
                } else {
                    tokens[Constants.OPERATION_INDEX] = "ANDI";
                }
            }
        }

        //Check label is valid
        if (tokens.length > 0) {
            //Checks to see if the label starts with invalid characters
            if (tokens[Constants.LABEL_INDEX].length() > 0) {

                //Gets the first letter of the label index
                char labelChar = tokens[Constants.LABEL_INDEX].charAt(0);
                //If the first character is x or R, return an error
                if (labelChar == 'x' || labelChar == 'R') {
                    ErrorHandler.queueError(
                            ERROR_TYPE.PARSER_LABEL_INVALID_CHARACTER,
                            Constants.NON_LINE_ERROR_INDICTATOR);
                    return null;
                }
            }
            //Checks to see if the label exceeds the maximum allowable size
            if (tokens[Constants.LABEL_INDEX].length() > 6) {
                ErrorHandler.queueError(ERROR_TYPE.PARSER_INVALID_LABEL_LENGTH,
                        Constants.NON_LINE_ERROR_INDICTATOR);
                return null;
            }
        }

        //Check to make sure the string array is not empty (due to a comment)
        if (tokens.length > 1) {
            //Checks to make the operation length is valid
            if (tokens[Constants.OPERATION_INDEX].length() > 5) {
                ErrorHandler.queueError(
                        ERROR_TYPE.PARSER_INVALID_OPERATION_LENGTH,
                        Constants.NON_LINE_ERROR_INDICTATOR);
                return null;
            }
        }

        //Check to make sure the label is alphanumeric if present
        if (tokens.length > 0) {
            //Checks to see if the label is blank
            if (!tokens[Constants.LABEL_INDEX].equals("")) {
                //Goes through every character in the label token and checks if it is alphanumeric
                for (int c = 0; c < tokens[Constants.LABEL_INDEX]
                        .length(); c++) {
                    //If the character is non alphanumric, queue and error and return a null string array
                    if (!Character.isLetterOrDigit(
                            tokens[Constants.LABEL_INDEX].charAt(c))) {
                        ErrorHandler.queueError(
                                ERROR_TYPE.PARSER_NON_ALPHANUMERIC_LABEL,
                                Constants.NON_LINE_ERROR_INDICTATOR);
                        return null;
                    }
                }
            }
        }

        //Return a blank token at the end of the tokenized array if the instruction doesn't have operands
        if (size < 3 && size > 0) {
            String[] minTokens = { tokens[Constants.LABEL_INDEX],
                    tokens[Constants.OPERATION_INDEX], "" };
            return minTokens;
        }

        //input.close(); //Closed in main
        return tokens;
    }

    /**
     * If the next line from the scanner starts with a colon, retrieve program
     * info needed in pass 2.
     *
     * @param input
     * 		A scanner that reads from the user's input file
     */
    public static void getProgramInfo(Scanner input) {
        //For parsing the intermediate file. Get info about program from pass 1
        if (input.hasNext()) {
            String firstLine = input.nextLine();
            char first = firstLine.charAt(0);
            //Check to see if unique symbol denoting prog info is present
            if (first == Constants.IM_META_INFO_INDICATOR) {
                /*
                 * OUTPUT FORMAT: : name initial_load_address
                 * initial_execution_address relocatable length.
                 */
                progName = firstLine.substring(1);
                startLoadAddress = Integer
                        .parseInt((input.nextLine().substring(1)));
                ;
                startExecAddress = Integer
                        .parseInt((input.nextLine().substring(1)));
                ;
                isRelative = Boolean
                        .parseBoolean((input.nextLine().substring(1)));
                ;
                segmentLength = Integer
                        .parseInt((input.nextLine().substring(1)));
            }
        }
    }

    /**
     * Gives a substring of the file line without the comments
     *
     * @param commentLine
     *            A string with comments still in it
     *
     * @return A string that contains the file line without any comments
     */
    public static String getLineNoComments(String commentLine) {
        //Do not want to consume the input of the next line so copy scanner
        int cut = commentLine.indexOf(';');
        if (cut != -1) {
            commentLine = commentLine.substring(0, cut);
        }
        return commentLine;
    }

    /**
     * Returns the line that was last tokenized.
     *
     * @return The most recent line that was tokenized without comments
     */
    public static String getLastTokenizedLine() {
        return getLineNoComments(mostRecentLine);
    }

    //Method(s) for pass 2

    /**
     * Returns whether or not the program is relocatable.
     *
     * @return Returns true if the program is relocatable, false if not.
     */
    public static boolean isRelocatable() {
        return isRelative;
    }

    /**
     * Returns the initial load address of the program.
     *
     * @return Returns the starting address value
     */
    public static int getLoadAddress() {
        return startLoadAddress;
    }

    /**
     * Returns the starting execution address of the program.
     *
     * @return Returns the current value of the location counter
     */
    public static int getExecAddress() {
        return startExecAddress;
    }

    /**
     * Returns the program name.
     *
     * @return Returns the current value of the location counter
     */
    public static String getProgName() {
        return progName;
    }

    /**
     * Returns the length of the program.
     *
     * @return Returns the max location counter or the segment length
     */
    public static int getSegmentLength() {
        return segmentLength;
    }

    /**
     * Returns the current value of the location counter.
     *
     * @return Returns the current value of the location counter
     */
    public static int getLocationCounter() {
        return locationCounter;
    }

    /**
     * Gets the Line number of the most recently tokenized line
     *
     * @return
     *  The line number of the most recently tokenized line
     */
    public static int getLineNumber() {
        return lineNumber;
    }
}