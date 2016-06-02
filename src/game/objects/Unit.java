package game.objects;

/**
 * Created by marcin on 07.05.16.
 */

import java.awt.Point;
import java.lang.reflect.Constructor;
import java.util.LinkedList;

import algorithm.*;
import network.GameEvent;
import graphic.*;
import game.Map;
import game.Player;

public abstract class Unit extends Sprite {

    protected static final int DIE_TIME = 1000;
    protected static final int HIT_TIME = 1000;
    private Animation animNormal;
    private Animation animDead;
    private UnitState state;
    private long stateTime;
    private int rotate;
    private boolean selected;
    private int rotateTime;
    private int goingTime;
    private int isGoingTo;
    private int isRotateTo;
    private int unitNo;
    private boolean isPlayerUnit;
    private boolean isSent;
    public Player owner;
    private int life;
    private Point currentGoTo;
    private Point goTo;
    private Point reservedPoint;
    private LinkedList<ListElement> listGoTo;
    private LinkedList plannedMoves;
    private Unit attackedUnit;
    private int timeFromLastHit;

    // tworzenie obiektu z okreslonymi atrybutami Animation
    public Unit(Animation animNormal, Animation animDead) {
        super(animNormal);
        this.animNormal = animNormal;
        this.animDead = animDead;
        state = UnitState.NORMAL;
        rotate = 0;
        rotateTime = 0;
        goingTime = 0;
        isGoingTo = -1;
        isRotateTo = -1;
        isSent = false;
        currentGoTo = new Point(-1, -1);
        goTo = new Point(-1, -1);
        reservedPoint = new Point(-1, -1);
        listGoTo = new LinkedList<ListElement>();
        plannedMoves = new LinkedList();
        attackedUnit = null;
        timeFromLastHit = 0;
        life = getMaxLife();
        // System.out.println(life);
    }

