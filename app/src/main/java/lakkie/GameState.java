package lakkie;

import org.json.JSONObject;

import lakkie.state.GameCharacter;
import lakkie.state.GameScene;
import lakkie.state.SceneLine;

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

public class GameState {
    
    public static final String SCRIPT_FILE = "/TestScript.txt";

    public final List<GameScene> scenes = new ArrayList<>();
    public final Map<String, GameCharacter> chars = new HashMap<>();
    public GameScene activeScene = null;

    public GameState(String scriptFile) throws IOException {
        InputStream scriptStream = GameFrame.class.getResourceAsStream(scriptFile);
        BufferedReader scriptStreamReader = new BufferedReader(new InputStreamReader(scriptStream));
        String line;
        while ((line = scriptStreamReader.readLine()) != null) {
            line = line.strip();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            try {
                String command = line.substring(0, line.indexOf(' '));
                if (command.equalsIgnoreCase("AddChar")) {
                    List<String> args = parseArguments(1, line);
                    GameCharacter character = new GameCharacter();
                    chars.put(args.get(0), character);
                } else if (command.equalsIgnoreCase("SetCharName")) {
                    List<String> args = parseArguments(2, line);
                    chars.get(args.get(0)).name = args.get(1);
                } else if (command.equalsIgnoreCase("SetCharColor")) {
                    List<String> args = parseArguments(4, line);
                    Color color = new Color(Integer.parseInt(args.get(1)),
                                            Integer.parseInt(args.get(2)),
                                            Integer.parseInt(args.get(3)));
                    chars.get(args.get(0)).color = color;
                } else if (command.equalsIgnoreCase("SetCharRight")) {
                    List<String> args = parseArguments(2, line);
                    chars.get(args.get(0)).isRightSide = args.get(1).equalsIgnoreCase("true");
                } else if (command.equalsIgnoreCase("SetCharTextItalics")) {
                    List<String> args = parseArguments(2, line);
                    chars.get(args.get(0)).isItalics = args.get(1).equalsIgnoreCase("true");
                } else if (command.equalsIgnoreCase("StartScene")) {
                    scenes.add(new GameScene());
                } else if (command.equalsIgnoreCase("SetBackdrop")) {
                    List<String> args = parseArguments(2, line);
                    int sceneId = Integer.parseInt(args.get(0));
                    BufferedImage img = ImageIO.read(GameFrame.class.getResourceAsStream(args.get(1)));
                    scenes.get(sceneId).backdrop = img;
                } else if (command.equalsIgnoreCase("AddLine")) {
                    List<String> args = parseArguments(3, line);
                    int sceneId = Integer.parseInt(args.get(0));
                    SceneLine sceneLine = new SceneLine();
                    sceneLine.character = args.get(1);
                    sceneLine.line = args.get(2);
                    scenes.get(sceneId).lines.add(sceneLine);
                } else if (command.equalsIgnoreCase("SelectScene")) {
                    List<String> args = parseArguments(1, line);
                    int sceneId = Integer.parseInt(args.get(0));
                    activeScene = scenes.get(sceneId);
                } else {
                    throw new Exception(String.format("Failed to parse script line: Unrecognized command(%s)", command));
                }
            } catch (Exception ex) {
                System.err.printf("Failed to parse script line: %s\n", line);
                ex.printStackTrace();
                System.exit(1);
            }
        }

        
    }

    public List<String> parseArguments(int numExpected, String line) {
        List<String> args = new ArrayList<>(numExpected);
        line = line.substring(line.indexOf(' ') + 1);
        for (int i = 0; i < numExpected; i++) {
            if (i == numExpected - 1) {
                args.add(line.strip());
            } else {
                int nextSpace = line.indexOf(' ');
                args.add(line.substring(0, nextSpace).strip());
                line = line.substring(nextSpace + 1);
            }
        }
        return args;
    }
}
