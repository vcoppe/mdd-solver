package core;

import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.VariableSelector;
import mdd.MDD;
import mdd.State;

import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Implementation of the branch and bound algorithm for MDDs.
 *
 * @author Vianney Coppé
 */
public class Solver {

    private int maxWidth = Integer.MAX_VALUE;
    private long startTime, endTime = -1;
    private double lowerBound, upperBound;
    private boolean adaptiveWidth = true;

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
        startTime = System.currentTimeMillis();

        State best = null;
        lowerBound = -Double.MAX_VALUE;
        upperBound = Double.MAX_VALUE;

        Queue<State> q = new PriorityQueue<>(); // nodes are popped starting with the one with least value
        q.add(this.problem.root());

        while (!q.isEmpty()) {
            State state = q.poll();
            if (state.relaxedValue() <= lowerBound) {
                continue;
            }

            this.mdd.setInitialState(state);
            State resultRestricted;
            if (this.adaptiveWidth) resultRestricted = this.mdd.solveRestricted(
                    problem.nVariables() - state.layerNumber(), // the width of the DD is equal to the number
                    startTime, timeOut);                               // of variables not bound
            else resultRestricted = this.mdd.solveRestricted(maxWidth, startTime, timeOut);

            if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                endTime = System.currentTimeMillis();
                return best;
            }

            if (best == null || resultRestricted.value() > lowerBound) {
                best = resultRestricted;
                lowerBound = best.value();
                printInfo(true);
            }

            if (!this.mdd.isExact()) {
                this.mdd.setInitialState(state);
                State resultRelaxed;
                if (this.adaptiveWidth) resultRelaxed = this.mdd.solveRelaxed(
                        problem.nVariables() - state.layerNumber(), // the width of the DD is equal to the number
                        startTime, timeOut);                               // of variables not bound
                else resultRelaxed = this.mdd.solveRelaxed(maxWidth, startTime, timeOut);

                if (System.currentTimeMillis() - startTime > timeOut * 1000) {
                    endTime = System.currentTimeMillis();
                    return best;
                }

                if (resultRelaxed.value() > lowerBound) {
                    for (State s : this.mdd.exactCutset()) {
                        s.setRelaxedValue(resultRelaxed.value());
                        q.add(s);
                    }
                }

                if (!q.isEmpty()) {
                    double queueUpperBound = -Double.MAX_VALUE;
                    for (State s : q) {
                        queueUpperBound = Math.max(queueUpperBound, s.relaxedValue());
                    }
                    if (queueUpperBound < upperBound) {
                        upperBound = queueUpperBound;
                        printInfo(false);
                    }
                }
            }
        }

        upperBound = lowerBound;
        endTime = System.currentTimeMillis();

        if (best == null) {
            System.out.println("No solution found.");
        } else {
            System.out.println("\n====== Search completed ======");
            System.out.println("Optimal solution : " + best.value());
            System.out.println("Assignment       : ");
            for (Variable var : best.variables) {
                System.out.println("\tVar. " + var.id + " = " + var.value());
            }
            System.out.println("Time elapsed : " + ((endTime - startTime) / 1000.0) + "s\n");
        }

        return best;
    }

    private void printInfo(boolean newSolution) {
        String sol = "";
        if (newSolution) sol = "*";
        double gap = 100 * gap();
        double timeElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        if (upperBound == Double.MAX_VALUE) {
            System.out.println("   |  Best sol.  Best bound |         Gap |        Time");
            System.out.format(Locale.US, "%2s | %10.3f  %10s | %10.3f%% | %10.3fs%n", sol, lowerBound, "inf", gap, timeElapsed);
        } else {
            System.out.format(Locale.US, "%2s | %10.3f  %10.3f | %10.3f%% | %10.3fs%n", sol, lowerBound, upperBound, gap, timeElapsed);
        }
    }

    /**
     * Solves the problem with no timeout.
     *
     * @return the state with optimal assignment
     */
    public State solve() {
        return this.solve(Integer.MAX_VALUE / 1000);
    }

    public void setWidth(int width) {
        this.adaptiveWidth = false;
        this.maxWidth = width;
    }

    public double gap() {
        if (upperBound == Double.MAX_VALUE) return 1;
        return Math.abs(upperBound - lowerBound) / Math.abs(lowerBound);
    }

    public double runTime() {
        return (endTime - startTime) / 1000.0;
    }
}
