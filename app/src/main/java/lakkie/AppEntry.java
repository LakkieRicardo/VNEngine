package lakkie;

import java.io.IOException;

public class AppEntry {
    
    public static void main(String[] args) {
        GameState state;
        try {
            state = new GameState(GameState.SCRIPT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load script file. Cannot continue execution.");
            System.exit(1);
        }

        GameFrame game = new GameFrame();
    }

}
