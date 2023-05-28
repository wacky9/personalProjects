package assembler;

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
     * Concatenates page and offset to get a full-sized address
     *
     * @param pc
     *            The program counter
     * @param pgOffest
     *            A pageOffset value
     * @return The value of the full address as a 16-bit short
     */
    public static short fullAddress(short pc, short pgOffest) {
        short pageHead = (short) (pc & progCountMask);
        short offset = (short) (pgOffest & directOffsetMask);
        return (short) (pageHead + offset);
    }

    /**
     * Adds the base register and the 6 bit offset (mask is to remove sign
     * extend issues).
     *
     * @param base
     *            The value from the register that serves as the base
     * @param offset
     *            The 6 bit immediate offset to be masked and added
     * @return The value of the address to go to as a short using index
     *         addressing mode
     */
    public static short indexAddress(short base, short offset) {
        return (short) (base + (offset & indexOffsetMask));
    }

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
        short fifth = Bits.getBitRange(undersizedValue, 4, 5);
        if (fifth == 1) {
            short tempVal = (short) (undersizedValue << diff);
            /*
             * Java does arithmetic right shift, so this will replace equal to
             * the leading value
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
     * Formats binary according to desired format
     *
     * @param hexText
     *            The instruction to be formatted, in hex
     * @param format
     *            The format to put the binary instruction into
     * @return A binary string with possible chars = {1,0,_}. If the string has
     *         _ removed and replaced either by spaces or 0's depending on
     *         context, it will be mathematically equal to the hexText string
     *         converted into an integer.
     */
    public static String formatBinaryInstruction(String hexText,
            int[][] format) {
        StringBuilder stringConstructor = new StringBuilder();
        short instruction = hexStringToShort(hexText);
        stringConstructor.append(
                paddedBinaryShortString(getBitRange(instruction, 12, 16), 4));
        stringConstructor.append('_');
        int index = 12;
        /* Format the operands. Starts at 4 to avoid opcode information */
        for (int i = 4; i < format.length; i++) {
            int length = format[i][1];
            int check = format[i][0];
            /* If the value is irrelevant, replace with underscores */
            if (check == -1) {
                for (int ii = 0; ii < length; ii++) {
                    stringConstructor.append('x');
                }
            } else {
                String binarySegment = paddedBinaryShortString(
                        getBitRange(instruction, index - length, index),
                        length);
                stringConstructor.append(binarySegment);
            }
            stringConstructor.append('_');
            index -= length;
        }
        /* Get rid of the last underscore */
        stringConstructor.deleteCharAt(stringConstructor.length() - 1);
        /* trim() removes any unnecessary whitespace */
        return stringConstructor.toString().trim();
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
     * @return Tru if it is valid and in the range and false otherwise
     */
    public static boolean isValidHexStringInFFFFRange(String hexStr) {

        return isValidHexString(hexStr) && hexStr.length() < 6;
    }

    /**
     * Returns whether a decimal string (Including the #) is valid and in the
     * range: [#-32678, #32677]
     *
     * @param decStr
     * 		The decimal string
     * @return
     * 		Whether the decimal string is in the valid range.
     */
    public static boolean isValidDecStringInShortRange(String decStr) {
        boolean success = false;
        if (decStr.length() > 0 && decStr.charAt(0) == '#') {

            try {
                int numVal = Integer.parseInt(decStr.substring(1));
                success = (numVal >= Short.MIN_VALUE
                        && numVal <= Short.MAX_VALUE);
            } catch (NumberFormatException exception) {
                success = false;
            }

        }
        return success;
    }

    /**
     * Returns whether or not the given string represents a value that is valid
     * as an operand for .BLKW. Valid values are in [x1, xFFFF].
     *
     * @param constStr
     *            The operand whose value is to be checked
     * @return True if constStr in [x1, xFFFF], false otherwise
     */
    public static boolean isInValidBLKWRange(String constStr) {
        boolean success = false;

        if (isLiteralOrImmediate(constStr)) {
            int constVal = parseLiteralOrImmediate(constStr);
            if (constVal > 0 && constVal <= 65535) {
                success = true;
            }
        }

        return success;

    }

    /**
     * Returns whether or not the given string represents is a valid integer
     * string.
     *
     * @param intStr
     *            A non-empty string
     * @return Whether the string is a valid integer string
     */
    public static boolean isValidIntegerString(String intStr) {
        boolean isValid = true;
        /* The first character can be '-' without error */
        if ('-' == intStr.charAt(0)) {
            intStr = intStr.substring(1);
        }
        for (int i = 0; i < intStr.length(); i++) {
            char c = intStr.charAt(i);
            if (!Character.isDigit(c)) {
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Returns whether or not the given string is a literal value or an
     * immediate value.
     *
     * @param potentialValue
     *            A string that is not null
     * @return Whether the string is a literal or immediate
     */
    public static boolean isLiteralOrImmediate(String potentialValue) {
        String potential = potentialValue;
        boolean isValid = false;
        /* A literal or immediate string needs at least 3 chars to be valid */
        if (potentialValue.length() < 2) {
            return false;
        }
        char firstChar = potential.charAt(0);
        /* If it's a literal, trim off the '=' */
        if (firstChar == '=') {
            potential = potential.substring(1);
        }
        char secondChar = potential.charAt(0);
        if (secondChar == 'x') {
            isValid = isValidHexString(potential);
        } else if (secondChar == '#') {
            String intStr = potential.substring(1);
            isValid = isValidIntegerString(intStr);
        }
        return isValid;
    }

    /**
     * Parses a literal or immediate value into a short
     *
     * @param literal
     *            A valid literal or immediate string (one equals sign,x or #,
     *            numerical value)
     * @return The value in the literal string parsed
     */
    public static int parseLiteralOrImmediate(String literal) {
        char firstChar = literal.charAt(0);
        int val;
        if (firstChar == '=') {
            val = parseLiteralOrImmediate(literal.substring(1));
        } else {
            if (firstChar == 'x') {
                val = Integer.parseInt(literal.substring(1), 16);
            } else {
                val = Integer.parseInt(literal.substring(1), 10);
            }
        }
        return val;
    }

    /**
     * Returns the binary representation of the given short. The binary string
     * is sign extended or trimmed to be the given size.
     *
     * @param num
     *            The short to convert to a binary string
     * @param size
     *            The desired number of chars. Any higher is trimmed, lower is
     *            padded. Should be lower than 16 although not strictly
     *            necessary
     * @return A string of 1's or 0's that is equivalent to the mathematical
     *         representation of num and is exactly the length of size.
     */
    public static String paddedBinaryShortString(short num, int size) {
        String binary = shortBinaryString(num);
        String result;
        int diff = binary.length() - size;
        if (diff > 0) {
            /* Need to trim first -diff chars */
            result = binary.substring(diff);
        } else if (diff < 0) {
            /* Add diff leading 0's */
            result = "0".repeat(-diff) + binary;
        } else {
            result = binary;
        }
        return result;
    }
}
