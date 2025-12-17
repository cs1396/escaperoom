package cs1396.escaperoom.screens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import cs1396.escaperoom.editor.events.EditorEvent;
import cs1396.escaperoom.editor.events.EditorEvent.EventType;
import cs1396.escaperoom.editor.events.EditorEventBus;
import cs1396.escaperoom.engine.assets.AssetManager;
import cs1396.escaperoom.engine.assets.maps.MapData;
import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.engine.types.Size;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.connectors.Utils;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.game.world.Grid.Tile;
import cs1396.escaperoom.screens.utils.CamMan;

public abstract class MapScreen extends AbstractScreen {
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(MapScreen.class.getName());
  protected Actor roomProxy;
  protected Image room;
  public Grid grid;
  protected final Batch batch;
  protected MapData mapData;
  protected Size gridSize;
  protected CamMan cameraManager;

  public MapData getMapData() {
    return mapData;
  }

  /**
   * Remove an item from the grid
   *
   * @param temporary whether or not this is likely a temporary removal
   */
  public void removeItemFromGrid(Item item, boolean temporary) {
    if (this instanceof GameScreen) {
      GameContext ctx = new GameContext((GameScreen) this);
      // Set false on removal of a connectable, this may or may not propage the signal
      Utils.isConnectable(item).ifPresent((ci) -> {
        ci.connectable.setActive(false, ctx);
      });
    }

    if (!temporary && this instanceof LevelEditor){
      EditorEventBus.post(new EditorEvent(EventType.ItemRemoved, item));
    }

    // Remove from grid
    grid.removeItem(item);

    // Try and update textures
    Utils.maybeUpateSurroundingTileables(item, this);
  }

  /**
   * Update the room proxy on the ui stage to align with
   * the game stage room
   */
  protected void updateProxy() {
    Vector2 proxyOrigin = gameCoordToUI(new Vector2(0, 0));
    Vector2 proxyDiagonalPoint = gameCoordToUI(new Vector2(gridSize.width, gridSize.height));
    roomProxy.setBounds(proxyOrigin.x, proxyOrigin.y, proxyDiagonalPoint.x - proxyOrigin.x,
        proxyDiagonalPoint.y - proxyOrigin.y);
  }

  /**
   * @return whether or not the an item fufilling {@code predicate} exists on the map
   */
  public boolean containsItemWhere(Predicate<Item> predicate) {
    return grid.containsItemWhere(predicate);
  }

  /**
   * @return an {@link Optional} containing the first item fulfilling the given {@code predicate}
   */
  public Optional<Item> findItemWhere(Predicate<Item> predicate) {
    return grid.findItemWhere(predicate);
  }

  /**
   * @return an {@link Optional} with all items in this square.
   *
   * - the inner array can be empty
   * - {@link Optional} just protects out of bounds access
   */
  public Optional<Item[]> getItemsAt(IntVector2 pos) {
    return grid.getAt(pos.x, pos.y).map(t -> t.getContainedItems());
  }

  /**
   * @return an {@link Optional} with all items in this square.
   *
   * - the inner array can be empty
   * - {@link Optional} just protects out of bounds access
   */
  public Optional<Item[]> getItemsAt(int x, int y) {
    return grid.getAt(x, y).map(t -> t.getContainedItems());
  }

  /**
   * @see MapScreen#getItemsAt(int, int)
   */
  public Optional<Item[]> getItemsAt(float x, float y) {
    return this.getItemsAt((int) x, (int) y);
  }

  /**
   * Get a list of all items present in a certain region
   *
   * - This returns items also partially contained in the region
   */
  public HashSet<Item> getItemsIn(Rectangle region) {
    int minX = (int) region.getX();
    int minY = (int) region.getY();
    int maxX = (int) (region.getX() + region.getWidth());
    int maxY = (int) (region.getY() + region.getHeight());

    HashSet<Item> items = new HashSet<>();

    for (int y = minY; y < maxY; y++) {
      for (int x = minX; x < maxX; x++) {
        grid.getAt(x, y)
            .map((t) -> t.getContainedItems())
            .ifPresent((is) -> {
              for (Item i : is) {
                items.add(i);
              }
            });
      }
    }

    return items;

  }

  /**
   * @return whether or not {@code item} can be placed at {@code newPos}
   */
  public static boolean canPlace(Item item, IntVector2 newPos, Grid grid) {
    for (int dy = 0; dy < item.getHeight(); dy++) {
      for (int dx = 0; dx < item.getWidth(); dx++) {
        Optional<Tile> t = grid.getAt(newPos.x + dx, newPos.y + dy);
        if (t.isEmpty()) {
          return false;
        }

        Tile tile = t.get();
        if (!tile.canAdd(item)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @return whether or not {@code item} can be placed at {@code newPos}
   */
  public boolean canPlace(Item item, IntVector2 newPos) {
    return canPlace(item, newPos, grid);
  }

  public MapScreen(MapData mapdata) {
    super();

    this.batch = new SpriteBatch();
    this.mapData = mapdata;
    this.gridSize = mapdata.getStartGrid().getSize();
    this.cameraManager = new CamMan((OrthographicCamera) getCamera());
    cameraManager.setZoom(mapData.getMetadata().gameSettings.defaultZoom);

    // Add gridded room
    AssetManager.instance().load("textures/tile.png", Texture.class);
    AssetManager.instance().finishLoading();
    Texture tile = AssetManager.instance().get("textures/tile.png", Texture.class);
    tile.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

    TextureRegion roomTexture = new TextureRegion(tile);
    roomTexture.setRegion(0, 0, tile.getWidth() * gridSize.width, tile.getHeight() * gridSize.height);

    room = new Image(roomTexture);
    room.setPosition(0, 0);
    room.setSize(gridSize.width, gridSize.height);
    room.setName("room");

    // So, create a proxy actor the same size as the room that we can then use to
    // project onto the actual game stage
    roomProxy = new Actor();
    updateProxy();
    addUI(roomProxy);
    addActor(room);
  }

  public void loadGrid(Grid grid) {
    if (grid == null) return;

    this.grid = grid;
    for (Item item : grid.items.values()) {
      item.map = this;
    }
  }


  /**
   * @return all items associated with this map
   *
   * - not all these items are necessarily placed
   */
  public Collection<Item> getItems() {
    return grid.items.values();
  }

  /**
   * Place an item on the grid
   *
   */
  public boolean placeItem(Item item) {
    item.map = this;
    return grid.placeItem(item);
  }

  public Grid getGrid() {
    return grid;
  }

  public TreeSet<Drawable> getDrawables() {
    return grid.getDrawables();
  }

  public void draw(boolean endBatch){
    Camera camera = getViewport().getCamera();
    camera.update();

    batch.setProjectionMatrix(getCamera().combined);
    batch.begin();
    getRoot().draw(batch, 1); // clear the screen

    TreeSet<Drawable> drawables = getDrawables();
    for (Drawable i : drawables) {
      i.draw(batch);
    }

    if (endBatch) batch.end();
  }

  @Override
  public void draw() {
    draw(true);
  }

  public boolean itemIsPlaced(Item i){
    return grid.placedItems.containsKey(i.getID());
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    updateProxy();

    for (Item i : grid.placedItems.values()){
      i.update(delta);
    }

    room.setZIndex(0);
    roomProxy.setZIndex(0);
  }

  @Override
  public void resize(int w, int h) {
    super.resize(w, h);
    if (roomProxy != null) {
      updateProxy();
    }
  }

  public int getTileDepthOf(Item item) {
    return grid.getTileDepthOf(item);
  }
}
