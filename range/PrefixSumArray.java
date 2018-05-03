/**
 * A prefix sum array is a data structure for efficient calculation of range sum
 * given a fix array of integers.
 *
 * It requires O(n) to build an array, and O(1) for answerign the range sum query.
 */
public class PrefixSumArray {
    final int[] prefixSum;

    public PrefixSumArray(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Argument cannot be empty array.");
        }

        prefixSum = new int[array.length];
        prefixSum[0] = array[0];
        for (int i = 1; i < array.length; i++) {
            prefixSum[i] = prefixSum[i-1] + array[i];
        }
    }

    public int rangeSum(int start, int end) {
        if (start < 0 || end >= prefixSum.length || start > end) {
            throw new IllegalArgumentException(String.format("Given range (%d, %d] is invalid, should be within (0, %d]", 
                        start, end, prefixSum.length-1));
        }
        return prefixSum[end] - (start > 0 ? prefixSum[start-1] : 0);
    }

    public static void main(final String[] args) {
        //             0  1  2  3  4  5  6  7
        int[] array = {1, 3, 8, 4, 6, 1, 3, 4};
        final PrefixSumArray prefixSum = new PrefixSumArray(array);
        System.out.println(prefixSum.rangeSum(0, 0));  // -> 1
        System.out.println(prefixSum.rangeSum(0, 7));  // -> 30
        System.out.println(prefixSum.rangeSum(7, 7));  // -> 4
        System.out.println(prefixSum.rangeSum(1, 3)); // ->15
    }
}

