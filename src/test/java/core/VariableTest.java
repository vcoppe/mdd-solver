package core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VariableTest {

    @Test
    public void testConstructor1() {
        Variable var = new Variable(0);

        assertEquals(var.value(), -1);
    }

    @Test
    public void testConstructor2() {
        Variable var = new Variable(0, 3);

        assertEquals(var.value(), 3);
    }

    @Test
    public void testAssign() {
        Variable var = new Variable(0);

        var.assign(2);
        assertEquals(var.value(), 2);

        var.assign(3);
        assertEquals(var.value(), 3);
    }
}
