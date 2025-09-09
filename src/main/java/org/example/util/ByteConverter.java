package org.example.util;

public class ByteConverter {
    public static byte stringToByte(String input) throws NumberFormatException {
        try {
            int value = Integer.parseInt(input);
            if (value < 0 || value > 255) {
                throw new NumberFormatException("Value must be between 0 and 255");
            }
            return (byte) value;
        } catch (NumberFormatException e) {
            if (input.length() == 1) {
                return (byte) input.charAt(0);
            }
            throw new NumberFormatException("Invalid input: " + input);
        }
    }

    public static String byteToString(byte b) {
        return Byte.toString(b);
    }

    public static String byteToHexString(byte b) {
        return String.format("%02X", b);
    }

    public static String byteToBinaryString(byte b) {
        return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }
}