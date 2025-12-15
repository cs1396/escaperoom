package cs1396.escaperoom.game.entities.conditions;

import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.RequiredItemEntry.RequiredItem;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.ConditionallyActive;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.MapScreen;

public class ItemIsActive extends RequiresItems {
  Logger log = Logger.getLogger(ItemIsActive.class.getName());

  @Override
  protected Array<Item> getPotentialItems(MapScreen map) {
    Array<Item> potentialItems = new Array<>();
    map.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.ConditionallyActive)) {
        potentialItems.add(item);
      }
    });
    return potentialItems;
  }

  @Override
  protected String getEmptyMessage() {
    return "No Conditionally active items on the map";
  }

  @Override
  public boolean evaluate(GameContext ctx) {
    for (RequiredItem ri : items) {
      Item i = ri.getItem();
      // SAFETY: we should only be able to select ConditionallyActive items for the
      // ItemIsActive conditional
      ConditionallyActive cap = i.getProperty(PropertyType.ConditionallyActive, ConditionallyActive.class).get();
      boolean valid = cap.isValid(ctx);

      log.fine("evaluating ItemIsActive for" + i.getItemName() + " (ID: " + i.getID() + ") -> evaluted as " + valid);

      if (!valid){
        log.fine("-> early return for evaluate");
        return false;
      }
    }
    return true;
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.ItemIsActive;
  }

  @Override
  public String getName() {
    return "Item is Active";
  }
}
