package assembler;
/**
 * The Record class stores finalized text records. Each record is stored in both necessary formats: the format
 * for the listing file and the format for the object file
 * @author Winston Basso-Schricker
 * **/
public class Record {
    /**
     * The record stored in hex format with the appropriate markers on it
     */
    private String objectFileText;

    /**
     * The record stored in assembler format, minus all comments
     */
    private String listingFileText;
    /**
     * The Instruction associated with this record or Instructions.INVALID if there is none
     */
    private Instructions I;
    /**
     * Determines whether the given record is a partial record. A partial record is a record that contains either
     * object file text or listing file text but not both. If this variable is true, at least one of ObjectFileText
     * or ListingFileText must be the empty string
     */
    private boolean partialRecord;

    /**
     * Creates a Record object with the default values: empty strings, false and I = INVALID
     */
    public Record(){
        objectFileText = "";
        listingFileText = "";
        partialRecord = false;
        I = Instructions.INVALID;
    }

    /**
     * Creates a Record object with the indicated values
     * @param hexText
     *          The object code of this record
     * @param instructionsText
     *          The line of assembly code corresponding to this record
     * @param isPartial
     *          Whether this record is partial. Should only be true if either hexText or instructionsText is
     *          the empty string
     * @param instr
     *          The instruction associated with this record or Instructions.INVALID if there is none
     */
    public Record(String hexText, String instructionsText, boolean isPartial, Instructions instr){
        objectFileText = hexText;
        listingFileText = instructionsText;
        partialRecord = isPartial;
        I = instr;
    }

    /**
     * Sets the object file text.
     * @return
     *      Gets the object file associated with this record
     */
    public String getObjectFileText(){
        return objectFileText;
    }

    /**
     * Gets the listing file text.
     * @return
     *      Gets the line of assembly associated with this record
     */
    public String getListingFileText(){
        return listingFileText;
    }

    /**
     * Determines whether the record is partial or not.
     * @return
     *      Gets whether this record is partial
     */
    public boolean isPartialRecord() {
        return partialRecord;
    }

    /**
     * Gets the instruction associated with the record.
     * @return
     *      Gets the instruction associated with this record
     */
    public Instructions getInstruction(){return I;}

    /**
     * Sets the object file text.
     * @param hexText
     *      The object code associated with this record
     */
    public void setObjectFileText(String hexText){
        objectFileText = hexText;
    }

    /**
     * Sets the listing file text.
     * @param instructionsText
     *      The line of assembly associated with this record
     */
    public void setListingFileText(String instructionsText){
        listingFileText = instructionsText;
    }

    /**
     *  Changes this record to be a partial record. Only use if either the object file or listing file
     *  text is the empty string
     */
    public void setToPartial() {
        partialRecord = true;
    }

    /**
     * @param instr
     *      The instruction associated with this record
     */
    public void setInstruction(Instructions instr){
        I = instr;
    }


    /**
     * Checks if two record objects are equal.
     * @param obj
     *  An object to be tested for equality
     * @return
     *      True if obj is a Record object with the same ObjectFileText, the same ListingFileText, and
     *      the same Instruction
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }

        if(obj.getClass() != this.getClass()){
            return false;
        }

        final Record other = (Record)obj;

        return this.getObjectFileText().equals(other.getObjectFileText()) &&
                this.getListingFileText().equals(other.getListingFileText())
                && this.getInstruction() == other.getInstruction();
    }
}
