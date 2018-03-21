import java.util.HashSet;
import java.util.Set;

public class Variable {
	
	private Set<Integer> domain;
	
	public Variable(int min, int max) {
		this.domain = new HashSet<Integer>();
		for (int i = min; i <= max; i++) {
			this.domain.add(i);
		}
	}
	
	public Variable(int n) {
		this(0, n-1);
	}
	
	public Variable(Set<Integer> domain) {
		this.domain = new HashSet<Integer>();
		for (Integer i : domain) {
			this.domain.add(i);
		}
	}
	
	public boolean isAssigned() {
		return this.domainSize() == 1;
	}
	
	public int domainSize() {
		return this.domain.size();
	}
	
	public void assign(int value) throws InconsistencyException {
		if(!this.contains(value)) {
			throw new InconsistencyException("Assigning incorrect value to variable.");
		}
		
		this.domain.clear();
		this.domain.add(value);
	}
	
	public boolean contains(int value) {
		return this.domain.contains(value);
	}
	
	public void remove(int value) throws InconsistencyException {
		if(this.isAssigned()) {
			throw new InconsistencyException("Removing value from assigned variable.");
		}
		
		if(!this.contains(value)) {
			throw new InconsistencyException("Removing incorrect value from variable.");
		}
		
		this.domain.remove(value);
	}
	
}
