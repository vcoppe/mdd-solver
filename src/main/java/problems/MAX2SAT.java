package problems;

import core.Problem;
import core.Variable;
import heuristics.VariableSelector;
import mdd.Layer;
import mdd.State;
import mdd.StateRepresentation;

import java.io.File;
import java.util.*;

public class MAX2SAT implements Problem {

    private static Map<Integer, double[]>[] g;

    private static int nVariables;
    private State root;
    private static boolean done = false;

    public double opt;

    /**
     * Returns the representation of the MAX2SAT problem.
     *
     * @param n       the number of boolean variables
     * @param clauses an array of {@code Clause} objects with variables indexes in [0,n-1]
     */
    MAX2SAT(int n, Clause[] clauses) {
        this(toGraph(n, clauses));
    }

    /**
     * Returns the representation of the MAX2SAT problem.
     *
     * @param g an array of maps where {@code g[i].get(j)} contains an array with the weight
     *          of the clauses with the variables {@code i} and {@code j} with
     *          0 = 00 -> FF
     *          1 = 01 -> FT
     *          2 = 10 -> TF
     *          3 = 11 -> TT
     *          where the first bit corresponds to the smallest variable id.
     */
    private MAX2SAT(Map<Integer, double[]>[] g) {
        nVariables = g.length;
        done = false;
        MAX2SAT.g = g;

        this.root = new State(new MAX2SATState(nVariables), Variable.newArray(nVariables), 0);
    }

    public State root() {
        return this.root;
    }

    public int nVariables() {
        return nVariables;
    }

    public List<State> successors(State s, Variable var) {
        int u = var.id;
        List<State> succs = new LinkedList<>();

        MAX2SATState max2satState = ((MAX2SATState) s.stateRepresentation);

        // assigning var to 0
        double[] benefits0 = new double[nVariables];
        double value0 = s.value() + Math.max(0, -max2satState.benefits[u]);

        for (int i = 0; i < nVariables; i++) {
            if (!s.isBound(i)) {
                if (u != i) benefits0[i] = max2satState.benefits[i];
                if (g[u].containsKey(i)) {
                    int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
                    if (u > i) {
                        numTF = 1;
                        numFT = 2;
                    }

                    if (u != i) {
                        value0 += g[u].get(i)[numFF] + g[u].get(i)[numFT]
                                + Math.min(
                                Math.max(0, max2satState.benefits[i]) + g[u].get(i)[numTT],
                                Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numTF]
                        );
                        benefits0[i] += g[u].get(i)[numTT] - g[u].get(i)[numTF];
                    } else {
                        value0 += g[u].get(i)[numFF];
                    }
                }
            }
        }

        succs.add(s.getSuccessor(new MAX2SATState(benefits0), value0, u, 0));

        // assigning var to 1
        double[] benefits1 = new double[nVariables];
        double value1 = s.value() + Math.max(0, max2satState.benefits[u]);

        for (int i = 0; i < nVariables; i++) {
            if (!s.isBound(i)) {
                if (u != i) benefits1[i] = max2satState.benefits[i];
                if (g[u].containsKey(i)) {
                    int numTT = 3, numTF = 2, numFT = 1, numFF = 0;
                    if (u > i) {
                        numTF = 1;
                        numFT = 2;
                    }

                    if (u != i) {
                        value1 += g[u].get(i)[numTF] + g[u].get(i)[numTT]
                                + Math.min(
                                Math.max(0, max2satState.benefits[i]) + g[u].get(i)[numFT],
                                Math.max(0, -max2satState.benefits[i]) + g[u].get(i)[numFF]
                        );
                        benefits1[i] += g[u].get(i)[numFT] - g[u].get(i)[numFF];
                    } else {
                        value1 += g[u].get(i)[numTT];
                    }
                }
            }
        }

        succs.add(s.getSuccessor(new MAX2SATState(benefits1), value1, u, 1));

