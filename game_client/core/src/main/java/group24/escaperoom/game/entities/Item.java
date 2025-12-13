package group24.escaperoom.game.entities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

import group24.escaperoom.editor.tools.TiledBrush;
import group24.escaperoom.engine.assets.AssetManager;
import group24.escaperoom.engine.assets.items.ItemTypeData;
import group24.escaperoom.engine.render.Drawable;
import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.engine.types.Size;
import group24.escaperoom.game.entities.conditions.ItemContainsItem;
import group24.escaperoom.game.entities.player.Player;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.AnimatedProperty;
import group24.escaperoom.game.entities.properties.connectors.ConnectorRelay;
import group24.escaperoom.game.entities.properties.connectors.ConnectorSource;
import group24.escaperoom.game.entities.properties.ContainsItemProperty;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.SpinnableProperty;
import group24.escaperoom.game.entities.properties.Stylable;
import group24.escaperoom.game.entities.properties.TiledBrushable;
import group24.escaperoom.game.entities.properties.base.ItemProperty;
import group24.escaperoom.game.entities.properties.util.PropertyMap;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.world.Grid;
import group24.escaperoom.screens.GameScreen;
import group24.escaperoom.screens.LevelEditor;
import group24.escaperoom.screens.MapScreen;
import group24.escaperoom.ui.notifications.Notifier;

public class Item implements Json.Serializable, Drawable {
  Logger log = Logger.getLogger(Item.class.getName());

  /**
   * A reference to the MapScreen that this item is current on.
   */
  @Null
  public MapScreen map;

  /**
   * A region in world units that blocks the player
   */
  Optional<BlockingParams> blockRegion = Optional.empty();

  /**
   * A static variable marking the id to be assigned to the next instantiated item
   */
  static int nextID = 0;

  /**
   * A reference to the texture region used for selection indication
   */
  static AtlasRegion selectHighlight;

  /**
   * Whether or not this item has player focus
   */
  boolean playerFocus;

  /**
   * A class decribing this items type data
   */
  ItemTypeData objectTypeData;

  /**
   * A reference to the UI stage of the screen this item is on
   */
  JsonValue jsonData;

  /**
   * Whether this item is contained in another item
   */
  boolean contained = false;

  public int id; // Item ID
  AtlasRegion texture; // Item Texture
  public IntVector2 position = new IntVector2(0, 0); // Position in world units
  /**
   * Logical rotation of this item.
   *
   * Valid values are [0, 90, 180, 270]
   *
  */
  private int rotation = 0; 
  /**
   * The render priority of this item.
   *
   * This is how "high up" the item is in the world.
   *
   *
   */
  public int renderPriority = 0;

  /**
   * Whether this item is currently selected
   */
  public boolean selected = false;

  /**
   * Whether this item is currently highlighted
   */
  private boolean highlight = false;

  /**
   * Whether this item is currently dimmed
   */
  private boolean dim = false;

  /**
   * {@code flipped} represents whether or not an item is rotated in such a way
   * that it's width and height are "flipped".
   *
   * This is only relevant for items with widths and heigts that are not equal
   *
   * e.g.
   *  _______
   * |       |
   *  -------
   * 
   */
  public boolean flipped = false;

  /**
   * Whether this item's texture is mirrored horizontally ({@code mirrorH}) 
   * and/or mirrored vertically ({@code mirrorV})
   */
  public boolean mirrorH = false, mirrorV = false;

  /**
   * {@code color} is the color applied to this batch before drawing this item
   */
  public Color color = new Color(1,1,1,1);

  /**
   * {@code occupiedSize} is the dynamically adjusted size of this item
   * that changes with rotations
   */
  public Size occupiedSize = new Size(0, 0);

  /**
   * {@code item} is the effectively final size of the item in world units,
   * as if no transformations have occured.
   */
  public Size itemSize = new Size(0, 0);

