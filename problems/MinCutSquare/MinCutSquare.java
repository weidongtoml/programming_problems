/**
 * Given a paper of size A x B. Task is to cut the paper into squares of any size. 
 * Find the minimum number of squares that can be cut from the paper.
 *
 * Input:
 * The first line of input contains an integer T denoting the number of test cases. 
 * Then T test cases follow. Each test case contains two integer A and B denoting the two size of the paper.
 *
 * Output:
 * Print the minimum number of squares that can be cut from the paper.
 * 
 * Constraints:
 * 1<=T<=10^5
 * 1<=A<=10^5
 * 1<=B<=10^5
 *
 * Example:
 * Input:
 * 2
 * 13 29
 * 30 35
 * 
 * Output:
 * 9
 * 5
 * src: https://practice.geeksforgeeks.org/problems/min-cut-square/0
 *
 * 30 35 -> 5
 *
 * +--- 10 ---+-------- 20 ---------+
 * |          |                     |
 * 10        10                     |
 * |          |                     |
 * +--- 10 ---+                    20
 * |          |                     |
 * 10        10                     |
 * |          |                     |
 * +---- 15 -------+------ 15 ------+
 * |               |                |
 * |               |                |
 * 15             15               15
 * |               |                |
 * |               |                |
 * +---- 15 -------+------ 15 ------+
 *
 * Solution suggested by the editorial (it is not correct).
 * Given a rectangle, we can either split it horizontally or vertically. 
 * When the two sides are equal, we can cover it with a square, otherwise, keep trying to divide it
 * until we reach a square area.
 * Run time complexity: O(m * n * max(m, n))
 * Space complexity: O(m * n)
 * 
 * Correct solution (?) http://int-e.eu/~bf3/squares/
 */
import java.lang.System;
import java.util.Scanner;

public class MinCutSquare {
    static int getMinSquare(final int m, final int n) {
        int[][] tmp = new int[m+1][n+1];

        for (int i = 0; i <= m; i++) {
            tmp[i][1] = i;
        }

        for (int j = 0; j <= n; j++) {
            tmp[1][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (i == j) {
                    tmp[i][j] = 1;
                } else {
                    int hMin = Integer.MAX_VALUE;
                    for (int k = 1; k <= i / 2; k++) {
                        hMin = Math.min(hMin, tmp[k][j] + tmp[i-k][j]);
                    }
                    int vMin = Integer.MAX_VALUE;
                    for (int k = 1; k <= j / 2; k++) {
                        vMin = Math.min(vMin, tmp[i][k] + tmp[i][j-k]);
                    }
                    tmp[i][j] = Math.min(hMin, vMin);
                }
            }
        }
        return tmp[m][n];
    }

    public static void main(final String[] args) {
        final Scanner scanner = new Scanner(System.in);
        final int numTestCases = scanner.nextInt();
        for (int i = 0; i < numTestCases; i++) {
            final int m = scanner.nextInt();
            final int n = scanner.nextInt();
            final int count = getMinSquare(m, n);
            System.out.println(count);
        }
    }
}

