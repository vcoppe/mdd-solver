package experiments;

import core.Solver;
import gurobi.GRBException;
import mdd.State;
import problems.Edge;

import java.io.File;
import java.util.Locale;
import java.util.Scanner;

public class MinLA {

    private static int n;
    private static Edge[] edges;

    /**
     * Instances can be found on <a href=https://www.cs.upc.edu/~jpetit/MinLA/Experiments/</a>.
     *
     * @param path path to a .gra file
     */
    private static void readGra(String path) {
        int m, deg[];
        double opt = 0;

        try {
            Scanner scan = new Scanner(new File(path));

            String line = scan.nextLine();
            String[] tokens = line.split("\\s+");

            if (tokens[0].equals("opt")) {
                opt = Integer.valueOf(tokens[1]);

                n = scan.nextInt();
                m = scan.nextInt();
            } else {
                n = Integer.valueOf(tokens[0]);
                m = scan.nextInt();
            }

            deg = new int[n];
            edges = new Edge[m * 2];

            for (int i = 0; i < n; i++) {
                deg[i] = scan.nextInt();
            }

            int cumul = 0, j;
            for (int i = 0; i < n; i++) {
                for (int k = cumul; k < cumul + deg[i]; k++) {
                    j = scan.nextInt();
                    edges[k] = new Edge(i, j, -1);
                }
                cumul += deg[i];
            }

            scan.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws GRBException {
        if (args.length < 3) {
            System.out.println("Arguments needed :\n\tfilename\n\ttimeLimit\n\tmip/mdd\n\t[mdd max width]");
            return;
        }

        readGra(args[0]);

        int timeLimit = Integer.valueOf(args[1]);

        if (args[2].equals("mip")) {
            mip.MinLA mip = new mip.MinLA(n, edges);
            mip.solve(timeLimit);

            System.out.printf(Locale.US, "%.3f %.0f %.5f\n", mip.runTime(), mip.objVal(), mip.gap());

            mip.dispose();
        } else if (args[2].equals("mdd")) {
            problems.MinLA mdd = new problems.MinLA(n, edges);

            Solver solver = new Solver(mdd);

            if (args.length == 4) solver.setWidth(Integer.valueOf(args[3]));

            State result = solver.solve(timeLimit);

            System.out.printf(Locale.US, "%.3f %.0f %.5f\n", solver.runTime(), -result.value(), solver.gap());
        }
    }

}
