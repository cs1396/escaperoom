package cs1396.escaperoom.game.entities.conditions;

import java.util.Optional;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.GridView;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.control.input.InputOverride;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.ContainsItemProperty;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.values.ContainedItem;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.world.Grid;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.ItemSelectUI;
import cs1396.escaperoom.ui.ItemSelectUI.SelectedItem;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ItemContainsItem extends Conditional {

  SelectedItem container = new SelectedItem();
  SelectedItem contained = new SelectedItem();

  @Override
  public boolean evaluate(GameContext ctx) {
    if (container.getItem() == null || contained.getItem() == null) return false;

    return container.getItem().getProperty(PropertyType.ContainsItemsProperty, ContainsItemProperty.class)
        .map((cip) -> {
          for (ContainedItem i : cip.getCurrentValues()) {
            if (i.getItem().getID() == contained.getItem().getID()) {
              return true;
            }
          }
          return false;
        }).orElse(false);
  }

  @Override
  public ConditionalType getType() {
    return ConditionalType.ItemContainsItem;
  }

  @Override
  public String getName() {
    return "Item contains Item";
  }

  /**
   * Remove dependencies on items which were removed.
   *
   * This still allows referencing contained or invisible items
   */
  public void removeStaleItems(){
    if (contained.getItem() == null || !Grid.current().items.containsKey(contained.getItem().getID())) contained = new SelectedItem();
    if (container.getItem() == null || !Grid.current().items.containsKey(container.getItem().getID())) container = new SelectedItem();
  }

  @Override
  public void write(Json json) {
    removeStaleItems();
    json.writeValue("container_id", container.getItem() == null ? -1 : container.getItem().getID());
    json.writeValue("contained_id", contained.getItem() == null ? -1 : contained.getItem().getID());
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    int containerID = jsonData.getInt("container_id", -1);
    int containedID = jsonData.getInt("contained_id", -1);
    Grid.onMapCompletion.add((grid) -> {
      if (containerID != -1){
        this.container = new SelectedItem(grid.items.get(containerID));
      }
      if (containedID != -1){
        this.contained = new SelectedItem(grid.items.get(containedID));
      }

      return null;
    });
  }


  @Override
  public Optional<Actor> getEditorConfiguration(LevelEditor editor) {
    removeStaleItems();

    Array<Item> potentialContainers = new Array<>();
    Array<Item> potentialContained = new Array<>();

    Grid.current().items.forEach((_id, item) -> {
      if (item.hasProperty(PropertyType.ContainsItemsProperty)) {
        potentialContainers.add(item);
      }
      if (item.hasProperty(PropertyType.Containable)) {
        potentialContained.add(item);
      }
    });

    ItemSelectUI select1 = new ItemSelectUI(potentialContainers, this.container, "No containers on the map!", editor);
    ItemSelectUI select2 = new ItemSelectUI(potentialContained, this.contained, "No containable items on the map!", editor);

    final class DoubleSelectOverride implements InputOverride {
      @Override
      public boolean handleInput(Input input, InputType type) {

        boolean handle1 = select1.getInputOverride().handleInput(input, type);
        boolean handle2 = select2.getInputOverride().handleInput(input, type);

        return handle1 || handle2;
      }

      @Override
      public Set<Input> getOverriddenInputs() {
        return select1.getInputOverride().getOverriddenInputs();
      }
    }

    InputOverride override = new DoubleSelectOverride();
    ControlsManager.pushOverride(override);

    final class ItemContainsItemUI extends VerticalGroup implements HandlesMenuClose {
      @Override
      public void handle() {
        ControlsManager.popOverride(override);
        select2.handle();
        select1.handle();
      }
    }


    ItemContainsItemUI ui = new ItemContainsItemUI();
    ui.space(20);
    ui.align(Align.center);

    ui.addActor(new G24Label("Container:", "bubble"));
    ui.addActor(select1);

    ui.addActor(new G24Label("Contained:", "bubble"));
    ui.addActor(select2);

    // Both of these UIs applied a view, discard them
    editor.clearGridView();
    editor.setGridView(GridView.compose(select1.getGridView(), select2.getGridView()));

    return Optional.of(ui);
  }
}
