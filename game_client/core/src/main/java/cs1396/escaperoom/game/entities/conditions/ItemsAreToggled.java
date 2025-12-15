package cs1396.escaperoom.game.entities.conditions;

import java.util.logging.Logger;

import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.RequiredItemEntry.RequiredItem;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.Toggleable;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.screens.MapScreen;

public class ItemsAreToggled extends RequiresItems {
  Logger log = Logger.getLogger(ItemsAreToggled.class.getName());


  @Override
  public boolean evaluate(GameContext ctx) {
    for (RequiredItem i : items) {
      boolean isToggled = i.getItem().getProperty(PropertyType.Toggleable, Toggleable.class).get().isToggled();
      if (!i.getRequired().matches(isToggled)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.ItemsAreToggled;
  }

  protected Array<Item> getPotentialItems(MapScreen map){
    Array<Item> potentialItems = new Array<>();
    map.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.Toggleable)) {
        potentialItems.add(item);
      }
    });
    return potentialItems;
  }

  protected String getEmptyMessage(){
    return "No Toggleable Items on the map";
  }


  @Override
  public String getName() {
    return "Items are toggled";
  }
}
