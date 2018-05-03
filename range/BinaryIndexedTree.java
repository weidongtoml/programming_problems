import java.util.Arrays;

/**
 * Binary Indexed Tree / Fenwick Tree is a dynamic data structure for
 * answering range minimum query, as such it supports the followig operations:
 * (1) update a value in an array
 * (2) query the sum of a given range in the array
 * each with a run time complexity of O(log(N)) where N is the number of
 * elements in the array.
 *
 * The basic idea is to use the binary representation of integer to partition
 * the different sums, as each integer can be broken down into sums of power of 2,
 * we can calculate sum(Arr_0, ..., Arr_i) easily using these disjoint partions.
 *
 * We maintain an array such that
 *  tree[k] = sum(Arr_(k - p(k) + 1), ..., Arr_k)
 *  where p(k) is the largest power of two that divides k.
 * Note: we are using array that index starting from 1 to make calculation easier.
 * e.g.
 *  tree[1] = sum(Arr_1), p(k) = 2^0 = 1, 1 % 1 = 0
 *  tree[2] = sum(Arr_1, Arr_2), p(k) = 2^1 = 2, 2 % 2 = 0
 *  tree[3] = sum(Arr_3), p(k) = 2^0 = 1, 3 % 1 = 0
 *  tree[4] = sum(Arr_1, ..., Arr_4), p(k) = 2^2 = 4, 4 % 4 = 0
 *  tree[5] = sum(Arr_5), p(k) = 2^0 = 1, 5 % 1 = 0
 *  tree[6] = sum(Arr_5, Arr_6), p(k) = 2^1 = 2, 6 % 2 = 0
 *  tree[7] = sum(Arr_7)
 *  tree[8] = sum(Arr_1, ..., Arr_8), p(k) = 2^3 = 8, 8 % 8 = 0
 *  ...
 *  tree[15] = sum(Arr_15, ..., Arr_16), p(k) = 2^0 = 1, 15 % 1 = 0
 *  tree[16] = sum(Arr_1, ..., Arr_16), p(k) = 2^4 = 16, 16 % 16 = 0
 *
 * Anyway value of sum(Arr_1, ..., Arr_k) can be calculated in O(log(N)) by
 * breaking down the interval into ranges that are stored in the tree.
 * e.g.
 *    sum(Arr_1, ..., Arr_7) = sum(Arr_1, ..., Arr_4) + sum(Arr_5, ..., Arr_6) + sum(Arr_7)
 *      = tree[4] + tree[6] + tree[7]
 * 
 * A sum of range sum(Arr_a, ..., Arr_b) can be calculated using:
 *  sum(Arr_a, ..., Arr_b) = sum(Arr_1, ..., Arr_b) - sum(1, Arr_(a-1))
 *
 * Note: p(k), the largest power of 2 that divides k, is the same as the last non-zero digit
 * of k in binary representation, e.g. 
 *     13 = 8 + 4 + 1 = 1101b, p(13) = 1b = 1
 *     12 = 8 + 4 = 1100b, p(13) = 100b = 4
 *     10 = 8 + 2 = 1010b, p(10) = 10b = 2
 *     8 = 1000b, p(8) = 1000b = 8
 *
 * this could be easily computed using:
 *  p(k) = k & -k.
 *
 * this can be understood as follow:
 *  given a number n, it can be represented as "a1b",
 *  where
 *      "a" are the digits before the last "1"
 *      "b" are the zeros after the last "1"
 *  -n = 2's complement of n + 1 = ~n + 1 = ~(a1b) + 1 = (~a)0(~b) + 1
 *      = (~a)1(b)  // since b is all zeros, ~b is all 1s, 0(~b) + 1 gives 1b
 *    (a)1(b)
 * & (~a)1(b)
 * ---------
 *       1(b)
 *
 * To calculate the sum of a range, we repeatedly add tree[idx] and subtract p(idx) from idx until idx is less than 1.
 * e.g.
 *    Since 13 = 1101b, we have
 *      sum(Arr_1, ..., Arr_13) = tree[1101b] + tree[1100b] + tree[1000b]
 *          = sum(Arr_(13 - 1 + 1), ..., Arr_13) + sum(Arr_(12 - 4 + 1), ..., Arr_12) + sum(Arr_(8 - 8 + 1), ..., Arr_8)
 *          = sum(Arr_13) + Sum(Arr_9,...,Arr_13) + sum(Arr_1, ..., Arr_8)
 * 
 * To update a value in the array, we need to make the increment idx until it is out of range,
 * e.g. 
 *  updatig Arr_5 = Arr_5 + k
 *    we need to
 *                        101b =  5, Arr_5 += k
 *  =>  101b +    1b =>   110b =  6, Arr_6 += k
 *  =>  110b +   10b =>  1000b =  8, Arr_8 += k
 *  => 1000b + 1000b => 10000b = 16, Arr_16 +=k
 *  ...
 *
 * Ref: https://www.topcoder.com/community/data-science/data-science-tutorials/binary-indexed-trees/
 *
 * TODO(weidong): extend this class to allow the underying tree to expand to accomodate more elements.
 * 
 */
