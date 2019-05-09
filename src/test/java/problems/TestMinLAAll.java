package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMinLAAll extends TestHelper {

    public TestMinLAAll(String path) {
        super(path);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        solver.setWidth(MinLA.maxBinomial[p.nVariables()] / 4);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/minla/nugent");
    }

    @Override
    protected boolean testData(int timeOut) {
        MinLA p = MinLA.readGra(path);
        run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        return true;
    }

}
