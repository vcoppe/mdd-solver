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
        StateRepresentation sr = p.new MISPState(n);
        State s = new State(sr, vars, 10);
        State s2 = s.copy();

        assertEquals(Double.compare(s.value(), s2.value()), 0);
        assertEquals(s.stateRepresentation(), s2.stateRepresentation());
        assertEquals(s.isExact(), s2.isExact());
        assertEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    public void testAssign() {
        StateRepresentation sr = p.new MISPState(n);
        State s = new State(sr, vars, 0);

        for (int i = 0; i < n; i++) {
            try {
                s.assign(i, 0);

                assertTrue(s.variables()[i].isBound());
                assertEquals(s.variables()[i].value(), 0);
            } catch (InconsistencyException e) {
                fail("Should not happen");
            }
        }
    }

    @Test
    public void testUpdate() {
        StateRepresentation sr = p.new MISPState(n);
        State s1 = new State(sr, vars, 10);
        State s2 = new State(sr, vars, 20);

        assertEquals(Double.compare(s1.value(), 10), 0);
        assertEquals(Double.compare(s2.value(), 20), 0);

        s1.update(s2);

        assertEquals(Double.compare(s1.value(), 20), 0);
        assertTrue(s1.isExact());

        State s3 = new State(sr, vars, 20, false);
        s3.addParent(s2);

        assertEquals(s3.exactParents().size(), 1);

        s1.update(s3);

        assertFalse(s1.isExact());
        assertEquals(s1.exactParents().size(), 1);
    }

}
