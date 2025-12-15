package cs1396.escaperoom.game.world;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.engine.types.Size;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.screens.MapScreen;

public class Grid implements Json.Serializable {
  private static Logger log = Logger.getLogger(Grid.class.getName());

  /**
   * {@code items} contains all items involved with this grid.
   * This includes invisible items, contained items, etc.
   */
  public HashMap<Integer, Item> items = new HashMap<>();

  /**
   * {@code placedItems} is a subset of {@code items}
   * 
   * {@code placedItems} only contains items which are visible and represented
   * on a tile.
   */
  public HashMap<Integer, Item> placedItems = new HashMap<>();


  public static class DrawableComparator implements Comparator<Drawable>{
    @Override
    public int compare(Drawable i1, Drawable i2) {
      int renderPriority1 = i1.renderPriority();
      int renderPriority2 = i2.renderPriority();
      if (renderPriority1 != renderPriority2) {
        return Integer.compare(renderPriority1, renderPriority2); // Higher priority comes later
      }

      float y1 = i1.position().y;
      float y2 = i2.position().y;
      if (y1 != y2) {
        return Float.compare(y2, y1); // Higher position in world is rendered underneath
      }

      /**
       * See Grid#getTileDepthOf
       */
      int stackLevel1 = i1.getTileDepth();
      int stackLevel2 = i2.getTileDepth();
      if (stackLevel1 != stackLevel2) {
        // A "higher" stack depth means that it should be rendered underneath 
        return Integer.compare(stackLevel2, stackLevel1); 
      }
      return Integer.compare(i1.hashCode(), i2.hashCode());
    }
  }

  public TreeSet<Drawable> getDrawables() {
    return placedItems.values().stream()
        .map(i -> (Drawable) i)
        .collect(Collectors.toCollection(() -> new TreeSet<>(new DrawableComparator())));
  }



  /**
   * {@link ItemProperty} initialization can depend on all items being available
   * in the map.
   * e.g. A container might need references to its inner items.
   *
   * During initialization, those properties can register functions here which
   * will be called
   * on map completion.
   */
  public static HashSet<Function<Grid, Void>> onMapCompletion = new HashSet<>();


  /**
   * Inner array holding all tiles
   */
  Tile[][] inner;
  int width, height;

  static Grid instance;

  /**
   * Get the current grid instance
   */
  public static Grid current() {
    return instance;
  }

  /**
   * A Tile of the Grid
   */
  public class Tile implements Json.Serializable {

    /**
     * Whether or not this Tile is occupied
     */
    boolean occupied = false;
    /**
     * The current stack size on this Tile
     */
    public int stacksize = 0;
    /**
     * Maximum number of items allowed on any given Tile
     */
    private final static int MAX_STACK = 5;
    /**
     * Position of this Tile on the Grid
     */
    int x, y;
    /**
     * Inner array
     * Valid indicies are [0, stacksize - 1]
     */
    Item[] items = new Item[Tile.MAX_STACK];

    /**
     * @return whether {@code item} can be added to this Tile
     */
    public boolean canAdd(Item item) {
      return !occupied;
    }

    @Override
    public String toString() {
      String ret = "[";
      for (int i = 0; i < MAX_STACK; i++) {
        ret += stacksize > i ? items[i].getID() : "__";
      }
      return ret + "]";
    }

    /**
     * @return whether or not any part of {@code item} is contained in this tile
     */
    public Item[] getContainedItems() {
      Item[] contained = new Item[stacksize];
      for (int i = 0; i < stacksize; i++) {
        contained[i] = items[i];
      }
      return contained;
    }

    /**
     * @return whether or not any part of {@code item} is contained in this tile
     */
    public boolean contains(Item item) {
      for (int i = 0; i < stacksize; i++) {
        if (items[i].getID() == item.getID()) {
          return true;
        }
      }
      return false;
    }

