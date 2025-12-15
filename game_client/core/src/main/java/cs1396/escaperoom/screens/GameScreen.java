package cs1396.escaperoom.screens;

import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.physics.Collisions;
import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.Player;
import cs1396.escaperoom.game.entities.properties.ConditionallyActive;
import cs1396.escaperoom.game.entities.properties.ConditionallyVisible;
import cs1396.escaperoom.game.entities.properties.InteractableProperty;
import cs1396.escaperoom.game.entities.properties.LockedProperty;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.TiledBrushable;
import cs1396.escaperoom.game.entities.properties.connectors.Utils;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.ui.ActionLog;
import cs1396.escaperoom.game.ui.GameSettingsDialog;
import cs1396.escaperoom.game.ui.PlayerInventoryItemSlot.PlayerInventorySource;
import cs1396.escaperoom.game.world.FowTile;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.services.GameStatistics;
import cs1396.escaperoom.ui.dnd.ItemPayload;
import cs1396.escaperoom.game.state.GameEventBus;
import cs1396.escaperoom.ui.widgets.G24Dialog;

/**
 *
 *
 * Extension of {@link AbstractScreen} with game utilities
 *
 */
public abstract class GameScreen extends MapScreen {

  public enum GameType {
    Standard,
    Editor,
    Verifying,
  }

  protected GameType gameType;

  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(GameScreen.class.getName());

  private Item inFlightItem = null;
  private DragAndDrop dragAndDrop = new DragAndDrop();
  public static Player player;
  public int playerId = -1;
  protected ActionLog actionlog;
  boolean[][] revealed;
  FowTile[][] fowTiles;
  protected Table rootTable;
  public GameStatistics stats = new GameStatistics();
  private GameEventBus bus = new GameEventBus();
  private long startTime;
  protected Array<Item> pollItems = new Array<>();

  public ActionLog getActionLog() {
    return actionlog;
  }

  public GameEventBus getEventBus() {
    return bus;
  }

  public GameType getGameType() {
    return gameType;
  }

  public DragAndDrop getDragAndDrop() {
    return dragAndDrop;
  }

  private void resetRevealed() {
    for (int y = 0; y < gridSize.height; y++) {
      for (int x = 0; x < gridSize.width; x++) {
        if (revealed[y][x]){
          revealed[y][x] = false;
          fowTiles[y][x].updateTiles(revealed, new Vector2(x, y), true);
        }
      }
    }
  }

  public GameScreen(MapData mapdata) {
    super(mapdata);

    revealed = new boolean[gridSize.height][gridSize.width];
    fowTiles = new FowTile[gridSize.height][gridSize.width];

    for (int y = 0; y < gridSize.height; y++) {
      for (int x = 0; x < gridSize.width; x++) {
        fowTiles[y][x] = new FowTile();
      }
    }
    FowTile.fowTiles = fowTiles;
    FowTile.gridSize = gridSize;

    AssetManager.instance().load("textures/game_tile.png", Texture.class);
    AssetManager.instance().finishLoading();
    Texture tile = AssetManager.instance().get("textures/game_tile.png", Texture.class);
    tile.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

    TextureRegion roomTexture = new TextureRegion(tile);
    roomTexture.setRegion(0, 0, tile.getWidth() * gridSize.width, tile.getHeight() * gridSize.height);

    room.remove();
    room = new Image(roomTexture);
    room.setName("room");
    room.setPosition(0, 0);
    room.setSize(gridSize.width, gridSize.height);
    addActor(room);

    startTime = System.currentTimeMillis();

    player = new Player(gridSize.width / 2, gridSize.height / 2, this);
    player.registerBinds();

    rootTable = new Table();
    rootTable.setPosition(0, 0);
    rootTable.setFillParent(true);
    rootTable.bottom().left();
    rootTable.defaults().pad(10);
    getUIStage().addActor(rootTable);

    actionlog = new ActionLog(this);
    rootTable.add(actionlog).left().bottom().size(300, 150);
    actionlog.emit("Game Started...");

    registerBinds();

    // fog of war
    for (int y = 0; y < gridSize.height; y++) {
      for (int x = 0; x < gridSize.width; x++) {
        revealed[y][x] = false;
      }
    }

    dragAndDrop.addTarget(new GameMapTarget(roomProxy));
  }

