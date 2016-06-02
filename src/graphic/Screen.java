package graphic;

/**
 * Created by marcin on 07.05.16.
 */

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;

public class Screen {

    public static final int MODE_640x480 = 0;
    public static final int MODE_800x600 = 1;
    public static final int MODE_1024x768 = 2;
    private GraphicsDevice device;
    private JFrame frame;
    private boolean isFullScreen;

    public Screen() {
        GraphicsEnvironment environment = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        device = environment.getDefaultScreenDevice();
    }

    public DisplayMode[] getDisplayModes() {
        return device.getDisplayModes();
    }

    public DisplayMode getFirstGoodMode(DisplayMode modes[]) {
        DisplayMode goodModes[] = getDisplayModes();
        
        for (int i = 0; i < modes.length; i++) {
            for (int j = 0; j < goodModes.length; j++) {
                if (compareDisplayModes(modes[i], goodModes[j])) {
                    return modes[i];
                }
            }
        }
        return null;
    }

    public DisplayMode getCurrentDisplayMode() {
        return device.getDisplayMode();
    }

    public DisplayMode[] getPossibleModes(int mode) {
        switch (mode) {
            case MODE_800x600: {
                DisplayMode posibleModes[] = {
                        new DisplayMode(800, 600, 32,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(800, 600, 24,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(800, 600, 16,
                                DisplayMode.REFRESH_RATE_UNKNOWN) };
                return posibleModes;
            }
            case MODE_1024x768: {
                DisplayMode posibleModes[] = {
                        new DisplayMode(1024, 768, 32,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(1024, 768, 24,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(1024, 768, 16,
                                DisplayMode.REFRESH_RATE_UNKNOWN) };
                return posibleModes;
            }
            default: {
                DisplayMode posibleModes[] = {
                        new DisplayMode(640, 480, 32,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(640, 480, 24,
                                DisplayMode.REFRESH_RATE_UNKNOWN),
                        new DisplayMode(640, 480, 16,
                                DisplayMode.REFRESH_RATE_UNKNOWN) };
                return posibleModes;
            }
        }
    }


     // sprawdza czy oba tryby pasują do siebie
    public boolean compareDisplayModes(DisplayMode mode1, DisplayMode mode2) {
        if (mode1.getWidth() != mode2.getWidth()
                || mode1.getHeight() != mode2.getHeight()) {
            return false;
        }

        if (mode1.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI
                && mode2.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI
                && mode1.getBitDepth() != mode2.getBitDepth()) {
            return false;
        }

        if (mode1.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN
                && mode2.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN
                && mode1.getRefreshRate() != mode2.getRefreshRate()) {
            return false;
        }

        return true;
    }

    public boolean checkFullScreenMode(int mode) {
        DisplayMode possibleModes[] = getPossibleModes(mode);
        DisplayMode displayMode;
        displayMode = getFirstGoodMode(possibleModes);
        if (displayMode == null/* || !device.isFullScreenSupported() */) {
            return false;
        }
        return true;
    }

    // przechodzi w tryb pełnoekranowy i zmienia tryb graficzny
    //  wykorzystany jest obiekt BufferStrategy z dwoma buforami.
      public void setScreen(int mode, boolean isFullScreen) {
        DisplayMode possibleModes[] = getPossibleModes(mode);
        DisplayMode displayMode;
        if (isFullScreen) {
            displayMode = getFirstGoodMode(possibleModes);
            if (displayMode == null) {
                displayMode = new DisplayMode(640, 480,
                        DisplayMode.BIT_DEPTH_MULTI,
                        DisplayMode.REFRESH_RATE_UNKNOWN);
                isFullScreen = false;
            }
        } else {
            displayMode = possibleModes[0];
        }

        if (frame == null) {
            frame = new JFrame("RTS game- WIN OR DIE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setIgnoreRepaint(true);
            frame.setResizable(false);
            frame.getContentPane().setLayout(null);
        } else {
            restoreScreen();
        }

        frame.setUndecorated(isFullScreen);
        frame.setSize(displayMode.getWidth(), displayMode.getHeight());

        if (isFullScreen) {
            device.setFullScreenWindow(frame);

            if (device.isDisplayChangeSupported()) {
                try {
                    device.setDisplayMode(displayMode);
                } catch (IllegalArgumentException ex) {
                }
            }

            this.isFullScreen = true;
        } else {
            this.isFullScreen = false;
        }
        hide();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public Graphics2D getGraphics() {
        if (frame != null) {
            BufferStrategy strategy = frame.getBufferStrategy();
            return (Graphics2D) strategy.getDrawGraphics();
        } else {
            return null;
        }
    }

    public void update() {
        if (frame != null) {
            BufferStrategy strategy = frame.getBufferStrategy();
            if (!strategy.contentsLost()) {
                strategy.show();
            }
        }
        // operacja Sync naprawia problem z kolejką zdarzeń AWT w Linuxie
        Toolkit.getDefaultToolkit().sync();
    }

    // zwraca czy jest w pełnoekranowym
    public JFrame getWindow() {
        return frame;
    }

    // zwraca szerokosc jezeli urządzenie nie jest w trybie pełnoekranowym
    public int getWidth() {
        if (frame != null) {
            return frame.getWidth();
        } else {
            return 0;
        }
    }

    // zwraca wysokosc jezeli urządzenie nie jest w trybie pełnoekranowym
    public int getHeight() {
        if (frame != null) {
            return frame.getHeight();
        } else {
            return 0;
        }
    }

    public void restoreScreen() {
        device.setFullScreenWindow(null);
        if (frame != null) {
            frame.dispose();
        }
    }

    public BufferedImage createBufferedImage(int w, int h, int transparancy) {
        if (frame != null) {
            GraphicsConfiguration gc = frame.getGraphicsConfiguration();
            return gc.createCompatibleImage(w, h, transparancy);
        }
        return null;
    }

    public boolean isVisible() {
        if (frame != null) {
            return frame.isVisible();
        } else {
            return false;
        }
    }

    public void show() {
        if (!isVisible() && frame != null) {
            frame.setVisible(true);
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                        frame.createBufferStrategy(2);
                    }
                });
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
        frame.requestFocus();
    }

    public void hide() {
        if (isVisible()) {
            frame.setVisible(false);
        }
    }
}
