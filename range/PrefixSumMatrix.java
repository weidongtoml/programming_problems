import java.util.Arrays;

/**
 * PrefixSumMatrix uses the same idea as in PrefixSumArray by precomputing the
 * sums of range (0,0) - (i, j), and then answer the sum query of (r0, c0) - (r1, c1)
 * using the relation:
 *  sum(r0, c0, r1, c1) = sum(0, 0, r1, c1) - sum(0, 0, r0, c1) - sum (0, 0, r1, c0) + sum(0, 0, r0, c0)
 * 
 * This requires preprocessing time of O(n*m), and query time of O(1).
 */
public class PrefixSumMatrix {
    private final int n;
    private final int m;
    private final int[][] sumMatrix;

    public PrefixSumMatrix(final int[][] matrix) {
        if (matrix == null || matrix.length == 0 || matrix[0].length == 0) {
            throw new IllegalArgumentException("Null or empty matrix");
        }
        n = matrix.length;
        m = matrix[0].length;
        sumMatrix = new int[n][m];
        for (int i = 0; i < n; i++) {
            sumMatrix[i] = Arrays.copyOf(matrix[i], m);
        }
        for (int i = 1; i < n; i++) {
            sumMatrix[i][0] += sumMatrix[i-1][0];
        }
        for (int j = 1; j < m; j++) {
            sumMatrix[0][j] += sumMatrix[0][j-1];
        }
        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                sumMatrix[i][j] += sumMatrix[i-1][j] + sumMatrix[i][j-1] - sumMatrix[i-1][j-1];
            }
        }
    }

    /**
     * (r0, c0) .... (r0, c1)
     * .             .
     * .             .
     * .             .
     * (r1, c0) .... (r1, c1)
     */
    public int getSum(final int r0, final int c0, final int r1, final int c1) {
        int v = sumMatrix[r1][c1];
        if (r0 > 0) {
            v -= sumMatrix[r0-1][c1];
        }
        if (c0 > 0) {
            v -= sumMatrix[r1][c0-1];
        }
        if (r0 > 0 && c0 > 0) {
            v += sumMatrix[r0-1][c0-1];
        }
        return v;
    }

    public static void main(final String[] args) {
        final int[][] matrix = {
            {
                1,  2,  3,  4,   5,  6
            }, {
                7,  8,  9,  10, 11, 12
            }, {
               13, 14, 15,  16, 17, 18
            }
        };

        final PrefixSumMatrix sumMatrix = new PrefixSumMatrix(matrix);
        System.out.printf("matrix(0, 0, 0, 0) = %d, expected 1.\n", sumMatrix.getSum(0, 0, 0, 0));
        System.out.printf("matrix(0, 0, 0, 5) = %d, expected 21.\n", sumMatrix.getSum(0, 0, 0, 5));
        System.out.printf("matrix(0, 0, 2, 0) = %d, expected 21.\n", sumMatrix.getSum(0, 0, 2, 0));
        System.out.printf("matrix(0, 0, 2, 5) = %d, expected 171.\n", sumMatrix.getSum(0, 0, 2, 5));
        System.out.printf("matrix(1, 3, 2, 5) = %d, expected 84.\n", sumMatrix.getSum(1, 3, 2, 5));
    }
}

