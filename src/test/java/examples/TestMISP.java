package examples;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import core.Problem;
import core.Solver;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.VariableSelector;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMISP extends TestHelper {
	
	public TestMISP(String path) {
		super(path);
	}

	static Random random = new Random(12);
	
	public static Problem generate(int n) {
		double [] weights = new double[n];
		LinkedList<Edge> edges = new LinkedList<Edge>();
		
		for(int i = 0; i < n; i++) {
			weights[i] = random.nextDouble() * 100 - 50;
			
			for(int j = 0; j < 1+random.nextInt(5); j++) {
				Edge e = new Edge(i, random.nextInt(n));
				if(e.v != i) {
					edges.add(e);
				}
			}
		}
		
		Edge [] input = new Edge[edges.size()];
		edges.toArray(input);
		
		return new MISP(n, weights, input);
	}
	
	private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		return solver.solve(timeOut).value();
	}
	private static long runTime(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		
		long startTime = System.currentTimeMillis();
		solver.solve();
		long endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	
	private static void testPerf() {
		Problem p = generate(70);
	
		int times = 5;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			//conf1 += runTime(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf2 += runTime(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf3 += runTime(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			//conf4 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf5 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
			System.out.print("=");
		}
		System.out.println("]");
		
		System.out.println("===== Average resolution time =====");
		System.out.println("All simple heuristics : " + conf1/times);
		System.out.println("With MinLP merging : " + conf2/times);
		System.out.println("With MinLP deleting : " + conf3/times);
		System.out.println("With MinLP merging & deleting : " + conf4/times);
		System.out.println("With MinLP merging & deleting + MISP variable selection : " + conf5/times);
	}
	
	/**
	 * Instances can be found on <a href="https://turing.cs.hbg.psu.edu/txn131/clique.html#DIMACS_cliques">this website</a>.
	 * Since they are maximum clique problems, we take the complement graph to use our MISP solver.
	 * @param path path to an input file in DIMACS edge format
	 */
	protected boolean testData(int timeOut) {
		int n = 0, m = 0, i = 0;
		double opt = -1;
		Edge [] edges = null;
		
		try {
			Scanner scan = new Scanner(new File(path));
			
			while(scan.hasNextLine()) {
				String line = scan.nextLine();
				String [] tokens = line.split("\\s+");
				
				if(tokens.length > 0) {
					if(tokens[0].equals("c")) {
						if(tokens.length > 2 && tokens[1].equals("opt")) {
							opt = Double.valueOf(tokens[2]);
						}
						continue;
					}
					if(tokens[0].equals("p")) {
						assert(tokens.length == 4);
						assert(tokens[1].equals("edge"));
						n = Integer.valueOf(tokens[2]);
						m = Integer.valueOf(tokens[3]);
						edges = new Edge[m];
					} else {
						if(tokens.length == 3) {
							int u = Integer.valueOf(tokens[1])-1;
							int v = Integer.valueOf(tokens[2])-1;
							edges[i++] = new Edge(u, v);
						}
					}
				}
			}
			
			scan.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		double [] weights = new double[n];
		
		LinkedList<Integer>[] g = MISP.toGraph(n, edges);
		@SuppressWarnings("unchecked")
		LinkedList<Integer>[] complement = new LinkedList[n];
		
		for(i = 0; i < n; i++) {
			weights[i] = 1;
			complement[i] = new LinkedList<Integer>();

			for(int j = 0; j < n; j++) if(i != j && !g[i].contains(j)) {
				complement[i].add(j);
			}
		}

		if(opt != -1) {
			System.out.println("Value to reach : " + opt);
		}
		return opt == run(new MISP(weights, complement), timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
	}
	
	@Parameterized.Parameters
    public static Object[] data() { 
		return dataFromFolder("data/misp/easy"); 
	}
	
}
