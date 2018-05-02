package heuristics;

import dp.Layer;
import dp.State;
import utils.StateComparator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Merges the |L|-W+1 nodes with the least path values.
 * @author Vianney Coppé
 */
public class MinLPMergeSelector implements MergeSelector {

	public Set<State> select(Layer layer, int number) {
		State [] states = new State[layer.width()];
		layer.states().toArray(states);

		Arrays.sort(states, StateComparator.COMPARATOR);
		Set<State> ret = new HashSet<State>();
		
		for(State s : states) {
			ret.add(s);
			if(ret.size() == number) {
				return ret;
			}
		}
		
		return ret;
	}

}
