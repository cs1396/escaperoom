package group24.escaperoom.game.entities.conditions;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.editor.ui.RequiredItemEntry.RequiredItem;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.state.GameContext;
import group24.escaperoom.screens.MapScreen;
import group24.escaperoom.game.entities.properties.connectors.ConnectorSink;

public class ItemsArePowered extends RequiresItems {

  @Override
  protected Array<Item> getPotentialItems(MapScreen map){
    Array<Item> potentialItems = new Array<>();
    map.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.ConnectorSink)) {
        potentialItems.add(item);
      }
    });
    return potentialItems;
  }

	@Override
	protected String getEmptyMessage() {
    return "No connector sinks on the map!";
	}

	@Override
	public boolean evaluate(GameContext ctx) {
    for (RequiredItem i : items) {
      boolean isPowered = i.getItem().getProperty(PropertyType.ConnectorSink, ConnectorSink.class).get().isConnected();
      if (!i.getRequired().matches(isPowered)) {
        return false;
      }
    }
    return true;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.ItemsArePowered;
	}

	@Override
	public String getName() {
    return "Item is powered";
	}
}
