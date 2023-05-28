package simulator;

/**
 * Represents and provides functionality for the program counter of the
 * simulated machine
 */
public class ProgramCounter extends Register {

    /**
     * The current address held in the program counter.
     */
    int address;

    /**
     * Creates a new program counter
     */
    public ProgramCounter() {
        super("PC");
    }

    /**
     * Increases the value of the program counter by 1. Does not increment if at
     * max value
     */
    public void increment() {
        if (this.address != 0xFFFF) {
            this.address++;
        }
    }

    /**
     * Sets the address value held by the program counter.
     *
     * @param newAddress
     *            New address to set the program counter to
     */
    public void setAddress(int newAddress) {
        if (newAddress >= 0 && newAddress <= 0xFFFF) {
            this.address = newAddress;
        }
    }

    /**
     * Gets the value of the register
     *
     * @return The current value of the register
     */
    public int getAddress() {
        return this.address;
    }

    /**
     * Returns a string representation of the program counter.
     *
     * @return The name and bit value of the register as a string
     */
    @Override
    public String toString() {
        /*
         * Converts it to a binary string of int length and zeroes out the top 2
         * bytes so that they do not print
         */
        return this.name + ": "
                + Integer.toHexString(this.address).toUpperCase();
    }
}
