package problems;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Representation of an edge for the MCP and MISP problems.
 *
 * @author Vianney Coppé
 */
public class Edge {

    public int u, v, w;

    /**
     * Returns an {@code Edge} object connecting the vertices {@code u} and {@code v}.
     */
    public Edge(int u, int v) {
        this.u = u;
        this.v = v;
    }

    /**
     * Returns an {@code Edge} object connecting the vertices {@code u} and {@code v}
     * with weight {@code w}.
     */
    public Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public static LinkedList<Integer>[] toGraph(int n, Edge[] edges) {
        @SuppressWarnings("unchecked")
        LinkedList<Integer>[] adj = new LinkedList[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new LinkedList<>();
        }

        for (Edge e : edges) {
            adj[e.u].add(e.v);
            adj[e.v].add(e.u);
        }

        return adj;
    }

    public static Map<Integer, Integer>[] toWeightedGraph(int n, Edge[] edges) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer>[] g = new Map[n];

        for (int i = 0; i < g.length; i++) {
            g[i] = new HashMap<>();
        }

        for (Edge e : edges) {
            g[e.u].put(e.v, e.w);
            g[e.v].put(e.u, e.w);
        }

        return g;
    }
}