    public Object clone() {
        Constructor constructor = getClass().getConstructors()[0];
        try {
            return constructor.newInstance(new Object[] {
                    (Animation) animNormal.clone(),
                    (Animation) animDead.clone() });
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public abstract int getSpeed();

    // czas na rotacje
    public int getRotationSpeed() {
        return 100;
    }

    public abstract int getMaxLife();

    public abstract int getCost();

    public abstract int getArmor();

    public abstract int getFirePower();

    public int getLife() {
        return life;
    }

    public float getLifeProportion() {
        float f = (float) getLife() / (float) getMaxLife();
        return f;
    }

    public void hit(int firePower) {
        if (firePower >= life) {
            life = 0;
        } else {
            life -= firePower;
        }
    }
// tutaj zmienilem dla testow
    public void attack(int x, int y, int firePower, int attackingUnitNo) {
        if (getX() == x && getY() == y && isAlive()) {
            	GameEvent ge = new GameEvent(GameEvent.C_HIT, owner.getPlayerNo()
            		+ "|" + unitNo + "|" + firePower + "|" + attackingUnitNo);
            	owner.game.client.sendMessage(ge);
        }
    }

    // zwraca stan obiektu- postaci np.NORMAL
    public UnitState getState() {
        return state;
    }

    public void setState(UnitState state) {
        if (this.state != state) {
            this.state = state;
            stateTime = 0;
            if (state == UnitState.DYING) {
                // setVelocityX(0);
                // setVelocityY(0);
            }
        }
    }

    public boolean isAlive() {
        return (state != UnitState.DYING && state != UnitState.DEAD);
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rot) {
        rotate = rot;
    }

    public void select(boolean state) {
        selected = state;
    }

    public boolean isSelected() {
        return selected;
    }

    public void beginAttack(Unit u) {
        if (Math.abs(getX() - u.getX()) > 1 || Math.abs(getY() - u.getY()) > 1
                || getState() == UnitState.MOVING) {
            goTo(u.getX(), u.getY());
        }
        attackedUnit = u;
    }

    public void rotateTo(int X, int Y) {
        if (X > getX()) {
            if (Y > getY())
                isRotateTo = 3;
            else if (Y < getY())
                isRotateTo = 1;
            else
                isRotateTo = 2;
        } else if (X < getX()) {
            if (Y > getY())
                isRotateTo = 5;
            else if (Y < getY())
                isRotateTo = 7;
            else
                isRotateTo = 6;
        } else {
            if (Y > getY())
                isRotateTo = 4;
            else if (Y < getY())
                isRotateTo = 0;
            else
                isRotateTo = -1;
        }
    }

    public synchronized void goTo(int x, int y) {
        plannedMoves = new LinkedList();
        goTo = new Point(x, y);
        attackedUnit = null;
    }

    public void failGoTo() {
        plannedMoves = new LinkedList();
        isSent = false;
    }

    private class ListElement {
        ListElement(int isGoingTo) {
            is_GoingTo = isGoingTo;
        }

        public Point getCurrentGoTo() {
            Point currGoTo = new Point(-1, -1);
            switch (is_GoingTo) {
                case 0:
                    currGoTo = new Point(getX(), getY() - 1);
                    break;
                case 1:
                    currGoTo = new Point(getX() + 1, getY() - 1);
                    break;
                case 2:
                    currGoTo = new Point(getX() + 1, getY());
                    break;
                case 3:
                    currGoTo = new Point(getX() + 1, getY() + 1);
                    break;
                case 4:
                    currGoTo = new Point(getX(), getY() + 1);
                    break;
                case 5:
                    currGoTo = new Point(getX() - 1, getY() + 1);
                    break;
                case 6:
                    currGoTo = new Point(getX() - 1, getY());
                    break;
                case 7:
                    currGoTo = new Point(getX() - 1, getY() - 1);
                    break;
            }
            return currGoTo;
        }

        public int getIsGoingTo() {
            return is_GoingTo;
        }

        private int is_GoingTo;
    }

    public synchronized void goTo(int r) {
        listGoTo.add(new ListElement(r));
        isSent = false;
    }

    public int getNo() {
        return unitNo;
    }

    public void setNo(int no) {
        unitNo = no;
    }

    public void setPlayerUnit(boolean b) {
        isPlayerUnit = b;
    }

    public boolean isPlayerUnit() {
        return isPlayerUnit;
    }

    // aktualna pozycja potora
    public synchronized void update(long elapsedTime, Map map) {

        if (isPlayerUnit) {
            map.visit(getX(), getY());
        }

        if (attackedUnit != null) {
            timeFromLastHit += elapsedTime;
        }

        if (getState() == UnitState.NORMAL && isRotateTo != -1) {
            if (isRotateTo != rotate) {
                rotateTime += elapsedTime;
                while (rotateTime > getRotationSpeed()) {
                    rotateTime -= getRotationSpeed();

                    if ((isRotateTo - rotate + 8) % 8 <= 4) {
                        setRotate((getRotate() + 1) % 8);
                    } else {
                        setRotate((getRotate() + 7) % 8);
                    }
                }
            } else {
                isRotateTo = -1;
            }
        }

        if (getState() == UnitState.NORMAL && !listGoTo.isEmpty()) {
            ListElement listelement = (ListElement) listGoTo.getFirst();
            listGoTo.removeFirst();
            isGoingTo = listelement.getIsGoingTo();
            currentGoTo = listelement.getCurrentGoTo();
            map.setFree(currentGoTo.x, currentGoTo.y, false);
            reservedPoint = currentGoTo;
            map.setFree(getX(), getY(), true);
            setState(UnitState.MOVING);
            // System.out.println("x=" + getX() + ", y=" + getY());
        }

        if (getState() == UnitState.NORMAL && !isSent) {
            if (plannedMoves.isEmpty()) {
                if (goTo.x != -1 && goTo.y != -1) {

                    boolean newPoint = false;
                    boolean exit = false;

                    int count = 0;

                    while (!exit || newPoint) {
                        if (++count > 5) {
                            goTo = new Point(-1, -1);
                            System.out.println("Break");
                            break;
                        }

                        if (newPoint) {
                            newPoint = false;
                            AStarNode node = map.getCloseNode(goTo.x, goTo.y);
                            if (node != null) {
                                goTo = new Point(node.getX(), node.getY());
                            } else {
                                goTo = new Point(-1, -1);
                                exit = true;
                            }
                        }

                        if (map.isFree(goTo.x, goTo.y)) {
                            AStarNode startNode = map.getNode(getX(), getY());
                            AStarNode endNode = map.getNode(goTo.x, goTo.y);
                            plannedMoves = AStarSearch.findPath(startNode,
                                    endNode, map);
                            if (plannedMoves.isEmpty()) {
                                newPoint = true;
                            } else {
                                exit = true;
                            }
                        } else if (!exit) {
                            newPoint = true;
                        }
                    }
                } else {
                    if (attackedUnit != null && attackedUnit.isAlive()) {
                        if (Math.abs(getX() - attackedUnit.getX()) <= 1
                                && Math.abs(getY() - attackedUnit.getY()) <= 1) {

                            if (timeFromLastHit > HIT_TIME) {

                                int firePower = getFirePower();

                                GameEvent ge = new GameEvent(
                                        GameEvent.C_ATTACK, owner
                                        .getOpponentNo()
                                        + "|"
                                        + attackedUnit.unitNo
                                        + "|"
                                        + firePower
                                        + "|"
                                        + attackedUnit.getX()
                                        + "|"
                                        + attackedUnit.getY()
                                        + "|"
                                        + getNo());
                                owner.game.client.sendMessage(ge);

                                timeFromLastHit = 0;
                            }
                        } else {
                            goTo = new Point(attackedUnit.getX(), attackedUnit
                                    .getY());
                        }
                    }
                }
            } else {
                AStarNode node = (AStarNode) plannedMoves.getFirst();
                plannedMoves.remove();

                Point nowGoTo = new Point(node.getX(), node.getY());

                if (map.isFree(nowGoTo.x, nowGoTo.y)) {
                    int nowGoingTo;

                    if (nowGoTo.x > getX()) {
                        if (nowGoTo.y > getY())
                            nowGoingTo = 3;
                        else if (nowGoTo.y < getY())
                            nowGoingTo = 1;
                        else
                            nowGoingTo = 2;
                    } else if (nowGoTo.x < getX()) {
                        if (nowGoTo.y > getY())
                            nowGoingTo = 5;
                        else if (nowGoTo.y < getY())
                            nowGoingTo = 7;
                        else
                            nowGoingTo = 6;
                    } else {
                        if (nowGoTo.y > getY())
                            nowGoingTo = 4;
                        else if (nowGoTo.y < getY())
                            nowGoingTo = 0;
                        else
                            nowGoingTo = -1;
                    }

                    GameEvent ge = new GameEvent(GameEvent.C_MOVE, owner
                            .getPlayerNo()
                            + "|"
                            + unitNo
                            + "|"
                            + nowGoingTo
                            + "|"
                            + getX()
                            + "|" + getY());
                    owner.game.client.sendMessage(ge);
                    isSent = true;
                }
            }
        }

        if (getState() == UnitState.MOVING) {
            if (isGoingTo != rotate) {
                rotateTime += elapsedTime;
                while (rotateTime > getRotationSpeed()) {
                    rotateTime -= getRotationSpeed();

                    if ((isGoingTo - rotate + 8) % 8 <= 4) {
                        setRotate((getRotate() + 1) % 8);
                    } else {
                        setRotate((getRotate() + 7) % 8);
                    }
                }
            } else {
                isRotateTo = rotate;
                goingTime += elapsedTime;
                while (goingTime > getSpeed()) {
                    goingTime -= getSpeed();

                    switch (isGoingTo) {
                        case 0:
                            setDisplacementY(getDisplacementY() - 1);
                            break;
                        case 1:
                            setDisplacementX(getDisplacementX() + 1);
                            setDisplacementY(getDisplacementY() - 1);
                            break;
                        case 2:
                            setDisplacementX(getDisplacementX() + 1);
                            break;
                        case 3:
                            setDisplacementX(getDisplacementX() + 1);
                            setDisplacementY(getDisplacementY() + 1);
                            break;
                        case 4:
                            setDisplacementY(getDisplacementY() + 1);
                            break;
                        case 5:
                            setDisplacementX(getDisplacementX() - 1);
                            setDisplacementY(getDisplacementY() + 1);
                            break;
                        case 6:
                            setDisplacementX(getDisplacementX() - 1);
                            break;
                        case 7:
                            setDisplacementX(getDisplacementX() - 1);
                            setDisplacementY(getDisplacementY() - 1);
                            break;
                    }

                    boolean nowExit = false;

                    if (getDisplacementX() >= Map.TILE_SIZE) {
                        // setDisplacementX(getDisplacementX()-Map.TILE_SIZE);
                        setDisplacementX(0);
                        setX(getX() + 1);
                        nowExit = true;
                    }
                    if (getDisplacementX() <= -Map.TILE_SIZE) {
                        // setDisplacementX(getDisplacementX()+Map.TILE_SIZE);
                        setDisplacementX(0);
                        setX(getX() - 1);
                        nowExit = true;
                    }
                    if (getDisplacementY() >= Map.TILE_SIZE) {
                        // setDisplacementY(getDisplacementY()-Map.TILE_SIZE);
                        setDisplacementY(0);
                        setY(getY() + 1);
                        nowExit = true;
                    }
                    if (getDisplacementY() <= -Map.TILE_SIZE) {
                        // setDisplacementY(getDisplacementY()+Map.TILE_SIZE);
                        setDisplacementY(0);
                        setY(getY() - 1);
                        nowExit = true;
                    }

                    if (nowExit) {
                        if (goTo.x == currentGoTo.x && goTo.y == currentGoTo.y) {
                            goTo = new Point(-1, -1);
                        }
                        currentGoTo = new Point(-1, -1);
                        isGoingTo = -1;
                        setState(UnitState.NORMAL);
                        break;
                    }
                }
            }
        }

        if (life == 0 && getState() != UnitState.DYING && getState() != UnitState.DEAD) {
            setState(UnitState.DYING);
        }

        Animation newAnim = anim;
        if (getState() == UnitState.NORMAL || getState() == UnitState.MOVING) {
            //		newAnim = animNormal;
        } else if (getState() == UnitState.DYING) {
            //		newAnim = animDead;
        }

        if (anim != newAnim) {
            //	anim = newAnim;
            //	anim.start();
        } else {
            //	anim.update(elapsedTime);
        }

        stateTime += elapsedTime;
        if (getState() == UnitState.DYING && stateTime >= DIE_TIME) {
            setState(UnitState.DEAD);
            if (isPlayerUnit()) {
                GameEvent ge = new GameEvent(GameEvent.C_DEAD, owner
                        .getPlayerNo()
                        + "|" + unitNo);
                owner.game.client.sendMessage(ge);
            }
        }
    }

    public synchronized void relaseLand(Map map) {
        if (reservedPoint.x != -1 && reservedPoint.y != -1) {
            map.setFree(reservedPoint.x, reservedPoint.y, true);
        } else {
            map.setFree(getX(), getY(), true);
        }
    }
}
