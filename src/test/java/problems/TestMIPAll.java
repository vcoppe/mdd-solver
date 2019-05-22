package problems;

import gurobi.GRBException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Scanner;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMIPAll extends TestHelper {

    private static int n;
    private static Edge[] edges;

    public TestMIPAll(String path) {
        super(path);
    }

    @Parameterized.Parameters
    public static Object[] data() {
        return dataFromFolder("data/minla/nugent");
    }

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

    @Override
    protected boolean testData(int timeOut) {
        readGra(path);
        try {
            mip.MinLA mip = new mip.MinLA(n, edges);
            mip.solve(timeOut);
            mip.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
