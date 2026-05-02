package lakkie.state;

public class GameScriptException extends Exception {
    
    public GameScriptException(int lineNum, String desc) {
        super(String.format("Error on line %d: %s", lineNum, desc));
    }

}
