package cs1396.escaperoom.game.entities.properties;

import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

public class Unique extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Unique",
    "Only one allowed on the map",
    "Unique items only have a single instance allowed on the map",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

	@Override
	public String getDisplayName() {
    return "Unique";
	}

	@Override
	public PropertyType getType() {
    return PropertyType.Unique;
	}
}
