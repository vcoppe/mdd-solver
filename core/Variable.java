package core;

import utils.InconsistencyException;

import java.util.HashSet;
import java.util.Set;

/*
 * Represents a variable that can take integer values
 */
public class Variable {
	
	int id;
	Set<Integer> domain;
	
	public Variable(int id, int min, int max) {
		this.id = id;
		this.domain = new HashSet<Integer>();
		for(int i = min; i <= max; i++) {
			this.domain.add(i);
		}
	}
	
	public Variable(int id, int n) {
		this(id, 0, n-1);
	}
	
	public Variable(int id, Set<Integer> domain) {
		this.id = id;
		this.domain = new HashSet<Integer>();
		for(Integer i : domain) {
			this.domain.add(i);
		}
	}
	
	public Variable copy() {
		return new Variable(this.id, this.domain);
	}
	
	public boolean isAssigned() {
		return this.domainSize() == 1;
	}
	
	public int domainSize() {
		return this.domain.size();
	}
	
	public int id() {
		return this.id;
	}
	
	public void assign(int value) throws InconsistencyException {
		if(!this.contains(value)) {
			throw new InconsistencyException("Assigning incorrect value to variable : " + value + ".");
		}
		
		this.domain.clear();
		this.domain.add(value);
		
		//System.out.println(">>> " + value + " assigned to variable " + this.id);
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
