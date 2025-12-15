package cs1396.escaperoom.game.entities.properties;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.game.entities.player.PlayerDetails;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.ui.widgets.G24NumberInput.IntInput;
import cs1396.escaperoom.ui.widgets.G24NumberInput.FloatInput;

public class PlayerProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Player",
    "The properties defining the player",
    "Allows the changing of the player's sprite, speed, and hitbox.",
    // incompatible with literally all of them (except cond vis)
    new HashSet<PropertyType>(Set.of(
      PropertyType.Animated,
      PropertyType.Barrier,
      PropertyType.Brushable,
      PropertyType.Collideable,
      PropertyType.Connector,
      PropertyType.ConnectorSource,
      PropertyType.ConnectorRelay,
      PropertyType.ConnectorSink,
      PropertyType.ConnectorBridge,
      PropertyType.CompletesLevel,
      PropertyType.ConditionallyActive,
      PropertyType.Containable,
      PropertyType.ContainsItemsProperty,
      PropertyType.CoveringProperty,
      PropertyType.Fragile,
      PropertyType.Interactable,
      PropertyType.LockedProperty,
      PropertyType.Obtainable,
      PropertyType.ReadableProperty,
      PropertyType.Spinable,
      PropertyType.Stylable,
      PropertyType.TiledBrushable,
      PropertyType.Toggleable,
      PropertyType.Unique,
      PropertyType.UnlocksProperty,
      PropertyType.Viewable
    ))
  );

  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  private PlayerDetails details = new PlayerDetails();

  public PlayerProperty() { }

  public PlayerDetails getDetails() {
    return details;
  }

	@Override
	public String getDisplayName() {
    return "Player Property";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Player;
	}

  @Override
  public void write(Json json) {
    details.write(json);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    details.read(json, jsonData);
    updateTexture();
  }

  @Override
  public void updateTexture() {
    owner.adjustTextureRegion(0, 0, details.textureInfo.frameWidth, details.textureInfo.frameHeight);
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();

    config.addElement("Hitbox", null, null, true);
    config.addNumberInput("Offset X", null, new FloatInput(details.hitboxInfo.xOffset, newVal -> {
      details.hitboxInfo.xOffset = newVal;
    }));
    config.addNumberInput("Offset Y", null, new FloatInput(details.hitboxInfo.yOffset, newVal -> {
      details.hitboxInfo.yOffset = newVal;
    }));
    config.addNumberInput("Width", null, new FloatInput(details.hitboxInfo.width, newVal -> {
      details.hitboxInfo.width = newVal;
    }));
    config.addNumberInput("Height", null, new FloatInput(details.hitboxInfo.height, newVal -> {
      details.hitboxInfo.height = newVal;
    }));

    config.addLine();
    config.addNumberInput("Speed", null, new FloatInput(details.speed, newVal -> {
      details.speed = newVal;
    }));

    config.addLine();
    config.addElement("Texture", null, null, true);
    config.addNumberInput("Idle Frames", null, new IntInput(details.textureInfo.idleFrames, newVal -> {
      details.textureInfo.idleFrames = newVal;
    }));
    config.addNumberInput("Idle Frame Delay (ms)", null, new IntInput(details.textureInfo.idleFrameDelayMS, newVal -> {
      details.textureInfo.idleFrameDelayMS = newVal;
    }));
    config.addNumberInput("Movement Frames", null, new IntInput(details.textureInfo.movementFrames, newVal -> {
      details.textureInfo.movementFrames = newVal;
    }));
    config.addNumberInput("Movement Frame Delay (ms)", null, new IntInput(details.textureInfo.moveFrameDelayMS, newVal -> {
      details.textureInfo.moveFrameDelayMS = newVal;
    }));
    config.addNumberInput("Frame Width", null, new IntInput(details.textureInfo.frameWidth, newVal -> {
      details.textureInfo.frameWidth = newVal;
    }));
    config.addNumberInput("Frame Height", null, new IntInput(details.textureInfo.frameHeight, newVal -> {
      details.textureInfo.frameHeight = newVal;
    }));

    return Optional.of(config);
  }
}
