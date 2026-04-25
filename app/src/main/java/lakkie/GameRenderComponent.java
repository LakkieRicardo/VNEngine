package lakkie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

public class GameRenderComponent extends JPanel {
    
    private Font loadedMedium, loadedSemibold, loadedThin;
    private Font dlgFont_Medium, dlgFont_Semibold, dlgFont_Thin;
    private final Thread redrawThread;
    private String currentLine = "", currentCharName = "";
    private Color currentColor = Color.white;
    private BufferedImage currentBackdrop;
    private Thread uiDrawThread = null;

    public GameRenderComponent() {
        super();
        setBackground(Color.black);
        try {
            loadedMedium = Font.createFont(Font.TRUETYPE_FONT, GameRenderComponent.class.getResourceAsStream("/valveoracle-medium.ttf"));
            dlgFont_Medium = loadedMedium.deriveFont(32.f);
            loadedSemibold = Font.createFont(Font.TRUETYPE_FONT, GameRenderComponent.class.getResourceAsStream("/valveoracle-semibold.ttf"));
            dlgFont_Semibold = loadedSemibold.deriveFont(32.f);
            loadedThin = Font.createFont(Font.TRUETYPE_FONT, GameRenderComponent.class.getResourceAsStream("/valveoracle-thin.ttf"));
            dlgFont_Thin = loadedThin.deriveFont(32.f);
        } catch (FontFormatException | IOException e) {
            System.out.println("Failed to initialize valve oracle font. Defaulting to Times New Roman.");
            e.printStackTrace();
            dlgFont_Medium = new Font("Times New Roman", Font.PLAIN, 32);
            dlgFont_Semibold = new Font("Times New Roman", Font.BOLD, 32);
            dlgFont_Thin = new Font("Times New Roman", Font.PLAIN, 32);
        }

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

    public void feed(String line) {
        currentLine = line;
    }

    public void setCurrentLineColor(Color color) {
        currentColor = color;
    }

    public void setCurrentCharName(String charName) {
        currentCharName = charName;
    }

    public void setCurrentBackdrop(BufferedImage img) {
        currentBackdrop = img;
    }

    private String[] wordSplit(String line) {
        if (line.contains(" ")) {
            return line.split(" ");
        } else {
            return new String[] { line };
        }
    }

    protected void paintComponent(Graphics g) {
        uiDrawThread = Thread.currentThread();
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (currentLine.length() == 0) {
            return;
        }
        
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedMedium);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedSemibold);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedThin);
        
        dlgFont_Medium = new Font("VALVE Oracle Medium", Font.PLAIN, 32);

        g2d.setColor(new Color(0x101010));
        g2d.fillRect(0, getHeight() - 48 * 6, getWidth(), 48 * 6);
        g2d.setFont(dlgFont_Medium);
        g2d.setColor(currentColor);
        FontMetrics metrics = g2d.getFontMetrics();
        String[] words = wordSplit(currentLine);
        StringBuilder nextLine = new StringBuilder();
        int numLines = 0;
        if (currentBackdrop != null) {
            g2d.drawImage(currentBackdrop, 0, 0, getWidth(), getHeight() - 48 * 6, null);
        }
        g2d.drawString(currentCharName, 32, getHeight() - 48 * 5);
        g2d.setStroke(new BasicStroke(4.f));
        g2d.drawLine(metrics.stringWidth(currentCharName) + 32 + 16, getHeight() - 48 * 5 - 8, getWidth() - 32, getHeight() - 48 * 5 - 8);
        for (String word : words) {
            String lineToRender = nextLine.toString();
            nextLine.append(word);
            nextLine.append(' ');
            int lineWidth = metrics.stringWidth(nextLine.toString());
            int maxWidth = getWidth() - 16;
            if (lineWidth > maxWidth) {
                g2d.drawString(lineToRender, 16, getHeight() - 48 * 4 + numLines++ * 48);
                // Clear the string builder
                nextLine.setLength(0);
                nextLine.append(word);
                nextLine.append(' ');
            }
        }
        if (nextLine.length() > 0) {
            g2d.drawString(nextLine.toString(), 16, getHeight() - 48 * 4 + numLines++ * 48);
        }
    }

    private void handleRedraw() {
        final int REDRAW_PER_SEC = 30, POLLS_PER_SEC = 1;
        long lastUpdateTime = System.currentTimeMillis();
        int numRedrawsSincePoll = 0;
        long lastPollTime = System.currentTimeMillis();
        while (!Thread.interrupted() && ((uiDrawThread == null || uiDrawThread.isAlive()))) {
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
        if (!uiDrawThread.isAlive()) {
            System.out.println("UI Thread is no longer alive, killing redraw thread.");
        }
    }

}
