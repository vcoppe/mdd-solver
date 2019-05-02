package mdd;

import core.Problem;
import core.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a layer of the MDD.
 *
 * @author Vianney Coppé
 */
public class Layer {

    private Map<State, Node> nodes;
    private Problem problem;
    private MDD mdd;
    private boolean exact;
    public int number;

    /**
     * Returns an empty layer of the problem.
     *
     * @param problem the implementation of a problem
     * @param mdd     the  associated decision diagram
     * @param number  the number of the layer
     */
    public Layer(Problem problem, MDD mdd, int number) {
        this.nodes = new HashMap<>();
        this.problem = problem;
        this.mdd = mdd;
        this.exact = true;
        this.number = number;
    }

    /**
     * Returns a layer containing the given node.
     *
     * @param problem the implementation of a problem
     * @param mdd     the  associated decision diagram
     * @param node    the node to be contained
     * @param number  the number of the layer
     */
    public Layer(Problem problem, MDD mdd, Node node, int number) {
        this(problem, mdd, number);
        this.nodes.put(node.state, node);
        this.exact = node.isExact();
    }

    /**
     * Clears the content of the layer in order to reuse the object.
     *
     * @param number the number of the layer
     */
    public void reset(int number) {
        this.nodes.clear();
        this.exact = true;
        this.number = number;
    }

    /**
     * Returns the next layer of the MDD using the {@code variableSelector} to choose the next variable
     * to assign and the {@code problem} implementation to provide the successors of all the nodes of
     * the layer.
     *
     * @return the next layer of the MDD
     */
    public Layer nextLayer(int width, boolean relaxed) {
        Variable nextVar = null;
        Layer next = new Layer(this.problem, this.mdd, this.number + 1);

        next.setExact(this.exact);
        for (Node node : this.nodes.values()) {
            if (node.isExact()) {
                node.exactParents().clear(); // we do not need them anymore -> garbage collection
            }

            if (nextVar == null) {
                nextVar = this.mdd.variableSelector.select(node.freeVariables(), this);
            }

            for (Node s : this.problem.successors(node, nextVar)) {
                if (node.isExact()) s.addParent(node);
                else s.setExact(false);
                next.addNode(s);
            }
        }

        while (next.width() > width) {
            if (relaxed) {
                Node[] toMerge = this.mdd.mergeSelector.select(next, next.width() - width + 1);
                next.removeNodes(toMerge, this.mdd.frontier);

                Node mergedNode = this.problem.merge(toMerge);
                mergedNode.setExact(false);

                next.addNode(mergedNode);
            } else {
                Node[] toRemove = this.mdd.deleteSelector.select(next, next.width() - width);
                next.removeNodes(toRemove);
            }
        }

        return next;
    }

    /**
     * Adds nodes to the layer or updates an existing node in the layer with the same {@code State}.
     *
     * @param node the node to be added
     */
    public void addNode(Node node) {
        this.exact &= node.isExact();
        node.setLayerNumber(this.number);
        Node existing = this.nodes.get(node.state);
        if (existing == null) this.nodes.put(node.state, node);
        else existing.update(node);
    }

    /**
     * Remove the nodes from the layer.
     *
     * @param nodes the nodes to be removed
     */
    public void removeNodes(Node[] nodes) {
        for (Node node : nodes) {
            this.nodes.remove(node.state);
        }
        this.exact = false;
    }

    /**
     * Remove the nodes from the layer.
     *
     * @param nodes   the nodes to be removed
     * @param frontier the frontier cutset in order to add exact parents
     */
    public void removeNodes(Node[] nodes, Set<Node> frontier) {
        for (Node node : nodes) {
            this.nodes.remove(node.state);
            frontier.addAll(node.exactParents());
        }
        this.exact = false;
    }

    /**
     * Returns a {@code Set} of all nodes contained in the layer.
     *
     * @return a {@code Set} with all the nodes
     */
    public Collection<Node> nodes() {
        return this.nodes.values();
    }

    /**
     * Returns the width of the layer.
     *
     * @return the number of nodes in the layer
     */
    public int width() {
        return this.nodes.size();
    }

    /**
     * Returns the best node of the layer.
     *
     * @return a {@code Node} object representing the best node of the layer
     */
    public Node best() {
        Node best = null;
        for (Node node : this.nodes.values()) {
            if (best == null || node.value() > best.value()) {
                best = node;
            }
        }
        return best;
    }

    /**
     * Returns a {@code boolean} telling if the layer is equal to the corresponding complete MDD layer.
     *
     * @return {@code true} <==> the layer is equal to the corresponding complete MDD layer
     */
    public boolean isExact() {
        return this.exact;
    }

    /**
     * Help function to set the exact property of the layer.
     *
     * @param exact a {@code boolean} telling if the layer is exact or not
     */
    private void setExact(boolean exact) {
        this.exact = exact;
    }

    /**
     * Returns a {@code boolean} telling if the layer is the last one i. e. all the variables are assigned.
     *
     * @return {@code true} <==> the layer is the final one
     */
    public boolean isFinal() {
        return this.number == this.problem.nVariables();
    }
}
