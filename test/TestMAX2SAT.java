package test;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import core.Problem;
import core.Solver;
import examples.Clause;
import examples.MAX2SAT;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleDeleteSelector;
import heuristics.SimpleMergeSelector;
import heuristics.SimpleVariableSelector;
import heuristics.VariableSelector;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMAX2SAT extends TestHelper {
	
	public TestMAX2SAT(String path) {
		super(path);
	}

	static Random random = new Random(12);
	
	public static Problem generate(int n) {
		LinkedList<Clause> clauses = new LinkedList<Clause>();
		
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < 1+random.nextInt(5); j++) {
				Clause c = new Clause(i, random.nextInt(n), random.nextInt(2), random.nextInt(2), random.nextDouble() * 100 - 50);
				if(c.v != i) {
					clauses.add(c);
				}
			}
		}
		
		Clause [] input = new Clause[clauses.size()];
		clauses.toArray(input);
		
		return new MAX2SAT(n, input);
	}
	
	private static long runTime(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		
		long startTime = System.currentTimeMillis();
		solver.solve();
		long endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	
	private static double run(Problem p, int timeOut, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		return solver.solve(timeOut).value();
	}
	
	private static void testPerf() {
		Problem p = generate(100);
		
		int times = 5;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			//conf1 += runTime(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf2 += runTime(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf3 += runTime(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf4 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			//conf5 += runTime(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MAX2SAT.MAX2SATVariableSelector());
			System.out.print("=");
		}
		System.out.println("]");
		
		System.out.println("===== Average resolution time =====");
		System.out.println("All simple heuristics : " + conf1/times);
		System.out.println("With MinLP merging : " + conf2/times);
		System.out.println("With MinLP deleting : " + conf3/times);
		System.out.println("With MinLP merging & deleting : " + conf4/times);
		System.out.println("With MinLP merging & deleting + MAX2SAT variable selection : " + conf5/times);
	}
	
	/**
	 * Instances can be found on <a href=http://sites.nlsde.buaa.edu.cn/~kexu/benchmarks/max-sat-benchmarks.htm">this website</a>.
	 * @param path path to an input file in DIMACS wcnf format
	 */
	protected boolean testData(int timeOut) {
		int n = 0, m = 0, i = 0;
		double opt = -1;
		Clause [] clauses = null;
		
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
						assert(tokens[1].equals("wcnf"));
						n = Integer.valueOf(tokens[2]);
						m = Integer.valueOf(tokens[3]);
						clauses = new Clause[m];
					} else {
						int u, v, tu = 1, tv = 1;
						double w;
						if(tokens.length == 3) {
							w = Double.valueOf(tokens[0]);
							u = Integer.valueOf(tokens[1]);
							if(u < 0) {
								u = -u;
								tu = 0;
							}
							clauses[i++] = new Clause(u-1, u-1, tu, tu, w);
						} else if(tokens.length == 4) {
							w = Double.valueOf(tokens[0]);
							u = Integer.valueOf(tokens[1]);
							v = Integer.valueOf(tokens[2]);
							if(u < 0) {
								u = -u;
								tu = 0;
							}
							if(v < 0) {
								v = -v;
								tv = 0;
							}
							clauses[i++] = new Clause(u-1, v-1, tu, tv, w);
						}
					}
				}
			}
			
			scan.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(opt != -1) {
			System.out.println("Value to reach : " + opt);
		}
		return opt == run(new MAX2SAT(n, clauses), timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
	}
	
	@Parameterized.Parameters
    public static Object[] data() { 
		return dataFromFolder("data/max2sat"); 
	}
	
}
