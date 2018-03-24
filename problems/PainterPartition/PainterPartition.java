import java.lang.System;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Finds the solution to the Painter's Partition problem
 *
 * Exhaustive search
 * =================
 * The simplest solution would be just tryng out all different partitions, which means
 * selecting k subset from n objects, C(k, n) = n! / ((n-k)! k!),
 * Run time complexity: O(n! / ((n-k)! k!))
 * Space complexity: O(k) since the running stack would be at most k steps deep.
 *
 * Dynamic Programming
 * ===================
 * The other way is to use dynamic programming to build up the solution
 *  k = 4, n = 7
 *  B = [10,  5,  7,  9,  2,  3,  1]
 *
 * k\n |  0,  1,  2,  3,  4,  5,  6
 *  0  | 10, 15, 22, 31, 33, 36, 37
 *  1  | 10, 10, 12, 16, 18, 21, 22  
 *  2  | 10, 10, 10, 12, 12, 14, 15
 *  3  | 10, 10, 10, 10, 11, 12, 12
 *
 * where k is the number of painters available to do the work
 * n is the number of boards available.
 * T[i][j] means the minimum time needed to finish painting the the first (j+1) boards with
 * (i+1) painters, 0 <= i < k, 0 <= j < n.
 * 
 * Obviously T[0][j] = sum(B[0..j]), since there's only one painter available to do the work
 * for i > 0:
 *  // there's more painters than boards to paint
 *  j < i =>  
 *          T[i][j] = T[i-1][j]  
 *  // there's at least as many painters as there are boards to paint
 *  j >= i => 
 *          T[i][j] = min(1 <= m <= j){
 *              // there is m ways to split the work, m = len([1...j])
 *              // have the previous [0...i-1] painters to paint first [0...j-m] boards, 
 *              //  which as the cost of T[i-1][j-m]
 *              // and ith painter to paint the remaining [j-m+1...j] boards,
 *              //  which has the cost of T[0][j] - T[0][j-m].
 *                  max(T[i-1][j-m], T[0][j] - T[0][j-m])
 *            }
 * Run time complexity: O(k * n * n)
 * Space complexity: O(n)
 *
 * Using Binary Search
 * ===================
 * The idea here is to use binary search to search for the partition size, which should be
 * between max(B[0...n]) for the case there's n painters and sum(B[0...n]) for the case there's only 1 painter.
 * For each binary step, use the max size to loop through the array to check for the number of painters needed.
 * If the number of painters needed is less than k, then we found a new upper bound, otherwise, we
 * find a new lower bound. Continue doing so until we have terminate the binary search.
 * Run time complexity: O(n * log sum(B[0...n]))
 * Space complexity: O(1)
 */
public class PainterPartition {
    // get mininum partition size using exhaustive search
    static int getMinPartitionSizeES(int maxPartitionCnt, int[] boards) {
        return getMinSizeES(maxPartitionCnt, boards, 0);
    }

    static int getMinSizeES(int partitionCnt, int[] boards, int index) {
        final int result;
        if (partitionCnt == 1) {
            int sum = 0;
            for (int i = index; i < boards.length; i++) {
                sum += boards[i];
            }
            result = sum;
        } else if (index == boards.length-1) {
            result = boards[index];
        } else {
            int min = Integer.MAX_VALUE;
            int curPaint = boards[index];
            for (int j = index+1; j < boards.length; j++) {
                final int restPaint = getMinSizeES(partitionCnt-1, boards, j);
                final int curMax = Math.max(curPaint, restPaint);
                min = Math.min(min, curMax);
                curPaint += boards[j];
            }
            result = min;
        }
        return result;
    }

