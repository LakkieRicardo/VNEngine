package lakkie;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class GameRenderComponent extends JPanel {
    
    private final Thread redrawThread;

    public GameRenderComponent() {
        super();
        setBackground(Color.black);
        redrawThread = new Thread(this::handleRedraw);
        redrawThread.setName("Redraw");
        redrawThread.start();
    }

    public void requestRedrawInterrupt() {
        redrawThread.interrupt();
        try {
            redrawThread.join(1000);
        } catch (InterruptedException e) {
            System.err.println("Failed to join redraw thread after sending interrupt after waiting 1s.");
            // System.exit call needed
        }
    }

    protected void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void handleRedraw() {
        final int REDRAW_PER_SEC = 60, POLLS_PER_SEC = 1;
        long lastUpdateTime = System.currentTimeMillis();
        int numRedrawsSincePoll = 0;
        long lastPollTime = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime > 1000 / REDRAW_PER_SEC) {
                repaint();
                lastUpdateTime = currentTime;
                numRedrawsSincePoll++;
            }
            if (currentTime - lastPollTime > 1000 / POLLS_PER_SEC) {
                System.out.printf("Frames per second: %d\n", numRedrawsSincePoll);
                numRedrawsSincePoll = 0;
                lastPollTime = currentTime;
            }
        }
        if (Thread.interrupted()) {
            System.out.println("Redraw thread was interrupted.");
        }
    }

}
