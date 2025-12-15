package cs1396.escaperoom.game.entities.player;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.screens.GameScreen;
import cs1396.escaperoom.services.GameStatistics;
import cs1396.escaperoom.services.GameStatistics.PlayerStatistics;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.physics.Collisions;
import cs1396.escaperoom.engine.render.Drawable;
import cs1396.escaperoom.game.entities.properties.InteractableProperty;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.state.GameEvent.EventType;
import cs1396.escaperoom.game.ui.PlayerInventoryDialog;
import cs1396.escaperoom.game.entities.Item;

public class Player extends Actor implements Drawable {

  public PlayerStatistics stats = new PlayerStatistics();
  Vector2 startPos;
  Vector2 velocity;
  boolean movementProcessed = false;
  AtlasRegion texture;
  HashSet<Integer> obtainedItems = new HashSet<>();
  public int renderPriority = 5;
  int originX;
  int prevX = -1;
  GameScreen gameScreen;
  int prevFrameX = -1;

  public GameScreen getGameScreen() {
    return gameScreen;
  }

  public Direction getDirection() {
    return lastMoveDir;
  }

  public enum Direction {
    North,
    East,
    South,
    West,
    Standing,

    ;
  }

  Direction moveDir = Direction.Standing;
  Direction lastMoveDir = Direction.South;
  Vector2 lastPosition = new Vector2();
  int moveState = 0;
  float lastFrameCheck = 0;
  private PlayerDetails details;
  private int width = 2;
  private int height = 2;
  private static final float INTERACT_RANGE = 2f;
  private Array<Item> inventory;
  private HashSet<Integer> inventoryIDs = new HashSet<>();
  private Array<Item> focusCanidates = new Array<>();
  private Optional<Integer> focusedItem = Optional.empty();
  private Actor room;
  private boolean inventoryOpen = false;
  private Optional<PlayerInventoryDialog> dialog = Optional.empty();

  public PlayerDetails getDetails(){
    return details;
  }

