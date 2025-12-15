package cs1396.escaperoom.game.entities.properties;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.ui.widgets.G24NumberInput.FloatInput;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

/**
 * Collidable items block the player movement. This can be configured to be an area that is not equal to the items actual size.
 */
public class Collideable extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Collidable",
    "Blocks player movement",
    "Collidable items block the player movement. This can be configured to be an area that is not equal to the items actual size.",
    new HashSet<>(Set.of(PropertyType.Obtainable, PropertyType.Containable))
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  float dx, dy, width, height;
  boolean wholeItem;

  /**
   * Empty constructor for {@link Json.Serializable} compatability
   * constructor
   */
  public Collideable(){}

  /**
   * @param item owner
   * @param dx offset from the bottom left corner where the blocking region begins
   * @param dy offset from the bottom left corner where the blocking region begins
   * @param width of the blocking region
   * @param height of the blocking region
   */
  public Collideable(Item item, float dx, float dy, float width, float height) {
    owner = item;

    if (dx == 0 && dy == 0 && width == item.getWidth() && height == item.getHeight()) {
      wholeItem = true;
      item.setBlocksPlayer(true);
    } else {
      this.dx = dx;
      this.dy = dy;
      this.width = width;
      this.height = height;
      item.setBlocksPlayer(dx, dy, width, height);
    }
  }

  /**
   * Convience constructor for when the owner's whole region blocks the player
   *
   * @param item owner
   */
  public Collideable(Item item) {
    owner = item;
    wholeItem = true;
    item.setBlocksPlayer(true);
  }

  @Override
  public String getDisplayName() {
    return "Collideable";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Collideable;
  }

  @Override
  public Collideable cloneProperty(Item newOwner){
    if (wholeItem){
      return new Collideable(newOwner);
    } else {
      return new Collideable(newOwner, dx, dy, width, height);
    }
  }

  @Override
  public void write(Json json) {
    json.writeValue("xoffset", dx);
    json.writeValue("yoffset", dy);
    json.writeValue("width", width);
    json.writeValue("height", height);
    json.writeValue("whole_item", wholeItem);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    dx = jsonData.getFloat("xoffset", 0);
    dy = jsonData.getFloat("yoffset", 0);
    width = jsonData.getFloat("width", owner.getWidth());
    height = jsonData.getFloat("height", owner.getHeight());
    wholeItem = jsonData.getBoolean("whole_item", false);

    updateBlockingRegion();
  }

  private void updateBlockingRegion(){
    if (dx == 0 && dy == 0 && width == owner.getWidth() && height == owner.getHeight()) {
      wholeItem = true;
    }

    if (wholeItem){
      owner.setBlocksPlayer(true);
    } else {
      owner.setBlocksPlayer(dx, dy, width, height);
    }   
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();

    config.addNumberInput(
      "Offset X",
      "Number of tiles in the positive X direction from the bottom left corner where the blocking region starts.",
      new FloatInput(dx, (newVal) -> {
        dx = newVal;
        updateBlockingRegion();
      })
    );

    config.addNumberInput(
      "Offset Y",
      "Number of tiles in the positive Y direction from the bottom left corner where the blocking region starts.",
      new FloatInput(dy, (newVal) -> {
        dy = newVal;
        updateBlockingRegion();
      })
    );

    config.addNumberInput(
      "Width",
      "Width of the blocking region in tiles",
      new FloatInput(width, (newVal) -> {
        width = newVal;
        updateBlockingRegion();
      })
    );

    config.addNumberInput(
      "Height",
      "Height of the blocking region in tiles",
      new FloatInput(height, (newVal) -> {
        height = newVal;
        updateBlockingRegion();
      })
    );

    return Optional.of(config);
  }

}
