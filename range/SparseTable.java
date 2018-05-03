import java.util.function.BiFunction;
import java.util.Arrays;

/**
 * Sparse Table is a data structure for efficently solving the range query problem
 * if the given array is fixed. A given range query could be completed in O(log(N))
 * time with initial O(N*log(N)) preprocessing time. Space complexity of O(N*log(N)).
 *
 * Sparse table can be applied iff:
 * (1) the given array is immutable
 * (2) the query function F is associative: F(a, b, c) = F(F(a, b), c) = F(a, F(b, c))
 *
 * The idea is having a table such that
 *   table[i][j] = F(Arr_i, Arr_(i+1), ..., Arr_(i + 2^j - 1))
 *   where 0 <= j <= log2(n - i), because i+2^j-1 < n
 * i.e.
 *   table[i][0] = Func(Arr_i)
 *   table[i][1] = Func(Arr_i, ..., Arr_(i + 1)) // i.e. [i..i+1]
 *   table[i][2] = Func(Arr_i, ..., Arr_(i + 3)) // i..e [i, i+1, i+2, i+3]
 *
 * This precomputed table is then used to answer the given range query.
 * Not that this table basically breaks down all combination intervals into ranges 
 * of sizes that are powers of 2. 
 * Then any range could be found by combining the dis-joint power of 2 ranges.
 * It is this decomposition that allows the query to be answered in O(log(N)) time
 * instead of O(N) time because we don't have to iterate through all elements.
 *
 * To calculate Func(Arr_0, ..., Arr_R), where R = 2^j-1
 *  we just use the precomputed result of table[0][j].
 * In the case where R != 2^j - 1, let's assume that R > 2^j -1,
 * then we can break the array into [Arr_0, ..., Arr_(2^j-1)], [Arr_(2^j),..., R],
 *  then calculate the result using Func(table[0][j], Func(Arr_(2^j), ..., R)),
 *  repeat the same process for calculating Func(Arr_(L'), ..., R)..., where L' = 2^j
 *  at each step, we try to find largest j that satisfies 2^j-1 <= R - L'. 
 *  continue breaking it down until there's no such j.
 *
 * more generally, the answer to F(Arr_L, ..., Arr_R) could be found using:
 *  answer = ZERO
 *  m = L
 *  for i=k...0
 *      if m + 2^i - 1 <= R: // find largest i that is still within range.
 *          answer = F(answer, table[m][i])
 *          m += 2^i
 * where F(ZERO, x) = F(x, ZERO) = x.
 * Note that here we are using greedy method to break down R - L + 1 into its binary representation.
 *
 * e.g.
 *   to calculate F(Arr_i, ..., Arr_(i+12))
 *   let
 *      x1 = F(Arr_i, ..., Arr_(i+7)) = table[i][3], 7 = 2^3 - 1
 *      x2 = F(Arr_(i+8), ..., Arr_(i+11)) = table[i+8][2], 11 - 8 = 3 = 2^2 - 1
 *      x3 = F(Arr_(i+12)) = table[i+12][0], 0 = 2^0 - 1
 *   then
 *      F(Arr_i, ..., Arr_(i+7)) = F(x1, x2, x3).
 *   hence only 4 operations are needed instead of 13. Note that we could use the binary
 *   representation of integer to break down to ranges to x1, x2, x3, in this case
 *   13 = 8 + 4 + 1 = 2^3 + 2^2 + 2^0
 *
 * To build the table efficiently, we can use the following property:
 *  table[r][j+1] = F(Arr_r, ..., Arr_(r + 2^(j+1) - 1))
 *      = F(F(Arr_r, ..., Arr_(r + 2^j - 1)), F(Arr_(r+2^j), ..., Arr_(r + 2^(j+1) - 1)))
 *      = F(table[r][j], F(Arr_(r+2^j), ..., Arr_((r+2^j) + (2^j) - 1))]
 *      = F(table[r][j], table[r+2^j][j])
 * i.e.
 *  table[r][j+1] = F(table[r][j], table[r+2^j][j])
 *
 * Examples of such queries include: maximum value, minimum value, sum, greatest common divisor, etc,
 * of a given range in the array.
 *
 * Ref: https://www.hackerearth.com/ja/practice/notes/sparse-table/
 */
public class SparseTable {
    private final int k;
    private final int n;
    private final int[][] table;
    private final int zero;
    private final BiFunction<Integer, Integer, Integer> func;

    /**
     * Constructs a SpareTable that can be used to answer the range query of func(array[left...right]).
     * @param array: an immutable array of querying
     * @param zero: the value that satisifies func(x, zero) = x.
     * @param func: the query function, which must be associative, i.e. func(x, func(y, z)) = func(func(x, y), z)
     */
    public SparseTable(
            final int[] array, 
            final int zero, 
            final BiFunction<Integer, Integer, Integer> func
    ) {
        n = array.length;
        k = largestPowerOf2NotGreaterThanN(n);
        this.zero = zero;
        this.func = func;

        table = new int[n][k+1];
        for (int i = 0; i < n; i++) {
            table[i][0] = array[i]; // F(Arr_i) = F(Arr_i, zero) = Arr_i.
        }
        // table[i][j] = Func(Arr_i,..., Arr_(i + 2^j - 1))
        // i + 2^j - 1 < n
        // i + (1 << j) - 1 < n
        // i + (1 << j) <= n
        for (int j = 1; j <= k; j++) { // n < 2 ^ (k+1)
            for (int i = 0; i + (1 << j) <= n; i++) {                  
                table[i][j] = func.apply(table[i][j-1], table[i + (1 << (j - 1))][j-1]);
            }
        }
    }

