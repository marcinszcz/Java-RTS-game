package algorithm;

/**
 * Created by marcin on 07.05.16.
 */

import game.Map;

import java.util.List;

public class AStarNode implements Comparable<AStarNode> {

    public AStarNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    AStarNode pathParent;

    float costFromStart;
    float estimatedCostToGoal;

    public float getCost() {
        return costFromStart + estimatedCostToGoal;
    }


    // zwracamy 1 jesli porownane stringi sa rowne
    public int compareTo(AStarNode other) {
        float otherValue = other.getCost();
        float thisValue = this.getCost();
        float v = thisValue - otherValue;
        return (v > 0) ? 1 : (v < 0) ? -1 : 0;
    }

    public float getCost(AStarNode node) {
        if (x == node.getX() || y == node.getY())
            return 1.0f; // żeby kompilator uważał za floata
        else
            return 1.44f;
    }

    public float getEstimatedCost(AStarNode node) {
        float dx = this.x - node.getX();
        float dy = this.y - node.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public List getNeighbors(Map map) {
        return map.buildNeighborList(x, y);

    }
}
