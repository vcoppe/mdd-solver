package core;

import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;
import mdd.MDD;
import mdd.State;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Implementation of the branch and bound algorithm for MDDs.
 *
 * @author Vianney Coppé
 */
public class Solver {

    private boolean print = true;
    private int maxWidth = Integer.MAX_VALUE;

    private Problem problem;
    private MDD mdd;

    /**
     * Constructor of the solver : allows the user to choose heuristics.
     *
     * @param problem          the implementation of a problem
     * @param mergeSelector    heuristic to select nodes to merge (to build relaxed MDDs)
     * @param deleteSelector   heuristic to select nodes to delete (to build restricted MDDs)
     * @param variableSelector heuristic to select the next variable to be assigned
     */
    public Solver(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        this.problem = problem;
        this.mdd = new MDD(problem, mergeSelector, deleteSelector, variableSelector);
    }

    /**
     * Solves the given problem with the given heuristics and returns the optimal solution if it exists.
     *
     * @return an object {@code State} containing the optimal value and assignment
     */
    public State solve(int timeOut) {
        long startTime = System.currentTimeMillis();

        State best = null;
        double bestBound = -Double.MAX_VALUE;

        Queue<State> q = new PriorityQueue<>(); // nodes are popped starting with the one with least value
        q.add(this.problem.root());

        while (!q.isEmpty()) {
            State state = q.poll();
            if (state.relaxedValue() <= bestBound) {
                continue;
            }

            this.mdd.setInitialState(state);
            State resultRestricted = this.mdd.solveRestricted(Math.min(maxWidth, problem.nVariables() - state.layerNumber()),// the width of the DD is equal to the number
                    startTime, timeOut);                                                                        // of variables not bound

            if (best == null || resultRestricted.value() > bestBound) {
                best = resultRestricted;
                bestBound = best.value();
                if (print) {
                    System.out.println("Improved solution : " + best.value());
                }
            }

            if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                return best;
            }

            if (!this.mdd.isExact()) {
                this.mdd.setInitialState(state);
                State resultRelaxed = this.mdd.solveRelaxed(Math.min(maxWidth, problem.nVariables() - state.layerNumber()), startTime, timeOut);

                if (resultRelaxed.value() > bestBound) {
                    for (State s : this.mdd.exactCutset()) {
                        s.setRelaxedValue(resultRelaxed.value());
                        q.add(s);
                    }
                }

                if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                    return best;
                }
            }
        }

        if (print) {
            if (best == null) {
                System.out.println("No solution found.");
            } else {
                long endTime = System.currentTimeMillis();
                System.out.println("====== Search completed ======");
                System.out.println("Optimal solution : " + best.value());
                System.out.println("Assignment       : ");
                for (Variable var : best.variables) {
                    System.out.println("Var. " + var.id + " = " + var.value());
                }
                System.out.println("Time elapsed : " + ((endTime - startTime) / 1000.0) + "s");
            }
        }

        return best;
    }

    /**
     * Solves the problem with no timeout.
     *
     * @return the state with optimal assignment
     */
    public State solve() {
        return this.solve(Integer.MAX_VALUE / 1000);
    }
}
