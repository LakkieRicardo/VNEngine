package lakkie;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import javax.swing.JPanel;

import lakkie.state.GameState;
import lakkie.state.GameState.Transcript;
import lakkie.state.GameState.TranscriptLine;

public class GameRenderComponent extends JPanel {
    
    private Font loadedMedium, loadedSemibold, loadedThin;
    private Font dlgFont_Medium;
    private final Thread redrawThread;
    private float transcriptYOffset = 0f;
    private Thread uiDrawThread = null;
    private boolean drawTranscript = false;
    private final GameState state;

    public GameRenderComponent(GameState state) {
        super();
        this.state = state;
        setBackground(Color.black);
        try {
            loadedMedium = Font.createFont(Font.TRUETYPE_FONT, GameRenderComponent.class.getResourceAsStream("/valveoracle-medium.ttf"));
            dlgFont_Medium = loadedMedium.deriveFont(32.f);
        } catch (FontFormatException | IOException e) {
            System.out.println("Failed to initialize valve oracle font. Defaulting to Times New Roman.");
            e.printStackTrace();
            dlgFont_Medium = new Font("Times New Roman", Font.PLAIN, 32);
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

    public boolean showTranscript() {
        return drawTranscript;
    }

    public void setShowTranscript(boolean showTranscript) {
        transcriptYOffset = Float.NaN;
        drawTranscript = showTranscript;
    }

    private String[] wordSplit(String line) {
        if (line.contains(" ")) {
            return line.split(" ");
        } else {
            return new String[] { line };
        }
    }

    private int calcTranscriptHeight(Graphics2D g2d, Transcript transcript) {
        if (transcript.lines() == null || transcript.lines().size() == 0) {
            return 0;
        }
        int height = 0;
        for (int i = 0; i < transcript.lines().size(); i++) {
            TranscriptLine line = transcript.lines().get(i);
            if (line.isItalics()) {
                g2d.setFont(dlgFont_Medium.deriveFont(Font.ITALIC, 24.f));
            } else {
                g2d.setFont(dlgFont_Medium.deriveFont(24.f));
            }
            height += 48;
            String lineText = line.line();
            String[] words = wordSplit(lineText);
            StringBuilder nextLine = new StringBuilder();
            FontMetrics metrics = g2d.getFontMetrics();
            for (String word : words) {
                nextLine.append(word);
                nextLine.append(' ');
                int lineWidth = metrics.stringWidth(nextLine.toString());
                int maxWidth = getWidth() - 16;
                if (lineWidth > maxWidth) {
                    height += 48;
                    // Clear the string builder
                    nextLine.setLength(0);
                    nextLine.append(word);
                    nextLine.append(' ');
                }
            }
            if (nextLine.length() > 0) {
                height += 48;
            }
            height += 16;
        }

        return height;
    }

    private void doDrawTranscript(Graphics2D g2d, Transcript transcript) {
        if (transcript == null || transcript.lines().size() == 0) {
            return;
        }
        if (Float.isNaN(transcriptYOffset)) {
            transcriptYOffset = -calcTranscriptHeight(g2d, transcript) + getHeight() - 48;
        }
        int currentY = 32 + (int)transcriptYOffset;
        for (int i = 0; i < transcript.lines().size(); i++) {
            TranscriptLine line = transcript.lines().get(i);
            if (line.isItalics()) {
                g2d.setFont(dlgFont_Medium.deriveFont(Font.ITALIC, 24.f));
            } else {
                g2d.setFont(dlgFont_Medium.deriveFont(24.f));
            }
            g2d.setColor(line.color());
            g2d.drawString(state.chars.get(line.charId()).name, 16, currentY);
            currentY += 48;
            String lineText = line.line();
            String[] words = wordSplit(lineText);
            StringBuilder nextLine = new StringBuilder();
            FontMetrics metrics = g2d.getFontMetrics();
            for (String word : words) {
                String lineToRender = nextLine.toString();
                nextLine.append(word);
                nextLine.append(' ');
                int lineWidth = metrics.stringWidth(nextLine.toString());
                int maxWidth = getWidth() - 16;
                if (lineWidth > maxWidth) {
                    g2d.drawString(lineToRender, 32, currentY);
                    currentY += 48;
                    // Clear the string builder
                    nextLine.setLength(0);
                    nextLine.append(word);
                    nextLine.append(' ');
                }
            }
            if (nextLine.length() > 0) {
                g2d.drawString(nextLine.toString(), 32, currentY);
                currentY += 48;
            }
            currentY += 16;
        }
    }

    protected void paintComponent(Graphics g) {
        uiDrawThread = Thread.currentThread();
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (drawTranscript) {
            doDrawTranscript(g2d, state.getTranscript());
            return;
        }

        if (state.line().length() == 0) {
            return;
        }
        
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedMedium);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedSemibold);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedThin);
        
        if (state.backdrop() != null) {
            g2d.drawImage(state.backdrop(), 0, 0, getWidth(), getHeight() - 48 * 6, null);
        }
        g2d.setColor(new Color(0x101010));
        g2d.fillRect(0, getHeight() - 48 * 6, getWidth(), 48 * 6);
        if (state.lineItalics()) {
            g2d.setFont(dlgFont_Medium.deriveFont(Font.ITALIC, 32.f));
        } else {
            g2d.setFont(dlgFont_Medium);
        }
        g2d.setColor(state.lineColor());
        FontMetrics metrics = g2d.getFontMetrics();
        String[] words = wordSplit(state.line());
        StringBuilder nextLine = new StringBuilder();
        int numLines = 0;
        if (state.charImg() != null) {
            int imgWidth = getWidth() / 3 - 32;
            int imgHeight = getHeight() - 48 * 6 - 32;
            if (state.charIsRight()) {
                g2d.drawImage(state.charImg(), getWidth() - imgWidth - 32, 32, imgWidth, imgHeight, null);
            } else {
                g2d.drawImage(state.charImg(), 32, 32, imgWidth, imgHeight, null);
            }
        }
        g2d.drawString(state.charName(), 32, getHeight() - 48 * 5);
        g2d.setStroke(new BasicStroke(4.f));
        g2d.drawLine(metrics.stringWidth(state.charName()) + 32 + 16, getHeight() - 48 * 5 - 8, getWidth() - 32, getHeight() - 48 * 5 - 8);
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

    public void offsetTranscript(float amount) {
        if (Float.isNaN(transcriptYOffset)) {
            return;
        }
        transcriptYOffset += amount;
    }
}
