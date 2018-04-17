package examples;

import java.util.LinkedList;
import java.util.Random;

import core.Problem;
import core.Solver;
import heuristics.MinLPDeleteSelector;
import heuristics.MinLPMergeSelector;
import heuristics.SimpleVariableSelector;

public class TestMISP {
	
	public static void main(String[] args) {
		int n = 50;
		double [] weights = new double[n];
		LinkedList<Edge> edges = new LinkedList<Edge>();
		
		Random random = new Random(12);
		
		for(int i = 0; i < n; i++) {
			weights[i] = random.nextDouble() * 100;
			
			for(int j = 0; j < 1+random.nextInt(10); j++) {
				Edge e = new Edge(i, random.nextInt(n));
				if(e.v != i) {
					edges.add(e);
				}
			}
		}
		
		Edge [] input = new Edge[edges.size()];
		edges.toArray(input);
		
		Problem p = new MISP(n, weights, input);
		
		Solver solver = new Solver(p, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
		solver.solve();
	}
	
}
