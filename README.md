# mdd-solver [![Codeship Status for vcoppe/mdd-solver](https://app.codeship.com/projects/12e85050-303f-0136-d8f8-0a3261184ed0/status?branch=master)](https://app.codeship.com/projects/288562) [![codecov](https://codecov.io/gh/vcoppe/mdd-solver/branch/master/graph/badge.svg)](https://codecov.io/gh/vcoppe/mdd-solver)

This project is a Java implementation of a generic discrete optimization solver based on *Multi-valued Decision Diagrams* (MDD), based on the theory presented in the article [Discrete Optimization with Decision Diagrams](https://www.andrew.cmu.edu/user/vanhoeve/papers/discrete_opt_with_DDs.pdf) by D. Bergman, A. A. Cire, W.-J. van Hoeve and J. N. Hooker. It provides a Java library allowing to solve new problems with simple and limited amount of code, given a correct decision diagram formulation. 

Four problems are already implemented in the `problems` package :
* Maximum Independent Set Problem
* Maximum Cut Problem
* Maximum 2-Satisfiability Problem
* Minimum Linear Arrangement Problem
where the three first are formalized in the article mentioned previously and the latter is part of my thesis (link to follow).

## Contents

- [Installation](#installation)
- [Usage](#usage)
    * [Example : the Unbounded Knapsack Problem](#example)
- [Javadoc](#javadoc)
- [License](#license)

## Installation
Use [IntelliJ IDEA](https://www.jetbrains.com/idea/) and open directly the Maven project (file [pom.xml](https://github.com/vcoppe/mdd-solver/blob/master/pom.xml)). 

[Gurobi Optimizer](http://www.gurobi.com/) is required to run the comparative MIP models.

## Usage

In order to implement a new problem with this library, you need to implement two interfaces.

The first one is the interface [State.java](https://github.com/vcoppe/mdd-solver/blob/master/src/main/java/mdd/State.java) showed below. Classes implementing this interface will contain the representation of the states *s<sup>j</sup>* of the dynamic programming formulation of a problem. We use `HashMap` objects to represent the layers of the decision diagrams and identify whether an equivalent node is already in the layer. `State` objects are used as keys of these hash tables so the methods `hashCode` and `equals` should contain key information to detect equivalent states and provoke collisions between them. For equivalent states, the method `hashCode` should return the same result and the method `equals` should return `true`. The method `rank` is used to compare nodes before deleting or merging a subset of them when building restricted or relaxed decision diagrams. Finally, the method `copy` should return another object with an equivalent state.
```java
public interface State {

    int hashCode();

    boolean equals(Object obj);

    double rank(Node node);

    State copy();

}
```

The interface [Problem.java](https://github.com/vcoppe/mdd-solver/blob/master/src/main/java/core/Problem.java) showed below is the second component which has to be adapted to every problem and it will contain the rest of the information about the problem, mainly the state transitions *t<sub>j</sub>(s<sup>j</sup>,x<sub>j</sub>)* and *h<sub>j</sub>(s<sup>j</sup>,x<sub>j</sub>)* and the merging operator ⊕(M). The two first methods are very simple : `root` should return a node with the root state *r* of the problem and `nVariables` should return the number of variables *n* of the problem. Given a node and an unbound variable, the method `successors` should return a list of nodes that we reach by assigning every possible value to the variable. The method `merge` takes an array of nodes and should return a single node resulting from a valid merging operation.
```java
public interface Problem {

    Node root();

    int nVariables();

    List<Node> successors(Node s, Variable var);

    Node merge(Node[] nodes);

}
```

### Example

In the `problems` package, we included an example implementing a very simple formulation of the Unbounded Knapsack Problem (see [Knapsack.java](https://github.com/vcoppe/mdd-solver/blob/master/src/main/java/problems/Knapsack.java)). We will quickly go through the implementation of this particular problem in our Java solver, which is realized through the two interfaces shown above.

The first one is the interface [State.java](https://github.com/vcoppe/mdd-solver/blob/master/src/main/java/mdd/State.java), the code below shows how this interface is implemented for the knapsack problem. The states are the remaining capacity so we only need to store an integer `capacity` in each state. In this case, two states (of a same layer) are equivalent if their remaining capacities are equal so the methods `hashCode` and `equals` are very straightforward. Finally, the method `rank` (which concerns the selection of nodes to remove/merge in *restricted*/*relaxed* decision diagrams) is chosen to return the longest-path value of the corresponding node.

```java
private class KnapsackState implements State {

    int capacity;

    KnapsackState(int capacity) { this.capacity = capacity; }

    public double rank(Node node) { return node.value(); }

    public int hashCode() { return capacity; }

    public boolean equals(Object o) {
            return o instanceof Knapsack.KnapsackState
                && capacity == ((KnapsackState) o).capacity;
    }

    public KnapsackState copy() { return new KnapsackState(capacity); }

}
```

We now discuss the implementation of the interface [Problem.java](https://github.com/vcoppe/mdd-solver/blob/master/src/main/java/core/Problem.java) for the knapsack problem. In the code displayed below, we have that :
* `n` is the number of different items
* `c` is the capacity of the knapsack 
* the arrays `w` and `v` respectively contain the weight and value of the items

In the constructor, we instantiate the root node of the decision diagram with 3 parameters : the associated state which has a remaining capacity equal to `c`, the variables in the number of `n` and the root value which is set to `0`.

```java
public class Knapsack implements Problem {

    private int n, w[];
    private double[] v;
    private Node root;

    Knapsack(int n, int c, int[] w, double[] v) {
        this.n = n;
        this.w = w;
        this.v = v;
        root = new Node(new KnapsackState(c), Variable.newArray(n), 0);
    }

    public Node root() { return root; }

    public int nVariables() { return n; }

    public List<Node> successors(Node node, Variable var) { ... }

    public Node merge(Node[] nodes) { ... }

    private class KnapsackState implements State { ... }
    
}

```

In `successors`, we first retrieve the index of the variable we are considering (since variables can be assigned in a different order in order to reduce the size of the decision diagram) and the remaining capacity contained in the state of the given node. Then, we iterate over each feasible quantity of item `i` that we can still put in the knapsack (from `0` to `knapsackState.capacity / w[i]`) and add the corresponding successor to the list. For each of them, a new state is created with the new remaining capacity and the new value of the knapsack is computed.

```java
public List<Node> successors(Node node, Variable var) {
    int i = var.id;
    KnapsackState knapsackState = (KnapsackState) node.state;
    List<Node> successors = new LinkedList<>();
    
    for (int x = 0; x <= knapsackState.capacity / w[i]; x++) {
        KnapsackState succKnapsackState = new KnapsackState(knapsackState.capacity - x * w[i]);
        double value = node.value() + x * v[i];
        successors.add(node.getSuccessor(succKnapsackState, value, i, x));
    }

    return successors;
}
```

In the method `merge`, we implement a simple valid merging operation : we keep the largest remaining capacity among the nodes to merge. We loop over the nodes to merge, store the largest remaining capacity and keep track of the node with the longest path value. We then replace its capacity by the largest we found and return that node.

```java
public Node merge(Node[] nodes) {
    Node<KnapsackState> best = nodes[0];
    int maxCapacity = 0;
    
    for (Node<KnapsackState> node : nodes) {
        maxCapacity = Math.max(maxCapacity, node.state.capacity);
        
        if (node.value() > best.value()) best = node;
    }
    
    best.state.capacity = maxCapacity;
    return best;
}
```

## Javadoc

You can also refer to the [javadoc](https://vcoppe.github.io/mdd-solver/) for general information about the implementation.

## License
This project is licensed under the GNU General Public License v3.0 - see [LICENSE.md](https://github.com/vcoppe/mdd-solver/blob/master/LICENSE.md) for details.
