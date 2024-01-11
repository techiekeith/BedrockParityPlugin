package me.techiekeith;

import java.util.Arrays;
import java.util.Random;

public class FlowerGenerationSimulator {

    public static void main(String[] args) {
        int sampleSize = 10000;
        for (int yValue = -1; yValue < 1; yValue++) {
            for (int yOffsetChance = 6; yOffsetChance < 11; yOffsetChance++) {
                for (int maxTries = 20; maxTries <= 32; maxTries += 4) {
                    int[] hits = getSampleOfHits(maxTries, sampleSize, yValue, yOffsetChance);
                    printStatistics(maxTries, sampleSize, hits, yValue, yOffsetChance);
                }
            }
        }
    }

    private static void printStatistics(int maxTries, int sampleSize, int[] hits, int yValue, int yOffsetChance) {
        double meanValue = mean(hits);
        double medianValue = median(hits);
        double sdValue = standardDeviation(hits);
        System.out.format("Tries: %d, Samples: %d, Y: %d, Yoff: %d, Mean: %f, Median: %f, SD: %f\n",
                maxTries, sampleSize, yValue, yOffsetChance, meanValue, medianValue, sdValue);
    }

    private static int[] getSampleOfHits(int maxTries, int sampleSize, int yValue, int yOffsetChance) {
        int[] hits = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            hits[i] = getHits(maxTries, yValue, yOffsetChance);
        }
        return hits;
    }

    private static int getHits(int maxTries, int yValue, int yOffsetChance) {
        Random random = new Random();
        boolean[] usableBlocks = new boolean[7*7*5];
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                if (x != 3 || z != 3) {
                    int blockIndex = x + z * 7 + (yValue + 2) * 49;
                    usableBlocks[blockIndex] = true;
                }
            }
        }

        int tries = maxTries;
        int hits = 0;

        while (tries > 0) {
            // Duplicated code from BedrockParityListener
            int nx = 3;
            int ny = 2;
            int nz = 3;
            for (int i = 0; i < 3; i++) {
                nx += random.nextInt(3) - 1;
                nz += random.nextInt(3) - 1;
                if (i > 0) {
                    int moveY = random.nextInt(yOffsetChance);
                    if (moveY < 3) {
                        ny += moveY  - 1;
                    }
                }
            }

            int blockIndex = nx + nz * 7 + ny * 49;
            if (usableBlocks[blockIndex]) {
                usableBlocks[blockIndex] = false;
                hits++;
            }
            tries--;
        }
        return hits;
    }

    private static double mean(int[] values) {
        double sum = Arrays.stream(values).reduce(0, Integer::sum);
        return sum / values.length;
    }

    private static double median(int[] values) {
        Arrays.sort(values);
        int index = values.length / 2;
        if (values.length % 2 == 0) {
            double sum = values[index - 1] + values[index];
            return sum / 2.0d;
        }
        return values[index];
    }

    private static double standardDeviation(int[] values) {
        double meanValue = mean(values);
        return Arrays.stream(values)
                .mapToDouble(value -> Math.pow(Math.abs(meanValue - value), 2))
                .reduce(0, Double::sum) / values.length;
    }
}
