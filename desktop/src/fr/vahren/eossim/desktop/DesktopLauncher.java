package fr.vahren.eossim.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import fr.vahren.eossim.ESGame;

public class DesktopLauncher {
	public static void main (String[] args) {
    	LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
   		config.width = ESGame.statusWidth * ESGame.cellWidth;
    	config.height = (ESGame.gridHeight+ ESGame.statusHeight) * ESGame.cellHeight;
    	config.addIcon("Tentacle-16.png", Files.FileType.Internal);
    	config.addIcon("Tentacle-32.png", Files.FileType.Internal);
    	config.addIcon("Tentacle-128.png", Files.FileType.Internal);
		config.title = "SquidLib Demo";
    	new LwjglApplication(new ESGame(), config);
    }
}
