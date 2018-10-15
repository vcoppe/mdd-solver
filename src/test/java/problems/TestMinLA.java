package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMinLA extends TestHelper {

    public TestMinLA(String path) {
        super(path);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/minla/pass");
    }

    @Override
    protected boolean testData(int timeOut) {
        MinLA p = MinLA.readGra(path);
        double found = run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        return -found == p.opt;
    }

}