  /**
   * The origin of this items {@link AtlasRegion} in the underlying
   * {@link Texture}
   */
  IntVector2 textureOrigin;

  /**
   * A hashmap of all this item's properties, keyed on {@link PropertyType}
   */
  PropertyMap propertyMap = new PropertyMap();

  /**
   * Helper class to hold information related to the 
   * sub-region of this item which blocks player movment
   */
  private class BlockingParams {
    float offsetX = 0;
    float offsetY = 0;
    Vector2 blockSize;

    private BlockingParams(float oX, float oY, float w, float h) {
      this(oX, oY, new Vector2(w, h));
    }

    private BlockingParams(float oX, float oY, Vector2 dims) {
      offsetX = oX;
      offsetY = oY;
      blockSize = dims;
    }
  }

  //----------------------------------------------------------------------------
  // INITIALIZATION
  //----------------------------------------------------------------------------

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public Item() {
    if (selectHighlight == null) {
      selectHighlight = AssetManager.instance().getRegion("selection_outline");
    }
    texture = AssetManager.instance().getRegion("placeholder");
    id = nextID++;
  }

  /**
   * Construct an item from {@link ItemTypeData}
   *
   * @param typeData to construct from
   */
  public Item(ItemTypeData typeData) {
    initItem(typeData);
  }

  public void initItem(ItemTypeData typeData) {
    objectTypeData = typeData;

    position = new IntVector2(0, 0);
    itemSize = objectTypeData.size.copy();
    occupiedSize = itemSize.copy();

    renderPriority = objectTypeData.renderPriority;

    setTexture(new AtlasRegion(AssetManager.instance().getRegion(this.objectTypeData.texture)));

    // add all properties to this item
    objectTypeData.propertyParameters.forEach((PropertyType prop, JsonValue propParams) -> {

      // initialize an empty property and set the owner
      ItemProperty<? extends ItemPropertyValue> p = prop.getEmptyProperty();
      if (p == null) {
        Notifier.error(String.format("Tried to load invalid property on item %s", typeData.name));
        return;
      }

      p.setOwner(this);

      // read the json
      p.read(new Json(), propParams);

      // add it!
      addProperty(p);
    });

    // Some properties may want to know if an item has another given property,
    // so they register callbacks during their initialization. After all properties
    // have been added,
    // we call them!
    PropertyMap.applyCallbacks();
  }



  /**
   * Write our this items json to {@code json}
   */
  @Override
  public void write(Json json) {
    json.writeValue("item_name", objectTypeData.name);
    json.writeValue("type_category", objectTypeData.category);
    json.writeValue("is_contained", contained);
    json.writeValue("id", id);
    json.writeValue("width", itemSize.width);
    json.writeValue("height", itemSize.height);
    json.writeValue("x", position.x);
    json.writeValue("y", position.y);
    json.writeValue("rotation", getRotation());
    json.writeValue("flipped", flipped);
    json.writeValue("texture", objectTypeData.texture);
    json.writeValue("mirror_h", mirrorH);
    json.writeValue("mirror_v", mirrorV);

    int tile_depth;
    try {
      tile_depth = map.getTileDepthOf(this);
    } catch (Exception e) {
      tile_depth = -1;
    }

    json.writeValue("tile_depth", tile_depth);
    json.writeValue("render_priority", this.objectTypeData.renderPriority);
    json.writeArrayStart("properties");
    this.propertyMap.write(json);
    json.writeArrayEnd();
  }

