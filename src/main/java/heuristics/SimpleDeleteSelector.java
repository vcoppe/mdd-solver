package heuristics;

import java.util.HashSet;
import java.util.Set;

import dp.Layer;
import dp.State;

public class SimpleDeleteSelector implements DeleteSelector {

	public Set<State> select(Layer layer, int number) {
		Set<State> ret = new HashSet<State>();
		for(State s : layer.states()) {
			ret.add(s);
			if(ret.size() == number) break;
		}
		return ret;
	}

}
