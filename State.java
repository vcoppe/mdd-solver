import java.util.List;

public interface State {
	
	boolean isTerminal();
	
	double value();
	
	List<State> successors();
	
	State merge(State other);
	
}
