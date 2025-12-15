package cs1396.escaperoom.game.entities.properties;


import java.util.Optional;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.game.entities.Item;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.entities.properties.util.PropertyMap;
import cs1396.escaperoom.ui.notifications.Notifier;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;

/**
 * Animated items have a texture that is `N` item widths wide, where N is the
 * number of frames in the animation and width is in pixels. 
 */
public class AnimatedProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Animated",
    "Change texture on fixed periods",
    "Animated items have a texture that is `N` item widths wide, where N is the number of frames in the animation and width is in pixels. Configure these parameters in the configuration menu",
    PropertyDescription.TEXTURE_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }


  private int numFrames;
  private int textureOffsetPerFrame;
  private float frameLength;
  private int width;
  private int height;
  private float lastUpdate = 0;

  /**
   * Constant used for default frame guessing
   */
  public static final int PIXELS_PER_WORLD_UNIT = 16;

  private int frameN = 0;

  /**
   * @param owner the owner of this property
   * @param numFrames is the number of frames available in the texture of the item
   * @param frameLength is the time in seconds that each frame should play for
   * @param frameWidth is the width, in pixels, of an individual frame
   * @param frameHeight is the height, in pixels, of an individual frame 
   */
  public AnimatedProperty(Item owner, int numFrames, float frameLength, int frameWidth, int frameHeight){
    init(owner, numFrames, frameLength, frameWidth, frameHeight);
  }

  private void init(Item owner, int numFrames, float frameLength, int frameWidth, int frameHeight){
    this.owner = owner;
    this.numFrames = numFrames;
    this.textureOffsetPerFrame = frameWidth;
    this.frameLength = frameLength;
    this.width = frameWidth;
    this.height = frameHeight;
    this.owner.adjustTextureRegion(0, 0, width, height);
  }

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public AnimatedProperty(){}

  /**
   * Maybe advance the animation frame
   *
   * @param delta the amount of game time passed since the last call
   */
  public void maybeAdvance(float delta){
    if (delta + lastUpdate > frameLength){
      lastUpdate = 0;
      frameN += 1;
      frameN = frameN % numFrames;
      updateTexture();
    } else {
      lastUpdate += delta;
    }
  }

	@Override
	public String getDisplayName() {
    return "Animated Property";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Animated;
	}

	@Override
	public AnimatedProperty cloneProperty(Item newOwner) {
    return new AnimatedProperty(newOwner, numFrames, frameLength, width, height);
	}

  @Override
  public void updateTexture() {
    owner.adjustTextureRegion(frameN * this.textureOffsetPerFrame, 0, this.width, this.height);
  }

  @Override
  public void write(Json json) {
    json.writeValue("frame_count", numFrames);
    json.writeValue("frame_length", frameLength);
    json.writeValue("frame_width", width);
    json.writeValue("frame_height", height);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    try {
      numFrames = jsonData.getInt("frame_count");
      frameLength = jsonData.getFloat("frame_length");
      width = jsonData.getInt("frame_width", owner.getWidth() * AnimatedProperty.PIXELS_PER_WORLD_UNIT);
      height = jsonData.getInt("frame_height", owner.getHeight() * AnimatedProperty.PIXELS_PER_WORLD_UNIT);
    } catch (IllegalArgumentException argE) {
      Notifier.error("Attempted to load Animated property without required\n'frame_count' and 'frame_length' property values");
      numFrames = 1;
      frameLength = 1000;
      width = owner.getHeight() * AnimatedProperty.PIXELS_PER_WORLD_UNIT;
      height = owner.getHeight() * AnimatedProperty.PIXELS_PER_WORLD_UNIT;
      return;
    }
    textureOffsetPerFrame = width;
    PropertyMap.onMapCompletion.add((Void) -> {
      owner.adjustTextureRegion(0, 0, width, height);
      return null;
    });
  }


  @Override
  public void apply(Item item){
    super.apply(item);
    defaultConfiguration(item);
  }

  @Override
  public void defaultConfiguration(Item owner) {
    super.defaultConfiguration(owner);
    AtlasRegion region = owner.getTexture();
    int numFrames = Math.max(1,region.getRegionWidth() / (owner.getWidth() * PIXELS_PER_WORLD_UNIT));
    float frameLength = 0.5f;
    int frameWidth = Math.max(PIXELS_PER_WORLD_UNIT, owner.getWidth() * PIXELS_PER_WORLD_UNIT);
    int frameHeight = Math.max(PIXELS_PER_WORLD_UNIT, region.getRegionHeight());
    init(owner, numFrames, frameLength, frameWidth, frameHeight);
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();
    config.addNumberInput(
      "Number of frames",
      null,
      new IntInput(numFrames, (newVal) -> {
        this.numFrames = newVal;
      })
    );

    config.addNumberInput(
      "Frame length (ms)",
      null,
      new IntInput(MathUtils.floor(frameLength * 1000), (newVal) -> {
        this.frameLength = newVal / 1000.0f;
      })
    );

    config.addNumberInput(
      "Frame width (px)",
      null,
      new IntInput(width, (newVal) -> {
        this.width = newVal;
        textureOffsetPerFrame = width;
        this.owner.adjustTextureRegion(0, 0, width, height);
      })
    );

    config.addNumberInput(
      "Frame height (px)",
      null,
      new IntInput(height, (newVal) -> {
        this.height = newVal;
        this.owner.adjustTextureRegion(0, 0, width, height);
      })
    );

    return Optional.of(config);
  }
}
