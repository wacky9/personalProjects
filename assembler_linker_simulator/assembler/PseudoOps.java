package assembler;

/**
 * Enums that represent the different pseudo ops.
 */
public enum PseudoOps {
    ORIG((short) 0), END((short) 1), EQU((short) 2), FILL((short) 3), STRZ(
            (short) 4), BLKW((short) 5), ENT((short) 6), EXT((short) 7), INVALID((short) -1);

    /**
     * Value of instruction.
     */
    short value;

    /**
     * Short constructor for PseudoOp enum.
     *
     * @param inherentValue
     *            The binary value that is set for the enum.
     */
    PseudoOps(short inherentValue) {
        this.value = inherentValue;
    }

}
