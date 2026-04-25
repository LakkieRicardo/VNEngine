package lakkie;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GameFrame extends JFrame implements WindowListener, MouseListener, KeyListener {

    private final GameRenderComponent gameRenderPanel;

    public GameFrame() {
        super("Super Cool Deadlock Game");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int winW = screenSize.width / 3 * 2;
        int winH = winW / 16 * 9;
        setSize(winW, winH);
        setMinimumSize(new Dimension(500, 500 / 16 * 9));
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
    
    public void setCurrentLineColor(Color color) {
        gameRenderPanel.setCurrentLineColor(color);
    }

    public void setCurrentCharName(String charName) {
        gameRenderPanel.setCurrentCharName(charName);
    }

    public void setCurrentBackdrop(BufferedImage img) {
        gameRenderPanel.setCurrentBackdrop(img);
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
    private static final Map<String, Color> charColors = new HashMap<>();
    private static final Map<String, BufferedImage> charImg = new HashMap<>();

    private static void updateText() {
        String rawLine = (String)scriptObj.getJSONArray("Lines").get(index);
        int splitterIdx = rawLine.indexOf('$');
        String characterId = rawLine.substring(0, splitterIdx);
        JSONArray charValues = scriptObj.getJSONObject("Characters").getJSONArray(characterId);
        String charName = charValues.getString(3);
        String charImgName = charValues.getString(4);
        if (!charImg.containsKey(charImgName)) {
            try {
                charImg.put(charImgName, ImageIO.read(GameRenderComponent.class.getResourceAsStream(charImgName)));
            } catch (IOException e) {
                System.err.println("Failed to load cat.png");
                e.printStackTrace();
                charImg.put(charImgName, null);
            }
        }
        Color charColor;
        if (!charColors.containsKey(characterId)) {
             charColor = new Color(charValues.getInt(0), charValues.getInt(1), charValues.getInt(2));
        } else {
            charColor = charColors.get(characterId);
        }
        String line = rawLine.substring(splitterIdx + 1);
        game.feed(line);
        game.setCurrentLineColor(charColor);
        game.setCurrentCharName(charName);
        game.setCurrentBackdrop(charImg.get(charImgName));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
        index = Math.min(scriptObj.getJSONArray("Lines").length() - 1, index + 1);
        updateText();
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