package problems;

public class Vertex implements Comparable<Vertex> {

    int degree;
    int weight;
    int index;

    public Vertex(int d, int w, int i) {
        this.degree = d;
        this.weight = w;
        this.index = i;
    }

    public int compareTo(Vertex other) {
        if (this.degree == other.degree)
            return other.weight - this.weight;
        return this.degree - other.degree;
    }
}
