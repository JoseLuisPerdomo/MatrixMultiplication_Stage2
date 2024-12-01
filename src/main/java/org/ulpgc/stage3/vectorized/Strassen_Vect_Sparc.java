package org.ulpgc.stage3.vectorized;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Strassen_Vect_Sparc {

    public static class SparseMatrix {
        int size; // Tamaño de la matriz (NxN)
        Map<Integer, Double> values; // Mapa para almacenar valores no nulos (clave: índice lineal)

        public SparseMatrix(int size) {
            this.size = size;
            this.values = new HashMap<>();
        }

        public double get(int row, int col) {
            return values.getOrDefault(row * size + col, 0.0);
        }

        public void set(int row, int col, double value) {
            if (value != 0.0) {
                values.put(row * size + col, value);
            } else {
                values.remove(row * size + col);
            }
        }

        public boolean isZero() {
            return values.isEmpty();
        }

        public int getSize() {
            return size;
        }
    }

    public static SparseMatrix generateRandomSparseMatrix(int N, double sparsity) {
        Random random = new Random();
        SparseMatrix matrix = new SparseMatrix(N);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (random.nextDouble() > sparsity) {
                    matrix.set(i, j, random.nextDouble() * 10);
                }
            }
        }
        return matrix;
    }

    public static SparseMatrix add(SparseMatrix A, SparseMatrix B) {
        int n = A.getSize();
        SparseMatrix result = new SparseMatrix(n);

        // Combinar las dos matrices
        A.values.forEach((key, value) -> result.set(key / n, key % n, value + B.get(key / n, key % n)));
        B.values.forEach((key, value) -> {
            if (!A.values.containsKey(key)) {
                result.set(key / n, key % n, value);
            }
        });

        return result;
    }

    public static SparseMatrix subtract(SparseMatrix A, SparseMatrix B) {
        int n = A.getSize();
        SparseMatrix result = new SparseMatrix(n);

        // Resta entre las dos matrices
        A.values.forEach((key, value) -> result.set(key / n, key % n, value - B.get(key / n, key % n)));
        B.values.forEach((key, value) -> {
            if (!A.values.containsKey(key)) {
                result.set(key / n, key % n, -value);
            }
        });

        return result;
    }

    public static SparseMatrix multiplyNaive(SparseMatrix A, SparseMatrix B) {
        int n = A.getSize();
        SparseMatrix result = new SparseMatrix(n);

        // Multiplicación naive para matrices dispersas
        A.values.forEach((keyA, valueA) -> {
            int row = keyA / n;
            int colA = keyA % n;
            for (int colB = 0; colB < n; colB++) {
                double valueB = B.get(colA, colB);
                if (valueB != 0.0) {
                    double currentValue = result.get(row, colB);
                    result.set(row, colB, currentValue + valueA * valueB);
                }
            }
        });

        return result;
    }

    public static SparseMatrix strassen(SparseMatrix A, SparseMatrix B) {
        int n = A.getSize();

        // Caso base: usar algoritmo naive si es suficientemente pequeño o si una matriz es cero
        if (n <= 128 || A.isZero() || B.isZero()) {
            return multiplyNaive(A, B);
        }

        int newSize = n / 2;

        // Submatrices representadas por índices, no nuevas matrices
        SparseMatrix a11 = extractSubMatrix(A, 0, 0, newSize);
        SparseMatrix a12 = extractSubMatrix(A, 0, newSize, newSize);
        SparseMatrix a21 = extractSubMatrix(A, newSize, 0, newSize);
        SparseMatrix a22 = extractSubMatrix(A, newSize, newSize, newSize);

        SparseMatrix b11 = extractSubMatrix(B, 0, 0, newSize);
        SparseMatrix b12 = extractSubMatrix(B, 0, newSize, newSize);
        SparseMatrix b21 = extractSubMatrix(B, newSize, 0, newSize);
        SparseMatrix b22 = extractSubMatrix(B, newSize, newSize, newSize);

        // Aplicar Strassen
        SparseMatrix m1 = strassen(add(a11, a22), add(b11, b22));
        SparseMatrix m2 = strassen(add(a21, a22), b11);
        SparseMatrix m3 = strassen(a11, subtract(b12, b22));
        SparseMatrix m4 = strassen(a22, subtract(b21, b11));
        SparseMatrix m5 = strassen(add(a11, a12), b22);
        SparseMatrix m6 = strassen(subtract(a21, a11), add(b11, b12));
        SparseMatrix m7 = strassen(subtract(a12, a22), add(b21, b22));

        SparseMatrix c11 = add(subtract(add(m1, m4), m5), m7);
        SparseMatrix c12 = add(m3, m5);
        SparseMatrix c21 = add(m2, m4);
        SparseMatrix c22 = add(subtract(add(m1, m3), m2), m6);

        return combineSubMatrices(c11, c12, c21, c22, n);
    }

    public static SparseMatrix extractSubMatrix(SparseMatrix matrix, int rowOffset, int colOffset, int newSize) {
        SparseMatrix subMatrix = new SparseMatrix(newSize);

        matrix.values.forEach((key, value) -> {
            int row = key / matrix.getSize();
            int col = key % matrix.getSize();
            if (row >= rowOffset && row < rowOffset + newSize && col >= colOffset && col < colOffset + newSize) {
                subMatrix.set(row - rowOffset, col - colOffset, value);
            }
        });

        return subMatrix;
    }

    public static SparseMatrix combineSubMatrices(SparseMatrix c11, SparseMatrix c12, SparseMatrix c21, SparseMatrix c22, int size) {
        SparseMatrix result = new SparseMatrix(size);

        c11.values.forEach((key, value) -> result.set(key / c11.getSize(), key % c11.getSize(), value));
        c12.values.forEach((key, value) -> result.set(key / c12.getSize(), key % c12.getSize() + c11.getSize(), value));
        c21.values.forEach((key, value) -> result.set(key / c21.getSize() + c11.getSize(), key % c21.getSize(), value));
        c22.values.forEach((key, value) -> result.set(key / c22.getSize() + c11.getSize(), key % c22.getSize() + c11.getSize(), value));

        return result;
    }

    public static void main(String[] args) {
        int N = 1024; // Tamaño de la matriz
        double sparsity = 0.8; // Fracción de ceros en la matriz

        System.out.println("Matrix size: " + N + " x " + N);

        SparseMatrix A = generateRandomSparseMatrix(N, sparsity);
        SparseMatrix B = generateRandomSparseMatrix(N, sparsity);

        long startTime = System.nanoTime();
        SparseMatrix result = strassen(A, B);
        long endTime = System.nanoTime();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Time taken for sparse matrix multiplication: " + elapsedTime + " seconds");
    }
}

