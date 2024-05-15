package com.qassistant.context.utils;

import java.util.List;

public class MathUtils {

    /**
     * Calculates the cosine similarity between two lists of doubles.
     * @param values1 First list of doubles
     * @param values2 Second list of doubles
     * @return the cosine similarity between the two lists
     */
    public static double cosineSimilarity(List<Double> values1, List<Double> values2) {
        double[] array1 = values1.stream().mapToDouble(Double::doubleValue).toArray();
        double[] array2 = values2.stream().mapToDouble(Double::doubleValue).toArray();
        return cosineSimilarity(array1, array2);
    }

    /**
     * Helper method to calculate cosine similarity between two double arrays.
     * @param array1 First array of doubles
     * @param array2 Second array of doubles
     * @return the cosine similarity between the two arrays
     */
    private static double cosineSimilarity(double[] array1, double[] array2) {
        double dotProduct = 0.0;
        double normArray1 = 0.0;
        double normArray2 = 0.0;
        for (int i = 0; i < array1.length; i++) {
            dotProduct += array1[i] * array2[i];
            normArray1 += array1[i] * array1[i];
            normArray2 += array2[i] * array2[i];
        }
        normArray1 = Math.sqrt(normArray1);
        normArray2 = Math.sqrt(normArray2);
        return dotProduct / (normArray1 * normArray2);
    }
}
