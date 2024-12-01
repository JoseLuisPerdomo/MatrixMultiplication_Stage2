package org.ulpgc.stage2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class StrassenT_T_S {

    private static final int THRESHOLD = 128;
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();

    public void multiply(
            Map<Integer, Map<Integer, Integer>> A,
            Map<Integer, Map<Integer, Integer>> B, int size) {
        FORK_JOIN_POOL.invoke(new StrassenTask(A, B, size, true));
    }

    private static class StrassenTask extends RecursiveTask<Map<Integer, Map<Integer, Integer>>> {
        private final Map<Integer, Map<Integer, Integer>> A;
        private final Map<Integer, Map<Integer, Integer>> B;
        private final int size;
        private final boolean allowParallel;

        StrassenTask(Map<Integer, Map<Integer, Integer>> A, Map<Integer, Map<Integer, Integer>> B, int size, boolean allowParallel) {
            this.A = A;
            this.B = B;
            this.size = size;
            this.allowParallel = allowParallel;
        }

        @Override
        protected Map<Integer, Map<Integer, Integer>> compute() {
            int n = size;
            if (n <= THRESHOLD) {
                return naiveMultiply(A, B);
            }

            Map<Integer, Map<Integer, Integer>> R = new HashMap<>();

            Map<Integer, Map<Integer, Integer>> A11 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> A12 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> A21 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> A22 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> B11 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> B12 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> B21 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> B22 = new HashMap<>();

            split(A, A11, 0, 0);
            split(A, A12, 0, n / 2);
            split(A, A21, n / 2, 0);
            split(A, A22, n / 2, n / 2);
            split(B, B11, 0, 0);
            split(B, B12, 0, n / 2);
            split(B, B21, n / 2, 0);
            split(B, B22, n / 2, n / 2);

            StrassenTask taskM1 = new StrassenTask(add(A11, A22), add(B11, B22), n / 2, allowParallel);
            StrassenTask taskM2 = new StrassenTask(add(A21, A22), B11, n / 2, allowParallel);
            StrassenTask taskM3 = new StrassenTask(A11, sub(B12, B22), n / 2, allowParallel);
            StrassenTask taskM4 = new StrassenTask(A22, sub(B21, B11), n / 2, allowParallel);
            StrassenTask taskM5 = new StrassenTask(add(A11, A12), B22, n / 2, allowParallel);
            StrassenTask taskM6 = new StrassenTask(sub(A21, A11), add(B11, B12), n / 2, allowParallel);
            StrassenTask taskM7 = new StrassenTask(sub(A12, A22), add(B21, B22), n / 2, allowParallel);

            if (allowParallel) {
                invokeAll(taskM1, taskM2, taskM3, taskM4, taskM5, taskM6, taskM7);
            }

            Map<Integer, Map<Integer, Integer>> M1 = taskM1.join();
            Map<Integer, Map<Integer, Integer>> M2 = taskM2.join();
            Map<Integer, Map<Integer, Integer>> M3 = taskM3.join();
            Map<Integer, Map<Integer, Integer>> M4 = taskM4.join();
            Map<Integer, Map<Integer, Integer>> M5 = taskM5.join();
            Map<Integer, Map<Integer, Integer>> M6 = taskM6.join();
            Map<Integer, Map<Integer, Integer>> M7 = taskM7.join();

            Map<Integer, Map<Integer, Integer>> C11 = add(sub(add(M1, M4), M5), M7);
            Map<Integer, Map<Integer, Integer>> C12 = add(M3, M5);
            Map<Integer, Map<Integer, Integer>> C21 = add(M2, M4);
            Map<Integer, Map<Integer, Integer>> C22 = add(sub(add(M1, M3), M2), M6);

            StrassenT_T_S.join(C11, R, 0, 0);
            StrassenT_T_S.join(C12, R, 0, n / 2);
            StrassenT_T_S.join(C21, R, n / 2, 0);
            StrassenT_T_S.join(C22, R, n / 2, n / 2);

            return R;
        }
    }

    private static Map<Integer, Map<Integer, Integer>> naiveMultiply(
            Map<Integer, Map<Integer, Integer>> A, Map<Integer, Map<Integer, Integer>> B) {
        int n = A.size();
        Map<Integer, Map<Integer, Integer>> C = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = 0;
                for (int k = 0; k < n; k++) {
                    value += A.getOrDefault(i, new HashMap<>()).getOrDefault(k, 0) *
                            B.getOrDefault(k, new HashMap<>()).getOrDefault(j, 0);
                }
                if (value != 0) {
                    C.computeIfAbsent(i, x -> new HashMap<>()).put(j, value);
                }
            }
        }
        return C;
    }

    private static Map<Integer, Map<Integer, Integer>> sub(
            Map<Integer, Map<Integer, Integer>> A, Map<Integer, Map<Integer, Integer>> B) {
        int n = A.size();
        Map<Integer, Map<Integer, Integer>> C = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = A.getOrDefault(i, new HashMap<>()).getOrDefault(j, 0) -
                        B.getOrDefault(i, new HashMap<>()).getOrDefault(j, 0);
                if (value != 0) {
                    C.computeIfAbsent(i, x -> new HashMap<>()).put(j, value);
                }
            }
        }
        return C;
    }

    private static Map<Integer, Map<Integer, Integer>> add(
            Map<Integer, Map<Integer, Integer>> A, Map<Integer, Map<Integer, Integer>> B) {
        int n = A.size();
        Map<Integer, Map<Integer, Integer>> C = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = A.getOrDefault(i, new HashMap<>()).getOrDefault(j, 0) +
                        B.getOrDefault(i, new HashMap<>()).getOrDefault(j, 0);
                if (value != 0) {
                    C.computeIfAbsent(i, x -> new HashMap<>()).put(j, value);
                }
            }
        }
        return C;
    }

    private static void split(Map<Integer, Map<Integer, Integer>> P, Map<Integer, Map<Integer, Integer>> C, int iB, int jB) {
        for (int i1 = 0, i2 = iB; i1 < C.size(); i1++, i2++) {
            for (int j1 = 0, j2 = jB; j1 < C.size(); j1++, j2++) {
                C.computeIfAbsent(i1, x -> new HashMap<>()).put(j1, P.getOrDefault(i2, new HashMap<>()).getOrDefault(j2, 0));
            }
        }
    }

    private static void join(Map<Integer, Map<Integer, Integer>> C, Map<Integer, Map<Integer, Integer>> P, int iB, int jB) {
        for (int i1 = 0, i2 = iB; i1 < C.size(); i1++, i2++) {
            for (int j1 = 0, j2 = jB; j1 < C.size(); j1++, j2++) {
                P.computeIfAbsent(i2, x -> new HashMap<>()).put(j2, C.getOrDefault(i1, new HashMap<>()).getOrDefault(j1, 0));
            }
        }
    }

    public static double run(int N, double sparcity) {

        Map<Integer, Map<Integer, Integer>> A = generateSparseMatrix(N, sparcity);
        Map<Integer, Map<Integer, Integer>> B = generateSparseMatrix(N, sparcity);

        StrassenT_T_S strassen = new StrassenT_T_S();

        long startTime = System.nanoTime();
        strassen.multiply(A, B, N);
        long endTime = System.nanoTime();

        return (endTime - startTime) / 1_000_000_000.0;
    }

    public static void main(String[] args) throws IOException {
        double[] sizes = {2000};
        double[] sparcity_values = {0.7, 0.8, 0.9};
        StringBuilder results = new StringBuilder();
        StrassenT_T_S s = new StrassenT_T_S();

        for (double size : sizes) {
            int N = (int) size;

            for (double sparcity : sparcity_values) {
                Map<Integer, Map<Integer, Integer>> A = generateSparseMatrix(N, sparcity);
                Map<Integer, Map<Integer, Integer>> B = generateSparseMatrix(N, sparcity);

                long startTime = System.nanoTime();
                s.multiply(A, B, N);
                long endTime = System.nanoTime();
                double time = (endTime - startTime) / 1_000_000_000.0;

                results.append("StrassenT_T_S ").append(N).append(" ").append(time);
            }
        }

        System.out.println(results);
    }

    private static void saveResultsToFile(String results) throws IOException {
        try (FileWriter writer = new FileWriter("Results.txt", true)) {
            writer.write(results);
        }
    }

    public static Map<Integer, Map<Integer, Integer>> generateSparseMatrix(int n, double sparsity) {
        Map<Integer, Map<Integer, Integer>> matrix = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.random() > sparsity) {
                    matrix.computeIfAbsent(i, x -> new HashMap<>()).put(j, (int) (Math.random() * 10));
                }
            }
        }
        return matrix;
    }
}
