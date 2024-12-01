package org.ulpgc.benchmark_stage3;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.stage3.vectorized.Strassen_Vect_Sparc;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StrassenVectSparcBenchmark {

    @Param({"128", "256", "512", "1024"})
    public int matrixSize;

    private Strassen_Vect_Sparc.SparseMatrix matrixA;
    private Strassen_Vect_Sparc.SparseMatrix matrixB;

    @Setup
    public void setup() {
        matrixA = Strassen_Vect_Sparc.generateRandomSparseMatrix(matrixSize, 0.8);
        matrixB = Strassen_Vect_Sparc.generateRandomSparseMatrix(matrixSize, 0.8);
    }

    @Benchmark
    public void benchmarkStrassenSparse() {
        Strassen_Vect_Sparc.strassen(matrixA, matrixB);
    }
}