  public void setDetails(PlayerDetails details) {
    this.details = details;
  }
  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
    setWidth(width);
    setHeight(height);
  }
  public void setTexture(AtlasRegion texture) {
    this.texture = texture;
    this.originX = texture.getRegionX();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    move(delta);

    findInteractables();
  }

  @Override
  public void setPosition(float x, float y) {
    super.setPosition(x, y);
    startPos = new Vector2(x, y);
  }

  public void calculateStatistics(GameStatistics stats) {
    this.stats.avgSpeed = this.stats.distanceTraveled / (stats.timeMilliseconds / 1000);
    stats.player = this.stats;
  }

  public Player(int x, int y, GameScreen game) {
    startPos = new Vector2(x, y);

    inventory = new Array<>();
    velocity = new Vector2(0, 0);

    gameScreen = game;

    game.getEventBus().addListener(
      (ev) -> addItemToInventory(ev.source), 
      (ev) -> ev.type == EventType.ItemObtained
    );

    setPosition(x, y);
    setWidth(width);
    setHeight(height);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    batch.draw(
        texture,
        getX(), getY(),
        getWidth(), getHeight());

  }

  public int getRenderPriority() {
    return renderPriority;
  }

  public Vector2 getPosition(){
    return new Vector2(getX(), getY());
  }

  public Vector2 getCenter(){
    return new Vector2(getX(Align.center), getY(Align.center));
  }

  //-----------------------------------------------------------------------------------------
  // INVENTORY 
  //-----------------------------------------------------------------------------------------
  public void findInteractables() {
    final class Canidate {
      Item i;
      float dst;

      Canidate(Item i, float dst) {
        this.i = i;
        this.dst = dst;
      }
    }
    Array<Canidate> candiates = new Array<>();

    // store the previously focused item, if there is one.
    Optional<Item> currItem = getFocusedItem();

    // clear the highlight of the current focused item; 
    // focusItem is necessarily a focusCanidate so it will be cleared by this action
    focusCanidates.forEach((i) -> i.setSelected(false));
    focusCanidates.clear();

    // find all candiate items
    GameContext ctx = new GameContext(gameScreen, this);
    Vector2 center = getCenter();
    for (Item item : gameScreen.itemsNear(this.getCenter(), INTERACT_RANGE)) {
        if (item.hasProperty(PropertyType.Interactable)) {
          Vector2 point = Collisions.getNearestPoint(item.getOccupiedRegion(), getCenter());
          float dst = point.dst2(center);

          Array<PlayerAction> actions = item.getPlayerActions(ctx);
          if (!actions.isEmpty()) {
            candiates.add(new Canidate(item, dst));
          }
        }
    }

    // sort our canidates by distance to player and add all items to focusCanidates
    candiates.sort((a, b) -> Float.compare(a.dst, b.dst));
    candiates.forEach((c) -> focusCanidates.add(c.i));

    // early return for no canidates
    if (focusCanidates.isEmpty()) {
      focusedItem = Optional.empty();
      return;
    }

    if (currItem.isPresent() &&                          // we are currently focusing an item
        lastPosition.equals(getPosition()) &&            // we haven't moved (focus should always update on movement)
        focusCanidates.contains(currItem.get(), false))  // our previously focused item is still a valid canidate
    {
      // the same item might be a different index now (conditions may have come true)
      // SAFETY: must != -1 bc focusCanidates.contains returned true
      focusedItem = Optional.of(focusCanidates.indexOf(currItem.get(), false)); 
      // SAFETY: must be valid index bc array is unmodified and are using index from above
      focusCanidates.get(focusedItem.get()).setFocus(true);
    } else {
      focusedItem = Optional.of(0);
      // SAFETY: focusCanidates is non empty from above check
      focusCanidates.first().setFocus(true);
    }
  }

  public Array<Item> getFocusCanidates(){
    return focusCanidates;
  }

  public Optional<Item> getFocusedItem(){
    return focusedItem.flatMap((i) -> {
      if (i >= 0 && i < focusCanidates.size) return Optional.of(focusCanidates.get(i));
      return Optional.empty();
    });
  }


  public void openInventory() {
    if (!inventoryOpen) {
      inventoryOpen = true;
      PlayerInventoryDialog d = new PlayerInventoryDialog(this);
      this.dialog = Optional.of(d);
      d.show(getGameScreen().getUIStage());
    }
  }

  public Array<Item> getInventory() {
    return inventory;
  }

  public HashSet<Integer> getInventoryIDs(){
    return inventoryIDs;
  }

  public void removeItemFromInventory(Item i) {
    inventory.removeValue(i, false);
    inventoryIDs.remove(i.getID());
    if (inventoryOpen) {
      dialog.ifPresent((d) -> d.inventoryChanged());
    }
  }

  public void addItemToInventory(Item i) {
    if (!obtainedItems.contains(i.getID())) {
      stats.itemsCollected += 1;
      obtainedItems.add(i.getID());
    }

    inventory.add(i);
    inventoryIDs.add(i.getID());

    i.setContained(true);

    if (inventoryOpen) {
      dialog.ifPresent((d) -> d.inventoryChanged());
    }
  }

  public boolean isInventoryOpen() {
    return inventoryOpen;
  }

  public void setInventoryOpen(boolean isOpen) {
    inventoryOpen = isOpen;

    if (!isOpen) dialog = Optional.empty();
  }

  public void printInventory() {
    System.out.println("Items:");
    int ndx = 0;
    for (Item i : this.inventory) {
      System.out.println(String.format("  Item %s, id %d. slot %d", i.getItemName(), i.getID(), ndx));
    }
  }

  //-----------------------------------------------------------------------------------------
  // INPUT 
  //-----------------------------------------------------------------------------------------
  public void registerBinds() {
    registerMovementBinds();
    registerItemBinds();
  }

  private void registerMovementBinds() {
    ControlsManager.registerInput(Input.MOVE_UP,    InputType.HELD, () -> setMoveDir(0,  details.speed, Direction.North));
    ControlsManager.registerInput(Input.MOVE_DOWN,  InputType.HELD, () -> setMoveDir(0, -details.speed, Direction.South));
    ControlsManager.registerInput(Input.MOVE_LEFT,  InputType.HELD, () -> setMoveDir(-details.speed, 0, Direction.West));
    ControlsManager.registerInput(Input.MOVE_RIGHT, InputType.HELD, () -> setMoveDir( details.speed, 0, Direction.East));
  }
  private void setMoveDir(float x, float y, Direction dir) {
    if (movementProcessed) return;

    movementProcessed = true;
    velocity.set(x, y);
    moveDir = dir;
  }

  private void registerItemBinds() {
    ControlsManager.registerInput(Input.PRINT_INVENTORY, InputType.PRESSED, this::printInventory);
    ControlsManager.registerInput(Input.INVENTORY,       InputType.PRESSED, this::openInventory);
    ControlsManager.registerInput(Input.INTERACT,        InputType.PRESSED, () -> {
      focusedItem.ifPresent((i) -> {
        focusCanidates.get(i).getProperty(PropertyType.Interactable, InteractableProperty.class)
          .ifPresent((prop) -> prop.interact(new GameContext(gameScreen, this)));
      });
    });
    ControlsManager.registerInput(Input.CHANGE_INTERACT_FOCUS, InputType.PRESSED, () -> {
      focusedItem.ifPresent((i) -> {
        focusCanidates.get(i).setFocus(false);
      });
      focusedItem = focusedItem.map((i) -> (i + 1) % focusCanidates.size);
      focusedItem.ifPresent((i) -> {
        focusCanidates.get(i).setFocus(true);
      });
    });
  }

  public void stepAnimation(float delta) {
    if (velocity.isZero()) {
      maybeAdvanceFrame(delta, details.textureInfo.idleFrameDelayMS, details.textureInfo.idleFrames);

      if (lastMoveDir != Direction.Standing && moveDir != Direction.Standing) {
        moveDir = Direction.Standing;
        lastFrameCheck = 0;
        moveState = 0;
      }

      updateAnimationFrame();
      return;
    }

    if (moveDir != lastMoveDir) {
      lastMoveDir = moveDir;
      lastFrameCheck = 0;
      moveState = 0;

      updateAnimationFrame();
      return;
    }

    maybeAdvanceFrame(delta, details.textureInfo.moveFrameDelayMS, details.textureInfo.movementFrames);
    updateAnimationFrame();
  }
  private void maybeAdvanceFrame(float delta, float frameDelay, int frameCount) {
    lastFrameCheck = lastFrameCheck + delta;
    if (lastFrameCheck > (frameDelay / 1000f)) {
      lastFrameCheck = 0;
      moveState = (moveState + 1) % frameCount;
    }
  }
  private void updateAnimationFrame() {
    int newFrameOffset;
    if (moveDir != Direction.Standing) {
      newFrameOffset = (4 * details.textureInfo.idleFrames) + (moveDir.ordinal() * details.textureInfo.movementFrames) + moveState;
    } else {
      newFrameOffset = (lastMoveDir.ordinal() * details.textureInfo.idleFrames) + moveState;
    }

    int newFrameX = newFrameOffset * details.textureInfo.frameWidth;

    if (prevFrameX != newFrameX) {
      prevFrameX = newFrameX;
      texture.setRegion(originX + newFrameX, texture.getRegionY(), details.textureInfo.frameWidth, details.textureInfo.frameHeight);
    }
  }

  /**
   * Stage in {@link Player#act} where we try to move
   *
   */
  private void move(float delta) {
    if (room == null) room = gameScreen.getRoot().findActor("room");

    float newX = MathUtils.clamp(
      getX() + velocity.x * delta,
      room.getX(),
      room.getX() + room.getWidth() - getWidth()
    );
    float newY = MathUtils.clamp(
      getY() + velocity.y * delta,
      room.getY(),
      room.getY() + room.getHeight() - getHeight()
    );

    boolean canMove = true;
    Rectangle newPostition = new Rectangle(
      newX + details.hitboxInfo.xOffset,
      newY + details.hitboxInfo.yOffset,
      details.hitboxInfo.width,
      details.hitboxInfo.height
    );

    for (Item item : getGameScreen().itemsNear(new Circle(newX, newY, details.speed))) {
      Optional<Rectangle> blockRegion = item.blockingRegion();
      if (blockRegion.isPresent()) {
        if (blockRegion.get().overlaps(newPostition)) {
          canMove = false;
          break;
        }
      }
    }

    if (canMove) {
      stats.distanceTraveled += getPosition().dst(newX, newY);
      lastPosition.set(getX(), getY());
      setPosition(newX, newY);
    }

    velocity.setZero();
    movementProcessed = false;
  }

  public Rectangle getOccupiedRegion() {
    return new Rectangle(getX(), getY(), width, height);
  }

  @Override
  public int renderPriority() {
    return renderPriority;
  }

  @Override
  public void draw(Batch batch) {
    batch.draw(
        texture,
        getX(), getY(),
        getWidth(), getHeight());
  }

  @Override
  public Vector2 position() {
    return getPosition();
  }

  @Override
  public int getTileDepth() {
    return Integer.MAX_VALUE;
  }
}


