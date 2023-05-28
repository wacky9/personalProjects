package assembler;

import java.util.HashMap;

/**
 * Implementation of MachineOpTable interface.
 *
 * @author Toby Simpson
 */
public class MachineOpTable1 implements MachineOpTable {

    /**
     * Custom data type for the value in the machine op HashMap.
     *
     * @author Toby Simpson
     */
    private static class MachineOpData {
        /**
         * Size to increment the location counter by.
         */
        int size;
        /**
         * Binary format of the machine code for the operation.
         */
        int[][] format;

        /**
         * MachineOpData Constructor.
         *
         * @param s
         *            Size of the machine op
         * @param f
         *            Binary format of the machine operation
         */
        public MachineOpData(int s, int[][] f) {
            // Initialize the values of the pair
            this.size = s;
            this.format = f;
        }
    }

    /**
     * HashMap for the op name key and size/format value pairs.
     */
    private HashMap<Instructions, MachineOpData> table;

    /**
     * MachineOpTable1 Constructor. Fills the table with all machine ops and an
     * invalid operation.
     */
    public MachineOpTable1() {
        // Initialize the HashMap
        this.table = new HashMap<>();

        // Add entries for all operations
        this.table.put(Instructions.ADD, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { Constants.FIRST_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { Constants.SECOND_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { 0, 1 }, { -1, 2 }, { Constants.THIRD_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.ADDI, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { Constants.FIRST_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { Constants.SECOND_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { 1, 1 }, { Constants.THIRD_OPERAND_INDEX,
                                Constants.IMMEDIATE_5_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.AND, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 1, 1 }, { 0, 1 }, { 1, 1 },
                        { Constants.FIRST_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { Constants.SECOND_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { 0, 1 }, { -1, 2 }, { Constants.THIRD_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.ANDI, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 1, 1 }, { 0, 1 }, { 1, 1 },
                        { Constants.FIRST_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { Constants.SECOND_OPERAND_INDEX,
                                Constants.REGISTER_OPERAND_BIT_LENGTH },
                        { 1, 1 }, { Constants.THIRD_OPERAND_INDEX,
                                Constants.IMMEDIATE_5_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BR, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 },
                        { 0, 1 }, { 0, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRN, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { 0, 1 }, { 0, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRNZ, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { 1, 1 }, { 0, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRNP, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { 0, 1 }, { 1, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRNZP, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { 1, 1 }, { 1, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRZ, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 },
                        { 1, 1 }, { 0, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRZP, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 },
                        { 1, 1 }, { 1, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.BRP, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 },
                        { 0, 1 }, { 1, 1 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.DBUG, new MachineOpData(1, new int[][] {
                { 1, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 }, { -1, 12 } }));
        this.table.put(Instructions.JSR, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 1, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                        { -1, 2 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.JSRR,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 1, 1 }, { 0, 1 }, { 0, 1 },
                                { 1, 1 }, { -1, 2 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX, 6 } }));
        this.table.put(Instructions.JMP, new MachineOpData(1,
                new int[][] { { 0, 1 }, { 1, 1 }, { 0, 1 }, { 0, 1 }, { 0, 1 },
                        { -1, 2 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.JMPR,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 1, 1 }, { 0, 1 }, { 0, 1 },
                                { 0, 1 }, { -1, 2 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX, 6 } }));
        this.table.put(Instructions.LD,
                new MachineOpData(1,
                        new int[][] { { 0, 1 }, { 0, 1 }, { 1, 1 }, { 0, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.LDI,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 0, 1 }, { 1, 1 }, { 0, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.LDR,
                new MachineOpData(1,
                        new int[][] { { 0, 1 }, { 1, 1 }, { 1, 1 }, { 0, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.THIRD_OPERAND_INDEX, 6 } }));
        this.table.put(Instructions.LEA,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 1, 1 }, { 1, 1 }, { 0, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.NOT,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 0, 1 }, { 0, 1 }, { 1, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { -1, 6 } }));
        this.table.put(Instructions.RET, new MachineOpData(1, new int[][] {
                { 1, 1 }, { 1, 1 }, { 0, 1 }, { 1, 1 }, { -1, 12 } }));
        this.table.put(Instructions.ST,
                new MachineOpData(1,
                        new int[][] { { 0, 1 }, { 0, 1 }, { 1, 1 }, { 1, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.STI,
                new MachineOpData(1,
                        new int[][] { { 1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.ADDR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.STR,
                new MachineOpData(1,
                        new int[][] { { 0, 1 }, { 1, 1 }, { 1, 1 }, { 1, 1 },
                                { Constants.FIRST_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.SECOND_OPERAND_INDEX,
                                        Constants.REGISTER_OPERAND_BIT_LENGTH },
                                { Constants.THIRD_OPERAND_INDEX, 6 } }));
        this.table.put(Instructions.TRAP,
                new MachineOpData(1, new int[][] { { 1, 1 }, { 1, 1 }, { 1, 1 },
                        { 1, 1 }, { -1, 4 }, { Constants.FIRST_OPERAND_INDEX,
                                Constants.TRAP_VECTOR_OPERAND_BIT_LENGTH } }));
        this.table.put(Instructions.INVALID,
                new MachineOpData(-1, new int[][] { {} }));
    }

    @Override
    public int getSize(Instructions instr) {
        // Return the size of the operation in the HashMap
        return this.table.get(instr).size;
    }

    @Override
    public int[][] getFormat(Instructions instr) {
        // Return the format of the operation in the HashMap
        return this.table.get(instr).format;
    }

    @Override
    public Instructions getOpCode(String op) {
        // Return the enum representation of the string
        if (op.equals("ADD")) {
            return Instructions.ADD;
        } else if (op.equals("ADDI")) {
            return Instructions.ADDI;
        } else if (op.equals("AND")) {
            return Instructions.AND;
        } else if (op.equals("ANDI")) {
            return Instructions.ANDI;
        } else if (op.equals("BR")) {
            return Instructions.BR;
        } else if (op.equals("BRN")) {
            return Instructions.BRN;
        } else if (op.equals("BRNZ")) {
            return Instructions.BRNZ;
        } else if (op.equals("BRNP")) {
            return Instructions.BRNP;
        } else if (op.equals("BRNZP")) {
            return Instructions.BRNZP;
        } else if (op.equals("BRZ")) {
            return Instructions.BRZ;
        } else if (op.equals("BRZP")) {
            return Instructions.BRZP;
        } else if (op.equals("BRP")) {
            return Instructions.BRP;
        } else if (op.contains("DBUG")) {
            return Instructions.DBUG;
        } else if (op.equals("JSR")) {
            return Instructions.JSR;
        } else if (op.equals("JSRR")) {
            return Instructions.JSRR;
        } else if (op.equals("JMP")) {
            return Instructions.JMP;
        } else if (op.equals("JMPR")) {
            return Instructions.JMPR;
        } else if (op.equals("LD")) {
            return Instructions.LD;
        } else if (op.equals("LDI")) {
            return Instructions.LDI;
        } else if (op.equals("LDR")) {
            return Instructions.LDR;
        } else if (op.equals("LEA")) {
            return Instructions.LEA;
        } else if (op.equals("NOT")) {
            return Instructions.NOT;
        } else if (op.equals("RET")) {
            return Instructions.RET;
        } else if (op.equals("ST")) {
            return Instructions.ST;
        } else if (op.equals("STI")) {
            return Instructions.STI;
        } else if (op.equals("STR")) {
            return Instructions.STR;
        } else if (op.equals("TRAP")) {
            return Instructions.TRAP;
        } else {
            return Instructions.INVALID;
        }
    }

}
