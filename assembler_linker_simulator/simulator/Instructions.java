package simulator;

/**
 * Enums that represent the different instructions and contain the value of the
 * opcode corresponding to each instruction in the .value field
 */
public enum Instructions {
    ADD((short) 0b0001), 
    AND((short) 0b0101), 
    BRX((short) 0b0000), 
    DBUG((short) 0b1000), 
    JSR((short) 0b0100), 
    JSRR((short) 0b1100),
    JMP((short) 0b10000),
    JMPR((short) 0b10001 ),
    LD((short) 0b0010), 
    LDI((short) 0b1010), 
    LDR((short) 0b0110), 
    LEA((short) 0b1110), 
    NOT((short) 0b1001), 
    RET((short) 0b1101), 
    ST((short) 0b0011), 
    STI((short) 0b1011), 
    STR((short) 0b0111), 
    TRAP((short) 0b1111), 
    NOEXE((short) 0b11111110), 
    HALT((short) 0b00100101), 
    OUT((short) 0x21), 
    PUTS((short) 0x22), 
    IN((short) 0x23), 
    OUTN((short) 0x31), 
    INN((short) 0x33), 
    RND((short) 0x43);

	/**
	 * Value of instruction.
	 */
    short value;
    
    /**
     * Constructor for Instruction enum.
     * @param inherentValue
     * 		The binary value that is set for the enum.
     */
    Instructions(short inherentValue) {
        this.value = inherentValue;
    }
}