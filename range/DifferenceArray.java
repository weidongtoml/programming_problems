import java.util.Arrays;

/**
 * Difference array is a data structure that allow range update to be done in O(1),
 * at the expense of querying a value in O(n).
 *
 * The idea is to store the difference between consecutive elements instead:
 * table[0] = array[0]
 * table[1] = array[1] - array[2]
 * table[2] = array[2] - array[1] 
 * ...
 * table[i] = array[i] - array[i-1] 
 *
 * To increase a given range [a, b] by c, we only need to do
 * table[a] += c
 * table[b+1] -= c
 */
public class DifferenceArray {
    private final int[] table;

    public DifferenceArray(final int[] array) {
        table = new int[array.length];
        table[0] = array[0];
        for (int i = 1; i < array.length; i++) {
            table[i] = array[i] - array[i-1];
        }
        System.out.println(Arrays.toString(table));
    }

    public void inc(final int start, final int end, final int val) {
       table[start] += val;
       if (end < table.length-1) {
           table[end+1] -= val;
       }
    } 

    public int[] toArray() {
        final int[] array = new int[table.length];
        int s = 0;
        for (int i = 0; i < table.length; i++) {
            s += table[i];
            array[i] = s;
        }
        return array;
    }

    public static void main(final String[] args) {
        //                    0  1  2  3  4  5  6  7
        final int[] array = { 3, 3, 1, 1, 1, 5, 2, 2 };
        final DifferenceArray diffArray = new DifferenceArray(array);
        System.out.printf("Original array:\n%s\n", Arrays.toString(array));
        System.out.println("Array reproduced (expected to be the same as above):");
        System.out.println(Arrays.toString(diffArray.toArray()));

        System.out.println("\nIncrease range [1, 5] by 10");
        diffArray.inc(1, 5, 10);
        System.out.println("Expected:\n[3, 13, 11, 11, 11, 15, 2, 2]\nand got:");
        System.out.println(Arrays.toString(diffArray.toArray()));

        System.out.println("\nDecrease range [6, 7] by 2");
        diffArray.inc(6, 7, -2);
        System.out.println("Expected:\n[3, 13, 11, 11, 11, 15, 0, 0]\nand got:");
        System.out.println(Arrays.toString(diffArray.toArray()));
    }
}

