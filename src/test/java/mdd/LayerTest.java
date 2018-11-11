package mdd;

import core.Variable;
import heuristics.SimpleVariableSelector;
import heuristics.VariableSelector;
import org.junit.BeforeClass;
import org.junit.Test;
import problems.Edge;
import problems.MISP;

import static org.junit.Assert.*;

public class LayerTest {

    private static int n;
    private static Variable[] vars;
    private static MISP p;
    private static VariableSelector vs;
    private static MDD mdd;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        n = 10;
        vars = new Variable[n];
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            vars[i] = new Variable(i);
            weights[i] = 1;
        }
        p = new MISP(n, weights, new Edge[0]);
        vs = new SimpleVariableSelector();
        mdd = new MDD(p, null, null, vs);
    }

    @Test
    public void testConstructor() {
        Layer layer = new Layer(p, mdd, 0);

        assertTrue(layer.isExact());
        assertFalse(layer.isFinal());
        assertEquals(layer.best(), null);
        assertEquals(layer.width(), 0);
    }

    @Test
    public void testExact() {
        Layer layer = new Layer(p, mdd, 0);

        assertTrue(layer.isExact());

        State state = new State(p.new MISPState(n), vars, 0, false);

        layer.addState(state);

        assertFalse(layer.isExact());
    }

    @Test
    public void testRemove() {
        Layer layer = new Layer(p, mdd, 0);
        State state = new State(p.new MISPState(0), new Variable[0], 0);
        layer.addState(state);

        assertEquals(layer.width(), 1);

        State[] states = {state};
        layer.removeStates(states);

        assertEquals(layer.width(), 0);
        assertFalse(layer.isExact());
    }

    @Test
    public void testNextLayer() {
        Layer layer = new Layer(p, mdd, 0);
        Layer layer2 = layer.nextLayer(Integer.MAX_VALUE, false);

        assertTrue(layer.isExact());
        assertTrue(layer2.isExact());

        layer = new Layer(p, mdd, 0);
        State state = new State(p.new MISPState(n), vars, 0, false);
        layer.addState(state);

        layer2 = layer.nextLayer(Integer.MAX_VALUE, false);

        assertFalse(layer2.isExact());
        assertEquals(layer2.width(), 1); // no edges -> leads to same BitSet

        try {
            assertEquals(Double.compare(layer2.best().value(), 1), 0);
        } catch (NullPointerException e) {
            fail("Should have one state");
        }
    }
}
