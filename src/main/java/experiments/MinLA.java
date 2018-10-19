package experiments;

import core.Solver;
import gurobi.GRBException;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;
import problems.Edge;

import java.util.LinkedList;
import java.util.Random;

public class MinLA {

    static Random random;

    /**
     * Creates a random graph with n vertices. Each edge has a probability p to appear.
     *
     * @param n the number of vertices
     * @param p the probability of each edge to appear
     * @return an array of edges representing the graph
     */
    private static Edge[] randomGraph(int n, double p) {
        LinkedList<Edge> edges = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (random.nextDouble() <= p) {
                    edges.add(new Edge(i, j, -1));
                }
            }
        }

        return edges.toArray(new Edge[0]);
    }

    public static void main(String[] args) {
        random = new Random(12);

        int n = 12;
        double p = 0.6;
        Edge[] edges = randomGraph(n, p);

        try {
            mip.MinLA mip = new mip.MinLA(n, edges);
            //mip.solve();
        } catch (GRBException e) {
            e.printStackTrace();
        }

        problems.MinLA mdd = new problems.MinLA(n, edges);
        Solver s = new Solver(mdd, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        s.solve();
    }

}
