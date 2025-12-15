package cs1396.escaperoom.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.GridView;
import cs1396.escaperoom.editor.ui.ItemDecoration;
import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.engine.control.ControlsManager;
import cs1396.escaperoom.engine.control.ControlsManager.InputType;
import cs1396.escaperoom.engine.control.input.Input;
import cs1396.escaperoom.engine.control.input.InputOverride;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.AbstractScreen;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.ui.widgets.G24Label;

public class ItemSelectUI extends Table implements HandlesMenuClose {
  private Function<Item, Void> onSelect = (i) -> null;
  private Function<Item, Void> onDeselect = (i) -> null;
  private Array<SelectedItem> selected = new Array<>();
  private Array<Item> canidateItems = new Array<>();
  private LevelEditor editor = null;
  private boolean allowMultiple;
  private SelectInputOverride selectInputOverride = new SelectInputOverride();
  private HashMap<Integer, SelectableItemSlot> itemSlotMap = new HashMap<>();

  private class SelectionView implements GridView {
    public ItemDecoration decorate(Item item){
      ItemDecoration decoration = new ItemDecoration();

      if (selected.contains(new SelectedItem(item), false)){
        // Set green for selected
        decoration.set(ItemDecoration.COLOR, new Color(0.5f,1,0.5f,1));
        decoration.set(ItemDecoration.HIGHTLIGHT, true);
      } else {
        if (canidateItems.contains(item, false)){
          decoration.set(ItemDecoration.COLOR, new Color(1,1,1,1));
          decoration.set(ItemDecoration.HIGHTLIGHT, true);
        } else {
          decoration.set(ItemDecoration.DIMMED, true);
        }
      }

      return decoration;
    }
  }

  private class SelectInputOverride implements InputOverride {
    private final HashSet<Input> overriddenInputs = new HashSet<>(Set.of(Input.SELECT, Input.SELECT_MULTI));
    /**
     * We always want to handle the select
     */
    private boolean handleSelect(int x, int y){
      Optional<Item> maybeTarget = editor.priorityItemAt(x, y);
      if (maybeTarget.isEmpty()) return true;

      Item target = maybeTarget.get();

      if (selected.contains(new SelectedItem(target), false)){
        deselectItem(target);
      } else if (canidateItems.contains(target, false)){
        selectItem(target);
      }

      return true;
    }

    private boolean handleSelectMulti(int x, int y){
      Optional<Item> maybeTarget = editor.priorityItemAt(x, y);
      if (maybeTarget.isEmpty()) return true;

      Item target = maybeTarget.get();

      if (selected.contains(new SelectedItem(target), false)){
        deselectItem(target);
      } else if (canidateItems.contains(target, false)){
        selectItem(target);
      }

      return true;
    }

    @Override
    public boolean handleInput(Input input, InputType type) {
      if (type != InputType.PRESSED || !editor.shouldHandleTouchDown()) return false;

      Vector2 screenCoords = new Vector2(Gdx.input.getX(), Gdx.input.getY());
      IntVector2 gameCoords = IntVector2.fromVector2(editor.screenToStageCoordinates(screenCoords));

      switch(input){
        case SELECT: 
          return handleSelect(gameCoords.x, gameCoords.y); 
        case SELECT_MULTI: 
          return handleSelectMulti(gameCoords.x, gameCoords.y);
        default: 
          return false;
      }
    }

    @Override
    public Set<Input> getOverriddenInputs() {
      return overriddenInputs;
    }
  }

  public static class SelectedItem {
    Item i;
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof SelectedItem){
        Item i = SelectedItem.class.cast(obj).i;
        return i != null && this.i != null && i.getID() == this.i.getID();
      }
      return false;
    }

    public SelectedItem(Item i) {
      this.i = i;
    }

    public SelectedItem() {
      this.i = null;
    }

    public void setItem(Item i) {
      this.i = i;
    }

    public Item getItem() {
      return i;
    }

    public SelectedItem clone() {
      return new SelectedItem(this.i);
    }
  }

  public void setOnSelect(Function<Item, Void> onSelect){
    this.onSelect = onSelect;
  }

  public void setOnDeselect(Function<Item, Void> onDeselect){
    this.onDeselect = onDeselect;
  }

  private void deselectItem(Item item){
    if (allowMultiple) {
      selected.removeValue(new SelectedItem(item), false);
    } else {
      if (selected.first().getItem() == null || selected.first().getItem().getID() == item.getID()){
        selected.first().setItem(null);
      }
    }

    item.setSelected(false);
    itemSlotMap.get(item.getID()).setSelected(false);

    onDeselect.apply(item);
  }

  private void selectItem(Item item){
    if (allowMultiple) {
      selected.add(new SelectedItem(item));
    } else {
      selected.first().setItem(item);

      for (int itemID : itemSlotMap.keySet()) {
        if (itemID == item.getID()) continue;

        itemSlotMap.get(itemID).inner.slotValues.ifPresent((i) -> deselectItem(i.item));
      }
    }

    item.setSelected(true);
    itemSlotMap.get(item.getID()).setSelected(true);
    onSelect.apply(item);
  }

  /**
   * Provide a UI that will update the passed {@code selection}
   * from the {@code potentialItems}
   *
   * @param allowMultiple whether or not to allow multiple items to be selected
   * @param mutSelections a mutable array of the selected items
   * @param emptyMessage the message to display on the ui when there are no {@code potentialItems}
   */
  public ItemSelectUI(
    Array<Item> potentialItems,
    String emptyMessage,
    Array<SelectedItem> mutSelections,
    boolean allowMultiple,
    AbstractScreen screen) {

    // Layout
    pad(10);
    
    // Field initialziation
    this.canidateItems = potentialItems;
    this.selected = mutSelections;
    this.editor = (LevelEditor)screen;
    this.allowMultiple = allowMultiple;

    if (canidateItems.isEmpty()){
      add(new G24Label(emptyMessage));
      return;
    }

    // Set a selection view;
    editor.setGridView(new SelectionView());

    // override our selection input
    ControlsManager.pushOverride(selectInputOverride);

    int count = 0;

    for (Item i : canidateItems) {

      // Create a row every 4 items
      if (count % 4 == 0) {
        row();
      }
      count++;

      SelectableItemSlot selectableSlot = new SelectableItemSlot(i);
      itemSlotMap.put(i.getID(), selectableSlot);

      // Callbacks for when the user clicks this slot
      selectableSlot.setOnSelect((item) -> {
        selectItem(item);
        return null;
      });

      selectableSlot.setOnDeselect((item) -> {
        deselectItem(item);
        return null;
      });

      // Check to see if this slot should start selected
      for (SelectedItem si : selected) {
        if (si.getItem() != null && (i.getID() == si.getItem().getID())) {
          selectableSlot.setSelected(true);
        } 
      }

      // add the slot to the table
      add(selectableSlot);
    }
  }

  public GridView getGridView(){
    return new SelectionView();
  }

  public InputOverride getInputOverride(){
    return selectInputOverride;
  }

  /**
   * Provide a UI that will update the passed {@code selection}
   * from the {@code potentialItems}
   *
   * @param emptyMessage the message to be displayed to the user should there be no potential items
   */
  public ItemSelectUI(Array<Item> potentialItems, SelectedItem selection, String emptyMessage, LevelEditor editor) {
    this(potentialItems, emptyMessage, Array.with(selection), false, editor);
  }

  @Override
  public void handle() {
    editor.clearGridView();
    ControlsManager.popOverride(selectInputOverride);
    selected.forEach((si) -> {if (si.i != null) si.i.setSelected(false); });
  }
}
