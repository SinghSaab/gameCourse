package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Administrator on 2015-08-16.
 */
public class Player extends gameObject {
    private Bitmap spritesheet;
    private int score;
    private double dya;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    public Player(Bitmap res, int w, int h, int numFrames) {
        x = 0;
        y = gamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;
        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(40);
        startTime = System.nanoTime();
    }

    //    This is what will cause the player to move when screen is touched
    public void setUp(boolean b) {
        up = b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

//      Unless homescreen is off, player will be stayed at the middle of screen
        if (!gamePanel.homescreen) {
//          This will test that player doesn't cross the top of screen
//          Although if the acceleration is enough, the player can cross the screen as first condition can be skipped
            if (up && y > 5) {
                dy = (int) (dya -= 0.30);   //ideal is 0.13
//            The acceleration with which the player will do up

            } else if (!up && y < (gamePanel.HEIGHT - 125)) {
                dy = (int) (dya += 0.15);
//            The acceleration with which the player will do down
            } else {
                dy = 0;
                dya = 0;
            }

            if (dy > 8) dy = 8;
            if (dy < -8) dy = -8;

            y += dy * 3;
            dy = 0;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public int getScore() {
        return score;
    }

    public boolean getPlaying() {
        return playing;
    }

    public void setPlaying(boolean b) {
        playing = b;
    }

    public void resetDYA() {
        dy = 0;
    }

    public void resetScore() {
        score = 0;
    }

}
