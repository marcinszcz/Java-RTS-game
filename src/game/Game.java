package game;

/**
 * Created by marcin on 07.05.16.
 */

import game.objects.*;
import graphic.*;
import gui.*;
import network.*;
import io.InputAction;
import io.InputManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.LinkedList;

public class Game extends Kernel {

    public game.Map map;
    public MainMenu mainMenu;
    public GameMenu gameMenu;
    public NetworkMenu networkMenu;
    public NetworkClientMenu networkClientMenu;
    public NetworkServerMenu networkServerMenu;
    public NetworkJoinMenu networkJoinMenu;
    public OptionsMenu optionsMenu;
    public Resources resources;
    public Player player;
    public Player opponent;
    public Player player1;
    public Player player2;
    public Client client;
    public Server server;
    public boolean isServer;
    private String nick;
    private LinkedList<Sprite> booms;
    private static final long BOOM_TIME = 1100;
    public InputManager inputManager;
    private InputAction pauseAction;
    private InputAction shiftLeftAction;
    private InputAction shiftRightAction;
    private InputAction shiftUpAction;
    private InputAction shiftDownAction;
    private InputAction leftButtonAction;
    private InputAction rightButtonAction;
    private boolean isStarting;
    private boolean isReady;
    private boolean isRunning;
    private boolean isPause;
    public long time = 0;
    public boolean canExit;
    public boolean canBegin = false;
    private boolean sendDead;

    public static void main(String[] args) {
        new Game().run();
    }

