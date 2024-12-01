package org.ulpgc.benchmark_stage3;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.stage3.vectorized.Strassen_Vect_Thread;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StrassenVectThreadBenchmark {

    @Param({"128", "256", "512", "1024"})
    public int matrixSize;

    private double[][] matrixA;
    private double[][] matrixB;

    @Setup
    public void setup() {
        matrixA = Strassen_Vect_Thread.generateRandomMatrix(matrixSize);
        matrixB = Strassen_Vect_Thread.generateRandomMatrix(matrixSize);
    }

    @Benchmark
    public void benchmarkStrassenThread() {
        Strassen_Vect_Thread.strassen(matrixA, matrixB);
    }
}