  /**
   * Read item json to initialize this item
   */
  @Override
  public void read(Json json, JsonValue jsonData) {

    // Store this items json data for cloning purposes
    this.jsonData = jsonData;

    // Retrieve all our type information
    String typeCategory = jsonData.getString("type_category");
    String item_name = jsonData.getString("item_name");
    int width = jsonData.getInt("width");
    int height = jsonData.getInt("height");
    int render_priority = jsonData.getInt("render_priority");
    String texture = jsonData.getString("texture");

    // Build the ObjectTypeData
    this.objectTypeData = new ItemTypeData(item_name, typeCategory, new Size(width, height), texture, render_priority,
        null);


    // Set the ID
    id = jsonData.getInt("id");

    // Increment our nextID if needed
    if (id >= nextID) Item.nextID = id + 1;

    // Intialize all data fields
    setTexture(new AtlasRegion(AssetManager.instance().getRegion(objectTypeData.texture)));
    setRotation(jsonData.getInt("rotation"));
    itemSize = objectTypeData.size.copy();
    contained = jsonData.getBoolean("is_contained", false);
    mirrorH = jsonData.getBoolean("mirror_h", false);
    mirrorV = jsonData.getBoolean("mirror_v", false);
    occupiedSize = objectTypeData.size.copy();
    flipped = jsonData.getBoolean("flipped");
    if (flipped){
      flip();
      flipped = true;
    }
    renderPriority = objectTypeData.renderPriority;
    position.x = jsonData.getInt("x");
    position.y = jsonData.getInt("y");
    propertyMap.readWrapper(this, json, jsonData.get("properties"));
  }


  //----------------------------------------------------------------------------
  // OPERATIONS
  //----------------------------------------------------------------------------


  public String printString() {
    return new Json().prettyPrint(this);
  }

  /**
   * Update item state
   *
   * @param delta amount of time since the last call
   */
  public void update(float delta) {
    getProperty(PropertyType.Animated, AnimatedProperty.class).ifPresent((a) -> {
      a.maybeAdvance(delta);
    });

    if (map instanceof GameScreen) {
      getProperty(PropertyType.ConnectorSource, ConnectorSource.class).ifPresent((csp) -> {
        csp.propagate(new GameContext((GameScreen) map), new HashSet<>());
      });
      getProperty(PropertyType.ConnectorRelay, ConnectorRelay.class).ifPresent((csp) -> {
        csp.propagate(new GameContext((GameScreen) map), new HashSet<>());
      });
    }
  }

  /**
   * Force an item to reload its texture
   */
  public void reloadTexture() {
    setTexture(new AtlasRegion(AssetManager.instance().getRegion(this.objectTypeData.texture)));
    for (ItemProperty<? extends ItemPropertyValue> p : getProperties()) {
      p.updateTexture();
    }
  }

  /**
   * Get an exact clone of this item, copying all attributes except {@link Item#id}
   *
   * @return an clone of this item with a new id
   */
  public Item clone() {
    return clone(false);
  }

  /**
   * Get an exact clone of this item
   *
   * @param preserveID whether or not to assign a new id to the newly created item
   * @return A clone of this item
   */
  public Item clone(boolean preserveID) {
    Item i = new Item();

    int newID = i.getID();

    // We are cloning an Item that was not deserialized
    jsonData = new JsonReader().parse(new Json().toJson(this));
    // this copies the id which is not what we want
    i.read(new Json(), this.jsonData);
    if (preserveID) {
      nextID -= 1;
    } else {
      i.id = newID;
    }
    return i;
  }

  /**
   * @return a copy of this item, respecting any property values 
   * that are non-copyable (like {@link ItemContainsItem})
   */
  public Item copy(){
    Item newItem = new Item();
    newItem.initItem(getType());

    propertyMap.forEach((t, p)->{
      newItem.addProperty(p.cloneProperty(newItem));
    });

    return newItem;
  }

  /**
   * @return the User-facing String describing this Item
   *
   * - This includes an optional style prefix followed by the name spcecified by {@link ItemTypeData#name}
   */
  public String getItemName() {
    if (this.hasProperty(PropertyType.Stylable)) {
      return this.getProperty(PropertyType.Stylable, Stylable.class).get().getCurrentValue().getStyleName()
          + " " + objectTypeData.name;
    } else {
      return objectTypeData.name;
    }
  }

