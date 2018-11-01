package experiments;

import core.Solver;
import mdd.State;
import problems.Edge;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MinLA {

    static Random random;

    /**
     * Creates a random graph with n vertices. Each edge has a probability p to appear.
     *
     * @param n the number of vertices
     * @param p the density of the graph
     * @return an array of edges representing the graph
     */
    private static Edge[] randomGraph(int n, double p) {
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

    public static void main(String[] args) {
        random = new Random(12);

        int timeLimit = 30 * 60, reps = 3;
        int[] vertices = {4, 6, 8}; //, 10, 12, 14, 17, 20, 24, 28};
        int[] widths = {100, 500, 1000};
        double p = 0.7;

        DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy@HH_mm_ss");
        Date date = new Date();
        String fileName = "results_" + dateFormat.format(date);

        try {
            PrintWriter out = new PrintWriter(fileName);

            out.println(2 + widths.length);
            out.println("mip");
            out.println("mdd");

            for (int width : widths) out.println("mdd-" + width);

            out.println(reps);

            for (int n : vertices) {
                out.println(n);

                for (int rep = 0; rep < reps; rep++) {
                    Edge[] edges = randomGraph(n, p);

                    mip.MinLA mip = new mip.MinLA(n, edges);
                    mip.solve(timeLimit);

                    out.printf(Locale.US, "%.3f %.0f %.5f\n", mip.runTime(), mip.objVal(), mip.gap());

                    mip.dispose();

                    problems.MinLA mdd = new problems.MinLA(n, edges);
                    Solver solver = new Solver(mdd);
                    State result = solver.solve(timeLimit);

                    out.printf(Locale.US, "%.3f %.0f %.5f\n", solver.runTime(), -result.value(), solver.gap());

                    for (int width : widths) {
                        solver.setWidth(width);
                        result = solver.solve(timeLimit);

                        out.printf(Locale.US, "%.3f %.0f %.5f\n", solver.runTime(), -result.value(), solver.gap());
                    }
                }
            }

            out.close();
        } catch (
                Exception e) {
            e.printStackTrace();
        }
    }

}
