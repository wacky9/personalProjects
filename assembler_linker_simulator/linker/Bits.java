package linker;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides functionality for various "lower level" bitwise and arithmetic
 * operations
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
     * Splits an address into a 7 bit PC and a 9 bit offset
     *
     * @param address
     *            The address to be split
     * @return An array where arr[0] = page and arr[1] = offset
     */
    public static short[] pageOffsetSplit(short address) {
        short[] split = new short[2];
        split[0] = getBitRange(address, 9, 16);
        split[1] = getBitRange(address, 0, 9);
        return split;
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
     * Takes in a short and returns a hex string s with |s| = 4
     *
     * @param num
     *            A valid short to be printed
     * @return A string that has exactly 4 chars
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
                build.insert(0, '0');
            }
            s = build.toString();
        }
        return s.toUpperCase();
    }

    /**
     * Takes in an arbitrary length hexadecimal string (With the x in front) and
     * evaluates if it is a valid hex string.
     *
     * @param hexStr
     *            The hexadecimal string to be evaluated
     * @return True if the hexadecimal string is a valid hex string with only
     *         digits and upper-case hex characters
     */
    public static boolean isValidHexString(String hexStr) {

        //Initial check to see that the x is in front.
        if (hexStr.length() < 2 || hexStr.charAt(0) != 'x') {
            return false;
        }

        //Get the modified hex string without the 'x'
        String modifiedHexStr = hexStr.substring(1);

        //Only accept upper-case hex characters
        HashSet<Character> hexCharSet = new HashSet<Character>(
                Arrays.asList('A', 'B', 'C', 'D', 'E', 'F'));

        for (int i = 0; i < modifiedHexStr.length(); i++) {
            char currentChar = modifiedHexStr.charAt(i);

            if (!hexCharSet.contains(currentChar)
                    && !Character.isDigit(currentChar)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a hex string is valid and in the range: [x0, xFFFF]
     *
     * @param hexStr
     *            A hex string.
     * @return True if it is valid and in the range and false otherwise
     */
    public static boolean isValidHexStringInFFFFRange(String hexStr) {

        return isValidHexString(hexStr) && hexStr.length() < 6;
    }

    /**
     * Determines if two given addresses are on the same page
     *
     * @param addressOne
     *            The first address
     * @param addressTwo
     *            The second address
     * @return True if the two addresses are on the same page, false otherwise
     */
    public static boolean onSamePage(short addressOne, short addressTwo) {
        short pageOne = pageOffsetSplit(addressOne)[0];
        short pageTwo = pageOffsetSplit(addressTwo)[0];
        return pageOne == pageTwo;
    }
}
