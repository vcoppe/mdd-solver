package heuristics;

import dp.Layer;

public interface StateSelector {
	
	Layer select(Layer layer, int width);
	
}
