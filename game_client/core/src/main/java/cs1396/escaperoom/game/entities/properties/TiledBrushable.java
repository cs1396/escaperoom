package cs1396.escaperoom.game.entities.properties;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.tools.TiledBrush;
import cs1396.escaperoom.engine.types.Size;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.util.PropertyMap;
import cs1396.escaperoom.screens.MapScreen;

public class TiledBrushable extends Brushable {
  private static final PropertyDescription description = new PropertyDescription(
    "Tiled Brushable",
    "Texture is a tile map",
    "This item can be used as a brush in the level editor. \n The backing texture must be a tile map. See official documentation for tile map specification.",
    PropertyDescription.TEXTURE_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  TileType inner = TileType.BLOCK_BL;
  int rotation = 0;

  public enum TileType {
    BLOCK_TL,
    BLOCK_T,
    BLOCK_TR,
    BLOCK_L,
    CENTER,
    BLOCK_R,
    BLOCK_BL,
    BLOCK_B,
    BLOCK_BR,

    SOLO,

    BLOCK_NO_TL,
    BLOCK_NO_TR,
    BLOCK_NO_BL,
    BLOCK_NO_BR,

    LINE_CAP_L,
    LINE_CAP_R,
    LINE_HORI,

    LINE_CAP_T,
    LINE_CAP_B,
    LINE_VERT,

    T_L,
    T_R,
    T_T,
    T_B,
    PLUS,

    CORNER_TL,
    CORNER_TR,
    CORNER_BL,
    CORNER_BR,

    BOT_CORNER_TL,
    BOT_CORNER_TR,
    TOP_CORNER_BL,
    TOP_CORNER_BR,
    LEFT_CORNER_BR,
    LEFT_CORNER_TR,
    RIGHT_CORNER_BL,
    RIGHT_CORNER_TL,

    CORNER_NOT_TL,
    CORNER_NOT_TR,
    CORNER_NOT_BL,
    CORNER_NOT_BR,

    CORNER_DB_L,
    CORNER_DB_R,
    CORNER_DB_T,
    CORNER_DB_B,

    CORNER_DIAG_TR,
    CORNER_DIAG_BR,
    ;

    public Size getOffset() {
      switch (this) {
        case BLOCK_TL:
          return new Size(0, 16);
        case BLOCK_T:
          return new Size(16, 16);
        case BLOCK_TR:
          return new Size(32, 16);
        case BLOCK_L:
          return new Size(0, 32);
        case CENTER:
          return new Size(16, 32);
        case BLOCK_R:
          return new Size(32, 32);
        case BLOCK_BL:
          return new Size(0, 48);
        case BLOCK_B:
          return new Size(16, 48);
        case BLOCK_BR:
          return new Size(32, 48);
        case BLOCK_NO_TL:
          return new Size(80, 48);
        case BLOCK_NO_TR:
          return new Size(64, 48);
        case BLOCK_NO_BL:
          return new Size(80, 32);
        case BLOCK_NO_BR:
          return new Size(64, 32);
        case LINE_CAP_L:
          return new Size(128, 32);
        case LINE_CAP_R:
          return new Size(144, 48);
        case LINE_HORI:
          return new Size(48, 48);
        case LINE_CAP_T:
          return new Size(144, 32);
        case LINE_CAP_B:
          return new Size(128, 48);
        case LINE_VERT:
          return new Size(48, 32);
        case T_L:
          return new Size(80, 16);
        case T_R:
          return new Size(64, 0);
        case T_T:
          return new Size(64, 16);
        case T_B:
          return new Size(80, 0);
        case PLUS:
          return new Size(48, 16);
        case CORNER_TL:
          return new Size(96, 32);
        case CORNER_TR:
          return new Size(112, 32);
        case CORNER_BL:
          return new Size(96, 48);
        case CORNER_BR:
          return new Size(112, 48);
        case BOT_CORNER_TL:
          return new Size(144, 16);
        case BOT_CORNER_TR:
          return new Size(96, 16);
        case TOP_CORNER_BL:
          return new Size(112, 0);
        case TOP_CORNER_BR:
          return new Size(128, 0);
        case LEFT_CORNER_BR:
          return new Size(96, 0);
        case LEFT_CORNER_TR:
          return new Size(128, 16);
        case RIGHT_CORNER_BL:
          return new Size(144, 0);
        case RIGHT_CORNER_TL:
          return new Size(112, 16);
        case CORNER_NOT_TL:
          return new Size(48, 0);
        case CORNER_NOT_TR:
          return new Size(32, 0);
        case CORNER_NOT_BL:
          return new Size(16, 0);
        case CORNER_NOT_BR:
          return new Size(0, 0);
        case CORNER_DB_L:
          return new Size(160, 16);
        case CORNER_DB_R:
          return new Size(160, 0);
        case CORNER_DB_T:
          return new Size(160, 32);
        case CORNER_DB_B:
          return new Size(160, 48);
        case CORNER_DIAG_TR:
          return new Size(176, 0);
        case CORNER_DIAG_BR:
          return new Size(176, 16);
        case SOLO:
          return new Size(176, 32);
      }

      throw new IllegalStateException();
    }
  }

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public TiledBrushable() { }

  public void refreshAdjacency(MapScreen map) {
    TiledBrush.updateTiles(owner.getPosition(), map, owner, true);
  }

  public TiledBrushable(Item item, TileType orientation, int rotation) {
    this.owner = item;
    this.inner = orientation;
    this.rotation = rotation;
    this.owner.setRotation(rotation);
    updateTexture();
  }

  public TiledBrushable(Item item) {
    this(item, TileType.CENTER, 0);
  }

  public TileType getOrientation() {
    return inner;
  }

  public void setOrientation(TileType orientation) {
    inner = orientation;
    updateTexture();
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
    owner.setRotation(rotation);
  }

  @Override
  public String getDisplayName() {
    return "Tiled Brushable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.TiledBrushable;
  }

  @Override
  public void write(Json json) {
    json.writeValue("orientation", inner.name());
    json.writeValue("rotation", rotation);
  }

  @Override
  public void updateTexture() {
    Size offset = inner.getOffset();
    owner.adjustTextureRegion(offset.width, offset.height, 16, 16);
  }

  @Override
  public void read(Json json, JsonValue data) {
    inner = TileType.valueOf(data.getString("orientation", TileType.BLOCK_BL.name()));
    rotation = data.getInt("rotation", 0);
    Size offset = inner.getOffset();
    this.owner.setRotation(rotation);
    PropertyMap.onMapCompletion.add((Void) -> {
      this.owner.adjustTextureRegion(offset.width, offset.height, 16, 16);
      return null;
    });
  }
}
