package lakkie;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import javax.swing.JPanel;

public class GameRenderComponent extends JPanel {
    
    private Font loadedMedium, loadedSemibold, loadedThin;
    private Font dlgFont_Medium, dlgFont_Semibold, dlgFont_Thin;
    private final Thread redrawThread;
    private String currentLine = "";

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

    protected void paintComponent(Graphics g) {
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

        g2d.setFont(dlgFont_Medium);
        g2d.setColor(Color.white);
        AttributedString attribLine = new AttributedString(currentLine);
        AttributedCharacterIterator lineIt = attribLine.getIterator();
        FontRenderContext fontCtx = g2d.getFontRenderContext();
        LineBreakMeasurer breakMeasure = new LineBreakMeasurer(lineIt, fontCtx);
        breakMeasure.setPosition(lineIt.getBeginIndex());
        float breakWidth = (float)getSize().width;
        float drawPosY = 0;
        int paragraphEnd = lineIt.getEndIndex();

        // Get lines until the entire paragraph has been displayed.
        while (breakMeasure.getPosition() < paragraphEnd) {

            // Retrieve next layout. A cleverer program would also cache
            // these layouts until the component is re-sized.
            TextLayout layout = breakMeasure.nextLayout(breakWidth);

            // Compute pen x position. If the paragraph is right-to-left we
            // will align the TextLayouts to the right edge of the panel.
            // Note: this won't occur for the English text in this sample.
            // Note: drawPosX is always where the LEFT of the text is placed.
            float drawPosX = layout.isLeftToRight()
                ? 0 : breakWidth - layout.getAdvance();

            // Move y-coordinate by the ascent of the layout.
            drawPosY += layout.getAscent();

            // Draw the TextLayout at (drawPosX, drawPosY).
            layout.draw(g2d, drawPosX, drawPosY);

            // Move y-coordinate in preparation for next layout.
            drawPosY += layout.getDescent() + layout.getLeading();
        }
    }

    private void handleRedraw() {
        final int REDRAW_PER_SEC = 120, POLLS_PER_SEC = 1;
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
