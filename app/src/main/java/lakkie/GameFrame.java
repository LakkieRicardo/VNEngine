package lakkie;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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

    public static void main(String[] args) {
        new GameFrame();
    }

}