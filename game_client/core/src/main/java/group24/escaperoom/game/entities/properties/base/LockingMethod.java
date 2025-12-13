package group24.escaperoom.game.entities.properties.base;

import java.util.Optional;
import java.util.function.Function;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;

import group24.escaperoom.editor.ui.Menu;
import group24.escaperoom.editor.ui.Menu.MenuEntry;
import group24.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.player.PlayerAction;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.locks.LockingMethodType;
import group24.escaperoom.game.entities.properties.values.ItemPropertyValue;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.game.state.GameEvent;
import group24.escaperoom.game.state.GameEvent.EventType;
import group24.escaperoom.screens.GameScreen;

abstract public class LockingMethod implements Json.Serializable, ItemPropertyValue {
  protected boolean isBarrier = false;
  protected Optional<Item> owner = Optional.empty();
  protected boolean isLocked = true;

  // display name (Combination Lock, KeyLock)
  abstract public String getName();

  public boolean isLocked(){
    return isLocked;
  }

  public Array<PlayerAction> getActions(){
    Array<PlayerAction> actions = new Array<>();

    PlayerAction la = getLockAction();
    PlayerAction ua = getUnlockAction();

    if (la != null) actions.add(la);
    if (ua != null) actions.add(ua);

    return actions;
  }

  protected void updateLocked(GameContext ctx, boolean locked){
    updateLocked(
      ctx,
      locked,
      owner.get().getItemName() + " is now locked!",
      owner.get().getItemName() + " is now unlocked!"
    );
  }

  protected void updateLocked(GameContext ctx, boolean locked, String lockMsg, String unlockMsg){
      // We are locked, update state
      isLocked = locked;
      if (isBarrier) {
        owner.get().setBlocksPlayer(locked);
        owner.get().setAlpha(locked ? 1.0f: 0.5f);
      }

      ctx.map.getEventBus().post(
        new GameEvent.Builder(EventType.ItemStateChange, ctx)
          .message(locked ? lockMsg : unlockMsg)
          .build()
      );
  }

  protected abstract class AbstractLockAction implements PlayerAction {
    @Override
    public String getActionName() {
      return "Lock";
    }

    @Override
    public boolean isValid(GameContext ctx) {
      if (isBarrier){

        // Without this small adjustment, the distance you have
        // to be from the item is frustrating.
        // This allows just enough overlap that the interaction is more 
        // natural while still preventing the player from being stuck
        Rectangle occupied = owner.get().getOccupiedRegion();
        Vector2 occupiedCenter = new Vector2();
        occupied.getCenter(occupiedCenter);
        Rectangle invalidZone = new Rectangle();

        invalidZone.setSize(
          occupied.width - 0.1f,
          occupied.height - 0.1f
        );

        invalidZone.setCenter(occupiedCenter);

        return !isLocked && !ctx.player.getOccupiedRegion().overlaps(invalidZone);
      } else {
        return !isLocked;
      }
    }
  }

  protected abstract class AbstractUnlockAction implements PlayerAction {

    @Override
    public String getActionName() {
      return "Unlock";
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return isLocked;
    }
  }


  private @Null PlayerAction getAction(GameContext ctx, Function<Void, PlayerAction> func){
    if (owner.isEmpty()) return null;

    PlayerAction action = func.apply(null);
    if (action == null || !action.isValid(ctx)){
      return null;
    }

    return action;
  }

  /**
   * Get a player action that would lock this locking method
   *
   * This can be null if the action is not valid
   */
  public @Null PlayerAction getLockAction(GameContext ctx){
    return getAction(ctx, (Void) -> getLockAction());
  }

  /**
   * Get a player action that would unlock this locking method
   *
   * This can be null if the action is not valid
   */
  public @Null PlayerAction getUnlockAction(GameContext ctx){
    return getAction(ctx, (Void) -> getUnlockAction());
  }

  abstract protected AbstractLockAction getLockAction();
  abstract protected AbstractUnlockAction getUnlockAction();

  abstract public LockingMethodType getType();

  public void onAttach(Item item){
    this.owner = Optional.of(item);
    if (item.hasProperty(PropertyType.Barrier)){
      item.setBlocksPlayer(true);
      isBarrier = true;
    }
  }
  public void onDetatch(){
    this.owner = Optional.empty();
  }

  @Override
  public MenuEntry getDisplay(Menu parent){
    return new MenuEntryBuilder(parent, getName()).build();
  }

  @Override
  public void write(Json json) {
    json.writeValue("locked", isLocked);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    isLocked  = jsonData.getBoolean("locked", true);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LockingMethod){
      return this.getType() == LockingMethod.class.cast(obj).getType();
    }
    return false;
  }

  abstract protected LockingMethod getEmptyMethod();

  public LockingMethod clone(Item newOwner) {
    LockingMethod p = this.getEmptyMethod();
    p.owner = Optional.of(newOwner);
    p.isLocked = this.isLocked;
    p.isBarrier = this.isBarrier;
    p.read(new Json(), new JsonReader().parse(new Json().toJson(this)));
    return p;
  }

  public void onGameLoad(GameScreen screen){ }

}
