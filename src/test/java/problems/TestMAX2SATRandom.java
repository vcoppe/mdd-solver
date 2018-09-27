package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class TestMAX2SATRandom {

    private static Random random = new Random(12);

    private static Problem generate(int n) {
        LinkedList<Clause> clauses = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 1 + random.nextInt(5); j++) {
                Clause c = new Clause(i, random.nextInt(n), random.nextInt(2), random.nextInt(2), random.nextInt(100) - 50);
                if (c.v != i) {
                    clauses.add(c);
                }
            }
        }

        Clause[] input = new Clause[clauses.size()];
        clauses.toArray(input);

        return new MAX2SAT(n, input);
    }

    private static double run(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve().value();
    }

    @Test
    public void testRandom() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new MAX2SAT.MAX2SATVariableSelector();

        assertEquals(Double.compare(run(generate(5), ms, ds, vs), 56), 0);
        assertEquals(Double.compare(run(generate(10), ms, ds, vs), 107), 0);
        assertEquals(Double.compare(run(generate(20), ms, ds, vs), 282), 0);
        assertEquals(Double.compare(run(generate(30), ms, ds, vs), 365), 0);
    }
}
