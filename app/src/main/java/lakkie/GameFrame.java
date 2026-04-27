package lakkie;

import javax.swing.JFrame;

import lakkie.state.GameState;

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

        gameRenderPanel = new GameRenderComponent(state);
        add(gameRenderPanel);

        setVisible(true);
        addWindowListener(this);
        addMouseListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
        gameRenderPanel.addMouseListener(this);
        gameRenderPanel.addKeyListener(this);
    }

    public boolean showTranscript() {
        return gameRenderPanel.showTranscript();
    }

    public void scrollTranscript(float amount) {
        gameRenderPanel.offsetTranscript(amount);
    }

    public void setShowTranscript(boolean showTranscript) {
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
        if (gameRenderPanel.showTranscript()) {
            return;
        }
        state.nextLine();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && !gameRenderPanel.showTranscript()) {
            state.lastLine();
        } else if (e.getKeyCode() == KeyEvent.VK_F2 || (e.getKeyCode() == KeyEvent.VK_ESCAPE && gameRenderPanel.showTranscript())) {
            gameRenderPanel.setShowTranscript(!gameRenderPanel.showTranscript());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!gameRenderPanel.showTranscript()) {
            return;
        }
        if (e.getPreciseWheelRotation() < 0.5f) {
            gameRenderPanel.offsetTranscript((float)e.getPreciseWheelRotation() * -10f);
        } else if (e.getPreciseWheelRotation() > 0.5f) {
            gameRenderPanel.offsetTranscript((float)e.getPreciseWheelRotation() * -10f);
        }
    }

}