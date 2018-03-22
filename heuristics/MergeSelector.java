package heuristics;

import dp.Layer;
import dp.State;

import java.util.Set;

public interface MergeSelector {
	
	/*
	 * Selects the states to be merged in order to relax the MDD
	 */
	Set<State> select(Layer layer);
	
}
