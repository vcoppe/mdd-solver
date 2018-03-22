package heuristics;

import java.util.Set;

import dp.Layer;
import dp.State;

public interface DeleteSelector {
	
	/*
	 * Selects the states to be deleted in order to restrict the MDD
	 */
	Set<State> select(Layer layer);
	
}
