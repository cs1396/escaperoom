package group24.escaperoom.editor.tools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.utils.Array;

import group24.escaperoom.editor.core.DragManager.PlacementAction;
import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.game.entities.Item;
import group24.escaperoom.game.entities.properties.connectors.Connectable;
import group24.escaperoom.game.entities.properties.connectors.ConnectableItem;
import group24.escaperoom.game.entities.properties.connectors.Utils;
import group24.escaperoom.screens.LevelEditor;
import group24.escaperoom.screens.MapScreen;

public class ItemBrush extends Brush {
  Item item;
  Array<IntVector2> path = new Array<>();
  HashSet<Integer> addedIDs = new HashSet<>();

  public ItemBrush(LevelEditor stage, Item item) {
    super(stage);
    this.item = item;
  }

  @Override
  public void handleTouchUp(){
    path.clear();
  }

  public void setItem(Item item){
    this.item = item;
  }

  @Override
  public void handleDrag(float x, float y){
    IntVector2 newPos = new IntVector2( x,  y);

    if (newPos.equals(path.get(path.size - 1))) {
      return;
    }

    // in a new square!
    //
    // check if we backtracked
    if (path.size >= 2 && newPos.equals(path.get(path.size - 2))) {
      placedItemAt(path.get(path.size - 1)).ifPresent((i) -> {
        i.remove(false);
        editor.recordEditorAction(new DeletionTool.Deletion(editor, i));
      });
      path.pop();
      return;
    }

    // don't place another item on top of a matching item
    Optional<Item> matchingItem = matchingItemAt(newPos, path.get(path.size - 1), editor, item);
    if (matchingItem.isPresent()){
      addedIDs.add(matchingItem.get().getID());
      path.add(newPos);
      return;
    }

    // we didn't backtrack!

    Item newItem = item.clone();
    if (editor.canPlace(newItem, newPos)) {
      placeAt(newItem, newPos);
    }
  }

  @Override
  public boolean handleTouchDown(float x, float y){
    IntVector2 newPos = new IntVector2(x, y);
    Optional<Item> matchingItem = matchingItemAt(newPos, newPos, editor, item);

    // matching item, "fake path"
    if (matchingItem.isPresent()) {
      path.add(newPos);
      return true;
    }

    Item newItem = item.clone();

    if (editor.canPlace(newItem, newPos)) {
      placeAt(newItem, newPos);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Given some position, a {@link MapScreen} and an item to match against,
   * return an optional of a matching item at that position.
   */
  static protected Optional<Item> matchingItemAt(IntVector2 checkPos, IntVector2 matchToPos, MapScreen editor,
      Item itemToMatch) {

    Optional<ConnectableItem> maybeConnectable = Utils.isConnectable(itemToMatch);

    // if the item is connectable, match on the ConnectorType
    if (maybeConnectable.isPresent()) {

      return editor.getItemsAt(checkPos.x, checkPos.y).flatMap((canidates) -> {

        Connectable itemToMatchConnectable = maybeConnectable.get().connectable;

        for (int i = 0; i < canidates.length; i++) {

          Item canidate = canidates[i];

          Optional<ConnectableItem> maybeConnectableCanidate = Utils.isConnectable(canidate)
              .filter((ci) -> ci.connectable.getConnectorType() == itemToMatchConnectable.getConnectorType());

          if (maybeConnectableCanidate.isPresent()){
            if (matchToPos.equals(checkPos)){
              return Optional.of(canidate);
            }

            ConnectableItem connectableItem = maybeConnectableCanidate.get();

            IntVector2 offset = matchToPos.cpy().sub(checkPos);

            if (Arrays.stream(connectableItem.connectable.connectionDirections()).anyMatch((v) -> v.equals(offset))){
              return Optional.of(canidate);
            }

          }
        }
        return Optional.empty();
      });

    } else {

      // Match on the item name
      return editor.getItemsAt(checkPos.x, checkPos.y).flatMap((items) -> {
        // check if we have an item of the same type
        for (int i = 0; i < items.length; i++) {
          if (items[i].getItemName().equals(itemToMatch.getItemName())) {
            return Optional.of(items[i]);
          }
        }
        return Optional.empty();
      });
    }
  }

  protected Optional<Item> placedItemAt(IntVector2 pos) {
    return editor.getItemsAt(pos.x, pos.y).flatMap((items) -> {

      // check if we have an item of the same type
      for (int i = 0; i < items.length; i++) {
        if (addedIDs.contains(items[i].getID())) {
          return Optional.of(items[i]);
        }
      }
      return Optional.empty();

    });
  }

  protected void placeAt(Item newItem, IntVector2 newPosition) {
    newItem.setPosition(newPosition);
    addedIDs.add(newItem.getID());
    path.add(newPosition);
    editor.placeItem(newItem);
    editor.recordEditorAction(new PlacementAction(newItem, editor));
  }

  @Override
  public String getName() {
    return item.getItemName() + " Brush";
  }

  @Override
  public Optional<Item> draw(int x, int y) {
    IntVector2 newPos = new IntVector2(x,  y);
    Item newItem = item.clone();
    if (editor.canPlace(newItem, newPos)) {
      newItem.setPosition(newPos);
      editor.placeItem(newItem);
      return Optional.of(newItem);
    }
    return Optional.empty();
  }

  @Override
  public boolean isDrawing() {
    return path.size > 0;
  }

  @Override
  public Item getItem() {
    return item;
  }
}
