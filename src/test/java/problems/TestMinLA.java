package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class TestMinLA {

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
                    edges.add(new Edge(i, j, -1));
                }
            }
        }
    }

    private static double run(MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Edge[] input = new Edge[edges.size()];
        edges.toArray(input);
        Problem p = new MinLA(n, input);
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return -solver.solve().value();
    }

    // from https://codeforces.com/blog/entry/3980?#comment-80388
    private static boolean next_permutation(int[] p) {
        for (int a = p.length - 2; a >= 0; --a) {
            if (p[a] < p[a + 1]) {
                for (int b = p.length - 1; ; --b) {
                    if (p[b] > p[a]) {
                        int t = p[a];
                        p[a] = p[b];
                        p[b] = t;
                        for (++a, b = p.length - 1; a < b; ++a, --b) {
                            t = p[a];
                            p[a] = p[b];
                            p[b] = t;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static double bruteForce() {
        double minValue = Double.MAX_VALUE;
        int[] pos = new int[n];
        int[] assignment = new int[n];

        for (int i = 0; i < n; i++) pos[i] = i;

        do {
            double value = 0;
            for (Edge e : edges) {
                value -= e.w * Math.abs(pos[e.u] - pos[e.v]);
            }

            if (value < minValue) {
                minValue = value;
                for (int i = 0; i < n; i++) {
                    assignment[pos[i]] = i;
                }
            }
        } while (next_permutation(pos));

        System.out.println("Optimal solution : " + minValue);
        System.out.println("Assignment       :");
        for (int i = 0; i < n; i++) {
            System.out.println("\tVar. " + i + " = " + assignment[i]);
        }

        return minValue;
    }

    @Test
    public void testRandom() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        body(ms, ds, vs);
    }

    @Test
    public void testRandom2() {
        MergeSelector ms = new SimpleMergeSelector();
        DeleteSelector ds = new SimpleDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        body(ms, ds, vs);
    }

    public void body(MergeSelector ms, DeleteSelector ds, VariableSelector vs) {
        p = 0.5;

        for (n = 4; n <= 8; n += 2) {
            for (int i = 0; i < 5; i++) {
                generate();
                assertTrue(run(ms, ds, vs) == bruteForce());
            }
        }
    }

    @Test
    public void test() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        n = 5;
        edges = new LinkedList<>();
        edges.add(new Edge(0, 1, -2));
        edges.add(new Edge(0, 2, -3));
        edges.add(new Edge(1, 2, -5));
        edges.add(new Edge(1, 3, -4));
        edges.add(new Edge(2, 4, -1));
        edges.add(new Edge(3, 4, -3));

        run(ms, ds, vs);
    }

    @Test
    public void testReadGRA() {
        MinLA p = MinLA.readGra("data/minla/jpetit/small.gra");
        Solver solver = new Solver(p);

        assertTrue(-solver.solve().value() == p.opt);
    }
}
