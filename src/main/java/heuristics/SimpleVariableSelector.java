package heuristics;

import core.Variable;
import dp.Layer;

public class SimpleVariableSelector implements VariableSelector {

	public Variable select(Variable [] vars, Layer layer) {
		for(Variable var : vars) {
            if (!var.isBound()) {
				return var;
			}
		}
		return null;
	}

}
