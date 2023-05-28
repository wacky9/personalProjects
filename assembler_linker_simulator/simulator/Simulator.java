package simulator;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Deals with user input and simulates the execution of the abstract machine for
 * CSE 3903
 */
public class Simulator {

    /**
     * Runs the simulator program
     *
     * @param args
     *            Unused arguments that can be passed into the command line.
     */
    public static void main(String[] args) {
        // Simulator startup
        Scanner inKeyboard = new Scanner(System.in);

        // Get command line arguments
        String[] commandLineArgs = args;

        // Create a default memory object to be filled
        MainMemory mainMemory = new MainMemory();

        // Load object file into memory
        Loader programLoader;
        boolean programLoadStatus = false;

        if (!commandLineArgs[0].equals("")) {
            // Use the command line arguments if given
            programLoader = new Loader(mainMemory, commandLineArgs[0]);
            programLoadStatus = programLoader.loadToMemory();
            if (!programLoadStatus) {
                // User input to load file if no/invalid command line argument
                programLoader = loadObjectFileToMemory(inKeyboard, mainMemory);
            }
        } else {
            // Uses user input to load file if no/invalid command line argument
            programLoader = loadObjectFileToMemory(inKeyboard, mainMemory);
        }

        // Ask user for running mode default at quiet mode
        int runningMode = 1;

        if (commandLineArgs.length > 1) {
            // Use the command line arguments if given
            try {
                runningMode = Integer.parseInt(commandLineArgs[1]);
            } catch (NumberFormatException e) {
                // Leave as default if invalid argument
            }
            if (runningMode > 3 || runningMode < 1) {
                // Leave as default if invalid argument
                runningMode = 1;
            }
        } else {
            runningMode = getRunningMode(inKeyboard);
        }

        // Get the user-specified time limit
        int timeLimitInstructions = 500;

        if (commandLineArgs.length > 2) {
            // Use the command line arguments if given
            try {
                timeLimitInstructions = Integer.parseInt(commandLineArgs[2]);
            } catch (NumberFormatException e) {
                // Leave as default if invalid argument
            }
        } else {
            // Use input if no command line argument is given
            timeLimitInstructions = getTimeLimitInstructions(inKeyboard);
        }

        // Registers array: 8 general purpose
        GeneralRegister[] registers = new GeneralRegister[8];
        for (int i = 0; i < 8; i++) {
            registers[i] = new GeneralRegister(i);
        }

        // Condition code registers N, Z, P
        ConditionRegister[] conditions = new ConditionRegister[4];
        conditions[0] = new ConditionRegister("N");
        conditions[1] = new ConditionRegister("Z");
        conditions[2] = new ConditionRegister("P");
        conditions[1].setVal((short) 1);

        // Set the PC to the starting execution address
        ProgramCounter programCounter = new ProgramCounter();
        programCounter.setAddress(
                0x0000FFFF & programLoader.getStartingExecutionAddress());

        // Execute program in user-specified mode
        System.out.println();
        executeProgram(mainMemory, registers, programCounter, conditions,
                runningMode, timeLimitInstructions, inKeyboard);
        System.out.println();

        // Print the error log
        if (ErrorHandler.getSize() > 0) {
            System.out.println();
            ErrorHandler.invokeAllErrors(new PrintWriter(System.err, true));
            System.out.println();
        }

        // Print exiting message and end simulation
        System.out.println("Execution of program "
                + programLoader.getCharacterSegmentName() + " has ended.");
    }

