package uottawa.gamecourse;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Administrator on 2015-08-15.
 */

public class gamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 507;
    public static final int HEIGHT = 102;
    public static final int MOVESPEED = -5;
    private long enemyStartTime;
    private long enemyElapsed;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Enemy> enemy;
    private Random rand = new Random();

    public gamePanel(Context context) {
        super(context);

//        add callback to surface holder to intercept events
        getHolder().addCallback(this);

//        Instantiate MainThread
        thread = new MainThread(getHolder(), this);

//        make gamepanel focusable so that it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.mountain_blue));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.player), 129, 67, 2);
        enemy = new ArrayList<Enemy>();
        enemyStartTime = System.nanoTime();
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying()) {
                player.setPlaying(true);
            } else {
                player.setUp(true);
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            bg.update();
            player.update();

            long enemyElapsed = (System.nanoTime() - enemyStartTime) / 1000000;

//            As score goes higher, less delay between enemy launches
            if (enemyElapsed > (2000 - player.getScore() / 4)) {
//                first enemy always goes down the middle
                if (enemy.size() == 0) {
                    enemy.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.flame),
                            WIDTH + 10, HEIGHT / 2, 91, 27, player.getScore(), 8));
                }

//                If first missile if off the screen, start randomizing the location of every other missile
                else {
                    enemy.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.flame),
                            WIDTH + 10, (int) ((rand.nextDouble() * HEIGHT)), 91, 27, player.getScore(), 8));
                }

//                reset timer
                enemyStartTime = System.nanoTime();
            }

//              loop through every missile
            for (int i = 0; i < enemy.size(); i++) {
//                update enemy obj
                enemy.get(i).update();
//                detect collision with player
                if (collision(enemy.get(i), player)) {
                    enemy.remove(i);
                    player.setPlaying(false);
                    break;
                }
//                else if there is no collision and enemy passes through, we'll remove the obj
                if (enemy.get(i).getX() < -100) {
                    enemy.remove(i);
                    break;
                }
            }
        }
    }

    public boolean collision(gameObject a, gameObject b) {

        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {

//      get the width "getWidth()" of the physical device
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f) / 2;

//          For the above method to follow, image size should be smaller than device size
//        else use the value of 1,1 for both
//        IF THE IMAGE SIZE IS SAME AS THAT OF NEXUS4, I CAN EVEN SKIP THE STEPS OF SCALEFACTORX,Y
        if (canvas != null) {
            final int savedState = canvas.save();
//            Still background drawing
            Background bgStill = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.sky_blue_still));
            bgStill.draw(canvas);
//            Moving background drawing
            canvas.scale(scaleFactorX, scaleFactorY, 0, -140);
            bg.draw(canvas);
            player.draw(canvas);

            for (Enemy e : enemy) {
                e.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }

    }


}
