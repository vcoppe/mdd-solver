package core;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a variable that can take integer values.
 */
public class Variable {

    public int id;
    private Set<Integer> domain;
    private int value;

    /**
     * Returns a variable with the domain [min, max].
     *
     * @param id  the id of the variable in the problem
     * @param min the minimum value of the domain
     * @param max the maximum value of the domain
     */
    public Variable(int id, int min, int max) {
        this.id = id;
        this.value = -1;
        this.domain = new HashSet<>();
        for (int i = min; i <= max; i++) {
            this.domain.add(i);
        }
    }

    /**
     * Returns a variable with the domain [0, n-1].
     *
     * @param id the id of the variable in the problem
     * @param n  the size of the domain
     */
    public Variable(int id, int n) {
        this(id, 0, n - 1);
    }

    /**
     * Returns a variable with the given domain.
     *
     * @param id     the id of the variable in the problem
     * @param domain the possible values of the variable
     */
    public Variable(int id, Set<Integer> domain) {
        this.id = id;
        this.value = -1;
        this.domain = domain;
    }

    /**
     * Returns a variable with the given domain.
     *
     * @param id     the id of the variable in the problem
     * @param domain the possible values of the variable
     */
    public Variable(int id, Set<Integer> domain, int value) {
        this.id = id;
        this.value = value;
        this.domain = domain;
    }

    /**
     * Returns a copy of the variable.
     *
     * @return an different object {@code Variable} with the same domain
     */
    public Variable copy() {
        return new Variable(this.id, this.domain, this.value);
    }

    /**
     * Returns a {@code Set} representation of the domain.
     *
     * @return a {@code Set} with the possible values of the variable
     */
    public Set<Integer> domain() {
        return this.domain;
    }

    /**
     * Returns the size of the domain.
     *
     * @return an integer equal to the number of possible values of the variable
     */
    public int domainSize() {
        return this.domain.size();
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
