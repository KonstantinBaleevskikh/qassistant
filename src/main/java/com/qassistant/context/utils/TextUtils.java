package com.qassistant.context.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class TextUtils {

    /**
     * Generates a SHA-256 hash for the given string.
     * @param input The string to hash.
     * @return The SHA-256 hash in hexadecimal format.
     */
    public static String generateSha256(String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception exception) {
            throw new RuntimeException("Error computing SHA-256 checksum", exception);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation of the byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Checks if the input string contains any Cyrillic characters.
     * @param input The string to check.
     * @return true if the string contains Cyrillic characters, false otherwise.
     */
    public static boolean containsCyrillic(String input) {
        return input.chars().mapToObj(c -> Character.UnicodeBlock.of((char) c))
                .anyMatch(ub -> ub.equals(Character.UnicodeBlock.CYRILLIC));
    }
}
