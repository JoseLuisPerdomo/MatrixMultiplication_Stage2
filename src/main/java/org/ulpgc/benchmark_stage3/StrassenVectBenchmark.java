package org.ulpgc.benchmark_stage3;

import org.ulpgc.stage3.vectorized.Strassen_Vect;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StrassenVectBenchmark {

    @Param({"128", "256", "512", "1024"})
    public int matrixSize;

    private double[][] matrixA;
    private double[][] matrixB;

    @Setup
    public void setup() {
        matrixA = Strassen_Vect.generateRandomMatrix(matrixSize);
        matrixB = Strassen_Vect.generateRandomMatrix(matrixSize);
    }

    @Benchmark
    public void benchmarkStrassen() {
        Strassen_Vect.strassen(matrixA, matrixB);
    }
}
