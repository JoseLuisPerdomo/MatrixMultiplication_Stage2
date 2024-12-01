package org.ulpgc.stage3.vectorized;
import java.util.Random;
import java.util.stream.IntStream;

public class MatrixMultiplication {

    public static double[][] generateRandomMatrix(int N) {
        Random random = new Random();
        double[][] matrix = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                matrix[i][j] = random.nextDouble() * 10;
            }
        }
        return matrix;
    }

    public static void multiply(double[][] A, double[][] B) {
        int n = A.length;
        int m = B[0].length;
        int p = B.length;

        if (A[0].length != p) {
            throw new IllegalArgumentException("Incompatible matrix dimensions");
        }

        double[][] result = new double[n][m];

        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = 0; j < m; j++) {
                int finalJ = j;
                result[i][j] = IntStream.range(0, p)
                        .mapToDouble(k -> A[i][k] * B[k][finalJ])
                        .sum();
            }
        });

    }

    public static void main(String[] args) {
        int N = 2000;
        System.out.println("Matrix size: " + N + " x " + N);

        double[][] A = generateRandomMatrix(N);
        double[][] B = generateRandomMatrix(N);

        long startTime = System.nanoTime();
        multiply(A, B);
        long endTime = System.nanoTime();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Time taken for matrix multiplication: " + elapsedTime + " seconds");
    }
}
