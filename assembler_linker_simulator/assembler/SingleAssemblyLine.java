package assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * This object represents a single line of assembly from the intermediate file.
 * This object separates the different parts of a given instruction into their
 * relevant pieces.
 *
 * @author Winston Basso-Schricker
 */
public class SingleAssemblyLine {
    /**
     * The label associated with this line or "" if there is none
     */
    private String Label;
    /**
     * The operation associated with this line, either an Instruction or a
     * PseudoOp
     */
    private String Operation;
    /**
     * A list of operands
     */
    private ArrayList<String> operands;
    /**
     * The location of this line
     */
    private short locationCounter;
    /**
     * The Instruction associated with this line or Instructions.INVALID if
     * there is none
     */
    private Instructions I = Instructions.INVALID;
    /**
     * The PseudoOp associated with this line or PsuedoOPs.INVALID if there is
     * none
     */
    private PseudoOps P = PseudoOps.INVALID;
    /**
     * Indicates if this line has a relocatable symbol
     */
    private boolean relocatable = false;

    /**
     * String of the external symbol on the single assembly line
     */
    private String externalSymbol = "";

    /**
     * Constructor for a single assembly line
     * 
     * @param la
     *            A label
     * @param op
     *            An operation string
     * @param opers
     *            An array of operands
     * @param lc
     *            A location counter
     */
    public SingleAssemblyLine(String la, String op, String[] opers, short lc) {
        this.Label = la;
        this.Operation = op;
        this.operands = new ArrayList<>();
        /* Adds all the values in opers as operands */
        Collections.addAll(this.operands, opers);
        this.locationCounter = lc;
    }

    /**
     * Gets the label associated with the line
     * @return Returns the label associated with this line or the empty string
     *         if there is none
     */
    public String getLabel() {
        return this.Label;
    }

    /**
     * Gets the operation associated with the line
     * @return Returns the operation associated with this line
     */
    public String getOperation() {
        return this.Operation;
    }

