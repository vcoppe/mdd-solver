package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Scanner;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestKnapsackAll extends TestHelper {

    public TestKnapsackAll(String path) {
        super(path);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/knapsack");
    }

    @Override
    protected boolean testData(int timeOut) {
        int n = 0, c = 0, w[] = null;
        double[] v = null;

        try {
            Scanner scan = new Scanner(new File(path));

            n = scan.nextInt();
            c = scan.nextInt();

            w = new int[n];
            v = new double[n];

            for (int i = 0; i < n; i++) {
                w[i] = scan.nextInt();
                v[i] = scan.nextInt();
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Problem p = new Knapsack(n, c, w, v);
        run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        return true;
    }
}