    /**
     * Remove an item from this tile
     */
    private void remove(Item item) {
      for (int i = 0; i < stacksize; i++) {
        if (items[i].getID() == item.getID()) {

          // reduce stacksize
          this.stacksize -= 1;
          // removing an item results in an unoccupied tile
          this.occupied = false;

          // shift items towards front of array;
          for (int j = i; j < MAX_STACK - 1; j++) {
            items[j] = items[j + 1];
          }
          items[MAX_STACK - 1] = null;
          return;
        }
      }
    }

    public int getX() {
      return x;
    }

    public int getY() {
      return y;
    }

    /**
     * Add an item to this tile
     *
     * - precondition -> {@link Tile#canAdd} returns true
     */
    public Tile addItem(Item item) {
      if (contains(item)) {
        log.severe("Trying to add item to tile which already contains item?");
        return this;
      }

      if (occupied || stacksize >= MAX_STACK) {
        throw new IllegalStateException(
            "Invalid state in Tile::setObject -> stack is " + stacksize + " occupied is " + occupied);
      }
      this.items[stacksize] = item;
      stacksize += 1;
      occupied = (stacksize == MAX_STACK);
      return this;
    }

    public Tile(int x, int y) {
      this.x = x;
      this.y = y;
    }

    /**
     * Empty constructor for {@link Json.Serializable} compatability 
     */
    public Tile() {}

    /**
     * Write this tile to json
     */
    @Override
    public void write(Json json) {
      Item[]  items = getContainedItems();

      if (items.length < 1)
        return;

      json.writeObjectStart();
      json.writeValue("x", this.x);
      json.writeValue("y", this.y);
      json.writeValue("stacksize", this.stacksize);
      json.writeValue("occupied", this.occupied);
      json.writeArrayStart("items");
      for (int i = 0; i < items.length; i++) {
        json.writeObjectStart();
        json.writeValue("index", i);
        json.writeValue("id", items[i].getID());
        json.writeObjectEnd();
      }
      json.writeArrayEnd();
      json.writeObjectEnd();
    }

    /**
     * Initialze this tile from a {@link JsonValue}
     */
    @Override
    public void read(Json json, JsonValue jsonData) {
      x = jsonData.getInt("x");
      y = jsonData.getInt("y");
      stacksize = jsonData.getInt("stacksize");
      occupied = jsonData.getBoolean("occupied");

      jsonData.get("items").forEach((JsonValue itemInfo) -> {
        int id = itemInfo.getInt("id");

        Optional<Item> oItem = Grid.this.getItemByID(id);
        if (oItem.isEmpty()) {
          log.severe("Item ID: " + id + " could not be found in the item map");
        }

        placedItems.put(id, oItem.get());
        this.items[itemInfo.getInt("index")] = oItem.get();
      });
    }
  }

  public Optional<Item> getItemByID(int id) {
    Item i = items.get(id);
    return i == null ? Optional.empty() : Optional.of(i);
  }

  public Grid() {
    Grid.instance = this;
  }

  public Grid(int w, int h) {
    this.width = w;
    this.height = h;
    this.inner = newEmptyInner(width, height);
    Grid.instance = this;
  }

  private Tile[][] newEmptyInner(int width, int height) {
    Tile[][] grid = new Tile[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        grid[y][x] = new Tile(x, y);
      }
    }
    return grid;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

  /**
   * @return whether or not a placed item on the grid fufills {@code predicate}
   */
  public boolean containsItemWhere(Predicate<Item> predicate) {
    return findItemWhere(predicate).isPresent();
  }

  public Size getSize() {
    return new Size(width, height);
  }

