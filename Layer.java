import java.util.ArrayList;
import java.util.List;

public class Layer {
	
	List<State> states;
	
	public Layer() {
		this.states = new ArrayList<State>();
	}
	
	public Layer(State state) {
		this.states = new ArrayList<State>();
		this.states.add(state);
	}
	
	public Layer(List<State> states) {
		this.states = states;
	}
	
	public void addState(State state) {
		this.states.add(state);
	}
	
	public void addStates(List<State> states) {
		for (State state : states) {
			this.states.add(state);
		}
	}
	
	public Layer nextLayer() {
		Layer next = new Layer();
		for (State state : this.states) {
			next.addStates(state.successors());
		}
		return next;
	}
	
}
