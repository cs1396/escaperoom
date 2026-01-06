package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;
import java.util.logging.Logger;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.AreaUI;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSelectUI;
import cs1396.escaperoom.ui.ItemSelectUI.SelectedItem;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ItemInArea extends Conditional {
  Logger log = Logger.getLogger(ItemInArea.class.getName());
  Rectangle targetRegion = new Rectangle();
  SelectedItem item = new SelectedItem();

	@Override
	public void write(Json json) {
    json.writeValue("x", targetRegion.x);
    json.writeValue("y", targetRegion.y);
    json.writeValue("width", targetRegion.width);
    json.writeValue("height", targetRegion.height);
    json.writeValue("item_id", item.getItem() == null ? -1 : item.getItem().getID());
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
    int x = jsonData.getInt("x", 0);
    int y = jsonData.getInt("y", 0);
    int width = jsonData.getInt("width", 0);
    int height = jsonData.getInt("height", 0);
    this.targetRegion = new Rectangle(x, y, width, height);

    int i = jsonData.getInt("item_id", -1);
    Grid.onMapCompletion.add((grid) -> {
      if (i != -1){
        this.item = new SelectedItem(grid.items.get(i));
      }
      return null;
    });
	}

	@Override
	public boolean evaluate(GameContext ctx) {
    if (item.getItem() != null && ctx.map.itemIsPlaced(item.getItem())) {
      return targetRegion.overlaps(item.getItem().getOccupiedRegion());
    }
    return false;
	}

	@Override
	public ConditionalType getType() {
    return ConditionalType.ItemInArea;
	}

  private class ItemInRegionUI extends Table implements HandlesMenuClose {
    private ItemSelectUI itemSelect;
    private AreaUI areaPicker;

    @Override
    public void handle() {
      if (itemSelect != null) itemSelect.handle();
      areaPicker.handle();
    }
  }

  @Override
  public Optional<ItemInRegionUI> getEditorConfiguration(LevelEditor screen) {

    Array<Item> potentialItems = new Array<>();
    screen.grid.items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.Obtainable)) {
        potentialItems.add(item);
      }
    });

    ItemInRegionUI itemInRegionUI = new ItemInRegionUI();

    itemInRegionUI.add(new G24Label("Item", "underline"));
    itemInRegionUI.row();

    ItemSelectUI selectUI = new ItemSelectUI(potentialItems, this.item, "No obtainable items are currently on the grid!", screen);
    itemInRegionUI.add(selectUI);
    itemInRegionUI.row();

    itemInRegionUI.add(new G24Label("Region", "underline"));
    itemInRegionUI.row();

    AreaUI areaUI = new AreaUI(screen, this.targetRegion);
    itemInRegionUI.add(areaUI);

    itemInRegionUI.areaPicker = areaUI;
    itemInRegionUI.itemSelect = selectUI;

    return Optional.of(itemInRegionUI);
  }

	@Override
	public String getName() {
    return "Item in area";
	}
}
