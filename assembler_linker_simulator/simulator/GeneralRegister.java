package simulator;

import java.util.Random;

/**
 * Represents a general register for the machine. For example: R0, R1, R2 ...
 */
public class GeneralRegister extends Register {

    /**
     * Whether the register has been modified by an instruction.
     */
    boolean modified;

    /**
     * The maximum unsigned short value.
     */
    private int Exclusive_UShort_Max = 65536;

    /**
     * The maximum signed short value.
     */
    private int Exclusive_Short_Max = 32768;

    /**
     * Creates a new general register
     *
     * @param index
     *            The number of the register
     */
    public GeneralRegister(int index) {
        super("" + index);
    }

    /**
     * Sets the value of the register to a random (or semi-random) value
     *
     * @return A random value between 0 and 32,767
     */
    public short randomize() {
        Random rand = new Random();
        int rndNum = rand
                .nextInt(this.Exclusive_UShort_Max - this.Exclusive_Short_Max);
        this.value = (short) rndNum;
        this.modified = true;
        return this.value;
    }

    /**
     * Sets the value of the register to the given value And set modified status
     * to true
     *
     * @param newVal
     *            The value to set the register to
     */
    @Override
    public void setVal(short newVal) {
        super.setVal(newVal);
        this.modified = true;
    }

    /**
     * Checks whether the register has been modified, and sets the modified
     * status to false.
     *
     *
     * @return Whether the register has been modified
     */
    public boolean getModified() {
        boolean modified = this.modified;
        this.modified = false;
        return modified;
    }

    /**
     * Return the string representation of the general register.
     *
     * @return The name and bit value of the register as a string
     */
    @Override
    public String toString() {
        /*
         * Converts it to a binary string of int length and zeroes out the top 2
         * bytes so that they do not print
         */
        return "R" + this.name + ": "
                + Integer.toBinaryString(0x0000FFFF & this.value) + " ("
                + this.value + ")";
    }
}
