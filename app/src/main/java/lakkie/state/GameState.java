package lakkie.state;

import lakkie.GameFrame;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Stores all of the scenes and characters in the game and provides to the renderer what needs to be displayed.
 */
public class GameState {
    
    public static final String SCRIPT_FILE = "/TestScript.txt";

    public final Map<String, GameScene> scenes = new HashMap<>();
    public final Map<String, GameCharacter> chars = new HashMap<>();
    public GameScene activeScene = null;
    private int lineIdx = 0;

    /**
     * Parses the given script file and loads it into the game state.
     * @param scriptFile The name of the script file to load.
     * @throws IOException If the file contents could not be read or parsed.
     */
    public GameState(String scriptFile) throws IOException {
        long startTime = System.currentTimeMillis();
        InputStream scriptStream = GameFrame.class.getResourceAsStream(scriptFile);
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(scriptStream));
        String line;

        // Some lines will be used to setup the next line(ex: Char -> SetCharName). This requires some state vars
        GameCharacter currentChar = null;
        GameScene currentScene = null;

        int lineNum = 0;
        while ((line = scriptStreamReader.readLine()) != null) {
            lineNum++;
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                String command;
                if (line.indexOf(' ') != -1) {
                    command = line.substring(0, line.indexOf(' '));
                } else {
                    command = line.strip();
                }
                if (command.equalsIgnoreCase("Char")) {
                    List<String> args = parseArguments(1, line);
                    if (chars.containsKey(args.get(0))) {
                        currentChar = chars.get(args.get(0));
                    } else {
                        currentChar = new GameCharacter();
                        chars.put(args.get(0), currentChar);
                    }
                } else if (command.equalsIgnoreCase("SetCharName")) {
                    if (currentChar == null) throw new GameScriptException(lineNum, "No character in scope");
                    List<String> args = parseArguments(1, line);
                    currentChar.name = args.get(0);
                } else if (command.equalsIgnoreCase("SetCharColor")) {
                    if (currentChar == null) throw new GameScriptException(lineNum, "No character in scope");
                    List<String> args = parseArguments(3, line);
                    Color color = new Color(Integer.parseInt(args.get(0)),
                                            Integer.parseInt(args.get(1)),
                                            Integer.parseInt(args.get(2)));
                    currentChar.color = color;
                } else if (command.equalsIgnoreCase("SetCharRight")) {
                    if (currentChar == null) throw new GameScriptException(lineNum, "No character in scope");
                    currentChar.isRightSide = true;
                } else if (command.equalsIgnoreCase("SetCharTextItalics")) {
                    if (currentChar == null) throw new GameScriptException(lineNum, "No character in scope");
                    currentChar.isItalics = true;
                } else if (command.equalsIgnoreCase("Scene")) {
                    List<String> args = parseArguments(1, line);
                    if (scenes.containsKey(args.get(0))) {
                        currentScene = scenes.get(args.get(0));    
                    } else {
                        currentScene = new GameScene();
                        scenes.put(args.get(0), currentScene);
                    }
                } else if (command.equalsIgnoreCase("SetBackdrop")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(1, line);
                    BufferedImage img = ImageIO.read(GameFrame.class.getResourceAsStream(args.get(0)));
                    currentScene.backdrop = img;
                } else if (command.equalsIgnoreCase("AddLine")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(2, line);
                    SceneLine sceneLine = new SceneLine();
                    sceneLine.charId = args.get(0);
                    sceneLine.line = args.get(1);
                    sceneLine.expression = "Default";
                    currentScene.lines.add(sceneLine);
                } else if (command.equalsIgnoreCase("SelectScene")) {
                    List<String> args = parseArguments(1, line);
                    String newSceneId = args.get(0);
                    if (!scenes.containsKey(newSceneId)) throw new GameScriptException(lineNum, "Scene does not exist at this point");
                    activeScene = scenes.get(newSceneId);
                } else if (command.equalsIgnoreCase("AddExpression")) {
                    if (currentChar == null) throw new GameScriptException(lineNum, "No character in scope");
                    List<String> args = parseArguments(2, line);
                    CharExpr expr = new CharExpr();
                    expr.name = args.get(0);
                    expr.sprite = ImageIO.read(GameFrame.class.getResourceAsStream(args.get(1)));
                    currentChar.expressions.put(expr.name, expr);
                } else {
                    throw new GameScriptException(lineNum, "Failed to parse command");
                }
            } catch (Exception ex) {
                System.err.printf("Failed to parse script line %d: %s\n", lineNum, line);
                ex.printStackTrace();
                System.exit(1);
            }
        }

        updateStateCache();
        long timeToLoad = System.currentTimeMillis() - startTime;
        System.out.printf("Time to load script: %.2f sec\n", (float)timeToLoad / 1000);
    }

    public List<String> parseArguments(int numExpected, String line) {
        List<String> args = new ArrayList<>(numExpected);
        line = line.substring(line.indexOf(' ') + 1);
        for (int i = 0; i < numExpected; i++) {
            if (i == numExpected - 1) {
                args.add(line.strip());
            } else {
                int nextSpace = line.indexOf(',');
                args.add(line.substring(0, nextSpace).strip());
                line = line.substring(nextSpace + 1);
            }
        }
        if (numExpected == 1 && args.get(0).equals("_")) {
            args.clear();
            args.add("");
        }
        return args;
    }

    private Object stateCacheMutex = new Object();
    private Color currentCharColor = Color.white;
    private String currentLine = "", currentCharName = "";
    private boolean currentLineItalics = false, currentCharIsRight = false;
    private BufferedImage currentCharImg = null, currentBackdrop = null;
    private Transcript transcript;

    private void updateStateCache() {
        synchronized (stateCacheMutex) {
            SceneLine line = activeScene.lines.get(lineIdx);
            GameCharacter character = chars.get(line.charId);
            currentCharColor = character.color;
            currentLine = line.line;
            currentLineItalics = character.isItalics;
            currentCharIsRight = character.isRightSide;
            currentCharImg = character.expressions.get(line.expression).sprite;
            currentBackdrop = activeScene.backdrop;
            currentCharName = character.name;
            List<TranscriptLine> lines = new ArrayList<>();
            transcript = new Transcript(lines);
            for (int idx = 0; idx <= lineIdx; idx++) {
                SceneLine sceneLine = activeScene.lines.get(idx);
                lines.add(new TranscriptLine(sceneLine.charId,
                    chars.get(sceneLine.charId).color,
                    sceneLine.line,
                    chars.get(sceneLine.charId).isItalics));
            }
        }
    }

    public Transcript getTranscript() {
        synchronized (stateCacheMutex) {
            return transcript;
        }
    }

	public String line() {
        synchronized (stateCacheMutex) {
		    return currentLine;
        }
	}

	public boolean lineItalics() {
        synchronized (stateCacheMutex) {
		    return currentLineItalics;
        }
	}

	public Color lineColor() {
        synchronized (stateCacheMutex) {
            return currentCharColor;
        }
	}

	public BufferedImage backdrop() {
		synchronized (stateCacheMutex) {
            return currentBackdrop;
        }
	}

	public boolean charIsRight() {
        synchronized (stateCacheMutex) {
		    return currentCharIsRight;
        }
	}

    public BufferedImage charImg() {
        synchronized (stateCacheMutex) {
            return currentCharImg;
        }
    }

	public String charName() {
        synchronized (stateCacheMutex) {
            return currentCharName;
        }
	}

    public void nextLine() {
        synchronized (stateCacheMutex) {
            lineIdx = Math.min(activeScene.lines.size() - 1, lineIdx + 1);
        }
        updateStateCache();
    }

    public void lastLine() {
        synchronized (stateCacheMutex) {
            lineIdx = Math.max(0, lineIdx - 1);
        }
        updateStateCache();
    }

    public static record Transcript(List<TranscriptLine> lines) { }

    public static record TranscriptLine(String charId, Color color, String line, boolean isItalics) { }
}
