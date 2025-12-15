package cs1396.escaperoom.editor.tools;

import java.util.Optional;

import com.badlogic.gdx.math.Vector2;

import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.TiledBrushable;
import cs1396.escaperoom.game.entities.properties.TiledBrushable.TileType;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.screens.LevelEditor;
import cs1396.escaperoom.screens.MapScreen;
import cs1396.escaperoom.engine.types.IntVector2;

public class TiledBrush extends ItemBrush {
  Vector2 lastTouchDown = new Vector2(0, 0);

  public boolean handleTouchDown(float x , float y) {
    IntVector2 newPos = new IntVector2((int) x, (int) y);
    lastTouchDown = newPos.asVector2();
    Optional<Item> matchingItem = matchingItemAt(newPos, newPos, editor, item);

    if (matchingItem.isPresent()) {
      path.add(newPos);
      return true;
    }

    Item newItem = item.clone();
    if (editor.canPlace(newItem, newPos)) {
      placeAt(newItem, newPos);
      updateTiles(newPos, editor, item, true);
      return true;
    }
    return false;
  }
  public void handleTouchUp(){
      path.clear();
  }
  public void handleDrag(float x, float y){
IntVector2 newPos = new IntVector2((int) x, (int) y);

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

      Optional<Item> matchingItem = matchingItemAt(newPos, newPos, editor, item);

      // don't place another item on top of a matching item
      if (matchingItem.isPresent()) {
        path.add(newPos);
        addedIDs.add(matchingItem.get().getID());
        return;
      }

      // in a new square!
      Item newItem = item.clone();
      if (editor.canPlace(newItem, newPos)) {
        placeAt(newItem, newPos);
        updateTiles(newPos, editor, item, true);
      }
  }

  public TiledBrush(LevelEditor editor, Item item) {
    super(editor, item);
  }

  public static void updateSurroundingTiles(IntVector2 origin, MapScreen map, Item item){
    for (int dx = -1; dx <= 1; dx++){
      for (int dy = -1; dy <= 1; dy++){
        origin.add(dx,dy);

        matchingItemAt(origin, origin, map, item).ifPresent((matching) -> {
          matching
            .getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent((tilable) -> {
              TiledBrush.updateTiles(origin, map, item, false);
            });
        });

        origin.sub(dx,dy);
      }
    }
  }

