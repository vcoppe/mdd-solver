package problems;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class TestMAX2SAT {

    private static Random random = new Random(12);

    private static int n;
    private static LinkedList<Clause> clauses;

    private static void generate() {
        clauses = new LinkedList<>();

        for (int i = 0; i < n; i++) {
            Set<Integer> s = new HashSet<Integer>();
            for (int j = 0; n - i - 1 > 0 && j < 1 + random.nextInt(5); j++) {
                Clause c = new Clause(i, i + 1 + random.nextInt(n - i - 1), random.nextInt(2), random.nextInt(2), random.nextInt(100) - 50);
                if (c.v != i && !s.contains(c.v)) {
                    clauses.add(c);
                    s.add(c.v);
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
        MergeSelector ms = new MinRankMergeSelector();
        DeleteSelector ds = new MinRankDeleteSelector();
        VariableSelector vs = new MAX2SAT.MAX2SATVariableSelector();

        for (n = 5; n <= 20; n += 5) {
            for (int i = 0; i < 5; i++) {
                generate();
                assertTrue(run(ms, ds, vs) == bruteForce());
            }
        }
    }

    @Test
    public void testRandom2() {
        MergeSelector ms = new SimpleMergeSelector();
        DeleteSelector ds = new SimpleDeleteSelector();
        VariableSelector vs = new SimpleVariableSelector();

        for (n = 5; n <= 20; n += 5) {
            for (int i = 0; i < 5; i++) {
                generate();
                assertTrue(run(ms, ds, vs) == bruteForce());
            }
        }
    }

    @Test
    public void testRandom3() {
        for (n = 5; n <= 20; n += 5) {
            for (int i = 0; i < 5; i++) {
                generate();

                Problem p = new MAX2SAT(n, clauses.toArray(new Clause[0]));
                Solver solver = new Solver(p);
                solver.setWidth(15);
                assertTrue(solver.solve().value() == bruteForce());
            }
        }
    }

    @Test
    public void testReadDIMACS() {
        MergeSelector ms = new MinRankMergeSelector();
        DeleteSelector ds = new MinRankDeleteSelector();
        VariableSelector vs = new MAX2SAT.MAX2SATVariableSelector();

        MAX2SAT p = MAX2SAT.readDIMACS("data/max2sat/pass.wcnf");
        Solver solver = new Solver(p, ms, ds, vs);

        assertTrue(solver.solve().value() == p.opt);
    }
}
