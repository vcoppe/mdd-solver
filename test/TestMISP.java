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
	
	static long startTime;
	static long endTime;
	
	static int n;
	static double [] weights;
	static Edge [] input;
	
	private static long run(MergeSelector mergeSelector, DeleteSelector deleteSelector, VariableSelector variableSelector) {
		Problem p = new MISP(n, weights, input);
		
		Solver solver = new Solver(p, mergeSelector, deleteSelector, variableSelector);
		
		startTime = System.currentTimeMillis();
		solver.solve();
		endTime = System.currentTimeMillis();
		
		return endTime - startTime;
	}
	
	public static void main(String[] args) {
		n = 35;
		weights = new double[n];
		LinkedList<Edge> edges = new LinkedList<Edge>();
		
		Random random = new Random(12);
		
		for(int i = 0; i < n; i++) {
			weights[i] = random.nextDouble() * 100 - 50;
			
			for(int j = 0; j < 1+random.nextInt(5); j++) {
				Edge e = new Edge(i, random.nextInt(n));
				if(e.v != i) {
					edges.add(e);
				}
			}
		}
		
		input = new Edge[edges.size()];
		edges.toArray(input);
		
		int times = 10;
		long conf1 = 0, conf2 = 0, conf3 = 0, conf4 = 0, conf5 = 0;
		System.out.print("[");
		for(int i = 0; i < times; i++) {
			conf1 += run(new SimpleMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf2 += run(new MinLPMergeSelector(), new SimpleDeleteSelector(), new SimpleVariableSelector());
			conf3 += run(new SimpleMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf4 += run(new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
			conf5 += run(new MinLPMergeSelector(), new MinLPDeleteSelector(), new MISP.MISPVariableSelector());
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
