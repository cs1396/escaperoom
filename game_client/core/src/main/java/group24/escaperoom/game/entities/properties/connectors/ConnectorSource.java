package group24.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import group24.escaperoom.editor.ui.PropertyConfiguration;
import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.state.GameContext;

public class ConnectorSource extends Connector {
  boolean alwaysOn = true;

  private static final PropertyDescription description = new PropertyDescription(
    "Connector Source",
    "Creates signals",
    "Connector sources propagates signals to other connectors with their same type.\nConnector sources do not accept signals! If combined with Toggleable, this source will only propagate signals when it is toggled on.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription() {
    return description;
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ConnectorSource;
  }

  @Override
  public String getDisplayName() {
    return "ConnectorSource";
  }

  @Override
  public void propagate(GameContext ctx, HashSet<Integer> seen) {
    if (seen.contains(owner.getID()))
      return;
    seen.add(owner.getID());

    IntVector2 position = owner.getPosition();
    IntVector2 pos;

    for (int yoff = 1; yoff >= -1; yoff--) {
      for (int xoff = -1; xoff <= 1; xoff++) {
        if (Math.abs(xoff) == Math.abs(yoff))
          continue;

        pos = position.cpy();
        pos.x += xoff;
        pos.y += yoff;

        Utils.connectableAt(pos, ctx.map, type).ifPresent((ci) -> {
          ci.connectable.acceptSignalFrom(this, position, ctx, seen);
        });
      }
    }
  }

  @Override
  public void setActive(boolean connected, GameContext ctx) {
    if (connected != this.connected) {
      this.connected = connected;
      propagate(ctx, new HashSet<>());
    }
    if (alwaysOn){
      this.connected = true;
      updateColor();
    }
  }

  @Override
  public void acceptSignalFrom(Connectable source, IntVector2 position, GameContext ctx, HashSet<Integer> seen) {
    // Sources do not accept signals
  }

  @Override
  public void write(Json json) {
    super.write(json);
    json.writeValue("connected", alwaysOn);
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = super.getCustomItemConfigurationMenu().get();

    config.addToggle(
      "Always on",
      "Whether or not this source always propagates a positive signal",
      alwaysOn,
      (isToggled) -> alwaysOn = isToggled
    );

    return Optional.of(config);
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    super.read(json, jsonData);
    this.alwaysOn = jsonData.getBoolean("connected", false);
    if(alwaysOn){
      this.connected = true;
      updateColor();
    }
  }
}
