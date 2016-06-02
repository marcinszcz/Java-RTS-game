package io;

/**
 * Created by marcin on 07.05.16.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;


    public class InputManager implements KeyListener, MouseListener,
            MouseMotionListener, MouseWheelListener {

        // tworzymy niewidoczny kursor
        public static final Cursor INVISIBLE_CURSOR = Toolkit.getDefaultToolkit()
                .createCustomCursor(Toolkit.getDefaultToolkit().getImage(""),
                        new Point(0, 0), "invisible");

        // kody myszy
        public static final int MOUSE_MOVE_LEFT = 0;

        public static final int MOUSE_MOVE_RIGHT = 1;

        public static final int MOUSE_MOVE_UP = 2;

        public static final int MOUSE_MOVE_DOWN = 3;

        public static final int MOUSE_WHEEL_UP = 4;

        public static final int MOUSE_WHEEL_DOWN = 5;

        public static final int MOUSE_BUTTON_1 = 6;

        public static final int MOUSE_BUTTON_2 = 7;

        public static final int MOUSE_BUTTON_3 = 8;

        private static final int NUM_MOUSE_CODES = 9;

        // Kody klawiszy są zdefiniowane w pakiecie java.awt.KeyEvent.
        private static final int NUM_KEY_CODES = 600;

        private InputAction[] keyActions = new InputAction[NUM_KEY_CODES];

        private InputAction[] mouseActions = new InputAction[NUM_MOUSE_CODES];

        private Point mouseLocation;

        private Component comp;

        public InputManager(Component comp) {
            this.comp = comp;
            mouseLocation = new Point();

            // rejestracja klawiszy i nasłuchów dla myszy
            comp.addKeyListener(this);
            comp.addMouseListener(this);
            comp.addMouseMotionListener(this);
            comp.addMouseWheelListener(this);

            // umo�liwia przechwytywanie klawisza TAB oraz pozosta�ych
            // i innych klawiszy wykorzystywanych normalnie do zmiany fokusu.
            comp.setFocusTraversalKeysEnabled(false);
        }

        // ustawia kursor w bieżącym komponencie
        public void setCursor(Cursor cursor) {
            comp.setCursor(cursor);
        }

        public void mapToKey(InputAction inputAction, int keyCode) {
            keyActions[keyCode] = inputAction;
        }

        public void mapToMouse(InputAction inputAction, int mouseCode) {
            mouseActions[mouseCode] = inputAction;
        }

        public void clearMap(InputAction inputAction) {
            for (int i = 0; i < keyActions.length; i++) {
                if (keyActions[i] == inputAction) {
                    keyActions[i] = null;
                }
            }

            for (int i = 0; i < mouseActions.length; i++) {
                if (mouseActions[i] == inputAction) {
                    mouseActions[i] = null;
                }
            }

            inputAction.reset();
        }

        /**
         * Zwraca list� nazw klawiszy i zdarze� myszy skojarzonych z bie��cym
         * obiektem InputAction. Ka�da pozycja w List jest typu String.
         */
        public List getMaps(InputAction gameCode) {
            ArrayList<String> list = new ArrayList<String>();

            for (int i = 0; i < keyActions.length; i++) {
                if (keyActions[i] == gameCode) {
                    list.add(getKeyName(i));
                }
            }

            for (int i = 0; i < mouseActions.length; i++) {
                if (mouseActions[i] == gameCode) {
                    list.add(getMouseName(i));
                }
            }
            return list;
        }

        // kasuje nam stan klawiszy==nic nie zostało naciśnięte
        public void resetAllInputActions() {
            for (int i = 0; i < keyActions.length; i++) {
                if (keyActions[i] != null) {
                    keyActions[i].reset();
                }
            }

            for (int i = 0; i < mouseActions.length; i++) {
                if (mouseActions[i] != null) {
                    mouseActions[i].reset();
                }
            }
        }

        public static String getKeyName(int keyCode) {
            return KeyEvent.getKeyText(keyCode);
        }

        public static String getMouseName(int mouseCode) {
            switch (mouseCode) {
                case MOUSE_MOVE_LEFT:
                    return "Mysz lewo";
                case MOUSE_MOVE_RIGHT:
                    return "Mysz prawo";
                case MOUSE_MOVE_UP:
                    return "Mysz góra";
                case MOUSE_MOVE_DOWN:
                    return "Mysz dół";
                case MOUSE_WHEEL_UP:
                    return "Kółko myszy góra";
                case MOUSE_WHEEL_DOWN:
                    return "Kółko myszy dół";
                case MOUSE_BUTTON_1:
                    return "Przycisk myszy 1";
                case MOUSE_BUTTON_2:
                    return "Przycisk myszy 2";
                case MOUSE_BUTTON_3:
                    return "Przycisk myszy 3";
                default:
                    return "Nieznany kod zdarzenia myszy " + mouseCode;
            }
        }

        // zwraca wsp x z myszy
        public int getMouseX() {
            return mouseLocation.x;
        }

        public int getMouseY() {
            return mouseLocation.y;
        }

        private InputAction getKeyAction(KeyEvent e) {
            int keyCode = e.getKeyCode();
            if (keyCode < keyActions.length) {
                return keyActions[keyCode];
            } else {
                return null;
            }
        }

        public static int getMouseButtonCode(MouseEvent e) {
            switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    return MOUSE_BUTTON_1;
                case MouseEvent.BUTTON2:
                    return MOUSE_BUTTON_2;
                case MouseEvent.BUTTON3:
                    return MOUSE_BUTTON_3;
                default:
                    return -1;
            }
        }

        private InputAction getMouseButtonAction(MouseEvent e) {
            int mouseCode = getMouseButtonCode(e);
            if (mouseCode != -1) {
                return mouseActions[mouseCode];
            } else {
                return null;
            }
        }

        // z interfejsu KeyListener
        public void keyPressed(KeyEvent e) {
            InputAction inputAction = getKeyAction(e);
            if (inputAction != null) {
                inputAction.press();
            }
            // test ze zdarzenie nie będzie dalej obsługiwane- upewnienie się
            e.consume();
        }

        // z interfejsu KeyListener
        public void keyReleased(KeyEvent e) {
            InputAction inputAction = getKeyAction(e);
            if (inputAction != null) {
                inputAction.release();
            }
            e.consume();
        }

        // z interfejsu KeyListener
        public void keyTyped(KeyEvent e) {
            e.consume();
        }

        // z interfejsu MouseListener
        public void mousePressed(MouseEvent e) {
            InputAction inputAction = getMouseButtonAction(e);
            if (inputAction != null) {
                inputAction.press();
            }
        }

        // z interfejsu MouseListener
        public void mouseReleased(MouseEvent e) {
            InputAction inputAction = getMouseButtonAction(e);
            if (inputAction != null) {
                inputAction.release();
            }
        }

        // z interfejsu MouseListener
        public void mouseClicked(MouseEvent e) {
            // nic nie rób
        }

        // z interfejsu MouseListener
        public void mouseEntered(MouseEvent e) {
            mouseMoved(e);
        }

        // z interfejsu MouseListener
        public void mouseExited(MouseEvent e) {
            mouseMoved(e);
        }

        // z interfejsu MouseMotionListener
        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }

        // z interfejsu MouseMotionListener
        public synchronized void mouseMoved(MouseEvent e) {
            int dx = e.getX() - mouseLocation.x;
            int dy = e.getY() - mouseLocation.y;
            mouseHelper(MOUSE_MOVE_LEFT, MOUSE_MOVE_RIGHT, dx);
            mouseHelper(MOUSE_MOVE_UP, MOUSE_MOVE_DOWN, dy);
            mouseLocation.x = e.getX();
            mouseLocation.y = e.getY();
        }

        // z interfejsu MouseWheelListener
        public void mouseWheelMoved(MouseWheelEvent e) {
            mouseHelper(MOUSE_WHEEL_UP, MOUSE_WHEEL_DOWN, e.getWheelRotation());
        }

        private void mouseHelper(int codeNeg, int codePos, int amount) {
            InputAction inputAction;
            if (amount < 0) {
                inputAction = mouseActions[codeNeg];
            } else {
                inputAction = mouseActions[codePos];
            }

            if (inputAction != null) {
                inputAction.press(Math.abs(amount));
                inputAction.release();
            }
        }
}
