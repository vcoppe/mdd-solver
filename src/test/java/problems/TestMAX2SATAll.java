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
public class TestMAX2SATAll extends TestHelper {

    public TestMAX2SATAll(String path) {
        super(path);
    }

    private static Random random = new Random(12);

    private static Problem generate(int n) {
        LinkedList<Clause> clauses = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 1 + random.nextInt(5); j++) {
                Clause c = new Clause(i, random.nextInt(n), random.nextInt(2), random.nextInt(2), random.nextDouble() * 100 - 50);
                if (c.v != i) {
                    clauses.add(c);
                }
            }
        }

        Clause[] input = new Clause[clauses.size()];
        clauses.toArray(input);

        return new MAX2SAT(n, input);
    }

    private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve(timeOut).value();
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/max2sat");
    }

    protected boolean testData(int timeOut) {
        MAX2SAT p = MAX2SAT.readDIMACS(path);
        double found = run(p, timeOut, new MinRankMergeSelector(), new MinRankDeleteSelector(), new SimpleVariableSelector());
        return p.opt == found;
    }

}
