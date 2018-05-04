package core;

import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class VariableTest {

    @Test
    public void testConstructor1() {
        int n = 10;
        Variable var = new Variable(0, n);

        assertEquals(var.id, 0);
        assertEquals(var.domainSize(), n);

        assertEquals(var.value(), -1);
    }

    @Test
    public void testConstructor2() {
        int min = 10, max = 20;
        Variable var = new Variable(0, min, max);

        assertEquals(var.domainSize(), max - min + 1);
    }

    @Test
    public void testConstructor3() {
        HashSet<Integer> domain = new HashSet<>();
        domain.add(0);
        domain.add(1);

        Variable var = new Variable(0, domain);

        assertEquals(var.id, 0);
        assertEquals(var.value(), -1);
        assertEquals(var.domain(), domain); // pass the reference
    }

    @Test
    public void testAssign() {
        int n = 10;
        Variable var = new Variable(0, n);

        var.assign(2);
        assertEquals(var.value(), 2);

        var.assign(3);
        assertEquals(var.value(), 3);
    }
}
