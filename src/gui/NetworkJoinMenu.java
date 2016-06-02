package gui;

/**
 * Created by marcin on 07.05.16.
 */
import game.Game;
import network.Client;
import network.GameEvent;
import network.Server;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;

public class NetworkJoinMenu implements ActionListener, KeyListener{

    Game game;
    JFrame window;
    JButton b1;
    JButton b2;
    JButton b3;
    JTextArea taChatBox;
    JTextField tfText;
    JPanel p1;
    String host;
    int port;
    String nick;
    boolean isServerRunning = false;
    boolean isClientRunning = false;
    boolean isServer;
    boolean isGameStarting = false;

    public NetworkJoinMenu(Game game) {
        this.game = game;
        window = game.screen.getWindow();
        init();
    }

    public void init() {
        p1 = new JPanel();
        p1.setLayout(null);
        p1.setSize(300, 350);

        taChatBox = new JTextArea();
        taChatBox.setLineWrap(true);
        taChatBox.setFocusable(false);
        taChatBox.setEditable(false);
        JScrollPane spChatBox = new JScrollPane(taChatBox);
        spChatBox.setSize(220, 125);
        spChatBox.setLocation(15, 25);

        tfText = new JTextField();
        tfText.setSize(140, 25);
        tfText.setLocation(15, 165);
        tfText.addActionListener(this);
        tfText.addKeyListener(this);

        b1 = new JButton("Wyslij");
        b1.setSize(75, 25);
        b1.setLocation(160, 165);
        b1.setFocusable(false);
        b1.addActionListener(this);

        JPanel p2 = new JPanel();
        p2.setLayout(null);
        p2.setSize(250, 200);
        p2.setLocation((p1.getWidth() - p2.getWidth()) / 2, 25);
        p2.setBorder(BorderFactory.createTitledBorder("Rozmowa:"));
        p2.add(spChatBox);
        p2.add(tfText);
        p2.add(b1);

        b2 = new JButton("Rozpocznij grę");
        b2.setSize(125, 50);
        b2.setLocation(150, 250);
        b2.setFocusable(false);
        b2.addActionListener(this);

        b3 = new JButton("Powrot");
        b3.setSize(100, 50);
        b3.setLocation(25, 250);
        b3.setFocusable(false);
        b3.addActionListener(this);

        p1.add(p2);
        p1.add(b2);
        p1.add(b3);

        b2.setEnabled(false);

        p1.setLocation((window.getWidth() - p1.getWidth()) / 2, (window
                .getHeight() - p1.getHeight()) / 2);
        window.getContentPane().add(p1);
        hide();
    }

    public void centerPanel() {
        p1.setLocation((window.getWidth() - p1.getWidth()) / 2, (window
                .getHeight() - p1.getHeight()) / 2);
    }

    public void login() {
        if (isClientRunning) {
            GameEvent ge = new GameEvent(GameEvent.C_LOGIN);
            ge.setPlayerId(nick);
            game.client.sendMessage(ge);
        }
    }

    public void joinToGame() {
        if (isClientRunning) {
            GameEvent ge = new GameEvent(GameEvent.C_JOIN_GAME);
            ge.setPlayerId(nick);
            game.client.sendMessage(ge);
        }
    }

    public void canJoinToGame(boolean b) {
        b2.setEnabled(b);
    }

