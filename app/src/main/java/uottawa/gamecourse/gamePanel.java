package uottawa.gamecourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Vibrator;
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
    public static int MOVESPEED = -7;
    public static boolean homescreen = false;
    private long enemyStartTime;
    private long enemyElapsed;

    private long powerupElapsed;
    private long powerupStartTime;

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Enemy> enemy;

    private ArrayList<TrollEnemy> troll;

    private ArrayList<Powerups> powerup;

    private int count = 0;   //no of attacks
    private boolean newGameCreated;
    //  increase to slow down difficulty progression
    private Random rand = new Random();
    private Explosion explosion;
    private boolean disappear;
    private boolean started;
    private int best;

    private MediaPlayer mplayer_explosion;
    private MediaPlayer mplayer_death;
    private MediaPlayer mplayer_powerup;


    public gamePanel(Context context) {
        super(context);
//      add callback to surface holder to intercept events
        getHolder().addCallback(this);
//      make gamepanel focusable so that it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//      Write best score to the shared preference
        SharedPreferences bestScore = getContext().getSharedPreferences("bestScore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = bestScore.edit();
        editor.putInt("bestScore", best);
        editor.apply();

        mplayer_death.release();
        mplayer_explosion.release();
        mplayer_powerup.release();

//      Close the thread when game is ended
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

    //    This function is called when the game run for the first time
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.night_land),
                BitmapFactory.decodeResource(getResources(), R.drawable.watermark));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.player), 280, 85, 3);
        enemy = new ArrayList<Enemy>();
        troll = new ArrayList<TrollEnemy>();
        enemyStartTime = System.nanoTime();

        powerup = new ArrayList<Powerups>();
        powerupStartTime = System.nanoTime();

//      Instantiate MainThread
        thread = new MainThread(getHolder(), this);
//      We can safely start the game loop
        thread.setRunning(true);
        thread.start();
//      Game didn't used to start at beginning  because waiting for onTouchEvent, hence this will start game automatically
        player.setPlaying(true);
        homescreen = true;
//      Read best score from the shared preference
        SharedPreferences sharedPref = getContext().getSharedPreferences("bestScore", Context.MODE_PRIVATE);
        int defaultValue = 0;
        best = sharedPref.getInt("bestScore", defaultValue);
//      Unless home-screen is off, player will be stayed at the middle of screen(check Player.java)

        mplayer_death = MediaPlayer.create(getContext(), R.raw.death);
        mplayer_explosion = MediaPlayer.create(getContext(), R.raw.hit);
        mplayer_powerup = MediaPlayer.create(getContext(), R.raw.powerup);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//          When on home-screen, getPlaying() is false; so first click will start game
            if (!player.getPlaying()) {
                newGameCreated = true;
                player.setPlaying(true);
                player.setUp(true);
            }
