package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestMCP {

    private static Random random = new Random(12);

    private static int n;
    private static double p;
    private static LinkedList<Edge> edges;

    private static void generate() {
        int m = (int) Math.floor(p * n * (n - 1) / 2);
        LinkedList<Boolean> in = new LinkedList<>();
        edges = new LinkedList<>();

        for (int i = 0; i < n * (n - 1) / 2; i++) {
            in.add(i < m);
        }

        Collections.shuffle(in, random);

        int k = 0, l = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (in.get(l++)) {
                    edges.add(new Edge(i, j, random.nextInt(100) - 50));
                }
            }
        }
    }

    private static double run(MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Edge[] input = new Edge[edges.size()];
        edges.toArray(input);
        Problem p = new MCP(n, input);
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve().value();
    }

    private static double bruteForce() {
        long vars = 0;
        double maxValue = -Double.MAX_VALUE;
        long assignment = 0;

        while (vars < Math.pow(2, n)) {
            double value = 0;

            for (Edge e : edges) {
                if (((vars >> e.u) & 1) != ((vars >> e.v) & 1)) {
                    value += e.w;
                }
            }

            if (value > maxValue) {
                maxValue = value;
                assignment = vars;
            }

            vars++;
        }

        System.out.println("Optimal solution : " + maxValue);
        System.out.println("Assignment       :");
        for (int i = 0; i < n; i++) {
            System.out.println("\tVar. " + i + " = " + ((assignment >> i) & 1));
        }

        return maxValue;
    }

    @Test
    public void testRandom() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        p = 0.5;

        for (n = 5; n <= 20; n += 5) {
            for (int i = 0; i < 10; i++) {
                generate();
                assertEquals(Double.compare(run(ms, ds, vs), bruteForce()), 0);
            }
        }
    }
}
