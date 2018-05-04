import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Segment tree, similar to sparse table, is a dynamic data structure for
 * answering range queries, but unlike sparse table, segment tree allows
 * modification of existing extries.
 * Segment tree allows
 * (1) query of a given range to be done in O(log(N)) runtime
 * (2) update existing entry in O(log(N)) runtime
 *
 * It does so by organizing the intermidiate operation result as inner nodes
 * in a binary tree so that to answer a given range query, it would break it
 * down to existing sub-ranges and retrieve the result instead of iterating
 * through each element.
 *
 * e.g. for the given array for finding range sum:
 * idx: 0 1 2 3 4 5 6 7
 * val: 5 8 6 3 2 7 2 6
 *
 * its segment tree would be
 *
 *                   39 
 *            22           17 
 *        13      9      9     8 
 * val: 5    8  6   3  2   7 2   6
 * idx: 0    1  2   3  4   5 6   7 
 *              ^                ^
 * to calculate the sum of range: [2, 7]
 * we use the inner nodes that are located as high as possible in tree,
 * in this case, they are 9 + 17 = 26. At most two nodes of each level of
 * the tree are needed, hence the total number of nodes is O(log(N)).
 *
 * Since a segment tree is a full binary tree, we can store it in an array
 * such that:
 * for any given node i, we have
 * (1) parent located at (i-1) / 2
 * (2) left child: 2*i
 * (3) right child: 2*i+1.
 * (4) root is located at tree[0]
 * (5) values of tree[n] to tree[2*n - 1] (leaf nodes) are the original values.
 * (6) tree[i] = F(tree[2*i], tree[2*i+1])
 *  a parent's value is the function application of its children.
 *
 * So the above example we have:
 * idx:   0   1   2   3   4   5   6   7   8   9   10   11   12   13   14   15
 * val:   x  39  22  17  13   9   9   8   5   8   6    3    2    7    2    6
 *
 * tree[i] represents the range 
 * tree[1] = A[0:7]
 * tree[2] = A[0:3]
 * tree[3] = A[4:7]
 * tree[4] = A[0:1]
 * tree[5] = A[2:3]
 * tree[6] = A[4:5]
 * tree[7] = A[6:7]
 * tree[8] = A[0:0]
 * tree[9] = A[1:1]
 * ...
 * tree[15]= A[7:7]
 *
 * To construct a segment tree, we first copy the array to positions tree[n],...,tree[2*n-1].
 * Then looping back from n-1 to 1, use the relation tree[i] = F(tree[2*i], tree[2*i+1]) to
 * update the inner nodes.
 *
 * To modify an existing index, update the value at location idx+n, then reuse the same
 * relation tree[i] = F(tree[2*i], tree[2*i+1]) to update all ancestors of idx+n.
 * Note that node idx's parent is given by idx / 2.
 *
 * To query a given range [a, b], we start at the leaf nodes, i.e. [l = a+n, r = b+n].
 *   we take l into account if l is a right child with result = F(result, tree[l]), 
 *      then increase l by 1 to exclude l in future search
 *      (if we do not do this, then l's parent would include l's sibling which is outside the range)
 *   we take r into account if r is a left child with result = F(result, tree[r]),
 *      then decrease r by 1 to exclude r in future search
 *      (if we do not do this, then r's parent would include r's sibling which is outside the range)
 *  we then move up to the parents usnig l = l / 2, r = r / 2. 
 *
 *  Note that the query function must be associative, i.e. F(x, F(y, z)) = F(F(x, y), z)
 *    because when calculating the value, do not guarentee only use one order of application.
 */
public class SegmentTree {
    private final int sz;
    private final int n;
    private final int[] tree;
    private final int zero;
    private final BiFunction<Integer, Integer, Integer> func;

    /**
     * Create a segment tree for the given array.
     * @param array: an array for doing the range query
     * @param zero: the zero value that satisfies func(zero, x) = func(x, zero) = x
     * @param func: the query function, must satisfies associative rule: func(x, func(y, z)) = func(func(x, y), z).
     */
    public SegmentTree(
            final int[] array,
            final int zero,
            final BiFunction<Integer, Integer, Integer> func
    ) {
        this.sz = array.length;
        this.zero = zero;
        this.func = func;
        n = getNextPowerOf2(array.length);
        tree = new int[2*n];
        System.arraycopy(array, 0, tree, n, array.length);
        // add padding to ensure that we have power of 2 size.
        Arrays.fill(tree, n + array.length, 2*n, zero);
        // construct the segment tree using the relation
        // tree[parent] = F(tree[left_child], tree[right_child])
        for (int i = n-1; i > 0; i--) {
            tree[i] = func.apply(tree[2*i], tree[2*i+1]); 
        }
        System.out.println(Arrays.toString(tree));
    }

