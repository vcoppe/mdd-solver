package heuristics;

import mdd.Layer;
import mdd.State;

import java.util.Arrays;

public class MinLPDeleteSelector implements DeleteSelector {

    @Override
    public State[] select(Layer layer, int number) {
        State[] states = new State[layer.width()];
        layer.states().toArray(states);

        Arrays.sort(states);
        State[] ret = new State[number];
        int i = 0;

        for (State s : states) {
            ret[i++] = s;
            if (i == number) break;
        }

        return ret;
    }

}
