package lakkie;

import javax.swing.JFrame;

import org.json.JSONObject;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameFrame extends JFrame implements WindowListener, MouseListener, KeyListener {

    private final GameRenderComponent gameRenderPanel;

    public GameFrame() {
        super("Game");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int winW = screenSize.width / 3 * 2;
        int winH = winW / 16 * 9;
        setSize(winW, winH);
        setLocationRelativeTo(null);

        gameRenderPanel = new GameRenderComponent();
        add(gameRenderPanel);

        setVisible(true);
        addWindowListener(this);
        addMouseListener(this);
        addKeyListener(this);
        gameRenderPanel.addMouseListener(this);
        gameRenderPanel.addKeyListener(this);
    }

    public void feed(String line) {
        gameRenderPanel.feed(line);
    }

    @Override
    public void windowActivated(WindowEvent e) { }

    @Override
    public void windowClosed(WindowEvent e) { }

    @Override
    public void windowClosing(WindowEvent e) {
        gameRenderPanel.requestRedrawInterrupt();
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent e) { }

    @Override
    public void windowDeiconified(WindowEvent e) { }

    @Override
    public void windowIconified(WindowEvent e) { }

    @Override
    public void windowOpened(WindowEvent e) { }

    private static JSONObject scriptObj;
    private static GameFrame game;
    private static int index = 0;

    private static void updateText() {
        game.feed((String)scriptObj.getJSONArray("Lines").get(index));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        index = Math.min(scriptObj.getJSONArray("Lines").length() - 1, index + 1);
        updateText();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            index = Math.max(0, index - 1);
            updateText();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) throws IOException {
        game = new GameFrame();

        InputStream scriptStream = GameFrame.class.getResourceAsStream("/TestScript.json");
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(scriptStream));
        StringBuilder scriptContents = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            scriptContents.append(line);
        }

        scriptObj = new JSONObject(scriptContents.toString());
        updateText();
    }

}