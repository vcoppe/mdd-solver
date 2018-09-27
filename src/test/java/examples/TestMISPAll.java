package examples;

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

    private static long runTime(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);

        long startTime = System.currentTimeMillis();
        solver.solve();
        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    private static void testPerf() {
        Problem p = generate(70);

        int times = 5;
        long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
        System.out.print("[");
        for (int i = 0; i < times; i++) {
            //conf1 += runTime(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
            //conf2 += runTime(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
            //conf3 += runTime(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
            //conf4 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
            conf5 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
            System.out.print("=");
        }
        System.out.println("]");

        System.out.println("===== Average resolution time =====");
        System.out.println("All simple heuristics : " + conf1 / times);
        System.out.println("With MinLP merging : " + conf2 / times);
        System.out.println("With MinLP deleting : " + conf3 / times);
        System.out.println("With MinLP merging & deleting : " + conf4 / times);
        System.out.println("With MinLP merging & deleting + MISP variable selection : " + conf5 / times);
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/misp/all");
    }

    protected boolean testData(int timeOut) {
        MISP p = MISP.readDIMACS(path);
        double found = run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
        return p.opt == found;
    }

}
