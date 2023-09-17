package org.fog.mobilitydata;

import java.util.Random;

public class Kohonen {
    private final int numInputs;
    private final int numOutputs;
    private final double[][] weights;
    private final double[][] inputs;
    private final double learningRate;
    private final double decayRate;
    private final Random rand = new Random();

    public Kohonen(int numInputs, int numOutputs, double learningRate, double decayRate) {
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.weights = new double[numOutputs][numInputs];
        this.inputs = new double[numInputs][];
        this.learningRate = learningRate;
        this.decayRate = decayRate;
        initializeWeights();
    }

    private void initializeWeights() {
        for (int i = 0; i < numOutputs; i++) {
            for (int j = 0; j < numInputs; j++) {
                weights[i][j] = rand.nextDouble();
            }
        }
    }

    public void train(double[][] inputSet, int numIterations) {
        for (int i = 0; i < numIterations; i++) {
            double[] input = inputSet[rand.nextInt(inputSet.length)];
            int winner = findWinner(input);
            adjustWeights(input, winner, i);
        }
    }

    private int findWinner(double[] input) {
        int winner = 0;
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < numOutputs; i++) {
            double distance = distance(weights[i], input);
            if (distance < minDistance) {
                minDistance = distance;
                winner = i;
            }
        }
        return winner;
    }

    private void adjustWeights(double[] input, int winner, int iteration) {
        for (int i = 0; i < numOutputs; i++) {
            double[] weight = weights[i];
            double influence = gaussian(i, winner, iteration);
            for (int j = 0; j < numInputs; j++) {
                weight[j] += influence * learningRate * (input[j] - weight[j]);
            }
        }
    }

    private double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private double gaussian(int i, int j, int t) {
        double sigma = numOutputs / (2 * Math.log(numOutputs));
        double distance = Math.abs(i - j);
        return Math.exp(-distance * distance / (2 * sigma * sigma * decayRate * t));
    }

    public int classify(double[] input) {
        int winner = findWinner(input);
        return winner;
    }
}
