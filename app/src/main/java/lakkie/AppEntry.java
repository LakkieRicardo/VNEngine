package lakkie;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lakkie.state.GameState;

public class AppEntry {
    
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("scripter")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | UnsupportedLookAndFeelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            new ScripterToolFrame();
            return;
        }
        GameState state;
        try {
            state = new GameState(AppEntry.class.getResourceAsStream(GameState.SCRIPT_FILE));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load script file. Cannot continue execution.");
            System.exit(1);
            state = null;
        }

        new GameFrame(state);

    }

}
