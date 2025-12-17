package cs1396.escaperoom.game.entities.properties;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConfigurationMenu;
import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.editor.ui.Menu.MenuEntry;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.player.PlayerAction;
import cs1396.escaperoom.game.entities.properties.base.ItemProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.values.ContainedItem;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.state.GameEvent;
import cs1396.escaperoom.game.state.GameEvent.EventType;
import cs1396.escaperoom.game.ui.ContainerUI;
import cs1396.escaperoom.game.ui.GameDialog;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSelectUI;
import cs1396.escaperoom.ui.ItemSelectUI.SelectedItem;
import cs1396.escaperoom.ui.widgets.G24TextInput.StringInput;

public class ContainsItemProperty
    extends ItemProperty<ContainedItem> {

  private static final PropertyDescription description = new PropertyDescription(
    "Contains Items",
    "Can contain other items",
    "Items with the contains items property can hold items inside them.",
    null
  );

  private String actionVerb = "Open";

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  protected Array<SelectedItem> selectedItem = new Array<>();
  @SuppressWarnings("unused")
  private Logger log = Logger.getLogger(ContainsItemProperty.class.getName());

  public class OpenAction implements PlayerAction {

    @Override
    public String getActionName() {
      return actionVerb;
    }

    @Override
    public ActionResult act(GameContext ctx) {
      Optional<LockedProperty> locked = owner.getProperty(PropertyType.LockedProperty, LockedProperty.class);
      if (locked.isPresent() && locked.get().isLocked()) {

        LockedProperty lockedProp = locked.get();
        PlayerAction unlockAction = lockedProp.getCurrentValue().getUnlockAction(ctx);

        if (unlockAction != null) unlockAction.act(ctx);

        if (lockedProp.isLocked()){
          ctx.map.getEventBus().post(
            new GameEvent.Builder(EventType.ItemStateChange, ctx)
              .message("Can't open " + owner.getItemName() + ", it is still locked!")
              .build()
          );
          return ActionResult.DEFAULT;
        }
      }
      return new ActionResult().showsDialog(
          new GameDialog.Builder("Contained...", ctx.player)
            .withContent(new ContainerUI( ContainsItemProperty.this, ctx.player))
            .build()
      );
    }

    @Override
    public boolean isValid(GameContext ctx) {
      return true;
    }
  }

  public void addValue(ContainedItem o) {
    selectedItem.add(new SelectedItem(o.inner));
  }

  public void removeItem(Item i) {
    selectedItem.removeValue(new SelectedItem(i), false);
  }

  @Override
  public Array<ContainedItem> getPotentialValues() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return "Contained Items";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ContainsItemsProperty;
  }

  @Override
  public void set(Array<ContainedItem> value) {
    selectedItem.clear();
    value.forEach(c -> addValue(c));
  }


  @Override
  public MenuType getInputType() {
    return MenuType.PopOut;
  }

  @Override
  public ConfigurationMenu<ItemSelectUI> getPopOut(MenuEntry parent) {
    LevelEditor editor = (LevelEditor)parent.getScreen();

    Array<Item> potentialValues = new Array<>();
    for (Item item : editor.getGrid().placedItems.values()) {
      if (item.getID() == owner.getID()) {
        continue;
      }
      if (item.hasProperty(PropertyType.Containable)) {
        potentialValues.add(item);
      }
    }
    this.selectedItem.forEach((si) -> potentialValues.add(si.getItem()));

    ItemSelectUI ui = new ItemSelectUI(potentialValues, 
                                       "No containable items are currently on the grid!",
                                       selectedItem, true, editor);
    ui.setOnSelect((i) -> {
      i.setContained(true);
      i.remove(false);
      return null;
    });
    ui.setOnDeselect((i) -> {
      i.setContained(false);
      editor.placeItem(i);
      return null;
    });
    ConfigurationMenu<ItemSelectUI> m = new ConfigurationMenu<>(parent, ui, "Contains Items", editor);
    return m;
  }

  @Override
  public Array<ContainedItem> getCurrentValues() {
    Array<ContainedItem> items = new Array<>();
    selectedItem.forEach((si) -> {
      items.add(new ContainedItem(si.getItem(), owner));
    });
    return items;
  }

  @Override
  public Array<PlayerAction> getAvailableActions() {
    return Array.with(new OpenAction());
  }

  @Override
  public ContainsItemProperty cloneProperty(Item newOwner) {
    ContainsItemProperty prop = new ContainsItemProperty();
    prop.owner = newOwner;
    // should not allow multiple ContainsItemProperty items to contain
    // the same item, so simply returns a new empty property.
    // --> prop.selectedItem.addAll(selectedItem);
    return prop;
  }

  @Override
  public void write(Json json) {
    json.writeArrayStart("contained");
    this.selectedItem.forEach((contained) -> {
      json.writeValue(contained.getItem().getID());
    });
    json.writeArrayEnd();
    json.writeValue("action", actionVerb);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue containedData = jsonData.get("contained");
    Array<Integer> ids = new Array<>();
    if (containedData != null) {
      containedData.forEach((JsonValue itemJson) -> {
        try {
          ids.add(itemJson.asInt());
        } catch (Exception e) {
          Item i = new Item();
          i.read(json, itemJson);
          ids.add(i.getID());
        }
      });
    }

    actionVerb = jsonData.getString("action", "Open");

    Grid.onMapCompletion.add((g) -> {
      ids.forEach((i) -> {
        this.selectedItem.add(new SelectedItem(g.items.get(i)));
      });
      return null;
    });
  }

  @Override
  public Class<ContainedItem> getValueClass() {
    return ContainedItem.class;
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();

    config.addStringInput(
      "Action Label",
      "Action label to view contained items. Default is \"Open\"",
      new StringInput(
        actionVerb,
        (newVal) -> actionVerb = newVal)
    );

    return Optional.of(config);
  }
}
