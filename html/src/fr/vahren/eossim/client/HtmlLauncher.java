package fr.vahren.eossim.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import fr.vahren.eossim.ESGame;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(80 * 11, (24 + 8) * 22);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new ESGame();
        }
}