package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSelectUI;
import cs1396.escaperoom.ui.ItemSelectUI.SelectedItem;

public class PlayerHasItem extends Conditional {
  SelectedItem item = new SelectedItem();
  Logger log = Logger.getLogger(PlayerHasItem.class.getName());

  @Override
  public boolean evaluate(GameContext ctx) {
    if (item.getItem() != null) {
      return ctx.player.getInventory().contains(this.item.getItem(), false);
    }
    return false;
  }

  @Override
  public void write(Json json) {
    json.writeValue("item_id", item.getItem() == null ? -1 : item.getItem().getID());
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    int i = jsonData.getInt("item_id", -1);
    Grid.onMapCompletion.add((grid) -> {
      if (i != -1) {
        this.item = new SelectedItem(grid.items.get(i));
      }
      return null;
    });
  }

  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor) {
    Array<Item> potentialItems = new Array<>();
    editor.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.Obtainable)) {
        potentialItems.add(item);
      }
    });

    return Optional.of(new ItemSelectUI(potentialItems, this.item, "No obtainable items currently on the grid", editor));
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.PlayerHasItem;
  }

  @Override
  public String getName() {
    return "Player has item";
  }
}
