package game.objects;

/**
 * Created by marcin on 07.05.16.
 */

import graphic.Animation;

public class Hero extends Unit {

    public Hero(Animation animNormal, Animation animDead) {
        super(animNormal, animDead);
    }

    public int getSpeed() {
        return 50;
    }

    public int getMaxLife() {
        return 200;
    }

    public int getCost() {
        return 1000;
    }

    public int getArmor() {
        return 25;
    }

    public int getFirePower() {
        return 15;
    }
}