  /**
   * @param ctx the {@link GameContext}
   * @return the available {@link PlayerAction}s for this item
   */
  public Array<PlayerAction> getPlayerActions(GameContext ctx) {
    Array<PlayerAction> actions = new Array<>();
    for (ItemProperty<? extends ItemPropertyValue> p : getProperties()) {
      actions.addAll(p.getActions(ctx));
    }
    return actions;
  };

  /**
   * Set the region of the underlying texture for this item
   *
   * @param x region-local offset in the x-direction
   * @param y region-local offset in the y-direction
   * @param width new width
   * @param height new height
   */
  public void adjustTextureRegion(int x, int y, int width, int height) {
    texture.setRegion(textureOrigin.x + x, textureOrigin.y + y, width, height);
  }

  /**
   * Gain {@link Player} focus
   */
  private void gainFocus() {
    setSelected(true);
  }

  /**
   * Lose {@link Player} focus
   */
  private void loseFocus() {
    setSelected(false);
  }

  @Override
  public String toString() {
    return getItemName();
  }

  @Override
  public boolean equals(Object obj) {
    if (Item.class.isInstance(obj)) {
      return Item.class.cast(obj).getID() == this.getID();
    }
    return false;
  }


  /**
   * Remove this item from the map
   *
   * @param temporary whether or not this removal is temporary 
   *                  this param signals to the map that 
   *                  this item will likely soon be replaced
   */
  public void remove(boolean temporary) {
    if (map != null) {
      if (!temporary && hasProperty(PropertyType.ContainsItemsProperty)) {
        getProperty(PropertyType.ContainsItemsProperty, ContainsItemProperty.class).ifPresent((cip) -> {
          cip.getCurrentValues().forEach((ci) -> {

            Item contained = ci.getItem();
            if (contained == null ||
                !contained.isContained() || 
                map.itemIsPlaced(contained)
            ) {
              return;
            }

            map.grid.items.remove(ci.getItem().getID());
          });
        });
      }
      map.removeItemFromGrid(this, temporary);

      // Update all the surronding tileables in the old position to reflect
      // this new adjaceny
      getProperty(PropertyType.TiledBrushable, TiledBrushable.class).ifPresent((tbp) -> {
        TiledBrush.updateSurroundingTiles(getPosition().cpy(), map, this);
      });
    }
  }

  /**
   * Remove this item from the map
   * @see  Item#remove(boolean)
   */
  public void remove(){
    remove(false);
  }

  //----------------------------------------------------------------------------
  // PROPERTY OPERATIONS
  //----------------------------------------------------------------------------

  /**
   * Add a property to this item
   * @param property to add
   */
  public void addProperty(ItemProperty<? extends ItemPropertyValue> property) {
    propertyMap.put(property.getType(), property);
  }

  /**
   * remove a property to from item
   *
   * @param type to remove
   */
  public void removeProperty(PropertyType type) {
    propertyMap.remove(type);
  }

  /**
   * @param type to query
   * @return whether this item has a certain property
   */
  public boolean hasProperty(PropertyType type) {
    return propertyMap.containsKey(type);
  }

  public Collection<ItemProperty<? extends ItemPropertyValue>> getProperties() {
    return propertyMap.values();
  }

  public Optional<ItemProperty<? extends ItemPropertyValue>> getProperty(PropertyType type) {
    ItemProperty<? extends ItemPropertyValue> prop = propertyMap.get(type);
    return prop == null ? Optional.empty() : Optional.of(prop);
  }

  public <T extends ItemPropertyValue, P extends ItemProperty<T>> Optional<P> getProperty(PropertyType type,
      Class<P> expectedClass) {
    P prop;
    try {
      prop = expectedClass.cast(propertyMap.get(type));
    } catch (Exception e) {
      return Optional.empty();
    }
    return prop == null ? Optional.empty() : Optional.of(prop);
  }