  public static void updateTiles(IntVector2 position, MapScreen map, Item item, boolean recurse) {
    matchingItemAt(position,position, map, item).ifPresent((matchingItem) -> {
      switch (getAdjancencies(position, map, item, recurse)) {
        // case of large block
        case 0b00001011:
        case 0b00001111:
        case 0b00101011:
        case 0b00101111:
        case 0b10101111:
        case 0b10001111:
        case 0b10101011:
        case 0b10001011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_TL));
          break;
        case 0b00011111:
        case 0b00111111:
        case 0b10011111:
        case 0b10111111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_T));
          break;
        case 0b00010110:
        case 0b00010111:
        case 0b10010110:
        case 0b10010111:
        case 0b10110111:
        case 0b00110111:
        case 0b10110110:
        case 0b00110110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_TR));
          break;
        case 0b01101011:
        case 0b11101011:
        case 0b01101111:
        case 0b11101111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_L));
          break;
        case 0b11111111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.CENTER));
          break;
        case 0b11010110:
        case 0b11010111:
        case 0b11110110:
        case 0b11110111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_R));
          break;
        case 0b01101000:
        case 0b01101001:
        case 0b11101000:
        case 0b11101001:
        case 0b11101101:
        case 0b01101101:
        case 0b11101100:
        case 0b01101100:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_BL));
          break;
        case 0b11111000:
        case 0b11111100:
        case 0b11111001:
        case 0b11111101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_B));
          break;
        case 0b11010000:
        case 0b11010100:
        case 0b11110000:
        case 0b11110100:
        case 0b11110101:
        case 0b11010101:
        case 0b11110001:
        case 0b11010001:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_BR));
          break;
        case 0b01111111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_NO_TL));
          break;
        case 0b11011111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_NO_TR));
          break;
        case 0b11111011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_NO_BL));
          break;
        case 0b11111110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.BLOCK_NO_BR));
          break;

        // case of some line
        case 0b00001000:
        case 0b00001001:
        case 0b00101000:
        case 0b00101001:
        case 0b10101000:
        case 0b00101100:
        case 0b10101001:
        case 0b00101101:
        case 0b10101101:
        case 0b00001101:
        case 0b10001100:
        case 0b00001100:
        case 0b10001000:
        case 0b10101100:
        case 0b10001101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_CAP_L));
          break;
        case 0b00010000:
        case 0b00010100:
        case 0b10010000:
        case 0b10010100:
        case 0b10110000:
        case 0b10010001:
        case 0b10110100:
        case 0b10010101:
        case 0b10110101:
        case 0b00010101:
        case 0b00110001:
        case 0b00010001:
        case 0b00110000:
        case 0b10110001:
        case 0b00110100:
        case 0b00110101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_CAP_R));
          break;
        case 0b00011000:
        case 0b10011100:
        case 0b10111100:
        case 0b10011101:
        case 0b10111101:
        case 0b00111001:
        case 0b00111101:
        case 0b10111001:
        case 0b00011001:
        case 0b00011100:
        case 0b10011000:
        case 0b00111000:
        case 0b10111000:
        case 0b00011101:
        case 0b00111100:
        case 0b10011001:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_HORI));
          break;
        case 0b00000010:
        case 0b00000110:
        case 0b00000011:
        case 0b00000111:
        case 0b00100011:
        case 0b10000011:
        case 0b00100111:
        case 0b10000110:
        case 0b10000111:
        case 0b10100111:
        case 0b10100010:
        case 0b00100010:
        case 0b10000010:
        case 0b00100110:
        case 0b10100011:
        case 0b10100110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_CAP_T));
          break;
        case 0b01000000:
        case 0b11000000:
        case 0b01100000:
        case 0b11100000:
        case 0b01100001:
        case 0b01100100:
        case 0b11100001:
        case 0b11000100:
        case 0b11100100:
        case 0b11100101:
        case 0b01000101:
        case 0b01000001:
        case 0b01000100:
        case 0b01100101:
        case 0b11000101:
        case 0b11000001:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_CAP_B));
          break;
        case 0b01000010:
        case 0b11100010:
        case 0b11100011:
        case 0b11100110:
        case 0b11100111:
        case 0b01000111:
        case 0b11000111:
        case 0b01100111:
        case 0b01100010:
        case 0b01000110:
        case 0b11000010:
        case 0b01000011:
        case 0b11000110:
        case 0b01100011:
        case 0b11000011:
        case 0b01100110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.LINE_VERT));
          break;

        case 0b01010010:
        case 0b01110011:
        case 0b01010011:
        case 0b01110010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
            .ifPresent(tp -> tp.setOrientation(TileType.T_L));
          break;
        case 0b11001110:
        case 0b01001010:
        case 0b11001010:
        case 0b01001110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.T_R));
          break;
        case 0b01011000:
        case 0b01011101:
        case 0b01011100:
        case 0b01011001:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.T_T));
          break;
        case 0b10111010:
        case 0b00011010:
        case 0b00111010:
        case 0b10011010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.T_B));
          break;
        case 0b01011010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.PLUS));
          break;

        case 0b01001000:
        case 0b01001100:
        case 0b01001001:
        case 0b11001000:
        case 0b11001001:
        case 0b11001101:
        case 0b11001100:
        case 0b01001101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_BL));
          break;
        case 0b00001010:
        case 0b10001010:
        case 0b00101010:
        case 0b00001110:
        case 0b00101110:
        case 0b10101110:
        case 0b10101010:
        case 0b10001110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_TL));
          break;
        case 0b00010010:
        case 0b00110010:
        case 0b10010010:
        case 0b00010011:
        case 0b10010011:
        case 0b10110011:
        case 0b00110011:
        case 0b10110010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_TR));
          break;
        case 0b01010000:
        case 0b01010001:
        case 0b01010100:
        case 0b01110000:
        case 0b01110100:
        case 0b01110101:
        case 0b01010101:
        case 0b01110001:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_BR));
          break;

        case 0b11010010:
        case 0b11110010:
        case 0b11010011:
        case 0b11110011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.RIGHT_CORNER_BL));
          break;
        case 0b01101010:
        case 0b11101010:
        case 0b01101110:
        case 0b11101110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.LEFT_CORNER_BR));
          break;
        case 0b01111000:
        case 0b01111001:
        case 0b01111100:
        case 0b01111101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.BOT_CORNER_TL));
          break;
        case 0b00011011:
        case 0b00111011:
        case 0b10011011:
        case 0b10111011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.TOP_CORNER_BL));
          break;
        case 0b01001011:
        case 0b01001111:
        case 0b11001011:
        case 0b11001111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.LEFT_CORNER_TR));
          break;
        case 0b01010110:
        case 0b01010111:
        case 0b01110110:
        case 0b01110111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.RIGHT_CORNER_TL));
          break;
        case 0b00011110:
        case 0b10011110:
        case 0b00111110:
        case 0b10111110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.TOP_CORNER_BR));
          break;
        case 0b11011000:
        case 0b11011100:
        case 0b11011001:
        case 0b11011101:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.BOT_CORNER_TR));
          break;

        case 0b11011010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_NOT_TL));
          break;
        case 0b01111010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_NOT_TR));
          break;
        case 0b01011011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_NOT_BR));
          break;
        case 0b01011110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_NOT_BL));
          break;

        case 0b11111010:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DB_B));
          break;
        case 0b01111011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DB_L));
          break;
        case 0b11011110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DB_R));
          break;
        case 0b01011111:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DB_T));
          break;

        case 0b01111110:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DIAG_BR));
          break;
        case 0b11011011:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CORNER_DIAG_TR));
          break;

        default:
          matchingItem.getProperty(PropertyType.TiledBrushable, TiledBrushable.class)
              .ifPresent(tp -> tp.setOrientation(TileType.CENTER));
          break;
      }
    });
  }

  public static int getAdjancencies(IntVector2 position, MapScreen screen, Item item, boolean recurse) {
    int adjancencies = 0;
    int checked = 0;

    IntVector2 pos;
    for (int yoff = 1; yoff >= -1; yoff--) {
      for (int xoff = -1; xoff <= 1; xoff++) {
        if (xoff == 0 && yoff == 0)
          continue;

        pos = position.cpy();
        pos.x += xoff;
        pos.y += yoff;

        if (matchingItemAt(pos, position, screen, item).isPresent()) {
          adjancencies = adjancencies | (1 << (7 - checked));
        }
        checked++;

        if (recurse)
          updateTiles(pos, screen, item, false);
      }
    }

    return adjancencies;
  }

  @Override
  public void cancel() {
    super.cancel();
    path.clear();
  }

  @Override
  public String getName() {
    return "TiledBrush for " + item.getItemName();
  }

  @Override
  public Optional<Item> draw(int x, int y) {
    IntVector2 newPos = new IntVector2( x, y);

    Item newItem = item.clone();
    if (editor.canPlace(newItem, newPos)) {
      newItem.setPosition(newPos);
      editor.placeItem(newItem);
      updateTiles(newPos, editor, item, true);
      return Optional.of(newItem);
    }
    return Optional.empty();
  }
}
