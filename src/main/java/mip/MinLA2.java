package mip;

import gurobi.*;
import problems.Edge;

import static problems.Edge.toMatrix;

public class MinLA2 {

    private GRBEnv env;
    private GRBModel model;
    private GRBVar[] pi;
    private GRBVar[][] x, w;

    public MinLA2(int n, Edge[] edges) throws GRBException {
        this(n, toMatrix(n, edges));
    }

    private MinLA2(int n, int[][] adj) throws GRBException {
        env = new GRBEnv("minla.log");
        model = new GRBModel(env);

        pi = new GRBVar[n];
        x = new GRBVar[n][n];
        w = new GRBVar[n][n];

        int m = 0;

        for (int u = 0; u < n; u++) {
            pi[u] = model.addVar(1, n, 0, GRB.CONTINUOUS, "pi_" + u);
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    x[u][v] = model.addVar(0, 1, 0, GRB.BINARY, "x_" + u + v);
                    w[u][v] = model.addVar(1, n - 1, adj[u][v], GRB.CONTINUOUS, "w_" + u + v);
                    m += adj[u][v];
                }
            }
        }

        m /= 2;

        model.addVar(1, 1, -m, GRB.CONTINUOUS, "OFFSET");

        GRBLinExpr lhs, rhs;
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                lhs = new GRBLinExpr();
                lhs.addTerm(1, x[u][v]);
                lhs.addTerm(1, x[v][u]);
                model.addConstr(lhs, GRB.EQUAL, 1, "c4_" + u + v);
            }
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs = new GRBLinExpr();
                    lhs.addTerm(1, pi[v]);
                    lhs.addTerm(-1, pi[u]);
                    rhs = new GRBLinExpr();
                    rhs.addTerm(1, w[u][v]);
                    rhs.addTerm(n, x[u][v]);
                    rhs.addConstant(-n);
                    model.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "c5_" + u + v);
                }
            }
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs = new GRBLinExpr();
                    lhs.addTerm(1, pi[v]);
                    lhs.addTerm(-1, pi[u]);
                    rhs = new GRBLinExpr();
                    rhs.addTerm(1, w[u][v]);
                    model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "c10_" + u + v);
                }
            }
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs = new GRBLinExpr();
                    lhs.addTerm(1, w[u][v]);
                    rhs = new GRBLinExpr();
                    rhs.addTerm(n - 2, x[u][v]);
                    rhs.addConstant(1);
                    model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "c11_" + u + v);
                }
            }
        }

        System.out.println(m);

        int p = 0, tot = 0;
        for (int i = 1; i <= m; i++) {
            tot += i;
            if (tot <= m) p = i;
            else break;
        }

        System.out.println(p);

        int pp = m, term2 = 0;
        for (int i = 1; i <= p; i++) {
            pp -= n - i;
            term2 += i * (n - i);
        }

        System.out.println(pp);

        lhs = new GRBLinExpr();
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (adj[u][v] == 1) {
                    lhs.addTerm(1, w[u][v]);
                }
            }
        }

        rhs = new GRBLinExpr();
        rhs.addConstant(m + term2 + pp * (p + 1));

        model.addConstr(lhs, GRB.GREATER_EQUAL, rhs, "c12");

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    for (int k = 0; k < n; k++) {
                        if (k != u && k != v) {
                            lhs = new GRBLinExpr();
                            lhs.addTerm(1, x[u][v]);
                            lhs.addTerm(1, x[v][k]);
                            lhs.addTerm(1, x[k][u]);
                            model.addConstr(lhs, GRB.LESS_EQUAL, 2, "c13_" + u + v + k);
                        }
                    }
                }
            }
        }

        lhs = new GRBLinExpr();
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs.addTerm(1, w[u][v]);
                }
            }
        }
        model.addConstr(lhs, GRB.EQUAL, n * (n - 1) * (n + 4) / 6, "c14");

        for (int u = 0; u < n; u++) {
            lhs = new GRBLinExpr();
            lhs.addTerm(1, pi[u]);
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs.addTerm(1, x[u][v]);
                }
            }
            model.addConstr(lhs, GRB.EQUAL, n, "c15_" + u);
        }

        for (int u = 0; u < n; u++) {
            lhs = new GRBLinExpr();
            lhs.addTerm(1, pi[u]);
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs.addTerm(-1, x[v][u]);
                }
            }
            model.addConstr(lhs, GRB.EQUAL, 1, "c16_" + u);
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    lhs = new GRBLinExpr();
                    lhs.addTerm(1, w[u][v]);
                    for (int t = 0; t < n; t++) {
                        if (t != u) {
                            lhs.addTerm(1, x[t][u]);
                        }
                    }
                    model.addConstr(lhs, GRB.LESS_EQUAL, n, "c17_" + u + v);
                }
            }
        }

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (u != v) {
                    for (int k = 0; k < n; k++) {
                        if (k != u && k != v) {
                            lhs = new GRBLinExpr();
                            lhs.addTerm(1, w[u][v]);
                            lhs.addTerm(1, w[v][u]);
                            rhs = new GRBLinExpr();
                            rhs.addTerm(1, w[v][k]);
                            rhs.addTerm(1, w[k][v]);
                            rhs.addTerm(1, w[k][u]);
                            rhs.addTerm(1, w[u][k]);
                            rhs.addConstant(-1);
                            model.addConstr(lhs, GRB.LESS_EQUAL, rhs, "c18_" + u + v + k);
                        }
                    }
                }
            }
        }
    }

    public void solve() throws GRBException {
        solve(Integer.MAX_VALUE);
    }

    public void solve(double timeLimit) throws GRBException {
        model.set(GRB.DoubleParam.TimeLimit, timeLimit);
        model.optimize();

        System.out.println("\nOptimal solution : " + model.get(GRB.DoubleAttr.ObjVal));

        System.out.println("Arrangement       :");
        for (int i = 0; i < x.length; i++)
            System.out.printf("%2.0f ", pi[i].get(GRB.DoubleAttr.X));

        System.out.println();
    }

    public double gap() throws GRBException {
        return model.get(GRB.DoubleAttr.MIPGap);
    }

    public double runTime() throws GRBException {
        return model.get(GRB.DoubleAttr.Runtime);
    }

    public double objVal() throws GRBException {
        return model.get(GRB.DoubleAttr.ObjVal);
    }

    public void dispose() throws GRBException {
        model.dispose();
        env.dispose();
    }
}
