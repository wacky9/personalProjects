package assembler;

import java.util.ArrayList;

/**
 * LiteralTable represents a literal table used by an assembler. Pass 1 will
 * write all literals and their addresses to the table; whereas, Pass2 will read
 * from the table to replace literals in the code with their addresses.
 *
 * @author Toby Simpson
 */
public interface LiteralTable {
    /**
     * Adds a literal to the table and calculates its hex string representation.
     * The key must start with "=x" or "=#".
     *
     * @param name
     *            The name of the literal to add to the table
     */
    public void put(String name);

    /**
     * Sets the address of the given literal.
     *
     * @param name
     *            The name of the literal whose address is being set. There must
     *            be a literal 'name' in the table
     * @param address
     *            The integer address to assign to the literal. 0 {@literal <}= address {@literal <}=
     *            65,535 and no other literal may have the same assigned address
     */
    public void setAddress(String name, int address);

    /**
     * Returns the hex string representation of the given literal.
     *
     * @param name
     *            The name of the literal to get the value of
     * @return Returns the 4 character hex string translation of the literal
     */
    public String getVal(String name);

    /**
     * Returns the address where the given literal is stored in memory.
     *
     * @param name
     *            The name of the literal to get the address of
     * @return Returns an address where the literal is stored in memory 0 {@literal <}=
     *         getAddress {@literal <}= 65,535
     */
    public int getAddress(String name);

    /**
     * Returns whether or not the given literal exists in the literal table.
     *
     * @param name
     *            The name of the literal to check the table for
     * @return Returns true if the literal is in the table, false if not.
     */
    public boolean hasLiteral(String name);

    /**
     * Returns an ArrayList with all the literals in order. The returned list
     * can be iterated over.
     *
     * @return Returns an ArrayList of strings with all the literal names in
     *         order of when they were added.
     */
    public ArrayList<String> getLiterals();

    /**
     * Debug method that prints the contents of the literal table.
     */
    public void debug();
}