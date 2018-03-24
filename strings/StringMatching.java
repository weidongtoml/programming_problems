import java.util.Map;
import java.util.HashMap;

public class StringMatching {
    static class HashUtil {
        private static final int MERSENNE_PRIME_13 = (2 << 12) - 1;
        private static final int MAX_CHAR = Character.MAX_VALUE; 

        static boolean isSubStrEqual(final String str, final int startIndex, final String pattern) {
            final int m = pattern.length();
            final int n = str.length() - startIndex;
            if (n < m) {
                return false;
            }
            for (int i = 0, j = startIndex; i < m; i++, j++) {
                if (pattern.charAt(i) != str.charAt(j)) {
                    return false;
                }
            }
            return true;
        }

        static int hashWithoutLeadingElement(final int hash, final char leadChar, final int multiplier) {
            return (hash - (leadChar * multiplier) % MERSENNE_PRIME_13 + MERSENNE_PRIME_13) % MERSENNE_PRIME_13;
        }

        static int hash(final String p) {
            int h = 0;
            for (final char c : p.toCharArray()) {
                h = hash_c(c, h);
            }
            return h;
        }

        static int getMultiplier(final int patternLen) {
            int h = 1;
            for (int i = 1; i < patternLen; i++) {
                h = hash_c((char)0, h);
            }
            return h;
        }

        static int hash_c(final char c, final int v) {
            return (v * MAX_CHAR + c) % MERSENNE_PRIME_13;
        }

        static void runTests() {
            final int hash0 = hash("jumps"); 
            final int hash1 = hash_c('s', 
                    hashWithoutLeadingElement(hash("ajump"), 'a', getMultiplier(5)));
            System.out.printf("hash('jump') =? hash_c('p', hash('ajum') - 'a' * multiplier)\n %d == %d\n",
                    hash0, hash1);
            System.out.printf("multiplier = %d\n", getMultiplier(5));
        }
    }

    /**
     * This class implements the Rabin Karp algorithm for string matching.
     * Rabin Karp's algorithm finds a pattern substring in a given text using average O(m + n)
     * runtime complexity, and worst case of O(m*n).
     * It does so by comparing the hash value of the pattern against each substring of the text
     * instead of the naive way of comparing characters by characters.
     * Here a rolling hash function is applied so that computation of the hash value of
     * the subsequent substring of text can be done in constant time using:
     *     hash(text[i+1...m+1]) = (hash(text[i...m]) - text[i]*D(m)) * C + text[m]
     * 
     * Note that the performance of the algorithm depends on the rolling hash function applied.
     *
     * Reference: https://en.wikipedia.org/wiki/Rabin–Karp_algorithm
     */
    public static class RabinKarp {
        private final int pHash;
        private final int multiplier;
        private final String pattern;

        public RabinKarp(final String pattern) {
            pHash = HashUtil.hash(pattern);
            multiplier = HashUtil.getMultiplier(pattern.length());
            this.pattern = pattern;

            System.out.printf("pattern: %s, pHash: %d, multiplier: %d\n", pattern, pHash, multiplier);
        }

        public int match(final String str) {
            final int m = pattern.length();
            int hash = HashUtil.hash(str.substring(0, m));

            final int n = str.length();
            for (int i = 0; i < n - m; i++) {
                System.out.printf("[%s] i=%d, hash=%d\n", str.substring(i, i+m), i, hash);
                if (hash == pHash && HashUtil.isSubStrEqual(str, i, pattern)) {
                    System.out.println("Found matching index");
                    return i;
                }
                hash = HashUtil.hash_c(str.charAt(i+m), 
                        HashUtil.hashWithoutLeadingElement(hash, str.charAt(i), multiplier));
            }
            System.out.println("Matching index not found");
            return -1;
        }

        static void runTests() {
            final String str = "The quick brown fox jumps over the lazy dog";
            final String pattern = "jumps";
            final RabinKarp rabinKarp = new RabinKarp(pattern);
            final int index = rabinKarp.match(str);
            System.out.println(index);
        }
    }

    /**
     * Rabin Karp's algorithm for matching multiple patterns the same length,
     * with an average running time of O(n + km) where k is the number patterns
     * to match against.
     */
    public static class RabinKarpSet {
        private final Map<Integer, String> hashToPattern;
        private final int multiplier;
        private final int m;

        public RabinKarpSet(final String... patterns) {
            m = patterns[0].length();
            multiplier = HashUtil.getMultiplier(m);
            
            hashToPattern = new HashMap<>();
            for (final String p : patterns) {
                final int hash = HashUtil.hash(p);
                hashToPattern.put(hash, p);
            }
        }

        public int match(final String str) {
            int hash = HashUtil.hash(str.substring(0, m));

            final int n = str.length();
            for (int i = 0; i < n - m; i++) {
                System.out.printf("[%s] i=%d, hash=%d\n", str.substring(i, i+m), i, hash);
                final String pattern = hashToPattern.get(hash);    
                if (pattern != null && HashUtil.isSubStrEqual(str, i, pattern)) {
                    System.out.println("Found matching index");
                    return i;
                }
                hash = HashUtil.hash_c(str.charAt(i+m), 
                        HashUtil.hashWithoutLeadingElement(hash, str.charAt(i), multiplier));
            }
            System.out.println("Matching index not found");
            return -1;
        }

        public static void runTests() {
            final String str = "The quick brown fox jumps over the lazy dog";
            final RabinKarpSet rabinKarpSet = new RabinKarpSet("quick", "brown", "jumps");
            int startIndex = 0;
            for (int i = 0; i < 3; i++) {
                final int index = rabinKarpSet.match(str.substring(startIndex));
                startIndex += index;
                System.out.printf("%d: %d\n", i, startIndex);
                startIndex += 5;
            }
        }
    }


    public static void main(final String[] args) {
        System.out.printf("Testing HashUtil\n");
        HashUtil.runTests();
        
        System.out.printf("\nTesting RabinKarp\n");
        RabinKarp.runTests();

        System.out.printf("\nTesting RabinKarpSet\n");
        RabinKarpSet.runTests();
    }
}

