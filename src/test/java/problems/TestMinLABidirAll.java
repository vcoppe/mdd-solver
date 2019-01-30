package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMinLABidirAll extends TestHelper {

    public TestMinLABidirAll(String path) {
        super(path);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/minla/nugent");
    }

    @Override
    protected boolean testData(int timeOut) {
        MinLABidir p = MinLABidir.readGra(path);
        run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MinLABidir.MinLABidirVariableSelector());
        return true;
    }

}
