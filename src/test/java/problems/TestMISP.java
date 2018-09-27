package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMISP extends TestHelper {

    public TestMISP(String path) {
        super(path);
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

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/misp/pass");
    }

    protected boolean testData(int timeOut) {
        MISP p = MISP.readDIMACS(path);
        double found = run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
        return p.opt == found;
    }

}