    /**
     * Resolves symbols, literals, registers, and immediates within operands by
     * replacing them with their numerical values. After calling resolve, this
     * will have only strings that can be parsed into numbers (or it will be
     * invalid).
     *
     * @param symbolTable
     *            A valid symbol table
     * @param literalTable
     *            A valid literal table
     * @return Whether this line contains a valid instruction
     */
    public boolean resolve(SymbolTable symbolTable, LiteralTable literalTable, HashSet<String> external,
                           String segName, int lineNumber) {
        /*
         * These constants describe different max/min values for different
         * operand ranges
         */
        final int MAX_HEX = 65535;
        final int MIN_HEX = -32768;
        final int MAX_REG = 7;
        final int MIN_REG = 0;
        final int TRAP_MAX = 255;
        final int TRAP_MIN = 0;
        final int INDEX6_MAX = 63;
        final int INDEX6_MIN = 0;
        final int IMM5_MAX = 15;
        final int IMM5_MIN = -16;

        /*These constants describe different kinds of operands*/
        final int REG_OP = 0;
        final int LIT_OP = 1;
        final int CONSTANT_OP = 2;
        final int SYM_OP = 3;
        final int STRZ_OP = 4;
        /*A list of all the valid trap values*/
        final int[] valid_traps = { 33, 34, 35, 37, 49, 51, 67 };


        /*Determines what instruction this line is*/
        this.I = SecondPass.determineInstruction(this);
        if (this.I == Instructions.INVALID) {
            /* If it isn't an operand, it must be a pseudo op */
            this.P = PseudoOps.valueOf(this.getOperation().substring(1));
        }
        /* Assumes that resolution will be correct */
        boolean valid = true;
        /*
         * Used to check whether an operand is the final in the list; the final
         * operand has different behavior from beginning/middle operands
         */
        int last = this.operands.size() - 1;
        /*
         * If the instruction has the wrong number of operands or a pseudo-op
         * has more than one, throw an error
         */
        if ((this.I != Instructions.INVALID
                && this.I.operandNum != this.operands.size())) {
            ErrorHandler.queueError(
                    ErrorHandler.ERROR_TYPE.INCORRECT_OPERAND_NUMBER,
                    lineNumber);
            valid = false;
        } else {
            switch (this.P) {
                case ORIG, END:
                    if (this.operands.size() != 0
                            && this.operands.size() != 1) {
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.INCORRECT_OPERAND_NUMBER,
                                lineNumber);
                        valid = false;
                    }
                    break;
                case EQU, STRZ, BLKW, FILL:
                    if (this.operands.size() != 1) {
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.INCORRECT_OPERAND_NUMBER,
                                lineNumber);
                        valid = false;
                    }
            }
        }
        /* Resolve each of the operands one-by-one */
        for (int i = 0; i < this.operands.size(); i++) {
            /*
             * This integer describes what kind of operand is being analyzed. By
             * default, we assume it's a symbol
             */
            int Operand_Identifier = 3;
            /* Get the value of the current operand in question */
            String operand = this.operands.get(i);
            /*
             * If the operand is a literal, replace it with the address in the
             * Literal Table
             */
            if (literalTable.hasLiteral(operand)) {
                this.operands.set(i, "" + literalTable.getAddress(operand));
                /* If an instruction uses a literal, then it is relocatable */
                this.relocatable = true;
                Operand_Identifier = LIT_OP;
            } else if (symbolTable
                    .hasSymbol(operand)) /*
             * If the operand is a symbol, replace
             * it with the symbol value
             */ {
                this.operands.set(i, "" + symbolTable.getVal(operand));
                if(external.contains(operand)){
                    setExternalSymbol(operand);
                } else {
                    setExternalSymbol(segName);
                }
                /* Mark if the line contains a relocatable */
                this.relocatable = checkRelocatableSymbol(symbolTable, operand);
            } else if (Bits.isLiteralOrImmediate(
                    operand)) /* If the operand is an immediate, parse it */ {
                this.operands.set(i,
                        "" + Bits.parseLiteralOrImmediate(operand));
                Operand_Identifier = CONSTANT_OP;
            } else if (operand.charAt(
                    0) == 'R') /* If the operand is a register, parse it */ {
                /* Replace with numerical value of the register */
                this.operands.set(i, operand.substring(1));
                Operand_Identifier = REG_OP;
            } else if (this.P == PseudoOps.STRZ) /*
             * Strz has a unique operand
             * that is handled in Pass 1.
             */ {
                Operand_Identifier = STRZ_OP;
            } else {
                /* If it isn't one of the above, it's an undefined symbol */
                ErrorHandler.queueError(ErrorHandler.ERROR_TYPE.EMPTY_SYMBOL,
                        lineNumber);
                /*
                 * Return immediately here; we can't go any further because the
                 * following code assumes the operand is a number
                 */
                return false;
            }

            /*
             * The code below checks to see if the operand in question is within
             * the correct range. The code will not return early b/c otherwise
             * we'd only be able to check a single operand each pass
             */
            /* The numerical value of the operand */
            int val = 0;
            /* STRZ has no numerical value so don't even try to parse it */
            if (this.P != PseudoOps.STRZ) {
                val = Integer.parseInt(this.operands.get(i));
            }
            /* If it isn't an instruction, it's .FILL */
            if (this.I == Instructions.INVALID) {
                /* Only pseudo-op to check is FILL */
                if (this.P == PseudoOps.FILL) {
                    /* The operand for .FILL must be a constant or symbols */
                    if (Operand_Identifier != SYM_OP
                            && Operand_Identifier != CONSTANT_OP) {
                        valid = false;
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                lineNumber);
                    }
                    /* Check within correct range */
                    if (!(val >= MIN_HEX && val <= MAX_HEX)) {
                        valid = false;
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.HEX_VAL_WRONG,
                                lineNumber);
                    }
                }
            } else {
                /*
                 * Every numerical operand that isn't the last operand is a
                 * register
                 */
                if (i != last) {
                    /* If it isn't last, it must be a register or symbol */
                    if (Operand_Identifier != REG_OP
                            && Operand_Identifier != SYM_OP) {
                        valid = false;
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                lineNumber);
                    }
                    if (!(val >= MIN_REG && val <= MAX_REG)) {
                        valid = false;
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.REG_VAL_WRONG,
                                lineNumber);
                    }
                    /*
                     * This cannot be a relocatable symbol because it's not the
                     * final operand
                     */
                    if (checkRelocatableSymbol(symbolTable, operand)) {
                        valid = false;
                        ErrorHandler.queueError(
                                ErrorHandler.ERROR_TYPE.IMPROPER_RELATIVE_SYMBOL,
                                lineNumber);
                    }
                } else {
                    switch (this.I) {
                        /*
                         * If the instruction is AND, ADD or NOT, the last
                         * operand is a register
                         */
                        case AND, ADD, NOT:
                            /* Register operands must be registers or symbols */
                            if (Operand_Identifier != REG_OP
                                    && Operand_Identifier != SYM_OP) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                        lineNumber);
                            }
                            /* Check to be sure it's in range */
                            if (!(val >= MIN_REG && val <= MAX_REG)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.REG_VAL_WRONG,
                                        lineNumber);
                            }
                            /* This cannot be a relative symbol */
                            if (checkRelocatableSymbol(symbolTable, operand)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.IMPROPER_RELATIVE_SYMBOL,
                                        lineNumber);
                            }
                            break;
                        /*
                         * If the instruction is ADDI or ANDI, then the last
                         * operand is an immediate
                         */
                        case ADDI, ANDI:
                            /*
                             * If it's an immediate, it must be a constant or
                             * symbol
                             */
                            if (Operand_Identifier != SYM_OP
                                    && Operand_Identifier != CONSTANT_OP) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                        lineNumber);
                            }
                            /* Check to see it's in range */
                            if (!(val >= IMM5_MIN && val <= IMM5_MAX)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.IMM5_VAL_WRONG,
                                        lineNumber);
                            }
                            /* This cannot be a relative symbol */
                            if (checkRelocatableSymbol(symbolTable, operand)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.IMPROPER_RELATIVE_SYMBOL,
                                        lineNumber);
                            }
                            break;
                        /*
                         * If the instruction is TRAP, the operand is an 8-bit
                         * trap vector
                         */
                        case TRAP:
                            /*
                             * If the operand is TRAP it must be a symbol or
                             * constant
                             */
                            if (Operand_Identifier != CONSTANT_OP
                                    && Operand_Identifier != SYM_OP) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                        lineNumber);
                            }
                            /* Check to see if the trap vec is 8 bits */
                            if (!(val >= TRAP_MIN && val <= TRAP_MAX)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.TRAP_VEC_WRONG,
                                        lineNumber);
                            } else /*
                             * Checks if it's a known trap value. An
                             * unknown trap isn't invalid, but it is
                             * worth a warning
                             */ {
                                boolean known_trap = false;
                                for (Integer II : valid_traps) {
                                    if (II == val) {
                                        known_trap = true;
                                        break;
                                    }
                                }
                                if (!known_trap) {
                                    ErrorHandler.queueError(
                                            ErrorHandler.ERROR_TYPE.UNDEFINED_TRAP,
                                            lineNumber);
                                }
                            }
                            /* Trap vectors cannot be a relative symbol */
                            if (checkRelocatableSymbol(symbolTable, operand)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.IMPROPER_RELATIVE_SYMBOL,
                                        lineNumber);
                            }
                            break;
                        /*
                         * If it's any of these, the last operand is an index6
                         * value
                         */
                        case STR, LDR, JSRR, JMPR:
                            /* Index6 values can only be constants or symbols */
                            if (Operand_Identifier != CONSTANT_OP
                                    && Operand_Identifier != SYM_OP) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                        lineNumber);
                            }
                            /* Check to see if it's in range */
                            if (!(val >= INDEX6_MIN && val <= INDEX6_MAX)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INDEX6_VAL_WRONG,
                                        lineNumber);
                            }
                            break;
                        /*
                         * DBUG and RET have no operands so nothing to check
                         * here
                         */
                        case DBUG, RET:
                            break;
                        /*
                         * For any of these instructions, the last operand is an
                         * addr value
                         */
                        case BR, BRN, BRNP, JSR, JMP, LD, LDI, LEA, ST, STI, BRNZ, BRNZP, BRP, BRZ, BRZP:
                            /*
                             * addr values must be symbols or constants with the
                             * sole exception of LD
                             */
                            if (Operand_Identifier != SYM_OP
                                    && Operand_Identifier != CONSTANT_OP
                                    && !(this.I == Instructions.LD
                                    && Operand_Identifier == LIT_OP)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.INVALID_OPERAND_USAGE,
                                        lineNumber);
                            }
                            /* Check to see if it's in range */
                            if (!(val >= MIN_HEX && val <= MAX_HEX)) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.HEX_VAL_WRONG,
                                        lineNumber);
                                /*
                                 * Check to make sure that the addr is on the
                                 * same page as the instruction
                                 */
                            } else if (!SecondPass.onSamePage((short) val,
                                    (short) (this.locationCounter + 1))) {
                                valid = false;
                                ErrorHandler.queueError(
                                        ErrorHandler.ERROR_TYPE.CROSS_PAGE_REFERENCE,
                                        lineNumber);
                            }
                            break;
                    }
                }
            }
        }
        if(this.externalSymbol.equals("")){
            this.setExternalSymbol(segName);
        }
        return valid;
    }

    /**
     * Checks if a given operand is a relocatable symbol
     *
     * @param symTable
     *            A valid symbol table that may or may not contain operand
     * @param operand
     *            An operand string
     * @return True if the symbol is both present and relocatable, false if
     *         otherwise
     */
    private static boolean checkRelocatableSymbol(SymbolTable symTable,
                                                  String operand) {
        boolean valid = false;
        if (symTable.hasSymbol(operand)) {
            if (symTable.isRelative(operand)) {
                valid = true;
            }
        }
        return valid;
    }

    /**
     * @return A list of all the operands in this line
     */
    public ArrayList<String> getOperands() {
        return this.operands;
    }

    /**
     * Gets the location counter associated with the line
     * @return The location of this line
     */
    public short getLocationCounter() {
        return this.locationCounter;
    }

    /**
     * Gets the instruction associated with the line
     * @return The Instruction associated with this line or Instructions.INVALID
     *         if there is none
     */
    public Instructions getInstr() {
        return this.I;
    }

    /**
     * Gets the pseudoOp associated with the line
     * @return The PseudoOp associated with this line or PseudoOps.INVALID if
     *         there is none
     */
    public PseudoOps getPseudo() {
        return this.P;
    }

    /**
     * Sets this line to relocatable
     */
    public void setRelocatable() {
        this.relocatable = true;
    }

    /**
     * Check if the line is relocatable or not
     * @return A boolean indicating if the line is relocatable
     */
    public boolean isRelocatable() {
        return this.relocatable;
    }

    /**
     * Gets the external symbol associated with the line
     * @return The external symbol associated with the line
     */
    public String getExternalSymbol() {
        return externalSymbol;
    }

    /**
     * Sets the external symbol associated with the line
     * @param externalSymbol
     * 			The external symbol
     */
    public void setExternalSymbol(String externalSymbol) {
        this.externalSymbol = externalSymbol;
    }
}