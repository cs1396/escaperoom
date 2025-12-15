package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.RequireItemsUI;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.RequiredItemEntry.ItemRequired;
import cs1396.escaperoom.editor.ui.RequiredItemEntry.RequiredItem;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.screens.MapScreen;
import cs1396.escaperoom.ui.widgets.G24Label;

public abstract class RequiresItems extends Conditional {
  Array<RequiredItem> items = new Array<>();

  public void removeStaleItems(){
    Array<RequiredItem> newArr = new Array<>();
    for (RequiredItem item : this.items){
      if (Grid.current().items.containsKey(item.getItem().getID())){
        newArr.add(item);
      }
    }
    this.items = newArr;
  }

  @Override
  public void write(Json json) {
    removeStaleItems();
    json.writeArrayStart("items");
    items.forEach((i) -> {
      json.writeObjectStart();
      json.writeValue("id", i.getItem().getID());
      json.writeValue("required", i.getRequired().name());
      json.writeObjectEnd();
    });
    json.writeArrayEnd();
  }

  abstract protected Array<Item> getPotentialItems(MapScreen map);
  abstract protected String getEmptyMessage();

  private class EmptyMessage extends G24Label implements HandlesMenuClose {
    EmptyMessage(String msg){
      super(msg);
    }
    @Override
    public void handle() {
    }
  }


  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor stage) {
    removeStaleItems();
    // Find our potential items
    Array<Item> potentialItems = getPotentialItems(stage);

    if (potentialItems.isEmpty()) {
      return Optional.of(new EmptyMessage(getEmptyMessage()));
    }

    return Optional.of(new RequireItemsUI(potentialItems, this.items, stage));
  }


  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue items = jsonData.get("items");

    Grid.onMapCompletion.add((grid) -> {

      if (items != null) {
        items.forEach((tij) -> {
          Item item = grid.items.get(tij.getInt("id"));
          ItemRequired typeRequired = ItemRequired.valueOf(tij.getString("required"));
          RequiredItem ti = new RequiredItem(item, typeRequired);
          this.items.add(ti);
        });
      }
      return null;
    });
  }
}