    public void scrollChatBox() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                taChatBox.setCaretPosition(taChatBox.getText().length());
            }
        });
    }

    public void update(long elapsedTime) {
        if (isClientRunning) {
            if (!game.client.isAlive()) {
                isClientRunning = false;
                taChatBox.append("Połączenie zostało przewane!\n");
                scrollChatBox();
                return;
            }
            GameEvent ge;
            while ((ge = game.client.receiveMessage()) != null) {
                switch (ge.getType()) {
                    case GameEvent.SB_CHAT_MSG:
                        taChatBox.append(ge.getMessage() + "\n");
                        scrollChatBox();
                        break;
                    case GameEvent.SB_LOGIN:
                        taChatBox.append("Przyłączył się: \"" + ge.getMessage()
                                + "\"\n");
                        scrollChatBox();
                        break;
                    case GameEvent.S_LOGIN_FAIL:
                        taChatBox.append(ge.getMessage() + "\n");
                        scrollChatBox();
                        break;
                    case GameEvent.S_DISCONNECT:
                        if (isClientRunning) {
                            game.client.stop();
                            isClientRunning = false;
                        }
                        taChatBox.append("Połączenie zostało zakończone!\n");
                        scrollChatBox();
                        break;
                    case GameEvent.SB_CAN_JOIN_GAME:
                        canJoinToGame(true);
                        break;
                    case GameEvent.SB_CANNOT_JOIN_GAME:
                        canJoinToGame(false);
                        break;
                    case GameEvent.S_JOIN_GAME_OK:
                        canJoinToGame(false);
                        break;
                    case GameEvent.S_JOIN_GAME_FAIL:
                        taChatBox
                                .append("Nie udało się dołączyć do gry, spróbuj ponownie!\n");
                        scrollChatBox();
                        break;
                    case GameEvent.SB_PLAYER_JOINED:
                        taChatBox.append("Gracz \"" + ge.getMessage()
                                + "\" jest gotowy!\n");
                        scrollChatBox();
                        break;
                    case GameEvent.SB_START_GAME:
                        hide();
                        String a = ge.getMessage();
                        try {
                            game.setPlayer(Integer.parseInt(a), nick);
                            game.start();
                        } catch (NumberFormatException ex) {
                            taChatBox.append("Nieoczekiwany błąd!\n");
                            scrollChatBox();
                        }
                        break;
                    default:
                        taChatBox.append("Nieznany komunikat: #" + ge.getType()
                                + "\n");
                        scrollChatBox();
                        break;
                }
            }
        }
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

        if (src == tfText || src == b1) {
            String toSend = tfText.getText().trim();
            if (toSend.length() > 0) {
                tfText.setText("");

                if (isClientRunning) {
                    GameEvent ge = new GameEvent(GameEvent.C_CHAT_MSG, toSend);
                    ge.setPlayerId(nick);
                    game.client.sendMessage(ge);
                }
            }
        } else if (src == b2) {
            joinToGame();
        } else if (src == b3) {
            if (isClientRunning) {
                game.client.stop();
                isClientRunning = false;
            }
            if (isServerRunning) {
                game.server.stop();
                isServerRunning = false;
            }
            hide();
            if (isServer) {
                game.networkServerMenu.show();
            } else {
                game.networkClientMenu.show();
            }
            canJoinToGame(false);
        }
    }

    public void showGameOver(String message) {
        show();
        taChatBox.append("Gra zakończona\n");
        taChatBox.append(message + "\n");
        scrollChatBox();
    }

    public void showForClient(String host, int port, String nick) {
        isServer = false;
        this.host = host;
        this.port = port;
        this.nick = nick;

        show();

        game.client = new Client(nick, host, port);
        if (game.client.start()) {
            isClientRunning = true;
            login();
        } else {
            taChatBox.append("Nie udało się połączyć z serwerem!\n");
            scrollChatBox();
        }
    }

    public void showForServer(int port, String nick) {
        isServer = true;
        this.host = "localhost";
        this.port = port;
        this.nick = nick;

        show();

        game.server = new Server(port);
        if (game.server.start()) {
            isServerRunning = true;
            game.server.setGameInstance(game);
            taChatBox.append("Serwer pomyślnie uruchomiony!\n");
            scrollChatBox();
        } else {
            taChatBox.append("Nie udało sie uruchonić serwera!\n");
            scrollChatBox();
        }

        if (isServerRunning) {
            game.client = new Client(nick, host, port);
            if (game.client.start()) {
                isClientRunning = true;
                login();
            } else {
                taChatBox.append("Nie udało się połączyć z serwerem!\n");
                scrollChatBox();
            }
        }
    }

    private void show() {
        centerPanel();
        taChatBox.setText("");
        tfText.setText("");
        p1.setVisible(true);
    }

    public void hide() {
        p1.setVisible(false);
    }

    public boolean isVisible() {
        return p1.isVisible();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getSource() == tfText) {
            if (e.getKeyChar() == '|') {
                e.consume();
            }
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
