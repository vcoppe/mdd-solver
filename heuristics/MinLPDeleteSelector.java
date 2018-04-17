package heuristics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import dp.Layer;
import dp.State;

public class MinLPDeleteSelector implements DeleteSelector {

	@Override
	public Set<State> select(Layer layer, int number) {
		State [] states = new State[layer.width()];
		layer.states().toArray(states);
		
		Arrays.sort(states);
		Set<State> ret = new HashSet<State>();
		
		for(State s : states) {
			ret.add(s);
			if(ret.size() == number) break;
		}
		
		return ret;
	}

}
