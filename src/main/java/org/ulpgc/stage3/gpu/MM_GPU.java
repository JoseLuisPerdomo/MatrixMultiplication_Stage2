package org.ulpgc.stage3.gpu;

import org.tensorflow.EagerSession;
import org.tensorflow.Operand;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.types.TFloat32;

public class MM_GPU {

    public static float[][] multiplyWithTensorFlow(float[][] A, float[][] B) {
        try (EagerSession session = EagerSession.create()) {
            Ops tf = Ops.create(session);

            Operand<TFloat32> tensorA = tf.constant(
                    TFloat32.tensorOf(Shape.of(A.length, A[0].length), data -> {
                        for (int i = 0; i < A.length; i++) {
                            for (int j = 0; j < A[0].length; j++) {
                                data.setFloat(A[i][j], i, j);
                            }
                        }
                    })
            );

            Operand<TFloat32> tensorB = tf.constant(
                    TFloat32.tensorOf(Shape.of(B.length, B[0].length), data -> {
                        for (int i = 0; i < B.length; i++) {
                            for (int j = 0; j < B[0].length; j++) {
                                data.setFloat(B[i][j], i, j);
                            }
                        }
                    })
            );

            Operand<TFloat32> result = tf.linalg.matMul(tensorA, tensorB);

            try (TFloat32 resultTensor = result.asTensor()) {
                return convertTo2DArray(resultTensor);
            }
        }
    }

    private static float[][] convertTo2DArray(TFloat32 tensor) {
        int rows = (int) tensor.shape().size(0);
        int cols = (int) tensor.shape().size(1);
        float[][] result = new float[rows][cols];
        tensor.scalars().forEachIndexed((idx, scalar) -> {
            int row = (int) idx[0];
            int col = (int) idx[1];
            result[row][col] = scalar.getFloat();
        });
        return result;
    }

    public static void main(String[] args) {
        float[][] A = {{1f, 2f}, {3f, 4f}};
        float[][] B = {{5f, 6f}, {7f, 8f}};

        long startTime = System.nanoTime();
        float[][] result = multiplyWithTensorFlow(A, B);
        long endTime = System.nanoTime();

        System.out.println("Result:");
        for (float[] row : result) {
            for (float val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }

        double elapsedTimeInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Execution time: " + elapsedTimeInSeconds + " seconds");
    }
}
