package heuristics;

import java.util.Set;

import dp.Layer;
import dp.State;

/**
 * Enables defining heuristics to select nodes to be deleted when building a restricted MDD.
 *  
 * @author Vianney Coppé
 */
public interface DeleteSelector {
	
	/**
	 * Selects the states to be deleted in order to restrict the MDD.
	 * @param layer the layer from which we need to remove states
	 * @return a {@code Set} of {@code State} objects to be deleted from the layer
	 */
	Set<State> select(Layer layer);
	
}
