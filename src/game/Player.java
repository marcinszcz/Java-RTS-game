package game;

/**
 * Created by marcin on 07.05.16.
 */

import game.objects.Unit;
import java.util.Iterator;
import java.util.LinkedList;

public class Player {

    private int no;
    private LinkedList<Unit> units;
    private LinkedList<Unit> selectedUnits;
    private boolean selectionVisable;
    private int selectionX1;
    private int selectionY1;
    private int selectionX2;
    private int selectionY2;
    public Game game;

    public Player(Game game) {
        units = new LinkedList<Unit>();
        selectedUnits = new LinkedList<Unit>();
        selectionVisable = false;
        this.game = game;
    }

    public int getPlayerNo() {
        return no;
    }

    public int getOpponentNo() {
        if (this == game.player) {
            return game.opponent.getPlayerNo();
        } else {
            return game.player.getPlayerNo();
        }
    }

    public void setPlayerNo(int no) {
        this.no = no;
    }

    // dodaje obiekt spite do mapy
    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public void removeUnit(Unit unit) {
        units.remove(unit);
    }

    public Iterator<Unit> getUnits() {
        return units.iterator();
    }

    public void setUnits(LinkedList<Unit> units) {
        this.units = units;
    }

    public void startSelection() {
        selectionVisable = true;
    }

    public void stopSelection() {
        selectionVisable = false;
        selectUnits(getSelectionBeginX(), getSelectionBeginY(),
                getSelectionEndX(), getSelectionEndY());

    }

    public void setSelectionBegin(int x, int y) {
        selectionX1 = x;
        selectionY1 = y;
    }

    public void setSelectionEnd(int x, int y) {
        selectionX2 = x;
        selectionY2 = y;
    }

    public boolean isSelectionVisable() {
        return selectionVisable;
    }

    public int getSelectionBeginX() {
        return (selectionX1 < selectionX2) ? selectionX1 : selectionX2;
    }

    public int getSelectionBeginY() {
        return (selectionY1 < selectionY2) ? selectionY1 : selectionY2;
    }

    public int getSelectionEndX() {
        return (selectionX1 > selectionX2) ? selectionX1 : selectionX2;
    }

    public int getSelectionEndY() {
        return (selectionY1 > selectionY2) ? selectionY1 : selectionY2;
    }

    public LinkedList<Unit> getSelectedUnits() {
        return selectedUnits;
    }

    public Unit getUnit(int unitNo) {
        Iterator<Unit> i = getUnits();
        while (i.hasNext()) {
            Unit unit = i.next();
            if (unit.getNo() == unitNo) {
                return unit;
            }
        }
        return null;
    }

    public void selectUnits(int x1, int y1, int x2, int y2) {
        selectedUnits = new LinkedList<Unit>();

        Iterator<Unit> i = getUnits();
        while (i.hasNext()) {
            Unit unit = i.next();

            if (unit.isAlive()) {
                int u1 = Map.tilesToPixels(unit.getX())
                        + Math.round(unit.getDisplacementX());
                int v1 = Map.tilesToPixels(unit.getY())
                        + Math.round(unit.getDisplacementY());
                int u2 = u1 + unit.getWidth();
                int v2 = v1 + unit.getHeight();

                if (u1 <= x2 && u2 >= x1 && v1 <= y2 && v2 >= y1) {
                    unit.select(true);
                    selectedUnits.add(unit);
                } else {
                    unit.select(false);
                }
            }
        }
    }

    public boolean isAlive() {
        return !units.isEmpty();
    }

    public void update(long elapsedTime) {

    }
}
