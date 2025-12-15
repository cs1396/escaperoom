package cs1396.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;

import cs1396.escaperoom.engine.types.IntVector2;
import cs1396.escaperoom.game.entities.properties.PropertyType;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;
import cs1396.escaperoom.game.state.GameContext;
import cs1396.escaperoom.game.state.GameEvent;
import cs1396.escaperoom.game.state.GameEvent.EventType;

public class ConnectorSink extends Connector {

  private static final PropertyDescription description = new PropertyDescription(
    "Connector Sink",
    "Accepts signals",
    "Connector items can have different types, connector sinks receive signals from other connectors of their same types.",
    PropertyDescription.CONNECTOR_CONFLICTS
  );

  @Override 
  public PropertyDescription getDescription() {
    return description;
  }

	@Override
	public void propagate(GameContext ctx, HashSet<Integer> seen) {
    // Sinks do not propagate
	}

	@Override
	public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen) {
    if (source.getConnectorType() != this.type) return;
    if (seen.contains(owner.getID())) return;

    seen.add(owner.getID());

    boolean incomingSignal = source.isConnected();
    if (incomingSignal != this.connected){
      this.connected = incomingSignal;

      ctx.map.getEventBus().post(
        new GameEvent.Builder(connected ? EventType.ItemConnected : EventType.ItemDisconnected , ctx)
        .source(owner)
        .build()
      );
    }
    updateColor();
	}

	@Override
	public void setActive(boolean connected, GameContext ctx) {
    this.connected = connected;
	}

	@Override
	public String getDisplayName() {
    return "Connector Sink";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.ConnectorSink;
	}
}
