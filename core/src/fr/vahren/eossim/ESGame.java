package fr.vahren.eossim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import fr.vahren.eossim.graphics.ESGraphics;
import fr.vahren.eossim.model.Enemy;
import fr.vahren.eossim.model.Player;
import fr.vahren.eossim.model.Unit;
import squidpony.FakeLanguageGen;
import squidpony.GwtCompatibility;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.gui.gdx.*;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The main class of the game, constructed once in each of the platform-specific Launcher classes. Doesn't use any
 * platform-specific code.
 */
// In SquidSetup, squidlib-util is always a dependency, and squidlib (the display code that automatically includes
// libGDX) is checked by default. If you didn't change those dependencies, this class should run out of the box.
//
// If you didn't select squidlib as a dependency in SquidSetup, this class will be full of errors. If you don't depend
// on LibGDX, you'll need to figure out display on your own, and the setup of multiple platform projects is probably
// useless to you. But, if you do depend on LibGDX, you can make some use of this class. You can remove any imports or
// usages of classes in the squidpony.squidgrid.gui.gdx package, remove as much of create() as you  want (some of it
// doesn't use the display classes, so you might want the dungeon generation and such, otherwise just empty out the
// whole method), remove any SquidLib-specific code in render() and resize(), and probably remove putMap entirely.

// A main game class that uses LibGDX to display, which is the default for SquidLib, needs to extend ApplicationAdapter
// or something related, like Game. Game adds features that SquidLib doesn't currently use, so ApplicationAdapter is
// perfectly fine for these uses.
public class ESGame extends ApplicationAdapter {
    SpriteBatch batch;
    public static RNG rng;
    private SquidLayers display;
    private DungeonGenerator dungeonGen;
    private char[][] decoDungeon, bareDungeon, lineDungeon, spaces;
    private int[][] colorIndices, bgColorIndices, languageBG, languageFG;
    /** Map W */
    public static int gridWidth = 80;
    /** Map H */
    public static int gridHeight = 32;
    /** Text W */
    public static int invWidth = 16;
    /** Text H */
    public static int invHeight = 32;
    /** Status W */
    public static int statusWidth = 96;
    /** Status H */
    public static int statusHeight = 16;
    /** The pixel width of a cell */
    public static int cellWidth = 8;
    /** The pixel height of a cell */
    public static int cellHeight = 16;
    private SquidInput input;
    private Color bgColor;
    private Stage stage;
    private DijkstraMap playerToCursor;
    private Coord cursor;
    private Unit player;
    private ArrayList<Coord> toCursor;
    private ArrayList<Coord> awaitedMoves;
    private float secondsWithoutMoves;
    private String[] lang;
    private int langIndex = 0;
    private List<Enemy> enemies = new ArrayList<>();
    /** The game graphic utilities class */
    public static ESGraphics graphics;

