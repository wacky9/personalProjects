package simulator;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides functionality for various "lower level" bitwise and arithmetic operations
 */
public class Bits {
	
	/**
	 * Mask to get Bits 9-15 of the program counter.
	 */
	private static int progCountMask = 65024;
	/**
	 * Mask to get Bits 0-8 of the immediate.
	 */
	private static int directOffsetMask = 511;
	/**
	 * Mask to get Bits 0-6 of the immediate
	 */
	private static int indexOffsetMask = 63;
	/**
	 * Mask to check if first bit is a 1.
	 */
	private static int bitValueMask = 0x8000;
	
    /**
     * Concatenates page and offset to get a full-sized address
     *
     * @param pc
     *            The program counter
     * @param imm
     *            An immediate 5 value
     * @return The value of the full address as a 16-bit short
     */
    public static short fullAddress(short pc, short imm) {
        short pageHead = (short) (pc & progCountMask);
        short offset = (short) (imm & directOffsetMask);
        return (short) (pageHead + offset);
    }
    
    /**
     * Adds the base register and the 6 bit offset (mask is to remove sign extend issues).
     * 
     * @param base
     * 				The value from the register that serves as the base
     * @param offset
     * 				The 6 bit immediate offset to be masked and added
     * @return The value of the address to go to as a short using index addressing mode
     */
    public static short indexAddress(short base, short offset) {
    	return (short) (base + (offset & indexOffsetMask));
    }

    /**
     * Sign extends the given value based on the 5th bit is a 1
     *
     * @param undersizedValue
     *            The value to sign extend, should be 5 bits or fewer
     * @return The sign-extended value as a short
     */
    public static short signExtend(short undersizedValue) {
        short iterateVal = undersizedValue;
        int i = 0;
        /* This should calculate how many bits are in iterateVal */
        while (iterateVal != 0) {
            iterateVal /= 2;
            i++;
        }
        int diff = 16 - i;
        short fifth = Bits.getBitRange(undersizedValue,4,5);
        if(fifth == 1) {
        	short tempVal = (short) (undersizedValue << diff);
            /*
             * Java does arithmetic right shift, so this will replace equal to the
             * leading value
             */
            return (short) (tempVal >> diff);
        }
        return undersizedValue;
    }

    /**
     * Gets a short with equal bit value to the bits within the specified range
     * of val
     *
     * @param val
     *            The short to get the bits from
     * @param startIndex
     *            The first bit to get (inclusive)
     * @param endIndex
     *            Where to stop getting bits (non-inclusive)
     * @return The short representation of the specified bits. That is to say, a
     *         short with equal mathematical value to the bits specified.
     *         Therefore, this method may return a short with fewer bits than
     *         specified in the case of leading zeroes, for example. All that is
     *         guaranteed is that 0b[exact bits] - return value = 0
     */
    public static short getBitRange(short val, int startIndex, int endIndex) {
        /* How many lower bits to throw out of val */
        int tossAway = 16 - endIndex;
        val = (short) (val << tossAway);
        int diff = endIndex - startIndex;
        short fewerBits = 0;
        for (int i = 0; i < diff; i++) {
            fewerBits = (short) (fewerBits << 1);
            if ((val & bitValueMask) != 0) {
                fewerBits++;
            }
            val = (short) (val << 1);
        }
        return fewerBits;
    }

    /**
     * Takes in a short and returns it as a binary string. Typically
     * Integer.toBinaryString() will automatically sign extend the short and
     * print out 32 bits; not desirable behavior. This method will only return
     * 16 bits
     *
     * @param val
     *            The short to be converted into a binary string
     * @return A string that consists of val converted to binary and is 16 bits
     *         or shorter (and not sign-extended)
     */
    public static String shortBinaryString(short val) {
        return Integer.toBinaryString(Short.toUnsignedInt(val));
    }

    /**
     * Takes in a 4 character hexadecimal string (Omitting the 0x in front) and
     * converts it into a short value assuming the hexadecimal string is valid.
     *
     * @param hexStr
     *            The hexadecimal string to be converted
     * @return Returns a short representation of the 4 character hex string.
     */
    public static short hexStringToShort(String hexStr) {
        return (short) Integer.parseInt(hexStr, 16);
    }

    /**
     * Takes in a short and returns a hex string s with 1 {@literal <}= |s| {@literal <} 4
     *
     * @param num
     *            A valid short to be printed
     * @return A string that has no more chars than necessary
     */
    public static String shortToHexString(short num) {
        String s = Integer.toHexString(num);
        if (s.length() > 4) {
            s = s.substring(s.length() - 4, s.length());
        }
        if (s.length() < 4) {
            int leading = 4 - s.length();
            StringBuilder build = new StringBuilder(s);
            for (int i = 0; i < leading; i++) {
                build.insert(0, ' ');
            }
            s = build.toString();
        }
        return s.toUpperCase();
    }

    /**
     * Takes in an arbitrary length hexadecimal string (Omitting the 0x in
     * front) and evaluates if it is a valid hex string.
     *
     * @param hexStr
     *            The hexadecimal string to be evaluated
     * @return True if the hexadecimal string is a valid hex string with only
     *         digits and upper-case hex characters
     */
    public static boolean isValidHexString(String hexStr) {

        //Only accept upper-case hex characters
        HashSet<Character> hexCharSet = new HashSet<Character>(
                Arrays.asList('A', 'B', 'C', 'D', 'E', 'F'));

        for (int i = 0; i < hexStr.length(); i++) {
            char currentChar = hexStr.charAt(i);

            if (!hexCharSet.contains(currentChar)
                    && !Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }
}