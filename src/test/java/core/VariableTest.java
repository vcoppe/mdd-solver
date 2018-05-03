package core;

import org.junit.Test;
import utils.InconsistencyException;

import static org.junit.Assert.*;

public class VariableTest {

    @Test
    public void testConstructor1() {
        int n = 10;
        Variable var = null;
        try {
            var = new Variable(0, n);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertEquals(var.id(), 0);
        assertEquals(var.domainSize(), n);

        assertEquals(var.value(), -1);

        for (int i = 0; i < 10; i++) {
            assertTrue(var.contains(i));
        }

        assertFalse(var.isBound());
    }

    @Test
    public void testConstructor2() {
        int min = 10, max = 20;
        Variable var = null;
        try {
            var = new Variable(0, min, max);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertEquals(var.domainSize(), max - min + 1);

        for (int i = min; i <= max; i++) {
            assertTrue(var.contains(i));
        }

        assertFalse(var.isBound());
    }

    @Test
    public void testAssign() {
        int n = 10;
        Variable var = null;
        try {
            var = new Variable(0, n);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        /*try {
            var.remove(n);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }*/

        assertFalse(var.isBound());

        /*try {
            var.remove(n - 1);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertEquals(var.domainSize(), n - 1);
        assertFalse(var.contains(n - 1));*/

        try {
            var.assign(2);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        //assertEquals(var.domainSize(), 1);
        assertTrue(var.contains(2));
        assertEquals(var.value(), 2);
        assertTrue(var.isBound());

        try {
            var.assign(3);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }

        /*try {
            var.remove(2);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }*/
    }
}
