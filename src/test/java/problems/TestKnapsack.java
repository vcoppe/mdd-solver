package problems;

import core.Problem;
import core.Solver;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

public class TestKnapsack {

    private static Random random = new Random(12);

    private static int n, c;
    private static int[] w;
    private static double[] v;

    private static void generate() {
        w = new int[n];
        v = new double[n];

        for (int i = 0; i < n; i++) {
            w[i] = 1 + random.nextInt(c);
            v[i] = 1 + random.nextInt(2 * w[i]);
        }
    }

    private static double run() {
        Problem p = new Knapsack(n, c, w, v);
        Solver solver = new Solver(p);
        return solver.solve().value();
    }

    private static double bruteForce(int i, int capacity) {
        if (i == n || capacity == 0) return 0;

        double maxVal = 0;
        for (int x = 0; x <= capacity / w[i]; x++) {
            maxVal = Math.max(maxVal, x * v[i] + bruteForce(i + 1, capacity - x * w[i]));
        }

        return maxVal;
    }

    private static double bruteForce() {
        return bruteForce(0, c);
    }

    @Test
    public void test() {
        c = 111;
        for (n = 4; n <= 10; n += 2) {
            for (int i = 0; i < 5; i++) {
                generate();
                assertTrue(run() == bruteForce());
            }
        }
    }

    @Test
    public void testCustom() {
        n = 3;
        c = 7;
        w = new int[]{4, 2, 3};
        v = new double[]{7, 2, 5};

        Problem p = new Knapsack(n, c, w, v);
        Solver solver = new Solver(p);
        solver.setWidth(2);
        solver.solve();
    }
}