  public Array<ItemProperty<? extends ItemPropertyValue>> getProperties(PropertyType... types) {
    Array<ItemProperty<? extends ItemPropertyValue>> ps = new Array<>();
    for (PropertyType type : types) {
      ItemProperty<? extends ItemPropertyValue> p = propertyMap.get(type);
      if (p != null) {
        ps.add(p);
      }
    }
    return ps;
  }

  //----------------------------------------------------------------------------
  // ATTRIBUTE MUTATORS
  //----------------------------------------------------------------------------

  /**
   * Set whether this item's texture is mirrored horizontally
   */
  public void mirrorHorizontal(){
    this.mirrorH = !this.mirrorH;
  }

  /**
   * Set the render priority of this item 
   * @param priority new value
   */
  public void setRenderPriority(int priority){
    renderPriority = priority;
    objectTypeData.renderPriority = priority;
  }

  /**
   * Set width
   *
   * - This is the original width of the item, not the currently occupied width
   *
   * @param width new value
   */
  public void setWidth(int width){
    itemSize.width = width;
    objectTypeData.size.width = width;

    if (flipped) occupiedSize.height = width;
    else occupiedSize.width = width;
  }

  /**
   * Set height
   *
   * - This is the original height of the item, not the currently occupied height
   *
   * @param height new value
   */
  public void setHeight(int height){
    itemSize.height = height;
    objectTypeData.size.height = height;

    if (flipped) occupiedSize.width = height;
    else occupiedSize.height = height;
  }

  /**
   * Increase this item's render priority
   */
  public void increaseRenderPriotity(){
    this.renderPriority += 1;
    this.objectTypeData.renderPriority += 1;
  }

  /**
   * Decrease this item's render priority
   */
  public void decreaseRenderPriotity(){
    this.renderPriority = Math.max(0, this.renderPriority - 1);
    this.objectTypeData.renderPriority = Math.max(0, this.renderPriority - 1);
  }

  /**
   * Set whether this item's texture is mirrored veritcally
   */
  public void mirrorVertical(){
    this.mirrorV = !this.mirrorV;
  }

  private void preMove(){
    remove(true);
  }

  private void postMove(){
    if (map != null){
      map.placeItem(this);
      TiledBrush.updateSurroundingTiles(getPosition(), map, this);
    }
  }

  public void setHighlighed(boolean highlighted){
    highlight = highlighted;
  }

  public void setDimmed(boolean dimmed){
    dim = dimmed;
  }

  /**
   * Set the position of this item
   *
   * @see Item#moveTo
   *
   * @param pos new value
   */
  public void setPosition(IntVector2 pos) {
    setPosition(pos.x, pos.y);
  }

  /**
   * Set the position of this item
   *
   * @see Item#moveTo
   *
   * @param x new x coord
   * @param y new y coord
   */
  public void setPosition(int x, int y) {
    position.x = x;
    position.y = y;
  }

  /**
   * Move the item.
   *
   * This differs from {@link Item#setPosition(int,int)} in it 
   * updates various properties that depend on position.
   *
   * e.g. {@link TiledBrushable} items need to refresh their adjacency
   *
   * @param x new x coord
   * @param y new y coord
   */
  public void moveTo(int x, int y) {
    if (position.x == x && position.y == y) return;

    preMove();
    setPosition(x, y);
    postMove();
  }

  /**
   *
   * Set if this object should block player movement
   *
   * @param offsetX from the bottom left corner for the start of the blocking region
   * @param offsetY from the bottom left corner for the start of the blocking region
   * @param blockWidth width of the blocking region
   * @param blockHeight height of the blocking region
   *
   */
  public void setBlocksPlayer(float offsetX, float offsetY, float blockWidth, float blockHeight) {
    this.blockRegion = Optional.of(
        new BlockingParams(
            offsetX,
            offsetY,
            new Vector2(blockWidth, blockHeight)));
  }

