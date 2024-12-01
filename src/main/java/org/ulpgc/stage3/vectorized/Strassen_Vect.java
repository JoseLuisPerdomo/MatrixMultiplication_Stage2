package org.ulpgc.stage3.vectorized;

import java.util.Random;
import java.util.stream.IntStream;

public class Strassen_Vect {

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

    public static double[][] add(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] + B[i][j];
            }
        });
        return result;
    }

    public static double[][] subtract(double[][] A, double[][] B) {
        int n = A.length;
        double[][] result = new double[n][n];
        IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = 0; j < n; j++) {
                result[i][j] = A[i][j] - B[i][j];
            }
        });
        return result;
    }

    public static double[][] multiplyNaive(double[][] A, double[][] B) {
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
        return result;
    }

    public static double[][] strassen(double[][] A, double[][] B) {
        int n = A.length;

        if (n <= 128) {
            return multiplyNaive(A, B);
        }

        int newSize = n / 2;
        double[][] a11 = new double[newSize][newSize];
        double[][] a12 = new double[newSize][newSize];
        double[][] a21 = new double[newSize][newSize];
        double[][] a22 = new double[newSize][newSize];

        double[][] b11 = new double[newSize][newSize];
        double[][] b12 = new double[newSize][newSize];
        double[][] b21 = new double[newSize][newSize];
        double[][] b22 = new double[newSize][newSize];

        IntStream.range(0, newSize).parallel().forEach(i -> {
            for (int j = 0; j < newSize; j++) {
                a11[i][j] = A[i][j];
                a12[i][j] = A[i][j + newSize];
                a21[i][j] = A[i + newSize][j];
                a22[i][j] = A[i + newSize][j + newSize];

                b11[i][j] = B[i][j];
                b12[i][j] = B[i][j + newSize];
                b21[i][j] = B[i + newSize][j];
                b22[i][j] = B[i + newSize][j + newSize];
            }
        });

        double[][] m1 = strassen(add(a11, a22), add(b11, b22));
        double[][] m2 = strassen(add(a21, a22), b11);
        double[][] m3 = strassen(a11, subtract(b12, b22));
        double[][] m4 = strassen(a22, subtract(b21, b11));
        double[][] m5 = strassen(add(a11, a12), b22);
        double[][] m6 = strassen(subtract(a21, a11), add(b11, b12));
        double[][] m7 = strassen(subtract(a12, a22), add(b21, b22));

        double[][] c11 = add(subtract(add(m1, m4), m5), m7);
        double[][] c12 = add(m3, m5);
        double[][] c21 = add(m2, m4);
        double[][] c22 = add(subtract(add(m1, m3), m2), m6);

        double[][] result = new double[n][n];
        IntStream.range(0, newSize).parallel().forEach(i -> {
            for (int j = 0; j < newSize; j++) {
                result[i][j] = c11[i][j];
                result[i][j + newSize] = c12[i][j];
                result[i + newSize][j] = c21[i][j];
                result[i + newSize][j + newSize] = c22[i][j];
            }
        });

        return result;
    }

    public static void main(String[] args) {
        int N = 2000;
        System.out.println("Matrix size: " + N + " x " + N);

        double[][] A = generateRandomMatrix(N);
        double[][] B = generateRandomMatrix(N);

        long startTime = System.nanoTime();
        double[][] result = strassen(A, B);
        long endTime = System.nanoTime();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Time taken for matrix multiplication: " + elapsedTime + " seconds");
    }
}

