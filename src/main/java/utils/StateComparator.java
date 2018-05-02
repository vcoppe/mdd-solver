package utils;

import dp.State;

import java.util.Comparator;

public class StateComparator implements Comparator<State> {

    public static final StateComparator COMPARATOR = new StateComparator();

    @Override
    public int compare(State o1, State o2) {
        return Double.compare(o1.value(), o2.value());
    }

}
