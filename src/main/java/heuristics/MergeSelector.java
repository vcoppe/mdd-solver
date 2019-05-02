package heuristics;

import mdd.Layer;
import mdd.Node;

/**
 * Enables defining heuristics to select nodes to be merged when building a relax MDD.
 *
 * @author Vianney Coppé
 */
public interface MergeSelector {

    /**
     * Selects the nodes to be merged in order to relax the MDD.
     *
     * @param layer  the layer in which we need to merge nodes
     * @param number the number of nodes to merge
     * @return an array of {@code Node} objects to be merged in the layer
     */
    Node[] select(Layer layer, int number);

}
