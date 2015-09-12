package uottawa.gamecourse;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Administrator on 2015-09-07.
 */
public class TopBorder extends gameObject {
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h) {
//        height = h;
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
        try {
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e) {
        }
        ;
    }

}
