package com.sots.routing.router;

import com.sots.LogisticsPipes2;
import com.sots.routing.LogisticsRoute;
import com.sots.routing.NetworkNode;
import com.sots.routing.WeightedNetworkNode;
import com.sots.util.data.Tuple;
import net.minecraft.util.EnumFacing;

import java.util.*;
import java.util.concurrent.*;

public class MultiCachedDijkstraRouter {
    private static final int NUM_THREADS = 9;
    protected volatile Map<UUID, WeightedNetworkNode> junctions;
    protected volatile Map<UUID, Tuple<NetworkNode, EnumFacing>> destinations;
    protected volatile Map<UUID, NetworkNode> nodes;
    protected volatile Set<UUID> sources = new HashSet<UUID>();
    //WeightedNetworkNode start, target;
    //private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    private Queue<WeightedNetworkNode> unvisited = new LinkedBlockingQueue<WeightedNetworkNode>();
    private Queue<WeightedNetworkNode> visited = new LinkedBlockingQueue<WeightedNetworkNode>();
    //private volatile Map<Tuple<NetworkNode, NetworkNode>, Triple<NetworkNode, NetworkNode, Deque<Tuple<UUID, EnumFacing>>>> cache = new HashMap<Tuple<NetworkNode, NetworkNode>, Triple<NetworkNode, NetworkNode, Deque<Tuple<UUID, EnumFacing>>>>();
    private volatile Set<LogisticsRoute> cache = new HashSet<LogisticsRoute>();

    public MultiCachedDijkstraRouter(Map<UUID, WeightedNetworkNode> junctions, Map<UUID, Tuple<NetworkNode, EnumFacing>> destinations, Map<UUID, NetworkNode> nodes) {
        this.junctions = junctions; //I am not sure if these two Maps will be kept updated with each other
        this.destinations = destinations;
        this.nodes = nodes;
    }

    /**
     * The first part of the output is a boolean, which is false if the route has not yet been calculated, and is true when the route has been calculated
     * The second part of the output is a triple consisting of the start node, the target node and the route from the start node to the target node
     */
    public LogisticsRoute route(NetworkNode s, NetworkNode t) {

        while (sources.contains(s.getId())) {
        }

        try {
            if (!cache.isEmpty()) {
                LogisticsRoute cachedRoute = cache.stream()
                        .filter(p -> p.isRouteFor(s.getId(), t.getId()) && p.isComplete() && p.getWeight() != 0)
                        .findFirst().get();
                if (cachedRoute != null) {
                    LogisticsPipes2.logger.info("Getting route from cache");
                    return cachedRoute;
                }
            }
        } catch (Exception e) {
            //Discard Exception, No route for this in cache or broken cache
        }
		
		/*if (cache.containsKey(input)) {
			LogisticsPipes2.logger.info("Getting route from cache");
			return new Tuple<Boolean, Triple<NetworkNode, NetworkNode, Deque<EnumFacing>>>(true, new Triple<NetworkNode, NetworkNode, Deque<EnumFacing>>(s, t, cache.get(input)));
		}*/

        LogisticsPipes2.logger.info("Calculating new route");
        LogisticsRoute result = doActualRouting(s, t);
        return result;
    }

