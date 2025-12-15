package cs1396.escaperoom.game.entities.properties;

import java.util.Optional;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.util.PropertyMap;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;

public class SpinnableProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Spinable",
    "Changes texture on rotation",
    "Spinable items have a texture that is 4 times as tall as the height of the item. This allows the texture of the item to change when it rotates. Ensure that any provided texture is four times as tall as it is normally would be for a nonspinable item",
    PropertyDescription.TEXTURE_CONFLICTS
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  private int width;
  private int height;
  private int spinCount = 0;
  private boolean symmetric = true;
  private int offset;

  public static final int PIXELS_PER_WORLD_UNIT = 16;
 
  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public SpinnableProperty() {};

  /**
   */
  public SpinnableProperty(Item owner, int pixelWidth, int pixelHeight, int spinCount) {
    this.owner = owner;
    this.width = pixelWidth;
    this.height = pixelHeight;
    this.spinCount = 0;
    this.symmetric = (width == height);
    this.offset = Math.max(width, height);
    setSpin(spinCount);
  }


  public void updateSize() {
    int temp = owner.itemSize.width;
    owner.itemSize.width = owner.itemSize.height;
    owner.itemSize.height = temp;
    owner.flip();
  }

  public void setSpin(int spinCount) {
    this.spinCount = (spinCount % 4);
    updateTexture();
    updateSize();
  }

  public int getSpinCount() {
    return spinCount;
  }

  public static int degreesToSpinCount(float degrees) {
    return (int) (degrees % 360) / 90;
  }

  @Override
  public String getDisplayName() {
    return "Spin";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Spinable;
  }

  @Override
  public SpinnableProperty cloneProperty(Item newOwner) {
    return new SpinnableProperty(newOwner, width, height, spinCount);
  }

  @Override
  public void updateTexture() {
    if (symmetric) {
      owner.adjustTextureRegion(0, offset * spinCount, width, height);
    } else {
      if (spinCount % 2 == 0) {
        owner.adjustTextureRegion(0, offset * spinCount, width, height);
      } else {
        owner.adjustTextureRegion(0, offset * spinCount, height, width);
      }
    }
  }

  private void updateDependentFields(){
    this.symmetric = (width == height);
    this.offset = Math.max(width, height);
    this.spinCount = 0;
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();
    config.addNumberInput(
      "Pixel Width",
      "The pixel width of this item", 
      new IntInput(
        width, 
        (i) -> {
          width = i;
          updateDependentFields();
          setSpin(0);
        }
      )
    );

    config.addNumberInput(
      "Pixel Height",
      "The pixel height of this item\n(the texure's height should be 4x this)", 
      new IntInput(
        height, 
        (i) -> {
          height = i;
          updateDependentFields();
          setSpin(0);
        }
      )
    );

    return Optional.of(config);
  }

  @Override
  public void write(Json json) {
    json.writeValue("spin_count", spinCount);
    json.writeValue("pixel_width", width);
    json.writeValue("pixel_height", height);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    spinCount = jsonData.getInt("spin_count", 0);
    width = jsonData.getInt("pixel_width", owner.getWidth() * PIXELS_PER_WORLD_UNIT);
    height = jsonData.getInt("pixel_height", owner.getHeight() * PIXELS_PER_WORLD_UNIT);
    symmetric = (width == height);
    offset = Math.max(width, height);

    PropertyMap.onMapCompletion.add((Void) -> {
      updateTexture();
      if (owner.flipped){
        updateSize();
      }
      return null;
    });
  }

}
