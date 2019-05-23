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
     * Returns a {@code Clause} object representing a disjunction with the two variables with indexes {@code u}
     * and {@code v}, and with {@code tu} and {@code tv} an integer containing the value they should take for the
     * corresponding literal to be true.
     *
     * @param u  the index of the first variable
     * @param v  the index of the second variable
     * @param tu the value of variable {@code u} that would make the clause true
     * @param tv the value of variable {@code v} that would make the clause true
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

    /**
     * Gives the truth value of the clause given values for the two variables.
     *
     * @param uValue the value given to variable {@code u}
     * @param vValue the value given to variable {@code v}
     * @return a {@code boolean} containing the truth value of the clause
     */
    public boolean value(int uValue, int vValue) {
        return uValue == tu || vValue == tv;
    }
}
