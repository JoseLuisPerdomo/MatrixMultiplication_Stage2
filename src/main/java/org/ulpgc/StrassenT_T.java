package org.ulpgc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class StrassenT_T {

    private static final int THRESHOLD = 128;
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    public void multiply(int[][] A, int[][] B) {
        FORK_JOIN_POOL.invoke(new StrassenTask(A, B, true));
    }

    private static class StrassenTask extends RecursiveTask<int[][]> {
        private final int[][] A;
        private final int[][] B;
        private final boolean allowParallel;

        StrassenTask(int[][] A, int[][] B, boolean allowParallel) {
            this.A = A;
            this.B = B;
            this.allowParallel = allowParallel;
        }

        @Override
        protected int[][] compute() {
            int n = A.length;
            if (n <= THRESHOLD) {
                return naiveMultiply(A, B);
            }

            int[][] R = new int[n][n];

            int[][] A11 = new int[n / 2][n / 2];
            int[][] A12 = new int[n / 2][n / 2];
            int[][] A21 = new int[n / 2][n / 2];
            int[][] A22 = new int[n / 2][n / 2];
            int[][] B11 = new int[n / 2][n / 2];
            int[][] B12 = new int[n / 2][n / 2];
            int[][] B21 = new int[n / 2][n / 2];
            int[][] B22 = new int[n / 2][n / 2];

            split(A, A11, 0, 0);
            split(A, A12, 0, n / 2);
            split(A, A21, n / 2, 0);
            split(A, A22, n / 2, n / 2);
            split(B, B11, 0, 0);
            split(B, B12, 0, n / 2);
            split(B, B21, n / 2, 0);
            split(B, B22, n / 2, n / 2);

            StrassenTask taskM1 = new StrassenTask(add(A11, A22), add(B11, B22), allowParallel);
            StrassenTask taskM2 = new StrassenTask(add(A21, A22), B11, allowParallel);
            StrassenTask taskM3 = new StrassenTask(A11, sub(B12, B22), allowParallel);
            StrassenTask taskM4 = new StrassenTask(A22, sub(B21, B11), allowParallel);
            StrassenTask taskM5 = new StrassenTask(add(A11, A12), B22, allowParallel);
            StrassenTask taskM6 = new StrassenTask(sub(A21, A11), add(B11, B12), allowParallel);
            StrassenTask taskM7 = new StrassenTask(sub(A12, A22), add(B21, B22), allowParallel);

            if (allowParallel) {
                invokeAll(taskM1, taskM2, taskM3, taskM4, taskM5, taskM6, taskM7);
            }

            int[][] M1 = taskM1.join();
            int[][] M2 = taskM2.join();
            int[][] M3 = taskM3.join();
            int[][] M4 = taskM4.join();
            int[][] M5 = taskM5.join();
            int[][] M6 = taskM6.join();
            int[][] M7 = taskM7.join();

            int[][] C11 = add(sub(add(M1, M4), M5), M7);
            int[][] C12 = add(M3, M5);
            int[][] C21 = add(M2, M4);
            int[][] C22 = add(sub(add(M1, M3), M2), M6);

            StrassenT_T.join(C11, R, 0, 0);
            StrassenT_T.join(C12, R, 0, n / 2);
            StrassenT_T.join(C21, R, n / 2, 0);
            StrassenT_T.join(C22, R, n / 2, n / 2);

            return R;
        }
    }

    private static int[][] naiveMultiply(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    private static int[][] sub(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];
        return C;
    }

    private static int[][] add(int[][] A, int[][] B) {
        int n = A.length;
        int[][] C = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];
        return C;
    }

    private static void split(int[][] P, int[][] C, int iB, int jB) {
        for (int i1 = 0, i2 = iB; i1 < C.length; i1++, i2++)
            for (int j1 = 0, j2 = jB; j1 < C.length; j1++, j2++)
                C[i1][j1] = P[i2][j2];
    }

    private static void join(int[][] C, int[][] P, int iB, int jB) {
        for (int i1 = 0, i2 = iB; i1 < C.length; i1++, i2++)
            for (int j1 = 0, j2 = jB; j1 < C.length; j1++, j2++)
                P[i2][j2] = C[i1][j1];
    }

    public static double run(int N) {
        StrassenT_T strassen = new StrassenT_T();
        int[][] A = generateRandomMatrix(N);
        int[][] B = generateRandomMatrix(N);

        long startTime = System.nanoTime();
        strassen.multiply(A, B);
        long endTime = System.nanoTime();

        return (endTime - startTime) / 1_000_000_000.0;
    }

    public static int[][] generateRandomMatrix(int n) {
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                matrix[i][j] = (int) (Math.random() * 10);
        return matrix;
    }

    public static void main(String[] args) throws IOException {
        double[] sizes = {8000};
        StringBuilder results = new StringBuilder();
        StrassenT_T s = new StrassenT_T();

        for (double size : sizes) {
            int N = (int) size;
            int[][] A = generateRandomMatrix(N);
            int[][] B = generateRandomMatrix(N);

            long startTime = System.nanoTime();
            s.multiply(A, B);
            long endTime = System.nanoTime();
            double time = (endTime - startTime) / 1_000_000_000.0;

            results.append("StrassenT_T ").append(N).append(" ").append(time);
        }

        System.out.println(results);
    }

    private static void saveResultsToFile(String results) throws IOException {
        try (FileWriter writer = new FileWriter("Results.txt", true)) {
            writer.write(results);
        }
    }
}
