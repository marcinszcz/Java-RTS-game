package game;

/**
 * Created by marcin on 07.05.16.
 */

import java.io.*;
import java.util.LinkedList;

public class Config {

    private int mode;
    private boolean isFullscreen;
    private String host;
    private String serverNick;
    private String clientNick;
    private int port;

    public Config() {
        mode = 0;
        isFullscreen = false;
        host = "localhost";
        serverNick = "serwer";
        clientNick = "klient";
        port = 4545;
    }

    public Config(String file) {
        this();
        readConfiguration(file);
    }

    public void readConfiguration(String fileName) {
        LinkedList<String> lines = new LinkedList<String>();

        try {
            BufferedReader reader;
            reader = new BufferedReader(new FileReader(fileName));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    reader.close();
                    break;
                }

                // dodawanie wszystkich wierszy poza komentarzami
                if (!line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
        }

        while (!lines.isEmpty()) {
            String s = (String) lines.getFirst();
            lines.removeFirst();
            int idx = s.indexOf('=');
            if (idx != -1) {
                String key = s.substring(0, idx).trim();
                String value = s.substring(idx + 1).trim();

                if (key.compareToIgnoreCase("mode") == 0) {
                    try {
                        int x = Integer.parseInt(value);
                        setMode(x);
                    } catch (NumberFormatException ex) {
                    }
                } else if (key.compareToIgnoreCase("isFullScreen") == 0) {
                    try {
                        int x = Integer.parseInt(value);
                        if (x == 1) {
                            setFullscreen(true);
                        } else {
                            setFullscreen(false);
                        }
                    } catch (NumberFormatException ex) {
                    }
                } else if (key.compareToIgnoreCase("host") == 0) {
                    setHost(value);
                } else if (key.compareToIgnoreCase("serverNick") == 0) {
                    setServerNick(value);
                } else if (key.compareToIgnoreCase("clientNick") == 0) {
                    setClientNick(value);
                } else if (key.compareToIgnoreCase("port") == 0) {
                    try {
                        int x = Integer.parseInt(value);
                        setPort(x);
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        }
    }

    public void writeConfiguration(String fileName) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(new BufferedWriter(
                    new FileWriter(fileName)));
            writer.print("mode=");
            writer.println(getMode());
            writer.print("isFullScreen=");
            if (isFullscreen()) {
                writer.println("1");
            } else {
                writer.println("0");
            }
            writer.print("host=");
            writer.println(getHost());
            writer.print("port=");
            writer.println(getPort());
            writer.print("serverNick=");
            writer.println(getServerNick());
            writer.print("clientNick=");
            writer.println(getClientNick());
            writer.close();
        } catch (IOException e) {
        }
    }

    public void setMode(int mode) {
        if (mode >= 0 && mode <= 2) {
            this.mode = mode;
        }
    }

    public int getMode() {
        return mode;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setClientNick(String nick) {
        clientNick = nick;
    }

    public String getClientNick() {
        return clientNick;
    }

    public void setServerNick(String nick) {
        serverNick = nick;
    }

    public String getServerNick() {
        return serverNick;
    }

    public void setFullscreen(boolean b) {
        isFullscreen = b;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }
}