  public void calculateStatistics(boolean completedSucessfully) {
    long finishTime = System.currentTimeMillis();
    stats.timeMilliseconds = finishTime - startTime;
    stats.completedSucessfully = completedSucessfully;
    player.calculateStatistics(stats);
  }

  /**
   * Registers an Item for polling if it needs it.
   *
   * If called with an Item that doesn't need polling, this function does nothing.
   */
  public void maybeAddPolling(Item item) {
    if (item.hasProperty(PropertyType.ConditionallyVisible)) {
      pollItems.add(item);
      return;
    }

    item.getProperty(PropertyType.ConditionallyActive, ConditionallyActive.class)
      .ifPresent((ConditionallyActive ca) -> {
        if (ca.requiresPoll()) pollItems.add(item);
      }
    );
  }

  @Override
  public void loadGrid(Grid grid) {
    super.loadGrid(grid);
    for (Item item : grid.items.values()){
      maybeAddPolling(item);
      item.getProperties().forEach((p) -> p.onGameLoad(this));
    }
  }

  private Optional<Item> priorityItemAt(int x, int y){
    // If the coords are off the map -> return empty
    Optional<Item[]> maybeItems = getItemsAt(x, y);
    if (maybeItems.isEmpty()) {
      return Optional.empty();
    }

    Item[] items = maybeItems.get();

    // If there are no items here -> return empty
    if (items.length == 0) {
      return Optional.empty();
    }

    // Try and see if the currently focused item is in our stack
    Optional<Item> focusedItem = player.getFocusedItem();
    if (focusedItem.isPresent()) {
      Item item = focusedItem.get();
      for (int i = 0; i < items.length; i++) {
        if (items[i].getID() == item.getID()) {
          return Optional.of(items[i]);
        }
      }
    }

    int maxRender = Integer.MIN_VALUE;
    Item item = null;

    // See if any of the focus canidates items are in our stack
    // Prioritize those with a higher render priority
    Array<Item> focusCanidates = player.getFocusCanidates();
    for (int i = 0; i < items.length; i++) {
      if (focusCanidates.contains(items[i], false)) {
        if (items[i].renderPriority() > maxRender){
          item = items[i];
          maxRender = items[i].renderPriority();
        }
      }
    }

    if (item == null){
      return Optional.empty();
    }

    return Optional.of(item);
  }

  //----------------------------------------------------------------------------
  // KEYBINDS
  //----------------------------------------------------------------------------

  private boolean collidesMenu(Vector2 uiPos){
    for (Actor a : getUIStage().getRoot().getChildren()){
      if (a instanceof Menu){
        Menu m = (Menu)a;
        Rectangle bounds = new Rectangle(m.getX(), m.getY(), m.getWidth(), m.getHeight());
        if (bounds.contains(uiPos)){
          return true;
        }
      } else if (a instanceof G24Dialog){
        G24Dialog m = (G24Dialog)a;
        Rectangle bounds = new Rectangle(m.getX(), m.getY(), m.getWidth(), m.getHeight());
        if (bounds.contains(uiPos)){
          return true;
        }
      }
    }
    return false;
  }

