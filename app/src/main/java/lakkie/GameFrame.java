package lakkie;

import javax.swing.JFrame;

import org.json.JSONObject;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameFrame extends JFrame implements WindowListener {

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

    public static void main(String[] args) throws IOException {
        new GameFrame();

        InputStream scriptStream = GameFrame.class.getResourceAsStream("/TestScript.json");
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(scriptStream));
        StringBuilder scriptContents = new StringBuilder();
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            scriptContents.append(line);
        }

        JSONObject scriptObj = new JSONObject(scriptContents.toString());
        System.out.println(scriptObj);
    }

}