package heuristics;

import mdd.Layer;
import mdd.State;

/**
 * Enables defining heuristics to select nodes to be deleted when building a restricted MDD.
 *
 * @author Vianney Coppé
 */
public interface DeleteSelector {

    /**
     * Selects the states to be deleted in order to restrict the MDD.
     *
     * @param layer  the layer from which we need to remove states
     * @param number the number of states to be removed
     * @return an array of {@code State} objects to be deleted from the layer
     */
    State[] select(Layer layer, int number);

}
