package test;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

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

public class TestMAX2SAT {
	
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
	
	private static long run(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		
		long startTime = System.currentTimeMillis();
		solver.solve();
		long endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	
	private static void testPerf() {
		Problem p = generate(150);
		
		int times = 5;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			//conf1 += run(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf2 += run(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			//conf3 += run(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf4 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			//conf5 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MAX2SAT.MAX2SATVariableSelector());
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
	 * instances can be found on http://sites.nlsde.buaa.edu.cn/~kexu/benchmarks/max-sat-benchmarks.htm
	 * @param path path to an input file in DIMACS wcnf format
	 */
	private static void testData(String path) {
		int n = 0, m = 0, i = 0;
		Clause [] clauses = null;
		
		try {
			Scanner scan = new Scanner(new File(path));
			
			while(scan.hasNextLine()) {
				String line = scan.nextLine();
				String [] tokens = line.split("\\s+");
				
				if(tokens.length > 0) {
					if(tokens[0].equals("c")) continue;
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
		
		//run(new MAX2SAT(n, clauses), new MinLPMergeSelector(), new MinLPDeleteSelector(), new MAX2SAT.MAX2SATVariableSelector());// new SimpleVariableSelector());
		run(new MAX2SAT(n, clauses), new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
	}
	
	public static void main(String[] args) {
		//testPerf();
		testData("data/max2sat/frb15-9-2.wcnf");
	}
	
}
