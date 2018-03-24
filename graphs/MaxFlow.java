import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;

/**
 * @see https://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 */
public class MaxFlow {

    /**
     * Uses Ford-Fulkerson's algorithm to calculate the maximum
     * flow of the graph from source to sink.
     * @param graph: n*n adjacency matrix.
     * @param source: source vertex id.
     * @param sink: sink vertex id.
     */
    public static int getMaxFlow(final int graph[][], final int source, final int sink) {
        final int n = graph.length;
        final int rGraph[][] = new int[n][0];

        for (int i = 0; i < n; i++) {
            rGraph[i] = Arrays.copyOf(graph[i], n);
        }

        int maxFlow = 0;
        final int parent[] = new int[n];

        // in the worst case, each iteration increase maxFlow by 1 unit,
        // then the following loop would require maxFlow times.
        //
        // Note that if in bfs we always find the shortest path instead of any path
        // which is known as the Edmond-Karp algorithm, then the following loop will run O(V*E) times.
        while (bfs(rGraph, source, sink, parent)) { 
            int minFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) {
                final int p = parent[v];
                minFlow = Math.min(minFlow, rGraph[p][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                final int p = parent[v];
                rGraph[p][v] -= minFlow;
                rGraph[v][p] += minFlow;
            }
            maxFlow += minFlow;
        }

        return maxFlow;
    }

    /**
     * Do breadth first search on the given graph startting from source, 
     * and build up a path from source to sink, return true if such a path
     * exists.
     * @param graph: n*n adjacency matrix
     * @param source: source vertex
     * @param sink: destination vertex
     * @param parant: array of the parent vertices
     * @return true if there's a path exist from source to sink, which could
     * be retrieved using parant array.
     * 
     * runtime complexity: O(n^2), actually it should be O(V*E)
     * space complexity: O(n), for keeping track of visited nodes and the queue.
     */
    private static boolean bfs(final int[][] graph, int source, int sink, int[] parent) {
        final int n = graph.length;
        final boolean[] visited = new boolean[n];
        final Queue<Integer> queue = new LinkedList<>();
        
        Arrays.fill(parent, -1);

        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        // this can be done better using adjency list, so that no edges are repeated, hence O(V+E).
        while (!queue.isEmpty()) { // repeat V time since each vertex goes into the queue once.
            final int p = queue.poll();
            // repeat E time to check each connecting vertex.
            for (int v = 0; v < n; v++) {  
                if (!visited[v] && graph[p][v] > 0) {
                    queue.add(v);
                    visited[v] = true;
                    parent[v] = p;
                }
            }
        }

        return visited[sink];
    }

    public static void main(final String[] args) {
        final int graph[][] = new int[][] {
            {0, 16, 13,  0,  0,  0},
            {0,  0, 10, 12,  0,  0},
            {0,  4,  0,  0, 14,  0},
            {0 , 0,  9,  0,  0, 20},
            {0,  0,  0,  7,  0,  4},
            {0,  0,  0,  0,  0,  0}
        };
        final int maxFlow = getMaxFlow(graph, 0, 5);
        System.out.printf("Maximum flow is %d\n", maxFlow);
    }
}
       
