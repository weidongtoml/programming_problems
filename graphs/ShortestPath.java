import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ShortestPath {
    /**
     * Use Dijkstra's algorithm to find the shortest path between source and destination.
     * @param graph: adjency matrix representation of the graph
     * @param source: source vertex
     * @param destination: destination vertext
     * @param parent: parent of each vertex in the shortest path
     * @return length of the shortest path from source to destination
     *
     * Runtime complexity: O(V*E)
     */
    public static int dijkstra(final int[][] graph, final int source, final int destination, final int[] parent) {
        final int n = graph.length;
        final boolean[] visited = new boolean[n];
        final int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        final Queue<Integer> queue = new LinkedList<>();

        dist[source] = 0;
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            final int p = queue.poll();
            final int curDist = dist[p];

            for (int v = 0; v < n; v++) {
                if (graph[p][v] > 0) {
                    // newDist is the distance from source to v via vertext p
                    final int newDist = graph[p][v] + curDist;
                    if (newDist < dist[v]) {
                        // current path is shorter than the old path, replace it.
                        dist[v] = newDist;
                        parent[v] = p;
                    }
                    // each vertex should go into the queue only once
                    if (!visited[v]) {
                        queue.add(v);
                        visited[v] = true;
                    }
                }
            }
        }
        return dist[destination];       
    }

    /**
     * Output the shortest path.
     * @param parent: array of parents of a given vertex on the path
     * @param v: end vertex of the shortest path
     */
    static void printShortestPath(final int[] parent, final int v) {
        if (parent[v] != -1) {
            printShortestPath(parent, parent[v]);
        }
        System.out.printf("%d ", v);
    }

    public static void main(final String[] args) {
        final int graph[][] = new int[][]{
            { 0,  4,  0,  0,  0,  0,  0,  8,  0},
            { 4,  0,  8,  0,  0,  0,  0, 11,  0},
            { 0,  8,  0,  7,  0,  4,  0,  0,  2},
            { 0,  0,  7,  0,  9, 14,  0,  0,  0},
            { 0,  0,  0,  9,  0, 10,  0,  0,  0},
            { 0,  0,  4, 14, 10,  0,  2,  0,  0},
            { 0,  0,  0,  0,  0,  2,  0,  1,  6},
            { 8, 11,  0,  0,  0,  0,  1,  0,  7},
            { 0,  0,  2,  0,  0,  0,  6,  7,  0}
        };
        final int[] parent = new int[graph.length];
        final int shortestPathLen = dijkstra(graph, 0, 4, parent);
        System.out.println(shortestPathLen);
        printShortestPath(parent, 4);
        System.out.println();
    }
}

