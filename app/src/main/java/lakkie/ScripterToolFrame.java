package lakkie;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import lakkie.state.GameScene;
import lakkie.state.GameState;

public class ScripterToolFrame extends JFrame {
    
    public ScripterToolFrame() {
        super("Super Cool Deadlock Game - Scripter");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int winW = screenSize.width / 2;
        int winH = winW / 16 * 9;
        setSize(winW, winH);
        setMinimumSize(new Dimension(500, 500 / 16 * 9));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupLayout();
        setupTitleBar();
        setDefaultLookAndFeelDecorated(true);
        setVisible(true);
    }

    private JTextArea log;

    private void addLogMessage(String format, Object... args) {
        String timestamp = new SimpleDateFormat("h:mm a").format(new Date());
        String msgContents = String.format(format, args);
        String existingContents = log.getText();
        String newContents = String.format("%s[%s] %s\n", existingContents, timestamp, msgContents);
        log.setText(newContents);
    }

    private void setupLayout() {
        GridLayout grid = new GridLayout(1, 2);
        setLayout(grid);

        log = new JTextArea();
        log.setEditable(false);
        log.setPreferredSize(new Dimension(600, 400));
        log.setBorder(BorderFactory.createTitledBorder("Info Console"));
        addLogMessage("Select a script to begin.");
        add(log);
    }

    private void setupTitleBar() {
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu menu = new JMenu("Scripts");
        JMenuItem itemLoad = new JMenuItem("Validate script file...");
        itemLoad.addActionListener(action -> validateScriptFile());
        menu.add(itemLoad);
        bar.add(menu);
    }

    private void validateScriptFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Script files", "txt");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File scriptFile = new File(chooser.getSelectedFile().getAbsolutePath());
            if (!scriptFile.exists()) {
                addLogMessage("Script file does not exist at: %s", scriptFile.getAbsolutePath());
                return;
            }
            try {
                long startTime_ms = System.currentTimeMillis();
                GameState state = new GameState(new FileInputStream(scriptFile));
                long timeToLoad_ms = System.currentTimeMillis() - startTime_ms;
                addLogMessage("Successfully loaded script in %.2fs.", (float)timeToLoad_ms / 1000);
                int scenes = state.scenes.size();
                int chars = state.chars.size();
                int contentLength = 0;
                for (GameScene scene : state.scenes.values()) {
                    contentLength += scene.content.size();
                }
                addLogMessage("Found %d scenes and %d characters with %d content lines.", scenes, chars, contentLength);
            } catch (Exception e) {
                addLogMessage("Failed to load script file: %s", e.getMessage());
                e.printStackTrace();
            }
        }
    }

}