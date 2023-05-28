package assembler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implementation of LiteralTable interface.
 *
 * @author Toby Simpson
 */
public class LiteralTable1 implements LiteralTable {

    /**
     * Custom data type for the value in the literal HashMap.
     *
     * @author Toby Simpson
     */
    private class LiteralData {
        /**
         * Hex string value of the literal
         */
        String value;
        /**
         * Address of the literal
         */
        int address;

        /**
         * LiteralData Constructor.
         *
         * @param v
         *            The hex string value of the literal
         */
        public LiteralData(String v) {
            // Initialize the values of the pair
            this.value = v;
            this.address = -1;
        }

        /**
         * Returns a string representation of the data pair.
         *
         * @return The string representation of the value and address stored in
         *         this pair.
         */
        @Override
        public String toString() {
            return this.value + "--" + this.address;
        }
    }

    /**
     * HashMap for the literal name key and value/address value pairs.
     */
    private HashMap<String, LiteralData> table;
    /**
     * ArrayList to store literal names in the order of when they are added to
     * the HashMap.
     */
    ArrayList<String> literals;

    /**
     * LiteralTable1 Constructor.
     */
    public LiteralTable1() {
        // Initialize the HashMap and ArrayList
        this.table = new HashMap<>();
        this.literals = new ArrayList<>();
    }

    @Override
    public void put(String name) {
        // Disregard the first 2 characters of the literal, ie. "=x" of "=#"
        String hexValue = name.substring(2);

        // If the literal is an int, convert the string to hex
        if (name.charAt(1) == '#') {
            // Set to all upper case letters
            hexValue = Integer.toHexString(Integer.parseInt(hexValue))
                    .toUpperCase();
        }

        // Only save the last 4 characters; cuts signed F's after the 4th digit
        if (hexValue.length() > Constants.WORD_HEX_LENGTH) {
            hexValue = hexValue
                    .substring(hexValue.length() - Constants.WORD_HEX_LENGTH);

        }

        // Add leading 0's to obtain a uniform length
        while (hexValue.length() < Constants.WORD_HEX_LENGTH) {
            hexValue = "0" + hexValue;
        }

        // Create a LiteralData pair to store in the HashMap
        LiteralData unitialized = new LiteralData(hexValue);

        // Add the literal and data to the HashMap
        this.table.put(name, unitialized);

        // Add the literal name to the end of the ArrayList to track order
        this.literals.add(name);
    }

    @Override
    public void setAddress(String name, int address) {
        // Modify the address of the literal's LiteralData pair
        LiteralData dataToModify = this.table.get(name);
        dataToModify.address = address;
    }

    @Override
    public String getVal(String name) {
        // Return the hex string value of the literal
        return this.table.get(name).value;
    }

    @Override
    public int getAddress(String name) {
        // Return the address to store the literal in memory
        return this.table.get(name).address;
    }

    @Override
    public boolean hasLiteral(String name) {
        // Return whether or no the literal is in the HashMap
        return this.table.containsKey(name);
    }

    @Override
    public ArrayList<String> getLiterals() {
        // Return an iterable list of the literal in the HashMap
        return this.literals;
    }

    @Override
    public void debug() {
        this.table.forEach((k, v) -> {
            System.out.print(k + ": [");
            System.out.println(v + "]");
        });
    }
}
