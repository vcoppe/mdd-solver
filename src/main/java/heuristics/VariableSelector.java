package heuristics;

import core.Variable;
import mdd.Layer;

/**
 * Enables defining heuristics to select the variable to be assigned next in the MDD and/or in the
 * branch and bound algorithm.
 *
 * @author Vianney Coppé
 */
public interface VariableSelector {

    /**
     * Given the list of free variables, choose the next variable that will be assigned.
     *
     * @param vars  the list of variables to choose from
     * @param layer the current layer of the DD
     * @return the variable on which we will branch next
     */
    Variable select(Variable[] vars, Layer layer);

}
