package gui;

/**
 * Created by marcin on 07.05.16.
 */
import game.Game;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import javax.swing.*;

public class NetworkServerMenu implements ActionListener, KeyListener{

    Game game;
    JFrame window;
    JButton b2;
    JButton b3;
    JTextField tfPort;
    JTextField tfNick;
    JPanel p1;

    public NetworkServerMenu(Game game) {
        this.game = game;
        window = game.screen.getWindow();
        init();
    }

    public void init() {
        p1 = new JPanel();
        p1.setLayout(null);
        p1.setSize(300, 350);

        // p1.setBackground(Color.ORANGE);

        JLabel l1 = new JLabel("Port:");
        l1.setSize(50, 25);
        l1.setLocation(50, 25);

        tfPort = new JTextField("4545");
        tfPort.setSize(100, 25);
        tfPort.setLocation(100, 25);

        JLabel l2 = new JLabel("Nick:");
        l2.setSize(50, 25);
        l2.setLocation(50, 55);

        tfNick = new JTextField("serwer");
        tfNick.setSize(100, 25);
        tfNick.setLocation(100, 55);
        tfNick.addKeyListener(this);

        JPanel p2 = new JPanel();
        p2.setLayout(null);
        p2.setSize(250, 100);
        p2.setLocation((p1.getWidth() - p2.getWidth()) / 2, 25);
        p2.setBorder(BorderFactory.createTitledBorder("Ustawienia:"));

        p2.add(l1);
        p2.add(tfPort);
        p2.add(l2);
        p2.add(tfNick);

        b2 = new JButton("Uruchom serwer");
        b2.setSize(250, 50);
        b2.setLocation((p1.getWidth() - b2.getWidth()) / 2, 150);
        b2.setFocusable(false);
        b2.addActionListener(this);

        b3 = new JButton("Powrot");
        b3.setSize(250, 50);
        b3.setLocation((p1.getWidth() - b3.getWidth()) / 2, 250);
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

    }

    public void draw(Graphics2D g) {
        g.setBackground(Color.BLACK);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, game.getWidth(), game.getHeight());

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
            try {
                int port = Integer.parseInt(tfPort.getText().trim());
                String nick = tfNick.getText().trim();
                if (port <= 1024 || port >= 65536) {
                    throw new NumberFormatException();
                }

                if (nick.length() < 3) {
                    JOptionPane.showMessageDialog(window,
                            "Nick musi mieć przynajmniej 3 znaki",
                            "Błędny nick!", JOptionPane.ERROR_MESSAGE);
                } else if (nick.length() > 8) {
                    JOptionPane.showMessageDialog(window,
                            "Nick nie może mieć więcej niż 8 znaków",
                            "Błędny nick!", JOptionPane.ERROR_MESSAGE);
                } else {
                    hide();
                    writeConfig();
                    game.networkJoinMenu.showForServer(port, nick);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(window,
                        "Numer portu jest nieprawidłowy.\n"
                                + "Nalely podać liczbę większą do 1024\n"
                                + "oraz nie wiekszą niż 65535",
                        "Błędny numer portu!", JOptionPane.ERROR_MESSAGE);
            }
        } else if (src == b3) {
            hide();
            writeConfig();
            game.networkMenu.show();
        }
    }

    public void readConfig() {
        tfNick.setText(game.config.getServerNick());
        tfPort.setText(Integer.toString(game.config.getPort()));
    }

    public void writeConfig() {
        try {
            int port = Integer.parseInt(tfPort.getText().trim());
            if (port <= 1024 || port >= 65536) {
                throw new NumberFormatException();
            }
            game.config.setPort(port);
        } catch (NumberFormatException ex) {
        }

        String nick = tfNick.getText().trim();

        if (nick.length() >= 3 || nick.length() <= 8) {
            game.config.setServerNick(nick);
        }
    }

    public void show() {
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

    public void keyPressed(KeyEvent e) {
        if (e.getSource() == tfNick) {
            if (e.getKeyChar() == '|' || e.getKeyChar() == ' ') {
                e.consume();
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
