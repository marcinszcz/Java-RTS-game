package gui;


import javax.swing.RepaintManager;
import javax.swing.JComponent;
/**
 * Created by marcin on 07.05.16.
 */
public class NullRepaintManager extends RepaintManager{

    // instaluje NullRepaintManager.
    public static void install() {
        RepaintManager repaintManager = new NullRepaintManager();
        repaintManager.setDoubleBufferingEnabled(false);
        RepaintManager.setCurrentManager(repaintManager);
    }

    public void addInvalidComponent(JComponent c) {
    }

    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
    }

    public void markCompletelyDirty(JComponent c) {
    }

    public void paintDirtyRegions() {
    }
}
