package com.qassistant.context.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static List<String> splitTextWithMarkdown(String text, int size) {
        if (text.length() <= size) return List.of(text);
        List<String> parts = new ArrayList<>();
        String codeBlockPattern = "(```\\w+)|(```)";
        Pattern pattern = Pattern.compile(codeBlockPattern);
        Matcher matcher = pattern.matcher(text);

        StringBuilder currentPart = new StringBuilder();
        boolean isInCodeBlock = false;
        String currentCodeTag = "";

        for (int i = 0, len = text.length(); i < len; i++) {
            if (matcher.find(i) && matcher.start() == i) {
                if (!isInCodeBlock) {
                    currentCodeTag = matcher.group();
                    isInCodeBlock = true;
                    currentPart.append(currentCodeTag).append(" ");
                    i += currentCodeTag.length() - 1;
                } else {
                    isInCodeBlock = false;
                    currentPart.append(text.charAt(i));
                }
                continue;
            }
            if (currentPart.length() < size || isInCodeBlock) {
                currentPart.append(text.charAt(i));
            }
            if (currentPart.length() == size || (i + 1 == len && !currentPart.isEmpty())) {
                if (isInCodeBlock) {
                    currentPart.append("```");
                }
                parts.add(currentPart.toString());
                currentPart.setLength(0);
                if (isInCodeBlock && i + 1 < len) {
                    currentPart.append(currentCodeTag).append(" ");
                }
            }
        }
        if (!currentPart.isEmpty()) {
            parts.add(currentPart.toString());
        }
        return parts;
    }
}
