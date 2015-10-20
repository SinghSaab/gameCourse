package uottawa.gamecourse;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Administrator on 2015-08-15.
 */
public class MainThread extends Thread {
    public static Canvas canvas;
    private int FPS = 35;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private gamePanel gamePanel;
    private boolean running;

    public MainThread(SurfaceHolder surfaceHolder, gamePanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000 / FPS;

        while (running) {
            startTime = System.nanoTime();
            canvas = null;

            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            } catch (Exception e) {
            } finally {
                if (canvas != null) {
                    try {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;

            try {
                this.sleep(waitTime);
            } catch (Exception e) {
            }

            totalTime += System.nanoTime() - startTime;
            frameCount++;

            if (frameCount == FPS) {
                averageFPS = 1000 / ((totalTime / frameCount) / 1000000);
                frameCount = 0;
                totalTime = 0;
//              System.out.println("Average FPS:"+averageFPS);
            }

        }

    }

    public void setRunning(boolean b) {
        running = b;
    }
}