  private void registerBinds() {
    ControlsManager.registerInput(Input.ZOOM_OUT, InputType.HELD, () -> {
      cameraManager.zoomOut();
      updateProxy();
    });
    ControlsManager.registerInput(Input.ZOOM_IN, InputType.HELD, () -> {
      cameraManager.zoomIn();
      updateProxy();
    });

    ControlsManager.registerInput(Input.SELECT, InputType.PRESSED, () -> {
      Vector2 screenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      Vector2 uiPos = getUIStage().screenToStageCoordinates(screenPos.cpy());
      if (collidesMenu(uiPos)) return;


      Vector2 position = screenToStageCoordinates(screenPos);
      priorityItemAt((int)position.x, (int)position.y).ifPresent(pi -> {
        player.getFocusedItem().ifPresent((fi) -> fi.setFocus(false));
        pi.setFocus(true);
        pi.getProperty(PropertyType.Interactable, InteractableProperty.class).get().interact(new GameContext(this, player));
      });

    });
    BackManager.setOnEmpty(() -> {
      GameSettingsDialog gsd = new GameSettingsDialog(GameScreen.this);
      gsd.show(getUIStage());
    });
  }

  @Override
  public void act(float time) {
    player.act(time);
    GameContext ctx = new GameContext(this, player);
    for (Item item : this.pollItems) {
      item.getProperty(PropertyType.ConditionallyActive, ConditionallyActive.class).ifPresent((cap) -> {
        if (!cap.poll(ctx))
          pollItems.removeValue(item, false);
      });
      item.getProperty(PropertyType.ConditionallyVisible, ConditionallyVisible.class).ifPresent((cvp) -> {
        cvp.poll(ctx);
      });
    }
    super.act(time);

    if (!metadata.gameSettings.persistentReveal){
      resetRevealed();
    }

    Rectangle hitbox = player.getOccupiedRegion();
    reveal((int) (hitbox.x + hitbox.width / 2), (int) hitbox.y);
  }

  public void reveal(int x, int y) {
    if (y < 0 || x < 0 || y >= gridSize.height || x >= gridSize.width) {
      return;
    }

    int numRays = 360;

    for (float angle = 0; angle < numRays; angle++) {
      float radians = MathUtils.degreesToRadians * angle;
      float dx = MathUtils.cos(radians);
      float dy = MathUtils.sin(radians);
      castRay(x, y, dx, dy, 15);
    }
  }

  private boolean shouldBlockRay(Item item){
    if (item.hasProperty(PropertyType.Barrier)) {
      if (item.hasProperty(PropertyType.LockedProperty)){
        LockedProperty prop = item.getProperty(PropertyType.LockedProperty, LockedProperty.class).get();
        if (!prop.isLocked()) return false;
      }
      return true;
    }
    return false;
  }

  private void castRay(int x, int y, float dx, float dy, int maxDistance) {
    IntVector2 tl = new IntVector2(), 
               tr = new IntVector2(),
               bl = new IntVector2(),
               br = new IntVector2();
    TreeSet<IntVector2> tiles = new TreeSet<>(new Comparator<IntVector2>() {
      @Override
      public int compare(IntVector2 arg0, IntVector2 arg1) {
        if (arg0.equals(arg1)) return 0;
        if (arg0.x != arg1.x) return arg0.x - arg1.x;
        return arg0.y - arg1.y;
      }
    });

    // add 0.5 and floor to round up or down more equally
    float newX = x + 0.5f;
    float newY = y + 0.5f;

    for (int i = 0; i < maxDistance; i++) {

      int tileX = MathUtils.floor(newX);
      int tileY = MathUtils.floor(newY);

      if (tileY < 0 || tileX < 0 || tileY >= gridSize.height || tileX >= gridSize.width) {
        break;
      }

      Optional<Item[]> oItems = getItemsAt(tileX, tileY);
      if (oItems.isPresent()) {
        for (Item item : oItems.get()) {
          if (shouldBlockRay(item)){
            return;
          }
        }
      }

      if (!revealed[tileY][tileX]) {
        revealTile(tileX, tileY);
      }

      float tmpX = newX + dx;
      float tmpY = newY + dy;
      tl.set(newX, newY);
      tr.set(newX, tmpY);
      bl.set(tmpX, newY);
      br.set(tmpX, tmpY);

      tiles.clear();
      tiles.add(tl);
      tiles.add(tr);
      tiles.add(bl);
      tiles.add(br);

      for (IntVector2 tile : tiles) {
        Optional<Item[]> items = getItemsAt(tile);
        if (items.isEmpty()) continue;

        for (Item item : items.get()) {
          if (shouldBlockRay(item)) return;
        }
      }

      newX = tmpX;
      newY = tmpY;
    }
  }

