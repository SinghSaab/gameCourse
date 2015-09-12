package uottawa.gamecourse;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Administrator on 2015-08-15.
 */

public class gamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 768;
    public static final int MOVESPEED = -5;
    private long enemyStartTime;
    private long enemyElapsed;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Enemy> enemy;
    private int count = 0;   //no of attacks
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private int maxborderheight = 1;
    private int minborderheight;
    private boolean topdown = true;
    private boolean botdown = true;
    private boolean newGameCreated;
    //  increase to slow down difficulty progression
    private int progressDenom = 20;
    //
    private Random rand = new Random();


    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int best;

    public gamePanel(Context context) {
        super(context);

//        add callback to surface holder to intercept events
        getHolder().addCallback(this);


//        make gamepanel focusable so that it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("In surfaceDestroyed()", "");

        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("In surfaceCreated()", "");

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.night_land));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.player), 150, 100, 2);
        enemy = new ArrayList<Enemy>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        enemyStartTime = System.nanoTime();
        //        Instantiate MainThread
        thread = new MainThread(getHolder(), this);

//      We can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("In onTouchEvent()", "");

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.getPlaying()) {
                if (!started) started = true;
                reset = false;
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
        Log.d("In update()", "");

        if (player.getPlaying()) {
            Log.d("In update.getPlaying()", "");


            if (botborder.isEmpty()) {
                Log.d("In botborder.isEmpty()", "");

                player.setPlaying(false);
                return;
            }

            if (topborder.isEmpty()) {
                Log.d("In topborder.isEmpty()", "");

                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

//            Calculate the threshold of height based on the score
//            maxborderheight = 30 + player.getScore() / progressDenom;
            maxborderheight = 1;
//            Calculate max border height so that only 1/2 of screen size could be taken at max by the borders
            if (maxborderheight > HEIGHT / 4) maxborderheight = HEIGHT / 4;
//            minborderheight = 5 + player.getScore() / progressDenom;
            minborderheight = 1;

//            check bottom border collision
            for (int i = 0; i < botborder.size(); i++) {
                if (collision(botborder.get(i), player)) {
                    player.setPlaying(false);     //reset the game
//                    player.setY(HEIGHT - (HEIGHT / 8)); //keep player at the bottom of screen

                }
            }

//            check top border collision
            for (int i = 0; i < topborder.size(); i++) {
                if (collision(topborder.get(i), player))
                    player.setPlaying(false);
            }


//            Update top border
            this.updateTopBorder();

//            Update bottom border
            this.updateBottomBorder();


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
                            WIDTH + 10, (int) ((rand.nextDouble() * (HEIGHT - (maxborderheight * 2)) + maxborderheight)),
                            91, 27, player.getScore(), 8));
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
                    count++;
                    Log.d("Damage", String.valueOf(count));
                    if (count == 3) {    //on 3 collisions with missile, reset the game
                        player.setPlaying(false);
                        count = 0;
                        break;
                    }
                }
//                else if there is no collision and enemy passes through, we'll remove the obj
                if (enemy.get(i).getX() < -100) {
                    enemy.remove(i);
                    break;
                }
            }
        } else {
            Log.d("In update.else()", "");
            player.resetDYA();

            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.collide),
                        player.getX(), player.getY() - 30, 151, 100, 4);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
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

        Log.d("In draw()", "");


//      get the width "getWidth()" of the physical device
//        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
//        final float scaleFactorY = getHeight() / (HEIGHT * 1.f) / 2;

//          For the above method to follow, image size should be smaller than device size
//        else use the value of 1,1 for both
//        IF THE IMAGE SIZE IS SAME AS THAT OF NEXUS4, I CAN EVEN SKIP THE STEPS OF SCALEFACTORX,Y
        if (canvas != null) {
            final int savedState = canvas.save();
//            Still background drawing
            Background bgStill = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.night_sky));
            bgStill.draw(canvas);
