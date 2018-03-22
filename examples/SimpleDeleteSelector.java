package examples;

import java.util.HashSet;
import java.util.Set;

import dp.Layer;
import dp.State;
import heuristics.DeleteSelector;

public class SimpleDeleteSelector implements DeleteSelector {

	public Set<State> select(Layer layer) {
		Set<State> ret = new HashSet<State>();
		for(State s : layer.states()) {
			ret.add(s);
			return ret;
		}
		return ret;
	}

}
