import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

public class GameFrame extends JFrame {

    public GameFrame() {
        super("Game");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int winW = screenSize.width / 3 * 2;
        int winH = winW / 16 * 9;
        setSize(winW, winH);
        setLocationRelativeTo(null);

        add(new GameRenderComponent());

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new GameFrame();
    }

}