        return succs;
    }

    public State merge(State[] states) {
        Variable[] variables = null;
        int[] indexes = null;
        double maxValue = -Double.MAX_VALUE;
        double[] benefits = new double[nVariables];
        double[] newValues = new double[states.length];
        MAX2SATState[] statesRep = new MAX2SATState[states.length];

        int i = 0;
        for (State state : states) {
            newValues[i] = state.value();
            statesRep[i++] = (MAX2SATState) state.stateRepresentation;
        }

        for (i = 0; i < nVariables; i++) {
            double sign = 0;
            double minValue = Double.MAX_VALUE;
            boolean same = true;

            for (MAX2SATState state : statesRep) {
                minValue = Math.min(minValue, Math.abs(state.benefits[i]));
                if (sign == 0 && state.benefits[i] != 0) {
                    sign = state.benefits[i] / state.benefits[i]; // +/- 1
                } else if (sign * state.benefits[i] < 0) {
                    same = false;
                    break;
                }
            }

            if (same) {
                benefits[i] = sign * minValue;
            }

            for (int j = 0; j < newValues.length; j++) {
                newValues[j] += Math.abs(statesRep[j].benefits[i]) - Math.abs(benefits[i]);
            }
        }

        for (i = 0; i < newValues.length; i++) {
            if (newValues[i] > maxValue) {
                maxValue = newValues[i];
                variables = states[i].variables;
                indexes = states[i].indexes;
            }
        }

        return new State(new MAX2SATState(benefits), variables, indexes, maxValue, false);
    }

    private static Map<Integer, double[]>[] toGraph(int n, Clause[] clauses) {
        @SuppressWarnings("unchecked")
        Map<Integer, double[]>[] g = new Map[n];
        for (int i = 0; i < g.length; i++) {
            g[i] = new HashMap<>();
        }

        for (Clause clause : clauses) {
            if (!g[clause.u].containsKey(clause.v)) {
                g[clause.u].put(clause.v, new double[4]);
            }
            g[clause.u].get(clause.v)[clause.num] += clause.w;

            if (!g[clause.v].containsKey(clause.u)) {
                g[clause.v].put(clause.u, new double[4]);
            }
            g[clause.v].get(clause.u)[clause.num] += clause.w;
        }

        return g;
    }

    class MAX2SATState implements StateRepresentation {

        double[] benefits;

        MAX2SATState(int size) {
            this.benefits = new double[size];
        }

        MAX2SATState(double[] benefits) {
            this.benefits = benefits;
        }

        public int hashCode() {
            return Arrays.hashCode(benefits);
        }

        public boolean equals(Object o) {
            if (!(o instanceof MAX2SATState)) {
                return false;
            }

            MAX2SATState other = (MAX2SATState) o;
            return Arrays.equals(benefits, other.benefits);
        }

        public double rank(State state) {
            double rank = state.value();

            for (double benefit : benefits) {
                rank += Math.abs(benefit);
            }

            return rank;
        }

        public MAX2SATState copy() {
            return new MAX2SATState(this.benefits.clone());
        }
    }

    public static class MAX2SATVariableSelector implements VariableSelector {

        int[] index;

        public Variable select(Variable[] vars, Layer layer) {
            if (!done) {
                index = new int[nVariables];
                @SuppressWarnings("unchecked")
                Map.Entry<Double,Integer> [] l = new Map.Entry[nVariables];

                for (int i = 0; i < nVariables; i++) {
                    double sum = 0;
                    for (double[] weights : g[i].values()) {
                        for (double w : weights) {
                            sum += w;
                        }
                    }
                    l[i] = new AbstractMap.SimpleEntry<>(sum, i);
                }

                Arrays.sort(l, (a, b) -> b.getKey().compareTo(a.getKey()));
                for (int i = 0; i < nVariables; i++) {
                    index[l[i].getValue()] = i;
                }

                done = true;
            }

            Variable ret = null;
            for (Variable var : vars) {
                if (ret == null || index[var.id] < index[ret.id]) {
                    ret = var;
                }
            }
            return ret;
        }

    }

    /**
     * Instances can be found on <a href=http://sites.nlsde.buaa.edu.cn/~kexu/benchmarks/max-sat-benchmarks.htm">this website</a>.
     *
     * @param path path to an input file in DIMACS wcnf format
     * @return a MAX2SAT object encoding the problem
     */
    public static MAX2SAT readDIMACS(String path) {
        int n = 0, m, i = 0;
        double opt = -1;
        Clause[] clauses = null;

        try {
            Scanner scan = new Scanner(new File(path));

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] tokens = line.split("\\s+");

                if (tokens.length > 0) {
                    if (tokens[0].equals("c")) {
                        if (tokens.length > 2 && tokens[1].equals("opt")) {
                            opt = Double.valueOf(tokens[2]);
                        }
                        continue;
                    }
                    if (tokens[0].equals("p")) {
                        assert (tokens.length == 4);
                        assert (tokens[1].equals("wcnf"));
                        n = Integer.valueOf(tokens[2]);
                        m = Integer.valueOf(tokens[3]);
                        clauses = new Clause[m];
                    } else {
                        int u, v, tu = 1, tv = 1;
                        double w;
                        if (tokens.length == 3) {
                            w = Double.valueOf(tokens[0]);
                            u = Integer.valueOf(tokens[1]);
                            if (u < 0) {
                                u = -u;
                                tu = 0;
                            }
                            clauses[i++] = new Clause(u - 1, u - 1, tu, tu, w);
                        } else if (tokens.length == 4) {
                            w = Double.valueOf(tokens[0]);
                            u = Integer.valueOf(tokens[1]);
                            v = Integer.valueOf(tokens[2]);
                            if (u < 0) {
                                u = -u;
                                tu = 0;
                            }
                            if (v < 0) {
                                v = -v;
                                tv = 0;
                            }
                            clauses[i++] = new Clause(u - 1, v - 1, tu, tv, w);
                        }
                    }
                }
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (opt != -1) {
            System.out.println("Value to reach : " + opt);
        }

        MAX2SAT p = new MAX2SAT(toGraph(n, clauses));
        p.opt = opt;
        return p;
    }
}