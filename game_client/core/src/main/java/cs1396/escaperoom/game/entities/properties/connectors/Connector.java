package cs1396.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;
import java.util.Optional;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import cs1396.escaperoom.editor.ui.PropertyConfiguration;
import cs1396.escaperoom.editor.ui.PropertyConfiguration.Select;
import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.state.GameContext;

public class Connector extends PhantomProperty implements Connectable {
  private static final PropertyDescription description = new PropertyDescription(
    "Connector",
    "Transmits signals",
    "Connector items can have different types, and propagate signals to other connectors of their same types.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription() {
    return description;
  }

  protected ConnectorType type = ConnectorType.Power;
  protected boolean connected;
  private Color connectedColor = new Color(0.75f, 1, 0.75f, 1f);
  private Color disconnectedColor = new Color(1, 0.75f, 0.75f, 1f);

  public enum ConnectorType {
    Power,
    Magic,
    Juice,
  }

  public ConnectorType getConnectorType() {
    return type;
  }

  @Override
  public void propagate(GameContext ctx, HashSet<Integer> seen) {
    if (seen.contains(owner.getID())) return;
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

        Utils.connectableAt(pos, ctx.map, type)
            .ifPresent(ci -> ci.connectable.acceptSignalFrom(this, position, ctx, seen));
      }
    }
  }

  @Override
  public void setActive(boolean connected, GameContext ctx) {

    this.connected = connected;
    propagate(ctx, new HashSet<>());
  }

  public boolean isConnected() {
    return connected;
  }

  @Override
  public String getDisplayName() {
    return "Connector";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.Connector;
  }

  @Override
  public void write(Json json) {
    json.writeValue("type", type.name());
  }

  @Override
  public void read(Json json, JsonValue jsonData) {
    this.type = ConnectorType.valueOf(jsonData.getString("type"));
  }

  protected void updateColor() {
    if (connected) {
      owner.setColor(connectedColor);
    } else {
      owner.setColor(disconnectedColor);
    }
  }

  @Override
  public void acceptSignalFrom(Connectable source, IntVector2 position, GameContext ctx, HashSet<Integer> seen) {
    if (source.getConnectorType() != this.type)
      return;
    this.connected = source.isConnected();
    updateColor();
    propagate(ctx, seen); // continues flow
  }

  @Override
  public Optional<PropertyConfiguration> getCustomItemConfigurationMenu() {
    PropertyConfiguration config = new PropertyConfiguration();

    config.addSelect(
      "Connector Type",
      "",
      new Select<ConnectorType>(
        (val) -> {this.type = (ConnectorType) val;}, 
        (val) -> {},
       ConnectorType.values(),
        (val) -> ((ConnectorType)val).name(),
        1,
        type
      )
    );

    return Optional.of(config);
  }
}
