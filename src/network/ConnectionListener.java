package network;

/**
 * Created by marcin on 07.05.16.
 */
import game.Game;
import game.objects.Unit;

import java.util.Iterator;
import java.util.LinkedList;

public class ConnectionListener extends Thread{
    private Server server;

    private boolean canJoinToGame = false;

    private int clientsCount = 0;

    private int joinedClientsCount = 0;

    private int readyClientsCount = 0;

    private boolean freeLand[][];

    private int width;

    private int height;

    private LinkedList<ListElement> p1;

    private LinkedList<ListElement> p2;

    private String n1;

    private String n2;

    private class ListElement {
        public ListElement(int no, int x, int y) {
            this.no = no;
            this.x = x;
            this.y = y;
        }

        public int no;

        private int x;

        private int y;

        public void update(int r) {
            switch (r) {
                case 0:
                    y -= 1;
                    break;
                case 1:
                    x += 1;
                    y -= 1;
                    break;
                case 2:
                    x += 1;
                    break;
                case 3:
                    x += 1;
                    y += 1;
                    break;
                case 4:
                    y += 1;
                    break;
                case 5:
                    x -= 1;
                    y += 1;
                    break;
                case 6:
                    x -= 1;
                    break;
                case 7:
                    x -= 1;
                    y -= 1;
                    break;
            }
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private void canJoinToGame(boolean b) {
        canJoinToGame = b;
        GameEvent geOut;
        if (canJoinToGame) {
            geOut = new GameEvent(GameEvent.SB_CAN_JOIN_GAME);
        } else {
            geOut = new GameEvent(GameEvent.SB_CANNOT_JOIN_GAME);
        }
        sendBroadcastMessage(geOut);
    }

    private void startGame() {
        readyClientsCount = 0;
        GameEvent geOut;
        geOut = new GameEvent(GameEvent.SB_START_GAME);
        int count = 1;
        Iterator i = server.connections.iterator();
        while (i.hasNext()) {
            Connection connection = (Connection) i.next();
            if (connection.isAlive() && connection.isJoined()) {
                geOut.setMessage(Integer.toString(count));
                sendMessage(connection, geOut);
                if (count == 1) {
                    n1 = connection.getNick();
                } else {
                    n2 = connection.getNick();
                }
                count++;
            }
        }
    }

    public ConnectionListener(Server server) {
        this.server = server;
    }

    public void run() {
        while (server.isRunning()) {
            for (int i = server.connections.size() - 1; i >= 0; --i) {
                Connection connection = (Connection) server.connections.get(i);

                if (!connection.isAlive()) {
                    if (connection.getNick() != "") {
                        GameEvent geOut;
                        geOut = new GameEvent(GameEvent.SB_CHAT_MSG,
                                "Odłączył się: \"" + connection.getNick()
                                        + "\"");
                        sendBroadcastMessage(geOut);
                        clientsCount--;
                        joinedClientsCount = 0;
                        canJoinToGame(false);
                    }
                    connection.close();
                    server.connections.remove(connection);
                } else {
                    GameEvent ge;
                    while ((ge = receiveMessage(connection)) != null) {
                        switch (ge.getType()) {
                            case GameEvent.C_CHAT_MSG:
                                if (ge.getPlayerId() != "") {
                                    GameEvent geOut;
                                    geOut = new GameEvent(GameEvent.SB_CHAT_MSG,
                                            "[" + ge.getPlayerId() + "]:"
                                                    + ge.getMessage());
                                    sendBroadcastMessage(geOut);
                                }
                                break;
                            case GameEvent.C_LOGIN:
                                if (ge.getPlayerId() != "") {
                                    if (clientsCount == 2) {
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.S_LOGIN_FAIL,
                                                "W grze znajduje się już dwóch graczy!");
                                        sendMessage(connection, geOut);
                                        geOut = new GameEvent(
                                                GameEvent.S_DISCONNECT);
                                        sendMessage(connection, geOut);
                                    } else if (isPlayerIDUnique(ge.getPlayerId())) {
                                        connection.setNick(ge.getPlayerId());
                                        GameEvent geOut;
                                        geOut = new GameEvent(GameEvent.SB_LOGIN,
                                                ge.getPlayerId());
                                        sendBroadcastMessage(geOut);
                                        clientsCount++;
                                        if (clientsCount == 2) {
                                            canJoinToGame(true);
                                        }
                                    } else {
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.S_LOGIN_FAIL,
                                                "Użytkownik \"" + ge.getPlayerId()
                                                        + "\" już istnieje");
                                        sendMessage(connection, geOut);
                                        geOut = new GameEvent(
                                                GameEvent.S_DISCONNECT);
                                        sendMessage(connection, geOut);
                                    }
                                }
                                break;
                            case GameEvent.C_JOIN_GAME:
                                if (connection.getNick() != "") {
                                    if (clientsCount != 2) {
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.S_JOIN_GAME_FAIL);
                                        sendMessage(connection, geOut);
                                    } else {
                                        connection.setJoined(true);
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.S_JOIN_GAME_OK);
                                        sendMessage(connection, geOut);
                                        geOut = new GameEvent(
                                                GameEvent.SB_PLAYER_JOINED, ge
                                                .getPlayerId());
                                        sendBroadcastMessage(geOut);

                                        joinedClientsCount++;
                                        if (joinedClientsCount == 2) {
                                            startGame();
                                        }
                                    }
                                }
                                break;
                            case GameEvent.C_READY:
                                if (connection.getNick() != "") {
                                    readyClientsCount++;
                                    if (readyClientsCount == 2) {
                                        createFreeLand();
                                        createUnitsInfo();
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.SB_ALL_READY);
                                        sendBroadcastMessage(geOut);
                                    }
                                }
                                break;
                            case GameEvent.C_MOVE: {
                                String x = ge.getMessage();
                                int idx1 = x.indexOf('|');
                                int idx2 = x.indexOf('|', idx1 + 1);
                                int idx3 = x.indexOf('|', idx2 + 1);
                                int idx4 = x.indexOf('|', idx3 + 1);
                                String a = x.substring(0, idx1);
                                String b = x.substring(idx1 + 1, idx2);
                                String c = x.substring(idx2 + 1, idx3);
                                String d = x.substring(idx3 + 1, idx4);
                                String e = x.substring(idx4 + 1);

                                try {
                                    int playerNo = Integer.parseInt(a);
                                    int unitNo = Integer.parseInt(b);
                                    int goingTo = Integer.parseInt(c);
                                    int X = Integer.parseInt(d);
                                    int Y = Integer.parseInt(e);

                                    if (checkMove(X, Y, goingTo)) {
                                        updateUnitInfo(playerNo, unitNo, goingTo);
                                        GameEvent geOut;
                                        geOut = new GameEvent(GameEvent.SB_MOVE,
                                                playerNo + "|" + unitNo + "|"
                                                        + goingTo);
                                        sendBroadcastMessage(geOut);
                                    } else {
                                        GameEvent geOut;
                                        geOut = new GameEvent(
                                                GameEvent.S_MOVE_FAIL, playerNo
                                                + "|" + unitNo);
                                        sendMessage(connection, geOut);
                                    }
                                } catch (NumberFormatException ex) {
                                }
                            }
                            break;
                            case GameEvent.C_ATTACK: {
                                GameEvent geOut;
                                geOut = new GameEvent(GameEvent.SB_ATTACK, ge
                                        .getMessage());
                                sendBroadcastMessage(geOut);
                            }
                            break;
                            case GameEvent.C_HIT: {
                                GameEvent geOut;
                                geOut = new GameEvent(GameEvent.SB_HIT, ge
                                        .getMessage());
                                sendBroadcastMessage(geOut);
                            }
                            break;
                            case GameEvent.C_DEAD: {
                                String x = ge.getMessage();
                                int idx1 = x.indexOf('|');
                                String a = x.substring(0, idx1);
                                String b = x.substring(idx1 + 1);

                                try {
                                    int playerNo = Integer.parseInt(a);
                                    int unitNo = Integer.parseInt(b);
                                    deleteUnit(playerNo, unitNo);
                                } catch (NumberFormatException ex) {
                                }

                                GameEvent geOut;
                                geOut = new GameEvent(GameEvent.SB_DEAD, ge
                                        .getMessage());
                                sendBroadcastMessage(geOut);
                            }
                            break;
                            case GameEvent.C_PLAYER_DEAD: {
                                String x = ge.getMessage();

                                try {
                                    int playerNo = Integer.parseInt(x);
                                    String nick = (playerNo == 1) ? n2 : n1;
                                    GameEvent geOut;
                                    geOut = new GameEvent(GameEvent.SB_GAME_OVER,
                                            "Wygrał gracz: \"" + nick + "\"");
                                    sendBroadcastMessage(geOut);
                                } catch (NumberFormatException ex) {
                                }
                            }
                            break;
                        }
                    }
                }
            }

            try {
                Thread.sleep(50);
            } catch (Exception ex) {
            }
        }
    }

    public void sendMessage(Connection connection, GameEvent ge) {
        connection.sendMessage(ge.toSend());
    }

    public void sendBroadcastMessage(GameEvent ge) {
        Iterator i = server.connections.iterator();
        while (i.hasNext()) {
            Connection connection = (Connection) i.next();
            if (connection.isAlive()) {
                sendMessage(connection, ge);
            }
        }
    }

    public GameEvent receiveMessage(Connection connection) {
        if (connection.messagesQueue.isEmpty()) {
            return null;
        } else {
            GameEvent ge = new GameEvent((String) connection.messagesQueue
                    .getFirst());
            connection.messagesQueue.removeFirst();
            return ge;
        }
    }

    public boolean isPlayerIDUnique(String nick) {
        Iterator i = server.connections.iterator();
        while (i.hasNext()) {
            Connection connection = (Connection) i.next();
            if (connection.getNick().compareTo(nick) == 0)
                return false;
        }
        return true;
    }

    private void createFreeLand() {
        Game game = server.getGameInstance();
        width = game.map.getWidth();
        height = game.map.getHeight();

        freeLand = new boolean[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                freeLand[i][j] = game.map.isFree(i, j);
    }

    private void createUnitsInfo() {
        Game game = server.getGameInstance();
        p1 = new LinkedList<ListElement>();
        p2 = new LinkedList<ListElement>();

        Iterator i;
        LinkedList<ListElement> list;

        for (int j = 1; j <= 2; j++) {
            if (j == 1) {
                i = game.player1.getUnits();
                list = p1;
            } else {
                i = game.player2.getUnits();
                list = p2;
            }

            while (i.hasNext()) {
                Unit unit = (Unit) i.next();
                list
                        .add(new ListElement(unit.getNo(), unit.getX(), unit
                                .getY()));
            }
        }
    }

    private void updateUnitInfo(int playerNo, int unitNo, int goingTo) {
        LinkedList<ListElement> list;
        if (playerNo == 1) {
            list = p1;
        } else {
            list = p2;
        }

        Iterator<ListElement> i = list.iterator();
        ListElement element = null;

        while (i.hasNext()) {
            ListElement tmp = i.next();
            if (tmp.no == unitNo) {
                element = tmp;
            }
        }

        if (element != null) {
            element.update(goingTo);
        }
    }

    private void deleteUnit(int playerNo, int unitNo) {
        LinkedList<ListElement> list;
        if (playerNo == 1) {
            list = p1;
        } else {
            list = p2;
        }

        Iterator<ListElement> i = list.iterator();

        while (i.hasNext()) {
            ListElement element = i.next();
            if (element.no == unitNo) {
                setFree(element.getX(), element.getY(), true);
                i.remove();
            }
        }

    }

    private void setFree(int x, int y, boolean b) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            freeLand[x][y] = b;
        }
    }

    /* private */public boolean isFree(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return freeLand[x][y];
        } else
            return false;
    }

    private boolean checkMove(int x, int y, int goingTo) {
        boolean result = false;
        switch (goingTo) {
            case 0:
                if (isFree(x, y - 1)) {
                    setFree(x, y - 1, false);
                    result = true;
                }
                break;
            case 1:
                if (isFree(x + 1, y - 1)) {
                    setFree(x + 1, y - 1, false);
                    result = true;
                }
                break;
            case 2:
                if (isFree(x + 1, y)) {
                    setFree(x + 1, y, false);
                    result = true;
                }
                break;
            case 3:
                if (isFree(x + 1, y + 1)) {
                    setFree(x + 1, y + 1, false);
                    result = true;
                }
                break;
            case 4:
                if (isFree(x, y + 1)) {
                    setFree(x, y + 1, false);
                    result = true;
                }
                break;
            case 5:
                if (isFree(x - 1, y + 1)) {
                    setFree(x - 1, y + 1, false);
                    result = true;
                }
                break;
            case 6:
                if (isFree(x - 1, y)) {
                    setFree(x - 1, y, false);
                    result = true;
                }
                break;
            case 7:
                if (isFree(x - 1, y - 1)) {
                    setFree(x - 1, y - 1, false);
                    result = true;
                }
                break;
        }

        if (result == true) {
            setFree(x, y, true);
        }

        return result;
    }
}