//            Moving background drawing
//            canvas.scale(1, scaleFactorY, 0, -550);
//            canvas.scale(1, 1, 0, 0);
            bg.draw(canvas);
            if (!disappear) {
                player.draw(canvas);
            }
            for (Enemy e : enemy) {
                e.draw(canvas);
            }

            for (TopBorder tb : topborder) {
                tb.draw(canvas);
            }

            for (BotBorder bb : botborder) {
                bb.draw(canvas);
            }
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }

    }

    public void updateTopBorder() {

//      Every 50 points, insert randomly placed top blocks that breaks the pattern

        if (player.getScore() % 50 == 0) {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topborder.get(topborder.size() - 1).getX() + 20, 0,
                    (int) ((rand.nextDouble() * (maxborderheight)) + 1)));
        }

        for (int i = 0; i < topborder.size(); i++) {
            topborder.get(i).update();

//            if border is moving off screen, add a new one
            if (topborder.get(i).getX() < -20) {
                topborder.remove(i);

//                remove element of array list, replace it by adding a new one

//                calculate in which dierection the boreder will be added to
                if (topborder.get(topborder.size() - 1).getHeight() >= maxborderheight) {
                    topdown = false;
                }

                if (topborder.get(topborder.size() - 1).getHeight() <= maxborderheight) {
                    topdown = true;
                }

//                new border added with larger height
                if (topdown) {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topborder.get(topborder.size() - 1).getX() + 20, 0,
                            topborder.get(topborder.size() - 1).getHeight() + 1));
                } else {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topborder.get(topborder.size() - 1).getX() + 20, 0,
                            topborder.get(topborder.size() - 1).getHeight() - 1));
                }

            }
        }

    }

    public void updateBottomBorder() {

//        every 40 points, insert randomly placed bottom blocks that break pattern
        if (player.getScore() % 40 == 0) {
//            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
//                    botborder.get(botborder.size() - 1).getX() + 20, (int) ((rand.nextDouble() * maxborderheight)) +
//                    (HEIGHT - maxborderheight)));

            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, (int) ((rand.nextDouble() * maxborderheight)) +
                    (HEIGHT - 1)));
        }

//        update bottom border
        for (int i = 0; i < botborder.size(); i++) {
            botborder.get(i).update();

            if (botborder.get(i).getX() < -20) {
                botborder.remove(i);//                remove element of array list, replace it by adding a new one

//                calculate in which dierection the boreder will be added to
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT - maxborderheight) {
                    botdown = false;
                }

                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - maxborderheight) {
                    botdown = true;
                }

//                new border added with larger height
                if (botdown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            botborder.get(botborder.size() - 1).getX() + 20,
                            botborder.get(botborder.size() - 1).getY() - 1));
                }
            }
        }
    }

    public void newGame() {
        Log.d("In newGame()", "");

        disappear = false;

        botborder.clear();
        topborder.clear();

        enemy.clear();

        minborderheight = 1;
        maxborderheight = 1;

        player.resetDYA();
        player.resetScore();
        player.setY(HEIGHT / 2);

        if (player.getScore() > best) {
            best = player.getScore();
        }

//        create initial top borders
        for (int i = 0; i * 20 < WIDTH + 40; i++) {

            if (i == 0) {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i * 20, 0, 0));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, 0, topborder.get(i - 1).getHeight() + 1));

            }
        }

        //        create initial bottom borders

        for (int i = 0; i * 20 < WIDTH + 40; i++) {

//first border created
            if (i == 0) {
//                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
//                        i * 20, HEIGHT - minborderheight));

                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, HEIGHT - 1));
            }

//adding borders until the initial screen is full

            else {
//                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
//                        i * 20, botborder.get(i - 1).getY() - 1));

                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, HEIGHT - 1));

            }
        }

        newGameCreated = true;

    }

    public void drawText(Canvas canvas) {
        Log.d("In drawText()", "");

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore() * 3), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }


}
