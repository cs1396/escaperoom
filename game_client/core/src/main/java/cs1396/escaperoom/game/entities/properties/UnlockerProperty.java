package cs1396.escaperoom.game.entities.properties;

import cs1396.escaperoom.game.entities.properties.base.PhantomProperty;
import cs1396.escaperoom.game.entities.properties.base.PropertyDescription;

public class UnlockerProperty extends PhantomProperty {

  private static final PropertyDescription description = new PropertyDescription(
    "Unlocker",
    "Unlocks locked items",
    "When in Player inventory, Unlocker items unlock Locked items with the key lock type.",
    null
  );
  @Override 
  public PropertyDescription getDescription(){
    return description;
  }

  /**
   * Empty constructor for {@code Json.Serializable} compatability 
   */
  public UnlockerProperty() {}

  @Override
  public String getDisplayName() {
    return "Unlocks Items";
  }

  @Override
  public PropertyType getType() {
    return PropertyType.UnlocksProperty;
  }
}
