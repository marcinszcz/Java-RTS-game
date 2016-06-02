package gui;

/**
 * Created by marcin on 07.05.16.
 */

import game.Game;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public class GameMenu implements ActionListener {
    Game game;
    JFrame window;
    JButton b1;
    JButton b2;
    JButton b3;
    JButton b4;
    JPanel p1;

    public GameMenu(Game game) {
        this.game = game;
        window = game.screen.getWindow();
        init();
    }

    public void init() {
        p1 = new JPanel();
        p1.setLayout(null);
        p1.setSize(300, 350);

        b1 = new JButton("Powrot do gry");
        b1.setSize(250, 50);
        b1.setLocation((p1.getWidth() - b1.getWidth()) / 2, 50);
        b1.setFocusable(false);
        b1.addActionListener(this);

        b2 = new JButton("Opcje");
        b2.setSize(250, 50);
        b2.setLocation((p1.getWidth() - b2.getWidth()) / 2, 150);
        b2.setFocusable(false);
        b2.addActionListener(this);

        b4= new JButton("Tabela wynikow");
        b4.setSize (250, 50);
        b4.setLocation ((p1.getWidth() - b4.getWidth()) / 2, 150);
        b4.setFocusable(false);
        b4.addActionListener(this);

        b3 = new JButton("Zakończ grę");
        b3.setSize(250, 50);
        b3.setLocation((p1.getWidth() - b3.getWidth()) / 2, 250);
        b3.setFocusable(false);
        b3.addActionListener(this);

        p1.add(b1);
        p1.add(b2);
        p1.add(b4);
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

    }

    public void draw(Graphics2D g) {
		/*
		 * g.setBackground(Color.BLACK); g.setColor(Color.BLACK);
		 * g.fillRect(0,0,game.getWidth(), game.getHeight());
		 */

        g.setColor(Color.WHITE);
        g.drawString("Gra zatrzymana",25,60);
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
        if (src == b1) {
            game.pause();
            hide();
        } else if (src == b2) {
            hide();
            game.optionsMenu.show();
        } else if (src == b3) {
            game.stop();
            hide();
            game.mainMenu.show();
        }
    }

    public void show() {
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
