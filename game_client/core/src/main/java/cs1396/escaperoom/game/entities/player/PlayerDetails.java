package cs1396.escaperoom.game.entities.player;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public class PlayerDetails implements Serializable {
  public static class HitboxInfo {
    public float xOffset,
                 yOffset,
                 width,
                 height;

    public HitboxInfo(float xOffset, float yOffset, float width, float height) {
      this.xOffset = xOffset;
      this.yOffset = yOffset;
      this.width = width;
      this.height = height;
    }
  }
  public static class TextureInfo {
    public int idleFrames,
               idleFrameDelayMS,
               movementFrames,
               moveFrameDelayMS,
               frameWidth,
               frameHeight;

    public TextureInfo(int idleFrames, int idleFrameDelayMS, int movementFrames, int moveFrameDelayMS, int frameWidth, int frameHeight) {
      this.idleFrames = idleFrames;
      this.idleFrameDelayMS = idleFrameDelayMS;
      this.movementFrames = movementFrames;
      this.moveFrameDelayMS = moveFrameDelayMS;
      this.frameWidth = frameWidth;
      this.frameHeight = frameHeight;
    }
  }

  public HitboxInfo hitboxInfo = new HitboxInfo(0, 0, 32, 32);
  public float speed = 1;
  public TextureInfo textureInfo = new TextureInfo(0, 1000, 1, 100, 32, 32);

  @Override
  public void write(Json json) {
    json.writeObjectStart("hitbox");
    json.writeValue("x_offset", hitboxInfo.xOffset);
    json.writeValue("y_offset", hitboxInfo.yOffset);
    json.writeValue("width", hitboxInfo.width);
    json.writeValue("height", hitboxInfo.height);
    json.writeObjectEnd();

    json.writeValue("speed", speed);

    json.writeObjectStart("texture");
    json.writeValue("idle_frames", textureInfo.idleFrames);
    json.writeValue("idle_frame_delay_ms", textureInfo.idleFrameDelayMS);
    json.writeValue("move_frames", textureInfo.movementFrames);
    json.writeValue("move_frame_delay_ms", textureInfo.moveFrameDelayMS);
    json.writeValue("frame_width", textureInfo.frameWidth);
    json.writeValue("frame_height", textureInfo.frameHeight);
    json.writeObjectEnd();
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    JsonValue hitbox = jsonData.get("hitbox");
    JsonValue texture = jsonData.get("texture");

    speed = jsonData.getFloat("speed", 5);

    if (hitbox != null) {
      hitboxInfo = new HitboxInfo(
        hitbox.getFloat("x_offset", 0.25f),
        hitbox.getFloat("y_offset", 0f),
        hitbox.getFloat("width", 1.5f),
        hitbox.getFloat("height", 1.5f)
      );
    } else {
      hitboxInfo = new HitboxInfo(0.25f, 0f, 1.5f, 1.5f);
    }

    if (texture != null) {
      textureInfo = new TextureInfo(
        texture.getInt("idle_frames", 1),
        texture.getInt("idle_frame_delay_ms", 1000),
        texture.getInt("move_frames", 4),
        texture.getInt("move_frame_delay_ms", 100),
        texture.getInt("frame_width", 32),
        texture.getInt("frame_height", 32)
      );
    } else {
      textureInfo = new TextureInfo(1, 1000, 4, 100, 32, 32);
    }
  }

  /**
   * Empty constructor for {@link Json.Serializable} compatability 
   */
  public PlayerDetails() { }
}

