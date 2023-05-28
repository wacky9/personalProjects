package assembler;

/**
 * Implementation of PseudoOpTable interface.
 *
 * @author Toby Simpson
 */
public class PseudoOpTable1 implements PseudoOpTable {

    /**
     * PseudoOpTable1 Constructor.
     */
    public PseudoOpTable1() {
        /*
         * Nothing needs to be initialized. Class is not static to fit the same
         * format of the other tables.
         */
    }

    @Override
    public int getSize(PseudoOps pseudoOp) {
        // Return the size to increment the location counter by
        switch (pseudoOp) {
            case ORIG:
            case END:
            case EQU:
                return 0;
            case FILL:
                return 1;
            case STRZ:
            case BLKW:
            default:
                return -1;
        }
    }

    @Override
    public PseudoOps getPseudoOp(String op) {
        // Return the enum representation of the string
        if (op.equals(".ORIG")) {
            return PseudoOps.ORIG;
        } else if (op.equals(".END")) {
            return PseudoOps.END;
        } else if (op.equals(".EQU")) {
            return PseudoOps.EQU;
        } else if (op.equals(".FILL")) {
            return PseudoOps.FILL;
        } else if (op.equals(".STRZ")) {
            return PseudoOps.STRZ;
        } else if (op.equals(".BLKW")) {
            return PseudoOps.BLKW;
        } else if (op.equals(".ENT")) {
        	return PseudoOps.ENT;
        } else if (op.equals(".EXT")) {
        	return PseudoOps.EXT;
        } else {
            return PseudoOps.INVALID;
        }
    }


}