    /**
     * Answer the range query of func(array[start...end]).
     * @param start: starting index
     * @param end: ending index
     * @return func(array[start...end])
     */
    public int query(final int start, final int end) {
        if (start > end) {
            throw new IllegalArgumentException(String.format("Invalid range, expected start <= end, but got [%d, %d]", start, end));
        }
        if (start < 0 || end >= n) {
            throw new IllegalArgumentException(String.format("Invalid range, expected [0, %d], but got [%d, %d].", n, start, end));
        }
        int result = zero;
        int m = start;
        for (int i = k; i >= 0; i--) {
            if (m + (1 << i) - 1 <= end) {
                // table[m][i] = Func(Arr_m, ..., Arr_(m + 2^i - 1))
                result = func.apply(result, table[m][i]);
                // next section is Arr_(m + 2^i), ..., Arr_R)]
                m += (1 << i);
            }
        }
        return result;
    }

    private static int largestPowerOf2NotGreaterThanN(final int n) {
        if (n < 1) {
            throw new IllegalArgumentException(
                    String.format("parameter must be greater than 1, but got: %d.", n));
        }
        int i = 0;
        while ((1L << i) <= n) {
            i++;
        }
        return i-1;
    }

    public static void main(final String args[]) {
        System.out.println("Testing largestPowerOf2NotGreaterThanN");
        System.out.printf("largestPowerOf2NotGreaterThanN(1) = %d, expected 0\n", largestPowerOf2NotGreaterThanN(1));
        System.out.printf("largestPowerOf2NotGreaterThanN(2) = %d, expected 1\n", largestPowerOf2NotGreaterThanN(2));
        System.out.printf("largestPowerOf2NotGreaterThanN(5) = %d, expected 2\n", largestPowerOf2NotGreaterThanN(5));
        System.out.printf("largestPowerOf2NotGreaterThanN(14) = %d, expected 3\n", largestPowerOf2NotGreaterThanN(14));
        // Java only has signed int, which ranges from -2^31 to 2^31 - 1.
        System.out.printf("largestPowerOf2NotGreaterThanN(%d) = %d, expected 30\n",
                Integer.MAX_VALUE,
                largestPowerOf2NotGreaterThanN(Integer.MAX_VALUE));

        //             0  1  2  3  4  5  6  7, 8
        int[] array = {1, 3, 8, 4, 6, 1, 3, 4, 5};

        System.out.printf("Testing query of range sum, for array: %s\n", Arrays.toString(array));
        final SparseTable rangeSumTable = new SparseTable(array, 0, (x, y) -> x + y);
        System.out.printf("sum(0, 0) = %d, expected 1\n", rangeSumTable.query(0, 0));
        System.out.printf("sum(7, 7) = %d, expected 4\n", rangeSumTable.query(7, 7));
        System.out.printf("sum(2, 2) = %d, expected 8\n", rangeSumTable.query(2, 2));
        System.out.printf("sum(0, 7) = %d, expected 30\n", rangeSumTable.query(0, 7));
        System.out.printf("sum(3, 5) = %d, expected 11\n", rangeSumTable.query(3, 5));
        System.out.printf("sum(7, 8) = %d, expected 9\n", rangeSumTable.query(7, 8));

        System.out.printf("Testing query of range min, for array: %s\n", Arrays.toString(array));
        final SparseTable rangeMinTable = new SparseTable(array, Integer.MAX_VALUE, (x, y) -> Math.min(x, y));
        System.out.printf("min(0, 0) = %d, expected 1\n", rangeMinTable.query(0, 0));
        System.out.printf("min(7, 7) = %d, expected 4\n", rangeMinTable.query(7, 7));
        System.out.printf("min(2, 2) = %d, expected 8\n", rangeMinTable.query(2, 2));
        System.out.printf("min(0, 7) = %d, expected 1\n", rangeMinTable.query(0, 7));
        System.out.printf("min(2, 4) = %d, expected 4\n", rangeMinTable.query(2, 4));
        System.out.printf("sum(7, 8) = %d, expected 4\n", rangeMinTable.query(7, 8));

        System.out.printf("Testing query of range max, for array: %s\n", Arrays.toString(array));
        final SparseTable rangeMaxTable= new SparseTable(array, Integer.MIN_VALUE, (x, y) -> Math.max(x, y));
        System.out.printf("max(0, 0) = %d, expected 1\n", rangeMaxTable.query(0, 0));
        System.out.printf("max(7, 7) = %d, expected 4\n", rangeMaxTable.query(7, 7));
        System.out.printf("max(2, 2) = %d, expected 8\n", rangeMaxTable.query(2, 2));
        System.out.printf("max(0, 7) = %d, expected 8\n", rangeMaxTable.query(0, 7));
        System.out.printf("sum(7, 8) = %d, expected 5\n", rangeMaxTable.query(7, 8));
        System.out.printf("max(3, 5) = %d, expected 6\n", rangeMaxTable.query(3, 5));
    }
}

