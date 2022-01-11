package com.crossroadsinn.search;

import com.crossroadsinn.problem.SquadComposition;
import javafx.concurrent.Task;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Task implementation of the GreedyBestFirstSearch algorithm.
 * Allows for concurrency when running the solver.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class AutoAssignTraineeTask extends Task<SquadComposition> {
    PriorityQueue<SquadComposition> prioQueue;
    int nodes;
    int maxDurationInMillis = 60*1000;

    public AutoAssignTraineeTask(SquadComposition initialState) {
        init(initialState);
    }

    /**
     * Reset the search object with a new initial state.
     * @param initialState the new initial state.
     */
    public void init(SquadComposition initialState) {
        prioQueue = new PriorityQueue<>(50, Comparator.comparingInt(SquadComposition::heuristic));
        nodes = 0;
        if ( prioQueue.isEmpty() ) prioQueue.add(initialState);
    }

    @Override
    protected SquadComposition call() {
        return solve();
    }

    /**
     * Look for solution by prioritizing lowest heuristics.
     * @return the solution if found, null otherwise.
     */
    public SquadComposition solve() {
        long startTime = System.currentTimeMillis();
        while (!prioQueue.isEmpty()) {
            if (isCancelled()) return null;
            if (System.currentTimeMillis() > startTime + maxDurationInMillis) return null;
            if (prioQueue.peek().isSolution()) {
                return prioQueue.peek();
            }
            ++nodes;
            List<SquadComposition> expandedNodes = prioQueue.poll().getChildren();
            prioQueue.addAll(expandedNodes);
        }
        // No solution found.
        return null;
    }

    public int getNodes() {
        return nodes;
    }
}
