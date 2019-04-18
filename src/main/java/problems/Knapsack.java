package problems;

import core.Problem;
import core.Variable;
import mdd.State;
import mdd.StateRepresentation;

import java.util.LinkedList;
import java.util.List;

public class Knapsack implements Problem {

    private int n, c;
    private int[] w;
    private double[] v;

    private State root;

    public Knapsack(int n, int c, int[] w, double[] v) {
        this.n = n;
        this.c = c;
        this.w = w;
        this.v = v;

        root = new State(new KnapsackState(c), Variable.newArray(n), 0);
    }

    public State root() {
        return root;
    }

    public int nVariables() {
        return n;
    }

    public List<State> successors(State state, Variable var) {
        int i = var.id;
        KnapsackState knapsackState = (KnapsackState) state.stateRepresentation;
        List<State> successors = new LinkedList<>();

        for (int x = 0; x <= c / w[i]; x++) {
            KnapsackState succKnapsackState = new KnapsackState(knapsackState.capacity - x * w[i]);
            double value = state.value() + x * v[i];
            successors.add(state.getSuccessor(succKnapsackState, value, i, x));
        }

        return successors;
    }

    public State merge(State[] states) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        KnapsackState knapsackState = new KnapsackState(0);

        for (State state : states) {
            knapsackState.capacity = Math.max(
                    knapsackState.capacity,
                    ((KnapsackState) state.stateRepresentation).capacity
            );

            if (state.value() > maxValue) {
                maxValue = state.value();
                variables = state.variables;
                indexes = state.indexes;
            }
        }

        return new State(knapsackState, variables, indexes, maxValue, false);
    }

    private class KnapsackState implements StateRepresentation {

        int capacity;

        KnapsackState(int capacity) {
            this.capacity = capacity;
        }

        public double rank(State state) {
            return state.value();
        }

        public KnapsackState copy() {
            return new KnapsackState(capacity);
        }
    }
}
