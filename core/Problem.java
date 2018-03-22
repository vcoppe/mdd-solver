package core;

import dp.State;

import java.util.List;

public interface Problem {
	
	State root();
	
	int nVariables();
	
	List<State> successors(State s, Variable var);
	
	State merge(State s1, State s2);
	
}
