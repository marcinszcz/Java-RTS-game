package game;

/**
 * Created by marcin on 07.05.16.
 */

import game.objects.*;
import graphic.Animation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ImageIcon;

public class Resources {

    private ArrayList<Image> mapTiles;
    private GraphicsConfiguration gc;
    private Image[] fogImages;
    private Unit normalWarrior;
    private Unit blackNormalWarrior;
    private Unit hero;
    private Unit blackHero;
    private Animation boom;

    public Resources(GraphicsConfiguration gc) {
        this.gc = gc;
    }

    public Image loadImage(String name) {
        String filename = "images/" + name;
        return new ImageIcon(filename).getImage();
    }

    public Image getMirrorImage(Image image) {
        return getScaledImage(image, -1, 1);
    }

    public Image getFlippedImage(Image image) {
        return getScaledImage(image, 1, -1);
    }

    private Image getScaledImage(Image image, float x, float y) {
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate((x - 1) * image.getWidth(null) / 2, (y - 1)
                * image.getHeight(null) / 2);

        // tworzenie przezroczystego rysunku
        Image newImage = gc.createCompatibleImage(image.getWidth(null), image
                .getHeight(null), Transparency.BITMASK);

        Graphics2D g = (Graphics2D) newImage.getGraphics();
        g.drawImage(image, transform, null);
        g.dispose();

        return newImage;
    }

    public Map loadMap(String name, int screenWidth, int screenHeight) {
        ArrayList lines = null;
        int width = 0;
        int height = 0;

        loadTileImages(name);
        loadFogImages();

        try {
            lines = readTextFileToArrayList("maps/" + name + ".map");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // analizowanie wiersza i tworzenie obiektu TileEngine
        width = ((String) lines.get(0)).length();
        height = lines.size();
        Map newMap = new Map(width, height, screenWidth, screenHeight,
                fogImages);
        for (int y = 0; y < height; y++) {
            String line = (String) lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                if (x >= width) {
                    break;
                }
                char ch = line.charAt(x);

                // zprawdzenie, czy znak reprezentuje kafelek A, B, C itd.
                int tile = ch - 'A';
                if (tile >= 0 && tile < mapTiles.size()) {
                    newMap.setTile(x, y, (Image) mapTiles.get(tile));
                }
            }
        }

        return newMap;
    }

    public void loadTileImages(String name) {
        ArrayList lines = null;
        try {
            lines = readTextFileToArrayList("maps/" + name + ".gfx");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        mapTiles = new ArrayList<Image>();
        for (int i = 0; i < lines.size(); i++) {

            mapTiles.add(loadImage((String) lines.get(i)));
        }

        createBoomAnimation();
    }

    public void loadFogImages() {
        if (fogImages == null) {
            fogImages = new Image[17];

            fogImages[0] = loadImage("fog/fog_0.png");
            fogImages[1] = loadImage("fog/fog_1.png");
            fogImages[2] = getMirrorImage(fogImages[1]);
            fogImages[1] = getMirrorImage(fogImages[2]);
            fogImages[3] = getFlippedImage(fogImages[2]);
            fogImages[4] = getMirrorImage(fogImages[3]);
            fogImages[5] = loadImage("fog/fog_2.png");
            fogImages[6] = loadImage("fog/fog_3.png");
            fogImages[7] = getMirrorImage(fogImages[5]);
            fogImages[5] = getMirrorImage(fogImages[7]);
            fogImages[8] = getFlippedImage(fogImages[6]);
            fogImages[6] = getFlippedImage(fogImages[8]);
            fogImages[9] = loadImage("fog/fog_4.png");
            fogImages[10] = getMirrorImage(fogImages[9]);
            fogImages[9] = getMirrorImage(fogImages[10]);
            fogImages[11] = getFlippedImage(fogImages[10]);
            fogImages[12] = getMirrorImage(fogImages[11]);
            fogImages[13] = loadImage("fog/fog_5.png");
            fogImages[14] = getMirrorImage(fogImages[13]);
            fogImages[13] = getMirrorImage(fogImages[14]);
            fogImages[15] = loadImage("fog/fog_7.png");
            fogImages[16] = getMirrorImage(fogImages[15]);
            fogImages[15] = getMirrorImage(fogImages[16]);
        }
    }

    private ArrayList readTextFileToArrayList(String fileName)
            throws IOException {
        ArrayList<String> lines = new ArrayList<String>();

        // odczytanie wszystkich wierszy z pliku do listy
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                reader.close();
                break;
            }

            if (!line.startsWith("#")) {
                lines.add(line);
            }
        }

        return lines;
    }

    public LinkedList<Unit> loadUnits(String name, Player player,
                                      boolean isPlayer) {
        LinkedList<Unit> list = new LinkedList<Unit>();
        createNormalWarrior();
        createHero();
        for (int i = 0; i < 8; i++) {
            Unit newUnit;

            if (player.getPlayerNo() == 1) {
                newUnit = (i == 3) ? (Unit) hero.clone()
                        : (Unit) normalWarrior.clone();
                newUnit.setX(10 + i % 4);
                newUnit.setY(5 + i / 4);
            } else {
                newUnit = (i == 3) ? (Unit) blackHero.clone()
                        : (Unit) blackNormalWarrior.clone();
                newUnit.setX(5 + i / 4);
                newUnit.setY(10 + i % 4);
            }

            newUnit.setNo(i);
            newUnit.setPlayerUnit(isPlayer);
            newUnit.owner = player;
            newUnit.setRotate(i);
            list.add(newUnit);
        }
        return list;
    }

    private void createNormalWarrior() {
        if (normalWarrior == null) {
            Animation anim = new Animation();
            anim.addFrame(loadImage("normal_warrior.png"), 200);
            normalWarrior = new NormalWarrior(anim, anim);
        }
        if (blackNormalWarrior == null) {
            Animation anim = new Animation();
            anim.addFrame(loadImage("normal_warrior_black.png"), 200);
            blackNormalWarrior = new NormalWarrior(anim, anim);
        }
    }

    private void createHero() {
        if (hero == null) {
            Animation anim = new Animation();
            anim.addFrame(loadImage("hero.png"), 200);
            hero = new Hero(anim, anim);
        }
        if (blackHero == null) {
            Animation anim = new Animation();
            anim.addFrame(loadImage("hero_black.png"), 200);
            blackHero = new Hero(anim, anim);
        }
    }

    private void createBoomAnimation() {
        boom = new Animation();
        boom.addFrame(loadImage("boom1.png"), 200);
        boom.addFrame(loadImage("boom2.png"), 400);
        boom.addFrame(loadImage("boom3.png"), 1000);
    }

    public Animation getBoomAnimation() {
        return boom;
    }
}
