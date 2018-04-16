package heuristics;

import java.util.HashSet;
import java.util.Set;

import dp.Layer;
import dp.State;

public class SimpleMergeSelector implements MergeSelector {

	public Set<State> select(Layer layer) {
		Set<State> ret = new HashSet<State>();
		for(State s : layer.states()) {
			ret.add(s);
			if(ret.size() > 1) break;
		}
		return ret;
	}

}
