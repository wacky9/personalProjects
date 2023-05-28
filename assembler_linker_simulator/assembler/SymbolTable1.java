package assembler;

import java.util.HashMap;

/**
 * Implementation of SymbolTable interface.
 *
 * @author Toby Simpson
 */
public class SymbolTable1 implements SymbolTable {

    /**
     * Custom data for type the value in the symbol HashMap.
     *
     * @author Toby Simpson
     */
    private class SymbolData {
        /**
         * Value of the symbol.
         */
        int value;
        /**
         * Whether the value of the symbol is relative or not.
         */
        boolean relative;

        /**
         * SymbolData Constructor.
         *
         * @param v
         *            The value of the symbol
         * @param r
         *            Whether the symbol is relative or not
         */
        public SymbolData(int v, boolean r) {
            // Initialize the values of the pair
            this.value = v;
            this.relative = r;
        }

        public String toString(){
            return value + "--" + relative;
        }
    }

    /**
     * HashMap for the symbol name key and value/relative value pairs.
     */
    private HashMap<String, SymbolData> table;

    /**
     * SymbolTable1 Constructor.
     */
    public SymbolTable1() {
        // Initialize the HashMap
        this.table = new HashMap<>();
    }

    @Override
    public void put(String name, int value, boolean relative) {
        // Create a data pair of the value and relative boolean
        SymbolData data = new SymbolData(value, relative);

        // Add the symbol entry to the table
        this.table.put(name, data);
    }

    @Override
    public int getVal(String name) {
        // Return the value of the symbol
        return this.table.get(name).value;
    }

    @Override
    public boolean isRelative(String name) {
        // Return whether the symbol is relative or not
        return this.table.get(name).relative;
    }

    @Override
    public boolean hasSymbol(String name) {
        // Return whether the symbol is in the table or not
        return this.table.containsKey(name);
    }

    public void debug(){
        table.forEach((k,v) -> {
            System.out.print(k + ": [");
            System.out.println(v+ "]");
        });
    }

}

