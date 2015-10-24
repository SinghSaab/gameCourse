package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by Administrator on 2015-08-15.
 */
public class Background {

    private Bitmap imageMoving;
    private Bitmap imageAtRest;
    private int x, y, dx;

    public Background(Bitmap res, Bitmap resAtRest) {
        imageAtRest = resAtRest;
        imageMoving = res;
        dx = gamePanel.MOVESPEED;
    }

    public void update() {
        x += dx;
        if (x < -gamePanel.WIDTH)
            x = 0;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(imageAtRest, 0, 0, null);
        canvas.drawBitmap(imageMoving, x, y, null);
        if (x < 0) {
            canvas.drawBitmap(imageMoving, x + gamePanel.WIDTH, y, null);
        }
    }

}