//          When game has started or is already running
            if (player.getPlaying()) {
//              If it's the first click, "reset score counter or continue" function
                if (!started) {
//                  player.resetScore() calls; so that the game would not start counting on the home-screen
                    player.resetScore();
//                  Depicts game has started and score can be shown on screen
                    started = true;
                }
//              Hides the home-screen now
                homescreen = false;
//              Player starts moving up on touch
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        if (event.getAction() == MotionEvent.BUTTON_BACK) {

        }
        return super.onTouchEvent(event);
    }

    //  This function keeps on running at each thread
    public void update() {

//      Checks if the game has started after player died/first time
        if (newGameCreated) {
//          Depicts as game in progress
            newGameCreated = false;
//          Set lives to full
            count = 0;
//          Thrash the old score
            player.resetScore();
//          Player starts playing
            player.setPlaying(true);
        }
//      Two loops will run; if the player is playing or now
//      This is the first part of the loop
        if (player.getPlaying()) {
//          Update the background and player animation
            bg.update();
            player.update();

            if (player.getY() == HEIGHT - 128) {
                Vibrator v = (Vibrator) this.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(5);
            }

            powerupElapsed = (System.nanoTime() - powerupStartTime) / 1000000;

            if (!homescreen && powerupElapsed > (10000 + player.getScore() / 2) && player.getScore() > 250) {
                powerup.add(new Powerups(BitmapFactory.decodeResource(getResources(), R.drawable.powerup_heart),
                        WIDTH + 10, (int) ((rand.nextDouble() * (HEIGHT - 10))),
                        50, 33, player.getScore(), 8));
                powerupStartTime = System.nanoTime();

            }

//            loop through every powerup to check collisions with the player
            for (int i = 0; i < powerup.size(); i++) {
                powerup.get(i).update();
//              detect collision with player
                if (collision(powerup.get(i), player)) {
                    mplayer_powerup.start();
//                  On collision, remove the particular enemy element
                    powerup.remove(i);
//                  If the element is a powerup or not
                    if (count > 0 && count < 3) {
//                      If element is powerup and life <=2; increases life
                        --count;
                    } else {
                        break;
                    }
                    if (!mplayer_powerup.isPlaying())
                        mplayer_powerup.stop();
                }
//              else if there is no collision and enemy passes through, we'll remove the enemy object
                if (powerup.get(i).getX() < -100) {
                    powerup.remove(i);
                    break;
                }
            }


//          Time to measure enemy placement
            long enemyElapsed = (System.nanoTime() - enemyStartTime) / 1000000;
//          No enemy while home-screen; As score goes higher, less delay between enemy launches
            if (enemyElapsed > (2000 - player.getScore() / 4) && !homescreen) {
//              first enemy always goes down the middle
                if (enemy.size() == 0) {
//                  enemy.size() gives the no. of enemies on the canvas at a time
                    enemy.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, HEIGHT / 2, 100, 30, player.getScore(), 8));

                }
//              After first missile is added to the screen, start randomizing the location of every other missile
                else {
                    int toTrollOrNot = rand.nextInt(10);
                    if (toTrollOrNot % 2 == 0) {
                        enemy.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                                WIDTH + 10, (int) ((rand.nextDouble() * (HEIGHT - 50))),
                                100, 30, player.getScore(), 8));
                    } else {
                        troll.add(new TrollEnemy(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                                WIDTH + 10, (int) ((rand.nextDouble() * (HEIGHT - 50))), 100, 30, player.getScore(), 8,
                                player));
                    }
                }
//              reset timer for the enemy to keep track of last enemy added
                enemyStartTime = System.nanoTime();
            }

//          loop through every troll missile to check collisions with the player
            for (int i = 0; i < troll.size(); i++) {
                troll.get(i).update();
                if (collision(troll.get(i), player)) {
//                  On collision, remove the particular troll element
                    mplayer_explosion.start();
                    troll.remove(i);
//                  Increase the no. of the times collision happened
                    ++count;
//                  On 3 collisions, game will end
                    if (count == 3) {
//                      Set the best score for the game session; will not save when game gets closed
                        if (best < player.getScore())
                            best = player.getScore();
//                      Player will stop playing
                        player.setPlaying(false);
//                      player.resetScore() calls; so that new game would not start counting on the home-screen
//                      Depicts that game hasn't started yet
                        started = false;
//                      Break the loop of collision detection
                        break;
                    }
                    if (!mplayer_explosion.isPlaying())
                        mplayer_explosion.stop();
                }
                if (troll.get(i).getX() < -100) {
                    troll.remove(i);
                    break;
                }
            }

            for (int i = 0; i < enemy.size(); i++) {
                enemy.get(i).update();
//              detect collision with player
                if (collision(enemy.get(i), player)) {
                    mplayer_explosion.start();
//                  On collision, remove the particular enemy element
                    enemy.remove(i);
//                  Increase the no. of the times collision happened
                    ++count;
//                  On 3 collisions, game will end
                    if (count == 3) {
//                      Set the best score for the game session; will not save when game gets closed
                        if (best < player.getScore())
                            best = player.getScore();
//                      Player will stop playing
                        player.setPlaying(false);
//                      player.resetScore() calls; so that new game would not start counting on the home-screen
//                      Depicts that game hasn't started yet
                        started = false;
//                      Break the loop of collision detection
                        break;
                    }
                    if (!mplayer_explosion.isPlaying())
                        mplayer_explosion.stop();
                }
//              else if there is no collision and enemy passes through, we'll remove the enemy object
                if (enemy.get(i).getX() < -100) {
                    enemy.remove(i);
                    break;
                }
            }
        }