  /**
   * Find the first item on the grid where {@code predicate} is true
   */
  public Optional<Item> findItemWhere(Predicate<Item> predicate) {
    for (int y = this.height - 1; y >= 0; y--) {
      for (int x = 0; x < this.width; x++) {
        Tile t = inner[y][x];
        for (int i = 0; i < t.stacksize; i++) {
          if (predicate.test(t.items[i])) {
            return Optional.of(t.items[i]);
          }
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Get the tile at {@code x, y} if it is within bounds
   */
  public Optional<Tile> getAt(int x, int y) {
    if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
      return Optional.empty();
    } else {
      return Optional.of(inner[y][x]);
    }
  }

  public void printGrid() {
    for (int y = this.height - 1; y >= 0; y--) {
      System.out.printf("%2d", y);
      for (int x = 0; x < this.width; x++) {
        Tile t = inner[y][x];
        if (t.getX() != x || t.getY() != y) {
          throw new IllegalStateException("tile is not in correct position");
        }
        System.out.print(inner[y][x].toString());
      }
      System.out.println();
    }
    System.out.printf(" ");
    for (int x = 0; x < this.width; x++) {
      System.out.printf("%10d", x);
    }
    System.out.println();
  }

  /**
   *
   * Place an {@code item} on the Grid
   *
   * - a precondition to this function is that {@link MapScreen#canPlace} returned true
   */
  public boolean placeItem(Item item) {
    if (placedItems.containsKey(item.getID())) {
      return false;
    }

    placedItems.put(item.getID(), item);

    if (!items.containsKey(item.getID())){
      items.put(item.getID(), item);
    }

    int bX = item.getX();
    int bY = item.getY();
    for (int y = 0; y < item.getHeight(); y++) {
      for (int x = 0; x < item.getWidth(); x++) {
        inner[bY + y][bX + x].addItem(item);
      }
    }
    return true;
  }

  /**
   * Return the tile depth of this item
   *
   * For a stack of items in a given tile
   * 
   *      "TOP"
   *       ______________
   *      |    item A     |
   *      | ndx=4 dpth=0  |
   *      |_______________|
   *      |    item B     |
   *      | ndx=3 dpth=1  |
   *      |_______________|
   *      |    item C     |
   *      | ndx=2 dpth=2  |
   *      |_______________|
   *      |    item D     |
   *      | ndx=1 dpth=3  |
   *      |_______________|
   *      |    item E     |
   *      | ndx=0 dpth=4  |
   *      |_______________|
   *
   *      "BOTTOM"
   * 
   */
  public int getTileDepthOf(Item item) {
    int x = item.getX();
    int y = item.getY();

    if (x < 0 || x >= this.width || y < 0 || y >= this.height) {
      return -1;
    }

    Tile t = inner[y][x];
    Item[] items = t.getContainedItems();

    for (int i = 0; i < items.length; i++) {
      if (items[i] == null)
        return -1;

      if (items[i].getID() == item.getID()) {
        return items.length - i - 1;
      }
    }
    return -1;
  }

  /**
   * Remove this item from the grid.
   *
   * - This only removes the item from the grid and 
   *           does perform any additionally operation that 
   *           may need to occur on item removal
   */
  public void removeItem(Item item) {
    placedItems.remove(item.getID());

    if (!item.isContained()){
      items.remove(item.getID());
    }

    int bX = item.getX();
    int bY = item.getY();
    for (int y = 0; y < item.getHeight(); y++) {
      for (int x = 0; x < item.getWidth(); x++) {
        inner[bY + y][bX + x].remove(item);
      }
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("width", this.width);
    json.writeValue("height", this.height);

    json.writeArrayStart("items");
    items.forEach((Integer id, Item i) -> {
      json.writeObjectStart();
      json.writeValue("id", id);
      i.write(json);
      json.writeObjectEnd();
    });
    json.writeArrayEnd();

    json.writeArrayStart("tiles");
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.width; x++) {
        inner[y][x].write(json);
      }
    }
    json.writeArrayEnd();
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    instance = this;
    width = jsonData.getInt("width");
    height = jsonData.getInt("height");

    JsonValue itemJson = jsonData.get("items");
    itemJson.forEach((itemData) -> {
      Item i = new Item();
      i.read(json, itemData);
      items.put(itemData.getInt("id"), i);
    });

    this.inner = newEmptyInner(width, height);

    JsonValue tiles = jsonData.get("tiles");
    tiles.forEach((tileData) -> {
      Tile tile = new Tile();
      tile.read(json, tileData);
      inner[tile.y][tile.x] = tile;
    });

    onMapCompletion.forEach((f) -> f.apply(this));
    onMapCompletion.clear();
  }
}
