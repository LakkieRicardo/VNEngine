package lakkie.state;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GameCharacter {
    
    public String name = "";
    public Color color = Color.white;
    public boolean isItalics = false;
    public Map<String, CharExpr> expressions = new HashMap<>();

}
