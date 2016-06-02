package game.objects;

/**
 * Created by marcin on 07.05.16.
 */

import graphic.Animation;

public class NormalWarrior extends Unit {

    public NormalWarrior(Animation animNormal, Animation animDead) {
        super(animNormal, animDead);
    }

    public int getSpeed() {
        return 20;
    }

    public int getMaxLife() {
        return 50;
    }

    public int getCost() {
        return 100;
    }

    public int getArmor() {
        return 10;
    }

    public int getFirePower() {
        return 10;
    }
}
