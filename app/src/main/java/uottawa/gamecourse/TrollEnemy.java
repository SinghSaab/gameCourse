package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by Administrator on 2015-10-28.
 */
public class TrollEnemy extends gameObject {

    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;

    public TrollEnemy(Bitmap res, int x, int y, int w, int h, int s, int numFrames) {
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        speed = -gamePanel.MOVESPEED + (int) (rand.nextDouble() * score / 30);

//        cap missile speed
        if (speed >= 40 - gamePanel.MOVESPEED) speed = 40 - gamePanel.MOVESPEED;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i * height, width, height);
        }
        animation.setFrames(image);
        animation.setDelay(60 - speed);

    }

    public void update() {
        x -= speed;
        if (y <= 0) {
            ++y;
        } else if (y > gamePanel.HEIGHT - 127) {
            --y;
        } else {
            y += 1;
        }
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        } catch (Exception e) {
        }
    }

    @Override

    public int getWidth() {
        return width;
    }
}
