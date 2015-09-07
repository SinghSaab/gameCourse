package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Administrator on 2015-08-22.
 */
public class Explosion {

    private int x;
    private int y;
    private int width;
    private int height;
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public Explosion(Bitmap res, int x, int y, int w, int h, int numFrames) {
        this.x = x;
        this.y = y;
        this.height = h;
        this.width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;
        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i * height, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(20);
    }

    public void draw(Canvas canvas) {
        if (!animation.playedOnce()) {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }
    }

    public void update() {
        if (!animation.playedOnce()) {
            animation.update();
        }
    }

    public int getHeight() {
        return height;
    }


}
