package assembler;

/**
 * PseudoOpTable represents a machine op table used by an assembler. All
 * possible instructions are hard coded in this table along with their sizes,
 * formats, and enumerations for ease of use.
 *
 * @author Toby Simpson
 */

public interface PseudoOpTable {

    /**
     * Returns the number of addresses the location counter should be
     * incremented have the given pseudo op.
     *
     * Returns 1 for pseudo ops that increment the location counter by 1
     * address; 0 for pseudo ops that do not increment the loction counter; -1
     * for pseudo ops that increment the location by an operand dependent value
     * or invalid pseudo ops.
     *
     * If -1 is returned, the operand must be parsed.
     *
     * @param pseudoOp
     *            The specific pseudo op to find the size of
     * @return Returns an integer in [-1,0,1] to increment the location counter
     */
    public int getSize(PseudoOps pseudoOp);
    

    /**
     * Returns the PseudoOps enumeration for a given string. If the string is
     * not a valid pseudo op, then INVALID is returned.
     *
     * @param op
     *            The string of a pseudo op name including "." at the beginning
     * @return Returns the PseudoOps enumeration for the given op string
     */
    public PseudoOps getPseudoOp(String op);


}