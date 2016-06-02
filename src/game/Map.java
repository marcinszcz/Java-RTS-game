package game;

import game.objects.Unit;
/**
 * Created by marcin on 07.05.16.
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import algorithm.AStarNode;

public class Map {

    public static final int TILE_SIZE = 32;

    // rozmiar kafelka w bitach
    // Math.pow(2, TILE_SIZE_BITS) == TILE_SIZE
    public static final int TILE_SIZE_BITS = 5;

    private Image[][] tiles;

    private Image fogImages[];

    private int[][] fog;

    private boolean[][] visited;

    private boolean[][] freeLand;

    private AStarNode[][] nodes;

    private float positionX;

    private float positionY;

    public static final float SHIFT_SPEED = 0.4f;

    public Map(int width, int height, int screenWidth, int screenHeight,
               Image[] images) {
        tiles = new Image[width][height];
        positionX = (float) screenWidth / 2;
        positionY = (float) screenHeight / 2;
        fogImages = images;
        createFog(width, height);
        createVisited(width, height);
        createFreeLand(width, height);
        createNodes(width, height);
    }

    private void createFog(int width, int height) {
        fog = new int[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                fog[i][j] = 0;
    }

    private void createVisited(int width, int height) {
        visited = new boolean[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                visited[i][j] = false;
    }

    private void createFreeLand(int width, int height) {
        freeLand = new boolean[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                freeLand[i][j] = true;
    }

    private void createNodes(int width, int height) {
        nodes = new AStarNode[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                nodes[i][j] = new AStarNode(i, j);
    }

    // zwraca ilosc "kafelkow" na szerokosc
    public int getWidth() {
        return tiles.length;
    }

    public int getHeight() {
        return tiles[0].length;
    }

    public void visit(int x, int y) {
        if (!visited[x][y]) {
            visited[x][y] = true;
            clearFog(x, y);
        }
    }

    public boolean isVisible(int x, int y) {
        return (getFog(x, y) != -1) ? true : false;
    }

    public AStarNode getNode(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return nodes[x][y];
        } else
            return null;
    }

    public AStarNode getCloseNode(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            LinkedList<AStarNode> list = new LinkedList<AStarNode>();
            int i = 0;
            while (list.isEmpty()) {
                i++;
                for (int j = x - i; j <= x + i; j++) {
                    for (int k = y - i; k <= y + i; k++) {
                        if (j == x - i || j == x + i || k == y - i
                                || k == y + i) {
                            if (isFree(j, k)) {
                                list.add(nodes[j][k]);
                            }
                        }
                    }
                }
            }

            Random r = new Random();
            return (AStarNode) list.get(r.nextInt(list.size()));
        }
        return null;
    }

    private AStarNode getNeighbor(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            if (isFree(x, y))
                return nodes[x][y];
            else
                return null;
        } else
            return null;
    }

    public ArrayList buildNeighborList(int x, int y) {
        ArrayList<AStarNode> list = new ArrayList<AStarNode>();
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            for (int i = x - 1; i <= x + 1; i++)
                for (int j = y - 1; j <= y + 1; j++) {
                    if (i != x || j != y) {
                        AStarNode node;
                        node = getNeighbor(i, j);
                        if (node != null) {
                            list.add(node);
                        }
                    }
                }
        }
        return list;
    }

    public void setFree(int x, int y, boolean b) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            freeLand[x][y] = b;
        }
    }

    public boolean isFree(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return freeLand[x][y];
        } else
            return false;
    }

    public void setFree(Iterator i) {
        while (i.hasNext()) {
            Unit unit = (Unit) i.next();
            setFree(unit.getX(), unit.getY(), false);
        }
    }

    private Image getFogImage(int x, int y) {
        int tmp = getFog(x, y);
        if (tmp < 0 || tmp >= fogImages.length) {
            return null;
        } else {
            return fogImages[tmp];
        }
    }

    private int getFog(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return -1;
        } else if (fog[x][y] < 0 || fog[x][y] >= fogImages.length) {
            return -1;
        } else {
            return fog[x][y];
        }
    }

    private void setFog(int x, int y, int f) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            fog[x][y] = f;
        }
    }

    private void clearFog(int x, int y) {

        for (int i = -2; i <= 2; i++) {
            if (i == -2 || i == 2) {
                for (int j = x - 1; j <= x + 1; j++)
                    setFog(j, y + i, -1);
            } else {
                for (int j = x - 2; j <= x + 2; j++)
                    setFog(j, y + i, -1);
            }
        }

        for (int i = -1; i <= 1; i++) {
            switch (getFog(x + i, y - 3)) {
                case 0:
                case 1:
                case 2:
                    setFog(x + i, y - 3, 6);
                    break;
                case 3:
                case 7:
                case 15:
                    setFog(x + i, y - 3, 10);
                    break;
                case 4:
                case 5:
                case 16:
                    setFog(x + i, y - 3, 9);
                    break;
                case 8:
                case 11:
                case 12:
                    setFog(x + i, y - 3, -1);
                    break;
            }

            switch (getFog(x + i, y + 3)) {
                case 0:
                case 3:
                case 4:
                    setFog(x + i, y + 3, 8);
                    break;
                case 1:
                case 5:
                case 15:
                    setFog(x + i, y + 3, 12);
                    break;
                case 2:
                case 7:
                case 16:
                    setFog(x + i, y + 3, 11);
                    break;
                case 6:
                case 9:
                case 10:
                    setFog(x + i, y + 3, -1);
                    break;
            }

            switch (getFog(x - 3, y + i)) {
                case 0:
                case 1:
                case 4:
                    setFog(x - 3, y + i, 5);
                    break;
                case 2:
                case 6:
                case 16:
                    setFog(x - 3, y + i, 9);
                    break;
                case 3:
                case 8:
                case 15:
                    setFog(x - 3, y + i, 12);
                    break;
                case 7:
                case 10:
                case 11:
                    setFog(x - 3, y + i, -1);
                    break;
            }

            switch (getFog(x + 3, y + i)) {
                case 0:
                case 2:
                case 3:
                    setFog(x + 3, y + i, 7);
                    break;
                case 1:
                case 6:
                case 15:
                    setFog(x + 3, y + i, 10);
                    break;
                case 4:
                case 8:
                case 16:
                    setFog(x + 3, y + i, 11);
                    break;
                case 5:
                case 9:
                case 12:
                    setFog(x + 3, y + i, -1);
                    break;
            }
        }

        for (int i = 0; i <= 1; i++) {
            switch (getFog(x - 3 + i, y - 2 - i)) {
                case 0:
                    setFog(x - 3 + i, y - 2 - i, 1);
                    break;
                case 2:
                    setFog(x - 3 + i, y - 2 - i, 6);
                    break;
                case 3:
                    setFog(x - 3 + i, y - 2 - i, 15);
                    break;
                case 4:
                    setFog(x - 3 + i, y - 2 - i, 5);
                    break;
                case 7:
                    setFog(x - 3 + i, y - 2 - i, 10);
                    break;
                case 8:
                    setFog(x - 3 + i, y - 2 - i, 12);
                    break;
                case 11:
                    setFog(x - 3 + i, y - 2 - i, -1);
                    break;
            }

            switch (getFog(x + 3 - i, y - 2 - i)) {
                case 0:
                    setFog(x + 3 - i, y - 2 - i, 2);
                    break;
                case 1:
                    setFog(x + 3 - i, y - 2 - i, 6);
                    break;
                case 3:
                    setFog(x + 3 - i, y - 2 - i, 7);
                    break;
                case 4:
                    setFog(x + 3 - i, y - 2 - i, 16);
                    break;
                case 5:
                    setFog(x + 3 - i, y - 2 - i, 9);
                    break;
                case 8:
                    setFog(x + 3 - i, y - 2 - i, 11);
                    break;
                case 12:
                    setFog(x + 3 - i, y - 2 - i, -1);
                    break;
            }

            switch (getFog(x + 3 - i, y + 2 + i)) {
                case 0:
                    setFog(x + 3 - i, y + 2 + i, 3);
                    break;
                case 1:
                    setFog(x + 3 - i, y + 2 + i, 15);
                    break;
                case 2:
                    setFog(x + 3 - i, y + 2 + i, 7);
                    break;
                case 4:
                    setFog(x + 3 - i, y + 2 + i, 8);
                    break;
                case 5:
                    setFog(x + 3 - i, y + 2 + i, 12);
                    break;
                case 6:
                    setFog(x + 3 - i, y + 2 + i, 10);
                    break;
                case 9:
                    setFog(x + 3 - i, y + 2 + i, -1);
                    break;
            }

            switch (getFog(x - 3 + i, y + 2 + i)) {
                case 0:
                    setFog(x - 3 + i, y + 2 + i, 4);
                    break;
                case 1:
                    setFog(x - 3 + i, y + 2 + i, 5);
                    break;
                case 2:
                    setFog(x - 3 + i, y + 2 + i, 16);
                    break;
                case 3:
                    setFog(x - 3 + i, y + 2 + i, 8);
                    break;
                case 6:
                    setFog(x - 3 + i, y + 2 + i, 9);
                    break;
                case 7:
                    setFog(x - 3 + i, y + 2 + i, 11);
                    break;
                case 10:
                    setFog(x - 3 + i, y + 2 + i, -1);
                    break;
            }
        }

        {
            switch (getFog(x - 2, y - 2)) {
                case 0:
                case 1:
                case 2:
                case 4:
                case 5:
                case 6:
                case 16:
                    setFog(x - 2, y - 2, 9);
                    break;
                case 3:
                case 10:
                case 11:
                case 12:
                case 15:
                    setFog(x - 2, y - 2, -1);
                    break;
            }

            switch (getFog(x + 2, y - 2)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 6:
                case 7:
                case 15:
                    setFog(x + 2, y - 2, 10);
                    break;
                case 4:
                case 9:
                case 11:
                case 12:
                case 16:
                    setFog(x + 2, y - 2, -1);
                    break;
            }

            switch (getFog(x + 2, y + 2)) {
                case 0:
                case 2:
                case 3:
                case 4:
                case 7:
                case 8:
                case 16:
                    setFog(x + 2, y + 2, 11);
                    break;
                case 1:
                case 9:
                case 10:
                case 12:
                case 15:
                    setFog(x + 2, y + 2, -1);
                    break;
            }

            switch (getFog(x - 2, y + 2)) {
                case 0:
                case 1:
                case 3:
                case 4:
                case 5:
                case 8:
                case 15:
                    setFog(x - 2, y + 2, 12);
                    break;
                case 2:
                case 9:
                case 10:
                case 11:
                case 16:
                    setFog(x - 2, y + 2, -1);
                    break;
            }
        }
    }

    // rysowanie kafelkow lub zwracanie null jak nie ma kafelkow
    public Image getTile(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return null;
        } else {
            return tiles[x][y];
        }
    }

    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }

    public float getX() {
        return positionX;
    }

    public void setX(float x) {
        positionX = x;
    }

    public float getY() {
        return positionY;
    }

    public void setY(float y) {
        positionY = y;
    }

    public void shiftLeft(long elapsedTime, int screenWidth) {
        positionX -= elapsedTime * SHIFT_SPEED;
        if (positionX < screenWidth / 2) {
            positionX = screenWidth / 2;
        }
    }

    public void shiftRight(long elapsedTime, int screenWidth) {
        positionX += elapsedTime * SHIFT_SPEED;
        if (positionX > tilesToPixels(getWidth()) - screenWidth / 2) {
            positionX = tilesToPixels(getWidth()) - screenWidth / 2;
        }
    }

    public void shiftUp(long elapsedTime, int screenHeight) {
        positionY -= elapsedTime * SHIFT_SPEED;
        if (positionY < screenHeight / 2) {
            positionY = screenHeight / 2;
        }
    }

    public void shiftDown(long elapsedTime, int screenHeight) {
        positionY += elapsedTime * SHIFT_SPEED;
        if (positionY > tilesToPixels(getHeight()) - screenHeight / 2) {
            positionY = tilesToPixels(getHeight()) - screenHeight / 2;
        }
    }

    public static int pixelsToTiles(float pixels) {
        return pixelsToTiles(Math.round(pixels));
    }

    public static int pixelsToTiles(int pixels) {
        // wykorzystanie przesuwania do korygowania ujemnych wpolrzednych
        return pixels >> TILE_SIZE_BITS;

    }

    public static int tilesToPixels(int numTiles) {
        return numTiles << TILE_SIZE_BITS;
    }

    public int getOffsetX(int screenWidth) {
        int mapWidth = tilesToPixels(getWidth());

        int offsetX = screenWidth / 2 - Math.round(getX()) - TILE_SIZE;
        offsetX = Math.min(offsetX, 0);
        offsetX = Math.max(offsetX, screenWidth - mapWidth);
        return offsetX;
    }

    public int getOffsetY(int screenHeight) {
        int mapHight = tilesToPixels(getHeight());
        int offsetY = screenHeight / 2 - Math.round(getY()) - TILE_SIZE;
        offsetY = Math.min(offsetY, 0);
        offsetY = Math.max(offsetY, screenHeight - mapHight);
        return offsetY;
    }

    public void drawMap(Graphics2D g, int screenWidth, int screenHeight,
                        Game game) {
        int offsetX = getOffsetX(screenWidth);
        int offsetY = getOffsetY(screenHeight);

        g.setColor(Color.red);
        g.fillRect(0, 0, screenWidth, screenHeight);

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                Image image = getTile(x, y);

                if (image != null /* && isFree(x,y) */) {
                    g.drawImage(image, tilesToPixels(x) + offsetX,
                            tilesToPixels(y) + offsetY, null);
                }
            }
        }
    }

    public void drawFoog(Graphics2D g, int screenWidth, int screenHeight) {
        int offsetX = getOffsetX(screenWidth);
        int offsetY = getOffsetY(screenHeight);

        int firstTileX = pixelsToTiles(-offsetX);
        int lastTileX = firstTileX + pixelsToTiles(screenWidth) + 1;
        for (int y = 0; y < getHeight(); y++) {
            for (int x = firstTileX; x <= lastTileX; x++) {
                Image fogImage = getFogImage(x, y);
                if (fogImage != null) {
                    g.drawImage(fogImage, tilesToPixels(x) + offsetX,
                            tilesToPixels(y) + offsetY, null);
                }
            }
        }
    }
}
