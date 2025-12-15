package cs1396.escaperoom.game.entities.properties;

import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

/**
 * Barrier items block player movement and provide the boundary of what the player can see through fog of war.
 */
public class Barrier extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Barrier",
    "Blocks the advance of fog of war",
    "Barrier items block player movement and provide the boundary of what the player can see through fog of war.",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }


	@Override
	public String getDisplayName() {
    return "Barrier";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Barrier;
	}
}
