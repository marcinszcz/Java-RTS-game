package network;

/**
 * Created by marcin on 07.05.16.
 */
public class GameEvent {
    // stałe EventType
    // C_* (Client) - zdarzenia wysyłane przez klienta
    // S_* (Server) - zdarzenia wysyłane przez serwer
    // SB_* (Server broadcast) - zdarzenia wysyłane przez serwer do wszystkich klientow

    public static final int C_LOGIN = 1001;

    public static final int S_LOGIN_FAIL = 1002;

    public static final int SB_LOGIN = 1003;

    public static final int C_LOGOUT = 1004;

    public static final int SB_LOGOUT = 1005;

    public static final int S_DISCONNECT = 1006;

    public static final int SB_CAN_JOIN_GAME = 1101;

    public static final int SB_CANNOT_JOIN_GAME = 1102;

    public static final int C_JOIN_GAME = 1103;

    public static final int S_JOIN_GAME_OK = 1104;

    public static final int S_JOIN_GAME_FAIL = 1105;

    public static final int SB_PLAYER_JOINED = 1106;

    public static final int SB_START_GAME = 1107;

    public static final int C_QUIT_GAME = 1108;

    public static final int SB_PLAYER_QUIT = 1109;

    public static final int C_READY = 1110;

    public static final int SB_ALL_READY = 1111;

    public static final int C_CHAT_MSG = 1201;

    public static final int SB_CHAT_MSG = 1202;

    public static final int C_MOVE = 1301;

    public static final int SB_MOVE = 1302;

    public static final int S_MOVE_FAIL = 1303;

    public static final int C_ATTACK = 1304;

    public static final int SB_ATTACK = 1305;

    public static final int C_HIT = 1306;

    public static final int SB_HIT = 1307;

    public static final int C_DEAD = 1308;

    public static final int SB_DEAD = 1309;

    public static final int C_PLAYER_DEAD = 1310;

    public static final int SB_GAME_OVER = 1311;

    // -----------------------------------------------------

    private int eventType;

   // kto przesyla wiadomosc
    private String playerId = "";

    // tresc wiadomosci
    private String message;

    public GameEvent() {

    }

    public GameEvent(int type) {
        setType(type);
    }

    public GameEvent(int type, String message) {
        this(type);
        this.message = message;
    }

    public GameEvent(String receivedMessage) {
        String x = receivedMessage;
        int idx1 = x.indexOf('|');
        int idx2 = x.indexOf('|', idx1 + 1);
        String a = x.substring(0, idx1);
        String b = x.substring(idx1 + 1, idx2);
        String c = x.substring(idx2 + 1);
        try {
            setType(Integer.parseInt(a));
        } catch (NumberFormatException ex) {
            setType(-1);
        }
        setPlayerId(b);
        setMessage(c);
    }

    public String toSend() {
        String toSend = eventType + "|" + playerId + "|" + getMessage();
        return toSend;
    }

    public void setType(int type) {
        eventType = type;
    }

    public int getType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String id) {
        playerId = id;
    }
}