  /**
   * Set the texture of this item.
   *
   * @param region new region
   */
  public void setTexture(AtlasRegion region) {
    textureOrigin = new IntVector2(region.getRegionX(), region.getRegionY());
    texture = region;
    objectTypeData.texture = region.name;
  }

  /**
   * Set whether this item is currently focused by the {@link Player}
   *
   * @param isFocused whether or not is focused
   */
  public void setFocus(boolean isFocused) {
    playerFocus = isFocused;
    if (playerFocus) {
      gainFocus();
    } else {
      loseFocus();
    }
  }

  public void setContained(boolean isContained) {
    this.contained = isContained;
  }

  /**
   *
   * Set if this object should block player movement
   *
   * The region that is blocked is assumed to be the region defined by
   *
   * <pre>
   * new Rectangle(getX(), getY(), getWidth(), getHeight());
   * </pre>
   *
   * Unless a different region was specified
   *
   * {@link Item#setBlocksPlayer(int,int,int,int)}
   *
   * @param blocksPlayer whether or not the entire item blocks the player
   *
   */
  public void setBlocksPlayer(boolean blocksPlayer) {
    if (blocksPlayer) {
      this.blockRegion = Optional.of(new BlockingParams(0, 0, new Vector2(this.occupiedSize.width, this.occupiedSize.height)));
    } else {
      this.blockRegion = Optional.empty();
    }
  }

  /**
   * Set whether this object is selected in the {@link LevelEditor}
   *
   * @param isSelected whether or not the item is selected
   *
   */
  public void setSelected(boolean isSelected) {
    selected = isSelected;
  }


  /**
   * @param color new color
   */
  public void setColor(Color color){
    this.color.r = color.r;
    this.color.g = color.g;
    this.color.b = color.b;
  }

  /**
   * @param r red component
   * @param g green component
   * @param b blue component
   * @param a alpha component
   */
  public void setColor(float r, float b, float g, float a){
    this.color.set(r, g, b, a);
  }

  /**
   * Convience method to set the alpha component of this items's {@link Color}
   *
   * @param alpha new alpha
   */
  public void setAlpha(float alpha) {
    color.a = alpha;
  }

  public float getAlpha() {
    return color.a;
  }

  //----------------------------------------------------------------------------
  //  ROTATION  OPERATIONS
  //----------------------------------------------------------------------------

  /**
   * Temporarily removes this item from the {@link Grid} during rotation
   *
   * - Replaced by calling {@link Item#postRotate(int)}
   */
  private void preRotate() {
    remove(true);
  }

  /**
   * Set the logical rotation of this item
   *
   * @param degrees new value
   */
  public void setRotation(int degrees) {
    if (trySpin(degrees)) {
      return;
    }
    
    int oldRotation = getRotation();
    degrees = degrees % 360;

    preRotate();
    rotation = degrees;
    postRotate(oldRotation);
  }

  /**
   * Add {@code degrees} to this item's logical rotation
   *
   * @param degrees amount to add
   */
  public void rotateBy(int degrees) {
    int oldRotation = getRotation();
    degrees = degrees % 360;
    int newRotation = (oldRotation + degrees) % 360;

    setRotation(newRotation);
  }

  /**
   * Items are temorarily removed from the map during rotation
   *
   * - Tries to place the item onto the current {@link Item#map} if it is non-null
   * - Tries to {@link Item#flip()} the current item
   *
   */
  private void postRotate(int oldRotation) {
    maybeFlip();

    // Items are temporarily removed from the map during rotation,
    // Replace this item if we can, otherwise, reset the rotation
    if (map != null){
      if (map.canPlace(this, position)) {
        map.placeItem(this);
      } else {
        this.setRotation(oldRotation);
      }
    }
  }

