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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameFrame extends JFrame implements WindowListener, MouseListener, KeyListener, MouseWheelListener {

    private final GameRenderComponent gameRenderPanel;
    private final GameState state;

    public GameFrame(GameState state) {
        super("Super Cool Deadlock Game");
        this.state = state;
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
        addMouseWheelListener(this);
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
    
    public void setCurrentCharImg(BufferedImage img) {
        gameRenderPanel.setCurrentCharImg(img);
    }

    public void setCurrentCharIsRight(boolean isRight) {
        gameRenderPanel.setCurrentCharIsRight(isRight);
    }

    public void setCurrentTextItalics(boolean isItalics) {
        gameRenderPanel.setCurrentTextItalics(isItalics);
    }

    public void setCurrentBackdrop(BufferedImage img) {
        gameRenderPanel.setCurrentBackdrop(img);
    }

    public boolean showTranscript() {
        return gameRenderPanel.showTranscript();
    }

    public void scrollTranscript(float amount) {
        gameRenderPanel.offsetTranscript(amount);
    }

    private List<GameRenderComponent.TranscriptLine> getCurrentTranscript() {
        List<GameRenderComponent.TranscriptLine> transcript = new ArrayList<>();
        for (int i = 1; i < index + 1; i++) {
            String rawLine = scriptObj.getJSONArray("Lines").getString(i);
            String charId = rawLine.substring(0, rawLine.indexOf('$'));
            String line = rawLine.substring(rawLine.indexOf('$') + 1);
            JSONArray charValues = scriptObj.getJSONObject("Characters").getJSONArray(charId);
            String charName = charValues.getString(3);
            transcript.add(new GameRenderComponent.TranscriptLine(charName,
                new Color(charValues.getInt(0), charValues.getInt(1), charValues.getInt(2)),
                line,
                charValues.getBoolean(5)
            ));
        }

        return transcript;
    }

    public void setShowTranscript(boolean showTranscript) {
        if (showTranscript) {
            gameRenderPanel.setTranscriptState(getCurrentTranscript());
            gameRenderPanel.resetTranscriptScroll();
        }
        gameRenderPanel.setShowTranscript(showTranscript);
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
    private static int index = 1;
    private static final Map<String, Color> charColors = new HashMap<>();
    private static final Map<String, BufferedImage> charImg = new HashMap<>();

    private static void updateText() {
        String rawLine = (String)scriptObj.getJSONArray("Lines").get(index);
        int splitterIdx = rawLine.indexOf('$');
        String characterId = rawLine.substring(0, splitterIdx);
        JSONArray charValues = scriptObj.getJSONObject("Characters").getJSONArray(characterId);
        String charName = charValues.getString(3);
        String charImgName = charValues.getString(4);
        boolean isRight = charValues.getBoolean(5);
        boolean isItalics = charValues.getBoolean(6);
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
        game.setCurrentCharImg(charImg.get(charImgName));
        game.setCurrentCharIsRight(isRight);
        game.setCurrentTextItalics(isItalics);
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
        if (game.showTranscript()) {
            return;
        }
        index = Math.min(scriptObj.getJSONArray("Lines").length() - 1, index + 1);
        updateText();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !game.showTranscript()) {
            index = Math.max(1, index - 1);
            updateText();
        } else if (e.getKeyCode() == KeyEvent.VK_F2 || (e.getKeyCode() == KeyEvent.VK_ESCAPE && game.showTranscript())) {
            game.setShowTranscript(!game.showTranscript());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!game.showTranscript()) {
            return;
        }
        if (e.getPreciseWheelRotation() < 0.5f) {
            game.scrollTranscript((float)e.getPreciseWheelRotation() * -10f);
        } else if (e.getPreciseWheelRotation() > 0.5f) {
            game.scrollTranscript((float)e.getPreciseWheelRotation() * -10f);
        }
    }

}