    public LogisticsRoute doActualRouting(NetworkNode s, NetworkNode t) {
        NetworkNode start;
        WeightedNetworkNode target;

        Map<UUID, WeightedNetworkNode> junctions = new HashMap<UUID, WeightedNetworkNode>(this.junctions);
        Map<UUID, Tuple<NetworkNode, EnumFacing>> destinations = new HashMap<UUID, Tuple<NetworkNode, EnumFacing>>(this.destinations);
        Map<UUID, NetworkNode> nodes = new HashMap<UUID, NetworkNode>(this.nodes);

        if (junctions.containsKey(s.getId())) {
            start = junctions.get(s.getId());
            //LogisticsPipes2.logger.info("Routing from a junction");
        } else {
            //LogisticsPipes2.logger.info("You tried routing from a node, which was not a destination or junction.");
            //return null;
            //LogisticsPipes2.logger.info("Routing from a non-junction");
            start = nodes.get(s.getId());
        }

        if (junctions.containsKey(t.getId())) {
            target = junctions.get(t.getId());
        } else {
            LogisticsPipes2.logger.info("You tried routing to a node, which was not a destination or junction.");
            return null;
        }

        sources.add(s.getId());

        LogisticsRoute result = new LogisticsRoute(start, target);

        FutureTask<Void> routingTask =
                new FutureTask<Void>(
                        new Callable<Void>() {
                            @Override
                            public Void call()
                                    throws Exception {
                                if (!(start instanceof WeightedNetworkNode)) {
                                    start.p_cost = 0;
                                    start.parent = null;
                                    Queue<NetworkNode> unvisitedNonWeighted = new LinkedBlockingQueue<NetworkNode>();
                                    Queue<NetworkNode> visitedNonWeighted = new LinkedBlockingQueue<NetworkNode>();
                                    unvisitedNonWeighted.add(start);
                                    while (!unvisitedNonWeighted.isEmpty()) {
                                        NetworkNode current = unvisitedNonWeighted.poll();
                                        current.getMember().spawnParticle(0f, 1.000f, 0f);
                                        for (int i = 0; i < 6; i++) {
                                            NetworkNode neighbor = current.getNeighborAt(i);
                                            if (neighbor == null) {
                                                continue;
                                            }
                                            if (junctions.containsKey(neighbor.getId())) {
                                                WeightedNetworkNode neighborW = junctions.get(neighbor.getId());
                                                neighborW.p_cost = current.p_cost + 1;
                                                neighborW.parent = new Tuple<NetworkNode, EnumFacing>(current, EnumFacing.getFront(i));
                                                unvisited.add(neighborW);
                                            } else {
                                                if (!(unvisitedNonWeighted.contains(neighbor) || visitedNonWeighted.contains(neighbor))) {
                                                    neighbor.p_cost = current.p_cost + 1;
                                                    neighbor.parent = new Tuple<NetworkNode, EnumFacing>(current, EnumFacing.getFront(i));
                                                    unvisitedNonWeighted.add(neighbor);
                                                }
                                            }
                                        }
                                        visitedNonWeighted.add(current);
                                    }
                                } else {
                                    start.p_cost = 0;
                                    start.parent = null;
                                    unvisited.add((WeightedNetworkNode) start);
                                }
                                while (!unvisited.isEmpty()) {
                                    WeightedNetworkNode current = unvisited.poll();
                                    current.getMember().spawnParticle(0f, 1.000f, 0f);
                                    //Thread.sleep(120);

                                    for (int i = 0; i < 6; i++) {
                                        Tuple<WeightedNetworkNode, Integer> neighborT = current.weightedNeighbors[i];
                                        if (neighborT == null) {
                                            continue;
                                        }

                                        WeightedNetworkNode neighbor = neighborT.getKey();
                                        int distance = neighborT.getVal();
                                        if (!(unvisited.contains(neighbor) || visited.contains(neighbor))) {
                                            neighbor.p_cost = Integer.MAX_VALUE;
                                            unvisited.add(neighbor);
                                            neighbor.getMember().spawnParticle(0.502f, 0.000f, 0.502f);
                                        }
                                        if (current.p_cost + distance < neighbor.p_cost) {
                                            neighbor.p_cost = current.p_cost + distance;
                                            neighbor.parent = new Tuple<NetworkNode, EnumFacing>(current, EnumFacing.getFront(i));
                                        }
                                    }

                                    visited.add(current);
                                    //Thread.sleep(120);
                                }

                                for (Tuple<NetworkNode, EnumFacing> n : destinations.values()) {

                                    NetworkNode help = junctions.get(n.getKey().getId());

                                    Deque<EnumFacing> route = new ArrayDeque<EnumFacing>();
                                    route.push(n.getVal());
                                    while (help.parent != null) {
                                        pushToRouteUntillParent(help, route);

                                        help.getMember().spawnParticle(1.0f, 0.549f, 0.0f);
                                        help = help.parent.getKey();
                                    }
                                    Deque<EnumFacing> tmp_result = route;
                                    cache.add(new LogisticsRoute(start, n.getKey(), tmp_result));
                                    //LogisticsPipes2.logger.info("Route found of length " + route.size());
                                }

                                result.setDirectionStack(cache.stream()
                                        .filter(p -> p.isRouteFor(start.getId(), target.getId()))
                                        .findFirst().get().getdirectionStack());
                                result.weightFromStack();
                                result.setCompletion(true);
                                cache.add(result);
                                LogisticsPipes2.logger.info("Done routing for now " + start + "-" + target);
                                sources.remove(start.getId());
                                return null;
                            }

                        });
        executor.execute(routingTask);

        return result;
    }

    private void pushToRouteUntillParent(NetworkNode current, Deque<EnumFacing> route) {
        NetworkNode parent = current.parent.getKey();
        EnumFacing direction = current.parent.getVal();
        int parentDirection = direction.getOpposite().getIndex();

        NetworkNode help = current;
        while (help.getId() != parent.getId()) {
            help = help.getNeighborAt(parentDirection);
            route.push(direction);
            help.getMember().spawnParticle(1.0f, 0.0f, 0.0f);
            //Thread.sleep(120);
            //LogisticsPipes2.logger.info(route.size());
        }
    }

    public void clean() {
        unvisited.clear();
        visited.clear();
        //executor = Executors.newSingleThreadExecutor();
        executor = Executors.newFixedThreadPool(NUM_THREADS);
    }

    public void shutdown() {
        executor.shutdownNow();
        //executor = Executors.newSingleThreadExecutor();
        executor = Executors.newFixedThreadPool(NUM_THREADS);
        cache.clear();
    }

    public void clearCache() {
        cache.clear();
    }

}
