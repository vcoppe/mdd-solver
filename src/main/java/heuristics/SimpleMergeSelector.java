package heuristics;

import mdd.Layer;
import mdd.Node;

public class SimpleMergeSelector implements MergeSelector {

    public Node[] select(Layer layer, int number) {
        Node[] ret = new Node[number];
        int i = 0;

        for (Node s : layer.nodes()) {
            ret[i++] = s;
            if (i == number) break;
        }

        return ret;
    }

}
