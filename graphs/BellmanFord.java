import java.util.Arrays;

/**
 * Bellman-Ford algorithm calculates the shortest distance from
 * a starting point to all other points, assuming that the graph
 * does not contain a cycle with negative length.
 *
 * It does so by iterating V time:
 *  for each edge (a, b) with weight w, update
 *      dist[b] = min(dist[b], dist[a]+w)
 * 
 * note that dist[i] is initialized to INF with dist[start] = 0.
 *
 * runtime complexity: O(VE) where V is the number of vertices and
 * E is the number of edges.
 */
public class BellmanFord {
    static class Edge {
        private int start;
        private int end;
        private int weight;

        public Edge(int start, int end, int weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getWeight() {
            return weight;
        }
    }

    public static int[] getShortestDistances(final Edge[] edges, final int nVertices, final int start) {
        final int[] dist = new int[nVertices];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[start] = 0;

        for (int i = 0; i < nVertices; i++) {
            // loop through each edge
            for (final Edge e : edges) {
                final int a = e.getStart();
                final int b = e.getEnd();
                final int weight = e.getWeight();
                dist[b] = Math.min(dist[b], dist[a]+weight);
            }
        }
        return dist;
    }

    public static void main(final String[] args) {
        final Edge[] edges = {
            new Edge(1, 2, 2),
            new Edge(1, 3, 3),
            new Edge(1, 4, 7),
            new Edge(2, 4, 3),
            new Edge(2, 5, 5),
            new Edge(3, 4, -2),
            new Edge(4, 5, 2)
        };
        final int[] dist = getShortestDistances(edges, 6, 1);
        System.out.println(Arrays.toString(dist));
        System.out.println("Expected");
        System.out.println(Arrays.toString(new int[]{ Integer.MAX_VALUE, 0, 2, 3, 1, 3 }));
    }
}

