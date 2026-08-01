package lakkie.state;

import lakkie.GameFrame;
import lakkie.state.SceneContent.Label;
import lakkie.state.SceneContent.Line;
import lakkie.state.SceneContent.Redirect;
import lakkie.state.SceneContent.Selection;
import lakkie.state.SceneContent.SelectionOption;

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
    public GameState(InputStream scriptStream) throws IOException {
        long startTime = System.currentTimeMillis();
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(scriptStream));
        String line;

        // Some lines will be used to setup the next line(ex: Char -> SetCharName). This requires some state vars
        GameCharacter currentChar = null;
        GameScene currentScene = null;
        SceneContent.Selection currentSelection = null;

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
                } else if (command.equalsIgnoreCase("Backdrop")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(1, line);
                    BufferedImage img = ImageIO.read(GameFrame.class.getResourceAsStream(args.get(0)));
                    currentScene.backdrop = img;
                } else if (command.equalsIgnoreCase("Line")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(2, line);
                    Line sceneLine = new Line(lineNum);
                    sceneLine.charId = args.get(0);
                    sceneLine.line = args.get(1);
                    sceneLine.expression = "Default";
                    currentScene.content.add(sceneLine);
                } else if (command.equalsIgnoreCase("Label")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(1, line);
                    Label label = new Label(lineNum);
                    label.value = args.get(0);
                    currentScene.content.add(label);
                } else if (command.equalsIgnoreCase("Selection")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    currentSelection = new SceneContent.Selection(lineNum);
                    currentScene.content.add(currentSelection);
                } else if (command.equalsIgnoreCase("Option")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    if (currentSelection == null) throw new GameScriptException(lineNum, "No selection in scope");
                    List<String> args = parseArguments(2, line);
                    SelectionOption option = new SelectionOption();
                    option.label = args.get(0);
                    option.display = args.get(1);
                    currentSelection.options.add(option);
                } else if (command.equalsIgnoreCase("Redirect")) {
                    if (currentScene == null) throw new GameScriptException(lineNum, "No scene in scope");
                    List<String> args = parseArguments(1, line);
                    Redirect redirect = new Redirect(lineNum);
                    redirect.targetSceneId = args.get(0);
                    currentScene.content.add(redirect);
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

        // TODO: This implies the first content in the script needs to be a Line which might not be true
        updateStateCache(null, -1);
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

    private boolean isSelectionActive = false;
    private Selection activeSelection;
    // private Transcript transcript;

    private void updateStateCache(GameScene oldScene, int oldIdx) {
        if (oldIdx != -1 && oldScene != null) {
            assert oldScene.content.get(oldIdx) instanceof Line;
        }
        // By the time we call this function the lineIdx should only point to a Line or Selection
        // Also, the argument for this function should always point to a Line
        synchronized (stateCacheMutex) {
            SceneContent content = activeScene.content.get(lineIdx);
            Line line;
            if (content instanceof Selection selection) {
                if (oldIdx == -1 || oldScene == null) {
                    // Use default properties if there is no previous line to go off of
                    isSelectionActive = true;
                    activeSelection = selection;
                    currentCharColor = Color.white;
                    currentLine = "";
                    currentLineItalics = false;
                    currentCharIsRight = false;
                    currentCharImg = null;
                    currentBackdrop = activeScene.backdrop;
                    currentCharName = "";
                    return;
                }
                line = (Line)oldScene.content.get(oldIdx);
            } else {
                line = (Line)activeScene.content.get(lineIdx);
                isSelectionActive = false;
                activeSelection = null;
            }
            GameCharacter character = chars.get(line.charId);
            currentCharColor = character.color;
            currentLine = line.line;
            currentLineItalics = character.isItalics;
            currentCharIsRight = character.isRightSide;
            currentCharImg = character.expressions.get(line.expression).sprite;
            currentBackdrop = activeScene.backdrop;
            currentCharName = character.name;
            if (content instanceof Selection selection) {
                isSelectionActive = true;
                activeSelection = selection;
            }
            // List<TranscriptLine> lines = new ArrayList<>();
            // transcript = new Transcript(lines);
            // for (int idx = 0; idx <= lineIdx; idx++) {
            //     SceneContent sceneLine = activeScene.content.get(idx);
            //     lines.add(new TranscriptLine(sceneLine.charId,
            //         chars.get(sceneLine.charId).color,
            //         sceneLine.line,
            //         chars.get(sceneLine.charId).isItalics));
            // }
        }
    }

    // TODO: Implement transcript
    // public Transcript getTranscript() {
    //     synchronized (stateCacheMutex) {
    //         return transcript;
    //     }
    // }

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

    public Selection selection() {
        return activeSelection;
    }

    public boolean isSelectionActive() {
        return isSelectionActive;
    }

    private void findNextRenderableContent() throws GameScriptException {
        lineIdx = Math.min(activeScene.content.size() - 1, lineIdx + 1);
        while (!(activeScene.content.get(lineIdx) instanceof Line || activeScene.content.get(lineIdx) instanceof Selection)) {
            SceneContent content = activeScene.content.get(lineIdx);
            if (content instanceof Redirect redirect) {
                if (!scenes.containsKey(redirect.targetSceneId)) {
                    throw new GameScriptException(redirect.lineNum, "Target scene does not exist");
                }
                activeScene = scenes.get(redirect.targetSceneId);
                lineIdx = -1;
                findNextRenderableContent();
            } else if (content instanceof Label) {
                lineIdx = Math.min(activeScene.content.size() - 1, lineIdx + 1);
            }
        }
    }

    public void nextLine() throws GameScriptException {
        // TODO: If the first line in a scene is a selection, the last line might be wrong
        int oldIdx = lineIdx;
        GameScene oldScene = activeScene;
        synchronized (stateCacheMutex) {
            findNextRenderableContent();
        }
        updateStateCache(oldScene, oldIdx);
    }

    private void findLastRenderableContent() {
        int lastSafeIdx = lineIdx;
        lineIdx = Math.max(0, lineIdx - 1);
        // In theory there shouldn't be >1 labels in a row, but theoretically you could have a stack of them
        // In this case, skip all of the labels unless we hit a selection, in which case we abort going to the
        // last line and default to the last "safe" renderable line.
        while (activeScene.content.get(lineIdx) instanceof Label) {
            lineIdx = Math.max(0, lineIdx - 1);
            if (activeScene.content.get(lineIdx) instanceof Selection || activeScene.content.get(lineIdx) instanceof Redirect) {
                lineIdx = lastSafeIdx;
                break;
            }
        }
    }

    public void lastLine() {
        int oldIdx = lineIdx;
        GameScene oldScene = activeScene;
        synchronized (stateCacheMutex) {
            findLastRenderableContent();
        }
        if (lineIdx != oldIdx) {
            updateStateCache(oldScene, oldIdx);
        }
    }

    public static record Transcript(List<TranscriptLine> lines) { }

    public static record TranscriptLine(String charId, Color color, String line, boolean isItalics) { }

    public void gotoLabel(String target) throws GameScriptException {
        for (int idx = 0; idx < activeScene.content.size(); idx++) {
            if (activeScene.content.get(idx) instanceof Label label && label.value.equalsIgnoreCase(target)) {
                lineIdx = idx;
                synchronized (stateCacheMutex) {
                    findNextRenderableContent();
                }
                updateStateCache(null, -1);
                return;
            }
        }

        throw new GameScriptException(activeScene.content.get(lineIdx).lineNum, String.format("Failed to find target label in scene: %s", target));
    }
}
