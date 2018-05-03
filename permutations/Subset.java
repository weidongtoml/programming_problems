import java.util.function.Consumer;
import java.util.Set;
import java.util.HashSet;

/**
 * A class to generate all subsets of the set of integer {0, 1, ..., n-1}.
 * When a subset is generated, the consumer is invoked to process it.
 * There's two ways to generate the subsets
 * (1) create an object of Subset and invoke its run method.
 * (2) use static method processSubsets
 */
public class Subset {
    final Consumer<Set<Integer>> consumer;
    final int n;

    public Subset(final Consumer<Set<Integer>> consumer, final int n) {
        this.consumer = consumer;
        this.n = n;
    }

    public void run() {
        final Set<Integer> set = new HashSet<Integer>();
        generate(set, 0);
    }

    private void generate(final Set<Integer> elements, final int k) {
        if (k == n) {
            consumer.accept(elements);
        } else {
            generate(elements, k+1);
            elements.add(k);
            generate(elements, k+1);
            elements.remove(k);
        }
    }

    public static void processSubsets(final Consumer<Set<Integer>> consumer, final int n) {
        final Set<Integer> set = new HashSet<Integer>();
        for (int s = 0; s < (1 << n); s++) {
            set.clear();
            for (int i = 0; i < n; i++) {
               if ((s & (1 << i)) != 0) {
                  set.add(i);
               }
            }
           consumer.accept(set);
        }
    } 


    public static void main(final String[] args) {
        // using search method to generate all subsets.
        System.out.println("Using recursive search to generate all subsets");
        final Subset s = new Subset(subset -> System.out.println(subset), 4);
        s.run();

        System.out.println("Using binary representation of integer to generate all subsets.");
        // exploting bit representation of integer for generating all subsets
        Subset.processSubsets(subset -> System.out.println(subset), 4);
    }
}

