package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Administrator on 2015-09-07.
 */
public class BotBorder extends gameObject {
    private Bitmap image;

    public BotBorder(Bitmap res, int x, int y) {
//        height = 200;
        height = 1;
        width = 20;

        this.x = x;
        this.y = y;

        dx = gamePanel.MOVESPEED;
        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    public void update() {
        x += dx;
    }

    public void draw(Canvas canvas) {

        canvas.drawBitmap(image, x, y, null);
    }

}
