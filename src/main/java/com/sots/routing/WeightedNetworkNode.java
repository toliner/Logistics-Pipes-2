package com.sots.routing;

import com.sots.routing.interfaces.IRoutable;
import com.sots.util.data.Tuple;

import java.util.UUID;

public class WeightedNetworkNode extends NetworkNode {

    public Tuple<WeightedNetworkNode, Integer>[] weightedNeighbors = (Tuple<WeightedNetworkNode, Integer>[]) new Tuple[6]; //Apparently one cannot create arrays of generic types directly


    public WeightedNetworkNode(UUID id, IRoutable pipe) {
        super(id, pipe);
        p_cost = Integer.MAX_VALUE;
    }
}

