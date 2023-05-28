package simulator;

/**
 * Abstract Register class to provide skeleton for condition registers, general
 * registers, and program counters
 */
public abstract class Register {

    /**
     * The numerical quantity held in a Register.
     */
    short value;

    /**
     * The name of a Register.
     */
    String name;

    /**
     * Creates a new generic register
     *
     * @param name
     *            Name of the register
     */
    public Register(String name) {
        this.value = 0;
        this.name = name;
    }

    /**
     * Sets the value of the register to the given value
     *
     * @param newVal
     *            The value to set the register to
     */
    public void setVal(short newVal) {
        this.value = newVal;
    }

    /**
     * Gets the value of the register
     *
     * @return The current value of the register
     */
    public short getVal() {
        return this.value;
    }

    /**
     * Gets the name of the register.
     *
     * @return The name of the register
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a string representation of the register.
     *
     * @return The name and bit value of the register as a string
     */
    @Override
    public String toString() {
        /*
         * Converts it to a binary string of int length and zeroes out the top 2
         * bytes so that they do not print
         */
        //Change to using the Bits method for this
        return this.name + ": "
                + Integer.toBinaryString(0x0000FFFF & this.value);
    }
}