package core;

/**
 * Represents a variable that can take integer values.
 */
public class Variable {

    public int id;
    private double value;

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
    public Variable(int id, double value) {
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
     * Returns an array of {@code n} variables with {@code id}s from {@code 0} to {@code n-1}.
     *
     * @param n the number of variables needed
     * @return an array of {@code Variable} objects
     */
    public static Variable[] newArray(int n) {
        Variable[] variables = new Variable[n];
        for (int i = 0; i < n; i++) {
            variables[i] = new Variable(i);
        }
        return variables;
    }

    /**
     * Assigns the variable to a value.
     *
     * @param value the value to be assigned which should be in the domain
     */
    public void assign(int value) {
        this.value = value;
    }

    /**
     * Returns the value of the variable if it is assigned.
     *
     * @return the value of the variable of {@code -1} if it is not assigned
     */
    public double value() {
        return this.value;
    }
}