    /**
     * Query the given range [left, right] and return the result.
     * @param left: starting range (inclusive)
     * @param right: ending range (inclusive)
     * @return query result of applying func over the given range.
     */
    public int query(final int left, final int right) {
        if (left < 0 || right >= sz || left > right) {
            throw new IllegalArgumentException(
                    String.format("Invalid range, expected [0, %d) but got [%d, %d]", sz, left, right));
        }
        int a = left + n;
        int b = right + n;
        int s = zero;
        while (a <= b) {
            // since a and b are always at the same level, as long as we have a < b
            // we are sure that there's no overlap between the range they represent.
            // And for the case of a = b, one and only one of the following function
            // application will be invoked.
            if (a % 2 == 1) {
                // a is a right child, a's parent would include it's left sibling
                // since a is already the left most range under consideration,
                // its left sibling is outside the range, so we have to handle a now, 
                // then exclude it from future consideration using a++
                s = func.apply(s, tree[a]);
                a++;
            }
            if (b % 2 == 0) { 
                // b is a left child, b's parent would include it's right sibling
                // since b is already the right most range under consideration,
                // its right sibling is outside the range, so we have to handle b now,
                // then exlude it from future consideration using b--
                s = func.apply(s, tree[b]);
                b--;
            }
            a /= 2;
            b /= 2;
        }
        return s;
    }

    /**
     * Update the value at the given index.
     * @param idx: index of the original array.
     * @param val: new value.
     */
    public void set(final int idx, final int val) {
        if (idx < 0 || idx >= sz) {
            throw new IndexOutOfBoundsException(
                    String.format("Expected index to be within range [0, %d), but got %d.", sz, idx));
        }
        int k = idx + n;
        tree[k] = val;
        for (int p = k / 2; p > 0; p /= 2) {
            tree[p] = func.apply(tree[2*p], tree[2*p+1]);
        }
    }

    private static int getNextPowerOf2(int n) {
        if (n < 1) {
            throw new IllegalArgumentException(
                    String.format("Expected argument to be greater than 0 but got: %d", n));
        }
        int p = 1;
        while (p < n) {
            p *= 2;
        }
        return p;
    }

    public static void main(final String[] args) {
        System.out.println("Testing getNextPowerOf2");
        System.out.printf("getNextPowerOf2(1) = %d, expected 1\n", getNextPowerOf2(1));
        System.out.printf("getNextPowerOf2(2) = %d, expected 2\n", getNextPowerOf2(2));
        System.out.printf("getNextPowerOf2(4) = %d, expected 4\n", getNextPowerOf2(4));
        System.out.printf("getNextPowerOf2(6) = %d, expected 8\n", getNextPowerOf2(6));
        System.out.printf("getNextPowerOf2(9) = %d, expected 16\n", getNextPowerOf2(9));

        //                    0  1  2  3  4  5  6  7  8
        final int[] array = { 5, 8, 6, 3, 2, 7, 2, 6, 10};
        final SegmentTree sumTree = new SegmentTree(array, 0, (x, y) -> x + y);
        System.out.printf("Testing SegmentTree against array: %s\n", Arrays.toString(array));
        System.out.printf("rangeSum(0, 0) = %d, expected 5\n", sumTree.query(0, 0));
        System.out.printf("rangeSum(3, 3) = %d, expected 3\n", sumTree.query(3, 3));
        System.out.printf("rangeSum(8, 8) = %d, expected 10\n", sumTree.query(8, 8));
        System.out.printf("rangeSum(0, 8) = %d, expected 49\n", sumTree.query(0, 8));
        System.out.printf("rangeSum(1, 5) = %d, expected 26\n", sumTree.query(1, 5));

        System.out.println("Set array[0] = 10");
        sumTree.set(0, 10);
        System.out.println("Set array[6] = 1");
        sumTree.set(6, 1);
        // idx:  0  1  2  3  4  5  6  7  8
        // val: 10, 8, 6, 3, 2, 7, 1, 6, 10
        System.out.printf("rangeSum(0, 0) = %d, expected 10\n", sumTree.query(0, 0));
        System.out.printf("rangeSum(6, 6) = %d, expected 1\n", sumTree.query(6, 6));
        System.out.printf("rangeSum(0, 8) = %d, expected 53\n", sumTree.query(0, 8));
        System.out.printf("rangeSum(1, 5) = %d, expected 26\n", sumTree.query(1, 5));

        final SegmentTree minTree = new SegmentTree(array, Integer.MAX_VALUE, (x, y) -> Math.min(x, y));
        System.out.printf("rangeMin(0, 0) = %d, expected 5\n", minTree.query(0, 0));
        System.out.printf("rangeMin(3, 3) = %d, expected 3\n", minTree.query(3, 3));
        System.out.printf("rangeMin(8, 8) = %d, expected 10\n", minTree.query(8, 8));
        System.out.printf("rangeMin(0, 8) = %d, expected 2\n", minTree.query(0, 8));
        System.out.printf("rangeMin(1, 5) = %d, expected 2\n", minTree.query(1, 5));

        System.out.println("Set array[0] = 0");
        minTree.set(0, 0);
        System.out.println("Set array[6] = 1");
        minTree.set(6, 1);
        // idx:  0  1  2  3  4  5  6  7  8
        // val:  0, 8, 6, 3, 2, 7, 1, 6, 10
        System.out.printf("rangeMin(0, 0) = %d, expected 0\n", minTree.query(0, 0));
        System.out.printf("rangeMin(6, 6) = %d, expected 1\n", minTree.query(6, 6));
        System.out.printf("rangeMin(0, 8) = %d, expected 0\n", minTree.query(0, 8));
        System.out.printf("rangeMin(1, 5) = %d, expected 2\n", minTree.query(1, 5));
    }
}

