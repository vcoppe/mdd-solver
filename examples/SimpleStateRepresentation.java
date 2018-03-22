package examples;

import dp.StateRepresentation;

public class SimpleStateRepresentation implements StateRepresentation {
	
	static int idGen = 0;
	int id;
	
	public SimpleStateRepresentation() {
		this.id = idGen++;
	}

	public int hashCode() {
		return this.id;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof SimpleStateRepresentation) {
			SimpleStateRepresentation other = (SimpleStateRepresentation) obj;
			return Integer.compare(this.id, other.hashCode()) == 0;
		}
		return false;
	}
	
}