package problems;

import core.Problem;
import core.Variable;
import mdd.Node;
import mdd.State;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the Unbounded Knapsack Problem.
 *
 * @author Vianney Coppé
 */
public class Knapsack implements Problem {

    private int n, w[];
    private double[] v;

    private Node root;

    Knapsack(int n, int c, int[] w, double[] v) {
        this.n = n;
        this.w = w;
        this.v = v;

        root = new Node(new KnapsackState(c), Variable.newArray(n), 0);
    }

    public Node root() {
        return root;
    }

    public int nVariables() {
        return n;
    }

    public List<Node> successors(Node node, Variable var) {
        int i = var.id;
        KnapsackState knapsackState = (KnapsackState) node.state;
        List<Node> successors = new LinkedList<>();

        for (int x = 0; x <= knapsackState.capacity / w[i]; x++) {
            KnapsackState succKnapsackState = new KnapsackState(knapsackState.capacity - x * w[i]);
            double value = node.value() + x * v[i];
            successors.add(node.getSuccessor(succKnapsackState, value, i, x));
        }

        return successors;
    }

    public Node merge(Node[] nodes) {
        Node<KnapsackState> best = nodes[0];
        int maxCapacity = 0;

        for (Node<KnapsackState> node : nodes) {
            maxCapacity = Math.max(maxCapacity, node.state.capacity);

            if (node.value() > best.value()) best = node;
        }

        best.state.capacity = maxCapacity;
        return best;
    }

    private class KnapsackState implements State {

        int capacity;

        KnapsackState(int capacity) {
            this.capacity = capacity;
        }

        public double rank(Node node) {
            return node.value();
        }

        public int hashCode() {
            return capacity;
        }

        public boolean equals(Object o) {
            return o instanceof Knapsack.KnapsackState && capacity == ((KnapsackState) o).capacity;
        }

        public KnapsackState copy() {
            return new KnapsackState(capacity);
        }
    }
}
