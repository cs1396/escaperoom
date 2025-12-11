package group24.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;

import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.game.entities.properties.PropertyType;
import group24.escaperoom.game.entities.properties.base.PropertyDescription;
import group24.escaperoom.game.state.GameContext;

public class ConnectorBridge extends Connector {
  private static final PropertyDescription description = new PropertyDescription(
    "Connector Bridge",
    "Propagates signals",
    "Connector items can have different types, and propagate signals to other connectors of their same types.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override
  public PropertyDescription getDescription() {
      return description;
  }


  IntVector2 output(int rotation) {
    switch (rotation) {
      case 0:
        return new IntVector2(3, 0);
      case 90:
        return new IntVector2(0, 3);
      case 180:
        return new IntVector2(-1, 0);
      case 270:
        return new IntVector2(0, -1);
    }
    return null;

  }

  IntVector2 input(int rotation) {
    switch (rotation) {
      case 0:
        return new IntVector2(-1, 0);
      case 90:
        return new IntVector2(0, -1);
      case 180:
        return new IntVector2(3, 0);
      case 270:
        return new IntVector2(0, 3);
    }
    return null;
  }

  @Override
  public IntVector2[] connectionDirections() {
    return new IntVector2[0];
  }

  @Override
  public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen) {

    IntVector2 position = owner.getPosition();

    IntVector2 input = input((int)owner.getRotation());
    IntVector2 cpy = position.cpy();

    cpy.x += input.x;
    cpy.y += input.y;

    if (cpy.equals(pos)) {
      connected = source.isConnected();
      updateColor();
      propagate(ctx, seen);
      return;
    }
  }

  public void propagate(GameContext ctx, HashSet<Integer> seen) {
    if (seen.contains(owner.getID())) return;

    seen.add(owner.getID());

    IntVector2 position = owner.getPosition();
    IntVector2 pos;
    IntVector2 output = output((int)owner.getRotation());
    pos = position.cpy();

    pos.y += output.y;
    pos.x += output.x;

    Utils.connectableAt(pos, ctx.map, type)
      .ifPresent(ci -> ci.connectable.acceptSignalFrom(this, position, ctx, seen));
  }

  @Override
  public PropertyType getType() {
    return PropertyType.ConnectorBridge;
  }

}
