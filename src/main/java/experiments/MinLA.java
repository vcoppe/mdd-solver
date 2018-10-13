package experiments;

import core.Solver;
import gurobi.GRBException;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;
import problems.Edge;

import java.util.Random;

public class MinLA {

    public static void main(String[] args) {
        int n = 12, m = 40;
        Edge[] edges = new Edge[m];

        Random random = new Random(12);
        int u, v;
        for (int i = 0; i < m; i++) {
            do {
                u = random.nextInt(n);
                v = random.nextInt(n);
            } while (u == v);
            edges[i] = new Edge(u, v, -1);
        }


        try {
            mip.MinLA mip = new mip.MinLA(n, edges);
            mip.solve();
        } catch (GRBException e) {
            e.printStackTrace();
        }

        problems.MinLA mdd = new problems.MinLA(n, edges);
        Solver s = new Solver(mdd, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        s.solve();
    }

}