public class BinaryIndexedTree {
    final int[] tree;

    /**
     * Constructs a Binary Indexed Tree based on the given array.
     * Runtime complexity: O(N*log(N)), N = array.length.
     * @param array: the integer array to do the range sum query.
     */
    public BinaryIndexedTree(final int[] array) {
        tree = new int[array.length + 1];
        for (int i = 0; i < array.length; i++) {
            int idx = i + 1;
            while (idx < tree.length) {
                tree[idx] += array[i];
                idx += (idx & -idx);
            }
        }
    }

    /**
     * Calculate the sum of integer of the arrange in the range [start, end].
     * Runtime complexity: O(log(N)).
     * @param start: start index
     * @param end: ending index
     * @return sum of array[start...end]
     */
    public int rangeSum(final int start, final int end) {
       return sumFrom0(end) - sumFrom0(start-1); 
    }

    private int sumFrom0(int end) {
        end++;  // we are using array indexed from 1
        int sum = 0;
        while (end > 0) {
            sum += tree[end];
            end -= (end & -end);
        }
        return sum;
    }

    /**
     * Set modify the array of index idx to the value of val.
     * Runtime complexity: O(log(N)).
     * @param idx: index of the array to modify the value
     * @param val: the new value
     */
    public void set(final int idx, final int val) {
        final int diff = val - rangeSum(idx, idx);
        int i = idx + 1;  // we are using array indexed from 1
        while (i < tree.length) {
            tree[i] += diff;
            i += (i & -i);
        }
    }
    
    public static void main(final String[] args) {
        //                    0, 1, 2, 3, 4, 5, 6, 7, 8
        final int[] array = { 1, 3, 4, 8, 6, 1, 4, 2, 3};
        final BinaryIndexedTree indexedTree = new BinaryIndexedTree(array);

        System.out.printf("Testing BinaryIndexedTree against array: %s\n", Arrays.toString(array));
        System.out.printf("rangeSum(0, 0) = %d, expected 1\n", indexedTree.rangeSum(0, 0));
        System.out.printf("rangeSum(3, 3) = %d, expected 8\n", indexedTree.rangeSum(3, 3));
        System.out.printf("rangeSum(8, 8) = %d, expected 3\n", indexedTree.rangeSum(8, 8));
        System.out.printf("rangeSum(4, 4) = %d, expected 6\n", indexedTree.rangeSum(4, 4));
        System.out.printf("rangeSum(0, 8) = %d, expected 32\n", indexedTree.rangeSum(0, 8));
        System.out.printf("rangeSum(2, 5) = %d, expected 19\n", indexedTree.rangeSum(2, 5));

        System.out.println("set array[3] = 10");
        indexedTree.set(3, 10);
        array[3] = 10; // 8 -> 10, +2

        System.out.println("set array[0] = 21");
        indexedTree.set(0, 21);
        array[0] = 21; // 1 -> 21, + 20

        System.out.printf("Re-testing BinaryIndexedTree against array: %s\n", Arrays.toString(array));
        System.out.printf("rangeSum(0, 0) = %d, expected 21\n", indexedTree.rangeSum(0, 0));
        System.out.printf("rangeSum(8, 8) = %d, expected 3\n", indexedTree.rangeSum(8, 8));
        System.out.printf("rangeSum(4, 4) = %d, expected 6\n", indexedTree.rangeSum(4, 4));
        System.out.printf("rangeSum(0, 8) = %d, expected 54\n", indexedTree.rangeSum(0, 8));
        System.out.printf("rangeSum(2, 5) = %d, expected 21\n", indexedTree.rangeSum(2, 5));
    }
}

