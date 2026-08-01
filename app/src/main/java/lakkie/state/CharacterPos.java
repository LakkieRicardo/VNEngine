package lakkie.state;

public enum CharacterPos {
    
    LEFT,
    RIGHT;

    public static CharacterPos fromString(String name) {
        for (CharacterPos pos : CharacterPos.values()) {
            if (pos.name().equalsIgnoreCase(name)) {
                return pos;
            }
        }
        return null;
    }

}
