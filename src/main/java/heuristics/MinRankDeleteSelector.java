package heuristics;

import mdd.Layer;
import mdd.Node;

import java.util.Arrays;

/**
 * Deletes the nodes with the least rank values.
 *
 * @author Vianney Coppé
 */
public class MinRankDeleteSelector implements DeleteSelector {

    @Override
    public Node[] select(Layer layer, int number) {
        if (number <= 0) return new Node[0];
        Node[] nodes = new Node[layer.width()];
        layer.nodes().toArray(nodes);

        Arrays.sort(nodes);
        Node[] ret = new Node[number];
        int i = 0;

        for (Node s : nodes) {
            ret[i++] = s;
            if (i == number) break;
        }

        return ret;
    }

}