    public void init() {
        super.init();

        // sprawdz czy komponenty Swing nie odrysują się samodzielnie.
        NullRepaintManager.install();

        resources = new Resources(screen.getWindow().getGraphicsConfiguration());

        gameMenu = new GameMenu(this);
        mainMenu = new MainMenu(this);
        networkMenu = new NetworkMenu(this);
        networkClientMenu = new NetworkClientMenu(this);
        networkServerMenu = new NetworkServerMenu(this);
        networkJoinMenu = new NetworkJoinMenu(this);
        optionsMenu = new OptionsMenu(this);

        booms = new LinkedList<Sprite>();

        inputManager = new InputManager(screen.getWindow());
        createInputActions();

        player1 = new Player(this);
        player2 = new Player(this);

        client = null;
        server = null;
        isServer = false;

        isStarting = false;
        isRunning = false;
        isPause = false;

        screen.show();
        mainMenu.show();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPause() {
        return isPause;
    }

// tworzy obiekty inputAction i przypisuje je klawiszy
    public void createInputActions() {
        pauseAction = new InputAction("pause",
                InputAction.DETECT_INITAL_PRESS_ONLY);
        inputManager.mapToKey(pauseAction, KeyEvent.VK_ESCAPE);
        shiftLeftAction = new InputAction("shiftleft");
        inputManager.mapToKey(shiftLeftAction, KeyEvent.VK_LEFT);
        shiftRightAction = new InputAction("shiftright");
        inputManager.mapToKey(shiftRightAction, KeyEvent.VK_RIGHT);
        shiftUpAction = new InputAction("shiftup");
        inputManager.mapToKey(shiftUpAction, KeyEvent.VK_UP);
        shiftDownAction = new InputAction("shiftdown");
        inputManager.mapToKey(shiftDownAction, KeyEvent.VK_DOWN);
        leftButtonAction = new InputAction("leftbutton");
        inputManager.mapToMouse(leftButtonAction, InputManager.MOUSE_BUTTON_1);
        rightButtonAction = new InputAction("rightbutton",
                InputAction.DETECT_INITAL_PRESS_ONLY);
        inputManager.mapToMouse(rightButtonAction, InputManager.MOUSE_BUTTON_3);
    }

    // sprawdza stan obiektow z up
    public void checkInputs(long elapsedTime) {
        if (pauseAction.isPressed()) {
            pause();
            inputManager.resetAllInputActions();
        }

        if (!isPause()) {
            if (shiftLeftAction.isPressed()) {
                map.shiftLeft(elapsedTime, screen.getWidth());
            }
            if (shiftRightAction.isPressed()) {
                map.shiftRight(elapsedTime, screen.getWidth());
            }
            if (shiftUpAction.isPressed()) {
                map.shiftUp(elapsedTime, screen.getHeight());
            }
            if (shiftDownAction.isPressed()) {
                map.shiftDown(elapsedTime, screen.getHeight());
            }

            if (screen.isFullScreen())
                if (!shiftLeftAction.isPressed()
                        && !shiftRightAction.isPressed()
                        && !shiftUpAction.isPressed()
                        && !shiftDownAction.isPressed()) {

                    Insets insets = screen.getWindow().getInsets();

                    if (inputManager.getMouseX() <= insets.left + 10
                            && inputManager.getMouseX() >= 0) {
                        map.shiftLeft(elapsedTime, screen.getWidth());
                    }

                    if (inputManager.getMouseX() >= screen.getWidth()
                            - insets.right - 11
                            && inputManager.getMouseX() < screen.getWidth()) {
                        map.shiftRight(elapsedTime, screen.getWidth());
                    }

                    if (inputManager.getMouseY() <= insets.top + 10
                            && inputManager.getMouseY() >= 0) {
                        map.shiftUp(elapsedTime, screen.getHeight());
                    }

                    if (inputManager.getMouseY() >= screen.getHeight()
                            - insets.bottom - 11
                            && inputManager.getMouseY() < screen.getHeight()) {
                        map.shiftDown(elapsedTime, screen.getHeight());
                    }
                }

            if (leftButtonAction.isPressed()) {
                if (!player.isSelectionVisable()) {
                    player.startSelection();
                    player.setSelectionBegin(inputManager.getMouseX()
                            - map.getOffsetX(screen.getWidth()), inputManager
                            .getMouseY()
                            - map.getOffsetY(screen.getHeight()));
                }
                player.setSelectionEnd(inputManager.getMouseX()
                        - map.getOffsetX(screen.getWidth()), inputManager
                        .getMouseY()
                        - map.getOffsetY(screen.getHeight()));
            } else if (player.isSelectionVisable()) {
                player.setSelectionEnd(inputManager.getMouseX()
                        - map.getOffsetX(screen.getWidth()), inputManager
                        .getMouseY()
                        - map.getOffsetY(screen.getHeight()));
                player.stopSelection();
            }

            if (rightButtonAction.isPressed()) {
                if (!player.getSelectedUnits().isEmpty()) {
                    Iterator i = player.getSelectedUnits().iterator();
                    while (i.hasNext()) {
                        Unit unit = (Unit) i.next();
                        int x = Map.pixelsToTiles(inputManager.getMouseX()
                                - map.getOffsetX(screen.getWidth()));
                        int y = Map.pixelsToTiles(inputManager.getMouseY()
                                - map.getOffsetY(screen.getHeight()));

                        if (map.isFree(x, y)) {
                            unit.goTo(x, y);
                        } else {
                            Unit u = getUnit(x, y);
                            if (u != null) {
                                if (!u.isPlayerUnit()) {
                                    unit.beginAttack(u);
                                } else {
                                    unit.goTo(x, y);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // konczenie gry
    public void stop() {
        isRunning = false;
    }

    public Unit getUnit(int x, int y) {
        for (int j = 0; j < 2; j++) {
            Iterator i;

            if (j == 0) {
                i = player.getUnits();
            } else {
                i = opponent.getUnits();
            }

            while (i.hasNext()) {
                Unit unit = (Unit) i.next();

                if (unit.getX() == x && unit.getY() == y) {
                    return unit;
                }
            }
        }
        return null;
    }

    public void setPlayer(int i, String nick) {
        this.nick = nick;
        if (i == 1) {
            player = player1;
            opponent = player2;
            player.setPlayerNo(1);
            opponent.setPlayerNo(2);
        } else {
            player = player2;
            opponent = player1;
            player.setPlayerNo(2);
            opponent.setPlayerNo(1);
        }
    }

    public void ready() {
        GameEvent ge = new GameEvent(GameEvent.C_READY);
        ge.setPlayerId(nick);
        client.sendMessage(ge);
    }

    public void start() {
        new Thread() {
            public void run() {
                sendDead = false;
                isStarting = true;
                map = resources.loadMap("test", screen.getWidth(), screen
                        .getHeight());
                player.setUnits(resources.loadUnits("test", player, true));
                opponent.setUnits(resources.loadUnits("test", opponent, false));
                map.setFree(player.getUnits());
                map.setFree(opponent.getUnits());
                ready();
            }
        }.start();
    }

    public void pause() {
        isPause = !isPause;
        if (isPause) {
            gameMenu.show();
        } else {
            gameMenu.hide();
        }
    }

    public void update(long elapsedTime) {
        if (isRunning()) {
            checkInputs(elapsedTime);
            checkMessages();

            Iterator<Unit> i;

            if (!player.isAlive() && !sendDead) {
                GameEvent ge = new GameEvent(GameEvent.C_PLAYER_DEAD, Integer
                        .toString(player.getPlayerNo()));
                client.sendMessage(ge);
                sendDead = true;
            }

            i = player.getUnits();

            while (i.hasNext()) {
                Unit unit = i.next();
                unit.update(elapsedTime, map);
            }

            i = opponent.getUnits();
            while (i.hasNext()) {
                Unit unit = i.next();
                unit.update(elapsedTime, map);
            }

            Iterator<Sprite> i2;

            i2 = booms.iterator();
            while (i2.hasNext()) {
                Sprite sprite = i2.next();
                if (sprite.getAnimationTime() >= BOOM_TIME) {
                    i2.remove();
                } else {
                    sprite.update(elapsedTime);
                }
            }

            time += elapsedTime;

            if (isPause()) {
                if (gameMenu.isVisible()) {
                    gameMenu.update(elapsedTime);
                } else if (optionsMenu.isVisible()) {
                    optionsMenu.update(elapsedTime);
                }
            }
        } else {
            if (mainMenu.isVisible()) {
                mainMenu.update(elapsedTime);
            } else if (networkMenu.isVisible()) {
                networkMenu.update(elapsedTime);
            } else if (networkClientMenu.isVisible()) {
                networkClientMenu.update(elapsedTime);
            } else if (networkServerMenu.isVisible()) {
                networkServerMenu.update(elapsedTime);
            } else if (networkJoinMenu.isVisible()) {
                networkJoinMenu.update(elapsedTime);
            } else if (optionsMenu.isVisible()) {
                optionsMenu.update(elapsedTime);
            } else if (isStarting) {
                checkMessages();
                if (isReady) {
                    isStarting = false;
                    time = 0;
                    isPause = false;
                    isRunning = true;
                }
            }
        }
    }

    public void checkMessages() {
        // odbieramy komunikaty i obsługujemy je
        if (!client.isAlive()) {
            // niedobrze w sumie to problem
            return;
        }
        GameEvent ge;
        while ((ge = client.receiveMessage()) != null) {
            switch (ge.getType()) {
                case GameEvent.SB_ALL_READY:
                    isReady = true;
                    break;
                case GameEvent.SB_MOVE: {
                    String x = ge.getMessage();
                    int idx1 = x.indexOf('|');
                    int idx2 = x.indexOf('|', idx1 + 1);
                    String a = x.substring(0, idx1);
                    String b = x.substring(idx1 + 1, idx2);
                    String c = x.substring(idx2 + 1);

                    try {
                        int playerNo = Integer.parseInt(a);
                        int unitNo = Integer.parseInt(b);
                        int goingTo = Integer.parseInt(c);
                        Unit unit;
                        if (playerNo == 1) {
                            unit = player1.getUnit(unitNo);
                        } else {
                            unit = player2.getUnit(unitNo);
                        }
                        if (unit != null) {
                            unit.goTo(goingTo);
                        }
                    } catch (NumberFormatException ex) {
                    }
                    break;
                }
                case GameEvent.S_MOVE_FAIL: {
                    String x = ge.getMessage();
                    int idx1 = x.indexOf('|');
                    String a = x.substring(0, idx1);
                    String b = x.substring(idx1 + 1);

                    try {
                        int playerNo = Integer.parseInt(a);
                        int unitNo = Integer.parseInt(b);
                        Unit unit;
                        if (playerNo == 1) {
                            unit = player1.getUnit(unitNo);
                        } else {
                            unit = player2.getUnit(unitNo);
                        }
                        if (unit != null) {
                            unit.failGoTo();
                        }
                    } catch (NumberFormatException ex) {
                    }
                    break;
                }
                case GameEvent.SB_ATTACK: {
                    String x = ge.getMessage();
                    int idx1 = x.indexOf('|');
                    int idx2 = x.indexOf('|', idx1 + 1);
                    int idx3 = x.indexOf('|', idx2 + 1);
                    int idx4 = x.indexOf('|', idx3 + 1);
                    int idx5 = x.indexOf('|', idx4 + 1);
                    String a = x.substring(0, idx1);
                    String b = x.substring(idx1 + 1, idx2);
                    String c = x.substring(idx2 + 1, idx3);
                    String d = x.substring(idx3 + 1, idx4);
                    String e = x.substring(idx4 + 1, idx5);
                    String f = x.substring(idx5 + 1);

                    try {
                        int playerNo = Integer.parseInt(a);
                        int unitNo = Integer.parseInt(b);
                        int firePower = Integer.parseInt(c);
                        int X = Integer.parseInt(d);
                        int Y = Integer.parseInt(e);
                        int attackingUnitNo = Integer.parseInt(f);
                        Player p, o;
                        if (playerNo == 1) {
                            p = player1;
                            o = player2;
                        } else {
                            p = player2;
                            o = player1;
                        }

                        Unit unit = p.getUnit(unitNo);
                        Unit attackingUnit = o.getUnit(attackingUnitNo);

                        if (unit != null && attackingUnit != null) {
                            if (unit.isPlayerUnit()) {
                                unit.attack(X, Y, firePower, attackingUnitNo);
                            }
                            attackingUnit.rotateTo(X, Y);
                        }
                    } catch (NumberFormatException ex) {
                    }

                    break;
                }
                case GameEvent.SB_HIT: {
                    String x = ge.getMessage();
                    int idx1 = x.indexOf('|');
                    int idx2 = x.indexOf('|', idx1 + 1);
                    int idx3 = x.indexOf('|', idx2 + 1);
                    String a = x.substring(0, idx1);
                    String b = x.substring(idx1 + 1, idx2);
                    String c = x.substring(idx2 + 1, idx3);
                    String d = x.substring(idx3 + 1);

                    try {
                        int playerNo = Integer.parseInt(a);
                        int unitNo = Integer.parseInt(b);
                        int firePower = Integer.parseInt(c);
                        int attackingUnitNo = Integer.parseInt(d);
                        Unit unit;
                        Unit attackingUnit;
                        if (playerNo == 1) {
                            unit = player1.getUnit(unitNo);
                            attackingUnit = player2.getUnit(attackingUnitNo);
                        } else {
                            unit = player2.getUnit(unitNo);
                            attackingUnit = player1.getUnit(attackingUnitNo);
                        }

                        if (unit != null && attackingUnit != null) {
                            unit.hit(firePower);
                            Animation boom = (Animation) resources
                                    .getBoomAnimation().clone();
                            boom.start();
                            Sprite sprite = new Sprite(boom);
                            sprite.setX(unit.getX());
                            sprite.setY(unit.getY());

                            int p = Map.TILE_SIZE >> 1;

                            if (attackingUnit.getX() > unit.getX()) {
                                sprite.setDisplacementX(p);
                            } else if (attackingUnit.getX() < unit.getX()) {
                                sprite.setDisplacementX(-p);
                            }

                            if (attackingUnit.getY() > unit.getY()) {
                                sprite.setDisplacementY(p);
                            } else if (attackingUnit.getY() < unit.getY()) {
                                sprite.setDisplacementY(-p);
                            }
                            booms.add(sprite);
                        }
                    } catch (NumberFormatException ex) {
                    }
                    break;
                }
                case GameEvent.SB_DEAD: {
                    String x = ge.getMessage();
                    int idx1 = x.indexOf('|');
                    String a = x.substring(0, idx1);
                    String b = x.substring(idx1 + 1);

                    try {
                        int playerNo = Integer.parseInt(a);
                        int unitNo = Integer.parseInt(b);

                        boolean updateSelected = false;

                        Iterator i;
                        if (playerNo == 1) {
                            i = player1.getUnits();
                        } else {
                            i = player2.getUnits();
                        }

                        while (i.hasNext()) {
                            Unit unit = (Unit) i.next();
                            if (unit.getNo() == unitNo) {
                                if (unit.isSelected()) {
                                    updateSelected = true;
                                }
                                unit.relaseLand(map);
                                i.remove();
                            }
                        }

                        if (updateSelected) {
                            i = player.getSelectedUnits().iterator();

                            Unit unit = (Unit) i.next();
                            if (unit.getNo() == unitNo) {
                                i.remove();
                            }
                        }

                    } catch (NumberFormatException ex) {
                    }
                    break;

                }
                case GameEvent.SB_GAME_OVER: {
                    isRunning = false;
                    networkJoinMenu.showGameOver(ge.getMessage());
                    break;
                }
                default:
                    System.out.println("Nieznany komunikat: #" + ge.getType()
                            + "\n");
                    break;
            }
        }
    }

    public void draw(Graphics2D g) {
        if (isRunning()) {
			/*
			 * g.setBackground(Color.BLACK); g.setColor(Color.BLACK);
			 * g.fillRect(0,0,game.screen.getWidth(), game.screen.getHeight());
			 */
            map.drawMap(g, screen.getWidth(), screen.getHeight(), this); // /ble

            int p = Map.TILE_SIZE >> 1;
            int q = p >> 1;
            int r = q >> 1;
            int s = p / 5;

            // rysowanie obiektów
            for (int j = 0; j < 2; j++) {
                Iterator i;
                if (j == 0) {
                    i = player.getUnits();
                } else {
                    i = opponent.getUnits();
                }

                while (i.hasNext()) {
                    Unit unit = (Unit) i.next();

                    int x = Map.tilesToPixels(unit.getX())
                            + Math.round(unit.getDisplacementX())
                            + map.getOffsetX(screen.getWidth());
                    int y = Map.tilesToPixels(unit.getY())
                            + Math.round(unit.getDisplacementY())
                            + map.getOffsetY(screen.getHeight());

                    // zakomentowane obracanie, bo wygląda nienaturalnie
                    AffineTransform transform = new AffineTransform();
                    transform.translate(x + p + 1, y + p + 1);
                    // transform.rotate(unit.getRotate() * Math.PI / 4);
                    transform.translate(/* 1 */-p,/* 1 */-p);
                    g.drawImage(unit.getImage(), transform, null);

                    if (unit.isSelected()) {
                        g.setColor(new Color(120, 136, 152));
                        g.setStroke(new BasicStroke(1.25F));
                        g.drawOval(x/* +1 */, y/* +1 */, Map.TILE_SIZE,
                                Map.TILE_SIZE);
                    }

                    if (!unit.isPlayerUnit()) {
                        g.setColor(new Color(200, 77, 50));
                        g.setBackground(new Color(200, 77, 50));
                    } else {
                        g.setColor(new Color(47, 160, 40));
                        g.setBackground(new Color(47, 160, 40));
                    }
                    {
                        g.setStroke(new BasicStroke(0.0F));
                        g.fillRect(x + r, y + s, Math.round((p + q)
                                * unit.getLifeProportion()), s);
                        g.setColor(new Color(75, 75, 75));
                        g.setStroke(new BasicStroke(1.0F));
                        g.drawRect(x + r, y + s, p + q, s);
                    }
                }
            }

            Iterator<Sprite> i = booms.iterator();
            while (i.hasNext()) {
                Sprite sprite = i.next();

                int x = Map.tilesToPixels(sprite.getX())
                        + Math.round(sprite.getDisplacementX())
                        + map.getOffsetX(screen.getWidth());
                int y = Map.tilesToPixels(sprite.getY())
                        + Math.round(sprite.getDisplacementY())
                        + map.getOffsetY(screen.getHeight());

                g.drawImage(sprite.getImage(), x, y, null);
            }

            // rysowanie mg�y wojny
            map.drawFoog(g, screen.getWidth(), screen.getHeight());

            // rysowanie zaznaczenia
            if (player.isSelectionVisable()) {
                g.setColor(new Color(120, 136, 152));
                g.setStroke(new BasicStroke(1.2F));
                g.drawRect(player.getSelectionBeginX()
                        + map.getOffsetX(screen.getWidth()), player
                        .getSelectionBeginY()
                        + map.getOffsetY(screen.getHeight()), player
                        .getSelectionEndX()
                        - player.getSelectionBeginX(), player
                        .getSelectionEndY()
                        - player.getSelectionBeginY());
            }

            g.setColor(Color.WHITE);
            long time = this.time / 1000;
            long m = time % 60;
            long h = time / 60;
            String timeStr = (h < 10) ? "0" + h + ":" : h + ":";
            timeStr += (m < 10) ? "0" + m : Long.toString(m);
            g.drawString("Czas: " + timeStr, 25, 35);
			/*
			 * DisplayMode displayMode = game.screen.getCurrentDisplayMode();
			 * g.drawString("Rozdzielczo��: " + displayMode.getWidth() + "x" +
			 * displayMode.getHeight() + "x" + displayMode.getBitDepth(),25,50);
			 */

            if (isPause()) {
                if (gameMenu.isVisible()) {
                    gameMenu.draw(g);
                } else if (optionsMenu.isVisible()) {
                    optionsMenu.draw(g);
                }
            }
        } else {
            if (mainMenu.isVisible()) {
                mainMenu.draw(g);
            } else if (networkMenu.isVisible()) {
                networkMenu.draw(g);
            } else if (networkClientMenu.isVisible()) {
                networkClientMenu.draw(g);
            } else if (networkServerMenu.isVisible()) {
                networkServerMenu.draw(g);
            } else if (networkJoinMenu.isVisible()) {
                networkJoinMenu.draw(g);
            } else if (optionsMenu.isVisible()) {
                optionsMenu.draw(g);
            } else if (isStarting) {
                g.setBackground(Color.BLACK);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                g.drawString("Ładowanie...", 20, getHeight() - 20);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
