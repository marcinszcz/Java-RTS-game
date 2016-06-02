package game;

/**
 * Created by marcin on 07.05.16.
 */

import java.awt.*;
import javax.swing.ImageIcon;

import graphic.Screen;

public abstract class Kernel {

    protected static final int FONT_SIZE = 16;
    private boolean isRunning;
    public Screen screen;
    public Config config;

    public void exit() {
        isRunning = false;
    }

    public void run() {
        try {
            init();
            gameLoop();
        } finally {
            screen.restoreScreen();
            sureExit();
        }
    }

    // konczymy dzialanie maszyny wirtualnej z wątku demona
    public void sureExit() {
        Thread thread = new Thread() {
            public void run() {
                // na początku czekamy na samodzielne zakonczenie
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }
                // system nadal działa, więc wymuszamy zakończenie
                System.exit(0);
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void init() {
        screen = new Screen();
        config = new Config("config.ini");
        screen.setScreen(config.getMode(), config.isFullscreen());
        Window window = screen.getWindow();
        window.setFont(new Font("Dialog", Font.PLAIN, FONT_SIZE));
        window.setBackground(Color.white);
        window.setForeground(Color.black);

        isRunning = true;
    }

    public void gameLoop() {
        long startTime = System.currentTimeMillis();
        long currTime = startTime;

        while (isRunning) {
            long elapsedTime = System.currentTimeMillis() - currTime;
            currTime += elapsedTime;

            // aktualizacja
            update(elapsedTime);

            // rysowanie
            Graphics2D g = screen.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            draw(g);
            g.dispose();
            screen.update();
			/*
			 * try { Thread.sleep(20); } catch (InterruptedException ex) {
			 * ex.printStackTrace(); }
			 */
        }
        config.writeConfiguration("config.ini");
    }

    public int getWidth() {
        return screen.getWidth();
    }

    public int getHeight() {
        return screen.getHeight();
    }

    // aktualizuje stan gry/animacji
    public abstract void update(long elapsedTime);
    
    public abstract void draw(Graphics2D g);
}
