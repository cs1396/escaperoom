package cs1396.escaperoom.editor.tools;

import java.util.Optional;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.core.ToolManager.ToolType;
import cs1396.escaperoom.editor.tools.DeletionTool.Deletion;
import cs1396.escaperoom.engine.BackManager;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.state.Pollable;
import cs1396.escaperoom.screens.LevelEditor;

public class ItemSelectionTool extends SelectionTool implements Pollable {


  public ItemSelectionTool(Stage uiStage, LevelEditor editor) {
    super(editor);
  }

  /**
   * Get the selected objects held in this tool
   *
   */
  @Override
  public Optional<Selection> getSelection() {
    if (selection == null){
      return Optional.empty();
    }
    if (selection.getItems().isEmpty()){
      return Optional.empty();
    }
    return Optional.of(selection);
  }

  public void setItems(Array<Item> items){
    if (selection == null){
      selection = new Selection(new Rectangle(), new Array<>(), editor);
    }
    selection.setItems(items);
  }


  @Override
  public void startSelection(float x, float y) {
      super.startSelection(x, y);
      editor.addPollable(this);
  }

  @Override
  public void endSelection() {
    selecting = false;
    if (getSelection().isEmpty()){
      editor.removeOverlayDrawable(this);
    } else {
      selection.minimizeArea();
    }

    BackManager.addBack(() -> {
      if (getSelection().isEmpty()){ 
        return false;
      };

      editor.removeOverlayDrawable(this);
      selection.clearSelectedItems();
      selection = null;
      return true;
    });
  }

  public void deleteSelected(){
    getSelection().ifPresent((selection) -> {
      Array<Item> items = selection.getItems();
      Item[] itemsArr = new Item[items.size];
      for (int i = 0; i < items.size; i++) {
        Item item = items.get(i);
        item.remove();
        itemsArr[i] = item;
      }
      editor.recordEditorAction(new Deletion(editor, itemsArr));
      selection.clearSelectedItems();
    });
    editor.removeOverlayDrawable(this);
  }


  private void selectItems() {
    for (Item item : editor.getItemsIn(selection.area)) {
      item.setSelected(true);
      selection.addItem(item);
    }
  }

  @Override
  public String getButtonStyle() {
    return "selectButton";
  }

  @Override
  public ToolType getType() {
    return ToolType.ItemSelect;
  }

  @Override
  public boolean poll(float delta){
    if (selecting){
      selection.clearSelectedItems();
      this.selectItems();
      return true;
    }
    return false;
  }
}
