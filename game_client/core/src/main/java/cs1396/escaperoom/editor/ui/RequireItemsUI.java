package cs1396.escaperoom.editor.ui;

import java.util.Collection;
import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import cs1396.escaperoom.editor.ui.ConfigurationMenu.HandlesMenuClose;
import cs1396.escaperoom.editor.ui.RequiredItemEntry.ItemRequired;
import cs1396.escaperoom.editor.ui.RequiredItemEntry.RequiredItem;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;

public class RequireItemsUI extends Container<ScrollPane> implements HandlesMenuClose {

  private LevelEditor editor;
  private HashMap<Integer, Item> canidates = new HashMap<>();
  private HashMap<Integer, RequiredItem> selected = new HashMap<>();

  private class RequiresItemsView implements GridView {

    @Override
    public void apply(Collection<Item> items) {

      for (Item i : items) {
        if (!canidates.containsKey(i.getID())) {
          i.setDimmed(true);
        }
      }

      for (Item i : canidates.values()) {
        if (selected.containsKey(i.getID())) {
          switch (selected.get(i.getID()).required) {
            case Either:
              i.setColor(Color.WHITE);
              break;
            case Forbidden:
              i.setColor(Color.RED);
              break;
            case Yes:
              i.setColor(Color.GREEN);
              break;
          }
        } else {
          i.setColor(new Color(1, 1, 1, 1));
        }
      }
    }

    @Override
    public void reset(Collection<Item> items) {
      for (Item i : items) {
        if (!canidates.containsKey(i.getID())) {
          i.setDimmed(false);
        }
        if (selected.containsKey(i.getID())) {
          i.setColor(new Color(1, 1, 1, 1));
        }
      }
    }
    @Override
    public ItemDecoration decorate(Item item) {
      ItemDecoration decoration = new ItemDecoration();

      if (!canidates.containsKey(item.getID())){
        decoration.set(ItemDecoration.DIMMED, true);
        return decoration;
      }


      if (selected.containsKey(item.getID())){
          Color c = Color.WHITE;
          switch (selected.get(item.getID()).required) {
            case Either:
              c = Color.YELLOW;
              break;
            case Forbidden:
              c = Color.RED;
              break;
            case Yes:
              c = Color.GREEN;
              break;
          }
          decoration.set(ItemDecoration.COLOR, c);

          return decoration;
      }

      return null;
    }
  }
  public RequireItemsUI(Array<Item> potentialItems, Array<RequiredItem> mutCurrentSelection, LevelEditor editor){
    super();
    this.editor = editor; 

    editor.setGridView(new RequiresItemsView());

    Table inner = new Table();
    inner.padLeft(20);

    inner.defaults().left().expandX().pad(2);
    for (Item potentialItem : potentialItems) {

      RequiredItemEntry entry = null;
      boolean found = false;
      canidates.put(potentialItem.getID(), potentialItem);

      for (RequiredItem selectedItem : mutCurrentSelection) {
        if (selectedItem.getItem().getID() == potentialItem.getID()) {
          found = true;
          entry = new RequiredItemEntry(selectedItem.getItem(), selectedItem.getRequired());
          selected.put(potentialItem.getID(), selectedItem);
          break;
        } 
      }

      if (!found){
        entry = new RequiredItemEntry(potentialItem, ItemRequired.Either);
      }
      entry.setOnChanged(RequiredItemEntry.onChangeHelper(mutCurrentSelection, selected));
      inner.add(entry.slot);
      inner.add(entry.label);
      inner.add(entry.checkBtn);
      inner.add(entry.forbidBtn);
      inner.row();
    }

    inner.pack();

    ScrollPane scroll = new ScrollPane(inner);
    scroll.setFadeScrollBars(false);
    scroll.setScrollBarPositions(false, false);
    scroll.setForceScroll(false, true);

    maxHeight(300);
    setActor(scroll);
    pack();
  }

  @Override
  public void handle() {
    editor.clearGridView();
  }
}
