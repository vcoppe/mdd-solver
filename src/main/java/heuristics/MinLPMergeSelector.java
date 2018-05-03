package heuristics;

import dp.Layer;
import dp.State;
import utils.StateComparator;

import java.util.Arrays;

/**
 * Merges the nodes with the least path values.
 * @author Vianney Coppé
 */
public class MinLPMergeSelector implements MergeSelector {

    public State[] select(Layer layer, int number) {
		State [] states = new State[layer.width()];
		layer.states().toArray(states);

		Arrays.sort(states, StateComparator.COMPARATOR);
        State[] ret = new State[number];
        int i = 0;
		
		for(State s : states) {
            ret[i++] = s;
            if (i == number) break;
		}
		
		return ret;
	}

}