    // get mininum parition size using dynamic programming
    static int getMinPartitionSizeDP(int maxPartitionCnt, int[] boards) {
        // ps: temporary working space
        // ps[cur] means the current row we are filling in
        // ps[(cur+1)%2] is the previous row we had filled
        // once ps[cur] is completed, then we do cur = (cur+1)%2 to swap the rows
        // we conitnue doing this after we have reached maxPartitionCnt times
        // then the answer would be found at ps[cur][boards.length-1]
        //
        int cur = 0;
        final int[][] ps = new int[2][boards.length];
        final int len = boards.length;

        ps[cur][0] = boards[0];
        for (int i = 1; i < len; i++) {
            ps[cur][i] = boards[i] + ps[cur][i-1];
        }
        System.arraycopy(ps[0], 0, ps[1], 0, len);
        final int[] initPs = Arrays.copyOf(ps[0], len);
        cur = 1;

        for (int i = 1; i < maxPartitionCnt; i++, cur = (cur+1)%2) {
            final int[] curPs = ps[cur];
            final int[] prevPs = ps[(cur+1)%2];
            for (int j = 0; j < len; j++) {
                if (j >= i) {
                    int min = Math.max(prevPs[j-1], initPs[j] - initPs[j-1]);
                    for (int m = 2; m <= j; m++) {
                        final int curMax = Math.max(prevPs[j-m], initPs[j] - initPs[j-m]);
                        min = Math.min(min, curMax);
                    }
                    curPs[j] = min;
                }
            }
        }

        return ps[(cur+1)%2][len-1];
    }
    
    static int getMinPartitionSizeBS(int maxPartitionCnt, int[] boards) {
        int sum = 0;
        int max = Integer.MIN_VALUE;
        for (int b : boards) {
            sum += b;
            max = Math.max(max, b);
        }
        int high = sum;
        int low = max;
        while (high > low) {
            final int mid = low + (high - low)/2;
            final int cnt = findPainterCnt(boards, mid);
            if (cnt <=  maxPartitionCnt) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    static int findPainterCnt(int[] boards, int size) {
        int cnt = 1;
        int s = 0;
        for (final int b : boards) {
            if (s + b > size) {
                cnt++;
                s = b;
            } else {
                s += b;
            }
        }
        return cnt;
    }

    static int[][] getPartitions(int[] boards, int minPartitionSize, int partitionCnt) {
        final int[][] splits = new int[partitionCnt][];
        int ind = 0;
        int prev = 0;
        int i = 0;
        for (; ind < boards.length; i++, prev=ind) {
            int runningSum = boards[ind];
            while (runningSum <= minPartitionSize && ind < boards.length) {
                runningSum += boards[ind++];
            }
            splits[i] = Arrays.copyOfRange(boards, prev, ind);
        }
        if (i < partitionCnt) {
            final int[][] result = new int[i][];
            for (int j = 0; j < i; j++) {
                result[j] = splits[j];
            }
            return result;
        }
        return splits;
    }

    public static void main(final String[] args) throws Exception {
        final Scanner scanner = new Scanner(System.in);
        final int numTestCases = scanner.nextInt();
        for (int i = 0; i < numTestCases; i++) {
            final int numPainter = scanner.nextInt();
            final int numBoards = scanner.nextInt();
            final int[] boards = new int[numBoards];
            for (int j = 0; j < numBoards; j++) {
                boards[j] = scanner.nextInt();
            }
            final int painterCnt = Math.min(numPainter, numBoards);
            final int result = getMinPartitionSizeDP(painterCnt, boards);
            final int result2 = getMinPartitionSizeES(painterCnt, boards);
            final int result3 = getMinPartitionSizeBS(painterCnt, boards);
            if (result != result2) {
                throw new Exception(
                        String.format("Expected getMinPartitionSizeDP = getMinPartitionSizeES, but got %d and %d\n", result, result2));
            }
            if (result != result3) {
                throw new Exception(
                        String.format("Expected getMinPartitionSizeDP = getMinPartitionSizeBS, but got %d and %d\n", result, result3));
            }
            System.out.println(result);

            // also output the split result
            final int[][] partitions = getPartitions(boards, result, painterCnt);
            for (int[] p : partitions) {
                System.out.print(Arrays.toString(p));
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}

