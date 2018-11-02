package problems;

import java.util.*;

/**
 * Representation of an edge for the MCP and MISP problems.
 *
 * @author Vianney Coppé
 */
public class Edge {

    public int u, v;
    double w;

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
    public Edge(int u, int v, double w) {
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

    public static Map<Integer, Double>[] toWeightedGraph(int n, Edge[] edges) {
        @SuppressWarnings("unchecked")
        Map<Integer, Double>[] g = new Map[n];

        for (int i = 0; i < g.length; i++) {
            g[i] = new HashMap<>();
        }

        for (Edge e : edges) {
            g[e.u].put(e.v, e.w);
            g[e.v].put(e.u, e.w);
        }

        return g;
    }

    /**
     * Creates a random graph with n vertices. Each edge has a probability p to appear.
     *
     * @param n the number of vertices
     * @param p the density of the graph
     * @return an array of edges representing the graph
     */
    private static Edge[] randomGraph(int n, double p, Random random) {
        int m = (int) Math.floor(p * n * (n - 1) / 2);
        Edge[] edges = new Edge[m];
        LinkedList<Boolean> in = new LinkedList<>();

        for (int i = 0; i < n * (n - 1) / 2; i++) {
            in.add(i < m);
        }

        Collections.shuffle(in, random);

        int k = 0, l = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (in.get(l++)) {
                    edges[k++] = new Edge(i, j, -1);
                }
            }
        }

        return edges;
    }
}