  private void revealTile(int x, int y) {
    revealed[y][x] = true;
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        if (Math.abs(dx) == Math.abs(dy)) {
          continue;
        }

        int cX = x + dx;
        int cY = y + dy;

        if (cX < 0 || cX >= gridSize.width || cY < 0 || cY >= gridSize.height) {
          continue;
        }
        fowTiles[cY][cX].updateTiles(revealed, new Vector2(cX, cY), true);

      }
    }
  }

  @Override
  public TreeSet<Drawable> getDrawables() {
    TreeSet<Drawable> drawables = grid.getDrawables();
    drawables.add(player);
    return drawables;
  }

  @Override
  public void draw() {
    super.draw(false);
    for (int y = 0; y < gridSize.height; y++) {
      for (int x = 0; x < gridSize.width; x++) {
        if (!revealed[y][x]) {
          batch.draw(fowTiles[y][x].texture, x, y, 1, 1);
        }
      }
    }
    if (inFlightItem != null) inFlightItem.draw(batch);
    batch.end();
  }

  private class GameMapTarget extends DragAndDrop.Target {
    GameMapTarget(Actor proxy){
      super(proxy);
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
      // Calculate game coordinates
      IntVector2 gameCoords = IntVector2.fromVector2(
        getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()))
      );

      // Ensure in bounds
      if (!gameCoords.contained(0, 0, gridSize.width - 1, gridSize.height - 1)) return false;
      // Ensure revealed
      if (!revealed[(int) gameCoords.y][(int) gameCoords.x]) return false; 
      // Ensure placement range
      if (player.getCenter().dst(gameCoords.asVector2()) > 4) return false;
      // Ensure PlayerInventorySource
      if (!(source instanceof PlayerInventorySource)) return false; 

      // Get the item payload
      Item item = ((ItemPayload) payload).getItem();

      // Initialize item preview if needed
      if (inFlightItem == null) {
        inFlightItem = item;
        inFlightItem.setAlpha(0.75f);
      }

      if (canPlace(item, gameCoords)) {
        // Set position
        inFlightItem.setPosition(gameCoords.x, gameCoords.y);
        return true;
      }
    return false;
  }

    @Override
    public void reset(Source source, Payload payload) {
      if (inFlightItem == null) return;

      inFlightItem.setAlpha(1);

      inFlightItem.remove(false);
      inFlightItem = null;
    }

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
      if (inFlightItem == null) return;

      inFlightItem.setContained(false);
      inFlightItem.setAlpha(1);

      // Calculate game coordinates
      IntVector2 gameCoords = IntVector2.fromVector2(
        getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()))
      );
      if (canPlace(inFlightItem, gameCoords)) {
        inFlightItem.moveTo(gameCoords.x, gameCoords.y);
        grid.placeItem(inFlightItem);
      }

      Utils.maybeUpateSurroundingTileables(inFlightItem, GameScreen.this);
      inFlightItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class).ifPresent((tbp) -> {
        tbp.refreshAdjacency(GameScreen.this);
      });

      inFlightItem = null;
    }
    
  }


  public Array<Item> itemsNear(Circle circle) {
      Array<Item> ret = new Array<>();
      for (Item item : grid.placedItems.values()) {
          if (Collisions.collides(circle, item.getOccupiedRegion())) {
              ret.add(item);
          }
      }
      return ret;
  }

  public Array<Item> itemsNear(Vector2 center, float radius) {
     return itemsNear(new Circle(center.x, center.y, radius));
  }
}