    @Override
    public void create () {
        //These variables, corresponding to the screen's width and height in cells and a cell's width and height in
        //pixels, must match the size you specified in the launcher for input to behave.
        //This is one of the more common places a mistake can happen.
        //In our desktop launcher, we gave these arguments to the configuration:
        //	config.width = 80 * 11;
        //  config.height = (24 + 8) * 22;
        //Here, config.height refers to the total number of rows to be displayed on the screen.
        //We're displaying 24 rows of dungeon, then 8 more rows of text generation to show some tricks with language.
        //That adds up to 32 total rows of height.
        //gridHeight is 24 because that variable will be used for generating the dungeon and handling movement within
        //the upper 24 rows. Anything that refers to the full height, which happens rarely and usually for things like
        //screen resizes, just uses gridHeight + 8. Next to it is gridWidth, which is 50 because we want 50 grid spaces
        //across the whole screen. cellWidth and cellHeight are 11 and 22, and match the multipliers for config.width
        //and config.height, but in this case don't strictly need to because we soon use a "Stretchable" font. While
        //gridWidth and gridHeight are measured in spaces on the grid, cellWidth and cellHeight are the pixel dimensions
        //of an individual cell. The font will look more crisp if the cell dimensions match the config multipliers
        //exactly, and the stretchable fonts (technically, distance field fonts) can resize to non-square sizes and
        //still retain most of that crispness.

        // gotta have a random number generator. We can seed an RNG with any long we want, or even a String.
        rng = new RNG("SquidLib!");

        //Some classes in SquidLib need access to a batch to render certain things, so it's a good idea to have one.
        batch = new SpriteBatch();
        //Here we make sure our Stage, which holds any text-based grids we make, uses our Batch.
        stage = new Stage(new StretchViewport(statusWidth * cellWidth, (gridHeight + statusHeight) * cellHeight), batch);

        // display is a SquidLayers object, and that class has a very large number of similar methods for placing text
        // on a grid, with an optional background color and lightness modifier per cell. It also handles animations and
        // other effects, but you don't need to use them at all. SquidLayers also automatically handles the stretchable
        // distance field fonts, which are a big improvement over fixed-size bitmap fonts and should probably be
        // preferred for new games. SquidLayers needs to know what the size of the grid is in columns and rows, how big
        // an individual cell is in pixel width and height, and lastly how to handle text, which can be a BitmapFont or
        // a TextCellFactory. Either way, it will use what is given to make its TextCellFactory, and that handles the
        // layout of text in a cell, among other things. DefaultResources stores pre-configured BitmapFont objects but
        // also some TextCellFactory objects for distance field fonts; either one can be passed to this constructor.
        // the font will try to load Inconsolata-LGC-Square as a bitmap font with a distance field effect.
        display = new SquidLayers(statusWidth, gridHeight + statusHeight, cellWidth, cellHeight, DefaultResources.getStretchableFont());
        graphics = new ESGraphics(display);
        // a bit of a hack to increase the text height slightly without changing the size of the cells they're in.
        // this causes a tiny bit of overlap between cells, which gets rid of an annoying gap between vertical lines.
        // if you use '#' for walls instead of box drawing chars, you don't need this.
        display.setTextSize(cellWidth, cellHeight + 1);

        // this makes animations very fast, which is good for multi-cell movement but bad for attack animations.
        // 0.03 is in seconds, so each animation will be 0.03 seconds in duration.
        display.setAnimationDuration(0.03f);

        //These need to have their positions set before adding any entities if there is an offset involved.
        //There is no offset used here, but it's still a good practice here to set positions early on.
        display.setPosition(0, 0);

        //This uses the seeded RNG we made earlier to build a procedural dungeon using a method that takes rectangular
        //sections of pre-drawn dungeon and drops them into place in a tiling pattern. It makes good "ruined" dungeons.
        dungeonGen = new DungeonGenerator(gridWidth, gridHeight, rng);
        //uncomment this next line to randomly add water to the dungeon in pools.
        dungeonGen.addWater(15);
        //decoDungeon is given the dungeon with any decorations we specified. (Here, we didn't, unless you chose to add
        //water to the dungeon. In that case, decoDungeon will have different contents than bareDungeon, next.)
        decoDungeon = dungeonGen.generate();
        //There are lots of options for dungeon generation in SquidLib; you can pass a TilesetType enum to generate()
        //as shown on the following lines to change the style of dungeon generated from ruined areas, which are made
        //when no argument is passed to generate or when TilesetType.DEFAULT_DUNGEON is, to caves or other styles.
        //decoDungeon = dungeonGen.generate(TilesetType.REFERENCE_CAVES); // generate caves
        //decoDungeon = dungeonGen.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS); // generate large round rooms

        //getBareDungeon provides the simplest representation of the generated dungeon -- '#' for walls, '.' for floors.
        bareDungeon = dungeonGen.getBareDungeon();
        //When we draw, we may want to use a nicer representation of walls. DungeonUtility has lots of useful methods
        //for modifying char[][] dungeon grids, and this one takes each '#' and replaces it with a box-drawing character.
        lineDungeon = DungeonUtility.hashesToLines(decoDungeon);
        // it's more efficient to get random floors from a packed set containing only (compressed) floor positions.
        // CoordPacker is a deep and involved class, but when other classes request packed data, you usually just need
        // to give them a short[] representing a region, as produced by CoordPacker.pack().
        short[] placement = CoordPacker.pack(bareDungeon, '.');
        //Coord is the type we use as a general 2D point, usually in a dungeon.
        //Because we know dungeons won't be huge, Coord is optimized for x and y values between -3 and 255, inclusive.
        cursor = Coord.get(-1, -1);
        //player is, here, just a Coord that stores his position. In a real game, you would probably have a class for
        //creatures, and possibly a subclass for the player.
        player = new Player();
        player.position = dungeonGen.utility.randomCell(placement);

        // Random monsters
        Enemy e1 = new Enemy();
        e1.position = dungeonGen.utility.randomCell(placement);
        Enemy e2 = new Enemy();
        e2.position = dungeonGen.utility.randomCell(placement);
        enemies.add(e1);
        enemies.add(e2);

        //This is used to allow clicks or taps to take the player to the desired area.
        toCursor = new ArrayList<>(100);
        awaitedMoves = new ArrayList<>(100);
        //DijkstraMap is the pathfinding swiss-army knife we use here to find a path to the latest cursor position.
        playerToCursor = new DijkstraMap(decoDungeon, DijkstraMap.Measurement.MANHATTAN);
        bgColor = SColor.DARK_SLATE_GRAY;
        // DungeonUtility provides various ways to get default colors or other information from a dungeon char 2D array.
        colorIndices = DungeonUtility.generatePaletteIndices(decoDungeon);
        bgColorIndices = DungeonUtility.generateBGPaletteIndices(decoDungeon);
        // Here, we're preparing some 2D arrays so they don't get created during rendering.
        // Creating new arrays or objects during rendering can put lots of pressure on Java's garbage collector,
        // and Android's garbage collector can be very slow, especially when compared to desktop.
        // These methods are in GwtCompatibility not because GWT has some flaw with array creation,
        // but because Java in general is missing methods for dealing with 2D arrays.
        // So, you can think of the class more like "Gwt, and also Compatibility".
        // fill2D constructs a 2D array filled with one item. Other methods can insert a
        // 2D array into a differently-sized 2D array, or copy a 2D array of various types.
        spaces = GwtCompatibility.fill2D(' ', statusWidth, statusHeight-2);
        languageBG = GwtCompatibility.fill2D(1, statusWidth, statusHeight-2);
        languageFG = GwtCompatibility.fill2D(0, statusWidth, statusHeight-2);

        // this creates an array of sentences, where each imitates a different sort of language or mix of languages.
        // this serves to demonstrate the large amount of glyphs SquidLib supports.
        // FakeLanguageGen doesn't attempt to produce legible text, so any of the arguments given to these method calls
        // can be changed in a trial-and-error way to find the subjectively best output. The arguments to sentence()
        // are the minimum words, maximum words, between-word punctuation, sentence-ending punctuation, chance out of
        // 0.0 to 1.0 of putting between-word punctuation after a word, and lastly the max characters per sentence.
        // It is recommended that you don't increase the max characters per sentence much more, since it's already very
        // close to touching the edges of the message box it's in.
        lang = new String[]
                {
                        FakeLanguageGen.ENGLISH.sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.GREEK_AUTHENTIC.sentence(5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        FakeLanguageGen.GREEK_ROMANIZED.sentence(5, 11, new String[]{",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                        FakeLanguageGen.LOVECRAFT.sentence(3, 9, new String[]{",", ",", ";"},
                                new String[]{".", ".", "!", "!", "?", "...", "..."}, 0.15, gridWidth - 4),
                        FakeLanguageGen.FRENCH.sentence(4, 12, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.RUSSIAN_AUTHENTIC.sentence(6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        FakeLanguageGen.RUSSIAN_ROMANIZED.sentence(6, 13, new String[]{",", ",", ",", ",", ";", " -"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.25, gridWidth - 4),
                        FakeLanguageGen.JAPANESE_ROMANIZED.sentence(5, 13, new String[]{",", ",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "...", "..."}, 0.12, gridWidth - 4),
                        FakeLanguageGen.SWAHILI.sentence(4, 9, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?"}, 0.12, gridWidth - 4),
                        FakeLanguageGen.SOMALI.sentence(3, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "!", "...", "?"}, 0.1, gridWidth - 4),
                        FakeLanguageGen.HINDI_ROMANIZED.sentence(3, 7, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "?", "..."}, 0.22, gridWidth - 4),
                        FakeLanguageGen.FANTASY_NAME.sentence(4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        FakeLanguageGen.FANCY_FANTASY_NAME.sentence(4, 8, new String[]{",", ",", ",", ";", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.22, gridWidth - 4),
                        FakeLanguageGen.FRENCH.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.65).sentence(5, 9, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "?", "..."}, 0.14, gridWidth - 4),
                        FakeLanguageGen.ENGLISH.addAccents(0.5, 0.15).sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.17, gridWidth - 4),
                        FakeLanguageGen.SWAHILI.mix(FakeLanguageGen.JAPANESE_ROMANIZED, 0.5).mix(FakeLanguageGen.FRENCH, 0.35)
                                .mix(FakeLanguageGen.RUSSIAN_ROMANIZED, 0.25).mix(FakeLanguageGen.GREEK_ROMANIZED, 0.2).mix(FakeLanguageGen.ENGLISH, 0.15)
                                .mix(FakeLanguageGen.FANCY_FANTASY_NAME, 0.12).mix(FakeLanguageGen.LOVECRAFT, 0.1)
                                .sentence(5, 10, new String[]{",", ",", ",", ";"},
                                new String[]{".", ".", ".", "!", "?", "..."}, 0.2, gridWidth - 4),
                };


        // this is a big one.
        // SquidInput can be constructed with a KeyHandler (which just processes specific keypresses), a SquidMouse
        // (which is given an InputProcessor implementation and can handle multiple kinds of mouse move), or both.
        // keyHandler is meant to be able to handle complex, modified key input, typically for games that distinguish
        // between, say, 'q' and 'Q' for 'quaff' and 'Quip' or whatever obtuse combination you choose. The
        // implementation here handles hjkl keys (also called vi-keys), numpad, arrow keys, and wasd for 4-way movement.
        // Shifted letter keys produce capitalized chars when passed to KeyHandler.handle(), but we don't care about
        // that so we just use two case statements with the same body, i.e. one for 'A' and one for 'a'.
        // You can also set up a series of future moves by clicking within FOV range, using mouseMoved to determine the
        // path to the mouse position with a DijkstraMap (called playerToCursor), and using touchUp to actually trigger
        // the event when someone clicks.
        input = new SquidInput(new SquidInput.KeyHandler() {
            @Override
            public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
                switch (key)
                {
                    case SquidInput.UP_ARROW:
                    case 'k':
                    case 'w':
                    case 'K':
                    case 'W':
                    {
                        //-1 is up on the screen
                        player.move(0, -1,bareDungeon);
                        break;
                    }
                    case SquidInput.DOWN_ARROW:
                    case 'j':
                    case 's':
                    case 'J':
                    case 'S':
                    {
                        //+1 is down on the screen
                        player.move(0, 1,bareDungeon);
                        break;
                    }
                    case SquidInput.LEFT_ARROW:
                    case 'h':
                    case 'a':
                    case 'H':
                    case 'A':
                    {
                        player.move(-1, 0,bareDungeon);
                        break;
                    }
                    case SquidInput.RIGHT_ARROW:
                    case 'l':
                    case 'd':
                    case 'L':
                    case 'D':
                    {
                        player.move(1, 0,bareDungeon);
                        break;
                    }
                    case 'Q':
                    case 'q':
                    case SquidInput.ESCAPE:
                    {
                        Gdx.app.exit();
                        break;
                    }
                }
            }
        },
                //The second parameter passed to a SquidInput can be a SquidMouse, which takes mouse or touchscreen
                //input and converts it to grid coordinates (here, a cell is 12 wide and 24 tall, so clicking at the
                // pixel position 15,51 will pass screenX as 1 (since if you divide 15 by 12 and round down you get 1),
                // and screenY as 2 (since 51 divided by 24 rounded down is 2)).
                new SquidMouse(cellWidth, cellHeight, statusWidth, gridHeight+statusHeight, 0, 0, new InputAdapter() {

            // if the user clicks and there are no awaitedMoves queued up, generate toCursor if it
            // hasn't been generated already by mouseMoved, then copy it over to awaitedMoves.
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(awaitedMoves.isEmpty()) {
                    if (toCursor.isEmpty()) {
                        cursor = Coord.get(screenX, screenY);
                        //This uses DijkstraMap.findPath to get a possibly long path from the current player position
                        //to the position the user clicked on.
                        toCursor = playerToCursor.findPath(100, null, null, player.position, cursor);
                    }
                    awaitedMoves = new ArrayList<>(toCursor);
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                return mouseMoved(screenX, screenY);
            }

            // causes the path to the mouse position to become highlighted (toCursor contains a list of points that
            // receive highlighting). Uses DijkstraMap.findPath() to find the path, which is surprisingly fast.
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                if(!awaitedMoves.isEmpty())
                    return false;
                if(cursor.x == screenX && cursor.y == screenY)
                {
                    return false;
                }
                cursor = Coord.get(screenX, screenY);
                toCursor = playerToCursor.findPath(100, null, null, player.position, cursor);
                return false;
            }
        }));
        //Setting the InputProcessor is ABSOLUTELY NEEDED TO HANDLE INPUT
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
        //You might be able to get by with the next line instead of the above line, but the former is preferred.
        //Gdx.input.setInputProcessor(input);
        // and then add display, our one visual component, to the list of things that act in Stage.
        stage.addActor(display);

    }


    /**
     * Draws the map, applies any highlighting for the path to the cursor, and then draws the player.
     */
    public void putMap()
    {
        // MAP
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                display.put(i, j, lineDungeon[i][j], colorIndices[i][j], bgColorIndices[i][j], 40);
            }
        }
        // PATH
        for (Coord pt : toCursor)
        {
            // use a brighter light to trace the path to the cursor, from 170 max lightness to 0 min.
            display.highlight(pt.x, pt.y, 100);
        }

        // UNITS
        //places the player as an '@' at his position in orange (6 is an index into SColor.LIMITED_PALETTE).
        player.render(display);
        for(Enemy e:enemies){
            e.render(display);
        }

        // The arrays we produced in create() are used here to provide a blank area behind text
        display.put(0, gridHeight + 1, spaces, languageFG, languageBG);
        for (int i = 0; i < statusHeight-2; i++) {
            display.putString(2, gridHeight + i + 1, lang[(langIndex + i) % lang.length], 0, 1);
        }
    }
    @Override
    public void render () {
        // standard clear the background routine for libGDX
        Gdx.gl.glClearColor(bgColor.r / 255.0f, bgColor.g / 255.0f, bgColor.b / 255.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();

        // if the user clicked, we have a list of moves to perform.
        if(!awaitedMoves.isEmpty())
        {
            // this doesn't check for input, but instead processes and removes Points from awaitedMoves.
            secondsWithoutMoves += Gdx.graphics.getDeltaTime();
            if (secondsWithoutMoves >= 0.1) {
                secondsWithoutMoves = 0;
                Coord m = awaitedMoves.remove(0);
                toCursor.remove(0);
                player.move(m.x - player.position.x, m.y - player.position.y,bareDungeon);
            }
        }
        // if we are waiting for the player's input and get input, process it.
        else if(input.hasNext()) {
            input.next();
        }

        //stage has its own batch and must be explicitly told to draw().
        stage.draw();
        //you may need to explicitly tell stage to act() if input isn't working.
        //there shouldn't be a problem from calling act(), but
        //you can comment out the next line to test if input has problems.
        stage.act();
    }

    @Override
	public void resize(int width, int height) {
		super.resize(width, height);
        //very important to have the mouse behave correctly if the user fullscreens or resizes the game!
        // this gets the mouse's understanding of the screen updated for the newly resized grid.
        // you may need to change this if your game has multiple sections, like a SquidMessageBox or other widget below
        // the main grid of the game. If a game uses VisualInput, then that way of handling input will need to be told
        // about the changes to the screen as well.
		input.getMouse().reinitialize((float) width / statusWidth, (float)height / (gridHeight + statusHeight), gridWidth, gridHeight, 0, 0);
	}
}
