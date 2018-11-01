package core;

/**
 * Represents a variable that can take integer values.
 */
public class Variable {

    public int id;
    private int value;

    /**
     * Returns a free variable.
     *
     * @param id  the id of the variable in the problem
     */
    public Variable(int id) {
        this.id = id;
        this.value = -1;
    }

    /**
     * Returns a variable with the given value.
     *
     * @param id     the id of the variable in the problem
     * @param value the value of the variable
     */
    public Variable(int id, int value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Returns a copy of the variable.
     *
     * @return an different object {@code Variable} with the same domain
     */
    public Variable copy() {
        return new Variable(this.id, this.value);
    }

    /**
     * Returns the value of the variable if it is assigned.
     *
     * @return the value of the variable of {@code -1} if it is not assigned
     */
    public int value() {
        return this.value;
    }

    /**
     * Assigns the variable to a value.
     *
     * @param value the value to be assigned which should be in the domain
     */
    public void assign(int value) {
        this.value = value;
    }
}
