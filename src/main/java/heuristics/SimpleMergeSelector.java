package heuristics;

import dp.Layer;
import dp.State;

public class SimpleMergeSelector implements MergeSelector {

    public State[] select(Layer layer, int number) {
        State[] ret = new State[number];
        int i = 0;

		for(State s : layer.states()) {
            ret[i++] = s;
            if (i == number) break;
        }

		return ret;
	}

}
