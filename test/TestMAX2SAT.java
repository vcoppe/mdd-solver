package test;

import java.util.LinkedList;
import java.util.Random;

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
	
	public static void main(String[] args) {
		Problem p = generate(25);
		
		int times = 5;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			conf1 += run(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf2 += run(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf3 += run(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf4 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf5 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MAX2SAT.MAX2SATVariableSelector());
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
	
}
