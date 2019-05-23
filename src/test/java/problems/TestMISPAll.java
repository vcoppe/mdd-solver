package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.LinkedList;
import java.util.Random;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMISPAll extends TestHelper {

    private static Random random = new Random(12);

    public TestMISPAll(String path) {
        super(path);
    }

    private static Problem generate(int n) {
        double[] weights = new double[n];
        LinkedList<Edge> edges = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            weights[i] = random.nextDouble() * 100 - 50;

            for (int j = 0; j < 1 + random.nextInt(5); j++) {
                Edge e = new Edge(i, random.nextInt(n));
                if (e.v != i) {
                    edges.add(e);
                }
            }
        }

        Edge[] input = new Edge[edges.size()];
        edges.toArray(input);

        return new MISP(n, weights, input);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/misp/easy");
    }

    protected boolean testData(int timeOut) {
        MISP p = MISP.readDIMACS(path);
        double found = run(p, timeOut, new MinRankMergeSelector(), new MinRankDeleteSelector(), new MISP.MISPVariableSelector());
        return p.opt == found;
    }

}
