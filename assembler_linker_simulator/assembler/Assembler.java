package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import assembler.ErrorHandler.ERROR_TYPE;

/**
 * Assembler code for the 3903 Assembler.
 */
public class Assembler {

    /**
     * From a given assembly program input file, generate an object file and a
     * listing file to be used in lab 3.
     *
     * @param args
     *            Command line arguments format: arg[0] contains the name of the
     *            input file
     */
    public static void main(String[] args) {
        //Initialize tables
        SymbolTable symbols = new SymbolTable1();
        LiteralTable literals = new LiteralTable1();
        MachineOpTable machineOps = new MachineOpTable1();
        PseudoOpTable pseudoOps = new PseudoOpTable1();

        //Initialize PrintWriter for error printing
        PrintWriter pw = new PrintWriter(System.out);

        //Create the First Pass object to create intermediate file
        FirstPass pass1 = new FirstPass(literals, symbols, pseudoOps,
                machineOps);

        //Create the file object that will hold the assembly program
        File inputFile;

        //Prompt for file if one is not given in command line arguements or is not readable
        if (args.length == 0) {
            inputFile = Parser.getInputFile();
        } else {
            inputFile = new File(args[0]);
            //Make sure the file is readable. If not queue error and ask for new file
            if (!inputFile.canRead()) {
                ErrorHandler.queueError(ERROR_TYPE.PARSER_FILE_NOT_READABLE,
                        Constants.NON_LINE_ERROR_INDICTATOR);
                System.out.println(
                        "File is not readable. Please enter a new file name.");
                inputFile = Parser.getInputFile();
            }
        }

        //Create the First Pass object to create intermediate file
        File intermediate = pass1.runFirstPass(inputFile);
        // Uncomment these 5 lines for debugging
        //symbols.debug();
        //literals.debug();
        //pass1.getExtSet().forEach((v) ->{
        //    System.out.println(v);
       // });
        //If the intermediate file is null, then a fatal error occurred
        if (intermediate == null) {
            System.out.println("Fatal error in pass 1. Terminating Process.");
            ErrorHandler.queueError(
                    ERROR_TYPE.ASSEMBLER_INVALID_INTERMEDIATE_FILE,
                    Constants.NON_LINE_ERROR_INDICTATOR);
            ErrorHandler.invokeAllErrors(pw);
            return;
        }



        //Create the Second Pass object to create object and listing file
        SecondPass pass2 = new SecondPass(literals, machineOps, symbols,
                pseudoOps, pass1.getExtSet());

        //Create object to hold records generated by pass two
        ArrayList<Record> recordsList = new ArrayList<>();
        try {
            recordsList = pass2.pass(intermediate);
        } catch (FileNotFoundException e) {
            //Print out error if the intermediate file can't be read
            ErrorHandler.queueError(ERROR_TYPE.ASSEMBLER_FILE_NOT_READABLE,
                    Constants.NON_LINE_ERROR_INDICTATOR);
        }

        //Pass two returns a null array list if the second pass fails
        if (recordsList == null) {
            ErrorHandler.invokeAllErrors(pw);
            return;
        }

        //Gets the prog name stored in a parser global variable
        String programName = Parser.getProgName();

        //Creates the two output files in a folder called generated
        File objectFile = new File(Constants.GENERATED_OBJECT_FILE_FOLDER_PATH
                + programName + Constants.OBJECT_FILE_SUFFIX);
        File listingFile = new File(Constants.GENERATED_LISTING_FILE_FOLDER_PATH
                + programName + Constants.LISTING_FILE_SUFFIX);

        //Tries to open a data stream to the object file
        PrintWriter objectWriter = null;
        try {
            objectWriter = new PrintWriter(objectFile);
        } catch (FileNotFoundException e) {
            ErrorHandler.queueError(ERROR_TYPE.ASSEMBLER_FILE_NOT_READABLE,
                    Constants.NON_LINE_ERROR_INDICTATOR);
            e.printStackTrace();
            return;
        }

        //Writes to the newly created object file
        FileGenerator.generateObjectFile(recordsList, objectWriter);
        objectWriter.close();

        //Tries to open a data stream to the listing file
        PrintWriter listingWriter = null;
        try {
            listingWriter = new PrintWriter(listingFile);
            System.out.println("Assembly successful");
        } catch (FileNotFoundException e) {
            ErrorHandler.queueError(ERROR_TYPE.ASSEMBLER_FILE_NOT_READABLE,
                    Constants.NON_LINE_ERROR_INDICTATOR);
            e.printStackTrace();
            return;
        }

        //Writes to the newly created listing file
        FileGenerator.generateListingFile(recordsList, listingWriter,
                machineOps);
        listingWriter.close();

        //Prints out the contents of the tables and errors that occurred during execution
        ErrorHandler.invokeAllErrors(pw);

    }
}
