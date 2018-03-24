/**
 * Heap's algorithm for generating all permutations
 * https://en.wikipedia.org/wiki/Heap%27s_algorithm
 */
import java.util.Arrays;
import java.util.Iterator;
import java.lang.System;

public class Permutation {
    /**
     * use recursion to generate all permutation of a given array.
     */
    static void generateAllPermutation(final int[] array) {
        final int size = array.length;
        final boolean[] selected = new boolean[size];
        final int[] indices = new int[size];
        gen(array, size, selected, indices, 0);
    }

    /**
     * Since we need to check if a given item has been selected or not, there's N operation for at each step,
     * this brings the complexity to O(n*n!). 
     */
    private static void gen(final int[] array, final int size, final boolean[] selected, int[] indices, int ind) {
        if (ind == size) {
            // we have finished generating a permutation, let's output it.
            final int[] output = new int[size];
            for (int i = 0; i < size; i++) {
                output[i] = array[indices[i]];
            }
            System.out.println(Arrays.toString(output));
        } else {
            // select iteratively select an index that's not been selected, and move to the next element to be
            // selected for the current permutation
            for (int i = 0; i < size; i++) {
                if (!selected[i]) { // this is really a naive way, another way is to just keep track of the unselected elements
                    selected[i] = true;
                    indices[ind] = i;
                    gen(array, size, selected, indices, ind+1);
                    selected[i] = false;
                }
            }
        }
    }

    static void permutationBySwapping(final int[] array) {
        swapPermutate(array, array.length - 1);
    }

    /**
     * by simply swapping the elements, we do not need to check if an element has been selected,
     * bring the time complexity to be O(2*n!), each step uses 2 swap operations.
     * The initial step, there are n iteration in the for-loop, the second step, there's n-1 iteration, etc.
     * therefore O(2 * n * (n-1) * ... * 1) = O(2*n!).
     */
    private static void swapPermutate(final int[] array, final int n) {
        if (n == 0) {
            System.out.println(Arrays.toString(array));
        } else {
            for (int i = 0; i <= n; i++) { // each element appears once in location n.
                swap(array, i, n);
                swapPermutate(array, n-1);
                swap(array, i, n);
            }
        }
    }

    /**
     * Using Heap's algorithm for generating permutation, we are able to use only 1 swap per permutation,
     * reaching O(n!)
     */
    static void permutationByHeaps(final int[] array) {
        permutateByHeaps(array, array.length - 1);
    }

    private static void permutateByHeaps(final int[] array, final int n) {
        if (n == 0) {
            System.out.println(Arrays.toString(array));
        } else {
            for (int i = 0; i <= n; i++) {
                permutateByHeaps(array, n-1);
                swap(array, (n % 2 == 0) ? i : 0, n);
            }
        }
    }

    static void iterativeHeaps(final int[] array) {
        final int n = array.length;
        final int[] c = new int[n];

        System.out.println(Arrays.toString(array));

        int i = 0;
        while (i < n) {
            if (c[i] < i) {
                swap(array, (n % 2 == 0) ? 0 : c[i], i);
                c[i] += 1;
                i = 0;
                System.out.println(Arrays.toString(array));
            } else {
                c[i] = 0;
                i += 1;
            }
        }
    }

    private static void swap(final int[] array, final int i, final int j) {
        final int t = array[i];
        array[i] = array[j];
        array[j] = t;
    }

    public static void main(final String[] args) {
        int[] case1 = {1, 2, 3, 4};
        generateAllPermutation(case1);
        System.out.println();
        permutationBySwapping(case1);
        System.out.println();
        permutationByHeaps(case1);
        System.out.println();
        iterativeHeaps(case1);
    }
}

