package mdd;

import core.Variable;
import org.junit.BeforeClass;
import org.junit.Test;
import problems.Edge;
import problems.MISP;

import java.util.BitSet;

import static org.junit.Assert.*;

public class NodeTest {

    private static int n;
    private static Variable[] vars;
    private static MISP p;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        n = 10;
        vars = new Variable[n];
        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            vars[i] = new Variable(i);
        }
        p = new MISP(n, weights, new Edge[0]);
    }

    @Test
    public void testCopy() {
        State sr = p.new MISPState(n);
        Node s = new Node(sr, vars, 10);

        assertEquals(s.nVariables(), n);

        Node s2 = s.copy();

        assertEquals(Double.compare(s.value(), s2.value()), 0);
        assertEquals(s.state, s2.state);
        assertEquals(s.isExact(), s2.isExact());
        assertEquals(s.hashCode(), s2.hashCode());

        assertTrue(s.equals(s2));
        assertEquals(s.compareTo(s2), 0);
    }

    @Test
    public void testAssign() {
        State sr = p.new MISPState(n);
        Node s = new Node(sr, vars, 0);

        for (int i = 0; i < n; i++) {
            s.setLayerNumber(1);
            s.assign(i, 0);
            assertEquals(Double.compare(s.getVariable(i).value(), 0), 0);
            assertTrue(s.isBound(i));
        }
    }

    @Test
    public void testUpdate() {
        State sr = p.new MISPState(n);
        Node s1 = new Node(sr, vars, 10);
        Node s2 = new Node(sr, vars, 20);

        assertEquals(Double.compare(s1.value(), 10), 0);
        assertEquals(Double.compare(s2.value(), 20), 0);

        s1.update(s2);

        assertEquals(Double.compare(s1.value(), 20), 0);
        assertTrue(s1.isExact());

        Node s3 = new Node(sr, vars, 20, false);
        s3.addParent(s2);

        assertEquals(s3.exactParents().size(), 1);

        s1.update(s3);

        assertFalse(s1.isExact());
        assertEquals(s1.exactParents().size(), 1);
    }

    @Test
    public void testSettersGetters() {
        State sr = p.new MISPState(n);
        Node s = new Node(sr, vars, 10);

        s.setExact(true);
        assertTrue(s.isExact());

        s.setExact(false);
        assertFalse(s.isExact());

        s.setLayerNumber(1);
        assertEquals(s.layerNumber(), 1);
        assertFalse(s.isFinal());

        s.setLayerNumber(n);
        assertEquals(s.layerNumber(), n);
        assertTrue(s.isFinal());

        s.setRelaxedValue(10);
        assertEquals(Double.compare(s.relaxedValue(), 10), 0);

        s.setRelaxedValue(20);
        assertEquals(Double.compare(s.relaxedValue(), 20), 0);
    }

    @Test
    public void testEquals() {
        State sr = p.new MISPState(n);
        Node s = new Node(sr, vars, 10);

        BitSet bs = new BitSet(n);
        bs.flip(0, n);
        State sr2 = p.new MISPState(bs);
        Node s2 = new Node(sr2, vars, 10);

        assertTrue(s.equals(s2));

        bs.clear(1);

        State sr3 = p.new MISPState(bs);
        Node s3 = new Node(sr3, vars, 10);

        assertFalse(s.equals(s3));

    }
}
