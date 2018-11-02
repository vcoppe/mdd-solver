package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestMAX2SAT {

    private static Random random = new Random(12);

    private static int n;
    private static LinkedList<Clause> clauses;

    private static void generate() {
        clauses = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < 1 + random.nextInt(5); j++) {
                Clause c = new Clause(i, random.nextInt(n), random.nextInt(2), random.nextInt(2), random.nextInt(100) - 50);
                if (c.v != i) {
                    clauses.add(c);
                }
            }
        }
    }

    private static double run(MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        Clause[] input = new Clause[clauses.size()];
        clauses.toArray(input);
        Problem p = new MAX2SAT(n, input);
        Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
        return solver.solve().value();
    }

    private static double bruteForce() {
        long vars = 0;
        double maxValue = -Double.MAX_VALUE;
        long assignment = 0;

        while (vars < Math.pow(2, n)) {
            double value = 0;

            for (Clause c : clauses) {
                if (c.value((int) ((vars >> c.u) & 1), (int) ((vars >> c.v) & 1))) {
                    value += c.w;
                }
            }

            if (value > maxValue) {
                maxValue = value;
                assignment = vars;
            }

            vars++;
        }

        System.out.println("Optimal solution : " + maxValue);
        System.out.println("Assignment       :");
        for (int i = 0; i < n; i++) {
            System.out.println("\tVar. " + i + " = " + ((assignment >> i) & 1));
        }

        return maxValue;
    }

    @Test
    public void testRandom() {
        MergeSelector ms = new MinLPMergeSelector();
        DeleteSelector ds = new MinLPDeleteSelector();
        VariableSelector vs = new MAX2SAT.MAX2SATVariableSelector();

        for (n = 5; n <= 20; n += 5) {
            for (int i = 0; i < 10; i++) {
                generate();
                assertEquals(Double.compare(run(ms, ds, vs), bruteForce()), 0);
            }
        }
    }
}
