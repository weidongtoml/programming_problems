import java.lang.System;
import java.util.Scanner;

/**
 * Permutations of multi-sets is given by
 * P(n; m1, m2, m3, ...,m_i) = n! / (m1! m2! m3! ... m_i !), n = m1 + m2 + m3 + ... m_i
 * @see https://en.wikipedia.org/wiki/Permutation#Permutations_of_multisets
 *
 * all a's => 1
 * (n-1) a's + b  => P(n; n-1, 1) => n
 * (n-1) a's + c  => P(n; n-1, 1) => n
 * (n-2) a's + b + c => P(n; n-2, 1, 1) => n*(n-1)
 * (n-2) a's + 2 c's => P(n; n-2, 2) => n*(n-1) / 2
 * (n-3) a's + b + 2 c's => P(n; n-3, 1, 2) => n*(n-1)*(n-2) / 2
 *
 * Summing them up and simplify gives: 1 + 2*n + n*(n*n - 1) / 2
 *
 * TODO(weidong): what about using exhaustive search?
 */
public class StringPermutation {
    static int getPermutation(final int n) {
        return 1 + 2 * n + n * (n * n - 1) / 2;
    }

    public static void main(final String[] args) throws Exception {
        final Scanner scanner = new Scanner(System.in);
        final int numTestCases = scanner.nextInt();
        for (int i = 0; i < numTestCases; i++) {
            final int n = scanner.nextInt();
            final int count = getPermutation(n);
            System.out.println(count);
        }
    }
}

