package dp;

import core.Variable;
import examples.Edge;
import examples.MISP;
import org.junit.BeforeClass;
import org.junit.Test;
import utils.InconsistencyException;

import static org.junit.Assert.*;

public class StateTest {

    private static int n;
    private static Variable[] vars;
    private static MISP p;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        n = 10;
        vars = new Variable[n];
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            vars[i] = new Variable(i, 2);
        }
        p = new MISP(n, weights, new Edge[0]);
    }

    @Test
    public void testCopy() {
        fail("Not yet implemented");
    }

    @Test
    public void testAssign() {
        System.out.println(n);
        StateRepresentation sr = p.new MISPState(n);
        State s = new State(sr, vars, 0);

        for (int i = 0; i < n; i++) {
            try {
                s.assign(i, 0);

                assertTrue(s.variables()[i].isAssigned());
                assertEquals(s.variables()[i].value(), 0);
            } catch (InconsistencyException e) {
                fail("Should not happen");
            }
        }
    }

    @Test
    public void testUpdate() {
        fail("Not yet implemented");
    }

}
