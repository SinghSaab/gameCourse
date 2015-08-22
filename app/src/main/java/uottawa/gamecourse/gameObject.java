package uottawa.gamecourse;

import android.graphics.Rect;

/**
 * Created by Administrator on 2015-08-16.
 */
public abstract class gameObject {
    protected int x, y, dy, dx, width, height;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Rect getRectangle() {
        return new Rect(x,y,x+width, y+height);
    }

}