    /**
     * Executes the program loaded into the MainMemory object to completion.
     * Stops execution when the instruction limit is hit, or when a halt
     * instruction is executed. Prints an execution trace when not in quiet
     * mode. Waits for user command to continue when in step mode. Prints an
     * exit message explaining why execution stopped.
     *
     * @param executingMemory
     *            The MainMemory object where the executing prorgam is loaded
     *            into
     * @param registers
     *            The registers the program is using. 0-7 are general purpose, 8
     *            is the program counter
     * @param programCounter
     *            The program counter has as its value the address of the next
     *            instruction to execute
     * @param conditions
     *            The condition code registers, N, Z, P
     * @param runningMode
     *            The running mode of the simulator
     * @param maxInstructionsLeft
     *            The maximum number of instructions the program is allowed to
     *            execute
     * @param inKeyboard
     *            Java Scanner object to read user input from the keyboard
     */
    private static void executeProgram(MainMemory executingMemory,
            GeneralRegister[] registers, ProgramCounter programCounter,
            ConditionRegister[] conditions, int runningMode,
            int maxInstructionsLeft, Scanner inKeyboard) {
        // Set exit status of the interpreter to START
        Instructions interpreterExitStatus = Instructions.NOEXE;
        //Print pre-execution machine state unless in quiet mode
        if (runningMode != 1) {
            printDebugInfo(executingMemory, registers, programCounter,
                    conditions, interpreterExitStatus);
            if (runningMode == 3) {
                waitForUserStep(inKeyboard);
            }
        }

        // Run until execution is halted by instruction or time limits
        while (interpreterExitStatus != Instructions.HALT
                && maxInstructionsLeft > 0) {
            // Execute the next instruction
            interpreterExitStatus = Interpreter.executeCycle(executingMemory,
                    registers, conditions, programCounter);

            // Print post-instruction machine state unless in quiet mode
            if (runningMode != 1
                    || interpreterExitStatus == Instructions.DBUG) {
                printDebugInfo(executingMemory, registers, programCounter,
                        conditions, interpreterExitStatus);
            }

            // Wait for user step if in step mode
            if (runningMode == 3) {
                waitForUserStep(inKeyboard);
            }

            // Decrement maxInstructionsLeft
            maxInstructionsLeft--;
        }

        // Print an execution exiting message
        if (maxInstructionsLeft == 0) {
            // If instruction limit is hit
            System.out.println(
                    "Execution ended, because the instruction limit was hit.");
        } else if (interpreterExitStatus == Instructions.HALT) {
            // If HALT instruction is execution
            System.out
                    .println("Execution ended, because the program finished.");
        } else {
            // If an unknown exit occurs
            System.out.println("Execution ended because of an unknown reason.");
        }

        // Set exit status of the interpreter to FINISH
        interpreterExitStatus = Instructions.NOEXE;

        //Print post-execution machine state unless in quiet mode
        if (runningMode != 1) {
            printDebugInfo(executingMemory, registers, programCounter,
                    conditions, interpreterExitStatus);
        }

    }

    /**
     * Prints a debug execution trace. Prints the current memory page and
     * affected registers in Hex. Prints the last instruction executed.
     *
     * @param memory
     *            The MainMemory object into which the object file is to be
     *            loaded
     * @param registers
     *            The array of registers the current program is using
     * @param programCounter
     *            The program counter has as its value the address of the next
     *            instruction to execute
     * @param conditions
     *            The condition code registers, N, Z, P
     * @param interpreterExitStatus
     *            The exit status of the interpreter including last instruction
     *            executed, and affected registers
     */
    private static void printDebugInfo(MainMemory memory,
            GeneralRegister[] registers, ProgramCounter programCounter,
            ConditionRegister[] conditions,
            Instructions interpreterExitStatus) {

        // Get the current page in memory
        short[] currentMemoryPage = memory
                .getPage((short) programCounter.getAddress());

        // Print desired debug trace
        if (interpreterExitStatus == Instructions.NOEXE
                || interpreterExitStatus == Instructions.DBUG) {
            System.out.println();
            // Print non-executing trace

            // Print the program counter
            System.out.println(programCounter);

            // Print all the registers
            for (int i = 0; i < registers.length; i++) {
                System.out.println(registers[i]);
            }

            // Print the condition codes
            System.out.println(conditions[0] + "\t" + conditions[1] + "\t"
                    + conditions[2]);

            // Print the current page in memory
            for (int i = 0; i < currentMemoryPage.length; i++) {
                System.out.print(
                        " " + Bits.shortToHexString(currentMemoryPage[i]));
                if ((i + 1) % 24 == 0) {
                    System.out.println();
                }
            }
        } else { // Print in-execution debug trace
            // Print the program counter
            System.out.println(programCounter);

            for (int i = 0; i < registers.length; i++) {
                if (registers[i].getModified()) {
                    System.out.println(registers[i]);
                }
            }

            // Print the condition codes
            System.out.println(conditions[0] + "\t" + conditions[1] + "\t"
                    + conditions[2]);

            // Print the last executed instruction
            System.out.println(
                    "Last Instruction Executed: " + interpreterExitStatus);

            // Print the current page in memory
            for (int i = 0; i < currentMemoryPage.length; i++) {
                System.out.print(
                        " " + Bits.shortToHexString(currentMemoryPage[i]));
                if ((i + 1) % 24 == 0) {
                    System.out.println();
                }
            }
        }

        // Print current errors
        System.out.println();
        ErrorHandler.invokeAllErrors(new PrintWriter(System.err, true));

        System.out.print("\n\n");
    }

