package cs1396.escaperoom.game.entities.properties;

import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

/**
 * This item can be used as a brush in the level editor
 */
public class Brushable extends PhantomProperty  {

  private static final PropertyDescription description = new PropertyDescription(
    "Brushable",
    "Can be used as a brush in the editor",
    "This item can be used as a brush in the level editor",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

	@Override
	public String getDisplayName() {
    return "Brushable";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Brushable;
	}
}
