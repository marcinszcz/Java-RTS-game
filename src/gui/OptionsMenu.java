package gui;

/**
 * Created by marcin on 07.05.16.
 */
import game.Game;
import graphic.Screen;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

public class OptionsMenu implements ActionListener{
    Game game;
    JFrame window;
    JButton b2;
    JButton b3;
    JRadioButton rb640x480;
    JRadioButton rb800x600;
    JRadioButton rb1024x768;
    JCheckBox cbFullScreen;
    JPanel p1;

    private boolean setOptions;

    private int configMode;

    private boolean configIsFullScreen;

    public OptionsMenu(Game game) {
        this.game = game;
        window = game.screen.getWindow();
        init();
    }

    public void init() {
        p1 = new JPanel();
        p1.setLayout(null);
        p1.setSize(300, 350);

        // p1.setBackground(Color.ORANGE);

        JLabel l1 = new JLabel("Rozdzielczosc ekranu:");
        l1.setSize(200, 20);
        l1.setLocation(20, 20);
        rb640x480 = new JRadioButton("640 x 480", false);
        rb640x480.setSize(100, 20);
        rb640x480.setLocation(50, 50);
        rb800x600 = new JRadioButton("800 x 600", false);
        rb800x600.setSize(100, 20);
        rb800x600.setLocation(50, 80);
        rb1024x768 = new JRadioButton("1024 x 768", false);
        rb1024x768.setSize(100, 20);
        rb1024x768.setLocation(50, 110);
        cbFullScreen = new JCheckBox("Tryb pełnoekranowy", false);
        cbFullScreen.setSize(200, 20);
        cbFullScreen.setLocation(20, 150);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rb640x480);
        bg.add(rb800x600);
        bg.add(rb1024x768);

        JPanel p2 = new JPanel();
        p2.setLayout(null);
        p2.setSize(250, 200);
        p2.setLocation((p1.getWidth() - p2.getWidth()) / 2, 25);
        p2.setBorder(BorderFactory.createTitledBorder("Opcje:"));
        p2.add(l1);
        p2.add(rb640x480);
        p2.add(rb800x600);
        p2.add(rb1024x768);
        p2.add(cbFullScreen);

        b2 = new JButton("OK");
        b2.setSize(112, 50);
        b2.setLocation(163, 250);
        b2.setFocusable(false);
        b2.addActionListener(this);

        b3 = new JButton("Anuluj");
        b3.setSize(112, 50);
        b3.setLocation(50, 250);
        b3.setFocusable(false);
        b3.addActionListener(this);

        p1.add(p2);
        p1.add(b2);
        p1.add(b3);

        p1.setLocation((window.getWidth() - p1.getWidth()) / 2, (window
                .getHeight() - p1.getHeight()) / 2);
        window.getContentPane().add(p1);
        hide();
    }

    public void centerPanel() {
        p1.setLocation((window.getWidth() - p1.getWidth()) / 2, (window
                .getHeight() - p1.getHeight()) / 2);
    }

    public void update(long elapsedTime) {
        if (setOptions) {
            setOptions = false;
            int mode;
            if (rb800x600.isSelected()) {
                mode = Screen.MODE_800x600;
            } else if (rb1024x768.isSelected()) {
                mode = Screen.MODE_1024x768;
            } else {
                mode = Screen.MODE_640x480;
            }

            if (mode != configMode
                    || cbFullScreen.isSelected() != configIsFullScreen) {

                if (!cbFullScreen.isSelected()
                        || game.screen.checkFullScreenMode(mode)) {
                    game.screen.setScreen(mode, cbFullScreen.isSelected());
                    writeConfig(mode, cbFullScreen.isSelected());
                    game.screen.show();
                    setOptions = false;
                    centerPanel();
                    hide();
                    if (game.isPause()) {
                        game.gameMenu.show();
                    } else {

                        game.mainMenu.show();
                    }
                } else {
                    JOptionPane.showMessageDialog(window,
                            "Ten tryb graficzny jest niedostępny!",
                            "Błąd trybu graficznego!",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                hide();
                if (game.isPause()) {
                    game.gameMenu.show();
                } else {
                    game.mainMenu.show();
                }
            }
        }
    }

    public void draw(Graphics2D g) {

        if (!game.isPause()) {
            g.setBackground(Color.BLACK);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, game.getWidth(), game.getHeight());
        }

        Insets insets = window.getInsets();

        g.translate(insets.left, insets.top);
        final Graphics2D g2 = g;
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    window.getContentPane().paintComponents(g2);
                }
            });
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
        }
        g.translate(-insets.left, -insets.top);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == b2) {
            // zatwierdz zmiany
            setOptions = true;
        } else if (src == b3) {
            hide();
            if (game.isPause()) {
                game.gameMenu.show();
            } else {
                game.mainMenu.show();
            }
        }

    }

    public void readConfig() {
        configMode = game.config.getMode();
        configIsFullScreen = game.config.isFullscreen();

        switch (configMode) {
            case Screen.MODE_800x600:
                rb800x600.setSelected(true);
                break;
            case Screen.MODE_1024x768:
                rb1024x768.setSelected(true);
                break;
            default:
                rb640x480.setSelected(true);
                break;
        }

        cbFullScreen.setSelected(configIsFullScreen);
    }

    public void writeConfig(int mode, boolean isFullScreen) {
        game.config.setMode(mode);
        game.config.setFullscreen(isFullScreen);
    }

    public void show() {
        setOptions = false;
        readConfig();
        centerPanel();
        p1.setVisible(true);
    }

    public void hide() {
        p1.setVisible(false);
    }

    public boolean isVisible() {
        return p1.isVisible();
    }
}
