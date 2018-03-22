package dp;

import heuristics.StateSelector;
import heuristics.VariableSelector;
import core.Problem;

public class DP {
	
	int nVariables;
	Layer [] layers;
	
	Problem problem;
	StateSelector stateSelector;
	
	public DP(Problem problem, StateSelector stateSelector, VariableSelector variableSelector) {
		this.nVariables = problem.nVariables();
		this.layers = new Layer[this.nVariables+1];
		this.layers[0] = new Layer(problem.root(), variableSelector, problem);
		this.stateSelector = stateSelector;
	}
	
	public double solveRestricted(int width) {
		for(int i = 0; i < nVariables; i++) {
			// System.out.println("> Generating layer " + (i+1));
			this.layers[i+1] = this.layers[i].nextLayer();
			
			while(this.layers[i+1].width() > width) {
				this.layers[i+1] = this.stateSelector.select(this.layers[i+1], width);
			}
		}
		return this.layers[nVariables].value();
	}
	
	public double solveRelaxed(int width) {
		for(int i = 0; i < nVariables; i++) {
			// System.out.println("> Generating layer " + (i+1));
			this.layers[i+1] = this.layers[i].nextLayer();
			
			while(this.layers[i+1].width() > width) {
				this.layers[i+1] = this.stateSelector.select(this.layers[i+1], width);
			}
		}
		return this.layers[nVariables].value();
	}
	
	public double solveExact() {
		return this.solveRelaxed(Integer.MAX_VALUE);
	}
}
