public class DP {
	
	int nVariables;
	Variable [] variables;
	Layer [] layers;
	
	StateSelector stateSelector;
	VariableSelector variableSelector;
	
	public DP(State root, Variable [] variables) {
		this.variables = variables;
		this.nVariables = variables.length;
		layers = new Layer[nVariables+1];
		layers[0] = new Layer(root);
	}
	
	
}
