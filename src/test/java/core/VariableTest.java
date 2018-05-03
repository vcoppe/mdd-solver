package core;

import org.junit.Test;
import utils.InconsistencyException;

import java.util.HashSet;

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

        try {
            var = new Variable(0, 2, 1);
            fail("Should throw an exception");
        } catch (InconsistencyException e) {

        }
    }

    @Test
    public void testConstructor3() {
        HashSet<Integer> domain = new HashSet<>();
        Variable var = null;
        try {
            var = new Variable(0, domain);
            fail("Should throw an exception");
        } catch (InconsistencyException e) {

        }

        domain.add(0);
        domain.add(1);
        try {
            var = new Variable(0, domain);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertFalse(var.isBound());
        assertEquals(var.id(), 0);
        assertEquals(var.value(), -1);
        assertEquals(var.domain(), domain); // pass the reference
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

        try {
            var.assign(n);
            fail("Should throw an exception");
        } catch (InconsistencyException e) {

        }

        assertFalse(var.isBound());

        try {
            var.assign(2);
        } catch (InconsistencyException e) {
            fail("Should not happen");
        }

        assertTrue(var.contains(2));
        assertEquals(var.value(), 2);
        assertTrue(var.isBound());

        try {
            var.assign(3);
            fail("Should throw an error");
        } catch (InconsistencyException e) {

        }
    }
}
