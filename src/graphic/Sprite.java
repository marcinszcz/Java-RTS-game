package graphic;

/**
 * Created by marcin on 07.05.16.
 */

import java.awt.Image;

public class Sprite {

    protected Animation anim;

    // pozycja - w ktorym kafelku sie znajduje
    private int x;

    private int y;

    // polozenie wzgledem kafelka (w pikselach)
    private float dx;

    private float dy;

    public Sprite(Animation anim) {
        this.anim = anim;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    // zwraca szerokosc obiektu Sprite
    public int getWidth() {
        return anim.getImage().getWidth(null);
    }

    public int getHeight() {
        return anim.getImage().getHeight(null);
    }

    public float getDisplacementX() {
        return dx;
    }

    // zwraca predkosc w pionie
    public float getDisplacementY() {
        return dy;
    }

    public void setDisplacementX(float dx) {
        this.dx = dx;
    }

    public void setDisplacementY(float dy) {
        this.dy = dy;
    }

    public long getAnimationTime() {
        return anim.getAnimationTime();
    }

    public Image getImage() {
        return anim.getImage();
    }

    public void update(long elapsedTime) {
        anim.update(elapsedTime);
    }

   // powiela bierzący obiekt Sprite
    public Object clone() {
        return new Sprite(anim);
    }
}
