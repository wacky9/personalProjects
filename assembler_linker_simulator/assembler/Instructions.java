package assembler;

/**
 * Enums that represent the different instructions and contain the value of the
 * opcode corresponding to each instruction in the .value field
 */
public enum Instructions {
    ADD((short) 0b0001,"ADD",3),
    ADDI((short) 0b10001,"ADDI",3),
    AND((short) 0b0101,"AND",3),
    ANDI((short) 0b10101,"ANDI",3),
    BR((short) 0b0000,"BR",1),
    BRN((short) 0b0000,"BRN",1),
    BRZ((short) 0b0000,"BRZ",1),
    BRP((short) 0b0000,"BRP",1),
    BRNZ((short) 0b0000,"BRNZ",1),
    BRNP((short) 0b0000,"BRNP",1),
    BRZP((short) 0b0000,"BRZP",1),
    BRNZP((short) 0b0000,"BRNZP",1),
    DBUG((short) 0b1000,"DBUG",0),
    JSR((short) 0b0100,"JSR",1),
    JSRR((short) 0b1100,"JSRR",2),
    JMP((short) 0b10100,"JMP",1),
    JMPR((short) 0b11100,"JMPR",2),
    LD((short) 0b0010,"LD",2),
    LDI((short) 0b1010,"LDI",2),
    LDR((short) 0b0110,"LDR",3),
    LEA((short) 0b1110,"LEA",2),
    NOT((short) 0b1001,"NOT",2),
    RET((short) 0b1101,"RET",0),
    ST((short) 0b0011,"ST",2),
    STI((short) 0b1011,"STI",2),
    STR((short) 0b0111,"STR",3),
    TRAP((short) 0b1111,"TRAP",1),
    INVALID((short)0b11111111,"INVALID",0);

	/**
	 * Value of instruction.
	 */
    short value;

    /**
     * Name of instruction.
     */
    String name;

    /**
     * Number of operands for instruction.
     */
    int operandNum;

    /**
     * Constructor for Instruction enum.
     * @param inherentValue
     *            The binary value that is set for the enum.
     */
    Instructions(short inherentValue, String inherentName, int num) {
        this.value = inherentValue;
        this.name = inherentName;
        this.operandNum = num;
    }
}