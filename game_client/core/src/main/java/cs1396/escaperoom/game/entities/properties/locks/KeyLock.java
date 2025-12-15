package cs1396.escaperoom.game.entities.properties.locks;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConfigurationMenu;
import cs1396.escaperoom.editor.ui.Menu;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.editor.ui.Menu.MenuEntryBuilder;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.FragileProperty;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.UnlockerProperty;
import cs1396.escaperoom.game.entities.properties.base.LockingMethod;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.state.GameEvent;
import cs1396.escaperoom.game.state.GameEvent.EventType;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSelectUI;
import cs1396.escaperoom.ui.ItemSelectUI.SelectedItem;

public class KeyLock extends LockingMethod {

  private Array<SelectedItem> selectedItems = new Array<>();

  private Optional<Item> holdsKey(GameContext ctx){
    for (Item i : ctx.player.getInventory()) {
      if (selectedItems.contains(new SelectedItem(i), false)){
        return Optional.of(i);
      }
    }
    return Optional.empty();
  }

  /**
   * Check if an item is fragile, if so, "break" it.
   */
  private void checkFragile(GameContext ctx, Item item){
    item.getProperty(PropertyType.Fragile, FragileProperty.class).ifPresent((fp) -> {
      if (fp.isTrue()){
        ctx.player.removeItemFromInventory(item);
        ctx.map.getEventBus().post(
          new GameEvent.Builder(EventType.ItemStateChange, ctx)
            .message(item.getItemName() + " was fragile and broke!")
            .build()
        );
      }
    });
  }

  /**
   * Try to set lock status to {@code isLocked}
   *
   * @return {@code Some(key)} if valid  
   */
  private Optional<Item> trySetLock(GameContext ctx, boolean isLocked){
    Optional<Item> maybeKey = holdsKey(ctx);

    if (maybeKey.isEmpty()){
      String msg = "Hm, " + owner.get().getItemName() +
                   " is still " + (this.isLocked ? "locked": "unlocked");
      ctx.map.getEventBus().post(
        new GameEvent.Builder(EventType.ItemStateChange, ctx)
          .message(msg)
          .build()
      );
    } else {
      updateLocked(ctx, isLocked);
    }

    return maybeKey;
  }

  protected class LockAction extends AbstractLockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      trySetLock(ctx, true).ifPresent((key) -> checkFragile(ctx, key));
      return ActionResult.DEFAULT;
    }
  };

  protected class UnlockAction extends AbstractUnlockAction {
    @Override
    public ActionResult act(GameContext ctx) {
      trySetLock(ctx, false).ifPresent((key) -> checkFragile(ctx, key));
      return ActionResult.DEFAULT;
    }
  };

  @Override
  public String getName() {
    return "Key Lock";
  }

  @Override
  public LockingMethodType getType() {
    return LockingMethodType.KeyLock;
  }

  @Override
  public MenuEntry getDisplay(Menu parent) {
    return new MenuEntryBuilder(parent, getName())
      .spawns((e) -> {
        return new ConfigurationMenu<ItemSelectUI>(e,configurationDisplay((LevelEditor)parent.getScreen()), "Unlocked By", parent.getScreen());
      })
      .build();
  }

  private ItemSelectUI configurationDisplay(LevelEditor editor){
    HashSet<Item> potentialValues = new HashSet<>();
    for (Item i : editor.getGrid().items.values()) {
      i.getProperty(PropertyType.UnlocksProperty, UnlockerProperty.class).ifPresent((p) -> {
        potentialValues.add(i);
      });
    }
    Array<Item> potentialValueArray = Array.with(potentialValues.toArray(new Item[0]));

    ItemSelectUI ui = new ItemSelectUI(potentialValueArray,"No unlocker items on the grid!", selectedItems,  true, editor);
    return ui;
  }


  @Override
  protected LockingMethod getEmptyMethod() {
    return new KeyLock();
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeArrayStart("unlocked_by");
    selectedItems.forEach((si) -> {
      if (si != null && si.getItem() != null) json.writeValue(si.getItem().getID());
    });
    json.writeArrayEnd();
  }

  @Override
  public void read(Json json, JsonValue data) {
    super.read(json, data);

    JsonValue arr = data.get("unlocked_by");

    Array<Integer> ids = new Array<>();

    if (arr != null) arr.forEach((val) -> ids.add(val.asInt()));

    if (!ids.isEmpty()){
      Grid.onMapCompletion.add((g) -> {
        ids.forEach((id) -> selectedItems.add(new SelectedItem(g.items.get(id))));
        return null;
      });
    }
  }

  @Override
  protected AbstractLockAction getLockAction() {
    return new LockAction();
  }

  @Override
  protected AbstractUnlockAction getUnlockAction() {
    return new UnlockAction();
  }
}