//      This is the second part of major loop; what to do when player is not playing
        else {
//          There is only 1 case for this execution cycle; when player has died
//          These steps will just reset the game and other parameters
            player.resetScore();
            player.resetDYA();
//          Clear the remaining enemies off the screen
            enemy.clear();

            troll.clear();

            powerup.clear();
//          Bring the player back to the home-screen
            homescreen = true;
//          Set the player in the middle  and at the extreme left
            player.setY(HEIGHT / 2);
            player.setX(0);
//          Set the newGameCreated parameter to depict beginning of a new game or not
            newGameCreated = true;
        }
    }

    //  Function to detect the collision rectangles
    public boolean collision(gameObject a, gameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }
        return false;
    }

    //  Takes control of the canvas and text related drawing
    @Override
    public void draw(Canvas canvas) {
        if (canvas != null) {
            final int savedState = canvas.save();
//          Moving background drawing
//          get the width "getWidth()" of the physical device
//          final float scaleFactorX = getWidth() / (WIDTH * 1.f);
//          final float scaleFactorY = getHeight() / (HEIGHT * 1.f) / 2;
//          For the above method to follow, image size should be smaller than device size
//          IF THE IMAGE SIZE IS SAME AS THAT OF NEXUS4, ONE CAN EVEN SKIP THE STEPS OF SCALEFACTOR X,Y
//          canvas.scale(scaleFactorX, scaleFactorY, 0, -550);
//                  OR
//          canvas.scale(1, 1, 0, 0);
            bg.draw(canvas);
            drawText(canvas);
//          If disappear parameter is false; show the player on canvas (or we can hide the player as well)
            if (!disappear) {
                player.draw(canvas);
            }
//          Draw the enemy
            for (Enemy e : enemy) {
                e.draw(canvas);
            }
//          Draw the powerup
            for (Powerups p : powerup) {
                p.draw(canvas);
            }
            for (TrollEnemy te : troll) {
                te.draw(canvas);
            }
//          Can't make use of this till now. Argh!

            if (!player.getPlaying()) {
                explosion.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }
    }

    //  This is used to create the home screen
    public void drawText(Canvas canvas) {
//      When home screen is not displayed, show the game data (lives, score, best score)
        if (!homescreen) {
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(30);
            paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
            canvas.drawText("DISTANCE: " + (player.getScore()), 10, HEIGHT - 10, paint);
//          Depending upon the collisions, Lives color will change in the shape of heart
            if (count == 0) {
                paint.setColor(Color.GREEN);
                canvas.drawText("❤❤❤", WIDTH / 2 - 65, HEIGHT - 10, paint);
            } else if (count == 1) {
//              This is for orange color
                paint.setColor(Color.rgb(255, 128, 0));
                canvas.drawText("❤❤⛶", WIDTH / 2 - 65, HEIGHT - 10, paint);
            } else if (count == 2) {
                paint.setColor(Color.RED);
                canvas.drawText("❤⛶⛶", WIDTH / 2 - 65, HEIGHT - 10, paint);
            } else if (count == 3) {
                paint.setColor(Color.LTGRAY);
                canvas.drawText("⛶⛶⛶", WIDTH / 2 - 65, HEIGHT - 10, paint);
            }
            paint.setColor(Color.WHITE);
            canvas.drawText("BEST: " + best, WIDTH - 250, HEIGHT - 10, paint);
        }

//      When homescreen is displayed, Show the instructions/controls
        if (homescreen) {
            Paint paint1 = new Paint();
            paint1.setTextSize(50);
            paint1.setColor(Color.rgb(220, 20, 5));
            paint1.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);
            paint1.setTextSize(35);
            paint1.setColor(Color.rgb(239, 206, 11));
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 45, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 75, paint1);
        }
    }
}
