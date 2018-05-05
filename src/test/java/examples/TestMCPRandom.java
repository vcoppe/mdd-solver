package examples;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class TestMCPRandom {

    static Random random = new Random(12);

    private static Problem generate(int n) {
        LinkedList<Edge> edges = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 1 + random.nextInt(5); j++) {
                Edge e = new Edge(i, random.nextInt(n), random.nextInt(100) - 50);
                if (e.v != i) {
                    edges.add(e);
                }
            }
        }

        Edge[] input = new Edge[edges.size()];
        edges.toArray(input);

        return new MCP(n, input);
    }

    private static double run(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve().value();
    }

    @Test
    public void testRandom() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        assertEquals(Double.compare(run(generate(5), ms, ds, vs), 97), 0);
        assertEquals(Double.compare(run(generate(10), ms, ds, vs), 164), 0);
        assertEquals(Double.compare(run(generate(20), ms, ds, vs), 328), 0);
        assertEquals(Double.compare(run(generate(30), ms, ds, vs), 457), 0);
    }
}
