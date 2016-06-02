package algorithm;

import game.Map;

import java.util.List;
import java.util.LinkedList;

/**
 * Created by marcin on 07.05.16.
 */
public class AStarSearch {

    // powyzej wartosci 1000 nie będzie nam szukało ścieżki (wyswietli sie stosowny komunikat)
    public static final int MAX_SEARCH_COUNT = 1000;

    public static class PriorityList<T extends Comparable<T>> extends
            LinkedList<T> {

        private static final long serialVersionUID = 3527382178368866145L;

        public boolean add(T object) {
            for (int i = 0; i < size(); i++) {
                if (object.compareTo(get(i)) <= 0) {
                    add(i, object);
                    return true;
                }
            }
            addLast(object);
            return true;
        }
    }


    private static LinkedList<AStarNode> constructPath(AStarNode node) {
        LinkedList<AStarNode> path = new LinkedList<AStarNode>();
        while (node.pathParent != null) {
            path.addFirst(node);
            node = node.pathParent;
        }
        return path;
    }

    public static LinkedList<AStarNode> findPath(AStarNode startNode,
                                                 AStarNode goalNode, Map map) {
        int count = 0;
        if (startNode == null || goalNode == null) {
            return new LinkedList<AStarNode>();
        }

        PriorityList<AStarNode> openList = new PriorityList<AStarNode>();
        LinkedList<AStarNode> closedList = new LinkedList<AStarNode>();

        startNode.costFromStart = 0;
        startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
        startNode.pathParent = null;
        openList.add(startNode);

        while (!openList.isEmpty()) {
            AStarNode node = openList.removeFirst();
            if (node == goalNode) {
                // zwracam sciezke od startu do mety
                return constructPath(node);
            }

			/* if ( */++count/* > MAX_SEARCH_COUNT) return new LinkedList() */;

            List neighbors = node.getNeighbors(map);
            for (int i = 0; i < neighbors.size(); i++) {
                AStarNode neighborNode = (AStarNode) neighbors.get(i);
                boolean isOpen = openList.contains(neighborNode);
                boolean isClosed = closedList.contains(neighborNode);
                float costFromStart = node.costFromStart
                        + node.getCost(neighborNode);

                // sprawdzam czy wezel nie byl juz sprawdzony wczesniej
                if ((!isOpen && !isClosed)
                        || costFromStart < neighborNode.costFromStart) {
                    neighborNode.pathParent = node;
                    neighborNode.costFromStart = costFromStart;
                    neighborNode.estimatedCostToGoal = neighborNode
                            .getEstimatedCost(goalNode);
                    if (isClosed) {
                        closedList.remove(neighborNode);
                    }
                    if (!isOpen) {
                        openList.add(neighborNode);
                    }
                }
            }
            closedList.add(node);
        }
        System.out.println("Nie znalazlem sciezki:" + count);
        // nie znaleziono sciezki
        return new LinkedList<AStarNode>();
    }
}
