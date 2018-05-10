import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

public class MinSpanTree {
    static class Edge {
        final int vertex1;
        final int vertex2; 
        final int weight;

        Edge(final int v1, final int v2, final int w) {
            vertex1 = v1;
            vertex2 = v2;
            weight = w;
        }

        @Override
        public String toString() {
            return String.format("%d<->%d (%d)", vertex1, vertex2, weight);
        }
    }

    static class UnionFind {
        private Map<Integer, Integer> sets = new HashMap<>();

        void newSet(int v) {
            sets.put(v, v);
        }

        boolean isPresent(final int v) {
            return sets.containsKey(v);
        }

        private int find(final int v) {
            int r = v;
            while (r != sets.get(r)) {
                r = sets.get(r);
            }
            // collapse the tree to only 1 level
            int r1 = v;
            while (r1 != sets.get(r1)) {
                final int old_r = r1;
                r1 = sets.get(r1);
                sets.put(old_r, r);
            }
            return r;
        }

        void union(final int v1, final int v2) {
            final int c1 = find(v1);
            final int c2 = find(v2);
            sets.put(Math.max(c1, c2), Math.min(c1, c2));
        }

        boolean sameSet(final int v1, final int v2) {
            return find(v1) == find(v2);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("k: ");
            for (final Integer i : sets.keySet()) {
                sb.append(i);
                sb.append(' ');
            }
            sb.append("\nv: ");
            for (final Integer i : sets.keySet()) {
                sb.append(sets.get(i));
                sb.append(' ');
            }
            sb.append("\n");

            return sb.toString();
        }
    }

    static Edge[] minSpanTreeViaKruskal(final Edge[] graph) {
        Arrays.sort(graph, 0, graph.length, new Comparator<Edge>() {
            @Override
            public int compare(final Edge e1, final Edge e2) {
                return e1.weight - e2.weight;
            }
        });

        final List<Edge> result = new ArrayList<Edge>();
        final UnionFind components = new UnionFind();
        for (final Edge e : graph) {
            final int v1 = e.vertex1;
            final int v2 = e.vertex2;
            if (!components.isPresent(v1)) {
                components.newSet(v1);
            }
            if (!components.isPresent(v2)) {
                components.newSet(v2);
            }
            if (!components.sameSet(v1, v2)) {
                components.union(v1, v2);
                result.add(e);
            }
        }

        System.out.println(components.toString());
        return result.toArray(new Edge[0]);
    }

    public static void main(final String[] args) {
        final Edge[] graph = {
            new Edge(5, 6, 2),
            new Edge(1, 2, 3),
            new Edge(3, 6, 3),
            new Edge(1, 5, 5),
            new Edge(2, 3, 5),
            new Edge(2, 5, 6),
            new Edge(4, 6, 7),
            new Edge(3, 4, 9)
        };

        final Edge[] spanningTree = minSpanTreeViaKruskal(graph);
        System.out.println(Arrays.toString(spanningTree));
    }
}

