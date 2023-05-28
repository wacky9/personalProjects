package assembler;

/**
 * SymbolTable represents a symbol table used by an assembler. Pass 1 will write
 * all symbols and their values to the table; whereas, pass 2 will read from the
 * table to replace symbols in the code with their values.
 *
 * @author Toby Simpson
 */
public interface SymbolTable {
    /**
     * Adds a symbol along with its value. The value must been known when
     * inserting.
     *
     * @param name
     *            The name of the symbol to add to the table
     */
    public void put(String name, int value, boolean relative);

    /**
     * Returns the value of the given symbol that is already in the table.
     *
     * @param name
     *            The name of the symbol to add to the table
     * @return Returns the value of the symbol.
     */
    public int getVal(String name);

    /**
     * Returns whether or not the symbol is relative.
     *
     * @param name
     *            The name of the symbol to add to the table
     * @return Returns true if the symbol is relative, false if not
     */
    public boolean isRelative(String name);

    /**
     * Returns whether or not the symbol is in the table.
     *
     * @param name
     *            The name of the symbol to add to the table
     * @return Returns true if the symbol is in the table, false if not
     */
    public boolean hasSymbol(String name);

    /**
     * Prints the contents of the Symbol Table.
     */

    public void debug();
}