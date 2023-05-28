package assembler;

/**
 * MachineOpTable represents a machine op table used by an assembler. All
 * possible instructions are hard coded in this table along with their sizes,
 * formats, and enumerations for ease of use.
 *
 * @author Toby Simpson
 */
public interface MachineOpTable {
    /**
     * Returns the Instructions enumeration for a given string. If the string is
     * not a valid instruction, then INVALID is returned.
     *
     * @param op
     *            The string of an instruction name
     * @return Returns the Instructions enumeration for the given instr string
     */
    public Instructions getOpCode(String op);

    /**
     * Returns the number of address spaces to increment the location counter by
     * for the given instruction. All instructions increment the location
     * counter by 1.
     *
     * @param instr
     *            The string of an instruction name
     * @return Returns an integer to increment the location counter
     */
    public int getSize(Instructions instr);

    /**
     * Returns an array representing the format for the given instruction. Each
     * instruction can have up to 3 operands. The returned array represents
     * location of each operand in the lower 12 as tuples. The first item in the
     * tuple is what is written, the second item is the number of bits to write
     * to. When the first slot is 0 or 1, that bit is written. When the first
     * slot is -1, those bits are disregarded by the machine. When the first
     * slot is greater than 1, the bits written represent an operand. 2 is the
     * first operand, 3 is the second, 4 is the third.
     *
     * For example: ADD ACC,ACC,#-1 ->
     * [[0,1],[0,1],[0,1],[1,1],[2,3],[3,3],[1,1],[4,5]]
     *
     * @param name
     *            The string of an instruction name
     * @return Returns an integer array representing the instruction format
     */
    public int[][] getFormat(Instructions name);

}