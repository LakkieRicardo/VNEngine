package lakkie;

import java.io.IOException;

import lakkie.state.GameState;

public class AppEntry {
    
    public static void main(String[] args) {
        GameState state;
        try {
            state = new GameState(GameState.SCRIPT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load script file. Cannot continue execution.");
            System.exit(1);
            state = null;
        }

        GameFrame game = new GameFrame(state);
    }

}
