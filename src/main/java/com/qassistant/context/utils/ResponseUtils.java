package com.qassistant.context.utils;

public class ResponseUtils {

    /**
     * Checks if the response is good based on certain criteria.
     * This method evaluates the quality of the response.
     *
     * @param response the response to evaluate
     * @return true if the response is good, false otherwise
     */
    public static boolean isResponseGood(String response) {
        // Example criteria for a good response:
        // - The response is not null or empty.
        // - The response contains certain keywords or phrases.
        // - The response meets a minimum length requirement.

        if (response == null || response.trim().isEmpty()) {
            return false;
        }

        // Additional criteria can be added here.
        // For example, checking if the response contains expected keywords.
        String[] keywords = {"function", "method", "example", "code"};
        for (String keyword : keywords) {
            if (response.contains(keyword)) {
                return true;
            }
        }

        // If none of the keywords are found, the response is not considered good.
        return false;
    }

    /**
     * Calculates the difference between the initial response and the refined response.
     * This can be based on various metrics such as length, content changes, etc.
     *
     * @param initialResponse the initial response
     * @param refinedResponse the refined response
     * @return the calculated difference
     */
    public static int calculateDiff(String initialResponse, String refinedResponse) {
        // Example diff calculation logic:
        // - Calculate the difference in length between the two responses.
        // - Calculate the number of words added or removed.
        // - Calculate the percentage change in content.

        // Simple example: difference in length.
        int lengthDifference = Math.abs(initialResponse.length() - refinedResponse.length());

        // Example: number of words added or removed.
        int wordDifference = Math.abs(countWords(initialResponse) - countWords(refinedResponse));

        // Combine the differences for a final diff value.
        // This can be adjusted based on the importance of each metric.
        return lengthDifference + wordDifference;
    }

    /**
     * Counts the number of words in a given string.
     *
     * @param text the text to count words in
     * @return the number of words
     */
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }

        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}
