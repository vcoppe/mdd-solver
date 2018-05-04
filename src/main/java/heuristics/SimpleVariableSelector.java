package heuristics;

import core.Variable;
import dp.Layer;

public class SimpleVariableSelector implements VariableSelector {

    public Variable select(Variable[] vars, Layer layer) {
        if (vars.length == 0) {
            return null;
        } else {
            return vars[0];
        }
    }

}
