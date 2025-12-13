package group24.escaperoom.game.entities.properties.connectors;

import java.util.HashSet;

import group24.escaperoom.engine.types.IntVector2;
import group24.escaperoom.game.entities.properties.connectors.Connector.ConnectorType;
import group24.escaperoom.game.state.GameContext;

public interface Connectable {

  public static final IntVector2[] defaultConnectionDirections = { 
      new IntVector2(-1, 1), new IntVector2(0, 1), new IntVector2(1, 1), 
      new IntVector2(-1, 0),  new IntVector2(1, 0),
      new IntVector2(-1, -1), new IntVector2(0, -1), new IntVector2(1, -1), 
  };

  /**
   * Potentially propagate the the connectors current siganl
   * 
   * @param seen is IDs of all {@code Connectable} items which have already
   *             propagated
   */
  public void propagate(GameContext ctx, HashSet<Integer> seen);


  /**
   * By default, connectable items connect visually to 8 directions. 
   *
   *  ^   ^   ^
   *  ^   me  ^ 
   *  ^   ^   ^
   *
   *  Override this method to change visual connection directions
   */
  default public IntVector2[] connectionDirections(){
    return defaultConnectionDirections;
  }

  /**
   * Called when this {@code Connectable} receives a signal from another.
   * 
   * @param source the Connectable sending the signal
   * @param pos    the source's position
   * @param seen   A set of already visited IDs
   */
  public void acceptSignalFrom(Connectable source, IntVector2 pos, GameContext ctx, HashSet<Integer> seen);

  /**
   * Set the {@code Connectable} to be {@code connected}
   *
   * This may or may not propagate the signal
   */
  public void setActive(boolean connected, GameContext ctx);

  /**
   * Get the type of connector.
   */
  public ConnectorType getConnectorType();

  /**
   * Get whether or not this Connectable is connected (active)
   */
  public boolean isConnected();
}