    /**
     * This method successfully loads an object file into memory. The user will
     * be prompt to enter the name of the object file, and the Loader object
     * will load it into the MainMemory object. If loading fails, the user will
     * be prompted for another file until a file is successfully loaded.
     *
     * @param inKeyboard
     *            Java Scanner object to read user input from the keyboard
     * @param memory
     *            The MainMemory object into which the object file is to be
     *            loaded
     * @return Returns the Loader object which loaded the object file to memory.
     *         This object contains the program name, starting execution
     *         address, initial load address, and segment length
     */
    private static Loader loadObjectFileToMemory(Scanner inKeyboard,
            MainMemory memory) {
        // Loader to be returned on successful load
        Loader programLoader;
        boolean successfulLoad;

        do {
            // Get file name from user
            System.out.print("Enter the name of the object file to be run: ");
            String objectFileName = inKeyboard.nextLine();

            // Send file name to Loader to load into memory
            programLoader = new Loader(memory, objectFileName);

            // Load the object file into memory
            successfulLoad = programLoader.loadToMemory();
            if (!successfulLoad) {
                System.out.println("Object file could not be loaded.");
            }
        } while (!successfulLoad);

        // Return the loader on successful load
        return programLoader;
    }

    /**
     * This method prompts the user to input the desired number of instructions
     * to run before terminating the program on time out. Tries to parse the
     * user input as an integer. If a parsing failure occurs, or if a negative
     * value is inputed, a default limit of 500 instructions is returned.
     *
     * @param inKeyboard
     *            Java Scanner object to read user input from the keyboard
     * @return Returns the time limit as an integer if parsing is successful, a
     *         default value of 500 if not
     */
    private static int getTimeLimitInstructions(Scanner inKeyboard) {
        // Set default time limit
        int timeLimitInstructions = 500;

        // Prompt user to input time limit
        System.out.print("Enter maximum number of instructions to run: ");
        String timeLimitInstructionsUserInput = inKeyboard.nextLine();

        // Try to parse the user input as the time limit
        try {
            timeLimitInstructions = Integer
                    .parseInt(timeLimitInstructionsUserInput);
        } catch (NumberFormatException e) {
            // Use default time limit on parsing failure
        }
        // Use default time limit when negative value is inputed
        if (timeLimitInstructions < 0) {
            timeLimitInstructions = 500;
        }

        // Return time limit
        return timeLimitInstructions;
    }

    /**
     * This method prompts the user to input the desired running. Keeps
     * prompting until a valid option is chosen. Returns 1 for quiet running
     * mode, 2 for trace running mode, 3 for step running mode.
     *
     * @param inKeyboard
     *            Java Scanner object to read user input from the keyboard
     * @return Returns 1 for quiet running mode, 2 for trace running mode, 3 for
     *         step running mode
     */
    private static int getRunningMode(Scanner inKeyboard) {
        int runningMode = 0;
        // Print running mode information
        System.out.println("Machine running modes:");
        System.out.println("\t1) Quiet: Program executes without interuption.");
        System.out.println(
                "\t2) Trace: Print machine state at every instruction.");
        System.out.println(
                "\t3) Step: Print machine state at every instruction and wait for user to step.");

        // Prompt user for running mode until a valid option is chosen
        do {
            // Get user input
            System.out.print("Choose a running mode: ");
            String runningModeUserInput = inKeyboard.nextLine();

            // Try to parse user input as an integer
            try {
                runningMode = Integer.parseInt(runningModeUserInput);
            } catch (NumberFormatException e) {
                // Input cannot be parsed as an integer
            }

            // Print message is user doesn't input a valid mode
            if (runningMode < 1 || runningMode > 3) {
                System.out.println("Invalid running mode entered.");
            }
        } while (runningMode < 1 || runningMode > 3);

        // Return the running mode
        return runningMode;
    }

    /**
     * Continuously prompts the user to enter "step", returns once it's entered.
     *
     * @param inKeyboard
     *            Java Scanner object to read user input from the keyboard
     */
    private static void waitForUserStep(Scanner inKeyboard) {
        // User response for "step" prompt
        String userResponse = new String();
        System.out.println();
        // Prompt the user until "step" is inputed
        do {
            // Prompt user to enter "step"
            System.out.print("Enter \"step\" to continue: ");

            // Read user input
            userResponse = inKeyboard.nextLine();
        } while (!userResponse.equals("step"));
    }

}
