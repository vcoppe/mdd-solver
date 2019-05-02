package heuristics;

import mdd.Layer;
import mdd.Node;

/**
 * Enables defining heuristics to select nodes to be deleted when building a restricted MDD.
 *
 * @author Vianney Coppé
 */
public interface DeleteSelector {

    /**
     * Selects the nodes to be deleted in order to restrict the MDD.
     *
     * @param layer  the layer from which we need to remove nodes
     * @param number the number of nodes to be removed
     * @return an array of {@code Node} objects to be deleted from the layer
     */
    Node[] select(Layer layer, int number);

}
