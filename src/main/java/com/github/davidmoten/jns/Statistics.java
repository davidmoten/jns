package com.github.davidmoten.jns;

public class Statistics {
    private double sum = 0;
    private double sumSquares = 0;
    private Double min;
    private Double max;
    private long count = 0;

    void add(double x) {
        sum += x;
        sumSquares += x * x;
        count++;
        if (min == null)
            min = x;
        if (max == null)
            max = x;
        if (x < min)
            min = x;
        if (x > max)
            max = x;
    }

    double sum() {
        return sum;
    }

    double sumSquares() {
        return sumSquares;
    }

    double mean() {
        return sum / count;
    }

    double variance() {
        return sumSquares / count - mean() * mean();
    }

    double std() {
        return Math.sqrt(variance());
    }

    double count() {
        return count;
    }

    double min() {
        return min;
    }

    double max() {
        return max;
    }
}
