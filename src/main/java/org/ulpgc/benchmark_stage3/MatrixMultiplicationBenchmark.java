package org.ulpgc.benchmark_stage3;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.stage3.vectorized.MatrixMultiplication;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MatrixMultiplicationBenchmark {

    @Param({"128", "256", "512", "1024", "2048"})
    public int matrixSize;

    private double[][] matrixA;
    private double[][] matrixB;

    @Setup
    public void setup() {
        matrixA = MatrixMultiplication.generateRandomMatrix(matrixSize);
        matrixB = MatrixMultiplication.generateRandomMatrix(matrixSize);
    }

    @Benchmark
    public void benchmarkMultiply() {
        MatrixMultiplication.multiply(matrixA, matrixB);
    }
}
