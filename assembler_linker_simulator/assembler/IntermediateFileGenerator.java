package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Queue;

import assembler.ErrorHandler.ERROR_TYPE;

/**
 * Contains methods and the functionality to generate
 * intermediate files needed for pass 2 of the assembling algorithm.
 */
public class IntermediateFileGenerator {

	/**
	 * A queue of program lines to be written to the output file.
	 */
	private Queue<String> programLineQueue;
	/**
	 * Whether the input file is relocatable or not.
	 */
	private boolean isRelocatable;
	
	/**
	 * The segment name.
	 */
	private String segmentName;
	
	/**
	 * The segment length.
	 */
	private int segmentLength;
	
	/**
	 * The initial execution address.
	 */
	private short initialExecutionAddress;
	
	/**
	 * The initial load address.
	 */
	private short initialLoadAddress;
	
	/**
	 * A symbol table holding symbols and their values
	 */
	private SymbolTable symbolTable;
	
	/**
	 * A set of strings for symbols defined by the .ENT pseudo-op
	 */
	private HashSet<String> entSet;
	
	/**
	 * The name of the intermediate file that gets generated
	 */
	private final String STANDARD_IM_FILENAME = "./temp/intermediate/Intermediate.txt";
	/**
	 * The text encoding format used during file generation 
	 */
	private final String TEXT_ENCODING = "UTF-8";
	
	
	/**
	 * Constructor for IntermediateFileGenerator.
	 * @param programLineQueue
	 * 		Queue of Strings that represent program lines to be outputted in the intermediate file.
	 * @param isRelocatable
	 * 		Whether the program file is relocatable or not.
	 */
	public IntermediateFileGenerator(Queue<String> programLineQueue, boolean isRelocatable, HashSet<String> entSet, SymbolTable symbolTable) {
		this.programLineQueue = programLineQueue;
		this.isRelocatable = isRelocatable;
		this.entSet = entSet;
		this.symbolTable = symbolTable;
	}
	
	/**
	 * Sets the header information to be written in the intermediate file.
	 * @param segName
	 * 		The segment name of the input program.
	 * @param segLength
	 * 		The segment length of the input program.
	 * @param initialExecAddr
	 * 		The initial execution address of the input program.
	 * @param initialLoadAddr
	 * 		The initial load address of the input program.
	 */
	public void loadHeaderInformation(String segName, int segLength, short initialExecAddr, short initialLoadAddr) {
		this.segmentName = segName;
		this.segmentLength = segLength;
		this.initialExecutionAddress = initialExecAddr;
		this.initialLoadAddress = initialLoadAddr;
	}
	
	/*
	 * OUTPUT FORMAT:
	 * name -> initial load address -> initial execution address -> relocatable -> length (max lc) -> ent values -> modified program.
	 */
	
	/**
	 * Generates an intermediate file to be used for pass 2.
	 * This requires header information to be initialized via loadHeaderInformation() first.
	 * @return
	 * 		A reference to the generated intermediate file.
	 */
	public File generateIntermediateFile() {
		
		File generatedFile = new File(STANDARD_IM_FILENAME);
		try {
			PrintWriter fileWriter = new PrintWriter(STANDARD_IM_FILENAME, TEXT_ENCODING);
			/*
			 * Print the non-program information first.
			 */
			fileWriter.println(Constants.IM_META_INFO_INDICATOR + "" + this.segmentName);
			fileWriter.println(Constants.IM_META_INFO_INDICATOR + "" + this.initialLoadAddress);
			fileWriter.println(Constants.IM_META_INFO_INDICATOR + "" + this.initialExecutionAddress);
			fileWriter.println(Constants.IM_META_INFO_INDICATOR + "" + this.isRelocatable);
			fileWriter.println(Constants.IM_META_INFO_INDICATOR + "" + this.segmentLength);
			
			/*
			 * Print all the N records
			 */
			for (String entSymbolName : entSet) {
				fileWriter.println("N" + entSymbolName + "=x" + Integer.toHexString(this.symbolTable.getVal(entSymbolName)).toUpperCase());
			}
			
			/*
			 * Print the program lines last.
			 */
			while (!this.programLineQueue.isEmpty()) {
				fileWriter.println(programLineQueue.remove());
			}
			
			/**
			 * Close the PrintWriter
			 */
			fileWriter.close();
			
		} catch (FileNotFoundException e) {
			//Intermediate file couldn't be opened for some reason so log an error.
			ErrorHandler.queueError(ERROR_TYPE.INTERMEDIATE_FILE_NOT_GENERATED, Constants.NON_LINE_ERROR_INDICTATOR);
			generatedFile = null;
		} catch (UnsupportedEncodingException e) {
			//Encoding format didn't match so log an error
			ErrorHandler.queueError(ERROR_TYPE.INTERMEDIATE_FILE_ENCODING_ERROR, Constants.NON_LINE_ERROR_INDICTATOR);
			generatedFile = null;
		}
	
		return generatedFile;
	}
	
	
}