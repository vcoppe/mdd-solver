package test;

import java.util.LinkedList;
import java.util.Random;

import core.Problem;
import core.Solver;
import examples.Edge;
import examples.MISP;
import heuristics.DeleteSelector;
import heuristics.MergeSelector;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleDeleteSelector;
import heuristics.SimpleMergeSelector;
import heuristics.SimpleVariableSelector;
import heuristics.VariableSelector;

public class TestMISP {
	
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
	
	private static long run(Problem p, MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		
		long startTime = System.currentTimeMillis();
		solver.solve();
		long endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	
	public static void main(String[] args) {
		Problem p = generate(35);
		
		int times = 5;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			conf1 += run(p, new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf2 += run(p, new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf3 += run(p, new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf4 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf5 += run(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
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
	
}
