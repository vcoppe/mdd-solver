package core;

import org.junit.Test;
import utils.InconsistencyException;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void testConstructor1() {
        int n = 10;
        Variable var = new Variable(0, n);

        assertEquals(var.id(), 0);
        assertEquals(var.domainSize(), n);

        assertEquals(var.value(), -1);

        for (int i = 0; i < 10; i++) {
            assertTrue(var.contains(i));
        }

        assertFalse(var.isAssigned());
    }

    @Test
    public void testConstructor2() {
        int min = 10, max = 20;
        Variable var = new Variable(0, min, max);

        assertEquals(var.domainSize(), max - min + 1);

        for (int i = min; i <= max; i++) {
            assertTrue(var.contains(i));
        }

        assertFalse(var.isAssigned());
    }

    @Test
    public void testAssign() {
        int n = 10;
        Variable var = new Variable(0, n);

        try {
            var.remove(n);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }

        assertFalse(var.isAssigned());

        try {
            var.remove(n - 1);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertEquals(var.domainSize(), n - 1);
        assertFalse(var.contains(n - 1));

        try {
            var.assign(2);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertEquals(var.domainSize(), 1);
        assertTrue(var.contains(2));
        assertEquals(var.value(), 2);
        assertTrue(var.isAssigned());

        try {
            var.assign(3);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }

        try {
            var.remove(2);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }
    }
}