  /**
   * This function swaps the item's occuped with and height,
   * toggling its {@code flipped} state
   */
  public void flip() {
    // Swap height and width
    int oldTileHeight = getHeight();
    int oldTileWidth = getWidth();

    occupiedSize.width = oldTileHeight;
    occupiedSize.height = oldTileWidth;

    flipped = !flipped;
  }

  /**
   * Given the new value for this item's rotation, spin that item if 
   * it has the {@link SpinableProperty}
   *
   * @return whether or not this item had the {@link SpinableProperty} 
   *         and therefore was "spun"
   */
  private boolean trySpin(float degrees) {
    if (!hasProperty(PropertyType.Spinable)){
      return false;
    }
    SpinnableProperty sp = getProperty(PropertyType.Spinable, SpinnableProperty.class).get();

    // store our previous rotation
    int prevRotation = getRotation();

    // ensure degrees is a valid range
    degrees = degrees % 360;


    preRotate();
    sp.setSpin(SpinnableProperty.degreesToSpinCount(degrees));
    postRotate(prevRotation);

    return true;
  }

  /**
   * {@link Item#flip()} this item if necessary.
   *
   * That is, when it's flip state is not consistent with it's current rotation
   * (like immediately after updating rotation)
   */
  private void maybeFlip() {
    float degrees = getRotation();
    degrees = degrees % 360;

    if (!flipped && (degrees == 90 || degrees == 270)) {
      flip();
    } else if (flipped && (degrees == 180 || degrees == 0)) {
      flip();
    }
  }

  //----------------------------------------------------------------------------
  // ATTRIBUTE ACCESSORS
  //----------------------------------------------------------------------------

  /**
   * @return x coordinate of the lower left corner of this item
   *
   */
  public int getX() {
    switch (rotation){
      case 0:
      case 90:
        return position.x;
      case 180:
      case 270:
        return position.x - occupiedSize.width + 1;
    }
    return position.x;
  }

  /**
   * @return y coordinate of the lower left corner of this item
   */
  public int getY() {
    switch (rotation){
      case 0:
      case 270:
        return position.y;
      case 90:
      case 180:
        return position.y - occupiedSize.height + 1;
    }
    return position.y;
  }

  /**
   *
   * @return an {@link Optional} region where this object should block player movement
   *
   * If {@code Optional.empty()} is returned, this object should not block player
   * movement
   *
   * @see Item#setBlocksPlayer(boolean)
   *
   *
   */
  public Optional<Rectangle> blockingRegion() {
    return blockRegion
        .map((bp) -> new Rectangle(
            getX() + bp.offsetX,
            getY() + bp.offsetY,
            bp.blockSize.x,
            bp.blockSize.y));
  }

  /**
   * @return this item's texture 
   */
  public AtlasRegion getTexture() {
    return texture;
  }

  /**
   *
   * This is different than the {@link Item#blockingRegion()}
   * in that every object must have an occupied region, but not all objects must
   * block the player.
   *
   * For objects which block the player, it need not be that the blocking region
   * == occupied region
   *
   * @return the items occupied region
   */
  public Rectangle getOccupiedRegion() {
    return new Rectangle(getX(), getY(), getWidth(), getHeight());
  }

  /**
   * @return the lower left corner of this item
   */
  public IntVector2 getPosition() {
    return new IntVector2(getX(), getY());
  }

  /**
   * @return the {@link ItemTypeData} describing this item
   */
  public ItemTypeData getType() {
    return objectTypeData;
  }

  /**
   * @return the occupied size of this item.
   *
   * @see Item#occupiedSize
   */
  public Size getOccupiedSize() {
    return occupiedSize;
  }

  public int getID() {
    return this.id;
  }

  /**
   *
   * @return ths items height
   *
   * {@link Item#getOccupiedSize()}
   */
  public int getHeight() {
    return occupiedSize.height;
  }

  /**
   *
   * @return ths items width
   *
   * {@link Item#getOccupiedSize()}
   */
  public int getWidth() {
    return occupiedSize.width;
  }

