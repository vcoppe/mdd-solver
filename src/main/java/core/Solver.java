package core;

import java.util.PriorityQueue;
import java.util.Queue;

import dp.DP;
import dp.State;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;

/**
 * Implementation of the branch and bound algorithm for MDDs.
 *
 * @author Vianney Coppé
 */
public class Solver {

    private boolean print = true;
    private int maxWidth = 5;

    private Problem problem;
    private DP dp;

    /**
     * Constructor of the solver : allows the user to choose heuristics.
     *
     * @param problem          the implementation of a problem
     * @param mergeSelector    heuristic to select nodes to merge (to build relaxed MDDs)
     * @param deleteSelector   heuristic to select nodes to delete (to build restricted MDDs)
     * @param variableSelector heuristic to select the next variable to be assigned
     * @return the {@code Solver} object ready to solve the problem
     */
    public Solver(Problem problem, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
        this.problem = problem;
        this.dp = new DP(problem, mergeSelector, deleteSelector, variableSelector);
    }

    /**
     * Solves the given problem with the given heuristics and returns the optimal solution if it exists.
     *
     * @return an object {@code State} containing the optimal value and assignment
     */
    public State solve(int timeOut) {
        long startTime = System.currentTimeMillis();

        State best = null;
        double bestBound = Double.MIN_VALUE;

        Queue<State> q = new PriorityQueue<State>(); // nodes are popped starting with the one with least value
        q.add(this.problem.root());

        while (!q.isEmpty()) {
            State state = q.poll();
            if (state.relaxedValue() <= bestBound) {
                continue;
            }

            this.dp.setInitialState(state);
            State result = this.dp.solveRestricted(Math.min(maxWidth, problem.nVariables() - state.layerNumber()),// the width of the DD is equal to the number
                    startTime, timeOut);                                                                        // of variables not bound


            if (best == null || result.value() > best.value()) {
                best = result.copy();
                bestBound = best.value();
                if (print) {
                    System.out.println("Improved solution : " + best.value());
                }
            }

            if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                return best;
            }

            if (!this.dp.isExact()) {
                this.dp.setInitialState(state);
                result = this.dp.solveRelaxed(Math.min(maxWidth, problem.nVariables() - state.layerNumber()), startTime, timeOut);

                if (result.value() > bestBound) {
                    for (State s : this.dp.exactCutset()) {
                        s.setRelaxedValue(result.value());
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
                System.out.println("====== Search completed ======");
                System.out.println("Optimal solution : " + best.value());
                System.out.print("Assignment       : ");
                for (Variable var : best.variables()) {
                    if (var.value() == 1) System.out.print(var.id() + " ");
                }
                System.out.println();
            }
        }

        return best;
    }

    public State solve() {
        return this.solve(Integer.MAX_VALUE / 1000);
    }
}
