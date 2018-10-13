package mip;

import gurobi.*;
import problems.Edge;

import java.util.LinkedList;

import static problems.Edge.toGraph;

public class MinLA {

    private GRBEnv env;
    private GRBModel model;
    private GRBVar[][] g;
    private GRBVar[][][][] z;

    public MinLA(int n, Edge[] edges) throws GRBException {
        this(n, toGraph(n, edges));
    }

    private MinLA(int n, LinkedList<Integer>[] adj) throws GRBException {
        env = new GRBEnv("minla.log");
        model = new GRBModel(env);

        g = new GRBVar[n][n];
        z = new GRBVar[n][n][n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                g[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "g" + i + j);
                for (int p = 0; p < n; p++) {
                    for (int q = 0; q < n; q++) {
                        if (p < q && adj[i].contains(j)) {
                            z[i][p][j][q] = model.addVar(0, GRB.INFINITY, q - p, GRB.INTEGER, "z" + i + p + j + q);
                        } else {
                            z[i][p][j][q] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "z" + i + p + j + q);
                        }
                    }
                }
            }
        }

        GRBLinExpr expr;
        for (int j = 0; j < n; j++) {
            expr = new GRBLinExpr();
            for (int i = 0; i < n; i++) {
                expr.addTerm(1, g[i][j]);
            }
            model.addConstr(expr, GRB.EQUAL, 1, "c1_" + j);
        }

        for (int i = 0; i < n; i++) {
            expr = new GRBLinExpr();
            for (int j = 0; j < n; j++) {
                expr.addTerm(1, g[i][j]);
            }
            model.addConstr(expr, GRB.EQUAL, 1, "c2_" + i);
        }

        for (int i = 0; i < n; i++) {
            for (int p = 0; p < n; p++) {
                for (int q = p + 1; q < n; q++) {
                    expr = new GRBLinExpr();
                    for (int j = 0; j < n; j++) {
                        if (i != j) {
                            expr.addTerm(1, z[i][p][j][q]);
                        }
                    }
                    model.addConstr(expr, GRB.EQUAL, g[i][p], "c3_" + i + p);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            for (int q = 0; q < n; q++) {
                for (int p = q + 1; p < n; p++) {
                    expr = new GRBLinExpr();
                    for (int j = 0; j < n; j++) {
                        if (i != j) {
                            expr.addTerm(1, z[j][q][i][p]);
                        }
                    }
                    model.addConstr(expr, GRB.EQUAL, g[i][p], "c4_" + i + p);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            for (int p = 0; p < n; p++) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        expr = new GRBLinExpr();
                        for (int q = 0; q < n; q++) {
                            if (p < q) {
                                expr.addTerm(1, z[i][p][j][q]);
                            } else if (p > q) {
                                expr.addTerm(1, z[j][q][i][p]);
                            }
                        }
                        model.addConstr(expr, GRB.EQUAL, g[i][p], "c5_" + i + p);
                    }
                }
            }
        }

        expr = new GRBLinExpr();
        for (int i = 0; i < n / 2 + 1; i++) {
            expr.addTerm(1, g[i][0]);
        }
        model.addConstr(expr, GRB.EQUAL, 1, "breaking symmetry");
    }

    public void solve() throws GRBException {
        model.optimize();

        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

        for (int j = 0; j < g.length; j++) {
            for (int i = 0; i < g.length; i++) {
                if (g[i][j].get(GRB.DoubleAttr.X) == 1) {
                    System.out.println("Var. " + j + " = " + i);
                }
            }
        }

        model.dispose();
        env.dispose();
    }
}
