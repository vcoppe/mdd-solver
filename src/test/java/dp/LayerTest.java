package dp;

import core.Variable;
import examples.Edge;
import examples.MISP;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LayerTest {

    @Test
    public void testExact() {
        Layer layer = new Layer(null, null, 0);

        assertTrue(layer.isExact());

        MISP p = new MISP(0, new double[0], new Edge[0]);
        State state = new State(p.new MISPState(0), new Variable[0], 0, false);

        layer.addState(state);

        assertFalse(layer.isExact());
    }

}
