package problems;

/**
 * Representation of a boolean clause for the MAX2SAT problem.
 *
 * @author Vianney Coppé
 */
public class Clause {

    int num;
    // 0 = 00 -> FF
    // 1 = 01 -> FT
    // 2 = 10 -> TF
    // 3 = 11 -> TT
    double w;
    int u, v;
    private int tu, tv;

    /**
     * Returns a {@code Clause} object representing a disjunction with the two variables {@code u} and {@code v},
     * and with {@code tu} and {@code tv} their corresponding truth values :
     * {@code true <==> ((u == tu) || (v == tv))}.
     */
    public Clause(int u, int v, int tu, int tv, double w) {
        if (u > v) {
            int tmp = u;
            u = v;
            v = tmp;

            tmp = tu;
            tu = tv;
            tv = tmp;
        }

        this.u = u;
        this.v = v;
        this.tu = tu;
        this.tv = tv;
        this.w = w;

        this.num = (tu << 1) | tv;
    }

    public boolean value(int uValue, int vValue) {
        return uValue == tu || vValue == tv;
    }
}
