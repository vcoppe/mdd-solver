package examples;

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

}