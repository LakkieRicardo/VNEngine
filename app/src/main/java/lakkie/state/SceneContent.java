package lakkie.state;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all the content registered in a script. This can be renderable content
 * such as a script line or selection, or actionable content like a scene redirect, selection,
 * as well as label(selecction target).
 */
public class SceneContent {

    public int lineNum;

    public SceneContent(int lineNum) {
        this.lineNum = lineNum;
    }

    public static class Line extends SceneContent {
        public String line;
        public String charId;
        public String expression;

        public Line(int lineNum) {
            super(lineNum);
        }
    }

    public static class Label extends SceneContent {
        public String value;

        public Label(int lineNum) {
            super(lineNum);
        }
    }

    public static class Redirect extends SceneContent {
        public String targetSceneId;

        public Redirect(int lineNum) {
            super(lineNum);
        }
    }

    public static class Selection extends SceneContent {
        public final List<SelectionOption> options = new ArrayList<>();

        public Selection(int lineNum) {
            super(lineNum);
        }
    }

    public static class SelectionOption {
        public String display;
        public String label;
    }

}
