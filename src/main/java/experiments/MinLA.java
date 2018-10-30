package experiments;

import core.Solver;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;
import mdd.State;
import problems.Edge;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
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

        int timeLimit = 30 * 60, minN = 6, maxN = 26;
        int[] widths = {100, 1000, 10000};
        double p = 0.7;

        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy@HH_mm_ss");
        Date date = new Date();
        String fileName = "/Users/Vianney/Desktop/results_" + dateFormat.format(date);

        try {
            PrintWriter out = new PrintWriter(fileName);

            for (int n = minN; n <= maxN; n += 2) {
                out.println("n " + n);

                Edge[] edges = randomGraph(n, p);

                mip.MinLA mip = new mip.MinLA(n, edges);
                mip.solve(timeLimit);

                out.printf(Locale.US, "%.3f %.0f %.5f\n", mip.runTime(), mip.objVal(), mip.gap());

                mip.dispose();

                problems.MinLA mdd = new problems.MinLA(n, edges);
                Solver solver = new Solver(mdd, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
                State result = solver.solve(timeLimit);

                out.printf(Locale.US, "%.3f %.0f %.5f\n", solver.runTime(), -result.value(), solver.gap());

                for (int width : widths) {
                    solver.setWidth(width);
                    result = solver.solve(timeLimit);

                    out.printf(Locale.US, "%.3f %.0f %.5f\n", solver.runTime(), -result.value(), solver.gap());
                }
            }

            out.close();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

}
