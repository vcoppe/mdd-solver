package examples;

import core.Problem;
import core.Solver;
import heuristics.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.LinkedList;
import java.util.Random;

@SuppressWarnings("unused")
@RunWith(Parameterized.class)
public class TestMAX2SATAll extends TestHelper {

    public TestMAX2SATAll(String path) {
		super(path);
	}

    private static Random random = new Random(12);

    private static Problem generate(int n) {
        LinkedList<Clause> clauses = new LinkedList<>();
		
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

    protected boolean testData(int timeOut) {
        MAX2SAT p = MAX2SAT.readDIMACS(path);
        double found = run(p, timeOut, new MinLPMergeSelector(), new MinLPDeleteSelector(), new SimpleVariableSelector());
        return p.opt == found;
	}
	
	@Parameterized.Parameters
    public static Object[] data() { 
		return dataFromFolder("data/max2sat"); 
	}
	
}