  /**
   *
   * @return whether or not this item is contained
   *
   */
  public boolean isContained() {
    return contained;
  }

  /**
   * @return the logical rotation of this item
   */
  public int getRotation() {
    return getProperty(PropertyType.Spinable, SpinnableProperty.class)
        .map((p) -> p.getSpinCount() * 90)
        .orElse(rotation);
  }

  //---------------------------------------------------------------------------
  // RENDER LOGIC
  //---------------------------------------------------------------------------

  /**
   * Draw rotation versus logical rotation is relevant for
   * {@link SpinnableProperty} items
   *
   * The draw rotation for a spinnable item is always 0;
   *
   * {@link Item#draw}
   *
   * @return the draw rotation
   *
   */
  public float getDrawRotation() {
    if (hasProperty(PropertyType.Spinable)) {
      return 0;
    } else {
      return 360 - rotation;
    }
  }

  //---------------------------------------------------------------------------
  // DRAWABLE
  //
  // {@link Drawable} impls
  //
  //---------------------------------------------------------------------------
  @Override
  public int renderPriority() {
    return renderPriority;
  }

  @Override
  public Vector2 position() {
    return getPosition().asVector2();
  }

  @Override
  public int getTileDepth() {
    return Grid.current().getTileDepthOf(this);
  }

  private IntVector2 mirrorOffsets(){
    IntVector2 ofst = new IntVector2(0,0);

    if (mirrorH) ofst.x = itemSize.width;
    if (mirrorV) ofst.y = itemSize.height;
    // if (rotation % 180 == 0) {
    //   if (mirrorH) ofst.x = itemSize.width;
    //   if (mirrorV) ofst.y = itemSize.height;
    // } else {
    //   if (mirrorV) ofst.x = itemSize.width;
    //   if (mirrorH) ofst.y = itemSize.height;
    // }

    return ofst;
  }

  private IntVector2 mirrorFactors(){
    IntVector2 facts = new IntVector2(1,1);

    if (mirrorH) facts.x = -1;
    if (mirrorV) facts.y = -1;
    // if (rotation % 180 == 0) {
    //   if (mirrorH) facts.x = -1;
    //   if (mirrorV) facts.y = -1;
    // } else {
    //   if (mirrorH) facts.x = -1;
    //   if (mirrorV) facts.y = -1;
    // }

    return facts;
  }

  @Override
  public void draw(Batch batch) {
    Color drawColor = color.cpy();

    if (dim) drawColor.a = 0.2f;
    if (highlight) drawColor.a = 1;

    // set the color to that of this item
    batch.setColor(drawColor);

    IntVector2 mirOfsts = mirrorOffsets();
    IntVector2 mirFct = mirrorFactors();

    // draw on the batch
    batch.draw(
        texture,                      // texture
        position.x + mirOfsts.x,      // x
        position.y + mirOfsts.y,      // y
        0.5f - mirOfsts.x,            // x for rotation origin
        0.5f - mirOfsts.y,            // y for rotation origin
        itemSize.width * mirFct.x,    // width
        itemSize.height * mirFct.y,   // height
        1, 1,                         // scalex, scale y
        getDrawRotation()             // rotation
    );

    // if we are selected, draw our outline
    if (selected) {
      batch.setColor(1, 1, 1, 0.75f);
      batch.draw(
          selectHighlight,            // texture
          position.x + mirOfsts.x,    // x
          position.y + mirOfsts.y,    // y
          0.5f - mirOfsts.x,          // x for rotation origin
          0.5f - mirOfsts.y,          // y for rotation origin
          itemSize.width * mirFct.x,  // width
          itemSize.height * mirFct.y, // height
          1, 1,                       // scalex, scale y
          getDrawRotation()           // rotation
      );
    }

    // reset the color!
    batch.setColor(1, 1, 1, 1);
  }
}
