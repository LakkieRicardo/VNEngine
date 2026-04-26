package lakkie.state;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GameCharacter {
    
    public String name = "";
    public Color color = Color.white;
    public boolean isRightSide = false; // TODO: Add more positioning options as needed
    public boolean isItalics = false;
    public Map<String, CharExpr> expressions = new HashMap<>();

}
